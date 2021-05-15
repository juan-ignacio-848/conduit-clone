(ns app.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST json-response-format]]))

(defonce api-uri "https://conduit.productionready.io/api")
(defonce articles-state (r/atom nil))

(defn handler [response]
  (reset! articles-state response))

(defn error-handler [{:keys [status status-text]}]
  (js/console.log (str "Error: " status " " status-text)))

(defn browse-articles []
  (GET (str api-uri "/articles?limit=20")
       {:handler handler
        :response-format (json-response-format {:keywords? true})
        :error-handler error-handler}))

(comment
  (browse-articles)
  @articles-state
  ,)

(defn header []
  [:nav {:class "navbar navbar-light"}
   [:div.container
    [:a.navbar-brand "conduit"]]])

(defn banner [token]
  (when-not token
    [:div.banner>div.container
     [:h1.logo-front "conduit"]
     [:p "A place to share your knowledge"]]))

(defn article-preview
  [{:keys [title description favoritesCount author createdAt tagList]}]
  [:div.article-preview
   [:div.article-meta
    [:a
     [:img {:src (:image author)}]]]
   [:div.info
    [:a.author (:username author)]
    [:span.date (.toDateString (new js/Date createdAt))]]
   [:div.pull-xs-right
    [:button.btn.btn-sm.btn-outline-primary
     [:i.ion-heart favoritesCount]]]
   [:a.preview-link
    [:h1 title]
    [:p description]
    [:span "Read more..."]
    [:ul.tag-list
     (for [tag tagList]
       ^{:key tag} [:li.tag-default.tag-pill.tag-outline tag])]]])

(defn articles [items]
  (if-not (seq items)
    [:div.article-preview "Loading..."]
    [:div
     (for [article items]
       ^{:key article}
       [article-preview article])]))

(defn main-view []
  [:div.col-md-9
   [:div.feed-toggle
    [:ul.nav.nav-pills.outline-active
     [:li.nav-item
      [:a.nav-link.active {:href ""} "Global Feed"]]]]
   [articles (:articles @articles-state)]])

(defn home-page []
  [:div.home-page
   [banner]
   [:div.container.page>div.row
    [main-view]
    [:div.col-md-3
     [:div.sidebar
      [:p "Popular tags"]]]]])

(defn app []
  [:div
   [header]
   [home-page]])

(defn ^:dev/after-load render
  "Render the toplevel component for this app."
  []
  (r/render [app] (.getElementById js/document "app")))

(defn ^:export main
  "Run application startup logic."
  []
  (browse-articles)
  (render))
