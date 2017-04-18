package Snowball;

import org.omg.PortableInterceptor.INACTIVE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.DoubleBinaryOperator;

/**
 * Created by Hedy on 2017/4/2.
 * terms以被抽取的次数排序
 */
public class Mutual_Exclusion_Bootstrapping {
    private static final int k = 3;
    private static final int top = 5;

    public static void main(String[] args){
        String datafile =  "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\Extract\\OL_TAB.txt";
        String seedfile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\Extract\\seeds.txt";
        //String driftfile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\driftpattern.txt";

        HashSet<String> all_tuples[] = new HashSet[k];
        HashSet<String> all_pattern[] = new HashSet[k];    //保存每一个类在迭代抽取过程中最终被保存的所有结果
        HashMap<String , Integer>[] term = new HashMap[k];    // 保存一轮中每个terms被抽取的次数
        //HashMap<String , Integer>[] new_term = new HashMap[k];
        HashSet<String> pa_delete = new HashSet<>();
        HashSet<String> tu_delete = new HashSet<>();
        HashSet<String>[] seeds =new HashSet[k];    //保存initial tuple seeds
        //HashSet<String> drift = new HashSet<>();    //保存会产生漂移的pattern

        List<Map.Entry<String , Integer>>[] sort_ls = new ArrayList[k];

        //给每个数组分配空间
        for (int i = 0 ; i < k ; i++){
            all_pattern[i] = new HashSet<>();
            all_tuples[i] = new HashSet<>();
            term[i] = new HashMap<>();
            seeds[i] = new HashSet<>();
        }
        //从文件中将initial tuple seeds 读取出啦
        ReadSeeds(seedfile,seeds);

        boolean flag = true;
        int count = 1 ;
        while (flag){
            //根据seeds从文件中读取terms
            ReadPa(datafile,term,seeds);
            //删除出现在多个class中的pattern,并删除已被抽取过的item
            flag = DeleteOverlap(term,all_pattern,pa_delete);
            if(!flag){
                break;
            }
            //根据被抽取的次数排序
            sort_ls = Sort(term);
            //根据排序保存前top k个结果
            //seeds = new HashSet[k];
            System.out.println("第"+count+"轮保存的pattern结果有：");
            Restore(sort_ls,all_pattern,seeds);
            //根据保存的pattern抽取tuple
            ReadTuple(datafile,term,seeds);
            flag = DeleteOverlap(term,all_tuples,tu_delete);
            if(!flag){
                break;
            }
            //根据被抽取的次数排序
            sort_ls = Sort(term);
            System.out.println("第"+count+"轮保存的tuple结果有：");
            Restore(sort_ls,all_tuples,seeds);
            count++;
            if(count>= 50){
                break;
            }
        }
    }

    public static void   ReadTuple(String filename , HashMap<String ,Integer>[] term, HashSet<String>[] seeds){
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = new String();
            String tuple = new String();
            String pa;
            String org,loc;
            int count;   //记录tuple被抽取的次数
            while ((line = br.readLine()) != null) {
                pa = line.split("\t")[1];
                for (int i = 0  ; i < k ; i++){
                    if (seeds[i].contains(pa)){    //找到该pattern所在的分类，保存其抽取到的tuple
                        loc = line.split("\t")[0].contains("</L>") ? line.split("\t")[0]:line.split("\t")[2];
                        org = line.split("\t")[2].contains("</O>") ? line.split("\t")[2] : line.split("\t")[0];
                        tuple = org +"\t" + loc;
                        if(term[i].containsKey(tuple)) {   //判断该tuple是否已经被抽取过了，若抽取过，则直接修改抽取的次数即可
                            count = term[i].get(tuple);
                            count += Integer.parseInt(line.split("\t")[3]);
                            term[i].put(tuple,count);
                        }
                        else {
                            count = Integer.parseInt(line.split("\t")[3]);
                            term[i].put(tuple,count);
                        }
                        break;
                    }
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void Restore(List<Map.Entry<String ,Integer>>[] ls, HashSet<String>[] all, HashSet<String>[] set){

        Map.Entry entry ;
        String item ;
        for (int i = 0 ;  i < k ; i++){
            set[i] = new HashSet<>();
            System.out.println("第"+i+"个类：");
            for (int j = 0 ; j < top ; j++){
                entry = ls[i].get(j);
                item = (String) entry.getKey();
                all[i].add(item);
                set[i].add(item);
                System.out.print(item+"\t");
            }
            System.out.println();
        }
    }

    public static List<Map.Entry<String , Integer>>[] Sort(HashMap<String ,Integer>[] map){

        List<Map.Entry<String ,Integer>> ls[] = new ArrayList[k];
        for (int i = 0 ; i < k ;i++){
            //将map.entrySet()转换成list
            List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String, Integer>>(map[i].entrySet());
            //通过比较器来实现排序
            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            for (Map.Entry<String ,Integer> mapping : list){
                System.out.println(mapping.getKey()+":"+mapping.getValue());
            }
            ls[i] = list;
        }
        return ls;
    }

    public static boolean DeleteOverlap(HashMap<String ,Integer>[] map ,HashSet<String>[] all , HashSet<String> de){

        Map.Entry entry;
        String item;
        boolean flag ;
        for (int i = 0 ; i < k - 1 ; i ++){
            Iterator it = map[i].entrySet().iterator();
            while (it.hasNext()){
                entry = (Map.Entry)it.next();
                item = (String) entry.getKey();
                flag = false;
                for (int j = i +1 ; j < k ; j++){    //判断其他类别中是否存在相同的item，若若存在，则将flag改为true
                    if(all[j].contains(item)){
                        flag = true;
                    }
                    if(map[j].containsKey(item)){
                        flag = true;
                        map[j].remove(item);
                    }
                }
                if(flag){   //若当前item出现在多个class中，则删除
                    de.add(item);
                    it.remove();
                }
            }
        }

        //判断是否每个class都抽取到对象了
        flag = true;
        for (int i = 0 ; i < k ; i ++){
            if (map[i].size() ==0){
                flag = false;
                break;
            }
        }
        return flag;
    }

    public static void ReadSeeds(String filename, HashSet<String>[] set){

        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = new String();
            int count = 0;
            while ((line = br.readLine()) != null){
                if(line.length() == 0){     //某一类的seeds读取完成后，就进入下一个类别的读取
                    count++;
                    continue;
                }
                set[count].add(line);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void ReadPa(String filename , HashMap<String , Integer>[] map , HashSet<String>[] set){

        try{
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = new String();
            String tuples = new String();
            String loc,org;
            String pa = new String() , in_pa = new String();
            int count ;   //记录pattern被抽取的次数
            while ((line = br.readLine()) != null){
                loc = line.split("\t")[0].contains("</L>") ? line.split("\t")[0]:line.split("\t")[2] ;
                org = line.split("\t")[2].contains("</O>") ? line.split("\t")[2] : line.split("\t")[0];
                tuples = org +","+ loc;       //将每句中的tuple抽取出来，判断是否是初始tuples
                for(int i = 0 ; i < k ; i++) {    //找到该tuple所在的分类
                    if(set[i].contains(tuples)){    //若该tuple是初始tuple，则保存pattern
                        pa = line.split("\t")[1];
                        if(!map[i].containsKey(pa)){   //若不包含该pattern，则直接添加
                            map[i].put(pa,Integer.parseInt(line.split("\t")[3]));
                            continue;
                        }
                        //否则需要修改该pattern被抽取的次数
                        count = map[i].get(pa);
                        count += Integer.parseInt(line.split("\t")[3]);
                        map[i].put(pa,count);
                        break;
                    }
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
