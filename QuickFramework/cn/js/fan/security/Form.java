package cn.js.fan.security;

import javax.servlet.*;
import javax.servlet.http.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.RandomSecquenceCreator;
import com.redmoon.kit.util.FileUpload;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Form {
    public static final String TOKEN = "form_token";
    long maxtimespan = 20000; // 20��

    public Form() {
    }

    /**
     * �����µ�token������session��
     * @param request HttpServletRequest
     * @return String
     */
    public static String createNewToken(HttpServletRequest request) {
        String token = RandomSecquenceCreator.getId(20);
        HttpSession session = request.getSession(true);
        session.setAttribute(TOKEN, token);
        return token;
    }

    // ��ֹ���ˣ�ˢ��
    public static String getTokenHideInput(HttpServletRequest request) {
        String token = createNewToken(request);
        return "<input type=\"hidden\" name=\"" + TOKEN + "\" value=\"" + token + "\">";
    }

    public static boolean isTokenValid(HttpServletRequest request) {
        boolean re = false;
        String token = request.getParameter(TOKEN);
        if (token==null) {
            return false;
        }
        HttpSession session = request.getSession(true);
        String token_session = (String)session.getAttribute(TOKEN);
        if (token_session==null) {
            re = false;
        }
        else {
        	if (token_session.equals(token))
	            re = true;
	        else
	            re = false;
        }
        // ����Token��ʹ��ֻ�����ϴ�һ�Σ��������Ժ�ˢ��
        createNewToken(request);

        return re;
    }

    public static boolean isTokenValid(HttpServletRequest request, FileUpload fu) {
        String token = fu.getFieldValue(TOKEN);
        boolean re = false;
        if (token==null) {
            re = false;
        }
        HttpSession session = request.getSession(true);
        String token_session = (String)session.getAttribute(TOKEN);
        if (token_session==null)
            re = false;
        if (token_session.equals(token))
            re = true;
        else
            re = false;
        // ����Token��ʹ��ֻ�����ϴ�һ�Σ��������Ժ�ˢ��
        createNewToken(request);

        return re;
    }

    public boolean cansubmit(HttpServletRequest request, String prefix) throws
            ErrMsgException {
        HttpSession session = request.getSession(true);
        String strt = (String) session.getAttribute(prefix + "_submit_time");
        if (strt == null) { //��һ��submit
            session.setAttribute(prefix + "_submit_time",
                                 "" + System.currentTimeMillis());
            return true;
        }
        long t = 0;
        try {
            t = Long.parseLong(strt);
        } catch (NumberFormatException e) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.cn.js.fan.security.Form", "err_time_format"));
        }
        if (System.currentTimeMillis() - t < maxtimespan) {
            String str = SkinUtil.LoadString(request,
                                             "res.cn.js.fan.security.Form",
                                             "err_too_quick");
            str = StrUtil.format(str, new Object[] {"" + maxtimespan / 1000});
            throw new ErrMsgException(str); // "���ύ���ٶ�̫���ˣ�����"+maxtimespan/1000+"��������ύ��");
        }
        session.setAttribute(prefix + "_submit_time",
                             "" + System.currentTimeMillis());
        return true;
    }

    public static boolean cansubmit(HttpServletRequest request, String prefix,
                                    int maxtimespan) throws ErrMsgException {
        HttpSession session = request.getSession(true);
        String strt = (String) session.getAttribute(prefix + "_submit_time");
        if (strt == null) { //��һ��submit
            session.setAttribute(prefix + "_submit_time",
                                 "" + System.currentTimeMillis());
            return true;
        }
        long t = 0;
        try {
            t = Long.parseLong(strt);
        } catch (NumberFormatException e) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.cn.js.fan.security.Form", "err_time_format"));
        }
        if (System.currentTimeMillis() - t < maxtimespan) {
            String str = SkinUtil.LoadString(request,
                                             "res.cn.js.fan.security.Form",
                                             "err_too_quick");
            str = StrUtil.format(str, new Object[] {"" + maxtimespan / 1000});
            throw new ErrMsgException(str); // "���ύ���ٶ�̫���ˣ�����"+maxtimespan/1000+"��������ύ��");
        }
        session.setAttribute(prefix + "_submit_time",
                             "" + System.currentTimeMillis());
        return true;
    }

}
