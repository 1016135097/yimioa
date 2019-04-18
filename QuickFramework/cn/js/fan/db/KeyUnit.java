package cn.js.fan.db;

import java.io.Serializable;
import cn.js.fan.util.DateUtil;

public class KeyUnit implements Serializable,Cloneable {
    /**
     * ֻ�����ڵ�һ��������������������������KeyUnit(type, orders)��һ��Ҫ�����
     * @param type int
     */
    public KeyUnit(int type) {
        this.type = type;
    }

    public KeyUnit(Object value) {
        if (value instanceof Integer)
            this.type = PrimaryKey.TYPE_INT;
        else if (value instanceof String) {
            this.type = PrimaryKey.TYPE_STRING;
        }
        else if (value instanceof Long) {
            this.type = PrimaryKey.TYPE_LONG;
        }
        else if (value instanceof java.util.Date) {
            this.type = PrimaryKey.TYPE_DATE;
        }
        this.value = value;
    }

    /**
     * ���ڸ�������,��QDBConfig��getQDBTable�õ����Ա���primaryKey.toObjectArray()�п��԰��մ���������key����initDB��ע��һ��
     * Ҫ����orders(˳��)���������list��ʱ�򣬱��磺sql=select pk1, pk2 from table where cond=*��ʱ�򣬾Ϳ��ܻ���Ϊ����Ϊ0����ʹ�ó���
     * ��QObjectDb�в���������������⣬��Ϊ�Ǵ�XML�ļ��ж�ȡʱ�͸�����˳��
     * @param value Object
     * @param orders int
     */
    public KeyUnit(int type, int orders) {
        this.type = type;
        this.orders = orders;
    }

    /**
     * ע��valueֻ��Ӱ�ӿ�¡����¡��value��������
     * @return Object
     */
    public Object clone() {
        KeyUnit o = null;
        try {
            o = (KeyUnit)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return o;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getStrValue() {
        if (type==PrimaryKey.TYPE_STRING)
            return (String)value;
        else if (type==PrimaryKey.TYPE_INT)
            return "" + ((Integer)value).intValue();
        else if (type==PrimaryKey.TYPE_LONG)
            return "" + ((Long)value).longValue();
        else if (type==PrimaryKey.TYPE_DATE)
            return "" + ((java.util.Date)value).getTime();
        else
            return null;
    }

    public int getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    private int type;
    private Object value;
    private int orders;
}
