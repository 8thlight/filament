(ns com.eighthlight.filament.modal
  (:require-macros [hiccup.core :as h])
  (:require [com.eighthlight.filament.util :as util]
            [com.eighthlight.filament.fx :as fx]
            [domina :as dom]
            [domina.events :as event]
            [hiccup.core]))

(defn modal-view
  "Returns a hiccup data structure representing the modal DOM."
  ([id] (modal-view id "modal"))
  ([id class]
    [:div {:tabindex "0" :id id :class (str class "-backdrop")} ; tabindex is necessary to grant focus
     [:div {:class class}
      [:div {:class (str class "-header")}
       [:button {:type "button" :class (str class "-close-button")}
        "&#x2715;"]
       [:h3]]
      [:div {:class (str class "-body")}]]]))

(defn modal-node [modal] (first (dom/children modal)))
(defn header-node [modal] (first (dom/children (modal-node modal))))
(defn title-node [modal] (second (dom/children (header-node modal))))
(defn close-button [modal] (first (dom/children (header-node modal))))
(defn body [modal] (second (dom/children (modal-node modal))))

(defn show!
  "Attaches the modal to the DOM and makes it visible."
  [modal]
  (dom/set-style! modal :opacity 0)
  (dom/append! (.-body js/document) modal)
  (fx/show! modal)
  (.focus modal)
  (fx/fade-in modal
              :duration 100
              :on-end (fn [e] (dom/set-style! modal :opacity 100))))

(defn hide!
  "Hides the modal and detaches it from the DOM."
  [modal]
  (fx/fade-out modal
    :duration 100
    :on-finish #(do
                  (dom/detach! modal)
                  (fx/hide! modal))))

(defn set-title!
  "Populates the title of the modal."
  [modal title]
  (dom/set-text! (title-node modal) title))

(defn title
  "Returns the title of the modal."
  [modal]
  (dom/text (title-node modal)))

(defn set-content!
   "Populates the body of the modal."
  [modal content]
  (dom/set-html! (body modal) content))

(defn populate!
  "Populates the modal. The second parameter should be map containing:
    :title - a title for the modal
    :content - the main content of the modal"
  [modal {:keys [title content]}]
  (set-title! modal title)
  (set-content! modal content))

(defn clear!
  "Clears out any content that is currently in the modal dom."
  [modal]
  (populate! modal {:title "" :content ""}))

(defn- process-keyup [modal e]
  (when (and (util/ESC? e)
          (not= "SELECT" (.-tagName (event/target e))))
    (hide! modal)))

(defn- bind-listeners [modal]
  (event/listen! modal :keyup (partial process-keyup modal))
  (util/override-click! modal (fn [e] (hide! modal)))
  (util/override-click! (close-button modal) (fn [e] (hide! modal)))
  (util/override-click! (modal-node modal) #(event/stop-propagation %)))

(defn create-modal
  "Returns a detached modal DOM element with no content.  This is typically used to create the modal at a high level
  that can be reused. See populate!, show!, and hide! for usage.
  The id parameter is added to the root DOM element so that it can be retreived easily.
  The optional class parameter will be prefixed onto all classes applied to the elements. Defaults to 'modal'.

  CSS: Presuming the default 'modal' class, the following selectors should be styled:
    .modal-backdrop - covers the page and provide background for the foreground div
    .modal - centered window for modal content
    .modal-header - div at the top of the modal
    .modal-header button - close button
    .modal-header h3 - header/title for the modal
    .modal-body - container for the modal content"
  ([id] (create-modal id "modal"))
  ([id class]
    (let [modal (dom/single-node (h/html (modal-view id class)))]
      (bind-listeners modal)
      modal)))
