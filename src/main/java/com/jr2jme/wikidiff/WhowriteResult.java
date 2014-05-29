package com.jr2jme.wikidiff;

import com.jr2jme.doc.DeletedTerms;
import com.jr2jme.doc.InsertedTerms;
import com.jr2jme.doc.WhoWrite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hirotaka on 2014/04/21.
 */
public class WhoWriteResult {
    WhoWriteVer whoWritever=null;
    private InsertedTerms insertedTerms=null;
    private Map<String,DeletedTerms> deletedTerms=null;
    private List<String> dellist=new ArrayList<String>();//消された編集者のリスト
    List<String> delwordcount = new ArrayList<String>();
    Map<String,Integer> insertmap = new HashMap<String, Integer>();
    Map<String,Integer> deletemap = new HashMap<String, Integer>();
    String title;
    String editor;
    int version;
    int order=0;
    boolean isreverted=false;
    //private String editor;
    List<String> text;//比較用ハッシュ
    public WhoWriteResult(String title,List<String> text,String editor,int ver){
        whoWritever=new WhoWriteVer(ver);
        insertedTerms = new InsertedTerms(title,editor,ver);
        deletedTerms=new HashMap<String, DeletedTerms>();
        this.editor=editor;
        this.title=title;
        this.version=ver;
        this.text=text;
    }


    public String getEditor(){
        return editor;
    }
    public List<String> getWordcount() {
        return delwordcount;
    }
    public List<String> getDelwordcount(){
        return delwordcount;
    }
    public void adddelterm(String preeditor,String term){

        dellist.add(preeditor);
        if(deletedTerms.containsKey(preeditor)) {
            deletedTerms.get(preeditor).addterm(term);
        }
        else{
            DeletedTerms deletedterms=new DeletedTerms(title,editor,preeditor,version);
            deletedterms.addterm(term);
            deletedTerms.put(preeditor,deletedterms);
        }
        delwordcount.add(term);
        int i=1;
        if(deletemap.containsKey(term)){
            i+=deletemap.get(term);
        }
        deletemap.put(term,i);
    }

    public void addaddterm(String term){
        whoWritever.addwhowrite(new WhoWrite(editor,title,version,term,order));
        insertedTerms.add(term);
        int i=1;
        if(insertmap.containsKey(term)){
            i+=insertmap.get(term);
        }
        insertmap.put(term,i);
        order++;
    }
    public void remain(String preeditor,String term){
        whoWritever.addwhowrite(new WhoWrite(preeditor, title, version, term, order));
        order++;
    }
    public WhoWriteVer getWhoWritever() {
        return whoWritever;
    }

    public List<String> getDellist() {
        return dellist;
    }

    public List<String> getText() {
        return text;
    }

    public InsertedTerms getInsertedTerms() {
        return insertedTerms;
    }

    public Map<String, Integer> getDeletemap() {
        return deletemap;
    }

    public Map<String, Integer> getInsertmap() {
        return insertmap;
    }

    public Map<String,DeletedTerms> getDeletedTerms() {
        return deletedTerms;
    }

    public void complete(List<WhoWrite> prevdata){
        Map<String,Integer> deletecount=new HashMap<String, Integer>();
        for(String deleditor:dellist){//消された編集者
            if(deletecount.containsKey(deleditor)){
                deletecount.put(deleditor,deletecount.get(deleditor)+1);
            }else{
                deletecount.put(deleditor,1);
            }
        }
        for(Map.Entry<String,DeletedTerms> deleditor:deletedTerms.entrySet()){//消された単語数登録
            deleditor.getValue().setDelnum(deletecount.get(deleditor.getKey()));
            int count=0;
            for(WhoWrite who:prevdata){
                if(deleditor.getKey().equals(who.getEditor())){
                    count++;
                }
            }
            deleditor.getValue().setTotal(count);

        }
    }
    public void reverted(){
        this.isreverted=true;
    }
    public boolean isreverted(){
        return isreverted;
    }

    public boolean compare(WhoWriteResult ddd){

        if((this.insertedTerms.getTerms().equals(ddd.getWordcount())&&this.getWordcount().equals(ddd.getInsertedTerms().getTerms()))) {//ある編集と逆の操作をしているか
            return true;
        }else{
            return false;
        }
    }
    public boolean contain(WhoWriteResult ddd){
        boolean i=false;
        for(Map.Entry<String,Integer> entry:ddd.getDeletemap().entrySet()){
            i=true;
            if(!this.insertmap.containsKey(entry.getKey())||this.insertmap.get(entry.getKey())<entry.getValue()){
                return false;
            }
        }
        for(Map.Entry<String,Integer> entry:ddd.getInsertmap().entrySet()){
            i=true;
            if(!this.deletemap.containsKey(entry.getKey())||this.deletemap.get(entry.getKey())<entry.getValue()){
                return false;
            }
        }
        for(Map.Entry<String,Integer> entry:ddd.getDeletemap().entrySet()){
            this.insertmap.put(entry.getKey(),insertmap.get(entry.getKey())-entry.getValue());
        }
        for(Map.Entry<String,Integer> entry:ddd.getInsertmap().entrySet()){
            this.deletemap.put(entry.getKey(),deletemap.get(entry.getKey())-entry.getValue());
        }
        return i;
    }

    public boolean contain2(WhoWriteResult ddd){
        for(Map.Entry<String,Integer> entry:ddd.getDeletemap().entrySet()){
            if(!this.insertmap.containsKey(entry.getKey())||this.insertmap.get(entry.getKey())<entry.getValue()){
                return false;
            }
        }
        for(Map.Entry<String,Integer> entry:ddd.getInsertmap().entrySet()){
            if(!this.deletemap.containsKey(entry.getKey())||this.deletemap.get(entry.getKey())<entry.getValue()){
                return false;
            }
        }

        return true;
    }


}

