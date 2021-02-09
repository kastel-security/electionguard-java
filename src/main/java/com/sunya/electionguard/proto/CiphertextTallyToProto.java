package com.sunya.electionguard.proto;

import com.sunya.electionguard.PublishedCiphertextTally;

import java.util.Map;

import static com.sunya.electionguard.proto.CommonConvert.convertCiphertext;
import static com.sunya.electionguard.proto.CommonConvert.convertElementModQ;

public class CiphertextTallyToProto {

  public static CiphertextTallyProto.PublishedCiphertextTally translateToProto(PublishedCiphertextTally tally) {
    CiphertextTallyProto.PublishedCiphertextTally.Builder builder = CiphertextTallyProto.PublishedCiphertextTally.newBuilder();
    builder.setObjectId(tally.object_id);
    for (Map.Entry<String, PublishedCiphertextTally.CiphertextTallyContest> contest : tally.contests.entrySet()) {
      builder.putContests(contest.getKey(), convertContest(contest.getValue()));
    }
    return builder.build();
  }

  static CiphertextTallyProto.CiphertextTallyContest convertContest(PublishedCiphertextTally.CiphertextTallyContest contest) {
    CiphertextTallyProto.CiphertextTallyContest.Builder builder = CiphertextTallyProto.CiphertextTallyContest.newBuilder();
    builder.setObjectId(contest.object_id);
    builder.setDescriptionHash(convertElementModQ(contest.description_hash));
    for (Map.Entry<String, PublishedCiphertextTally.CiphertextTallySelection> selection : contest.tally_selections.entrySet()) {
      builder.putTallySelections(selection.getKey(), convertSelection(selection.getValue()));
    }
    return builder.build();
  }

  static CiphertextTallyProto.CiphertextTallySelection convertSelection(PublishedCiphertextTally.CiphertextTallySelection selection) {
    CiphertextTallyProto.CiphertextTallySelection.Builder builder = CiphertextTallyProto.CiphertextTallySelection.newBuilder();
    builder.setObjectId(selection.object_id);
    builder.setDescriptionHash(convertElementModQ(selection.description_hash));
    builder.setCiphertext(convertCiphertext(selection.ciphertext()));
    return builder.build();
  }
}