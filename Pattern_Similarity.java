package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/3/30.
 * 根据之前得到的tuple的相似度，计算pattern的相似度，每次都只聚类相似度最大的两个pattern，考虑所有的tuples
 */
public class Pattern_Similarity {

    private static final double threshold = 0.375;

    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    public static void main(String[] args){
        /*String prefix = "Snowball";
        String tuplename = prefix+"/tuple3_pattern_OL.txt";   //抽取的个数大于3的才需要计算相似度
        String tupleSimilar = prefix+"/Tu_similarity.txt";
        String subtupleSimilar = prefix+threshold+"/sub_Tu_Similar.txt";
        String patternSimilar = prefix+threshold+"/first_pa.txt";
        String merge = prefix+threshold+"/merge.txt";
        String delete_similarity = prefix+threshold+"/delete_sim.txt";
        String patternSimilar2 = prefix+threshold+"/pa_sim.txt";
        String finalfile = prefix+threshold+ "/final.txt";
        String on_pattern =prefix+threshold+ "/on.txt";*/

        String prefix  = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\";

        String tuplename = prefix + "tuple3_pattern_OL.txt";
        String tupleSimilar = prefix + "Tu_similarity.txt";
        String subtupleSimilar = prefix+threshold + "_sub_Tu_similar.txt";
        String patternSimilar = prefix+threshold +"_first_pa.txt";
        String merge = prefix + threshold+"_merge.txt";
        String delete_similarity = prefix +threshold +"_delete_sim.txt";
        String patternSimilar2 = prefix +threshold +"_pa_sim.txt";
        String finalfile = prefix+threshold+"_final.txt";
        String on_pattern = prefix+threshold+"_on.txt";


        HashSet<String> tup_set = new HashSet<>();
        HashMap<List<String>,Double> map = new HashMap<>();  //记录两个pattern族之间的相似度
        HashMap<String , Double> tup_simil  = new HashMap<>();   //记录tuples之间的相似度
        HashMap<List<String> , Double> delete_pa = new HashMap<>();   // 被聚类的pattern的信息
        HashSet<List<String>> pattern_set = new HashSet<>();     //记录每个pattern抽取到的tuples数
        String pa ;     //当前被聚类的两个pattern

        System.out.println("main:现将需要用到的tuple抽取出来");
        Tuple_Similarity.DrawTuples(tuplename,tup_set);
        System.out.println("main:将这些tuples之间的相似度提取出来，并写入文件中");
        DrawTupleSimilar(tupleSimilar,subtupleSimilar,tup_set);
        System.out.println("main:从文件中将tuples的相似度读取出来");
        ReadSimilar(subtupleSimilar,tup_simil);
        System.out.println("main:将需要计算相似度的pattern提取出来");
        ReadPatternToSet(tuplename,pattern_set);
        //计算pattern的相似度
        PatternSimile(pattern_set,map,tup_simil);   //pattern_set保存的是每个pattern抽取的情况，map保存的是每对pattern之间的相似度，tup_simil保存的是每对tuple之间的相似度
        System.out.println("将第一轮计算出pattern的相似度，写入文件中");
        WritepaToTxt(patternSimilar,map);
        //当前若第一轮已经计算结束，并将结果保存在文件中，则下次实验时，可以直接读取相似度
        map = new HashMap<>();
        ReadPaSimilar(patternSimilar,map);
        //将需要的pattern相似度写入文件中
        WritepaToTxt(patternSimilar2,map);
        //是否存在比阈值大的相似度
        pa = new String() ;
        //找到最大的相似度，并将于这两个相似度相关的pattern聚类
        pa = FindMax(map,delete_pa);   //map保存的是每对pattern之间的相似度
        System.out.println("第一轮删除的有:"+delete_pa.size());
        int count = 1 ;
        //若有，则进行聚类
        while(pa != null){
            System.out.println("main:找到需要合并的两个pattern是:"+ pa );
            //System.out.println("main:删除的相似度有:"+delete_pa.size());
            //将新合成的pattern抽取到的tuples也进行合成，重新计算新合成的pattern与其他pattern之间的相似度
            NewPtternandCacul(pattern_set,tup_simil,map,pa);   //pattern_set保存的是每组pattern抽取的情况
            //输出当前的相似度的情况
            WritepaToTxt(patternSimilar2,map);
            //将当前pattern的情况保存下来
            WriteClusterToTxt(on_pattern,pattern_set,count);
            //将合并的两个patter写入文件中
            System.out.println("main:将需要合并的pattern写入文件中！");
            WriteToText(merge,pa,count);
            System.out.println("main:合并到第"+(++count)+"轮！");
            //找到最大的相似度，并把这两个pattern相关的相似度删除
            pa = new String();
            pa = FindMax(map,delete_pa);
            if(map.size() == 0 ){   //所有有相似度的pattern都被合并了，则结束聚类
                break;
            }

        }
        System.out.println("main:将中间删除的pattern相似度写入文件！");
        WritepaToTxt(delete_similarity,delete_pa);
        System.out.println("main:将计算出pattern的相似度写入文件！");
        WritepaToTxt(patternSimilar2 , map);
        System.out.println("main:similarity_pa的大小为:"+map.size());
        //将最后聚类的结果写入文件中
        WriteClusterToTxt(finalfile,pattern_set,count);
        System.out.println("main:pattern的大小为:"+pattern_set.size());

    }

