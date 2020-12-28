package com.sunya.electionguard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.sunya.electionguard.Election.*;
import static com.sunya.electionguard.Group.*;
import static com.sunya.electionguard.TestUtils.elgamal_keypairs;

public class ElectionTestHelper {

  private static final List<String> _first_names = ImmutableList.of(
          "James",
          "Mary",
          "John",
          "Patricia",
          "Robert",
          "Jennifer",
          "Michael",
          "Linda",
          "William",
          "Elizabeth",
          "David",
          "Barbara",
          "Richard",
          "Susan",
          "Joseph",
          "Jessica",
          "Thomas",
          "Sarah",
          "Charles",
          "Karen",
          "Christopher",
          "Nancy",
          "Daniel",
          "Margaret",
          "Matthew",
          "Lisa",
          "Anthony",
          "Betty",
          "Donald",
          "Dorothy",
          "Sylvia",
          "Viktor",
          "Camille",
          "Mirai",
          "Anant",
          "Rohan",
          "François",
          "Altuğ",
          "Sigurður",
          "Böðmóður",
          "Quang Dũng"
  );

  private static final List<String> _last_names = ImmutableList.of(
          "SMITH",
          "JOHNSON",
          "WILLIAMS",
          "JONES",
          "BROWN",
          "DAVIS",
          "MILLER",
          "WILSON",
          "MOORE",
          "TAYLOR",
          "ANDERSON",
          "THOMAS",
          "JACKSON",
          "WHITE",
          "HARRIS",
          "MARTIN",
          "THOMPSON",
          "GARCIA",
          "MARTINEZ",
          "ROBINSON",
          "CLARK",
          "RODRIGUEZ",
          "LEWIS",
          "LEE",
          "WALKER",
          "HALL",
          "ALLEN",
          "YOUNG",
          "HERNANDEZ",
          "KING",
          "WRIGHT",
          "LOPEZ",
          "HILL",
          "SCOTT",
          "GREEN",
          "ADAMS",
          "BAKER",
          "GONZALEZ",
          "STEELE-LOY",
          "O'CONNOR",
          "ANAND",
          "PATEL",
          "GUPTA",
          "ĐẶNG");

  private static final String alphabet = "abcdefghijklmnopqrstuvwxyz";


  // valid email
  private static String email() {
    return "you@email.com";
  }

  // valid url
  private static String urls() {
    return "http://your/name/here";
  }

  private <T> T drawList(List<T> list) {
    Preconditions.checkArgument(list.size() > 0);
    return list.get(random.nextInt(list.size()));
  }

  private final Random random;

  ElectionTestHelper(Random random) {
    this.random = random;
  }

  // Generates a string with a human first and last name.
  String human_names() {
    int chooseFirst = random.nextInt(_first_names.size());
    int chooseLast = random.nextInt(_last_names.size());
    return String.format("%s %s", _first_names.get(chooseFirst), _last_names.get(chooseLast));
  }

  // random string
  String rstr(String root) {
    return String.format("%s-%d", root, random.nextInt());
  }

  ElectionType election_types() {
    ElectionType[] values = ElectionType.values();
    int choose = random.nextInt(values.length);
    return values[choose];
  }

  ReportingUnitType reporting_unit_types() {
    ReportingUnitType[] values = ReportingUnitType.values();
    int choose = random.nextInt(values.length);
    return values[choose];
  }

  ContactInformation contact_infos() {
    // empty lists for email and phone, for now
    return new ContactInformation(null, ImmutableList.of(annotated_strings()), null, human_names());
  }

  String two_letter_codes() {
    int chooseFirst = random.nextInt(alphabet.length());
    int chooseSecond = random.nextInt(alphabet.length());
    char[] choices = new char[2];
    choices[0] = alphabet.charAt(chooseFirst);
    choices[1] = alphabet.charAt(chooseSecond);
    return new String(choices);
  }


  //     Generates a `Language` object with an arbitrary two-letter string as the code and something messier for
  //     the text ostensibly written in that language.
  Language languages() {
    return new Language(rstr("text"), two_letter_codes());
  }

  //     Generates a `Language` object with an arbitrary two-letter string as the code and a human name for the
  //     text ostensibly written in that language.
  Language language_human_names() {
    return new Language(human_names(), two_letter_codes());
  }

