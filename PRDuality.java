package Snowball;

import weka.gui.visualize.MatrixPanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Hedy on 2017/4/25.
 */
public class PRDuality {


    public static Integer length = 0;
    public static Integer tuple_size  = 0 ;
    public static final long MAX_ITERATION_TIMES = 1000;
    public static final double MIN_ERRORS = 0.000001;
    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.000000");

    public static void main(String[] args) {
        int index = 0 ;
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

        String patternname = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" + tag[index] + "\\pattern.txt";
        String tuplename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" + tag[index] + "\\tuples.txt";
        String patternfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" + tag[index] + "\\patternoftuples.txt";
        String tuplefile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" + tag[index] + "\\tuplesofpattern.txt";
        String matrixfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" + tag[index] + "\\PRD_matrix.txt";
        String randomfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" + tag[index] + "\\PRD_random.txt";
        String F_file = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" + tag[index] + "\\PRD_F_score.txt";

        HashMap<String, Integer> pa_order = new HashMap<>();     //记录每个pattern被抽取的轮次
        HashMap<String, Integer> pattern = new HashMap<>();   //记录每个元素在矩阵中的位置
        HashMap<String, Integer> tuple_order = new HashMap<>();    //记录每个tuple被抽取的轮次
        HashMap<String, Integer> tuple = new HashMap<>();     //记录每个元素在矩阵中的位置
        HashMap<String, Integer> all_elem = new HashMap<>();  //所有的pattern和tuple放在一起

        //从文件读取元素
        OperateTextFile.ReadFileToMap(tuplename, tuple, tuple_order, 2, 0);    //先读取的是tuple，然后读取的是pattern
        OperateTextFile.ReadFileToMap(patternname, pattern, pa_order, 1, tuple.size());
        tuple_size = tuple.size();

        //找到最后一轮抽取到的pattern
        all_elem.putAll(pattern);
        all_elem.putAll(tuple);
        length = all_elem.size();
        System.out.println("tuple的个数是："+tuple.size());
        System.out.println("pattern的个数是:" + pattern.size());
        System.out.println("矩阵大小是:" + length);

        int[][] graph1, graph2;
        //建立pattern和tuple之间的图
        graph1 = CreateGraph(patternfile, pa_order, tuple_order, pattern, tuple, 0);   //0标记是从tuple抽pattern
        graph2 = CreateGraph(tuplefile, tuple_order, pa_order, tuple, pattern, 1); //1标记是从pattern抽tuple
        //合并两个矩阵,同时进行归一化
        double[][] graph;
        graph = Merge(graph1, graph2);
        /*System.out.println("graph[504][0]:"+graph[504][0]);
        System.out.println("graph[504][1]:"+graph[504][1]);
        System.out.println("graph[504][2]:"+graph[504][2]);
        System.out.println("graph[504][3]:"+graph[504][3]);
        System.out.println("graph[505][0]:"+graph[505][0]);
        System.out.println("graph[505][1]:"+graph[505][1]);
        System.out.println("graph[505][2]:"+graph[505][2]);
        System.out.println("graph[505][3]:"+graph[505][3]);*/

        //将所有的信息都写入文件中
        OperateTextFile.writeMapTofile(matrixfile, pattern);
        OperateTextFile.writeMapTofile(matrixfile, tuple);
        OperateTextFile.WriteMatricToFile1(matrixfile, graph, length);

        //前向随机游走,计算召回率
        double[] recall;
        recall = RandomWalk_Rec(tuple_order,all_elem,graph);
        /*for (int i =0 ; i < length ; i++){
            System.out.print(recall[i]+"\t");
            if ((i +1) % 100 == 0 ){
                System.out.println();
            }
        }*/
        HashMap<Integer,double[]> result = new HashMap<>();
        result.put(0,recall);
        OperateTextFile.WriteMaptoFile(randomfile,result,all_elem,length);

        //找到抽取过程中的最后一轮的pattern
        Iterator it = pa_order.entrySet().iterator();
        int ord,max = 0 ;
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            ord = (Integer) entry.getValue() ;
            max = ord > max ? ord : max ;
        }

        //向后游走，计算准确率
        double[] precision ;
        precision = RandomWalk_Pre(pa_order,all_elem,graph,max);
        /*for (int i =0 ; i < length ; i++){
            System.out.print(precision[i]+"\t");
            if (( i +1 ) % 100 == 0 ){
                System.out.println();
            }
        }*/
        result.put( 1 , precision ) ;
        OperateTextFile.WriteMaptoFile(randomfile,result,all_elem,length);

