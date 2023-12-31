package org.example.eclipsejdt.structural;

import org.apache.commons.lang3.tuple.*;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.example.eclipsejdt.visitors.MethodDeclarationVisitor;
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

import static org.example.eclipsejdt.structural.ClusterStruct.ComparatorCouplage;

public class CallGraph {

    //le nom de chaque classe parsée sera associé à une map qui contiendra : les noms des classes qu'elle appelle et le nombre d'appel à ces dernieres
    private final Map<String,Map<String, Integer>> classMap;
    private int totalCouplage = 0;

    //structure pour le graphe de couplage pondéré
    public SimpleWeightedGraph<String, DefaultWeightedEdge> weightedGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    //structure de transition entre le graphe de couplage pondéré et le cluster hierarchique
    public SimpleWeightedGraph<String, DefaultWeightedEdge> clusterGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    //pour la structure du cluster hierarchique
    public ArrayList<ClusterStruct> clusterList = new ArrayList<>();

    //Constrcuteur
    public CallGraph() {
        classMap = new HashMap<>();
    }

    //getters
    public int getTotalCouplage() {
        return totalCouplage;
    }

    //modifiers

    //A chaque ajout de classe on lui associe une nouvelle map
    public void addClassKey(String Ckey) {
        if(Ckey.length()<=3) System.err.println("Attention, le nom de la classe est tres court : "+Ckey
                +"\nDes erreurs peuvent survenir lors de l'analyse de ce projet");

        if (!classMap.containsKey(Ckey)) {
            classMap.put(Ckey, new HashMap<>());
        }
    }

    //après initialisation des classes, on cherche les couplages entre elles
    public void findCouplages(TypeDeclaration c){

        //pour chaque methode de cette classe
        for (MethodDeclaration m : c.getMethods()){

            //on visite la methode pour trouver tous les appels à d'autres méthodes
            MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
            m.accept(methodDeclarationVisitor);

            //pour toutes les methodes invoquées, on ajoute un couplage entre la classe courante et la classe invoquée
            for (MethodInvocation mi : methodDeclarationVisitor.getMethodsCalled()){

                if (mi.getExpression() != null) { // c'est null quand appel à this implicite par exemple ou appel à une méthode statique

                    //ATTENTION POSSIBILITÉ D'ÉRREUR D'EXECUTION :
                    // bug constaté dans la librairie :
                    // Si une classe à un nom de taille 1 (ex: mi.getExpression() renvoie une classe "A")
                    // le resolveTypeBinding() retourne null (très embêtant oui)

                    if (mi.getExpression().resolveTypeBinding() != null) {
                        String invokedMethodName = mi.getExpression().resolveTypeBinding().getName();

                        //Autre Petite protection :
                        // si la classe invoquée n'est pas dans le projet on ne l'ajoute pas
                        // du genre System.out.println, Iterator, List.contains()...
                        // ou les appels a une Interface, Visitor ou autre

                        if (classMap.containsKey(invokedMethodName)) {
                            addCouplage(c.getName().getFullyQualifiedName(), invokedMethodName);
                        }
                    }
                    //si le resolveTypeBinding() est null c'est peut-être un appel à une Méthode statique
                    //on doit récupérer l'expression et vérifier si elle est bien une classe du projet
                    else if (mi.getExpression().toString().length() > 1) {

                        //on récupère l'expression
                        String invokedMethodName = mi.getExpression().toString();

                        //si l'expression est une classe du projet on ajoute le couplage
                        if (classMap.containsKey(invokedMethodName)) {
                            addCouplage(c.getName().getFullyQualifiedName(), invokedMethodName);
                        }
                    }
                }
            }
        }
    }

