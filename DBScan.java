package Snowball;

import com.csvreader.CsvWriter;
import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/3/17.
 * 根据已求得的tuples的相似度，对pattern的相似度进行计算，并进行聚类
 */

public class DBScan {

    private  static double threshold = 0.1 ;
    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    public static void main(String[] args){
        /* String tuplename = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\pattern_tuple_OL.txt";
        String patternname = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\OL_Tuples.txt";
        String writename1 = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\Tu_similarity.csv";
        String writetxtname1 = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\result\\Tu_similarity.txt";
        String writetxtname2 = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\Pa_similarity.txt";
        String no_similarity = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\no_Pa_similarity.txt";
        String writename2 = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\Pa_similarity.csv";
        String patternsimilarity = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\PatternSimi.txt";*/

       String writename1 = "Snowball/Tu_similarity.csv";
        String writename2 = "Snowball/Pa_similarity.csv";
        String tuplename = "Snowball/pattern_tuple_OL.txt";
        String patternname = "Snowball/OL_Tuples.txt";
        String writetxtname1 = "Snowball/Tu_similarity.txt";
        String writetxtname2 = "Snowball/Pa_similarity.txt";
        String delete_similarity = "Snowball/de_Pa_Similarity.txt";
        String finalfile = "Snowball/final_cluster.txt";

        List<List<String>> pattern = new ArrayList<>();
        HashMap<String,Double> tuplemap = new HashMap<String, Double>();
        HashSet<String> tupleset = new HashSet<>();
        //ReadTupleAndCacul(tuplename,tuple,tuplemap);
        //ReadAndCaculTuple(tuplename,tuple,tuplemap);
        //WriteToTxt(writetxtname1,tuplemap);
        //WriteToExcel(writename1,tuplemap);
        //文件中读取计算出的tuples对的相似度，并将涉及到的tuples保存到一个set中
        ReadMap(writetxtname1,tuplemap,tupleset);
        //WriteToExcel(writename1,tuplemap);
        System.out.println("一共抽取了:"+tuplemap.size());
        System.out.println("读取pattern!");
        Classify_Pattern.ReadPattern(patternname,pattern);
        System.out.println("pattern的个数为："+pattern.size());

        HashMap<String ,Double> similarity_pa = new HashMap<>();    //保存每对pattern之间的相似度
        HashMap<String , Double> delete_pa = new HashMap<>();     //合并pattern时删除的相似度

        boolean flag ;
        //将pattern分成几个子表进行比较
        List<List<String>>[] sub_pattern = new ArrayList[100];
        int size = 100;          //一共分成了多少个子集
        DivideToSublist( pattern, sub_pattern , size);
        for(int i = 0 ; i < size ; i++){
            //System.out.println("计算内部的pattern相似度："+i);
            PatternSimilarity(sub_pattern[i],tuplemap,tupleset,similarity_pa );
        }
        //计算列表之间的相似度
        System.out.println("计算列表之间的相似度:");
        for(int i = 0  ; i < size -1 ; i++){
            for(int j = i + 1 ; j < size ; j++ ){
                PatternSimile(sub_pattern[i] , sub_pattern[j],tuplemap , similarity_pa ,tupleset);
            }
        }
        //找出相似度最大的两个pattern
        String pa = null ;
        int count = 1 ;
        flag = FindMax(similarity_pa,pa,delete_pa);
         //若最大相似度小于阈值，则退出程序
        while(flag){
            //将找到的两个pattern合并，并重新计算新和成的pattern与其他pattern之间的相似度
            NewPtternandCacul(pattern,tuplemap,similarity_pa,pa,tupleset);
            System.out.println("合并到第"+(++count)+"轮！");
            flag = FindMax(similarity_pa,pa,delete_pa);
        }
        System.out.println("将计算出pattern的相似度写入文件！");
        WritepaToTxt(delete_similarity,delete_pa);
        WritepaToTxt(writetxtname2 , similarity_pa);
        System.out.println("similarity_pa的大小为:"+similarity_pa.size());
        WriteToTxt(finalfile,pattern);
        System.out.println("pattern的大小为:"+pattern.size());
    }

