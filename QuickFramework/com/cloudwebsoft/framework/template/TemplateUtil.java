package com.cloudwebsoft.framework.template;

/**
 * <p>Title: ģ��淶����</p>
 *
 * <p>Description: </p>
 * ��ģ�������б��ǩʱ���б��ǩ��<!--��ʼ����-->����������Щ�༭���У���Fckeditor�У����Զ���ʽ��
 * ���б��ǩ�Ŀ�ʼ����������붥�񣬷���ᵼ�½������������ڱ༭�����ݺ������format�淶������
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class TemplateUtil {
    public TemplateUtil() {
    }

    /**
     * �����ݽ��а���ģ��淶�ĸ�ʽ���������������FCKEditor��ȥ���س���
     * @param content String
     * @return String
     */
    public static String format(String content) {
        content = content.replaceAll("<\\!--", "\n<!--");
	content = content.replaceAll("-->", "-->\n");
        return content;
    }
}
