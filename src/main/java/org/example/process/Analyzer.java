package org.example.process;


import org.example.Config.Config;
import org.example.visitors.*;
import org.eclipse.jdt.core.dom.*;
import org.example.structural.CallGraph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Analyzer {

    private final ProjectHandler ph = new ProjectHandler();
    private final ArrayList<CompilationUnit>CUnits;
    private HashMap<String, ASTVisitor> visitors;
    private ClassDeclarationVisitor visitor = new ClassDeclarationVisitor();


    public Analyzer() {
        this.visitors = new HashMap<>();
        this.CUnits = new ArrayList<>();
//        this.visitor = new ClassDeclarationVisitor();
    }

    public ArrayList<CompilationUnit> getCUnits() {
        return CUnits;
    }

    public ASTVisitor getVisitor(String type) {
        ASTVisitor visitor = visitors.get(type);
        if(visitor == null){
            switch (type){
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
    }

    public void initUnits() throws IOException {
        for (File f : ph.getJavaFiles()) {
            this.CUnits.add(Parser.parse(f));
        }
    }

    //Parse every file/Class in the project
    public void parseUnits(){
        for (CompilationUnit CUnit : CUnits) {
            CUnit.accept(visitor);
        }
    }


    //TODO
    //Pour chaque classe,
    //  récupèrer méthodes

    //  pour chaque méthode,
    //      récupèrer les appels de méthodes

    //  pour chaque appel de méthode,
    //      récupèrer la classe solliscitée

    //  pour chaque classe solliscitée,
    //      ajouter un lien entre la classe courante et la classe solliscitée











//    //Merge every call graph of each class
//    public CallGraph buildCallGraph() throws IOException {
//        CallGraph graph = new CallGraph();
//
//        for(TypeDeclaration type: visitor.getClasses()) {
//            //graph.addNode(buildClassCallGraph(type));
//        }
//
//        //graph.toMutableGraph();
//        return graph;
//    }

    //Create and return the call graph of a class
//    public Map<TypeDeclaration, Map<TypeDeclaration, Integer>> buildClassCallGraph(TypeDeclaration clazz){
//        Map<TypeDeclaration, Map<TypeDeclaration, Integer>> node = new HashMap<>();
//        for(MethodDeclaration method: clazz.getMethods()) {
//            MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
//            method.accept(methodInvocationVisitor);
//            Map<TypeDeclaration, Integer> relation = new HashMap<>();
//            List<MethodInvocation> methodInvocations = methodInvocationVisitor.getMethodInvocations();
//            List<String> calledMethods = new ArrayList<>();
//            for(MethodInvocation methodInvocation: methodInvocations) {
//                Expression expression = methodInvocation.getExpression();
//                ITypeBinding typeBinding;
//                if(expression == null) {
//                    // appel de méthode avec 'this' implicite
//                    typeBinding = clazz.resolveBinding();
//                } else {
//                    typeBinding = expression.resolveTypeBinding();
//                }
//                if(typeBinding != null) {
//                    // on ne s'interesse pas aux classes à l'exterieur du projet
//                    if(isTypeInProject(typeBinding.getName())) continue;
//                    String calleeFullName = typeBinding.getName();
//                    calledMethods.add(calleeFullName + "." + methodInvocation.getName().getIdentifier());
//                } else {
//                    // suite d'appels de méthodes i.e. m1().m2().m3();
//                    // le type de m1().m2() ne peut pas être déduit
//                    if(expression != null && !expression.toString().contains(".")) {
//                        if (!isTypeInProject(expression.toString())) continue;
//                        calledMethods.add(expression + "." + methodInvocation.getName().getIdentifier());
//                    }
//                    else
//                        calledMethods.add(methodInvocation.getName().toString());
//                    calledMethods.add(methodInvocation.getName().toString());
//                }
//            }
//            graph.addNode(method, calledMethods);
//        }
//        return node;
//
//    }

//
//    public boolean isTypeInProject(String name){
//        return visitor.getclassByName(name) != null;
//    }
//
//
//
//    public TypeDeclaration getClassByName(String name){
//        return visitor.getclassByName(name);
//    }
//
//    //Find and return all file in a directory
//
//
//    //Check if we already have the desired visitor and return it if yes otherwise return a new one
//
//
//    //Visit every file with a visitor
//    public void visitProject() throws IOException {
//        if(!this.visitor.hasVisited()){
//            for (File fileEntry : javaFiles) {
//                CompilationUnit parse = Parser.parse(fileEntry);
//                parse.accept(visitor);
//            }
//        }
//    }
//
//    public void run() throws IOException {
//        ArrayList<CompilationUnit> CUnits = new ArrayList<>();
//
//        for (File f : javaFiles) {
//            CUnits.add(Parser.parse(f));
//        }
//
//
//    }

}
