(ns cljs.repl.gnome
  (:require [clojure.java.io :as io]
            [cljs.analyzer :as ana]
            [cljs.repl :as repl]
            [clj-http.client :as client]
            [cemerick.piggieback :as piggieback]))

(defn setup [repl-env]
  (let [env (ana/empty-env)]
    #_(repl/load-file repl-env "cljs/core.cljs")
    #_(swap! (:loaded-libs repl-env) conj "cljs.core")
    #_(repl/evaluate-form repl-env env "<cljs repl>" '(ns cljs.user))))

(defn repl-url [repl-env]
  (str "http://" (:host repl-env) ":" (:port repl-env) "/evaluate"))

(defn evaluate [repl-env filename line code]
  (prn "code is:")
  (prn code)
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

(defn repl-env
  "Create a Node.js REPL environment."
  [& {:keys [host port]}]
  (let [base (io/resource "goog/base.js")
        deps (io/resource "goog/deps.js")
        new-repl-env (GnomeEnv. host port (atom #{}))]
    (assert base "Can't find goog/base.js in classpath")
    (assert deps "Can't find goog/deps.js in classpath")
    #_(load-resource new-repl-env "goog/base.js")
    #_(load-resource new-repl-env "goog/deps.js")
    new-repl-env))

(defn run-gnome-repl [& args]
  (repl/repl (apply repl-env args)))

(defn nrepl-env [& args]
  (setup (apply repl-env args)))

(defn run-gnome-nrepl [& args]
  (piggieback/cljs-repl :repl-env (apply nrepl-env args)))

;; (def env (repl-env :host "localhost" :port 1080))
;; (repl-url env)
;; (evaluate env "foo" 1 "1")
;; => 1
;; (load-resource env "goog/base.js")
;; (repl/evaluate-form env (ana/empty-env) "foo" '(+ 1 1))
;; => 2
;; (repl/evaluate-form env (ana/empty-env) "foo" '(conj nil 1))
;; => (1)
;; (repl/evaluate-form env (ana/empty-env) "foo" 'conj)
;; => nil
;; (repl/evaluate-form env (ana/empty-env) "foo" '*cljs-ns*)
;; => nil
;; (repl/load-file env "cljs/core.cljs")
;; (setup env)
;; (run-gnome-repl :host "localhost" :port 1080)
;; (run-gnome-nrepl :host "localhost" :port 1080)
