package cn.js.fan.util;

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import java.util.Set;

public class PropertiesUtil {
    private String fileName;
    private Properties p;
    private FileInputStream in;
    private FileOutputStream out;
    String charset = "gb2312";

    /**
     * ���ݴ������ļ��������ļ�
     * @param fileName String
     */
    public PropertiesUtil(String fileName, String charset) {
        this.fileName = fileName;
        this.charset = charset;
        File file = new File(fileName);
        try {
            in = new FileInputStream(file);
            p = new Properties(charset);
            // �����ļ�
            p.load(in, charset);
            in.close();
        }
        catch (FileNotFoundException e) {
            System.err.println("�����ļ�config.properties�Ҳ�����");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.err.println("��ȡ�����ļ�config.properties����");
            e.printStackTrace();
        }
    }

    public PropertiesUtil(String fileName) {
        this.fileName = fileName;
        File file = new File(fileName);
        try {
            in = new FileInputStream(file);
            p = new Properties(charset);
            // �����ļ�
            p.load(in);
            in.close();
        }
        catch (FileNotFoundException e) {
            System.err.println("�����ļ�config.properties�Ҳ�����");
            e.printStackTrace();
        }
        catch (Exception e) {
            System.err.println("��ȡ�����ļ�config.properties����");
            e.printStackTrace();
        }
    }

    /**
     * �����ļ�һ��Ϊconfig.propertities������ͳһ����webӦ�õĸ�Ŀ¼�¡�
     * @return String
     */
    public static String getConfigFile(HttpServlet hs) {
        return getConfigFile(hs, "config.properties");
    }

    /**
     * ��servlet��ʹ��,ֱ����this��Ϊ����,HttpServlet����
     * ���������ļ����ӵ�ǰwebӦ�õĸ�Ŀ¼���ҳ������ļ�
     * @param hs HttpServlet
     * @param configFileName String�����ļ�����
     * @return String
     */
    public static String getConfigFile(HttpServlet hs, String configFileName) {
        String configFile = "";
        ServletContext sc = hs.getServletContext();
        configFile = sc.getRealPath("/" + configFileName);
        if (configFile == null || configFile.equals("")) {
            configFile = "/" + configFileName;
        }
        return configFile;
    }

    /**
     * jsp����pageContext������
     * @param hs PageContext
     * @param configFileName String �����ļ�����
     * @return String
     */
    public static String getConfigFile(PageContext hs, String configFileName) {
        String configFile = "";
        ServletContext sc = hs.getServletContext();
        configFile = sc.getRealPath("/" + configFileName);
        if (configFile == null || configFile.equals("")) {
            configFile = "/" + configFileName;
        }
        return configFile;
    }

    public Set getKeys() {
        return p.keySet();
    }

    /**
     * ָ�����������ƣ���������ֵ
     * @param itemName String
     * @return String
     */
    public String getValue(String itemName) {
        String str = "";
        try {
            str = new String(p.getProperty(itemName).getBytes("ISO8859_1"), charset);
        }
        catch (Exception e) {
            System.out.println("PropertiesUtil: getValue " + e.getMessage());
        }
        return str;
    }

    /**
     * ָ�����������ƺ�Ĭ��ֵ����������ֵ
     * @param itemName String
     * @param defaultValue String
     * @return String
     */
    public String getValue(String itemName,
                           String defaultValue) {
        return p.getProperty(itemName, defaultValue);
    }

    /**
     * �������������Ƽ���ֵ
     * @param itemName String
     * @param value String
     */
    public void setValue(String itemName, String value) {
        try {
            value = new String(value.getBytes(charset), "ISO8859_1");
        }
        catch (Exception e) {
            System.out.println("PropertiesUtil: setValue " + e.getMessage());
        }
        p.setProperty(itemName, value);
        return;
    }

    /**
     * ���������ļ���ָ���ļ�����̧ͷ����
     * @param fileName String
     * @param description String
     * @throws Exception
     */
    public void saveFile(String fileName, String description) throws Exception {
        try {
            File f = new File(fileName);
            out = new FileOutputStream(f);
            p.store(out, description); // �����ļ�
            out.close();
        }
        catch (IOException ex) {
            throw new Exception
                    ("�޷�����ָ���������ļ�:" + fileName);
        }
    }

    /**
     * ���������ļ���ָ���ļ���
     * @param fileName String
     * @throws Exception
     */
    public void saveFile(String fileName)
            throws Exception {
        saveFile(fileName, "");
    }
}