        //计算每个元素的F-score
        HashMap<String, Double> F_score = new HashMap<>();
        Cacluate_F(recall,precision,all_elem,F_score);
        OperateTextFile.WriteMapToFile(F_file,F_score);

        //根据F值排序
        ValueComparator bvc = new ValueComparator(F_score);
        TreeMap<String,Double> sorted_map = new TreeMap<>(bvc);
        OperateTextFile.WriteMapToFile(F_file,sorted_map);

    }

    public  static void Cacluate_F(double[] recall, double[] precision, HashMap<String ,Integer> order,HashMap<String,Double>F_score){
        double re,pre , result;
        String elem;
        int index,count = 0 ;
        Iterator it = order.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            elem = (String) entry.getKey();
            index = (Integer) entry.getValue();
            re = recall[index];
            pre = precision[index];
            System.out.println("re:" +re+"  pre:"+pre);
            //result = 2 * re * pre /(re+pre);
            if (re == 0 && pre == 0){
                result = 0 ;
            }
            else
                result = Double.parseDouble(df.format(2 * re * pre / (re + pre)));
            if (result != 0 ){
                System.out.print(result+"\t");
                count++;
                if (count % 100 == 0){
                    System.out.println();
                }
            }
            F_score.put(elem,result);
        }

    }

    public static double[] RandomWalk_Pre(HashMap<String,Integer> pa_level,HashMap<String,Integer> matrix ,double[][] transMatrix ,int turn){
        //matrix保存的是每个元素在矩阵中的位置，transMatrix保存的是元素的关系图
        double[] start = new double[length];
        Iterator it = pa_level.entrySet().iterator();
        int level,index;
        String pa;
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            level = (Integer) entry.getValue();
            if (level == turn){
                pa = (String) entry.getKey();
                index = matrix.get(pa);    //获取当前pattern在矩阵中的位置
                start[index] = 1;
            }
        }

        //随机游走
        int iterationTimes = 0 ;
        boolean flag = true;
        double tep;
        while (iterationTimes < MAX_ITERATION_TIMES){
            double[] temp = new double[length];
            if (flag){
                for(int i = 0 ; i < length ; i++){
                    temp[i] = start[i];
                    if (start[i] == 1){   //如果是seeds则不需要计算
                        continue;
                    }
                    tep = 0 ;
                    for(int j = 0; j < length; j++){
                        tep += Double.parseDouble(df.format(transMatrix[i][j]*start[j]));
                    }
                    start[i] = Double.parseDouble(df.format(tep));
                    /*if (start[i] != 0 ) {
                        System.out.print(i + ":" + start[i] + "\t");
                        if ( ( i + 1 ) % 100 == 0){
                            System.out.println();
                        }
                    }*/
                }
                if(judge(temp,start, MIN_ERRORS)){
                    flag = false;
                }
            }else
                break;
            iterationTimes++;
        }
        //System.out.println("迭代到第"+iterationTimes+"轮结束!");
        return start;
    }

    public static double[] RandomWalk_Rec(HashMap<String ,Integer> tu_level,HashMap<String,Integer> matrix, double[][] transMatrix){
        //tu_level保存的是每个元素被抽取的轮次，matrix保存的是每个元素在矩阵中位置
        double[] start = new double[length];   //开始时的向量
        Iterator it = tu_level.entrySet().iterator();
        int level , index, all = 0  ;
        String tuple;
        //初始化开始向量,先找到tuple seeds的个数
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            level =(Integer) entry.getValue();
            if (level == 1){
                all++;
            }
        }
        double re = Double.parseDouble(df.format(1.0 / all));
        //System.out.print(re+"\t");
        it = tu_level.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            level =(Integer) entry.getValue();
            if (level == 1){   //找到起始元素所在的位置将其设置为1
                tuple = (String) entry.getKey();
                index = matrix.get(tuple);
                start[index] = re ;
                //System.out.print(index+"\t");
            }
        }
        //进行随机游走
        int iterationTimes = 0 ;
        boolean flag = true;
        double tep ;
        double[] temp = new double[length] ;

        while(iterationTimes < MAX_ITERATION_TIMES){
            for (int i = 0 ; i < length ; i++){
                temp[i] = start[i];
            }
            if(flag == true){
                //System.out.println("\n第"+iterationTimes+"轮迭代的结果是:");   //+length+"\n");
                for(int i = 0 ; i < length ; i++){
                    //temp[i] = start[i];
                    if (iterationTimes % 2 == 0 ){   //若当前计算的是pattern的recall，则tuple的recall不需要改变
                        if (i < tuple_size ){
                            i = tuple_size-1 ;   //直接跳到需要计算的数组的下标
                            continue;
                        }
                    }
                    else {
                        if ( i >= tuple_size){   //否则pattern的recall不需要改变
                            break;
                        }
                    }
                    tep = 0 ;
                    for(int j = 0; j < length; j++){
                        tep += Double.parseDouble(df.format(transMatrix[i][j]*start[j]));
                    }
                    start[i] = Double.parseDouble(df.format(tep));
                    /*if (start[i] != 0 ) {
                        System.out.print(i + ":" + start[i] + "\t");
                        if ( ( i + 1 ) % 100 == 0){
                            System.out.println();
                        }
                    }*/
                }
                if(judge(temp,start, MIN_ERRORS)){
                    flag = false;
                }
            }else
                break;
            iterationTimes++;
        }
        //System.out.println("迭代到第"+iterationTimes+"轮结束!");

        /*for (int i = 0; i < length ; i++){
            System.out.print(temp[i]+"\t");
            if ((i+1) % 100 == 0 ){
                System.out.println();
            }
        }*/
        return start;
    }

    public static boolean judge(double a[], double b[], double minErrors){
        boolean flag = true;
        for(int i = 0 ; i < length ; i++){
            if(Math.abs(a[i]-b[i]) < minErrors)
                continue;
            else{
                flag = false;
                break;
            }
        }
        return flag;
    }

    public static double[][] Merge(int[][] g1 , int[][] g2)
    {

        double[][] g = new double[length][length];
        int all ;
        double score;

        for (int i = 0 ; i < length ; i++ ){
            all = 0 ;
            for (int j = 0 ; j < length ; j++){
                if (g1[i][j] == 1 || g2[i][j] == 1){
                    all++;
                }
            }
            if (all == 0 ){
                continue;
            }
            score = Double.parseDouble(df.format(1.0 / all));
            for (int j = 0 ; j < length ; j++ ){
                if (g1[i][j] == 1 || g2[i][j] == 1){
                    g[i][j] = score;
                    //System.out.print(g[i][j]+"\t");
                }
            }
            //System.out.println();
        }

        return g;
    }

    public static int[][] CreateGraph(String filename , HashMap<String , Integer> pa , HashMap<String , Integer> tu , HashMap<String,Integer> pa_order , HashMap<String,Integer> tu_order ,int tab)
    {
        int[][] graph = new int[length][length];

        try {
            File f1 = new File(filename);
            if (!f1.exists()){
                System.out.println("can't find the file!"+filename);
            }

            BufferedReader br1 = new BufferedReader(new FileReader(f1));
            String line , pattern = null  ;
            int pa_seq  ,tu_seq, index = 1 ;    // 记录pattern和tuple在矩阵中的位置
            boolean flag = true ;

            while ((line = br1.readLine()) != null){
                if (line.contains(":")){   //判断当前行是否是抽取到的一个新的pattern
                    pattern = line.split(":")[0];
                    if (pa.containsKey(pattern)){   //若当前的pattern最后被删除了，则不需要其被抽取的情况
                        flag = true;
                        index = pa.get(pattern);  //获得该pattern被抽取的轮次
                    }
                    else {
                        flag = false;
                    }
                    continue;
                }
                if (line.equals("")){
                    continue;
                }

                if (!flag){    //判断当前信息是否需要读取
                    continue;
                }
                //否则读出抽取到该pattern的tuple,并设置两者之间的边
                if ( !tu.containsKey(line)){   //若最终的矩阵中没有该元素，则直接跳到下一步循环
                    continue;
                }

                tu_seq = tu.get(line) ;   //获取该tuple被抽取的轮次
                //System.out.println(line + "\t"+index+"\t"+tu_seq);
                if(index == (tab + tu_seq)){    //若该pattern被tuple抽取到的，则设置两者之间存在边
                    tu_seq = tu_order.get(line) ;
                    pa_seq = pa_order.get(pattern) ;
                    //System.out.println(pattern+"\t"+line+"\t"+1);
                    graph[pa_seq][tu_seq] = 1 ;
                    graph[tu_seq][pa_seq] = 1;
                    // System.out.println(pa_seq+"\t"+tu_seq+" : 1");
                }
            }
            br1.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return graph;
    }

}
