package com.jr2jme.wikidiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JR2JME on 2014/03/24.
 */
public class wikidiffcore {
    public static void main(String[] arg){
        /*Mongo mongo=null;
        try {
            mongo = new Mongo("192.168.11.6",27017);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert mongo != null;
        DB db=mongo.getDB("wikipedia_test");
        DBCollection dbCollection=db.getCollection("text_test2");
        JacksonDBCollection<Wikitext,String> coll = JacksonDBCollection.wrap(dbCollection, Wikitext.class,String.class);
        DBCursor<Wikitext> cursor = coll.find(DBQuery.is("title","Bon Appetit!"));*/
        List<String> prev_text=new ArrayList();
        Levenshtein3 d = new Levenshtein3();
        List<String> current_text=new ArrayList();
        prev_text.add("hoge");
        prev_text.add("hoge");
        long start=System.currentTimeMillis();
        List<String[]> diff = d.diff(prev_text, current_text);
        List<String[]> predata=new ArrayList<String[]>();

        /*for(Wikitext wikitext:cursor){
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
            Levenshtein3 d = new Levenshtein3();
            List<String[]> diff = d.diff(prev_text, current_text);
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
        cursor.close();
        mongo.close();*/
        //System.out.println(System.currentTimeMillis()-start);

    }
}
