package com.sunya.electionguard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.sunya.electionguard.Group.*;

/**
 * Guardian of election responsible for safeguarding information and decrypting results.
 */
@Immutable
public class Guardian extends ElectionObjectBase {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /** A unique number in [1, 256) that is the polynomial x value for this guardian. */
  private final int sequence_order;
  private final KeyCeremony.CeremonyDetails ceremony_details;
  /** An auxiliary RSA keypair to do lighter duty encryption/decryption. . */
  private final Auxiliary.KeyPair auxiliary_keys;
  /** The Ki = election keypair for this Guardian. */
  private final KeyCeremony.ElectionKeyPair election_keys;

  //// From Other Guardians
  /** The collection of other guardians' auxiliary public keys that are shared with this guardian. */
  private final ImmutableMap<String, Auxiliary.PublicKey> otherGuardianAuxiliaryKeys; // map(GUARDIAN_ID, Auxiliary.PublicKey)

  /** The collection of other guardians' election public keys that are shared with this guardian. */
  private final ImmutableMap<String, KeyCeremony.ElectionPublicKey> otherGuardianElectionKeys; // map(GUARDIAN_ID, ElectionPublicKey)

  /** The collection of other guardians' partial key backups that are shared with this guardian. */
  private final ImmutableMap<String, KeyCeremony.ElectionPartialKeyBackup> otherGuardianPartialKeyBackups; // Map(GUARDIAN_ID, ElectionPartialKeyBackup)

  public Guardian(String object_id, int sequence_order,
                  KeyCeremony.CeremonyDetails ceremony_details,
                  Auxiliary.KeyPair auxiliary_keys,
                  KeyCeremony.ElectionKeyPair election_keys,
                  Map<String, Auxiliary.PublicKey> otherGuardianAuxiliaryKeys,
                  Map<String, KeyCeremony.ElectionPublicKey> otherGuardianElectionKeys,
                  Map<String, KeyCeremony.ElectionPartialKeyBackup> otherGuardianPartialKeyBackups) {
    super(object_id);
    this.sequence_order = sequence_order;
    this.ceremony_details = Preconditions.checkNotNull(ceremony_details);
    this.auxiliary_keys = Preconditions.checkNotNull(auxiliary_keys);
    this.election_keys = Preconditions.checkNotNull(election_keys);
    this.otherGuardianAuxiliaryKeys = ImmutableMap.copyOf(otherGuardianAuxiliaryKeys);
    this.otherGuardianElectionKeys = ImmutableMap.copyOf(otherGuardianElectionKeys);
    this.otherGuardianPartialKeyBackups = ImmutableMap.copyOf(otherGuardianPartialKeyBackups);
  }

  /** A unique number in [1, 256) that is the polynomial x value for this guardian. */
  public int sequence_order() {
    return sequence_order;
  }
  /** An auxiliary RSA keypair to do lighter duty encryption/decryption. . */
  public Auxiliary.KeyPair auxiliary_keys() {
    return auxiliary_keys;
  }
  /** The Ki = election keypair for this Guardian. */
  public KeyCeremony.ElectionKeyPair election_keys() {
    return election_keys;
  }
  /** The collection of other guardians' auxiliary public keys that are shared with this guardian. */
  public ImmutableCollection<Auxiliary.PublicKey> auxiliary_public_keys() {
    return otherGuardianAuxiliaryKeys.values();
  }
  /** The collection of other guardians' election public keys that are shared with this guardian. */
  public ImmutableCollection<KeyCeremony.ElectionPublicKey> election_public_keys() {
    return otherGuardianElectionKeys.values();
  }
  /** The collection of other guardians' partial key backups that are shared with this guardian. */
  public ImmutableCollection<KeyCeremony.ElectionPartialKeyBackup> election_partial_key_backups() {
    return otherGuardianPartialKeyBackups.values();
  }
  /** The guardian's secret polynomial coefficients. */
  public KeyCeremony.CoefficientSet coefficients() {
    return KeyCeremony.CoefficientSet.create(object_id, sequence_order, this.election_keys.polynomial().coefficients);
  }

