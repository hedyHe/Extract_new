package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/4/11.
 */
public class OperateTextFile {

    public static void WriteMatricToFile(String filename , double[][] matrix , int lentgh){
        try {
            File file = new File(filename);
            if (!file.exists()){
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            for (int i = 0 ;  i < lentgh ; i++ ){
                for (int  j = 0 ; j  < lentgh ; j++){
                    bw.write(matrix[j][i]+"\t");
                }
                bw.write("\n");
            }
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void ReadFileToMap(String filename , HashMap<String , HashSet<String>> map){
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("can't find the file!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line,key,value;
            while ((line = br.readLine()) != null){
                if (line.contains(":")){
                    value = line.split(":")[0];
                }
                else {
                    if (line.equals("")){   //若当前聚类抽取结束

                    }

                }

            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void ReadFile(String filename, HashSet<String> set){
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("can't find the file!");
            }
            String line ;
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine())!= null){
                if (line.contains(":") || line.equals("")){
                    continue;
                }
                set.add(line.split("\t")[0]);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void ReadFiletoMap(String filename , HashMap<String,HashSet<String>> map){
        try{
            File file = new File(filename);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line ,tuple=null ;
            HashSet<String> set ;
            while ((line = br.readLine())!= null){
                if (line.contains(":")){
                    tuple = line.split(":")[0];
                    continue;
                }
                if (line.equals("")){
                    continue;
                }
                if(map.containsKey(line)){
                    set = map.get(line);
                    set.add(tuple);
                    map.put(line,set);
                }
                else {
                    set = new HashSet<>();
                    set.add(tuple);
                    map.put(line,set);
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void ReadPatterntoMap(String filename,HashMap<String,Integer> map , HashMap<String ,Integer> pa){
        int count = 0 ;
        int sequence = 0 ;
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("can't find the file!");
            }
            String line ;
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine())!= null){
                if (line.contains(":")){   // || line.equals("")){
                    count++;
                    //System.out.println();
                    continue;
                }
                if (line.equals("")){
                    continue;
                }
                //System.out.print(line.split("\t")[0]+"\t");
                map.put(line.split("\t")[0],count);    //记录每个pattern所在的级别
                pa.put(line.split("\t")[0],sequence++);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void WriteMaptoFile(String filename,HashMap<String, Allstored.Type>map){
        Iterator it = map.entrySet().iterator();
        String key ;
        Allstored.Type type;
        try {
            File file = new File(filename);
            if (!file.exists()){
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                key = (String) entry.getKey();
                type = (Allstored.Type) entry.getValue();
                bw.write(key+"\t"+type.getConf()+"\t"+type.getCount()+"\t"+type.getTurn()+"\n");
            }
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void ReadFiletotupleSet(String filename, String tag , HashSet<String> tuple){
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine())!= null){
                if (line.contains(tag)){
                    while (!(line = br.readLine()).equals("")){
                        if (line.contains("<")){
                            tuple.add(line);
                        }
                    }
                    break;
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void ReadFiletopaSet(String filename , String tag,HashSet<String> pattern){
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null){
                if (line.contains(tag)){    //一直找到我们需要提取的那个聚类
                    while (!(line = br.readLine()).contains("<")){
                        pattern.add(line);
                    }
                    break;
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void WriteTwoSettoTXT(String filename ,String tag, HashSet<String> pattern, HashSet<String>tuple){

        try {
            File file = new File(filename);
            if (!file.exists()){
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            bw.write(tag+":\n");     //输出当前的标签
            Iterator it = pattern.iterator();
            while (it.hasNext()){
                bw.write(it.next() +"\n");
            }
            it = tuple.iterator();
            while (it.hasNext()){
                bw.write(it.next()+"\n");
            }
            bw.write("\n");
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void WritetoTxt(String filename, TreeMap<String , Double> map){

        String tu = new String();
        //System.out.println("the size is :"+ls.size());

        try {
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext()){
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                output.write(key+"\t"+value+"\n");
            }
            output.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void ReadFiletoSetList(String filename, List<HashSet<String>> tu_ls , List<HashSet<String>> pa_ls , List<String> label){
        HashSet<String> pa = new HashSet<>();
        HashSet<String> tu = new HashSet<>();
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = new String();

            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.contains(":")) {
                    label.add(0,line.split(":")[1]);
                    continue;
                }
                if (line.contains("<")) {    //判断抽取到的是否是tuple，若是则添加到tuple的列表中
                    tu.add(line);
                    continue;
                }
                if (line.equals("")) {   //一个聚类读取完毕
                    if(pa.size() == 1){    //若该聚类中只有一个pattern，则删除
                        pa = new HashSet<>();
                        tu = new HashSet<>();
                        continue;
                    }
                    count++;    //聚类的个数加1,并将tuple和pattern分别加入列表中
                    tu_ls.add(0, tu);
                    pa_ls.add(0, pa);
                    //System.out.println("pa的大小："+pa.size());
                    pa = new HashSet<>();
                    tu = new HashSet<>();
                    continue;
                }
                pa.add(line);   //否则将pattern添加入对应的列表中
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ReadFileByPa(String filename, HashSet<String> pa, HashSet<String> tu){
        try {
            File file = new File(filename);
            if (!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line, org, loc,arg1,arg2,pattern;
            //抽取这些pattern对应的tuple，并按OL存储
            while ((line = br.readLine()) != null){
                pattern =  line.split("\t")[1] ;   //提取当前行的pattern，判断其是否是我们要找的pattern
                if(pa.contains(pattern)){
                    arg1 = line.split("\t")[0];
                    arg2 = line.split("\t")[2];
                    org = arg1.contains("</O>") ? arg1 :arg2;
                    loc = arg2.contains("</L>") ? arg2 :arg1;
                    tu.add(org+"\t"+loc);
                }
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
