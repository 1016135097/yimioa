package cn.js.fan.cache.jcs;

import cn.js.fan.web.Global;
import org.apache.log4j.Logger;

public abstract class AbstractRMCacheMgr implements ICacheMgr {
    public Logger logger;
    public RMCache rmCache = RMCache.getInstance();
    public static boolean isRegisted = false;
    public String connname = "";
    public String cachePrix = "Abstract";

    public AbstractRMCacheMgr() {
        init();
    }

    public abstract void initLogger();
    public abstract void initCachePrix();

    public void init() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("AbstractRMCacheMgr:connname is empty.");
        initLogger();
        initCachePrix();
    }

    /**
     * regist in RMCache
     */
    public void regist() {
        // ���಻��Ҫ�Ǽ�
        /*
        if (!isRegisted) {
            rmCache.regist(this);
            isRegisted = true;
        }
        */
    }

    /**
     * ��ʱˢ�»���
     */
    public void timer() {

    }
}
