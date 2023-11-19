package org.example.spoon.main;

import org.example.spoon.process.DendroBuilder;
import org.example.spoon.structural.CallGraph2;
import org.example.spoon.structural.Dendrogram;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;

import java.util.Scanner;

public class SpoonMain {
    public static void Spoonmain(String[] args) {
        //disable log4j strange warnings
        System.setProperty("org.apache.logging.log4j.simplelog.StatusLogger.level", "OFF");

        String projectPath;

        Scanner sc = new Scanner(System.in);
        System.out.println("veuillez entrer le chemin du projet que vous souhaitez analyser :" +
                        "\n(ne rien mettre pour analyser l'application actuele)");
        projectPath = sc.nextLine();

        if (projectPath.isEmpty()) {
            projectPath = System.getProperty("user.dir")+"/src";//default projectPath in ProjectHandler.java
        }

        SpoonAPI spoon = new Launcher();
        spoon.addInputResource(projectPath);
        CtModel model = spoon.buildModel();

        CallGraph2 c = new CallGraph2();
        c.buildMap(model);
        c.buildGraph();

        //Question 1 :
        //export the graph to a .dot file
        System.out.println("\n----------\nVeuillez entrer un nom pour le nouveau fichier .dot généré : ");
        String name = sc.nextLine();
        String outPath;

        if (name.isEmpty()) {
            outPath = System.getProperty("user.dir")+"/Graphes/g.dot";
        }else {
            outPath = System.getProperty("user.dir") + "/Graphes/" + name + ".dot";
        }

        SimpleWeightedGraph<CtType<?>, DefaultWeightedEdge> graph = c.graph;
        CallGraph2.exportGraphToDot(graph, outPath);



        //Question 2 :
        DendroBuilder builder = new DendroBuilder(c);
        System.out.println("\n----------\nclustering en cours");

        Dendrogram dendro = builder.buildCluster();

        System.out.println("Dendrogramme généré: \n" + dendro.toString());

        double moy;
        do {
            System.out.println("\n----------\nVeuillez entrer la moyenne de couplage minimum de chaque module:\n" +
                    "doit être compris entre 0 et 1 (la virgule est le séparateur décimal)");
            moy = sc.nextDouble();
        }while (moy>1 || moy < 0 );

        for (Dendrogram d : builder.identifyModules(moy)){
            System.out.println(d.toString()+"\nmoyenne de couplage = "+builder.calcMoyCouplage(d));
            System.out.println("----------");
        }

    }
}