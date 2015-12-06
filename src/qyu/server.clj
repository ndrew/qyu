(ns qyu.server
  (:require
    [compojure.core :as compojure]
    [compojure.route :as route]
    [compojure.handler :refer [site]]
    [org.httpkit.server :as httpkit]
    [ring.util.response :as response]
    [ring.middleware.session :as session]
    [ring.middleware.reload :as reload]
    [cognitect.transit :as t]
    [me.raynes.fs :as fs]
    )
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


; --- Application config -------------------------------------------------------

(def app-port 8080)
(def app-db-path (if-let [p (System/getenv "QYU_DB")] p "db/"))
(def app-db-dir (fs/file app-db-path))
(def user "test")


(defn handle-message[chan message]
  (let [user-db-file (str (.getAbsolutePath app-db-dir) "/" user ".edn")]
    (if-let [db (:db message)]
      (do
        (println "storing")
        
        (spit user-db-file db)
        
        (httpkit/send! chan (write-transit-str ["pong" "saved"]))
        ;(println db)
        ;(println (pr-str (fs/list-dir app-db-dir)))
      )
      (if (:load-db message)
        (let [db (slurp user-db-file)] 

          (httpkit/send! chan (write-transit-str ["db" db]))
          )
        (do
          (println "Recieved:" message)
          (httpkit/send! chan (write-transit-str ["pong" message]))
          )
        ) 
      
      ) 
    )

  )


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
              (handle-message chan message)         

            )))))



  (compojure/GET "/" [] (response/resource-response "public/index.html"))
  (compojure/POST "/update-links" {:as req} (do
    (println (str "[DEBUG] Update links requested: " {:session req}))
    (response/response "Updated")))
  (route/resources "/" {:root "public"}))

(def app-with-session
  (-> (site app)
      session/wrap-session))


; --- Application startup ------------------------------------------------------
(defn in-dev? [_] true) ;; TODO read a config variable from command line, env, or file?

(defn -main [& args] ;; entry point, lein run will pick up and start from here

  (println (str "working with " app-db-path))

  (let [handler (if (in-dev? args)
                  (reload/wrap-reload (site #'app-with-session)) ;; only reload when dev
                  (site app-with-session))]
    (httpkit/run-server handler {:port app-port})))
