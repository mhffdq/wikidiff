package com.jr2jme.wikidiff;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hirotaka on 2014/04/09.
 */
public class DeletedTerms_ex extends com.jr2jme.doc.DeletedTerms {

    Map<String,Integer> wordcount;

    public DeletedTerms_ex(String title,String editor,int version){
        super();
        wordcount=new HashMap<String, Integer>();

    }

    public void add(String term,String editor){
        if(this.terms.containsKey(editor)){
            if(this.terms.get(editor).getWords().containsKey(term)) {
                this.terms.get(editor).getWords().get(term);
            }
        }
    }


    public boolean equalswords(Map<String,Integer> a){
        return wordcount.equals(a);
    }
}

