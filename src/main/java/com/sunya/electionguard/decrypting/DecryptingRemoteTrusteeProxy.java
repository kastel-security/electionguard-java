package com.sunya.electionguard.decrypting;

import com.google.common.flogger.FluentLogger;
import com.sunya.electionguard.DecryptionProofTuple;
import com.sunya.electionguard.ElGamal;
import com.sunya.electionguard.Group;
import com.sunya.electionguard.guardian.DecryptingTrusteeIF;
import com.sunya.electionguard.proto.CommonConvert;
import com.sunya.electionguard.proto.DecryptingTrusteeProto;
import com.sunya.electionguard.proto.DecryptingTrusteeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** A Remote Trustee client proxy, communicating over gRpc. */
class DecryptingRemoteTrusteeProxy implements DecryptingTrusteeIF  {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private static final int MAX_MESSAGE = 51 * 1000 * 1000; // 51 Mb

  @Override
  public String id() {
    return trusteeId;
  }

  @Override
  public int xCoordinate() {
    return xCoordinate;
  }

  @Override
  public Group.ElementModP electionPublicKey() {
    return electionPublicKey;
  }

  //   public Optional<DecryptionProofTuple> compensatedDecrypt(
  //          String missing_guardian_id,
  //          ElGamal.Ciphertext elgamal,
  //          Group.ElementModQ extended_base_hash,
  //          @Nullable Group.ElementModQ nonce_seed) {

  @Override
  public Optional<DecryptionProofTuple> compensatedDecrypt(
          String missing_guardian_id,
          ElGamal.Ciphertext text,
          Group.ElementModQ extended_base_hash,
          @Nullable Group.ElementModQ nonce_seed) {
    try {
      DecryptingTrusteeProto.DecryptRequest.Builder request = DecryptingTrusteeProto.DecryptRequest.newBuilder()
              .setMissingGuardianId(missing_guardian_id)
              .setText(CommonConvert.convertCiphertext(text))
              .setExtendedBaseHash(CommonConvert.convertElementModQ(extended_base_hash));

      DecryptingTrusteeProto.DecryptionResponse response = blockingStub.compensatedDecrypt(request.build());
      if (response.hasError()) {
        logger.atSevere().log("compensatedDecrypt failed: %s", response.getError().getMessage());
        return Optional.empty();
      }
      return Optional.of(new DecryptionProofTuple(
              CommonConvert.convertElementModP(response.getDecryption()),
              CommonConvert.convertChaumPedersenProof(response.getProof())));

    } catch (StatusRuntimeException e) {
      logger.atSevere().withCause(e).log("compensatedDecrypt failed: ");
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public Optional<DecryptionProofTuple> partialDecrypt(
          ElGamal.Ciphertext text,
          Group.ElementModQ extended_base_hash,
          @Nullable Group.ElementModQ nonce_seed) {
    try {
      DecryptingTrusteeProto.DecryptRequest.Builder request = DecryptingTrusteeProto.DecryptRequest.newBuilder()
              .setText(CommonConvert.convertCiphertext(text))
              .setExtendedBaseHash(CommonConvert.convertElementModQ(extended_base_hash));

      DecryptingTrusteeProto.DecryptionResponse response = blockingStub.partialDecrypt(request.build());
      if (response.hasError()) {
        logger.atSevere().log("partialDecrypt failed: %s", response.getError().getMessage());
        return Optional.empty();
      }
      return Optional.of(new DecryptionProofTuple(
              CommonConvert.convertElementModP(response.getDecryption()),
              CommonConvert.convertChaumPedersenProof(response.getProof())));

    } catch (StatusRuntimeException e) {
      logger.atSevere().withCause(e).log("partialDecrypt failed: ");
      e.printStackTrace();
      return Optional.empty();
    }
  }

  @Override
  public Optional<Group.ElementModP> recoverPublicKey(String guardianId) {
    try {
      DecryptingTrusteeProto.RecoverPublicKeyRequest.Builder request = DecryptingTrusteeProto.RecoverPublicKeyRequest.newBuilder()
              .setGuardianId(guardianId);

      DecryptingTrusteeProto.RecoverPublicKeyResponse response = blockingStub.recoverPublicKey(request.build());
      if (response.hasError()) {
        logger.atSevere().log("partialDecrypt failed: %s", response.getError().getMessage());
        return Optional.empty();
      }
      return Optional.of(CommonConvert.convertElementModP(response.getRecoveredKey()));

    } catch (StatusRuntimeException e) {
      logger.atSevere().withCause(e).log("partialDecrypt failed: ");
      e.printStackTrace();
      return Optional.empty();
    }
  }

  /*
  boolean finish(boolean allOk) {
    try {
      RemoteTrusteeProto.FinishRequest request = RemoteTrusteeProto.FinishRequest.newBuilder().setAllOk(allOk).build();
      RemoteTrusteeProto.BooleanResponse response = blockingStub.finish(request);
      if (response.hasError()) {
        logger.atSevere().log("commit failed: %s", response.getError().getMessage());
        return false;
      }
      return response.getOk();

    } catch (StatusRuntimeException e) {
      logger.atSevere().withCause(e).log("commit failed: ");
      e.printStackTrace();
      return false;
    }
  } */

  boolean shutdown() {
    try {
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
      return true;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return false;
    }
  }

  ////////////////////////////////////////////
  private final String trusteeId;
  private final int xCoordinate;
  private final Group.ElementModP electionPublicKey;
  private final ManagedChannel channel;
  private final DecryptingTrusteeServiceGrpc.DecryptingTrusteeServiceBlockingStub blockingStub;

  static Builder builder() {
    return new Builder();
  }

  /** Construct client for accessing HelloWorld server using the existing channel. */
  private DecryptingRemoteTrusteeProxy(String trusteeId, int xCoordinate, Group.ElementModP electionPublicKey, ManagedChannel channel) {
    this.trusteeId = trusteeId;
    this.xCoordinate = xCoordinate;
    this.electionPublicKey = electionPublicKey;
    this.channel = channel;
    blockingStub = DecryptingTrusteeServiceGrpc.newBlockingStub(channel);
  }

  static class Builder {
    String trusteeId;
    String target;
    int xCoordinate;
    Group.ElementModP electionPublicKey;

    Builder setTrusteeId(String trusteeId) {
      this.trusteeId = trusteeId;
      return this;
    }

    Builder setUrl(String target) {
      this.target = target;
      return this;
    }

    Builder setXCoordinate(int xCoordinate) {
      this.xCoordinate = xCoordinate;
      return this;
    }

    Builder setElectionPublicKey(Group.ElementModP electionPublicKey) {
      this.electionPublicKey = electionPublicKey;
      return this;
    }

    DecryptingRemoteTrusteeProxy build() {
      ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
              .usePlaintext()
              .enableFullStreamDecompression()
              .maxInboundMessageSize(MAX_MESSAGE).usePlaintext().build();
      return new DecryptingRemoteTrusteeProxy(trusteeId, xCoordinate, electionPublicKey, channel);
    }
  }
}