  //     Generates an `InternationalizedText` object with a list of `Language` objects within (representing a multilingual string).
  InternationalizedText internationalized_texts() {
    return new InternationalizedText(ImmutableList.of(languages()));
  }

  //     Generates an `InternationalizedText` object with a list of `Language` objects within (representing a multilingual human name).
  InternationalizedText internationalized_human_names() {
    return new InternationalizedText(ImmutableList.of(language_human_names()));
  }


  //     Generates an `AnnotatedString` object with one `Language` and an associated `value` string.
  AnnotatedString annotated_strings() {
    Language s = languages();
    return new AnnotatedString(s.language, s.value);
  }


  /**
   * Generates a `BallotStyle` object, which rolls up a list of parties and
   * geopolitical units (passed as arguments), with some additional information
   * added on as well.
   * @param parties: a list of `Party` objects to be used in this ballot style
   * @param geo_units: a list of `GeopoliticalUnit` objects to be used in this ballot style
   */
  BallotStyle ballot_styles(List<Party> parties, List<GeopoliticalUnit> geo_units) {
    List<String> geopolitical_unit_ids = geo_units.stream().map(g -> g.object_id).collect(Collectors.toList());
    List<String> party_ids = parties.stream().map(p -> p.get_party_id()).collect(Collectors.toList());
    return new BallotStyle(rstr("BallotStyle"), geopolitical_unit_ids, party_ids, urls());
  }

  List<Party> party_lists(int num_parties) {
    List<Party> result = new ArrayList<>();
    for (int i = 0; i < num_parties; i++) {
      String partyName = String.format("Party%d", i);
      String partyAbbrv = String.format("P%d", i);
      result.add(new Party(
              rstr("Party"),
              new InternationalizedText(ImmutableList.of(new Language(partyName, "en"))),
              partyAbbrv,
              null,
              String.format("Logo%d", i)));
    }
    return result;
  }

  GeopoliticalUnit geopolitical_units() {
    return new GeopoliticalUnit(
            rstr("GeopoliticalUnit"),
            rstr("name"),
            reporting_unit_types(),
            contact_infos());
  }

  /**
   * Generates a `Candidate` object, assigning it one of the parties from `party_list` at random,
   * with a chance that there will be no party assigned at all.
   * @param party_listO: A list of `Party` objects. If None, then the resulting `Candidate`
   * will have no party.
   *
   * @return
   */
  Candidate candidates(Optional<List<Party>> party_listO) {
    String party_id;
    if (party_listO.isPresent()) {
      List<Party> party_list = party_listO.get();
      Party party = party_list.get(random.nextInt(party_list.size()));
      party_id = party.get_party_id();
    } else {
      party_id = null;
    }

    /*random.nextBoolean()
    String object_id,
                     InternationalizedText ballot_name,
                     @Nullable String party_id,
                     @Nullable String image_uri,
                     @Nullable Boolean is_write_in
     */
    return new Candidate(
            rstr("Candidate"),
            internationalized_human_names(),
            party_id,
            random.nextBoolean() ? null : urls(),
            false);
  }

  /**
   * Given a `Candidate` and its position in a list of candidates, returns an equivalent
   * `SelectionDescription`. The selection's `object_id` will contain the candidates's
   * `object_id` within, but will have a "c-" prefix attached, so you'll be able to
   * tell that they're related.
   */
  private SelectionDescription _candidate_to_selection_description(Candidate candidate, int sequence_order) {
    return new SelectionDescription(
            String.format("c-%s", candidate.object_id), candidate.get_candidate_id(), sequence_order);
  }

  public static class CandidateTuple {
    final List<Candidate> candidates;
    final ContestDescription contest; // CandidateContestDescription or ReferendumContestDescription

    public CandidateTuple(List<Candidate> candidates, ContestDescription contest) {
      this.candidates = candidates;
      this.contest = contest;
    }
  }

