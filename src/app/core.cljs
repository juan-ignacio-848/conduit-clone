(ns app.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST json-response-format]]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.spec :as rss]
            [spec-tools.data-spec :as ds]))

(defonce api-uri "https://conduit.productionready.io/api")
(defonce articles-state (r/atom nil))
(defonce routes-state (r/atom nil))

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
  (swap! articles-state
         assoc
         :articles
         [(assoc (first (:articles @articles-state)) :tagList ["aabcabcabcabcabcbc"])])
  ((-> @routes-state :data :view))
  (rfe/href ::login)
  ,)

(defn header []
  [:nav {:class "navbar navbar-light"}
   [:div.container
    [:a.navbar-brand {:href (rfe/href ::home)} "conduit"]
    [:ul.nav.navbar-nav.pull-xs-right
     [:li.nav-item
      [:a.nav-link {:href (rfe/href ::home)} "Home"]]
     [:li.nav-item
      [:a.nav-link {:href (rfe/href ::login)} "Login"]]
     [:li.nav-item
      [:a.nav-link {:href (rfe/href ::register)} "Sign Up"]]]]])

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

(defn auth-signin [event]
  (.preventDefault event)
  (js/console.log "LOGIN"))

(defn auth-signup [event]
  (.preventDefault event)
  (js/console.log "REGISTER"))

(defn login-page []
  [:div.auth-page>div.container.page>div.row
   [:div.col-md-6.offset-md-3.col-xs-12
    [:h1.text-xs-center "Sign In"]
    [:p.text-xs-center [:a {:href (rfe/href ::register)} "Need an account?"]]
    [:form {:on-submit auth-signin}
     [:fieldset
      [:fieldset.form-group
       [:input.form-control.form-control-lg {:type :email :placeholder "Email"}]]
      [:fieldset.form-group
       [:input.form-control.form-control-lg {:type :password :placeholder "Password"}]]
      [:button.btn.btn-lg.btn-primary.pull-xs-right "Sign In"]]]]])

(defn register-page []
  [:div.auth-page>div.container.page>div.row
   [:div.col-md-6.offset-md-3.col-xs-12
    [:h1.text-xs-center "Sign Up"]
    [:p.text-xs-center [:a {:href (rfe/href ::login)} "Have an account?"]]
    [:form {:on-submit auth-signup}
     [:fieldset
      [:fieldset.form-group
       [:input.form-control.form-control-lg {:type :text :placeholder "Username"}]]
      [:fieldset.form-group
       [:input.form-control.form-control-lg {:type :email :placeholder "Email"}]]
      [:fieldset.form-group
       [:input.form-control.form-control-lg {:type :password :placeholder "Password"}]]
      [:button.btn.btn-lg.btn-primary.pull-xs-right "Sign In"]]]]])

(def routes
  [
   ["/" {:name ::home
         :view #'home-page}]
   ["/login" {:name ::login
              :view #'login-page}]
   ["/register" {:name ::register
              :view #'register-page}]])

(defn router-start! []
  (rfe/start!
   (rf/router routes {:data {:coercion rss/coercion}})
   (fn [m] (reset! routes-state m))
   {:use-fragment false}))

(defn app []
  [:div
   [header]
   (let [current-view (-> @routes-state :data :view)]
     (current-view))])

(defn ^:dev/after-load render
  "Render the toplevel component for this app."
  []
  (r/render [app] (.getElementById js/document "app")))

(defn ^:export main
  "Run application startup logic."
  []
  (router-start!)
  (browse-articles)
  (render))
