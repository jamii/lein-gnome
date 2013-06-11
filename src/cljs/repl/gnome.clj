(ns cljs.repl.gnome
  (:require [clojure.java.io :as io]
            [cljs.repl :as repl]
            [cljs.closure :as cljsc]
            [clj-http.client :as client]
            [cemerick.piggieback :as piggieback]))

(declare evaluate)

(defn setup [repl-env]
  (evaluate repl-env "<cljs repl>" 1
            (cljsc/-compile
             '[(ns cljs.user)
               #_(set! *print-fn* clojure.browser.repl/repl-print)]
             {})))

(defn repl-url [repl-env]
  (str "http://" (:host repl-env) ":" (:port repl-env) "/evaluate"))

(defn evaluate [repl-env filename line code]
  (let [body (str {:filename filename :line line :code code})
        response (client/post (repl-url repl-env) {:body body})]
    (if (= 200 (:status response))
      (read-string (:body response))
      {:status :error :value response})))

(defn load [repl-env ns url]
  (let [missing (remove #(contains? @(:loaded-libs repl-env) %) ns)]
    (when (seq missing)
      (evaluate repl-env (.toString url) 1 (slurp url))
      (swap! (:loaded-libs repl-env) (partial apply conj) missing))))

(defn tear-down [repl-env])

(defrecord GnomeEnv [host port loaded-libs]
  repl/IJavaScriptEnv
  (-setup [this]
    (setup this))
  (-evaluate [this filename line js]
    (evaluate this filename line js))
  (-load [this ns url]
    (load this ns url))
  (-tear-down [this]
    (tear-down this)))

(defn load-resource
  "Load a JS file from the classpath into the REPL environment."
  [repl-env filename]
  (let [resource (io/resource filename)]
    (assert resource (str "Can't find " filename " in classpath"))
    (evaluate repl-env filename 1 (slurp resource))))

(defn repl-env [& {:keys [host port]}]
  (GnomeEnv. host port (atom #{})))

(defn run-gnome-repl [& args]
  (repl/repl (apply repl-env args)))

(defn nrepl-env [& args]
  (setup (apply repl-env args)))

(defn run-gnome-nrepl [& args]
  (piggieback/cljs-repl :repl-env (apply nrepl-env args)))

;; (use 'cljs.repl.gnome)
;; (run-gnome-repl :host "localhost" :port 1080)
;; (run-gnome-nrepl :host "localhost" :port 1080)