    public static void WriteClusterToTxt(String filename , HashSet<List<String>>pattern_set , int count){

        try {
            File file = new File(filename);
            file.createNewFile();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            bw.write("第"+count+"轮聚类的效果如下:\n");
            Iterator it = pattern_set.iterator();
            List<String > pa ;
            while(it.hasNext()){    //只需要将聚类的pattern写入文件中
                pa = (List<String>)it.next();
                for(int i = 0 ; i < pa.get(0).split("\t").length ; i++){
                    bw.write(pa.get(0).split("\t")[i]+"\n");
                }
                for(int j = 1; j < pa.size() ; j++){     //输出tuples
                    bw.write(pa.get(j)+"\n");
                }
                bw.write("\n");
            }
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //
   public static void WritepaToTxt(String filename , HashMap<List<String> ,Double>delete_pa){

        System.out.println("WritepaToTxt:当前的相似度列表为:"+delete_pa.size());
        try {
            File file = new File(filename);

            file.createNewFile();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            List<String> ls = new ArrayList<>();
            double sim ;
            Iterator it = delete_pa.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                ls = (List<String>)entry.getKey();
                //System.out.println(ls);
                sim =(Double) entry.getValue();
                bw.write(ls.get(0)+","+ls.get(1)+","+sim+"\n");
            }
            bw.write("\n");
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //将新合并的两个pattern写入文件中
    public  static  void WriteToText(String filename , String pa , int count){
        try {
            File file = new File(filename);
            file.createNewFile();

            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            wr.write(count+" merge:\n");
            for(int i =  0 ;  i < pa.split(",").length ; i++){
                wr.write(pa.split(",")[i]+"\n");
            }
            wr.write("\n");
            wr.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //将新合成的pattern抽取到的tuples也进行合成，重新计算新合成的pattern与其他pattern之间的相似度
    public static boolean NewPtternandCacul(HashSet<List<String>> pa ,HashMap<String , Double > tuplemap ,HashMap<List<String> ,Double > sim_pa ,String pattern){
        //pattern是要聚类的两个pattern，pa是保存每组pattern抽取到的tuples的集合，tuplemap保存的是每对tuples之间的相似度，sim_pa保存的是每组pattern之间的相似度
        Iterator it = pa.iterator();
        List<String> p ;
        List<String> p1 = new ArrayList<>();
        List<String> p2 = new ArrayList<>();
        int flag = 0 ;
        HashSet<String> set = new HashSet<>();     //要聚类的所有的pattern
        for(int i = 0 ; i < pattern.split(",").length ; i++){
            set.add(pattern.split(",")[i]);
        }
        String sp = new String();
        //System.out.println("删除之前的pattern数为："+pa.size());
        while(it.hasNext()){
            p = new ArrayList<>((List<String>) it.next());
            sp = p.get(0);
            for(int i = 0 ; i < sp.split(",").length ; i ++){
                if(set.contains(sp.split(",")[i])) {     //判断当前的pattern是否存在于要聚类的范围内
                    flag ++;
                    if(flag == 1){
                        p1 = p;
                        it.remove();
                    }
                    else {
                        if(flag == 2){
                            p2 = p ;
                            it.remove();
                            break;
                        }
                    }
                    break;
                }
            }
        }
        //System.out.println("需要合并的两个pattern是:" + pattern);
        if(flag != 2 ){
            System.out.println("NewPtternandCacul:没有找到这两个pattern!");
            return false;
        }
        //System.out.println("NewPtternandCacul:删除的两个列表是:");
        //System.out.println(p1+"\n"+p2);

        //否则将这两个pattern合并
        p1.remove(0);
        p2.remove(0);
        p2.removeAll(p1);
        p1.addAll(p2);
        p1.add(0,pattern);

        //计算新的pattern与其他pattern之间的相似度
        System.out.println("NewPtternandCacul:合并之后的pattern信息:" + p1);
        //System.out.println("被聚类的pattern与其他pattern之间的相似度:");
        it = pa.iterator();
        while(it.hasNext()){
            p = (List<String>) it.next();
            //System.out.println(p);
            CaculPaSim(p1,p,tuplemap,sim_pa);
        }

        //将新的pattern加入set中
        //System.out.println("合并之后的pattern信息是:"+p1);
        pa.add(p1);
        //System.out.println("聚类后的pattern个数为:"+pa.size());
        return true;
    }

    //找到当前相似度最大的两个pattern，并删除与这两个pattern相关的所有相似度
    public static  String  FindMax(HashMap<List<String> , Double >  sim_pa, HashMap<List<String>,Double> delete){

        double max = 0 ;
        List<String> ls ;
        String pa  = new String();
        Iterator it = sim_pa.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            if((Double) entry.getValue() > max){
                max = (Double)entry.getValue();
                ls = new ArrayList<>((List<String>) entry.getKey());
                pa = ls.get(0)+","+ls.get(1);
            }
        }
        if( max > threshold ){   //若最大相似度大于阈值，则下一步需要聚类，这边先将与这两个pattern相关的相似度删除
            it = sim_pa.entrySet().iterator();
            double sim ;
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                ls = new ArrayList<>((List<String>) entry.getKey());
                sim  = (Double) entry.getValue();
                //System.out.println("当前读取的是:"+ls);
                if(pa.contains(ls.get(0)+",") || pa.contains(","+ls.get(0) )){
                    delete.put(ls,sim);
                    //System.out.println("删除了"+ls);
                    it.remove();
                    continue;
                }
                else {
                    if (pa.contains(ls.get(1)+",") || pa.contains(","+ls.get(1))) {
                        delete.put(ls, sim);
                        //System.out.println("删除了" + ls);
                        it.remove();
                        continue;
                    }
                }
            }
            //输出删除不需要的相似度之后的map信息
           // System.out.println("删除之后的相似度有:");
            it = sim_pa.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                ls = (List<String>) entry.getKey();
                //System.out.println(ls);
            }
            //System.out.println("需要删除的相似度有"+delete.size());
        }
        else{
            pa = null;
        }
        return pa;
    }

    public static void PatternSimile(HashSet<List<String>> pa ,HashMap<List<String> , Double > pa_sim ,HashMap<String , Double > tu_sim ) {

        System.out.println("计算pattern列表之间的相似度!");
        List<String> pa1;
        List<String> pa2;
        List<String> newp,newp1 ;
        String tu1, tu2;

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
                newp = new ArrayList<>();
                newp1 = new ArrayList<>();
                newp.add(tu1);
                newp.add(tu2);
                newp1.add(tu2);
                newp1.add(tu1);
                if(pa_sim.containsKey(newp) || pa_sim.containsKey(newp1)){
                    continue;
                }
                //否则计算这两个pattern的相似度，并将结果保存到相似度Map中
                CaculPaSim(pa1,pa2,tu_sim,pa_sim);
            }
        }
    }

