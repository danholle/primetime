<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="primetime" prefix="primetime" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta http-equiv="Pragma"       content="no-cache">
    <meta http-equiv="Expires"      content="-1">
    <title>2**<primetime:latest/> - 1 is prime!</title>
    <style type="text/css" media="screen">
      td.boxul {
        background-color:  #C0C0C0;
        padding: 5px 10px 5px 10px;
        font-size: 10pt;
        border: solid black;
        border-width: 1px 0px 1px 1px;
        white-space: nowrap;
        vertical-align: middle;
      }
      td.boxur {
        background-color:  #C0C0C0;
        padding: 5px 10px 5px 10px;
        font-size: 10pt;
        border: solid black;
        border-width: 1px 1px 1px 1px;
        white-space: nowrap;
        vertical-align: middle;
      }
      td.boxml {
        padding: 0px 10px 0px 10px;
        font-size: 10pt;
        border: solid black;
        border-width: 0px 0px 0px 1px;
        white-space: nowrap;
        vertical-align: middle;
      }
      td.boxmr {
        padding: 0px 10px 0px 10px;
        font-size: 10pt;
        border: solid black;
        border-width: 0px 1px 0px 1px;
        white-space: nowrap;
        vertical-align: middle;
      }
      td.boxbl {
        padding: 0px 10px 0px 10px;
        font-size: 10pt;
        border: solid black;
        border-width: 0px 0px 1px 1px;
        white-space: nowrap;
        vertical-align: middle;
      }
      td.boxbr {
        padding: 0px 10px 0px 10px;
        font-size: 10pt;
        border: solid black;
        border-width: 0px 1px 1px 1px;
        white-space: nowrap;
        vertical-align: middle;
      }
    </style>
  </head>
  <body bgColor=#ffffff leftMargin=0 topMargin=0 marginheight=0 marginwidth=0
  style="font-family: Helvetica, Arial, sans-serif">
    <a name="top"/>
    <table height=100% cellspacing=0 cellpadding=0 width=100% border=0>
      <tr>
        <td><img src="mathgeek.jpg"/></td>
        <td style="background: lightblue">
          <table width=100% border=0 cellpadding=0 cellspacing=0>
            <tr>
              <td style="padding: 10px 20px 10px 30px; color: darkblue; font-size: 24pt; font-weight: bold">
                Prime Time, Real Time!<br>
                <span style="font-size: medium">
                This insomniac server is searching for primes 24x7
              </td>
              <td align=right valign=middle 
              style="font-weight: bold; font-size: small; color: darkblue; padding-right: 20px">
                <primetime:summary/>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td width=150 valign=top
        style="font-size: 14pt; background: lightblue; color: darkblue; padding: 10px; font-weight: bold">
          <p>
          Intro<br>
          <p>
          <a href=#mersenne>Mersenne</a><br>
          <p>
          <a href=#moore>Moore's Law</a><br>
          <p>
          <a href=#personal>Personal Note</a><br>
        </td>
        <td valign=top style="background: white; padding: 20px">
          <table align=right border=0 cellpadding=0 cellspacing=0 style="margin: 0px 0px 15px 15px">
            <tr>
              <td>
                <primetime:sofar/>
              </td>
            </tr>
          </table>
          Prime numbers!  A topic which captures the imagination of a math 
          geek like no other.  For good reason:
          <ul>
            <li>Prime numbers, in principle, are as simple as you can get
            <li>On second glance, they are about as deep and complex as you can imagine
            <li>The study of prime numbers is almost entirely without practical application.
            <li>Brilliant minds have worked on the subject for centuries... indeed, millenia.
          </ul>
          So, what can this website contribute?  Read on...
          <p>
          <hr>
          <p>
          <img src=360_91.jpg  align=left width=300 style="padding: 0px 10px 10px 0px"/>
          <p>
          Back in the days before the invention of colors, the 360/91 (pictured) 
          was the fastest computer on the planet.
          Only 15 were ever made, and at a cost of US$40M a pop (in 2014
          dollars), you can see why.
          <p> 
          When I was in high school, I imagined that massive machines like this
          one would reveal Nature's deepest secrets by brute force.  
          As evidence of this, a 360/91 found the world's largest prime in 1971.  It only
          took 35 minutes to prove that 
          2<sup>19937</sup> - 1 (a number over 6000 digits long) was prime.
          <p>
          <hr>
          <p>
          <img src=hp2133.jpg align=right width=200 style="padding: 0px 0px 10px 10px" />
          <p>
          Well, things have changed.
          <p>   
          This is my HP 2133 netbook.  For the price of the 360/91, you could buy
          100,000 of these beauties... and, if you're a <i>serious</i> Math Geek,
          you could give one to each of your friends, keep one for yourself,
          and still have 99,999 left...
          <p>  
          By contemporary standards, its processor is... well, it's a steaming pile
          of poo.  It's perhaps 20x slower than a typical desktop... or worse.  
          <p>
          As evidence of how far we've come, though... the HP 2133 carried out the
          calculation to show 2<sup>19937</sup> - 1 is prime in 45 seconds, easily
          besting the 360/91... by a factor of 50.
          <p>
          I must admit I haven't used its marvelous computational powers to reveal any
          of Nature's deepest secrets today.  Check back tomorrow! 
          <p>
          PS Never mind.  I threw it away.  My 7" tablet turned out to be lighter, smaller, cheaper, and faster.
          <p
          <hr>
          <p>
          So, why exactly do we care about
          <a href="http://en.wikipedia.org/wiki/Prime_number">Prime Numbers<a>?
          Perhaps because they are at once
          very simple, and yet elusively complex.  That combination strikes deep
          at the heart of Math-Geek-ness.
          <p>
          We've known for 2300
          years that there are an infinite number of primes.
          But in all those centuries, we've found no formula
          which yields prime numbers.  They seem kind of random;  but there's no
          randomness.  We just don't know the secret.  To this day, if we want to find
          a big prime, we're
          stuck with finding a likely candidate, and testing it.
          <p>
          Modern cryptography is heavily reliant on prime numbers.
          So, yes, there is
          a practical side to prime number research.  But I won't pretend for
          an instant that what's going on with this site is practical.
          <p>
          This site is about finding big primes... just <i>because.</i>  
          If you don't get it, you're just not Math Geek material.  
          <p>
          <a name=mersenne />
          <hr>
          <p>
          <h3>Mersenne Primes</h3>
          <p>
          While we don't have a formula for prime numbers, we have made
          some significant progress in finding faster ways of testing
          <i>particular kinds</i> of numbers for primality.  The most
          significant of these are 
          <a href="http://en.wikipedia.org/wiki/Mersenne_prime">Mersenne</a> primes,
          which are of the form
          <div style="font-size: large; margin: 15px 0px 15px 30px">
            2<sup>p</sup>&nbsp;-&nbsp;1
          </div>
          where p itself is prime.
          <p>
          In the 19th century, Edouard Lucas found a way
          of testing these numbers which still requires a lot of arithmetic;
          but the amount of arithmetic is related to p, not to 2<sup>p</sup>.
          This may not sound like much, but the impact on finding large
          primes is incredible... 
          To put that in perspective:  the number
          <div style="font-size: large; margin: 15px 0px 15px 30px">
            2<sup>127</sup>&nbsp;-&nbsp;1
          </div>
          is prime... a 39 digit number.  To prove this via brute-force
          factor checking would require about 80,000 years on my desktop PC.
          But using the Lucas test, it requires about 3 milliseconds.
          <p>
          Wow!
          <p>
          Now maybe you can see why the largest known prime, at any
          particular point in history, has often been a Mersenne prime.
          <p>
          Thanks, Ed...
          <p>
          <a name=moore />
          <hr>
          <p>
          <h3>Moore's Law Kicks Ass!</h3>
          <p>
          Moore's Law:  Computers double in speed every 2 years.  And yet, they
          always seem too slow for the task at hand... what's that all about?
          <p>
          It's because we keep upping the ante, throwing bigger and
          bigger problems at the machines we have.  As Prime Numbers have always
          been at the forefront of large-scale computing, Prime Number research has
          morphed along with the phenomental growth in compute power.
          <p>
          To put this in perspective, let's compare historical work with Prime Numbers
          with what's possible with my boring old desktop PC.
          <ul>
            <li>Prior to the advent of the computer, it took mathematicians
              over 400 years to find the first 12 Mersenne primes.<br>
              <p>
              <i>My PC did it in 0.2 seconds.</i>
              <p>
            <li>Lucas determined that 2<sup>127</sup> - 1 was prime, by hand.  This
              took him 19 years.
              <p>
              <i>My PC did it in 0.003 seconds.</i>
              <p>
            <li>With the help of computers, the <i>next</i> 12 Mersenne primes were
              found over a period of <i>only</i> 20 years.<br>
              <p>
              <i>My PC did it in an hour.</i>
              <p>
          </ul>
          <p>
          Holy cow!
          <p>
          Not only are computers faster, but there's more of them.  There are literally
          billions of machines out there, and many of these are mostly idle.  This leads
          to some interesting approaches to computing at the bleeding edge.
          <p>
          For example... if you find this
          website just a bit creepy or just a bit too geeky, 
          you'll be pleased to learn that
          this stuff's small time.  As you read this there are <i>tens of thousands</i>
          of people looking for Mersenne primes, and <i>hundreds of thousands</i> of
          processors working on the task.  
          <p>
          Have a quick look at the <a href=http://www.mersenne.org>GIMPS Project</a> 
          (Great Internet Mersenne 
          Prime Search) for an example of massive community computing at its best. 
          <p>
          <a name=personal />
          <hr>
          <p>
          <h3>Personal Note</h3>
          <p>
          I played around with Mersenne primes back in the 70s.  At my first job,
          while still in high school, I had access to an IBM 370/145 mainframe;
          later, that was replaced with a Xerox Sigma 9, which was actually
          a scientific machine, better for large-scale mathematical stuff.
          <p>
          I used the Sigma box to calculate &pi; to 1,250,000 places... and also used
          it to search for Mersenne primes.  I managed to find that p=21701
          yielded a prime;  since p=19937 was already known, this only required
          a few hundred tests.
          <p>
          If there is a quintessential scenario that is "math geek nirvana,"
          it might be this:  you alone know &pi; more accurately than anyone on the
          planet, and you alone know the world's largest prime -- and you don't
          know who to tell.
          <p>
          The Mersenne prime 2<sup>21701</sup> - 1 was "officially" found
          Noll &amp; Nickel 3 years later.
          <p>
          When I left to go to the University of Illinois, my work colleagues arranged
          a going-away party, at which they ceremoniously 
          presented me with a bill for the recreational machine time I'd used...
          several thousand CPU hours on a large mainframe.
          <ul>
            <li>Calculating &pi; and finding primes: on the order of a million bucks.
            <li>Being math alpha geek:  priceless.
          </ul>
        </td>
      </tr>
      <tr valign=bottom>
        <td width=100 style="background: lightblue">
          &nbsp;
        </td>
        <td style="background: white; padding: 10px">
          <center>
            <a href="#top">Top</a>
            --
            <a href="mailto:primetime@danholle.com">Comments</a>
            --
            <a href="../home">Dan's Home Page</a>
          </center>
        </td>
      </tr>
    </table>
  </body>
</html>

            
