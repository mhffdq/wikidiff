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
import org.mongojack.JacksonDBCollection;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hirotaka on 2014/03/24.
 */

public class wikidiffcore {
    public static void main(String[] arg){
        Mongo mongo=null;
        try {
            mongo = new Mongo("192.168.11.6",27017);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert mongo != null;
        DB db=mongo.getDB("wikipedia_test");
        DBCollection dbCollection=db.getCollection("text_test2");
        JacksonDBCollection<Wikitext,String> coll = JacksonDBCollection.wrap(dbCollection, Wikitext.class,String.class);
        DBCursor<Wikitext> cursor = coll.find(DBQuery.is("title","Bon Appetit!"));
        List<String> pre_text=null;
        for(Wikitext wikitext:cursor){
            StringTagger tagger = SenFactory.getStringTagger(null);
            List<Token> tokens = new ArrayList<Token>();
            try {
                tagger.analyze(wikitext.getText(), tokens);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> current_text=new ArrayList<String>();
            for(Token token:tokens){
                System.out.println(token.getSurface());
                current_text.add(token.getSurface());
            }
            Levenshtein.diff(current_text, pre_text);
            pre_text=current_text;
        }
    }
}
