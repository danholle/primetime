package com.danholle.primetime;

//import com.danholle.core.util;
import java.io.*;
import java.text.*;
import java.util.*;
import java.lang.*;
import org.apache.commons.io.*;

/**
 *  
 *  A quest to find Mersenne primes.
 *  <p>
 *  At any point there is a single quest running that knows the global state of
 *  the search for Mersenne primes.  In other words, quest is the right place for 
 *  common data and services that relate to the search as a whole.  There are 
 *  static, synchronized classes to update the state of the quest, and to query 
 *  progress for reporting.
 *  <p>
 *  The actual compute-intensive work is not here;  quest spawns some number
 *  of autonomous compute threads ({@link qthread}) each of which asks quest 
 *  for p values needing checking. quest can see the current state of each test.
 *  <p>
 *  Although quest primarily runs in a web environment, it's designed to be run
 *  either there or from the command line.
 *  <p>
 *  The central global data structure here is <i>tests,</i> which contains
 *  {@link test} results for every prime, in sequence.  Whenever we report
 *  status of the quest, we do so from this structure. 
 *
 *  TODO maybe should not allow syncr methods to call each other?
 */
public class quest {

  // Tests for every prime starting with 2, in sorted order.
  // These are present for completed tests, and tests in progress
  static diary d;

  // Context for this computation, nominally pulled in
  // from environment
  static String logfn; // Log file where we store diary.  (MERSENNE_LOG)
  static String cpu;  // Name of processor, e.g. i7-3517U.  (MERSENNE_CPU)
  static int threads; // Number of threads we use on this node. (MERSENNE_THREADS)

  // Context for determining the next p to test.
  static int born;  // any incomplete tests below this point should be resurrected
  static int lastp; // last p we initiated (or -1 if working backlog)

  // Compute threads that search for primes
  static qthread[] qthreads=null;

  // History lesson: Mersenne primes & year they were found
  static int ps[] = {
      13, 17, 19, 31, 61, 89, 107, 127, 521, 607, 
      1279, 2203, 2281, 3217, 4253, 4423, 9689, 9941, 11213, 19937, 
      21701, 23209, 44497, 86243, 110503, 132049, 216091, 756839, 859433, 1257787, 
      1398269, 2976221, 3021377, 6972593, 13466917
  };
  static String found[] = {
      "1456", "1588", "1588", "1772", "1883", "1911", "1914", "1876", "1952", "1952", 
      "1952", "1952", "1952", "1957", "1961", "1961", "1963", "1963", "1963", "1971", 
      "(1975)", "1979", "1979", "1982", "1988", "1983", "1985", "1992", "1994", "1996", 
      "1996", "1997", "1998", "1999", "2001"
  };


  static final DecimalFormat x3=new DecimalFormat("#0.000");
  static final DecimalFormat x2=new DecimalFormat("#0.00");

