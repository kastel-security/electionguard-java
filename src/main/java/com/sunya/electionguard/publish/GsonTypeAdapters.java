package com.sunya.electionguard.publish;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sunya.electionguard.Ballot;
import com.sunya.electionguard.Election;
import com.sunya.electionguard.Group;
import com.sunya.electionguard.KeyCeremony;
import com.sunya.electionguard.PublishedCiphertextTally;
import com.sunya.electionguard.PlaintextTally;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;

public class GsonTypeAdapters {

  public static Gson enhancedGson() {
    return new GsonBuilder().setPrettyPrinting().serializeNulls()
            .registerTypeAdapter(Group.ElementModQ.class, new ModQDeserializer())
            .registerTypeAdapter(Group.ElementModQ.class, new ModQSerializer())
            .registerTypeAdapter(Group.ElementModP.class, new ModPDeserializer())
            .registerTypeAdapter(Group.ElementModP.class, new ModPSerializer())
            .registerTypeAdapter(Election.ElectionDescription.class, new ElectionDescriptionSerializer())
            .registerTypeAdapter(Election.ElectionDescription.class, new ElectionDescriptionDeserializer())
            .registerTypeAdapter(KeyCeremony.CoefficientValidationSet.class, new CoefficientsSerializer())
            .registerTypeAdapter(KeyCeremony.CoefficientValidationSet.class, new CoefficientsDeserializer())
            .registerTypeAdapter(Ballot.CiphertextAcceptedBallot.class, new CiphertextBallotSerializer())
            .registerTypeAdapter(Ballot.CiphertextAcceptedBallot.class, new CiphertextBallotDeserializer())
            .registerTypeAdapter(Ballot.PlaintextBallot.class, new PlaintextBallotSerializer())
            .registerTypeAdapter(Ballot.PlaintextBallot.class, new PlaintextBallotDeserializer())
            .registerTypeAdapter(PlaintextTally.class, new PlaintextTallySerializer())
            .registerTypeAdapter(PlaintextTally.class, new PlaintextTallyDeserializer())
            .registerTypeAdapter(PublishedCiphertextTally.class, new PublishedCiphertextTallySerializer())
            .registerTypeAdapter(PublishedCiphertextTally.class, new PublishedCiphertextTallyDeserializer())
            .create();
  }

  private static class ModQDeserializer implements JsonDeserializer<Group.ElementModQ> {
    @Override
    public Group.ElementModQ deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      String content = json.getAsJsonPrimitive().getAsString();
      return Group.int_to_q(new BigInteger(content)).orElseThrow(RuntimeException::new);
    }
  }

  private static class ModPDeserializer implements JsonDeserializer<Group.ElementModP> {
    @Override
    public Group.ElementModP deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      String content = json.getAsJsonPrimitive().getAsString();
      return Group.int_to_p(new BigInteger(content)).orElseThrow(RuntimeException::new);
    }
  }

  private static class ModQSerializer implements JsonSerializer<Group.ElementModQ> {
    @Override
    public JsonElement serialize(Group.ElementModQ src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.getBigInt().toString());
    }
  }

  private static class ModPSerializer implements JsonSerializer<Group.ElementModP> {
    @Override
    public JsonElement serialize(Group.ElementModP src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.getBigInt().toString());
    }
  }

  private static class ElectionDescriptionSerializer implements JsonSerializer<Election.ElectionDescription> {
    @Override
    public JsonElement serialize(Election.ElectionDescription src, Type typeOfSrc, JsonSerializationContext context) {
      return ElectionDescriptionToJson.serialize(src);
    }
  }

  private static class ElectionDescriptionDeserializer implements JsonDeserializer<Election.ElectionDescription> {
    @Override
    public Election.ElectionDescription deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return ElectionDescriptionFromJson.deserialize(json);
    }
  }

  private static class CoefficientsSerializer implements JsonSerializer<KeyCeremony.CoefficientValidationSet> {
    @Override
    public JsonElement serialize(KeyCeremony.CoefficientValidationSet src, Type typeOfSrc, JsonSerializationContext context) {
      return CoefficientsPojo.serialize(src);
    }
  }

  private static class CoefficientsDeserializer implements JsonDeserializer<KeyCeremony.CoefficientValidationSet> {
    @Override
    public KeyCeremony.CoefficientValidationSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return CoefficientsPojo.deserialize(json);
    }
  }

  private static class CiphertextBallotSerializer implements JsonSerializer<Ballot.CiphertextAcceptedBallot> {
    @Override
    public JsonElement serialize(Ballot.CiphertextAcceptedBallot src, Type typeOfSrc, JsonSerializationContext context) {
      return CiphertextAcceptedBallotPojo.serialize(src);
    }
  }

  private static class CiphertextBallotDeserializer implements JsonDeserializer<Ballot.CiphertextAcceptedBallot> {
    @Override
    public Ballot.CiphertextAcceptedBallot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return CiphertextAcceptedBallotPojo.deserialize(json);
    }
  }

  private static class PlaintextBallotSerializer implements JsonSerializer<Ballot.PlaintextBallot> {
    @Override
    public JsonElement serialize(Ballot.PlaintextBallot src, Type typeOfSrc, JsonSerializationContext context) {
      return PlaintextBallotPojo.serialize(src);
    }
  }

  private static class PlaintextBallotDeserializer implements JsonDeserializer<Ballot.PlaintextBallot> {
    @Override
    public Ballot.PlaintextBallot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return PlaintextBallotPojo.deserialize(json);
    }
  }

  private static class PlaintextTallySerializer implements JsonSerializer<PlaintextTally> {
    @Override
    public JsonElement serialize(PlaintextTally src, Type typeOfSrc, JsonSerializationContext context) {
      return PlaintextTallyPojo.serialize(src);
    }
  }

  private static class PlaintextTallyDeserializer implements JsonDeserializer<PlaintextTally> {
    @Override
    public PlaintextTally deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return PlaintextTallyPojo.deserialize(json);
    }
  }

  private static class PublishedCiphertextTallySerializer implements JsonSerializer<PublishedCiphertextTally> {
    @Override
    public JsonElement serialize(PublishedCiphertextTally src, Type typeOfSrc, JsonSerializationContext context) {
      return PublishedCiphertextTallyPojo.serialize(src);
    }
  }

  private static class PublishedCiphertextTallyDeserializer implements JsonDeserializer<PublishedCiphertextTally> {
    @Override
    public PublishedCiphertextTally deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return PublishedCiphertextTallyPojo.deserialize(json);
    }
  }

  @SuppressWarnings("UnstableApiUsage")
  public static final class ImmutableListDeserializer implements JsonDeserializer<ImmutableList<?>> {
    @Override
    public ImmutableList<?> deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) throws JsonParseException {
      final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
      final Type parameterizedType = listOf(typeArguments[0]).getType();
      final List<?> list = context.deserialize(json, parameterizedType);
      return ImmutableList.copyOf(list);
    }

    private <E> TypeToken<List<E>> listOf(final Type arg) {
      return new TypeToken<List<E>>() {}
              .where(new TypeParameter<>() {}, (TypeToken<E>) TypeToken.of(arg));
    }
  }
}