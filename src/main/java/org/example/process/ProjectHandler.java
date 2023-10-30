package org.example.process;

import java.io.File;
import java.util.ArrayList;

public class ProjectHandler {

    private ArrayList<File> javaFiles;
    public static String projectSourcePath =  "/home/e20170009949/IdeaProjects/SimpleTestProject/src";

    ProjectHandler(){
        javaFiles = new ArrayList<File>();
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
        ArrayList<File> JF = new ArrayList<File>();

        for (File fileEntry : folder.listFiles()) {
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
                "\nPath : "+ projectSourcePath +
                "\njavaFiles=" + javaFiles +
                "\n}";
    }
}
