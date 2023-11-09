package org.example.structural;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClusterStruct{
    public String cluster;
    public ClusterStruct nodeL;
    public ClusterStruct nodeR;

    //finalement on va utiliser la structure pour stocker aussi les poids...
    //je voulais réserver cette classe à l'affichage à l'origine mais ça sauve beaucoup de calculs
    public double couplageInterne;

    public ClusterStruct(String cluster, ClusterStruct nodeL, ClusterStruct nodeR, Double coup) {
        this.cluster = cluster;
        this.nodeL = nodeL;
        this.nodeR = nodeR;
        this.couplageInterne = coup + nodeL.couplageInterne + nodeR.couplageInterne;
    }

    public ClusterStruct(String newCluster, ClusterStruct c, String nodeR, Double coup) {
        this.cluster = newCluster;
        this.nodeL = c;
        this.nodeR = new ClusterStruct(nodeR);
        this.couplageInterne = coup + c.couplageInterne;
    }

    public ClusterStruct(String newCluster, String nodeL, ClusterStruct c, Double coup) {
        this.cluster = newCluster;
        this.nodeL = new ClusterStruct(nodeL);
        this.nodeR = c;
        this.couplageInterne = coup + c.couplageInterne;
    }

    public ClusterStruct(String cluster, String nodeL, String nodeR, Double coup) {
        this.cluster = cluster;
        this.nodeL = new ClusterStruct(nodeL);
        this.nodeR = new ClusterStruct(nodeR);
        this.couplageInterne = coup;
    }

    public ClusterStruct(String cluster){
        this.cluster = cluster;
        this.nodeL = null;
        this.nodeR = null;
        this.couplageInterne = 0;
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
            return Double.compare(c1.couplageInterne, c2.couplageInterne);
        }
    };

}
