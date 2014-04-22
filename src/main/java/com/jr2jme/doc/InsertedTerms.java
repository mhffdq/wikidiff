package com.jr2jme.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hirotaka on 2014/04/09.
 */
public class InsertedTerms {
    String title;
    String editor;
    Map<String,Integer> terms=new HashMap<String, Integer>();
    int version;
    public InsertedTerms(String title,String editor,int version){
        this.title=title;
        this.editor=editor;
        this.version=version;
    }
    public void add(String str){
        if(terms.containsKey(str)){
            terms.put(str,terms.get(str)+1);
        }
        else{
            terms.put(str,1);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getEditor() {
        return editor;
    }

    public Map<String,Integer> getTerms() {
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
        return this.getTerms().equals(a);
    }
}


