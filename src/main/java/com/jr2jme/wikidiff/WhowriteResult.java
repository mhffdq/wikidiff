package com.jr2jme.wikidiff;

import com.jr2jme.doc.InsertedTerms;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by Hirotaka on 2014/04/21.
 */
public class WhoWriteResult {
    private WhoWriteVer whoWritever;
    private InsertedTerms insertedTerms;
    private DeletedTerms_ex deletedTerms;
    private List<String> dellist;
    //private String editor;
    private String texthash;//比較用ハッシュ
    public WhoWriteResult(WhoWriteVer who,InsertedTerms insert,DeletedTerms_ex del,List<String> dellist,List<String> text,String editor){
        whoWritever=who;
        insertedTerms=insert;
        deletedTerms=del;
        this.dellist=dellist;
        String tex = "";
        for(String str:text){
            tex +=str;
        }
        this.texthash=String2MD5(tex);
    }

    /*public String getEditor() {
        return editor;
    }*/

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

    public DeletedTerms_ex getDeletedTerms() {
        return deletedTerms;
    }

    public boolean compare(WhoWriteResult ddd){
        if((this.insertedTerms.getTerms().equals(ddd.deletedTerms.wordcount)&&this.deletedTerms.wordcount.equals(ddd.insertedTerms.getTerms()))) {//ある編集と逆の操作をしているか
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
