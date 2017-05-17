package Snowball;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.net.estimate.SimpleEstimator;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * Created by Hedy on 2017/5/8.
 */
public class weka {

    public static void main(String[] args) throws  Exception {
        int index = 0 ;
        String[] tag = new String[9];
        tag[0] = "0.85_LO";
        tag[1] = "0.84_ME_1";
        tag[2] = "0.84_MA";
        tag[3] = "0.84_HO";
        tag[5] = "0.75_TL";
        tag[4] = "0.8_AC";
        tag[6] = "0.84_HE";
        tag[7] = "0.79_JO";    //Logistic
        tag[8] = "0.75_PR";   //SMO

        String DPfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\result\\DP_classify.txt";
        String datafile =  "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\Extract\\new_OL_data.txt";
        String prefix = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" ;
        String filename =  "\\data.arff";
        String Featurefile = "\\Feature.txt";
        HashMap<Integer,HashSet<String>> Correct = new HashMap<>();
        HashMap<Integer,HashSet<String>> Drift = new HashMap<>();
        HashSet<String> sub_correct = new HashSet<>();
        HashSet<String> drift ;

        //得到每个concept的分类结果
        /*for ( index = 0 ; index < 7; index ++ ){
            drift = new HashSet<>();
            SimpleLogisticByWeka(prefix+tag[index]+filename,prefix+tag[index]+Featurefile,sub_correct,drift);
            if (drift.size()!= 0 ){
                Drift.put(index,drift);
            }
            Correct.put(index,sub_correct);
        }
        drift = new HashSet<>();
        SMOByWeka(prefix+tag[8]+filename,prefix+tag[8]+Featurefile,sub_correct,drift);
        Correct.put(8,sub_correct);
        if (drift.size() != 0){
            Drift.put(8,drift);
        }
        drift = new HashSet<>();
        LogisticByWeka(prefix+tag[7]+filename,prefix+tag[7]+Featurefile,sub_correct,drift);
        Correct.put(7,sub_correct);
        if (drift.size() != 0){
            Drift.put(7,drift);
        }*/
        //找出所有的DP抽取到的tuple属于所有类别的概率
        HashMap<String,String> classify = new HashMap<>();    //保存每个tuple所属的class
        /*String driftfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\result\\drift_pattern.txt";
        HashSet<String> stddrift = new HashSet<>();
        OperateTextFile.ReadFiletoSet(driftfile,stddrift);
        //找到这些DP抽取到所有的tuple，并保存其在每个聚类中的随机游走的分数，将其分在分数最高的那个聚类中
        HashSet<String> tu = new HashSet<>();
        OperateTextFile.ReadFileByPa(datafile,stddrift,tu);    //tuple是org+loc
        String behind = "\\PRD_F_score.txt";
        DefineClass(prefix,tag,behind,classify,tu);
        //将最后的DP的分类结果写入文件中
        System.out.println(classify.size());
        OperateTextFile.writeMaptoFile(DPfile,classify);*/
        //因为之前已经保存了所有的结果，所以可以直接读取相关的信息
        OperateTextFile.ReadfiletoMap(DPfile,classify);


        for (index = 0 ; index < 9 ; index++){
            String patternfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\patternoftuples.txt";
            String tuplefile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\tuplesofpattern.txt";
            //读取整个抽取过程
            HashMap<String,HashSet<String>> tupleofpattern = new HashMap<>();
            HashMap<String,HashSet<String>> patternoftuple = new HashMap<>();
            ReadExtract(tuplefile,tupleofpattern);
            ReadExtract(patternfile,patternoftuple);
            //对抽取过程进行清洗
            Clean(Correct,classify,Drift,tupleofpattern,patternoftuple,index,tag);

            //对清洗之后的结果进行总结

        }



    }

