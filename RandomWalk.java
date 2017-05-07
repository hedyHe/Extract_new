package Snowball;

import java.util.*;

/**
 * Created by Hedy on 2017/4/16.
 */
public class RandomWalk {

    public static Integer length = 0;
    public static final long MAX_ITERATION_TIMES = 100;
    public static final double MIN_ERRORS = 0.00001;
    public static final double alpha = 0.8;
    //public static final int seeds_size = 2;
    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    public static void main(String[] args){

        int index = 0;
        String[] tag = new String[9];
        tag[0] = "0.85_LO";
        tag[1] = "0.84_ME_1";
        tag[2] = "0.84_MA";
        tag[3] = "0.84_HO";
        tag[4] = "0.79_JO";
        tag[5] = "0.75_TL";
        tag[6] = "0.75_PR";
        tag[7] = "0.8_AC";
        tag[8] = "0.84_HE";

        //String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\0.85_LO\\tuples.txt";
        String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\pattern.txt";
        String tuplefile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\tuplesofpattern.txt";
        String patternfile ="F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\patternoftuples.txt";
        String matrixfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" + tag[index] +"\\RWR_Matrix.txt";
        String randomfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index] +"\\RWR.txt";
        String F_file = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" + tag[index] + "\\RWR_F_score.txt";
        //HashSet<String> tu_set = new HashSet<>();
        //HashSet<String> pa_set = new HashSet<>();
        HashMap<String, Integer> pa_order= new HashMap<>();    //记录每个pattern被抽取的轮次
        HashMap<String,HashSet<String>> pa_map = new HashMap<>();    //保存每个pattern可以抽取到的tuple
        HashMap<String,HashSet<String>> tu_map = new HashMap<>();    //保存每个tuple可以抽取到的pattern
        HashMap<String,Integer> pa_seq = new HashMap<>();    //记录每个pattern在二维数组中的位置

        //先将所有的pattern从文件中读取出来
        OperateTextFile.ReadPatterntoMap(filename,pa_order,pa_seq);
        System.out.println("the size of pa_order is:"+pa_order.size());

        //从文件中读取每个pattern可以抽取到的tuple
        OperateTextFile.ReadFiletoMap(tuplefile,pa_map);
        System.out.println("the size of pa_map is:"+pa_map.size());

        //从文件中读取每个tuple可以抽取到的pattern
        OperateTextFile.ReadFiletoMap(patternfile,tu_map);
        System.out.println("the size of tu_map is:"+tu_map.size());
        length = pa_order.size();
        System.out.println("the length is :"+length);
        //建立pattern之间的关系

        int [][] dir_graph ;
        dir_graph = FindRelation(pa_map,tu_map,pa_order,pa_seq);
        OperateTextFile.WriteMatricToFile(matrixfile,dir_graph,length);
        int count = 0 ;

        int[][] new_graph = new int[length][length] ;
        for (int i = 0 ;  i < length ; i ++){
            for (int j = 0 ; j  < length ; j++){
                new_graph[i][j] = 0;
                if (dir_graph[i][j] == 1 ){
                    count++;
                }
            }
        }
        System.out.println("the count = "+count);

        //建立邻接矩阵
        count = 0 ;
        for (int i = 0 ; i < length  ; i++){
            for (int j = 0 ; j < length  ; j++){
                if (dir_graph[i][j] == 1){
                    new_graph[j][i] = 1;
                    count++;
                }
            }
        }

        System.out.println("the count = "+count);

        OperateTextFile.WriteMatricToFile1(matrixfile,new_graph,length);

        //将矩阵归一化
        double[][] Adj_matrix = new double[length][length];

        for (int i = 0 ; i < length ; i++ ){
            count = 0 ;
            for (int j = i + 1 ; j < length ; j++){
                if (new_graph[j][i] == 1){
                    count++;
                }
            }
            if (count == 0 ){
                continue;
            }
            for (int j = i + 1 ; j < length ; j++){
                if (new_graph[j][i] == 1){
                    Adj_matrix[j][i] = Double.parseDouble(df.format(1.0 / count));
                }
            }
        }

        OperateTextFile.WriteMatricToFile1(matrixfile,Adj_matrix,length);
        HashMap<Integer, double[]> result = new HashMap<>();
        result = RWRGraph(pa_order,pa_seq,Adj_matrix,length);    //pa_order保存的是每个pattern被抽取的轮次，Adj_matrix是邻接矩阵


        OperateTextFile.WriteMaptoFile(randomfile,result,pa_seq,length);
        HashMap<String,Double> maxmin = new HashMap<>();
        FindMaxMin(result,pa_seq,maxmin,length);

