package com.sunya.electionguard.standard;

import com.sunya.electionguard.GuardianRecord;
import com.sunya.electionguard.json.ConvertFromJson;
import com.sunya.electionguard.json.ConvertToJson;
import net.jqwik.api.Example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

public class TestGuardianRecordPrivateJsonRoundtrip {

  @Example
  public void readGuardianRecordPrivateRoundtrip() throws IOException {
    File file = File.createTempFile("temp", null);
    file.deleteOnExit();
    String outputFile = file.getAbsolutePath();

    // original
    Guardian guardian = Guardian.createForTesting("test", 5, 4, 3, null);
    GuardianPrivateRecord org = guardian.export_private_data();
    // write json
    ConvertToJson.writeGuardianRecordPrivate(org, Path.of(outputFile));
    // read it back
    GuardianPrivateRecord fromFile = ConvertFromJson.readGuardianRecordPrivate(outputFile);
    assertThat(fromFile).isEqualTo(org);
  }

  @Example
  public void testGuardianRecordRoundtrip() throws IOException {
    File file = File.createTempFile("temp", null);
    file.deleteOnExit();
    String outputFile = file.getAbsolutePath();

    // original
    Guardian guardian = Guardian.createForTesting("test", 5, 4, 3, null);
    GuardianRecord org = guardian.publish();
    // write json
    ConvertToJson.writeGuardianRecord(org, file.toPath());
    // read it back
    GuardianRecord fromFile = ConvertFromJson.readGuardianRecord(outputFile);
    assertThat(fromFile).isEqualTo(org);
  }

}
