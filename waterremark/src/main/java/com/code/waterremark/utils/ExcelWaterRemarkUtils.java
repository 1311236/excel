package com.code.waterremark.utils;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;


public class ExcelWaterRemarkUtils {
    /*
     * 为Excel打上水印工具函数 请自行确保参数值，以保证水印图片之间不会覆盖。 在计算水印的位置的时候，并没有考虑到单元格合并的情况，请注意
     *
     * @param wb
     *            Excel Workbook
     * @param sheet
     *            需要打水印的Excel
     * @param waterRemarkPath
     *            水印地址，classPath，目前只支持png格式的图片，
     *            因为非png格式的图片打到Excel上后可能会有图片变红的问题，且不容易做出透明效果。
     *            同时请注意传入的地址格式，应该为类似："\\excelTemplate\\test.png"
     * @param startXCol
     *            水印起始列
     * @param startYRow
     *            水印起始行
     * @param betweenXCol
     *            水印横向之间间隔多少列
     * @param betweenYRow
     *            水印纵向之间间隔多少行
     * @param XCount
     *            横向共有水印多少个
     * @param YCount
     *            纵向共有水印多少个
     * @param waterRemarkWidth
     *            水印图片宽度为多少列
     * @param waterRemarkHeight
     *            水印图片高度为多少行
     * @throws IOException
     */
    public static void putWaterRemarkToExcel(Workbook wb, Sheet sheet, String waterRemarkPath, int startXCol,
                                             int startYRow, int betweenXCol, int betweenYRow, int XCount, int YCount, int waterRemarkWidth,
                                             int waterRemarkHeight) throws IOException {

        // 校验传入的水印图片格式
        if (!waterRemarkPath.endsWith("png") && !waterRemarkPath.endsWith("PNG")) {
            throw new RuntimeException("向Excel上面打印水印，目前支持png格式的图片。");
        }

        // 加载图片
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        InputStream imageIn = new FileInputStream(waterRemarkPath);
//        InputStream imageIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(waterRemarkPath);
        if (null == imageIn || imageIn.available() < 1) {
            throw new RuntimeException("向Excel上面打印水印，读取水印图片失败(1)。");
        }
        BufferedImage bufferImg = ImageIO.read(imageIn);
        if (null == bufferImg) {
            throw new RuntimeException("向Excel上面打印水印，读取水印图片失败(2)。");
        }
        ImageIO.write(bufferImg, "png", byteArrayOut);

        // 开始打水印
        Drawing drawing = sheet.createDrawingPatriarch();

        // 按照共需打印多少行水印进行循环
        for (int yCount = 0; yCount < YCount; yCount++) {
            // 按照每行需要打印多少个水印进行循环
            for (int xCount = 0; xCount < XCount; xCount++) {
                // 创建水印图片位置
                int xIndexInteger = startXCol + (xCount * waterRemarkWidth) + (xCount * betweenXCol);
                int yIndexInteger = startYRow + (yCount * waterRemarkHeight) + (yCount * betweenYRow);
                /*
                 * 参数定义： 第一个参数是（x轴的开始节点）； 第二个参数是（是y轴的开始节点）； 第三个参数是（是x轴的结束节点）；
                 * 第四个参数是（是y轴的结束节点）； 第五个参数是（是从Excel的第几列开始插入图片，从0开始计数）；
                 * 第六个参数是（是从excel的第几行开始插入图片，从0开始计数）； 第七个参数是（图片宽度，共多少列）；
                 * 第8个参数是（图片高度，共多少行）；
                 */
                ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, xIndexInteger,
                        yIndexInteger, xIndexInteger + waterRemarkWidth, yIndexInteger + waterRemarkHeight);

                Picture pic = drawing.createPicture(anchor,
                        wb.addPicture(byteArrayOut.toByteArray(), Workbook.PICTURE_TYPE_PNG));
                pic.resize();
            }
        }
    }

    public static final String waterRemarkImgFilePath = "D:\\test\\timg(1)_1.png";
    public static final String inFilePath = "D:\\test\\绩效考评表_罗宇航-2019年7月绩效表.xlsx";
    public static final String saveFilePath = "D:\\test\\test4.xlsx";

    public static void main(String[] args) throws IOException {
        //读取excel文件
        Workbook wb = new XSSFWorkbook(new FileInputStream(inFilePath));
        //设置水印图片路径
        String imgPath = waterRemarkImgFilePath;
        ImageUtils.createWaterMark("水银", imgPath);
        //获取excel sheet个数
        int sheets = wb.getNumberOfSheets();
        //循环sheet给每个sheet添加水印
        for (int i = 0; i < sheets; i++) {
            Sheet sheet = wb.getSheetAt(i);
            //excel加密只读
            //sheet.protectSheet(UUID.randomUUID().toString());
            //获取excel实际所占行
            int row = sheet.getFirstRowNum() + sheet.getLastRowNum();
            //获取excel实际所占列
            int cell = sheet.getRow(sheet.getFirstRowNum()).getLastCellNum() + 1;
            //根据行与列计算实际所需多少水印
            ExcelWaterRemarkUtils.putWaterRemarkToExcel(wb, sheet, imgPath, 0, 0, 5, 5, cell / 5 + 1, row / 5 + 1, 0, 0);

            sheet.protectSheet("21212121212121212");
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            wb.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        wb.close();
        byte[] content = os.toByteArray();
        // Excel文件生成后存储的位置。
        File file1 = new File(saveFilePath);
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(file1);
            fos.write(content);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
