(ns quing.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [postal.core :as postal]
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
                        (.setCapability "phantomjs.cli.args" (into-array String ["--webdriver-loglevel=WARN"]))))}))

(defn random-words [n]
  (with-open [rdr (io/reader "/usr/share/dict/words")]
    (doall (take n (shuffle (line-seq rdr))))))

(defn report []
  (postal/send-message (get-in config [:email :server])
                       (update-in (get-in config [:email :msg]) [:body] concat
                                  (for [[user] (:users config)]
                                    {:type :inline
                                     :content (io/file (str user ".png"))})))
  (doseq [[user] (:users config)]
    (io/delete-file (str user ".png"))))

(defn click-offer []
  (to "https://www.bing.com")
  (click "[title=\"Notification Center\"]")
  (Thread/sleep 500)
  (switch-to-frame "[id=bepfm]")
  (when-let [e (find-element {:xpath "//a/div/div[contains(.,'Earn ')]"})]
    (doto e click)))

(defn do-searches [user pass platform searches]
  (Thread/sleep 500)
  (println "Creating driver")
  (set-driver! (create-driver platform))
  (implicit-wait 5000)
  (println "Created")
  (try
    (to "https://login.live.com")
    (println "At login")
    (quick-fill-submit {"[name=login]" user}
                       {"[name=passwd]" pass}
                       {"[name=passwd]" submit})

    (println "***" user platform "***")
    (to "https://www.bing.com")
    (doseq [word (random-words (+ (rand-int 10) searches))]
        (Thread/sleep (+ (rand-int 2000) 1000))
        (quick-fill-submit {"[name=q]" clear}
                           {"[name=q]" word}
                           {"[name=q]" submit})
        (print word " ")
        (flush))
    (println "<")

    (when (= platform :pc)
      (while (click-offer)
        (println "Earnt credit"))
      (to "http://www.bing.com/rewards/dashboard")
      (take-screenshot :file (str user ".png")))

    (catch Exception e
      (println user platform "failed")
      (println e))

    (finally (quit))))

(defn -main []
  (doseq [platform [:mobile :pc]
          [user pass pc-searches mobile-searches] (shuffle (:users config))
          :let [searches (case platform
                           :mobile mobile-searches
                           :pc pc-searches)]]
    (do-searches user pass platform searches))
  (println "Reporting")
  (report)
  (println "*** Done ***"))