  /**
   *
   *  Constructor for quest.
   *  <p>
   *  blah blah blah.
   *
   *  @param fn File name of the log file we write to (and recover from).
   *
   */
  quest() {
    
    // CPU we're running on
    cpu="Unknown";
    String envcpu=System.getenv("MERSENNE_CPU");
    if (envcpu!=null) cpu=envcpu.trim();
 
    // Number of threads we run on
    threads=2;
    int availproc=Runtime.getRuntime().availableProcessors();
    if (availproc>0) threads=availproc;
    String envthreads=System.getenv("MERSENNE_THREADS");
    if (envthreads!=null) 
      try {threads=Integer.parseInt(envthreads);} 
      catch (Exception e) {System.out.println("Invalid MERSENNE_THREADS setting:  "+envthreads);}

    // Log file
    logfn="/home/danholle/quest.log";
    String envlog=System.getenv("MERSENNE_LOG");
    if (envlog!=null) logfn=envlog.trim();


    // A list of all Mersenne primes found.
    d=new diary();
    
    // Read through previous test results 
    // which we've captured in the log file.
    try {
      // Open the log file, which contains results for previous quests
      BufferedReader br=new BufferedReader(new FileReader(logfn));

      String rec=null;
      while ((rec=br.readLine())!=null) {
        rec=rec.trim();
        if (rec.length()>0 && rec.indexOf("*")!=0) d.add(new test(rec));
      } // while reading log records

      br.close();
      br = null;

    } // try reading the log
    catch(Exception e) { }

    born=d.count();
    lastp=1;
    if (born>0) { // we are resuming
      // d.tail(5);
      lastp=d.tests[born-1].p; // pick up here after incomplete guys finished
    } // we are resuming

    // Hang out for a few seconds so our computations
    // don't get screwed up with web server startup
    try {Thread.sleep(10000);} catch (Exception e) {;}

    // Fire up some compute threads 
    qthreads=new qthread[threads];
    for (int i=0;i<threads;i++) {
      qthreads[i]=new qthread();
      Thread t=new Thread(qthreads[i]);
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    } // for each qthread 

  } // quest



  /**
   *
   *  Generate a new candidate p for Mersenne testing.
   *  There are 2 states we may be in:
   *  <ol>
   *   <li>The normal state:  lastp is the last number we handed out.  Find the 
   *     next prime, and hand that out.
   *   <li>Catch-up state:  Diary entries &lt; born have been read in.
   *     Scan these for any incomplete tests, and re-drive them
   *  </ol>
   *
   */
  public static synchronized test nexttest() {
    int newp=0;
    if (born>0) { // catching up
      while ((born>0)&&(newp==0)) {
        born--;
        test t=d.tests[born];
        if ((t.state!=test.UNQUALIFIED)
          &&(t.state!=test.LUCASP)
          &&(t.state!=test.LUCASC)) // not finished
          newp=t.p;
      } // for all read-in diary entries
      if (newp>0) System.out.println("Resuming test:  p="+newp);
    } // if catching up

    if (newp==0) { // normal:  find next prime
      lastp++;
      while (!putils.isPrime(lastp)) lastp++;
      newp=lastp;
    } // normal:  find next prime

    test t=new test(newp,cpu,threads);
    test2log(t);
    d.add(t);     
    return t;
  } // nexttest



  /**
   * 
   *  Write the most recent news to the log file
   *
   */
  public static void test2log(test t) {
    if (logfn==null) {
      logfn="/home/danholle/quest.log";
      String envlog=System.getenv("MERSENNE_LOG");
      if (envlog!=null) logfn=envlog.trim();
    } // if logfn not set

    try   {FileUtils.writeStringToFile(new File(logfn),t.toString()+"\n",true);}
    catch (Exception e) {System.out.println("Mersenne log failure: "+e);}
  } // test2log



  /**
   *  Produce HTML for a short 3-line summary of status.
   */
  public static synchronized String summaryhtml() {
    
    // Line 1:  Status as of Oct 8, 2008 21:05 London Time
    String fmt = "MMM d, yyyy HH:mm";
    SimpleDateFormat sdf = new SimpleDateFormat(fmt);
    String s = "Status as of "+sdf.format(new Date())+" London Time<br>";

    // Take a pass over the tests
    int primecnt=0;
    int factcnt=0;
    int compcnt=0;
    long minclock=999999999999999999L;
    long maxclock=0;
    test hightest=null;
    for (int i=0;i<d.count();i++) {
      test t=d.get(i);
      int state=t.getstate();

      if      (state==test.LUCASP)      {primecnt++; hightest=t;} // prime
      else if (state==test.LUCASC)      {compcnt++;} // composite via lucas
      else if (state==test.UNQUALIFIED) {factcnt++;} // factor found

      long ts=t.getfirst();  if ((ts>0)&&(ts<minclock)) minclock=ts;
      ts=t.getlast();        if (ts>maxclock) maxclock=ts;
    } // for all tests      


    // Line 2:  26 primes, 2087 composites found in 0.785 days
    s+=primecnt+" primes, "+factcnt+" factored, "+compcnt+" composites found in "
      +x3.format((maxclock-minclock)/86400000.0)+" days<br>";

    // Line 3:  Largest prime found: 2**21701 - 1  (6533 digits)
    int highfound = hightest.getp();
    int digits=1+(int)(0.30103*highfound);
    s+="Largest prime found: 2<sup>"+highfound+"</sup> - 1 ("+digits+" digits)<br>";

    return s;

  } // summaryhtml


