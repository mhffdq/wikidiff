package com.jr2jme.wikidiff;

import java.util.List;

/**
 * Created by Hirotaka on 2014/03/26.
 */
public class Levenshtein {
    static public List<String> diff(List<String> a,List<String> b){
        int n = a.size();
        int m = b.size();
        int max= n+m;
        int[] arraya=new int[max*2+1];
        for(int d=0;d<=max;d++){
            for(int k=-d;k<=d;k+=2){
                if(k<=-m||n<k){
                    k++;
                    continue;
                }
                int x =Math.max(-d,m);
            }
        }
        return a;
    }
}
