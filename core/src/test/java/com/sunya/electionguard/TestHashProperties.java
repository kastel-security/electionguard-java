package com.sunya.electionguard;

import com.google.common.collect.ImmutableList;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;

public class TestHashProperties extends TestProperties {

  @Property
  public void test_same_answer_twice_in_a_row(@ForAll("elements_mod_q") Group.ElementModQ q, @ForAll("elements_mod_p") Group.ElementModP p) {
    // if this doesn't work, then our hash function isn't a function
    Group.ElementModQ  h1 = Hash.hash_elems(q, p);
    Group.ElementModQ  h2 = Hash.hash_elems(q, p);
    assertThat(h1).isEqualTo(h2);
  }

  @Property
  public void test_basic_hash_properties(@ForAll("elements_mod_q") Group.ElementModQ q, @ForAll("elements_mod_q") Group.ElementModQ q2) {
    Group.ElementModQ h1 = Hash.hash_elems(q);
    Group.ElementModQ h2 = Hash.hash_elems(q2);
    assertThat(h1.equals(h2)).isEqualTo(q.equals(q2));
  }

  @Example
  public void test_hash_for_zero_number_is_zero_string() {
    assertThat(Hash.hash_elems(0)).isEqualTo(Hash.hash_elems("0"));
  }

  @Example
  public void test_hash_for_non_zero_number_string_same_as_explicit_number() {
    assertThat(Hash.hash_elems(1)).isEqualTo(Hash.hash_elems("1"));
  }

  @Example
  public void test_different_strings_casing_not_the_same_hash() {
    assertThat(Hash.hash_elems("Welcome To ElectionGuard")).isNotEqualTo(Hash.hash_elems("welcome To ElectionGuard"));
  }

  @Example
  public void test_hash_for_none_same_as_null_string() {
    assertThat(Hash.hash_elems(Optional.empty())).isEqualTo(Hash.hash_elems("null"));
  }

  @Example
  public void test_hash_of_same_values_in_list_are_same_hash() {
    assertThat(Hash.hash_elems(List.of("0", "0"))).isEqualTo(Hash.hash_elems(List.of("0", "0")));
  }

  // @Example fails
  public void test_hash_null_equivalents() {
    assertThat(Hash.hash_elems(List.of())).isEqualTo(Hash.hash_elems("null"));
    assertThat(Hash.hash_elems(List.of())).isEqualTo(Hash.hash_elems(Optional.empty()));
  }

  @Example
  public void test_hash_not_null_equivalents() {
    assertThat(Hash.hash_elems(List.of())).isNotEqualTo(Hash.hash_elems(0));
    assertThat(Hash.hash_elems(List.of())).isNotEqualTo(Hash.hash_elems(""));
  }

  @Example
  public void test_object_arrays() {
    String s = "test";
    Group.ElementModQ q1 = TestUtils.elements_mod_q();
    Group.ElementModQ q2 = TestUtils.elements_mod_q();
    Group.ElementModQ q3 = TestUtils.elements_mod_q();

    Group.ElementModQ h1 = Hash.hash_elems(s, q1, q2, q3);
    assertThat(h1).isEqualTo(Hash.hash_elems(s, q1, q2, q3));

    List<Group.ElementModQ> list = ImmutableList.of(q2, q3);
    Group.ElementModQ h2 = Hash.hash_elems(s, q1, list);
    assertThat(h2).isEqualTo(Hash.hash_elems(s, q1, list));
    assertThat(h1).isNotEqualTo(h2);
  }

  @Example
  public void test_python_hash() {
    Group.setPrimes(ElectionConstants.STANDARD_CONSTANTS);
    BigInteger bc = new BigInteger("456307447671777756516264348336497927238843602051346751575459074974301384166262514429149690566915978060486447070032990910712583572336622083032351729951048606771649542469023496667235836642139711235512741559721544287819964425149979339020727602556270801887613908010846402932303712072694690956440629767609197078644072805252764985336319844720473777347991151787926150472970654903090663653438785056840028559839707422529989465914151666532545416342804653990846676917164188539357052164158807234803567677447887320978408940131597189038730191564711633429090345245721289991680082007381028850457505918457557846207644308309844287273030886387852734231865260826619958504972588541549204640931013398853896977332299492018782892146827436446749316900004557719324866688568203426487110770548138445597631587221491552619514671748249136925562389016205764964327400538043849809385463974899721671875536784816124893672424200290614915221708728208000622838197198848547322365615576092323082001736300160716065631595608027231813877198585699764254372325111129867562297006067146594985395180878628936825710583932148925344393571929392413956759418131406483236320221958244269824985854388462575548025300803359492531181574497707262348175004765374794838655178400804449791397949594");
    Group.ElementModP commitment = Group.int_to_p_unchecked(bc);

    BigInteger bk = new BigInteger("309694985779474339872804589384518892195154485634096698903099248622790407217361779540339247474379093588382554784973875701846430451626061540003651570874611023423585748619649761484926991915841517616795657848691852682195357532407804428632795157756408985993317975137553787404163397542845923151815574185499713719481578126455336680068426904405793229038752416357810175892135343613440938166728972747633035842708881701893112895620739439060052694477719328527130365438190230952227975916272162564050820952059458759016507474936417513080527679853148602803008451039596386313502135292753416813661242857201641146361132184966294936555669892307990227920782116708631403670577431800319153583060376988877152749222960422639958964283434787696957760831206932370706095920817805992687098109439020144430561879479742985933255804065176348268023740269108527343150740249239866073807252861925927264921875386966762800429982040632134057064321193879848444264400292171396965420022772615566594385309503051047361784464105316695261063948358659313467765380488628788025293080864896770013092168844410026102117774744737424134535490892163634751800227899046286151270906697803040431004557596168742636279317938775868648893088818750890182940010895051657318680028553166409352251447369");
    Group.ElementModP public_key = Group.int_to_p_unchecked(bk);

    Group.ElementModQ hash = Hash.hash_elems(public_key, commitment);
    System.out.printf("hash = %s%n", hash);

    // result from python code.
    BigInteger expected = new BigInteger("73907835436692778174315355071416211755752665140661101495813745896807543323020");
    assertThat(hash.getBigInt()).isEqualTo(expected);
  }

}