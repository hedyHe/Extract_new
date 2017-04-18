package Snowball;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * Created by Hedy on 2017/2/15.
 */
public class Extract {

    //  根据pattern从数据集中读取instance tuples,count记录抽取的轮次
    public static void ReadFileAccordingPattern(String filepath, HashMap<String , Allstored.Type> pattern, int turn , HashMap<String , HashSet<String>> ls , HashMap<String , Allstored.Type> tuple ){
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
                        if(!ls.containsKey(pa)){    //判断该pattern是否抽取过tuple，若没，添加新的map，否则需要找到该pattern进行添加
                            set= new HashSet<>();
                            set.add(tu);
                            ls.put(pa,set);
                        }
                        //否则需要更新对应的tuple列表
                        else{
                            set = ls.get(pa);
                            set.add(tu);
                            ls.put(pa,set);
                        }
                        if(tuple.containsKey(tu)){    //若tuple之前被抽取过，则直接修改
                            Allstored.Type type = tuple.get(tu);
                            count = type.getCount();
                            count += 1; // Integer.parseInt(lineTxt.split("\t")[3]);
                            type.setCount(count);
                            tuple.put(tu,type);
                        }
                        else {    //否则添加新项
                            count = 1 ;//Integer.parseInt(lineTxt.split("\t")[3]);
                            Allstored.Type type = new Allstored.Type();
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
    public static void ReadFilebyTuples(String filepath, HashMap<String , Allstored.Type> seeds , HashMap<String , HashSet<String> > sp , HashMap<String , Allstored.Type> p  , int turn){
        //seeds保存的是用来抽取的initial tuple seeds，sp保存的是每个tuple抽取到的pattern，p保存的是抽取到的所有的pattern

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
                Allstored.Type type ;
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
                    if(!sp.containsKey(tu)){
                        temp = new HashSet<>();
                        temp.add(pa);
                        sp.put(tu,temp);
                    }
                    else {
                        //否则需要找到该pattern对应的列表，将新的tuples添加进去
                        temp = sp.get(tu);
                        temp.add(pa);
                        sp.put(tu,temp);
                    }
                    if(p.containsKey(pa)) {    //判断该pattern之前是否被抽取过，若被抽取过则直接修改相关数据，否则需要添加新项
                        type = p.get(pa);
                        count = type.getCount();
                        count += 1;   //Integer.parseInt(lineTxt.split("\t")[3]);
                        type.setCount(count);
                        p.put(pa,type);
                    }
                    else {
                        type = new Allstored.Type();
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

}
