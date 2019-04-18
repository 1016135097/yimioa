package cn.js.fan.util;

import java.util.Random;
import java.util.Hashtable;
import com.cloudwebsoft.framework.util.ThreadUtil;
import com.cloudwebsoft.framework.console.ConsoleConfig;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RandomSecquenceCreator {
    static Hashtable hash = new Hashtable();
    // �������������
    static Random rand = new Random(System.currentTimeMillis());
    static long lastRandTime = System.currentTimeMillis();

    public RandomSecquenceCreator() {
    }

    public static Hashtable getHash() {
        return hash;
    }

    /**
     * ���ɳ��Ȳ��޵�����������к�
     * @return String
     */
    public static String getId() {
        // ����ʱ��ֵ������hash������hash����������
        if (System.currentTimeMillis()-lastRandTime>20000) {
            hash.clear();
            lastRandTime = System.currentTimeMillis();
        }
        Integer id = new Integer(0);
        synchronized (hash) {
            // ����һ��Ψһ���������
            id = new Integer(rand.nextInt());
            while (hash.containsKey(id)) {
                id = new Integer(rand.nextInt());
            }
            // Ϊ��ǰ�û�������ID
            String data = "";
            if (ConsoleConfig.isDebug())
                data = ThreadUtil.getStackTraceString();

            hash.put(id, data);
        }
        return System.currentTimeMillis() + "" + Math.abs(id.intValue());
    }

    /**
     * ���ɳ�����length֮�ڵ�����������к�
     * @param length int
     * @return String
     */
    public static String getId(int length) {
        // ����е���ÿ��5�����һ�Σ���ˢ�������û��б����ʹ��lastRandTime���ϸ��£���ʹ����ԶҲ����clear
        // ����ʱ��ֵ������hash������hash���������� System.currentTimeMillis()��13λ
        if (System.currentTimeMillis()-lastRandTime>20000) {
            hash.clear();
            lastRandTime = System.currentTimeMillis();
        }
        Integer id = new Integer(0);
        String strId = "";
        synchronized (hash) {
            // ����һ��Ψһ���������
            id = new Integer(rand.nextInt());
            if (length > 15)
                strId = System.currentTimeMillis() + "" + Math.abs(id.intValue());
            else
                strId = "" + Math.abs(id.intValue());
            if (strId.length() > length)
                strId = strId.substring(0, length);
            while (hash.containsKey(strId)) {
                id = new Integer(rand.nextInt());
                if (length > 15)
                    strId = System.currentTimeMillis() + "" + Math.abs(id.intValue());
                else
                    strId = "" + Math.abs(id.intValue());
                if (strId.length() > length)
                    strId = strId.substring(0, length);
            }
            // Ϊ��ǰ�û�������ID
            String data = "";
            if (ConsoleConfig.isDebug())
                data = ThreadUtil.getStackTraceString();
            hash.put(strId, data);
        }
        return strId;
    }
}
