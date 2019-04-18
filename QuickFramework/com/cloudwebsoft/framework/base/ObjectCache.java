package com.cloudwebsoft.framework.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import cn.js.fan.cache.jcs.ICacheMgr;
import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.KeyUnit;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.resource.Constant;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.Connection;
import org.apache.log4j.Logger;

public class ObjectCache implements ICacheMgr, java.io.Serializable {
    public String group;
    public String COUNT_GROUP_NAME;

    public transient Logger logger;
    public transient RMCache rmCache;
    public String connname = "";

    public ObjectDb objectDb;

    public ObjectCache() {
        init();
        regist();
    }

    public ObjectCache(ObjectDb obj) {
        this.objectDb = obj;
        init();
        regist();
    }

    public void renew() {
        if (logger==null)
            logger = Logger.getLogger(this.getClass().getName());
        if (rmCache==null)
            rmCache = RMCache.getInstance();
    }

    public void init() {
        logger = Logger.getLogger(this.getClass().getName());
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info(Constant.DB_NAME_NOT_FOUND);
        rmCache = RMCache.getInstance();
        setGroup();
        setGroupCount();
    }

    public void setGroup() {
        group = this.getClass().getName();
    }

    public void setGroupCount() {
        this.COUNT_GROUP_NAME = group + ".Count";
    }

    /**
     * ��ʱˢ�»���
     */
    public void timer() {
/*      // ˢ��ȫ�ļ���
        curFulltextLife--;
        if (curFulltextLife<=0) {
            refreshFulltext();
            curFulltextLife = FULLTEXTMAXLIFE;
        }
*/
    }

    /**
     * regist in RMCache
     */
    public void regist() {
/*        if (!isRegisted) {
            rmCache.regist(this);
            isRegisted = true;
        }
 */
    }

    public void setObjectDb(ObjectDb objectDb) {
        this.objectDb = objectDb;
    }

    public void setObjCachable(boolean objCachable) {
        this.objCachable = objCachable;
    }

    public void setListCachable(boolean listCachable) {
        this.listCachable = listCachable;
    }

    public void refreshCreate() {
        refreshCreate("");
    }

    public void refreshCreate(String groupName) {
        if (!listCachable)
            return;
        refreshList(groupName);
    }

    public void refreshList() {
        refreshList("");
    }

