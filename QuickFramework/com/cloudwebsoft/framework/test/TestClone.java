package com.cloudwebsoft.framework.test;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.template.plugin.PluginMgr;

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
public class TestClone implements Cloneable {
    public Integer i;
    public Long l;
    public String s;
    public Double d;
    public Object o;

    public TestClone() {
        i = new Integer(10);
        l = new Long(11);
        s = "12";
        d = new Double(5.0);
        o = new Integer(6);
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (Exception e) {

        }
        return null;
    }

    public static void main(String[] args) {
        Pattern varNamePat2 = Pattern.compile(
                // "\\$(\\S+)\\.([^\\(]+)(\\((.*?)\\))?", // ��������Ϊ̰��ģʽ����{$cms.include(template/column.htm)}�У��õ���name=cms.include(template/column
                "\\$([A-Z0-9a-z]+)\\.([^\\(]+)(\\((.*?)\\))?",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            String nameStr = "$cM2s.include(template/column.htm)";
        Matcher m = varNamePat2.matcher(nameStr);
        if (m.find()) {
            if (m.groupCount() < 2) {
                throw new IllegalArgumentException(nameStr +
                        " is invalid! varNamePat2 match group count is " +
                        m.groupCount());
            }
            String name = m.group(1);
            String field = m.group(2);
            String props = "";
            if (m.groupCount() >= 4) {
                props = m.group(4);
            }

           System.out.println(
                    "getVarPartByNameString: nameStr=" +
                    nameStr + " name=" + name + " props=" + props);
        }
            if (true)
                return;
        TestClone tc = new TestClone();
        TestClone tc2 = (TestClone)tc.clone();
        System.out.println(tc);
        System.out.println(tc2);
        System.out.println(tc.i + " " + tc.l + " " + tc.s + " " + tc.d + " o=" + tc.o);
        System.out.println(tc2.i + " " + tc2.l + " " + tc2.s + " " + tc2.d + " o=" + tc2.o);

        String str = "[face=����GB2312]dddd[face=����123]ffff[/face]ddd[/face]asdasdf[face=333]333[/face]";
        String patternStr = "\\[face=(.[^\\[]*)\\](.[^\\[]*)\\[\\/face\\]";
        Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(str);
        String content = matcher.replaceAll("<font face=$1>$2</font>");
        System.out.println(content);

         matcher = pattern.matcher(content);

        content = matcher.replaceAll("<font face=$1>$2</font>");
        System.out.println(content);

        str = "a  b c    d   ";
        System.out.println(str);
        str = str.replaceAll(" +", " ");
        System.out.println(str);

        String[] ary = str.split(" ");
        for (int i=0; i<ary.length; i++)
            System.out.println(ary[i] + "]");

        // ����Ƿ��зǷ��ַ���ֻ�������ĺ���ĸ������
        // Pattern pa = Pattern.compile("[^\u4E00-\u9FA5]+");
        String tag = "���� �й�,dd";
        String s = "   abc   d   ��   ";
        Pattern pa = Pattern.compile("[^\u4e00-\u9fa5 \\w]+", Pattern.CANON_EQ);
        Matcher m2 = pa.matcher(tag);
        while (m2.find()) {
            System.out.println("find:" + m2.group());
        }
    }
}
