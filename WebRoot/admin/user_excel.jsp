<%@ page contentType="text/html; charset=gb2312"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.post.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import="jxl.*"%>
<%@ page import="jxl.write.*"%>
<%@page import="com.redmoon.oa.dept.DeptDb"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.redmoon.oa.Config"%>
<%@page import="com.redmoon.oa.pvg.RoleDb"%>
<%@page import="java.sql.SQLException"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
			
response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode("ȫ����Ա") + ".xls");  

OutputStream os = response.getOutputStream();
WritableWorkbook wwb = null;
WritableSheet ws = null;

int isValid = ParamUtil.getInt(request, "isValid", 1);
try {
	UserMgr um = new UserMgr();
	String[] preinfo = {"�ʺ�", "����","����","�ֻ�","�Ա�","Ա�����"};
	String[] depts = {"����һ��", "���Ŷ���", "��������", "�����ļ�", "�����弶", "��������", "�����߼�", "���Ű˼�", "���žż�", "����ʮ��", "����ʮһ��", "����ʮ����"};
	String[] sufinfo = {"��ɫ", "��λ", "���֤", "���", "��������", "����", "QQ", "�绰", "��ְ����", "�̺�", "��Ȥ����", "��ַ"};

	// ����excel
	wwb = Workbook.createWorkbook(os);
	ws = wwb.createSheet("ȫ����Ա", 0);
	
	Config cfg = new Config();
	int maxLevel = 12;
	int deptLevels = cfg.getInt("export_dept_levels");
	if (deptLevels > maxLevel) {
		deptLevels = maxLevel;
	} else if (deptLevels < 0) {
		deptLevels = 4;
	}
	
	// ��ͷ
	String title = "";
	Label a = null;
	// ����ǰ�����Ϣ
	for (int i = 0; i < preinfo.length; i++) {
		a = new Label(i, 0, preinfo[i]);
		ws.addCell(a);
	}
	// ����
	if (deptLevels==0) {
		depts[0] = "����";
	}
	for (int i = 0; i < (deptLevels==0?1:deptLevels); i++) {
		a = new Label(preinfo.length + i, 0, depts[i]);
		ws.addCell(a);
	}
	// ���ź������Ϣ
	for (int i = 0; i < sufinfo.length; i++) {
		a = new Label(preinfo.length + (deptLevels==0?1:deptLevels) + i, 0, sufinfo[i]);
		ws.addCell(a);
	}
	
	String account = "", realName = "", mobile = "", gender = "", personNo = "", idCard="", married = "", email="", QQ="", phone="", entryDate="", shortMobile="", hobbies="", address="";
	String postName = "";
	// ����
	String sql = "select users.id,name,dept_code,realname,case gender when 0 then '��' when 1 then'Ů' end g,date_format(birthday,'%Y-%m-%d') d,'' s,mobile,idcard,ISMARRIAGED,person_no,phone,'' fax,address,mobile,email,qq,msn,hobbies,address,entryDate from users,dept_user where name=dept_user.user_name and isvalid=" + isValid + " order by dept_code asc,dept_user.orders asc";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = null;
	ri = jt.executeQuery(sql);
	int row = 1;
	HashMap<String, java.lang.Boolean> map = new HashMap<String, java.lang.Boolean>();
	while (ri.hasNext()) {
		int col = 0;
		ResultRecord rr = (ResultRecord)ri.next();
		String name = rr.getString(2);
		if (map.containsKey(name)) {
			continue;
		} else {
			map.put(name, true);
		}
		
		a = new Label(col++, row, rr.getString("name"));
		ws.addCell(a);		
		
		a = new Label(col++, row, rr.getString("realname"));
		ws.addCell(a);
		
		AccountDb ad = new AccountDb();
		ad = ad.getUserAccount(name);
		if (ad!=null) {
			account = ad.getName();
		}
		a = new Label(col++, row, account);
		ws.addCell(a);
		
		a = new Label(col++, row, StrUtil.getNullStr(rr.getString("mobile")));
		ws.addCell(a);
		
		a = new Label(col++, row, StrUtil.getNullStr(rr.getString("g")));
		ws.addCell(a);

		a = new Label(col++, row, StrUtil.getNullStr(rr.getString("person_no")));
		ws.addCell(a);
								
		//String deptCode = rr.getString(3);
		// ��ȡ������������
		//���Ӷಿ�ŵ���
		String[] deptNames = new String[maxLevel];
		String depts_ = "";
		String sqlDepts = "select * from dept_user where user_name = ?";
		ResultIterator resultIterator = jt.executeQuery(sqlDepts,new Object[]{rr.getString(2)});
		while (resultIterator.hasNext()){
		    ResultRecord resultRecord = (ResultRecord)resultIterator.next();
		    String deptCode = resultRecord.getString("dept_code");
			DeptDb dd = new DeptDb(deptCode);
			deptNames[0] = dd.getName();
			int j = 0;
			while (!dd.getParentCode().equals("-1")
					&& !dd.getParentCode().equals(DeptDb.ROOTCODE)
					&& j < maxLevel - 1) {
				dd = new DeptDb(dd.getParentCode());
				// System.out.println(getClass() + " dd.getParentCode()=" + dd.getParentCode());
				if (dd != null && !dd.getParentCode().equals("")) {
					deptNames[++j] = dd.getName();
				} else {
					break;
				}
			}
			if (deptLevels==0) {
				String dps = "";
				// System.out.println(getClass() + " j=" + j);
				for (int i = j; i >= 0; i--) {
					if (dps.equals("")) {
						dps = deptNames[i];
					}
					else {
						dps += "\\" + deptNames[i];
					}
				}
				if ("".equals(depts_)){
				    depts_ = dps;
				}else {
				    depts_ += "," + dps;
				}

			}
			//���ŷּ���ʾûʲô�ã��Ұ��մ��ַ�ʽ��������֯�����޷������µ���ϵͳ

			/*else {
				// xls�м��벿��
				for (int i = j; i >= 0; i--) {
					a = new Label(col++, row, deptNames[i]);
					ws.addCell(a);
				}
				// xls�в���
				for (int i = j + 1; i < deptLevels; i++) {
					a = new Label(col++, row, "");
					ws.addCell(a);
				}
			}*/
		}
		a = new Label(col++, row, depts_);
		ws.addCell(a);

		
		// �û���ɫ
		UserDb ud = new UserDb(name);
		RoleDb[] roleary = ud.getRoles();
		String roles = "";
		if (roleary != null) {
			for (int i = 0; i < roleary.length; i++) {
				if (roleary[i].getCode().equals(RoleDb.CODE_MEMBER)) {
					continue;
				}
				roles += (i == 0 ? "" : "��") + roleary[i].getDesc();
			}
		}
		a = new Label(col++, row, roles);
		ws.addCell(a);
		
		PostUserDb pud = new PostUserDb();
		pud = pud.getPostUserDb(name);
		if (pud!=null) {
			PostDb pd = new PostDb();
			pd = pd.getPostDb(pud.getInt("post_id"));
			if (pd!=null) {
				postName = pd.getString("name");
			}
		}
		a = new Label(col++, row, postName);
		ws.addCell(a);
				
		a = new Label(col++, row, rr.getString("idCard"));
		ws.addCell(a);		
		
		married = rr.getInt("ISMARRIAGED")==1?"�ѻ�":"δ��";		
		a = new Label(col++, row, married);
		ws.addCell(a);
				
		a = new Label(col++, row, rr.getString("d"));
		ws.addCell(a);
				
		a = new Label(col++, row, StrUtil.getNullStr(rr.getString("email")));
		ws.addCell(a);
		
		// �ʺ� ���� ����	�ֻ�	�Ա�	Ա�����	���� ��ɫ��λ	���֤	���	 �������� 	E-mail	QQ	�绰	��ְ����	�̺�	��Ȥ����	��ַ
		a = new Label(col++, row, StrUtil.getNullStr(rr.getString("qq")));
		ws.addCell(a);									
		a = new Label(col++, row, StrUtil.getNullStr(rr.getString("phone")));
		ws.addCell(a);	
		a = new Label(col++, row, DateUtil.format(rr.getDate("entryDate"), "yyyy-MM-dd"));
		ws.addCell(a);	
		a = new Label(col++, row, StrUtil.getNullStr(rr.getString("MSN")));
		ws.addCell(a);			
		a = new Label(col++, row, StrUtil.getNullStr(rr.getString("hobbies")));
		ws.addCell(a);			
		a = new Label(col++, row, StrUtil.getNullStr(rr.getString("address")));
		ws.addCell(a);						

		row++;
	}

	wwb.write();
} catch (IOException e) {
	out.println("user_excel: " + e.getMessage());
} catch (SQLException e) {
	out.println("user_excel: " + e.getMessage());
	e.printStackTrace();
} catch (Exception e) {
	out.println("user_excel: " + e.getMessage());
	e.printStackTrace();
} finally {
	try {
		if (wwb != null) {
			wwb.close();
		}
	} catch (IOException e) {
		out.println("user_excel: " + e.getMessage());
	}
	os.close();
	out.clear();
	out = pageContext.pushBody();
}
%>
