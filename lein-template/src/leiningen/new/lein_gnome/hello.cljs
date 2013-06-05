(ns hello)

(def main js/imports.ui.main)

(def text (js/imports.gi.St.Label. (js-obj "style_class" "helloworld-label"
                                           "text" "Hello, ClojureScript!")))

(def button (js/imports.gi.St.Bin. (js-obj "style_class" "panel-button"
                                           "reactive" true
                                           "can_focus" true
                                           "x_fill" true
                                           "y_fill" false
                                           "track_hover" true)))

(def icon (js/imports.gi.St.Icon. (js-obj "icon_name" "system-run"
                                          "style_class" "system-status-icon")))

(defn hide-hello []
  (.. main -uiGroup (remove_actor text)))

(defn show-hello []
  (.. main -uiGroup (add_actor text))
  (set! (.-opacity text) 255)
  (let [monitor (.. main -layoutManager -primaryMonitor)]
    (.set_position text
                   (Math/floor (- (/ (.-width monitor) 2)
                                  (/ (.-width text) 2)))
                   (Math/floor (- (/ (.-height monitor) 2)
                                  (/ (.-height text) 2))))
    (.addTween js/imports.ui.tweener
               text (js-obj "opacity" 0
                            "time" 2
                            "transition" "easeOutQuad"
                            "onComplete" hide-hello))))

(defn init []
  (.set_child button icon)
  (.connect button "button-press-event" show-hello))

(defn enable []
  (.. main -panel -_rightBox (insert_child_at_index button 0)))

(defn disable []
  (.. main -panel -_rightBox (remove_child button)))

(this-as self
         (set! (.-init self) (fn [] (init) nil))
         (set! (.-enable self) enable)
         (set! (.-disable self) disable))
