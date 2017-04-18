package Snowball;


import jxl.Cell;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import java.io.*;
import java.util.HashSet;


/**
 * Created by Hedy on 2017/4/1.
 */
public class Drift_Pattern {
    public static void main(String[] args){

        //String filename = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_0.35_final.txt";
        //String writefile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_0.35_drift_pattern.txt";

        String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\Label_Cluster_0.06.xls";
        String writefile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\result\\drift_pattern.txt";


        HashSet<String> set = new HashSet<>();
        HashSet<String> drift = new HashSet<>();

        //先将漂移点读取出来
        try {
            File file = new File(writefile);
            if (!file.exists()){
                System.out.println("can't find the file!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line ;
            while ((line = br.readLine()) != null){
                drift.add(line);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        //修改excel表格中漂移点的标志
        File f = new File(filename);
        try {
            Workbook wb = Workbook.getWorkbook(f);
            WritableWorkbook book =  wb.createWorkbook(f,wb);
            WritableSheet st = book.getSheet(0);
            for (int i = 0 ; i < st.getRows() ; i++){
                Cell cell = st.getCell(2,i);
                if(cell.getContents().equals("")){
                    continue;
                }
                if (cell.getContents().equals("F")){
                    continue;
                }
                cell = st.getCell(1,i);
                if(drift.contains(cell.getContents())){
                    Label label = new Label(2,i,"D");
                    st.addCell(label);
                }
            }
            book.write();
            book.close();
            wb.close();
        }catch (Exception e){
            e.printStackTrace();
        }


        /*int count  = 0 ;
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null){
                //首先判断当前读取的是否是pattern
                if(line.equals("")){
                    count++;
                    continue;
                }
                if (line.contains(":")){
                    continue;
                }
                if(line.contains("<")){
                    continue;
                }
                if(!set.add(line)){    //如果加入不成功，则表示该pattern出现在多个类中
                    drift.add(line);
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("所有聚类中一共有:"+set.size()+"个不同的pattern!");
        System.out.println("一共有"+count+"个聚类！");
        try {
            File file  = new File(writefile);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true) ));
            Iterator it = drift.iterator();
            while (it.hasNext()){
                bw.write((String) it.next()+"\n");
            }
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("重复的pattern有:"+drift.size());*/
    }
}
