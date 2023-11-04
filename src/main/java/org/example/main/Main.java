package org.example.main;

import org.example.process.Analyzer;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        Analyzer analyzer;

        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter the path of the project you want to analyze : ");
        String path = sc.nextLine();

        if (path.isEmpty()) {
            analyzer = new Analyzer(); //default path in ProjectHandler.java
        }else {
            analyzer = new Analyzer(path);
        }

//        analyzer.initUnits();
//        analyzer.parseUnits();
//        analyzer.initCallGraph();
//        analyzer.findCouplages();
//        analyzer.printGraph();
//        analyzer.allCouplages();

        analyzer.processAll();

    }
}