package org.example.App;

import java.io.IOException;
import java.util.Scanner;
import org.example.eclipsejdt.main.EclipseJDTMain;
import org.example.spoon.main.SpoonMain;

public class DoubleMain {
    public static void main(String[] args) throws IOException {

        //choisir la version de l'application à utiliser

        System.out.println("veuillez choisir la version de l'application à utiliser :");
        System.out.println("1 - EclipseJDT");
        System.out.println("2 - Spoon");
        System.out.println("autre type d'entrée - Quitter");

        Scanner sc = new Scanner(System.in);


        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                EclipseJDTMain.eclipseJDTmain(args);
                break;
            case "2":
                SpoonMain.Spoonmain(args);
                break;
            default:
                System.out.println("Au revoir");
                System.exit(0);
        }
    }
}
