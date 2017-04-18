package Snowball;

import java.util.*;

/**
 * Created by Hedy on 2017/4/16.
 */
public class RandomWalk {

    public static Integer length = 0;
    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    public static void main(String[] args){
        //String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\0.85_LO\\tuples.txt";
        String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\0.85_LO\\pattern.txt";
        String tuplefile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\0.85_LO\\tuplesofpattern.txt";
        String patternfile ="F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\0.85_LO\\patternoftuples.txt";
        String matrixfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\0.85_LO\\Matrix.txt";
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
        for (String p : pa_order.keySet()){
            if (!pa_map.containsKey(p)){
                System.out.println(p);
            }


        }
        //从文件中读取每个tuple可以抽取到的pattern
        OperateTextFile.ReadFiletoMap(patternfile,tu_map);
        System.out.println("the size of tu_map is:"+tu_map.size());
        length = pa_order.size();
        //建立tuple之间的关系
        double [][] dir_graph ; //= new int[length][length];
        dir_graph = FindRelation(pa_map,tu_map,pa_order,pa_seq);

        //将矩阵归一化
        int count  = 0 ;
        for (int i = 0 ; i < length ; i++ ){
            count = 0 ;
            for (int j = 0 ; j < length ; j++){
                if (dir_graph[i][j] == 1){
                    count++;
                }
            }
            if (count == 0 ){
                continue;
            }
            for (int j = 0 ; j < length ; j++){
                if (dir_graph[i][j] == 1){
                    dir_graph[i][j] = Double.parseDouble(df.format(1.0 / count));
                }
            }
        }

        OperateTextFile.WriteMatricToFile(matrixfile,dir_graph,length);

    }

    public static double[][] FindRelation(HashMap<String,HashSet<String>> pa_map,HashMap<String,HashSet<String>> tu_map, HashMap<String,Integer> order ,HashMap<String ,Integer> seq ){

        double graph[][] = new double[length][length];
        Iterator it = pa_map.entrySet().iterator();
        Iterator set_it ;
        String pa, tu;
        HashSet<String> tuple , pattern;
        int pre_order, beh_order,temp;
        int seq_1 , seq_2;
        //HashSet<String> tu;
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            pa = (String)entry.getKey();
            //System.out.print("当前的pattern对象是:"+pa);
            pre_order = order.get(pa);
            seq_1 = seq.get(pa);
            //System.out.print("\t"+temp);
            if (!order.containsKey(pa)){
                continue;
            }
            tuple = (HashSet<String>) entry.getValue();
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
                        beh_order = order.get(p);
                        seq_2 = seq.get(p);
                        //System.out.println(p+"\t"+beh_order);
                        if (beh_order == pre_order){
                            continue;
                        }
                        if (Math.abs(beh_order-pre_order) != 1){
                            continue;
                        }
                        if(pre_order >  beh_order){
                            temp = seq_1;
                            seq_1 = seq_2;
                            seq_2 = temp;
                        }
                        graph[seq_1][seq_2] = 1;    //将数组中对应位置上的值设为1
                        //System.out.println(seq_1+"\t"+seq_2+":1");
                    }
                }
            }
        }
        return graph;
    }
}
