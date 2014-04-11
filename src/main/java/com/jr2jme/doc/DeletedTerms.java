package com.jr2jme.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongojack.ObjectId;

import java.util.List;
import java.util.Map;

/**
 * Created by Hirotaka on 2014/04/09.
 */
public class DeletedTerms {
    String title;
    String editor;
    Delete delterms;
    int version;
    //Map<String,Integer> wordcount;
    public DeletedTerms(){

    }
    public DeletedTerms(String title,String editor,Map<String,Delete> terms,int version){
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

    public Map<String,Delete> getTerms() {
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

    protected class Delete {
        String deletededitor;
        List<termcount> words;


        public Delete(String deletededitor, Map<String, Integer> words) {
            this.deletededitor = deletededitor;
            this.words = words;
        }

        public Map<String, Integer> getWords() {
            return words;
        }

        public String getDeletededitor() {
            return deletededitor;
        }

        public void addTerm(String term) {
            if (words.containsKey(term)) {
                words.put(term, words.get(term) + 1);
            } else {
                words.put(term, 1);
            }
        }

        @Override
        public boolean equals(Object o) {
            return deletededitor.equals(o);
        }

        @Override
        public int hashCode() {
            return deletededitor.hashCode();
        }
        protected class termcount {
            String ter;
            int count;
        }
    }

}


