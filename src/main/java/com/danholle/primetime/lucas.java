package com.danholle.primetime;

import java.math.*;
import java.io.*;

/**
 *
 *  Lucas test using fourier.
 *  
 */
public class lucas {

  private test t;
  int p;
   
  //
  // Parts to the "multiplier" we build
  //
  long[] a;        // Number we are squaring.  
  int bits;        // "a" is a big binary number with this many bits per word
  int radix;       // 2**bits.  "a" is a radix-based number
  fourier f;       // Fourier transform engine
  double[] ar,ai;  // half-complex version of a
  double[] sr,si;  // transformed a / transformed square
  long mask;       // used for carry propagate.  111...1, "bits" long.
  int llen;        // # of words in "a". bits*llen>2*p  
  int clen;        // Length of complex version = llen/2
  int wlen;        // Entry in "a" where we start wrapping
  int wbits;       // # bits in that word that don't wrap
  long wmask;      // mask for those bits, i.e. wbits 1's
  
  // Times for various things
  long nsrealfft;
  long nscarry;
  long nssquare;
  long nsall;
  long nsprimality;
  long nsfft;
  int words;

  /**
   *
   *  Constructor.
   *  Build a Lucas test engine around a primality test.
   *
   */
  lucas(test tt) {t=tt;}


  /**
   *
   *  Carry out a Lucas-Lehmer test for primality of M = 2**p - 1.
   *  <p>
   *  While the test is computationally complex, the statement of the test is incredibly simple:
   *  <table style="white-space: nowrap; width: 0%; background-color: lightblue; font-size: 150%; margin: 20px; padding: 20px">
   *    <tr>
   *      <td>Define the sequence:</td>
   *    </tr>
   *    <tr>
   *      <td style="padding-left: 30px">a<sub>0</sub>&nbsp;=&nbsp;4</td>
   *    </tr>
   *    <tr>
   *      <td style="padding-left: 30px">a<sub>n+1</sub>&nbsp;=&nbsp;a<sub>n</sub><sup>2</sup>&nbsp;-&nbsp;2</td>
   *    </tr>
   *    <tr>
   *      <td style="padding-top: 20px">Then M<sub>p</sub>&nbsp;=&nbsp;2<sup>p</sup>&nbsp;-&nbsp;1&nbsp; is prime iff</td>
   *    </tr>
   *    <tr>
   *      <td style="padding-left: 30px">a<sub>p-2</sub>&nbsp;=&nbsp;0&nbsp;mod M<sub>p</sub></td>
   *    </tr>
   *  </table>
   *  <b>Implementation:</b>
   *  <ul>
   *    <li>Create an optimal FFT multiplier for squaring p-bit numbers.  For large p, we can
   *      afford to spend a lot of time on this because of the large payback.
   *    <li>Run through the p-2 iterations, periodically reporting 
   *      progress in the test object.  In due course, we may periodically also
   *      preserve computation state to facilitate restart.
   *    <li>If the final result is 0, M is prime;  else composite.
   *  </ul>
   *
   */
  public boolean isMersennePrime() {
    long nsstart=System.nanoTime();
    nsfft=0;
    nsrealfft=0;
    nscarry=0;
    nssquare=0;
    nsall=0;
    nsprimality=0;

    // Tell the world we are doing a Lucas test
    t.lucasing(0);
    
    p=t.getp();

    // The more bits per word we can use, the faster we go.
    // Raise bitcount until we fail;  then back down by 1 bit.
    double worsterr=1e20;
    for (int i=10;i<32;i++) {
      bits=i;
      multmake();
      for (int j=0;j<wlen;j++) a[j]=mask;
      a[wlen]=wmask;
      square();
      carry();
      boolean crap=false;
      for (int j=0;j<=wlen;j++) 
        if (crap=(a[j]!=0)) {
          //String ss="Residue: ";
          //for (int k=0;k<5;k++) ss+=" "+a[k];
          //ss+=" ...";
          //for (int k=wlen-3;k<=wlen;k++) ss+=" "+a[k];
          //System.out.println(ss);
          break;
        }
      if (crap) {
        bits--;
        multmake();
        break;
      } // square failed!
      else { // square worked
        worsterr=-1e20;
        for (int j=0;j<ar.length;j++) {
          double v=ar[j]/clen;
          double err=v-(long)(0.5+v);
          if (err<0.0) err=-err;
          if (err>worsterr) worsterr=err;
          v=ai[j]/clen;
          err=v-(long)(0.5+v);
          if (err<0.0) err=-err;
          if (err>worsterr) worsterr=err;
        } // for all 
      } // square worked
    } // for progressively more optimistic word lengths
    //System.out.println("Bits per word:  "+bits);
    System.out.println("Worst error "+worsterr);


    //
    // The actual primality test starts here!
    //
    long nsprimalitystart=System.nanoTime();
    long nsfftstart=f.nsfft;

    // Start with a=4
    a[0]=4;
    for (int i=1;i<llen;i++) a[i]=0;

    // Compute a*a-2 mod M ... p-2 times
    int timebomb=0;
    for (int i=1;i<p-1;i++) {
      if (timebomb--<0) {
         //t.lucasing(i);  // report progress
         timebomb=1000;
      }
      square();
      a[0]-=2;
      //int pos=0;
      //while (a[pos]<0) {
      //  a[pos]+=radix;
      //  pos++;
      //  if (pos>=a.length) System.out.println("tilt "+i);
      //  a[pos]--;
      //} // while propagating
      carry();
    } // for each iteration of Lucas test 
    
    boolean isprime=true;
    //String ss="Residue: ";
    //for (int i=0;i<5;i++) ss+=" "+a[i];
    //ss+=" ...";
    //for (int i=wlen-3;i<=wlen;i++) ss+=" "+a[i];
    //System.out.println(ss);

    for (int i=0;i<=wlen;i++) 
      if (a[i]!=0) { // a is not 0
        //System.out.println("wlen="+wlen+"; a["+i+"]="+a[i]);
        isprime=false; 
        break;
      } // a is not 0

    nsprimality=System.nanoTime()-nsprimalitystart;
    nsfft=f.nsfft-nsfftstart;

    if (isprime) t.lucasp();
    else t.lucasc();

    nsall=System.nanoTime()-nsstart;

    return isprime;

  } // isMersennePrime


