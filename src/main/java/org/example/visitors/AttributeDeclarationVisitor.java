package org.example.visitors;

import org.eclipse.jdt.core.dom.FieldDeclaration;

import java.util.ArrayList;
import java.util.List;

public class AttributeDeclarationVisitor extends AbstractVisitor {
    private List<FieldDeclaration> attributes = new ArrayList<>();

    public boolean visit(FieldDeclaration node) {
        hasVisited = true;
        attributes.add(node);
        return super.visit(node);
    }

    public List<FieldDeclaration> getAttributes() {
        return attributes;
    }
}
