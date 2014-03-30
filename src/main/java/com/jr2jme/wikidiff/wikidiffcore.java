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
 * Created by JR2JME on 2014/03/24.
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
        List<String> prev_text=new ArrayList();
        List<String[]> predata=new ArrayList<String[]>();
        long start=System.currentTimeMillis();
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
                //System.out.println(token.getSurface());
                current_text.add(token.getSurface());
            }
            List<String[]> diff = Levenshtein2.diff(prev_text, current_text);
            List<String[]> data=new ArrayList<String[]>();
            int i = 0;

            for(String[] st:diff){
                if(st[1].equals("i")){
                    String[] tes ={st[0],wikitext.getName()};
                    data.add(tes);
                }
                else if(st[1].equals("d")){
                    i++;
                }
                else if(st[1].equals("r")){
                    String[] tes = {st[0],predata.get(i)[1]};
                    data.add(tes);
                    i++;
                }
            }
            DBCollection dbCollection2=db.getCollection("edit_test");
            JacksonDBCollection<WikiEdit,String> coll2 = JacksonDBCollection.wrap(dbCollection2, WikiEdit.class,String.class);
            coll2.insert(new WikiEdit(data, wikitext.getTitle(), wikitext.getRevid()));

            predata=data;
            prev_text=current_text;
        }
        System.out.println(start-System.currentTimeMillis());

    }
}
