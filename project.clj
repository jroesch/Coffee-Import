(defproject Coffee-Import "0.0.0-SNAPSHOT"
  :main Coffee-Import.core
  :plugins [[lein-cljsbuild "0.2.7"]]
  :cljsbuild {
  	:builds [{
    	:source-path "src-cljs"
        :compiler {
          :output-to "build/js"
          :optimizations :whitespace 
          :pretty-print true}}]}
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]])