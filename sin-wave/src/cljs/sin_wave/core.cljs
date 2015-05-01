(ns sin-wave.core
  (:refer-clojure :exclude [concat]))

(defn main [])

(def canvas (.getElementById js/document "myCanvas"))
(def ctx (.getContext canvas "2d"))

(.clearRect ctx  0 0 (.-width canvas) (.-height canvas))

(.log js/console ctx)

(def interval js/Rx.Observable.interval)
(def time (interval 2))

#_(-> time
    (.take 5)
    (.subscribe (fn [n]
                  (.log js/console n))))

(defn deg-to-rad [n]
  (* (/ Math/PI 180) n))

(defn log [& rest]
  (.log js/console (apply str rest)))

(defn sine-coord
  [freq phase x]
  (let [sin (->> x deg-to-rad (* freq) (+ phase) Math/sin)
        y   (- 90 (* sin 90))]
    {:x x
     :y y
     :sin sin}))

(defn fill-rect [x y colour]
  (let [factor (/ x 600)
        size   (* 50 factor)]
    (set! (.-fillStyle ctx) colour)
    (.fillRect ctx x y size size)))

(def sine-wave (.map time (partial sine-coord 2 3)))

(defn create-sine-wave [freq phase]
  (.map time (partial sine-coord freq phase)))

#_(-> sine-wave
    (.take 700)
    (.subscribe (fn [{:keys [x y]}]
                  (fill-rect x y "orange"))))

(def colour (.map sine-wave (fn [{:keys [sin]}]
                              (if (neg? sin) "red" "blue"))))

#_(-> (.zip sine-wave colour (partial vector))
    (.take 600)
    (.subscribe (fn [[{:keys [x y sin]} colour]]
                  (fill-rect x y colour ))))

(def red (.map time (fn [_] "red")))
(def blue (.map time (fn [_] "blue")))

(def concat js/Rx.Observable.concat)
(def defer js/Rx.Observable.defer)
(def from-event js/Rx.Observable.fromEvent)
(def mouse-click (from-event canvas "click"))

(def cycle-colour (concat (.takeUntil red mouse-click)
                          (defer #(concat (.takeUntil blue mouse-click)
                                          cycle-colour))))

#_(-> (.zip sine-wave cycle-colour (partial vector))
    (.take 600)
    (.subscribe (fn [[{:keys [x y sin]} colour]]
                  (fill-rect x y colour ))))

(def gray (Math/pow 2 23))

(defn int-to-rgb [x]
  (let [red   (bit-shift-right (bit-and 0xFF0000 x) 16)
        green (bit-shift-right (bit-and 0x00FF00 x) 8)
        blue  (bit-and 0x0000FF x)]
    (str "rgb(" red "," green "," blue ")")))

(def rainbow
  (-> (.zip (create-sine-wave 0.7 0)
            (create-sine-wave 0.7 2)
            (create-sine-wave 0.7 4)
            (partial vector))
      (.map (fn [[red green blue]]
              (let [to-int (fn [x] (-> x (* 127) (+ 128) int))]
                (str "rgb("
                     (to-int (:sin red)) ","
                     (to-int (:sin green)) ","
                     (to-int (:sin blue))
                     ")"))
              #_"red"))))

(-> (.zip (create-sine-wave 1 0) rainbow (partial vector))
    (.take 600)
    (.subscribe (fn [[{:keys [x y sin]} colour]]
                  (.log js/console colour)
                  (fill-rect x y colour))))
