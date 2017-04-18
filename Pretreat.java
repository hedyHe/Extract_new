package Snowball;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Hedy on 2017/3/24.
 * 先对数据集进行预处理，将所有的命名方式统一，如corp.cor.
 */
public class Pretreat {
    public static void main(String[] args){

        String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\Extract\\OL_TAB.txt";
        String writename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\Extract\\new_OL_data.txt";
        HashSet<String> set = new HashSet<>();
        HashMap<String , String> map = new HashMap<>();
        map.put("u.s.</L>","us</L>");
        map.put("america</L>","us</L>");
        //map.put("america ","us ");
        map.put("united states</L>", "us</L>");
        //map.put("united states ","us ");
        map.put("america</O>","us</O>");
        map.put("company</O>","co</O>");
        //map.put("company ","co ");
        map.put("corporation</O>","co</O>");
        //map.put("corporation ","co ");
        map.put("corp</O>","co</O>");
        //map.put("corp ","co ");
        map.put("inc</O>","co</O>");
        map.put("ltd</O>","co</O>");
        map.put("u. s.</L>","us</L>");
        //map.put("university</O>","univ</O>");
        //map.put("university ","univ ");
        map.put("college</O>","univ</O>");
        map.put("washington dc</L>","washington</L>");
        map.put("deutsche bank ag</O>","deutsche bank</O>");
        map.put("south san francisco</L>","san francisco</L>");
        map.put("dallas county</L>","dallas</L>");
        map.put("united nations</O>","un</O>");
        map.put("european union</O>","eu</O>");
        map.put("national football league</O>","nfl</O>");
        map.put("e.u.</O>","eu</O>");


        try {
            File file = new File(filename);
            File wf = new File(writename);
            if(!file.exists()){
                System.out.println("the file doesn't exist!");
            }
            if(!wf.exists()){
                wf.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(wf, true)));
            String line , first , sec , pa = new String();
            String gets , count , conf;
            while((line = br.readLine()) != null){
                first = line.split("\t")[0] ;
                sec = line.split("\t")[2] ;
                pa = line.split("\t")[1] ;
                count = line.split("\t")[3];
                conf = line.split("\t")[4];
                Iterator it = map.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry entry  = (Map.Entry) it.next();
                    gets = (String) entry.getKey();
                    if(first.contains(gets)){
                        first = first.replaceAll(gets,(String)entry.getValue());
                        continue;
                    }
                    if(sec.contains(gets)){
                        sec = sec.replaceAll(gets,(String) entry.getValue());
                        continue;
                    }
                }
                wr.write(first +"\t"+pa+"\t"+sec+"\t"+count+"\t"+conf+"\n");
            }
            wr.close();
            br.close();
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("ss");
        }


    }
}
