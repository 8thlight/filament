(ns com.eighthlight.filament.autocomplete-field
  (:require-macros [hiccups.core :as h])
  (:require [clojure.string :as string]
            [com.eighthlight.filament.async :as async]
            [com.eighthlight.filament.fx :as fx]
            [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]
            [goog.style]))

(defn- dropdown [] (dom/by-id "autocomplete-dropdown"))

(defn active-options []
  (if-let [dropdown (dropdown)]
    (dom/children dropdown)))

(def navigation-keys 
	#{util/ENTER util/ESC util/DOWN_ARROW util/UP_ARROW util/LEFT_ARROW util/RIGHT_ARROW})

(defn handle-possible-search [text-field callback e]
  (when-not (navigation-keys (.-keyCode e)) ; don't search on selection
    (let [search-term (dom/value text-field)]
      (callback search-term))))

(defn- arm-search-callback [text-field callback]
  (async/handle-throttled-events
    text-field
    "keyup"
    (partial handle-possible-search text-field callback)))

(defn- close-dropdown []
  (when-let [dropdown (dropdown)]
    (dom/detach! dropdown)))

(defn- highlighted-option []
  (when-let [dropdown (dropdown)]
    (when-let [index (dom/get-data dropdown :highlighted-index)]
      (nth (dom/children dropdown) index))))

(defn- highlight-option-at
  ([index]
    (when-let [dropdown (dropdown)] (highlight-option-at index dropdown)))
  ([index dropdown]
    (let [options (dom/children dropdown)]
      (when-let [highlighted highlighted-option]
          (dom/remove-class! highlighted "highlighted"))
      (let [new-highlighted (nth options index)]
        (dom/add-class! new-highlighted "highlighted")
        (dom/set-data! dropdown :highlighted-index index)))))

(defn- highlight-next-option []
  (let [dropdown (dropdown)]
    (let [highlighted-index (or (dom/get-data dropdown :highlighted-index) -1)
          next-index (inc highlighted-index)]
      (when (< next-index (count (dom/children dropdown)))
        (highlight-option-at next-index dropdown)))))

(defn- highlight-previous-option []
  (let [dropdown (dropdown)]
    (let [highlighted-index (or (dom/get-data dropdown :highlighted-index) (count (dom/children dropdown)))
          next-index (dec highlighted-index)]
      (when (>= next-index 0)
        (highlight-option-at next-index dropdown)))))

(defn- handle-dropdown-navigation [text-field e]
  (cond
    (util/DOWN_ARROW? e) (highlight-next-option)
    (util/UP_ARROW? e) (highlight-previous-option)))

(defn- process-selection [input payload]
  (when input
    (let [on-select-action (or (dom/get-data input :on-select) #())]
      (close-dropdown)
      (on-select-action payload))))

(defn select-highlighted-option [text-field]
  (when-let [selection (highlighted-option)]
    (process-selection text-field (dom/get-data selection :payload))))

(defn- handle-possible-selection [text-field e]
  (when (util/ENTER? e) (select-highlighted-option text-field)))

(defn arm-field [text-field callbacks]
  (dom/set-data! text-field :on-select (:on-select callbacks))
  (event/listen! text-field :keydown (partial handle-dropdown-navigation text-field))
  (event/listen! text-field :keyup (partial handle-possible-selection text-field))
  (when-let [on-search (:on-search callbacks)]
    (arm-search-callback text-field on-search))
  (event/listen! text-field :blur close-dropdown))

(defn attach-dropdown [input]
  (let [dropdown (dom/single-node (h/html [:ul#autocomplete-dropdown.dropdown]))
        coords (goog.style/getPosition input)
        size (goog.style/getSize input)]
    (dom/append! (.-parentElement input) dropdown)
    (dom/set-style! dropdown "width" (str (.-width size) "px"))
    (dom/set-style! dropdown "top" (str (+ (.-y coords) (.-height size)) "px"))
    (dom/set-style! dropdown "left" (str (.-x coords) "px"))
    dropdown))

(defn hide-dropdown []
  (when-let [dropdown (dropdown)]
    (dom/detach! dropdown)))

(defn show-dropdown [input items]
  (let [dropdown (or (dropdown) (attach-dropdown input))]
    (dom/destroy-children! dropdown)
    (doseq [[text data] items]
      (let [item (dom/single-node (h/html [:li text]))]
        (dom/set-data! item :payload [text data])
        (event/listen! item :mousedown #(process-selection input [text data]))
        (dom/append! dropdown item)))
    dropdown))
