package Snowball;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Hedy on 2017/4/2.
 * pattern是以被抽取的次数排序，在该算法中只考虑pattern是否出现在多个cluster中
 */
public class Mutual_Exclusion_Bootstrapping {
    private static final int k = 3;
    //private static final int top_k = 5;   //topK个tuple
    private static final int top_p = 5;  //topK个pattern

    public static void main(String[] args){
        int index = 8;
        String label = "HE";
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

        String stdfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\Cluster\\all_Cluster.txt";
        String datafile =  "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\Extract\\new_OL_data.txt";
        String seedfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\seeds\\"+label+"_seeds.txt";
        String mutualfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\mutual_result.txt";
        String deletefile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\"+tag[index]+"\\mutual_delete.txt";

        HashMap<String, Double> all_tuples = new HashMap<>();
        HashMap<String, Double> all_pattern = new HashMap<>();    //保存每一个类在迭代抽取过程中最终被保存的所有结果

        HashMap<String , Double> pa = new HashMap<>();
        HashMap<String, Double> seeds =new HashMap<>();    //保存initial tuple seeds
        HashMap<String ,Double > pa_delete = new HashMap<>();
        //HashMap<String ,Double > tu_delete = new HashMap<>();

        long startTime =  System.nanoTime();   //获取开始时间
        //从文件中将initial tuple seeds 读取出来
        OperateTextFile.ReadFileToMap(seedfile,seeds);

        //boolean flag = true;
        int count = 1 ;
        while (true){
            //根据seeds从文件中读取pattern
            pa = new HashMap<>();
            ReadPa(datafile,pa,seeds);
            //删除出现在多个class中的pattern,并删除已被抽取过的pattern
            DeleteOverlap(stdfile,label,pa,pa_delete);
            //根据被抽取的次数排序,并保存前K个pattern
            Sort(pa,pa_delete);
            if (pa.size() == 0 ){   //若没有可用的pattern产生，则结束循环
                break;
            }
            //将抽取到的pattern都保存到map中，并删除已抽取过的pattern
            Restore(pa,all_pattern);
            all_pattern.putAll(pa);
            //根据保存的pattern抽取tuple
            seeds = new HashMap<>();
            ReadTuple(datafile,pa,seeds);
            //删除已经抽取过的tuple
            Delete(seeds,all_tuples);
            all_tuples.putAll(seeds);
            if (seeds.size() == 0 ){
                break;
            }
            count++;
            if (count > 10){
                break;
            }
        }

        long endTime = System.nanoTime();
        double time = endTime - startTime;
        //将运行的时间保存在pattern的文件中
        all_pattern.put("运行时间:(ns)",time);
        //将最后结果写入文件中
        OperateTextFile.WriteMapToFile(mutualfile,all_pattern);
        OperateTextFile.WriteMapToFile(mutualfile,all_tuples);
        //将删除的pattern写入文件中
        OperateTextFile.WriteMapToFile(deletefile,pa_delete);
    }

    public static void Delete(HashMap<String ,Double> seeds,HashMap<String ,Double> all){
        String tu;
        double count;
        Map.Entry entry ;
        Iterator it = seeds.entrySet().iterator();
        while (it.hasNext()){
            entry = (Map.Entry) it.next();
            tu = (String) entry.getKey();
            if (all.containsKey(tu)){
                count = (double) entry.getValue();
                count += all.get(tu);
                all.put(tu,count);
                it.remove();
            }
        }
    }

    public static void  ReadTuple(String filename , HashMap<String ,Double> pa, HashMap<String , Double> tuple){

        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("can't find the file!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String tu ;
            String pattern;
            String org,loc;
            double count;   //记录tuple被抽取的次数
            while ((line = br.readLine()) != null) {
                pattern = line.split("\t")[1];
                if (pa.containsKey(pattern)){    //判断该pattern是否需要抽取
                    loc = line.split("\t")[0].contains("</L>") ? line.split("\t")[0]:line.split("\t")[2];
                    org = line.split("\t")[2].contains("</O>") ? line.split("\t")[2] : line.split("\t")[0];
                    tu = org +"\t" + loc;
                    if(tuple.containsKey(tu)) {   //判断该tuple是否已经被抽取过了，若抽取过，则直接修改抽取的次数即可
                        count = tuple.get(tu);
                        count += Integer.parseInt(line.split("\t")[3]);
                        tuple.put(tu,count);
                    }
                    else {
                        count = Integer.parseInt(line.split("\t")[3]);
                        tuple.put(tu,count);
                    }
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void Restore(HashMap<String , Double> new_pa , HashMap<String , Double> all){

        Map.Entry entry ;
        String pa ;
        double count;
        Iterator it = new_pa.entrySet().iterator();
        while (it.hasNext()){
            entry = (Map.Entry) it.next();
            pa = (String) entry.getKey();
            if (all.containsKey(pa)){
                count = (double) entry.getValue();
                count += all.get(pa);
                all.put(pa,count);
                it.remove();
            }
        }
    }

    public static void Sort(HashMap<String ,Double> map,HashMap<String ,Double> delete){

        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, Double> sorted_map = new TreeMap<>(bvc);
        sorted_map.putAll(map);
        //只保存前topK个pattern
        Iterator it = sorted_map.entrySet().iterator();
        int count = 0 ;
        String pa;
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            count++;
            if (count > top_p){   // topK之后的pattern都删除
                pa = (String) entry.getKey();
                delete.put(pa,(double)entry.getValue());
                map.remove(pa);
            }
        }
    }

    public static void DeleteOverlap(String filename , String tag, HashMap<String ,Double> map , HashMap<String , Double> delete){

        String flag = new String();
        double count;
        String line,pa;
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("can't find the file!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null){
                if (line.contains(":") ){
                    flag = line.split(":")[0];
                    continue;
                }
                if (line.equals("")){
                    continue;
                }
                pa = line.split("\t")[0];
                if (!flag.equals(tag) && map.containsKey(pa) ){   //如果在其他cluster中找到该pattern，则删除
                    count = map.get(pa);
                    map.remove(pa);
                    delete.put(pa,count);
                }
            }

            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void ReadPa(String filename , HashMap<String , Double> map , HashMap<String , Double> seed){

        try{
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("can't find the file!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String tuple;
            String loc,org;
            String pa  , in_pa ;
            double count ;   //记录pattern被抽取的次数
            while ((line = br.readLine()) != null){
                loc = line.split("\t")[0].contains("</L>") ? line.split("\t")[0] : line.split("\t")[2] ;
                org = line.split("\t")[2].contains("</O>") ? line.split("\t")[2] : line.split("\t")[0];
                tuple = org +"\t"+ loc;       //将每句中的tuple抽取出来，判断是否是初始tuples

                if(seed.containsKey(tuple)){    //若该tuple在初map中，则保存当前的pattern
                    pa = line.split("\t")[1];
                    if( !map.containsKey(pa)){
                        map.put(pa,1.0);
                    }
                    //否则需要修改该pattern被抽取的次数
                    count = map.get(pa);
                    count += Integer.parseInt(line.split("\t")[3]);
                    map.put(pa,count);
                }

            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
