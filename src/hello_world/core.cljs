(ns hello-world.core)

(def LOGO "Pecazen")
(def DIGIT_DISTANCE 0.3)
(def SEC_HAND_LENGTH 0.30)
(def SEC_HAND_WIDTH 0.005)
(def MIN_HAND_LENGTH 0.30)
(def MIN_HAND_WIDTH 0.02)
(def HOUR_HAND_LENGTH 0.2)
(def HOUR_HAND_WIDTH 0.03)
(def STRAIGHT_UP_RADIAN (* (/ -1 4.0) 2 Math.PI))
(def OPPOSITE_LENGTH 0.025)

(def ring-color nil)
(def sec-hand-color nil)
(def min-hand-color nil)
(def hour-hand-color nil)
(def text-color nil)

(defn change-colors []
  (letfn [(f [] (+ 0.75 (/ (Math.random) 4)))
          (g [] (+ 0.125 (/ (Math.random) 4)))]
    (set! ring-color (list (f) (f) (f)))
    (set! sec-hand-color (list (f) (f) (f)))
    (set! min-hand-color (list (f) (f) (f)))
    (set! hour-hand-color (list (f) (f) (f)))
    (set! text-color (list (g) (g) (g)))))
(change-colors)

(defn rgb [r g b]
  (str "rgb("
       (Math.floor (* 255 r)) ","
       (Math.floor (* 255 g)) ","
       (Math.floor (* 255 b)) ")"))

(defn render-clock [ctx w h hour min sec]
  (letfn [ (draw-bg []
             (set! (.-fillStyle ctx) (rgb 0.5 0.5 0.5))
             (.fillRect ctx 0 0 w h))

           (draw-circum []
             (.beginPath ctx)
             (set! (.-lineWidth ctx) (* 0.02 w))
             (set! (.-strokeStyle ctx) (apply #'rgb ring-color))
             (.arc ctx (* w 0.5) (* h 0.5) (* w 0.375) 0 (* Math.PI 2))
             (.stroke ctx))

           (draw-logo []
             (set! (.-font ctx) (str "bold " (* h 0.055) "px Georgia"))
             (set! (.-fillStyle ctx) (apply rgb text-color))
             (let [te (.measureText ctx LOGO)]
               (.fillText ctx LOGO (- (* w 0.5) (/ (.-width te) 2)) (* h 0.575))))

           (draw-digits []
             (set! (.-font ctx) (str "bold " (* 0.1 h) "px Georgia"))
             (set! (.-fillStyle ctx) (apply rgb text-color))

             (dotimes [i 12]
               (let [text (if (= i 0) "12" (str i))]
                 (draw-digit text (to-radian (mod i 12) 12)))))

           (draw-digit [text radian]
             (let* [te (.measureText ctx text)
                    xoff (- (/ (.-width te) 2))
                    yoff (* h 0.02)]
               (.fillText ctx
                          text
                          (+ (* w (+ 0.5 (* DIGIT_DISTANCE (Math.cos radian)))) xoff)
                          (+ (* h (+ 0.5 (* DIGIT_DISTANCE (Math.sin radian)))) yoff))))

           (to-radian [value unit]
             (+ STRAIGHT_UP_RADIAN (* (/ value unit) 2 Math.PI)))

           (hour-to-radian []
             (to-radian (+ (mod hour 12) (/ min 60)) 12))

           (draw-hand [width length dir color]
             (.beginPath ctx)

             (set! (.-lineWidth ctx) (* width w))
             (set! (.-strokeStyle ctx) (apply rgb color))

             (.moveTo ctx (* w 0.5) (* h 0.5))
             (.lineTo ctx
              (* w (+ 0.5 (* OPPOSITE_LENGTH (Math.cos (+ dir Math.PI)))))
              (* h (+ 0.5 (* OPPOSITE_LENGTH (Math.sin (+ dir Math.PI))))))

             (.moveTo ctx (* w 0.5) (* h 0.5))
             (.lineTo ctx
              (* w (+ 0.5 (* length (Math.cos dir))))
              (* w (+ 0.5 (* length (Math.sin dir)))))

             (.stroke ctx))

           (draw-sec-hand []
             (draw-hand SEC_HAND_WIDTH SEC_HAND_LENGTH (to-radian sec 60) sec-hand-color))

           (draw-min-hand []
             (draw-hand MIN_HAND_WIDTH MIN_HAND_LENGTH (to-radian min 60) min-hand-color))

           (draw-hour-hand []
             (draw-hand HOUR_HAND_WIDTH HOUR_HAND_LENGTH (hour-to-radian) hour-hand-color))

           ]
    (draw-bg)

    (draw-circum)
    (draw-digits)
    (draw-logo)

    (draw-sec-hand)
    (draw-min-hand)
    (draw-hour-hand)
    ))

(defn get-hms []
  (let [d (js/Date.)]
    (list (.getHours d)
          (.getMinutes d)
          (+ (.getSeconds d)
             (/ (.getMilliseconds d) 1000)))))

(let* [canvas (.getElementById js/document "screen")
       ctx (.getContext canvas "2d")]
  (defn refresh [timestamp]
    (let [[h m s] (get-hms)]
      (render-clock ctx (.-width canvas) (.-height canvas) h m s))
    (.requestAnimationFrame js/window refresh))
  )
(.requestAnimationFrame js/window refresh)
