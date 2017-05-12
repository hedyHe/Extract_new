package Snowball;


import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;


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
        tag[4] = "0.79_JO";
        tag[5] = "0.75_TL";
        tag[6] = "0.75_PR";
        tag[7] = "0.8_AC";
        tag[8] = "0.84_HE";

        String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\data.arff";

        J48ByWeka(filename);

    }

    public static void J48ByWeka(String filepath) throws Exception{
        File file = new File(filepath);
        ArffLoader loader = new ArffLoader();
        loader.setFile(file);
        Instances dataSet = loader.getDataSet();    //得到数据集中的数据结构
        dataSet.setClassIndex(dataSet.numAttributes()-1);   //设置数据集中表示类别的属性
        J48 dt = new J48();    //实例化一个J48的决策树对象
        double precision[] = new double[3] , recall[] = new double[3];

        //建立分类器模型
        dt.buildClassifier(dataSet);
        System.out.println(dt.toString());   //输出训练模型

        //自己实现评测
        //利用模型进行预测
        int a0 = 0, a1 = 0 ,a2 = 0 , b0 = 0, b1 = 0 ,b2 = 0 , c0 = 0 , c1 = 0 , c2 = 0 ;   //记录每个类别的个数，方便计算评价指标
        for (int i = 0;  i < dataSet.numInstances(); i++){
            double classification = dt.classifyInstance(dataSet.instance(i));
            double classValue = dataSet.instance(i).classValue();
            if (classification == 1  && classValue == 1){
                a1 ++;
            }
            else if (classification == 0 && classValue == 0){
                a0++;
            }
            else if (classification == 2 && classValue == 2){
                a2++;
            }
            else if (classification == 0  && classValue != 0 ){
                b0++;
            }
            else if( classification == 1 && classValue != 1){
                b1++;
            }
            else if (classification == 2 && classValue != 2){
                b2++;
            }
            else if (classValue == 0 && classification !=0 ){
                c0++;
            }
            else if (classValue == 1 && classification != 1){
                c1++;
            }
            else if (classValue == 2 && classification != 2){
                c2++;
            }
        }

        //得出预测效果评价指标
        if ( a0 != 0 ){
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
        }
        double Fm[] = new double[3];
        for (int i = 0 ; i < 3 ; i ++){
            Fm[i] = 2 * precision[i]*recall[i] / (precision[i]+recall[i]);
            System.out.println(precision+"\t"+recall+"\t"+Fm[i]);
        }
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
