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
        System.out.println("Please enter the path of the project you want to analyze : ");
        String path = sc.nextLine();

        if (path.isEmpty()) {
            analyzer = new Analyzer(cg); //default path in ProjectHandler.java
        }else {
            analyzer = new Analyzer(cg,path);
        }

//        analyzer.initUnits();
//        analyzer.parseUnits();
//        analyzer.initCallGraph();
//        analyzer.findCouplages();
//        analyzer.printGraph();
//        analyzer.allCouplages();

        analyzer.processAll();

        //cg.createGraph();
        cg.exportGraphToDot();

    }
}