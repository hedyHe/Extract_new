package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/3/7.
 * 分离出数据集中的OL.OP,LP的这三种数据
 */

//还原原数据中各个参数的分隔符

public class ReadLabel {

    public static void DeleteSame(List<String> ls){

        for(int i = 0 ; i < ls.size()-1 ; i++){
            for(int j = i+1; j < ls.size() ; j++){
                if(ls.get(i).equals(ls.get(j))){
                    ls.remove(j);
                    j--;
                }
            }
        }
    }

    public static void main(String arg[]){

        String labelname = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\TAB.txt";    //有标签的数据文件
        String org_loc = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\Org_Loc.txt";    //没有标签的数据文件
        String per_org = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\Per_Org.txt";       //写入的文件
        String per_loc = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\Per_Loc.txt";


        int num= 0;    //总line数
        int org_loc_count = 0 ; //被标记的line数
        int per_org_count = 0 ;
        int per_loc_count = 0;

        try{
            File file = new File(labelname);   //读取的文件
            File org_locf = new File(org_loc);
            File per_orgf = new File(per_org);
            File per_locf = new File(per_loc);

            if(!file.exists()){
                System.out.println("the file label doesn't exist!");
            }
            if(!org_locf.exists()){
                org_locf.createNewFile();
            }
            if(!per_locf.exists()){
                per_locf.createNewFile();
            }
            if(!per_orgf.exists()){
                per_orgf.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedWriter outputol = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(org_locf,true)));
            BufferedWriter outputpl = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(per_locf,true)));
            BufferedWriter outputpo = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(per_orgf,true)));
            String label_line = new String();

            int per_index ;  //出现人名的下标
            int org_index;    //出现组织的小标
            int loc_index;

            while((label_line = br.readLine()) != null ){
                per_index = label_line.indexOf("</P>");
                org_index = label_line.indexOf("</O>");
                loc_index = label_line.indexOf("</L>");

                //将pattern写入对应的文件里
                if(per_index > 0 && org_index > 0 ){
                    per_org_count++;
                    outputpo.write(label_line.split("\t")[1]+"\n");

                }
                else{
                    if(per_index > 0 && loc_index > 0){
                        per_loc_count++;
                        outputpl.write(label_line.split("\t")[1]+"\n");
                    }
                    else{
                        if(loc_index > 0 && org_index > 0 ){
                            org_loc_count++;
                            outputol.write(label_line.split("\t")[1]+"\n");
                        }
                    }
                }
                num++;
            }

            System.out.println("一共有："+num+"关系!");
            System.out.println("其中org_loc有："+org_loc_count+"种\n"+"per_org有："+per_org_count+"种\n"+"per_loc有："+per_loc_count+"种");

            br.close();
            outputol.close();
            outputpl.close();
            outputpo.close();

        }catch(Exception e){
            e.printStackTrace();
        }

        return ;
    }

}
