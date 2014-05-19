(ns quing.core
  (:require [clojure.edn :as edn]
            [clj-webdriver.taxi :refer :all]
            [clj-webdriver.driver :refer [init-driver]])
  (:import [org.openqa.selenium.phantomjs PhantomJSDriver]
           [org.openqa.selenium.remote DesiredCapabilities]))


(def config (edn/read-string (slurp "quing.config")))

(def user-agent
  {:mobile "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25"
   :pc "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/537.13+ (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2"})

(defn create-driver [k]
  (init-driver
   {:webdriver
    (PhantomJSDriver. (doto (DesiredCapabilities.)
                        (.setCapability "phantomjs.page.settings.userAgent" (user-agent k))
                        (.setCapability "phantomjs.page.customHeaders.Accept-Language" "en-US")
                        (.setCapability "phantomjs.page.customHeaders.Connection" "keep-alive")
                        (.setCapability "phantomjs.cli.args" (into-array String ["--ignore-ssl-errors=true"
                                                                                 "--webdriver-loglevel=WARN"]))))}))

(defn random-words [n]
  (with-open [rdr (clojure.java.io/reader "/usr/share/dict/words")]
    (doall (take n (shuffle (line-seq rdr))))))

(defn -main []
  (doseq [platform [:mobile :pc]
          [user pass pc-searches mobile-searches] config]
    (println user platform)
    (set-driver! (create-driver platform))

    (to "https://login.live.com")
    (quick-fill-submit {"[name=login]" user}
                       {"[name=passwd]" pass}
                       {"[name=passwd]" submit})

    (to "https://www.bing.com")
    (doseq [word (random-words (+ (rand-int 10) (case platform
                                                  :mobile mobile-searches
                                                  :pc pc-searches)))]
      (Thread/sleep (+ (rand-int 1000) 500))
      (quick-fill-submit {"[name=q]" clear}
                         {"[name=q]" word}
                         {"[name=q]" submit}))
    (quit)))