    public void refreshList(String groupName) {
        if (!listCachable)
            return;
        try {
            // �����޸�OA�е���ģ��ʱ������������û�Ϊinvalid���������û�ְλʱ����Ϊuser_sel.jsp��ʹ����getObjects������saveʱ����δˢ���б�������Ϊδˢ��
            // �����Ӵ˷���
            rmCache.invalidateGroup(COUNT_GROUP_NAME + groupName);
            rmCache.invalidateGroup(group + groupName);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void refreshSave(PrimaryKey pk) {
        if (objCachable)
            removeFromCache(pk);
    }

    public void refreshDel(PrimaryKey pk) {
        refreshDel(pk, "");
    }

    public void refreshDel(PrimaryKey pk, String groupName) {
        try {
            if (objCachable)
                removeFromCache(pk);
            if (listCachable)
                refreshList(groupName);
        }
        catch (Exception e) {
            logger.error("refreshDel:" + e.getMessage());
        }
    }

    /**
     * ÿ���ڵ�������Cache��һ�Ǳ�����һ�������ڴ洢�亢�ӽ���cache
     * @param code String
     */
    public void removeFromCache(PrimaryKey pk) {
        if (objCachable) {
            try {
                rmCache.remove(pk.getValue(), group);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    public IObjectDb getObjectDb(PrimaryKey pk) {
        IObjectDb obj = null;
        if (objCachable) {
            try {
                obj = (ObjectDb) rmCache.getFromGroup(pk.getValue(), group);
            } catch (Exception e) {
                logger.error("getObjectDb:" + e.getMessage());
            }

            // logger.info("obj=" + obj + " pk=" + pk.getValue() + " group=" + group);
            if (obj == null) {
                obj = objectDb.getObjectRaw(pk);
                // ���û������Ͳ��ܷ��뻺�棬������뻺��������������������ˣ�
                // ����SweetUserDb���Ͳ����ٴ����ݿ��л�ȡ���ͻᱻ��Ϊuserδ����SweetUserDb��Ӧ�ı���
                if (obj != null && obj.isLoaded()) {
                    try {
                        rmCache.putInGroup(pk.getValue(), group, obj);
                    } catch (Exception e) {
                        logger.error("getObjectDb1:" + e.getMessage());
                    }
                }
            } else {
                // logger.info("logger=" + obj.logger);
                obj.renew();
                // obj.logger.info("yes");
            }
        }
        else {
            obj = objectDb.getObjectRaw(pk);
        }
        return obj;
    }

    public int getObjectCount(String sql) {
        return getObjectCount(sql, "");
    }

    /**
     * ��ȡ��¼��Ŀ
     * @param sql String
     * @return int -1 ��ʾsql��䲻�Ϸ�
     */
    public int getObjectCount(String sql, String groupName) {
        //����sql���ó�����������sql��ѯ���
        String query = cn.js.fan.db.SQLFilter.getCountSql(sql);
        if (!SecurityUtil.isValidSql(query))
            return -1;
        Integer count = null;
        if (listCachable) {
            try {
                count = (Integer) rmCache.getFromGroup(query, COUNT_GROUP_NAME+groupName);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            // If already in cache, return the count.
            if (count != null) {
                return count.intValue();
            }
        }
        // Otherwise, we have to load the count from the db.
        int docCount = objectDb.getObjectCountRaw(query);
        // Add the count to cache
        if (listCachable) {
            try {
                rmCache.putInGroup(query, COUNT_GROUP_NAME+groupName,
                                   new Integer(docCount));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return docCount;
    }

    public Object[] getObjectBlock(String sql, String groupName, int startIndex) {
        // First, discover what block number the results will be in.
        int blockSize = objectDb.getBlockSize();
        int blockID = startIndex / blockSize;
        int blockStart = blockID * blockSize;

        PrimaryKey primaryKey = objectDb.getPrimaryKey();
        // ȡ�ø��������Ĳ�ѯ���
        // String pk = primaryKey.getName();
        // String query = "select " + pk + " " + SQLFilter.getFromSql(sql); // ��Ϊ���ϲ�ѯʱ���˾��е�pk��������⣬��Ϊȱ�ٱ�ı�����Ϊǰ׺ 2006.6.9
        // String query = "select " + objectDb.getTableName() + "." + pk + " " + SQLFilter.getFromSql(sql); // �ӱ�����Ϊǰ׺��oracle��Ҳ����
        String query = sql;

        // �������õ�key
        String key = query + blockID;

        Object[] objArray = null;
        // ���ʹ���б���
        if (listCachable) {
            try {
                objArray = (Object[]) rmCache.getFromGroup(key,
                        group + groupName);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            // If already in cache, return the block.
            if (objArray != null) {
                /**
                 * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
                 * the case, it means two things:
                 *  1) We're at the end boundary of all the results.
                 *  2) If the start index is greater than the length of the current
                 *     block, than there aren't really any results to return.
                 */
                Object[] objkeys = objArray;
                // ��startIndex����ʱ
                if (startIndex >= blockStart + objkeys.length) {
                    // Return an empty array
                    return ObjectDb.EMPTY_BLOCK;
                } else {
                    return objkeys;
                }
            }
        }
        // Otherwise, we have to load up the block from the database.

        Vector block = new Vector();
        ResultSet rs = null;
        Connection conn = new Connection(connname);
        try {
            // Set the maxium number of rows to end at the end of this block.
            conn.setMaxRows(blockSize * (blockID + 1));
            rs = conn.executeQuery(query);
            // System.out.println(getClass() + " sql=" + sql + " blockStart=" + blockStart);
            //logger.info("query=" + query);
            // Grab THREAD_BLOCK_ROWS rows at a time.
            conn.setFetchSize(blockSize);
            // Many JDBC drivers don't implement scrollable cursors the real
            // way, but instead load all results into memory. Looping through
            // the results ourselves is more efficient.
            for (int i = 0; i < blockStart; i++) {
                rs.next();
            }
            // Keep reading results until the result set is exaughsted or
            // we come to the end of the block.
            int count = 0;
            while (rs.next() && count < blockSize) {
                // System.out.println(getClass() + " sql=" + sql + " count=" + count);
                // ������Ǹ�������
                if (primaryKey.getKeyCount() == 1) {
                    if (primaryKey.getType() == primaryKey.TYPE_INT)
                        block.addElement(new Integer(rs.getInt(1)));
                    else if (primaryKey.getType() == primaryKey.TYPE_STRING)
                        block.addElement(rs.getString(1));
                    else if (primaryKey.getType() == primaryKey.TYPE_LONG)
                        block.addElement(new Long(rs.getLong(1)));
                    else if (primaryKey.getType() == primaryKey.TYPE_DATE)
                        block.addElement(new java.util.Date(rs.getTimestamp(1).getTime()));
                } else if (primaryKey.getType() == primaryKey.TYPE_COMPOUND) { // ����Ǹ�������
                    HashMap keys = ((PrimaryKey) primaryKey.clone()).getKeys();
                    Iterator ir = keys.keySet().iterator();
                    while (ir.hasNext()) {
                        String keyName = (String) ir.next();
                        KeyUnit ku = (KeyUnit) keys.get(keyName);
                        if (ku.getType() == primaryKey.TYPE_INT) {
                            ku.setValue(new Integer(rs.getInt(ku.getOrders() + 1)));
                        } else if (ku.getType() == primaryKey.TYPE_LONG) {
                            ku.setValue(new Long(rs.getLong(ku.getOrders() + 1)));
                        }
                        else if (ku.getType() == PrimaryKey.TYPE_DATE) {
                            ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
                        }
                        else {
                            ku.setValue(rs.getString(ku.getOrders() + 1));
                        }
                    }
                    // ��©���¾䣬����2005-8-22
                    block.addElement(keys);
                }
                count++;
            }
        } catch (SQLException sqle) {
            logger.error("getObjectBlock:" + sqle.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        int len = block.size();
        Object[] objkeys = new Object[len];
        for (int i = 0; i < len; i++) {
            objkeys[i] = block.elementAt(i);
        }
        // ��block�������棬lenΪ0ʱҲ���������棬��Ϊ����������ݲ����˱��У�����ز���Ӧˢ�»��棬�˴�����Ҳ�ܵõ�����
        if (listCachable) {
            try {
                // System.out.println(getClass() + " getObjectBlock:" + group + groupName + " objkeys.length=" + objkeys.length);

                rmCache.putInGroup(key, group + groupName, objkeys);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        /**
         * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
         * the case, it means two things:
         *  1) We're at the end boundary of all the results.
         *  2) If the start index is greater than the length of the current
         *     block, than there aren't really any results to return.
         */
        if (startIndex >= blockStart + objkeys.length) {
            // Return an empty array
            return ObjectDb.EMPTY_BLOCK;
        } else {
            return objkeys;
        }
    }

    public boolean isObjCachable() {
        return objCachable;
    }

    public boolean isListCachable() {
        return listCachable;
    }

    public boolean objCachable = true;
    public boolean listCachable = true;

}


