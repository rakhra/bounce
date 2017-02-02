(ns electron.core)

(def electron       (js/require "electron"))

(def ipc            (.-ipcMain electron))

;(def globalShortcut (js/require "global-shortcut"))

(def globalShortcut (.-globalShortcut electron))

(def menubar        (js/require "menubar"))

(def indexFile      (str "file://" js/__dirname "/public/index.html"))

(def mb             (menubar (clj->js { 
  :resizable false,
  :index indexFile,
  :width 800, 
  :height 350, 
  :preloadWindow true, 
  :windowPosition "center",
  :backgroundColor "#bf7fff"})))


; (def main-window (atom nil))
(enable-console-print!)


(defn registerShortcut [keybinding initialization]
  (do (.unregisterAll globalShortcut)
      (try
        (.register globalShortcut keybinding (fn []
          (if (.isVisible (.-window mb)) 
            (.hideWindow mb)
            (.showWindow mb))))
          (catch js/Object e
            (println e)))))

; (defn init-browser []
;   (reset! main-window (browser-window.
;                         (clj->js {:width 800
;                                   :height 600})))
;   ; Path is relative to the compiled js file (main.js in our case)
;   (.loadURL @main-window (str "file://" js/__dirname "/public/index.html"))
;   (.on @main-window "closed" #(reset! main-window nil)))

; CrashReporter can just be omitted
; (.start crash-reporter
;         (clj->js
;           {:companyName "MyAwesomeCompany"
;            :productName "MyAwesomeApp"
;            :submitURL "https://example.com/submit-url"
;            :autoSubmit false}))

; (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
;                                 (.quit app)))

;(.on (.-app mb) "ready" true)

(.on mb "show" (fn [] 
  (.send (.. mb -window -webContents) "show")))

(.on (.-app mb) "activate" (fn []
  (.showWindow mb)))

(.on ipc "update-preference" (fn [evt pref initialization]
      (registerShortcut pref initialization)))


(.on ipc "abort" (fn []
      (.hideWindow mb)))







