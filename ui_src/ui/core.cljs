(ns ui.core
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.core :as core]
            [clojure.string :as string :refer [split-lines includes? trim]]
            ))

(def electron       (js/require "electron"))

(def ipc            (.-ipcRenderer electron))


(enable-console-print!)

(def shell (js/require "shell"))

(def fs (js/require "fs"))
(def path (js/require "path"))
(def current-dir (.resolve path "."))

(def filename (str current-dir "/bounce.config"))
(def commandFile (.readFileSync fs filename))
(def commands-broken (map #(string/split %1 #",") (split-lines commandFile)))
                 
(def command-map (apply merge (map #(hash-map (.toLowerCase (first %1)) (vec (rest %1))) commands-broken)))
(def commands (keys command-map))
(def currentCommand (atom nil))

(def join-lines (partial string/join "\n"))

(defonce searchText   (atom ""))
(defonce matches      (atom []))

(js/Notification. "Bounce" (clj->js {:body "Thanks for being an early adoptor of bounce."}))

(defonce proc (js/require "child_process"))

(defn findit []
  (if (empty? (trim @searchText))
    []
    (vec 
      (filter 
        (fn [command] 
          (and 
            (not (empty? command)) 
            (every? #(includes? command (trim %1)) (string/split @searchText " "))))
      commands))))


(defn runCommand []

  (let [s (println command-map )
        a @matches
        b @currentCommand
        z (get @matches @currentCommand)
        _ (println a b z)
        [type data] (command-map z)
        _ (println (str type data))]
    (do
      (cond (= type "link") (.openExternal shell data)
          :else (.openItem shell data))))
      (.send ipc "abort"))



(defn changeCurrentCommand [movement]
  (do
    (when (not (nil? @currentCommand))
      (reset! currentCommand (movement @currentCommand)))
    (reset! currentCommand (max @currentCommand 0))
    (reset! currentCommand (min @currentCommand (- (count @matches) 1)))
  ))



(defn selectCommand [event]
 (println (.-keyCode event)) 
 (cond (= 13 (.-keyCode event)) (runCommand)
       (= 40 (.-keyCode event)) (changeCurrentCommand #(+ %1 1))
       (= 38 (.-keyCode event)) (changeCurrentCommand #(- %1 1))))



(defn root-component []
  (let
      []
   ;[d (.send ipc "update-preference" (clj->js {:open-window-shortcut "ctrl+j"}) true)]
    
    [:div
      [:p.hide (str @currentCommand "/" (count @matches))]
      [:div
       [:input.search-bar
        {:type :text
         :on-key-down (fn [e] (selectCommand e))
         :on-change (fn [e]
                      (do (reset! searchText
                              (.-value (.-target e)))
                          (reset! matches (vec (take 5 (findit))))
                          (reset! currentCommand 0)))
         :value @searchText
         :placeholder "Bounce Search"
         :auto-focus true}]
       ]

     [:ol.search-results (for [item @matches] 
        ^{:key item} [:li.search-item {:class (if (= (.indexOf @matches item) @currentCommand) "current" "")} item])]]
     ))


(reagent/render
  [root-component]
  (js/document.getElementById "app-container"))

(.send ipc "update-preference"  "ctrl+j"  true)


