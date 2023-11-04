package org.example.structural;

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
    private Map<TypeDeclaration,Map<String, Integer>> classMap = new HashMap<>();
    private int totalCouplage = 0;

    public SimpleWeightedGraph<String, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>
                    (DefaultWeightedEdge.class);

    public CallGraph() {
        classMap = new HashMap<>();
    }


    public int getTotalCouplage() {
        return totalCouplage;
    }

    //A chaque ajout de classe on crée une nouvelle map qui lui est associée
    public void addClassKey(TypeDeclaration Ckey) {
        if (!classMap.containsKey(Ckey)) {
            classMap.put(Ckey, new HashMap<>());
        }
//        else System.out.println("Class "+ Ckey.getName() +" is already in the ClassMap");
    }

    //après initialisation des classes, on cherche les couplages entre elles
    public void findCouplages(){
        //pour chaque classe
        for (TypeDeclaration c : this.classMap.keySet()){
            //System.out.println("\n--------- Class : " + c.getName().getFullyQualifiedName());

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
                        // bug constaté dans la librairie:
                        // si une classe à un nom de taille 1 (ex: mi.getExpression() renvoie A)
                        // le resolveTypeBinding() retourne null (très embêtant oui)

                        if (mi.getExpression().resolveTypeBinding() != null) {
                            //System.out.println(" from " + mi.getExpression().resolveTypeBinding().getName());
                            addCouplage(c, mi.getExpression().resolveTypeBinding().getName());
                        }
//                        else System.err.println("resolveTypeBinding() returned null for " + mi.getExpression().toString());
                    }
                }
            }
        }
    }

    // ajoute un couplage entre la classe C et la classe C2
    // et incrémente le couplage total
    private void addCouplage(TypeDeclaration c, String c2) {
        //System.out.println("Adding coupling between " + c.getName() + " and " + c2);

        //on n'ajoute pas de couplage réflexif
        if (!c2.equals(c.getName().toString())){

            //si la classe C n'est pas deja dans la map on l'ajoute
            // (mais c'est bizarre si elle y est pas)
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

    public float calcCouplage(String c1, String c2){
        float couplage = 0;

        Map<String, Integer> c1Calls = new HashMap<>();
        Map<String, Integer> c2Calls = new HashMap<>();

        for (Map.Entry<TypeDeclaration, Map<String, Integer>> entry : classMap.entrySet()) {

            if (entry.getKey().getName().toString().equals(c1))
                c1Calls = entry.getValue();
            if (entry.getKey().getName().toString().equals(c2))
                c2Calls = entry.getValue();
            if (!c1Calls.isEmpty() && !c2Calls.isEmpty())
                break;
        }

        //si les deux classes appartiennent bien au projet
        if (!c1Calls.isEmpty() && !c2Calls.isEmpty()) {

            //on récupère les classes appelées par la classe c1
            if (c1Calls.containsKey(c2)) {
                couplage += c1Calls.get(c2);
            }
            //idem pour c2
            if (c2Calls.containsKey(c1)) {
                couplage += c2Calls.get(c1);
            }
        }
        return  couplage / totalCouplage;
    }

    public void allCouplages(){
        System.out.println("\n---------\nCouplage Total de l'application : "+totalCouplage+"\n");

        //pour éviter de parcourir deux fois la même paire de classes
        List<Pair<String, String>> done = new ArrayList<>();
        String s1;
        String s2;

        for (Map.Entry<TypeDeclaration, Map<String, Integer>> entry : classMap.entrySet()) {
            s1 = entry.getKey().getName().toString();

            for (Map.Entry<TypeDeclaration, Map<String, Integer>> entry2 : classMap.entrySet()) {
                s2 = entry2.getKey().getName().toString();

                if (!s1.equals(s2)){
                    if(!done.contains(new MutablePair<>(s1,s2)) && !done.contains(new MutablePair<>(s2,s1))) {
                        //System.out.println("Couplage entre (" + s1 + " et " + s2 + ") = " + calcCouplage(s1, s2));
                        done.add(new MutablePair<>(s1,s2));
                    }
                }
            }
        }
    }

    //pas utile si on affiche déja dans allCouplages()
    public void printCoupling() {
        System.out.println("\n---------\nCouplages : \n");
        for(Map.Entry<TypeDeclaration, Map<String, Integer>> entry : classMap.entrySet()) {
            System.out.println(entry.getKey().resolveBinding().getQualifiedName() + " -> ");
            for(Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                System.out.println("\t" + entry2.getKey() + " (" + entry2.getValue() + ")");
            }
        }
    }

    public void createGraph(){

        String som1;
        String som2;

        //pour chaque classe on ajoute un sommet au graphe
        for(Map.Entry<TypeDeclaration, Map<String, Integer>> entry : classMap.entrySet()) {

            //ATTENTION POSSIBILITE D'ERREUR D'EXECUTION :
            // on ne prend que le nom de la classe et pas le package
            // sinon on ne peut pas éviter les doublons d'arêtes (A,B) et (B,A)

            graph.addVertex(entry.getKey().resolveBinding().getQualifiedName().substring(entry.getKey().resolveBinding().getQualifiedName().lastIndexOf(".")+1));
        }
        System.out.println("\n---------\nGraph vertices : \n" + graph.vertexSet());
        //une fois que tous les sommets sont ajoutés, on ajoute les arêtes
        for(Map.Entry<TypeDeclaration, Map<String, Integer>> entry : classMap.entrySet()) {

            //get the last part of string (the class name) after the last dot
            som1 = entry.getKey().resolveBinding().getQualifiedName().substring(entry.getKey().resolveBinding().getQualifiedName().lastIndexOf(".")+1);

            for(Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                som2 = entry2.getKey();

                DefaultWeightedEdge e;

                //vérifier si on à pas déjà l'arête dans l'autre sens
                if (!graph.containsEdge(som1, som2) || !graph.containsEdge(som2, som1)) {
                    System.out.println("Adding edge between " + som1 + " and " + som2);
                    e = graph.addEdge(som1, som2);
                    graph.setEdgeWeight(e, (double) entry2.getValue() /totalCouplage);
                }
                else {
                    System.out.println("Edge between " + som1 + " and " + som2 + " already exists");
                    //sinon l'a déjà on incrémente juste le poids de l'arête
                    e = graph.getEdge(som2, som1);
                    graph.setEdgeWeight(e,graph.getEdgeWeight(graph.getEdge(som2, som1)) + (double) entry2.getValue() /totalCouplage);
                }
            }
        }
        //print graph edges with weights
        System.out.println("\n---------\nGraph : \n");
        for(DefaultWeightedEdge edge : graph.edgeSet()) {
            System.out.println(edge.toString() + " (" + graph.getEdgeWeight(edge) + ")");
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
}
