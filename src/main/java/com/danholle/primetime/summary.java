package com.danholle.primetime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;


/**
 *  Custom tag which displays a summary of current status, currently 4 lines shown
 *  in the upper right corner of the results page.
 */
public class summary extends TagSupport {

  public int doStartTag() throws JspException {

    HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
    JspWriter pw = pageContext.getOut();

    try {pw.println(quest.summaryhtml());}
    catch (Exception e) {e.printStackTrace();}

    return 1;

  } // doStartTag


  public int doEndTag() throws JspException {
    return 6;
  } // doEndTag

} // summary
