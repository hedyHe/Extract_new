package Snowball;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.*;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Hedy on 2017/4/10.
 */
public class Excel {
    public static void main(String[] args){
        String readfile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\0.06.txt";
        String writefile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\Cluster\\new_Cluster_0.06.xls";
        //先将分类的情况从文件中读取出来
        List<HashSet<String>> pa_ls = new ArrayList<>();
        List<HashSet<String>> tu_ls = new ArrayList<>();
        List<String> label = new ArrayList<>();
        //ReadFiletoSetList(readfile,tu_ls,pa_ls,label);
        //WriteExcel(writefile,tu_ls,pa_ls,label);
        HashSet<String> pattern = new HashSet<>();
        HashSet<String> tuple = new HashSet<>();
        String tag = "TL";
        //从excel文件中读取该聚类中的所有数据
        ReadExcel(writefile,pattern,tuple,tag);
        System.out.println("the size of pattern is:"+pattern.size());
        Iterator it = pattern.iterator();
        while (it.hasNext()){
            System.out.println(it.next());
        }
        System.out.println("the size of tuple is:"+tuple.size());
        it = tuple.iterator();
        while (it.hasNext()){
            System.out.println(it.next());
        }

    }

    public static void ReadExcel(String filename, HashSet<String> set ,HashSet<String>tu_set, String tag){
        jxl.Workbook readwb =  null;
        try {
            //构建Workbook对象，只读Workbook对象
            //直接从本地文件创建Workbook
            InputStream inputStream = new FileInputStream(filename);
            readwb = Workbook.getWorkbook(inputStream);

            //sheet下标从0开始
            //获取第一张sheet表
            Sheet readsheet = readwb.getSheet(0);
            //获取sheet表中所包含的总列数
            //int rsColums = readsheet.getColumns();
            //获取sheet表中所包含的总行数
            int rsRows  = readsheet.getRows();
            //找到该聚类所开始的行
            int i ;
            for(i = 0 ; i < rsRows ; i++){
                Cell cell = readsheet.getCell(0,i);
                if(cell.getContents().equals("")){    //若当前单元格没有内容，则跳过该循环
                    continue;
                }
                if (cell.getContents().equals(tag)){
                    break;
                }
            }
            String tuple, pa ;
            //从该行开始读取cluster中所有正确的pattern
            for ( ; i < rsRows ; i++){
                Cell tu = readsheet.getCell(3,i);
                Cell pattern = readsheet.getCell(1,i);
                tuple = tu.getContents();
                pa = pattern.getContents();
                if(tuple.equals("") && pa.equals("")){    //若pattern和tuple都读取结束，则结束当前循环
                    break;
                }
                if (!tuple.equals("")){
                    tu_set.add(tuple);
                }
                Cell flag = readsheet.getCell(2,i);
                //System.out.println(flag.getContents());
                if (flag.getContents().equals("T")){    //若当前的pattern是正确的，则将该pattern加入集合中
                    //System.out.println(pa);
                    set.add(pa);
                }
            }
            readwb.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void WriteExcel(String filename,List<HashSet<String>> tu_ls , List<HashSet<String>> pa_ls , List<String> label) {

        try {
            WritableWorkbook book = Workbook.createWorkbook(new File(filename));
            WritableSheet sheet = book.createSheet("OL", 0);
            sheet.setColumnView(0, 25);    //设置列的宽度
            sheet.setColumnView(1, 35);
            sheet.setColumnView(2, 30);

            WritableFont wf = new WritableFont(WritableFont.ARIAL, 15, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.CORAL);
            WritableCellFormat wcf = new WritableCellFormat(wf);
            wcf.setBackground(Colour.BLACK);
            wcf.setAlignment(Alignment.CENTRE);

            sheet.addCell(new Label(0, 0, "label", wcf));
            sheet.addCell(new Label(1, 0, "pattern", wcf));
            sheet.addCell(new Label(2, 0, "classification", wcf));
            sheet.addCell(new Label(3, 0, "tuple", wcf));
            //sheet.mergeCells(5,0,10,0);
            String tuple, pattern;
            HashSet<String> pa ;
            int num = 0;
            int count = 1;   //记录pattern的个数
            System.out.println("pattern的大小是："+pa_ls.size()+"tuple的大小是："+tu_ls.size());
            for (int i = 0; i < pa_ls.size(); i++) {
                pa = pa_ls.get(i);
                num = 0;
                sheet.addCell(new Label(0,count,label.get(i)));//String.valueOf(pa.size())));   //输出该聚类中的pattern数
                //依次写入pattern
                Iterator pa_it = pa.iterator();
                while (pa_it.hasNext()) {
                    pattern = (String) pa_it.next();   //读取当前的pattern，写入表格中
                    sheet.addCell(new Label(1,count+(num++),pattern));
                }
                //依次写入tuples
                num = 0 ;
                Iterator it = tu_ls.get(i).iterator();
                while (it.hasNext()) {
                    tuple = (String) it.next();
                    sheet.addCell(new Label(3, count+(num++), tuple));
                }
                num = pa.size() > tu_ls.get(i).size() ? pa.size() : tu_ls.get(i).size();    //找出最大的输出行
                count += num;
                count++;    //每个分类之间空一行
            }
            book.write();
            book.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
