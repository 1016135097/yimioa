package cn.js.fan.util;

import java.util.*;

import org.apache.log4j.Logger;
import cn.js.fan.web.Global;

public class ResBundle {
    // Ĭ���ַ���Ϊ��������
    Logger logger = Logger.getLogger(this.getClass().getName());
    Locale locale;
    ResourceBundle bundle;
    String encode = "";

    public ResBundle(String resName, Locale locale) {
        if (locale == null) {
            try {
                locale = Global.locale;
            } catch (Exception e) {
                logger.error("ResBundle1:" + e.getMessage());
                locale = Locale.getDefault();
            }
        }
        this.locale = locale;

        if (locale.getLanguage().equals("zh")) {
            if (locale.getCountry().equals("TW"))
                this.encode = "BIG5";
            else if (locale.getCountry().equals("CN"))
                this.encode = "gb2312";
        }
        if(locale.getLanguage().equals("en")){
        	if(locale.getCountry().equals("US")){
        		this.encode = "utf-8";
        	}
        }
        this.resName = resName;
        try {
            bundle = ResourceBundle.getBundle(resName, locale);
        } catch (MissingResourceException e) {
            logger.error("ResBundle2:" + e.getMessage());
            // bundle = ResourceBundle.getBundle(resName, Locale.CHINA);
        }
    }

    public String get(String key) {
        String str = bundle.getString(key);
        if (str==null)
            return "";
        if (!encode.equals("")) {
            try {
                // gb2312�����ݿ���ͨ�����·�ʽ������big5ȴ����
                // str = new String(str.getBytes("ISO8859-1"), encode);

                // ����gb2312����Դ�ļ�����Ҫת��utf-8���룬ת�˷����������룬��big5����Դ�ļ���ת��Ϊutf-8����
                // �Σ��ڱ��������Ϊ̨��ʱ��gb2312��Դ�ļ�����ת��Ϊutf8���룬������������Ļ��й�������ʱ��gb2312����Դ�ļ���ת��utf8����
                str = new String(str.getBytes("ISO8859-1"), "utf-8");
            } catch (java.io.UnsupportedEncodingException ex) {
                System.out.println("resName=" + resName + " key=" + key + " locale=" + locale);
                ex.printStackTrace();
            }
        }
        return str;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getResName() {
        return resName;
    }

    private String resName;

}
