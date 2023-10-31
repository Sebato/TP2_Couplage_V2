package org.example.structural;

import org.eclipse.jdt.core.dom.*;
import org.example.visitors.MethodDeclarationVisitor;

import javax.print.attribute.standard.Finishings;
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
    public void addClassNode(TypeDeclaration Cnode) {
        if (!classNodes.containsKey(Cnode)) {
            classNodes.put(Cnode, new HashMap<>());
        }else System.out.println("Class "+ Cnode.getName() +" is already in the ClassMap");
    }

    //après initialisation des classes, on cherche les couplages entre elles
    public void findCouplages(){
        //pour chaque classe
        for (TypeDeclaration c : this.classNodes.keySet()){

            //pour chaque methode de cette classe
            for (MethodDeclaration m : c.getMethods()){

                //on visite la methode pour trouver tous les appels à d'autres méthodes
                MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
                m.accept(methodDeclarationVisitor);

                //pour toutes les methodes invoquées, on ajoute un couplage entre la classe courante et la classe invoquée
                for (MethodInvocation mi : methodDeclarationVisitor.getMethodsCalled()){

                    System.out.println("\nMethod " + m.getName().getFullyQualifiedName() +
                            " calls " + mi.getName().getFullyQualifiedName());

                    System.out.println(mi);
                    System.out.println(mi.getExpression());
                    System.out.println(mi.getExpression().resolveTypeBinding());

                    if (mi.resolveMethodBinding() != null) {
                        System.out.println(" from " + mi.resolveMethodBinding().getDeclaringClass().getName());
                        addCouplage(c, (TypeDeclaration) mi.resolveMethodBinding().getDeclaringClass());
                    }
                }
            }
        }
    }

    private void addCouplage(TypeDeclaration c, TypeDeclaration c2) {
        //on n'ajoute pas de couplage réflexif
        if (c != c2){

            //si la classe C n'est pas deja dans la map on l'ajoute
            // (mais c'est bizarre si elle y est pas)
            if (!classNodes.containsKey(c))
                addClassNode(c);

            //pareil pour C2
            if (!classNodes.containsKey(c2))
                addClassNode(c2);

            //si la classe C2 n'est pas deja dans la map de la classe C on l'ajoute
            if (!classNodes.get(c).containsKey(c2))
                classNodes.get(c).put(c2, 1);
            else //sinon on incrémente le nombre d'appel
                classNodes.get(c).put(c2, classNodes.get(c).get(c2)+1);
        }
    }

//    public int calcCouplage(String c1, String c2){
//        int couplage =0;
//        //Add nb appelle method c1 from c2
//        if (classNodes.containsKey(c1))
//            if (classNodes.get(c1).containsKey(c2))
//                couplage += classNodes.get(c1).get(c2);
//
//        //Add nb appelle method c2 from c1
//        if (classNodes.containsKey(c2))
//            if (classNodes.get(c2).containsKey(c1))
//                couplage += classNodes.get(c2).get(c1);
//
//        return couplage/totalCouplage;
//    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CallGraph{");
        for (Map.Entry<TypeDeclaration, Map<TypeDeclaration, Integer>> entry : classNodes.entrySet()) {
            sb.append(entry.getKey().resolveBinding().getQualifiedName()).append(" -> ");
            for (Map.Entry<TypeDeclaration, Integer> entry2 : entry.getValue().entrySet()) {
                sb.append(entry2.getKey().resolveBinding().getQualifiedName()).append(" (").append(entry2.getValue()).append("), ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String ResolveBinding(MethodInvocation mi){
        String result = ""; //pour stocker le retour
        ITypeBinding typeBinding; // our tester le type de l'expression au fur et à mesure

        Expression exp = mi.getExpression(); // la partie avant le point

        String calleeFullName = "";

        typeBinding = exp.resolveTypeBinding();
        if (typeBinding != null) {
            calleeFullName = typeBinding.getQualifiedName() + "." + mi.getName().getFullyQualifiedName();
        } else {
            calleeFullName = mi.getName().getFullyQualifiedName();
        }
        return calleeFullName;
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
