package com.sunya.electionguard;

import net.jqwik.api.*;

import java.math.BigInteger;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static com.sunya.electionguard.Group.*;
import static org.junit.Assert.fail;

public class TestGroupProperties extends TestProperties {

  //// TestEquality
  @Property
  public void testPsNotEqualToQs(@ForAll("elements_mod_q") ElementModQ q, @ForAll("elements_mod_q") ElementModQ q2) {
    ElementModP p = int_to_p_unchecked(q.getBigInt());
    ElementModP p2 = int_to_p_unchecked(q2.getBigInt());

    // same value should imply they're equal
    assertThat(p).isEqualTo(q);
    assertThat(q).isEqualTo(p);

    if (!q.getBigInt().equals(q2.getBigInt())) {
      // these are genuinely different numbers
      assertThat(q).isNotEqualTo(q2);
      assertThat(p).isNotEqualTo(p2);
      assertThat(q).isNotEqualTo(p2);
      assertThat(p).isNotEqualTo(q2);

      // of course, we're going to make sure that a number is equal to itself
      assertThat(p).isEqualTo(p);
      assertThat(q).isEqualTo(q);
    }
  }

  //// TestModularArithmetic
  @Property
  public void test_add_q(@ForAll("elements_mod_q") ElementModQ q) {
    BigInteger as_int = q.elem.add(BigInteger.ONE);
    ElementModQ as_elem = add_q(q, ONE_MOD_Q);
    assertThat(as_int).isEqualTo(as_elem.elem);
  }

  @Property
  public void test_no_mult_inv_of_zero() {
    try {
      mult_inv_p(ZERO_MOD_P);
      fail();
    } catch (Exception e) {
      //correct
    }
  }

  @Property
  public void test_mult_inverses(@ForAll("elements_mod_p_no_zero") ElementModP p_no_zero) {
    ElementModP inv = mult_inv_p(p_no_zero);
    assertThat(mult_p(p_no_zero, inv)).isEqualTo(ONE_MOD_P);
  }

  @Property
  public void test_mult_identity(@ForAll("elements_mod_p") ElementModP p) {
    assertThat(p).isEqualTo(mult_p(p));
  }

  @Property
  public void test_mult_noargs() {
    assertThat(ONE_MOD_P).isEqualTo(mult_p());
  }

  @Property
  public void test_add_noargs() {
    assertThat(ZERO_MOD_Q).isEqualTo(add_q());
  }

  @Property
  public void test_simple_powers() {
    ElementModP gp = int_to_p_unchecked(getPrimes().generator);
    assertThat(gp).isEqualTo(g_pow_p(ONE_MOD_Q));
    assertThat(ONE_MOD_P).isEqualTo(g_pow_p(ZERO_MOD_Q));
  }

  @Property
  public void test_in_bounds_q(@ForAll("elements_mod_q") ElementModQ q) {
    assertThat(q.is_in_bounds()).isTrue();
    BigInteger too_big = q.getBigInt().add(Group.getPrimes().small_prime);
    BigInteger too_small = q.getBigInt().subtract(Group.getPrimes().small_prime);
    assertThat(int_to_q_unchecked(too_big).is_in_bounds()).isFalse();
    assertThat(int_to_q_unchecked(too_small).is_in_bounds()).isFalse();
    assertThat(int_to_q(too_big)).isEmpty();
    assertThat(int_to_q(too_small)).isEmpty();
  }

  @Property
  public void test_in_bounds_p(@ForAll("elements_mod_p") ElementModP p) {
    assertThat(p.is_in_bounds()).isTrue();
    BigInteger too_big = p.getBigInt().add(Group.getPrimes().large_prime);
    BigInteger too_small = p.getBigInt().subtract(Group.getPrimes().large_prime);
    assertThat(int_to_p_unchecked(too_big).is_in_bounds()).isFalse();
    assertThat(int_to_p_unchecked(too_small).is_in_bounds()).isFalse();
    assertThat(int_to_p(too_big)).isEmpty();
    assertThat(int_to_p(too_small)).isEmpty();
  }

  @Property
  public void test_in_bounds_q_no_zero(@ForAll("elements_mod_q_no_zero") ElementModQ q_no_zero) {
    assertThat(is_in_bounds_no_zero(q_no_zero)).isTrue();
    assertThat(is_in_bounds_no_zero(ZERO_MOD_Q)).isFalse();
    assertThat(is_in_bounds_no_zero(int_to_q_unchecked(q_no_zero.getBigInt().add(Group.getPrimes().small_prime)))).isFalse();
    assertThat(is_in_bounds_no_zero(int_to_q_unchecked(q_no_zero.getBigInt().subtract(Group.getPrimes().small_prime)))).isFalse();
  }

  @Property
  public void test_in_bounds_p_no_zero(@ForAll("elements_mod_p_no_zero") ElementModP p_no_zero) {
    assertThat(is_in_bounds_no_zero(p_no_zero)).isTrue();
    assertThat(is_in_bounds_no_zero(ZERO_MOD_P)).isFalse();
    assertThat(is_in_bounds_no_zero(int_to_p_unchecked(p_no_zero.getBigInt().add(Group.getPrimes().large_prime)))).isFalse();
    assertThat(is_in_bounds_no_zero(int_to_p_unchecked(p_no_zero.getBigInt().subtract(Group.getPrimes().large_prime)))).isFalse();
  }

  @Property
  public void test_large_values_rejected_by_int_to_q(@ForAll("elements_mod_q") ElementModQ q) {
    BigInteger oversize = q.elem.add(Group.getPrimes().small_prime);
    assertThat(int_to_q(oversize)).isEmpty();
  }

  private boolean is_in_bounds_no_zero(ElementModP p) {
    return Group.between(BigInteger.ONE, p.elem, Group.getPrimes().large_prime);
  }

  private boolean is_in_bounds_no_zero(ElementModQ q) {
    return Group.between(BigInteger.ONE, q.elem, Group.getPrimes().small_prime);
  }

}
