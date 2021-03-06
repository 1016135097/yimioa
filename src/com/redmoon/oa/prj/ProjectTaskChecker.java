package com.redmoon.oa.prj;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.IModuleChecker;

/**
 * @Description:
 * @author:
 * @Date: 2016-1-24下午05:48:27
 */
public class ProjectTaskChecker implements IModuleChecker {

	public ProjectTaskChecker() {
		super();
	}

	public boolean validateUpdate(HttpServletRequest request, FileUpload fu,
			FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {

		return true;
	}

	public boolean validateCreate(HttpServletRequest request, FileUpload fu,
			Vector fields) throws ErrMsgException {

		return true;
	}

	public boolean validateDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {

		return true;
	}

	public boolean onDel(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		return true;
	}

	public boolean onCreate(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		if (fdao.getCwsId() == null || fdao.getCwsId().equals("")) {
			String prjId = StrUtil.getNullStr(fdao.getFieldValue("prj_id"));
			fdao.setCwsId(prjId);
			fdao.save();
		}
		return true;
	}

	public boolean onNestTableCtlAdd(HttpServletRequest request,
			HttpServletResponse response, JspWriter out) {
		return false;
	}

	public boolean onUpdate(HttpServletRequest request, FormDAO fdao)
			throws ErrMsgException {
		if (fdao.getCwsId() == null || fdao.getCwsId().equals("")) {
			String prjId = StrUtil.getNullStr(fdao.getFieldValue("prj_id"));
			fdao.setCwsId(prjId);
			fdao.save();
		}
		return true;
	}
}
