package com.jr2jme.wikidiff;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

import java.util.List;

/**
 * Created by Hirotaka on 2014/03/27.
 */
public class WikiEdit {
    List<Term_Editor> text_editor;
    String title;
    int revid;
    public WikiEdit(){
    }
    public WikiEdit(List<Term_Editor> l,String title,int id){
        text_editor=l;
        this.title=title;
        revid=id;
    }

    public void setRevid(int revid) {
        this.revid = revid;
    }

    public int getRevid() {
        return revid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setText_editor(List<Term_Editor> text_editor) {
        this.text_editor = text_editor;
    }
    public List<Term_Editor> getText_editor(){
        return text_editor;
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
}
class Term_Editor {
    String term=null;
    String name = null;
    public Term_Editor(){

    }
    public Term_Editor(String term,String name){
        this.term=term;
        this.name=name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getName() {
        return name;
    }

    public String getTerm() {
        return term;
    }
}
