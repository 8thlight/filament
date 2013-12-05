(ns com.eighthlight.filament.collapsable
  (:require [com.eighthlight.filament.util :as util]
            [com.eighthlight.filament.fx :as fx]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as event]))

(defn animate-out [subject animation]
  (if (= :fade animation)
    (fx/fade-out subject)
    (fx/hide! subject)))

(defn animate-in [subject animation]
  (if (= :fade animation)
    (fx/fade-in subject)
    (fx/show! subject)))

(defn- collapse! [switch subject]
  (animate-out subject (dom/get-data switch :animation))
  (dom/remove-class! switch "expanded")
  (dom/add-class! switch "collapsed"))

(defn- expand! [switch subject]
  (animate-in subject (dom/get-data switch :animation))
  (dom/remove-class! switch "collapsed")
  (dom/add-class! switch "expanded"))

(defn toggle [switch subject]
  (if (fx/visible? subject)
    (collapse! switch subject)
    (expand! switch subject)))

(defn activate-collapsable! [switch subject & args]
  (when (and (dom/single-node switch) (dom/single-node subject))
    (if (fx/visible? subject)
      (dom/add-class! switch "expanded")
      (dom/add-class! switch "collapsed"))
    (let [options (util/->options args)]
      (dom/set-data! switch :animation (or (:animation options) :fade)))
    (event/listen! switch :click #(toggle switch subject))
    (dom/set-data! switch :activated? true)))
