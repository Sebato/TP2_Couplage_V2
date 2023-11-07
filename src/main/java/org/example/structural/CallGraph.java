package org.example.structural;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.example.visitors.MethodDeclarationVisitor;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.ExportException;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class CallGraph {

    //chaque classe sera associée à une map qui contiendra
    // les classes qu'elle appelle et
    // le nombre d'appel à ces dernieres
    private Map<String,Map<String, Integer>> classMap = new HashMap<>();
    private int totalCouplage = 0;

    public SimpleWeightedGraph<String, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>
                    (DefaultWeightedEdge.class);
    public SimpleWeightedGraph<String, DefaultWeightedEdge> clusterGraph =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>
                    (DefaultWeightedEdge.class);

    public CallGraph() {
        classMap = new HashMap<>();
    }

    public int getTotalCouplage() {
        return totalCouplage;
    }
    public void printAllClasses(){
        System.out.println("\n---------\nClasses : \n");
        for(Map.Entry<String, Map<String, Integer>> entry : classMap.entrySet()) {
            System.out.println(entry.getKey());
        }
    }

    //A chaque ajout de classe on crée une nouvelle map qui lui est associée
    public void addClassKey(String Ckey) {
        if (!classMap.containsKey(Ckey)) {
            classMap.put(Ckey, new HashMap<>());
        }
    }

    //après initialisation des classes, on cherche les couplages entre elles
    public void findCouplages(TypeDeclaration c){

        //pour chaque methode de cette classe
        for (MethodDeclaration m : c.getMethods()){

            //on visite la methode pour trouver tous les appels à d'autres méthodes
            MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
            m.accept(methodDeclarationVisitor);

            //pour toutes les methodes invoquées, on ajoute un couplage entre la classe courante et la classe invoquée
            for (MethodInvocation mi : methodDeclarationVisitor.getMethodsCalled()){

//                    System.out.println("\n--Method : " + m.getName().getFullyQualifiedName() +
//                            " calls : " + mi.getName().getFullyQualifiedName());

                if (mi.getExpression() != null) { // c'est null quand appel à this implicite par exemple

                    //ATTENTION POSSIBILITÉ D'ÉRREUR D'EXECUTION :
                    // bug constaté dans la librairie :
                    // Si une classe à un nom de taille 1 (ex: mi.getExpression() renvoie "A")
                    // le resolveTypeBinding() retourne null (très embêtant oui)

                    if (mi.getExpression().resolveTypeBinding() != null) {
                        String invokedMethodName = mi.getExpression().resolveTypeBinding().getName();

//                            System.out.println(" from " + tmp);

                        //Autre Petite protection :
                        // si la classe invoquée n'est pas dans le projet on ne l'ajoute pas
                        // du genre System.out.println, Iterator, List.contains()...
                        // ou les appels a une Interface, Visitor ou autre

                        if (classMap.containsKey(invokedMethodName)) {
                            addCouplage(c.getName().getFullyQualifiedName(), invokedMethodName);
                        }
                    }
                }
            }
        }
    }

    // ajoute un couplage entre la classe C et la classe C2
    // et incrémente le couplage total
    private void addCouplage(String c, String c2) {

        //on n'ajoute pas de couplage réflexif
        if (!c2.equals(c)){

            //si la classe C n'est pas deja dans la map on l'ajoute
            // (mais ce serait extrêmement bizarre si elle y est pas)
            if (!classMap.containsKey(c))
                addClassKey(c);

            //si la classe C2 n'est pas deja dans la map de la classe C on l'ajoute
            if (!classMap.get(c).containsKey(c2))
                classMap.get(c).put(c2, 1);
            else //sinon on incrémente le nombre d'appel
                classMap.get(c).put(c2, classMap.get(c).get(c2)+1);

            //on incrémente le nombre total de couplages
            totalCouplage++;
        }
    }

//    Couplage (A,B) = Nombre de relations (relation = appel)
//    entre les couples de méthodes appartenant respectivement
//    aux deux classes en question
//    (A.mi et B.mj) / nombre de toutes  les  relations (binaire)
//    entre  les  couples  de  méthodes  appartenant respectivement
//    à n’importe quelles deux classes de l’application analysée

    public float getCouplage(String c1, String c2){
        float couplage = 0;

        //si les deux classes appartiennent bien au projet
        if (classMap.containsKey(c1) && classMap.containsKey(c2)) {

            //on prend la valeur coté c1
            if (classMap.get(c1).containsKey(c2))
                couplage += (float) classMap.get(c1).get(c2);

            //et on ajoute la valeur coté c2
            if (classMap.get(c2).containsKey(c1))
                couplage += (float) classMap.get(c2).get(c1);

            //on divise par le nombre total de couplages
            couplage /= totalCouplage;

            //et voila !
            return couplage;
        }
        return 0;
    }

    //affiche tous les couplages de l'application (displayAll = true)
    // ou seulement ceux qui sont > 0 (displayAll = false)
    public void allCouplages(boolean displayAll){
        System.out.println("\n---------\nCouplage Total de l'application : "+totalCouplage+"\n");

        List<Pair<String, String>> done = new ArrayList<>();
        float couplage = 0;

        for (Map.Entry<String, Map<String, Integer>> entry : classMap.entrySet()) {
            String s1 = entry.getKey();

            for (Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                String s2 = entry2.getKey();

                if (!s1.equals(s2)) {

                    //pour éviter de parcourir deux fois la même paire de classes
                    if (!done.contains(new ImmutablePair<>(s1, s2)) && !done.contains(new ImmutablePair<>(s2, s1))) {

                        couplage = getCouplage(s1, s2);
                        if (displayAll || couplage > 0) {
                            System.out.println("Couplage entre (" + s1 + " et " + s2 + ") = " + couplage);
                        }
                        done.add(new ImmutablePair<>(s1, s2));
                    }
                }
            }
        }
    }

    public void createGraph(){

        String som1;
        String som2;

        //pour chaque classe on ajoute un sommet au graphe
        for(Map.Entry<String, Map<String, Integer>> entry : classMap.entrySet()) {

            //ATTENTION POSSIBILITE D'ERREUR D'EXECUTION :
            // on ne prend que le nom de la classe et pas le package
            // sinon on ne peut pas éviter les doublons d'arêtes (A,B) et (B,A)

            graph.addVertex(entry.getKey().substring(entry.getKey().lastIndexOf(".")+1));

            // donc on ne pourra pas gérer des projets avec des classes rangées comme suit :
            //      org.version1.AClass.java
            //      org.version2.AClass.java
            // De toute façon le resolveTypeBinding() plus haut ne différenciera pas les deux classes :-/
        }

        //une fois que tous les sommets sont ajoutés, on ajoute les arêtes
        for(Map.Entry<String, Map<String, Integer>> entry : classMap.entrySet()) {

            som1 = entry.getKey();

            for(Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                som2 = entry2.getKey();

                DefaultWeightedEdge e;

                //vérifier si on à pas déjà l'arête dans l'autre sens
                if (!graph.containsEdge(som1, som2) || !graph.containsEdge(som2, som1)) {
                    e = graph.addEdge(som1, som2);
                    graph.setEdgeWeight(e, (double) entry2.getValue() /totalCouplage);
                }
                else {
                    //sinon l'a déjà on incrémente juste le poids de l'arête
                    e = graph.getEdge(som2, som1);
                    graph.setEdgeWeight(e,graph.getEdgeWeight(graph.getEdge(som2, som1)) + (double) entry2.getValue() /totalCouplage);
                }
            }
        }
    }

    //export graph in dot format with weights
    public void exportGraphToDot( String outputPath) {
        DOTExporter<String, DefaultWeightedEdge> exporter = new DOTExporter<>();

        //set vertex id with lambdas
        exporter.setVertexIdProvider((v) -> v);

        //make sure the weights are printed
        exporter.setEdgeAttributeProvider((e) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("weight", DefaultAttribute.createAttribute(graph.getEdgeWeight(e)));
            map.put("label", DefaultAttribute.createAttribute(new DecimalFormat("#.##").format(graph.getEdgeWeight(e))));
            return map;
        });

        try {
            FileWriter fileWriter = new FileWriter(outputPath);
            exporter.exportGraph(graph, fileWriter);
            System.out.println("\n----------Graphe exporté ici : " + outputPath);
            System.out.println("\n Pour en générer un fichier PDF, tapez la commande suivante " +
                    "dans un terminal ouvert à l'endroit où vous souhaitez l'enregistrer : " +
                    "\ndot -Tpdf "+outputPath+" -o <leNomDeVotreGraphe>.pdf\n");
            fileWriter.close();
        } catch (ExportException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public String clusturing(){
        List<String> clusters = new ArrayList<>(graph.vertexSet().stream().toList());
        String prettyPrint = "";
        //on recupère toutes les classes dans le graphe pondéré
        this.clusterGraph.vertexSet().addAll(graph.vertexSet());

        //tant qu'on peut regrouper
        while(clusterGraph.vertexSet().size()>1){

            //on récupère les classes de l'arête avec la plus forte pondération
            Pair<String,String> fuseNodes = clusterProches();
            String nodeL = fuseNodes.getLeft();
            String nodeR = fuseNodes.getRight();

            //on crée le nom du noeud qui représentera ce regroupement
            String newCluster = "("+nodeL+" , "+nodeR+")";
            prettyPrint += "\n\t"+nodeL+"\n\t"+nodeR;

            //on ajoute le nouveau noeud créé
            clusterGraph.addVertex(newCluster);

            //et les aretes qui étaient liées aux précédentes deux classes:
            clusterGraph.edgeSet().addAll(resolveNewEdges(newCluster, nodeL, nodeR));

            //on retire l'arête traitée
            clusterGraph.removeEdge(nodeL,nodeR);
            //et les noeuds qui lui sont associés
            clusterGraph.removeVertex(nodeR);
            clusterGraph.removeVertex(nodeL);


            clusters.add(newCluster);
        }
        return clusters.get(0);
    }

    private Pair<String,String > clusterProches() {
        double maxPond = 0;
        double eWeight = 0;
        String c1 = null, c2 = null;
        for (DefaultWeightedEdge e : graph.edgeSet().stream().toList()){
            eWeight = graph.getEdgeWeight(e);
            if( eWeight > maxPond) {
                maxPond = eWeight;
                c1 = graph.getEdgeSource(e);
                c2 = graph.getEdgeTarget(e);
                break;
            }
        }
        return new ImmutablePair<String,String>(c1,c2);
    }

    //retourne la liste des nouvelles arêtes à ajouter au cluster modifié
    private Collection<? extends DefaultWeightedEdge> resolveNewEdges(String newCluster, String nodeL, String nodeR) {
    }
}
