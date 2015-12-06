(ns qyu.db
  (:require
    [cognitect.transit :as t]

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


(defn listen! [id f]
	(d/listen! conn id f))



;; logging of all transactions (prettified)
#_(d/listen! conn :log
  (fn [tx-report]
    (let [tx-id  (get-in tx-report [:tempids :db/current-tx])
          datoms (:tx-data tx-report)
          datom->str (fn [d] (str (if (:added d) "+" "−")
                               "[" (:e d) " " (:a d) " " (pr-str (:v d)) "]"))]
      (println
        (str/join "\n" (concat [(str "tx " tx-id ":")] (map datom->str datoms)))))))



(defn get-serialized-db[]
	(dt/write-transit-str @conn)
)


;; persisting DB between page reloads
(defn persist [db]
  (js/localStorage.setItem "qyu/DB" (dt/write-transit-str db)))


(defn remove-vals [f m]
  (reduce-kv (fn [m k v] (if (f v) m (assoc m k v))) (empty m) m))


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


(defn from-local-storage! []
	(or
    	(when-let [stored (js/localStorage.getItem "qyu/DB")]
      		(let [stored-db (dt/read-transit-str stored)]
        		(when (= (:schema stored-db) schema) ;; check for code update
					(reset! conn stored-db)
		  			;;(swap! history conj @conn)
			        true
	          )))
    (do
		#_(doseq [link canned-data]
		 ;; form entity
		    (let [entity (->> {
		            :qyu/url   (:url link)
		            :qyu/tags  (:tags link)
		            :qyu/title (:title link)
		            } (remove-vals nil?))
		        ]
		        (d/transact! conn [entity])
		        )
		    )

    	;"adding local fixtures"
    ;;(d/transact! conn u/fixtures)
  ;;  )
	)

	conn
)



;; add stuff









;(println (:eavt @conn))

;; query stuff

#_(let [links (d/q '[:find ?e :where [?e :qyu/url _]] @conn)]
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
