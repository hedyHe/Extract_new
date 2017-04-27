package Snowball;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.*;


/**
 * Created by Hedy on 2017/4/26.
 */
public class arff {

    public static void main(String[] args ) throws Exception{
        int index = 0;
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

        FastVector atts;
        FastVector attVals;
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
        //最后一列是分类结果
        //atts.addElement(new Attribute("class"),(new ));
       /* attVals = new FastVector();
        attVals.addElement("T");
        attVals.addElement("F");
        attVals.addElement("D");
        atts.addElement(new Attribute("class",attVals));*/

        data = new Instances("Pattern",atts,0);
        /*double[] vals=new double[data.numAttributes()];
        vals[0]=Math.PI;
        vals[1]=1;
        vals[2]=0;
        vals[3] = 1;
        data.add(new Instance(1.0,vals));*/

        //data.setClassIndex(data.numAttributes());
        CreateInstance(filename,data);
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

    public static void CreateInstance(String filename, Instances data){

        double[] vals ;
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("can't find the file!");
            }
            String line ;
            BufferedReader br = new BufferedReader(new FileReader(file));
            Instance instance;
            while ((line = br.readLine()) != null){
                //instance = new DenseInstance(data.numAttributes());
                vals = new double[data.numAttributes()];
                for (int i = 0 ; i < 9 ; i++){
                    vals[i] = Double.parseDouble(line.split("\t")[i+1]);
                }
                data.add(new Instance(1.0,vals));
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

}
