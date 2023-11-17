package org.example.spoon.structural;

import java.util.ArrayList;
import java.util.List;

public class Dendrogram {
    public String cluster;
    public Dendrogram nodeL;
    public Dendrogram nodeR;
    
    
    public Dendrogram(String cluster){
        this.cluster = cluster;
        this.nodeL = null;
        this.nodeR = null;
    }

    public Dendrogram(String s, Dendrogram d1, Dendrogram d2){
        this.cluster = s;
        this.nodeR = d1;
        this.nodeL = d2;
    }

    public int size(){
        int size = 0;
        if (this.nodeL != null){
            size += this.nodeL.size();
        }
        if (this.nodeR != null){
            size += this.nodeR.size();
        }
        return size + 1;
    }

    private String indent(String s){
        //insérer '\t' après chaque '\n'
        //sauf si le '\n' est suivi d'un '(' (début d'un nouveau cluster au même niveau

        StringBuilder res = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            res.append(s.charAt(i));
            if (s.charAt(i) == '\n'){
                res.append("\t");
            }
        }
        return res.toString();
    }

    //retourne une liste contenant le cluster et tous les tous ses sous-clusters
    public List<String> getComposants(){
        List<String> composants = new ArrayList<>();

        composants.add(this.cluster);
        if (this.nodeL != null) composants.addAll(this.nodeL.getComposants());
        if (this.nodeR != null) composants.addAll(this.nodeR.getComposants());

        return composants;
    }

    public String toString(){
        String res = "";
        res += this.cluster;
        if (this.nodeL != null){
            res += "\n  |--" + indent(this.nodeL.toString());
        }
        if (this.nodeR != null){
            res += "\n  |--" + indent(this.nodeR.toString());
        }
        return res;
    }
    

}
