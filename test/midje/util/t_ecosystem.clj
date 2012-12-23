(ns midje.util.t-ecosystem
  (:use [midje.sweet]
        [clojure.pprint]
        [midje.test-util]
        [midje.util.ecosystem]))

                                        ;;; Facts on disk

(facts "locating fact namespaces"
  (fact "defaults to test directory"
    (let [default-namespaces (fact-namespaces)]
      default-namespaces => (contains 'midje.t-repl)
      default-namespaces =not=> (contains 'midje.ideas.facts)))

  (fact "can be given explicit directories" 
    (let [chosen-namespaces (fact-namespaces "src")]
      chosen-namespaces =not=> (contains 'midje.ideas.t-facts)
      chosen-namespaces => (contains 'midje.ideas.facts))
    (let [chosen-namespaces (fact-namespaces "src" "test")]
      chosen-namespaces => (contains #{'midje.ideas.facts
                                       'midje.sweet}
                                     :gaps-ok)
      chosen-namespaces => (contains 'behaviors.t-isolated-metaconstants)))

  (fact "can filter by prefix"
    (let [default-prefix (fact-namespaces :prefix "midje.checkers")]
      default-prefix => (contains 'midje.checkers.t-chatty)
      default-prefix =not=> (contains 'midje.ideas.t-facts)
      default-prefix =not=> (contains 'midje.ideas.facts))

    (let [chosen-prefix (fact-namespaces "src" :prefix "midje.ideas")]
      chosen-prefix => (contains 'midje.ideas.facts)
      chosen-prefix =not=> (contains 'midje.checkers.chatty)
      chosen-prefix =not=> (contains 'midje.ideas.t-facts))

    ;; truly a prefix
    (fact-namespaces "src" :prefix "ideas") => empty?))


;;; Directory structure

(when-1-3+

  (fact "can find paths to load from project.clj"
    (fact "if it exists"
      (project-directories) => ["/test1" "/src1"]
      (provided (leiningen.core.project/read) => {:test-paths ["/test1"]
                                                  :source-paths ["/src1"]}))
    
    (fact "and provides a default if it does not"
      (project-directories) => ["test"]
      (provided (leiningen.core.project/read)
                =throws=> (new java.io.FileNotFoundException))))
  
  
  (fact "unglob-partial-namespaces returns namespace symbols"
    (fact "from symbols or strings"
      (unglob-partial-namespaces ["explicit-namespace1"]) => ['explicit-namespace1]
      (unglob-partial-namespaces ['explicit-namespace2]) => ['explicit-namespace2])
    
    (fact "can 'unglob' wildcards"
      (unglob-partial-namespaces ["ns.foo.*"]) => '[ns.foo.bar ns.foo.baz]
      (provided (bultitude.core/namespaces-on-classpath :prefix "ns.foo.")
                => '[ns.foo.bar ns.foo.baz])
      
      (unglob-partial-namespaces ['ns.foo.*]) => '[ns.foo.bar ns.foo.baz]
      (provided (bultitude.core/namespaces-on-classpath :prefix "ns.foo.")
                => '[ns.foo.bar ns.foo.baz])))
  
  )


;;; Working with modification times and dependencies

(when-1-3+

 (fact "working with the tools.namespace tracker structure"
   (against-background (file-modification-time ..file1..) => 222
                       (file-modification-time ..file2..) => 3333)
   (let [core-tracker
         {time-key 11
          unload-key [..ns1.. ..ns2..]
          load-key [..ns1.. ..ns2..]
          filemap-key {..file1.. ..ns1..
                       ..file2.. ..ns2..}}]
     (facts "modification times"
       (latest-modification-time core-tracker) => 3333
       (latest-modification-time (assoc core-tracker load-key [])) => 11
       (autotest-augment-tracker core-tracker) => (contains {next-time-key 3333}))

     (facts "creating the tracker appropriate for the next check"
       (autotest-next-tracker (autotest-augment-tracker core-tracker))
       => (contains {time-key 3333, unload-key [], load-key []})))


   (fact "a namespace and all that depend on it can be removed"
     (let [dependency-tracker {load-key [..core-ns.. ..depends-on-core.. ..does-not-depend..]
                               deps-key {:dependents {..core-ns.. #{..depends-on-core..}}}}]
       (without-first-required-and-dependents dependency-tracker) => (contains {load-key [..does-not-depend..]}))))
 )
                              
