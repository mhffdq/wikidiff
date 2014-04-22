package com.jr2jme.wikidiff;

import com.jr2jme.doc.Delta;
import com.jr2jme.doc.InsertedTerms;
import com.jr2jme.doc.WhoWrite;
import com.jr2jme.doc.WikiTerms;
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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;


public class WikiDiffCore {//Wikipediaのログから差分をとって誰がどこを書いたかを保存するもの リバート対応
    private JacksonDBCollection<WikiTerms,String> coll3;//テキストを形態素解析したやつ
    private JacksonDBCollection<Delta,String> coll4;//差分をとった結果のみ //"+","-","|"で表す．
    private String wikititle = null;//タイトル
    public static void main(String[] arg){
        WikiDiffCore wikidiff=new WikiDiffCore();
        wikititle= title;//タイトル取得
        Pattern pattern = Pattern.compile(title+"/log.+|"+title+"/history.+");
        wikidiff.wikidiff(arg[0]);

    }

    public void wikidiff(String title){
        //mongo準備
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
        DBCollection dbCollection2=db.getCollection("edit_test2");
        DBCollection dbCollection3=db.getCollection("terms");
        DBCollection dbCollection4=db.getCollection("delta");
        JacksonDBCollection<WhoWrite,String> coll2 = JacksonDBCollection.wrap(dbCollection2, WhoWrite.class,String.class);
        coll3 = JacksonDBCollection.wrap(dbCollection3, WikiTerms.class,String.class);
        coll4 = JacksonDBCollection.wrap(dbCollection4, Delta.class,String.class);


        ExecutorService exec = Executors.newFixedThreadPool(20);//マルチすれっど準備 20並列
        int offset=0;
        DBCursor<Wikitext> cursor = coll.find(DBQuery.is("title", wikititle).greaterThan("version",offset)).lessThanEquals("version",offset+500).limit(500).sort(DBSort.asc("version"));//500件ずつテキストを持ってくる．
        int version=1;
        WhoWriteVer prevdata = null;
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
                    tasks2.add(new CalDiff(future.get(), prev_text, wikititle, version, namelist.get(i)));
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
            assert futurelist2 != null;
            for(int ver=0;ver<futurelist2.size();ver++){//誰がどこを書いたかとか
                String current_editor=namelist.get(ver);
                try {
                    List<String> delta = futurelist2.get(ver).get();
                    List<String> text = futurelist.get(ver).get();

                    WhoWriteResult now=whowrite(current_editor,prevdata,text,prevtext,delta,offset+ver+1);
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

                        if(now.compare(resultsarray[(head+ccc)%20])){
                            int dd=0;
                            int ad=0;
                            for(String type:delta){
                                if(type.equals("+")){
                                    now.getWhoWritever().getWhowritelist().get(ad).setEditor(resultsarray[ccc].getDellist().get(dd));
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
                        if(now.comparehash(resultsarray[ccc].getTexthash())){//完全に戻していた場合
                            int index=0;
                            for(WhoWrite who:now.getWhoWritever().getWhowritelist()){
                                who.setEditor(resultsarray[ccc].getWhoWritever().getWhowritelist().get(index).getEditor());
                                index++;
                            }
                            break;
                        }
                    }

                    resultsarray[tail%20]=now;
                    tail++;

                    coll2.insert(now.getWhoWritever().getWhowritelist());
                    prevdata=now.getWhoWritever();
                    prevtext=text;


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            offset+=500;
            cursor = coll.find(DBQuery.is("title", wikititle).greaterThan("version",offset)).lessThanEquals("version",offset+500).limit(500).sort(DBSort.asc("version"));
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

    private WhoWriteResult whowrite(String currenteditor,WhoWriteVer prevdata,List<String> text,List<String> prevtext,List<String> delta,int ver){//誰がどこを書いたか
        int a = 0;
        int b = 0;
        InsertedTerms insertedterms = new InsertedTerms(wikititle,currenteditor,ver);
        DeletedTerms_ex del=new DeletedTerms_ex(wikititle,currenteditor,ver);
        List<String> dellist=new ArrayList<String>();
        WhoWriteVer whoWritever=new WhoWriteVer(ver);
        //Map<String,Map<String,Integer>> deleted= new HashMap<String, Map<String, Integer>>();
        for (String aDelta : delta) {//順番に見て，単語が残ったか追加されたかから，誰がどこ書いたか
            //System.out.println(delta.get(x));
            if (aDelta.equals("+")) {
                //System.out.println(text.get(a));
                whoWritever.getWhowritelist().add(new WhoWrite(currenteditor,wikititle,ver,text.get(a),a));
                insertedterms.add(text.get(a));
                a++;
            } else if (aDelta.equals("-")) {
                dellist.add(prevdata.getWhowritelist().get(b).getEditor());
                del.add(prevdata.getWhowritelist().get(b).getEditor(), prevtext.get(b));
                //System.out.println(prevdata.getText_editor().get(b).getTerm());
                b++;
            } else if (aDelta.equals("|")) {
                //System.out.println(prevdata.getText_editor().get(b).getTerm());
                whoWritever.getWhowritelist().add(new WhoWrite(currenteditor,wikititle,ver,text.get(a),a));
                a++;
                b++;
            }
        }

        return new WhoWriteResult(whoWritever,insertedterms,del,dellist,text,currenteditor);



    }



}

class WhoWriteVer {
    List<WhoWrite> whowritelist=new ArrayList<WhoWrite>();
    int version;
    public WhoWriteVer(int ver){
        version=ver;
    }
    public void addwhowrite(WhoWrite who){
        this.whowritelist.add(who);
    }

    public int getVersion() {
        return version;
    }

    public List<WhoWrite> getWhowritelist() {
        return whowritelist;
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