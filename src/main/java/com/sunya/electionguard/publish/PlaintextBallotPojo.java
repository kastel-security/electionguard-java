package com.sunya.electionguard.publish;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.sunya.electionguard.Ballot;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Conversion between PlaintextBallot and Json, using python's object model. */
public class PlaintextBallotPojo {
  public String object_id;
  public String ballot_style;
  public List<PlaintextBallotContest> contests;

  public static class PlaintextBallotContest {
    public String object_id;
    public List<PlaintextBallotSelection> ballot_selections;
  }

  public static class PlaintextBallotSelection {
    public String object_id;
    public String vote;
    public boolean is_placeholder_selection;
    public String extra_data; // optional
  }

  /////////////////////////////////////

  public static List<Ballot.PlaintextBallot> get_ballots_from_file(String filename) throws IOException {
    try (InputStream is = new FileInputStream(filename)) {
      Reader reader = new InputStreamReader(is);
      Gson gson = GsonTypeAdapters.enhancedGson();
      Type listType = new TypeToken<ArrayList<PlaintextBallotPojo>>(){}.getType();

      List<PlaintextBallotPojo> pojo = gson.fromJson(reader, listType);
      return convertList(pojo, PlaintextBallotPojo::convertPlaintextBallot);
    }
  }

  public static Ballot.PlaintextBallot get_ballot_from_file(String filename) throws IOException {
    try (InputStream is = new FileInputStream(filename)) {
      Reader reader = new InputStreamReader(is);
      Gson gson = GsonTypeAdapters.enhancedGson();
      PlaintextBallotPojo pojo = gson.fromJson(reader, PlaintextBallotPojo.class);
      return convertPlaintextBallot(pojo);
    }
  }

  @Nullable
  private static <T, U> List<U> convertList(@Nullable List<T> from, Function<T, U> converter) {
    return from == null ? null : from.stream().map(converter).collect(Collectors.toList());
  }

  ///////////////////////////////////////////////////////////////////////////////////////

  public static Ballot.PlaintextBallot deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    PlaintextBallotPojo pojo = gson.fromJson(jsonElem, PlaintextBallotPojo.class);
    return convertPlaintextBallot(pojo);
  }

  private static Ballot.PlaintextBallot convertPlaintextBallot(PlaintextBallotPojo pojo) {
    return new Ballot.PlaintextBallot(
            pojo.object_id,
            pojo.ballot_style,
            convertList(pojo.contests, PlaintextBallotPojo::convertPlaintextBallotContest));
  }

  private static Ballot.PlaintextBallotContest convertPlaintextBallotContest(PlaintextBallotPojo.PlaintextBallotContest pojo) {
    return new Ballot.PlaintextBallotContest(
            pojo.object_id,
            convertList(pojo.ballot_selections, PlaintextBallotPojo::convertPlaintextBallotSelection));
  }

  private static Ballot.PlaintextBallotSelection convertPlaintextBallotSelection(PlaintextBallotPojo.PlaintextBallotSelection pojo) {
    Ballot.ExtendedData extra = (pojo.extra_data == null) ? null :
            new Ballot.ExtendedData(pojo.extra_data, pojo.extra_data.length());

    return new Ballot.PlaintextBallotSelection(
            pojo.object_id,
            pojo.vote,
            pojo.is_placeholder_selection,
            extra);
  }

  ////////////////////////////////////////////////////////////////////////////////////////

  public static JsonElement serialize(Ballot.PlaintextBallot src) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    PlaintextBallotPojo pojo = convertPlaintextBallot(src);
    Type typeOfSrc = new TypeToken<PlaintextBallotPojo>() {}.getType();
    return gson.toJsonTree(pojo, typeOfSrc);
  }

  private static PlaintextBallotPojo convertPlaintextBallot(Ballot.PlaintextBallot src) {
     PlaintextBallotPojo pojo = new PlaintextBallotPojo();
    pojo.object_id = src.object_id;
    pojo.ballot_style = src.ballot_style;
    pojo.contests = convertList(src.contests, PlaintextBallotPojo::convertPlaintextBallotContest);
    return pojo;
  }

  private static PlaintextBallotPojo.PlaintextBallotContest convertPlaintextBallotContest(Ballot.PlaintextBallotContest src) {
    PlaintextBallotPojo.PlaintextBallotContest pojo = new PlaintextBallotPojo.PlaintextBallotContest ();
    pojo.object_id = src.contest_id;
    pojo.ballot_selections = convertList(src.ballot_selections, PlaintextBallotPojo::convertPlaintextBallotSelection);
    return pojo;
  }

  private static PlaintextBallotPojo.PlaintextBallotSelection convertPlaintextBallotSelection(Ballot.PlaintextBallotSelection src) {
    PlaintextBallotPojo.PlaintextBallotSelection pojo = new PlaintextBallotPojo.PlaintextBallotSelection ();
    pojo.object_id = src.selection_id;
    pojo.vote = src.vote;
    pojo.is_placeholder_selection = src.is_placeholder_selection;
    src.extended_data.ifPresent( data -> pojo.extra_data = data.value);
    return pojo;
  }

}
