package com.jr2jme.wikidiff;

import com.jr2jme.doc.DeletedTerms;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hirotaka on 2014/04/09.
 */
public class DeletedTerms_ex extends com.jr2jme.doc.DeletedTerms {

    Map<String,Integer> wordcount;
    Map<String,Map<String,Integer>> delterms;

    public DeletedTerms_ex(DeletedTerms superdel){
        this.title = superdel.getTitle();
        this.editor = superdel.getEditor();
        this.version = superdel.getVersion();
        wordcount=new HashMap<String, Integer>();
        delterms=new HashMap<String, Map<String, Integer>>(superdel.getTerms().size());
        for(Delete delete:superdel.getTerms()){
            Map<String,Integer> tempmap=new HashMap<String, Integer>();
            for(TermCount termcount:delete.getWords()){
                if(wordcount.containsKey(termcount.getTerm())) {
                    wordcount.put(termcount.getTerm(), wordcount.get(termcount.getTerm()) + termcount.getCount());
                }
                else{
                    wordcount.put(termcount.getTerm(),termcount.getCount());
                }
                tempmap.put(termcount.getTerm(),termcount.getCount());
            }
            delterms.put(delete.getDeletededitor(),tempmap);
        }

    }




    public boolean equalswords(Map<String,Integer> a){
        return wordcount.equals(a);
    }
}

