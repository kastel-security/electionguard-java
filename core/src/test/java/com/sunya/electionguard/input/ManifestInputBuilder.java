package com.sunya.electionguard.input;

import com.google.common.collect.ImmutableList;
import com.sunya.electionguard.ElectionContext;
import com.sunya.electionguard.Manifest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManifestInputBuilder {
  public static final String districtDef = "district";
  public static final String styleDef = "styling";

  private final String manifest_name;
  private final ArrayList<ContestBuilder> contests = new ArrayList<>();
  private final Set<Manifest.Candidate> candidates = new HashSet<>();
  private String style = styleDef;
  private Manifest.BallotStyle ballotStyle;
  private String district = districtDef;

  public ManifestInputBuilder(String manifest_name) {
    this.manifest_name = manifest_name;
  }

  ManifestInputBuilder setGpunit(String gpunit) {
    this.district = gpunit;
    return this;
  }

  ManifestInputBuilder setStyle(String style) {
    this.style = style;
    return this;
  }

  ManifestInputBuilder setBallotStyle(Manifest.BallotStyle ballotStyle) {
    this.ballotStyle = ballotStyle;
    return this;
  }

  public ContestBuilder addContest(String contest_id) {
    ContestBuilder c = new ContestBuilder(contest_id);
    contests.add(c);
    return c;
  }

  public void addCandidate(String candidate_id) {
    Manifest.Candidate c = new Manifest.Candidate(candidate_id);
    candidates.add(c);
  }

  public Manifest build() {
    Manifest.GeopoliticalUnit gpUnit = new Manifest.GeopoliticalUnit(district, "name", Manifest.ReportingUnitType.congressional, null);
    Manifest.BallotStyle ballotStyle = this.ballotStyle != null ? this.ballotStyle :
            new Manifest.BallotStyle(style, ImmutableList.of(district), null, null);

    List<Manifest.Party> parties = ImmutableList.of(new Manifest.Party("dog"), new Manifest.Party("cat"));

    // String election_scope_id,
    //                  ElectionType type,
    //                  OffsetDateTime start_date,
    //                  OffsetDateTime end_date,
    //                  List<GeopoliticalUnit> geopolitical_units,
    //                  List<Party> parties,
    //                  List<Candidate> candidates,
    //                  List<ContestDescription> contests,
    //                  List<BallotStyle> ballot_styles,
    //                  @Nullable InternationalizedText name,
    //                  @Nullable ContactInformation contact_information
    return new Manifest(manifest_name, ElectionContext.SPEC_VERSION, Manifest.ElectionType.general,
            OffsetDateTime.now(), OffsetDateTime.now(),
            ImmutableList.of(gpUnit), parties, candidates.stream().toList(),
            contests.stream().map(ContestBuilder::build).toList(),
            ImmutableList.of(ballotStyle), null, null, null);
  }

  private static int contest_seq = 0;
  private static int selection_seq = 0;
  public class ContestBuilder {
    private final String id;
    private final int seq = contest_seq++;
    private final ArrayList<SelectionBuilder> selections = new ArrayList<>();
    private int allowed = 1;
    private String district = districtDef;

    ContestBuilder(String id) {
      this.id = id;
    }

    ContestBuilder setAllowedVotes(int allowed) {
      this.allowed = allowed;
      return this;
    }

    ContestBuilder setGpunit(String gpunit) {
      this.district = gpunit;
      return this;
    }

    public ContestBuilder addSelection(String id, String candidate_id) {
      SelectionBuilder s = new SelectionBuilder(id, candidate_id);
      selections.add(s);
      addCandidate(candidate_id);
      return this;
    }

    public ManifestInputBuilder done() {
      return ManifestInputBuilder.this;
    }

    public Manifest.ContestDescription build() {
      // String object_id,
      //                              String electoral_district_id,
      //                              int sequence_order,
      //                              VoteVariationType vote_variation,
      //                              int number_elected,
      //                              int votes_allowed,
      //                              String name,
      //                              List<SelectionDescription> ballot_selections,
      //                              @Nullable InternationalizedText ballot_title,
      //                              @Nullable InternationalizedText ballot_subtitle
      return new Manifest.ContestDescription(id, seq, district, Manifest.VoteVariationType.one_of_m,
              allowed, allowed, "name",
              selections.stream().map(SelectionBuilder::build).toList(),
              null, null, ImmutableList.of());
    }

    public class SelectionBuilder {
      private final String id;
      private final String candidate_id;
      private final int seq = selection_seq++;

      SelectionBuilder(String id, String candidate_id) {
        this.id = id;
        this.candidate_id = candidate_id;
      }

      Manifest.SelectionDescription build() {
        return new Manifest.SelectionDescription(id, seq, candidate_id);
      }
    }
  }

}
