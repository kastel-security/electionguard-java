# 🗳 ElectionGuard Java

This is a port of https://github.com/microsoft/electionguard-python/ to Java.
It  closely follows the classes, naming and file structure of the original, to facilitate 
line-by-line comparison.

### Workflow

The following Command Line Programs simulate the pieces of the workflow needed to run an election:

1.   [PerformKeyCeremony](https://github.com/JohnLCaron/electionguard-java/blob/testdocs/docs/PerformKeyCeremony.md)

2.   [EncryptBallots](https://github.com/JohnLCaron/electionguard-java/blob/testdocs/docs/EncryptBallots.md)

3.   [DecryptBallots](https://github.com/JohnLCaron/electionguard-java/blob/testdocs/docs/DecryptBallots.md)

The entire workflow, consisting of the above three steps plus validation can be run from a single command:

4.   [RunElectionWorkflow](https://github.com/JohnLCaron/electionguard-java/blob/testdocs/docs/RunElectionWorkflow.md)

### Public API Javadoc 

### Library repositories

### Building from source

````
git clone https://github.com/JohnLCaron/electionguard-java.git
cd electionguard-java
./gradlew clean assemble
````

## Notes

 * The full [validation spec](https://www.electionguard.vote/spec/0.95.0/1_Overview/) is implemented.
 * Assumes Java 11.
 * Most classes are immutable, anticipating the need for multithreading. 
 * Uses Java's BigInteger for cryptographic computations.
 * Uses Gson for JSON serialization. 
 * Can also serialize using protobuf.
 * Using AutoValue for (some) immutable value classes.
 * Uses Flogger for logging.
 * Uses JUnit5 / jqwik library for property based testing.
 * Uses gradle for building.
 
## TODO

  * Not yet compatible with Python library JSON serialization, due to different ways to serialize Optional.
    It incorporates a workaround which should work for now, however.
  * Measure computational performance.
  * Investigate parallelization strategies.


