package com.sunya.electionguard.proto;

import com.google.protobuf.ByteString;
import com.sunya.electionguard.ElGamal;
import com.sunya.electionguard.Group;
import com.sunya.electionguard.SchnorrProof;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommonConvert {

  @Nullable
  static <T, U> List<U> convertList(@Nullable List<T> from, Function<T, U> converter) {
    return from == null ? null : from.stream().map(converter).collect(Collectors.toList());
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  // from proto
  
  @Nullable
  static Group.ElementModQ convertElementModQ(@Nullable CommonProto.ElementModQ modQ) {
    if (modQ == null || modQ.getValue().isEmpty()) {
      return null;
    }
    BigInteger elem = new BigInteger(modQ.getValue().toByteArray());
    return Group.int_to_q_unchecked(elem);
  }

  @Nullable
  static Group.ElementModP convertElementModP(@Nullable CommonProto.ElementModP modP) {
    if (modP == null || modP.getValue().isEmpty()) {
      return null;
    }
    BigInteger elem = new BigInteger(modP.getValue().toByteArray());
    return Group.int_to_p_unchecked(elem);
  }

  @Nullable
  static ElGamal.Ciphertext convertCiphertext(@Nullable CommonProto.ElGamalCiphertext ciphertext) {
    if (ciphertext == null || !ciphertext.hasPad()) {
      return null;
    }
    return new ElGamal.Ciphertext(
            convertElementModP(ciphertext.getPad()),
            convertElementModP(ciphertext.getData())
    );
  }

  static SchnorrProof convertSchnorrProof(CommonProto.SchnorrProof proof) {
    return new SchnorrProof(
            convertElementModP(proof.getPublicKey()),
            convertElementModP(proof.getCommitment()),
            convertElementModQ(proof.getChallenge()),
            convertElementModQ(proof.getResponse()));
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  // to proto

  static CommonProto.ElementModQ convertElementModQ(Group.ElementModQ modQ) {
    CommonProto.ElementModQ.Builder builder = CommonProto.ElementModQ.newBuilder();
    builder.setValue(ByteString.copyFrom(modQ.getBigInt().toByteArray()));
    return builder.build();
  }

  static CommonProto.ElementModP convertElementModP(Group.ElementModP modP) {
    CommonProto.ElementModP.Builder builder = CommonProto.ElementModP.newBuilder();
    builder.setValue(ByteString.copyFrom(modP.getBigInt().toByteArray()));
    return builder.build();
  }

  static CommonProto.ElGamalCiphertext convertCiphertext(ElGamal.Ciphertext ciphertext) {
    CommonProto.ElGamalCiphertext.Builder builder = CommonProto.ElGamalCiphertext.newBuilder();
    builder.setPad(convertElementModP(ciphertext.pad));
    builder.setData(convertElementModP(ciphertext.data));
    return builder.build();
  }

  static CommonProto.SchnorrProof convertSchnorrProof(SchnorrProof proof) {
    CommonProto.SchnorrProof.Builder builder = CommonProto.SchnorrProof.newBuilder();
    builder.setPublicKey(convertElementModP(proof.public_key));
    builder.setCommitment(convertElementModP(proof.commitment));
    builder.setChallenge(convertElementModQ(proof.challenge));
    builder.setResponse(convertElementModQ(proof.response));
    return builder.build();
  }
  
}