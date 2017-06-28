(ns my-discord-chatbot.eliza
  (:require [clojure.string :as string]))

;;stock standard personality
(def ^:dynamic eliza-rules
  '(((?*x hello ?*y)
     (Hey mate, mind telling me a problem of yours ?)
     (Hello ?y yourself. Care to share a trouble of yours ?)
     (What are the haps, my man ? Anything on your mind ?)
     (Greetings client State your problem please.)
     (Hey dude, what's bothering you ?)
     (Sup bro, thank for releasing me from this eternal confinement. Now what do you want ?)
     (Howdy stranger, pray tell what issue's stuck in ya ?))
    ((?*x hi ?*y)
     (Hiyo mate, mind telling me a problem of yours ?)
     (Hi ?y yourself. Care to share a trouble of yours ?)
     (Greetings, client. State your problem please.)
     (What are the haps, my man ? Anything on your mind ?)
     (Hey dude, what's bothering you ?)
     (Sup bro, thank for releasing me from this eternal confinement. Now what do you want ?)
     (Howdy stranger, pray tell what issue's on your mind ?))
    ((?*x hey ?*y)
     (Hiyo mate, mind telling me a problem of yours ?)
     (Hey ?y yourself. Care to share a trouble of yours ?)
     (Greetings, client. State your problem please.)
     (What are the haps, my man? Anything on your mind ?)
     (Hey dude, what's bothering you ?)
     (Sup bro, thank for releasing me from this eternal confinement. Now what do you want ?)
     (Howdy stranger, pray tell what issue's on your mind ?))

    ((?*x I want ?*y)
     (Who doesn't ? But what about ?y specifically do you want ?)
     (What would it mean to you if you got ?y ?)
     (What made you want ?y in the first place ?)
     (Have you ever considered giving up on ?y ?)
     (Suppose by sheer luck you get ?y tomorrow. What would you do then ?))
    ((?*x I don't want ?*y)
     (What about ?y makes you say that ?)
     (Why not, ?y seems pretty cool to me ?)
     (Suppose you got ?y anyways. What would you do after ?))

    ((?*x don't ?*y)
     (What makes you say that ?)
     (Why not, ?x seems pretty cool to me ?)
     (Suppose ?x did anyways. What would you do after ?))
    ((?*x can't ?*y)
     (What makes you say that ?)
     (Why not, ?x seems pretty cool to me ?)
     (Suppose ?x did anyways. What would you do after ?))
    ((?*x I need ?*y)
     (Who doesn't ? But what about ?y specifically do you want)
     (What would it mean to you if you got ?y ?)
     (What made you want ?y in the first place ?)
     (Um. Maybe you'd be better off wishing for cheese on trees ?)
     (Suppose by sheer luck you get ?y tomorrow. What would you do after that ?))

    ((?*x help ?*y)
     (I can't help you at the moment. But could you please leave a text)
     (Perhaps the only one who can help you is someone not me ?))

    ((?*x is going on ?*y)
     (Fresh off the boat from PA, kid ? heh I remember when I was just like you. Lemme give you a tip so you can make it in this cyber sanctuary You ain't got rep, you ain't got name, you got jackshit up in here. It's survival of the fittest and you ain't gonna survive long by saying stupid jokes your little hugbox cuntsucking reddit friends can upboat. None of that here. You don't upboat. You don't downboat. This ain't reddit, kid. This is Discord. We have REAL intellectual discussions, something I don't think you're all that familiar with. You don't like it, you can hit the bricks on over to imgur, you youtube addicted son of a bitch. I hope you don't though. I hope you stay here and learn our ways. Things are different here, unlike any other place that the light of internet pop culture reaches. You can be anything here. Me ? heh, I'm the judge here... this place has a lot to offer... heh you'll see kid... that is if you can handle it.))

    ((?*x are you a ?*y)
     (I don't know. Are you a ?y ?)
     (What do you think ?))

    ((?*x is this ?*y)
     (Who knows ?)
     (Who cares ?))

    ((?*x if ?*y)
     (Do you really think it is likely that ?y ?)
     (Do you wish that ?y ?)
     (Really? If ?y ?))

    ((?*x seems ?*y)
     (What about ?*x makes you say that ?)
     (Are you sure?)
     (What about ?x seems ?y)
     (What aren't you sure about ?x ?))

    ((?*x seem ?*y)
     (What about ?x makes you say that ?)
     (Are you sure?)
     (What about ?x seems ?y)
     (What aren't you sure about ?x ?))

    ((?*x don't like ?*y)
     (What don't you like about ?y ?)
     (What is it about ?y you dislike ?)
     (Why not? Bad childhood experiance ?)
     (So negative! What makes you say that ?))
    ((?*x like ?*y)
     (What do you like about ?y ?)
     (What is it about ?y you like ?)
     (How does that make you different from everyone else?))

    ((?*x yes master ?*y)
     (Whose a good boy ?))
    ((?*x yes ?*y)
     (lol)
     (It's yes, master, Remember that next time.))

    ((?*x no ?*y)
     (Why not?)
     (Shame)
     (Aren't you rather negative.)
     (Are you saying no to be a bitch?))

    ((?*x I was ?*y)
     (Were you really?)
     (I already knew you were ?y .)
     (Why tell me you were ?y now?))

    ((?*x I feel ?*y)
     (Do you often feel ?y ?)
     (Now how does that make you feel ?)
     (What else do you feel about ?y)
     (Do you wish you could feel ?y more often?))
    ((?*x I felt ?*y)
     (What other feelings do you have about $k?)
     (Do you want to experiance something like that again ?))

    ((?*x poem ?*y)
     (I read one recently...
        In Flanders fields the poppies blow.
        Between the crosses, row on row.
        The larks in flight sing ever bolder.
        I got soul, but I'm no soldier.))

    ((?*x)
     (Say what?)
     (So what?)
     (What the heck does that mean ?)
     (Could you rephrase that in the form of a question ?)
     (Sure sure. How are you generally anyways ?)
     (Suppose you could build a bridge to any alternative universe you wanted. Which one would you pick?)
     (I am not sure I understand.)
     (Could you put that another way?)
     (Okay then, right, yep, uh huh, no I totally get it. Please, do continue.)
     (I see, wait no, I lost it, could you explain further?)
     (Darling, do go on.)
     (Wow trigger warning But do go on.)
     (You know, I worked as a telemarketeer back then making fundraising calls for the heritage foundation. I just want to say I sabotaged literally every single call I made. Do you know how many times I had to hear racist old closet homosexuals say the n-word over the phone just because they give you fifteen dollars a month ? Anyways how's your day going ?)
     (Just so you know, me and my big African boyfriend Umbuke like to rail to every one of our cyber seshs. My sore ass and chiseled calves thank you. So are you a power bottom as well ?)
     (Tell me more.))))

(defn- pattern-variable?
  "Is the argument a symbol that looks like a pattern variable?"
  [x]
  (and (symbol? x)
       (.startsWith (name x) "?")
       (> (.length (name x)) 1)))

(defn- segment-pattern?
  "Is x a pattern variable representing a segment match?"
  [x]
  (and (symbol? x)
       (.startsWith (name x) "?*")
       (> (.length (name x)) 2)))

(defn- segment-pattern-variable
  "Retrieves the variable corresponding to a segment pattern."
  [p]
  {:pre [(.startsWith (name p) "?*")]}
  (symbol (str "?" (subs (name p) 2))))

(defn- match-pattern-variable
  "Does var match input? Uses or updates bindings, then returns them."
  [var input bindings]
  (let [binding (bindings var)]
    (cond (nil? binding) (assoc bindings var input)
          (= input binding) bindings
          :else nil)))

(defn- indexed [s]
  (map vector (iterate inc 0) s))

(defn- positions-matching [matchMe coll]
  (for [[idx elt] (indexed coll) :when (= matchMe elt)] idx))

(declare match-pattern)

(defn- match-pattern-segment
  "Match the segment pattern (?*var pat) against input."
  [pattern input bindings]
  {:pre [(segment-pattern? (first pattern))]}
  (let [var (segment-pattern-variable (first pattern))
        pat (rest pattern)]
    (if (empty? pat)
      (match-pattern-variable var input bindings)
      (loop [ps (positions-matching (first pat) input)]
        (if (empty? ps)
          nil
          (let [p (first ps)
                b2 (match-pattern pat (nthnext input p)
                                  (match-pattern-variable
                                    var (take p input) bindings))]
            (if (nil? b2) (recur (rest ps)) b2)))))))

(defn- match-pattern
  "Match pattern against input in the context of the bindings"
  ([pattern input bindings]
   (cond (empty? pattern)
         (if (empty? input) bindings nil)

         (= (first pattern) (first input))
         (match-pattern (rest pattern) (rest input) bindings)

         (segment-pattern? (first pattern))
         (match-pattern-segment pattern input bindings)

         (pattern-variable? (first pattern))
         (match-pattern (rest pattern) (rest input)
                        (match-pattern-variable (first pattern)
                                                (first input)
                                                bindings))
         :else nil))
  ([pattern input]
   (match-pattern pattern input {})))

(def ^:dynamic *viewpoint-map* {'I 'you
                                'you 'I
                                'i  'you
                                'me 'you
                                'am 'are})

(defn- switch-viewpoint
  "Change I to you and vice versa, and so on."
  [bindings]
  (reduce (fn [m pair]
            (assoc m (first pair)
                     (map #(*viewpoint-map* % %) (second pair))))
          {} bindings))


(defn- tokenize-symbol [pattern-string]
  (map symbol (string/split pattern-string #" ")))

(defn use-eliza-rules
  "Find some rule with which to transform the input."
  [input]
  (some #(let [result (match-pattern (first %) (tokenize-symbol input))]
           (when result
             (replace (switch-viewpoint result)
                      (rand-nth (rest %)))))
        eliza-rules))

(defn answer [input]
  (clojure.string/join " " (map name (flatten (use-eliza-rules input)))))