package com.jr2jme.wikidiff;

import com.jr2jme.st.Wikitext;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Token;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by JR2JME on 2014/03/24.
 */
public class wikidiffcore {
    public static void main(String[] arg){
        Mongo mongo=null;
        try {
            mongo = new Mongo("dragons.db.ss.is.nagoya-u.ac.jp",27017);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert mongo != null;
        DB db=mongo.getDB("wikipediaDB_kondou");
        DBCollection dbCollection=db.getCollection("text_test2");
        JacksonDBCollection<Wikitext,String> coll = JacksonDBCollection.wrap(dbCollection, Wikitext.class, String.class);
        DBCollection dbCollection2=db.getCollection("edit_test2");
        JacksonDBCollection<WikiEdit,String> coll2 = JacksonDBCollection.wrap(dbCollection2, WikiEdit.class,String.class);

        List<String> prev_text=new ArrayList();
        List<Term_Editor> predata=new ArrayList<Term_Editor>();
        ExecutorService exec = Executors.newFixedThreadPool(10);
        Levenshtein3 d = new Levenshtein3();
        int version=0;

        int offset=0;
        DBCursor<Wikitext> cursor = coll.find(DBQuery.is("title", "亀梨和也").greaterThan("version",offset)).lessThanEquals("version",offset+100).sort(DBSort.asc("version"));

        long start = System.currentTimeMillis();
        while(cursor.hasNext()) {
            //List<WikiEdit> wikieditlist = new ArrayList<WikiEdit>(50);
            for (Wikitext wikitext : cursor) {

                //System.out.println(version);
                StringTagger tagger = SenFactory.getStringTagger(null);
                List<Token> tokens = new ArrayList<Token>();
                try {
                    tagger.analyze(wikitext.getText(), tokens);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> current_text = new ArrayList<String>();
                for (Token token : tokens) {
                    //System.out.println(token.getSurface());
                    current_text.add(token.getSurface());
                }


                List<String[]> diff = d.diff(prev_text, current_text);
                List<Term_Editor> data = new ArrayList<Term_Editor>();
                int i = 0;
                Map<String, List<String>> deleteterms = new HashMap<String, List<String>>();
                new ArrayList<String>();
                for (String[] st : diff) {
                    if (st[1].equals("i")) {
                        Term_Editor tes = new Term_Editor(st[0], wikitext.getName());
                        data.add(tes);
                    } else if (st[1].equals("d")) {
                        
                        if (deleteterms.containsKey(predata.get(i))) {
                            deleteterms.get(predata.get(i).getName()).add(predata.get(i).getTerm());
                        } else {
                            List<String> terms = new ArrayList<String>();
                            terms.add(predata.get(i).getTerm());
                            deleteterms.put(predata.get(i).getName(), terms);
                        }
                        i++;
                    } else if (st[1].equals("r")) {
                        Term_Editor tes = new Term_Editor(st[0], predata.get(i).getName());
                        data.add(tes);
                        i++;
                    }

                }
                //wikieditlist.add(new WikiEdit(data,wikitext.getTitle(),version));
                //coll2.insert(new WikiEdit(data,wikitext.getTitle(),version));
                exec.submit(new Task(coll2, data, wikitext.getTitle(), version));
                predata = data;
                prev_text = current_text;
                version++;
            }
            offset+=100;
            cursor = coll.find(DBQuery.is("title", "亀梨和也").greaterThan("version",offset)).lessThanEquals("version",offset+100).sort(DBSort.asc("version"));
        }
        exec.shutdown();
        try {
            exec.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //coll2.insert(wikieditlist);
        System.out.println(System.currentTimeMillis() - start);
        cursor.close();
        mongo.close();


    }
}

class Task implements Runnable {
    JacksonDBCollection<WikiEdit,String> coll;
    List<Term_Editor> data;
    String name;
    int version;
    public Task(JacksonDBCollection<WikiEdit,String> coll,List<Term_Editor> data,String name,int version) {
        this.coll=coll;
        this.data=data;
        this.name=name;
        this.version=version;
    }
    @Override
    public void run(){
        //long start=System.currentTimeMillis();
        coll.insert(new WikiEdit(data, name, version));
        //System.out.println(System.currentTimeMillis() - start);
    }
    public void stopThread(){
    }
}