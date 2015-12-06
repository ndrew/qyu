(ns qyu.ui
  (:require
    [rum.core :as rum]

    [qyu.keys :as keys]
    [qyu.db :as db]
    [qyu.sockets :as s]

    [clojure.set :as set]
    [clojure.string :as str]
    ))



(rum/defc app < rum/reactive [*state]
  (let [state (rum/react *state)]

  [:.container 
    [:.toolbar
      [:.logo "qyu"]

      [:ul 
        [:li.search [:input {:type "search"}]]
        [:li [:button "?"]]
        ]
    ]

    [:.state {:key "state"} 
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

    ]

    ])
)
