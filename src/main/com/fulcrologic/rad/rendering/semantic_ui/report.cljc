(ns com.fulcrologic.rad.rendering.semantic-ui.report
  (:require
    [clojure.string :as str]
    [taoensso.timbre :as log]
    [com.fulcrologic.rad.attributes :as attr]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.fulcro.components :as comp]
    #?(:cljs
       [com.fulcrologic.fulcro.dom :as dom]
       :clj
       [com.fulcrologic.fulcro.dom-server :as dom])
    [com.fulcrologic.rad.form :as form]))

(defmethod report/render-layout :default [this]
  (let [{::report/keys [source-attribute BodyItem parameters]} (comp/component-options this)
        {::report/keys [columns column-headings edit-form]} (comp/component-options BodyItem)
        id-key (some-> edit-form comp/component-options ::form/id)
        props  (comp/props this)
        rows   (get props source-attribute [])]
    (log/info "Rendering report layout")
    (dom/div
      (dom/div :.ui.top.attached.segment
        (dom/h3 :.ui.header
          (or (some-> this comp/component-options ::report/title) "Report")
          (dom/button :.ui.tiny.right.floated.primary.button {:onClick (fn [] (report/run-report! this))} "Run!"))
        (dom/div :.ui.form
          (map-indexed
            (fn [idx k]
              (dom/div :.ui.inline.field {:key idx}
                (dom/label (some-> k name str/capitalize))
                (report/render-parameter-input this k)))
            (keys parameters))))
      (dom/div :.ui.attached.segment
        (when (seq rows)
          (if (seq columns)
            (dom/table :.ui.table
              (dom/thead
                (dom/tr
                  (if (seq column-headings)
                    (map-indexed
                      (fn [idx h]
                        (dom/th {:key idx}
                          h))
                      column-headings)
                    (map-indexed
                      (fn [idx k]
                        (dom/th {:key idx}
                          (or
                            (some-> k attr/key->attribute ::report/column-header)
                            (some-> k name str/capitalize))))
                      columns))))
              (dom/tbody
                (map-indexed (fn [idx row]
                               (dom/tr {:key (str "row-" idx)}
                                 (map-indexed
                                   (fn [idx k]
                                     (dom/td {:key (str "col-" idx)}
                                       ;; TODO: Coercion
                                       (let [label (str (get row k))]
                                         (if (and edit-form (= 0 idx))
                                           (dom/a {:onClick (fn [] (form/edit! this edit-form (get row id-key)))} label)
                                           label))))
                                   columns))) rows)))
            (let [factory (comp/factory BodyItem)]
              (dom/div :.ui.list
                (mapv factory rows)))))))))


