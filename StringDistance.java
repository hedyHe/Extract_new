package Snowball;

//计算相似度公式：1-它们的距离/两个字符串长度的最大值
/**
 * Created by Hedy on 2017/3/11.
 * 计算两个字符串的编辑距离
 */
public class StringDistance {

    public static void main(String[] args){
        //System.out.println(getStringSimilar("microsoft","microsoft corporation"));
        //System.out.println(getStringSimilar("michigan tech university","michigan technological university"));
        //System.out.println(getStringSimilar("hack","hankcs"));
        //System.out.println(getStringSimilar("abcdef","defsaca"));
        System.out.println(getStringSimilar("milwaukee public school","milwaukee school of engineering"));
    }

    public static int getStringSimilar(String s1, String s2) {

        int m = s1.length();
        int n = s2.length();

        int [][] d = new int[m+1][n+1];
        for(int j = 0 ; j <= n ; j++){
            d[0][j] = j;
        }
        for(int i = 0 ; i <= m ; i++){
            d[i][0] = i;
        }
        for(int i = 1 ; i<= m ; ++i){
            char ci = s1.charAt(i-1);
            for(int j = 1; j<= n ;j++){
                char cj = s2.charAt(j-1);
                if(ci == cj){
                    d[i][j] = d[i-1][j-1];
                }
                else{
                    d[i][j] = Math.min(d[i-1][j-1]+1 , Math.min(d[i][j-1]+1,d[i-1][j]+1));
                }
            }
        }

        return d[m][n];
    }

}
