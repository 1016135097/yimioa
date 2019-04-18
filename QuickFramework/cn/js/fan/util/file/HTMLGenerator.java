package cn.js.fan.util.file;

import java.io.*;
import java.util.Calendar;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.web.Global;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class HTMLGenerator {
    private String visualPath;
    public HTMLGenerator() {
    }

    public void exec(HttpServletRequest request,
                        HttpServletResponse response) throws
            ServletException, IOException {
        // http://localhost:8080/cwbbs/HTMLGenerator?fileName=doc_show&p0=id&v0=1
        String url = "";
        String name = "";

        String file_name = request.getParameter("fileName"); // ��Ҫ���ʵ�jsp�ļ�,��index.jsp
        // ����������servletʱ�Ӳ���.��http://localhost/toHtml?file_name=index
        String p0 = ParamUtil.get(request, "p0"); // dir_code����id
        String v0 = ParamUtil.get(request, "v0");
        String p1 = ParamUtil.get(request, "p1");
        String v1 = ParamUtil.get(request, "v1");

        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(cal.YEAR));
        String month = "" + (cal.get(cal.MONTH) + 1);

        String filepath = "article/" + year + "/" + month;
        url = "/" + file_name + ".jsp?" + p0 + "=" + v0;
        if (!p1.equals(""))
            url = url + "&" + p1 + "=" + v1;

        // url = "/" + file_name + ".jsp"; // ����Ҫ����HTML��jsp�ļ�,��//http://localhost/index.jsp��ִ�н��.
        String realPath = Global.getRealPath();
        name = v0 + ".htm"; // �������ɵ�html�ļ���,��index.htm.
        visualPath = filepath + "/" + name;
        String fullPath = realPath + visualPath; // ����html������·��

        // System.out.println(getClass() + " url=" + url);

        RequestDispatcher rd = request.getRequestDispatcher(url);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        final ServletOutputStream stream = new ServletOutputStream() {
            public void write(byte[] data, int offset, int length) {
                os.write(data, offset, length);
            }

            public void write(int b) throws IOException {
                os.write(b);
            }
        };

        final PrintWriter pw = new PrintWriter(new OutputStreamWriter(os,
                "utf-8"));

        HttpServletResponse rep = new HttpServletResponseWrapper(response) {
            public ServletOutputStream getOutputStream() {
                return stream;
            }

            public PrintWriter getWriter() {
                return pw;
            }
        };

        rd.include(request, rep);

        pw.flush();

        FileOutputStream fos = new FileOutputStream(fullPath); // ��jsp���������д��ָ��·����htm�ļ���
        os.writeTo(fos);
        fos.close();

        response.sendRedirect(visualPath); // ��д��Ϻ�ת��htmҳ��
    }

    public void setVisualPath(String visualPath) {
        this.visualPath = visualPath;
    }

    public String getVisualPath() {
        return visualPath;
    }
}
