package cn.js.fan.module.cms.site;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.ResKeyException;
import com.redmoon.forum.person.UserPropDb;
import com.redmoon.blog.ui.TemplateDb;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.template.TemplateLoader;

/**
 * <p>Title: �û��Զ���ģ��</p>
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
public class SiteUserTemplateDb extends QObjectDb {
    public SiteUserTemplateDb() {
    }

    public boolean init(String code) {
        SiteDb sd = new SiteDb();
        sd = sd.getSiteDb(code);
        SiteTemplateDb td = new SiteTemplateDb();
        td = td.getSiteTemplateDb(sd.getInt("skin"));
        boolean re = false;
        try {
            re = create(new JdbcTemplate(), new Object[] {
                code, td.getString("main_content"), td.getString("home_content"), td.getString("list_content"), td.getString("doc_content")
            });
        }
        catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        }
        return re;
    }

    /**
     * ����ϵͳģ���ؽ��û�ģ��
     * @return boolean
     * @throws ResKeyException
     */
    public boolean resumeAll() throws ResKeyException {
        del();
        // �ؽ�ģ��
        boolean re = init(getString("code"));
        if (re) {
            SiteDb sd = new SiteDb();
            sd = sd.getSiteDb(getString("code"));
            // ˢ�»���
            TemplateLoader.refreshTemplate(SiteUserTemplateDb.
                                           getTemplateCacheKey(
                    sd, SiteTemplateDb.TEMPL_TYPE_MAIN));
            TemplateLoader.refreshTemplate(SiteUserTemplateDb.
                                           getTemplateCacheKey(
                    sd, SiteTemplateDb.TEMPL_TYPE_HOME));
            TemplateLoader.refreshTemplate(SiteUserTemplateDb.
                                           getTemplateCacheKey(
                    sd, SiteTemplateDb.TEMPL_TYPE_LIST));
            TemplateLoader.refreshTemplate(SiteUserTemplateDb.
                                           getTemplateCacheKey(
                    sd, SiteTemplateDb.TEMPL_TYPE_DOC));
        }
        return re;
    }
    /**
     * ����ϵͳģ���ؽ�ģ���еĸ�������
     * @param contentType String main_content,home_content,list_content,doc_content
     * @return boolean
     * @throws ResKeyException
     */
    public boolean resume(String contentType) throws ResKeyException {
        SiteDb sd = new SiteDb();
        sd = sd.getSiteDb(getString("code"));
        SiteTemplateDb td = new SiteTemplateDb();
        td = td.getSiteTemplateDb(sd.getInt("skin"));
        set(contentType, td.getString(contentType));
        boolean re = save();
        if (re) {
            if (contentType.equals("main_content")) {
                TemplateLoader.refreshTemplate(SiteUserTemplateDb.
                                               getTemplateCacheKey(sd,
                        SiteTemplateDb.TEMPL_TYPE_MAIN));
            } else if (contentType.equals("home_content")) {
                TemplateLoader.refreshTemplate(SiteUserTemplateDb.
                                               getTemplateCacheKey(sd,
                        SiteTemplateDb.TEMPL_TYPE_HOME));
            } else if (contentType.equals("list_content")) {
                TemplateLoader.refreshTemplate(SiteUserTemplateDb.
                                               getTemplateCacheKey(sd,
                        SiteTemplateDb.TEMPL_TYPE_LIST));
            } else if (contentType.equals("doc_content")) {
                TemplateLoader.refreshTemplate(SiteUserTemplateDb.
                                               getTemplateCacheKey(sd,
                        SiteTemplateDb.TEMPL_TYPE_DOC));
            }
        }
        return re;
    }

    public SiteUserTemplateDb getSiteUserTemplateDb(String code) {
        SiteUserTemplateDb up = (SiteUserTemplateDb)getQObjectDb(code);
        // ���ǵ���������Ҫ����˴����������û�ע���ʱ���Զ�Ϊ�����User Prop��¼�����ǵ��õ�ʱ�Զ�����
        if (up==null) {
            init(code);
            return (SiteUserTemplateDb)getQObjectDb(code);
        }
        else
            return up;
    }

    /**
     * �������ڻ���ITemplate�ļ�ֵ
     * @param ucd UserConfigDb
     * @param templateType String
     * @return String
     */
    public static String getTemplateCacheKey(SiteDb sd, String templateType) {
        return "cms_site_user_templ_" + templateType + "_" + sd.getString("code");
    }

}
