package com.sunya.electionguard.proto;

import com.sunya.electionguard.Manifest;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

import static com.sunya.electionguard.proto.CommonConvert.convertList;
import static com.sunya.electionguard.proto.ManifestProto.AnnotatedString;
import static com.sunya.electionguard.proto.ManifestProto.BallotStyle;
import static com.sunya.electionguard.proto.ManifestProto.Candidate;
import static com.sunya.electionguard.proto.ManifestProto.ContactInformation;
import static com.sunya.electionguard.proto.ManifestProto.ContestDescription;
import static com.sunya.electionguard.proto.ManifestProto.GeopoliticalUnit;
import static com.sunya.electionguard.proto.ManifestProto.InternationalizedText;
import static com.sunya.electionguard.proto.ManifestProto.Language;
import static com.sunya.electionguard.proto.ManifestProto.Party;
import static com.sunya.electionguard.proto.ManifestProto.SelectionDescription;

public class ManifestFromProto {

  public static Manifest translateFromProto(ManifestProto.Manifest election) {
    OffsetDateTime start_date = OffsetDateTime.parse(election.getStartDate());
    OffsetDateTime end_date = OffsetDateTime.parse(election.getEndDate());
    Manifest.InternationalizedText name = election.hasName() ?
            convertInternationalizedText(election.getName()) : null;
    Manifest.ContactInformation contact = election.hasContactInformation() ?
            convertContactInformation(election.getContactInformation()) : null;

    // String election_scope_id,
    //            Manifest.ElectionType type,
    //            OffsetDateTime start_date,
    //            OffsetDateTime end_date,
    //            List< Manifest.GeopoliticalUnit > geopolitical_units,
    //            List< Manifest.Party > parties,
    //            List< Manifest.Candidate > candidates,
    //            List< Manifest.ContestDescription > contests,
    //            List< Manifest.BallotStyle > ballot_styles,
    //            @Nullable InternationalizedText name,
    //            @Nullable ContactInformation contact_information
    return new Manifest(
            election.getElectionScopeId(),
            convert(election.getElectionType()),
            start_date,
            end_date,
            convertList(election.getGeopoliticalUnitsList(), ManifestFromProto::convertGeopoliticalUnit),
            convertList(election.getPartiesList(), ManifestFromProto::convertParty),
            convertList(election.getCandidatesList(), ManifestFromProto::convertCandidate),
            convertList(election.getContestsList(), ManifestFromProto::convertContestDescription),
            convertList(election.getBallotStylesList(), ManifestFromProto::convertBallotStyle),
            name,
            contact);
  }

  static Manifest.AnnotatedString convertAnnotatedString(AnnotatedString annotated) {
    return new Manifest.AnnotatedString(annotated.getAnnotation(), annotated.getValue());
  }

  static Manifest.BallotStyle convertBallotStyle(BallotStyle style) {
    return new Manifest.BallotStyle(
            style.getObjectId(),
            style.getGeopoliticalUnitIdsList(),
            style.getPartyIdsList(),
            style.getImageUrl());
  }

  static Manifest.Candidate convertCandidate(Candidate candidate) {
    return new Manifest.Candidate(
            candidate.getObjectId(),
            convertInternationalizedText(candidate.getName()),
            candidate.getPartyId(),
            candidate.getImageUrl(),
            candidate.getIsWriteIn());
  }

  @Nullable
  static Manifest.ContactInformation convertContactInformation(@Nullable ContactInformation contact) {
    if (contact == null) {
      return null;
    }
    return new Manifest.ContactInformation(
            contact.getAddressLineList(),
            convertList(contact.getEmailList(), ManifestFromProto::convertAnnotatedString),
            convertList(contact.getPhoneList(), ManifestFromProto::convertAnnotatedString),
            contact.getName());
  }

  static Manifest.ContestDescription convertContestDescription(ContestDescription contest) {
    return new Manifest.ContestDescription(
            contest.getObjectId(),
            contest.getElectoralDistrictId(),
            contest.getSequenceOrder(),
            convertVoteVariationType(contest.getVoteVariation()),
            contest.getNumberElected(),
            contest.getVotesAllowed(),
            contest.getName(),
            convertList(contest.getBallotSelectionsList(), ManifestFromProto::convertSelectionDescription),
            contest.hasBallotTitle() ? convertInternationalizedText(contest.getBallotTitle()) : null,
            contest.hasBallotSubtitle() ? convertInternationalizedText(contest.getBallotSubtitle()) : null);
  }

  static Manifest.VoteVariationType convertVoteVariationType(ContestDescription.VoteVariationType type) {
    return Manifest.VoteVariationType.valueOf(type.name());
  }

  static Manifest.ElectionType convert(ManifestProto.Manifest.ElectionType type) {
    return Manifest.ElectionType.valueOf(type.name());
  }

  static Manifest.ReportingUnitType convertReportingUnitType(GeopoliticalUnit.ReportingUnitType type) {
    return Manifest.ReportingUnitType.valueOf(type.name());
  }

  static Manifest.GeopoliticalUnit convertGeopoliticalUnit(GeopoliticalUnit geoUnit) {
    return new Manifest.GeopoliticalUnit(
            geoUnit.getObjectId(),
            geoUnit.getName(),
            convertReportingUnitType(geoUnit.getType()),
            geoUnit.hasContactInformation() ? convertContactInformation(geoUnit.getContactInformation()) : null);
  }

  static Manifest.InternationalizedText convertInternationalizedText(InternationalizedText text) {
    return new Manifest.InternationalizedText(convertList(text.getTextList(), ManifestFromProto::convertLanguage));
  }

  static Manifest.Language convertLanguage(Language language) {
    return new Manifest.Language(language.getValue(), language.getLanguage());
  }

  static Manifest.Party convertParty(Party party) {
    return new Manifest.Party(
            party.getObjectId(),
            convertInternationalizedText(party.getName()),
            party.getAbbreviation(),
            party.getColor(),
            party.getLogoUri());
  }

  static Manifest.SelectionDescription convertSelectionDescription(SelectionDescription selection) {
    return new Manifest.SelectionDescription(
            selection.getObjectId(),
            selection.getCandidateId(),
            selection.getSequenceOrder());
  }
}
