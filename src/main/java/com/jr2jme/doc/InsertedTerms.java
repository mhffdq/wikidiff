package com.jr2jme.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

import java.util.Map;

/**
 * Created by Hirotaka on 2014/04/09.
 */
public class InsertedTerms {
    String title;
    String editor;
    Map<String,Integer> terms;
    int version;
    public InsertedTerms(){

    }
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
    public InsertedTerms(String title,String editor,Map<String,Integer> terms,int version){
        this.title=title;
        this.editor=editor;
        this.terms=terms;
        this.version=version;
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

class Insert{
    String deletededitor;
    Map<String,Integer> words;
    public Insert(String deletededitor,Map<String,Integer> words){
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

