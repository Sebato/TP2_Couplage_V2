package org.example.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import java.util.ArrayList;
import java.util.List;

public class MethodDeclarationVisitor extends ASTVisitor {
	private List<MethodInvocation> methodsCalled = new ArrayList<>();

	public List<MethodInvocation> getMethodsCalled() {
		return methodsCalled;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		//maintenant qu'on est dans la methode, on récupère les methodes qu'elle invoque
		MethodInvocationVisitor invocationVisitor = new MethodInvocationVisitor();
		node.accept(invocationVisitor);

		//toutes les invocations sont ajoutées à la liste des invocations de la methode
		methodsCalled.addAll(invocationVisitor.getMethodInvocations());

		//on pourrait déjà eliminer les invocations à des méthodes de notre propre classe ?

		return super.visit(node);
	}
}

