package cn.js.fan.kernel;

/**
 * ��reload��ʱ��contextDestroyed�ᱻ����
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
public class AppServletContextListener implements javax.servlet.ServletContextListener {
    private java.util.Timer timer;

    public AppServletContextListener() {
        // System.out.println( "startup init" );
        // timer = new java.util.Timer( true );
    }

    public void contextDestroyed( javax.servlet.ServletContextEvent event ) {
        System.out.println("Cloud Web Soft Framework has been destroyed." );
        // timer.cancel();
        // �رյ��ȳ�������رգ���ʹ���߳�dead���������㲻�ٵ�����
        // Scheduler.getInstance().doExit();
    }

    public void contextInitialized( javax.servlet.ServletContextEvent event ) {
        // ע�����������Global�����Ի򷽷�����ʹ�õ��ȱ���ʼ�����������е���������proxool����
        // ����ʱproxool�����ӻ�δ��ʼ���ã���ʱ�ͻ�ʹ�ó���Tomcat��������ȥ
        // System.out.println( Global.AppName + " has started." );
        // System.out.println( "The real path of " + Global.AppName + " is " + event.getServletContext().getRealPath( "/" ) );
        System.out.println("Cloud Web Soft Framework has been started." );
        /*
        timer.schedule( new java.util.TimerTask() {
            public void run() {
                System.out.println( "TimerTask run..." );
            }
        } , 0 , 1000 );
        */
        // Global.init();
    }

}
