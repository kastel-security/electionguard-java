package com.sunya.electionguard.standard;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.sunya.electionguard.ElGamal;
import com.sunya.electionguard.ElectionPolynomial;
import com.sunya.electionguard.Group;
import com.sunya.electionguard.GuardianRecord;
import com.sunya.electionguard.Hash;
import com.sunya.electionguard.SchnorrProof;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.sunya.electionguard.Group.*;

public class KeyCeremony {

  /** A Guardian's public key, commitments and proof. */
  @AutoValue
  public abstract static class ElectionPublicKey {
    /** The guardian object_id. */
    public abstract String owner_id();
    /** The guardian sequence. */
    public abstract int sequence_order();
    /** The guardian's ElGamal.KeyPair public key. */
    public abstract ElementModP key();
    /** The public keys `K_ij` generated from secret coefficients.*/
    public abstract ImmutableList<ElementModP> coefficient_commitments();
    /** A proof of possession of the private key for each secret coefficient. */
    public abstract ImmutableList<SchnorrProof> coefficient_proofs();

    public static ElectionPublicKey create(String owner_id, int sequence_order, ElementModP key,
                                           List<ElementModP> coefficient_commitments, List<SchnorrProof> coefficient_proofs) {
      return new AutoValue_KeyCeremony_ElectionPublicKey(
              owner_id, sequence_order, key,
              ImmutableList.copyOf(coefficient_commitments),
              ImmutableList.copyOf(coefficient_proofs));
    }

    public GuardianRecord publish_guardian_record() {
      return new GuardianRecord(
              this.owner_id(),
              this.sequence_order(),
              this.key(),
              this.coefficient_commitments(),
              this.coefficient_proofs()
      );
    }
  }

  /** Pair of keys (public and secret) used to encrypt/decrypt election. One for each Guardian. */
  @AutoValue
  public abstract static class ElectionKeyPair {
    /** The guardian object_id. */
    public abstract String owner_id();
    /** The guardian sequence. */
    public abstract int sequence_order();
    /** Ki = (si, g^si), for the ith Guardian */
    public abstract ElGamal.KeyPair key_pair();
    /** The Guardian's polynomial. */
    public abstract ElectionPolynomial polynomial();

    public static ElectionKeyPair create(String owner_id, int sequence_order, ElGamal.KeyPair key_pair, ElectionPolynomial polynomial) {
      return new AutoValue_KeyCeremony_ElectionKeyPair(owner_id, sequence_order, key_pair, polynomial);
    }

   /** Share the election public key and associated data. */
    public ElectionPublicKey share() {
      return ElectionPublicKey.create(
              this.owner_id(),
              this.sequence_order(),
              this.key_pair().public_key,
              this.polynomial().coefficient_commitments,
              this.polynomial().coefficient_proofs
              );
    }
  }

  @AutoValue
  public abstract static class ElectionJointKey {
    /** The product of the guardian public keys K = ∏ ni=1 Ki mod p. */
    public abstract ElementModP joint_public_key();

    /** The hash of the commitments that the guardians make to each other: H = H(K 1,0 , K 2,0 ... , K n,0 ). */
    public abstract ElementModQ commitment_hash();

    public static ElectionJointKey create(ElementModP joint_public_key, ElementModQ commitment_hash) {
      return new AutoValue_KeyCeremony_ElectionJointKey(joint_public_key, commitment_hash);
    }
  }

  /** A point on a secret polynomial, and commitments to verify this point for a designated guardian. */
  @AutoValue
  public abstract static class ElectionPartialKeyBackup {
    /** The Id of the guardian that generated this backup. */
    public abstract String owner_id();
    /** The Id of the guardian to receive this backup. */
    public abstract String designated_id();
    /** The sequence order of the designated guardian. */
    public abstract int designated_sequence_order();
    /** The coordinate corresponding to a secret election polynomial. */
    public abstract ElementModQ value();

    public static ElectionPartialKeyBackup create(String owner_id,
                                                  String designated_id,
                                                  int designated_sequence_order,
                                                  ElementModQ value) {
      return new AutoValue_KeyCeremony_ElectionPartialKeyBackup(
              owner_id,
              designated_id,
              designated_sequence_order,
              value);
    }
  }

  /** Details of key ceremony: number of guardians and quorum size. */
  @AutoValue
  public abstract static class CeremonyDetails {
    public abstract int number_of_guardians();
    public abstract int quorum();

