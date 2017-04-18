package Snowball;

import java.util.HashMap;

/**
 * Created by Hedy on 2017/4/18.
 */
public class SelfAlgorithm {


    public static int tuple_count  = 194;
    public static class Features{
        private int[] p1 = new int[tuple_count];                           //性质1是抽取到的tuple的分布情况
        private int p2 ;                             //性质2是互斥的concept的个数
        private double p3;                           //性质3是根据随机游走得到的得分
        private double p4 ;                         //性质4是触发的pattern的得分的平均值
        private int sub ;                           //性质5是触发的pattern的个数
        private double standDev ;                   //性质6是子集得分的偏差
        private int  level ;                        //性质7是第一次是发现的轮次
        private int all_num;                         //在该概念下抽到的所有的pattern数
        private int maxlevel ;                       //抽取到的最大的轮次


        public int[] getP1() {
            return p1;
        }

        public int getP2() {
            return p2;
        }

        public double getP3() {
            return p3;
        }

        public double getP4() {
            return p4;
        }

        public int getSub() {
            return sub;
        }

        public double getStandDev() {
            return standDev;
        }

        public int getLevel() {
            return level;
        }

        public int getAll_num() {
            return all_num;
        }

        public int getMaxlevel() {
            return maxlevel;
        }

        public void setP1(int[] p1) {
            this.p1 = p1;
        }

        public void setP2(int p2) {
            this.p2 = p2;
        }

        public void setP3(double p3) {
            this.p3 = p3;
        }

        public void setP4(double p4) {
            this.p4 = p4;
        }

        public void setSub(int sub) {
            this.sub = sub;
        }

        public void setStandDev(double standDev) {
            this.standDev = standDev;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void setAll_num(int all_num) {
            this.all_num = all_num;
        }

        public void setMaxlevel(int maxlevel) {
            this.maxlevel = maxlevel;
        }
    }

    public static void main(String[] args ){
        String filename = "F:\\Study\\Semanticdrift\\Data\\ValidData\\Snowball\\final\\punish\\result\\0.85_LO\\pattern.txt";
        HashMap<String, Integer> pa_order= new HashMap<>();
        HashMap<String,Integer> pa_seq = new HashMap<>();
        //先将所有的pattern从文件中读取出来
        OperateTextFile.ReadPatterntoMap(filename,pa_order,pa_seq);

    }



}
