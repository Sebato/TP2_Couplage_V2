package org.example.main;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.example.process.Analyzer;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {

        Analyzer analyzer = new Analyzer();

//        analyzer.initUnits();
//        analyzer.parseUnits();
//        analyzer.initCallGraph();
//        analyzer.findCouplages();

        analyzer.ProcessAll();


    }
}