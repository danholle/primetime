package com.danholle.primetime;

import java.util.*;
import java.text.*;

/**
 *
 *  Try out old and new Lucas-Lehmer test against user-provided p.
 *  
 */
public class trylucas {

  static int p;         
  static lucdwt luc0;    
  static lucas  luc1; 


  /**
   *  Command line tool
   *
   */
  public static void main (String[] args) {

    p=-1;
    if (args.length==1) try {p=Integer.parseInt(args[0].trim());} catch (Exception e) {p=-1;}
    if (p<0) {
      System.out.println("You loser!  Here's an example of how a non-loser might use trylucas:");
      System.out.println(" ");
      System.out.println("    java com.danholle.mersenne.trylucas 19937");
      System.out.println(" ");
      System.out.println("This runs the Lucas test against 2**19937 - 1 with both algorithms.");
      System.exit(1);
    }

    test t=new test(p,"trylucas",1);
    luc0=new lucdwt(t);
    luc1=new lucas(t);

    long b4=System.nanoTime();
    boolean prime=luc0.isMersennePrime();
    long ns=System.nanoTime()-b4;
    System.out.println(" ");
    System.out.println("lucdwt test took "+((ns+500000)/1000000)+" milliseconds.");
    System.out.println("  Result:  2**"+p+" - 1 is "+(prime?"prime.":"composite."));
    System.out.println("  Operand is "+luc0.words+" words.");
    System.out.println("  fft:                "+((luc0.nsfft+500000)/1000000)+" milliseconds.");
    System.out.println(" ");

    b4=System.nanoTime();
    prime=luc1.isMersennePrime();
    ns=System.nanoTime()-b4;
    System.out.println(" ");
    System.out.println("fourier test took "+((ns+500000)/1000000)+" milliseconds.");
    System.out.println("  Result:  2**"+p+" - 1 is "+(prime?"prime.":"composite."));
    System.out.println("  Operand is "+luc1.words+" words.");
    System.out.println("  Overall test:       "+((luc1.nsall+500000)/1000000)+" milliseconds.");
    System.out.println("  Lucas-Lehmer test:  "+((luc1.nsprimality+500000)/1000000));
    System.out.println("  square:             "+((luc1.nssquare+500000)/1000000));
    System.out.println("  carry:              "+((luc1.nscarry+500000)/1000000));
    System.out.println("  realfft:            "+((luc1.nsrealfft+500000)/1000000));
    System.out.println("  fft:                "+((luc1.nsfft+500000)/1000000));
    System.out.println(" ");



    // Run the carry test built into lucas
    //luc1.carrytest();


  } // main
 

} // trylucas