  /**
   * Share public election and auxiliary keys for guardian.
   * @return Public set of election and auxiliary keys
   */
  KeyCeremony.PublicKeySet share_public_keys() {
    return KeyCeremony.PublicKeySet.create(
            this.object_id,
            this.sequence_order,
            this.election_keys.key_pair().public_key,
            this.election_keys.proof(),
            this.auxiliary_keys.public_key);
  }

  /** Get a read-only view of the Guardian Manifest Public Keys shared with this Guardian. */
  ImmutableMap<String, KeyCeremony.ElectionPublicKey> otherGuardianElectionKeys() {
    return otherGuardianElectionKeys;
  }

  /** From the Guardian Manifest Public Keys shared with this Guardian, find the ElectionPublicKey by guardian_id. */
  @Nullable KeyCeremony.ElectionPublicKey otherGuardianElectionKey(String guardian_id) {
    return otherGuardianElectionKeys.get(guardian_id);
  }

  /** Share coefficient validation set to be used for validating the coefficients post election. */
  public KeyCeremony.CoefficientValidationSet share_coefficient_validation_set() {
    return KeyCeremony.get_coefficient_validation_set(this.object_id, this.election_keys.polynomial());
  }

  /**
   * Share election public key with another guardian.
   * @return Manifest public key
   */
  KeyCeremony.ElectionPublicKey share_election_public_key() {
    return KeyCeremony.ElectionPublicKey.create(
            this.object_id,
            this.sequence_order,
            this.election_keys.proof(),
            this.election_keys.key_pair().public_key);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // decrypting

  /**
   * Compute a partial decryption of an elgamal encryption.
   *
   * @param elgamal:            the `ElGamalCiphertext` that will be partially decrypted
   * @param extended_base_hash: the extended base hash of the election that
   *                            was used to generate t he ElGamal Ciphertext
   * @param nonce_seed:         an optional value used to generate the `ChaumPedersenProof`
   *                            if no value is provided, a random number will be used.
   * @return a `Tuple[ElementModP, ChaumPedersenProof]` of the decryption and its proof
   */
  DecryptionProofTuple partially_decrypt(
          ElGamal.Ciphertext elgamal,
          ElementModQ extended_base_hash,
          @Nullable ElementModQ nonce_seed) {

    if (nonce_seed == null) {
      nonce_seed = rand_q();
    }

    //TODO: ISSUE #47: Decrypt the election secret key

    // 𝑀_i = 𝐴^𝑠𝑖 mod 𝑝
    ElementModP partial_decryption = elgamal.partial_decrypt(this.election_keys.key_pair().secret_key);

    // 𝑀_i = 𝐴^𝑠𝑖 mod 𝑝 and 𝐾𝑖 = 𝑔^𝑠𝑖 mod 𝑝
    ChaumPedersen.ChaumPedersenProof proof = ChaumPedersen.make_chaum_pedersen(
            elgamal,
            this.election_keys.key_pair().secret_key,
            partial_decryption,
            nonce_seed,
            extended_base_hash);

    return new DecryptionProofTuple(partial_decryption, proof);
  }

  /**
   * Compute a compensated partial decryption of an elgamal encryption on behalf of the missing guardian.
   * LOOK Whats with ISSUE #47?
   * <p>
   * @param missing_guardian_id: the guardian
   * @param elgamal:             the `ElGamalCiphertext` that will be partially decrypted
   * @param extended_base_hash:  the extended base hash of the election used to generate the ElGamal Ciphertext
   * @param nonce_seed:          an optional value used to generate the `ChaumPedersenProof`
   *                             if no value is provided, a random number will be used.
   * @param decryptor            an `AuxiliaryDecrypt` function to decrypt the missing guardian private key backup
   * @return the decryption and its proof
   */
  Optional<DecryptionProofTuple> compensate_decrypt(
          String missing_guardian_id,
          ElGamal.Ciphertext elgamal,
          ElementModQ extended_base_hash,
          @Nullable ElementModQ nonce_seed,
          @Nullable Auxiliary.Decryptor decryptor) {

    if (nonce_seed == null) {
      nonce_seed = rand_q();
    }

    if (decryptor == null) {
      decryptor = Rsa::decrypt;
    }

    KeyCeremony.ElectionPartialKeyBackup backup = this.otherGuardianPartialKeyBackups.get(missing_guardian_id);
    if (backup == null) {
      logger.atInfo().log("compensate decrypt guardian %s missing backup for %s",
              this.object_id, missing_guardian_id);
      return Optional.empty();
    }

    // LOOK why string?
    Optional<String> decrypted_value = decryptor.decrypt(backup.encrypted_value(), this.auxiliary_keys.secret_key);
    if (decrypted_value.isEmpty()) {
      decryptor.decrypt(backup.encrypted_value(), this.auxiliary_keys.secret_key);
      logger.atInfo().log("compensate decrypt guardian %s failed decryption for %s",
              this.object_id, missing_guardian_id);
      return Optional.empty();
    }
    ElementModQ partial_secret_key = hex_to_q(decrypted_value.get()).orElseThrow(IllegalStateException::new);

    // 𝑀_{𝑖,l} = 𝐴^P𝑖_{l}
    ElementModP partial_decryption = elgamal.partial_decrypt(partial_secret_key);

    // 𝑀_{𝑖,l} = 𝐴^𝑠𝑖 mod 𝑝 and 𝐾𝑖 = 𝑔^𝑠𝑖 mod 𝑝
    ChaumPedersen.ChaumPedersenProof proof = ChaumPedersen.make_chaum_pedersen(
            elgamal,
            partial_secret_key,
            partial_decryption,
            nonce_seed,
            extended_base_hash);

    return Optional.of(new DecryptionProofTuple(partial_decryption, proof));
  }

  /** Compute the recovery public key for a given guardian. */
  Optional<ElementModP> recovery_public_key_for(String missing_guardian_id) {
    KeyCeremony.ElectionPartialKeyBackup backup = this.otherGuardianPartialKeyBackups.get(missing_guardian_id);
    if (backup == null) {
      logger.atInfo().log("compensate decrypt guardian %s missing backup for %s",
              this.object_id, missing_guardian_id);
      return Optional.empty();
    }

    // compute the recovery public key, corresponding to the secret share Pi(l)
    // K_ij^(l^j) for j in 0..k-1.  K_ij is coefficients[j].public_key
    ElementModP pub_key = ONE_MOD_P;
    int count = 0;
    for (ElementModP commitment : backup.coefficient_commitments()) {
      ElementModQ exponent = pow_q(BigInteger.valueOf(this.sequence_order), BigInteger.valueOf(count));
      pub_key = mult_p(pub_key, pow_p(commitment, exponent));
      count++;
    }

    return Optional.of(pub_key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Guardian guardian = (Guardian) o;
    boolean ok1 = sequence_order == guardian.sequence_order;
    boolean ok2 = ceremony_details.equals(guardian.ceremony_details);
    boolean ok3 = equalAuxilaryKeys(auxiliary_keys, guardian.auxiliary_keys);
    boolean ok4 = equalElectionKeys(election_keys, guardian.election_keys);
    boolean ok5 = otherGuardianAuxiliaryKeys.equals(guardian.otherGuardianAuxiliaryKeys);
    boolean ok6 = otherGuardianElectionKeys.equals(guardian.otherGuardianElectionKeys);
    boolean ok7 = otherGuardianPartialKeyBackups.equals(guardian.otherGuardianPartialKeyBackups);
    return ok1 && ok2 && ok3 && ok4 && ok5 && ok6 && ok7;
  }

  private boolean equalAuxilaryKeys(Auxiliary.KeyPair o1, Auxiliary.KeyPair o2) {
      boolean ok1 = Rsa.comparePrivateKeys(o1.secret_key, o2.secret_key);
      boolean ok2 = o1.public_key.equals(o2.public_key);
    return ok1 && ok2;
  }

  private boolean equalElectionKeys(KeyCeremony.ElectionKeyPair o1, KeyCeremony.ElectionKeyPair o2) {
    boolean ok1 = o1.key_pair().equals(o2.key_pair());
    boolean ok2 =         o1.proof().equals(o2.proof());
    boolean ok3 =         o1.polynomial().equals(o2.polynomial());
    return ok1 && ok2 && ok3;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), sequence_order, ceremony_details, auxiliary_keys, election_keys, otherGuardianAuxiliaryKeys, otherGuardianElectionKeys, otherGuardianPartialKeyBackups);
  }
}
