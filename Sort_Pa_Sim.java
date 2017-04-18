package Snowball;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Hedy on 2017/3/22.
 */
public class Sort_Pa_Sim {
    public static void main(String[] args){
        //String filename = "Snowball/first_pa.txt";
        //String writefile = "Snowball/Sort_pa_sim.txt";

        String filename = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\first_pa.txt";
        String writefile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\Sort_first_pa_sim.txt";

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
                sim = Double.parseDouble(new_line.split("\t")[2]);
                line = "";
                for(int i = 0 ; i < 2 ; i++){
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
