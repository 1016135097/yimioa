package cn.js.fan.db;

import java.util.Vector;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import java.util.*;
import java.sql.PreparedStatement;

/*
 �ھɰ��� ����������Vector result;��executeQuery�н�result��Ϊ����ֵ
 Ȼ����finalize()�������²���
if (result != null) {
      result.removeAllElements();
      result = null;
}
 ��Ϳ��ܵ��·���ֵresult�ڱ�ʹ��ʱ��������౻������result�е���Ϣ�ͻᶪʧ����Ϊsize=0
 ����Ҫע�⣬����Vector��Ҫע���߳�ͬ��������
 */

/**
 *  �����Ŀ�����滻PageConn
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class RMConn {
    //Vector result = null; //���ܻᵼ���̲߳���ȫ
    int rowCount = 0; //ʵ��ȡ�õļ�¼����
    int colCount = 0;
    int pageSize = 10;
    public int curPage = 1;
    public long total = 0; //��sql���õ����ܼ�¼����

    Logger logger;
    HashMap mapIndex;
    String connname;

    //����Ԥ�������
    public Conn pconn = null;

    public RMConn(String connname) {
        logger = Logger.getLogger(RMConn.class.getName());
        mapIndex = new HashMap();
        this.connname = connname;
    }

    public RMConn(String connname, int curPage, int pageSize) {
        logger = Logger.getLogger(RMConn.class.getName());
        mapIndex = new HashMap();
        this.curPage = curPage;
        this.pageSize = pageSize;
        this.connname = connname;
    }
/*
    public void ClosePre() {
        if (prestmt!=null) {
            try { prestmt.close();} catch (Exception e) {} prestmt = null;
        }
        if (con!=null) {
            try { con.close(); } catch (Exception e) {} con = null;
        }
    }
*/
    public long getTotal() {
        return total;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public int getColumnCount() {
        return colCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    /**
     * ȡ��ȫ����Ϣ����result��
     * @param sql String
     * @return Vector
     */
    public ResultIterator executeQuery(String sql) throws SQLException {
        rowCount = 0;
        colCount = 0;

        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = null;
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                //ȡ��������Ϣ
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                }

                result = new Vector();

                ResultWrapper rsw = new ResultWrapper(rs);
                while (rsw.next()) {
                    Vector row = new Vector();
                    for (int i = 0; i < colCount; i++)
                        row.addElement(rsw.getObject(i + 1));
                    result.addElement(row);
                    rowCount++;
                }
            }
        } catch (SQLException e) {
            throw e;
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
        return new ResultIterator(result, mapIndex);
    }

    /**
     * ȡ��ȫ����Ϣ����result��
     * @param sql String
     * @return Vector
     */
    public ResultIterator executePreQuery() throws SQLException {
        rowCount = 0;
        colCount = 0;

        ResultSet rs = null;
        Vector result = null;
        try {
            rs = pconn.executePreQuery();
            if (rs == null) {
                return null;
            } else {
                //ȡ��������Ϣ
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                }

                result = new Vector();

                ResultWrapper rsw = new ResultWrapper(rs);
                while (rsw.next()) {
                    Vector row = new Vector();
                    for (int i = 0; i < colCount; i++)
                        row.addElement(rsw.getObject(i + 1));
                    result.addElement(row);
                    rowCount++;
                }
            }
        } catch (SQLException e) {
            throw e;
        } finally {
                if (pconn!=null) {
                    pconn.close();
                    pconn = null;
                }
        }
        return new ResultIterator(result, mapIndex);
    }

    /**
     * ��ҳ��������ResultSet����Ϣ������Vector�У�������Iteratorģʽ
     * @param sql String��sql��ѯ���
     * @param curPage int����ǰҳ
     * @param pageSize int��ҳ�ļ�¼����
     * @return ResultIterator
     */
    public ResultIterator executeQuery(String sql, int curPage, int pageSize) throws
            SQLException {
        this.curPage = curPage;
        this.pageSize = pageSize;

        rowCount = 0;
        colCount = 0;

        ResultSet rs = null;
        Vector result = null;
        Conn conn = new Conn(connname);
        try {
            //ȡ���ܼ�¼����
            String countsql = SQLFilter.getCountSql(sql);
            //logger.debug(countsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getLong(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            // ��ֹ�ܵ�����ʱ��curPage����Ϊ�ܴ󣬻��ߺ�С
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages)
                curPage = totalpages;
            if (curPage <= 0)
                curPage = 1;

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); //���������ڴ��ʹ��

            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                //ȡ��������Ϣ
                ResultSetMetaData rm = rs.getMetaData();
                colCount = rm.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    //System.out.println(rm.getColumnName(i));
                    mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
                }

                rs.setFetchSize(pageSize);

                int absoluteLocation = pageSize * (curPage - 1) + 1;
                //System.out.println("���Զ�λ��: " + absoluteLocation);
                if (rs.absolute(absoluteLocation) == false) {
                    return null;
                }

                result = new Vector();

                ResultWrapper rsw = new ResultWrapper(rs);
                do {
                    Vector row = new Vector();
                    for (int i = 0; i < colCount; i++)
                        row.addElement(rsw.getObject(i + 1));
                    result.addElement(row);
                    rowCount++;
                } while (rsw.next());
            }
        } catch (SQLException e) {
            throw e;
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
        return new ResultIterator(result, mapIndex, total);
    }

    public int executeUpdate(String sql) throws SQLException {
        Conn conn = new Conn(connname);
        int r = 0;
        try {
            r = conn.executeUpdate(sql);
        } catch (SQLException e) {
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return r;
    }

    public int executePreUpdate() throws SQLException {
        int r = 0;
        try {
            r = pconn.executePreUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            if (pconn != null) {
                pconn.close();
                pconn = null;
            }
        }
        return r;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (pconn!=null) {
            pconn.close();
            pconn = null;
        }
        try {
            pconn = new Conn(connname);
            pconn.pstmt = pconn.prepareStatement(sql);
        } catch (SQLException e) {
            if (pconn!=null) {
                pconn.close();
                pconn = null;
            }
            throw e;
        }
        return pconn.pstmt;
    }
}
