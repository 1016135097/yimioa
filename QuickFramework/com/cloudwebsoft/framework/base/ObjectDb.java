package com.cloudwebsoft.framework.base;

import org.apache.log4j.Logger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.ListResult;
import cn.js.fan.web.Global;
import cn.js.fan.resource.Constant;
import cn.js.fan.db.KeyUnit;
import cn.js.fan.db.SQLFilter;

public abstract class ObjectDb implements IObjectDb {
    public static final PrimaryKey[] EMPTY_BLOCK = new PrimaryKey[0];

    public String connname = "";
    public transient Logger logger = null;
    public String QUERY_LOAD;
    public String QUERY_DEL;
    public String QUERY_SAVE;
    public String QUERY_CREATE;
    public String QUERY_LIST;

    public boolean isInitFromConfigDB = true;

    protected String tableName = "";

    public PrimaryKey primaryKey;
    public ObjectCache objectCache;

    public ObjectDb() {
        init();
    }

    public void init() {
        logger = Logger.getLogger(this.getClass().getName());
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            logger.info(Constant.DB_NAME_NOT_FOUND);
        }

        initDB();

        initFromConfigDB();
    }

    /**
     * ���ӻ�����ȡ�������Գ�ʼ��transient�ı���
     * @return Logger
     */
    public void renew() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        if (objectCache != null) {
            objectCache.renew();
        }
    }

    public void initDB() {
        isInitFromConfigDB = false;
    }

    public void initFromConfigDB() {
        if (!isInitFromConfigDB) {
            return;
        }
        DBConfig dc = new DBConfig();
        DBTable dt = dc.getDBTable(this.getClass().getName());
        if (dt == null) {
            logger.info(this +" cann't find table defination in config file.");
            return;
        }
        this.tableName = dt.getName();
        this.primaryKey = (PrimaryKey) dt.getPrimaryKey().clone();

        this.QUERY_CREATE = dt.getQueryCreate();
        this.QUERY_DEL = dt.getQueryDel();
        this.QUERY_LIST = dt.getQueryList();
        this.QUERY_LOAD = dt.getQueryLoad();
        this.QUERY_SAVE = dt.getQuerySave();
        this.objectCache = dt.getObjectCache(this);

        this.objectCache.setObjCachable(dt.isObjCachable());
        this.objectCache.setListCachable(dt.isListCachable());
    }

    /**
     * �����ݿ���ȡ����¼��
     * @param query String �Ѿ���getCountSqlת��
     * @return int -1 ��ʾsql��䲻�Ϸ�
     */
    public int getObjectCountRaw(String query) {
        // ����sql���ó�����������sql��ѯ���
        // Otherwise, we have to load the count from the db.
        int docCount = 0;
        Connection conn = new Connection(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(query);
            if (rs.next()) {
                docCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        return docCount;
    }

    /**
     *
     * @param sql String
     * @param startIndex int
     * @return Object[] ��ŵ��Ƕ�Ӧ��������ֵ
     */
    // abstract public Object[] getObjectBlock(String sql, int startIndex);
    public Object[] getObjectBlock(String query, String groupKey,
                                   int startIndex) {
        return objectCache.getObjectBlock(query, groupKey, startIndex);
    }

    public IObjectDb getObjectDb(Object primaryKeyValue) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return objectCache.getObjectDb(pk);
    }

    /*
    public IObjectDb getObjectDb(Object primaryKeyValue) {
        primaryKey.setValue(primaryKeyValue);
        return objectCache.getObjectDb(primaryKey);
    }
    */

    public ObjectBlockIterator getObjects(String query, String groupKey,
                                          int startIndex,
                                          int endIndex) {
        // ����ȡ�õ�infoBlock�е�Ԫ�ص�˳���С��endIndex
        Object[] blockValues = getObjectBlock(query, groupKey, startIndex);
        // for (int i=0; i<blockValues.length; i++)
        //     logger.info("getObjects i=" + i + " " + blockValues[i]);
        // System.out.println(getClass() + " getObjects:" + groupKey + " blockValues.length=" + blockValues.length);
        return new ObjectBlockIterator(this, blockValues, query, groupKey,
                                       startIndex, endIndex);
    }

    public ObjectBlockIterator getObjects(String query,
                                          int startIndex,
                                          int endIndex) {
        return getObjects(query, "", startIndex, endIndex);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    abstract public boolean create(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException;

    abstract public void load(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException;

    abstract public boolean save(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException;

    abstract public boolean del(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException;

    /**
     * �����ݿ���ȡ�ö���
     * @param objKey Object
     * @return Object
     */
    abstract public IObjectDb getObjectRaw(PrimaryKey pk);

    public int getObjectCount(String sql) {
        return objectCache.getObjectCount(sql, "");
    }

    public int getObjectCount(String sql, String groupName) {
        return objectCache.getObjectCount(sql, groupName);
    }

    public boolean loaded = false;

    public Vector list() {
        return list(QUERY_LIST);
    }

    /**
     * ȫ���ļ�¼�б�����¼����ʱ������ʹ�ñ����������г��������ӣ�������¼�ܶ�ʱ������ʹ��
     * @param QUERY_LIST String
     * @return Vector
     */
    public Vector list(String QUERY_LIST) {
        ResultSet rs = null;
        int total = 0;
        Vector result = new Vector();
        Connection conn = new Connection(connname);
        try {
            // ȡ���ܼ�¼����
            String countsql = SQLFilter.getCountSql(QUERY_LIST);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            conn.prepareStatement(QUERY_LIST);
            if (total != 0) {
                // sets the limit of the maximum number of rows in a ResultSet object
                conn.setMaxRows(total); // ���������ڴ��ʹ��
            }
            rs = conn.executePreQuery();
            if (rs == null) {
                return result;
            } else {
                // defines the number of rows that will be read from the database when the ResultSet needs more rows
                rs.setFetchSize(total); // rsһ�δ�POOL������ȡ�ļ�¼��
                if (rs.absolute(1) == false) {
                    return result;
                }
                do {
                    if (primaryKey.getType() == PrimaryKey.TYPE_INT) {
                        result.addElement(getObjectDb(new Integer(rs.getInt(1))));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_STRING) {
                        result.addElement(getObjectDb(rs.getString(1)));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_LONG) {
                        result.addElement(getObjectDb(new Long(rs.getLong(1))));
                    }
                    else if (primaryKey.getType() == primaryKey.TYPE_DATE)
                        result.addElement(getObjectDb(new java.util.Date(rs.getTimestamp(1).getTime())));
                    else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
                        HashMap keys = ((PrimaryKey) primaryKey.clone()).
                                       getKeys();
                        Iterator ir = keys.keySet().iterator();
                        while (ir.hasNext()) {
                            String keyName = (String) ir.next();
                            KeyUnit ku = (KeyUnit) keys.get(keyName);
                            if (ku.getType() == primaryKey.TYPE_INT) {
                                ku.setValue(new Integer(rs.getInt(ku.getOrders() +
                                        1)));
                            } else if (ku.getType() == primaryKey.TYPE_LONG) {
                                ku.setValue(new Long(rs.getLong(ku.getOrders() +
                                        1)));
                            }
                            else if (ku.getType() == PrimaryKey.TYPE_DATE) {
                                ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
                            }
                            else {
                                ku.setValue(rs.getString(ku.getOrders() + 1));
                            }
                        }
                        result.addElement(getObjectDb(keys));
                    }
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error("list: " + e.getMessage());
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
        return result;
    }

    public Vector list(int start, int end) {
        return list(QUERY_LIST, start, end);
    }

    /**
     * ������Hiebernate�е�list���ӻ�����ȡ�����󣬵�������������Ϊ�ܼ�¼����Ҳ��ȡ�Ի���
     * ȡ�õļ�¼��ResultSet�е�����Ϊ start+1 ~ end+1���ܹ�Ϊend-start+1��
     * @param sql String
     * @param start int ��0��ʼ����
     * @param end int ���ܹ�ȡ��end-start+1������ʱ��list�����һ����¼��ResultSet�е�����Ϊend+1
     * @return Vector
     */
    public Vector list(String sql, int start, int end) {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        Connection conn = new Connection(connname);
        try {
            // ȡ���ܼ�¼����
            String countsql = SQLFilter.getCountSql(sql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            conn.prepareStatement(sql);
            if (total != 0) {
                // sets the limit of the maximum number of rows in a ResultSet object
                conn.setMaxRows(end + 1); // ���������ڴ��ʹ��
            }
            rs = conn.executePreQuery();
            if (rs == null) {
                return result;
            } else {
                // defines the number of rows that will be read from the database when the ResultSet needs more rows
                int count = end - start + 1;
                rs.setFetchSize(count); // rsһ�δ�POOL������ȡ�ļ�¼��
                if (rs.absolute(start + 1) == false) {
                    return result;
                }

                int k = 0;
                do {
                    if (primaryKey.getType() == PrimaryKey.TYPE_INT) {
                        result.addElement(getObjectDb(new Integer(rs.getInt(1))));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_STRING) {
                        result.addElement(getObjectDb(rs.getString(1)));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_LONG) {
                        result.addElement(getObjectDb(new Long(rs.getLong(1))));
                    }
                    else if (primaryKey.getType() == primaryKey.TYPE_DATE)
                        result.addElement(getObjectDb(new java.util.Date(rs.getTimestamp(1).getTime())));
                    else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
                        HashMap keys = ((PrimaryKey) primaryKey.clone()).
                                       getKeys();
                        Iterator ir = keys.keySet().iterator();
                        while (ir.hasNext()) {
                            String keyName = (String) ir.next();
                            KeyUnit ku = (KeyUnit) keys.get(keyName);
                            if (ku.getType() == primaryKey.TYPE_INT) {
                                ku.setValue(new Integer(rs.getInt(ku.getOrders() +
                                        1)));
                            } else if (ku.getType() == primaryKey.TYPE_LONG) {
                                ku.setValue(new Long(rs.getLong(ku.getOrders() +
                                        1)));
                            }
                            else if (ku.getType() == PrimaryKey.TYPE_DATE) {
                                ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
                            }
                            else {
                                ku.setValue(rs.getString(ku.getOrders() + 1));
                            }
                        }
                        result.addElement(getObjectDb(keys));
                    }
                    k++;
                    //if (k>=count)
                    //    break;
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error("list: " + e.getMessage());
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
        return result;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public String getTableName() {
        return tableName;
    }

    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();

        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Connection conn = new Connection(connname);
        try {
            // ȡ���ܼ�¼����
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            // ��ֹ�ܵ�����ʱ��curPage����Ϊ�ܴ󣬻��ߺ�С
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages) {
                curPage = totalpages;
            }
            if (curPage <= 0) {
                curPage = 1;
            }

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // ���������ڴ��ʹ��
            }

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    if (primaryKey.getType() == PrimaryKey.TYPE_INT) {
                        result.addElement(getObjectDb(new Integer(rs.getInt(1))));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_STRING) {
                        result.addElement(getObjectDb(rs.getString(1)));
                    } else if (primaryKey.getType() == PrimaryKey.TYPE_LONG) {
                        result.addElement(getObjectDb(new Long(rs.getLong(1))));
                    }
                    else if (primaryKey.getType() == primaryKey.TYPE_DATE)
                        result.addElement(getObjectDb(new java.util.Date(rs.getTimestamp(1).getTime())));
                    else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
                        HashMap keys = ((PrimaryKey) primaryKey.clone()).
                                       getKeys();
                        Iterator ir = keys.keySet().iterator();
                        while (ir.hasNext()) {
                            String keyName = (String) ir.next();
                            KeyUnit ku = (KeyUnit) keys.get(keyName);
                            if (ku.getType() == primaryKey.TYPE_INT) {
                                ku.setValue(new Integer(rs.getInt(ku.getOrders() +
                                        1)));
                            } else if (ku.getType() == primaryKey.TYPE_LONG) {
                                ku.setValue(new Long(rs.getLong(ku.getOrders() +
                                        1)));
                            }
                            else if (ku.getType() == PrimaryKey.TYPE_DATE) {
                                ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
                            }
                            else {
                                ku.setValue(rs.getString(ku.getOrders() + 1));
                            }
                        }
                        result.addElement(getObjectDb(keys));
                    }
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error("listResult:" + e.getMessage());
            throw new ErrMsgException("���ݿ����");
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

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    private int blockSize = 100;
}
