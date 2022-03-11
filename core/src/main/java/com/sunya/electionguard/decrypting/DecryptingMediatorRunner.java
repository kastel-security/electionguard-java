package com.sunya.electionguard.decrypting;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.sunya.electionguard.AvailableGuardian;
import com.sunya.electionguard.CiphertextTally;
import com.sunya.electionguard.CiphertextTallyBuilder;
import com.sunya.electionguard.ElectionConstants;
import com.sunya.electionguard.Group;
import com.sunya.electionguard.InternalManifest;
import com.sunya.electionguard.PlaintextTally;
import com.sunya.electionguard.Scheduler;
import com.sunya.electionguard.input.CiphertextTallyInputValidation;
import com.sunya.electionguard.input.ManifestInputValidation;
import com.sunya.electionguard.proto.CommonConvert;
import electionguard.protogen.DecryptingProto;
import electionguard.protogen.DecryptingServiceGrpc;
import com.sunya.electionguard.publish.Consumer;
import com.sunya.electionguard.publish.Publisher;
import com.sunya.electionguard.verifier.ElectionRecord;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A command line program to decrypt a tally and optionally a collection of ballots with remote Guardians.
 * It opens up a channel to allow guardians to register with it.
 * It waits until navailable guardians register, then starts the decryption.
 * <p>
 * For command line help:
 * <strong>
 * <pre>
 *  java -classpath electionguard-java-all.jar com.sunya.electionguard.decrypting.DecryptingRemote --help
 * </pre>
 * </strong>
 */
public class DecryptingMediatorRunner {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static class CommandLine {
    @Parameter(names = {"-in"}, order = 0,
            description = "Directory containing input election record and encrypted ballots and tally", required = true)
    String encryptDir;

    @Parameter(names = {"-out"}, order = 1,
            description = "Directory where augmented election record is published", required = true)
    String outputDir;

    @Parameter(names = {"-navailable"}, order = 2, description = "Number of available Guardians", required = true)
    int navailable;

    @Parameter(names = {"-port"}, order = 3, description = "The port to run the server on")
    int port = 17711;

    @Parameter(names = {"-h", "--help"}, order = 9, description = "Display this help and exit", help = true)
    boolean help = false;

    private final JCommander jc;

    CommandLine(String progName, String[] args) throws ParameterException {
      this.jc = new JCommander(this);
      this.jc.parse(args);
      jc.setProgramName(String.format("java -classpath electionguard-java-all.jar %s", progName));
    }

    void printUsage() {
      jc.usage();
    }
  }

