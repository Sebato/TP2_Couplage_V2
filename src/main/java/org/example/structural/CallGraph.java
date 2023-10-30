package org.example.structural;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.example.visitors.MethodDeclarationVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallGraph {

    //chaque classe sera associée à une map qui contiendra les classes qu'elle appelle et le nombre d'appel à ces dernieres
    private Map<TypeDeclaration,Map<TypeDeclaration, Integer>> classNodes = new HashMap<>();


    public CallGraph() {
        classNodes = new HashMap<>();
    }

    //A chaque ajout de classe on crée une nouvelle map qui lui est associée
    public void addClassNode(TypeDeclaration clazz) {
        classNodes.put(clazz, new HashMap<>());
    }

    //chercher les couplages avec les autres classes
    public static void findCouplage(TypeDeclaration c){
        //pour chaque methode de la classe
            for (MethodDeclaration m : c.getMethods()){
                //on visite la methode
                MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
                m.accept(methodDeclarationVisitor);
                //on recupere les methodes appelees
                List<MethodDeclaration> methods = methodDeclarationVisitor.getMethods();
                //pour chaque methode appelee
                for (MethodDeclaration m2 : methods){
                    //on recupere la classe a laquelle elle appartient
                    TypeDeclaration c2 = (TypeDeclaration) m2.getParent();
                    //on ajoute le couplage
                    addCouplage(c, c2);
                }
            }
    }

    public int calcCouplage(String c1, String c2){
        int couplage =0;
        //Add nb appelle method c1 from c2
        if (classNodes.containsKey(c1))
            if (classNodes.get(c1).containsKey(c2))
                couplage += classNodes.get(c1).get(c2);

        //Add nb appelle method c2 from c1
        if (classNodes.containsKey(c2))
            if (classNodes.get(c2).containsKey(c1))
                couplage += classNodes.get(c2).get(c1);

        return couplage/totalCouplage;
    }
//    public void addClassNode(TypeDeclaration clazz, Map<TypeDeclaration, Integer> nodes) {
//        ClassNodes.put(clazz, nodes);

//    }
    public void addNode(MethodDeclaration method, List<String> calledMethods) {
        nodes.put(method, calledMethods);
    }

    public Map<MethodDeclaration, List<String>> getNodes() {
        return nodes;
    }

    public CallGraph merge(CallGraph sousGraphe) {
        nodes.putAll(sousGraphe.getNodes());

        return this;
    }























    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CallGraph{");
        for (Map.Entry<MethodDeclaration, List<String>> entry : nodes.entrySet()) {
            sb.append("\n\t");
            sb.append(entry.getKey().resolveBinding().getDeclaringClass().getName());
            sb.append(".");
            sb.append(entry.getKey().getName());
            sb.append(" -> ");
            sb.append(entry.getValue());
        }
        sb.append("\n}");
        return sb.toString();
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
