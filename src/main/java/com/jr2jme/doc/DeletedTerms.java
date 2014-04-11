package com.jr2jme.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

import java.util.List;

/**
 * Created by Hirotaka on 2014/04/09.
 */
public class DeletedTerms {
    protected String title;
    protected String editor;
    List<Delete> delterms;
    protected int version;

    //Map<String,Integer> wordcount;
    public DeletedTerms() {

    }

    public DeletedTerms(String title, String editor, List<Delete> terms, int version) {
        this.title = title;
        this.editor = editor;
        this.delterms = terms;
        this.version = version;
    }


    public String getTitle() {
        return title;
    }

    public String getEditor() {
        return editor;
    }

    public List<Delete> getTerms() {
        return delterms;
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

    protected class Delete {
        String deletededitor;
        List<TermCount> words;


        public Delete(String deletededitor, List<TermCount> words) {
            this.deletededitor = deletededitor;
            this.words = words;
        }

        public List<TermCount> getWords() {
            return words;
        }

        public String getDeletededitor() {
            return deletededitor;
        }

    }

    protected class TermCount {
        String term;
        int count;

        public void setCount(int count) {
            this.count = count;
        }

        public void setTer(String term) {
            this.term = term;
        }

        public int getCount() {
            return count;
        }

        public String getTerm() {
            return term;
        }
    }

}



