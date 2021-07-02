package com.sunya.electionguard.publish;

import com.sunya.electionguard.Group;
import net.jqwik.api.Example;

import java.math.BigInteger;

import static com.google.common.truth.Truth.assertThat;

public class TestBigIntegerCoding {

  @Example
  public void testBigIntegerEncoding() {
    BigInteger bi = Group.P;
    String s = bi.toString();
    System.out.printf("BigInteger.toString() = (%d) %s%n", s.length(), s);

    s = bi.toString(16);
    System.out.printf("BigInteger.toString(16) = (%d) %s%n", s.length(), s);

    Group.ElementModP modp = Group.int_to_p_unchecked(Group.P);
    s = modp.toString();
    System.out.printf("ElementModP.toString() = (%d) %s%n", s.length(), s);

    s = modp.to_hex();
    System.out.printf("ElementModP.to_hex() = (%d) %s%n", s.length(), s);
  }

  @Example
  public void testBigIntegerDecoding() {
    BigInteger bi = Group.P;
    String s = bi.toString();
    BigInteger rtrip = new BigInteger(s);
    assertThat(rtrip).isEqualTo(bi);

    s = bi.toString(16);
    rtrip = new BigInteger(s, 16);
    assertThat(rtrip).isEqualTo(bi);

    Group.ElementModP modp = Group.int_to_p_unchecked(Group.P);
    s = modp.to_hex();
    rtrip = new BigInteger(s, 16);
    assertThat(rtrip).isEqualTo(bi);
  }

  private final String cofactor = "0100000000000000000000000000000000000000000000000000000000000000BC93C467E37DB0C7A4D1BE3F810152CB56A1CECC3AF65CC0190C03DF34709B8AF6A64C0CEDCF2D559DA9D97F095C3076C686037619148D2C86C317102AFA2148031F04440AC0FF0C9A417A89212512E7607B2501DAA4D38A2C1410C4836149E2BDB8C8260E627C4646963EFFE9E16E495D48BD215C6D8EC9D1667657A2A1C8506F2113FFAD19A6B2BC7C45760456719183309F874BC9ACE570FFDA877AA2B23A2D6F291C1554CA2EB12F12CD009B8B8734A64AD51EB893BD891750B85162241D908F0C9709879758E7E8233EAB3BF2D6AB53AFA32AA153AD6682E5A0648897C9BE18A0D50BECE030C3432336AD9163E33F8E7DAF498F14BB2852AFFA814841EB18DD5F0E89516D557776285C16071D211194EE1C3F34642036AB886E3EC28882CE4003DEA335B4D935BAE4B58235B9FB2BAB713C8F705A1C7DE42220209D6BBCACC467318601565272E4A63E38E2499754AE493AC1A8E83469EEF35CA27C271BC792EEE21156E617B922EA8F713C22CF282DC5D6385BB12868EB781278FA0AB2A8958FCCB5FFE2E5C361FC174420122B0163CA4A46308C8C46C91EA7457C1AD0D69FD4A7F529FD4A7F529FD4A7F529FD4A7F529FD4A7F529FD4A7F529FD4A7F52A";
  private final String generator = "037DE384F98F6E038D2A3141825B33D5D45EC4CC64CFD15E750D6798F5196CF2A142CDF33F6EF853840EC7D4EC804794CFB0CFB65363B2566387B98EE0E3DEF1B706FA55D5038FFB4A62DCBB93B1DDD8D3B308DA86D1C3A525EF356FE5BB59314E65633480B396E1DD4B795F78DE07D86B0E2A05BE6AF78FD7F736FCBA6C032E26E050AF50A03C65FA7B6C87F4554CB57F3DABCBAD8EB9D8FDEBEEF58570669ACC3EDA17DBFC47B8B3C39AA08B829B28872E62B5D1B13A98F09D40AC20C2AB74A6750E7C8750B5141E221C41F55BBA31D8E41422B64D2CBA7AAA0E9FD8785702F6932825BF45DE8386D24900742062C1322B37C50AF182158090C35DA9355E6CF7F72DA39A2284FDFB1918B2A2A30E69501FA2342B728263DF23F1DB8355BDE1EB276FB3685F371672CEB313FDAB069CC9B11AB6C59BCE62BAAD96AAC96B0DBE0C7E71FCB22552545A5D1CEDEEE01E4BC0CDBDB76B6AD45F09AF5E71114A005F93AD97B8FE09274E76C94B2008926B38CAEC94C95E96D628F6BC80662BA06207801328B2C6A60526BF7CD02D9661385AC3B1CBDB50F759D0E9F61C11A07BF4218F299BCB2900520076EBD2D95A3DEE96D4809EF34ABEB83FDBA8A12C5CA82757288A89C931CF564F00E8A317AE1E1D828E61369BA0DDBADB10C136F8691101AD82DC54775AB8353840D9992197D80A6E94B38AC417CDDF40B0C73ABF03E8E0AA";
  private final String large_prime = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF93C467E37DB0C7A4D1BE3F810152CB56A1CECC3AF65CC0190C03DF34709AFFBD8E4B59FA03A9F0EED0649CCB621057D11056AE9132135A08E43B4673D74BAFEA58DEB878CC86D733DBE7BF38154B36CF8A96D1567899AAAE0C09D4C8B6B7B86FD2A1EA1DE62FF8643EC7C271827977225E6AC2F0BD61C746961542A3CE3BEA5DB54FE70E63E6D09F8FC28658E80567A47CFDE60EE741E5D85A7BD46931CED8220365594964B839896FCAABCCC9B31959C083F22AD3EE591C32FAB2C7448F2A057DB2DB49EE52E0182741E53865F004CC8E704B7C5C40BF304C4D8C4F13EDF6047C555302D2238D8CE11DF2424F1B66C2C5D238D0744DB679AF2890487031F9C0AEA1C4BB6FE9554EE528FDF1B05E5B256223B2F09215F3719F9C7CCC69DDF172D0D6234217FCC0037F18B93EF5389130B7A661E5C26E54214068BBCAFEA32A67818BD3075AD1F5C7E9CC3D1737FB28171BAF84DBB6612B7881C1A48E439CD03A92BF52225A2B38E6542E9F722BCE15A381B5753EA842763381CCAE83512B30511B32E5E8D80362149AD030AABA5F3A5798BB22AA7EC1B6D0F17903F4E234EA6034AA85973F79A93FFB82A75C47C03D43D2F9CA02D03199BACEDDD45334DBC6B5FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
  private final String small_prime = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF43";

  @Example
  public void testConstants() {
    assertThat(new BigInteger(large_prime, 16)).isEqualTo(Group.P);
    assertThat(new BigInteger(small_prime, 16)).isEqualTo(Group.Q);
    assertThat(new BigInteger(cofactor, 16)).isEqualTo(Group.R);
    assertThat(new BigInteger(generator, 16)).isEqualTo(Group.G);
  }
}