        //求相关度的平均值
        double[] mean_value = new double[length];
        double[] degree = new double[length];
        Iterator it = result.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = ( Map.Entry ) it.next();
            degree = (double[]) entry.getValue();
            for (int i = 0 ; i  < length ; i ++){
                mean_value[i] = degree[i];
            }
        }
        for (int i = 0 ; i < length ; i++){
            mean_value[i] = Double.parseDouble(df.format(mean_value[i] / result.size()));
        }

        OperateTextFile.writeArrayToFile(F_file,mean_value,pa_seq);

    }

    //找出相似度的最大值和最小值
    public static void FindMaxMin(HashMap<Integer,double[]> result ,HashMap<String,Integer> pa_seq, HashMap<String , Double> maxmin,int size){
        System.out.println("找出相关度最小和最大的pattern:");
        int max_index,min_index;
        double max,min ;
        for (Integer key : result.keySet()){
            double[] arr = result.get(key);
            max = 0 ;
            min = 10000 ;
            min_index = 0;
            max_index = 0;
            for (int i = 0 ;i < size ; i++){
                if (i == key){
                    continue;
                }
                if (arr[i] >= max){
                    max_index = i ;
                    max = arr[i];
                }
                if (arr[i] < min){
                    if (i != 0 && i != 1){
                        min_index = i ;
                        min = arr[i];
                    }
                }
            }
            System.out.println("与"+key+"相关度最高的是:"+max_index +"\t"+max);
            System.out.println("与"+key+"相关度最低的是:"+min_index +"\t"+min);
            Iterator it = pa_seq.entrySet().iterator();
            int num = 0 ;
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                int seq = (int)entry.getValue();
                if (seq == max_index){
                    maxmin.put((String)entry.getKey(),max);
                }
                else {
                    if (seq == min_index){
                        maxmin.put((String) entry.getKey(),min);
                    }
                }
                if (seq == max_index || seq == key || seq == min_index){
                    System.out.println(seq +" 是:"+ entry.getKey());
                    num++;
                    if (num == 3){
                        break;
                    }
                }
            }
        }
    }

    //RWR
    public static double[] randomWalkRestart( int startPoint , double transMatrix[][], int size ){
        int iterationTimes = 0;
        double[] rank_sp = new double[size];
        double[] e = new double[size];
        //init rank_sp, set identify vector
        for(int i = 0 ; i < size; i++){
            if(i == startPoint){
                rank_sp[i] = 1.0;
                e[i] = 1.0;
            }else{
                rank_sp[i] =0.0;
                e[i] =0.0;
            }
        }
        boolean flag = true;
        double tep ;
        while(iterationTimes < MAX_ITERATION_TIMES){
            double[] temp = new double[length];
            if(flag == true){
                for(int i = 0; i < size ; i++){
                    temp[i] = rank_sp[i];
                    tep = 0 ;
                    for(int j = 0; j < size; j++){
                        tep += Double.parseDouble(df.format(alpha*transMatrix[i][j]*rank_sp[j]));
                    }
                    rank_sp[i] = tep ;
                    tep = Double.parseDouble(df.format((1-alpha)*e[i]));
                    rank_sp[i] += tep ;
                    rank_sp[i] = Double.parseDouble(df.format(rank_sp[i]));
                    //System.out.print(rank_sp[i]+"\t");
                }
                if(judge(temp,rank_sp, MIN_ERRORS)){
                    flag = false ;
                }
            }else
                break;
            iterationTimes++;
        }

        System.out.println("进行到第"+iterationTimes+"轮!");
        return rank_sp;
    }

    //judge the difference between two interations
    public static boolean judge(double a[], double b[], double minErrors){
        boolean flag = true;
        for(int i=0; i<a.length; i++){
            if(Math.abs(a[i]-b[i])<minErrors)
                continue;
            else{
                flag = false;
                break;
            }
        }
        return flag;
    }

    public static HashMap<Integer,double[]> RWRGraph(HashMap<String ,Integer> pa , HashMap<String,Integer> seq,double[][] graph ,int size){
        double[] arr;
        HashMap<Integer , double[]> map = new HashMap<>();
        Iterator it = pa.entrySet().iterator();
        String pattern ;
        while (it.hasNext()){
            Map.Entry entry =(Map.Entry) it.next();
            pattern = (String) entry.getKey();
            if ((Integer)entry.getValue() != 1 ){   //判断是否是第一轮抽取到的pattern
                continue;
            }
            //System.out.println(pattern+"的ranking score为:");
            arr = randomWalkRestart((Integer) seq.get(pattern),graph,size);
            map.put((Integer)seq.get(pattern),arr);      //seq保存的是每个pattern在数组中的位置，
        }
        return map;
    }

    public static int[][] FindRelation(HashMap<String,HashSet<String>> pa_map,HashMap<String,HashSet<String>> tu_map, HashMap<String,Integer> order ,HashMap<String ,Integer> seq ){

        int graph[][] = new int[length][length];
        Iterator it = pa_map.entrySet().iterator();
        Iterator set_it ;
        String pa, tu;
        HashSet<String> tuple , pattern;
        int pre_order, beh_order,temp;
        int seq_1 , seq_2;
        while (it.hasNext()){   //对每个pattern寻找其邻接点
            Map.Entry entry = (Map.Entry) it.next();
            pa = (String)entry.getKey();
            pre_order = order.get(pa);    //该pattern被抽取的轮次
            seq_1 = seq.get(pa);           //该pattern在二维数组中的位置1
            if (!order.containsKey(pa)){
                continue;
            }
            tuple = (HashSet<String>) entry.getValue();   //该pattern可以抽取到的所有的tuple1
            //根据tuple集合找对应的pattern
            set_it = tuple.iterator();
            while (set_it.hasNext()){
                tu = (String) set_it.next();
                if (tu_map.containsKey(tu)){   //如果包含该tuple，则找出下一轮抽取到的pattern
                    pattern = tu_map.get(tu);
                    for (String p: pattern){        //建立pattern之间的边
                        if (!order.containsKey(p)){
                            continue;
                        }
                        beh_order = order.get(p);     //若该pattern是被抽取到的，则保存其被抽取的轮次 ，及其在二维数组中的位置
                        seq_2 = seq.get(p);
                        if (beh_order == pre_order){    //若这两个pattern是在同一轮被抽取的，则两者间不存在边
                            continue;
                        }
                        if (Math.abs(beh_order-pre_order) != 1){    //若两个pattern被抽取的轮次相差大于1则这两个pattern之间也没有边
                            continue;
                        }
                        if(pre_order > beh_order){
                            temp = seq_1;
                            seq_1 = seq_2;
                            seq_2 = temp;
                        }
                        graph[seq_1][seq_2] = 1;    //将数组中对应位置上的值设为1
                    }
                }
            }
        }
        return graph;
    }
}
