package com.danholle.primetime;

import java.math.*;
import java.io.*;

/**
 *  Literal translation of lucdwt.c to java
 */
public class lucdwt {

  static final double TWOPI    = 2*Math.PI;
  static final double SQRTHALF = 0.707106781186547524400844362104D;
  static final double SQRT2    = 1.414213562373095048801688724209D;
  static final int    BITS=16;

  private double[] cn, sn, two_to_phi, two_to_minusphi, scrambled;
  private double   high,low,highinv,lowinv;
  private int      b, c;
  private int[]    permute;

  private test t;

  long nsfft; // nanoseconds spent doing fft
  int words;  // length of thing we are squaring
 
  /**
   *
   *  Constructor
   *
   */
  lucdwt(test tt) {t=tt;}


  private double RINT(double x) {
    return Math.floor(x + 0.5);
  } // RINT

  private void print(double[] x, int N) {
        int printed = 0;

        while (N-->0)
	{
                if ((x[N]==0) && (printed==0))
			continue;
                System.out.println(""+(int)(x[N]));
		printed=1;
	}
        System.out.println("\n");
  } // print


  private void init_scramble_real(int n) {
        int i,j,k,halfn = n>>1;
        int                    tmp;

	for (i=0; i<n; ++i)
	{
		permute[i] = i;
	}
	for (i=0,j=0;i<n-1;i++)
	{
		if(i<j)
		{
			tmp = permute[i];
			permute[i] = permute[j];
			permute[j] = tmp;
		}
		k = halfn;
		while (k<=j)
		{
			j -= k;
			k>>=1;
		}
		j += k;
	}
  } // init_scramble_real


  private void init_fft(int n) {
        int    j;
	double 	e = TWOPI/n;

        cn = new double[n];
        sn = new double[n];
	for (j=0;j<n;j++)
	{
                cn[j] = Math.cos(e*j);
                sn[j] = Math.sin(e*j);
	}
        permute = new int[n];
        scrambled = new double[n];;
	init_scramble_real(n);
  } // init_fft


  private void fft_real_to_hermitian(double[] z,int n) {
        // Output is {Re(z^[0]),...,Re(z^[n/2),Im(z^[n/2-1]),...,Im(z^[1]).
        // This is a decimation-in-time, split-radix algorithm.
        
        int n4;
        double[] x;
        double         cc1, ss1, cc3, ss3;
        int i1, i2, i3, i4, i5, i6, i7, i8, a, a3, dil;
        double         t1, t2, t3, t4, t5, t6;
	double 				e;
	int 	   			nn = n>>1, nminus = n-1, is, id;
        int n2, n8, i, j;

        // x = z-1;   FORTRAN compatibility.

	is = 1;
	id = 4;
	do
	{
		for (i2=is;i2<=n;i2+=id)
		{
			i1 = i2+1;
                        e = z[i2-1];
                        z[i2-1] = e + z[i1-1];
                        z[i1-1] = e - z[i1-1];
		}
		is = (id<<1)-1;
		id <<= 2;
	} while (is<n);

	n2 = 2;
        while ((nn>>=1)>0)
	{
		n2 <<= 1;
		n4 = n2>>2;
		n8 = n2>>3;
		is = 0;
		id = n2<<1;
		do
		{
			for (i=is;i<n;i+=id)
			{
				i1 = i+1;
				i2 = i1 + n4;
				i3 = i2 + n4;
				i4 = i3 + n4;
                                t1 = z[i4-1]+z[i3-1];
                                z[i4-1] -= z[i3-1];
                                z[i3-1] = z[i1-1] - t1;
                                z[i1-1] += t1;
				if (n4==1)
					continue;
				i1 += n8;
				i2 += n8;
				i3 += n8;
				i4 += n8;
                                t1 = (z[i3-1]+z[i4-1])*SQRTHALF;
                                t2 = (z[i3-1]-z[i4-1])*SQRTHALF;
                                z[i4-1] = z[i2-1] - t1;
                                z[i3-1] = -z[i2-1] - t1;
                                z[i2-1] = z[i1-1] - t2;
                                z[i1-1] += t2;
			}
			is = (id<<1) - n2;
			id <<= 2;
		} while (is<n);
		dil = n/n2;
		a = dil;
		for (j=2;j<=n8;j++)
		{
			a3 = (a+(a<<1))&(nminus);
			cc1 = cn[a];
			ss1 = sn[a];
			cc3 = cn[a3];
			ss3 = sn[a3];
			a = (a+dil)&(nminus);
			is = 0;
			id = n2<<1;
			do
			{
				for (i=is;i<n;i+=id)
				{
					i1 = i+j;
					i2 = i1 + n4;
					i3 = i2 + n4;
					i4 = i3 + n4;
					i5 = i + n4 - j + 2;
					i6 = i5 + n4;
					i7 = i6 + n4;
					i8 = i7 + n4;
                                        t1 = z[i3-1]*cc1 + z[i7-1]*ss1;
                                        t2 = z[i7-1]*cc1 - z[i3-1]*ss1;
                                        t3 = z[i4-1]*cc3 + z[i8-1]*ss3;
                                        t4 = z[i8-1]*cc3 - z[i4-1]*ss3;
					t5 = t1 + t3;
					t6 = t2 + t4;
					t3 = t1 - t3;
					t4 = t2 - t4;
                                        t2 = z[i6-1] + t6;
                                        z[i3-1] = t6 - z[i6-1];
                                        z[i8-1] = t2;
                                        t2 = z[i2-1] - t3;
                                        z[i7-1] = -z[i2-1] - t3;
                                        z[i4-1] = t2;
                                        t1 = z[i1-1] + t5;
                                        z[i6-1] = z[i1-1] - t5;
                                        z[i1-1] = t1;
                                        t1 = z[i5-1] + t4;
                                        z[i5-1] -= t4;
                                        z[i2-1] = t1;
				}
				is = (id<<1) - n2;
				id <<= 2;
			} while (is<n);
		}
	}
  } // fft_real_to_hermitian


