package sunya.integration;

import sunya.electionguard.Election;
import sunya.electionguard.ElectionFactory;

import java.io.IOException;

public class TestHamiltonCountyElection {

  public static void main(String[] args) throws IOException {
    ElectionFactory election_factory = new ElectionFactory();
    Election.ElectionDescription description = election_factory.get_hamilton_election_from_file();
  }
}
