package com.sunya.electionguard.proto;

import com.sunya.electionguard.PublishedCiphertextTally;
import com.sunya.electionguard.PlaintextTally;
import com.sunya.electionguard.publish.ConvertFromJson;
import com.sunya.electionguard.publish.Publisher;
import net.jqwik.api.Example;
import net.jqwik.api.lifecycle.BeforeContainer;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class TestTallyToProtoRoundtrip {
  private static Publisher publisher;

  @BeforeContainer
  public static void setup() throws IOException {
    publisher = new Publisher(TestElectionDescriptionToProtoRoundtrip.testElectionRecord, false);
  }

  @Example
  public void testCiphertextTallyRoundtrip() throws IOException {
    PublishedCiphertextTally fromPython = ConvertFromJson.readCiphertextTally(publisher.encryptedTallyFile().toString());
    assertThat(fromPython).isNotNull();
    CiphertextTallyProto.PublishedCiphertextTally proto = CiphertextTallyToProto.translateToProto(fromPython);
    PublishedCiphertextTally roundtrip = CiphertextTallyFromProto.translateFromProto(proto);
    assertThat(roundtrip).isEqualTo(fromPython);
  }

  @Example
  public void testPlaintextTallyRoundtrip() throws IOException {
    PlaintextTally fromPython = ConvertFromJson.readPlaintextTally(publisher.tallyFile().toString());
    assertThat(fromPython).isNotNull();
    PlaintextTallyProto.PlaintextTally proto = PlaintextTallyToProto.translateToProto(fromPython);
    PlaintextTally roundtrip = PlaintextTallyFromProto.translateFromProto(proto);
    assertThat(roundtrip).isEqualTo(fromPython);
  }
}