  private void fftinv_hermitian_to_real(double[] z,int n) {
        // Input is {Re(z^[0]),...,Re(z^[n/2),Im(z^[n/2-1]),...,Im(z^[1]).
        // This is a decimation-in-frequency, split-radix algorithm.
         
        int n4;
        double         cc1, ss1, cc3, ss3;
        double         t1, t2, t3, t4, t5;
        double[] x;
        int n8, i1, i2, i3, i4, i5, i6, i7, i8,
			   			a, a3, dil;
	double 				e;
	int 	   			nn = n>>1, nminus = n-1, is, id;
	int 	   			n2, i, j;

        // x = z-1;

        n2 = n<<1;
        while((nn>>=1)>0)
	{
		is = 0;
		id = n2;
		n2 >>= 1;
		n4 = n2>>2;
		n8 = n4>>1;
		do
		{
			for (i=is;i<n;i+=id)
			{
				i1 = i+1;
				i2 = i1 + n4;
				i3 = i2 + n4;
				i4 = i3 + n4;
                                t1 = z[i1-1] - z[i3-1];
                                z[i1-1] += z[i3-1];
                                z[i2-1] += z[i2-1];
                                z[i3-1] = t1 - z[i4-1] - z[i4-1];
                                z[i4-1] = t1 + z[i4-1] + z[i4-1];
				if (n4==1)
					continue;
				i1 += n8;
				i2 += n8;
				i3 += n8;
				i4 += n8;
                                t1 = z[i2-1]-z[i1-1];
                                t2 = z[i4-1]+z[i3-1];
                                z[i1-1] += z[i2-1];
                                z[i2-1] = z[i4-1]-z[i3-1];
                                z[i3-1] = -SQRT2*(t2+t1);
                                z[i4-1] = SQRT2*(t1-t2);
			}
			is = (id<<1) - n2;
			id <<= 2;
		} while (is<nminus);
		dil = n/n2;
		a = dil;
		for (j=2;j<=n8;j++)
		{
			a3 = (a+(a<<1))&(nminus);
			cc1 = cn[a];
			ss1 = sn[a];
			cc3 = cn[a3];
			ss3 = sn[a3];
			a = (a+dil)&(nminus);
			is = 0;
			id = n2<<1;
			do
			{
				for (i=is;i<n;i+=id)
				{
					i1 = i+j;
					i2 = i1+n4;
					i3 = i2+n4;
					i4 = i3+n4;
					i5 = i+n4-j+2;
					i6 = i5+n4;
					i7 = i6+n4;
					i8 = i7+n4;
                                        t1 = z[i1-1] - z[i6-1];
                                        z[i1-1] += z[i6-1];
                                        t2 = z[i5-1] - z[i2-1];
                                        z[i5-1] += z[i2-1];
                                        t3 = z[i8-1] + z[i3-1];
                                        z[i6-1] = z[i8-1] - z[i3-1];
                                        t4 = z[i4-1] + z[i7-1];
                                        z[i2-1] = z[i4-1] - z[i7-1];
					t5 = t1 - t4;
					t1 += t4;
					t4 = t2 - t3;
					t2 += t3;
                                        z[i3-1] = t5*cc1 + t4*ss1;
                                        z[i7-1] = -t4*cc1 + t5*ss1;
                                        z[i4-1] = t1*cc3 - t2*ss3;
                                        z[i8-1] = t2*cc3 + t1*ss3;
				}
				is = (id<<1) - n2;
				id <<= 2;
			} while (is<nminus);
		}
	}
	is = 1;
	id = 4;
	do
	{
		for (i2=is;i2<=n;i2+=id)
		{
			i1 = i2+1;
                        e = z[i2-1];
                        z[i2-1] = e + z[i1-1];
                        z[i1-1] = e - z[i1-1];
		}
		is = (id<<1) - 1;
		id <<= 2;
	} while (is<n);
	e = 1/(double)n;
	for (i=0;i<n;i++)
	{
                z[i] *= e;
	}
  } // fftinv_hermitian_to_real x[]


