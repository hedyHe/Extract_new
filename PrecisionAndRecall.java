package Snowball;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Created by Hedy on 2017/5/2.
 */
public class PrecisionAndRecall {
    public static void main(String[] args){

        String expr = "Mutual";    //实验名
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

        String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result"+tag[index]+"\\pattern.txt";
        String standfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\result\\correct.txt";
        String pre_ra = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\result\\Pre_Rec.txt";
        HashSet<String> result = new HashSet<>();
        HashSet<String> standard = new HashSet<>();

        OperateTextFile.ReadFiletoSet(filename,result);
        OperateTextFile.ReadFiletoSet(standfile,standard);
        double[] pre_rec =  Precision(result,standard);   //计算得到正确率和召回率
        System.out.println("该实验的正确率是:"+pre_rec[0]);
        System.out.println("该实验的召回率是:"+pre_rec[1]);

        try {
            File file = new File(pre_ra);
            if (!file.exists()){
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            bw.write(expr+":\n");
            bw.write("precision:"+pre_rec[0]+"\n");
            bw.write("recall:"+pre_rec[1]+"\n");
        }catch (IOException e){
            e.printStackTrace();
        }


    }

    public static double[] Precision(HashSet<String> result , HashSet<String> stand){
        double[] pre = new double[2];
        String pa;
        int count = 0 ;
        Iterator it = result.iterator();
        while (it.hasNext()){        //先统计出结果中正确的pattern的个数
            pa = (String) it.next();
            if (stand.contains(pa)){
                count++;
            }
        }
        //计算正确率和召回率
        pre[0] = count * 1.0 / result.size();
        pre[1] = count * 1.0 / stand.size();

        return pre;
    }
}
