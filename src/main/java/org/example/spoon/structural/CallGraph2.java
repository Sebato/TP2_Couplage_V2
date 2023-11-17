package org.example.spoon.structural;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.ExportException;
import org.jgrapht.nio.dot.DOTExporter;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.support.reflect.declaration.CtNamedElementImpl;
import spoon.support.reflect.declaration.CtTypeImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class CallGraph2 {
    public Map<CtType<?>, Map<CtType<?>, Integer>> couplages = new HashMap<>();
    public SimpleWeightedGraph<CtType<?>, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    public int totalCouplages = 0;

    public CallGraph2() {
    }

    //Algorithm :
    //for all classes c in the model :
    //      for all method m in c :
    //          for all methods mi called by m :
    //              get the class c2 of mi
    //              if c2 is in the model :
    //                  if c2 is not in the map of couplages of c :
    //                      add c2 to the map of couplages of c
    //                      set the value of the couplage between c and c2 to 1
    //                  else :
    //                      increment the value of the couplage between c and c2
    //                  increment the total number of couplages


    public void buildMap( CtModel model){
        for (CtType<?> c : model.getAllTypes()) {

            couplages.put(c, new HashMap<>());
            List<CtMethod<?>> methods = c.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtMethod.class));

            for (CtMethod<?> m : methods) {
                List<CtInvocation<?>> methodsInvocations = m.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtInvocation.class));

                for (CtInvocation<?> mi : methodsInvocations) {

                    //pour éviter les appels à des méthodes que spoon arrive pas à résoudre
                    // (pas encore cerné complêtement le problème mais ça ne concerne pas les classes du projet de toute façon)
                    if (mi.getExecutable().getDeclaringType() != null && mi.getExecutable().getDeclaringType().getDeclaration() != null) {

                        CtType<?> c2 = mi.getExecutable().getDeclaringType().getDeclaration();

                        //if the method is called from a class in the model
                        if (c2 != null && c2 != c && model.getAllTypes().contains(c2)) {

                            if (couplages.get(c).get(c2) == null) {
                                couplages.get(c).put(c2, 1);
                            } else {
                                couplages.get(c).put(c2, couplages.get(c).get(c2) + 1);
                            }
                            totalCouplages++;
                        }
                    }
                }
            }
        }
    }

    public void buildGraph() {

        for (CtType<?> c : couplages.keySet()) {
            graph.addVertex(c);
        }

        for (CtType<?> c : couplages.keySet()) {
            for (CtType<?> c2 : couplages.get(c).keySet()) {
                //avoid duplicates and self-loops
                if (c == c2) continue;
                if (graph.getEdge(c2, c) == null) {
                    if (graph.getEdge(c, c2) == null) {
                        DefaultWeightedEdge e = graph.addEdge(c, c2);
                        graph.setEdgeWeight(e, (double) (couplages.get(c).get(c2)) / totalCouplages);
                    } else {
                        DefaultWeightedEdge e = graph.getEdge(c, c2);
                        graph.setEdgeWeight(e, graph.getEdgeWeight(e) + (double) couplages.get(c).get(c2) / totalCouplages);
                    }
                }
            }
        }
    }

    //export the graph to a .dot file
    public static void exportGraphToDot(SimpleWeightedGraph weightedGraph, String outputPath) {
        DOTExporter<CtTypeImpl, DefaultWeightedEdge> exporter = new DOTExporter<>();

        //set vertex id with lambdas BECAUSE THEY THOUGHT IT WAS A GOOD IDEA
        exporter.setVertexIdProvider(CtNamedElementImpl::getSimpleName);

        //make sure the WEIGHTS are printed
        exporter.setEdgeAttributeProvider((e) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("weight", DefaultAttribute.createAttribute(weightedGraph.getEdgeWeight(e)));
            map.put("label", DefaultAttribute.createAttribute(new DecimalFormat("#.##").format(weightedGraph.getEdgeWeight(e))));
            return map;
        });

        try {
            FileWriter fileWriter = new FileWriter(outputPath);
            exporter.exportGraph(weightedGraph, fileWriter);

            System.out.println("\n----------Graphe exporté ici : " + outputPath);
            System.out.println("\n Pour en générer un fichier PDF, tapez la commande suivante " +
                    "dans un terminal ouvert à l'endroit où vous souhaitez l'enregistrer : " +
                    "\ndot -Tpdf "+outputPath+" -o <leNomDeVotreGraphe>.pdf\n");

            fileWriter.close();
        } catch (ExportException | IOException e) {
            e.printStackTrace();
        }
    }

    public double getCouplages(List<String> composants) {
        if (composants.size() == 1) return 0;

        double couplages = 0;
        double couplageTmp = 0;

        for (String c1 : composants) {
            for (String c2 : composants) {
                if (Objects.equals(c1, c2)) continue;

                if (this.couplages.get(getCtTypeNode(c1)) != null &&
                    this.couplages.get(getCtTypeNode(c1)).get(getCtTypeNode(c2)) != null) {
                    couplageTmp += this.couplages.get(getCtTypeNode(c1)).get(getCtTypeNode(c2));
                }

                if (this.couplages.get(getCtTypeNode(c2)) != null &&
                    this.couplages.get(getCtTypeNode(c2)).get(getCtTypeNode(c1)) != null) {
                    couplageTmp += this.couplages.get(getCtTypeNode(c2)).get(getCtTypeNode(c1));
                }
                couplageTmp /= totalCouplages;
                couplages += couplageTmp;
            }
        }
        return couplages / composants.size();
    }

    public CtType<?> getCtTypeNode(String s) {
        for (CtType<?> c : this.graph.vertexSet()) {
            if (c.getQualifiedName().equals(s)) {
                return c;
            }
        }
        return null;
    }
}
