package cn.js.fan.module.cms.site;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.module.cms.Directory;
import cn.js.fan.module.cms.Leaf;
import cn.js.fan.module.cms.LeafPriv;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.*;
import com.redmoon.kit.util.*;

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
public class SiteMgr {
    FileUpload fu;

    public SiteMgr() {
    }

    public FileUpload getFileUpload() {
        return fu;
    }

    public boolean create(ServletContext application,
                          HttpServletRequest request) throws
            ErrMsgException {
        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        fu = new FileUpload();
        // fu.setValidExtname(new String[] {"wmv", "mpg", "rmvb", "rm", "avi"});
        int ret = -1;
        try {
            ret = fu.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }
        if (ret != FileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fu.getErrMessage(request));
        }

        boolean re = false;

        SiteDb sd = new SiteDb();
        String formCode = "cms_site_create";
        ParamConfig pc = new ParamConfig(sd.getTable().
                                         getFormValidatorFile());
        ParamChecker pck = new ParamChecker(request, fu);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // ���onError=exit������׳��쳣
            throw new ErrMsgException(e.getMessage());
        }

        // �������Ƿ��Ѵ���
        Leaf lf = new Leaf();
        lf = lf.getLeaf(pck.getString("code"));
        if (lf != null) {
            throw new ErrMsgException("�������Ѵ��ڣ�");
        }

        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);

        pck.setValue("owner", "owner", userName);

        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
        if (cfg.getBooleanProperty("cms.site_apply_need_check")) {
            pck.setValue("site_status", "site_status", new Integer(SiteDb.STATUS_NOT_CHECKED));
        }
        else {
            pck.setValue("site_status", "site_status", new Integer(SiteDb.STATUS_OPEN));
        }

        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = sd.create(jt, pck);
            if (re) {
                UserDb user = new UserDb();
                user = user.getUser(userName);
                Directory.initLeafOfSubsite(pck.getString("code"), pck.getString("name"), pck.getString("kind"), user);
            }
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean save(ServletContext application, HttpServletRequest request
            ) throws
            ErrMsgException {

        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        fu = new FileUpload();
        // fu.setValidExtname(new String[] {"wmv", "mpg", "rmvb", "rm", "avi"});

        int ret = -1;
        try {
            ret = fu.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }

        if (ret != FileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fu.getErrMessage(request));
        }

        SiteDb sd = new SiteDb();
        String formCode = "cms_site_save";

        ParamConfig pc = new ParamConfig(sd.getTable().
                                         getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request, fu);

        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // ���onError=exit������׳��쳣
            throw new ErrMsgException(e.getMessage());
        }

        boolean re = false;

        String code = pck.getString("code");
        sd = (SiteDb) sd.getQObjectDb(code);

        String kind = pck.getString("kind");
        if (kind.equals("not")) {
        	throw new ErrMsgException("��ѡ������еĶ���Ŀ¼!");
        }
        String oldKind = sd.getString("kind");
        if (!oldKind.equals(kind) && !kind.equals("")) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(sd.getString("code"));
            lf.update(kind);
        }

        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = sd.save(jt, pck);

            if (re) {
                // �������ԱȨ��
            	String managerNames = StrUtil.getNullStr(fu.getFieldValue("managerNames"));
                if (!managerNames.equals("")) {
                	String[] ary = StrUtil.split(managerNames, ",");
                	int len = ary.length;
                	UserMgr um = new UserMgr();
                	SiteManagerDb smd = new SiteManagerDb();
                	// ɾ��ԭ���Ĺ���Ա
                	smd.delManagersOfSite(sd.getString("code"));
                	// ���汾���õĹ���Ա
                	for (int i=0; i<len; i++) {
                		UserDb ud = um.getUserDbByNick(ary[i]);
                		if (ud!=null) {
                			smd.create(jt, new Object[]{sd.getString("code"), ud.getName()});
                        	// ���ù���Ա������վ����ڵ��Ȩ��
                            LeafPriv lp = new LeafPriv(sd.getString("code"));
                            lp.setAppend(1);
                            lp.setModify(1);
                            lp.setDel(1);
                            lp.setSee(1);
                            lp.setExamine(1); // �������Ȩ�޵���Ա��Ϊվ�����Ա
                            lp.add(ud.getNick(), LeafPriv.TYPE_USER);                     		
                		}
                	}
                	
               
                }
            }
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }

        return re;
    }
    
    public boolean del(String code) throws ResKeyException {
        SiteDb sd = new SiteDb();
        sd = sd.getSiteDb(code);
        return sd.del();
    }
}
