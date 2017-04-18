package Snowball;

import java.util.*;

/**
 * Created by Hedy on 2017/4/1.
 * 添加惩罚函数
 */
public class New_Pa_Sim {

    private static final double threshold = 0.0575;
    private static final double  ratio = 0.6;

    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    public static void main(String[] args){
        //String prefix  = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\";

        String prefix = "Snowball/punish/";

        String tuplename = prefix + "tuple3_pattern_OL.txt";
        String tupleSimilar = prefix + "Tu_similarity.txt";
        String subtupleSimilar = prefix+threshold + "_sub_Tu_similar.txt";
        String patternSimilar = prefix+threshold +"_no_cluster_pa.txt";
        String patternSimilar2 = prefix+threshold+"_cluster.txt";
        String delete_similarity = prefix +threshold +"_final.txt";

        HashSet<String> tup_set = new HashSet<>();
        HashMap<String , Double> tup_simil  = new HashMap<>();   //记录tuples之间的相似度
        HashSet<List<String> > delete_pa = new HashSet<>();   // 被聚类的pattern的信息
        HashSet<List<String>> pattern_set = new HashSet<>();     //记录每个pattern抽取到的tuples数
        HashSet<List<String>> new_pa_set = new HashSet<>();     //记录合并之后的每个pattern族对应的tuples

        System.out.println("main:现将需要用到的tuple抽取出来");
        Tuple_Similarity.DrawTuples(tuplename,tup_set);
        System.out.println("main:将这些tuples之间的相似度提取出来，并写入文件中");
        Pattern_Similarity.DrawTupleSimilar(tupleSimilar,subtupleSimilar,tup_set);
        System.out.println("main:从文件中将tuples的相似度读取出来");
        Pattern_Similarity.ReadSimilar(subtupleSimilar,tup_simil);
        System.out.println("main:将需要计算相似度的pattern提取出来");
        Pattern_Similarity.ReadPatternToSet(tuplename,pattern_set);
        boolean flag   ;
        //计算pattern的相似度,将小于阈值的相似度删除,将相似度大于阈值的pattern进行合并，对应的tuples只存储对相似度有贡献的tuples
        flag = PatternSimile(pattern_set,new_pa_set,delete_pa,tup_simil);   //pattern_set保存的是每个pattern抽取的情况，new_pa_set保存的是聚类之后的pattern，delete_pa保存的是不能聚类的pattern，tup_simil保存的是每对tuple之间的相似度
        int count = 1 ;
        Pattern_Similarity.WriteClusterToTxt(patternSimilar,delete_pa,count);
        System.out.println("flag="+flag);
        while (flag){
            System.out.println("到了"+count+"轮！");
            System.out.println("新合成的pattern数有:"+new_pa_set.size());
            System.out.println("将新的一轮计算出pattern的相似度后合并成的新的pattern写入文件中");
            Pattern_Similarity.WriteClusterToTxt(patternSimilar2,new_pa_set,count);
            System.out.println("main:将中间删除的pattern相似度写入文件！");
            Pattern_Similarity.WriteClusterToTxt(delete_similarity,delete_pa,count);
            count++;
            delete_pa.clear();
            pattern_set.clear();
            pattern_set = new_pa_set;
            new_pa_set = new HashSet<>();
            flag = PatternSimile(pattern_set,new_pa_set,delete_pa,tup_simil);   //pattern_set保存的是每个pattern抽取的情况，map保存的是每对pattern之间的相似度，tup_simil保存的是每对tuple之间的相似度
            if(pattern_set.size() == 1){   //若只剩一个pattern，则不需要再聚类，直接退出
                break;
            }

        }

        //System.out.println("将最后的结果写入文件中!");
        Pattern_Similarity.WriteClusterToTxt(delete_similarity,delete_pa,count);
        //Pattern_Similarity.WriteClusterToTxt(delete_similarity,pattern_set,count);
        System.out.println("main:ending!");

    }

