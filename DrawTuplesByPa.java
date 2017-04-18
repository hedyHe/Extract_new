package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/3/29.
 * 将所有的tuples按照pattern分类
 */
public class DrawTuplesByPa {
    public static void main(String[] args){
        String datafile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_OL.txt";
        String writefile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_tuple_pattern_OL.txt";
        String writefile1 = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_tuple3_pattern_OL.txt";
        //String datafile = "OL_TAB.txt";
        //String writefile = "Snowball/tuple_pattern_OL.txt";
        //String writefile1 = "Snowball/tuple3_pattern_OL.txt";

        HashMap<String , HashSet<String>> tuples = new HashMap<>();
        int count = 2 ;
        //根据pattern对tuples进行分类
        DivideData(datafile,tuples);
        //并将抽取到的tuple数的大于阈值的pattern提取出来
        Drive(writefile1,tuples,count);
        //将分类的结果写入文件中
        DrawPaByTuples.WriteToTxt(writefile,tuples);

    }
    public static void Drive(String filename, HashMap<String , HashSet<String>> tuples, int count){
        HashMap<String, HashSet<String>> final_tu = new HashMap<>();
        HashSet set ;
        String pa ;

        Iterator it = tuples.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            set = (HashSet) entry.getValue();
            if(set.size() > count){    //若该pattern抽取的tuples数大于2，则将该pattern的信息提取出来
                pa = (String) entry.getKey();
                final_tu.put(pa,set);
            }
        }

        //将提取出来的结果写入文件中
        DrawPaByTuples.WriteToTxt(filename,final_tu);

    }

    public static void DivideData(String filename , HashMap<String , HashSet<String>>tuples){
        try {
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            HashSet<String> set = new HashSet<>() ;
            HashSet<String> pa = new HashSet<>();
            String arg1, arg2,pattern;
            String org,loc;
            String line ;
            Iterator it;
            while ((line = br.readLine()) != null){
                arg1 = line.split("\t")[0];
                arg2 = line.split("\t")[2];
                pattern = line.split("\t")[1];
                org = arg1.contains("</O>")?arg1:arg2;
                loc = arg2.contains("</L>") ? arg2:arg1;
                //判断这个pattern之前是否被抽取过
                if(pa.contains(pattern)){   //若存在，则直接添加tuple
                    set = tuples.get(pattern);
                    set.add(org+"\t"+loc);
                    tuples.put(pattern,set);
                    continue;
                }
                if(pa.add(pattern)){    //若没有抽取过，则建立一个新的映射
                    set = new HashSet<>();
                    set.add(org+"\t"+loc);
                    tuples.put(pattern,set);
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

}


