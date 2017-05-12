package Snowball;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.*;
import java.util.HashSet;


/**
 * Created by Hedy on 2017/4/26.
 */
public class arff {

    public static void main(String[] args ) throws Exception{
        int index = 8 ;
        String[] tag = new String[9];
        tag[0]="0.85_LO";
        tag[1]="0.84_ME_1";
        tag[2] = "0.84_MA";
        tag[3] = "0.84_HO";
        tag[4] = "0.79_JO";
        tag[5] = "0.75_TL";
        tag[6] = "0.75_PR";
        tag[7] = "0.8_AC";
        tag[8] = "0.84_HE";

        String filename ="F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\Feature.txt";
        String arfffile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\data.arff";
        String stdfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\result\\correct.txt";
        String driftfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\result\\drift_pattern.txt";

        FastVector atts;
        FastVector attVals ;
        Instances data;

        //set up attributes
        atts = new FastVector();
        //属性1是pattern抽取到的tuple的频率分布
        atts.addElement(new Attribute("att1"));
        //属性2是互斥的聚类个数
        atts.addElement(new Attribute("att2"));
        //属性3是随机游走的得分
        atts.addElement(new Attribute("att3"));
        //属性4是子集的平均得分
        atts.addElement(new Attribute("att4"));
        //属性5是子集的个数
        atts.addElement(new Attribute("att5"));
        //属性6是子集的标准偏差
        atts.addElement(new Attribute("att6"));
        //属性7是该pattern被抽取的轮次
        atts.addElement(new Attribute("att7"));
        //属性8是该概念下的抽取到的所有的pattern的个数
        atts.addElement(new Attribute("att8"));
        //属性9是该概念下能抽取的最大轮次
        atts.addElement(new Attribute("att9"));
        //属性10是分类结果
        //atts.addElement(new Attribute("att0"));
        //最后一列是分类结果
        attVals = new FastVector();
        attVals.addElement("0");
        attVals.addElement("1");
        attVals.addElement("2");
        atts.addElement(new Attribute("class",attVals));

        data = new Instances("Pattern",atts,0);
        /*double[] vals=new double[data.numAttributes()];
        vals[0]=Math.PI;
        vals[1]=1;
        vals[2]=0;
        vals[3] = 1;
        data.add(new Instance(1.0,vals));*/

        //data.setClassIndex(data.numAttributes());
        String label = tag[index].split("_")[1];
        CreateInstance(filename,stdfile,driftfile,label,data);
        System.out.println(data);
        //将数据写入文件夹中
        WriteToArff(arfffile,data);

    }


    public static void WriteToArff(String filename, Instances data){
        System.out.println("将数据写入文件中");
        System.out.println(data);

        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            bw.write(data.toString());
            bw.flush();
            bw.close();
            /*saver.setFile(new File(filename));
            saver.writeBatch();*/
        }catch (IOException e){
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    public static void CreateInstance(String filename, String stdfile,String driftfile,String tag,Instances data){

        HashSet<String> set = new HashSet<>();
        double[] vals ;
        try {
            File file = new File(filename);
            File std = new File(stdfile);
            File drift = new File(driftfile);
            if (!drift.exists()){
                System.out.println("can't find the file!"+driftfile);
            }
            if (!std.exists()){
                System.out.println("can't find the file!"+stdfile);
            }
            if (!file.exists()){
                System.out.println("can't find the file!"+filename);
            }
            String line ;
            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedReader br1 = new BufferedReader(new FileReader(std));
            BufferedReader br2 = new BufferedReader(new FileReader(drift));
            //从文件中将发生漂移的drift读取出来
            HashSet<String> driftset = new HashSet<>();
            while ((line = br2.readLine()) != null){
                driftset.add(line);
            }
            //从文件中将正确的pattern读取出来
            boolean flag = false ;
            while ((line = br1.readLine()) != null){
                if (line.contains(tag+":")){
                    flag = true;
                }
                if (flag){
                    //如果读取到tuple，则结束读取
                    if (line.contains("</O>")){
                        break;
                    }
                    set.add(line);
                }
            }

            while ((line = br.readLine()) != null){
                //instance = new DenseInstance(data.numAttributes());
                vals = new double[data.numAttributes()];
                for (int i = 0 ; i < 9 ; i++){
                    vals[i] = Double.parseDouble(line.split("\t")[i+1]);
                }
                if (!set.contains(line.split("\t")[0])){
                    vals[9] = 0 ;
                }
                else{
                    if (driftset.contains(line.split("\t")[0])){
                        vals[9] = 2 ;   //如果是漂移点，则设置为2
                    }
                    else
                        vals[9] = 1 ;   //正确的就是1
                }
                data.add(new Instance(1.0,vals));
            }
            br.close();
            br1.close();
            br2.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

}
