package com.jr2jme.wikidiff;

import com.jr2jme.doc.DeletedTerms;
import com.jr2jme.doc.InsertedTerms;
import com.jr2jme.doc.WhoWrite;
import com.jr2jme.st.Wikitext;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;


public class WikiDiffCore {//Wikipediaのログから差分をとって誰がどこを書いたかを保存するもの リバート対応
    private static JacksonDBCollection<Wikitext,String> coll;
    private static JacksonDBCollection<WhoWrite,String> coll2;
    private static JacksonDBCollection<InsertedTerms,String> coll3;//insert
    private static JacksonDBCollection<DeletedTerms,String> coll4;//del&
    //private String wikititle = null;//タイトル
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
        coll = JacksonDBCollection.wrap(dbCollection, Wikitext.class, String.class);
        DBCollection dbCollection2=db.getCollection("editor_term_Islam");
        DBCollection dbCollection3=db.getCollection("Insertedterms_Islam");
        DBCollection dbCollection4=db.getCollection("DeletedTerms_Islam");
        coll2 = JacksonDBCollection.wrap(dbCollection2, WhoWrite.class,String.class);
        coll3 = JacksonDBCollection.wrap(dbCollection3, InsertedTerms.class,String.class);
        coll4 = JacksonDBCollection.wrap(dbCollection4, DeletedTerms.class,String.class);


        WikiDiffCore wikidiff=new WikiDiffCore();
        //wikititle= title;//タイトル取得
        //Pattern pattern = Pattern.compile(title+"/log.+|"+title+"/history.+");
        DBCursor<Wikitext>cur=null;

        cur=wikidiff.wikidiff(arg[0]);

        cur.close();
        mongo.close();

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

    public DBCursor<Wikitext> wikidiff(String title){
        //mongo準備

        ExecutorService exec = Executors.newFixedThreadPool(20);//マルチすれっど準備 20並列
        int offset=0;
        DBCursor<Wikitext> cursor = coll.find(DBQuery.is("title",title).greaterThan("version",offset)).lessThanEquals("version",offset+300).limit(300).sort(DBSort.asc("version"));//300件ずつテキストを持ってくる．
        int version=1;
        List<WhoWrite> prevdata = null;
        long start=System.currentTimeMillis();
        List<String> prev_text=new ArrayList<String>();
        List<String> prevtext = new ArrayList<String>();
        WhoWriteResult[] resultsarray= new WhoWriteResult[20];//キューっぽいもの
        int tail=0;
        int head;
        while(cursor.hasNext()) {//回す
            //List<List<String>> editlist=new ArrayList<List<String>>();
            //List<WikiEdit> wikieditlist = new ArrayList<WikiEdit>(50);
            List<String> namelist=new ArrayList<String>(cursor.size());
            List<Future<List<String>>> futurelist = new ArrayList<Future<List<String>>>(cursor.size());
            for (Wikitext wikitext : cursor) {//まず100件ずつテキストを(並列で)形態素解析
                futurelist.add(exec.submit(new Kaiseki(wikitext)));
                namelist.add(wikitext.getName());
            }
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
                    for(int ccc=0;ccc<last;ccc++){//リバート検知
                        int index=(head+ccc)%20;
                        if(now.compare(resultsarray[index])){
                            //System.out.println(now.version+":"+resultsarray[index].version);
                            int dd=0;
                            int ad=0;
                            for(String type:delta){

                                if(type.equals("+")){
                                    //System.out.println(now.getInsertedTerms().getTerms().get(dd));
                                    now.getWhoWritever().getWhowritelist().get(ad).setEditor(resultsarray[index].getDellist().get(dd));
                                    //now.whoWrite.getEditors().set(ad,resultsarray[ccc].dellist.get(dd));
                                    dd++;
                                    ad++;
                                }
                                else if(type.equals("|")){
                                    ad++;
                                }
                            }
                            //now=whowrite(current_editor,prevdata,text,prevtext,delta,offset+ver+1)
                            break;
                        }
                        if(now.comparehash(resultsarray[ccc].getText())){//完全に戻していた場合
                            int indext=0;
                            for(WhoWrite who:now.getWhoWritever().getWhowritelist()){
                                who.setEditor(resultsarray[ccc].getWhoWritever().getWhowritelist().get(indext).getEditor());
                                indext++;
                            }
                            break;
                        }
                    }

                    resultsarray[tail%20]=now;
                    tail++;

                    coll2.insert(now.getWhoWritever().getWhowritelist());
                    prevdata=now.getWhoWritever().getWhowritelist();
                    prevtext=text;


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            offset+=300;
            System.out.println(offset);
            cursor = coll.find(DBQuery.is("title", title).greaterThan("version",offset)).lessThanEquals("version",offset+300).limit(300).sort(DBSort.asc("version"));
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

        System.out.println(System.currentTimeMillis() - start);
        //coll2.insert(wikieditlist);

        return cursor;

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

    private WhoWriteResult whowrite(String title,String currenteditor,List<WhoWrite> prevdata,List<String> text,List<String> prevtext,List<String> delta,int ver) {//誰がどこを書いたか
        int a = 0;//この関数が一番重要
        int b = 0;
        WhoWriteResult whowrite = new WhoWriteResult(title, text, currenteditor, ver);
        //Map<String,Map<String,Integer>> deleted= new HashMap<String, Map<String, Integer>>();
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
        coll3.insert(whowrite.getInsertedTerms());
        for (DeletedTerms de : whowrite.getDeletedTerms().values()){
            coll4.insert(de);
        }
        return whowrite;



    }



}
/*class Task implements Runnable {//
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
}*/

class CalDiff implements Callable<List<String>> {//差分
    List<String> current_text;
    List<String> prev_text;
    String title;
    int version;
    String name;
    public CalDiff(List<String> current_text,List<String> prev_text,String title,int version,String name){
        this.current_text=current_text;
        this.prev_text=prev_text;
        this.title=title;
        this.version=version;
        this.name=name;
    }
    @Override
    public List<String> call() {//並列で差分
        //wikidiffcore.coll3.insert(new WikiTerms(title,version,current_text,name));
        Levenshtein3 d = new Levenshtein3();
        List<String> diff = d.diff(prev_text, current_text);
        //wikidiffcore.coll4.insert(new Delta(title,version,diff,name));
        return diff;
    }
}

class Kaiseki implements Callable<List<String>> {//形態素解析
    Wikitext wikitext;//gosenだとなんか駄目だった
    public Kaiseki(Wikitext wikitext){
        this.wikitext=wikitext;
    }
    @Override
    public List<String> call() {

        /*StringTagger tagger = SenFactory.getStringTagger(null);
        List<Token> tokens = new ArrayList<Token>();
        try {
            tagger.analyze(wikitext.getText(), tokens);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        Tokenizer tokenizer = Tokenizer.builder().build();
        List<Token> tokens = tokenizer.tokenize(wikitext.getText());
        List<String> current_text = new ArrayList<String>(tokens.size());

        for (Token token : tokens) {

            current_text.add(token.getSurfaceForm());
        }
        return current_text;
    }


}