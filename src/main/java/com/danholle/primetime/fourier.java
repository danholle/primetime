package com.danholle.primetime;

import java.lang.*;

/**
 * 
 *  Fourier transform utilities for working with large Mersenne primes.
 *  This effort differs from class fft in the following dimensions:
 *  <ul>
 *    <li>Rewritten from the ground up to be non-recursive &amp; support real FFT
 *    <li>TODO Adapted to multi-radix
 *    <li>TODO Recoded for loop unwinding, vectorization tweaking, etc.
 *  </ul>
 *
 */
public class fourier {

  int flen;     // factor length, in words
  int halfn;    // factor length, grossed up to a power of 2
  int n;        // length of the product = 2*halfn

  long nsfft;   // Cumulative time in the fft method

  double[] tr;  // work area
  double[] ti; 
  
  double[] wr;  // Roots of unity:  w[k]=exp(2πik/n).
  double[] wi;

  double[] whr; // Roots (twice as long).  Used for Hermitian stuff
  double[] whi;

  int[] rev;

  /**
   *
   *  Constructor.
   *
   *  This sets up a multiplication engine for factors of 
   *  a specified length.
   *
   */
  fourier(int fwords) {
    flen=fwords;
    halfn=1; 
    while (halfn<flen) halfn+=halfn;
    n=halfn+halfn;
    nsfft=0;

    // Work area
    tr=new double[n];
    ti=new double[n];

    // Compute roots of unity
    // w[k]=exp(2πik/n),  
    wr=new double[n];   
    wi=new double[n];
    wr[0]=1.0;          
    wi[0]=0.0;
    double theta=2*Math.PI/(double)n;
    for (int k=1;k<n;k++) {
      wr[k]=Math.cos(theta*k);
      wi[k]=Math.sin(theta*k);
    } // for every root of unity

    whr=new double[2*n];   
    whi=new double[2*n];
    whr[0]=1.0;          
    whi[0]=0.0;
    theta=Math.PI/(double)n;
    for (int k=1;k<2*n;k++) {
      whr[k]=Math.cos(theta*k);
      whi[k]=Math.sin(theta*k);
    } // for every root of unity

    // Map n into bit-reversed n
    rev=new int[n];
    int bits=0;
    int nn=1;
    while (nn<n) {bits++; nn+=nn;}
    int[] digits=new int[bits];
    int[] weight=new int[bits];
    nn=halfn;
    for (int k=0;k<bits;k++) {
      digits[k]=0;
      weight[k]=nn;
      nn/=2;
    }
    rev[0]=0;
    for (int k=1;k<n;k++) {
      // increment the number in digits.
      int m=0;
      while (digits[m]==1) {digits[m]=0;m++;}
      digits[m]+=1;
      rev[k]=0;
      for (m=0;m<bits;m++) rev[k]+=weight[m]*digits[m];
    }  
  } // fourier constructor


  /**
   *
   *  Generate transform length, given a minimum.
   *
   *  Fast transform requires a composite length, where we divide the length by a radix at each step.
   *  For now, we only support radix-2... but stay tuned.
   *
   *  @param mn Number of "words" we'd ideally like to see in the transform
   *  @return A number >= mn, which is a width our FFT supports.
   *
   */
  static int roundup(int mn) {
    int m=mn;
    boolean worried=true;
    while (worried) {
      int resid=m;
      while (resid%2==0) resid/=2;
      //while (resid%3==0) resid/=3; etc for each radix we support
      if (resid==1) worried=false;
      else m++;
    } // while worried

    return m;
  } // roundup


