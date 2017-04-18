package Snowball;

import java.util.HashSet;

/**
 * Created by Hedy on 2017/3/9.
 * 删除聚类中错误的tuple
 */
public class DeleteFalseTuple {

    public  static void  main(String[] args){
        String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\Cluster\\new_Cluster_0.06.xls";
        //String datafile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\Extract\\OL_TAB.txt";
        String datafile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test1\\OL_TAB.txt";
        String writefile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\result\\correct.txt";
        HashSet set ;
        HashSet<String> pattern = new HashSet<>();
        HashSet<String> tuple = new HashSet<>();
        String tag = "AL";
        //从excel文件中读取该聚类中的所有数据，同时删除不正确的pattern
        Excel.ReadExcel(filename,pattern,tuple,tag);
        System.out.println("the size of pattern is:"+pattern.size());
        //删除不正确的tuples
        set = Delete(datafile,tuple,pattern);
        System.out.println("the size of tuple is:"+set.size());
        //将最终结果写入文本文件中
        OperateTextFile.WriteTwoSettoTXT(writefile,tag , pattern,set);
    }

    public static HashSet<String> Delete(String filename , HashSet<String> tu , HashSet<String> pa){
        //先找出这些pattern能够抽取到的所有的tuples,然后用新的tuple集合和原来的求交集，结果就是正确的集合
        HashSet<String> new_tu = new HashSet<>();
        HashSet<String> correct = new HashSet<>();
        OperateTextFile.ReadFileByPa(filename,pa,new_tu);
        System.out.println("the size of new_tu is :"+new_tu.size());
       /* for (String s : new_tu){
            System.out.println(s.split("\t").length);
            break;
        }*/
        //求交集
        for (String s : tu){

            if (new_tu.contains(s)){
                correct.add(s);
            }
        }
        return correct;
    }

}
