package com.jr2jme.doc;

import java.util.Map;

/**
 * Created by Hirotaka on 2014/04/09.
 */
public class Delete{
    String deletededitor;
    Map<String,Integer> words;
    public Delete(String deletededitor,Map<String,Integer> words){
        this.deletededitor=deletededitor;
        this.words=words;
    }

    public Map<String, Integer> getWords() {
        return words;
    }

    public String getDeletededitor() {
        return deletededitor;
    }
    public void addTerm(String term){
        if(words.containsKey(term)){
            words.put(term,words.get(term)+1);
        }
        else{
            words.put(term,1);
        }
    }
    @Override
    public boolean equals(Object o){
        return deletededitor.equals(o);
    }
    @Override
    public int hashCode(){
        return deletededitor.hashCode();
    }
}