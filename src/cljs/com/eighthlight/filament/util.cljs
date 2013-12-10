(ns com.eighthlight.filament.util
  (:require-macros [hiccups.core :as h])
  (:require [clojure.string :as string]
            [clojure.walk :as walk]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]
            [hiccups.runtime]))

(defn ->options
  "Takes keyword argument and converts them to a map.  If the args are prefixed with a map, the rest of the
  args are merged in."
  [options]
  (if (map? (first options))
    (merge (first options) (apply hash-map (rest options)))
    (apply hash-map options)))

(defn ->json [data]
  (.stringify js/JSON (clj->js data)))

; CLJS ONLY BELOW

(def ENTER 13)
(defn ENTER? [e] (= ENTER (.-keyCode (event/raw-event e))))

(def ESC 27)
(defn ESC? [e] (= ESC (.-keyCode (event/raw-event e))))

(def SPACE 32)
(defn SPACE? [e] (= SPACE (.-keyCode (event/raw-event e))))

(def LEFT_ARROW 37)
(defn LEFT_ARROW? [e] (= LEFT_ARROW (.-keyCode (event/raw-event e))))

(def UP_ARROW 38)
(defn UP_ARROW? [e] (= UP_ARROW (.-keyCode (event/raw-event e))))

(def RIGHT_ARROW 39)
(defn RIGHT_ARROW? [e] (= RIGHT_ARROW (.-keyCode (event/raw-event e))))

(def DOWN_ARROW 40)
(defn DOWN_ARROW? [e] (= DOWN_ARROW (.-keyCode (event/raw-event e))))

(def not-blank? (complement string/blank?))

(defn override-click! [nodes action]
  (event/listen! nodes
    :click (fn [e]
             (event/prevent-default e)
             (action e))))

(defn element-id [element]
  (dom/attr element :id))

(defn errors->messages [errors]
  (mapcat
    (fn [[key messages] error]
      (map #(str (name key) " " %) messages))
    errors))

(def default-error "There was a problem.")

(defn flash-error [text]
  (let [message (if (string/blank? text)
                  default-error
                  text)]
    (dom/set-html! (css/sel "#flash-container")
      (h/html [:div.flash [:h2.error message]]))))

(defn clear-flash []
  (dom/set-html! (css/sel "#flash-container") ""))

(defn remove-loading-placeholder [name]
  (dom/detach! (dom/by-id (str name "-loading-placeholder"))))
