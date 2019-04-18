package cn.js.fan.module.cms.site;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.base.QObjectBlockIterator;

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
public class SiteTemplateDb extends QObjectDb {
    // public static final String basePath = "upfile/cms/template";

    // ��Ϊmain��subģ����Ҫ����ΪITemplate����ͨ��ģ��ֻ���滻��������������Բ������ó���
    public static String TEMPL_TYPE_MAIN = "main"; // ��ģ��
    public static String TEMPL_TYPE_HOME = "home"; // ��ҳģ��
    public static String TEMPL_TYPE_LIST = "list"; // �б�ҳģ��
    public static String TEMPL_TYPE_DOC = "doc"; // ����ҳģ��

    public SiteTemplateDb() {
    }

    public SiteTemplateDb getSiteTemplateDb(int id) {
        return (SiteTemplateDb)getQObjectDb(new Integer(id));
    }

    /**
     * ���ɵ�����ģ���ַ�������TemplateLoader��ʼ��ģ��ʱ���乹�캯���д���Ĳ���cacheKey
     * @return String
     */
    public String getCacheKey(String templateType) {
        return "cms_site_sys_templ_" + templateType + "_" + getInt("id");
    }

    /**
     * ����˳��ţ�����Ϊ��һ��ΪĬ��ģ��
     * @return TemplateDb
     */
    public SiteTemplateDb getDefaultSiteTemplateDb() {
        String sql = "select id from " + table.getName() + " order by orders";
        QObjectBlockIterator qbi = (QObjectBlockIterator)getQObjects(sql, 0, 1);
        if (qbi.hasNext()) {
            return (SiteTemplateDb)qbi.next();
        }
        return null;
    }

}
