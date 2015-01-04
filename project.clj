(defproject quing "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main quing.core
  :jvm-opts ["-Xdock:icon=resources/img/bing.png"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.draines/postal "1.11.3"]
                 [clj-webdriver "0.6.1"]
                 [org.seleniumhq.selenium/selenium-server "2.44.0"]
                 [org.seleniumhq.selenium/selenium-java "2.44.0"]
                 [org.seleniumhq.selenium/selenium-remote-driver "2.44.0"]
                 [com.github.detro.ghostdriver/phantomjsdriver "1.1.0"
                  :exclusion [org.seleniumhq.selenium/selenium-java
                              org.seleniumhq.selenium/selenium-server
                              org.seleniumhq.selenium/selenium-remote-driver]]])
