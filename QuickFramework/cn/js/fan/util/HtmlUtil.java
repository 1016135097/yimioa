package cn.js.fan.util;

import javax.servlet.http.*;

import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import org.htmlparser.*;
import org.htmlparser.filters.*;
import org.htmlparser.nodes.*;
import org.htmlparser.tags.*;
import org.htmlparser.util.*;

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
public class HtmlUtil {
    public static final int MAX_LEN2 = 3000;

    public HtmlUtil() {
    }

    /**
     * ȥ�����ҿո���ַ����Ƿ�Ϊ��
     * @param astr String
     * @return boolean
     */
    public static boolean isTrimEmpty(String astr) {
        if ((null == astr) || (astr.length() == 0)) {
            return true;
        }
        if (isBlank(astr.trim())) {
            return true;
        }
        return false;
    }


    /**
     * �ַ����Ƿ�Ϊ��:null���߳���Ϊ0.
     * @param astr Դ�ַ���.
     * @return boolean
     */
    public static boolean isBlank(String astr) {
        if ((null == astr) || (astr.length() == 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param request HttpServletRequest
     * @param htmlStr String
     * @param len int
     * @param isFormat boolean �Ƿ�����ȡ�õ���ÿ�仰ǰ����϶����ǣ��磺���ڷ��Ͷ��ţ���ӦΪfalse��
     * @return String
     */
    public static String getAbstract(HttpServletRequest request, String htmlStr, int len, boolean isFormat) {
        String content = StrUtil.getLeft(htmlStr, len);

        // ��δ��ɵı�ǩ���룬�������<im��<tab�����ı�ǩ
        int idx1 = content.lastIndexOf('<');
        int idx2 = content.lastIndexOf('>');
        // �����ȡʱ��δȡ�� > ���������ǰȡ��ֱ��ȡ��Ϊֹ
        // System.out.println("MsgUtil.java getAbstract: idx1=" + idx1 + " idx2=" + idx2);
        if ((idx2 == -1 && idx1 >= 0) || (idx1 > idx2)) {
            String ct3 = htmlStr;
            int idx3 = ct3.indexOf('>', idx1);
            if (idx3!=-1) {
                if (idx3 < MAX_LEN2) {
                    content = ct3.substring(0, idx3 + 1);
                }
            }
        }

        // ����ActiveX�������Ԥ����
        idx2 = content.toLowerCase().lastIndexOf("</object>");
        idx1 = content.toLowerCase().lastIndexOf("<object");
        if ((idx2 == -1 && idx1 >= 0) || idx1 > idx2) {
            String ct2 = htmlStr.toLowerCase();
            int idx3 = ct2.indexOf("</object>");
            if (idx3 != -1)
                content += htmlStr.substring(content.length(), content.length() + idx3 + 9);
            else
                content = htmlStr.substring(0, idx1);
        }

        String str = "";
        try {
            Parser myParser;
            NodeList nodeList = null;
            myParser = Parser.createParser(content, "utf-8");
            NodeFilter textFilter = new NodeClassFilter(TextNode.class);
            NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
            NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);
            // ��ʱ������ meta
            // NodeFilter metaFilter = new NodeClassFilter(MetaTag.class);
            OrFilter lastFilter = new OrFilter();
            lastFilter.setPredicates(new NodeFilter[] {textFilter, linkFilter,
                                     imgFilter});
            nodeList = myParser.parse(lastFilter);
            Node[] nodes = nodeList.toNodeArray();
            for (int i = 0; i < nodes.length; i++) {
                Node anode = (Node) nodes[i];
                String line = "";
                if (anode instanceof TextNode) {
                    TextNode textnode = (TextNode) anode;
                    // line = textnode.toPlainTextString().trim();
                    line = textnode.getText();
                } else if (anode instanceof ImageTag) {
                    ImageTag imagenode = (ImageTag) anode;
                    String url = imagenode.getImageURL();
                    String ext = StrUtil.getFileExt(url).toLowerCase();
                    // �����ַ����
                    if (ext.equals("gif") || ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("bmp")) {
                        // System.out.println("MsgUtil.java getAbstract:" + imagenode.toHtml() + " url=" + imagenode.getImageURL());
                        if (imagenode.getImageURL().startsWith("http"))
                            ; // line = "<div align=center>" + imagenode.toHtml() + "</div>";
                        else if (imagenode.getImageURL().startsWith("/")) {
                            ; //line = "<div align=center>" + imagenode.toHtml() + "</div>";
                        }
                        else { // ���·��
                            // line = "<div align=center><img src='" + request.getContextPath() + "/forum/" + imagenode.getImageURL() + "'></div>";
                            url = request.getContextPath() + "/forum/" + imagenode.getImageURL();
                        }
                        line = "<div align=center><a onfocus=this.blur() href=\"" + url + "\" target=_blank><IMG SRC=\"" + url + "\" border=0 alt=" +
                                SkinUtil.LoadString(request,
                                 "res.cn.js.fan.util.StrUtil",
                                    "click_open_win") + " onload=\"javascript:if(this.width>screen.width-333) this.width=screen.width-333\"></a></div><BR>";
                        // System.out.println(line);
                    }
                }
                if (isTrimEmpty(line))
                    continue;
                if (isFormat) {
                    str += "<p>" + line + "</p>";
                }
                else
                    str += " " + line;
            }
        }
        catch (ParserException e) {
            LogUtil.getLog(HtmlUtil.class.getName()).error("getAbstract:" + e.getMessage());
        }
        return str;
    }

    public static String getAbstract(HttpServletRequest request, String htmlStr, int len) {
        return getAbstract(request, htmlStr, len, true);
    }

    /**
     * ��HTML�����л�ȡ�ı�
     * @param content String
     * @return String
     */
    public static String getTextFromHTML(String content) {
        String str = "";
        try {
            Parser myParser;
            NodeList nodeList = null;
            myParser = Parser.createParser(content, "utf-8");
            NodeFilter textFilter = new NodeClassFilter(TextNode.class);
            NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
            NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);
            NodeFilter styleFilter = new NodeClassFilter(StyleTag.class);
            NodeFilter tableFilter = new NodeClassFilter(TableTag.class);
            // ��ʱ������ meta
            // NodeFilter metaFilter = new NodeClassFilter(MetaTag.class);
            OrFilter lastFilter = new OrFilter();
            lastFilter.setPredicates(new NodeFilter[] {linkFilter,
                                     imgFilter, styleFilter, textFilter, tableFilter});
            nodeList = myParser.parse(lastFilter);
            Node[] nodes = nodeList.toNodeArray();
            for (int i = 0; i < nodes.length; i++) {
                Node anode = (Node) nodes[i];
                String line = "";
                if (anode instanceof TextNode) {
                    TextNode textnode = (TextNode) anode;
                    // line = textnode.toPlainTextString().trim();

                    if (textnode.getParent() != null) {
                        if (!(textnode.getParent() instanceof StyleTag)) {
                            line = textnode.getText();
                        }
                        // System.out.println(HtmlUtil.class + " line1=" + line);
                    }
                    else {
                        line = textnode.getText();
                    }
                }
                else if (anode instanceof TableTag) {
                    TableTag tableTag = (TableTag) anode;
                    line = tableTag.toPlainTextString(); //�õ���������
                }
                /*
                else if (anode instanceof StyleTag) {
                    System.out.println(HtmlUtil.class + " " + ((StyleTag)anode).toPlainTextString());
                }
                */

                str += line;
            }
        }
        catch (ParserException e) {
            e.printStackTrace();
        }
        return str;
    }
}
