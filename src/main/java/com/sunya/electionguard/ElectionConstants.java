package com.sunya.electionguard;

import com.google.common.base.Preconditions;

import javax.annotation.concurrent.Immutable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * The constants for mathematical functions used for this election.
 * python: stores these as Group.ElementMod, so that their Json Serialization is in hex.
 */
@Immutable
public class ElectionConstants {

  enum PrimeOption { Standard, LargeTest, MediumTest, SmallTest, ExtraSmallTest, StandardPrevious, Custom }

  public static ElectionConstants get(PrimeOption used) {
    switch (used) {
      case Standard : return STANDARD_CONSTANTS;
      case LargeTest: return LARGE_TEST_CONSTANTS;
      case MediumTest: return MEDIUM_TEST_CONSTANTS;
      case SmallTest: return SMALL_TEST_CONSTANTS;
      case ExtraSmallTest: return EXTRA_SMALL_TEST_CONSTANTS;
    }
    throw new IllegalStateException();
  }

  public static final ElectionConstants STANDARD_CONSTANTS = new ElectionConstants(
          new BigInteger("1044388881413152506691752710716624382579964249047383780384233483283953907971553643537729993126875883902173634017777416360502926082946377942955704498542097614841825246773580689398386320439747911160897731551074903967243883427132918813748016269754522343505285898816777211761912392772914485521155521641049273446207578961939840619466145806859275053476560973295158703823395710210329314709715239251736552384080845836048778667318931418338422443891025911884723433084701207771901944593286624979917391350564662632723703007964229849154756196890615252286533089643184902706926081744149289517418249153634178342075381874131646013444796894582106870531535803666254579602632453103741452569793905551901541856173251385047414840392753585581909950158046256810542678368121278509960520957624737942914600310646609792665012858397381435755902851312071248102599442308951327039250818892493767423329663783709190716162023529669217300939783171415808233146823000766917789286154006042281423733706462905243774854543127239500245873582012663666430583862778167369547603016344242729592244544608279405999759391099769165589722584216017468464576217318557948461765770700913220460557598574717173408252913596242281190298966500668625620138188265530628036538314433100326660047110143"),
          new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639747"),  //  pow(2, 256) - 189")
          new BigInteger("9019518416950528558373478086511232658951474842525520401496114928154304263969655687927867442562559311457926593510757267649063628681241064260953609180947464800958467390949485096429653122916928704841547265126247408167856620024815508684472819746384115369148322548696439327979752948311712506113890045287907335656308945630141969472484100558565879585476547782717283106837945923693806973017510492730838409381014701258202694245760602718602550739205297257940969992371799325870179746191672464736721424617639973324090288952006260483222894269928179970153634220390287255837625331668555933039199194619824375869291271098935000699785346405055160394688637074599519052655517388596327473273906029869030988064607361165803129718773877185415445291671089029845994683414682274353665003204293107284473196033588697845087556526514092678744031772226855409523354476737660407619436531080189837076164818131039104397776628128325247709678431023369197272126578394856752060591013812807437681624251867074769638052097737959472027002770963255207757153746376691827309573603635608169799503216990026029763868313819255248026666854405409059422844776556067163611304891154793770115766608153679099327786"),
          new BigInteger("119359756198641231858139651428439585561105914902686985078252796680474637856752833978884422594516170665312423393830118608408063594508087813277769835084746883589963798527237870817233369094387978405585759195339509768803496494994109693743279157584139079471178850751266233150727771094796709619646350222242437970473900636242584673413224137139139346254912172628651028694427789523683070264102332413084663100402635889283790741342401259356660761075766365672754329863241692760862540151023800163269173550320623249398630247531924855997863109776955214403044727497968354022277828136634059011708099779241302941071701051050378539485717425482151777277387633806111112178267035315726401285294598397677116389893642725498831127977915200359151833767358091365292230363248410124916825814514852703770457024102738694375502049388804979035628232209959549199366986471874840784466132903083308458356458177839111623113116525230200791649979270165318729763550486200224695556789081331596212761936863634467236301450039399776963661755684863012396788149479256016157814129329192490798309248914535389650594573156725696657302152874510063002532052622638033113978672254680147128450265983503193865576932419282003012093526302631221491418211528781074474515924597472841036553107847")
  );

  /* Changed python PR #387, 8/4/21; previous values:
    public static final BigInteger P = new BigInteger("1044388881413152506691752710716624382579964249047383780384233483283953907971553643537729993126875883902173634017777416360502926082946377942955704498542097614841825246773580689398386320439747911160897731551074903967243883427132918813748016269754522343505285898816777211761912392772914485521155521641049273446207578961939840619466145806859275053476560973295158703823395710210329314709715239251736552384080845836048778667318931418338422443891025911884723433084701207771901944593286624979917391350564662632723703007964229849154756196890615252286533089643184902706926081744149289517418249153634178342075381874131646013444796894582106870531535803666254579602632453103741452569793905551901541856173251385047414840392753585581909950158046256810542678368121278509960520957624737942914600310646609792665012858397381435755902851312071248102599442308951327039250818892493767423329663783709190716162023529669217300939783171415808233146823000766917789286154006042281423733706462905243774854543127239500245873582012663666430583862778167369547603016344242729592244544608279405999759391099775667746401633668308698186721172238255007962658564443858927634850415775348839052026675785694826386930175303143450046575460843879941791946313299322976993405829119");
    public static final BigInteger R = new BigInteger("9019518416950528558373478086511232658951474842525520401496114928154304263969655687927867442562559311457926593510757267649063628681241064260953609180947464800958467390949485096429653122916928704841547265126247408167856620024815508684472819746384115369148322548696439327979752948311712506113890045287907335656308945630141969472484100558565879585476547782717283106837945923693806973017510492730838409381014701258202694245760602718602550739205297257940969992371799325870179746191672464736721424617639973324090288952006260483222894269928179970153634220390287255837625331668555933039199194619824375869291271098935000699785346405055160394688637074599519052655517388596327473273906029869030988064607361165803129718773877185415445291671089029845994683414682274353665003204293107284473196033588697845087556526514092678744031772226855409523354476737660407619436531080189837076164818131039104397776628128325247709678431023369197272126578394856752060591013812807437681624251867074769638052097737959472027002770963255207757153746376691827309573603635608169799503216990026029763868313819311401747718758606328306442737694783044330450178447543246397189503997649637375210794");
    public static final BigInteger G = new BigInteger("14245109091294741386751154342323521003543059865261911603340669522218159898070093327838595045175067897363301047764229640327930333001123401070596314469603183633790452807428416775717923182949583875381833912370889874572112086966300498607364501764494811956017881198827400327403252039184448888877644781610594801053753235453382508543906993571248387749420874609737451803650021788641249940534081464232937193671929586747339353451021712752406225276255010281004857233043241332527821911604413582442915993833774890228705495787357234006932755876972632840760599399514028393542345035433135159511099877773857622699742816228063106927776147867040336649025152771036361273329385354927395836330206311072577683892664475070720408447257635606891920123791602538518516524873664205034698194561673019535564273204744076336022130453963648114321050173994259620611015189498335966173440411967562175734606706258335095991140827763942280037063180207172918769921712003400007923888084296685269233298371143630883011213745082207405479978418089917768242592557172834921185990876960527013386693909961093302289646193295725135238595082039133488721800071459503353417574248679728577942863659802016004283193163470835709405666994892499382890912238098413819320185166580019604608311466");
   */
  public static final ElectionConstants STANDARD_PREVIOUS = new ElectionConstants(
          new BigInteger("1044388881413152506691752710716624382579964249047383780384233483283953907971553643537729993126875883902173634017777416360502926082946377942955704498542097614841825246773580689398386320439747911160897731551074903967243883427132918813748016269754522343505285898816777211761912392772914485521155521641049273446207578961939840619466145806859275053476560973295158703823395710210329314709715239251736552384080845836048778667318931418338422443891025911884723433084701207771901944593286624979917391350564662632723703007964229849154756196890615252286533089643184902706926081744149289517418249153634178342075381874131646013444796894582106870531535803666254579602632453103741452569793905551901541856173251385047414840392753585581909950158046256810542678368121278509960520957624737942914600310646609792665012858397381435755902851312071248102599442308951327039250818892493767423329663783709190716162023529669217300939783171415808233146823000766917789286154006042281423733706462905243774854543127239500245873582012663666430583862778167369547603016344242729592244544608279405999759391099775667746401633668308698186721172238255007962658564443858927634850415775348839052026675785694826386930175303143450046575460843879941791946313299322976993405829119"),
          new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639747"),  //  pow(2, 256) - 189")
          new BigInteger("9019518416950528558373478086511232658951474842525520401496114928154304263969655687927867442562559311457926593510757267649063628681241064260953609180947464800958467390949485096429653122916928704841547265126247408167856620024815508684472819746384115369148322548696439327979752948311712506113890045287907335656308945630141969472484100558565879585476547782717283106837945923693806973017510492730838409381014701258202694245760602718602550739205297257940969992371799325870179746191672464736721424617639973324090288952006260483222894269928179970153634220390287255837625331668555933039199194619824375869291271098935000699785346405055160394688637074599519052655517388596327473273906029869030988064607361165803129718773877185415445291671089029845994683414682274353665003204293107284473196033588697845087556526514092678744031772226855409523354476737660407619436531080189837076164818131039104397776628128325247709678431023369197272126578394856752060591013812807437681624251867074769638052097737959472027002770963255207757153746376691827309573603635608169799503216990026029763868313819311401747718758606328306442737694783044330450178447543246397189503997649637375210794"),
          new BigInteger("14245109091294741386751154342323521003543059865261911603340669522218159898070093327838595045175067897363301047764229640327930333001123401070596314469603183633790452807428416775717923182949583875381833912370889874572112086966300498607364501764494811956017881198827400327403252039184448888877644781610594801053753235453382508543906993571248387749420874609737451803650021788641249940534081464232937193671929586747339353451021712752406225276255010281004857233043241332527821911604413582442915993833774890228705495787357234006932755876972632840760599399514028393542345035433135159511099877773857622699742816228063106927776147867040336649025152771036361273329385354927395836330206311072577683892664475070720408447257635606891920123791602538518516524873664205034698194561673019535564273204744076336022130453963648114321050173994259620611015189498335966173440411967562175734606706258335095991140827763942280037063180207172918769921712003400007923888084296685269233298371143630883011213745082207405479978418089917768242592557172834921185990876960527013386693909961093302289646193295725135238595082039133488721800071459503353417574248679728577942863659802016004283193163470835709405666994892499382890912238098413819320185166580019604608311466")
  );

  // TEST ONLY
  // These constants serve as sets of primes for future developers
  // Currently, all the sets are all valid but may break certain tests
  // As tests adapt, these constants can be used to speed up tests
  public static final ElectionConstants EXTRA_SMALL_TEST_CONSTANTS = new ElectionConstants(
          BigInteger.valueOf(157),
          BigInteger.valueOf(13),
          BigInteger.valueOf(12),
          BigInteger.valueOf(16));
  public static final ElectionConstants SMALL_TEST_CONSTANTS = new ElectionConstants(
          BigInteger.valueOf(503),
          BigInteger.valueOf(251),
          BigInteger.valueOf(2),
          BigInteger.valueOf(5));
  public static final ElectionConstants MEDIUM_TEST_CONSTANTS = new ElectionConstants(
          BigInteger.valueOf(65267),
          BigInteger.valueOf(32633),
          BigInteger.valueOf(2),
          BigInteger.valueOf(3));
  public static final ElectionConstants LARGE_TEST_CONSTANTS = new ElectionConstants(
          new BigInteger("18446744073704586917"),
          new BigInteger("65521"),
          new BigInteger("281539415968996"),
          new BigInteger("15463152587872997502"));

  //////////////////////////////////////////////////////////////////////////////////////////////

  /** large prime or P. */
  public final BigInteger large_prime;
  /** small prime or Q. */
  public final BigInteger small_prime;
  /** cofactor or R. */
  public final BigInteger cofactor;
  /** generator or G. */
  public final BigInteger generator;

  /** Q - 1, expect this to go away */
  private BigInteger qminus1;

  public ElectionConstants(BigInteger large_prime, BigInteger small_prime, BigInteger cofactor, BigInteger generator) {
    this.large_prime = Preconditions.checkNotNull(large_prime);
    this.small_prime = Preconditions.checkNotNull(small_prime);
    this.cofactor = Preconditions.checkNotNull(cofactor);
    this.generator = Preconditions.checkNotNull(generator);

    this.qminus1 = this.small_prime.subtract(BigInteger.ONE);
  }

  public BigInteger qminus1() {
    if (this.qminus1 == null) {
      this.qminus1 = this.small_prime.subtract(BigInteger.ONE);
    }
    return qminus1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ElectionConstants that = (ElectionConstants) o;
    return large_prime.equals(that.large_prime) &&
            small_prime.equals(that.small_prime) &&
            cofactor.equals(that.cofactor) &&
            generator.equals(that.generator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(large_prime, small_prime, cofactor, generator);
  }

  @Override
  public String toString() {
    return "ElectionConstants (" + getPrimeOptionType() +
            ")\n  large_prime= " + large_prime +
            "\n  small_prime= " + small_prime +
            "\n  cofactor= " + cofactor +
            "\n  generator= " + generator +
            "}";
  }

  public PrimeOption getPrimeOptionType() {
    if (this.equals(STANDARD_CONSTANTS)) {
      return PrimeOption.Standard;
    }
    if (this.equals(STANDARD_PREVIOUS)) {
      return PrimeOption.StandardPrevious;
    }
    if (this.equals(LARGE_TEST_CONSTANTS)) {
      return PrimeOption.LargeTest;
    }
    if (this.equals(MEDIUM_TEST_CONSTANTS)) {
      return PrimeOption.MediumTest;
    }
    if (this.equals(SMALL_TEST_CONSTANTS)) {
      return PrimeOption.SmallTest;
    }
    if (this.equals(EXTRA_SMALL_TEST_CONSTANTS)) {
      return PrimeOption.ExtraSmallTest;
    }
    return PrimeOption.Custom;
  }

}
