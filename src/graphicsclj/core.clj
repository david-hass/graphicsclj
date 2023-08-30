(ns graphicsclj.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))



(defn handle-user-input
  [state event]
  (case (:key event)
    (:w :up) (assoc state :movedir "w")
    (:s :down) (assoc state :movedir "s")
    (:a :left) (assoc state :movedir "a")
    (:d :right) (assoc state :movedir "d")
    state))


(defn setup [] (q/frame-rate 60) {:movedir "a", :x 396, :y 396})


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


(defn update-state
  [state]
  (-> state
      (update-in [:x] (fn [_x] (move-x (:movedir state) _x)))
      (update-in [:y] (fn [_y] (move-y (:movedir state) _y)))))


(defn draw-state
  [state]
  (q/background 24 24 24)
  (q/fill 204 102 230)
  (q/rect (:x state) (:y state) 10 10)
  (q/fill 102 204 230)
  (q/ellipse 102 304 10 10))




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