    // ajoute un couplage entre la classe C et la classe C2
    // et incrémente le couplage total
    private void addCouplage(String c, String c2) {

        //on n'ajoute pas de couplage réflexif
        if (!c2.equals(c)){

            //si la classe C n'est pas deja dans la map on l'ajoute
            // (mais ce serait extrêmement bizarre si elle y est pas)
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

    public float getCouplage(String c1, String c2){
        float couplage = 0;

        //si les deux classes appartiennent bien au projet
        if (classMap.containsKey(c1) && classMap.containsKey(c2)) {

            //on prend la valeur coté c1
            if (classMap.get(c1).containsKey(c2))
                couplage += (float) classMap.get(c1).get(c2);

            //et on ajoute la valeur coté c2
            if (classMap.get(c2).containsKey(c1))
                couplage += (float) classMap.get(c2).get(c1);

            //on divise par le nombre total de couplages
            couplage /= totalCouplage;

            //et voila !
            return couplage;
        }
        return 0;
    }

    public double getMoyCouplage(List<String> classes){
        if (classes.size()<2) return 0;
        float moy = 0;
        int nb = 0;

        //pour chaque classe de la liste
        for (String c : classes){

            //on récupère la moyenne de couplage avec toutes les autres classes
            for (String c2 : classes){
                moy += getCouplage(c,c2);
                nb++;
            }
        }
        return moy/nb;
    }

    //Methode de création du graphe pondéré à partir de la map de couplage
    public void createGraph(){

        //si la map est vide on ne peut pas créer le graphe
        if (classMap.isEmpty()) {
            System.err.println("La map de couplage est vide, impossible de créer le graphe");
            return;
        }

        //les noms des sommets/classes qu'on va manipuler au fur et à mesure
        String som1;
        String som2;

        //pour chaque classe on ajoute un sommet au graphe
        for(Map.Entry<String, Map<String, Integer>> entry : classMap.entrySet()) {

            //ATTENTION POSSIBILITE D'ERREUR D'EXECUTION :
            // on ne prend que le nom de la classe et pas le package
            // sinon on ne peut pas éviter les doublons d'arêtes (A,B) et (B,A)
            // donc on ne pourra pas gérer des projets avec des classes rangées comme suit :
            //      org.version1.AClass.java
            //      org.version2.AClass.java
            // De toute façon le resolveTypeBinding() plus haut ne différenciera pas les deux classes (?) :-/

            weightedGraph.addVertex(entry.getKey().substring(entry.getKey().lastIndexOf(".")+1));

        }

        //une fois que tous les sommets sont ajoutés, on ajoute les arêtes
        for(Map.Entry<String, Map<String, Integer>> entry : classMap.entrySet()) {

            som1 = entry.getKey();

            for(Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                som2 = entry2.getKey();

                DefaultWeightedEdge e;

                //vérifier si on à pas déjà l'arête dans l'autre sens
                if (!weightedGraph.containsEdge(som1, som2) || !weightedGraph.containsEdge(som2, som1)) {
                    e = weightedGraph.addEdge(som1, som2);
                    weightedGraph.setEdgeWeight(e, (double) entry2.getValue() /totalCouplage);
                }
                else {
                    //sinon si on l'a déjà, on incrémente juste le poids de l'arête
                    e = weightedGraph.getEdge(som2, som1);
                    weightedGraph.setEdgeWeight(e, weightedGraph.getEdgeWeight(weightedGraph.getEdge(som2, som1)) +
                            (double) entry2.getValue() /totalCouplage);
                }
            }
        }
    }





    //modifie le clusterGraph pour transférer les arêtes au nouveau cluster fusionné
    //et renvoie la liste des arêtes à supprimer
    private Collection<DefaultWeightedEdge> resolveNewEdges(String newCluster, String nodeL, String nodeR) {

        List<DefaultWeightedEdge> edgesToRemove = new ArrayList<>();

        for (DefaultWeightedEdge e : clusterGraph.edgeSet().stream().toList()){

            //si source d'une arête = nodeL ou nodeR, et que target != l'autre
            // on la remplace la source par newCluster
            if((clusterGraph.getEdgeSource(e).equals(nodeL) && !clusterGraph.getEdgeTarget(e).equals(nodeR)) ||
                    (clusterGraph.getEdgeSource(e).equals(nodeR) && !clusterGraph.getEdgeTarget(e).equals(nodeL))){

                //si on a pas déjà créé une arête entre newCluster et le target de l'arête
                if(!clusterGraph.containsEdge(newCluster, clusterGraph.getEdgeTarget(e))){

                    //on ajoute une arête entre newCluster et le target de l'arête
                    clusterGraph.addEdge(newCluster, clusterGraph.getEdgeTarget(e));
                    //on oublie PAS de rajouter le poids
                    clusterGraph.setEdgeWeight(clusterGraph.getEdge(newCluster, clusterGraph.getEdgeTarget(e)),
                            clusterGraph.getEdgeWeight(e));

                }
                //sinon on incrémente le poids de l'arête
                else{
                    clusterGraph.setEdgeWeight(clusterGraph.getEdge(newCluster, clusterGraph.getEdgeTarget(e)),
                            clusterGraph.getEdgeWeight(clusterGraph.getEdge(newCluster, clusterGraph.getEdgeTarget(e)))
                                    + clusterGraph.getEdgeWeight(e));
                }
                edgesToRemove.add(e);
            }

            //idem pour target = nodeL ou nodeR, avec source != l'autre
            else if((clusterGraph.getEdgeTarget(e).equals(nodeL) && !clusterGraph.getEdgeSource(e).equals(nodeR)) ||
                    (clusterGraph.getEdgeTarget(e).equals(nodeR) && !clusterGraph.getEdgeSource(e).equals(nodeL))){

                //si on a pas déjà une arête entre newCluster et le source de l'arête
                if(!clusterGraph.containsEdge(clusterGraph.getEdgeSource(e),newCluster)){

                    //on ajoute une arête entre newCluster et le source de l'arête
                    clusterGraph.addEdge(clusterGraph.getEdgeSource(e),newCluster);
                    //on oublie pas de rajouter le poids encore une fois
                    clusterGraph.setEdgeWeight(clusterGraph.getEdge(clusterGraph.getEdgeSource(e),newCluster),
                            clusterGraph.getEdgeWeight(e));
                }
                //sinon on incrémente le poids de l'arête
                else {
                    clusterGraph.setEdgeWeight(clusterGraph.getEdge(clusterGraph.getEdgeSource(e), newCluster),
                            clusterGraph.getEdgeWeight(clusterGraph.getEdge(clusterGraph.getEdgeSource(e), newCluster))
                                    + clusterGraph.getEdgeWeight(e));
                }
                edgesToRemove.add(e);
            }
        }
        //comme on itère sur la collection des arêtes, mieux vaut ne peut pas les supprimer directement
        return edgesToRemove;
    }

    //Methode de création du cluster hierarchique à partir du graphe pondéré
    public void clustering(){

        //si graphe pondéré vide on ne peut pas créer le cluster
        if (weightedGraph.vertexSet().isEmpty()) {
            System.err.println("Le graphe pondéré est vide, impossible de créer le cluster");
            return;
        }

        //bon... la méthode est un peu lourde mais c'est plus pratique à manipuler,
        // à voir si les gros projets tiennent le coup:

        List<String> notDoneYet = new ArrayList<>();

        //on recupère toutes les classes/sommets du graphe pondéré
        for(String c : weightedGraph.vertexSet()){
            clusterGraph.addVertex(c);
            notDoneYet.add(c);
        }

        //on recupère toutes les arêtes (avec les poids!!) du graphe pondéré
        for (DefaultWeightedEdge e : weightedGraph.edgeSet().stream().toList()){
            clusterGraph.addEdge(weightedGraph.getEdgeSource(e), weightedGraph.getEdgeTarget(e));
            clusterGraph.setEdgeWeight(clusterGraph.getEdge(weightedGraph.getEdgeSource(e), weightedGraph.getEdgeTarget(e)),
                    weightedGraph.getEdgeWeight(e));
        }

        //on initialise la variable de boucle qui va nous permettre de sortir de la boucle prématurément si besoin
        boolean stop = false;

        //tant qu'on peut regrouper des clusters
        while(clusterGraph.vertexSet().size()>1 && !stop) {

            //on récupère les classes de l'arête avec la plus forte pondération
            Pair<String, String> fuseNodes = clusterProches();

            String nodeL;
            String nodeR;

            //si il n'y a plus d'arêtes
            if (fuseNodes == null) {

                //on ajoute les classes restantes à clusterList
                //ajouter tous les sommets restants dans la liste des clusters
                for (String s : notDoneYet){
                    clusterList.add(new ClusterStruct(s));
                }

                //initialiser la valeur moycouplage de chaque cluster
                for(ClusterStruct cs : clusterList){
                    calcMoyCoup(cs);
                }

                //trier les clusters dans l'ordre décroissant (assure que les classes isolées sont derrière)
                clusterList.sort(ComparatorCouplage);
                Collections.reverse(clusterList);

                //on va dire de fusionner les deux premiers éléments de la ClusterList
                nodeL = clusterList.get(0).cluster;
                nodeR = clusterList.get(1).cluster;

                //hoping it works
            }else {

                nodeL = fuseNodes.getLeft();
                nodeR = fuseNodes.getRight();
            }

            //on crée le nom du noeud qui représentera ce regroupement
            String newCluster = "(" + nodeL + " , " + nodeR + ")";

            //on l'ajoute au graphe
            clusterGraph.addVertex(newCluster);

            //on change de source/target les aretes qui étaient liées aux précédentes deux classes
            // et on recupère les arêtes à supprimer
            ArrayList<DefaultWeightedEdge> rem = new ArrayList<>(resolveNewEdges(newCluster, nodeL, nodeR));

            //on supprime les arêtes qui ne servent plus
            for (DefaultWeightedEdge e : rem){
                clusterGraph.removeEdge(e);
            }

            //et les noeuds qui leur étaient associés
            clusterGraph.removeVertex(nodeR);
            clusterGraph.removeVertex(nodeL);

            //on retire aussi les deux classes de la liste des classes à traiter
            notDoneYet.remove(nodeL);
            notDoneYet.remove(nodeR);


            //partie structurelle du cluster

            if (clusterList.isEmpty()){
                clusterList.add(new ClusterStruct(newCluster, nodeL, nodeR));

            }else{

                List<ClusterStruct> ClusToRem = new ArrayList<>();
                ClusterStruct newClust1 = null;
                ClusterStruct newClust2 = null;

                //si nodeL ou nodelR identifie un des cluster dans la liste
                for (ClusterStruct cl : clusterList){

                    if (cl.cluster.equals(nodeL)){
                        newClust1 = new ClusterStruct(newCluster, cl, nodeR);
                        ClusToRem.add(cl);
                    }else if (cl.cluster.equals(nodeR)){
                        newClust2 = new ClusterStruct(newCluster, nodeL, cl);
                        ClusToRem.add(cl);
                    }
                }

                //si on a eu deux correspondances : on fusionne les deux pp
                if (newClust1 != null && newClust2 != null){
                    ClusterStruct newClust = new ClusterStruct(newCluster,newClust1.nodeL, newClust2.nodeR );
                    clusterList.add(newClust);
                }
                //cas si aucune correspondance
                else if (newClust1 == null && newClust2 == null){
                    clusterList.add(new ClusterStruct(newCluster, nodeL, nodeR));
                }
                //Cas si correspondance avec seulement nodeL
                else if (newClust2 == null){
                    clusterList.add(newClust1);
                }
                //Cas si correspondance avec seulement nodeR
                else {
                    clusterList.add(newClust2);
                }

                //on supprime les clusters qui ont été fusionnés
                for (ClusterStruct cluster : ClusToRem) {
                    clusterList.remove(cluster);
                }
            }
        }
        //on affiche le dernier cluster restant de la liste (le plus haut dans la hierarchie normalement
        System.out.println("\n----------\nCluster hierarchique : \n" + clusterList.get(0));

    }

    //Methode qui renvoie les deux classes sur l'arête avec la plus forte pondération et cette valeur
    private Pair<String,String> clusterProches() {
        double maxPond = 0;
        double eWeight;
        String c1 = null, c2 = null;

        //si il n'y a plus d'arêtes on renvoie null
        if (clusterGraph.edgeSet().isEmpty()) return null;

        //on récupère les classes de l'arête avec la plus forte pondération
        for (DefaultWeightedEdge e : clusterGraph.edgeSet().stream().toList()){

            //poids de l'arête en cours
            eWeight = clusterGraph.getEdgeWeight(e);

            //comparaison et mise à jour du max
            if( eWeight > maxPond) {
                maxPond = eWeight;
                c1 = clusterGraph.getEdgeSource(e);
                c2 = clusterGraph.getEdgeTarget(e);
            }
        }
        return new ImmutablePair<>(c1, c2);
    }

    //Méthode d’indentification des groupes de  classes  couplées
    // (services / composants / modules / fonctionnalités)
    // à partir du cluster hierarchique
    // - Une application doit être composée au plus de M/2 modules
    //      (M est le nombre de classes dans l’application).
    // - Chaque module doit contenir uniquement les classes d’une seule branche
    //      du dendrogramme.
    // - La moyenne du couplage de tous les couples de classes du module
    //      doit être supérieure à CP (CP est un paramètre).
    public void moduleIdentifier(double CP, ClusterStruct cs) {

        // on va considérer que les classes qui n'avaient pas de couplage ne peuvent pas être dans un module

        //si le nombre de modules identifiés est inférieur à M/2
        //  si la moyenne de couplage de notre Cluster > à CP
        //      on regarde les moyennes de couplage moy1 et moy2 de ses deux sous clusters s1 et s2
        //          si moy1 > à CP et moy2 > à CP
        //              on recommence l'algo sur s1 et s2.
        //          sinon si moy1 > CP et moy2 == 0 ET s2 non isolée
        //              on lance l'algo sur l'autre individuellement.
        //          sinon si moy2 > CP et moy1 == 0 ET s1 non isolée
        //              on lance l'algo sur l'autre individuellement.
        //          sinon s1 ou s2 aura un couplage interne insuffisant de toute façon
        //              on ajoute le cluster actuel à la liste des clusters identifiés.

        //les Modules déja identifiés seront stockés dans this.clusterList
        //attention on supprime jamais le premier élément qui etait le cluster final !!

        //le nombre de modules identifiés
        int cpt = this.clusterList.size()-1;

        //si on a atteint la taille max on s'arrête
        if (cpt >= weightedGraph.vertexSet().size()/2){
            return;
        }
        //sinon on lance l'algo
        else{
            double moyCoup = calcMoyCoup(cs);

            //si la moyenne de couplage interne du cluster est supérieure à CP
            if (moyCoup > CP){

                //on récupère les deux sous clusters
                ClusterStruct cs1 = cs.nodeL;
                ClusterStruct cs2 = cs.nodeR;

                //on récupère leur moyenne de couplage interne
                double moy1 = calcMoyCoup(cs1);
                double moy2 = calcMoyCoup(cs2);

                //si les deux sont > à CP
                if (moy1 > CP && moy2 > CP){

                    //on lance l'algo sur les deux sous clusters
                    moduleIdentifier(CP, cs1);
                    moduleIdentifier(CP, cs2);

                    //on supprime le cluster actuel des clusters identifiés
                    //attention on ne supprime pas le premier élément qui est le cluster final !!
                    if (!(this.clusterList.get(0).equals(cs))) this.clusterList.remove(cs);
                }
                //si moy1 > CP et moy2 == 0 ET que la deuxième branche est une classe isolée
                else if (moy1 > CP && moy2 == 0 && classMap.get(cs2.cluster) == null){
                    //on on lance l'algo sur le sous cluster 1
                    moduleIdentifier(CP, cs1);
                }
                //sinon si moy2 > CP et moy1 == 0 ET que la deuxième branche est une classe isolée
                else if (moy2 > CP && moy1 == 0 && classMap.get(cs1.cluster) == null){
                    //on on lance l'algo sur le sous cluster 2
                    moduleIdentifier(CP, cs2);
                }
                //sinon un des deux aura un couplage interne insuffisant de toute façon donc on ajoute le cluster actuel
                else{
                    //on ajoute le cluster actuel à la liste des clusters identifiés
                    this.clusterList.add(cs);
                }
            }
        }
    }

    //calcule la moyenne de couplage interne d'un cluster
    private double calcMoyCoup(ClusterStruct cs) {
        double moyCoup = 0;
        if (cs.moycouplage == null) {
            moyCoup = getMoyCouplage(cs.getComposants());
            cs.moycouplage = moyCoup;
            return moyCoup;
        }
        else return cs.moycouplage;
    }


    //METHODE D'EXPORT

    //exporter le graphe en .dot
    public void exportGraphToDot( String outputPath) {
        DOTExporter<String, DefaultWeightedEdge> exporter = new DOTExporter<>();

        //set vertex id with lambdas BECAUSE THEY THOUGHT IT WAS A GOOD IDEA
        exporter.setVertexIdProvider((v) -> v);

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



    //METHODES D'AFFICHAGE

    public void printAllClasses(){
        System.out.println("\n---------\nClasses : \n");
        for(Map.Entry<String, Map<String, Integer>> entry : classMap.entrySet()) {
            System.out.println(entry.getKey());
        }
    }

    // affiche tous les couplages de l'application (displayAll = true)
    // ou seulement ceux qui sont > 0 (displayAll = false)
    public void allCouplages(boolean displayAll){
        System.out.println("\n---------\nCouplage Total de l'application : "+totalCouplage+"\n");

        //on garde l'info des paires de classes déjà affichées
        List<Pair<String, String>> done = new ArrayList<>();
        float couplage;

        for (Map.Entry<String, Map<String, Integer>> entry : classMap.entrySet()) {
            String s1 = entry.getKey();

            for (Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                String s2 = entry2.getKey();

                if (!s1.equals(s2)) {

                    //pour éviter de parcourir deux fois la même paire de classes
                    if (!done.contains(new ImmutablePair<>(s1, s2)) && !done.contains(new ImmutablePair<>(s2, s1))) {

                        couplage = getCouplage(s1, s2);
                        if (displayAll || couplage > 0) {
                            System.out.println("Couplage entre (" + s1 + " et " + s2 + ") = " + couplage);
                        }
                        done.add(new ImmutablePair<>(s1, s2));
                    }
                }
            }
        }
    }

    //Methode d'affichage de la liste des sommets et des arêtes du graphe avec les poids
    public void printGraphe(SimpleWeightedGraph<String, DefaultWeightedEdge> g){
        System.out.println("\n---------\nGraphe : \n");
        System.out.println("sommets : "+g.vertexSet());
        for (DefaultWeightedEdge e : g.edgeSet().stream().toList()){
            System.out.println(g.getEdgeSource(e)+" -> "+g.getEdgeTarget(e)+" : "+g.getEdgeWeight(e));
        }
    }

    //à appeler apres ModuleIdentifier, affiche les clusters dans clusterList à l'exception du premier
    //(sauf si ModuleIdentifier n'a pas pu identifier de modules)
    public void displayModules() {
        if (clusterList.size() > 1) {
            System.out.println("\n----------\nModules identifiés : "+(clusterList.size()-1)+"\n");
            for (int i = 1; i < clusterList.size(); i++) {
                System.out.println(clusterList.get(i)+"\n moyenne de couplage : "+clusterList.get(i).moycouplage+"\n###########################\n");
            }
        }
    }
}
