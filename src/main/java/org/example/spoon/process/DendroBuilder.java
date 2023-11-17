package org.example.spoon.process;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.example.spoon.structural.CallGraph2;
import org.example.spoon.structural.Dendrogram;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DendroBuilder {
    public CallGraph2 callGraph2;
    public SimpleWeightedGraph<Dendrogram, DefaultWeightedEdge> tmpGraph;
    public List<Dendrogram> clusters;

    public DendroBuilder(CallGraph2 cg){
        this.callGraph2 = cg;
        this.tmpGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        this.clusters = new ArrayList<>();

        for(CtType<?> c : cg.graph.vertexSet()){
            Dendrogram d = new Dendrogram(c.getQualifiedName());
            tmpGraph.addVertex(d);
            clusters.add(d);
        }

        //on recupère toutes les arêtes (avec les poids!!) du graphe pondéré
        for (DefaultWeightedEdge e : cg.graph.edgeSet().stream().toList()){

            String s1 = cg.graph.getEdgeSource(e).getQualifiedName();
            String s2 = cg.graph.getEdgeTarget(e).getQualifiedName();

            tmpGraph.addEdge(getGraphNode(s1),getGraphNode(s2));
            tmpGraph.setEdgeWeight(tmpGraph.getEdge(getGraphNode(s1), getGraphNode(s2)),
                    cg.graph.getEdgeWeight(e));
        }
    }

    public Dendrogram getGraphNode(String s) {
        for (Dendrogram d : this.tmpGraph.vertexSet()){
            if (Objects.equals(d.cluster, s)){
                return d;
            }
        }
        return null;
    }

    public Dendrogram buildCluster(){
        Dendrogram finalDendro;

        while (clusters.size()>1){

            Pair<Dendrogram, Dendrogram> pair = clusterProches();

            Dendrogram c1 = pair.getLeft();
            Dendrogram c2 = pair.getRight();

            clusters.remove(c1);
            clusters.remove(c2);

        }
        finalDendro = clusters.get(0);
        return finalDendro;
    }

    //recupère les sommets sur l'arrête à la plus forte pondération
    // si il n'y a plus d'arrête, renvoie les clusters les plus gros
    private Pair<Dendrogram, Dendrogram> clusterProches() {
        double maxPond = 0;
        double eWeight;
        Dendrogram c1 = null, c2 = null;
        Pair<Dendrogram, Dendrogram> pair;

        //si il n'y a plus d'arêtes on renvoie les clsuters les plus gros
        if (tmpGraph.edgeSet().isEmpty()) {
            pair = clusterLourds();
            c1 = pair.getLeft();
            c2 = pair.getRight();
        }else {
            //on récupère les classes de l'arête avec la plus forte pondération
            for (DefaultWeightedEdge e : tmpGraph.edgeSet().stream().toList()){

                //poids de l'arête en cours
                eWeight = tmpGraph.getEdgeWeight(e);

                //comparaison et mise à jour du max
                if( eWeight > maxPond) {
                    maxPond = eWeight;
                    c1 = tmpGraph.getEdgeSource(e);
                    c2 = tmpGraph.getEdgeTarget(e);
                }
            }
        }

        for(DefaultWeightedEdge d : resolveEdges(c1, c2)){
            tmpGraph.removeEdge(d);
        }

        tmpGraph.removeEdge(c1, c2);
        tmpGraph.removeEdge(c2, c1);

        tmpGraph.removeVertex(c1);
        tmpGraph.removeVertex(c2);

        return new ImmutablePair<>(c1, c2);
    }

    //renvoie les clusters les plus gros

    private Pair<Dendrogram, Dendrogram> clusterLourds() {
        Dendrogram c1 = null, c2 = null;
        int maxsize = 0;
        int maxsize2 = 0;
        for (Dendrogram d : clusters){
            if ( d.size() > maxsize){
                maxsize2 = maxsize;
                c2 = c1;
                maxsize = d.size();
                c1 = d;
            }else if (d.size() > maxsize2){
                maxsize2 = d.size();
                c2 = d;
            }
        }

        return new ImmutablePair<>(c1, c2);
    }

    private List<DefaultWeightedEdge> resolveEdges(Dendrogram c1, Dendrogram c2) {
        List<DefaultWeightedEdge> edgesToRemove = new ArrayList<>();

        Dendrogram c = clusterize(c1, c2);
        tmpGraph.addVertex(c);

        for (DefaultWeightedEdge e : tmpGraph.edgeSet().stream().toList()){

            Dendrogram s = tmpGraph.getEdgeSource(e);
            Dendrogram t = tmpGraph.getEdgeTarget(e);

            if (s == c1 && t != c2 || s == c2 && t != c1){
                if (tmpGraph.containsEdge(c, t)){
                    tmpGraph.setEdgeWeight(tmpGraph.getEdge(c, t),
                            tmpGraph.getEdgeWeight(tmpGraph.getEdge(c, t)) + tmpGraph.getEdgeWeight(e));
                }else {
                    tmpGraph.addEdge(c, t);
                    tmpGraph.setEdgeWeight(tmpGraph.getEdge(c, t), tmpGraph.getEdgeWeight(e));
                }
                edgesToRemove.add(e);
            }else if (t == c1 && s != c2 || t == c2 && s != c1){
                if (tmpGraph.containsEdge(c, s)){
                    tmpGraph.setEdgeWeight(tmpGraph.getEdge(c, s),
                            tmpGraph.getEdgeWeight(tmpGraph.getEdge(c, s)) + tmpGraph.getEdgeWeight(e));
                }else {
                    tmpGraph.addEdge(c, s);
                    tmpGraph.setEdgeWeight(tmpGraph.getEdge(c, s), tmpGraph.getEdgeWeight(e));
                }
                edgesToRemove.add(e);
            }
        }
        return edgesToRemove;
    }

    public Dendrogram clusterize(Dendrogram d1 , Dendrogram d2){
        Dendrogram d = new Dendrogram("("+ d1.cluster + ", " + d2.cluster + ")", d1, d2);
        clusters.add(d);
        return d;
    }

    //Méthode d’indentification des groupes de  classes  couplées
    // (services / composants / modules / fonctionnalités)
    // à partir du dendrogramme
    // - Une application doit être composée au plus de M/2 modules
    //      (M est le nombre de classes dans l’application).
    // - Chaque module doit contenir uniquement les classes d’une seule branche
    //      du dendrogramme.
    // - La moyenne du couplage de tous les couples de classes du module
    //      doit être supérieure à CP (CP est un paramètre).

    public List<Dendrogram> identifyModules(double CP){
        List<Dendrogram> modules = new ArrayList<>();

        int M = callGraph2.graph.vertexSet().size()/2;

        double mGlob = idModRec(M, CP, clusters.get(0), modules);

        if (modules.isEmpty()) {
            System.out.println("Aucun sous module identifié avec cette valeur de CP");
            System.out.println("le dendrogramme complet à une moyenne de couplage de : "+mGlob);
        }else {
            System.out.println("le dendrogramme complet à une moyenne de couplage de : "+mGlob);
            System.out.println("les modules identifiés sont les suivants :\n");
        }
        return modules;
    }
    private double idModRec(int M, double CP, Dendrogram d, List<Dendrogram> modules){

        // classe isolée = classe qui n'a pas de couplage
        // on va considérer qu'une classe isolée ne peut pas être dans un module

        if (isIsolated(d)) return 0;

        double moy = calcMoyCouplage(d);

        double moyL = calcMoyCouplage(d.nodeL);
        double moyR = calcMoyCouplage(d.nodeR);
        boolean moySupCP = moy > CP;

        if (modules.size() >= M || !moySupCP || d.getComposants().size() == 1) return moy;

        if (moyL > CP && moyR > CP){

            idModRec(M, CP, d.nodeL, modules);
            idModRec(M, CP, d.nodeR, modules);

            modules.remove(d);

        }else if (moyL > CP && isIsolated(d.nodeR)) {
            idModRec(M, CP, d.nodeL, modules);

        }else if (isIsolated(d.nodeL) && moyR > CP ) {
            idModRec(M, CP, d.nodeR, modules);

        }else modules.add(d);
        return moy;
    }

    public double calcMoyCouplage(Dendrogram d){
        double couplage;

        couplage = callGraph2.getCouplages(d.getComposants());

        return couplage;
    }

    public boolean isIsolated(Dendrogram d){

        boolean b1 =  d.size() == 1;

        Map<CtType<?>, Integer> m1 = callGraph2.couplages.get(callGraph2.getCtTypeNode(d.cluster));
        boolean b2 = m1 == null || m1.isEmpty();

        boolean b3 = true;

        for(CtType entry : callGraph2.couplages.keySet()){
            if (callGraph2.couplages.get(entry).containsKey(callGraph2.getCtTypeNode(d.cluster))){
                b3 = false;
            }
        }
        return(b1 && b2 && b3);
    }
}
