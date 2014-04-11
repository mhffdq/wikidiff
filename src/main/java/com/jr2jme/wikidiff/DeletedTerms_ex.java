package com.jr2jme.wikidiff;

import com.jr2jme.doc.DeletedTerms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public DeletedTerms makeDeletedTerms(){
        List<Delete> dellist=new ArrayList<Delete>();
        for(Map.Entry<String,Map<String,Integer>> entry:delterms.entrySet()){
            List<TermCount> termlist = new ArrayList<TermCount>();
            for(Map.Entry<String,Integer> termc:entry.getValue().entrySet()){
                termlist.add(new TermCount(termc.getKey(),termc.getValue()));
            }
            dellist.add(new Delete(entry.getKey(),termlist));
        }
        return new DeletedTerms(this.title,this.editor,dellist,this.version);
    }




    public boolean equalswords(Map<String,Integer> a){
        return wordcount.equals(a);
    }
}

