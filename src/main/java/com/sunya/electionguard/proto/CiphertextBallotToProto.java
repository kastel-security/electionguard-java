package com.sunya.electionguard.proto;

import com.sunya.electionguard.Ballot;
import com.sunya.electionguard.ChaumPedersen;

import static com.sunya.electionguard.proto.CommonConvert.convertCiphertext;
import static com.sunya.electionguard.proto.CommonConvert.convertElementModQ;
import static com.sunya.electionguard.proto.CommonConvert.convertElementModP;

import static com.sunya.electionguard.proto.CiphertextBallotProto.*;

public class CiphertextBallotToProto {

  public static CiphertextAcceptedBallot translateToProto(Ballot.CiphertextAcceptedBallot ballot) {
    CiphertextAcceptedBallot.Builder builder = CiphertextAcceptedBallot.newBuilder();
    builder.setCiphertextBallot(convertCiphertextBallot(ballot));
    builder.setState(convertBallotBoxState(ballot.state));
    return builder.build();
  }

  static CiphertextAcceptedBallot.BallotBoxState convertBallotBoxState(Ballot.BallotBoxState type) {
    return CiphertextAcceptedBallot.BallotBoxState.valueOf(type.name());
  }

  static CiphertextBallot convertCiphertextBallot(Ballot.CiphertextBallot ballot) {
    CiphertextBallot.Builder builder = CiphertextBallot.newBuilder();
    builder.setObjectId(ballot.object_id);
    builder.setBallotStyleId(ballot.ballot_style);
    builder.setDescriptionHash(convertElementModQ(ballot.description_hash));
    builder.setTrackingHash(convertElementModQ(ballot.tracking_hash));
    builder.setPreviousTrackingHash(convertElementModQ(ballot.previous_tracking_hash));
    ballot.contests.forEach(value -> builder.addContests(convertContest(value)));
    builder.setTimestamp(ballot.timestamp);
    builder.setCryptoHash(convertElementModQ(ballot.crypto_hash));
    ballot.nonce.ifPresent(value -> builder.setNonce(convertElementModQ(value)));
    return builder.build();
  }

  static CiphertextBallotContest convertContest(Ballot.CiphertextBallotContest contest) {
    CiphertextBallotContest.Builder builder = CiphertextBallotContest.newBuilder();
    builder.setObjectId(contest.object_id);
    builder.setDescriptionHash(convertElementModQ(contest.description_hash));
    contest.ballot_selections.forEach(value -> builder.addSelections(convertSelection(value)));
    builder.setCryptoHash(convertElementModQ(contest.crypto_hash));
    builder.setEncryptedTotal(convertCiphertext(contest.encrypted_total));
    contest.nonce.ifPresent(value -> builder.setNonce(convertElementModQ(value)));
    contest.proof.ifPresent(value -> builder.setProof(convertConstantProof(value)));
    return builder.build();
  }

  static CiphertextBallotSelection convertSelection(Ballot.CiphertextBallotSelection selection) {
    CiphertextBallotSelection.Builder builder = CiphertextBallotSelection.newBuilder();
    builder.setObjectId(selection.object_id);
    builder.setDescriptionHash(convertElementModQ(selection.description_hash));
    builder.setCiphertext(convertCiphertext(selection.ciphertext()));
    builder.setCryptoHash(convertElementModQ(selection.crypto_hash));
    builder.setIsPlaceholderSelection(selection.is_placeholder_selection);
    selection.nonce.ifPresent(value -> builder.setNonce(convertElementModQ(value)));
    selection.proof.ifPresent(value -> builder.setProof(convertDisjunctiveProof(value)));
    selection.extended_data.ifPresent(value -> builder.setExtendedData(convertCiphertext(value)));
    return builder.build();
  }

  static ConstantChaumPedersenProof convertConstantProof(ChaumPedersen.ConstantChaumPedersenProof proof) {
    ConstantChaumPedersenProof.Builder builder = ConstantChaumPedersenProof.newBuilder();
    builder.setPad(convertElementModP(proof.pad));
    builder.setData(convertElementModP(proof.data));
    builder.setChallenge(convertElementModQ(proof.challenge));
    builder.setResponse(convertElementModQ(proof.response));
    builder.setConstant(proof.constant);
    return builder.build();
  }

  static DisjunctiveChaumPedersenProof convertDisjunctiveProof(ChaumPedersen.DisjunctiveChaumPedersenProof proof) {
    DisjunctiveChaumPedersenProof.Builder builder = DisjunctiveChaumPedersenProof.newBuilder();
    builder.setProofZeroPad(convertElementModP(proof.proof_zero_pad));
    builder.setProofZeroData(convertElementModP(proof.proof_zero_data));
    builder.setProofOnePad(convertElementModP(proof.proof_one_pad));
    builder.setProofOneData(convertElementModP(proof.proof_one_data));
    builder.setProofZeroChallenge(convertElementModQ(proof.proof_zero_challenge));
    builder.setProofOneChallenge(convertElementModQ(proof.proof_one_challenge));
    builder.setProofZeroResponse(convertElementModQ(proof.proof_zero_response));
    builder.setChallenge(convertElementModQ(proof.challenge));
    builder.setProofOneResponse(convertElementModQ(proof.proof_one_response));
    return builder.build();
  }
}