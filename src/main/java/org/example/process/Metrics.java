//package org.example.process;
//
//
//import org.eclipse.jdt.core.dom.TypeDeclaration;
//
//import java.io.IOException;
//
//public class Metrics {
//
///*Couplage (A,B) = Nombre de relations (relation = appel)
//entre les couples de méthodes appartenant respectivement
//aux deux classes en question
//(A.mi et B.mj) / nombre de toutes  les  relations (binaire)
//entre  les  couples  de  méthodes  appartenant respectivement
//à n’importe quelles deux classes de l’application analysée.*/
//
//
//    public int calcCouplage(String classA, String classB) throws IOException {
//        if (classA == null || classB == null) {
//            throw new IllegalArgumentException("Class name cannot be null");
//        }
//
//        Analyzer analyzer = new Analyzer();
//        analyzer.visitProject();
//
//        TypeDeclaration cA = analyzer.getClassByName(classA);
//        TypeDeclaration cB = analyzer.getClassByName(classB);
//
//        if (cA == null || cB == null) {
//            throw new IllegalArgumentException("Class not found");
//        }
//
//        //nombre de fois ou des methodes de A sont appelées par des methodes de B et inversement
//        int nbRelationsAB = 0;
//
//        //nombre de fois ou des methodes d'une classe C1 apelle une classe C2 du projet
//        int nbRelationsBin = 0;
//        //nécessite graphe entier
//
//        //besoin de savoir quelle méthode est dans quelle classe facilement
//
//        //Construction du graphe d'appel entier
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        return 0;
//
//    }
//
//
//
//}
