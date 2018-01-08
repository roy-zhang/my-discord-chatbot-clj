(ns clj-discord.core
    (:gen-class)
    (:require [clj-http.client :as http]
      [clojure.data.json :as json]
      [gniazdo.core :as ws]
      [clj-discord.eliza :as eliza]))

(defonce the-token (atom nil))
(defonce the-gateway (atom nil))
(defonce the-socket (atom nil))
(defonce the-heartbeat-interval (atom nil))
(defonce the-keepalive (atom false))
(defonce the-seq (atom nil))
(defonce reconnect-needed (atom false))

(def base-url "https://discordapp.com/api/")

(defn disconnect []
      (reset! reconnect-needed false)
      (reset! the-keepalive false)
      (if (not (nil? @the-socket)) (ws/close @the-socket))
      (reset! the-token nil)
      (reset! the-gateway nil)
      (reset! the-socket nil)
      (reset! the-seq nil)
      (reset! the-heartbeat-interval nil))

(defn connect [token functions log-events]
      (disconnect)
      (reset! the-keepalive true)
      (reset! the-token (str "Bot " token))
      (reset! the-gateway (str
                            (get
                              (json/read-str
                                (:body (http/get (str base-url "gateway")
                                                 {:headers {:authorization @the-token}})))
                              "url")
                            "?v=6&encoding=json"))
      (reset! the-socket
              (ws/connect
                @the-gateway
                :on-receive #(let [received (json/read-str %)
                                   logevent (if log-events (println "\n" %))
                                   op (get received "op")
                                   type (get received "t")
                                   data (get received "d")
                                   seq (get received "s")]
                                  (if (= 10 op) (reset! the-heartbeat-interval (get data "heartbeat_interval")))
                                  (if (not (nil? seq)) (reset! the-seq seq))
                                  (if (not (nil? type)) (doseq [afunction (get functions type (get functions "ALL_OTHER" []))] (afunction type data))))))

      ;;starts the responder, which disables repl input unfortunately
      (.start (Thread. (fn []
                           (try
                             (while @the-keepalive
                                    (if (nil? @the-heartbeat-interval)
                                      (Thread/sleep 100)
                                      (do
                                        (if log-events (println "\nSending heartbeat " @the-seq))
                                        (ws/send-msg @the-socket (json/write-str {:op 1, :d @the-seq}))
                                        (Thread/sleep @the-heartbeat-interval))))

                             (catch Exception e (do
                                                  (println "\nCaught exception: " (.getMessage e))
                                                  (reset! reconnect-needed true)))))))

      (Thread/sleep 1000)
      (ws/send-msg @the-socket (json/write-str {:op 2, :d {"token" @the-token
                                                           "properties" {"$os" "linux"
                                                                         "$browser" "clj-discord"
                                                                         "$device" "clj-discord"
                                                                         "$referrer" ""
                                                                         "$referring_domain" ""}
                                                           "compress" false}}))
      (while (not @reconnect-needed) (Thread/sleep 1000))
      (connect token functions log-events))

(defn post-message [channel_id message]
      (http/post (str base-url "channels/" channel_id "/messages")
                 {:body (json/write-str {:content message
                                         :nonce (str (System/currentTimeMillis))
                                         :tts false})
                  :headers {:authorization @the-token}
                  :content-type :json
                  :accept :json}))

(defn get-user [id]
      (json/read-str
        (:body (http/get
                 (str base-url "users/" id)
                 {:headers {:authorization @the-token}}))))

(defn post-message-with-mention [channel_id message user_id]
      (println "====" channel_id)
      (post-message channel_id (str "<@" user_id ">" message)))

(defn answer-command [data command answer]
      (if (= command (get data "content"))
        (post-message-with-mention
          (get data "channel_id")
          (str " " answer)
          (get (get data "author") "id"))))

(def token1 "MzI3ODU1NTQxNzI2NDc4MzM2")
(def token2 "DC7nkw")
(def token3 "PGCADk34PBRKe2vzLIMgb3mj3H8")

(def token (str token1 . token2 . token3))

(defn d100 [type data]
      (answer-command data "!d100" (str "rand num 1 to 100: " (+ (rand-int 100) 1))))

(def last-data (atom nil))
(defn log-event [type data]
      (reset! last-data data)
      (println "\nReceived: " type " -> " data))

(defn get-channel-messages [channel-id]
      (json/read-str
        (:body (http/get
                 (str base-url "channels/" channel-id "/messages")
                 {:headers {:authorization @the-token}}))))

(def pa-guild-id "178505837805830144")
(def bot-user-id "327855541726478336")

(def bot-channel-id "327903378195742721")
(def pa-channel-id "178505837805830144")



(defn get-guild [guild-id]
      (json/read-str
        (:body (http/get
                 (str base-url "guilds/" guild-id "/members")
                 {:headers {:authorization @the-token}}))))

(defn compile-status [guild])


;; track who is currently talking to you
(defonce talking-to (atom {}))
(defn add-to-talking [user-id channel-id]
      (println (str "adding user " user-id))
      (swap! talking-to conj [user-id channel-id]))
(defn remove-from-talking [user-id]
      (println (str "removing user " user-id))
      (swap! talking-to dissoc user-id))

(defn check-start [type data]
      (when (= "start talking to me" (get data "content"))
            (let [author (get (get data "author") "id")
                  channel (get data "channel_id")]
                 (add-to-talking author channel) ()
                 (post-message-with-mention channel (str " " "Hello") author))))

(defn check-stop [type data]
      (when (= "stop talking to me" (get data "content"))
            (let [author (get (get data "author") "id")
                  channel (get data "channel_id")]
                 (remove-from-talking author)
                 (post-message-with-mention channel (str " Cya") author))))

(defn feed-to-eliza [type data]
      (let [author  (get (get data "author") "id")
            content (get data "content")
            channel (get data "channel_id")]
           (when (and (contains? @talking-to author) (= channel (@talking-to author)))
                 (post-message-with-mention channel (str " " (eliza/answer content)) author))))


(defn -main [& args]
      (connect token
               {"MESSAGE_CREATE" [d100 check-stop feed-to-eliza check-start]
                "MESSAGE_UPDATE" [d100]}
               ;  "GUILD_CREATE"   [save-the-guild-create]
               ;  "PRESENCE_UPDATE" [update-presences]}
               ;; "ALL_OTHER" [log-event]}

               true))