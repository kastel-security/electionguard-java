package com.sunya.electionguard.proto;

import com.google.common.collect.ImmutableList;
import com.sunya.electionguard.ElGamal;
import com.sunya.electionguard.Group;
import com.sunya.electionguard.decrypting.DecryptingTrustee;
import com.sunya.electionguard.keyceremony.KeyCeremony2;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sunya.electionguard.proto.CommonConvert.convertElementModP;

import com.sunya.electionguard.protogen.CommonProto;
import com.sunya.electionguard.protogen.TrusteeProto;


public class TrusteeFromProto {

  public static List<DecryptingTrustee> readTrustees(String filename) throws IOException {
    TrusteeProto.DecryptingTrustees proto;
    try (FileInputStream inp = new FileInputStream(filename)) {
      proto = TrusteeProto.DecryptingTrustees.parseDelimitedFrom(inp);
    }
    return convertTrustees(proto);
  }

  public static DecryptingTrustee readTrustee(String filename) throws IOException {
    TrusteeProto.DecryptingTrustee proto;
    try (FileInputStream inp = new FileInputStream(filename)) {
      proto = TrusteeProto.DecryptingTrustee.parseDelimitedFrom(inp);
    }
    return convertTrustee(proto);
  }

  private static ImmutableList<DecryptingTrustee> convertTrustees(TrusteeProto.DecryptingTrustees proto) {
    ImmutableList.Builder<DecryptingTrustee> builder = ImmutableList.builder();
    for (TrusteeProto.DecryptingTrustee guardianProto : proto.getTrusteesList()) {
      builder.add(convertTrustee(guardianProto));
    }
    return builder.build();
  }

  private static DecryptingTrustee convertTrustee(TrusteeProto.DecryptingTrustee proto) {
    String guardian_id = proto.getGuardianId();
    int sequence_order = proto.getGuardianXCoordinate();
    ElGamal.KeyPair election_keypair = convertElgamalKeypair(proto.getElectionKeyPair());

    Map<String, KeyCeremony2.PartialKeyBackup> otherGuardianPartialKeyBackups =
            proto.getOtherGuardianBackupsList().stream()
                    .collect(Collectors.toMap(p -> p.getGeneratingGuardianId(), p -> convertElectionPartialKeyBackup(p)));

    Map<String, ImmutableList<Group.ElementModP>> commitments =
            proto.getGuardianCommitmentsList().stream()
                    .collect(Collectors.toMap(p -> p.getGuardianId(), p -> convertCoefficients(p)));

    return new DecryptingTrustee(guardian_id, sequence_order, election_keypair,
            otherGuardianPartialKeyBackups, commitments);
  }

  private static KeyCeremony2.PartialKeyBackup convertElectionPartialKeyBackup(TrusteeProto.ElectionPartialKeyBackup2 proto) {
    return new KeyCeremony2.PartialKeyBackup(
            proto.getGeneratingGuardianId(),
            proto.getDesignatedGuardianId(),
            proto.getDesignatedGuardianXCoordinate(),
            CommonConvert.convertElementModQ(proto.getCoordinate()),
            proto.getError());
  }

  private static ElGamal.KeyPair convertElgamalKeypair(CommonProto.ElGamalKeyPair keypair) {
    return new ElGamal.KeyPair(
            CommonConvert.convertElementModQ(keypair.getSecretKey()),
            convertElementModP(keypair.getPublicKey()));
  }

  private static ImmutableList<Group.ElementModP> convertCoefficients(TrusteeProto.CommitmentSet proto) {
    ImmutableList.Builder<Group.ElementModP> builder = ImmutableList.builder();
    for (CommonProto.ElementModP commit : proto.getCommitmentsList()) {
      builder.add(convertElementModP(commit));
    }
    return builder.build();
  }

}