  private void square_hermitian(double[] b, int n) {
        int k, half = n>>1;
        double         c, d;

	b[0] *= b[0];
	b[half] *= b[half];
	for (k=1;k<half;k++)
	{
		c = b[k];
		d = b[n-k];
		b[n-k] = 2.0*c*d;
		b[k] = (c+d)*(c-d);
	}
  } // square_hermitian


  private void squareg(double[] x, int size) {

	long start=System.nanoTime();
        fft_real_to_hermitian(x, size);
        nsfft+=System.nanoTime()-start;
	
        square_hermitian(x, size);
	
        start=System.nanoTime();
        fftinv_hermitian_to_real(x, size);
        nsfft+=System.nanoTime()-start;
	
  } // squareg




  // ------------ Lucas Test - specific routines ------------------- 

  private void init_lucas(int q,int N) {
	int 	j,qn,a,len;
        double  log2 = Math.log(2.0);

        two_to_phi = new double[N];
        two_to_minusphi = new double[N];

        low = RINT(Math.exp(Math.floor((double)q/N)*log2));
	high = low+low;
	lowinv = 1.0/low;
	highinv = 1.0/high;
	b = q & (N-1);
	c = N-b;

	two_to_phi[0] = 1.0;
	two_to_minusphi[0] = 1.0;
	qn = q&(N-1);

	for(j=1; j<N; ++j)
	{
		a = N - (( j*qn)&(N-1));
                two_to_phi[j] = Math.exp(a*log2/N);
		two_to_minusphi[j] = 1.0/two_to_phi[j];
	}
  } // init_lucas

    
  private double addsignal(double[] x,int N,int error_log) {
        int k,j,bj,bk,sign_flip,NminusOne = N-1;
        double         zz,w; // *xptr = x, *xxptr;
        double         hi = high, lo = low, hiinv = highinv, loinv = lowinv;
   	double 				err, maxerr = 0.0;

        // mapped *xptr  --> x[ix]
        //        *xxptr --> x[ixx]
        int ix=0;
        int ixx;

   	bk  =  0;
  	for (k=0; k<N; ++k) 
  	{
        if ((zz=x[ix])<0) 
    	{
                zz = Math.floor(0.5 - zz);
       		sign_flip = 1;
       	}
     	else 
     	{
                zz = Math.floor(zz+0.5);
       		sign_flip = 0;
       	}
        if (error_log!=0) 
     	{
                if (sign_flip!=0) 
                        err = Math.abs(zz + x[ix] );  
     		else 
                        err = Math.abs(zz  - x[ix]);
	 		if (err > maxerr) 
	 			maxerr = err;
     	}
        x[ix] = 0;
     	j = k;
     	bj = bk;
        ixx=ix++;
     	do 
     	{
         	if (j==N) 
         		j=0;
	  		if (j==0)
	  		{
                                ixx=0; 
	  			bj = 0;  
                                w = Math.floor(zz*hiinv);
                        if (sign_flip!=0) 
                                x[ixx] -= (zz-w*hi); 
	  	    	else 
                                x[ixx] += (zz-w*hi); 
	  	    }
	  		else if (j==NminusOne) 
	  		{ 
                                w = Math.floor(zz*loinv);
                    if (sign_flip!=0) 
                        x[ixx] -= (zz-w*lo); 
	            else 
                        x[ixx] += (zz-w*lo); 
	        }
	    	else if (bj >= c) 
	    	{ 
                        w = Math.floor(zz*hiinv);
                    if (sign_flip!=0) 
                        x[ixx] -= (zz-w*hi); 
	    	    else 
                        x[ixx] += (zz-w*hi); 
	    	}
	       	else 
	       	{ 
                        w = Math.floor(zz*loinv);
                    if (sign_flip!=0) 
                        x[ixx] -= (zz-w*lo); 
	            else 
                        x[ixx] += (zz-w*lo); 
	        }
          	zz = w;
          	++j;
                        ++ixx;
          	bj += b; 
          	if (bj>=N) 
          		bj -= N;
         }  while(zz!=0.0);
     
     	bk += b; 
     	if (bk>=N) 
     		bk -= N;
    }
	return(maxerr);
  } // addsignal 


