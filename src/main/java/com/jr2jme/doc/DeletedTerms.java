package com.jr2jme.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Hirotaka on 2014/04/09.
 */
public class DeletedTerms {
    String title;
    String editor;
    MAp<Delete> terms;
    int version;
    Map<String,Integer> wordcount;
    public DeletedTerms(){

    }
    public DeletedTerms(String title,String editor,Set<Delete> terms,int version){
        this.title=title;
        this.editor=editor;
        this.terms=terms;
        this.version=version;
        Map<String,Integer> wordcount=new HashMap<String, Integer>();
        for(Delete delete:this.getTerms()){
            Map<String,Integer>deletewords = delete.getWords();
            for(String word:deletewords.keySet()){
                if(wordcount.containsKey(word)){
                    wordcount.put(word,deletewords.get(word));
                }
                else{
                    wordcount.put(word,deletewords.get(word)+wordcount.get(word));
                }
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public String getEditor() {
        return editor;
    }

    public Set<Delete> getTerms() {
        return terms;
    }

    public int getVersion() {
        return version;
    }

    private String id;
    @ObjectId
    @JsonProperty("_id")
    public String getId() {
        return id;
    }
    @ObjectId
    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }

    public boolean equalswords(Map<String,Integer> a){
        return wordcount.equals(a);
    }
}


