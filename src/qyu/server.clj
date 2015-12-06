(ns qyu.server
  (:require
    [compojure.core :as compojure]
    [compojure.route :as route]
    [compojure.handler :refer [site]]
    [org.httpkit.server :as httpkit]
    [ring.util.response :as response]
    [ring.middleware.session :as session]
    [ring.middleware.reload :as reload]
    [cognitect.transit :as t])
  (:gen-class))

; --- Transit utilities --------------------------------------------------------
(defn read-transit-str [s]
  (-> s
      (.getBytes "UTF-8")
      (java.io.ByteArrayInputStream.)
      (t/reader :json)
      (t/read)))

(defn write-transit-str [o]
  (let [os (java.io.ByteArrayOutputStream.)]
    (t/write (t/writer os :json) o)
    (String. (.toByteArray os) "UTF-8")))

; --- Application web endpoints (routes) ---------------------------------------
(compojure/defroutes app
  (compojure/GET "/api/websocket" [:as req]
    (httpkit/with-channel req chan
      (println "Client connected")

      (httpkit/on-close chan
        (fn [status]
          (println "Client disconnected")))

      (httpkit/on-receive chan
        (fn [payload]
          (let [message (read-transit-str payload)]
            (println "Recieved:" message)
            (httpkit/send! chan (write-transit-str ["pong" message])))))))
  (compojure/GET "/" [] (response/resource-response "public/index.html"))
  (compojure/GET "/update-links" [:as req] (response/response "It is ok."))
  (route/resources "/" {:root "public"}))

; --- Session managment --------------------------------------------------------

; (defn set-session-var [session]
;   (if (:my-var session)
;     {:body "Session variable already set"}
;     {:body "Nothing in session, setting the var"
;      :session (assoc session :my-var "foo")}))
;
; (defroutes sessiontest-routes
;   (ANY "/" {session :session} (set-session-var session))
;   (route/not-found "Page not found"))
;
; (def sessiontest-app
;   (-> sessiontest-routes
;       session/wrap-session))
;
; (defn start-server []
;   (future (jetty/run-jetty (var sessiontest-app) {:port 8080})))

; --- Application config -------------------------------------------------------
(def app-port 8080)
(def app-db-path "db/links")

; --- Application startup ------------------------------------------------------
(defn in-dev? [_] true) ;; TODO read a config variable from command line, env, or file?

(defn -main [& args] ;; entry point, lein run will pick up and start from here
  (let [handler (if (in-dev? args)
                  (reload/wrap-reload (site #'app)) ;; only reload when dev
                  (site app))]
    (httpkit/run-server handler {:port app-port})))

; (defn -main [& args]
;   (println (str "Starting server at port " app-port))
;   (httpkit/run-server #'app {:port app-port}))