    public static CeremonyDetails create(int number_of_guardians, int quorum) {
      return new AutoValue_KeyCeremony_CeremonyDetails(number_of_guardians, quorum);
    }
  }

  /** Verification of election partial key used in key sharing. */
  @AutoValue
  public abstract static class ElectionPartialKeyVerification {
    public abstract String owner_id();
    public abstract String designated_id();
    public abstract String verifier_id();
    public abstract boolean verified();

    public static ElectionPartialKeyVerification create(String owner_id, String designated_id, String verifier_id, boolean verified) {
      return new AutoValue_KeyCeremony_ElectionPartialKeyVerification(owner_id, designated_id, verifier_id, verified);
    }
  }

  /** Challenge of election partial key used in key sharing. */
  @AutoValue
  public abstract static class ElectionPartialKeyChallenge {
    public abstract String owner_id();
    public abstract String designated_id();
    public abstract int designated_sequence_order(); // The sequence order of the designated guardian
    public abstract ElementModQ value();
    public abstract ImmutableList<ElementModP> coefficient_commitments();
    public abstract ImmutableList<SchnorrProof> coefficient_proofs();

    public static ElectionPartialKeyChallenge create(String owner_id, String designated_id, int designated_sequence_order,
           ElementModQ value, List<ElementModP> coefficient_commitments, List<SchnorrProof> coefficient_proofs) {
      return new AutoValue_KeyCeremony_ElectionPartialKeyChallenge(owner_id, designated_id, designated_sequence_order, value,
              ImmutableList.copyOf(coefficient_commitments), ImmutableList.copyOf(coefficient_proofs));
    }
  }

  //////////////////
  // extraneous

  // TODO delete
  /** The secret polynomial coefficients for one Guardian. */
  @AutoValue
  public abstract static class CoefficientSet {
    /** Guardian.object_id. */
    public abstract String guardianId();
    /** i: a unique number in [1, 256) that is the polynomial x value for this guardian. */
    public abstract int guardianSequence();
    /** The secret polynomial coefficients. */
    public abstract ImmutableList<ElementModQ> coefficients();

    /**
     * Create a CoefficientSet for the ith guardian, i &gt; 0
     * @param guardian_id the Guardian.object_id
     * @param guardian the guardian value i, must be &gt; 0
     * @param coefficients the secret polynomial coefficients
     */
    public static CoefficientSet create(String guardian_id, int guardian, List<ElementModQ> coefficients) {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(guardian_id));
      Preconditions.checkArgument(guardian > 0);
      Preconditions.checkNotNull(coefficients);
      return new AutoValue_KeyCeremony_CoefficientSet(guardian_id, guardian, ImmutableList.copyOf(coefficients));
    }

    // This is what the Guardian needs.
    public ElectionKeyPair generate_election_key_pair() {
      ElectionPolynomial polynomial = generate_polynomial(coefficients());
      ElGamal.KeyPair key_pair = new ElGamal.KeyPair(
              polynomial.coefficients.get(0), polynomial.coefficient_commitments.get(0));
      return ElectionKeyPair.create(guardianId(), guardianSequence(), key_pair, polynomial);
    }

