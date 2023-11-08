package org.example.process;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ProjectHandler {

    private ArrayList<File> javaFiles;
    public static String projectSourcePath =  System.getProperty("user.dir")+"/src";

    ProjectHandler(){
        javaFiles = new ArrayList<>();
        initProjectStructure();
    }

    ProjectHandler(String path){
        javaFiles = new ArrayList<>();
        projectSourcePath = path;
        initProjectStructure();
    }

    public ArrayList<File> getJavaFiles() {
        return javaFiles;
    }

    public String getProjectSourcePath() {
        return projectSourcePath;
    }

    private void initProjectStructure() {
        if (projectSourcePath == null) {
            System.out.println("Project source path not set");
            System.exit(1);
        }
        File folder = new File(projectSourcePath);
        javaFiles = listJavaFilesInFolder(folder);
    }

    private ArrayList<File> listJavaFilesInFolder(File folder) {
        ArrayList<File> JF = new ArrayList<>();

        for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                JF.addAll(listJavaFilesInFolder(fileEntry));
            } else if (fileEntry.getName().contains(".java")) {
                JF.add(fileEntry);
            }
        }
        return JF;
    }


    @Override
    public String toString() {
        return "ProjectHandler{" +
                "\n\tPath : "+ projectSourcePath +
                "\n\tjavaFiles=" + javaFiles +
                "\n}";
    }

    public static CompilationUnit parse(File fileEntry) throws IOException {
        ASTParser parser = ASTParser.newParser(AST.JLS4); // java +1.6

        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);

        Map options = JavaCore.getOptions();
        parser.setCompilerOptions(options);

        parser.setUnitName("parserUnit");

        String[] sources = { projectSourcePath };
        String[] classpath = { System.getProperty("java.home") };

        parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
        parser.setSource(FileUtils.readFileToString(fileEntry).toCharArray());

        return (CompilationUnit) parser.createAST(null); // create and parse
    }
}
