package cn.js.fan.db;

import java.util.*;

/**
 * �÷����£�
 * <tr>
 * <%@ taglib uri="/WEB-INF/tlds/ResultTag.tld" prefix="rm" %> <rm:RITag
 * query="select name, link from nav order by orders" db="redmoon">
 * <td width=94><img src="images/seperate.gif" width="13" height="26"
 * align="absmiddle">&nbsp;<rm:RRTag field="name"/></td> </rm:RITag>
 * <tr>
 * 
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class ResultIterator implements Iterator, java.io.Serializable {
	/**
	 * ����ֶΡ�ֵ
	 */
	HashMap mapIndex = new HashMap();
	/**
	 * ����ֶα���
	 */
	HashMap mapLabel = new HashMap();
	/**
	 * ����ֶ�����
	 */
	HashMap mapType = new HashMap();

	public HashMap getMapType() {
		return mapType;
	}

	public void setMapType(HashMap mapType) {
		this.mapType = mapType;
	}

	Vector result;
	int curRowIndex = 0;
	int rows = 0;
	Vector curRow;
	long total = 0; // ��ѯ���ü�¼��ȫ������,���ڷ�ҳ

	public ResultIterator() {

	}

	public ResultIterator(Vector result, HashMap mapIndex) {
		if (result == null)
			return;
		this.result = result;
		rows = result.size();
		this.mapIndex = mapIndex;
	}

	public ResultIterator(Vector result, HashMap mapIndex, long total) {
		if (result == null)
			return;
		this.result = result;
		this.total = total;
		rows = result.size();
		this.mapIndex = mapIndex;
	}

	public void beforeFirst() {
		curRowIndex = 0;
	}

	/**
	 * ��ѯ���õļ�¼��
	 * 
	 * @return int
	 */
	public int size() {
		return rows;
	}

	public int getRows() {
		return rows;
	}

	public void setTotal(long t) {
		this.total = t;
	}

	public long getTotal() {
		return this.total;
	}

	/**
	 * Returns <tt>true</tt> if the iteration has more elements.
	 * 
	 * @return <tt>true</tt> if the iterator has more elements.
	 * @todo Implement this java.util.Iterator method
	 */
	public boolean hasNext() {
		if (result == null)
			return false;
		if (curRowIndex <= rows - 1)
			return true;
		else
			return false;
	}

	/**
	 * Returns the next element in the iteration.
	 * 
	 * @return the next element in the iteration.
	 * @todo Implement this java.util.Iterator method
	 */
	public Object next() {
		curRow = (Vector) result.elementAt(curRowIndex);
		curRowIndex++;
		return new ResultRecord(curRow, mapIndex);
	}

	/**
	 * Removes from the underlying collection the last element returned by the
	 * iterator (optional operation).
	 * 
	 * @todo Implement this java.util.Iterator method
	 */
	public void remove() throws RuntimeException {
		throw new RuntimeException("This operate is not suported��");
	}

	public Vector getResult() {
		return result;
	}

	public HashMap getMapIndex() {
		return mapIndex;
	}

	public HashMap getMapLabel() {
		return mapLabel;
	}

	public void setMapLabel(HashMap mapLabel) {
		this.mapLabel = mapLabel;
	}

}
