package com.danholle.primetime;

import java.math.BigInteger;



/**
 *
 *  Prime number utilities:  isPrime, isQualified, isMersennePrime, etc.
 *  <p>
 *  These routines are pure arithmetic;  they don't have visibility into the
 *  broader computational context.
 *
 */
public class putils {



  /**
   *
   *  Is this 32 bit integer a prime number?
   *  <p>
   *  A moderately efficient, albeit brute-force, primality check.
   *
   *  @param p A 32 bit number.  
   *  @return True if prime, false if not.
   *
   */
  public static boolean isPrime(int p) {
    if (p==2)   return true;
    if (p==3)   return true;
    if (p==5)   return true;
    if (p<=1)   return false;
    if (p%2==0) return false;
    if (p%3==0) return false;
    if (p%5==0) return false;

    int to = (int)Math.sqrt(p);
    for(int i=7;i<=to;i+=30) {
      if (p%i     ==0) return false;
      if (p%(i+4) ==0) return false;
      if (p%(i+6) ==0) return false;
      if (p%(i+10)==0) return false;
      if (p%(i+12)==0) return false;
      if (p%(i+16)==0) return false;
      if (p%(i+22)==0) return false;
      if (p%(i+24)==0) return false;
    } // for factor candidates

    return true;
  } // isPrime


 
  /**
   *
   *  Is this 64 bit integer a prime number?
   *  <p>
   *  A moderately efficient, albeit brute-force, primality check.
   *
   *  @param p A 64 bit number.  
   *  @return True if prime, false if not.
   *
   */
  public static boolean isPrime(long p) {
    if (p==2)   return true;
    if (p==3)   return true;
    if (p==5)   return true;
    if (p<=1)   return false;
    if (p%2==0) return false;
    if (p%3==0) return false;
    if (p%5==0) return false;

    int to = (int)Math.sqrt(p);
    for(int i=7;i<=to;i+=30) {
      if (p%i     ==0) return false;
      if (p%(i+4) ==0) return false;
      if (p%(i+6) ==0) return false;
      if (p%(i+10)==0) return false;
      if (p%(i+12)==0) return false;
      if (p%(i+16)==0) return false;
      if (p%(i+22)==0) return false;
      if (p%(i+24)==0) return false;
    } // for factor candidates

    return true;
  } // isPrime


 
  /**
   *
   *  Lucas test for primality.
   *  <p>
   *  If the number is small, we use BigInteger arithmetic to do it directly.
   *  If not, we use the FFT arithmetic magic found in {@link lucdwt}.
   *  <p>
   *  As we proceed, we update the test status from time to time. 
   *
   *  @param t A test of a qualified Mersenne candidate.  
   *  @return True if prime, false if not.
   *
   */
  public static boolean isMersennePrime(test t) {
    t.lucasing(0);
    
    int p = t.getp();
    if (p==2) {
      t.lucasp();
      return true;
    } // 2**2-1 is prime!
    
    if (p<50) { // Use BigInteger to do Lucas test
      BigInteger mp=BigInteger.ONE.shiftLeft(p).subtract(BigInteger.ONE);
      BigInteger s=BigInteger.valueOf(4L);
      for (int i=3;i<=p;i++) s=s.multiply(s).subtract(BigInteger.valueOf(2L)).mod(mp);

      if (s.equals(BigInteger.ZERO)) {
        t.lucasp();
        return true;
      } // 2**p-1 is prime
      else {
        t.lucasc();
        return false;
      } // 2**p-1 is composite
    } // Use BigInteger

    // Otherwise, use FFT magic to do Lucas test
    lucdwt lucas=new lucdwt(t);
    return lucas.isMersennePrime();
  
  } // isMersennePrime
  
  
  public static void main(String[] args) {
    int p=Integer.parseInt(args[0]);
    test t=new test(p, " ", 1);
    String ans="not ";
    long starttime=System.currentTimeMillis();
    if (isMersennePrime(t)) ans="";
    long elapsed=System.currentTimeMillis()-starttime;
    System.out.println("It took "+elapsed+" milliseconds to determine that 2^"+p
      +" - 1 is "+ans+"prime.");
  } // mail


  /**
   *
   *  Look for small factors of 2**p-1, to avoid the expensive Lucas test.
   *  <p>
   *  All factors of 2**p-1 are primes of the form f=2pj+1 where f=+/-1 mod 8; we eliminate obvious
   *  candidates for p by checking j up to a particular limit.
   *  <p>
   *  As we proceed, we update the test status from time to time. 
   *
   *  @param t A test of a Mersenne candidate, destined for a Lucas test if we let it through.
   *  @param maxj The largest j we test.
   *  @return -j if we tested up to 2pj+1 without finding a factor; +j if 2pj+1 is a factor.
   *
   */
  public static int qualify(test t, int maxj) {
    t.qualifying(0,maxj);

    int p=t.getp();

    // Skip this test for small p.
    if (p>30) {
        
      BigInteger pow[] = new BigInteger[30];
      int ex[] = new int[30];
      
      // Table of exponents x, and powers 2**x 
      ex[0]=1;  pow[0]=BigInteger.valueOf(2L);
      ex[1]=2;  pow[1]=BigInteger.valueOf(4L);
      ex[2]=4;  pow[2]=BigInteger.valueOf(16L);
      ex[3]=8;  pow[3]=BigInteger.valueOf(256L);
      int hard=3; // marks part of the table that is hard wired

      for (int j=1;j<=maxj;j++) {

        // Factors are of the form 2pj+1. 
        long factl=p+p;
        factl=factl*j+1;

        // Possible factors must be prime, and be +1 or -1 mod 8.
        long mod8=factl%8;
        if (mod8==7) mod8=1;
        if ((mod8==1)&&isPrime(factl)) {
          BigInteger factbi=BigInteger.valueOf(factl);

          // Compute all powers of 2 we need to get to 2**p
          int lev=hard;
          BigInteger last = pow[lev];
          while (2*ex[lev]<p) {
            ex[lev+1]=2*ex[lev];
            pow[lev+1]=pow[lev].multiply(pow[lev]).mod(factbi);
            lev++;
          } // while 

          int rem = p;
          BigInteger prod=BigInteger.ONE;
          while (rem>0) {
            if (rem>=ex[lev]) {
              rem-=ex[lev];
              prod=prod.multiply(pow[lev]).mod(factbi);
            } // if
            lev--;
          } // while 
      
          if (prod.equals(BigInteger.ONE)) {
            t.unqualified(j);
            return j;
          } // 2pj+1 is a factor
        } // if factl is prime and +/-1 mod 8
 
        // Report that we're still running, so we can display compute time & progress
        if ((j<maxj)&&((j%1000)==0)) t.qualifying(j,maxj);

      } // for all j from 1 to maxj

    } // if p is big enough to worry about 
    
    return -maxj;

  } // qualify



} // putils
