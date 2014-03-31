package com.jr2jme.wikidiff;

/**
 * Created by JR2JME on 2014/03/30.
 */

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Hirotaka on 2014/03/26.
 */
public class Levenshtein3 {
    private EditNode2[] fp=null;
    private int m;
    private int n;
    private int offset;
    private int delta;
    private int size;
    private List<String> A;
    private List<String> B;


    public List<String[]> diff(List<String> a, List<String> b){
        m = a.size();
        n = b.size();
        A=a;
        B=b;
        Boolean reverse=false;
        if(n>m){//入れ替え
            List<String> x;
            x=A;
            A=B;
            B=x;
            int i;
            i=m;
            m=n;
            n=i;
            reverse=true;
        }
        offset=n;
        delta=m-n;
        size=n+m+1;
// v[k] は k で到達可能な x の位置
        fp = new EditNode2[size];
        for(int i=0;i<size;i++){
            fp[i]=new EditNode2(null);
        }


        int p=-1;
        do {
            p = p + 1;
            for (int k = -p; k < delta; k++) {
                snake(k);
            }
            for (int k = delta + p; k > delta; k--) {
                snake(k);
            }
            snake(delta);
        } while(fp[delta+offset].getY()!=null&&fp[delta+offset].getY()<n);

        List<String[]> list=new ArrayList<String[]>();
        int a_index=m-1;
        int b_index=n-1;
        EditNode2 current = fp[delta+offset];
        for(EditTree i=current.getTree();i!=null;i=i.getPrevnode()){
            String type=i.getType();
            if(reverse){
                if(type.equals("+")){
                    String[] str={B.get(b_index),"d"};
                    list.add(str);
                    System.out.println("]削除["+B.get(b_index));
                    //currenttype=0;
                    b_index--;
                }
                else if(type.equals("-")){
                    String[] str={A.get(a_index),"i"};
                    list.add(str);
                    System.out.println(A.get(a_index));
                    //currenttype=1;
                    a_index--;
                }
                else if(type.equals("|")){
                    String[] str={A.get(a_index),"r"};
                    list.add(str);
                    System.out.println("]残存[" + A.get(a_index));
                    //currenttype=2;
                    a_index--;
                    b_index--;
                }
            }
            else{
                if(type.equals("+")){
                    String[] str={B.get(b_index),"i"};
                    list.add(str);
                    System.out.println("]追加["+B.get(b_index));
                    //currenttype=0;
                    b_index--;
                }
                else if(type.equals("-")){
                    String[] str={A.get(a_index),"d"};
                    list.add(str);
                    System.out.println("]削除[" + A.get(a_index));
                    //currenttype=1;
                    a_index--;
                }
                else if(type.equals("|")){
                    String[] str={A.get(a_index),"r"};
                    list.add(str);
                    System.out.println("]残存[" + A.get(a_index));
                    //currenttype=2;
                    a_index--;
                    b_index--;
                }
            }
        }
        List<String[]> list2=new ArrayList<String[]>();
        for(int i=list.size()-1;i>=0;i--){
            list2.add(list.get(i));
        }
        return list2;

    }

   private void snake(int k){
        if(k<-n||m<k){
            return;
        }
        else{
            EditNode2 current = fp[k+offset];
            if(k==-n){
                EditNode2 down=fp[k+1+offset];
                if(down.getY()!=null) {
                    current.setY(down.getY() + 1);
                }else{
                    current.setY(1);
                }
                current.addnode(down.getTree(), "+");
            }
            else if(k==m){
                EditNode2 slide = fp[k-1+offset];
                if(slide.getY()!=null) {
                    current.setY(slide.getY());
                }else{
                    current.setY(0);
                }
                current.addnode(slide.getTree(), "-");
            }
            else{
                EditNode2 slide = fp[k-1+offset];
                EditNode2 down  = fp[k+1+offset];
                if(slide.getY() ==null && down.getY()==null){
                    // どちらも未定義 => (0, 0)について
                    current.setY(0);
                } else if(down.getY() ==null || slide.getY() ==null){
                    // どちらかが未定義状態
                    if(down.getY() ==null){
                        current.setY(slide.getY());
                        current.addnode(slide.getTree(),"-");
                        //System.out.println("a");
                    } else {
                    current.setY(down.getY()+1);
                    current.addnode(down.getTree(),"+");
                        //System.out.println("z");

                    }
                } else {
                    // どちらも定義済み
                    if(slide.getY() > (down.getY()+1)){
                        current.setY(slide.getY());
                        current.addnode(slide.getTree(),"-");
                        System.out.println("y");
                    } else {
                        current.setY(down.getY()+1);
                        current.addnode(down.getTree(),"+");
                        System.out.println("x");

                    }
                }
            }
            int y=current.getY();
            int x=y+k;
            while(x < m && y < n && A.get(x).equals(B.get(y))){
                current.addnode(current.getTree(),"|");
                x++;
                y++;
            }
            current.setY(y);
        }

    }
}

class EditNode2{

    Integer y=null;
    EditTree tree= null;
    public EditNode2(Integer yx) {
        y = yx;
    }
    public void addnode(EditTree t,String type){
        tree=new EditTree(t,type);
    }
    public EditTree getTree(){
        return tree;
    }
    public void setY(int y){
        this.y=y;
    }
    public Integer getY(){
        return y;
    }


}