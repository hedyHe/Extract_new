package Snowball;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Hedy on 2017/3/29.
 * 根据tuples将pa进行分类
 */
public class DrawPaByTuples {
    public static void main(String[] args){
        String datafile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_OL.txt";
        String writename = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_pattern_tuple_OL.txt";

        //String datafile = "OL_TAB.txt";
        //String writename = "Snowball/pattern_tuple_OL.txt";

        HashMap<String , HashSet<String>> tuples = new HashMap<>();
        //根据tuples对patterns进行分类
        DivideData(datafile,tuples);
        //将分类的结果写入文件中
        WriteToTxt(writename,tuples);

    }

    public static void WriteToTxt(String filename , HashMap<String , HashSet<String>> tuples){
        try {
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            Iterator it = tuples.entrySet().iterator();
            HashSet<String> set ;
            Iterator setit ;
            while(it.hasNext()){
                Map.Entry entry =(Map.Entry) it.next();      //获取当前的tuples
                wr.write(entry.getKey().toString()+"\n");
                set = (HashSet) entry.getValue();
                setit = set.iterator();      //将该tuples抽取到的所有的pattern的依次写入文件
                while(setit.hasNext()){
                    wr.write(setit.next().toString()+"\n");
                }
                wr.write("\n");    //每两个分类之间间隔一行
            }
            wr.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void DivideData(String filename, HashMap<String , HashSet<String>> tuples){

        HashSet<String> tu = new HashSet<>();
        String org,loc ;
        String arg1 = new String();
        String arg2  = new String();
        String pattern = new String();
        try {
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = new String();
            Iterator it ;
            String tuple = new String() ;
            HashSet<String> set = new HashSet() ;

            while ((line = br.readLine()) != null){
                arg1 = line.split("\t")[0];
                arg2 = line.split("\t")[2];
                org = arg1.contains("</O>") ? arg1:arg2;
                loc = arg2.contains("</L>") ? arg2:arg1;
                pattern = line.split("\t")[1];

                if(tu.contains(org+"\t"+loc)){    //先判断该tuples是否出现过,若出现过更换后面的pattern
                    set = tuples.get(org+"\t"+loc);
                    set.add(pattern);
                    tuples.put(org+"\t"+loc,set);
                    continue;
                }
                //否则添加新的tuple
                if(tu.add(org+"\t"+loc)){    //如果添加成功，则说明该tuples之前没有出现过
                    set = new HashSet();
                    set.add(pattern);
                    tuples.put(org+"\t"+loc,set);
                }

            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

}
