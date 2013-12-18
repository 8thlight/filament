(ns com.eighthlight.filament.inplace-field
  (:require-macros [hiccup.core :as h])
  (:require [com.eighthlight.filament.fx :as fx]
            [com.eighthlight.filament.util :as util]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]
            [hiccup.core]))

(def text-input [:input.inplace-field-input {:type "text"}])

(defn inplace-field
  ([id] (inplace-field id text-input))
  ([id input]
    [:span {:class "inplace-field" :id id}
     [:span.inplace-field-value {}
      [:span.inplace-field-text ""]]
     [:span.inplace-field-form {:style "display: none;"}
      [:div.inplace-errors {:style "display: none;"}]
      input]]))

(defn value-container [e] (first (dom/children e)))
(defn value-node [e] (first (dom/children (value-container e))))
(defn input-container [e] (second (dom/children e)))
(defn input-node [e] (nth (dom/children (input-container e)) 1))
(defn errors-node [e] (nth (dom/children (input-container e)) 0))

(defmulti display-value #(dom/attr % "type"))
(defmethod display-value :default [input model field-key]
  (str (get model field-key)))

(defmulti inplace-value #(dom/attr % "type"))
(defmethod inplace-value :default [node] (dom/value node))

(defmulti set-inplace-value (fn [node value] (dom/attr node "type")))
(defmethod set-inplace-value :default [node value] (dom/set-value! node value))

(defmulti inplace-focus #(dom/attr % "type"))
(defmethod inplace-focus :default [node] (fx/focus! node))

(defmulti register-update-callback (fn [node value] (dom/attr node "type")))
(defmethod register-update-callback :default [node callback]
  (event/listen! node :blur callback)
  (event/listen! node :keyup (fn [e]
                               (when (util/ENTER? e)
                                 (event/stop-propagation e)
                                 (callback)))))

(defmulti register-escape-callback (fn [node value] (dom/attr node "type")))
(defmethod register-escape-callback :default [node callback]
  (event/listen! node :keyup (fn [e] (when (util/ESC? e) (callback)))))

(defn- display-errors [field errors]
  (let [html (apply str (map #(str "<p>" % "</p>") errors))
        errors-n (errors-node field)]
    (dom/set-html! errors-n html)
    (fx/show! errors-n)
    (fx/remove-spinner (input-container field))
    (dom/add-class! (input-node field) "error")))

(defn- hide-errors [field]
  (let [errors-n (errors-node field)]
    (fx/hide! errors-n)
    (dom/set-html! errors-n "")
    (dom/remove-class! (input-node field) "error")))

(defn- view-mode! [e]
  (let [model (dom/get-data e :model)
        field-key (dom/get-data e :field-key)]
    (fx/hide! (input-container e))
    (fx/remove-spinner (input-container e))
    (dom/set-text! (value-node e) (display-value (input-node e) model field-key))
    (fx/show! (value-container e))
    (hide-errors e)
    (dom/set-data! e :mode :view)))

(defn- edit-mode! [e]
  (let [input-n (input-node e)]
    (fx/hide! (value-container e))
    (set-inplace-value input-n (dom/text (value-node e)))
    (fx/show! (input-container e))
    (inplace-focus input-n)
    (dom/set-data! e :mode :edit)))

(defn- edit-mode? [e]
  (= :edit (dom/get-data e :mode)))

(defn value-updated [field new-value]
  (let [model (dom/get-data field :model)
        field-key (dom/get-data field :field-key)]
    (dom/set-data! field :model (assoc model field-key new-value))
    (fx/focus! field)
    (view-mode! field)
    (fx/fade-checkmark (value-container field))))

(defn- process-result [field on-update result]
  (if-let [errors (when (map? result) (seq (:errors result)))]
    (display-errors field (util/errors->messages errors))
    (do (value-updated field result)
      (on-update))))

(defn- do-remote-save [field remote-call on-update]
  (when (edit-mode? field)
    (let [input-n (input-node field)
          model (dom/get-data field :model)
          field-key (dom/get-data field :field-key)
          existing-value (get model field-key)
          new-value (inplace-value input-n)]
      (hide-errors field)
      (fx/add-spinner (input-container field))
      (if (= existing-value new-value)
        (value-updated field existing-value)
        (remote-call model field-key new-value
          {:on-success (partial process-result field on-update)
           :on-error #(display-errors field [(str "Error: " %)])})))))

(defn- escape-edit-mode! [field]
  (view-mode! field)
  (fx/focus! field))

(defn- stop-key-propagation [field e]
  (when (edit-mode? field) (event/stop-propagation e)))

(defn- check-key-for-activation [field e]
  (when (or (util/ENTER? e) (util/SPACE? e))
    (edit-mode! field)))

(defn prevent-form-submit [e]
  (when (util/ENTER? e)
    (event/stop-propagation e)
    (event/prevent-default e)))

(defn activate-inplace-field!
  ([field model field-key remote-call]
    (activate-inplace-field! field model field-key remote-call #()))
  ([field model field-key remote-call on-update]
    (when (dom/single-node field)
      (dom/set-attr! field "tabindex" "0")
      (dom/set-data! field :model model)
      (dom/set-data! field :field-key field-key)
      (let [value-n (value-node field)
            input-n (input-node field)]
        (view-mode! field)
        (event/listen! (value-container field) :click #(edit-mode! field))
        (event/listen! field :keypress prevent-form-submit)
        (event/listen! field :keyup (partial check-key-for-activation field))
        (event/listen! input-n :keyup (partial stop-key-propagation field))
        (register-update-callback input-n #(do-remote-save field remote-call on-update))
        (register-escape-callback input-n #(escape-edit-mode! field))
        )
      (dom/set-data! field :activated? true))))

(defn create-inplace-field [model field-key remote-call]
  (let [field (dom/single-node (h/html (inplace-field "randomid")))]
    (activate-inplace-field! field model field-key remote-call)
    field))
