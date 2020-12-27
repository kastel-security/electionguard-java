package com.sunya.electionguard;

import com.google.auto.value.AutoValue;
import com.google.common.flogger.FluentLogger;

import javax.annotation.concurrent.Immutable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.sunya.electionguard.Group.*;

class DecryptionShare {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /**
   * A compensated fragment of a Guardian's Partial Decryption of a selection generated by an available guardian.
   */
  @AutoValue
  static abstract class CiphertextCompensatedDecryptionSelection implements ElectionObjectBaseIF {
    abstract String guardian_id(); // The Available Guardian that this share belongs to

    abstract String missing_guardian_id(); // The Missing Guardian for whom this share is calculated on behalf of

    abstract ElementModQ description_hash(); //  The SelectionDescription hash

    abstract ElementModP share(); // The Share of the decryption of a selection. `M_{i,l} in the spec`

    abstract ElementModP recovery_key(); // The Recovery Public Key for the missing_guardian that corresponds to the available guardian's share of the secret

    abstract ChaumPedersen.ChaumPedersenProof proof(); // The Proof that the share was decrypted correctly

    public static CiphertextCompensatedDecryptionSelection create(
            String object_id,
            String guardian_id,
            String missing_guardian_id,
            ElementModQ description_hash,
            ElementModP share,
            ElementModP recovery_key,
            ChaumPedersen.ChaumPedersenProof proof) {
      return new AutoValue_DecryptionShare_CiphertextCompensatedDecryptionSelection(object_id, guardian_id, missing_guardian_id, description_hash, share, recovery_key, proof);
    }

  }

  /**
   * A Guardian's Partial Decryption of a selection.  A CiphertextDecryptionSelection
   * can be generated by a guardian directly, or it can be compensated for by a quoprum of guardians
   * <p>
   * When the guardian generates this share directly, the `proof` field is populated with
   * a `chaumPedersen` proof that the decryption share was generated correctly.
   * <p>
   * When the share is generated on behalf of this guardian by other guardians, the `recovered_parts`
   * collection is populated with the `CiphertextCompensatedDecryptionSelection` objects generated
   * by each available guardian.
   */
  @AutoValue
  static abstract class CiphertextDecryptionSelection implements ElectionObjectBaseIF {
    /**
     * The Available Guardian that this share belongs to
     */
    abstract String guardian_id();

    /**
     * The SelectionDescription hash.
     */
    abstract ElementModQ description_hash();

    /**
     * The Share of the decryption of a selection. `M_i` in the spec.
     */
    abstract ElementModP share();

    /**
     * The Proof that the share was decrypted correctly, if the guardian was available for decryption
     */
    abstract Optional<ChaumPedersen.ChaumPedersenProof> proof();

    /**
     * the recovered parts of the dectyption provided by available guardians, if the guardian was missing from decryption.
     */
    abstract Optional<Map<String, CiphertextCompensatedDecryptionSelection>> recovered_parts();

    public static CiphertextDecryptionSelection create(
            String object_id,
            String guardian_id,
            ElementModQ description_hash,
            ElementModP share,
            Optional<ChaumPedersen.ChaumPedersenProof> proof,
            Optional<Map<String, CiphertextCompensatedDecryptionSelection>> recovered_parts) {
      return new AutoValue_DecryptionShare_CiphertextDecryptionSelection(object_id, guardian_id, description_hash, share, proof, recovered_parts);
    }

    /**
     * Verify that this CiphertextDecryptionSelection is valid for a
     * specific ElGamal key pair, public key, and election context.
     * <p>
     * @param message: the `ElGamalCiphertext` to compare
     * @param election_public_key: the `ElementModP Election Public Key for the Guardian
     * @param extended_base_hash: The `ElementModQ` election extended base hash.
     *
     * @return
     */
    boolean is_valid(ElGamal.Ciphertext message, ElementModP election_public_key, ElementModQ extended_base_hash) {
      // verify we have a proof or recovered parts
      if (!this.proof().isPresent() && !this.recovered_parts().isPresent()) {
        // f"CiphertextDecryptionSelection is_valid failed for guardian: {self.guardian_id} selection: {self.object_id} with missing data"
        return false;
      }

      if (this.proof().isPresent() && this.recovered_parts().isPresent()) {
        // f"CiphertextDecryptionSelection is_valid failed for guardian: {self.guardian_id} selection: {self.object_id} cannot have proof and recovery"
        return false;
      }

      if (this.proof().isPresent()) {
        ChaumPedersen.ChaumPedersenProof proof = this.proof().get();
        if (!proof.is_valid(message, election_public_key, this.share(), extended_base_hash)) {
          // f"CiphertextDecryptionSelection is_valid failed for guardian: {self.guardian_id} selection: {self.object_id} with invalid proof"
          return false;
        }
      }

      if (this.recovered_parts().isPresent()) {
        Map<String, CiphertextCompensatedDecryptionSelection> recovered = this.recovered_parts().get();
        for (CiphertextCompensatedDecryptionSelection part : recovered.values()) {
          if (!part.proof().is_valid(message, part.recovery_key(), part.share(), extended_base_hash)) {
            // f"CiphertextDecryptionSelection is_valid failed for guardian: {self.guardian_id} selection: {self.object_id} with invalid partial proof"
            return false;
          }
        }
      }
      return true;
    }
  }

