package org.example.structural;

import org.eclipse.jdt.core.dom.*;
import org.example.visitors.MethodDeclarationVisitor;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.HashMap;
import java.util.Map;

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
//    à n’importe quelles deux classesde l’application analysée

    public int calcCouplage(String c1, String c2){
        int couplage = 0;

        Map<String, Integer> c1Calls = new HashMap<>();
        Map<String, Integer> c2Calls = new HashMap<>();

        for (Map.Entry<TypeDeclaration, Map<String, Integer>> entry : classMap.entrySet()) {
            if (entry.getKey().getName().toString().equals(c1))
                c1Calls = entry.getValue();
            if (entry.getKey().getName().toString().equals(c2))
                c2Calls = entry.getValue();
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
    return couplage;
}

    public void allCouplages(){
        System.out.println("\n---------\nCouplage Total de l'application : "+totalCouplage+"\n");
        for (Map.Entry<TypeDeclaration, Map<String, Integer>> entry : classMap.entrySet()) {
            for (Map.Entry<TypeDeclaration, Map<String, Integer>> entry2 : classMap.entrySet()) {
                if (!entry.getKey().getName().toString().equals(entry2.getKey().getName().toString())) {
                    System.out.println("Couplage entre " + entry.getKey().getName().toString() + " et " + entry2.getKey().getName().toString() + " : " + calcCouplage(entry.getKey().getName().toString(), entry2.getKey().getName().toString()));
                }
            }
        }
    }


    public void printCoupling() {
        System.out.println("\n---------\nCouplages : \n");
        for(Map.Entry<TypeDeclaration, Map<String, Integer>> entry : classMap.entrySet()) {
            System.out.println(entry.getKey().resolveBinding().getQualifiedName() + " -> ");
            for(Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                System.out.println("\t" + entry2.getKey() + " (" + entry2.getValue() + ")");
            }
        }
    }

    //using jgrapht to create the Weighted graph
    public void createGraph(){
        System.out.println("\n---------\nCreating Weighted Graph : \n");

        String som1;
        String som2;

        //pour chaque classe on ajoute un sommet au graphe
        for(Map.Entry<TypeDeclaration, Map<String, Integer>> entry : classMap.entrySet()) {
            graph.addVertex(entry.getKey().resolveBinding().getQualifiedName());
        }

        //une fois que tous les sommets sont ajoutés, on ajoute les arêtes
        for(Map.Entry<TypeDeclaration, Map<String, Integer>> entry : classMap.entrySet()) {
            som1 = entry.getKey().resolveBinding().getQualifiedName();

            for(Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                som2 = entry2.getKey();
                DefaultWeightedEdge e;

                //vérifier si on à pas déjà l'arête dans l'autre sens
                if (!graph.containsEdge(som2, som1)) {
                     e = graph.addEdge(som1, som2);
                    graph.setEdgeWeight(e, entry2.getValue());
                }
                else {
                    //sion l'a déjà on incrémente juste le poids de l'arête
                    e = graph.getEdge(som2, som1);
                    graph.setEdgeWeight(e,graph.getEdgeWeight(graph.getEdge(som2, som1)) + entry2.getValue());
                }
            }
        }
    }


/*    public MutableGraph toMutableGraph(String name) throws IOException {
        MutableGraph graph = Factory.mutGraph("callGraph").setDirected(true);
        for (Map.Entry<MethodDeclaration, List<String>> entry : nodes.entrySet()) {
            String caller = entry.getKey().resolveBinding().getDeclaringClass().getName() + "." + entry.getKey().getName();
            graph.add(Factory.mutNode(caller));
            for(String calledMethod : entry.getValue()){
                graph.add(Factory.mutNode(calledMethod));
                graph.add(Factory.mutNode(caller).addLink(Factory.mutNode(calledMethod)));
            }
        }

        Graphviz viz = Graphviz.fromGraph(graph);

        viz.render(Format.SVG).toFile(new File("Graphs/"+name+".svg"));
        viz.rasterize(Rasterizer.builtIn("pdf")).toFile(new File("Graphs/"+name+".pdf"));
        return graph;
    }*/

}
