(ns clj-discord.playing)

(def current-status (atom nil))



(def original-guild-create [])

(defn save-the-guild-create [type data])
;;populate the current-status

(defn update-presences [type data])
;;takeout the change, update current-status as needed



(defn get-players-playing [game])
;;return all player playing that game