  /**
   * Generates a tuple: a `List[Candidate]` and a corresponding `CandidateContestDescription` for
   * an n-of-m contest.
   * @param sequence_order: integer describing the order of this contest; make these sequential when
   * generating many contests.
   * @param party_list: A list of `Party` objects; each candidate's party is drawn at random from this list.
   * @param geo_units: A list of `GeopoliticalUnit`; one of these goes into the `electoral_district_id`
   * @param no: optional integer, specifying a particular value for n in this n-of-m contest, otherwise
   * it's varied by Hypothesis.
   * @param mo: optional integer, specifying a particular value for m in this n-of-m contest, otherwise
   * it's varied by Hypothesis.
   *
   * @return
   */
  CandidateTuple candidate_contest_descriptions(
          int sequence_order,
          List<Party> party_list,
          List<GeopoliticalUnit> geo_units,
          Optional<Integer> no,
          Optional<Integer> mo) {

    int n = no.orElse(1 + random.nextInt(2));
    int m = mo.orElse(n + random.nextInt(3)); // for an n-of-m election

    // party_ids = [p.get_party_id() for p in party_list]
    List<String> party_ids = party_list.stream().map(p -> p.get_party_id()).collect(Collectors.toList());

    // contest_candidates = draw(lists(candidates(party_list), min_size=m, max_size=m))
    List<Candidate> contest_candidates = new ArrayList<>();
    for (int i = 0; i < m; i++) {
      contest_candidates.add(candidates(Optional.of(party_list)));
    }

    // selection_descriptions = [ _candidate_to_selection_description(contest_candidates[i], i) for i in range(m) ]
    List<SelectionDescription> selection_descriptions = new ArrayList<>();
    for (int i = 0; i < m; i++) {
      selection_descriptions.add(_candidate_to_selection_description(contest_candidates.get(i), i));
    }

    VoteVariationType vote_variation = (n == 1) ? VoteVariationType.one_of_m : VoteVariationType.n_of_m;

    return new CandidateTuple(
            contest_candidates,
            new CandidateContestDescription(
                    rstr("CandidateContestDescription"),
                    drawList(geo_units).object_id,
                    sequence_order,
                    vote_variation,
                    n,
                    n,  // should this be None or n?
                    rstr("Name"),
                    selection_descriptions,
                    internationalized_texts(),
                    internationalized_texts(),
                    party_ids));
  }


  /**
   * Similar to `contest_descriptions`, but guarantees that for the n-of-m contest that n < m,
   * therefore it's possible to construct an "overvoted" plaintext, which should then fail subsequent tests.
   *
   * @param sequence_order: integer describing the order of this contest; make these sequential when
   *                        generating many contests.
   * @param party_list:     A list of `Party` objects; each candidate's party is drawn at random from this list.
   * @param geo_units:      A list of `GeopoliticalUnit`; one of these goes into the `electoral_district_id`
   */
  CandidateTuple contest_descriptions_room_for_overvoting(
          int sequence_order,
          List<Party> party_list,
          List<GeopoliticalUnit> geo_units) {

    int n = 1 + random.nextInt(2);
    int m = n + 1 + random.nextInt(2);
    return candidate_contest_descriptions(
            sequence_order,
            party_list,
            geo_units,
            Optional.of(n),
            Optional.of(m));
  }


  /**
   * Generates a tuple: a list of party-less candidates and a corresponding `ReferendumContestDescription`.
   * @param sequence_order: integer describing the order of this contest; make these sequential when
   * generating many contests.
   * @param geo_units: A list of `GeopoliticalUnit`; one of these goes into the `electoral_district_id`
   */
  CandidateTuple referendum_contest_descriptions(int sequence_order, List<GeopoliticalUnit> geo_units) {
    int n = 1 + random.nextInt(2);

    // contest_candidates = draw(lists(candidates(None), min_size = n, max_size = n))
    List<Candidate> contest_candidates = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      contest_candidates.add(candidates(Optional.empty()));
    }

