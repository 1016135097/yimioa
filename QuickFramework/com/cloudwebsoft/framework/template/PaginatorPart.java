package com.cloudwebsoft.framework.template;

import java.util.*;

import javax.servlet.http.*;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: ҳ���ǩ</p>
 *
 * <p>Description:PaginatorPart����һ��Ԫ�أ��ɾ�̬Ԫ����FieldPart��ɣ����ڷ�ҳ�����ѭ���У�ֻ����һ�� </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PaginatorPart implements ITemplate {
    public String name;
    public String parentName;
    public List steps = null;

    public PaginatorPart() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String toString(HttpServletRequest request, List param) {
        return name;
    }

    public void addStep(ITemplate step) {
        if (steps == null) {
            steps = new ArrayList();
        }
        steps.add(step);
    }

    /**
     * �������ɾ�̬pageBlock
     * @param request HttpServletRequest
     * @param listPart ListPart
     * @param pageHtmlBlcok String
     * @return String
     */
    public String write(HttpServletRequest request, ListPart listPart, String pageStatics, String pageHtmlBlcok) {
        if (steps == null)
            return "";
        StringBuffer buf = new StringBuffer();
        int nSteps = steps.size();
        for (int i = 0; i < nSteps; i++) {
            ITemplate step = (ITemplate) steps.get(i);

            if (step instanceof FieldPart) {
                FieldPart fieldPart = (FieldPart) step;
                // LogUtil.getLog(getClass()).info("write: field name=" + fieldPart.getName());
                if (fieldPart.subField.equals("currentPageBlock")) {
                    buf.append(pageHtmlBlcok);
                }
                else if (fieldPart.subField.equals("pageStatics")) {
                    buf.append(pageStatics);
                }
                else
                    buf.append(fieldPart.write(listPart));
            }
            else { // StaticPart or VariablePart or IgnoredPart
                buf.append(step.toString(request, null));
            }
        }
        return buf.toString();
    }

    /**
     * ���ڶ�̬����pageBlock
     * @param request HttpServletRequest
     * @param listPart ListPart
     * @return String
     */
    public String write(HttpServletRequest request, ListPart listPart) {
        if (steps == null)
            return "";
        StringBuffer buf = new StringBuffer();
        int nSteps = steps.size();
        for (int i = 0; i < nSteps; i++) {
            ITemplate step = (ITemplate) steps.get(i);

            if (step instanceof FieldPart) {
                FieldPart fieldPart = (FieldPart) step;
                // LogUtil.getLog(getClass()).info("write: field name=" + fieldPart.getName());
                if (fieldPart.subField.equals("currentPageBlock")) {
                    String page = StrUtil.getNullStr((String)fieldPart.props.get("page"));
                    if (url==null)
                        buf.append(listPart.paginator.getCurPageBlock(page + "?", "p" + i)); // pre����ֱ����i����Ϊҳ���е�Ԫ��name���������ֿ�ͷ
                    else {
                        // System.out.println(getClass() + " " + listPart.paginator);
                        buf.append(listPart.paginator.getCurPageBlock(page + url, "p"+i));
                    }
                }
                else if (fieldPart.subField.equals("pageStatics")) {
                    buf.append(listPart.paginator.getPageStatics(request));
                }
                else
                    buf.append(fieldPart.write(listPart));
            }
            else { // StaticPart or VariablePart or IgnoredPart
                buf.append(step.toString(request, null));
            }
        }
        return buf.toString();
    }

    public String getParentName() {
        return parentName;
    }

    public String getUrl() {
        return url;
    }

    private String url;
}
