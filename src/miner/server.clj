(ns miner.server)

;(def socket (Socket. "stratum.slushpool.com", 3333))
;(def outstream (PrintWriter. (.getOutputStream socket) true))
;(def instream (BufferedReader. (InputStreamReader. (.getInputStream socket))))
;
;(.println outstream "{\"id\": 1, \"method\": \"mining.subscribe\", \"params\": []}\n")
;;(pprint (json/read-str (.readLine instream)))
;;(println "----------------")
;;(.println outstream "{\"params\": [\"ckiehl.worker1\", \"password\"], \"id\": 2, \"method\": \"mining.authorize\"}\n")
;;(println (.readLine instream))