package cn.js.fan.util;

import java.util.Properties;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.net.URL;
import java.io.File;

public class CFGParser {

  public CFGParser() {
  }

  //����һ��Properties ������� dbhost dbuser dbpassword��ֵ
  private Properties props;

  //�����props
  public Properties getProps() {
    return this.props;
  }

  public void parse(String filename) throws Exception {
    //�����ǵĽ���������
    CFGHandler handler = new CFGHandler();

    //��ȡSAX��������
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(false);
    factory.setValidating(false);

    //��ȡSAX����
    SAXParser parser = factory.newSAXParser();

   //�õ������ļ�myenv.xml����Ŀ¼. tomcat������WEB-INF/classes
   //������BeansConstants���������xml�ļ���������Ϣ����,�����Լ��������
   //URL confURL = BeansConstants.class.getClassLoader().getResource(filename);
   //URL confURL = ClassLoader.getSystemClassLoader().getResource(filename);//ֵΪnull
    URL confURL = getClass().getClassLoader().getResource(filename);
    if (confURL == null) {
      System.out.println("Can't find configration file.");
      return;
    }
    try {
      //���������ͽ�������myenv.xml��ϵ����,��ʼ����
      parser.parse(confURL.toString(), handler);
      //��ȡ�����ɹ�������� �Ժ� ��������Ӧ�ó���ֻҪ���ñ������props�Ϳ�����ȡ���������ƺ�ֵ��
      props = handler.getProps();
    }
    finally {
      factory = null;
      parser = null;
      handler = null;
    }

  }

  public void parseFile(String filename) throws Exception {
    //�����ǵĽ���������
    CFGHandler handler = new CFGHandler();

    //��ȡSAX��������
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(false);
    factory.setValidating(false);

    //��ȡSAX����
    SAXParser parser = factory.newSAXParser();

    File f = new File(filename);
    if (f==null || !f.exists())
      return;
    try {
      //���������ͽ�������myenv.xml��ϵ����,��ʼ����
      parser.parse(f, handler);
      //��ȡ�����ɹ�������� �Ժ� ��������Ӧ�ó���ֻҪ���ñ������props�Ϳ�����ȡ���������ƺ�ֵ��
      props = handler.getProps();
    }
    finally {
      factory = null;
      parser = null;
      handler = null;
    }

  }
}
