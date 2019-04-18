package cn.js.fan.db;

import javax.servlet.jsp.tagext.*;
import org.apache.log4j.Logger;
import java.sql.SQLException;

public class RITag extends TagSupport {
    ResultIterator ri = null;
    String query, db;
    Logger logger = Logger.getLogger(RITag.class.getName());

    /**
     * put your documentation comment here
     */
    public RITag() {

    }

    //��������
    public void setQuery(String query){
      this.query = query;
    }

    public void setDb(String db) {
        this.db = db;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doStartTag() {
        Query();
        return EVAL_BODY_INCLUDE;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doAfterBody() {
        try {
            if (ri==null || !ri.hasNext()) {
                return SKIP_BODY; // ���������������ִ��
            } else {
                return EVAL_BODY_AGAIN; //����δ��������¶���body content��ֵ
            }
        }

        catch (Exception e) {
            logger.error(e.getMessage());
        }
        return SKIP_BODY;
    }


    /**
     * put your documentation comment here
     * @return
     */
    public int doEndTag() {
        return EVAL_PAGE;
    }

    public void Query() {
        RMConn rmconn = new RMConn(db);
        try {
            ri = rmconn.executeQuery(query);
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }


}