  /**
   *
   *  Simplest possible FFT.
   *  <p>
   *  Compute the Fourier transform y = F(x) where x is comprised of n complex numbers x<sub>0</sub>, 
   *  x<sub>1</sub>, ..., x<sub>n-1</sub>:
   *  <table style="background-color: lightblue; font-size: 150%; width: 0%; margin: 20px; padding: 20px">
   *    <tr>
   *      <td>&nbsp;</td>
   *      <td style="vertical-align: bottom; text-align: center">n-1</td>
   *      <td>&nbsp;</td>
   *    </tr>
   *    <tr>
   *      <td style="text-align: right; vertical-align: middle">y<sub>k</sub>&nbsp;&nbsp;=</td>
   *      <td style="line-height: 80%; vertical-align: middle; text-align: center; font-size: 400%">&Sigma;</td>
   *      <td style="text-align: left; vertical-align: middle">x<sub>j</sub>&omega;<sup>jk</sup></td>
   *    </tr>
   *    <tr>
   *      <td>&nbsp;</td>
   *      <td style="text-align: center">j=0</td>
   *      <td>&nbsp;</td>
   *    </tr>
   *  </table>
   *  where &omega; is the nth complex root of unity.  The reverse transform <span style="text-decoration: overline">F</span> 
   *  is the same, except &omega;'s exponent is negative.  
   *  <p>
   *  Okay, I'll fess up:  this is not quite the Fourier transform because we don't scale the result.  That is, 
   *  <table style="background-color: lightblue; font-size: 150%; width: 0%; margin: 20px; padding: 20px">
   *    <tr>
   *      <td><span style="text-decoration: overline">F</span>(F(x))&nbsp;=&nbsp;nx
   *    </tr>
   *  </table>
   *  <p>
   *  A few other random notes:
   *  <ul>
   *    <li>We set up as much as possible in the fourier constructor.  For example, the n roots of unity
   *      are precomputed, and simply referenced during the computation.
   *    <li>This implementation is restricted to width n being a power of 2.
   *    <li>The recursive FFT implementation is flattened to a loop for performance reasons.
   *  </ul>
   *  @param xr    Input (real part).  n values.
   *  @param xi    Input (imaginary part). n values
   *  @param yr    Input (real part).  n values.
   *  @param yi    Input (imaginary part). n values
   *  @param sign  +1 for forward transform;  -1 for inverse transform (with scaling).
   */
  public void fft(double[] xr, double[] xi,  // input
                  double[] yr, double[] yi,  // output
                  int sign) {                // +1=forward, -1=inverse transform

    long start=System.nanoTime();

    // Binary shuffle of inputs into output arrays
    for (int k=0;k<n;k++) {
      yr[rev[k]]=xr[k];
      yi[rev[k]]=xi[k];
    }
    
    // Each pass of this loop is as follows:
    //  -  We have length mmax FFT's in y arrays.
    //  -  Graduate to length 2*mmax
    int mmax=1;
    while (n > mmax) {
      int dmax=mmax+mmax;
      int nw=0;
      int dnw=sign*n/dmax;
      for (int m=0;m<mmax;m++) {
        double wrn=wr[nw];
        double win=wi[nw];
        for (int k=m;k<n;k+=dmax) {                                                   
          int j=k+mmax;                                                                       
          double tr=wrn*yr[j] - win*yi[j];                                             
          double ti=win*yr[j] + wrn*yi[j];                                             
          yr[j]=yr[k]-tr;                                                               
          yi[j]=yi[k]-ti;                                                       
          yr[k]=yr[k]+tr;                                                               
          yi[k]=yi[k]+ti;                                                       
        }            
        nw+=dnw;
        if (nw<0) nw+=n;                    
      }                                                                                                
      mmax=dmax;                                                                    
    } // while                                                                                                   


    nsfft+=System.nanoTime()-start;
  } // fft                                                                                                        


  /**
   *
   *  FFT of real data.
   *
   *  Compute the forward or inverse Fourier Transform of data, with                                       
   *  data containing real valued data only. The output is complex                                         
   *  valued after the first two entries, stored in alternating real                                       
   *  and imaginary parts. The first two returned entries are the real                                     
   *  parts of the first and last value from the conjugate symmetric                                       
   *  output, which are necessarily real. The length must be a power                                       
   *  of 2.
   *                                                                                                
   */   
  public void realfft(double[] xr,double[] xi, // packed input 
                      double[] yr,double[] yi, // packed output?
                      int sign) {
    int n=2*xr.length;
    if (sign>0) fft(xr,xi, tr,ti, 1);  
    else for (int i=0;i<xr.length;i++) {tr[i]=xr[i]; ti[i]=xi[i];}                                                                          
    int wn=1;
    if (sign<0) wn=whr.length-1;                                                                                       
    for (int j = 1; j <= n/4; ++j) {                                                                       
      double wjr=whr[wn];
      double wji=whi[wn];
      int k = n / 2 - j;                                                                               
      double tkr = tr[k];    // real and imaginary parts of t_k  = t_(n/2 - j)                      
      double tki = ti[k];                                                                       
      double tjr = tr[j];    // real and imaginary parts of t_j                                     
      double tji = ti[j];                                                                       
      double a = (tjr - tkr) * wji;                                                                      
      double b = (tji + tki) * wjr;                                                                       
      double c = (tjr - tkr) * wjr;                                                                       
      double d = (tji + tki) * wji;                                                                       
      double e = (tjr + tkr);                                                                             
      double f = (tji - tki);                                                                             

      // compute entry y[j]                                                                            
      tr[j] = 0.5 * (e + sign * (a + b));                                                        
      ti[j] = 0.5 * (f + sign * (d - c));                                                    

      // compute entry y[k]                                                                            
      tr[k] = 0.5 * (e - sign * (b + a));                                                        
      ti[k] = 0.5 * (sign * (d - c) - f);                                                    
                                                                                                               
      wn+=sign;                                                                    
    } // for j                                                                                                   

    if (sign>0) {
      // compute final y0 and y_{N/2}, store in data[0], data[1]                                       
      double temp = tr[0];                                                                              
      tr[0]+= ti[0];                                                                              
      ti[0]= temp - ti[0]; 
      for (int i=0;i<yr.length;i++) {yr[i]=tr[i]; yi[i]=ti[i];}                                                                       
    }                                                                                                    
    else {
      double temp = tr[0]; // unpack the y0 and y_{N/2}, then invert FFT                                
      tr[0] = 0.5 * (temp + ti[0]);                                                                
      ti[0] = 0.5 * (temp - ti[0]);                                                                
      for (int i=0;i<xr.length;i++) {xr[i]=yr[i]; xi[i]=yi[i];}                                                                          
      fft(tr,ti, yr,yi, -1);                                                                           
    } // if inverse

  } // realfft


} // fourier
