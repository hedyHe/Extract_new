package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/4/18.
 */
public class SelfAlgorithm {

    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.0000");
    public static int tuple_count  = 194;
    public static double[] tuple ;

    public static class Features{
        private double p1 ;                          //性质1是抽取到的tuple的分布情况,cosine
        private int p2 = -1 ;                        //性质2是互斥的concepts的个数
        private double p3;                           //性质3是根据随机游走得到的得分
        private double p4 ;                          //性质4是触发的pattern的得分的平均值
        private int sub ;                            //性质5是触发的pattern的个数
        private double standDev ;                    //性质6是子集得分的偏差
        private int  level ;                         //性质7是第一次是发现的轮次
        private int all_num ;                        //性质8是在该概念下抽到的所有的pattern数
        private int maxlevel ;                       //性质9是可以抽取到的最大的轮次

        public Features(){
            p1 = 0;
            p2 = -1;
            p3 = 0;
            p4 = 0 ;
            sub = 0 ;
            standDev = 0 ;
            level = 0;
            all_num = 0;
            maxlevel = 0;
        }

        public double getP1() {
            return p1;
        }

        public int getP2() {
            return p2;
        }

        public double getP3() {
            return p3;
        }

        public double getP4() {
            return p4;
        }

        public int getSub() {
            return sub;
        }

        public double getStandDev() {
            return standDev;
        }

        public int getLevel() {
            return level;
        }

        public int getAll_num() {
            return all_num;
        }

        public int getMaxlevel() {
            return maxlevel;
        }

        public void setP2(int p2) {
            this.p2 = p2;
        }

        public void setP3(double p3) {
            this.p3 = p3;
        }

        public void setP4(double p4) {
            this.p4 = p4;
        }

        public void setSub(int sub) {
            this.sub = sub;
        }

        public void setStandDev(double standDev) {
            this.standDev = standDev;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void setAll_num(int all_num) {
            this.all_num = all_num;
        }

        public void setMaxlevel(int maxlevel) {
            this.maxlevel = maxlevel;
        }

        public void setP1(double p1) {
            this.p1 = p1;
        }
    }

    public static void main(String[] args ){
        String tag = "0.84_ME_1";
        String datafile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\Extract\\OL_TAB.txt";
        String patternfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag+"\\pattern.txt";
        String tuplefile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag+"\\tuples.txt";
        String stdfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\Cluster\\all_Cluster.txt";
        String matrixfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag+"\\Matrix.txt";
        String randomfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag+"\\RandomWalk.txt";
        String freaturefile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag+"\\Feature.txt";

        HashMap<String, Integer> pa_order= new HashMap<>();     //记录每个pattern被抽取的轮次
        HashMap<String,Integer> pa_seq = new HashMap<>();       //记录每个pattern在数组中的次序
        HashMap<String ,Integer> all_tuple = new HashMap<>();    //记录每个tuple被抽取的次数
        HashMap<String ,Integer> tuple_order = new HashMap<>();    //记录每个tuple在数组中的序号
        HashMap<String , Features> pattern = new HashMap<>();
        HashMap<String , double[]> tu_pa = new HashMap<>();    //每个pattern抽取到的tuple的情况

        //先将所有的pattern从文件中读取出来
        OperateTextFile.ReadPatterntoMap(patternfile,pa_order,pa_seq);
        //将所有的tuple从文件中读取出来
        OperateTextFile.ReadFileToList(tuplefile,all_tuple);
        tuple_count = all_tuple.size();
        tuple = new double[tuple_count];
        System.out.println("the size of all_tuple:"+all_tuple.size()+"\t");
        //将tuple被抽取的次数归一化
        tuple = normalization(all_tuple,tuple_order);
        //根据pattern到数据集中抽取tuple，并记录性质7，8
        int max_level = SetP78ofPattern(datafile,tuple_order,pa_order,pattern,tu_pa);
        //将每个pattern抽取到的tuple的个数归一化,并计算余弦相似度，设置性质1的值
        normalization(tu_pa,pattern,tuple);
        //计算每个pattern所属的cluster的数量,设置性质2和9的值
        Caculate_p2(stdfile,pattern,max_level);
        //从文件中读取pattern之间的关系
        int[][] dir_graph  ;            // = new int[pa_order.size()][pa_order.size()];
        System.out.println("pa_order.size = "+pa_order.size());
        dir_graph = OperateTextFile.ReadFiletoArr(matrixfile,pa_order.size());
        //从文件中读取每个pattern随机游走的得分,及其子集得分的平均数和偏差
        HashMap<Integer , double[]> score = new HashMap<Integer, double[]>();
        //根据随机游走的结果设置性质3,4,5,6的值
        Set_P3456(randomfile,dir_graph,pa_seq,score,pattern);
        OperateTextFile.WritetoTxt1(freaturefile , pattern);
    }