    //计算两个pattern之间的相似度
    public static void CaculPaSim(List<String> pa1, List<String> pa2 , HashMap<String , Double > tu_sim ,HashMap<List<String> , Double > pa_sim){

        String tu1,tu2;
        Double similarity = 0.0 ;
        double flag  ;
        //依次比较两个pattern的tuples，计算其相似度,分母为最小的tuples的个数
        List<String> temp ;
        if(pa1.size() > pa2.size()){    //将Pa1调换成tuple个数小的那个列表
            temp = pa1 ;
            pa1 = pa2;
            pa2 = temp;
        }
        double  max ;
        for(int k = 1 ; k < pa1.size() ; k++){   //从第一个tuple开始计算相似度
            tu1 = pa1.get(k);
            max = 0.0 ;
            for(int m = 1 ; m < pa2.size() ; m++){
                tu2 = pa2.get(m);
                if(tu1.equals(tu2)){    //若遇到相同的tuple则直接将该轮的相似度最大值置为1，并退出当前循环
                    max = 1;
                    break;
                }
                //否则，首先判断这两个tuples是否都有相似度，若没有则直接跳转到一下个比较
                if(tu_sim.containsKey(tu1+"\t"+tu2 ) ){
                    flag = tu_sim.get(tu1+"\t"+tu2);
                }
                else{
                    if(tu_sim.containsKey(tu2+"\t"+tu1) ){
                        flag = tu_sim.get(tu2+"\t"+tu1);
                    }
                    else
                        continue;    //直接跳转到下一轮比较
                }
                max = max > flag ? max : flag ;      //更新当前的最大值
            }
            similarity += max ;   //加入每一轮的最大相似度
        }
        if(similarity == 0){
            return;
        }
        //若存在相似度，则将相似度保存到set集合中
        int denom = pa1.size() - 1 ;
        double similar = Double.parseDouble(df.format(similarity * 1.0 / denom));
        //判断相似度是否小于阈值，若小于则不需要记录
        if(similar < threshold){
            return;
        }
        temp = new ArrayList<>();
        temp.add(pa1.get(0));
        temp.add(pa2.get(0));
        pa_sim.put(temp,similar);
        //System.out.println(pa1.get(0)+","+pa2.get(0)+","+similar+",");
        return;
    }

