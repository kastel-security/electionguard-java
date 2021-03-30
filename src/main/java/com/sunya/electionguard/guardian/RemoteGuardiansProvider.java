package com.sunya.electionguard.guardian;

import com.google.common.collect.ImmutableList;
import com.sunya.electionguard.proto.TrusteeFromProto;

import java.io.IOException;

/**
 * A GuardiansProvider using serialized Guardian objects from a "secret" place on disk.
 */
public class RemoteGuardiansProvider implements ProxyGuardiansProvider {
  private final String location;
  private Iterable<DecryptingTrusteeProxy> guardians;

  public RemoteGuardiansProvider(String location) {
    this.location = location;
  }

  @Override
  public Iterable<DecryptingTrusteeProxy> guardians() {
    if (guardians == null) {
      guardians = read(location);
    }
    return guardians;
  }

  static ImmutableList<DecryptingTrusteeProxy> read(String location) {
    try {
      ImmutableList<DecryptingTrustee> trustees = TrusteeFromProto.readTrustees(location);
      return trustees.stream().map(t -> new DecryptingTrusteeProxy(t)).collect(ImmutableList.toImmutableList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}