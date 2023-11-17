package org.example.eclipsejdt.main;

import org.example.eclipsejdt.process.Analyzer;
import org.example.eclipsejdt.structural.CallGraph;

import java.io.IOException;
import java.util.Scanner;

public class EclipseJDTMain {
    public void eclipseJDTmain(String[] args) throws IOException {

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


        //boucler si utilisateur donne une valeur > 1
        double moy;
        do {
            System.out.println("\n----------\nVeuillez entrer la moyenne de couplage minimum de chaque module:\n" +
                    "doit être compris entre 0 et 1 (la virgule est le séparateur décimal)");
            moy = sc.nextDouble();
        }while (moy>1 || moy < 0 );

        cg.moduleIdentifier(moy, cg.clusterList.getFirst());

        System.out.println("les modules identifiés avec une moyenne de couplage > "+moy+" sont les suivants:\n");

        cg.displayModules();
    }
}