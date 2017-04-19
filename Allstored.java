package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/2/17.
 * Snowball对数据集进行抽取，只要被pattern抽取到的tuple都保存下来
 * 根据1-(1-a)^x计算pattern的置信度，根据noisy_all计算tuples的置信度
 */

public class Allstored {

    public static class Type{
        private double conf;     //置信度
        private int order;
        private int turn;         //被抽取的轮次
        private int count;         //被抽取的次数

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public double getConf() {
            return conf;
        }

        public int getTurn() {return turn;}

        public int getCount() {
            return count;
        }

        public void setTurn(int turn) {
            this.turn = turn;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void setConf(double conf) {
            this.conf = conf;
        }

    }

    public static double threshold = 0.85 ;
    private static final double a = 0.75;
    private static final int top_k = 4;
    private static final int max_k = 100;
    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    public static void main(String argv[]){
        String prefix = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\";
        //String prefix = "Snowball/";
        //String datapath =  prefix + "Extract\\new_OL_data.txt";
        String datapath =  prefix + "Extract\\OL_TAB.txt";
        String seedspath = prefix + "punish\\seeds\\LO_seeds.txt";
        String tuplespath = prefix +"punish\\result\\"+ "tuples.txt";
        String patternpath = prefix +"punish\\result\\"+ "pattern.txt";
        String tuplesofpattern = prefix + "punish\\result\\"+"tuplesofpattern.txt";
        String patternoftuples = prefix +"punish\\result\\"+ "patternoftuples.txt";
        String falsepattern = prefix+"punish\\result\\"+ "false_pattern.txt";
        String stdfile = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\result\\correct.txt";

        String tag = "LO";
        HashMap<String , Type> seeds = new HashMap<>();
        HashMap<String , Type> pattern = new HashMap<>();
        HashMap<String , HashSet<String>> ls = new HashMap<>();    //有标签的列表
        HashMap<String , Type> new_seeds = new HashMap<>();     //保存到当前为止抽取到的所有的有效tuples
        HashMap<String,Type> new_pa = new HashMap<>();

        HashSet<String> false_tu = new HashSet<>();
        HashMap<String,Type> false_pa = new HashMap<>();

        int turn = 1  ;   // 记录抽取的轮次
        //int count = 0 ;   // 记录一共抽取到的tuples的个数
        int num = 2;

        //首先读取initial tuple seeds
        ReadSeeds(seedspath , seeds);
        new_seeds.putAll(seeds);
        //将seeds写入文件中
        WritetoFile(tuplespath,seeds,turn);
        //根据initial seed tuples 从数据集中抽取pattern
        ReadFilebyTuples(datapath , seeds , ls , pattern ,turn ) ;
        //计算pattern的置信度
        System.out.print("计算pattern的置信度:");
        Caculate(ls,new_seeds,pattern,0);
        //只取置信度排名前一半的pattern
        Top(pattern,ls);
        new_pa.putAll(pattern);
        //将抽取到的pattern写入文件中
        WritetoFile(patternpath,pattern,turn);
        //将抽取到的pattern列表写入文件中
        WritetoFile1(patternoftuples , ls);
        turn ++;
        ls.clear();
        seeds = new HashMap<>();
        //根据pattern从数据集中抽取所有的tuples
        ReadFileAccordingPattern(datapath,pattern,turn,ls,seeds);
        //删除抽取次数小于阈值的元素
        //Delete(ls,seeds,num);
        //删除已经被抽取过的所有tuples
        JudgeExist(tuplespath,ls,seeds);
        //计算tuples的置信度,并将大于阈值的tuples保存下来
        System.out.print("计算tuple置信度:");
        Caculate(ls,new_pa,seeds,0);
        new_seeds.putAll(seeds);
        //处理之后的tuples写入文件
        WritetoFile(tuplespath,seeds,turn);
        //将抽取到的tuple列表写入文件中
        WritetoFile1(tuplesofpattern,ls);
        //计算每一轮的正确率
        CaculateAccuracy(stdfile,pattern,false_pa,seeds,false_tu,tag);
        //num = 3;
        while(turn <= 7){
            ls.clear();
            pattern = new HashMap<>();
            //从数据集中抽取pattern
            ReadFilebyTuples(datapath,seeds,ls,pattern ,turn);
            //判断是否已经被抽取过
            JudgeExist(patternpath,ls,pattern);
            //删除抽取次数小于阈值的元素
            Delete(ls,pattern,num);
            //计算pattern的正确率
            System.out.print("计算pattern的置信度:");
            Caculate(ls,new_seeds,pattern,0);
            if (pattern.size() ==0){
                break;
            }
            //只取置信度排名前一半的pattern
            Top(pattern,ls);
            new_pa.putAll(pattern);
            WritetoFile(patternpath,pattern,turn);
            WritetoFile1(patternoftuples,ls);
            turn++;
            //根据pattern抽取tuples
            ls.clear();
            seeds = new HashMap<>();
            ReadFileAccordingPattern(datapath,pattern,turn,ls,seeds);
            //删除与tuplespath文件中相同的所有项
            JudgeExist(tuplespath,ls,seeds);
            //删除抽取次数小于阈值的元素
            //Delete(ls,seeds,num);
            //计算tuples的正确率,并将大于阈值的tuples保存下来
            System.out.print("计算tuple的置信度:");
            Caculate(ls,new_pa,seeds,0);
            if (seeds.size() == 0){
                break;
            }
            new_seeds.putAll(seeds);
            //处理之后的tuples写入文件
            WritetoFile(tuplespath,seeds,turn);
            WritetoFile1(tuplesofpattern,ls);

            //计算每一轮的正确率
            CaculateAccuracy(stdfile,new_pa,false_pa,new_seeds,false_tu,tag);
        }

        OperateTextFile.WriteMaptoFile(falsepattern,false_pa);
        System.out.println("抽取到第"+turn+"轮结束!");
        System.out.println("一共抽取到的pattern数为:"+new_pa.size());
        System.out.println("一共抽取到的tuples数为:"+new_seeds.size());
    }


