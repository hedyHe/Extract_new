package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/3/22.
 * 对之前求的tuples的相似度进行排序，并输出到新文件中
 */

class ValueComparator implements Comparator<String>{
    Map<String ,Double> base;
    public ValueComparator(Map<String ,Double> base){
        this.base = base;
    }
    public int compare(String a ,String b){
        if(base.get(a) >= base.get(b)){
            return -1;
        }else {
            return 1;
        }
    }

}
public class Sort_Tu_Sim {

    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    public static void main(String[] args){
        //String filename =  "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_Tu_similarity.txt";
        //String writefile =  "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\test\\sub_Sort_Tu_sim.txt";
        String filename = "Snowball/Tu_similarity.txt";
        String writefile = "Snowball/Sort_Tu_sim.txt";

        HashMap<String , Double> map = new HashMap<>();
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, Double> sorted_map = new TreeMap<>(bvc);

        ReadSimilar(filename ,map);
        sorted_map.putAll(map);
        OperateTextFile.WritetoTxt(writefile,sorted_map);
        System.out.println("一共有"+sorted_map.size()+"组！");
    }

    public static void ReadSimilar(String filename  , HashMap<String,Double> map){

        try {
            File file = new File(filename);
            if(!file.exists()){
                System.out.println("the file doesn't exist!");
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String new_line , line ;
            double sim ;
            int count = 0 ;
            while((new_line = br.readLine()) != null){
                sim = Double.parseDouble(df.format(Double.parseDouble(new_line.split("\t")[4])));
                line = "";
                for(int i = 0 ; i < 4 ; i++){
                    line +=new_line.split("\t")[i]+"\t";
                }
                map.put(line,sim);
                count++;
            }
            System.out.println("一共有:"+count);
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
