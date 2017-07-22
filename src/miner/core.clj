(ns miner.core
  (:require digest)
  (:import [java.net Socket])
  (:import java.io.InputStreamReader)
  (:import java.io.BufferedReader)
  (:import java.io.PrintWriter)
  (:require [clojure.math.numeric-tower :as math])
  (:require [clojure.data.json :as json])
  (:use clojure.string)
  (:use clojure.pprint)
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer
                     close! thread alts!! timeout]])
  (:gen-class))


(declare hexToByte)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


(defn double-hash [content]
  (-> content
      (digest/sha-256)
      (digest/sha-256)))


(defn calc-difficulty [bits]
  (let [base (bit-and bits 0xffffff)
        exponent (bit-shift-right bits 24)
        res (* base (math/expt 2 (* 8 (- exponent 3))))]
    res))


(defn target-string [big-num]
  (format "%064x" (biginteger big-num)))


(defn unhexify [hex]
  (byte-array
         (map
           (fn [[x y]] (hexToByte (str x y )))
           (partition 2 hex))))


(defn hexToByte [hexString]
  ; TODO: pre-condition len(input) == 2
  ; Java only has signed bytes.
  (byte (.byteValue (Integer/valueOf hexString, 16))))


(defn to-hex [num, width]
  (format (format "%%0%sx" width) num))


(defn hexify [s]
  (apply str
         (map #(format "%02x" (int %)) s)))


(def pool-data {:params
                   ["4393db"
                    "6ab6fef8e441084bdce1d6e795faafd41998d46f01509d9f0000000000000000"
                    "01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff4503c44307fabe6d6d88c95140e2f1d8c09f1873aef802b6fa8e1a3c5e0700f7c2711c781bf2fc2b180100000000000000"
                    "db93432f736c7573682f0000000001dc4c8a4f000000001976a9147c154ed1dc59609e3d26abb2df2ea3d587cd8c4188ac00000000"
                    ["467defd387081cbeedf955612a9207e047c4499ee91ebb194fd229eb1dd7db90"
                     "b21f5e98fe3cfb6064b205a09f2397f95c0b79299c599f4459086ecd5fdf9dbe"
                     "bb65c602705191e38626afefacfbd9eae4ee1927ca62df517e709918427aa272"
                     "e97e52a3fd6d356d3adfcc4e263893b73668e1edf7a475bd6c3333392a29a373"
                     "5788a135fc7221c88bd5016e9f3a3807cf3ba81570d104b915699e8b17e0d494"
                     "b7cc8c172e55e3b8c3dff1a4dcb5e141efead6ba539a6802234c7faae66e9d80"
                     "b67626df2692d367bb22e0ab3e404b5950415ad903553daf671ff2bf100c8343"
                     "1f3257dfc01001eea81f055657acdb9d0327191fa9281de7e9f31793f70670c4"
                     "230ed41b8833e02b719637f9487d6fe0c043ba5fc2da27a145717d7787ad1ac6"
                     "fabcf4094628afee2aed733e555b55b5c131d5df9a389ffcb6a5f7fa2a8885c4"
                     "a0a7f7768969de7f772a32c7039a049b37fdf9c681c7205a6ef62fe0812983ef"]
                    "20000002"
                    "18015ddc"
                    "596ba901"
                    true],
          "id" nil,
          "method" "mining.notify"})

(defn build-coinbase [nonce1 nonce2 params]
  (let [[jobid
         prevhash
         coin1
         coin2
         merkle-hashes
         version
         nbits
         ntime
         clean
         ] (:params pool-data)]
    (unhexify (str coin1 nonce1 nonce2 coin2))))


(defn make-nonce [curr]
  (bit-and (+ curr 1) 0xFFFFFF))


(let [[jobid
       prevhash
       coin1
       coin2
       merkle-hashes
       version
       nbits
       ntime
       clean
       ] (:params pool-data)])

(defn calc-hashes [nonce1 nonce2-seed params]
  (let [start (System/currentTimeMillis)
        end (+ start 10000)
        nonce2 (atom nonce2-seed)]
    (while (< (System/currentTimeMillis) end)
      (let [cb (build-coinbase nonce1 (to-hex @nonce2 4) params)
            hash (digest/sha-256 cb)]
        (if (ends-with? hash "00000")
          (println "FOUND ONE!!!" nonce2 hash))
        (swap! nonce2 inc)))
    (println "Hashes calculated: " @nonce2)))


(defn calc-hashes-par [nonce1 nonce2-seed params]
  (let [start (System/currentTimeMillis)
        end (+ start 1000)
        nonce2 (atom nonce2-seed)
        in (chan 4)]
    (doseq [x (range 4)]
      (go
        (while true
          (let [cb (build-coinbase nonce1 (to-hex (<! in) 4) params)
                  hash (digest/sha-256 cb)]
              (if (ends-with? hash "00000")
                (println "FOUND ONE!!!" nonce2 hash))
              ))))
    (while (< (System/currentTimeMillis) end)
      (>!! in @nonce2)
      (swap! nonce2 inc))
    (println "Hashes calculated: " @nonce2)))

