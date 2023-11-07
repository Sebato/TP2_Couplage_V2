package org.example.main;

import org.example.process.Analyzer;
import org.example.structural.CallGraph;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        Analyzer analyzer;
        CallGraph cg = new CallGraph();

        Scanner sc = new Scanner(System.in);
        System.out.println("veuillez entrer le chemin du projet que vous souhaitez analyser : ");
        String path = sc.nextLine();

        if (path.isEmpty()) {
            analyzer = new Analyzer(cg); //default path in ProjectHandler.java
        }else {
            analyzer = new Analyzer(cg,path);
        }

        analyzer.processAll();
        cg.allCouplages(false);
        cg.createGraph();

        System.out.println("Cluster hierarchique généré : \n"+cg.clusturing());

        System.out.println("\n----------\nVeuillez entrer un nom pour le nouveau fichier .dot généré : ");
        String name = sc.nextLine();
        String outPath;

        if (name.isEmpty()) {
            outPath = System.getProperty("user.dir")+"/Graphes/g.dot";
        }else {
            outPath = System.getProperty("user.dir") + "/Graphes/" + name + ".dot";
        }
        cg.exportGraphToDot(outPath);


    }
}