  /**
   * 
   *  Generate a multiplier.
   *  <p>
   *  For performance reasons, we don't want the multiply operation
   *  to allocate any objects or recompute anything.  So we push
   *  all this work into multmake, setting up a multiplier suitable
   *  to square p bit numbers stored with "bits" bits per word.
   *
   */
  public void multmake() {
    radix=1;
    for (int i=0;i<bits;i++) radix+=radix;
    mask=radix-1;

    clen=fourier.roundup((p+bits-1)/bits);
    llen=clen+clen;
    words=llen;

    wbits=p%bits;
    wlen=p/bits;
    wmask=1;
    for (int i=0;i<wbits;i++)wmask+=wmask;
    wmask--;

    f=new fourier(clen/2);
    a=new long[llen];
    ar=new double[clen];
    ai=new double[clen];
    sr=new double[clen];
    si=new double[clen];
  } // multmake


  /**
   *
   *  Test a multiplier.
   *  <p>
   *  We verify proper operation of a multiplier set up by multmake.
   *  <p>
   *  <b>Usage pattern:</b>  multtest is used to figure out how many
   *  bits we can store per word, so we get the fastest possible 
   *  multiply for a given p.
   *  <p>  
   *  <b>Implementation:</b>  We test the multiply operation
   *  by computing the square a test value of "a" and verifying that
   *  (a*a mod k) = ((a mod k)*(a mod k) mod k).  We do this with multiple k's.
   *
   
  public boolean multtest() {
    long m0=19937;
    long m1=21701;
    long rm0=radix%m0;
    long rm1=radix%m1;
    long cn0=1;
    long cn1=1;
    long xm0=0;
    long xm1=0;

    int flen=(p+bits-1)/bits;
    for (int i=0;i<llen;i++) {
      if (i<flen) {
        a[i]=(long)(radix*Math.random());
        xm0=(xm0+cn0*a[i])%m0;
        xm1=(xm1+cn1*a[i])%m1;
        cn0=(cn0*rm0)%m0;
        cn1=(cn1*rm1)%m1;
      } //
      else a[i]=0;
    } // for every word in the test value
    
    // Square a in place
    square();

    // See if the square is right via modular arithmetic
    cn0=1;
    cn1=1;
    long zm0=0;
    long zm1=0;
    for (int i=0;i<llen;i++) {
      zm0=(zm0+cn0*a[i])%m0;
      zm1=(zm1+cn1*a[i])%m1;
      cn0=(cn0*rm0)%m0;
      cn1=(cn1*rm1)%m1;
    }

    // Expected answers
    long zm0e=(xm0*xm0)%m0;
    long zm1e=(xm1*xm1)%m1;

    // If unexpected result, multiplier failed
    if (zm0e!=zm0) return false;
    if (zm1e!=zm1) return false;

    return true;

  } // multtest
  */
  
