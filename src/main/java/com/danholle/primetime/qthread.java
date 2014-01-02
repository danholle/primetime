package com.danholle.primetime;

/**
 *
 *  A background thread, started by {@link quest}, that searches incessantly for Mersenne primes.
 *
 */
public class qthread implements Runnable {


  public void run() {

    while (true) {
      test t=quest.nexttest();
      
      int p=t.getp();

      // We check factors of the form 2pj+1.  How high should we check?
      int topj=(int)(1.0*p+200); 
      
      // If p qualifies, do the Lucas test.
      if (putils.qualify(t,topj)<0) putils.isMersennePrime(t);
    } // while the world is still spinning

  } // run


} // qthread