    public static void Set_P3456(String filename , int[][] adj , HashMap<String ,Integer> seq, HashMap<Integer,double[]>map, HashMap<String ,Features> pattern){
        //文件中存储的是每个pattern随机游走的得分，adj是邻接矩阵，seq是指每个pattern在数组中的排序，map是需要保存的pattern对应的机游走的得分，子集的平均得分，及偏差
        int order , count = 0 ;
        double score, arr[]  ;
        String pa ;
        HashMap<Integer , List<Double>> sub = new HashMap<>();  //保存每个pattern的抽取到的pattern的得分
        List<Double> ls ;

        try {
            File file  = new File(filename);
            if (!file.exists()){
                System.out.println("can't find the file!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line ;
            while ((line = br.readLine()) != null){
                if (line.contains(":")){
                    count++;
                    continue;
                }
                if (line.equals("")){
                    continue;
                }
                //否则将该pattern及其得分提取出来
                pa = line.split("\t")[1];
                //System.out.println("pa = "+pa);
                score = Double.parseDouble(line.split("\t")[2]);
                order = seq.get(pa);   //获得当前pattern所在的序号
                if (map.containsKey(order)){   //若之前已经读取过一次了，则直接修改数组中的第一项
                    arr = map.get(order);
                    arr[0] = (arr[0]+score) ;   //几次游走的得分相加
                    arr[1] = 0;
                    arr[2] = 0;
                    //System.out.println(arr[0]+"\t"+arr[1]+"\t"+arr[2]);
                    map.put(order,arr);
                    continue;
                }
                //否则插入新的pattern对应的数组
                arr = new double[3];
                arr[0] = score;
                arr[1] = 0;
                arr[2] = 0;
                map.put(order,arr);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        double array[]  ;
        //int sub_count[] = new int[pattern.size()];   //记录每个pattern子集的个数
        int pa_count;

        //文件读取结束后，对pattern的数组进行处理
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            order =(int) entry.getKey();   //获得当前pattern对应的序号
            arr = (double[]) entry.getValue();
            arr[0] = Double.parseDouble(df.format(arr[0] / count));   //自己的相关度取平均值
            //获得该pattern能够抽取到的pattern子集
            score = 0 ;
            pa_count = 0 ;
            ls = new ArrayList<Double>();
            for (int i = order + 1 ; i < pattern.size() ; i++){
                if (adj[i][order] != 0){   //若两个pattern之间存在边
                    array = map.get(i);
                    score += array[0];
                    //System.out.println("array[0]"+array[0]);
                    ls.add(array[0]);
                    pa_count++;   //记录关联的pattern的个数
                }
            }
            //System.out.print(order+"\t");
            //System.out.println(ls);
            if (pa_count == 0 ){   //最后一轮的pattern没有与之关联的pattern，值为0
                arr[1] = 0 ;
                continue;
            }
            sub.put(order,ls);
            //否则需要计算其子集的相关信息
            score = Double.parseDouble(df.format(score/pa_count));   //计算平均值
            arr[1] = score;
            //sub_count[order] = pa_count;
        }

        double sc;
        //将所有的信息存储到性质map中
        it = pattern.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            pa = (String) entry.getKey();
            //System.out.println(pa);
            //获得当前pattern在数组中的位置
            order = seq.get(pa);
            //根据位置找到之前存储的数组
            Features f = pattern.get(pa);
            array = map.get(order);  //获取保存的数组信息
            //设置性质3的值
            //System.out.println("order = "+order);
            f.setP3(array[0]);
            //设置性质4的值
            f.setP4(array[1]);
            //设置性质5的值
            ls = sub.get(order);
            if (ls == null){       //最后一轮抽取到的pattern是没有子集的
                f.setSub(0);
                f.setStandDev(0);
            }
            else{
                //System.out.println("the size of ls :"+ls.size());
                f.setSub(sub.get(order).size());
                //计算性质6的值
                count = sub.get(order).size();
                score = 0 ;
                for (int i = 0 ; i < count; i++){
                    sc = sub.get(order).get(i);
                    score += ( sc - f.getP4()) * ( sc - f.getP4());
                }
                score = Double.parseDouble(df.format(Math.sqrt(score / count)));
                f.setStandDev(score);
            }


            pattern.put(pa,f);
        }
    }

    public static void normalization( HashMap<String,double[]> tu_pa , HashMap<String,Features> pa,double[] tu ){
        //tu_pa保存的是每个pattern抽取到的tuple的分布情况，pa保存的是每个pattern对应的9个性质的值，tu保存的是整个抽取过程中所有的tuple的分布情况
        double[] no ;
        double all ; //一个pattern抽取到的tuple的个数
        double  deno1,deno2;
        String pattern;
        Features f;
        Iterator it = tu_pa.entrySet().iterator();
        //对每个pattern的tuple的个数归一化
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            pattern = (String) entry.getKey();
            no = (double[]) entry.getValue();
            all = 0 ;
            deno1 = 0 ;
            deno2 = 0 ;
            for (int i = 0 ; i < tuple_count ; i++){
                all += no[i];
            }
            for (int i = 0 ; i < tuple_count ; i++){
                no[i] = no[i] / all;
            }
            //归一化之后算余弦相似度
            all = 0 ;
            for (int i = 0 ; i < tuple_count ; i++){
                deno1 += no[i]*no[i];
                deno2 += tu[i]*tu[i];
                all += no[i] * tu[i];
            }
            //计算相似度之后，设置性质1的值
            deno1 = Double.parseDouble(df.format( Math.sqrt(deno1)));
            deno2 = Double.parseDouble(df.format( Math.sqrt(deno2)));
            all = Double.parseDouble(df.format(all / (deno1*deno2)));
            f = pa.get(pattern);
            f.setP1(all);
        }
    }

    //将tuple被抽取的次数归一化
    public static double[] normalization(HashMap<String ,Integer> all_tuple, HashMap<String,Integer> order )
    {
        double[] nor = new double[tuple_count];

        Iterator it = all_tuple.entrySet().iterator();
        String tu ;
        int all = 0 ,count,ord = 0 ;
        double no ;

        //先求出所有的tuple的个数
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            count = (int)entry.getValue();
            all += count;
        }
        //根据tuple的总数进行归一化
        it = all_tuple.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            tu = (String) entry.getKey();
            count = (int)entry.getValue();
            no = Double.parseDouble(df.format(count*1.0/all));
            order.put(tu,ord);
            //ord = order.get(tu);                    //获取当前的pattern在数组中的位置
            nor[ord] = no;
            ord++;
        }

