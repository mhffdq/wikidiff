package com.jr2jme.wikidiff;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

import java.util.List;

/**
 * Created by JR2JME on 2014/04/06.
 */
public class DeletedTerms {
    String title;
    String deleteeditor;
    String deletedsditor;
    int version;
    List<String> terms;

    public DeletedTerms(){

    }

    public DeletedTerms(String title,String deleteeditor,String deletedsditor,int version,List<String> terms){
        this.title=title;
        this.deleteeditor=deleteeditor;
        this.deletedsditor=deletedsditor;
        this.version=version;
        this.terms=terms;
    }

    public int getVersion() {
        return version;
    }

    public List<String> getTerms() {
        return terms;
    }

    public String getDeletedsditor() {
        return deletedsditor;
    }

    public String getTitle() {
        return title;
    }

    public String getDeleteeditor() {
        return deleteeditor;
    }

    public void setDeletedsditor(String deletedsditor) {
        this.deletedsditor = deletedsditor;
    }

    public void setDeleteeditor(String deleteeditor) {
        this.deleteeditor = deleteeditor;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVersion(int version) {
        this.version = version;
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
