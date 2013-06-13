(ns cljs.repl.gnome.server
  (:use [cljs.reader :only [read-string]]))

(def session (new js/imports.gi.Soup.Session))

(defn print-fn [config & args]
  (js/log "printing" config args)
  (let [uri (new js/imports.gi.Soup.URI (str "http://" (:clj-host @config) ":" (:clj-port @config) "/"))
        _ (js/log "made uri")
        msg (new js/imports.gi.Soup.Message (js-obj "method" "POST" "uri" uri))]
    (js/log "made msg")
    (.. msg -request_body (append (apply pr-str args)))
    (js/log "set request body")
    (let [status (.send_message session msg)]
      (js/log "lein-gnome repl got status" status "while printing" args))))

(defn eval-here [code]
  (try {:status :success :value (js* "eval(~{code})")}
       (catch js/Error e
         {:status :exception :value (pr-str e)
          :stacktrace (if (.hasOwnProperty e "stack")
                        (.-stack e)
                        "No stacktrace available.")})))

(defn setup-handler [config server msg path query client]
  (reset! config (read-string (.. msg -request_body -data)))
  (js/log "lein-gnome repl got config" @config)
  (set! *print-fn* (partial print-fn config))
  (.. msg -response_body (append (pr-str :ok))))

(defn evaluate-handler [server msg path query client]
  ;; TODO error handling
  (let [{:keys [filename line code]} (read-string (.. msg -request_body -data))
        _ (js/log "lein-gnome repl is evaluating" code)
        result (eval-here code)
        _ (js/log "lein-gnome repl got result" result)]
    (set! (.-status_code msg) 200)
    (.. msg -response_body (append (pr-str result)))))

(defn server []
  (let [config (atom {})
        server (new js/imports.gi.Soup.Server (js-obj "port" 1080))]
    (.add_handler server "/setup" (partial setup-handler config))
    (.add_handler server "/evaluate" evaluate-handler)
    (.run_async server)
    server))
