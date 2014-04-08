package com.jr2jme.wikidiff;

import com.jr2jme.doc.Delta;
import com.jr2jme.doc.WikiTerms;
import com.jr2jme.st.Wikitext;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
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
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by JR2JME on 2014/03/24.
 */
public class wikidiffcore {
    public static JacksonDBCollection<WikiTerms,String> coll3;
    public static JacksonDBCollection<Delta,String> coll4;
    public static void main(String[] arg){
        MongoClient mongo=null;
        try {
            mongo = new MongoClient("dragons.db.ss.is.nagoya-u.ac.jp",27017);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert mongo != null;
        DB db=mongo.getDB("wikipediaDB_kondou");
        DBCollection dbCollection=db.getCollection("text_test2");
        JacksonDBCollection<Wikitext,String> coll = JacksonDBCollection.wrap(dbCollection, Wikitext.class, String.class);
        //DBCollection dbCollection2=db.getCollection("edit_test2");
        DBCollection dbCollection3=db.getCollection("terms");
        DBCollection dbCollection4=db.getCollection("delta");
        //JacksonDBCollection<WikiEdit,String> coll2 = JacksonDBCollection.wrap(dbCollection2, WikiEdit.class,String.class);
        coll3 = JacksonDBCollection.wrap(dbCollection3, WikiTerms.class,String.class);
        coll4 = JacksonDBCollection.wrap(dbCollection4, Delta.class,String.class);


        ExecutorService exec = Executors.newFixedThreadPool(10);
        int offset=0;
        DBCursor<Wikitext> cursor = coll.find(DBQuery.is("title", "亀梨和也").greaterThan("version",offset)).lessThanEquals("version",offset+100).sort(DBSort.asc("version"));
        int version=1;

        while(cursor.hasNext()) {
            List<List<String>> editlist=new ArrayList<List<String>>();
            //List<WikiEdit> wikieditlist = new ArrayList<WikiEdit>(50);
            List<Kaiseki> tasks = new ArrayList<Kaiseki>();
            List<String> namelist=new ArrayList<String>();
            for (Wikitext wikitext : cursor) {
                tasks.add(new Kaiseki(wikitext));
                namelist.add(wikitext.getName());
            }

            List<Future<List<String>>> futurelist = null;
            long start=System.currentTimeMillis();
            try {
                futurelist=exec.invokeAll(tasks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis() - start);
            List<String> prev_text=new ArrayList();
            int i=0;
            List<Task2> tasks2 = new ArrayList<Task2>();



            for(Future<List<String>> future:futurelist){
                try {
                    tasks2.add(new Task2(future.get(), prev_text, "亀梨和也", version, namelist.get(i)));
                    i++;
                    version++;
                    prev_text=future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            List<Future<List<String>>> futurelist2 = null;
            try {
                exec.invokeAll(tasks2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(Future<List<String>> future:futurelist){
                
            }
            offset+=100;
            cursor = coll.find(DBQuery.is("title", "亀梨和也").greaterThan("version",offset)).lessThanEquals("version",offset+100).sort(DBSort.asc("version"));
        }
                //wikieditlist.add(new WikiEdit(data,wikitext.getTitle(),version));
                //coll2.insert(new WikiEdit(data,wikitext.getTitle(),version));
                //exec.submit(new Task(coll2, data, wikitext.getTitle(), version));
        exec.shutdown();
        try {
            exec.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //coll2.insert(wikieditlist);
        cursor.close();
        mongo.close();


    }

    /*public void makehoge(List<String[]> diff){
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

    }*/
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
}

class Task2 implements Callable<List<String>> {
    List<String> current_text;
    List<String> prev_text;
    String title;
    int version;
    String name;
    public Task2(List<String> current_text,List<String> prev_text,String title,int version,String name){
        this.current_text=current_text;
        this.prev_text=prev_text;
        this.title=title;
        this.version=version;
        this.name=name;
    }
    @Override
    public List<String> call() {
        wikidiffcore.coll3.insert(new WikiTerms("亀梨和也",version,current_text,name));
        Levenshtein3 d = new Levenshtein3();
        List<String> diff = d.diff(prev_text, current_text);
        wikidiffcore.coll4.insert(new Delta("亀梨和也",version,diff,name));
        return diff;
    }
}

class Kaiseki implements Callable<List<String>> {
    Wikitext wikitext;
    public Kaiseki(Wikitext wikitext){
        this.wikitext=wikitext;
    }
    @Override
    public List<String> call() {

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
        return current_text;
    }


}