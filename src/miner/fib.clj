(ns miner.fib)

(defn naive-fib [x]
  (if (<= x 1)
    1
    (+ (naive-fib (- x 1)) (naive-fib (- x 2)))))

(defn do-work [x]
  (naive-fib x))

; ckiehl.worker1

(defn foo
  []
  (let [work-chan (chan)
        count (atom 0)
        start (System/currentTimeMillis)
        end (+ start 100)
        workers (map
                  (fn [x]
                    (go (while true (do-work (<! work-chan))
                                    (swap! count inc)
                                    (println count))))
                  (range 2))]
    (while (< (System/currentTimeMillis) end)
      (>!! work-chan 1)
      count)))


(defn foo-par []
  (let [count (atom 0)
        end (+ (System/currentTimeMillis) 1000)
        in (chan)]
    (doseq [x (range 3)]
      (go (while true
            (naive-fib (<! in))
            (swap! count inc))))
    (while (< (System/currentTimeMillis) end)
      (>!! in 29))
    (println "Total executions: " @count)))