<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.io.*,
				 cn.js.fan.db.*,
				 cn.js.fan.util.*,
				 cn.js.fan.web.*,
				 com.cloudwebsoft.framework.db.*,
				 org.jdom.*,
				 java.util.*"
%>
<%@page import="cn.js.fan.cache.jcs.RMCache" %>
<%@page import="com.redmoon.oa.kernel.License" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%
    XMLConfig cfg = new XMLConfig("config_oa.xml", false, "gb2312");
    License lic = License.getInstance();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=lic.getCompany()%>系统安装 - 配置数据库连接</title>
    <link rel="stylesheet" type="text/css" href="../common.css">
    <!-- 注意不能用SkinMgr.getSkin(request)，因为如果数据库连接出错，其中调用到数据库的地方就会出错 -->
    <link type="text/css" rel="stylesheet" href="../skin/nightSky/css.css"/>
</head>
<body>
<table cellpadding="6" cellspacing="0" border="0" width="100%">
    <tr>
        <td width="1%" valign="top"></td>
        <td width="99%" align="center" valign="top">
            <div align="left"><b>欢迎您使用<%=lic.getCompany()%>系统<%=cfg.get("oa.version")%> MySQL版本</b></div>
            <hr size="0">
            <%
                cfg = new XMLConfig(application.getRealPath("/") + "WEB-INF" + java.io.File.separator + "proxool.xml", true, "iso-8859-1");
                Element root, driverProp, e_user = null, e_pwd = null, e_driverclass = null;
                String user = "", pwd = "", url = "", ip = "", database = "", port = "", maximum_connection_count = "", dirverclass = "";
                List list;

                try {
                    root = cfg.getRootElement();
                    driverProp = root.getChild("proxool").getChild("driver-properties");

                    list = driverProp.getChildren();
                    e_user = (Element) list.get(0);
                    e_pwd = (Element) list.get(1);
                    user = e_user.getAttributeValue("value");
                    pwd = e_pwd.getAttributeValue("value");

                    dirverclass = cfg.get("proxool.driver-class");

                    //jdbc:mysql://localhost:3306/redmoonoa?useUnicode=true&amp;characterEncoding=UTF-8&amp;zeroDateTimeBehavior=convertToNull
                    url = cfg.get("proxool.driver-url");
                    ;
                    int beginIndex = url.indexOf("//");
                    ip = url.substring(beginIndex + 2, url.indexOf(":", beginIndex));
                    database = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
                    port = url.substring(url.lastIndexOf(":") + 1, url.lastIndexOf("/"));
                    maximum_connection_count = cfg.get("proxool.maximum-connection-count");

                    Class.forName(dirverclass);

                } catch (ClassNotFoundException cnfe) {
                    out.print("<font style='font-size:14px' color='#FF0000'>请检查WEB-INF/proxool.xml文件中的driver-class是否设置正确！<br>参照设置为：com.mysql.jdbc.Driver</font><br>");
                } catch (Exception e) {
                    out.print("<font style='font-size:14px' color='#FF0000'>请检查WEB-INF/proxool.xml文件中的driver-url是否设置正确！<br>参照设置为：jdbc:mysql://192.168.1.27:3306/cwbbs?useUnicode=true&amp;characterEncoding=UTF-8&amp;zeroDateTimeBehavior=convertToNull</font><br>");
                }

                String op = ParamUtil.get(request, "op");
                boolean isValid = false;
                if (op.equals("setup")) {
                    try {
                        RMCache.getInstance().clear();
                        user = ParamUtil.get(request, "user");
                        ip = ParamUtil.get(request, "ip");
                        port = ParamUtil.get(request, "port");
                        database = ParamUtil.get(request, "database");
                        pwd = ParamUtil.get(request, "pwd");
                        url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull";
                        maximum_connection_count = ParamUtil.get(request, "maximum_connection_count");
                        cfg.set("proxool.driver-url", url);
                        cfg.set("proxool.driver-class", "com.mysql.jdbc.Driver");
                        e_user.setAttribute("value", user);
                        e_pwd.setAttribute("value", pwd);
                        cfg.set("proxool.maximum-connection-count", maximum_connection_count);
                        cfg.writemodify();
                        Global.init();

                        String realPath = application.getRealPath("/");
                        if (realPath.lastIndexOf("/") != realPath.length() - 1)
                            realPath += "/";
                        org.logicalcobwebs.proxool.ProxoolFacade.removeAllConnectionPools(5000); //
                        org.logicalcobwebs.proxool.configuration.JAXPConfigurator.configure(realPath + "WEB-INF/proxool.xml", false);

                        com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
                        String strVersion = StrUtil.getNullStr(oaCfg.get("version"));
                        double version = StrUtil.toDouble(strVersion, -1);
                        // System.out.println("version=" + version);
                        if (version < 3) {
                            // 添加拉单及冲抵字段
                            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                            JdbcTemplate jt = new JdbcTemplate();
                            ResultIterator ri = jt.executeQuery(sql);
                            while (ri.hasNext()) {
                                ResultRecord rr = (ResultRecord) ri.next();
                                String tableName = rr.getString(1).toLowerCase();
                                if (tableName.startsWith("form_table_")) {
                                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_flag` TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER `cws_status`, ADD COLUMN `cws_quote_id` INTEGER UNSIGNED AFTER `cws_flag`";
                                    try {
                                        jt.executeUpdate(sql);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_progress` INTEGER UNSIGNED NOT NULL DEFAULT 0 AFTER `cws_status`";
                                    try {
                                        jt.executeUpdate(sql);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else if (version < 4.0) {
                            // 添加cws_parent_form抵字段
                            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                            JdbcTemplate jt = new JdbcTemplate();
                            ResultIterator ri = jt.executeQuery(sql);
                            while (ri.hasNext()) {
                                ResultRecord rr = (ResultRecord) ri.next();
                                String tableName = rr.getString(1).toLowerCase();
                                if (tableName.startsWith("form_table_")) {
                                    // sql = "ALTER TABLE `" + tableName + "` DROP COLUMN `cws_parent_form`";
                                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_parent_form` varchar(20) AFTER `cws_status`";
                                    try {
                                        jt.executeUpdate(sql);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        else if (version < 5.0) {
                            // 添加cws_create_date cws_modify_date cws_finish_date抵字段
                            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                            JdbcTemplate jt = new JdbcTemplate();
                            ResultIterator ri = jt.executeQuery(sql);
                            while (ri.hasNext()) {
                                ResultRecord rr = (ResultRecord) ri.next();
                                String tableName = rr.getString(1).toLowerCase();
                                if (tableName.startsWith("form_table_") && !tableName.endsWith("_log")) {
                                    sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_create_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_modify_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_finish_date` datetime AFTER `cws_status`";
                                    try {
                                        jt.executeUpdate(sql);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        else {
                            // 因嵌套表格2控件的version改为了2，所以需处理一下
                            String sql = "update form_field set description=defaultValue where description='' and macroType='nest_sheet'";
                            try {
                                JdbcTemplate jt = new JdbcTemplate();
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        out.print("Problem configuring: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            %>
            <table width="100%" border="0" cellpadding="0" cellspacing="0">
                <form name=form1 action="?op=setup" method=post>
                    <tr>
                        <td height="24" colspan="2" align="left">配置数据库连接：</td>
                    </tr>
                    <tr>
                        <td height="24" align="right">&nbsp;</td>
                        <td><%
                            if (op.equals("setup")) {
                                String sql = "select * from redmoonid";
                                Conn conn = new Conn(Global.getDefaultDB());
                                if (conn.getCon() != null) {
                                    try {
                                        conn.executeQuery(sql);
                                        isValid = true;
                                    } catch (Exception e) {
                                        out.print(e.getMessage());
                                    } finally {
                                        if (conn != null) {
                                            conn.close();
                                            conn = null;
                                        }
                                    }
                                }
                                if (!isValid) {
                                    out.print("<font color=red>测试连接失败！请检查连接字符串、用户名和密码是否正确！</font>");
                                } else
                                    out.print("<font color=green><b>测试连接成功！</b></font>");
                            }
                        %></td>
                    </tr>
                    <tr>
                        <td height="24" align="right">配置文件路径：</td>
                        <td><%=application.getRealPath("/") + "WEB-INF" + java.io.File.separator + "proxool.xml"%>&nbsp;&nbsp;&nbsp;(在初始化前，请先将redmoonoa.sql导入mysql数据库)</td>
                    </tr>
                    <!--
                          <tr>
                            <td height="24" align="right">数据库连接字符串：</td>
                            <td><input name="url" value="" size="50"/></td>
                          </tr>
                    -->
                    <tr>
                        <td height="24" align="right">用户名：</td>
                        <td><input name="user" value="<%=user%>"/></td>
                    </tr>
                    <tr>
                        <td height="24" align="right">密码：</td>
                        <td><input type="password" name="pwd" value="<%=pwd%>"/></td>
                    </tr>
                    <tr>
                        <td height="24" align="right"><span class="thead" style="PADDING-LEFT: 10px">主机名：</span></td>
                        <td><input name="ip" value="<%=ip%>"/></td>
                    </tr>
                    <tr>
                        <td height="24" align="right"><span class="thead" style="PADDING-LEFT: 10px">端口号：</span></td>
                        <td><input name="port" value="<%=port%>" size="8"/></td>
                    </tr>
                    <tr>
                        <td height="24" align="right"><span class="thead" style="PADDING-LEFT: 10px">数据库名：</span></td>
                        <td><input name="database" value="<%=database%>"/></td>
                    </tr>
                    <tr>
                        <td height="24" align="right">最大连接数：</td>
                        <td><input type="text" name="maximum_connection_count" value="<%=maximum_connection_count%>"/></td>
                    </tr>
                </form>
            </table>
            <hr size="0">
            <div align="center">
                <input name="button22" type="button" onclick="window.location.href='setup.jsp'" value="上一步"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input type="button" value="连接测试" onClick="form1.submit()">
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input name="button2" type="button" onclick="window.location.href='setup3.jsp?db_type=mysql'" value="下一步"/>
            </div>
        </td>
    </tr>
</table>
</body>
</html>