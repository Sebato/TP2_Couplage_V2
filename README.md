
# TP2 Compréhension Logicielle

Application développée pour répondre aux questions du TP2 de L'UE HAI913I Evolution et restructuration des logiciels
les consignes sont disponibles dans le fichier : TP2-SoftwareComprehension.pdf

## ReadMe :


### Choix de la librairie
Lors du lancement de l'application il est demandé de choisir quelle librairie utilisée tapez une option parmi les suivantes :

    - 1 pour Eclipse JDT
    - 2 pour Spoon

### Choix du projet à analyser
entrez le chemin complet du dossier contenant les sources du projet à analyser, ne rien mettre lance l'analyse du l'application elle-même.

### Graphe de couplage
Une fois le programme lancé il peut être nécessaire de patienter un certain temps en fonction de la taille du projet,
la création du graphe de couplage se lance ensuite automatiquement.

Il sera demandé un nom pour le fichier .dot qui contiendra le graphe de couplage généré

### Export du graphe de couplage en pdf
Une fois le fichier .dot créé, la commande pour l'exporter en pdf sera affichée :

     Pour en générer un fichier PDF, tapez la commande suivante dans un terminal ouvert à l'endroit où vous souhaitez l'enregistrer :  
     dot -Tpdf chemin/vers/le/fichier/dot/<leNomDeVotreFichierDot>.dot -o <leNomDeVotrePDF>.pdf


### clustering
Le clustering se lance ensuite et affiche hierarchiquement le regroupement réalisé

### Identification des modules
Il est demandé d'entrer une valeur de couplage minimale pour tous les modules que cette dernière partie d'execution va trouver.  
Le paramètre doit être compris entre 0 et 1 et le caractère de séparation pour les décimaux doit être la virgule.  
(ou le point si la langue du système est l'anglais)

Les modules répondant aux critères sont alors affichés et l'application termine.