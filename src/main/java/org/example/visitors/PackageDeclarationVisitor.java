package org.example.visitors;

import org.eclipse.jdt.core.dom.PackageDeclaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PackageDeclarationVisitor extends AbstractVisitor {
    private List<PackageDeclaration> packages = new ArrayList<>();

    public boolean visit(PackageDeclaration node){
        packages.add(node);
        hasVisited = true;
        return super.visit(node);
    }

    public List<String> getPackageNames(){
        List<String> names = packages.stream()
                .map(packageDeclaration -> packageDeclaration.getName().getFullyQualifiedName())
                .distinct()
                .toList();
        List<String> packageNames = new ArrayList<>();
        for(String name : names){
            String[] split = name.split("\\.");
            if(split.length > 1){
                packageNames.addAll(Arrays.asList(split));
            }
        }
        return packageNames.stream().distinct().toList();
    }

}
