package cn.js.fan.db;

import java.util.Vector;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import java.util.*;

/**
 *
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

public class PageConn {
  int rowcount = 0; //ʵ��ȡ�õļ�¼����
  int colcount = 0;
  int pageSize = 10;
  public int curPage = 1;
  public long total = 0; //��sql���õ����ܼ�¼����

  Logger logger;
  HashMap mapIndex;
  String connname;

  public PageConn(String connname) {
    logger = Logger.getLogger(PageConn.class.getName());
    mapIndex = new HashMap();
    this.connname = connname;
  }

  public PageConn(String connname, int curPage, int pageSize) {
    logger = Logger.getLogger(PageConn.class.getName());
    mapIndex = new HashMap();
    this.curPage = curPage;
    this.pageSize = pageSize;
    this.connname = connname;
  }

  public long getTotal() {
    return total;
  }

  protected void finalize() throws Throwable {
    super.finalize();
  }

  public int getColumncount() {
    return colcount;
  }

  public int getRowcount() {
    return rowcount;
  }

  /**
   * ȡ��ȫ����Ϣ����result��
   * @param sql String
   * @return Vector
   */
  public Vector executeQuery(String sql) {
    rowcount = 0;
    colcount = 0;

    ResultSet rs = null;
    Vector result = null;
    Conn conn = new Conn(connname);
    try {
      rs = conn.executeQuery(sql);
      if (rs == null) {
        return null;
      }
      else {
        //ȡ��������Ϣ
        ResultSetMetaData rm = rs.getMetaData();
        colcount = rm.getColumnCount();
        for (int i = 1; i <= colcount; i++) {
          mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
        }

        result = new Vector();

        ResultWrapper rsw = new ResultWrapper(rs);
        while (rsw.next()) {
          Vector row = new Vector();
          for (int i = 0; i < colcount; i++)
            row.addElement(rsw.getObject(i + 1));
          result.addElement(row);
          rowcount++;
        }
      }
    }
    catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage());
      }
      return null;
    }
    finally {
      if (rs != null) {
        try {
          rs.close();
        }
        catch (Exception e) {}
        rs = null;
      }
      if (conn != null) {
        conn.close();
        conn = null;
      }
    }
    return result;
  }

  /**
   * ��ҳ��������ResultSet����Ϣ������Vector�У�������Iteratorģʽ
   * @param sql String��sql��ѯ���
   * @param curPage int����ǰҳ
   * @param pageSize int��ҳ�ļ�¼����
   * @return Vector������Ϣ�洢��Vector�У��������ٴ洢��һ�����Vector��
   */
  public Vector executeQuery(String sql, int curPage, int pageSize) {
    this.curPage = curPage;
    this.pageSize = pageSize;

    rowcount = 0;
    colcount = 0;

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
      }
      else {
        //ȡ��������Ϣ
        ResultSetMetaData rm = rs.getMetaData();
        colcount = rm.getColumnCount();
        for (int i = 1; i <= colcount; i++) {
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
          for (int i = 0; i < colcount; i++)
            row.addElement(rsw.getObject(i + 1));
          result.addElement(row);
          rowcount++;
        }
        while (rsw.next());
      }
    }
    catch (Exception e) {
      logger.error(e.getMessage());
      return null;
    }
    finally {
      if (rs != null) {
        try {
          rs.close();
        }
        catch (Exception e) {}
        rs = null;
      }
      if (conn != null) {
        conn.close();
        conn = null;
      }
    }
    return result;
  }

  public ResultIterator getAllResultIterator(String sql) {
    Vector r = executeQuery(sql);
    return new ResultIterator(r, mapIndex);
  }

  public ResultIterator getResultIterator(String sql) {
    Vector r = executeQuery(sql, curPage, pageSize);
    return new ResultIterator(r, mapIndex, total);
  }
}
