package com.jr2jme.wikidiff;

import java.util.List;

/**
 * Created by Hirotaka on 2014/03/26.
 */
public class Levenshtein2 {
    static public List<String> diff(List<String> a,List<String> b){
        int n = a.size();
        int m = b.size();
        int max= n+m;
// v[k] は k で到達可能な x の位置
        EditNode[] edittree = new EditNode[max*2+1];
        for(int i=0;i<max+1;i++){
            edittree[i]=new EditNode(null,0);
        }
        EditNode current=null;
        EditNode add=null;
        EditNode del=null;
        for(int d = 0;d<max+1;d++){
            for(int k = -d;k<=d;k+=2){
                if(k < -n||m < k){
                    continue;
                }
                current=edittree[k+n];
                if(d!=0){
                    if(k==-d||k==-n){
                        add=edittree[k+1+n];
                        current.setY(add.getY()+1);
                        current.setType("+");
                        current.setPrevnode(add);
                    }
                    else if(k==d||k==m){
                        del=edittree[k+1+n];
                        current.setY(del.getY());
                        current.setType("-");
                        current.setPrevnode(del);
                    }
                    else {
                        if(edittree[k-1+n].getY()>edittree[k+1+n].getY()+1){
                            del=edittree[k-1+n];
                            current.setY(del.getY()+1);
                            current.setType("-");
                            current.setPrevnode(del);
                        }else{
                            add=edittree[k+1+n];
                            current.setY(add.getY()+1);
                            current.setType("+");
                            current.setPrevnode(add);
                        }
                    }
                }

                int x = current.getY()+k;
                int y = current.y;
                while(x<m&&y<n&&a.get(x).equals(b.get(y))){
                    current.setType("|");
                    current.setPrevnode(current);
                    x++;
                    y++;
                }
                current.setY(y);
                if(x>=m&&y>=n){//完成
                    break;
                }
            }
        }
        String list="";
        int a_index=n-1;
        int b_index=m-1;
        for(EditNode i=current.getPrevnode();i!=null;i=i.getPrevnode()){
            String type="";
            if(type.equals(("+"))){
                list=list+=("追加"+b.get(b_index));
            }

        }
        return a;
    }
}

class EditNode{
    EditNode prevnode=null;
    int y=0;
    String type="";
    public EditNode(EditNode t,int yx) {
        prevnode = t;
        y = yx;
    }
    public EditNode(EditNode t){
        prevnode = t;
    }
    public void setPrevnode(EditNode e){
        prevnode=e;
    }
    public void setY(int y){
        this.y=y;
    }
    public int getY(){
        return y;
    }
    public EditNode getPrevnode(){
        return prevnode;
    }
    public void setType(String s){
        type = s;
    }
    public String getType(){
        return type;
    }
}