  public static String showhtml() {
    
    String s="<table cellpadding=0 cellspacing=0 border=0>";

    String fmt="MMM d, yyyy";
    SimpleDateFormat sdf=new SimpleDateFormat(fmt);

    s+="<tr>";
    s+="<td class=boxul align=left>2<sup>p</sup>&nbsp;-&nbsp;1 is prime!</td>";
    s+="<td class=boxul align=center>First<br>Found</td>";
    s+="<td class=boxul align=left>Found By<br>This Server</td>";
    s+="<td class=boxul align=right>Primality<br>Test (Sec.)</td>";
    s+="<td class=boxur align=right>Cumulative<br>Machine Time</td>";
    s+="</tr>";

    // How many primes will we display?
    int lastp=0;
    for (int i=0;i<d.testcount; i++)
      if (d.tests[i].state==test.LUCASP) lastp=i;

    String oldwhen = "x";
    String value="";
    long started=d.get(0).getfirst();
    int lineno=0;

    long totms=0;

    for (int i=0; i<=lastp; i++) {
      test t=d.tests[i];
      
      // Capture cumulative compute time
      if ((t.factms0>0)&&(t.factms1>0)&&(t.factms1>t.factms0))      totms+=t.factms1-t.factms0;  
      if ((t.lucasms0>0)&&(t.lucasms1>0)&&(t.lucasms1>t.lucasms0))  totms+=t.lucasms1-t.lucasms0;  
      
      if (t.state==test.LUCASP) { // if 2**p-1 is prime
        int p = t.getp();
        lineno++;
        
        // Display the value of 2**p-1.  
        // If too long, just display something like "(6231 digits)"
        String s2p1 = "&nbsp;";
        if (p<40) { // show the value of 2**p-1
          long l2p1=1;
          for (int j=0; j<p; j++) l2p1 *= 2L;
          l2p1--;
          s2p1 = ""+l2p1;
          value=" = "+s2p1;
        } // show the value
        else { // number too long, show # digits{
          int len = 1 + (int)(0.30103 * (double)p);
          s2p1 = "("+len+" digits)";
          value=s2p1;
        } // show # of digits

        // Display time when this prime was found.
        String when=sdf.format(new Date(t.getlast()));
        if (when.equals(oldwhen))
          when = "&nbsp;&nbsp;''";
        else
          oldwhen=when;
        
        // Display elapsed time to get this result
        long cumul=totms;
        long tt=0;
        String scum = "";
        if (cumul<60000) { // if less than a minute
          scum=x3.format(cumul*0.001)+"s";
          cumul=0;
        } // if < 1 minute
        else {
          cumul=(cumul+500)/1000;
          tt=cumul%60;
          scum=""+tt+"s";
        }
        while (scum.length()<4) scum=" "+scum;
        
        // Do minutes.
        cumul/=60;
        tt=cumul%60;
        if (tt>0) scum=""+tt+"m"+scum;
        while (scum.length()<8) scum=" "+scum;

        // Do hours.
        cumul/=60;
        tt=cumul%24;
        if (tt>0) scum=""+tt+"h"+scum;
        while (scum.length()<12) scum=" "+scum;

        // do days
        cumul/=24;
        if (cumul>0) scum=""+cumul+"d"+scum;
        while (scum.length()<17) scum=" "+scum;
      

        // What year was this found originally?
        String yrfound = "&nbsp;";
        for(int j = 0; j < ps.length; j++)
          if(ps[j] == p) yrfound = found[j];
 
        // Finally, paste together the HTML
        String prefix = "<td class=boxm";
        if ((i==lastp)||(lineno%5==0)) prefix = "<td class=boxb";

        s+="<tr>";
        s+=prefix+"l align=left>2<sup>"+p+"</sup> - 1 "+value+"</td>";
        s+=prefix+"l align=center>"+yrfound+"</td>";
        s+=prefix+"l align=left>"+when+"</td>";
        s+=prefix+"l align=right>"+x3.format((double)t.getlucasms() * 0.001D)+"</td>";
        s+=prefix+"r align=right>"+scum+"</td>";
        s+="</tr>";
      } // if 2**p-1 is prime
    } // for every test
 
    s+="</table>";
    
  
    // Now display recent test activity

    s+="<p>";
    s+="Recent test results:<br>";
    s+="<table cellspacing=0 cellpadding=0 border=0>";
    s+="<tr>";
    s+="<td class=boxul>When Reported</td>";
    s+="<td class=boxul align=right>p</td>";
    s+="<td class=boxul align=right>CPU Sec.</td>";
    s+="<td class=boxur>Result</td>";
    s+="</tr>";
    fmt = "MMM d, yyyy HH:mm";
    sdf = new SimpleDateFormat(fmt);
    oldwhen = "xxxxxxxxxxxx";

    int lines=25;
    for (int i=0; i<lines; i++) {
      test t=d.get(d.count()-1-i);
      int state=t.getstate();

      String ans="Test pending.";
      
      if (state==test.QUALIFYING)        { // looking for "small" factors
                                           int pct=(50+t.fact*100)/t.maxfact;
                                           ans=""+pct+"% done trying factors.";
                                         } // looking for "small" factors 
      else if (state==test.UNQUALIFIED)  ans="2*"+t.getfact()+"*p+1 is a factor.";
      else if (state==test.QUALIFIED)    ans="0% done with Lucas-Lehmer test.";
      else if (state==test.LUCASING)     { // Running Lucas test
                                           int pct=(50+t.lucas*100)/t.p;
                                           ans=""+pct+"% done with Lucas-Lehmer.";
                                         } // Running Lucas test
      else if (state==test.LUCASC)       ans="Composite via Lucas.";
      else if (state==test.LUCASP)       ans="----- PRIME -----";
      

      long last=t.getlast();
      String when="&nbsp;";
      if (last>0) when=sdf.format(new Date(last));
      String swhen=when;
      if (oldwhen.substring(0, 11).equals(when.substring(0, 11)))
        swhen = when.substring(12, when.length()).trim();
      oldwhen = when;
      
      long cpums=t.getfactms()+t.getlucasms();
      String cputime="&nbsp;";
      if (cpums>0) cputime=x2.format(cpums*0.001);

      String prefix = "<td class=boxm";
      if (i%5==4) prefix = "<td class=boxb";

      s+="<tr>";
      s+=prefix+"l align=right>"+swhen+"</td>";
      s+=prefix+"l align=right>"+t.getp()+"</td>";
      s+=prefix+"l align=right>"+cputime+"</td>";
      s+=prefix+"r>"+ans+"</td>";
      s+="</tr>";
    }

    s+="</table>";
    


    return s;

  } // showhtml

                
  /**
   *  Return latest p found for which 2**p - 1 is prime.
   *
   *  @return Latest Mersenne prime exponent.
   */
  public static int getLatest() {
    int ans=-1;
    int j=d.count()-1;
    
    while (ans<0) {
      test t=d.get(j);
      if (t.getstate()==test.LUCASP) ans=t.getp();
      j--;
    } // while we haven't found a prime yet 
    
    return ans;
  } // getLatest


} // quest 
