(ns com.eighthlight.filament.modal
  (:require-macros [hiccup.core :as h])
  (:require [com.eighthlight.filament.util :as util]
            [com.eighthlight.filament.fx :as fx]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]
            [hiccup.core]))

(defn modal-view
  ([id] (modal-view id "modal"))
  ([id class]
    [:div {:tabindex "0" :id id :class (str class "-backdrop")} ; tabindex is necessary to grant focus
     [:div {:class class}
      [:div {:class (str class "-header")}
       [:button {:type "button" :class (str class "-close-button")}
        "&#x2715;"]
       [:h3]]
      [:div {:class (str class "-body")}]]]))

(defn title-node [modal] (css/sel modal "h3"))

(defn show! [modal]
  (dom/set-style! modal :opacity 0)
  (dom/append! (css/sel "body") modal)
  (fx/show! modal)
  (.focus modal)
  (fx/fade-in modal
              :duration 100
              :on-end (fn [e] (dom/set-style! modal :opacity 100))))

(defn hide! [modal]
  (fx/fade-out modal
    :duration 100
    :on-finish #(do
                  (dom/detach! modal)
                  (fx/hide! modal))))

(defn set-title! [modal title]
  (dom/set-text! (title-node modal) title))

(defn title [modal]
  (dom/text (title-node modal)))

(defn set-content! [modal content]
  (dom/set-html! (css/sel modal ".modal-body") content))

(defn body [modal]
  (css/sel modal ".modal-body"))

(defn populate! [modal {:keys [title content]}]
  (set-title! modal title)
  (set-content! modal content))

(defn clear! [modal]
  (populate! modal {:title "" :content ""}))

(defn process-keyup [modal e]
  (when (and (util/ESC? e)
          (not= "SELECT" (.-tagName (event/target e))))
    (hide! modal)))

(defn bind-listeners [modal class]
  (event/listen! modal :keyup (partial process-keyup modal))
  (event/listen! modal :click (fn [e] (hide! modal)))
  (event/listen!
    (css/sel modal (str "." (str class "-close-button")))
    :click (fn [e] (hide! modal)))
  (let [modal-node (css/sel modal (str "." class))]
    (event/listen! modal-node :click (fn [e] (event/stop-propagation e)))))

(defn create-modal
  ([id] (create-modal id "modal"))
  ([id class]
    (let [modal (dom/single-node (h/html (modal-view id class)))]
      (bind-listeners modal class)
;      (hide! modal)
      modal)))
