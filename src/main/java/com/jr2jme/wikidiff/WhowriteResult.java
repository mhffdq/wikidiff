package com.jr2jme.wikidiff;

import com.jr2jme.doc.DeletedTerms;
import com.jr2jme.doc.InsertedTerms;
import com.jr2jme.doc.WhoWrite;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hirotaka on 2014/04/21.
 */
public class WhoWriteResult {
    private WhoWriteVer whoWritever;
    private InsertedTerms insertedTerms;
    private Map<String,DeletedTerms> deletedTerms;
    private List<String> dellist=new ArrayList<String>();
    Map<String,Integer> delwordcount = new HashMap<String, Integer>();
    String title;
    String editor;
    int version;
    int order=0;
    //private String editor;
    private String texthash;//比較用ハッシュ
    public WhoWriteResult(String title,List<String> text,String editor,int ver){
        whoWritever=new WhoWriteVer(ver);
        insertedTerms = new InsertedTerms(title,editor,ver);
        deletedTerms=new HashMap<String, DeletedTerms>();
        this.editor=editor;
        this.title=title;
        this.version=ver;
        String tex = "";
        for(String str:text){
            tex +=str;
        }
        this.texthash=String2MD5(tex);
        for(Map.Entry<String,DeletedTerms>delete:this.getDeletedTerms().entrySet()){
            for(String str:delete.getValue().getTerms()){
                if(delwordcount.containsKey(str)){
                    delwordcount.put(str,delwordcount.get(str)+1);
                }
                else{
                    delwordcount.put(str,1);
                }
            }
        }
    }

    public void setDellist(List<String> dellist) {
        this.dellist = dellist;
    }

    public void setWhoWritever(WhoWriteVer whoWritever) {
        this.whoWritever = whoWritever;
    }

    public void setInsertedTerms(InsertedTerms insertedTerms) {
        this.insertedTerms = insertedTerms;
    }

    public void setDeletedTerms(Map<String, DeletedTerms> deletedTerms) {
        this.deletedTerms = deletedTerms;
    }
    /*public String getEditor() {
        return editor;
    }*/

    public Map<String, Integer> getWordcount() {
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
    }

    public void addaddterm(String term){
        whoWritever.addwhowrite(new WhoWrite(editor,title,version,term,order));
        insertedTerms.add(term);
        order++;
    }
    public void remain(String preeditor,String term){
        whoWritever.addwhowrite(preeditor,title,version,term,order));
        order++;
    }
    public WhoWriteVer getWhoWritever() {
        return whoWritever;
    }

    public List<String> getDellist() {
        return dellist;
    }

    public String getTexthash() {
        return texthash;
    }

    public InsertedTerms getInsertedTerms() {
        return insertedTerms;
    }

    public Map<String,DeletedTerms> getDeletedTerms() {
        return deletedTerms;
    }

    public boolean compare(WhoWriteResult ddd){

        if((this.insertedTerms.getTerms().equals(ddd.getWordcount())&&this.getWordcount().equals(ddd.insertedTerms.getTerms()))) {//ある編集と逆の操作をしているか
            //取り消しだった場合
            /*for(Map.Entry<String,Integer> hoge:this.deletedTerms.wordcount.entrySet()){
                System.out.println(hoge.getKey());
            }*/
            return true;
        }else{
            return false;
        }
    }
    public boolean comparehash(String hash){//同じか
        return texthash.equals(hash);
    }
    private String String2MD5(String key){
        byte[] hash = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(key.getBytes());
            hash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            //
        }
        return hashByte2MD5(hash);
    }

    private String hashByte2MD5(byte []hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte aHash : hash) {
            if ((0xff & aHash) < 0x10) {
                hexString.append("0").append(Integer.toHexString((0xFF & aHash)));
            } else {
                hexString.append(Integer.toHexString(0xFF & aHash));
            }
        }

        return hexString.toString();
    }

}
