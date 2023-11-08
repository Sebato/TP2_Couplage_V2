package org.example.structural;

public class PreetyPrinter {
    public String cluster;
    public PreetyPrinter nodeL;
    public PreetyPrinter nodeR;

    public PreetyPrinter(String cluster, PreetyPrinter nodeL, PreetyPrinter nodeR) {
        this.cluster = cluster;
        this.nodeL = nodeL;
        this.nodeR = nodeR;
    }

    public PreetyPrinter(String newCluster, PreetyPrinter pp, String nodeR) {
        this.cluster = newCluster;
        this.nodeL = pp;
        this.nodeR = new PreetyPrinter(nodeR);
    }

    public PreetyPrinter(String newCluster, String nodeL, PreetyPrinter pp) {
        this.cluster = newCluster;
        this.nodeL = new PreetyPrinter(nodeL);
        this.nodeR = pp;
    }

    public PreetyPrinter(String cluster, String nodeL, String nodeR) {
        this.cluster = cluster;
        this.nodeL = new PreetyPrinter(nodeL);
        this.nodeR = new PreetyPrinter(nodeR);
    }

    public PreetyPrinter(String cluster){
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