  /**
   * Create a ciphertext decryption selection
   * <p>
   * @param object_id: Object id
   * @param guardian_id: Guardian id
   * @param description_hash: Description hash
   * @param share: Share
   * @param proof_or_recovery: Proof or recovery
   *
   * @return
   */
  static CiphertextDecryptionSelection create_ciphertext_decryption_selection(
          String object_id,
          String guardian_id,
          ElementModQ description_hash,
          ElementModP share,
          Optional<ChaumPedersen.ChaumPedersenProof> proof,
          Optional<Map<String, CiphertextCompensatedDecryptionSelection>> recovered_parts) {

    if (proof.isEmpty() && recovered_parts.isEmpty()) {
      logger.atInfo().log("decryption share cannot assign {proof_or_recovery}");
    }

    return CiphertextDecryptionSelection.create(
            object_id, guardian_id, description_hash, share,
            proof, recovered_parts);
  }

  /**
   * A Guardian's Partial Decryption of a contest.
   */
  @AutoValue
  static abstract class CiphertextDecryptionContest implements ElectionObjectBaseIF {
    abstract String guardian_id(); // The Available Guardian that this share belongs to

    abstract ElementModQ description_hash(); // The ContestDescription Hash

    abstract Map<String, CiphertextDecryptionSelection> selections(); // the collection of decryption shares for this contest's selections

    public static CiphertextDecryptionContest create(
            String object_id,
            String guardian_id,
            ElementModQ description_hash,
            Map<String, CiphertextDecryptionSelection> selections) {
      return new AutoValue_DecryptionShare_CiphertextDecryptionContest(object_id, guardian_id, description_hash, selections);
    }

  }

  /**
   * A Guardian's Partial Decryption of a contest.
   */
  @AutoValue
  static abstract class CiphertextCompensatedDecryptionContest implements ElectionObjectBaseIF {

    /**
     * The Available Guardian that this share belongs to
     */
    abstract String guardian_id();

    /**
     * The Missing Guardian for whom this share is calculated on behalf of.
     */
    abstract String missing_guardian_id();

    /**
     * The ContestDescription Hash.
     */
    abstract ElementModQ description_hash();

    /**
     * the collection of decryption shares for this contest's selections.
     */
    abstract Map<String, CiphertextCompensatedDecryptionSelection> selections();

    public static CiphertextCompensatedDecryptionContest create(
            String object_id,
            String guardian_id,
            String missing_guardian_id,
            ElementModQ description_hash,
            Map<String, CiphertextCompensatedDecryptionSelection> selections) {
      return new AutoValue_DecryptionShare_CiphertextCompensatedDecryptionContest(object_id, guardian_id, missing_guardian_id, description_hash, selections);
    }

  }

  /**
   * A Guardian's Partial Decryption Share of a specific ballot (e.g. of a spoiled ballot)
   */
  @AutoValue
  static abstract class BallotDecryptionShare {
    abstract String guardian_id(); // The Available Guardian that this share belongs to

    abstract ElementModP public_key(); // The election public key for the guardian

    abstract String ballot_id(); // The Ballot Id that this Decryption Share belongs to

    abstract Map<String, CiphertextDecryptionContest> contests(); //  The collection of all contests in the ballot

    public static BallotDecryptionShare create(
            String guardian_id,
            ElementModP public_key,
            String ballot_id, Map<String, CiphertextDecryptionContest> contests) {
      return new AutoValue_DecryptionShare_BallotDecryptionShare(guardian_id, public_key, ballot_id, contests);
    }
  } // class BallotDecryptionShare

  /**
   *     A Compensated Partial Decryption Share generated by
   *     an available guardian on behalf of a missing guardian
   */
  @AutoValue
  static abstract class CompensatedBallotDecryptionShare {
    abstract String guardian_id(); // The Available Guardian that this share belongs to

    abstract String missing_guardian_id(); //  The Missing Guardian for whom this share is calculated on behalf of

