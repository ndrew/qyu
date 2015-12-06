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

; TODO:
; - Add session managment
; - Add db data saving


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
  (compojure/POST "/update-links" {:as req} (do
    (println (str "[DEBUG] Update links requested: " {:session req}))
    (response/response "Updated")))
  (route/resources "/" {:root "public"}))

(def app-with-session
  (-> (site app)
      session/wrap-session))

; --- Application config -------------------------------------------------------
(def app-port 8080)
(def app-db-path "db/links")

; --- Application startup ------------------------------------------------------
(defn in-dev? [_] true) ;; TODO read a config variable from command line, env, or file?

(defn -main [& args] ;; entry point, lein run will pick up and start from here
  (let [handler (if (in-dev? args)
                  (reload/wrap-reload (site #'app-with-session)) ;; only reload when dev
                  (site app-with-session))]
    (httpkit/run-server handler {:port app-port})))
