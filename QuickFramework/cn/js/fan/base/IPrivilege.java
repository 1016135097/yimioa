package cn.js.fan.base;

import javax.servlet.http.HttpServletRequest;

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
public interface IPrivilege {
    public static final String MSG_INVALID = "�Բ�������Ȩ���ʣ�";
    /**
     * isValid
     */
    public boolean isValid(HttpServletRequest request, String priv);
    public String getUser(HttpServletRequest request);
}
