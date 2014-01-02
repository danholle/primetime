package com.danholle.primetime;

import java.text.*;

/**
 *
 *  Status of a single Mersenne prime test, including test state and various timestamps.
 *  <p>
 *  {@link quest} creates a test for every prime in sequence, passing them off to {@link qthread}
 *  instances which asynchronously carry out the testing function itself.  
 * 
 */
public class test {

  //
  //  The following 11 values are the ones that get written to the log file.
  // 

  int p;         // Mersenne exponent being checked, e.g. 19937.

  int state;     // Where are we with this test?  The options are
  static final int NOTSTARTED=0;   // Initial state; all we know is that p is prime.
  static final int QUALIFYING=1;   // Running the qualification test (looking for small factors)
  static final int QUALIFIED=2;    // We checked all 2pj+1 up to maxj and found no factors.
  static final int UNQUALIFIED=3;  // We found a factor 2pj+1.
  static final int LUCASING=4;     // Running the Lucas test.
  static final int LUCASP=5;       // Lucas test yielded PRIME!!
  static final int LUCASC=6;       // Lucas test yielded Composite  :(

  // Detail on search for factors of the form 2pj+1.
  // If state is NOTSTARTED,          this is 0.
  // If state is QUALIFYING,          this is the last j tried (2pj+1 is not a factor).
  // If state is UNQUALIFIED,         2p*fact+1 is a factor of 2**p-1.
  // If state is QUALIFIED or LUCAS*, this is the highest j tested (no factors found).
  int fact;

  // The maximum fact value we (plan to) check.  This is used to compute % finished.
  int maxfact;

  // These start,stop timestamps delimit the execution of the factor test for this p.
  // The timestamps are initially set to 0, and then are updated as appropriate as a
  // side-effect of test method invocations.
  long factms0;  // When we started factor test
  long factms1;  // Time we finished factor test (or time of last update)

  // Detail on Lucas test.
  // If state is LUCAS*, this is the last Lucas iteration completed (there are p iterations).
  // Otherwise, this is 0.
  int lucas;

  // These timestamps delimit execution of the Lucas test;  similar comments apply as with factor timestamps
  long lucasms0; // Time we started Lucas test
  long lucasms1; // Time we finished Lucas test (or time of last update)

  // Capture the compute environment of the test
  String cpu;   // type of CPU e.g. i7-3517U
  int threads;  // number of concurrent threads
  int verno;    // Version of the mersenne software the test thread is running


  /**
   *  Constructor, given prime exponent to test
   */
  test(int pp,String pcpu,int pthreads) {

    p=pp;
    state=NOTSTARTED;
    
    fact=0;
    maxfact=0;
    factms0=0;
    factms1=0;
    
    lucas=0;
    lucasms0=0;
    lucasms1=0;

    cpu=pcpu;
    threads=pthreads;
    verno=version.getBuild();
    
  } // constructor (p)



  /**
   *  Constructor, given test state from log file
   */
  test(String s) {

    String v[] = s.split(",");

    if (v.length!=12) 
      System.out.println("Bad test status string: "+s);
    else {
      p=Integer.parseInt(v[0]);
      state=Integer.parseInt(v[1]);
      
      fact=Integer.parseInt(v[2]);
      maxfact=Integer.parseInt(v[3]);
      factms0=Long.parseLong(v[4]);
      factms1=Long.parseLong(v[5]);
      
      lucas=Integer.parseInt(v[6]);
      lucasms0=Long.parseLong(v[7]);
      lucasms1=Long.parseLong(v[8]);
      
      cpu=v[9].trim();
      threads=Integer.parseInt(v[10]);
      verno=Integer.parseInt(v[11]);
    } // if string has the right number of fields

  } // constructor (String)