  private void patch(double[] x,int N) {  
        int j,bj,NminusOne = N-1, carry;
        double         hi = high, lo = low, highliminv, lowliminv;
        double         xx;  // *px = x
        double                         highlim,lowlim, lim, inv, base;

        // Same drill: map *px --> x[ipx]
        int ipx=0;

	carry = 0;
	highlim = hi*0.5;
	lowlim = lo*0.5;
	highliminv =1.0/highlim;
	lowliminv = 1.0/lowlim;

        xx = x[ipx] + carry;
	if (xx >= highlim)
		carry =((int)(xx*highliminv+1))>>1;
	else if (xx<-highlim)
		carry = -(((int)(1-xx*highliminv))>>1);
	else
		carry = 0;

        x[ipx++] = xx - carry*hi;
	bj = b;
	for (j=1; j<NminusOne; ++j)
	{
                xx = x[ipx] + carry;
		if ((bj & NminusOne) >= c)
		{
			if (xx >= highlim)
				carry =((int)(xx*highliminv+1))>>1;
			else if (xx<-highlim)
				carry = -(((int)(1-xx*highliminv))>>1);
			else
				carry = 0;

                        x[ipx] = xx - carry*hi;
		}
		else
		{
			if (xx >= lowlim)
				carry =((int)(xx*lowliminv+1))>>1;
			else if (xx<-lowlim)
				carry = -(((int)(1-xx*lowliminv))>>1);
			else
				carry = 0;

                        x[ipx] = xx - carry*lo;
		}
                ++ipx;
		bj += b;
	}

        xx = x[ipx] + carry;
	if (xx >= lowlim)
		carry = ((int)(xx*lowliminv+1))>>1;
	else if (xx<-lowlim)
		carry = -(((int)(1-xx*lowliminv))>>1);
	else
		carry = 0;

        x[ipx] = xx - carry*lo;
        if (carry!=0)
	{
		j = 0;
		bj = 0;
                ipx=0;
                while (carry!=0)
		{
                        xx = x[ipx] + carry;
			if (j==0)
			{
				lim = highlim;
				inv = highliminv;
				base = hi;
			}
			else if (j==NminusOne)
			{
				lim = lowlim;
				inv = lowliminv;
				base = lo;
			}
			else if ((bj & NminusOne) >= c)
			{
				lim = highlim;
				inv = highliminv;
				base = hi;
			}
			else
			{
				lim = lowlim;
				inv = lowliminv;
				base = lo;
			}

			if (xx>=lim)
				carry = ((int)(xx*inv+1))>>1;
			else if (xx<-lim)
				carry = -(((int)(1-xx*inv))>>1);
			else
				carry = 0;

                        x[ipx++] = xx - carry*base;
			bj += b;
			if (++j == N)
			{
				j = 0;
				bj = 0;
                                ipx = 0;
			}
		}
	}
  } // patch


