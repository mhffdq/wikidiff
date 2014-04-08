package com.jr2jme.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

import java.util.List;

/**
 * Created by Hirotaka on 2014/04/08.
 */
public class WhoWrite {
    List<String> editors;
    String title;
    int revid;
    public WhoWrite(){
    }
    public WhoWrite(List<String> editors,String title,int id){
        this.editors=editors;
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

    public void setEditors(List<String> text_editor) {
        this.editors = text_editor;
    }
    public List<String> getEditors(){
        return editors;
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