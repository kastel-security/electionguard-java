package com.sunya.electionguard.simulate;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Preconditions;
import com.sunya.electionguard.AvailableGuardian;
import com.sunya.electionguard.CiphertextTally;
import com.sunya.electionguard.CiphertextTallyBuilder;
import com.sunya.electionguard.Group;
import com.sunya.electionguard.InternalManifest;
import com.sunya.electionguard.Manifest;
import com.sunya.electionguard.PlaintextTally;
import com.sunya.electionguard.Scheduler;
import com.sunya.electionguard.decrypting.DecryptingTrustee;
import com.sunya.electionguard.input.ManifestInputValidation;
import com.sunya.electionguard.publish.Consumer;
import com.sunya.electionguard.publish.PrivateData;
import com.sunya.electionguard.publish.Publisher;
import com.sunya.electionguard.json.PublisherOld;
import com.sunya.electionguard.publish.ElectionRecord;
import electionguard.ballot.DecryptionResult;
import electionguard.ballot.TallyResult;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

/**
 * A command line program to decrypt a collection of ballots.
 * <p>
 * For command line help:
 * <strong>
 * <pre>
 *  java -classpath electionguard-java-all.jar com.sunya.electionguard.workflow.RunDecryptingSimulator --help
 * </pre>
 * </strong>
 *
 * @see <a href="https://www.electionguard.vote/spec/0.95.0/7_Verifiable_decryption/">Ballot Decryption</a>
 */
public class RunDecryptionSimulator {

  private static class CommandLine {
    @Parameter(names = {"-in"}, order = 0,
            description = "Directory containing input election record and encrypted ballots and tally", required = true)
    String encryptDir;

    @Parameter(names = {"-trusteeDir"}, order = 1,
            description = "location of serialized guardian files")
    String trusteeDir;

    @Parameter(names = {"-out"}, order = 3,
            description = "Directory where augmented election record is published", required = true)
    String outputDir;

    @Parameter(names = {"-h", "--help"},  order = 4, description = "Display this help and exit", help = true)
    boolean help = false;

    private final JCommander jc;

    public CommandLine(String progName, String[] args) throws ParameterException {
      this.jc = new JCommander(this);
      this.jc.parse(args);
      jc.setProgramName(String.format("java -classpath electionguard-java-all.jar %s", progName));
    }

    public void printUsage() {
      jc.usage();
    }
  }

  public static void main(String[] args) {
    String progName = RunDecryptionSimulator.class.getName();
    RunDecryptionSimulator decryptor;
    CommandLine cmdLine = null;

    try {
      cmdLine = new CommandLine(progName, args);
      if (cmdLine.help) {
        cmdLine.printUsage();
        return;
      }
    } catch (ParameterException e) {
      System.err.println(e.getMessage());
      System.err.printf("Try '%s --help' for more information.%n", progName);
      System.exit(1);
    }

    RemoteGuardiansProvider guardiansProvider = new RemoteGuardiansProvider(cmdLine.trusteeDir);

    try {
      Consumer consumer = new Consumer(cmdLine.encryptDir);
      ElectionRecord electionRecord = consumer.readElectionRecord();
      TallyResult tallyResult = consumer.readTallyResult();
      // LOOK how to validate guardians??
      ManifestInputValidation validator = new ManifestInputValidation(electionRecord.manifest());
      Formatter errors = new Formatter();
      if (!validator.validateElection(errors)) {
        System.out.printf("*** ElectionInputValidation FAILED on %s%n%s", cmdLine.encryptDir, errors);
        System.exit(1);
      }

      System.out.printf(" BallotDecryptor read from %s%n Write to %s%n", cmdLine.encryptDir, cmdLine.outputDir);
      decryptor = new RunDecryptionSimulator(consumer, electionRecord, guardiansProvider);

      // Do the accumulation if the encryptedTally doesnt exist
      if (electionRecord.ciphertextTally() == null) {
        decryptor.accumulateTally();
      }

      decryptor.decryptTally();
      boolean ok = decryptor.publish(cmdLine.encryptDir, cmdLine.outputDir, tallyResult);
      System.out.printf("*** RunDecryptingSimulator %s%n", ok ? "SUCCESS" : "FAILURE");

    } catch (Throwable t) {
      System.out.printf("*** RunDecryptingSimulator FAILURE%n");
      t.printStackTrace();
      System.exit(4);

    } finally {
      Scheduler.shutdown();
    }
  }

  private static class RemoteGuardiansProvider {
    private final String location;
    private Iterable<DecryptingTrustee> guardians;