  public static void main(String[] args) {
    String progName = DecryptingMediatorRunner.class.getName();
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

    boolean allOk = false;
    DecryptingMediatorRunner decryptor = null;
    try {
      Consumer consumer = new Consumer(cmdLine.encryptDir);
      ElectionRecord electionRecord = consumer.readElectionRecord();
      ManifestInputValidation validator = new ManifestInputValidation(electionRecord.manifest);
      Formatter errors = new Formatter();
      if (!validator.validateElection(errors)) {
        System.out.printf("*** ElectionInputValidation FAILED on %s%n%s", cmdLine.encryptDir, errors);
        System.exit(1);
      }

      if (electionRecord.constants != null) {
        Group.setPrimes(electionRecord.constants);
      }

      // check that outputDir exists and can be written to
      Publisher publisher = new Publisher(cmdLine.outputDir, Publisher.Mode.createNew, false);
      if (!publisher.validateOutputDir(errors)) {
        System.out.printf("*** Publisher validateOutputDir FAILED on %s%n%s", cmdLine.outputDir, errors);
        System.exit(1);
      }

      decryptor = new DecryptingMediatorRunner(consumer, electionRecord, cmdLine.encryptDir, cmdLine.outputDir,
              cmdLine.navailable, publisher);
      decryptor.start(cmdLine.port);

      System.out.print("Waiting for guardians to register: elapsed seconds = ");
      Stopwatch stopwatch = Stopwatch.createStarted();
      while (!decryptor.ready()) {
        System.out.printf("%s ", stopwatch.elapsed(TimeUnit.SECONDS));
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      System.out.printf("%n");

      allOk = decryptor.runDecryption();

    } catch (Throwable t) {
      System.out.printf("*** DecryptingMediatorRunner FAILURE%n");
      t.printStackTrace();
      allOk = false;

    } finally {
      if (decryptor != null) {
        decryptor.shutdownRemoteTrustees(allOk);
      }
      Scheduler.shutdown();
    }

    System.exit(allOk ? 0 : 1);
  }

  ///////////////////////////////////////////////////////////////////////////
  private Server server;

  private void start(int port) throws IOException {
    server = ServerBuilder.forPort(port) //
            .addService(new DecryptingRegistrationService()) //
            // .intercept(new MyServerInterceptor())
            .build().start();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // Use stderr here since the logger may have been reset by its JVM shutdown hook.
      System.err.println("*** shutting down gRPC server since JVM is shutting down");
      try {
        stopit();
      } catch (InterruptedException e) {
        e.printStackTrace(System.err);
      }
      System.err.println("*** server shut down");
    }));

    System.out.printf("---- DecryptingRemote started, listening on %d ----%n", port);
  }

  private void stopit() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  /** Await termination on the main thread since the grpc library uses daemon threads. */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  final Stopwatch stopwatch = Stopwatch.createUnstarted();

  final Consumer consumer;
  final ElectionRecord electionRecord;
  final String encryptDir;
  final String outputDir;
  final int navailable;

  final int nguardians;
  final int quorum;
  final List<DecryptingRemoteTrusteeProxy> trusteeProxies = Collections.synchronizedList(new ArrayList<>());
  final boolean startedDecryption = false;

  CiphertextTally encryptedTally;
  PlaintextTally decryptedTally;
  List<PlaintextTally> spoiledDecryptedTallies;
  List<AvailableGuardian> availableGuardians;
  final Publisher publisher;

  DecryptingMediatorRunner(Consumer consumer, ElectionRecord electionRecord, String encryptDir, String outputDir,
                           int navailable, Publisher publisher) {
    this.consumer = consumer;
    this.electionRecord = electionRecord;
    this.encryptDir = encryptDir;
    this.outputDir = outputDir;
    this.navailable = navailable;
    this.publisher = publisher;

    this.nguardians = electionRecord.context.numberOfGuardians;
    this.quorum = electionRecord.context.quorum;
    Preconditions.checkArgument(this.navailable >= this.quorum,
            String.format("Available guardians (%d) must be >= quorum (%d)", this.navailable, this.quorum));
    Preconditions.checkArgument(this.navailable <= this.nguardians,
            String.format("Available guardians (%d) must be <= nguardians (%d)", this.navailable, this.nguardians));

    System.out.printf("DecryptingRemote startup at %s%n", LocalDateTime.now());
    System.out.printf("DecryptingRemote quorum = %d available = %d nguardians = %d%n", this.quorum, this.navailable, this.nguardians);
    stopwatch.start();
  }

  boolean ready() {
    return trusteeProxies.size() == this.navailable;
  }

  private boolean runDecryption() {
    // Do the accumulation if the encryptedTally doesnt exist
    if (this.electionRecord.ciphertextTally == null) {
      System.out.printf("   DecryptingMediatorRunner accumulateTally%n");
      accumulateTally();
    } else {
      this.encryptedTally = this.electionRecord.ciphertextTally;
      CiphertextTallyInputValidation validator = new CiphertextTallyInputValidation(electionRecord.manifest);
      Formatter errors = new Formatter();
      if (!validator.validateTally(this.encryptedTally, errors)) {
        System.out.printf("*** CiphertextTallyInputValidation FAILED on electionRecord%n%s", errors);
        System.exit(1);
      }
    }

    decryptTally();

    boolean ok;
    try {
      publish(encryptDir);
      ok = true;
    } catch (IOException e) {
      e.printStackTrace();
      ok = false;
    }

    System.out.printf("*** DecryptingMediatorRunner %s%n", ok ? "SUCCESS" : "FAILURE");
    return ok;
  }

  void accumulateTally() {
    System.out.printf("%nAccumulate tally%n");
    InternalManifest metadata = new InternalManifest(this.electionRecord.manifest);
    CiphertextTallyBuilder ciphertextTally = new CiphertextTallyBuilder("DecryptingMediatorRunner", metadata, electionRecord.context);
    int nballots = ciphertextTally.batch_append(electionRecord.acceptedBallots);
    this.encryptedTally = ciphertextTally.build();
    System.out.printf(" done accumulating %d ballots in the tally%n", nballots);
  }

  void decryptTally() {
    // LOOK validate tally is well formed
    System.out.printf("%nDecrypt tally%n");

    // The guardians' election public key is in the electionRecord.guardianRecords.
    Map<String, Group.ElementModP> guardianPublicKeys = electionRecord.guardianRecords.stream().collect(
            Collectors.toMap(coeff -> coeff.guardianId(), coeff -> coeff.guardianPublicKey()));

    DecryptingMediator mediator = new DecryptingMediator(electionRecord.context,
            this.encryptedTally,
            consumer.submittedSpoiledBallotsProto(),
            guardianPublicKeys);

    int count = 0;
    for (DecryptingRemoteTrusteeProxy guardian : this.trusteeProxies) {
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

    // Here's where the spoiled ballots are decrypted.
    this.spoiledDecryptedTallies = mediator.decrypt_spoiled_ballots().orElseThrow();
    System.out.printf("SpoiledBallotAndTally = %d%n", spoiledDecryptedTallies.size());

    this.availableGuardians = mediator.getAvailableGuardians();
    System.out.printf("Done decrypting tally%n%n");
  }

  private void shutdownRemoteTrustees(boolean allOk) {
    System.out.printf("Shutdown Remote Trustees%n");
    // tell the remote trustees to finish
    for (DecryptingRemoteTrusteeProxy trustee : trusteeProxies) {
      try {
        boolean ok = trustee.finish(allOk);
        System.out.printf(" DecryptingRemoteTrusteeProxy %s shutdown was success = %s%n", trustee.id(), ok);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }

    // close the proxy channels
    boolean shutdownOk = true;
    for (DecryptingRemoteTrusteeProxy trustee : trusteeProxies) {
      if (!trustee.shutdown()) {
        shutdownOk = false;
      }
    }
    System.out.printf(" Proxy channel shutdown was success = %s%n", shutdownOk);
  }

  void publish(String inputDir) throws IOException {
    publisher.writeDecryptionResultsProto(
            this.electionRecord,
            this.encryptedTally,
            this.decryptedTally,
            this.spoiledDecryptedTallies,
            this.availableGuardians);

    publisher.copyAcceptedBallots(inputDir);
  }

  //////////////////////////////////////////////////////////////////////////////////////////

  private synchronized DecryptingRemoteTrusteeProxy registerTrustee(DecryptingProto.RegisterDecryptingTrusteeRequest request) {
    for (DecryptingRemoteTrusteeProxy proxy : trusteeProxies) {
      if (proxy.id().equalsIgnoreCase(request.getGuardianId())) {
        throw new IllegalArgumentException("Already have a guardian id=" + request.getGuardianId());
      }
    }
    DecryptingRemoteTrusteeProxy.Builder builder = DecryptingRemoteTrusteeProxy.builder();
    builder.setTrusteeId(request.getGuardianId());
    builder.setUrl(request.getRemoteUrl());
    builder.setXCoordinate(request.getGuardianXCoordinate());
    builder.setElectionPublicKey(CommonConvert.convertElementModP(request.getPublicKey()));
    DecryptingRemoteTrusteeProxy trustee = builder.build();
    trusteeProxies.add(trustee);
    return trustee;
  }

  private class DecryptingRegistrationService extends DecryptingServiceGrpc.DecryptingServiceImplBase {

    @Override
    public void registerTrustee(DecryptingProto.RegisterDecryptingTrusteeRequest request,
                                StreamObserver<DecryptingProto.RegisterDecryptingTrusteeResponse> responseObserver) {

      System.out.printf("DecryptingRemote registerTrustee %s url %s %n", request.getGuardianId(), request.getRemoteUrl());

      if (startedDecryption) {
        responseObserver.onNext(DecryptingProto.RegisterDecryptingTrusteeResponse.newBuilder()
                .setError("Already started Decryption").build());
        responseObserver.onCompleted();
        return;
      }

      DecryptingProto.RegisterDecryptingTrusteeResponse.Builder response = DecryptingProto.RegisterDecryptingTrusteeResponse.newBuilder();
      try {
        DecryptingRemoteTrusteeProxy trustee = DecryptingMediatorRunner.this.registerTrustee(request);
        if (Group.getPrimes().getPrimeOptionType() != ElectionConstants.PrimeOption.Standard) {
          response.setConstants(Group.getPrimes().getPrimeOptionType().name());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
        logger.atInfo().log("DecryptingRemote registerTrustee %s", trustee.id());

      } catch (Throwable t) {
        logger.atSevere().withCause(t).log("DecryptingRemote registerTrustee failed");
        t.printStackTrace();
        response.setError(t.getMessage());
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
      }
    }
  }

}
