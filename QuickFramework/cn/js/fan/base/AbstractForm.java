package cn.js.fan.base;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.StrUtil;
import org.apache.log4j.Logger;

public abstract class AbstractForm {
    HttpServletRequest request;
    public Logger logger = null;

    public AbstractForm() {
        logger = Logger.getLogger(this.getClass().getName());
    }

    String errmsg = "";

    public void init() {
        errmsg = "";
    }

    public void log(String err, String div) {
        errmsg += err + div;
    }

    public void log(String err) {
        log(err, "\\n");
    }

    public String getMessage() {
        return errmsg;
    }

    public boolean isValid() {
        if (errmsg.equals(""))
            return true;
        else
            return false;
    }

    /**
     * �������
     * @throws ErrMsgException
     */
    public void report() throws ErrMsgException {
        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);
    }

    /**
     * ����ַ����ĳ���
     * @param str String
     * @param low int
     * @param high int
     * @param isequal boolean
     * @return boolean �ж��Ƿ����
     */
    public boolean chkStrLen(String str, int low, int high, boolean isequal) {
        if (str==null)
            return false;
        if (low>high)
            return false;
        int len = StrUtil.UTF8Len(str);
        if (isequal) {
            if (len > high || len < low)
                return false;
        }
        else {
            if (len >= high || len <= low)
                return false;
        }
        return true;
    }

    public boolean chkStrLen(String str, int low, int high) {
        return chkStrLen(str, low, high, true);
    }
}