    // selection_descriptions = [_candidate_to_selection_description(contest_candidates[i], i) for i in range(n)]
    List<SelectionDescription> selection_descriptions = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      selection_descriptions.add(_candidate_to_selection_description(contest_candidates.get(i), i));
    }

    return new CandidateTuple(
            contest_candidates,
            new ReferendumContestDescription(
                    rstr("CandidateContestDescription"),
                    drawList(geo_units).object_id,
                    sequence_order,
                    VoteVariationType.one_of_m,
                    1,
                    1,  // should this be None or n?
                    rstr("Name"),
                    selection_descriptions,
                    internationalized_texts(),
                    internationalized_texts()));
  }

  /**
   * Generates either the result of `referendum_contest_descriptions` or `candidate_contest_descriptions`.
   * @param sequence_order: integer describing the order of this contest; make these sequential when
   * generating many contests.
   * @param party_list: A list of `Party` objects; each candidate's party is drawn at random from this list.
   * See `candidates` for details on this assignment.
   * @param geo_units: A list of `GeopoliticalUnit`; one of these goes into the `electoral_district_id`
   */
  CandidateTuple contest_descriptions(int sequence_order, List<Party> party_list, List<GeopoliticalUnit> geo_units) {
    return (random.nextBoolean()) ?
            referendum_contest_descriptions(sequence_order, geo_units) :
            candidate_contest_descriptions(sequence_order, party_list, geo_units, Optional.empty(), Optional.empty());
  }

  /**
   * Generates an `ElectionDescription` -- the top-level object describing an election.
   * @param num_parties: The number of parties that will be generated.
   * @param num_contests: The  number of contests that will be generated.
   */
  ElectionDescription election_descriptions(int num_parties, int num_contests) {
    Preconditions.checkArgument(num_parties > 0, "need at least one party");
    Preconditions.checkArgument(num_contests > 0, "need at least one contest");

    List<GeopoliticalUnit> geo_units = ImmutableList.of(geopolitical_units());
    List<Party> parties = party_lists(num_parties);

    // generate a collection candidates mapped to contest descriptions
    //  candidate_contests: List[Tuple[List[Candidate], ContestDescription]] = [
    //      draw(contest_descriptions(i, parties, geo_units)) for i in range(num_contests) ]
    List<CandidateTuple> candidate_contests = new ArrayList<>();
    for (int i = 0; i < num_contests; i++) {
      candidate_contests.add(contest_descriptions(i, parties, geo_units));
    }

    // candidates_ = reduce(lambda a, b:a + b,[candidate_contest[0] for candidate_contest in candidate_contests],)
    List<Candidate> candidates_ = candidate_contests.stream().map(t -> t.candidates).flatMap(List::stream)
            .collect(Collectors.toList());

    // contests = [candidate_contest[1] for candidate_contest in candidate_contests]
    List<ContestDescription> contests = candidate_contests.stream().map(t -> t.contest).collect(Collectors.toList());

    BallotStyle styles = ballot_styles(parties, geo_units);

    //maybe later on we'll do something more complicated with dates
    OffsetDateTime start_date = OffsetDateTime.now();
    OffsetDateTime end_date = start_date;

    return new ElectionDescription(
            rstr("election_scope_id"),
            ElectionType.general,  // good enough for now
            start_date,
            end_date,
            geo_units,
            parties,
            candidates_,
            contests,
            ImmutableList.of(styles),
            internationalized_texts(),
            contact_infos());
  }

  List<Ballot.PlaintextBallot> plaintext_voted_ballots(InternalElectionDescription metadata, int count) {
    // ballots: List[PlaintextBallot] = [] for i in range(count):ballots.append(draw(plaintext_voted_ballot(metadata)))
    List<Ballot.PlaintextBallot> ballots = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      ballots.add(plaintext_voted_ballot(metadata));
    }
    return ballots;
  }

  /**
   *     Given an `InternalElectionDescription` object, generates an arbitrary `PlaintextBallot` with the
   *     choices made randomly.
   *     @param metadata: Any `InternalElectionDescription
   */
  Ballot.PlaintextBallot plaintext_voted_ballot(InternalElectionDescription metadata) {
    int num_ballot_styles = metadata.ballot_styles.size();
    assertWithMessage("invalid election with no ballot styles").that(num_ballot_styles > 0).isTrue();

          // pick a ballot style at random
    BallotStyle ballot_style = drawList(metadata.ballot_styles);

    List<ContestDescriptionWithPlaceholders> contests = metadata.get_contests_for(ballot_style.object_id);
    assertWithMessage("invalid ballot style with no contests in it").that(contests.size() > 0).isTrue();

    List<Ballot.PlaintextBallotContest> voted_contests = new ArrayList<>();
    for (ContestDescriptionWithPlaceholders contest : contests) {
      assertWithMessage("every contest needs to be valid").that(contest.is_valid()).isTrue();
      // TODO dont really understand "we need exactly this many 1 's, and the rest 0' s"
      int n = contest.number_elected ; // we need exactly this many 1 's, and the rest 0' s
      ArrayList<SelectionDescription> ballot_selections = new ArrayList(contest.ballot_selections);
      assertThat(ballot_selections.size() >= n).isTrue();
      // TODO shuffle leave this out for now.
      // Collections.shuffle(ballot_selections);

      int cut_point = random.nextInt(n + 1); // a number between 0 and n, inclusive
      List<SelectionDescription> yes_votes = ballot_selections.subList(0, cut_point);
      List<SelectionDescription> no_votes = ballot_selections.subList(cut_point, ballot_selections.size());

      List<Ballot.PlaintextBallotSelection> voted_selections = new ArrayList<>();
      yes_votes.stream().map(d -> Encrypt.selection_from(d, false, true))
              .forEach(voted_selections::add);
      no_votes.stream().map(d -> Encrypt.selection_from(d, false, false))
              .forEach(voted_selections::add);

      voted_contests.add(new Ballot.PlaintextBallotContest(contest.object_id, voted_selections));
    }

    return new Ballot.PlaintextBallot(rstr("PlaintextBallot"), ballot_style.object_id, voted_contests);
  }

  static class CIPHERTEXT_ELECTIONS_TUPLE_TYPE {
    ElementModQ secret_key;
    CiphertextElectionContext context;

    public CIPHERTEXT_ELECTIONS_TUPLE_TYPE(ElementModQ secret_key, CiphertextElectionContext context) {
      this.secret_key = secret_key;
      this.context = context;
    }
  }


  /**
   *     Generates a `CiphertextElectionContext` with a single public-private key pair as the encryption context.
   *
   *     In a real election, the key ceremony would be used to generate a shared public key.
   *
   *     @param election_description: An `ElectionDescription` object, with which the `CiphertextElectionContext` will be associated
   *     @return a tuple of a `CiphertextElectionContext` and the secret key associated with it
   */
  CIPHERTEXT_ELECTIONS_TUPLE_TYPE ciphertext_elections(ElectionDescription election_description) {
    ElGamal.KeyPair keypair = elgamal_keypairs();
    return new CIPHERTEXT_ELECTIONS_TUPLE_TYPE(
            keypair.secret_key,
            make_ciphertext_election_context(
                    1,
                    1,
                    keypair.public_key,
                    election_description.crypto_hash()));
  }

  public static class EverythingTuple {
    ElectionDescription election_description;
    InternalElectionDescription internal_election_description;
    List<Ballot.PlaintextBallot> ballots;
    ElementModQ secret_key;
    CiphertextElectionContext context;

    public EverythingTuple(ElectionDescription election_description,
                 InternalElectionDescription internal_election_description,
                 List<Ballot.PlaintextBallot> ballots,
                 ElementModQ secret_key,
                 CiphertextElectionContext context) {
      this.election_description = election_description;
      this.internal_election_description = internal_election_description;
      this.ballots = ballots;
      this.secret_key = secret_key;
      this.context = context;
    }
  }

  /**
   * A convenience generator to generate all of the necessary components for simulating an election.
   * Every ballot will match the same ballot style. Hypothesis doesn't
   * let us declare a type hint on strategy return values, so you can use `ELECTIONS_AND_BALLOTS_TUPLE_TYPE`.
   * <p>
   *
   * @param num_ballots: The number of ballots to generate (default: 3).
   * @return: a tuple of: an `InternalElectionDescription`, a list of plaintext ballots, an ElGamal secret key,
   * and a `CiphertextElectionContext`
   */
  EverythingTuple elections_and_ballots(int num_ballots) {
    Preconditions.checkArgument(num_ballots >= 0, "You're asking for a negative number of ballots?");
    ElectionDescription election_description = election_descriptions(3, 3);
    InternalElectionDescription internal_election_description = new InternalElectionDescription(election_description);

    // ballots = [draw(plaintext_voted_ballots(internal_election_description)) for _ in range(num_ballots)]
    List<Ballot.PlaintextBallot> ballots = new ArrayList<>();
    for (int i=0; i<num_ballots; i++) {
      ballots.addAll(plaintext_voted_ballots(internal_election_description, 1));
    }

    CIPHERTEXT_ELECTIONS_TUPLE_TYPE tuple = ciphertext_elections(election_description);

    return new EverythingTuple(
            election_description,
            internal_election_description,
            ballots,
            tuple.secret_key,
            tuple.context);
  }

}
