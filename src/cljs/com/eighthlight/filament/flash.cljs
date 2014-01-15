(ns com.eighthlight.filament.flash
  (:require-macros [hiccup.core :as h])
  (:require [clojure.string :as str]
            [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [domina.css :as css]))

(defn flash-container-node
  "Returns the flash container node.  The node is added to the DOM if it isn't already attached."
  []
  (or
    (dom/by-id "flash-container")
    (let [container (dom/single-node (h/html [:div#flash-container.flash-container]))]
      (dom/append! (.-body js/document) container)
      container)))

(defn- flash-section [section]
  (let [name (str "flash-" section)]
    (or
      (dom/by-id name)
      (let [section-node (dom/single-node (h/html [:div {:id name :class name} [:a.flash-close "X"]]))]
        (util/override-click! (first (dom/children section-node)) #(dom/destroy! section-node))
        (dom/append! (flash-container-node) section-node)
        section-node))))

(defn errors-node
  "Returns the errors container node.  The node is added to the DOM if it isn't already attached."
  [] (flash-section "errors"))

(defn notices-node
  "Returns the notices container node.  The node is added to the DOM if it isn't already attached."
  [] (flash-section "notices"))

(defn successes-node
  "Returns the successes container node.  The node is added to the DOM if it isn't already attached."
  [] (flash-section "successes"))

(defn flash-error!
  "Adds an error flash to the DOM.

  CSS:
    .flash-container - a div added to the body element so consider using absolute/fixed positioning
    .flash-errors - a div inside the flash-container that will hold all the error flashes
    a.flash-close - the first node in a flash section. Contains the text \"X\". Clicking it will close the section.
    p.flash-error - one is added to the flash-errors div for each message"
  [message]
  (dom/append! (errors-node) (h/html [:p.flash-error message])))

(defn flash-notice!
  "Adds an notice flash to the DOM.

  CSS:
    .flash-container - a div added to the body element so consider using absolute/fixed positioning
    .flash-notices - a div inside the flash-container that will hold all the notice flashes
    a.flash-close - the first node in a flash section. Contains the text \"X\". Clicking it will close the section.
    p.flash-notice - one is added to the flash-notices div for each message"
  [message]
  (dom/append! (notices-node) (h/html [:p.flash-notice message])))

(defn flash-success!
  "Adds an success flash to the DOM.

  CSS:
    .flash-container - a div added to the body element so consider using absolute/fixed positioning
    .flash-successes - a div inside the flash-container that will hold all the success flashes
    a.flash-close - the first node in a flash section. Contains the text \"X\". Clicking it will close the section.
    p.flash-success - one is added to the flash-successes div for each message"
  [message]
  (dom/append! (successes-node) (h/html [:p.flash-success message])))

(defn clear-flash!
  "Removes all flash nodes from the DOM."
  []
  (when-let [container (dom/by-id "flash-container")]
    (dom/destroy! container)))

(defn flash-messages
  "Returns a map of currently displayed flash messages in the form:

    {:error [] :notice [] :success []}"
  []
  {:error (map dom/text (rest (dom/children (dom/by-id "flash-errors"))))
   :notice (map dom/text (rest (dom/children (dom/by-id "flash-notices"))))
   :success (map dom/text (rest (dom/children (dom/by-id "flash-successes"))))})