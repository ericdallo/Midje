(ns midje.sweet-test
  (:use clojure.test)
  (:use [midje.sweet] :reload-all)
  (:use [midje.test-util]))


(deftest simple-assertion-examples
  (after 
   (fact (+ 1 1) => 3)
   (is (last-type? :mock-expected-result-failure)))

  (after 
   (facts (+ 10 10) => 20
	  (+ 20 20) => 40)
   (is (no-failures?)))
)


(only-mocked g)
(defn f [n] (g n))
(defn call2 [n m]
  (+ (g n) (g m)))
  

(deftest simple-mocking-examples
  (after
   (fact (f 1) => 33
      (provided (g 1) => 33))
   (is (no-failures?)))


  (after 
   (facts 

    (f 1) => 33
      (provided
         (g 1) => 33)
    
    (f 22) => 500
      (provided 
         (g 22) => 500)
    )
   (is (no-failures?)))


  (after 
   (facts 
    (call2 1 2) => 30
      (provided 
         (g 1) => 10
	 (g 2) => 20)
    )
   (is (no-failures?)))

)

