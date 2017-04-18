package Snowball;

import java.io.*;
import java.util.*;

/**
 * Created by Hedy on 2017/2/26.
 * 抽取出数据集中所有的OL,PL,OP的关系，并存入文件
 */

public class Tmap {

    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("0.00000");

    //分解数据集中的每行
    public static String[] getSen(String line){

        String[] sen = new String[7];

        sen[0] = line.split("\t")[0] ;         //arg1
        sen[1] = line.split("\t")[1];           //pattern
        sen[2] = line.split("\t")[2];           //arg2
        sen[3] = line.split("\t")[3];            //arg1
        sen[4] = line.split("\t")[4];           //relation
        sen[5] = line.split("\t")[5];           //arg2
        sen[6] = line.split("\t")[6];           //confidence

        return sen;
    }

    public static boolean Con(String sen , List<String> l){
        for(int i = 0 ; i < l.size()/2 ; i++){
            if(sen.contains(l.get(i))  && sen.contains(l.get(2*i))){
                return  true;
            }
        }
        return false;
    }

    public static void DeleteSame(List<String> ls){

        for(int i = 0 ; i < ls.size()-1 ; i++){
            for(int j = i+1; j < ls.size() ; j++){
                if(ls.get(i).equals(ls.get(j))){
                    ls.remove(j);
                    j--;
                }
            }
        }
    }

    public static void main(String[] args){
        String filename = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\LABEL.txt";
        String newfile = "Snowball/relation.txt";

        List<String> orgre = new ArrayList<String>();
        List<String> perre = new ArrayList<String>();
        List<String> perorg = new ArrayList<String>();

        try{
            File file = new File(filename);
            File newf = new File(newfile);
            if(!file.exists()){
                System.out.println("the file doesn't exist!");
            }
            if(!newf.exists()){
                newf.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedWriter output  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newf,true)));

            String line = new String();
            while((line = br.readLine()) != null){
                if(line.contains("<LOCATION>") && line.contains("<ORGANIZATION>")){
                    orgre.add(getSen(line)[4]);
                }
                else
                {
                    if(line.contains("<LOCATION>") && line.contains("<PERSON>")){
                        perre.add(getSen(line)[4]);
                    }
                    else
                    {
                        if(line.contains("<ORGANIZATION>") && line.contains("<PERSON>")){
                            perorg.add(getSen(line)[4]);
                        }
                    }
                }

            }
            DeleteSame(orgre);
            DeleteSame(perorg);
            DeleteSame(perre);

            output.write("all relationships for org and location:\n");
            for(int i = 0 ; i < orgre.size() ; i++){
                output.write(orgre.get(i)+"\n");
            }
            output.write("\n");
            output.write("all relationships for person and location:\n");
            for(int i = 0 ; i < perre.size() ; i++){
                output.write(perre.get(i)+"\n");
            }
            output.write("\n");
            output.write("all relationships for person and org:\n");
            for(int i = 0 ; i < perorg.size() ; i++){
                output.write(perorg.get(i)+"\n");
            }
            br.close();
            output.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

}
