(ns cljs.gnome)

(defn log-errors [name f]
  (fn []
    (try
      (js/log (str "Trying " name))
      (f)
      (catch js/Object exc
        (js/log (str "Error in " name) exc)
        (js/log (if (.hasOwnProperty exc "stack")
                        (.-stack exc)
                        "No stacktrace available."))))
    nil ;; Without this nil we get "TypeError: extension.stateObj.enable is not a function"
    ))

(defn defextension! [{:keys [init enable disable]}]
  (this-as self
           (set! (.-init self) (log-errors "init" init))
           (set! (.-enable self) (log-errors "enable" enable))
           (set! (.-disable self) (log-errors "disable" disable))))