    public static void ReadPatternToSet(String filename , HashSet<List<String>> pa){

        HashSet<String> set = new HashSet<>();
        String line ;
        int flag = 1 ;   //记录下一行是否是一个新的pattern
        List<String> newls = null ;
        String loc = new String(),org = new String();

        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("the file doesn't exist!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((line = br.readLine()) != null){
                if(line.equals("")){
                    flag = 1;     //下一行开始抽取新的pattern
                    if(set.add(newls.get(0))){     //判断当前pattern是否已经添加进去
                        pa.add(newls);
                    }
                    continue;
                }
                if(flag == 1) {   //如果当前是新的pattern，则将该pattern存入List中
                    newls = new ArrayList<String>();   //将pattern添入列表
                    newls.add(line);
                    flag = 0 ;
                    continue;
                }
                //否则将tuples加入列表中
                org =line.split("\t")[0];
                loc = line.split("\t")[1];
                newls.add(org+"\t"+loc);
            }
            if(set.add(newls.get(0))){
                pa.add(newls);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("ReadPatternToSet:原始需要聚类的pattern一共有"+set.size());
    }

    public static void ReadSimilar(String filename,HashMap<String ,Double>map){

        try {
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = new String();
            String tus = new String();
            double sim ;
            while ((line = br.readLine()) != null){
                tus = line.split("\t")[0]+"\t"+line.split("\t")[1]+"\t"+line.split("\t")[2]+"\t"+line.split("\t")[3];
                sim = Double.parseDouble(line.split("\t")[4]);
                map.put(tus,sim);
            }
            System.out.println("ReadSimilar:有相似度的tuples对一共有："+map.size());
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void  ReadPaSimilar(String filename, HashMap<List<String> , Double>map){
        List pa_ls;
        try {
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = new String();
            String tus = new String();
            double sim ;
            while ((line = br.readLine()) != null){
                if(line.length() == 0){
                    break;
                }
                pa_ls = new ArrayList();      //将两个pattern族读到列表中
                pa_ls.add(line.split(",")[0]);
                pa_ls.add(line.split(",")[1]);
                sim = Double.parseDouble(line.split(",")[2]);
                if(sim < threshold) {     //若相似度小于阈值，则直接跳过
                    continue;
                }
                map.put(pa_ls,sim);
            }
            System.out.println("ReadPaSimilar:相似度大于阈值的pattern对一共有："+map.size());
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void DrawTupleSimilar(String filename , String writefile, HashSet<String> tup_set){
        int count = 0;

        try {
            File file = new File(filename);
            File f1 = new File(writefile);
            if(!file.exists()){
                System.out.println("the file failed!");
            }
            if(!f1.exists()){
                f1.createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f1,true)) );
            String line = new String();
            String org,loc;
            while ((line = br.readLine()) != null){
                org = line.split("\t")[0];
                loc = line.split("\t")[1];
                if(tup_set.contains(org+"\t"+loc)){   //首先判断第一个tuples是否存在，若存在则比较第二个tuple
                    org = line.split("\t")[2];
                    loc = line.split("\t")[3];
                    if(tup_set.contains(org+"\t"+loc)){    //若第二个tuple也存在，则将他们的相似度保存下来，写入新文件中
                        count ++;
                        bw.write(line+"\n");
                    }
                }
            }
            br.close();
            bw.close();
            System.out.println("一共有"+count+"对tuples的相似度!");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
