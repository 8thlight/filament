(ns com.eighthlight.filament.fx
  (:require-macros [hiccup.core :as h])
  (:require [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]
            [goog.dom.forms :as forms]
            [goog.fx.easing :as ease]
            [goog.fx.dom]
            [hiccup.core]))

(defn calc-duration [options]
  (or (:duration options) 500))

(defn hidden? [e]
  (= "none" (dom/style e :display)))

(defn visible? [e]
  (not (hidden? e)))

(defn hide! [e]
  (when (not (hidden? e))
    (dom/set-data! e :display (dom/style e :display))
    (dom/set-style! e :display "none")))

(defn show! [e]
  (let [display (or (dom/get-data e :display) "")]
    (dom/set-style! e :display display)))

(defn focused? [e]
  (= e (.-activeElement (.-ownerDocument e))))

(defn focus! [e]
  (forms/focusAndSelect e))

(defn- apply-animation-hooks [animation options]
  (when-let [on-end (:on-end options)]
    (event/listen! animation "end" on-end))
  (when-let [on-end (:on-finish options)]
    (event/listen! animation "finish" on-end)))

(defn- play [animation options]
  (doto animation
    (apply-animation-hooks options)
    (.play)))

(defn fade-in [e & args]
  (let [options (util/->options args)
        duration (calc-duration options)]
    (play (goog.fx.dom.FadeInAndShow. e duration ease/inAndOut) options)))

(defn fade-out [e & args]
  (let [options (util/->options args)
        duration (calc-duration options)]
    (play (goog.fx.dom.FadeOutAndHide. e duration ease/inAndOut) options)))

(defn h-blind-in [e & args]
  (let [options (util/->options args)
        duration (calc-duration options)
        start-width (or (:start-width options) 0)
        end-width (or (:end-width options) (.-offsetWidth e))]
    (play (goog.fx.dom.ResizeWidth. e start-width end-width duration ease/inAndOut) options)))

(defn h-blind-out [e & args]
  (let [options (util/->options args)
        duration (calc-duration options)
        start-width (or (:start-width options) (.-offsetWidth e))
        end-width (or (:end-width options) 0)]
    (play (goog.fx.dom.ResizeWidth. e start-width end-width duration ease/inAndOut) options)))

(def white-rgb (clj->js [255 255 255]))

(defn- ->rgb [color]
  (cond
    (nil? color) white-rgb
    (= "transparent" color) white-rgb
    (= "rgba(0, 0, 0, 0)" color) white-rgb
    :else (goog.color.hexToRgb (.-hex (goog.color.parse color)))))

(defn highlight [e & args]
  (when e
    (let [node (dom/single-node e)
          options (util/->options args)
          duration (calc-duration options)
          highlight-color (or (:color options) "#25A8E0")
          initial-color (or (:background-color options) (dom/style e :background-color) "")
          calculated-color (goog.style.getBackgroundColor node)
          end-rgb-color (->rgb calculated-color)
          start-rgb-color (->rgb highlight-color)
          animation (goog.fx.dom.BgColorTransform. node start-rgb-color end-rgb-color duration)]
      (event/listen! animation "end" #(dom/set-style! node :background-color initial-color))
      (play animation options))))

(defn fade-checkmark
  ([node] (fade-checkmark node "check.png"))
  ([node image]
    (let [check-mark (dom/single-node (h/html [:img.decoration {:src (str "/images/" image)}]))]
      (dom/append! node check-mark)
      (show! check-mark)
      (fade-out check-mark :duration 2000 :on-finish #(dom/detach! check-mark)))))

(defn add-spinner
  ([node] (add-spinner node "snake_spinner.gif"))
  ([node image]
    (let [spinner (dom/single-node (h/html [:img.filament-spinner.decoration {:src (str "/images/" image)}]))]
      (dom/append! node spinner)
      (show! spinner))))

(defn remove-spinner [node]
  (dom/detach! (css/sel node ".filament-spinner")))

(defn has-spinner? [node]
  (> (count (dom/nodes (css/sel node ".filament-spinner"))) 0))

; MDM - For testing
; Forego the pleasantries. Just get 'er done!
(when-not js/window.navigator.onLine
  (defn calc-duration [options] 0)
  (defn play [animation options]
    (apply-animation-hooks animation options)
    (.onEnd animation)
    (.onFinish animation)))



