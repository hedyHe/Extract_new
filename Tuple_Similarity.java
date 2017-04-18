package Snowball;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Hedy on 2017/3/29.
 * 根据tuples与pattern的二分图，计算tuples的相似度
 */
public class Tuple_Similarity {

    private static final double threshold = 0.05 ;
    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    public static void main(String[] args){

        String tuplename = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_tuple3_pattern_OL.txt";          //根据pattern抽取的tuples分类
        String patternname = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_pattern_tuple_OL.txt";         //根据tuples抽取的pattern分类
        String use_tuple = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\use_PT.txt";
        String writetxtname1 = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_Tu_similarity.txt";
        /*String tuplename = "Snowball/tuple_pattern_OL.txt";
        String writetxtname1 = "Snowball/Tu_similarity.txt";
        String use_tuple = "Snowball/use_PT.txt";
        String patternname = "Snowball/pattern_tuple_OL.txt";*/

        HashMap<String,Double> tuplemap = new HashMap<String, Double>();    //保存是两个tuples之间的相似度
        List<List<String>> tuples = new ArrayList<>();     //保存的是每个tuples抽取到的pattern
        HashMap<String, HashSet<String>> tup_pa = new HashMap<>();

        HashSet<String> tup_set = new HashSet<>();
        //现将需要计算相似度的tuple抽取出来
        DrawTuples(tuplename,tup_set);
        System.out.println("一共需要计算相似度的tuples有："+tup_set.size());
        //将这些tuple所对应的pattern也抽取出来
        DrawPatterns(patternname,tup_set,tup_pa);
        //将需要计算相似度的tuple写入文件中
        DrawPaByTuples.WriteToTxt(use_tuple,tup_pa);
        //计算tuple之间的相似度
        ReadAndCaculTuple(use_tuple,tuples,tuplemap);
        //写入文件夹中
        DBScan.WritepaToTxt(writetxtname1,tuplemap);

    }

    public static void DrawPatterns(String filename, HashSet<String> set , HashMap<String , HashSet<String>> map){
        try {
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            int flag = 0;
            String line = new String();
            String tu = new String();
            HashSet<String> map_set = new HashSet<>();
            while((line = br.readLine()) != null){
                if(line.contains("</L>")){   //若当前行是tuple，则先判断是否在计算范围内，若在，则将其抽取的pattern提取出来
                    if(set.contains(line)){   //若包含,则将标志设置为1
                        flag = 1;
                        tu = line;   //记录下需要保存的tuple
                        map_set = new HashSet<>();
                        continue;
                    }
                    else {
                        flag = 0;
                        continue;
                    }
                }
                //先判断是否需要提取该pattern
                if(flag == 0){
                    continue;
                }
                if(line.length() == 0){   //若当前tuple的所有信息都抽取结束
                    if(flag == 1){   //判断是否需要保存
                        map.put(tu,map_set);
                        continue;
                    }
                    //并将集合清空
                    set.clear();
                }
                //否则将pattern保存在集合中
                map_set.add(line);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void DrawTuples(String filename, HashSet<String> set){
        int count = 0 ;
        try {
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = new String();
            while ((line = br.readLine()) != null){
                if(line.contains("</L>")){   //若当前行保存的是tuple的信息，则保存到set中，文件中的所有tuple都已经是org_loc形式的
                    set.add(line);
                }
                if(line.length() == 0 ){
                    count++;
                }
            }
            System.out.println("一共有"+count+"个patterns!");
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void ReadAndCaculTuple(String filename, List<List<String>> ls, HashMap<String,Double> tuples){

        System.out.println("读取tuples并计算其相似度:");
        List<String> pa = null;
        String line = new String();
        String org,loc, new_pa ;
        String tuple ;
        int same_pattern , length;
        double similarity ;
        HashSet set = new HashSet() ;

        int count = 0 ;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("the file doesn't exist!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                if(line.contains("</O>")){        //表示当前读取得是tuples的信息
                    if(count > 1){                //若之前已经有tuples被存储了，就需要计算该tuple与前面的tuples之间的相似度
                        length = pa.size();
                        for(int i = 0 ; i < count - 1 ; i++){     //找到两个列表中不同的pattern数
                            set.clear();
                            //将两个列表中的pattern加入集合中，以便计算
                            for(int j = 1; j < ls.get(i).size() ; j++) {
                                set.add(ls.get(i).get(j));
                            }
                            for(int k = 1; k < length ; k++){
                                set.add(pa.get(k));
                            }
                            same_pattern = ls.get(i).size() + length - 2 - set.size();   //计算出两个列表之间相同的pattern数
                            if(same_pattern > 0){   //如果找到相同的pattern就进行计算相似度，否则直接删除

                                similarity = same_pattern * 1.0 /set.size();

                                if( count % 100 == 0 ){   //中间输出结果
                                    System.out.println(ls.get(i)+"\t"+pa+"\t"+same_pattern+"\t"+set+similarity);
                                    System.out.println(ls.get(i).get(0)+"\t"+pa.get(0)+"\t"+similarity);
                                }
                                tuples.put(ls.get(i).get(0)+"\t"+pa.get(0),Double.parseDouble(df.format(similarity)));
                                //tuples.put(pa.get(0)+"\t"+ls.get(i).get(0),similarity);
                            }
                        }
                    }
                    if(count > 0 ){
                        //将当前抽取好的tuple的pattern列表添加入整个列表中
                        ls.add(pa);
                    }
                    org = line.split("\t")[0].contains("</O>") ? line.split("\t")[0]:line.split("\t")[1];
                    loc = line.split("\t")[1].contains("</L>") ? line.split("\t")[1]:line.split("\t")[0];
                    tuple = org+"\t"+loc;
                    count++;
                    pa = new ArrayList<>();
                    pa.add(tuple);
                    continue;
                }
                else{
                    if(line.length() == 0){
                        continue;
                    }
                    //当前行如果之间保存的是pattern，则直接保存到当前列表中
                    pa.add(line);
                }
            }
            //最后一个tuples并没有和前面抽取的tuples进行相似度的计算
            for(int i = 0 ; i < ls.size() ; i++ ){
                set.clear();
                for(int j = 1; j < ls.get(i).size() ; j++) {
                    set.add(ls.get(i).get(j));
                }
                for(int k = 1; k < pa.size() ; k++ ) {
                    set.add(pa.get(k));
                }
                same_pattern = ls.get(i).size() + pa.size() -2 - set.size();   //计算相同的pattern的个数
                if(same_pattern > 0 ){
                    similarity = same_pattern * 1.0 /set.size();
                    tuples.put(ls.get(i).get(0)+"\t"+pa.get(0), Double.parseDouble(df.format(similarity)));
                }
            }
            //将最后一个tuples的列表添加入整个列表中
            ls.add(pa);
            br.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("ending!");
    }
}