    //将最好产生的聚类效果输出到文本文件中
    public static void WriteToTxt(String filename, List<List<String>> pattern){
        try {
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            for(int i = 0 ; i < pattern.size() ; i++){
                wr.write(pattern.get(i).get(0)+"\n");
            }
            wr.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    //找到当前相似度最大的两个pattern，并删除与这两个pattern相关的所有相似度
    public static  boolean  FindMax(HashMap<String , Double >  sim_pa, String pa, HashMap<String,Double> delete){

        double max = 0 ;
        String tu;
        Iterator it = sim_pa.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            tu = (String) entry.getKey();
            if((Double) entry.getValue() > max){
                max = (Double)entry.getValue();
                pa = tu;
            }
        }
        if( max > threshold ){   //若最大相似度大于阈值，则下一步需要聚类，这边现将pattern相关的相似度删除
            String pa1= pa.split("\t")[0];
            String pa2 = pa.split("\t")[1];
            it = sim_pa.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                tu = (String) entry.getKey();
                if(tu.contains(pa1+"\t") || tu.contains("\t"+pa1)){
                    delete.put(tu,(Double) entry.getValue());
                    it.remove();
                }
                else {
                    if(tu.contains(pa2+"\t") || tu.contains("\t"+pa2)){
                        delete.put(tu,(Double) entry.getValue());
                        it.remove();
                    }
                }
            }
            return  true;
        }
        return  false;
    }

