package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/3/12.
 * 读取pattern最后被分类的情况
 */
public class Classify_Pattern {
    public static int number = 4 ;   //同时被number个tuples抽取到的pattern算一个类别
    private  static double threshold = 0.5;

    public static void Delete(List<List<String>> ls , List<List<String>> no_use){
        List<String> newls ;

        for(int i = 0 ; i < ls.size() ; i++){
            if(ls.get(i).size() < number+1){
                newls = new ArrayList<String>();
                newls = ls.get(i);
                no_use.add(newls);
                ls.remove(i);
                i--;
                continue;
            }

        }

    }

    //删除两个字符串中重复的pattern，形成一个新的pattern构成的字符串
    public static String DeletePattern(String p1 , String p2){

        String newpa = new String();
        HashSet set = new HashSet();

        for(int i = 0 ; i < p1.split("\t").length ; i++) {
            set.add(p1.split("\t")[i]);
        }
        for(int j = 0 ; j < p2.split("\t").length ; j++ ) {
            set.add(p2.split("\t")[j]);
        }
        Iterator it = set.iterator();
        while(it.hasNext()){
            newpa = it.next().toString() + "\t" + newpa;
        }

        return newpa;

    }

    public static void WritetoTxt(String filename, List<List<String>> ls){

        List<String> tu ;
        //System.out.println("the size is :"+ls.size());

        try {
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            for(int i = 0 ; i < ls.size() ; i++){
                tu = new ArrayList<String>();
                tu = ls.get(i);
                output.write(tu.get(0)+"\n");
                for(int j = 1 ; j < tu.size() ; j++){
                    output.write(tu.get(j)+"\n");
                }
                output.write("\n");
            }
            output.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //将所有的pattern及其抽取到的tuples从文件中读取出来
    public static void ReadPattern(String filename , List<List<String>> ls ){
        String line ;
        int flag = 1 ;   //记录下一行是否是一个新的pattern
        List<String> newls = null ;
        HashSet<String> set = new HashSet();

        int count = 0;

        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("the file doesn't exist!");
            }
            String pa = null ;
            BufferedReader br = new BufferedReader(new FileReader(file));

            while((line = br.readLine()) != null){
                if(line.equals("")){
                    count++;

                    flag = 1;     //下一行开始抽取新的pattern
                    //newls = new ArrayList<String>();
                    for(String str : set){
                        newls.add(str);
                    }
                    ls.add(newls);
                    continue;
                }

                if(flag == 1) {   //如果当前是新的pattern，则将该pattern存入List中
                    set = new HashSet();
                    pa = new String();
                    int f_index = line.indexOf("[");
                    int b_index = line.indexOf("]");
                    //System.out.println(line+"\t"+f_index+"\t"+b_index);
                    pa = line.substring(f_index+1 , b_index);
                    newls = new ArrayList<String>();   //将pattern添入列表
                    newls.add(pa);
                    flag = 0 ;

                    continue;
                }
                //否则将tuples加入列表中
                int f_index = line.indexOf("[");
                int b_index = line.indexOf("]");
                int m_index = line.indexOf(",");
                String arg1 =line.substring(f_index+1 , m_index);
                String arg2 = line.substring(m_index+2 , b_index);
                String org = arg1.contains("</O>") ? arg1 : arg2 ;
                String loc = arg2.contains("</L>") ? arg2 : arg1 ;
                set.add(org+"\t"+loc);
                //newls.add(org+"\t"+loc);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void Turn(List<List<String>> ls , List<List<String>> no_classify , List<List<String>> newls){

        boolean flag ;   //标记当前有木有pattern被分类
        boolean last_flag = false;
        double ratio ;
        List<String> ex_ls = new ArrayList<>();
        List<String> in_ls = new ArrayList<>();
        List<String> same_tuple = new ArrayList<String>();

        HashSet<String> set = new HashSet<>();

        for(int i = 0 ; i < ls.size()-1 ; i++){
            flag = false ;
            ex_ls = ls.get(i);
            for(int j = i+1 ; j < ls.size() ; j++){
                in_ls  = ls.get(j);
                same_tuple = new ArrayList<String>();
                String new_pattern_list = new String();
                new_pattern_list = DeletePattern(ex_ls.get(0),in_ls.get(0));
                //same_tuple.add(new_pattern_list);
                for(int  k = 1 ; k < ex_ls.size() ; k++){    //列表第一项是pattern，不需要比较
                    //String org1 = ex_ls.get(k).split("\t")[0];
                    //String loc1 = ex_ls.get(k).split("\t")[1];
                    for(int m = 1 ; m < in_ls.size() ; m++){
                        if(ex_ls.get(k).equals(in_ls.get(m))){
                            same_tuple.add(ex_ls.get(k));
                            continue;
                        }
                    }
                }
                //计算相同tuples的比例
                ratio = same_tuple.size() * 1.0 / (ex_ls.size() + in_ls.size() - same_tuple.size());

                if(ratio >= threshold)   //若比例大于所设置的阈值，则将这两个pattern聚类
                {
                    System.out.println("ratio = "+ratio +"\t"+same_tuple.size()+"\t"+(ex_ls.size() + in_ls.size() - same_tuple.size()));
                    //String newpa = same_tuple.get(0);
                    same_tuple.clear();
                    same_tuple.add(new_pattern_list);   //现将新产生的pattern放入列表中
                    set.clear();
                    for(int k = 1 ; k < ex_ls.size() ; k++){    //一次将tuples不重复的放入
                        if(set.add(ex_ls.get(k))){
                            same_tuple.add(ex_ls.get(k));
                        }
                    }
                    for(int m = 1 ; m < in_ls.size() ; m++){
                        if(set.add(in_ls.get(m))){
                            same_tuple.add(in_ls.get(m));
                        }
                    }
                    newls.add(same_tuple);
                }
                /*
                if(same_tuple.size() >= number+1){    //相同的tuples的个数大于阈值，则加入新的列表中
                    newls.add(same_tuple);
                    flag = true;
                }*/
                if(( j == ls.size()-1) && (ratio >= threshold)){
                    last_flag = true;
                }
            }
            if(flag == false){
                no_classify.add(ex_ls);
            }
        }
        if(last_flag == false){    //若最后一项没有被合并过，则将最后一项写入没有被分类的列表中
            no_classify.add(ls.get(ls.size()-1));
        }
        //System.out.println("此轮没有被分类的pattern有："+no_classify.size());
        //System.out.println("此轮新的有分类有："+newls.size());
    }

//判断这两个list中存储的tuples是否完全一样
    public static String Judge(List<String> l1, List<String> l2  ){  //}, String pa){

        String newpa = new String();
        boolean flag ;
        String s1 = new String();
        String s2 = new String();

        for(int i = 1 ;  i< l1.size() ;i++){
            flag = false;
            s1 = l1.get(i);
            for(int j = 1 ; j < l2.size() ; j++){
                s2 = l2.get(j);
                if(s1.equals(s2)){
                    flag = true;
                    break;
                }
            }
            if(flag == false){
                return null; //false;
            }
        }

        HashSet<String> set  = new HashSet();
        //汇总pattern
        for(int i = 0 ; i < l1.get(0).split("\t").length ; i++){
            set.add(l1.get(0).split("\t")[i]);
        }
        for(int i = 0 ; i < l2.get(0).split("\t").length ; i++){
            set.add(l2.get(0).split("\t")[i]);
        }

        Iterator it = set.iterator();
        while(it.hasNext()){
            newpa =  it.next()+"\t"+newpa;
        }

        //pa = newpa ;
        //System.out.println(pa);
        return newpa;
    }

    public static void Merge(List<List<String >> ls){
        System.out.println("进行合并!");

        List<List<String>> new_ls = new ArrayList<>();
        //List<String> newpat = new ArrayList<String>();
        String newpa = new String();
        HashSet set = new HashSet();
        List<String> ex_ls = new ArrayList<>();
        List<String> in_ls = new ArrayList<>();

        for(int i = 0 ; i < ls.size()-1 ; i++){
            ex_ls = ls.get(i);
            for(int j = i+1 ; j < ls.size() ; j++){
                newpa = new String();
                in_ls = ls.get(j);
                if(ex_ls.size() == in_ls.size()){
                    newpa = Judge(ex_ls,in_ls);
                    if(newpa != null){
                        ex_ls.set(0,newpa);
                        ls.remove(j);
                        j--;
                        continue;
                    }
                }
                else continue;
            }
        }
        System.out.println("合并结束!");
    }

    public static void main(String[] args){
        String datafile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\OL_Tuples.txt";
        String noclassifyfile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\noClassify_OL_Pa.txt";
        String excelfile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\Excel0.xls";
        String excel_pre = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\Excel";
        String excel_back = ".xls";
        String filename = null ;

       /* String datafile = "Snowball/OL_Tuples.txt";
        String noclassifyfile = "Snowball/noClassify_OL_Pa.txt";
       // String excelfile = "Snowball/Classification2.xls";
        String finalTxt = "Snowball/Classification2.txt";*/

        List<List<String>> myPattern = new ArrayList<List<String>>();
        List<List<String>> newPattern = new ArrayList<List<String>>();
        List<List<String>>  noUse = new ArrayList<List<String>>();
        List<List<String>> noclassify = new ArrayList<List<String>>();

        //先从文件中读取数据
        ReadPattern(datafile,myPattern);
        //现将一部分不符合要求的pattern抽取出来

        //System.out.println("没有被分类的pattern的个数为："+noUse.size());
        //System.out.println("需要被分类的有："+myPattern.size());

        int count = myPattern.size() ;
        System.out.println("初始时一共有："+count+"个pattern!");
        int i = 0 ;
       // int allcount = count ;
        while(count > 0){
            i++;
            Turn(myPattern , noclassify , newPattern);
            System.out.println("没有被分类的pattern有:"+noclassify.size());
            //根据结果再次对pattern进行处理
            Merge(newPattern);
            System.out.println("一共被分为："+ newPattern.size());
            //filename = excel_pre+i+excel_back;
            WritetoTxt(noclassifyfile,noclassify);
            myPattern.clear();
            for(int j = 0 ; j < newPattern.size() ; j++) {
                List<String> ls = new ArrayList<String>(newPattern.get(j));
                myPattern.add(ls);
            }
            newPattern.clear();
            noclassify.clear();
            count = myPattern.size();
            System.out.println(count);
        }
    }


}
