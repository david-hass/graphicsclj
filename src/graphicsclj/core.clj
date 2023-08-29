(ns graphicsclj.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn setup [] (q/frame-rate 60) {:movedir "up", :x 250, :y 250})

(defn move-x
  [movedir x]
  (if (or (= movedir "right") (= movedir "left"))
    (case x
      500 -499
      0 499
      1)
    0))

(defn move-y
  [movedir y]
  (if (or (= movedir "up") (= movedir "down"))
    (case y
      500 -499
      0 499
      1)
    0))

(defn update-state
  [state]
  {:movedir "up",
   :x (+ (:x state) (move-x (:movedir state) (:x state))),
   :y (+ (:y state) (move-y (:movedir state) (:y state)))})

(defn draw-state
  [state]
  ; Clear sketch
  (q/background 24 24 24)
  (q/fill 173 216 230)
  ; Draw movement
  (let [_x (:x state)
        _y (:y state)
        x _x
        y _y]
    ; Move origin point to the center of the sketch.
    (q/rect x y 10 10)
    ;(q/with-translation [(/ (q/width) 2) (/ (q/height) 2)] (q/rect x y 10 10))
  ))


#_{:clj-kondo/ignore [:unresolved-symbol]}
(q/defsketch graphicsclj
             :title "You spin my circle right round"
             :size [500 500]
             ; setup function called only once, during sketch initialization.
             :setup setup
             ; update-state is called on each iteration before draw-state.
             :update update-state
             :draw draw-state
             :features [:keep-on-top]
             ; This sketch uses functional-mode middleware.
             ; Check quil wiki for more info about middlewares and particularly
             ; fun-mode.
             :middleware [m/fun-mode])
