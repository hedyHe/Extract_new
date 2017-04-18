package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/3/23.
 * 根据tuples的相似度计算pattern的相似度
 */
public class DBSCan_Map {

    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    public static void main(String[] args){
       /*String patternname = "Snowball/OL_Tuples_3.txt";
        String writetxtname1 = "Snowball/Tu_similar_3.txt";
        String writetxtname2 = "Snowball/Pa_similarity_3.txt";
        String delete_similarity = "Snowball/de_Pa_Similarity_3.txt";
        String finalfile = "Snowball/final_cluster_3.txt";
        String first = "Snowball/first_Pa_Similar_3.txt";
        String merge = "Snowball/merge.txt";*/
        String patternname = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\final\\OL\\OL_Tuples_3.txt";
        String writetxtname1 = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\result\\Tu_similarity.txt";
        String writetxtname2 = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\Pa_similarity.txt";
        String first = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\First_Pa_similar.txt";
        String delete_similarity =  "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\de_Pa_similarity.txt";
        String finalfile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\Cluster_Pattern.txt";
        String merge = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\merge.txt";

        HashMap<String,Double> tuplemap = new HashMap<String, Double>();
        HashSet<String> tupleset = new HashSet<>();
        HashSet<List<String>> pattern_set = new HashSet<>();
        System.out.println("读取pattern!");
        ReadPatternToSet(patternname,pattern_set);
        System.out.println("pattern的个数为："+pattern_set.size());
        //文件中读取计算出的tuples对的相似度，并将涉及到的tuples保存到一个set中
        DBScan.ReadMap(writetxtname1,tuplemap,tupleset);
        System.out.println("tuples相似度的对数为："+tuplemap.size());
        System.out.println("涉及到的tuples数为："+tupleset.size());

        /*HashMap<String ,Double> similarity_pa = new HashMap<>();    //保存每对pattern之间的相似度
        HashMap<String , Double> delete_pa = new HashMap<>();     //合并pattern时删除的相似度
        //计算pattern之间的相似度
        PatternSimile(pattern_set, tuplemap , similarity_pa ,tupleset);
        DBScan.WritepaToTxt(first,similarity_pa);
        String pa = new String();
        //是否存在比阈值大的相似度
        boolean flag = DBScan.FindMax(similarity_pa,pa,delete_pa);
        int count = 0 ;
        //若有，则进行聚类
        while(flag){
            //将找到的两个pattern合并，并重新计算新和成的pattern与其他pattern之间的相似度
            NewPtternandCacul(pattern_set,tuplemap,similarity_pa,pa,tupleset);
            //将合并的两个patter写入文件中
            WriteToText(merge,pa,count);
            System.out.println("合并到第"+(++count)+"轮！");
            //找到最大的相似度，并把这两个pattern相关的相似度删除
            pa = null;
            flag = DBScan.FindMax(similarity_pa,pa,delete_pa);
        }
        System.out.println("将计算出pattern的相似度写入文件！");
        DBScan.WritepaToTxt(delete_similarity,delete_pa);
        DBScan.WritepaToTxt(writetxtname2 , similarity_pa);
        System.out.println("similarity_pa的大小为:"+similarity_pa.size());
        WriteToTxt(finalfile,pattern_set);
        System.out.println("pattern的大小为:"+pattern_set.size());*/

    }
    public static void ReadMap(String filename , HashMap<String , Double> map ,HashSet<String> set){

        double thred = 0.05;

        System.out.println("从文件中读取tuples的相似度：");
        try{
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("the file doesn't exist!");
            }
            String pa = null ;
            BufferedReader br = new BufferedReader(new FileReader(file));
            String[] t1 = new String[2];
            String[] t2 = new String[2];
            double s = 0 ;

            while((pa = br.readLine()) != null){
                s = Double.parseDouble(df.format(Double.parseDouble(pa.split("\t")[4])));
                if(s < thred){
                    continue;
                }
                //System.out.println(pa);
                t1[0] = pa.split("\t")[0];     //org
                t1[1] = pa.split("\t")[1];       //loc
                t2[0] = pa.split("\t")[2];    //org
                t2[1] = pa.split("\t")[3];    //loc
                //将涉及到的两个tuples保存到set中
                set.add(t1[0]+"\t"+t1[1]);
                set.add(t2[0]+"\t"+t2[1]);
                //map.put(t1[0]+"\t"+t1[1]+"\t"+t2[0]+"\t"+t2[1] , s);

            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("ending!");
    }


    //将新合并的两个pattern写入文件中
    public  static  void WriteToText(String filename , String pa , int count){
        try {
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("fail!");
            }
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            wr.write(count+" merge:\n");
            for(int i =  0 ;  i < pa.split("\t").length ; i++){
                wr.write(pa.split("\t")[i]+"\n");
            }
            wr.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //将最后pattern的聚类效果输出
    public static void WriteToTxt(String filename , HashSet<List<String>> pa){
        List<String > p ;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            Iterator it = pa.iterator();
            while(it.hasNext()){
                p = (List<String>) it.next();
                for(int i =0 ; i < p.size() ; i++ ){
                    wr.write(p.get(i)+"\n");
                }
                wr.write("\n");
            }
            wr.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static boolean NewPtternandCacul(HashSet<List<String>> pa ,HashMap<String , Double > tuplemap ,HashMap<String ,Double > sim_pa ,String pattern,  HashSet<String> set){
        //pattern是要聚类的两个pattern，pa是保存pattern抽取到的tuples的集合，tuplemap保存的是每对tuples之间的相似度，set保存的是存在相似度的tuples
        Iterator it = pa.iterator();
        String pa1 = pattern.split("\t")[0];
        String pa2 = pattern.split("\t")[1];
        List<String> p ;
        List<String> p1 = new ArrayList<>();
        List<String> p2 = new ArrayList<>();
        int flag = 0 ;
        while(it.hasNext()){
            p = (List<String>) it.next();
            if( pa1.equals(p.get(0))){
                p1 = p;
                it.remove();
                flag++;
                if(flag == 2){
                    break;
                }
                continue;
            }
            if( pa2.equals(p.get(0))){
                p2 = p ;
                it.remove();
                flag++;
                if(flag == 2){
                    break;
                }
                continue;
            }
        }
        if(flag !=2 ){
            System.out.println("没有找到这两个pattern!");
            return false;
        }
        System.out.println("删除后的pattern数："+pa.size());
        //否则将这两个pattern合并
        p1.remove(0);
        p2.remove(0);
        p2.removeAll(p1);
        p1.addAll(p2);
        p1.add(0,pattern);

        //计算新的pattern与其他pattern之间的相似度
        it = pa.iterator();
        while(it.hasNext()){
            p = (List<String>) it.next();
            CaculPaSim(p1,p,tuplemap,sim_pa,set);
        }

        //将新的pattern加入set中
        pa.add(p1);
        return true;
    }

    public static void PatternSimile(HashSet<List<String>> pa ,HashMap<String , Double > tuplemap ,HashMap<String , Double > sim_pa , HashSet<String> set) {

        System.out.println("计算pattern列表之间的相似度!");
        List<String> pa1;
        List<String> pa2;
        String tu1, tu2, tu;
        int flag;

        Iterator it  = pa.iterator();
        while(it.hasNext()){
            pa1 = (List<String>) it.next();
            tu1 = pa1.get(0);
            Iterator iter  = pa.iterator();
            while(iter.hasNext()){
                pa2 = (List<String>) iter.next();

                tu2 = pa2.get(0);
                if(tu1.equals(tu2)){   //若是相同的pattern，则不需要比较
                    continue;
                }
                //否则先查看这两个pattern是否已经计算过相似度了
                flag = 0 ;
                Iterator sim_pa_iter =  sim_pa.entrySet().iterator();
                while(sim_pa_iter.hasNext()){
                    Map.Entry entry = (Map.Entry)sim_pa_iter.next();
                    tu = (String) entry.getKey();
                    if((tu1+"\t"+tu2).equals(tu) || (tu2+"\t"+tu1).equals(tu)){
                        flag = 1;
                        break;
                    }
                }
                if(flag == 1){     //若已经计算过了则调到下一个循环
                    break;
                }
                //否则计算这两个pattern的相似度，并将结果保存到相似度Map中
                CaculPaSim(pa1,pa2,tuplemap,sim_pa,set);
            }
        }
    }

    //计算两个pattern之间的相似度
    public static void CaculPaSim(List<String> pa1, List<String> pa2 , HashMap<String , Double > tuplemap ,HashMap<String , Double > sim_pa , HashSet<String> set){

        String tu1,tu2,iter,tu;
        Double similarity = 0.0 ;
        int flag ;
        //依次比较两个pattern的tuples，计算其相似度,用最小的tuples列表
        List<String> temp = new ArrayList<>() ;
        if(pa1.size() > pa2.size()){
            temp = pa1 ;
            pa1 = pa2;
            pa2 = temp;
        }
        double  max ;
        for(int k = 1 ; k < pa1.size() ; k++){
            tu1 = pa1.get(k);
             max = 0.0 ;
            for(int m = 1 ; m < pa2.size() ; m++){
                tu2 = pa2.get(m);
                if(tu1.equals(tu2)){
                    max = 1;
                    break;
                }
                //否则，首先判断这两个tuples是否都有相似度，若没有则直接跳转到一下个比较
                Iterator iterator = set.iterator();
                flag = 0 ;
                while(iterator.hasNext()){
                    iter = (String) iterator.next();
                    if(iter.equals(tu1) || iter.equals(tu2)){
                        flag++;
                    }
                    if(flag == 2){
                        break;
                    }
                }
                if(flag != 2 ){    //则说明最少有一个tuples是没有相似度的，可以直接跳转到下一次的比较
                    continue;
                }
                Iterator it = tuplemap.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry entry = (Map.Entry) it.next();
                    tu = (String) entry.getKey();
                    if(tu.equals(tu1+"\t"+tu2) || tu.equals(tu2+"\t"+tu1)){   //若找到该tuples组合，则相似度相加
                        if( (Double) entry.getValue() > max){
                            max = (Double ) entry.getValue();
                        }
                    }
                }
            }
            similarity += max ;
        }
        if(similarity == 0){
            return;
        }
        //若存在相似度，则将相似度保存到set集合中
        int denom = pa1.size() - 1 ;
        double similar = Double.parseDouble(df.format(similarity * 1.0 / denom));
        sim_pa.put(pa1.get(0)+"\t"+pa2.get(0),similar);
        System.out.println(pa1.get(0)+"\t"+pa2.get(0)+"\t"+similar+"\t"+ similarity + "\t"+pa1.size() +"\t"+pa2.size());
        return;
    }

    public static void ReadPatternToSet(String filename , HashSet<List<String>> pa){

        String line ;
        int flag = 1 ;   //记录下一行是否是一个新的pattern
        List<String> newls = null ;
        String pattern ,loc = new String(),org = new String();
        String arg1, arg2;

        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("the file doesn't exist!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((line = br.readLine()) != null){
                if(line.equals("")){
                    flag = 1;     //下一行开始抽取新的pattern
                    pa.add(newls);
                    continue;
                }
                if(flag == 1) {   //如果当前是新的pattern，则将该pattern存入List中
                    pattern = line.split("\t")[0];
                    newls = new ArrayList<String>();   //将pattern添入列表
                    newls.add(pattern);
                    flag = 0 ;
                    continue;
                }
                //否则将tuples加入列表中
                arg1 =line.split("\t")[0];
                arg2 = line.split("\t")[1];
                org = arg1.contains("</O>") ? arg1 : arg2 ;
                loc = arg2.contains("</L>") ? arg2 : arg1 ;
                newls.add(org+"\t"+loc);
            }
            pa.add(newls);
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
