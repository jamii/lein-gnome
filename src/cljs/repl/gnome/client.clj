(ns cljs.repl.gnome.client
  (:require [org.httpkit.client :as client]
            [org.httpkit.server :as server]
            [cljs.repl :as repl]
            [cljs.closure :as cljsc]
            [cemerick.piggieback :as piggieback]))

(defn repl-url [repl-env command]
  (str "http://" (:js-host repl-env) ":" (:js-port repl-env) "/" command))

(defn post [repl-env command body]
  @(client/post (repl-url repl-env command) {:body (str body) :as :text}))

(declare -evaluate)

(defn -setup [repl-env]
  (post repl-env "setup" (select-keys repl-env [:clj-host :clj-port]))
  (-evaluate repl-env "<cljs repl>" 1 (cljsc/-compile '[(ns cljs.user)] {})))

(defn -evaluate [repl-env filename line code]
  (let [response (post repl-env "evaluate" {:filename filename :line line :code code})]
    (if (= 200 (:status response))
      (read-string (:body response))
      {:status :error :value response})))

(defn -load [repl-env ns url]
  (let [missing (remove #(contains? @(:loaded-libs repl-env) %) ns)]
    (when (seq missing)
      (-evaluate repl-env (.toString url) 1 (slurp url))
      (swap! (:loaded-libs repl-env) (partial apply conj) missing))))

(defn -tear-down [repl-env]
  (let [server (:server repl-env)]
    (server) ;; stops the server
    ))

(defrecord GnomeEnv [js-host js-port clj-host clj-port loaded-libs server]
  repl/IJavaScriptEnv
  (-setup [this]
    (-setup this))
  (-evaluate [this filename line js]
    (-evaluate this filename line js))
  (-load [this ns url]
    (-load this ns url))
  (-tear-down [this]
    (-tear-down this)))

(let [out *out*]
  (defn print-handler [request]
    (binding [*out* out]
      ;; for some reason the http-kit server doesn't support {:as :text}
      (let [bytes (.bytes (:body request))
            charset (java.nio.charset.Charset/forName "utf8")
            body (String. bytes 0 (alength bytes) charset)]
        (try (prn (read-string body))
         (catch Exception e
           (if (string? body)
             (println body)
             (prn nil)))))
      {:status 200})))

(defn repl-env [& {:keys [js-host js-port clj-host clj-port]
                   :or {js-host "localhost"
                        js-port 6034
                        clj-host "localhost"
                        clj-port 6044}}]
  (let [server (server/run-server print-handler {:ip clj-host :port clj-port :threads 1})]
    (GnomeEnv. js-host js-port clj-host clj-port (atom #{}) server)))

(defn run-gnome-repl [& args]
  (repl/repl (apply repl-env args)))

(defn run-gnome-nrepl [& args]
  (piggieback/cljs-repl
   :repl-env (doto (apply repl-env args) -setup)
   :eval piggieback/cljs-eval))
