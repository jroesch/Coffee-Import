(ns Coffee-Import.core
  (:require clojure.java.io 
            [clojure.string :as string]
            [Coffee-Import.set :as iset])
  (:import java.io.File)
  (:gen-class :main true))

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

(defn get-contents [dir-name]
  (let [dir (new File dir-name)]
    (map #(.getAbsolutePath %) (filter #(not (.isHidden %)) (seq (.listFiles dir))))))

(defn expand-wild-cards 
  [fname]
  (let [matches (re-find #".*/\*" fname)]
    (if matches
      (get-contents 
	     (.substring fname 0 (- (.length fname) 1))) 
       fname)))

(defn lines [buffer] 
  (string/split buffer #"\n"))

(defn get-path [filename]
  (let [i (+ (.lastIndexOf filename "/") 1)]
    (.substring filename 0 i)))

(defn get-deps 
  "Retreive all dependencies from a file"
  [filename]
  (let [contents (read-file filename) ls (lines contents)]
  	(filter #(not (empty? %)) 
      (map extract-filename ls))))

(defn order-deps [st filename]
  (let [root  (get-path filename)
        resolve-path #(str root %)
        files (flatten (map #(expand-wild-cards (resolve-path %)) (get-deps filename)))]
  	(iset/insert (reduce order-deps st files) filename)))

(defn output-file [filename]
  (let [contents (read-file filename)]
    (str "#" filename "\n" contents)))

(defn -main
  "Takes a filename and returns a concatenated fully resolved version."
  [& args]
  (if (nil? args)
    (println "Please provide a filename: coffee-import <filename>")
    (let [file   (first args)
          files  ((order-deps iset/empty-set file) :show)
          output (map output-file files)]
      (do
        ;(println "#Ordering: ")
        ;(map #(println (str "#" %)) files)
      	(print (reduce #(str %1 %2 "\n\n") output))))))