  /**
   *
   *   Square "a" in place.
   *   <p>
   *   "a" contains a p-bit binary value we want to square, with "bits" bits per word.
   *   <p>
   *   <b>Implementation:</b>
   *   <ol>
   *     <li>Convert p-bit "a" to half-complex &lt;ar,ai&gt;
   *     <li>Real FFT: &lt;ar,ai&gt; --&gt; &lt;sr,si&gt;
   *     <li>Convolved square of &lt;sr,si&gt; in place
   *     <li>Inverse Real FFT: &lt;sr,si&gt; --&gt; &lt;ar,ai&gt;
   *     <li>Convert &lt;ar,ai&gt; into "a" (dirty)
   *   </ol>
   *
   */
  public void square() {
    long nssquarestart=System.nanoTime();

    // Put a into <ar,ai> as packed half-complex        
    for (int i=0;i<clen;i++) {
      ar[i]=a[2*i];
      ai[i]=a[2*i+1];
    }
    
    // Do real FFT
    long nsrealfftstart=System.nanoTime();
    f.realfft(ar,ai,sr,si,1);
    nsrealfft+=System.nanoTime()-nsrealfftstart;

    // square half-complex transformed thingy
    sr[0]*=sr[0];
    si[0]*=si[0];
    for (int i=1;i<clen;i++) {
      double re=sr[i];
      double im=si[i];
      sr[i]=(re+im)*(re-im);
      si[i]=2*re*im;
    } // for most complex entries

    // Unravel the squared result
    nsrealfftstart=System.nanoTime();
    f.realfft(sr,si,ar,ai,-1);
    nsrealfft+=System.nanoTime()-nsrealfftstart;
    double rn=1.0D/(double)clen;
    for (int i=0;i<clen;i++) {
      //a[2*i]  =Math.round(rn*ar[i]);
      //a[2*i+1]=Math.round(rn*ai[i]);
      a[2*i]  =(long)(0.5D+rn*ar[i]);
      a[2*i+1]=(long)(0.5D+rn*ai[i]);
    } // for each half complex item  

    nssquare+=System.nanoTime()-nssquarestart;
  } // square


