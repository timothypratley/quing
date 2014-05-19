(ns quing.core-test
  (:require [clojure.test :refer :all]
            [quing.core :refer :all]))

(deftest a-test
  (is (every? string? (random-words 10))))
