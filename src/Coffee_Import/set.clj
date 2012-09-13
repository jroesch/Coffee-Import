(ns Coffee-Import.set)

(defn elem? [x xs] 
  (reduce #(or %1 (= x %2)) false xs))

(defn uconj [coll e]
  (if (elem? e coll)
    coll
    (conj coll e)))

(defn -wrap-set [st ls]
  ;; NOTE:: Use vector grows left instead of right 
  (fn [method & args]
      (case method
        :show    ls 
        :insert (let [v (first args)]
                  (-wrap-set (conj st v) (uconj ls v)))
        :member (let [v (first args)]
                  (st v)))))

(def empty-set (-wrap-set #{} []))

(defn insert [s v]
  (s :insert v))

(defn insert-many 
  "naive impl for now"
  ;; Catamorphism from List -> Set?
  [s vs] (reduce insert s vs))