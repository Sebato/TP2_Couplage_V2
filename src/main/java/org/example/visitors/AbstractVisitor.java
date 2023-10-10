package org.example.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;

public abstract class AbstractVisitor extends ASTVisitor {
    protected boolean hasVisited = false;
    public boolean hasVisited() {
        return hasVisited;
    }
}
