package Snowball;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hedy on 2017/3/10.
 *根据pattern对tuples进行合并
 */
public class ReadByPa {
    public static String[] getSen(String line){
        String sen[] = new String[3];
        sen[0] = line.split("\t")[0];   //arg1
        sen[1] = line.split("\t")[1];
        sen[2] = line.split("\t")[2];   //arg2
        return sen;
    }

    public static void main(String[] args) {      //List<List<List<String>>> main(String[] args){
        String pafile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\new_PO.txt" ;
        String datafile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\TAB.txt";
        String OLdata = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\PO_data.txt";
        String tuplefile = "D:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\pattern\\PO_Tuples.txt";

        List<List<List<String>>> OL_tuple = new ArrayList<List<List<String>>>();
        List<List<String>> pat_list ;
        List<String> temp ;
        String tab1 = "</O>";
        String tab2 = "</P>";

        String line ;
        //读pattern
        try{
            File paf = new File(pafile);
            if(!paf.exists()){
                System.out.println("the pattern file doesn't exist!");
            }
            BufferedReader br = new BufferedReader(new FileReader(paf));

            line = br.readLine();
            line = new String();
            int count = 0 ;

            while((line = br.readLine()) != null){ //将每一个pattern添入列表中
                temp = new ArrayList<String>();
                pat_list = new ArrayList<List<String>>();
                temp.add(line.trim());
                pat_list.add(temp);
                OL_tuple.add(pat_list);
                line = new String();
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("the size of pattern is :"+OL_tuple.size());

        //根据pattern读tuples
        try {
            File dataf = new File(datafile);
            File newdata = new File(OLdata);

            if(!dataf.exists()){
                System.out.println("the data file doesn't exist");
            }
            if(!newdata.exists()){
                newdata.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(dataf));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newdata,true)));

            while((line = br.readLine()) != null){
                if(line.contains(tab1) && line.contains(tab2)){   //将符合条件的句子写入新文件中
                    output.write(line+"\n");
                    temp = new ArrayList<String>();
                    for(int i = 0 ; i < OL_tuple.size() ; i ++){
                        temp = OL_tuple.get(i).get(0);
                        if(line.contains("\t"+temp.get(0)+"\t")){    //将tuple加入对应的pattern后面
                            temp = new ArrayList<String>();
                            String arg1 = getSen(line)[0];
                            String arg2 = getSen(line)[2];
                            temp.add(arg1.substring(0,arg1.length()));
                            temp.add(arg2.substring(0,arg2.length()));
                            OL_tuple.get(i).add(temp);
                            //System.out.println(OL_tuple.get(i));
                            break;
                        }
                    }
                }
                line = new String();
            }

            br.close();
            output.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        try {
            File file = new File(tuplefile);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
            for (int i = 0 ; i < OL_tuple.size() ; i++){
                //System.out.println(OL_tuple.get(i).get(0)+":\t"+(OL_tuple.get(i).size()-1));
                output.write(OL_tuple.get(i).get(0)+"\t"+(OL_tuple.get(i).size()-1)+"\n");
                for(int j = 1 ; j < OL_tuple.get(i).size() ; j ++){
                    output.write(OL_tuple.get(i).get(j)+"\n");
                }
                output.write("\n ");
            }
            output.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        //return OL_tuple;
    }

}