    //计算两个列表之间的相似度
    public static void PatternSimile(List<List<String>>sub1,List<List<String >>sub2 ,HashMap<String , Double > tuplemap ,HashMap<String , Double > sim_pa , HashSet<String> set){

        System.out.println("计算pattern列表之间的相似度!");
        List<String > pa1;
        List<String > pa2 ;
        String tu1,tu2,tu,iter;
        int denom , flag ;
        double similarity ;
        for(int i = 0 ; i < sub1.size() ;i ++){
            pa1 = sub1.get(i);
            for(int j = 0 ; j < sub2.size() ; j++){
                pa2 = sub2.get(j);
                similarity = 0 ;
                //依次比较两个pattern的tuples，计算其相似度
                for(int k = 1 ; k < pa1.size() ; k++){
                    tu1 = pa1.get(k);
                    for(int m = 1 ; m < pa2.size() ; m++){
                        tu2 = pa2.get(m);
                        if(tu1.equals(tu2)){
                            similarity += 1;
                            continue;
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
                                similarity +=(Double) entry.getValue();
                                break;
                            }
                        }
                    }
                }
                if(similarity == 0){
                    continue;
                }
                denom = Math.max(pa1.size() , pa2.size()) - 1 ;
                similarity = similarity *1.0 /denom;
                tu1  =pa1.get(0);
                tu2 = pa2.get(0);
                sim_pa.put(tu1+"\t"+tu2,similarity);    //将这两个pattern的相似度放入map中
            }
        }
    }

    public static boolean  NewPtternandCacul(List<List<String>> pattern ,HashMap<String , Double > tuplemap , HashMap<String , Double >sim_pa,String pa,HashSet<String> set){

        String pa1 = pa.split("\t")[0];
        String pa2 = pa.split("\t")[1];
        int first = 0 ,second = 0 , flag = 0;

        for(int i = 0 ; i < pattern.size() ; i++) {
            if (pattern.get(i).get(0).equals(pa1)) {
                first = i;
                flag++;
            } else{
                if(pattern.get(i).get(0).equals(pa2)){
                    second = i;
                    flag ++;
                }
            }
            if(flag == 2){
                break;
            }
        }

        if(flag == 2){    //若找到两个pattern，则进行合并
            List<String> p1 = new ArrayList<>();
            List<String > p2 = new ArrayList<>();
            p1 = pattern.get(first);
            p2 = pattern.get(second);
            p1.remove(0);
            p2.remove(0);
            p2.removeAll(p1);
            p1.addAll(p2);
            pattern.remove(first);
            pattern.remove(second);
            p1.add(0,pa);
            pattern.add(0,p1);
            RegainCacul(pattern,tuplemap,sim_pa,set);
            return true;
        }
        else {
            System.out.println("没有找到这两个pattern,不能进行合并!");
            return false;
        }
    }

    public static void RegainCacul(List<List<String>> pattern ,HashMap<String, Double> tuple, HashMap < String, Double > sim_pa,HashSet<String> set){

        System.out.println("重新计算合并pattern之后的相似度!" );
        List<String> pa1 = new ArrayList<>();
        List<String> pa2  = new ArrayList<>();
        String tu ;
        double similarity;
        String tu1 , tu2 , iter;
        int denom ,flag;

        pa1 = pattern.get(0);
        for (int j =  1; j < pattern.size(); j++) {
            pa2 = pattern.get(j);
            //计算相似度
            similarity = 0;
            for (int k = 1 ; k < pa1.size() ; k++) {
                tu1 = pa1.get(k);
                for (int m = 1 ; m < pa2.size() ; m++) {
                    tu2 = pa2.get(m);
                    if(tu1.equals(tu2)){
                        similarity += 1 ;
                    }
                    Iterator iterator = set.iterator();
                    flag = 0 ;
                    while(iterator.hasNext()){
                        iter = (String) iterator.next();
                        if(iter.equals(tu1)||iter.equals(tu2)){
                            flag++;
                            if(flag == 2){
                                break;
                            }
                        }
                    }
                    if(flag != 2){
                        continue;
                    }
                    Iterator it = tuple.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry entry = (Map.Entry) it.next();
                        tu = (String) entry.getKey();
                        if ((tu1+"\t"+tu2).equals(tu) || (tu2+"\t"+tu1).equals(tu)) {
                            similarity += (Double) entry.getValue();   //相似度相加
                            break;
                        }
                    }
                }
            }
            if(similarity == 0 ){
                continue;
            }
            denom = Math.max(pa1.size(), pa2.size()) - 1;
            similarity = Double.parseDouble(df.format(similarity *1.0 /denom));
            System.out.println(pa1.get(0)+"\t"+pa2.get(0)+similarity);
            sim_pa.put(pa1.get(0)+"\t"+pa2.get(0),similarity);
        }
    }

    //计算列表内部的pattern之间的相似度
    public static void  PatternSimilarity(List<List<String>> pattern ,HashMap<String, Double> tuple, HashSet<String> set, HashMap < String, Double > sim_pa ){

        //System.out.println("计算pattern列表内部的相似度!" );
        List<String> pa1 = new ArrayList<>();
        List<String> pa2  = new ArrayList<>();
        String tu ;
        int denom,flag;
        double similarity;
        String tu1 , tu2 ;
        String iter;
        for(int i = 0 ; i < pattern.size() - 1 ; i++ ) {
            pa1 = pattern.get(i);
            for (int j = i + 1; j < pattern.size(); j++) {
                pa2 = pattern.get(j);
                //计算相似度
                similarity = 0;
                for (int k = 1 ; k < pa1.size() ; k++) {
                    tu1 = pa1.get(k);
                    for (int m = 1 ; m < pa2.size() ; m++) {
                        tu2 = pa2.get(m);
                        if(tu1.equals(tu2)){
                            similarity += 1;
                            continue;
                        }
                        //否则，首先判断这两个tuples是否都有相似度，若没有则直接跳转到一下个比较
                        Iterator iterator = set.iterator();
                        flag = 0 ;
                        while(iterator.hasNext()){
                            iter = (String) iterator.next();
                            if(iter.equals(tu1) || iter.equals(tu2)){
                                flag++;
                                if(flag == 2){
                                    break;
                                }
                            }

                        }
                        if(flag != 2 ){    //则说明最少有一个tuples是没有相似度的，可以直接跳转到下一次的比较
                            continue;
                        }
                        //否则需要找到这对tuples的相似度
                        Iterator it = tuple.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            tu = (String) entry.getKey();
                            if ((tu1+"\t"+tu2).equals(tu) || (tu2+"\t"+tu1).equals(tu)) {
                                similarity += (Double) entry.getValue();   //相似度相加
                                break;
                            }
                        }
                    }
                }
                if(similarity == 0 ){
                    continue;
                }
                denom = Math.max(pa1.size(), pa2.size() ) - 1;
                similarity = Double.parseDouble(df.format(similarity *1.0 /denom));
                System.out.println(pa1.get(0)+"\t"+pa2.get(0)+similarity);
                sim_pa.put(pa1.get(0)+"\t"+pa2.get(0),similarity);
            }
        }
    }

    public static void DivideToSublist(List<List<String>> pattern , List<List<String>> [] sub_pattern ,int size) {

        int length = pattern.size() / size ;
        if(pattern.size() % size != 0 ){
            length += 1;
        }
        //System.out.println("每一个列表有："+length +"最后一个有："+( pattern.size() - (size - 1)*length));
        List<List<String>> new_sub = new ArrayList<>();

        for(int i = 0 ; i < size - 1 ; i++){
            sub_pattern[i] = new ArrayList<>();
            for(int j = 0 ; j < length ; j++){
               sub_pattern[i].add(pattern.get(i*length+j));
            }
        }
        sub_pattern[size-1] = new ArrayList<>();
        for(int i = (size-1)*length ; i< pattern.size() ; i ++){
            sub_pattern[size-1].add(pattern.get(i));
        }
        System.out.println("每一个列表有："+sub_pattern[0].size() +"最后一个有："+sub_pattern[size-1].size());

    }

    public static void WritepaToTxt(String filename, HashMap<String , Double>map){

        System.out.println("将计算的相似度写入TXT文件！");
        try {
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            Iterator<Map.Entry<String,Double>> it = map.entrySet().iterator();
            double similarity;
            String pa ;

            while(it.hasNext()){
                Map.Entry<String,Double> entry = it.next();
                pa = entry.getKey();
                similarity = entry.getValue();
                wr.write(pa +"\t"+similarity+"\n");
            }
            wr.close();
        }catch (IOException e){
            e.printStackTrace();
        }
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
                s = Double.parseDouble(pa.split("\t")[4]);
                System.out.println(s);
                s = Double.parseDouble(df.format(s));
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
                map.put(t1[0]+"\t"+t1[1]+"\t"+t2[0]+"\t"+t2[1] , s);

            }

            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("ending!");
    }

    public static void WriteToTxt(String filename, HashMap<String,Double>map){

        try {
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            Iterator<Map.Entry<String,Double>> it = map.entrySet().iterator();
            double similarity;
            String tu;

            while(it.hasNext()){
                Map.Entry<String,Double> entry = it.next();
                tu = entry.getKey();
                similarity = entry.getValue();
                wr.write(tu+"\t"+similarity +"\t"+"\n");
            }
            wr.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static  void WritepaToExcel(String filename , HashMap<String , Double> map){
        System.out.println("将计算的pattern相似度写入文件");
        Iterator<Map.Entry<String,Double>> it = map.entrySet().iterator();
        String pa;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter wr = new BufferedWriter(new FileWriter(file));
            CsvWriter cwr = new CsvWriter(wr, ',');

            String line  ;
            while(it.hasNext()){
                Map.Entry<String,Double>  entry = it.next()  ;
                pa  = entry.getKey();
                double similar = entry.getValue();

                line = null;
                for ( int i = 0 ; i < pa.split("\t").length ; i++){
                    line = pa.split("\t")[i]+","+line;
                }
                cwr.writeRecord(line.split(","),true);

                cwr.endRecord();   //换行
                cwr.flush();       //刷新数据

            }
            cwr.close();

        }catch (IOException e){
            e.printStackTrace();
        }

    }
    public static void WriteToExcel(String filename ,HashMap<String,Double> tuplemap){
        System.out.println("将计算的tuples的相似度写入Excel表格中！");
        Iterator<Map.Entry<String ,Double>> it = tuplemap.entrySet().iterator();

        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter wr = new BufferedWriter(new FileWriter(file));
            CsvWriter cwr = new CsvWriter(wr, ',');

            String line  ;
            while(it.hasNext()){
                Map.Entry<String,Double>  entry = it.next()  ;
                String tu  = entry.getKey();
                double similar = entry.getValue();

                tu.replace("\t" , ",");
                line = null;
                for(int i = 0 ; i < tu.split("\t").length ; i++){
                    line += tu.split("\t")[i]+",";
                }
                line += similar;
                cwr.writeRecord(line .split(","),true);

                //cwr.endRecord();   //换行
                cwr.flush();       //刷新数据

            }
            cwr.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static  void WriteToExcel(String filename , List<List<String>> pa_ls){

        List<String> temp = new ArrayList<>();
        String pa;
        try{
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter wr = new BufferedWriter(new FileWriter(file));
            CsvWriter cwr = new CsvWriter(wr,',');

            for(int i = 0; i < pa_ls.size() ; i++){
                temp = pa_ls.get(i);
                pa = temp.get(0);
                cwr.writeRecord(pa.split("\t"),true);
                cwr.flush();       //刷新数据
            }
            cwr.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String  PatternSimilarity(List<List<String>> pa_ls , HashMap<String,Double> tuple ,HashSet<String> set, HashMap<String,Double> pattern, List<List<String>> nosimilarity_pa){

        System.out.println("计算pattern的相似度!");
        List<String> pa1 = new ArrayList<>();
        List<String> pa2  = new ArrayList<>();
        String tu ;
        int max_index_i = 0 ,  max_index_j = 0  ,denom;
        double max ,similarity;
        String tu1 , tu2 ;

        max = threshold ;
        double sed_max = threshold ,sim;
        int flag ;   //记录这个pattern有木有和其他pattern相似
        for(int i = 0 ; i < pa_ls.size() - 1 ; i++ ){
            flag = 0 ;   //默认没有相似的pattern
            pa1 = pa_ls.get(i) ;
            for(int  j = i+1 ; j < pa_ls.size() ; j++) {
                pa2 = pa_ls.get(j);
                //System.out.println("pa2: "+pa2);
                tu1 = pa1.get(0)+"\t"+pa2.get(0) ;
                tu2 = pa2.get(0)+"\t"+pa1.get(0) ;
              /*  if(pattern.containsKey(tu1))   {                   //  || pattern.containsKey(pa2.get(0)+"\t"+pa1.get(0))) {    //判断该相似度是否已经存在与map中
                    //若存在，则读取他们的相似度
                    sim = pattern.get(tu1);
                    if(sim > max){
                        sed_max = max;
                        max = sim;
                        max_index_i = i;
                        max_index_j = j;
                        System.out.println(tu1+"\t"+tu2+"similarity:"+ sim);
                    }
                    continue;
                }
                if(pattern.containsKey(tu2)){
                    sim = pattern.get(tu2);
                    if(sim > max){
                        sed_max = max;
                        max = sim;
                        max_index_i = i;
                        max_index_j = j;
                        System.out.println("similarity:"+ sim);
                    }
                    continue;
                }*/
                similarity = 0;
                for (int k = 1 ; k < pa1.size() ; k++) {
                    tu1 = pa1.get(k);
                    for (int m = 1 ; m < pa2.size() ; m++) {
                        tu2 = pa2.get(m);
                        //tup = new Tuples(tu1,tu2);
                        Iterator it = tuple.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            tu = (String) entry.getKey();
                            if ((tu1+"\t"+tu2).equals(tu) || (tu2+"\t"+tu1).equals(tu)) {
                                //System.out.println(tu1+tu2 + entry.getValue());
                                similarity += (Double) entry.getValue();   //相似度相加
                                break;
                            }
                        }
                    }

                }
                if (similarity == 0) {
                    continue;
                }
                flag = 1 ;
                denom = Math.min(pa1.size(), pa2.size()) - 1;    //有一项是pattern，需要减掉

                //System.out.println("similarity:"+similarity +"denom:"+denom);
                similarity = Double.parseDouble(df.format(similarity * 1.0 / denom));

                if(i % 100 == 0){
                    System.out.println("pa1: "+pa1);
                    System.out.println("pa2: "+pa2);
                    System.out.println("similarity:"+similarity);
                }

                if (similarity > max) {
                    sed_max = max ;
                    //将之前的有相似度的pattern放入map中
                    tu1 = pa_ls.get(max_index_i).get(0);
                    tu2 = pa_ls.get(max_index_j).get(0);
                    if (!tu1.equals(tu2)) {
                        pattern.put(tu1 + "\t" + tu2, max);
                    }
                    max = similarity;
                    max_index_i = i;
                    max_index_j = j;
                }
            }
            if(flag == 0){    //如果该pattern没有找到相似的pattern，那么之后也不会找到，可以直接删除
                nosimilarity_pa.add(pa1);
                pa_ls.remove(i);
                if(max_index_j > i ){
                    max_index_j -= 1;
                }
                i--;
            }
        }

        if(max_index_i == 0 && max_index_j == 0){    //若没有找到比阈值大的相似度，则没有聚类
            return "false" +"\t"+"0"+"\t";
        }

        pa1 = pa_ls.get(max_index_i);
        pa2 = pa_ls.get(max_index_j);
        tu1 = pa_ls.get(max_index_i).get(0);
        tu2 = pa_ls.get(max_index_j).get(0);
        //删除与两个pattern有关的所有相似度的计算结果
        Iterator it = pattern.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            tu = (String) entry.getKey();
            if(tu.equals(tu1+"\t"+tu2) || tu.equals(tu2+"\t"+tu1)){
                it.remove();
            }
        }

        pa1.remove(0);
        int length = pa1.size();
        pa2.remove(0);
        pa1.removeAll(pa2);
        pa1.addAll(pa2);
        //将每次合成的放在第一个
        List<String> new_pa = new ArrayList<>();
        pa1.set(0,tu1+"\t"+tu2);
        new_pa.addAll(pa1);
        pa_ls.remove(max_index_j);
        pa_ls.remove(max_index_i);
        pa_ls.add(0,new_pa);

        System.out.println("合成了："+pa1.get(0)+"similarity:"+max+" size:"+pa1.size()+"  "+pa2.size()+"   "+length);

        return "true"+"\t"+ sed_max+"\t";
    }

    public static void ReadAndCaculTuple(String filename, List<List<String>> ls, HashMap<String,Double> tuples){
        System.out.println("读取tuples并计算其相似度:");
        List<String> pa = null;
        String line = new String();
        String org,loc, new_pa ;
        String tuple ;
        int same_pattern , length;
        double similarity ;

        int count = 0 ;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("the file doesn't exist!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                if(line.contains("</O>")){     //表示当前读取得失tuples的信息
                    if(count > 1){      //若之前已经有tuples被存储了，就需要计算该tuple与前面的tuples之间的相似度
                        length = pa.size();
                        for(int i = 0 ; i < count-2 ; i++){
                            same_pattern = 0 ;
                            //System.out.println(ls.get(i));
                            for(int j = 1; j < ls.get(i).size() ; j++){
                                for(int k = 1; k < length ; k++){
                                    if(ls.get(i).get(j).equals(pa.get(k))){
                                        same_pattern++;
                                        break;
                                    }
                                }
                            }
                            if(same_pattern > 0){   //如果找到相同的pattern就进行计算相似度
                                //System.out.println(same_pattern+"\t"+ls.get(i).size()+"\t"+pa.size());
                                similarity = same_pattern * 1.0 /(ls.get(i).size() + length - 2);

                                if( count % 5000 == 0 ){
                                    System.out.println(ls.get(i).get(0)+"\t"+pa.get(0)+"\t"+similarity);
                                }
                                tuples.put(ls.get(i).get(0)+"\t"+pa.get(0),Double.parseDouble(df.format(similarity)));
                                //tuples.put(pa.get(0)+"\t"+ls.get(i).get(0),similarity);
                            }
                        }
                    }
                    if(count > 0 ){
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
                    pa.add(line);
                }
            }
            //最后一个tuples并没有和前面抽取的tuples进行相似度的计算
            for(int i = 0 ; i < ls.size() ; i++ ){
                same_pattern = 0 ;
                for(int j = 1; j < ls.get(i).size() ; j++){
                    for(int k = 1; k < pa.size() ; k++ ){
                        if(ls.get(i).get(j).equals(pa.get(k))){
                            same_pattern++;
                            break;
                        }
                    }
                    if(same_pattern > 0 ){
                        similarity = same_pattern *1.0 /(ls.get(i).size()+pa.size()-2);
                        tuples.put(ls.get(i).get(0)+"\t"+pa.get(0),similarity);
                        //tuples.put(pa.get(0)+"\t"+ls.get(i).get(0),similarity);
                    }
                }
            }
            ls.add(pa);
            br.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("ending!");
    }

    public static void Readtuples(String filename , List<List<String>> ls){
        System.out.println("读取tuple抽取到的pattern!");
        String line = new String();
        List<String> pa = new ArrayList<String>() ;
        int count = 0 ;
        String org,loc;

        try{
            File file  = new File(filename);
            if(!file.exists()){
                System.out.println("the file doesn't exist!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((line = br.readLine()) != null){

                if(line.contains("</O>")){    //判断是否开始读取新的tuples,并将该tuple放入列表中
                    if(count != 0){
                        ls.add(pa);
                        pa = new ArrayList<>();
                    }
                    count++;

                    //System.out.println(line);
                    org = line.split("\t")[0].contains("</O>") ? line.split("\t")[0] : line.split("\t")[1] ;
                    loc = line.split("\t")[1].contains("</L>") ? line.split("\t")[1] : line.split("\t")[0] ;
                    pa.add(org+"\t"+loc);
                }
                else{
                    pa.add(line);
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        //System.out.println("一共有"+ls.size()+"个tuples!");
    }


}
