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

(defn- active-options []
  (if-let [dropdown (dropdown)]
    (dom/children dropdown)))

(defn- possible-search-handler [text-field callback]
  (let [last-query (atom (dom/value text-field))]
    (fn [e]
      (let [query (dom/value text-field)]
        (when (or (and (util/ARROW? e) (not (dropdown))) (not (= @last-query query)))
          (reset! last-query query)
          (cond
            (util/ENTER? e) nil ; don't search on selection
            (util/ESC? e) nil ; exit
            :else (callback query)))))))

(defn- arm-search-callback [text-field callback throttle]
  (if (number? throttle)
    (async/handle-throttled-events text-field "keyup" (possible-search-handler text-field callback) throttle)
    (event/listen! text-field :keyup (possible-search-handler text-field callback))))

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
     (when-let [highlighted-index (dom/get-data dropdown :highlighted-index)]
       (let [highlighted (nth options highlighted-index)]
         (dom/remove-class! highlighted "highlighted")))
     (let [new-highlighted (nth options index)]
       (dom/add-class! new-highlighted "highlighted")
       (dom/set-data! dropdown :highlighted-index index)))))

(defn- highlight-next-option []
  (when-let [dropdown (dropdown)]
    (let [highlighted-index (or (dom/get-data dropdown :highlighted-index) -1)
          next-index (inc highlighted-index)]
      (when (< next-index (count (dom/children dropdown)))
        (highlight-option-at next-index dropdown)))))

(defn- highlight-previous-option []
  (when-let [dropdown (dropdown)]
    (let [highlighted-index (or (dom/get-data dropdown :highlighted-index) (count (dom/children dropdown)))
          next-index (dec highlighted-index)]
      (when (>= next-index 0)
        (highlight-option-at next-index dropdown)))))

(defn- handle-dropdown-navigation [text-field e]
  (cond
    (util/DOWN_ARROW? e) (highlight-next-option)
    (util/UP_ARROW? e) (highlight-previous-option)
    (util/ENTER? e) (when (dropdown) (event/prevent-default e))
    (util/ESC? e) (when (dropdown) (close-dropdown))))

(defn- process-selection [input payload]
  (when input
    (let [on-select-action (or (dom/get-data input :on-select) #())]
      (close-dropdown)
      (on-select-action payload))))

(defn- select-highlighted-option [text-field]
  (when-let [selection (highlighted-option)]
    (process-selection text-field (dom/get-data selection :payload))))

(defn- handle-possible-selection [text-field e]
  (when (util/ENTER? e)
    (event/prevent-default e)
    (select-highlighted-option text-field)))

(defn- attach-dropdown [input]
  (let [dropdown (dom/single-node (h/html [:ul#autocomplete-dropdown.autocomplete-dropdown]))
        coords (goog.style/getPosition input)
        size (goog.style/getSize input)]
    (dom/append! (.-parentElement input) dropdown)
    (dom/set-style! dropdown "width" (str (.-width size) "px"))
    (dom/set-style! dropdown "top" (str (+ (.-y coords) (.-height size)) "px"))
    (dom/set-style! dropdown "left" (str (.-x coords) "px"))
    dropdown))

(defn arm-field
  "Arms an input (text) with autocomplete behavior.

  callbacks/options:
  :on-search - a fn that takes one parameter, the query or current text of the input.  This fn typically performs
    a search pased on the query and loads a dropdown (see show-dropdown), but it doesn't have to.
  :on-select - a fn that take a dropdown payload ([<display name> <value>]) and is called when a dropdown item
    is selected.
  :throttle - number of millisecond.  Searches will not take place more frequently then the specified period.
    By default, every change will trigger a search."
  [text-field callbacks]
  (dom/set-data! text-field :on-select (:on-select callbacks))
  (event/listen! text-field :keydown (partial handle-dropdown-navigation text-field))
  (event/listen! text-field :keyup (partial handle-possible-selection text-field))
  (when-let [on-search (:on-search callbacks)]
    (arm-search-callback text-field on-search (:throttle callbacks)))
  (event/listen! text-field :blur close-dropdown))

(defn show-dropdown
  "Open a dropdown menu attached to the bottom of the input.  The second parameter must be a seq of pairs where the
  first value is the display text and the second value is a payload for the programmer's use.

  CSS: Consider adding the following CSS to make dropdowns look good
    .autocomplete-dropdown - to style the containing ul
    .autocomplete-dropdown li - to style each list item in the dropdown
    .autocomplete-dropdown li:hover - to style a list item when it's moused-over
    .autocomplete-dropdown .highlighted - to style a list item highlighted via arrow keys"
  [input items]
  (let [dropdown (or (dropdown) (attach-dropdown input))]
    (dom/destroy-children! dropdown)
    (doseq [[text data] items]
      (let [item (dom/single-node (h/html [:li text]))]
        (dom/set-data! item :payload [text data])
        (event/listen! item :mousedown #(process-selection input [text data]))
        (dom/append! dropdown item)))
    dropdown))

(def hide-dropdown
  "Removes the dropdown menu form the dom."
  close-dropdown)
