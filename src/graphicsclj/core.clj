(ns graphicsclj.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def ^:const bodyHeight 10)
(def ^:const bodyWidth 10)


(declare move-snake
         rand-pos-food
         add-food-when-no-food
         check-for-food-eaten
         detect-collision
         move-x
         move-y)


(defn setup
  []
  (q/frame-rate 60)
  {:foods [], :score 0, :movedir "a", :bodyParts [{:x 396, :y 396}]})


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
      move-snake
      (update-in [:foods] add-food-when-no-food)
      check-for-food-eaten))


(defn draw-state
  [state]
  (q/background 24 24 24)
  (q/fill 255 165 0)
  (println (:bodyParts state))
  (doseq [bodyPart (:bodyParts state)]
    (q/rect (:x bodyPart) (:y bodyPart) bodyWidth bodyHeight))
  (q/fill 255 255 255)
  (q/text-size 20)
  (q/text (str (:score state)) 15 30)
  (doseq [food (:foods state)] ((:renderFn food) bodyWidth bodyHeight)))


(defrecord RandomPosFood [x y renderFn])


(defn add-food-when-no-food
  [foods]
  (if (empty? foods) (conj foods (rand-pos-food)) foods))


(defn rand-pos-food
  []
  (let [rand-pos #(rand-int 439)
        x (rand-pos)
        y (rand-pos)]
    (RandomPosFood. x y (partial q/rect x y))))


(defn check-for-food-eaten
  [state]
  (if (detect-collision state (first (:foods state)))
    (assoc state
      :foods []
      :score (inc (:score state)))
    state))


(defn detect-collision
  [{bb1x :x, bb1y :y} {bb2x :x, bb2y :y}]
  (and (and (>= (+ bodyWidth bb1x) bb2x) (>= (+ bb2x bodyWidth) bb1x))
       (and (>= (+ bodyHeight bb1y) bb2y) (>= (+ bb2y bodyHeight) bb1y))))


(defn move-snake
  [state]
  (assoc state
    :bodyParts (conj (let [head (first (:bodyParts state))]
                       (assoc head
                         :x (move-x (:moveDir state) (:x head))
                         :y (move-y (:moveDir state) (:y head))))
                     (rest (:bodyParts state)))))


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
