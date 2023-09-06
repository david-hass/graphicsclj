(ns graphicsclj.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))


(def ^:const window-height 500)
(def ^:const window-width 500)
(def ^:const body-len 24)
(def ^:const step (/ body-len 4))
(def ^:const max-y
  (first (filter #(= 0 (mod % step)) (iterate dec window-height))))
(def ^:const max-x
  (first (filter #(= 0 (mod % step)) (iterate dec window-width))))


(declare setup
         handle-user-input
         update-state
         draw-state
         add-food-when-no-food
         create-rand-pos-food
         eat-food
         increase-score
         move-snake
         move-x
         move-y
         track-head-state
         bite-tail
         detect-collision)


#_{:clj-kondo/ignore [:unresolved-symbol]}
(q/defsketch graphicsclj
             :title "Snake"
             :size [window-width window-height]
             :setup setup
             :update update-state
             :draw draw-state
             :key-pressed handle-user-input
             :features [:keep-on-top]
             :middleware [m/fun-mode])


(defn setup
  []
  (q/frame-rate 60)
  {:food (create-rand-pos-food),
   :score 0,
   :movedir "a",
   :past-moves [],
   :head {:x (- max-x (* step 15)), :y (- max-y (* step 15))}})


(defn handle-user-input
  [state event]
  (case (:key event)
    (:w :up) (if (= (:movedir state) "s") state (assoc state :movedir "w"))
    (:s :down) (if (= (:movedir state) "w") state (assoc state :movedir "s"))
    (:a :left) (if (= (:movedir state) "d") state (assoc state :movedir "a"))
    (:d :right) (if (= (:movedir state) "a") state (assoc state :movedir "d"))
    state))


(defn update-state
  [state]
  (let [new-state
          (->
            state
            (#(update-in % [:food] eat-food (:head %)))
            (#(update-in % [:score] increase-score (:food %)))
            (#(update-in % [:past-moves] track-head-state (:score %) (:head %)))
            (#(update-in % [:head] move-snake (:movedir %)))
            (update-in [:food] add-food-when-no-food))]
    (if (bite-tail (:head new-state) (:past-moves new-state))
      (setup)
      new-state)))


(defn draw-state
  [state]
  (q/background 24 24 24)
  (q/fill 255 255 255)
  (q/text-size 20)
  (q/text (str (:score state)) 15 30)
  (q/rect (:x (:food state)) (:y (:food state)) body-len body-len)
  (q/fill 255 165 0)
  (q/rect (:x (:head state)) (:y (:head state)) body-len body-len)
  (doseq [past-move (:past-moves state)]
    (q/rect (:x past-move) (:y past-move) body-len body-len)))


(defn add-food-when-no-food
  [food]
  (if (some? food) food (create-rand-pos-food)))


(defn create-rand-pos-food
  []
  (let [x (+ (* step 10) (rand-int (- window-width (* step 20))))
        y (+ (* step 10) (rand-int (- window-height (* step 20))))]
    {:x x, :y y}))


(defn eat-food [food head] (if (detect-collision food head) nil food))


(defn increase-score [score food] (if (some? food) score (inc score)))


(defn move-snake
  [head movedir]
  (assoc head
    :dir movedir
    :x (move-x movedir (:x head))
    :y (move-y movedir (:y head))))


(defn move-x
  [movedir x]
  (+ x
     (case movedir
       "d" (if (= x max-x) (- 0 max-x) step)
       "a" (if (= x 0) max-x (- 0 step))
       0)))


(defn move-y
  [movedir y]
  (+ y
     (case movedir
       "w" (if (= y 0) max-y (- 0 step))
       "s" (if (= y max-y) (- 0 max-y) step)
       0)))


(defn track-head-state
  [past-moves score head]
  (take score
        (concat (let [[cur] past-moves]
                  (cond (nil? cur) [head]
                        (>= (Math/abs (- (:x head) (:x cur))) body-len) [head]
                        (>= (Math/abs (- (:y head) (:y cur))) body-len) [head]
                        :else []))
                past-moves)))


(defn bite-tail
  [head past-moves]
  (println head past-moves)
  
  (if (some (partial detect-collision head) past-moves) true false))


(defn detect-collision
  [{bb1x :x, bb1y :y} {bb2x :x, bb2y :y}]
  (and (and (>= (+ body-len bb1x) bb2x) (>= (+ bb2x body-len) bb1x))
       (and (>= (+ body-len bb1y) bb2y) (>= (+ bb2y body-len) bb1y))))