    public RemoteGuardiansProvider(String location) {
      this.location = location;
    }

    public Iterable<DecryptingTrustee> guardians() {
      if (guardians == null) {
        guardians = read(location);
      }
      return guardians;
    }

    List<DecryptingTrustee> read(String location) {
      try {
        PrivateData pdata = new PrivateData(location, false, false);
        List<DecryptingTrustee> result = pdata.readDecryptingTrustees(location);
        if (result.isEmpty()) {
          System.out.printf("RemoteGuardiansProvider: no guardian files in %s%n", location);
        }
        return result;
      } catch (IOException e) {
        System.out.printf("RemoteGuardiansProvider failed to read %s%n", location);
        throw new RuntimeException(e);
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  final Consumer consumer;
  final ElectionRecord electionRecord;
  final Manifest election;

  Iterable<DecryptingTrustee> guardians;
  CiphertextTally encryptedTally;
  PlaintextTally decryptedTally;
  List<PlaintextTally> spoiledDecryptedTallies;
  List<AvailableGuardian> availableGuardians;
  int quorum;
  int numberOfGuardians;

  public RunDecryptionSimulator(Consumer consumer, ElectionRecord electionRecord, RemoteGuardiansProvider provider) {
    this.consumer = consumer;
    this.electionRecord = electionRecord;
    this.election = electionRecord.manifest();
    this.quorum = electionRecord.quorum();
    this.numberOfGuardians = electionRecord.numberOfGuardians();
    this.encryptedTally = electionRecord.ciphertextTally();

    this.guardians = provider.guardians();
    for (DecryptingTrustee guardian : provider.guardians()) {
      // LOOK test Guardians against whats in the electionRecord.
    }
    System.out.printf("%nReady to decrypt%n");
  }

  void accumulateTally() {
    System.out.printf("%nAccumulate tally%n");
    InternalManifest metadata = new InternalManifest(this.election);
    CiphertextTallyBuilder ciphertextTally = new CiphertextTallyBuilder("RunDecryptingSimulator", metadata, electionRecord);
    int nballots = ciphertextTally.batch_append(electionRecord.submittedBallots());
    this.encryptedTally = ciphertextTally.build();
    System.out.printf(" done accumulating %d ballots in the tally%n", nballots);
  }

  void decryptTally() {
    System.out.printf("%nDecrypt tally%n");

    // The guardians' election public key is in the electionRecord.guardianRecords.
    Map<String, Group.ElementModP> guardianPublicKeys = electionRecord.guardians().stream().collect(
            Collectors.toMap(guardian -> guardian.getGuardianId(), guardian -> guardian.publicKey()));

    DecryptingTrusteeMediator mediator = new DecryptingTrusteeMediator(electionRecord,
            this.encryptedTally,
            consumer.iterateSubmittedBallots(),
            guardianPublicKeys);

    int count = 0;
    for (DecryptingTrustee guardian : this.guardians) {
      boolean ok = mediator.announce(guardian);
      Preconditions.checkArgument(ok);
      System.out.printf(" Guardian Present: %s%n", guardian.id());
      count++;
      if (count == this.quorum) {
        System.out.printf("Quorum of %d reached%n", this.quorum);
        break;
      }
    }

    // Here's where the ciphertext Tally is decrypted.
    this.decryptedTally = mediator.get_plaintext_tally().orElseThrow();
    this.spoiledDecryptedTallies = mediator.decrypt_spoiled_ballots().orElseThrow();
    this.availableGuardians = mediator.getAvailableGuardians();
    System.out.printf("Done decrypting tally%n%n%s%n", this.decryptedTally);
  }

  boolean publish(String inputDir, String publishDir, TallyResult tallyResult) throws IOException {
    DecryptionResult results = new DecryptionResult(
            tallyResult,
            this.decryptedTally,
            this.availableGuardians,
            emptyMap()
    );

    Publisher publisher = new Publisher(publishDir, Publisher.Mode.createIfMissing);
    publisher.writeDecryptionResults(results);
    // LOOK publisher.copyAcceptedBallots(inputDir);
    return true;
  }

  boolean publishOld(String inputDir, String publishDir) throws IOException {
    PublisherOld publisher = new PublisherOld(publishDir, PublisherOld.Mode.createIfMissing);
    publisher.writeDecryptionResultsProto(
            this.electionRecord,
            this.encryptedTally,
            this.decryptedTally,
            this.spoiledDecryptedTallies,
            availableGuardians);

    publisher.copyAcceptedBallots(inputDir);
    return true;
  }
}
