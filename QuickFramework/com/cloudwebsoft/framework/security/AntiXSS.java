package com.cloudwebsoft.framework.security;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class AntiXSS {
    public static String antiXSS( String html ) { 
    	// Ӧ�Թ�����ʽ��'A"+alert(1295)+"�����˺��Ϊ��'A&quot;+alert(1295)+&quot;
    	// �ᵼ�¹�����ͨ�����������Ľű�<p>1111</p>
    	// html = StringEscapeUtils.escapeHtml4(html);
    	
    	return cn.js.fan.security.AntiXSS.antiXSS(html);
    	
    	// Jsoup Whitelist.none()ֻ�ܹ��˱�ǩ
    	// return Jsoup.clean(html, Whitelist.none());  
    }
    
    public static String clean(String html) {
    	return Jsoup.clean(html, Whitelist.none());  
    }
}