    public static boolean PatternSimile(HashSet<List<String>>  pa_set, HashSet<List<String>> new_pa_set, HashSet<List<String>> delete,HashMap<String , Double> tuple_sim){

        System.out.println("PatternSimile:计算pattern列表之间的相似度!");

        List<String> pa1;
        List<String> pa2;
        String tu1, tu2;
        boolean flag , total_flag = false;
        HashSet<String> cacul_pa  = new HashSet<>();  //记录已经计算过相似度的pattern对

        Iterator it  = pa_set.iterator();
        while(it.hasNext()){
            pa1 = new ArrayList<>((List<String>) it.next());
            tu1 = pa1.get(0);
            Iterator iter  = pa_set.iterator();
            flag  = false;
            while(iter.hasNext()){
                pa2 = (List<String>) iter.next();
                tu2 = pa2.get(0);
                if(tu1.equals(tu2)){   //若是相同的pattern，则不需要比较
                    continue;
                }
                //否则先判断这两个pattern是否已经计算过相似度
                if(cacul_pa.add(tu1+"\t"+tu2)  && cacul_pa.add(tu2+"\t"+tu1)){   //两个同时添加成功才说明之前没有计算过相似度
                    flag = ( flag || CaculPaSim(pa1,pa2,new_pa_set,tuple_sim)) ;    //若相似度大于阈值，则将新合成的pattern写入new_pa_set集合中
                }
                else
                    continue;
            }
            total_flag = (total_flag || flag);
        }
        //所有的相似度都计算结束之后，把没有被聚类的pattern提取出来
        DrawNoClusterPa(pa_set,new_pa_set,delete);
        System.out.println("这一轮没有被聚类的pattern有："+delete.size());
        return total_flag;
    }

    public static void DrawNoClusterPa(HashSet<List<String>> pa_set , HashSet<List<String>> new_pa_set , HashSet<List<String>>delete){
        Iterator it = pa_set.iterator();
        List<String> ls,ls1 ;
        String pa,pa1 ;
        boolean flag ;
        HashSet<String> set ;
        while (it.hasNext()){
            ls  = (List<String>) it.next();
            //判断ls中的pattern是否都出现在new_pa_set中了，若都出现了则说明下一步需要聚类，否则不需要考虑，追写入删除集合中
            pa = ls.get(0);
            //判断该pattern是否被合成
            flag = false;
            Iterator iter = new_pa_set.iterator();
            while (iter.hasNext()){
                ls1 = (List<String>) iter.next();
                pa1 = ls1.get(0);
                if(pa.split("\t").length > pa1.split("\t").length ){    //若当前包含的pattern数比合并之后的pattern数大，则不需要比较，直接进入下一轮比较
                    continue;
                }

                //将pa1中的pattern一个个取出来，放在set中
                set = new HashSet<>();
                for(int i = 0 ; i < pa1.split("\t").length ; i++ ){
                    set.add(pa1.split("\t")[i]);
                }
                for(int i = 0 ; i < pa.split("\t").length ; i++){
                    if(!set.contains(pa.split("\t")[i])) {    //只要发现有一个pattern不存在，就结束比较，进入下一轮比较
                        flag = false;
                        break;
                    }
                    if( i == pa.split("\t").length - 1 ) {     //若到了最后一个pattern都存在，则该pattern是需要被合并的pattern
                        flag = true;
                    }
                }

                if(flag){    //若判断出该pattern是需要合并的pattern，则直接退出循环
                    break;
                }
            }
            if(!flag)  {    //若该pattern没有被合并，则写入删除集合中
                delete.add(ls);
            }
        }

    }

