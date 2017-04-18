package Snowball;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Hedy on 2017/3/23.
 * 将之前抽取到的2个以下tuples的pattern删除，并将这些tuples抽取出来，并找出他们的相似度
 */
public class OL_Tuples {

    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    public static void main(String[] args){
        /*String filename = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\final\\OL\\OL_Tuples_3.txt";
        String Tu_sim =  "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\result\\Tu_similarity.txt";
        String write =  "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\result\\Tu_similar_3.txt";*/
        //String write1 = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\result\\Tuples.txt";

        String filename = "Snowball/OL_Tuples_3.txt";
        String  Tu_sim = "Snowball/Tu_similarity.txt";
        String write = "Snowball/Tu_similar_3.txt";
        HashSet<String >  tu = new HashSet<>();
        HashMap<String,Double> sim = new HashMap<>();

        //先将相关的tuples都提取出来
        ReadPaAndTu(filename,tu);
        //WriteToTxt(write1,tu);
        //将有相似度的提取出来
        FindSimilar(Tu_sim,tu,sim);
        //将抽取的部分相似度写入文件
        DBScan.WritepaToTxt(write,sim);

    }
    public static void WriteToTxt(String filename, HashSet<String> tu){
        try {
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            Iterator it = tu.iterator();
            while(it.hasNext()){
                wr.write((String) it.next()+"\n");
            }
            wr.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void FindSimilar(String filename , HashSet<String > tu, HashMap<String ,Double> sim){
        String line ;
        int flag ;
        String first,sec ;
        String tuple = new String();
        double similar;

        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("the file doesn't exist!");
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                flag = 0;
                first = line.split("\t")[0]+"\t"+line.split("\t")[1];
                sec = line.split("\t")[2]+"\t"+line.split("\t")[3];
                Iterator it = tu.iterator();
                while(it.hasNext()){
                    tuple = (String) it.next();
                    if(tuple.equals(first) || tuple.equals(sec)){
                        flag ++;
                        if(flag == 2){
                            break;
                        }
                    }
                }
                if(flag == 2){    //若两个tuples都存在，则记录下这两个tuples的相似度
                    tuple = line.split("\t")[4];
                    //System.out.println(line);
                    similar = Double.parseDouble(df.format(Double.parseDouble(tuple)));
                    sim.put(first+"\t"+sec,similar);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void ReadPaAndTu(String filename, HashSet<String> tu){
        String line ;

        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("the file doesn't exist!");
            }

            int count = 0 ;
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                if(line.contains("<")){ // 判断当前行是否存储的是tuple
                    if(tu.add(line)){
                        continue;
                    }
                    count++;
                }

            }
            System.out.println("删除了"+count);
            System.out.println("相关的tuples一共有："+tu.size());
            br.close();
        }catch (IOException e){
        e.printStackTrace();
        }
    }
}
