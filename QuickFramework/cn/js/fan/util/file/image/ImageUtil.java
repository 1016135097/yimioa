package cn.js.fan.util.file.image;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

public class ImageUtil {
    /**
     * ����ͼƬ
     * @param source BufferedImage
     * @param targetW int
     * @param targetH int
     * @return BufferedImage
     */
    public static BufferedImage resize(BufferedImage source, int targetW,
                                       int targetH) {
        // targetW��targetH�ֱ��ʾĿ�곤�Ϳ�
        int type = source.getType();
        BufferedImage target = null;
        double sx = (double) targetW / source.getWidth();
        double sy = (double) targetH / source.getHeight();
        /*
        // ��targetW��targetH��Χ��ʵ�ֵȱ����š��������Ҫ�ȱ�����
        if (sx > sy) {
            sx = sy;
            targetW = (int) (sx * source.getWidth());
        } else {
            sy = sx;
            targetH = (int) (sy * source.getHeight());
        }
         */
        if (type == BufferedImage.TYPE_CUSTOM) { // handmade
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(targetW,
                    targetH);
            boolean alphaPremultiplied = cm.isAlphaPremultiplied();
            target = new BufferedImage(cm, raster, alphaPremultiplied, null);
        } else
            target = new BufferedImage(targetW, targetH, type);
        Graphics2D g = target.createGraphics();
        // smoother than exlax:
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);
        g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        return target;
    }

    /**
     * ����ͼƬ
     * @param fromFileStr String ͼƬ����·��
     * @param saveToFileStr String �����ļ��ľ���·��
     * @param width int
     * @param hight int
     * @throws Exception
     */
    public static void resizeImage(String fromFileStr, String saveToFileStr,
                                 int width, int hight) throws Exception {
        BufferedImage srcImage;
        // String ex = fromFileStr.substring(fromFileStr.indexOf("."),fromFileStr.length());
        String imgType = "JPEG";
        if (fromFileStr.toLowerCase().endsWith(".png")) {
            imgType = "PNG";
        }
        // System.out.println(ex);
        File saveFile = new File(saveToFileStr);
        File fromFile = new File(fromFileStr);
        srcImage = ImageIO.read(fromFile);
        if (width > 0 || hight > 0) {
            srcImage = resize(srcImage, width, hight);
        }
        ImageIO.write(srcImage, imgType, saveFile);
    }

    /**
     * ͬ�����ţ����ݳ����,ͬ�������ţ��������ڸߣ����Ը�Ϊ�������ţ���֮�����Կ�Ϊ��������
     * ����ָ���Ŀ�ߵĶ��ಿ�ֽ��ᱻ��ȥ�������Ͻ�Ϊ����ü�
     * @param source BufferedImage
     * @param oriTargetW int
     * @param oriTargetH int
     * @return BufferedImage
     */
    public static BufferedImage resizePoportionAndClip(BufferedImage source, int oriTargetW, int oriTargetH) {
        // targetW��targetH�ֱ��ʾĿ�곤�Ϳ�
        int type = source.getType();
        BufferedImage target = null;
        double scale = 1.0;

        int targetW = oriTargetW;
        int targetH = oriTargetH;

        double sx = (double) targetW / source.getWidth();
        double sy = (double) targetH / source.getHeight();

        // ���ݳ����,ͬ�������ţ��������ڸߣ����Ը�Ϊ�������ţ���֮�����Կ�Ϊ��������
        if (source.getWidth() > source.getHeight()) {
            double oldSx = sx;
            sx = sy;
            targetW = (int) (sx * source.getWidth());
            if (targetW < oriTargetW) {
                sx = oldSx;
            }
        } else {
            double oldSy = sy;
            sy = sx;
            targetH = (int) (sy * source.getHeight());
            if (targetH < oriTargetH) {
                sy = oldSy;
            }
        }




       /*
       if (oriTargetW > oriTargetH) {
           sy = sx;
           targetH = (int) (sy * source.getHeight());
       } else {
           sx = sy;
           targetW = (int) (sx * source.getWidth());
       }
       */

        // System.out.println("w=" + targetW + " h=" + targetH);

        if (type == BufferedImage.TYPE_CUSTOM) { // handmade
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(oriTargetW,
                    oriTargetH);
            boolean alphaPremultiplied = cm.isAlphaPremultiplied();
            target = new BufferedImage(cm, raster, alphaPremultiplied, null);
        } else
            target = new BufferedImage(oriTargetW, oriTargetH, type);
        Graphics2D g = target.createGraphics();
        // smoother than exlax:
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);
        g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        return target;
    }

    /**
     * �ȱ������Ų��ü�����Ԥ�����С��Χ��ͼƬ
     * @param fromFileStr String
     * @param saveToFileStr String
     * @param width int
     * @param hight int
     * @throws Exception
     */
    public static void resizeImagePoportionAndClip(String fromFileStr, String saveToFileStr,
                                 int width, int hight) {
        BufferedImage srcImage;
        // String ex = fromFileStr.substring(fromFileStr.indexOf("."),fromFileStr.length());
        String imgType = "JPEG";
        if (fromFileStr.toLowerCase().endsWith(".png")) {
            imgType = "PNG";
        }
        // System.out.println(ex);
        try {
            File saveFile = new File(saveToFileStr);
            File fromFile = new File(fromFileStr);
            srcImage = ImageIO.read(fromFile);
            if (width > 0 || hight > 0) {
                srcImage = resizePoportionAndClip(srcImage, width, hight);
            }
            ImageIO.write(srcImage, imgType, saveFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    public static void main(String argv[]) {
        try {
            //����1(from),����2(to),����3(��),����4(��)
            ImageUtil.resizeImagePoportionAndClip("c:/111.gif",
                                "c:/2.gif", 75,
                                75);
            ImageUtil.resizeImagePoportionAndClip("c:/222.gif",
                                "c:/3.gif", 75,
                                75);
            // Thumbnail.createThumbnail("c:/1.gif", "c:/2.jpg", 76);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
