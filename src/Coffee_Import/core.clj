(ns Coffee-Import.core
  (:require clojure.java.io 
            [clojure.string :as string]
            [Coffee-Import.set :as iset]))

(defn read-file
  "Takes a filename and returns its contents"
  [filename]
  (let [read-contents (fn [r]
    (loop [contents ""]
      (let [line (.readLine r)]
        (if (= line nil)
            contents
          (recur (str contents line "\n"))))))]
    (read-contents (clojure.java.io/reader filename))))

(defn or-empty 
  "Either perform identity on a Sequence or returns an Empty Sequence."
  [x] (if x x '()))

(defn extract-filename 
  "Retrieves the relative name of a dependency"
  [line]
  (let [matches (or-empty (re-find #"#import ((\"|').*(\"|'))" line))]
    (string/replace (nth matches 1 "") "\"" "")))

(defn lines [buffer] 
  (string/split buffer #"\n"))

(defn get-path [filename]
  (let [i (+ (.lastIndexOf filename "/") 1)]
    (.substring filename 0 i)))

(defn get-deps 
  "Retreive all dependencies from a file"
  [filename]
  (let [contents (read-file filename) ls (lines contents)]
  	(filter #(not (empty? %)) (map extract-filename ls))))

(defn order-deps [st filename]
  (let [root  (get-path filename)
        files (map #(str root %) (get-deps filename))]
  	(iset/insert (reduce order-deps st files) filename)))

(defn -main
  "Takes a filename and returns a concatenated fully resolved version."
  [& args]
  (if (nil? args)
    (println "Please provide a filename: coffee-import <filename>")
    (let [file   (first args)
          files  ((order-deps iset/empty-set file) :show)
          output (map read-file files)]
      (print (reduce #(str %1 %2 "\n\n") output)))))