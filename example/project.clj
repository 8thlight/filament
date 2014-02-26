(defproject example "1.0.0"
  :description "Example App using com.8thlight/Filament"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[com.8thlight/filament "1.2.1"]
                 [com.8thlight/hiccup "1.1.1"]
                 [org.clojure/clojure "1.5.1"]]

  :profiles {:dev {:dependencies [[org.clojure/clojurescript "0.0-2014"]
                                  [speclj "3.0.0"]]}}
  :plugins [[speclj "3.0.0"]
            [lein-cljsbuild "1.0.0"]]

  :cljsbuild ~(let [run-specs ["bin/specljs" "js/example_dev.js"]]
          {:builds {:dev {:source-paths ["src/cljs" "spec/cljs"]
                               :compiler {:output-to "js/example_dev.js"
                                          :optimizations :whitespace
                                          :pretty-print true}
                          :notify-command run-specs}}

              :test-commands {"test" run-specs}})

  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["spec/clj"])
