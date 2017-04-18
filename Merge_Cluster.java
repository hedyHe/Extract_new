package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/4/8.
 */

public class Merge_Cluster {
    public static void main(String[] args){
        String filename = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\new_0.06.txt";
        String writefile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\0.06.txt";
        HashMap<String,List<HashSet<String>>> map = new HashMap<>();

        try{
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("the file failed!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line ;
            HashMap<HashSet<String>,HashSet<String>> in_map;    //保存每个聚类中的pattern和tuples
            List<HashSet<String>> old ;
            HashSet<String>pa = new HashSet<>(),tu = new HashSet<>();
            String flag = null, flag1 = null,flag2 = null;
            int count=0;
            while ((line = br.readLine()) != null){
                if(line.contains("F:")){
                    //System.out.print(line+"\t");
                    flag = line.split(":")[1];
                    System.out.println(flag);
                    count = flag.contains("&") ? 2 : 1;     //标志当前cluster表示几个聚类
                    flag1 = count == 2 ? flag.split("&")[0] : flag;
                    flag2 = count == 2 ? flag.split("&")[1] : flag;

                    //in_map = new HashMap<>();
                    pa = new HashSet<>();
                    tu = new HashSet<>();
                    continue;
                }
                if (line.length() == 0 ){   //当前的cluster读取完成
                    if(flag1.equals("-1")){    //删除没有用的聚类
                        continue;
                    }
                    if(map.containsKey(flag1)){   //若当前的cluster已经存在了，则需要更新值
                        old = map.get(flag1);
                        Merge_Map(old,pa,tu);
                    }
                    else {    //否则，直接添加
                        List<HashSet<String>> newl = new ArrayList<>();
                        newl.add(pa);
                        newl.add(tu);
                        map.put(flag1,newl);
                    }
                    if(count == 2){   //若该cluster包含两种关系，则需要将该cluster添加到第二种关系中
                        if (map.containsKey(flag2)){
                            if(map.containsKey(flag2)){   //若当前的cluster已经存在了，则需要更新值
                                old = map.get(flag2);
                                Merge_Map(old,pa,tu);
                            }
                            else {    //否则，直接添加
                                List<HashSet<String>> newl = new ArrayList<>();
                                newl.add(pa);
                                newl.add(tu);
                                map.put(flag2,newl);
                            }
                        }
                    }
                    continue;
                }

                if(line.contains("<")){    //若当前读取的是tuples，则保存到tuple集合中
                    tu.add(line);
                    continue;
                }
                //所有情况都排除后，当前行读取的只可能是pattern
                pa.add(line);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        try {
            File file = new File(writefile);
            if (!file.exists()){
                file.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            Iterator it = map.entrySet().iterator();
            HashSet<String> pa, tu;
            List<HashSet<String>> ls ;
            String flag ;
            Iterator set_it ;
            while (it.hasNext()){
                Map.Entry entry = (Map.Entry) it.next();
                flag = (String) entry.getKey();
                ls = (List<HashSet<String>>) entry.getValue();
                pa = ls.get(0);
                tu = ls.get(1);
                bw.write("F:"+flag+"\n");
                set_it = pa.iterator();
                while (set_it.hasNext()){
                    bw.write((String) set_it.next()+"\n");
                }
                set_it = tu.iterator();
                while (set_it.hasNext()){
                    bw.write((String ) set_it.next()+"\n");
                }
                bw.write("\n");
            }
            bw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void Merge_Map(List<HashSet<String>> old ,HashSet<String> pa , HashSet<String> tu){

        HashSet<String> old_pa,old_tu;
       old_pa = old.get(0);
       old_tu = old.get(1);

        Iterator set_it = pa.iterator();    //将pattern合并
        while (set_it.hasNext()){
            old_pa.add((String) set_it.next());
        }
        set_it = tu.iterator();
        while (set_it.hasNext()){
            old_tu.add((String) set_it.next());
        }
    }

}
