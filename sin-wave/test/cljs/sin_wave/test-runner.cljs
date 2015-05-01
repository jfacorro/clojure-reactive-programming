(ns sin_wave.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [sin_wave.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'sin_wave.core-test))
    0
    1))