  private void check_balanced(double[] x, int N) {
	int 	j,bj = 0,NminusOne = N-1;
        double  limit, hilim,lolim; // *ptrx = x;

        // *ptrx -> x[ix]
        int ix=0;

	hilim = high*0.5;
	lolim = low*0.5;
	for (j=0; j<N; ++j)
	{
		if (j==0)
			limit = hilim;
		else if (j==NminusOne)
			limit = lolim;
		else if ((bj & NminusOne) >= c)
			limit = hilim;
		else
			limit = lolim;

                assert ((x[ix]<=limit) && (x[ix]>=-limit));
                ++ix;
		bj+=b;
	}
  } // check_balanced


  private double lucas_square(double[] x,int N,int error_log) {
        int j;
        double err;

        // *perm -> permute[ip]
        // *ptry -> scrambled[iy]
        // *ptrx -> x[ix]
        // *ptrmphi -> two_to_minusphi[im]

        int ip=0;
        int iy=0;

        for (j=0; j<N; ++j, ip++)
	{
                scrambled[iy++] = x[permute[ip]] * two_to_phi[permute[ip]];
	}

	squareg(scrambled,N);

        ip = 0;
        int ix=0;
        int im=0;

	for (j=0; j<N; ++j)
	{
                x[ix++] = scrambled[permute[ip++]] *  two_to_minusphi[im++];
	}
	err = addsignal(x,N, error_log);
	patch(x,N);
        if (error_log!=0)
		check_balanced(x,N);

	return(err);
  } // lucas_square


  private int isszero(double[] x,int N) {
        int j;
        int ip=0;

        // *xp -> x[ip]

	for(j=0; j<N; ++j)
	{
                if (RINT(x[ip++])!=0)
			return 0;
	}
	return 1;
  } // isszero


  private void balancedtostdrep(double[] x,int N) {
 	int 	sudden_death = 0, j = 0, NminusOne = N-1, bj = 0;

        while(true) 
  	{
		if (x[j] < 0) 
	 	{
			--x[(j+1) & NminusOne];
			if (j==0) 
				x[j]+=high;
        	else if (j==NminusOne) 
        		x[j]+=low;
	  		else if ((bj & NminusOne) >=c) 
	  			x[j]+=high;
	    	else 
	    		x[j]+=low;
		}
                else if (sudden_death!=0) 
	 		break;
	 	bj+=b;
	 	if (++j==N) 
	 	{
			sudden_death = 1;
			j = 0;
			bj = 0;
		}
	 }
  } // balancedtostdrep





  /**
   *  Use FFT to test a Mersenne candidate for primality.
   */
  public boolean isMersennePrime() {
    nsfft=0;

    // Tell the world we are doing a Lucas test
    t.lucasing(0);
    
    int q=t.getp();

    double err;
    int errflag=0;

    // What should n be?  Educated guess...
    double zn=1.1*0.0445*q;
    int n=1;
    while (n<zn) n+=n;
    words=n;
        
    double[] x = new double[n];
    init_fft(n);
    init_lucas(q,n);

    for (int j=0;j<n;j++) x[j]=0.0;
    x[0] = 4.0;

    for (int j=1;j<q-1;j++) {
      if ((j%50)==0) t.lucasing(j);  // report progress

      err = lucas_square(x,n,errflag);
      if (errflag!=0) System.out.println(" "+j+" maxerr: "+err);
		
      x[0] -= 2.0;
    } // for each iteration of Lucas test 

    boolean isprime=(isszero(x,n)!=0);

    if (isprime) t.lucasp();
    else t.lucasc();

    return isprime;
     
  } // isMersennePrime


} // lucdwt

