package com.jr2jme.wikidiff;

import com.jr2jme.doc.WhoWrite;
import com.mongodb.*;
import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.stream.CompositeTokenFilter;

import java.io.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.concurrent.*;

//import org.atilika.kuromoji.Token;

//import net.java.sen.dictionary.Token;

//import org.atilika.kuromoji.Token;


public class WikiDiffCore {//Wikipediaのログから差分をとって誰がどこを書いたかを保存するもの リバート対応
    private static DBCollection coll;
    //private static JacksonDBCollection<WhoWrite,String> coll2;
    //private static JacksonDBCollection<InsertedTerms,String> coll3;//insert
    //private static JacksonDBCollection<DeletedTerms,String> coll4;//del&
    //private String wikititle = null;//タイトル
    static DB db=null;
    public static void main(String[] arg){
       // Set<String> aiming=fileRead("input.txt");
        MongoClient mongo=null;
        try {
            mongo = new MongoClient("dragons.db.ss.is.nagoya-u.ac.jp",27017);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert mongo != null;
        DB db=mongo.getDB("wikipediaDB_kondou");
        DBCollection dbCollection=db.getCollection("wikitext_Islam");
        coll=db.getCollection("wikitext_Islam");
        //coll = JacksonDBCollection.wrap(dbCollection, Wikitext.class, String.class);
        DBCollection dbCollection2=db.getCollection("editor_term_Islam");
        DBCollection dbCollection3=db.getCollection("Insertedterms_Islam");
        DBCollection dbCollection4=db.getCollection("DeletedTerms_Islam");

        //coll2 = JacksonDBCollection.wrap(dbCollection2, WhoWrite.class,String.class);
        //coll3 = JacksonDBCollection.wrap(dbCollection3, InsertedTerms.class,String.class);
        //coll4 = JacksonDBCollection.wrap(dbCollection4, DeletedTerms.class,String.class);


        WikiDiffCore wikidiff=new WikiDiffCore();
        //wikititle= title;//タイトル取得
        //Pattern pattern = Pattern.compile(title+"/log.+|"+title+"/history.+");
        Cursor cur=null;
        cur=wikidiff.wikidiff("アクバル");
        cur.close();
        mongo.close();
        System.out.println("終了:"+arg[0]);

    }

    public static Set fileRead(String filePath) {

        FileReader fr = null;
        BufferedReader br = null;
        Set<String> aiming= new HashSet<String>(350);
        try {
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                aiming.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return aiming;
    }

    public Cursor wikidiff(String title){
        //mongo準備
        final int NUMBER=50;
        //DBCollection dbCollection5=db.getCollection("Revert");
        ExecutorService exec = Executors.newFixedThreadPool(20);//マルチすれっど準備 20並列
        int offset=0;
        BasicDBObject findQuery = new BasicDBObject();//
        findQuery.put("title", title);
        findQuery.put("version",new BasicDBObject("$gt",offset));
        BasicDBObject sortQuery = new BasicDBObject();
        sortQuery.put("version", 1);
        DBCursor cursor = coll.find(findQuery).sort(sortQuery).limit(NUMBER);
        int version=1;
        List<WhoWrite> prevdata = null;
        long start=System.currentTimeMillis();
        List<String> prev_text=new ArrayList<String>();
        List<String> prevtext = new ArrayList<String>();
        List<String> prevpara = new ArrayList<String>();
        WhoWriteResult[] resultsarray= new WhoWriteResult[20];//キューっぽいもの
        int tail=0;
        int head;
        while(cursor.hasNext()) {//回す
            List<String> namelist=new ArrayList<String>();
            List<Future<List<String>>> futurelist = new ArrayList<Future<List<String>>>(NUMBER+1);
            for (DBObject dbObject:cursor) {//まず100件ずつテキストを(並列で)形態素解析
                //Wikitext wikitext=new Wikitext(title,(Date)dbObject.get("date"),(String)dbObject.get("name"),(String)dbObject.get("text"),(Integer)dbObject.get("revid"),(String)dbObject.get("comment"),(Integer)dbObject.get("version"));
                List<String> prechange=new ArrayList<String>();
                List<String> nowchange=new ArrayList<String>();
                String wikitext=(String)dbObject.get("text");
                List<String> parastr= Arrays.asList(wikitext.split("\n\n|。"));
                Levenshtein3 d = new Levenshtein3();
                List<String> diff = d.diff(prevpara, parastr);

                futurelist.add(exec.submit(new Kaiseki((String)dbObject.get("text"))));
                namelist.add((String)dbObject.get("name"));
            }
            cursor.close();
            int i=0;
            List<CalDiff> tasks2 = new ArrayList<CalDiff>(futurelist.size());
            for(Future<List<String>> future:futurelist){//差分をとる
                try {
                    tasks2.add(new CalDiff(future.get(), prev_text, title, version, namelist.get(i)));
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
                futurelist2=exec.invokeAll(tasks2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }//差分ここまで
            for(int ver=0;ver<futurelist2.size();ver++){//誰がどこを書いたかとか
                String current_editor=namelist.get(ver);
                try {
                    List<String> delta = futurelist2.get(ver).get();
                    List<String> text = futurelist.get(ver).get();

                    WhoWriteResult now=whowrite(title,current_editor,prevdata,text,prevtext,delta,offset+ver+1);
                    int last;
                    if(tail>=20){
                        last=20;
                        head=tail+1;
                    }
                    else{
                        last=tail;
                        head=0;
                    }
                    List<String> edrvted=new ArrayList<String>();
                    List<Integer> rvted=new ArrayList<Integer>();
                    for(int ccc=last-1;ccc>=0;ccc--){//リバート検知
                        int index=(head+ccc)%20;


                    }

                    resultsarray[tail%20]=now;
                    tail++;

                    //coll2.insert(now.getWhoWritever().getWhowritelist());//ここは20140423現在使う
                    prevdata=now.getWhoWritever().getWhowritelist();
                    prevtext=text;


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            offset+=NUMBER;
            //System.out.println(offset);
            findQuery = new BasicDBObject();//
            findQuery.put("title", title);
            findQuery.put("version", new BasicDBObject("$gt", offset).append("$lte", offset + NUMBER));
            sortQuery = new BasicDBObject();
            sortQuery.put("version", 1);
            cursor = coll.find(findQuery).sort(sortQuery).limit(NUMBER);
        }
        exec.shutdown();
        try {
            exec.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(System.currentTimeMillis() - start);

        return cursor;

    }

    private WhoWriteResult whowrite(String title,String currenteditor,List<WhoWrite> prevdata,List<String> text,List<String> prevtext,List<String> delta,int ver) {//誰がどこを書いたか
        int a = 0;//この関数が一番重要
        int b = 0;
        WhoWriteResult whowrite = new WhoWriteResult(title, text, currenteditor, ver);
        for (String aDelta : delta) {//順番に見て，単語が残ったか追加されたかから，誰がどこ書いたか
            //System.out.println(delta.get(x));
            if (aDelta.equals("+")) {
                //System.out.println(text.get(a));
                whowrite.addaddterm(text.get(a));
                a++;
            } else if (aDelta.equals("-")) {
                whowrite.adddelterm(prevdata.get(b).getEditor(), prevtext.get(b));
                b++;
            } else if (aDelta.equals("|")) {
                //System.out.println(prevdata.getText_editor().get(b).getTerm());
                whowrite.remain(prevdata.get(b).getEditor(), text.get(a));
                a++;
                b++;
            }
        }
        whowrite.complete(prevdata);
        /*coll3.insert(whowrite.getInsertedTerms());
        for (DeletedTerms de : whowrite.getDeletedTerms().values()){
            coll4.insert(de);
        }*/
        return whowrite;



    }



}

class CalDiff implements Callable<List<String>> {//差分
    List<String> current_text;
    List<String> prev_text;
    public CalDiff(List<String> current_text,List<String> prev_text,String title,int version,String name){
        this.current_text=current_text;
        this.prev_text=prev_text;
    }
    @Override
    public List<String> call() {//並列で差分
        Levenshtein3 d = new Levenshtein3();
        List<String> diff = d.diff(prev_text, current_text);
        return diff;
    }
}

class Kaiseki implements Callable<List<String>> {//形態素解析
    String wikitext;//gosenだとなんか駄目だった→kuromojimo別のでダメ
    public Kaiseki(String wikitext){
        this.wikitext=wikitext;
    }
    @Override
    public List<String> call() {

        StringTagger tagger = SenFactory.getStringTagger(null);
        CompositeTokenFilter ctFilter = new CompositeTokenFilter();

        try {
            ctFilter.readRules(new BufferedReader(new StringReader("名詞-数")));
            tagger.addFilter(ctFilter);

            ctFilter.readRules(new BufferedReader(new StringReader("記号-アルファベット")));
            tagger.addFilter(ctFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Token> tokens = new ArrayList<Token>();
        try {
            tokens=tagger.analyze(wikitext, tokens);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> current_text = new ArrayList<String>(tokens.size());

       for(Token token:tokens){

            current_text.add(token.getSurface());
        }

        return current_text;
    }


}