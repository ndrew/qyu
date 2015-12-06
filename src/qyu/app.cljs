(ns qyu.app
  (:require
    [rum.core :as rum]
    [cognitect.transit :as t]

    [qyu.keys :as keys]


    [datascript.core :as d]
    [datascript.transit :as dt]

    [clojure.set :as set]
    [clojure.string :as str]
    ))

; first schema + connection (an atom)

(def schema {
             :qyu/title   {:db/index true}
             :qyu/url     {:db/index true}
             :qyu/tags    {:db/cardinality :db.cardinality/many}
             })

(defonce conn (d/create-conn schema))


(defonce *state (atom { :count 0
                        :message "Hello, world!"
                        :db conn
                         }))


(enable-console-print!)


(keys/register "ctrl+enter" #(print "HELP!"))
(keys/register "g" #(print "ggg!"))
(keys/register "shift+/" #(do
  (print "?????")
  (swap! *state assoc :message "???" )
  ))





(defn read-transit-str [s]
  (t/read (t/reader :json) s))


(defn write-transit-str [o]
  (t/write (t/writer :json ) o))



(declare send!)


(defonce socket
  (doto (js/WebSocket. (str "ws://" js/location.host "/api/websocket"))
    (aset "onmessage"
      (fn [payload]
        (let [message (read-transit-str (.-data payload))]
          (swap! *state #(-> %
                           (update :count inc)
                           (assoc :message message))))))
    (aset "onopen"
      #(send! "connected"))))


(defn send! [message]
  (when (== 1 (.-readyState socket)) ;; WS_OPEN
    (.send socket (write-transit-str message))))



(rum/defc app < rum/reactive []
  (let [state (rum/react *state)]
  [:.container 
    [:.toolbar
      [:.logo "qyu"]

      [:ul 
        [:li.search [:input {:type "search"}]]
        [:li [:button "?"]]
        ]
    ]

    [:.test 

      (pr-str (dissoc state :db))
    ]

    [:.app
      [:.links
        [:.header "Today"]
        [:.link 
          [:a {:href "#"} "A sample link!"] [:span.status "opened 1 day ago"] ]
      ]

  ;;   
     [:hr] 
      [:div "[" (:count state) "] " (pr-str (:message state)) ]


    [:p "Authors: ndrew, aigor"]

    ]])
)


(defn ^:export refresh []
  (send! "refreshed")
  (rum/mount (app) (js/document.querySelector "#app"))
  )


;;
;; db crap


;; logging of all transactions (prettified)
(d/listen! conn :log
  (fn [tx-report]
    (let [tx-id  (get-in tx-report [:tempids :db/current-tx])
          datoms (:tx-data tx-report)
          datom->str (fn [d] (str (if (:added d) "+" "−")
                               "[" (:e d) " " (:a d) " " (pr-str (:v d)) "]"))]
      (println
        (str/join "\n" (concat [(str "tx " tx-id ":")] (map datom->str datoms)))))))


(d/listen! conn :render
  (fn [_] 
    (refresh))
)

;; add stuff

(defonce canned-data [
  {
    :url "http://google.com"
    :tags ["testo" "pesto"]
    :title "Goooogle!"
  } 
  {
    :url "http://clojurecup.com"
    :tags []
    :title "Cloooojurecup"
  }
  ])



(defn remove-vals [f m]
  (reduce-kv (fn [m k v] (if (f v) m (assoc m k v))) (empty m) m))


#_(do 
(doseq [link canned-data]
 ;; form entity
    (let [entity (->> {
            :qyu/url   (:url link)
            :qyu/tags  (:tags link)
            :qyu/title (:title link)
            } (remove-vals nil?))
        ]
        (d/transact! conn [entity])
        ;; (js/alert "azaza")
        )
    )


;(println (:eavt @conn))

;; query stuff

(let [links (d/q '[:find ?e :where [?e :qyu/url _]] @conn)]
    (doall 
        (for [[eid] (->> links (sort-by first))
                    :let [entity (d/entity @conn eid)]]
        
        (do 
            (println entity)
            (println (:qyu/url entity))
            (println (:qyu/tags entity))
            )
    ))
)
)