  /**
   *
   *  Carry propagate on "a".
   *  <p>
   *  The value of "a" we get out from a Fourier multiply
   *  is a mess:  all llen words may be full.  We want to get 
   *  "a" back to a p-bit number with each word < radix.
   *  <p>
   *  <b>Implementation:</b>
   *  We do 1.5 passes over "a" as follows:
   *  <ul>
   *    <li>0 to wlen-1:  carry propagate
   *    <li>wlen:  Split point
   *    <li>wlen+1 to llen-1: carry and wrap
   *    <li>0 to wlen-1:  carry propagate
   *    <li>wlen:  wrap
   *  <ul>
   *
   */
  public void carry() {
    long nscarrystart=System.nanoTime();

    // In lower p bits of "a", just carry propagate.
    long v,c;
    int wslide,i,top;

    wslide=bits-wbits;
    i=wlen+wlen;
    while (i>wlen) {
      v=a[i];
      a[i-wlen]  +=v>>wbits;
      a[i-wlen-1]+=(v&wmask)<<wslide;
      a[i]=0;
      i--;
    } // while counting down

    v=a[wlen];
    a[wlen]=v&wmask;
    a[0]+=v>>wbits;

    c=0;
    for (i=0;i<wlen;i++) {
      v=a[i]+c;
      a[i]=mask&v;
      c=v>>bits;
    } // carry prop all words

    v=a[wlen]+c;
    a[wlen]=v&wmask;
    a[0]+=v>>wbits;
    i=0;
    while ((v=a[i])>=radix) {
      a[i]=v&mask;
      i++;
      a[i]+=v>>bits;
    }
    
    // It may be that the result is exactly 2**p-1.  
    // In which case, we make it 0.
    if ((a[0]==mask)&&(a[wlen]==wmask)) {
      boolean iszero=true;
      for (i=1;i<wlen;i++) if (a[i]!=mask) {iszero=false;break;}
      if (iszero) for (i=0;i<=wlen;i++) a[i]=0;
    } // if it looks like "a" may be 0

    nscarry+=System.nanoTime()-nscarrystart;
  } // carry


  /**
   *
   *  A simple test of the carry process
   *
   */
  public void carrytest() {
    bits=4;
    radix=16;
    mask=radix-1;
    llen=8;
    p=11;
    wlen=2;
    wbits=3;
    wmask=7;

    System.out.println("Testing split word wrap.");
    a=new long[] {1,2,41,0,0,0,0,0};
    carry();
    System.out.println(" -- Expect:   6 2 1 0 0 0 0 0");
    String s=" -- Actual:  ";
    for (int i=0;i<8;i++) s+=" "+a[i];
    System.out.println(s);

    System.out.println("Split word + higher word wrap.");
    a=new long[] {1,2,9,2,0,0,0,0};
    carry();
    System.out.println(" -- Expect:   6 2 1 0 0 0 0 0");
    s=" -- Actual:  ";
    for (int i=0;i<8;i++) s+=" "+a[i];
    System.out.println(s);

    System.out.println("Carry propagate.");
    a=new long[] {1,18,2,0,0,0,0,0};
    carry();
    System.out.println(" -- Expect:   1 2 3 0 0 0 0 0");
    s=" -- Actual:  ";
    for (int i=0;i<8;i++) s+=" "+a[i];
    System.out.println(s);

    System.out.println("1-8 test");
    a=new long[] {1,2,3,4,5,6,7,8};
    carry();
    System.out.println(" -- Expect:   7 14 1 0 0 0 0 0");
    s=" -- Actual:  ";
    for (int i=0;i<8;i++) s+=" "+a[i];
    System.out.println(s);

    System.out.println("8-1 test");
    a=new long[] {8,7,6,5,4,3,2,1};
    // index            7    6    5    4    3    2    1    0
    // original a:   0001 0010 0011 0100 0101 0110 0111 1000
    // a%2**11:                                110 0111 1000
    // a/2**11:                   0 0010 0100 0110 1000 1010
    // new a:                     0 0010 0100 1101 0000 0010
    // %                                       101 0000 0010
    // /                                        00 0100 1001
    // new a:                                  101 0100 1011
    carry();
    System.out.println(" -- Expect:   11 4 5 0 0 0 0 0");
    s=" -- Actual:  ";
    for (int i=0;i<8;i++) s+=" "+a[i];
    System.out.println(s);

    System.out.println("8-1 test carried");
    a=new long[] {24,22,21,20,19,18,17,0};
    carry();
    System.out.println(" -- Expect:   11 4 5 0 0 0 0 0");
    s=" -- Actual:  ";
    for (int i=0;i<8;i++) s+=" "+a[i];
    System.out.println(s);

  } // carrytest

} // lucas

