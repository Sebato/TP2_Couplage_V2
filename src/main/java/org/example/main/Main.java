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

        //processAll() va :
        //   - chercher tous les fichiers .java du projet, les parser et
        //   les stocker
        //   - initialiser une Map contenant tous les couplages entre les classes
        //   pour pouvoir créer le Graphe d'appel par la suite
        analyzer.processAll();

        //afficher tous les couplages trouvés
        //cg.allCouplages(false);

        //createGraph() va :
        // créer le graphe à partir de la Map de couplages
        cg.createGraph();


        System.out.println("\n----------\nVeuillez entrer un nom pour le nouveau fichier .dot généré : ");
        String name = sc.nextLine();
        String outPath;

        if (name.isEmpty()) {
            outPath = System.getProperty("user.dir")+"/Graphes/g.dot";
        }else {
            outPath = System.getProperty("user.dir") + "/Graphes/" + name + ".dot";
        }
        //exportGraphToDot() va :
        //   - créer un fichier .dot à partir du graphe à l'emplacement spécifié
        cg.exportGraphToDot(outPath);

        System.out.println("\n----------\nappuyez sur entrée pour passer au clustering");
        sc.nextLine();

        //clustering() va :
        //   - créer un cluster hierarchique à partir du graphe
        cg.clustering();

    }
}