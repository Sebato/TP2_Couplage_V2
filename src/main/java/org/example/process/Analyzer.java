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
    private final HashMap<String, ASTVisitor> visitors;
    private final ClassDeclarationVisitor visitor = new ClassDeclarationVisitor();
    public CallGraph cg;


    public Analyzer(CallGraph cg) {
        this.ph = new ProjectHandler();
        this.visitors = new HashMap<>();
        this.CUnits = new ArrayList<>();
        this.cg = cg;
    }

    public Analyzer(CallGraph cg, String path) {
        this.ph = new ProjectHandler(path);
        this.visitors = new HashMap<>();
        this.CUnits = new ArrayList<>();
        this.cg = cg;
    }

    public ArrayList<CompilationUnit> getCUnits() {
        return CUnits;
    }

/*    public ASTVisitor getVisitor(String type) {
        ASTVisitor visitor = visitors.get(type);
        if (visitor == null) {
            switch (type) {
                case "class":
                    visitor = new ClassDeclarationVisitor();
                    break;
                case "attribute":
                    visitor = new AttributeDeclarationVisitor();
                    break;
                case "method":
                    visitor = new MethodDeclarationVisitor();
                    break;
                case "package":
                    visitor = new PackageDeclarationVisitor();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
            visitors.put(type, visitor);
        }
        return visitor;
    }*/

    //get compilation Units from the project
    public void initUnits() throws IOException {
        System.out.println("\n\tInitializing Compilation Units");
        for (File f : ph.getJavaFiles()) {
            System.out.println("\n\t\t" + f.getName());
            this.CUnits.add(ProjectHandler.parse(f));
        }
    }

    //Parse every file/Class in the project, the visitor will store every TypeDeclaration we have
    public void parseUnits() {
        System.out.println("\n\tParsing Compilation Units");
        for (CompilationUnit CUnit : CUnits) {
            CUnit.accept(this.visitor);
        }
    }

    //initialize the call graph Map with all the classes we've parsed
    public void initCallGraph() throws IOException {
        System.out.println("\n\tInitializing Call Graph ClassMap");
        for (TypeDeclaration type : visitor.getClasses()) {
            cg.addClassKey(type);
        }
    }

    //find all the couplings between classes
    public void findCouplages() {
        cg.findCouplages();
    }

    //print the coupling between classes
    public void printCoupling() {
        cg.printCoupling();
    }

    public void allCouplages() {
        cg.allCouplages();
    }

    //do it all in one go
    public void processAll() throws IOException {
        //System.out.println("\n\tInitializing Compilation Units");
        for (File f : ph.getJavaFiles()) {
            //System.out.println("\n\t\t" + f.getName());
            this.CUnits.add(ProjectHandler.parse(f));
        }

        //at this point : all files parsed and stored in CUnits

        //System.out.println("\n\tParsing Compilation Units");
        for (CompilationUnit CUnit : CUnits) {
            CUnit.accept(this.visitor);
        }

        //at this point : all classes registered in the visitor

        for (TypeDeclaration type : visitor.getClasses()) {
            cg.addClassKey(type);
        }

        //at this point : all classes registered in the CG Map

        cg.findCouplages();

        //at this point : the values of the couplings between classes are in the CG Map

        //cg.printCoupling();

        //at this point : Couplings printed

        cg.allCouplages();

        //at this point : the couplings between classes have been displayed

        cg.createGraph();
    }
}