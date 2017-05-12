package Snowball;

import weka.gui.experiment.GeneratorPropertyIteratorPanel;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Hedy on 2017/5/9.
 */
public class PRD_Clean {

    public static double threshold = 0.000005;

    public static void main(String[] args){
        int index = 1 ;
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

        //读取数据
        String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\PRD_F_score.txt";
        String standfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\result\\correct.txt";
        String resultfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\" +tag[index] + "\\PRD_final_pattern.txt";

        HashSet<String> pa_set = new HashSet<>();
        HashSet<String> stand = new HashSet<>();
        HashSet<String> all_pa = new HashSet<>();
        HashSet<String> delete = new HashSet<>();
        HashSet<String> error = new HashSet<>();

        //将所有的pattern从文件中读取出来
        OperateTextFile.ReadFileToSet(filename,all_pa,-1);
        //从文件中将最后清洗后的pattern读取出来
        OperateTextFile.ReadFileToSet(filename,pa_set,threshold);
        //从文件中将正确的pattern读取出来
        OperateTextFile.ReadFiletopaSet(standfile,tag[index].split("_")[1]+":",stand);
        //将最后清洗之后的pattern写入文件中
        OperateTextFile.WriteSettoFile(resultfile,pa_set);
        //计算清洗之后的准确率和召回率
        double[] pre_rec = RWR_Clean.Precision(pa_set,stand);
        System.out.println("清洗之后的准确率:"+pre_rec[0]+"\t召回率:"+pre_rec[1]);

        //将删除的pattern找出来
        Iterator it = all_pa.iterator();
        String pa;
        while (it.hasNext()){
            pa = (String) it.next();
            if ( !pa_set.contains(pa)){
                delete.add(pa);
            }
            if (!stand.contains(pa)){
                error.add(pa);
            }
        }

        //计算被删除的pattern中错误的pattern的概率和所有错误的pattern被删除的概率
        pre_rec = RWR_Clean.Precision(delete, error);
        System.out.println("被删除的pattern的准确率:"+pre_rec[0]+"\t召回率:"+pre_rec[1]);

    }
}
