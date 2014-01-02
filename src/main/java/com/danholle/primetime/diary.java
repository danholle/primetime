package com.danholle.primetime;

import java.io.*;

/**
 *  
 *  Information on all primality tests, historic and current.
 *
 */
public class diary {

  test[] tests;  // all tests, sorted by p
  int testcount;  // number of live entries in tests

  
  diary() { // constructor
    tests=new test[100];
    testcount=0;
  } // constructor


  /**
   *  Add a test to the list.
   *  Preserves order.  If duplicate p, replace older test.  Expand list if required.
   */
  public void add(test t) {
    if (testcount+1>tests.length) {
      test[] ts=new test[100+tests.length];
      for (int i=0;i<testcount;i++) ts[i]=tests[i];
      tests=ts;
    } // if we need to expand
    
    // Determine insert point
    int ip=testcount;
    while ((ip>0)&&(tests[ip-1].p>t.p)) ip--;

    // Either we're replacing the entry at ip-1,
    // or were inserting at ip and moving up the
    // guys currently at ip..testcount-1.
    if ((ip>0)&&(tests[ip-1].p==t.p)) tests[ip-1]=t;
    else {
      for (int i=testcount-1;i>=ip;i--) tests[i+1]=tests[i];
      tests[ip]=t;
      testcount++;
    } 
    
  } // add


  /**
   *  Retrieve test count.
   */
  public int count() {return testcount;}


  /**
   *  Retrieve test #n.
   */
  public test get(int n) {return tests[n];}


  /**
   *  Show the last few test states (for debugging)
   */
  public void tail(int n) {
    if (testcount>0) {
      int ini=testcount-n;
      if (ini<0) ini=0;
      for (int i=ini;i<testcount;i++) System.out.println("Test#"+i+": p="+tests[i].p+", state="+tests[i].state);
    }
  } // tail


} // diary
