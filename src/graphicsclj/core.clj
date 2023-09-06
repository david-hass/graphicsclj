(ns graphicsclj.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))


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
         detect-collision)

(def ^:const window-height 500)
(def ^:const window-width 500)
(def ^:const body-height 12)
(def ^:const body-width 12)
(def ^:const step 3)
(def ^:const max-y
  (first (filter #(= 0 (mod % step)) (iterate dec window-height))))
(def ^:const max-x
  (first (filter #(= 0 (mod % step)) (iterate dec window-width))))



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
  {:food nil,
   :score 0,
   :movedir "a",
   :past-moves [],
   :snake-head {:x (- max-x (* step 15)), :y (- max-y (* step 15))}})


(defn handle-user-input
  [state event]
  (case (:key event)
    (:w :up) (if (= (:movedir state) "s") state (assoc state :movedir "w"))
    (:s :down) (if (= (:movedir state) "w") state (assoc state :movedir "s"))
    (:a :left) (if (= (:movedir state) "d") state (assoc state :movedir "a"))
    (:d :right) (if (= (:movedir state) "a")  state (assoc state :movedir "d"))
    state))


(defn update-state
  [state]
  (-> state
      (update-in [:food] add-food-when-no-food)
      (#(update-in % [:food] eat-food (:snake-head %)))
      (#(update-in % [:score] increase-score (:food %)))
      (#(update-in % [:past-moves] track-head-state (:score %) (:snake-head %)))
      (#(update-in % [:snake-head] move-snake (:movedir %)))))


(defn draw-state
  [state]
  (q/background 24 24 24)
  (q/fill 255 255 255)
  (q/text-size 20)
  (q/text (str (:score state)) 15 30)
  ((get (:food state) :renderFn +) body-width body-height)
  (q/fill 255 165 0)
  (q/rect (:x (:snake-head state))
          (:y (:snake-head state))
          body-width
          body-height)
  (doseq [past-move (:past-moves state)]
    (q/rect (:x past-move) (:y past-move) body-width body-height)))


(defn add-food-when-no-food
  [food]
  (if (some? food) food (create-rand-pos-food)))


(defn create-rand-pos-food
  []
  (let [x (+ (* step 10) (rand-int (- window-width (* step 10))))
        y (+ (* step 10) (rand-int (- window-height (* step 10))))]
    {:x x, :y y, :renderFn (partial q/rect x y)}))


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
                        (>= (Math/abs (- (:x head) (:x cur))) body-width) [head]
                        (>= (Math/abs (- (:y head) (:y cur))) body-height)
                          [head]
                        :else []))
                past-moves)))


(defn detect-collision
  [{bb1x :x, bb1y :y} {bb2x :x, bb2y :y}]
  (and (and (>= (+ body-width bb1x) bb2x) (>= (+ bb2x body-width) bb1x))
       (and (>= (+ body-height bb1y) bb2y) (>= (+ bb2y body-height) bb1y))))