    /**
     * Generates a polynomial when the coefficients are already chosen
     *
     * @param coefficients:           the k coefficients of the polynomial
     * @return Polynomial used to share election keys
     */
    private ElectionPolynomial generate_polynomial(List<Group.ElementModQ> coefficients) {
      ArrayList<Group.ElementModP> commitments = new ArrayList<>();
      ArrayList<SchnorrProof> proofs = new ArrayList<>();

      for (Group.ElementModQ coefficient : coefficients) {
        Group.ElementModP commitment = g_pow_p(coefficient);
        // TODO Alternate schnorr proof method that doesn't need KeyPair
        SchnorrProof proof = SchnorrProof.make_schnorr_proof(new ElGamal.KeyPair(coefficient, commitment), rand_q());
        commitments.add(commitment);
        proofs.add(proof);
      }

      return new ElectionPolynomial(coefficients, commitments, proofs);
    }
  }

  /////////////////////////////

  /**
   * Generate election key pair, proof, and polynomial.
   * @param quorum: Quorum of guardians needed to decrypt
   * @param nonce: Optional nonce for determinism, use null when generating in production.
   */
  public static ElectionKeyPair generate_election_key_pair(String owner_id, int sequence_order, int quorum, @Nullable ElementModQ nonce) {
    ElectionPolynomial polynomial = ElectionPolynomial.generate_polynomial(quorum, nonce);
    // the 0th coefficient is the secret s for the ith Guardian
    // the 0th commitment is the public key = g^s mod p
    // The key_pair is Ki = election keypair for ith Guardian
    ElGamal.KeyPair key_pair = new ElGamal.KeyPair(
            polynomial.coefficients.get(0), polynomial.coefficient_commitments.get(0));
    return ElectionKeyPair.create(owner_id, sequence_order, key_pair, polynomial);
  }

  /**
   * Generate election partial key backup for sharing.
   * @param owner_id: Owner of election key
   * @param polynomial: The owner's Manifest polynomial
   * @param designated_guardian_key: The Guardian's public key
   * @return Election partial key backup
   */
  public static ElectionPartialKeyBackup generate_election_partial_key_backup(
          String owner_id,
          ElectionPolynomial polynomial,
          ElectionPublicKey designated_guardian_key) {

    ElementModQ value = ElectionPolynomial.compute_polynomial_coordinate(
            BigInteger.valueOf(designated_guardian_key.sequence_order()), polynomial);
    return ElectionPartialKeyBackup.create(
            owner_id,
            designated_guardian_key.owner_id(),
            designated_guardian_key.sequence_order(),
            value);
  }

  /**
   * Verify election partial key backup contains point on owners polynomial.
   * @param verifier_id: Verifier of the partial key backup
   * @param backup: Manifest partial key backup
   * @param election_public_key: Other guardian's election public key
   */
  public static ElectionPartialKeyVerification verify_election_partial_key_backup(
          String verifier_id,
          ElectionPartialKeyBackup backup,
          ElectionPublicKey election_public_key) {

    return ElectionPartialKeyVerification.create(
            backup.owner_id(),
            backup.designated_id(),
            verifier_id,
            ElectionPolynomial.verify_polynomial_coordinate(backup.value(), BigInteger.valueOf(backup.designated_sequence_order()),
                    election_public_key.coefficient_commitments()));
  }

  /**
   * Generate challenge to a previous verification of a partial key backup.
   * @param backup: Manifest partial key backup in question
   * @param polynomial: Polynomial to regenerate point
   * @return Manifest partial key verification
   */
  public static ElectionPartialKeyChallenge generate_election_partial_key_challenge(
          ElectionPartialKeyBackup backup,
          ElectionPolynomial polynomial) {

    return ElectionPartialKeyChallenge.create(
            backup.owner_id(),
            backup.designated_id(),
            backup.designated_sequence_order(),
            ElectionPolynomial.compute_polynomial_coordinate(BigInteger.valueOf(backup.designated_sequence_order()), polynomial),
            polynomial.coefficient_commitments,
            polynomial.coefficient_proofs);
  }

  /**
   * Verify a challenge to a previous verification of a partial key backup.
   * @param verifier_id: Verifier of the challenge
   * @param challenge: Manifest partial key challenge
   * @return Manifest partial key verification
   */
  public static ElectionPartialKeyVerification verify_election_partial_key_challenge(
          String verifier_id, ElectionPartialKeyChallenge challenge) {

    return ElectionPartialKeyVerification.create(
            challenge.owner_id(),
            challenge.designated_id(),
            verifier_id,
            ElectionPolynomial.verify_polynomial_coordinate(challenge.value(),
                    BigInteger.valueOf(challenge.designated_sequence_order()),
                    challenge.coefficient_commitments()));
  }

  /**
   * Creates a joint election key from the public keys of all guardians.
   * @return Joint key and commitment hash for election
   */
  public static ElectionJointKey combine_election_public_keys(Collection<ElectionPublicKey> election_public_keys) {
    List<ElectionPublicKey> sorted = election_public_keys.stream()
            .sorted(Comparator.comparing(ElectionPublicKey::sequence_order))
            .collect(Collectors.toList());

    List<ElementModP> public_keys = sorted.stream()
            .map(ElectionPublicKey::key)
            .collect(Collectors.toList());
    ElementModP joint_public_keys = ElGamal.elgamal_combine_public_keys(public_keys);

    List<Group.ElementModP> commitments = new ArrayList<>();
    for (ElectionPublicKey pk : sorted) {
      commitments.addAll(pk.coefficient_commitments());
    }
    ElementModQ commitment_hash = Hash.hash_elems(commitments);

    return ElectionJointKey.create(joint_public_keys, commitment_hash);
  }
}
