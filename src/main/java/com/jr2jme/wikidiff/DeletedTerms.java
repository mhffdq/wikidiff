package com.jr2jme.wikidiff;

import com.jr2jme.doc.Delete;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hirotaka on 2014/04/09.
 */
public class DeletedTerms {
    String title;
    String editor;
    Map<String,Delete> terms;
    int version;
    Map<String,Integer> wordcount;
    public DeletedTerms(){

    }
    public DeletedTerms(String title,String editor,Map<String,Delete> terms,int version){
        this.title=title;
        this.editor=editor;
        this.terms=terms;
        this.version=version;
        wordcount=new HashMap<String, Integer>();
        for(Map.Entry<String,Delete> delete:this.terms.entrySet()){
            for(String word:delete.getValue().getWords().keySet()){
                if(wordcount.containsKey(word)){
                    wordcount.put(word,delete.getValue().getWords().get(word));
                }
                else{
                    wordcount.put(word,delete.getValue().getWords().get(word)+wordcount.get(word));
                }
            }
        }
    }

    public void add(String term,String editor){
        terms.containsKey(editor){
            terms.get(editor).add(term);
        }
    }


    public String getTitle() {
        return title;
    }

    public String getEditor() {
        return editor;
    }

    public Map<String,Delete> getTerms() {
        return terms;
    }

    public int getVersion() {
        return version;
    }



    public boolean equalswords(Map<String,Integer> a){
        return wordcount.equals(a);
    }
}

class Delete{
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

