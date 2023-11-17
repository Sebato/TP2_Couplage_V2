package org.example.eclipsejdt.structural;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClusterStruct{
    public String cluster;
    public ClusterStruct nodeL;
    public ClusterStruct nodeR;

    public Double moycouplage = null;

    public ClusterStruct(String cluster, ClusterStruct nodeL, ClusterStruct nodeR) {
        this.cluster = cluster;
        this.nodeL = nodeL;
        this.nodeR = nodeR;
    }

    public ClusterStruct(String newCluster, ClusterStruct c, String nodeR) {
        this.cluster = newCluster;
        this.nodeL = c;
        this.nodeR = new ClusterStruct(nodeR);
    }

    public ClusterStruct(String newCluster, String nodeL, ClusterStruct c) {
        this.cluster = newCluster;
        this.nodeL = new ClusterStruct(nodeL);
        this.nodeR = c;
    }

    public ClusterStruct(String cluster, String nodeL, String nodeR) {
        this.cluster = cluster;
        this.nodeL = new ClusterStruct(nodeL);
        this.nodeR = new ClusterStruct(nodeR);
    }

    public ClusterStruct(String cluster){
        this.cluster = cluster;
        this.nodeL = null;
        this.nodeR = null;
    }


    private String indent(String s){
        //insérer '\t' après chaque '\n'
        //sauf si le '\n' est suivi d'un '(' (début d'un nouveau cluster au même niveau

        String res = "";
        for (int i = 0; i < s.length(); i++) {
            res += s.charAt(i);
            if (s.charAt(i) == '\n'){
                res += "\t";
            }
        }

        return res;
    }

    //retourne une liste contenant le cluster et tous les tous ses sous-clusters
    public List<String> getComposants(){
        List<String> composants = new ArrayList<String>();

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

    public static Comparator<ClusterStruct> ComparatorCouplage = new Comparator<ClusterStruct>() {

        @Override
        public int compare(ClusterStruct c1, ClusterStruct c2) {
            if (c1.moycouplage == null && c2.moycouplage != null) return 1;
            if (c1.moycouplage != null && c2.moycouplage == null) return -1;
            return Double.compare(c1.moycouplage, c2.moycouplage);
        }
    };

}