    public static void Clean(HashMap<Integer,HashSet<String>>Correct, HashMap<String,String>classify, HashMap<Integer,HashSet<String>>Drift,HashMap<String,HashSet<String>>tuple,HashMap<String,HashSet<String>>pattern,int index,String[] tag){
        HashSet<String> std = Correct.get(index);   //正确的pattern
        HashSet<String> drift = Drift.get(index);   //DP
        HashSet<String> elem ;   //保存抽取到该tuple的所有pattern
        HashSet<String> delete_tu,delete_pa = new HashSet<>();

        boolean flag = true , flag1 ;
        Iterator it , in_it;
        String pa ,tu , cluster;
        while (flag){   //一直清洗到没有新的pattern或者tuple被删除
            flag = false;
            delete_tu = new HashSet<>();
            it = tuple.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                elem = (HashSet)entry.getValue();   //获取抽取到该tuple的所有pattern
                in_it = elem.iterator();
                while (in_it.hasNext()){   //先将前一轮被删除的pattern删除，再判断是否需要被删除
                    pa = (String) in_it.next();
                    if (delete_pa.contains(pa)){
                        in_it.remove();
                    }
                }
                if (elem.size() == 0 ){
                    flag = true;
                    it.remove();
                    delete_tu.add((String) entry.getKey());
                }
                //如果不仅有DP还有正确的pattern，则该tuple不需要考虑，直接保留
                else
                {
                    in_it = elem.iterator();
                    flag1 = false;   //标志该tuple是否需要检验
                    while (in_it.hasNext()){
                        pa = (String) in_it.next();
                        if (std.contains(pa)){
                            flag1 = true;     //只要包含一个正确的pattern，该tuple就不要检验
                            break;
                        }
                    }
                    if ( !flag1){   //若抽取到当前tuple的pattern都是DP，则需要进一步检验
                        cluster = classify.get(entry.getKey());
                        if ( !cluster.equals(tag[index])){    //如果该tuple不属于该类别，则删除
                            it.remove();
                            flag = true ;
                            delete_tu.add((String) entry.getKey());
                        }
                    }
                }
            }
            if (flag){   //若tuple删除了，则需要对pattern进行删除
                it  = pattern.entrySet().iterator();
                while (it.hasNext()){
                    Map.Entry entry = (Map.Entry)it.next();
                    pa = (String) entry.getKey();
                    if (!std.contains(pa) && !drift.contains(pa)){    //若该pattern是错误的pattern，则直接删除
                        delete_pa.add(pa);
                        flag = true;
                        it.remove();
                        continue;
                    }
                    //否则需要查看抽取到该pattern的tuple的情况
                    elem = (HashSet)entry.getValue();
                    in_it = elem.iterator();
                    while (in_it.hasNext()){    //先删除前一轮被删除的tuple
                        tu =(String) in_it.next();
                        if (delete_tu.contains(tu)){
                            in_it.remove();
                        }
                    }
                    if (elem.size() == 0 ){
                        flag = true;
                        it.remove();
                        delete_pa.add(pa);
                    }
                }

            }
        }
    }

    public static void ReadExtract(String filename , HashMap<String,HashSet<String>> map){
        HashSet<String> set = new HashSet<>();

        try{
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("can't find the file!"+filename);
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line , tag = null;
            String org,loc;
            while ((line = br.readLine()) != null){
                if (line.contains(":")){
                    tag = line.split(":")[0];
                    //将所有的tuple的顺序变成OL
                    if (line.indexOf("</O>") > line.indexOf("</L>")){
                        org = line.split("\t")[1];
                        loc = line.split("\t")[0];
                        tag = org+"\t"+loc;
                    }
                    set = new HashSet<>();
                    continue;
                }
                if (line.equals("")){
                    map.put(tag,set);
                    continue;
                }
                set.add(line);
                /*if (line.contains(":")){
                    tag = line.split(":")[0];
                    //将所有的tuple的顺序变成OL
                    if (line.indexOf("</O>") > line.indexOf("</L>")){
                        org = line.split("\t")[1];
                        loc = line.split("\t")[0];
                        tag = org+"\t"+loc;
                    }
                    continue;
                }
                if (line.equals("")){   //每一的类别抽取结束后，就放入map中
                    continue;
                }
                if (map.containsKey(line))  {   //若当前pattern之前抽取到其他的tuple,则在其基础上添加新的tuple，否则插入一个新的map
                    set = map.get(line);
                    set.add(tag);
                    map.put(line,set);
                }
                else {
                    set = new HashSet<>();
                    set.add(tag);
                    map.put(line,set);
                }*/
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void DefineClass(String pre, String[] tag , String be, HashMap<String,String> classify, HashSet<String> tuple){
        String filename , line ;
        String tu1,tu2;
        double score,sc;

        HashMap<String,Double>  max = new HashMap<>() ;   // 每个tuple得分最高的class
        for (int i = 0 ; i < 9 ;i ++){
            filename = pre+tag[i]+be;
            try {
                File file = new File(filename);
                BufferedReader br = new BufferedReader(new FileReader(file));
                while ((line = br.readLine()) != null){
                    if( !line.contains("<")){     //若该行不是tuple的信息，则直接跳过
                        continue;
                    }
                    tu1 = line.split("\t")[0]+"\t"+line.split("\t")[1];
                    tu2 = line.split("\t")[1]+"\t"+line.split("\t")[0] ;
                    if (tuple.contains(tu1) || tuple.contains(tu2)){     //如果该行是需要读取的tuple的信息，则保存信息
                        score = Double.parseDouble(line.split("\t")[2]);
                        if (max.containsKey(tu1) ){    //修改该tuple最高的得分
                            sc = max.get(tu1);
                            if (sc < score){
                                max.put(tu1,score);
                                classify.put(tu1,tag[i]);
                            }
                        }
                        else if (max.containsKey(tu2)){
                            sc = max.get(tu2);
                            if (sc < score){
                                max.put(tu2,score);
                                classify.put(tu2,tag[i]);
                            }
                        }
                        else {
                            max.put(tu1,score);   //若之前没有被抽取到，则直接将当前的得分作为其最高的得分记录下来
                            classify.put(tu1,tag[i]);
                        }
                    }
                }
                br.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void SimpleLogisticByWeka(String filepath, String feature, HashSet<String> sub , HashSet<String> drift) throws Exception{
        File file = new File(filepath);

        String line ;
        BufferedReader br = new BufferedReader(new FileReader(feature));
        ArffLoader loader = new ArffLoader();
        loader.setFile(file);
        Instances dataSet = loader.getDataSet();    //得到数据集中的数据结构
        dataSet.setClassIndex(dataSet.numAttributes()-1);   //设置数据集中表示类别的属性
        SimpleLogistic dt = new SimpleLogistic();    //实例化一个简单逻辑回归的对象

        //建立分类器模型
        dt.buildClassifier(dataSet);
        //System.out.println(dt.toString());   //输出训练模型

        //自己实现评测
        //利用模型进行预测
        int a0 = 0, a1 = 0 ,a2 = 0 , b0 = 0, b1 = 0 ,b2 = 0 , c0 = 0 , c1 = 0 , c2 = 0 ;   //记录每个类别的个数，方便计算评价指标
        for (int i = 0;  i < dataSet.numInstances(); i++){
            line = br.readLine();
            double classification = dt.classifyInstance(dataSet.instance(i));
            double classValue = dataSet.instance(i).classValue();
            if (classification == 1  && classValue == 1){
                sub.add(line.split("\t")[0]);
                a1 ++;
            }
            else if (classification == 0 && classValue == 0){
                a0++;
            }
            else if (classification == 2 && classValue == 2){
                drift.add(line.split("\t")[0]);
                a2++;
            }
            else if (classification == 0  && classValue != 0 ){
                b0++;
            }
            else if( classification == 1 && classValue != 1){
                sub.add(line.split("\t")[0]);
                b1++;
            }
            else if (classification == 2 && classValue != 2){
                drift.add(line.split("\t")[0]);
                b2++;
            }
            if (classValue == 0 && classification !=0 ){
                c0++;
            }
            else if (classValue == 1 && classification != 1){
                c1++;
            }
            else if (classValue == 2 && classification != 2){
                c2++;
            }
        }
        br.close();
        System.out.println("a0 = " +a0);
        System.out.println("a1 = " +a1);
        System.out.println("a2 = " +a2);
        System.out.println("b0 = " +b0);
        System.out.println("b1 = " +b1);
        System.out.println("b2 = " +b2);
        System.out.println("c0 = " +c0);
        System.out.println("c1 = " +c1);
        System.out.println("c2 = " +c2);

        System.out.println("precision = "+ (a1+a2+a0)*1.0/(a0+b0+a1+a2+b1+b2));
        System.out.println("recall = "+(a1+a2+a0)*1.0 / (a0+c0+a1+a2+c1+c2));

        //直接调用Evaluation即可完成
        Evaluation eval = null;
        for (int i = 0 ; i < dataSet.numInstances() ; i++){
            eval = new Evaluation(dataSet);
            eval.crossValidateModel(dt,dataSet,10,new Random(i));
        }
        System.out.println(eval.toSummaryString());   //输出总结信息
        System.out.println(eval.toClassDetailsString());   //输出分类详细信息
        System.out.println(eval.toMatrixString());    //输出分类的混肴矩阵
    }

    public static void LogisticByWeka(String filepath, String feature, HashSet<String> sub , HashSet<String> drift) throws Exception{
        File file = new File(filepath);
        String line ;
        BufferedReader br = new BufferedReader(new FileReader(feature));
        ArffLoader loader = new ArffLoader();
        loader.setFile(file);
        Instances dataSet = loader.getDataSet();    //得到数据集中的数据结构
        dataSet.setClassIndex(dataSet.numAttributes()-1);   //设置数据集中表示类别的属性
        Logistic dt = new Logistic();    //实例化一个简单逻辑回归的对象

        //建立分类器模型
        dt.buildClassifier(dataSet);
        //System.out.println(dt.toString());   //输出训练模型

        //自己实现评测
        //利用模型进行预测
        int a0 = 0, a1 = 0 ,a2 = 0 , b0 = 0, b1 = 0 ,b2 = 0 , c0 = 0 , c1 = 0 , c2 = 0 ;   //记录每个类别的个数，方便计算评价指标
        for (int i = 0;  i < dataSet.numInstances(); i++){
            line = br.readLine();
            double classification = dt.classifyInstance(dataSet.instance(i));
            double classValue = dataSet.instance(i).classValue();
            if (classification == 1  && classValue == 1){
                sub.add(line.split("\t")[0]);
                a1 ++;
            }
            else if (classification == 0 && classValue == 0){
                a0++;
            }
            else if (classification == 2 && classValue == 2){
                drift.add(line.split("\t")[0]);
                a2++;
            }
            else if (classification == 0  && classValue != 0 ){
                b0++;
            }
            else if( classification == 1 && classValue != 1){
                sub.add(line.split("\t")[0]);
                b1++;
            }
            else if (classification == 2 && classValue != 2){
                drift.add(line.split("\t")[0]);
                b2++;
            }
            if (classValue == 0 && classification !=0 ){
                c0++;
            }
            else if (classValue == 1 && classification != 1){
                c1++;
            }
            else if (classValue == 2 && classification != 2){
                c2++;
            }
        }

        br.close();
        System.out.println("a0 = " +a0);
        System.out.println("a1 = " +a1);
        System.out.println("a2 = " +a2);
        System.out.println("b0 = " +b0);
        System.out.println("b1 = " +b1);
        System.out.println("b2 = " +b2);
        System.out.println("c0 = " +c0);
        System.out.println("c1 = " +c1);
        System.out.println("c2 = " +c2);

        System.out.println("precision = "+ (a1+a2+a0)*1.0/(a0+b0+a1+a2+b1+b2));
        System.out.println("recall = "+(a1+a2+a0)*1.0 / (a0+c0+a1+a2+c1+c2));

        //直接调用Evaluation即可完成
        Evaluation eval = null;
        for (int i = 0 ; i < dataSet.numInstances() ; i++){
            eval = new Evaluation(dataSet);
            eval.crossValidateModel(dt,dataSet,10,new Random(i));
        }
        System.out.println(eval.toSummaryString());   //输出总结信息
        System.out.println(eval.toClassDetailsString());   //输出分类详细信息
        System.out.println(eval.toMatrixString());    //输出分类的混肴矩阵
    }

    public static void SMOByWeka(String filepath,String feature, HashSet<String> sub , HashSet<String> drift) throws Exception{
        File file = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(feature));
        String line ;
        ArffLoader loader = new ArffLoader();
        loader.setFile(file);
        Instances dataSet = loader.getDataSet();    //得到数据集中的数据结构
        dataSet.setClassIndex(dataSet.numAttributes()-1);   //设置数据集中表示类别的属性
        SMO dt = new SMO();    //实例化一个SMO的对象

        //建立分类器模型
        dt.buildClassifier(dataSet);
        System.out.println(dt.toString());   //输出训练模型

        //自己实现评测
        //利用模型进行预测
        int a0 = 0, a1 = 0 ,a2 = 0 , b0 = 0, b1 = 0 ,b2 = 0 , c0 = 0 , c1 = 0 , c2 = 0 ;   //记录每个类别的个数，方便计算评价指标
        for (int i = 0;  i < dataSet.numInstances(); i++){
            line = br.readLine();
            double classification = dt.classifyInstance(dataSet.instance(i));
            double classValue = dataSet.instance(i).classValue();
            if (classification == 1  && classValue == 1){
                sub.add(line.split("\t")[0]);
                a1 ++;
            }
            else if (classification == 0 && classValue == 0){
                a0++;
            }
            else if (classification == 2 && classValue == 2){
                drift.add(line.split("\t")[0]);
                a2++;
            }
            else if (classification == 0  && classValue != 0 ){
                b0++;
            }
            else if( classification == 1 && classValue != 1){
                sub.add(line.split("\t")[0]);
                b1++;
            }
            else if (classification == 2 && classValue != 2){
                drift.add(line.split("\t")[0]);
                b2++;
            }
            if (classValue == 0 && classification !=0 ){
                c0++;
            }
            else if (classValue == 1 && classification != 1){
                c1++;
            }
            else if (classValue == 2 && classification != 2){
                c2++;
            }
        }

        br.close();
        //直接调用Evaluation即可完成
        Evaluation eval = null;
        for (int i = 0 ; i < dataSet.numInstances() ; i++){
            eval = new Evaluation(dataSet);
            eval.crossValidateModel(dt,dataSet,10,new Random(i));
        }
        System.out.println(eval.toSummaryString());   //输出总结信息
        System.out.println(eval.toClassDetailsString());   //输出分类详细信息
        System.out.println(eval.toMatrixString());    //输出分类的混肴矩阵
    }

}


//得出预测效果评价指标
        /*if ( a0 != 0 ){
            precision[0] = a0 * 1.0 /(a0 + b0) ;
            recall[0] = a0 * 1.0 /(a0 + c0) ;
        }
        else{
            precision[0] = 0 ;
            recall[0] =0 ;
        }
        if (a1 != 0){
            precision[1] = a1 * 1.0 /(a1 + b1);
            recall[1] = a1 *1.0 /(a1 + c1);
        }
        else {
            precision[1] = 0 ;
            recall[1] = 0;
        }
        if (a2 != 0 ){
            precision[2] = a2 *1.0 /(a2+b2);
            recall[2] = a2 *1.0 /(a2+c2);
        }
        else {
            precision[2] =0 ;
            recall[2] = 0 ;
        }*/
        /*double Fm[] = new double[3];
        for (int i = 0 ; i < 3 ; i ++){
            Fm[i] = 2 * precision[i]*recall[i] / (precision[i]+recall[i]);
            System.out.println(precision+"\t"+recall+"\t"+Fm[i]);
        }*/