    public static boolean CaculPaSim(List<String> pa1, List<String> pa2, HashSet<List<String>> new_set, HashMap<String ,Double>tu_sim){

        HashSet<String> tu = new HashSet<>();    //保存对相似度计算有贡献的tuples
        HashSet<String> pa = new HashSet<>();    //保存该聚类中的所有pattern
        String tu1,tu2,max_tu1 = new String(),max_tu2 = new String();          //max_tu1和max_tu2保存的是相似度最大的两个tuples
        Double similarity = 0.0 ;
        double now_sim  ;
        boolean flag ;
        String new_pa = new String() ;
        HashSet<String> tuple1 = new HashSet<>();
        HashSet<String> tuple2  = new HashSet<>();

        //依次比较两个pattern的tuples，计算其相似度,分母为最小的tuples的个数
        List<String> temp ;
        if(pa1.size() > pa2.size()){    //将Pa1调换成tuple个数小的那个列表
            temp = pa1 ;
            pa1 = pa2;
            pa2 = temp;
        }

        temp = new ArrayList<>();
        double  max ;
        //System.out.println("当前需要合成的pattern为:"+pa1+"\n"+pa2);
        for(int k = 1 ; k < pa1.size() ; k++){   //从第一个tuple开始计算相似度
            tu1 = pa1.get(k);
            max = 0.0 ;
            flag = false ;
            for(int m = 1 ; m < pa2.size() ; m++){
                tu2 = pa2.get(m);
                if(tu1.equals(tu2)){    //若遇到相同的tuple则直接将该轮的相似度最大值置为1，并退出当前循环
                    max = 1;
                    max_tu1 = tu1;     //并记录下当前的两个tuples
                    max_tu2 = tu2;
                    flag = true;
                    break;
                }
                //否则，首先判断这两个tuples是否都有相似度，若没有则直接跳转到一下个比较
                if(tu_sim.containsKey(tu1+"\t"+tu2 ) ){
                    now_sim = tu_sim.get(tu1+"\t"+tu2);
                }
                else{
                    if(tu_sim.containsKey(tu2+"\t"+tu1) ){
                        now_sim = tu_sim.get(tu2+"\t"+tu1);
                    }
                    else
                        continue;    //直接跳转到下一轮比较
                }
                if(now_sim > max){     //更新当前的最大值
                    max = now_sim;
                    max_tu1 = tu1;
                    max_tu2 = tu2;
                    flag = true;
                }
            }
            //每一轮结束后，将该轮相似度最大的两个tuples保存到集合中
            if(flag){
                tu.add(max_tu1);
                tu.add(max_tu2);
                tuple1.add(max_tu1);
                tuple2.add(max_tu2);
                //System.out.println("需要加入的tuples有:"+max_tu1+"\t"+max_tu2);
            }
            similarity += max ;   //加入每一轮的最大相似度
        }
        if(similarity == 0){
            return false;
        }
        int denom = pa1.size() - 1 ;
        double similar = Double.parseDouble(df.format(similarity * 1.0 / denom));
        double punish1 = 1.0 / ( 1 + 9.0 / ( 1 + Math.pow(tuple1.size() * 1.0 / pa1.size() , 10)));
        double punish2 = 1.0 / ( 1 + 9.0 / ( 1 + Math.pow(tuple2.size() * 1.0/ pa2.size() , 10)));
        double punish = ratio * punish1 + ( 1 - ratio ) * punish2;
        similarity = Double.parseDouble(df.format(similar * punish));

        //判断相似度是否小于阈值，若小于则不需要记录
        if(similarity < threshold){
            return false ;
        }
        System.out.println(pa1.get(0)+"\t"+pa2.get(0)+"的相似度是:"+similarity+"\t"+punish+"\t"+ similar);
        //否则将两个pattern进行合并
        if(pa1.get(0).contains("\t")){
            for(int i = 0 ; i< pa1.get(0).split("\t").length ; i++){
                if(pa.add(pa1.get(0).split("\t")[i])) {     //若添加成功，则说明该pattern没有产生重合，需要加入到新的pattern字符串中
                    new_pa += pa1.get(0).split("\t")[i]+"\t";
                }
            }
        }
        else if(pa.add(pa1.get(0))){
            new_pa += pa1.get(0)+"\t";
        }
        if(pa2.get(0).contains("\t")){
            for (int i = 0 ; i < pa2.get(0).split("\t").length ; i++){
                if(pa.add(pa2.get(0).split("\t")[i])){
                    new_pa += pa2.get(0).split("\t")[i]+"\t";
                }
            }
        }
        else if(pa.add(pa2.get(0))){
            new_pa += pa2.get(0)+"\t";
        }

        temp.add(0,new_pa);

        //将有贡献的tuples添加到列表中
       // System.out.println("将有贡献的tuples添加到列表中!");
        Iterator it = tu.iterator();
        while (it.hasNext()){
            tu1  = (String) it.next();
            //System.out.println(tu1);
            temp.add(tu1);
        }

       // System.out.println("当前合成的新pattern为："+ temp);

        new_set.add(temp);

        return true ;
    }


}
