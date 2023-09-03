(ns graphicsclj.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def ^:const bodyHeight 12)
(def ^:const bodyWidth 12)


(declare move-snake
         create-rand-pos-food
         add-food-when-no-food
         eat-food
         increase-score
         append-body-part
         detect-collision
         move-x
         move-y)


(defn setup
  []
  (q/frame-rate 60)
  {:food nil,
   :score 0,
   :movedir "a",
   :posTracking [],
   :bodyParts [{:x 396, :y 396}]})


(defn handle-user-input
  [state event]
  (case (:key event)
    (:w :up) (assoc state :movedir "w")
    (:s :down) (assoc state :movedir "s")
    (:a :left) (assoc state :movedir "a")
    (:d :right) (assoc state :movedir "d")
    state))


(defn update-state
  [state]
  (-> state
      (update-in [:food] add-food-when-no-food)
      ;((fn [x] (println x) x))
      (#(update-in % [:food] eat-food (first (:bodyParts %))))
      ;((fn [x] (println x) x))
      (#(update-in % [:score] increase-score (:food %)))
      ;((fn [x] (println x) x))
      (#(update-in % [:bodyParts] append-body-part (:food %)))
      ;((fn [x] (println x) x))
      (#(update-in %
                   [:posTracking]
                   track-head-pos
                   (first (:bodyParts %))
                   (:score %)))
      (#(update-in % [:bodyParts] move-snake (:movedir %)))
      ;((fn [x] (println x) x))
  ))


(defn draw-state
  [state]
  (q/background 24 24 24)
  (q/fill 255 165 0)
  (doseq [bodyPart (:bodyParts state)]
    (q/rect (:x bodyPart) (:y bodyPart) bodyWidth bodyHeight))
  (q/fill 255 255 255)
  (q/text-size 20)
  (q/text (str (:score state)) 15 30)
  ((get (:food state) :renderFn +) bodyWidth bodyHeight))


(defn add-food-when-no-food
  [food]
  (if (some? food) food (create-rand-pos-food)))


(defn create-rand-pos-food
  []
  (let [rand-pos #(+ 30 (rand-int 439))
        x (rand-pos)
        y (rand-pos)]
    {:x x, :y y, :renderFn (partial q/rect x y)}))


(defn eat-food [food head] (if (detect-collision food head) nil food))


(defn detect-collision
  [{bb1x :x, bb1y :y} {bb2x :x, bb2y :y}]
  (and (and (>= (+ bodyWidth bb1x) bb2x) (>= (+ bb2x bodyWidth) bb1x))
       (and (>= (+ bodyHeight bb1y) bb2y) (>= (+ bb2y bodyHeight) bb1y))))


(defn increase-score [score food] (if (some? food) score (inc score)))


(defn append-body-part
  [[head & _ :as bodyParts] food]
  (if (some? food) bodyParts (conj bodyParts head)))




(defn track-head-pos
  [posTracking head score]
  (subvec (conj posTracking head)
          0
          (if (>= (count posTracking) score) score (count posTracking))))




(defn move-snake
  [[head & rest] movedir]
  (apply ((conj []
                (assoc head
                  :dir movedir
                  :x (move-x movedir (:x head))
                  :y (move-y movedir (:y head)))))
    (if (empty? rest) [] (drop-last rest))))





(defn move-x
  [movedir x]
  (+ x
     (case movedir
       "d" (case x
             498 -498
             3)
       "a" (case x
             0 498
             -3)
       0)))


(defn move-y
  [movedir y]
  (+ y
     (case movedir
       "w" (case y
             0 498
             -3)
       "s" (case y
             498 -498
             3)
       0)))


#_{:clj-kondo/ignore [:unresolved-symbol]}
(q/defsketch graphicsclj
             :title "Snake"
             :size [500 500]
             :setup setup
             :update update-state
             :draw draw-state
             :key-pressed handle-user-input
             :features [:keep-on-top]
             :middleware [m/fun-mode])
