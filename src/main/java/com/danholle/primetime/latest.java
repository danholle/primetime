package com.danholle.primetime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *  
 *  Custom tag returning the latest Mersenne exponent found;  used in the title of the web page.
 *
 */
public class latest extends TagSupport {

  public int doStartTag()
      throws JspException  {

    HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
    JspWriter pw = pageContext.getOut();

    try {pw.println(""+quest.getLatest());}
    catch (Exception e) {e.printStackTrace();}

    return SKIP_BODY; // 1
  } // doStartTag

  public int doEndTag() throws JspException {return EVAL_PAGE;} // 6

} // latest
