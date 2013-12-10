(defproject com.8thlight/filament "1.0.0"
  :description "Rich client utilities"
  :url "http://github.com/8thlight/filament"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[hiccups "0.2.0"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojars.trptcolin/core.async "0.1.242.1"] ; waiting on core/merge -> cljs.core/merge fix
                 [org.clojars.trptcolin/domina "1.0.2.1"] ; waiting on release including https://github.com/levand/domina/pull/65
                 ]

  :profiles {:dev {:dependencies [[org.clojure/clojurescript "0.0-2014"]
                                  [speclj "2.9.1"]
                                  [specljs "2.9.1"]]}}
  :plugins [[speclj "2.9.1"]
            [specljs "2.9.1"]
            [lein-cljsbuild "1.0.0"]]

  :cljsbuild ~(let [run-specs ["bin/specljs" "js/filament.js"]]
                {:builds {:dev {:source-paths ["src/cljs" "spec/cljs"]
                                :compiler {:output-to "js/filament.js"
                                           :optimizations :whitespace
                                           :pretty-print true}
                                :notify-command run-specs}}
                 :test-commands {"test" run-specs}})

  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["spec/clj"])
