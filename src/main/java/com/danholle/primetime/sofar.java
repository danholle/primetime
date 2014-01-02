package com.danholle.primetime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 *  Custom tag showing the current status of a {@link quest} in detail, currently displayed as several
 *  tables on the right at the top of the results page.
 *
 */
public class sofar extends TagSupport
{

    public sofar()
    {
    }

    public int doStartTag()
        throws JspException
    {
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        JspWriter pw = pageContext.getOut();
        try
        {
            pw.println(quest.showhtml());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return 1;
    }

    public int doEndTag()
        throws JspException
    {
        return 6;
    }
}
