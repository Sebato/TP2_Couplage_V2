package org.example.process;

import org.example.visitors.*;
import org.eclipse.jdt.core.dom.*;
import org.example.structural.CallGraph;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Analyzer {
    private final ProjectHandler ph;
    private final ArrayList<CompilationUnit> CUnits;
    private final ClassDeclarationVisitor visitor = new ClassDeclarationVisitor();
    public CallGraph cg;


    public Analyzer(CallGraph cg) {
        this.ph = new ProjectHandler();
        this.CUnits = new ArrayList<>();
        this.cg = cg;
    }

    public Analyzer(CallGraph cg, String path) {
        this.ph = new ProjectHandler(path);
        this.CUnits = new ArrayList<>();
        this.cg = cg;
    }

    public ArrayList<CompilationUnit> getCUnits() {
        return CUnits;
    }

    //get compilation Units from the project
    public void initUnits() throws IOException {
        //System.out.println("\n\tInitializing Compilation Units");
        for (File f : ph.getJavaFiles()) {
            //System.out.println("\n\t\t" + f.getName());
            this.CUnits.add(ProjectHandler.parse(f));
        }
    }

    //Parse every file/Class in the project, the visitor will store every TypeDeclaration we have
    public void parseUnits() {
        //System.out.println("\n\tParsing Compilation Units");
        for (CompilationUnit CUnit : CUnits) {
            CUnit.accept(this.visitor);
        }
    }

    //initialize the call graph Map with all the classes we've parsed
    public void initCallGraph() throws IOException {
       // System.out.println("\n\tInitializing Call Graph ClassMap");
        for (TypeDeclaration type : visitor.getClasses()) {
            cg.addClassKey(type.getName().getFullyQualifiedName());
        }
    }

    //find all the couplings between classes
    public void findCouplages() {
        for (TypeDeclaration type : visitor.getClasses()) {
            cg.findCouplages(type);
        }
    }

    //print the coupling between classes
    public void printCoupling(boolean displayAll) {
        cg.allCouplages(displayAll);
    }

    //do it all in one go
    public void processAll() throws IOException {

        initUnits();
        //at this point : all files parsed and stored in CUnits

        parseUnits();
        //at this point : all classes registered in the visitor

        initCallGraph();
        //at this point : all classes registered in the CG Map

        findCouplages();
        //at this point : the values of the couplings between classes are in the CG Map
    }
}