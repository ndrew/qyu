(ns qyu.ui
  (:require
    [rum.core :as rum]

    [qyu.keys :as keys]
    [qyu.db :as db]
    [qyu.sockets :as s]

    [clojure.set :as set]
    [clojure.string :as str]
    ))



(rum/defc header [state]
  [:.toolbar
      [:.logo "qyu"]
      [:ul 
        [:li.search {:key "search"} [:input {:type "search"}]]
        [:li        {:key "btns"} [:button "?"]]
        ]
    ]
  )

(rum/defc links [state]
      [:.app
      [:.links
        [:.header "Today"]
        [:.link 
          [:a {:href "#"} "A sample link!"] [:span.status "opened 1 day ago"] ]
      ]
  ;;   
    [:hr] 
      [:div "[" (:count state) "] " (pr-str (:message state)) ]
    ]
)


(rum/defc debug [state]
    [:.state {:key "state"} 
      (pr-str (dissoc state :db))
    ]
)

(rum/defc footer < rum/static [] 
  [:p.footer "Authors: ndrew, aigor"]
  )

(rum/defc app < rum/reactive [*state]
  (let [state (rum/react *state)]

  [:.container 
    (header state)
    (links state)
    (debug state)
    (footer)
    ])
)
