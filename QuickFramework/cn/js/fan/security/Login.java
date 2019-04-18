package cn.js.fan.security;

import javax.servlet.*;
import javax.servlet.http.*;
import cn.js.fan.util.*;
import cn.js.fan.web.SkinUtil;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Login {
    static final long delaytime = 60000; // ��ʱʱ��Ϊ60��
    static final int maxfailcount = 3;
    static final long failtimespan = 30000; // �ڰ�����ڵ����������Ϊ3

    public Login() {
    }

    /**
     * ��ʼ����¼ʧ�ܴ���Ϊ0���ڵ�¼������ʹ��,���ڷ������ƽ�
     * @param request
     * @param response
     */
    public static void initlogin(HttpServletRequest request, String prefix) {
        HttpSession session = request.getSession(true);
        String c = (String) session.getAttribute(prefix + "_loginfail_count");
        if (c == null)
            session.setAttribute(prefix + "_loginfail_count", "0");
    }

    public static boolean canlogin(HttpServletRequest request, String prefix) throws
            ErrMsgException {
        HttpSession session = request.getSession(true);
        String strcount = (String) session.getAttribute(prefix +
                "_loginfail_count");
        int count = 0;
        if (strcount == null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_login_invalid")); // �Ƿ���¼����Ϊ�ڵ�¼����ʱδд��session zjpages_loginfailcount��ֵ
        } else {
            try {
                count = Integer.parseInt(strcount);
            } catch (Exception e) {
                throw new ErrMsgException("��¼������");
            }
        }
        if (count < maxfailcount)
            return true;
        long first = 0;
        long last = 0;
        try {
            first = Long.parseLong((String) session.getAttribute(
                    prefix + "_loginfail_first"));
            last = Long.parseLong((String) session.getAttribute(
                    prefix + "_loginfail_last"));
        } catch (NumberFormatException e) {
            throw new ErrMsgException("ʱ��Ƿ���");
        }
        long timespan = last - first;
        if (timespan <= failtimespan) { // �������maxfailcountʱ�����ʱ����С��Ԥ��ֵ�����ɱ���������ȡ��ʩ
            long tspan = System.currentTimeMillis() - last;
            tspan = (delaytime - tspan) / 1000;
            if (tspan > 0) {
                // throw new ErrMsgException("�Բ���������" + failtimespan / 1000 +
                //                          "���ڵ�¼������" + maxfailcount + "�Σ�������ʱ" +
                //                          delaytime / 1000 + "���¼��������" + tspan +
                //                          "��ſ��Ե�¼��");
                String str = SkinUtil.LoadString(request, "err_login_can_not");
                str = str.replaceFirst("\\$s", "" + failtimespan/1000);
                str = str.replaceFirst("\\$c", "" + maxfailcount);
                str = str.replaceFirst("\\$d", "" + delaytime/1000);
                str = str.replaceFirst("\\$t", "" + tspan);
                throw new ErrMsgException(str);
            }
        } else
            session.setAttribute(prefix + "_loginfail_count", "0");
        return true;
    }

    /**
     * ���ݵ�¼�Ƿ�ɹ��޸�session�е���Ӧ�ı���
     * @param request
     * @param response
     * @param count
     */
    public static void afterlogin(HttpServletRequest request,
                                  boolean isloginsuccess, String prefix,
                                  boolean keepsession) throws ErrMsgException {
        HttpSession session = request.getSession(true);
        if (isloginsuccess) {
            if (!keepsession)
                session.invalidate(); //������豣����������session
            return;
        }
        String strcount = (String) session.getAttribute(prefix +
                "_loginfail_count");
        int count = 0;
        if (strcount == null) {
            throw new ErrMsgException("After:" + SkinUtil.LoadString(request, "err_login_invalid")); //�Ƿ���¼����Ϊ�ڵ�¼����ʱδд��session zjpages_loginfailcount��ֵ
        } else {
            try {
                count = Integer.parseInt(strcount);
            } catch (Exception e) {
                throw new ErrMsgException("��¼������");
            }
        }
        count++;
        session.setAttribute(prefix + "_loginfail_count", "" + count);
        long t = System.currentTimeMillis();
        // �õ�¼ʧ�ܵ�һ�ε�ʱ������ε�ʱ��
        if (count == 1) {
            session.setAttribute(prefix + "_loginfail_first", "" + t);
            session.setAttribute(prefix + "_loginfail_last", "" + t);
        } else
            session.setAttribute(prefix + "_loginfail_last",
                                 "" + System.currentTimeMillis());
        long timespan = 0;
        long first = 0, last = 0;
        if (count == 1) {
            // throw new ErrMsgException("����ʧ����" + count + "�Σ���ע�⣺���" +
            //                          failtimespan / 1000 + "���ڴ���" +
            //                          maxfailcount + "����������ʱ" +
            //                          delaytime / 1000 + "���¼��");
            String str = SkinUtil.LoadString(request, "err_login_fail_one");
            str = str.replaceFirst("\\$c", "" + count);
            str = str.replaceFirst("\\$s", "" + failtimespan/1000);
            str = str.replaceFirst("\\$m", "" + maxfailcount);
            str = str.replaceFirst("\\$d", "" + delaytime/1000);
            throw new ErrMsgException(str);
        }
        else
            last = t;
        try {
            first = Long.parseLong((String) session.getAttribute(
                    prefix + "_loginfail_first"));
        } catch (NumberFormatException e) {
            throw new ErrMsgException("ʱ���ʽ��");
        }
        timespan = (last - first) / 1000;
        // throw new ErrMsgException("������" + timespan + "����ʧ����" + count +
        //                          "�Σ���ע�⣺���" + failtimespan / 1000 + "���ڴ���" +
        //                          maxfailcount + "����������ʱ" + delaytime / 1000 +
        //                          "���¼��");
        String str = SkinUtil.LoadString(request, "err_login_fail");
        str = str.replaceFirst("\\$t", "" + timespan);
        str = str.replaceFirst("\\$c", "" + count);
        str = str.replaceFirst("\\$f", "" + failtimespan/1000);
        str = str.replaceFirst("\\$m", "" + maxfailcount);
        str = str.replaceFirst("\\$s", "" + delaytime/1000);
        throw new ErrMsgException(str);
    }
}