  /**
   *
   *  Report that the qualification test is running.
   *  <p>
   *  @param j We have tested up to 2pj+1 without finding a factor.
   *  @param maxj We intend to test j up to maxj.
   *
   */
  public void qualifying (int j,int maxj) {
    factms1=System.currentTimeMillis();
    if (factms0==0) factms0=factms1;
    
    fact=j;
    maxfact=maxj;
    state=QUALIFYING;
  } // qualifying



  /**
   *
   *  Report that we found a factor of this Mersenne candidate.
   *  <p>
   *  @param j 2pj+1 is a factor of 2**p-1.
   *
   */
  public void unqualified(int j) {
    factms1=System.currentTimeMillis();
    if (factms0==0) factms0=factms1;
    
    fact=j;
    state=UNQUALIFIED;
    quest.test2log(this);
  } // unqualified



  /**
   *
   *  Report that this p qualified for Lucas testing (we found no factors of 2**p-1).
   *  <p>
   *  @param j The highest value for which possible factor 2pj+1 was checked.
   *
   */
  public void qualified(int j) {
    factms1=System.currentTimeMillis();
    if (factms0==0) factms0=factms1;
    
    fact=j;
    state=QUALIFIED;
  } // qualified



  /**
   *
   *  Report that the Lucas test is running.
   *  <p>
   *  @param j Last completed iteration number (there are p iteractions).
   *
   */
  public void lucasing (int j) {
    lucasms1=System.currentTimeMillis();
    if (lucasms0==0) lucasms0=lucasms1;
    
    lucas=j;
    state=LUCASING;
  } // qualifying



  /**
   *
   *  Report that Lucas found this number to be composite.
   *
   */
  public void lucasc() {
    lucasms1=System.currentTimeMillis();
    if (lucasms0==0) lucasms0=lucasms1;
    
    state=LUCASC;
    quest.test2log(this);
  } // lucasc



  /**
   *
   *  Report that Lucas found this number to be prime.
   *
   */
  public void lucasp() {
    lucasms1=System.currentTimeMillis();
    if (lucasms0==0) lucasms0=lucasms1;

    // Tell the world.
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    System.out.println(" "+sdf.format(lucasms1)+"  2**"+p+" - 1 is prime.");
    
    state=LUCASP;
    quest.test2log(this);
  } // lucasp



  /** 
   *
   *  How much time have we spent looking for factors?
   *
   *  @return Time in milliseconds
   */
  public long getfactms() {return factms1-factms0;}



  /** 
   *
   *  How much time have we spent on the Lucas test??
   *
   *  @return Time in milliseconds
   */
  public long getlucasms() {return lucasms1-lucasms0;}



  /**
   *
   *  When was the first activity on this test?
   *
   *  @return Timestamp.
   *
   */
  public long getfirst() {return factms0;}



  /**
   *
   *  Return the factor information.
   *
   *  @return 0 if no factor found, or j if 2pj+1 is a factor.
   *
   */
  public int getfact() {return fact;}



  /**
   *
   *  When was the last activity on this test?
   *
   *  @return Timestamp.
   *
   */
  public long getlast() {
    long ans = 0L;
    if (factms1>ans)  ans=factms1;
    if (lucasms1>ans) ans=lucasms1;
    return ans;
  } // getlast



  /**
   *
   *  What p are we testing?
   *
   *  @return p (we are testing 2**p-1 for primality)
   *
   */
  public int getp() {return p;}



  /**
   *
   *  What state is this test in?
   *
   *  @return Either test.NOTSTARTED, QUALIFYING, QUALIFIED, UNQUALIFIED, LUCASING, LUCASP, or LUCASC
   *
   */
  public int getstate() {return state;}



  /**
   *
   *  Convert this test status to a CSV String.
   *  <p>
   *  This is typically used to write test status to a file.
   *
   */
  public String toString() {
    return ""+p+","+state+","+fact+","+maxfact+","+factms0+","+factms1+","+lucas+","
      +lucasms0+","+lucasms1+","+cpu+","+threads+","+verno;
  } // toString



} // test
