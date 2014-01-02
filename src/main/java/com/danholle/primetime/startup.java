package com.danholle.primetime;

import java.io.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 *  
 *  Startup servlet which kicks off the Mersenne search in the background.
 *
 *  This servlet never actually services any user requests.  It's just here to
 *  fire off a {@link quest} for Mersenne primes.
 *
 */
public final class startup extends HttpServlet {

  static quest q;



  /**
   *  Start Mersenne search as a separate, low-priority thread.
   */
  public void init(ServletConfig cfg)
      throws ServletException   {
    // TODO get log name from web.xml
    q=new quest();
  } // init



  /**
   *
   *  Get the quest we're currently running.
   *
   *  @return The quest we're currently running.
   *
   */
  public static quest getQuest() {return q;}



  /**
   *
   *  Handle any pesky user who tries to access this servlet directly.
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
    response.setContentType("text/html");
    PrintWriter writer = response.getWriter();
    writer.println("<html><body>Go away</body></html>");
  } // doGet


} // startup
