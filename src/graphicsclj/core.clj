(ns graphicsclj.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))


(declare move-x move-y rand-pos-food add-food-when-no-food)


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


(defn setup
  []
  (q/frame-rate 60)
  {:foods [], :score 0, :movedir "a", :x 396, :y 396})


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
      (update-in [:foods] add-food-when-no-food)
      (update-in [:x] (fn [_x] (move-x (:movedir state) _x)))
      (update-in [:y] (fn [_y] (move-y (:movedir state) _y)))))


(defn draw-state
  [state]
  (q/background 24 24 24)
  (q/fill 255 165 0)
  (q/rect (:x state) (:y state) 10 10)
  (q/fill 255 255 255)
  (q/text-size 20)
  (q/text (str (:score state)) 15 30)
  (doseq [food (:foods state)] ((:renderFn food) 10 10)))


(defn add-food-when-no-food
  [foods]
  (if (empty? foods) (conj foods (rand-pos-food)) foods))


(defrecord RandomPosFood [x y renderFn])
(defn rand-pos-food
  []
  (let [rand-pos #(rand-int 439)
        x (rand-pos)
        y (rand-pos)]
    (RandomPosFood. x y (partial q/rect x y))))


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