    public static void Top(HashMap<String ,Type> pattern ,HashMap<String,HashSet<String>> ls){
        System.out.print("Top:只保存前一半的pattern:");
        //先将pattern和对应的置信度提取出来，放在新的map中
        HashMap<String ,Double> new_p = new HashMap<>();
        Iterator it = pattern.entrySet().iterator();
        String key ;
        Type type ;
        double conf;
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            type = (Type) entry.getValue();
            key =(String) entry.getKey();
            conf = type.getConf();
            new_p.put(key,conf);
        }

        //对新的pattern根据置信度进行排序
        ValueComparator bvc = new ValueComparator(new_p);
        TreeMap<String, Double> sorted_map = new TreeMap<>(bvc);
        sorted_map.putAll(new_p);
        //提取前一半的元素
        new_p = new HashMap<>();
        int top =  sorted_map.size()/2;
        top = top > top_k ? top : top_k;
        top = top < max_k ? top : max_k;
        //System.out.print("top = " +top+"\t");
        int count = 0 ;
        it = sorted_map.entrySet().iterator();
        //System.out.print("the size of sorted_map is:"+sorted_map.size()+"\t");
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            if (count > top){
                break;
            }
            //System.out.print("count= "+count+"\t");
            new_p.put((String) entry.getKey(),(Double) entry.getValue());
            count++;
        }
        //System.out.print("the size of new_p is:"+new_p.size()+"\t");
        it = pattern.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry =(Map.Entry) it.next();
            key = (String)entry.getKey();
            if (!new_p.containsKey(key)){
                it.remove();
            }
        }
        it = ls.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            key = (String) entry.getKey();
            if(!new_p.containsKey(key)){
                it.remove();
            }
        }
        System.out.println("the size of pattern is :"+pattern.size());

    }

    public static void CaculateAccuracy(String filename, HashMap<String , Type>pa,HashMap<String,Type> false_pa,HashMap<String ,Type >tu , HashSet<String> false_tu, String tag){

        //先从文件中将正确的pattern提取出来
        HashSet<String> standard = new HashSet<>();
        OperateTextFile.ReadFiletopaSet(filename,tag,standard);
        //开始统计pattern的情况
        int pos = 0 , neg = 0;
        String key;
        Iterator it = pa.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            key =(String) entry.getKey();
            if (standard.contains(key)){
                pos++;
            }
            else {
                neg++;
                false_pa.put(key,(Type) entry.getValue());
            }
        }
        double accuracy = Double.parseDouble(df.format(pos*1.0 / (pos+neg)));
        System.out.println("该轮的pattern正确率为:"+ accuracy+"\t"+pos+"\t"+neg);

        /*//将正确的tuple从文件中提取出来
        standard.clear();
        OperateTextFile.ReadFiletotupleSet(filename,tag,standard);
        pos = 0 ;
        neg = 0 ;
        it = tu.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            key = (String) entry.getKey();
            if (standard.contains(key)){
                pos++;
            }
            else {
                neg++;
                false_tu.add(key);
            }
        }
        accuracy = Double.parseDouble(df.format(pos*1.0/(pos+neg)));
        System.out.println("该轮的tuple正确率为:"+accuracy+"\t"+pos+"\t"+neg);*/

    }

    public static void WriteMaptoFile(String writefile, HashMap<String , Integer>  map , int count){

        try {
            File file  = new File(writefile);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true) ));
            bw.write("第"+count+"轮抽取的pattern有:"+"\n");
            String pa ;
            int  order ;
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry)it.next();
                pa = (String) entry.getKey();
                order =(Integer) entry.getValue();
                bw.write(pa+"\t"+order+"\n");
            }
            bw.write("\n");
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //删除抽取次数小于count的元素
     public static void Delete(HashMap<String , HashSet<String>> ls, HashMap<String , Type> map , int count){

        System.out.print("删除被抽取次数小于阈值的元素:");
        int num ;
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry =(Map.Entry) it.next();
            Type type = (Type) entry.getValue();
            num = type.getCount();
            if (num < count){
                ls.remove((String) entry.getKey());
                it.remove();
            }
        }
        System.out.println("ending!"+"\t"+ls.size());
     }

    //计算每轮抽取下来的正确率
    public static void Caculate( HashMap<String, HashSet<String>> pa_tu , HashMap<String,Type> seeds, HashMap<String , Type> map, int tab ){
        double conf ;
        double x ;
        Type type ;

        Iterator it = pa_tu.entrySet().iterator();
        Iterator set_it ;
        HashSet<String> tuple ;
        String pa , temp;
        if (tab == 0) {         //若当前计算的是pattern的相似度
            while (it.hasNext()){
                Map.Entry entry =(Map.Entry) it.next();
                pa = (String) entry.getKey();
                tuple = (HashSet<String>) entry.getValue();    //将该pattern抽取到的tuple提取出来，进行置信度的计算
                x = 0 ;
                set_it = tuple.iterator();
                while (set_it.hasNext()){
                    temp  = (String) set_it.next();
                    if (seeds.containsKey(temp)){
                        type = seeds.get(temp);
                        x += type.getConf();
                    }
                }

                conf =  1 - Math.pow(1-a,x);
                conf = Double.parseDouble(df.format(conf));

                //System.out.println(pa+"\t"+conf);
                if(conf < threshold) {    //若置信度小于阈值，则直接删除
                    map.remove(pa);
                    it.remove();
                    continue;
                }
                map.get(pa).setConf(conf);  //重置pattern的置信度
            }
        }
        else {   //当前计算的是tuple的相似度
            while (it.hasNext()){
                Map.Entry entry =(Map.Entry) it.next();
                pa = (String) entry.getKey();
                tuple = (HashSet<String>) entry.getValue();    //将该pattern抽取到的tuple提取出来，进行置信度的计算
                x = 1 ;
                set_it = tuple.iterator();
                while (set_it.hasNext()){
                    temp  = (String) set_it.next();
                    if (seeds.containsKey(temp)) {
                        type = seeds.get(temp);
                        x *= ( 1-type.getConf() );
                    }
                }
                conf = Double.parseDouble(df.format(1-x));
                if(conf < threshold) {    //若置信度小于阈值，则直接删除
                    map.remove(pa);
                    it.remove();
                    continue;
                }
                map.get(pa).setConf(conf);  //重置pattern的置信度
            }
        }

        System.out.println("ending\t"+map.size());
    }

    //判断本轮抽取的元素有没有之前已经被抽取过得，若有，则删除
    public static void JudgeExist(String filename ,HashMap<String  ,HashSet<String>> map , HashMap<String , Type> ls){

        int count = 0 ;

        for (String s : ls.keySet()){
            count = s.split("\t").length == 1 ? 1: 0 ;
            break;
        }
        try{
            File file = new File(filename);
            if(file.exists()) {    //判断文件是否存在
                BufferedReader bf = new BufferedReader(new FileReader(file));
                String linetxt = new String();
                String arg ;
                while ((linetxt = bf.readLine()) != null){
                    if(linetxt.contains(":")){
                        continue;
                    }
                    if (linetxt.equals("")){
                        continue;
                    }
                    if(count == 1){    //若当前比较是pattern
                        arg = linetxt.split("\t")[0];
                    }
                    else {
                        arg = linetxt.split("\t")[0]+"\t"+linetxt.split("\t")[1];
                    }
                    if (ls.containsKey(arg)){
                        ls.remove(arg);
                        map.remove(arg);
                    }
                }
                bf.close();
            }
            else{
                System.out.println("the file doesn't  exist!");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("judge exist: "+count+"\t"+ ls.size());

    }

    public static void WritetoFile1 (String filename , HashMap<String ,HashSet<String>> ls){

        try{
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }

            BufferedWriter output  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            System.out.println("write to file1: "+ ls.size() );
            String tag ;
            HashSet<String> set;

            Iterator it = ls.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry)it.next();
                tag = (String) entry.getKey();
                set = (HashSet<String>) entry.getValue();
                //System.out.println(tag+":");
                output.write(tag+":"+set.size()+"\n");     //先输出标签
                for (String s: set){     //再输出抽取的具体的内容
                    output.write(s+"\n");
                    //System.out.println(in_ls.get(i));
                }
                output.write("\n");

            }
            output.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //将抽取的结果写入文件中
    public static void WritetoFile(String filename, HashMap<String , Type> ls , int count ){

        try{
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }

            BufferedWriter output  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            //System.out.println("WritetoFile :the size is : "+ ls.size() );

            output.write("第"+count+"轮抽取到的数据有:"+"\n");
            Iterator it = ls.entrySet().iterator();
            String s ;
            Type t;
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                s = (String) entry.getKey();
                t = (Type) entry.getValue();
                output.write(s+"\t"+t.getConf()+"\t"+t.getCount()+"\t"+t.getOrder()+"\n");
            }
            output.write("\n");
            output.close();
            System.out.println("WritetoFile: "+ls.size());
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    //  根据pattern从数据集中读取instance tuples,count记录抽取的轮次
    public static void ReadFileAccordingPattern(String filepath, HashMap<String , Type> pattern, int turn ,HashMap<String , HashSet<String>> ls , HashMap<String , Type> tuple ){
        //pattern保存的是用于抽取的pattern，ls保存的是每个pattern抽取到的所有的tuples,seeds保存的是抽取到的所有的tuples
        HashSet<String> set ;

        try{
            File file = new File(filepath);

            if(!file.exists()){
                System.out.println("can't open the file of patterns！");
            }
            else{
                //System.out.println("read the pattern,extract tuples!");
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                BufferedReader br = new BufferedReader(read);
                String lineTxt = new String();    //每次读取一行
                String org,loc,tu;   //记录每个句子的信息
                int order,count;
                String pa ;

                while((lineTxt = br.readLine())!= null ){
                   pa = lineTxt.split("\t")[1];
                   if(pattern.containsKey(pa)){    //判断该句子的pattern是否是我们需要抽取的
                       order = pattern.get(pa).getOrder();
                       if(order == 0 ){
                           org =  lineTxt.split("\t")[2];
                           loc =  lineTxt.split("\t")[0];
                       }
                       else {
                           org = lineTxt.split("\t")[0];
                           loc = lineTxt.split("\t")[2];
                       }
                       tu = org+"\t"+loc;

                       if(!ls.containsKey(tu)){    //判断该tuple之前是否被抽取过，若没，添加新的map，否则需要找到该tuple进行添加
                           set= new HashSet<>();
                           set.add(pa);
                           ls.put(tu,set);
                       }
                       //否则需要更新对应的tuple列表
                       else{
                           set = ls.get(tu);
                           set.add(pa);
                           ls.put(tu,set);
                       }
                       if(tuple.containsKey(tu)){    //若tuple之前被抽取过，则直接修改
                           Type type = tuple.get(tu);
                           count = type.getCount();
                           count += 1; // Integer.parseInt(lineTxt.split("\t")[3]);
                           type.setCount(count);
                           tuple.put(tu,type);
                       }
                       else {    //否则添加新项
                           count = 1 ;//Integer.parseInt(lineTxt.split("\t")[3]);
                           Type type = new Type();
                           type.setCount(count);
                           type.setOrder(order);
                           type.setTurn(turn);
                           tuple.put(tu,type);
                       }
                   }

                }
                read.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("read file according pattern:"+ls.size());

    }

    //根据instance tuples抽取pattern
    public static void ReadFilebyTuples(String filepath, HashMap<String ,Type> seeds , HashMap<String , HashSet<String> > sp , HashMap<String , Type> p  ,int turn){
        //seeds保存的是用来抽取的initial tuple seeds，sp保存的是每个tuple seeds抽取到的pattern，p保存的是抽取到的所有的pattern

        try{
            File file = new File(filepath);

            if(!file.exists()){
                System.out.println("can't find the file!");
            }
            else{
                InputStreamReader read = new InputStreamReader(new FileInputStream(file));
                BufferedReader br = new BufferedReader(read);
                String lineTxt = new String() ;
                String arg1,arg2,tu1,tu2 ,tu , pa;
                HashSet<String> temp ;
                Type type ;
                int order,count ;

                while((lineTxt = br.readLine())!= null  ){
                    arg1 = lineTxt.split("\t")[0];
                    arg2 = lineTxt.split("\t")[2];
                    tu1 = arg1+"\t"+arg2;
                    tu2 = arg2+"\t"+arg1;
                    if (! (seeds.containsKey(tu1) || seeds.containsKey(tu2))){     //若不包含seed，则直接跳转到下一行
                        continue;
                    }
                    //System.out.println(lineTxt);
                    tu =  seeds.containsKey(tu1) ? tu1 : tu2 ;
                    order = seeds.containsKey(tu1) ? 1: 0;   //1表示O在L之前，否则为0
                    //若该pattern之前没有被抽取过，则直接添加
                    pa = lineTxt.split("\t")[1];
                    if(!p.containsKey(pa)){
                        temp = new HashSet<>();
                        temp.add(tu);
                        sp.put(pa,temp);
                    }
                    else {
                        //否则需要找到该pattern对应的列表，将新的tuples添加进去
                        temp = sp.get(pa);
                        temp.add(tu);
                        sp.put(pa,temp);
                    }
                    if(p.containsKey(pa)) {    //判断该pattern之前是否被抽取过，若被抽取过则直接修改相关数据，否则需要添加新项
                        type = p.get(pa);
                        count = type.getCount();
                        count += 1;   //Integer.parseInt(lineTxt.split("\t")[3]);
                        type.setCount(count);
                        p.put(pa,type);
                    }
                    else {
                        type = new Type();
                        type.setConf(0);
                        type.setTurn(turn);
                        type.setOrder(order);
                        type.setCount(1);   //Integer.parseInt(lineTxt.split("\t")[3]));
                        p.put(pa,type);
                    }

                }
                read.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Read File by tuple:\t"+p.size());

    }

    //分解数据集中的每行
    public static String[] getSen(String line){

        String[] comp = new String[4];

        comp[0] = line.split("\t")[0];         //arg1
        comp[1] = line.split("\t")[1];           //relation
        comp[2] = line.split("\t")[2];           //arg2
        comp[3] = line.split("\t")[3];           //confidence

        return comp;
    }

    //判断读取的一行字符串中是否存在该字符串
    public static int Contain(String s , List<String> ls ,int tab){   //返回1表示1一样，2表示位置变化，0表示不存在
        int flag = 0;
        if(tab == 1){    //如果是pattern
            String s1 = ls.get(0);
            String pa = getSen(s)[1];  // 抽取该句子中的pattern
            if(pa.equals(s1)){
                flag = 1;
            }
            //System.out.println(ls+"当前比较的是pattern!");
        }
        else {
            String s1 = ls.get(0);
            String s2 = ls.get(1);
            //获取句子中个部分的信息
            // List<String> infor = new ArrayList<String>();
            String sen1 = getSen(s)[0];   //arg1
            String sen2 = getSen(s)[2];    //arg2
            //System.out.println(sen1+sen2+s1+s2);
            //判断s1和s2是否是字符串中单独的子串

            if(sen1.equals(s1) && sen2.equals(s2)){
                flag = 1;
            }
            else{
                if(sen1.equals(s2) && sen2.equals(s1)){
                    flag = 2;
                }
            }
            /*
            if(SameEntity(sen1,s1) && SameEntity(sen2,s2)){    //顺序一样
                //System.out.println("两个参数都一样"+sen1+sen2+s1+s2);
                flag = 1;
            }
            else {
                if(SameEntity(sen1,s2) && SameEntity(sen2,s1)){     //位置变化
                    // System.out.println(sen1+sen2+s1+s2);
                    flag = 2 ;
                }
            }*/
        }

        //System.out.println("Contain:"+flag);
        return flag;
    }

    //从文件中读取initial seed tuples
    public static void ReadSeeds(String filename , HashMap<String , Type> seeds){

        System.out.println("ReadSeeds:read the seed tuples from :"+filename);

        String line;
        String infor[] = new String[4];
        Type type = new Type() ;

        try{
            BufferedReader br = new BufferedReader(new FileReader(filename));
            line = new String();
            while((line  =  br.readLine()) != null){
                infor[0] = line.split("\t")[0];      //arg1
                infor[1] = line.split("\t")[1];      //arg2
                infor[2] = line.split("\t")[2];      //order
                infor[3] = line.split("\t")[3];      //count
                type.setConf(1);
                type.setCount(Integer.parseInt(infor[3]));
                type.setTurn(0);
                type.setOrder(Integer.parseInt(infor[2]));
                seeds.put(infor[0]+"\t"+infor[1], type);
            }
            br.close();
            System.out.println("一共有："+seeds.size()+"个seeds!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