        return nor;

    }

    public static void Caculate_p2(String filename , HashMap<String ,Features> pattern , int max){
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("can't find the file!"+filename);
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            Features f;
            while ((line = br.readLine()) != null){
                if(line.equals("") || line.contains(":")){
                    continue;
                }
                if (pattern.containsKey(line.split("\t")[0])){
                    f = pattern.get(line.split("\t")[0]);
                    //System.out.println(line+f.getP2());
                    f.setP2(1+f.getP2());   //被抽取的聚类的个数加1
                    f.setMaxlevel(max);
                    pattern.put(line.split("\t")[0],f);
                }

            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static int SetP78ofPattern(String filename ,HashMap<String ,Integer> tuple, HashMap<String , Integer>pa_order, HashMap<String ,Features > pattern , HashMap<String,double[]> tu_pa){
        //filename是数据集，tuple是抽取过程中出现过的所有的tuple，pa_order保存的是pattern对应的抽取轮次，pattern保存的是每个pattern的9个性质值，tu_pa保存的是每个pattern抽取到的tuple的情况
        int max_level = 0;
        double[] tu ;
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("can't find the file!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line , pa ,tu1,tu2;
            int order ,turn  ;
            Features f ;
            while ((line = br.readLine()) != null){
                pa = line.split("\t")[1];
                if (pa_order.containsKey(pa)){    //判断该pattern是否需要
                    //
                    turn = pa_order.get(pa);
                    max_level = turn > max_level ? turn : max_level;
                    tu1 = line.split("\t")[0]+"\t"+line.split("\t")[2];
                    tu2 = line.split("\t")[2]+"\t"+line.split("\t")[0];
                    if ( !tuple.containsKey(tu1) && !tuple.containsKey(tu2)){
                        continue;
                    }
                    //System.out.println(pa);
                    if (tuple.containsKey(tu1)){
                        order = tuple.get(tu1);     //获得当前抽取到的tuple在数组中的序号
                    }
                    else {
                        order = tuple.get(tu2);
                    }
                    if (pattern.containsKey(pa)){   //先判断该pattern之前是否被抽取过，若被抽取过，则需要修改该pattern抽取到的tuple的情况
                        tu = tu_pa.get(pa);   //获取之前的tuple数组的情况
                        tu[order] += 1;        //当前抽取到的tuple被抽取的次数加1
                        //System.out.println("tu[order] = "+ tu[order]);
                        tu_pa.put(pa,tu);
                        continue;
                    }
                    f = new Features();
                    f.setLevel(turn);    //第一次被抽取的轮次
                    f.setAll_num(pa_order.size());     //该概念下抽取的pattern的总数
                    tu = new double[tuple_count];
                    for (int i = 0 ; i < tuple_count ; i++){
                        tu[i] = 0 ;
                    }
                    //System.out.println("tu[order] = "+ tu[order]);
                    tu[order] += 1 ;
                    tu_pa.put(pa,tu);
                    pattern.put(pa,f);

                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return max_level;
    }


}