    abstract ElementModP public_key(); // The election public key for the guardian

    abstract String ballot_id(); // The Ballot Id that this Decryption Share belongs to

    abstract Map<String, CiphertextCompensatedDecryptionContest> contests();

    public static CompensatedBallotDecryptionShare create(String guardian_id, String missing_guardian_id, ElementModP public_key, String ballot_id, Map<String, CiphertextCompensatedDecryptionContest> contests) {
      return new AutoValue_DecryptionShare_CompensatedBallotDecryptionShare(guardian_id, missing_guardian_id, public_key, ballot_id, contests);
    }

  }

  /**
   * A Compensated Partial Decryption Share generated by
   * an available guardian on behalf of a missing guardian
   */
  @AutoValue
  static abstract class CompensatedTallyDecryptionShare {

    /**
     * The Available Guardian that this share belongs to
     */
    abstract String guardian_id();

    /**
     * The Missing Guardian for whom this share is calculated on behalf of
     */
    abstract String missing_guardian_id();

    /**
     * The election public key for the guardian.
     */
    abstract ElementModP public_key();

    /**
     * The collection of decryption shares for all contests in the election.
     */
    abstract Map<String, CiphertextCompensatedDecryptionContest> contests();

    /**
     * The collection of decryption shares for all spoiled ballots in the election.
     */
    abstract Map<String, CompensatedBallotDecryptionShare> spoiled_ballots();

    public static CompensatedTallyDecryptionShare create(String guardian_id,
                                                         String missing_guardian_id,
                                                         ElementModP public_key,
                                                         Map<String, CiphertextCompensatedDecryptionContest> contests,
                                                         Map<String, CompensatedBallotDecryptionShare> spoiled_ballots) {
      return new AutoValue_DecryptionShare_CompensatedTallyDecryptionShare(guardian_id, missing_guardian_id, public_key, contests, spoiled_ballots);
    }


  } // class CompensatedTallyDecryptionShare

  @Immutable
  static class KeyAndSelection {
    final ElementModP public_key;
    final CiphertextDecryptionSelection decryption;

    public KeyAndSelection(ElementModP public_key, CiphertextDecryptionSelection decryption) {
      this.public_key = public_key;
      this.decryption = decryption;
    }
  }

  /**
   * A Guardian's Partial Decryption Share of an election tally.
   */
  @AutoValue
  static abstract class TallyDecryptionShare {
    abstract String guardian_id(); // The Available Guardian that this share belongs to

    abstract ElementModP public_key(); // The election public key for the guardian

    abstract Map<String, CiphertextDecryptionContest> contests(); // The collection of decryption shares for all contests in the election

    abstract Map<String, BallotDecryptionShare> spoiled_ballots(); // The collection of decryption shares for all spoiled ballots in the election

    public static TallyDecryptionShare create(
            String guardian_id,
            ElementModP public_key,
            Map<String, CiphertextDecryptionContest> contests,
            Map<String, BallotDecryptionShare> spoiled_ballots) {
      return new AutoValue_DecryptionShare_TallyDecryptionShare(guardian_id, public_key, contests, spoiled_ballots);
    }

  }

  /**
   * Get all of the cast shares for a specific selection
   */
  static Map<String, KeyAndSelection> get_tally_shares_for_selection(String selection_id, Map<String, TallyDecryptionShare> shares) {
    HashMap<String, KeyAndSelection> cast_shares = new HashMap<>();
    for (TallyDecryptionShare share : shares.values()) {
      for (CiphertextDecryptionContest contest : share.contests().values()) {
        for (CiphertextDecryptionSelection selection : contest.selections().values()) {
          if (selection.object_id().equals(selection_id)) {
            cast_shares.put(share.guardian_id(), new KeyAndSelection(share.public_key(), selection));
          }
        }
      }
    }
    return cast_shares;
  }

  /**
   * Get the ballot shares for a given selection, in the context of a specific ballot.
   */
  static Map<String, KeyAndSelection> get_ballot_shares_for_selection(String selection_id, Map<String, BallotDecryptionShare> shares) {
    HashMap<String, KeyAndSelection> ballot_shares = new HashMap<>();
    for (BallotDecryptionShare ballot_share : shares.values()) {
      for (CiphertextDecryptionContest contest_share : ballot_share.contests().values()) {
        for (CiphertextDecryptionSelection selection_share : contest_share.selections().values()) {
          if (selection_share.object_id().equals(selection_id)) {
            ballot_shares.put(ballot_share.guardian_id(), new KeyAndSelection(ballot_share.public_key(), selection_share));
          }
        }
      }
    }
    return ballot_shares;
  }
}
