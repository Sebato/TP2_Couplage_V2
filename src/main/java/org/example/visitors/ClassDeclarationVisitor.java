package org.example.visitors;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassDeclarationVisitor extends AbstractVisitor {
	private Map<String, TypeDeclaration> types = new HashMap<>();


	
	public boolean visit(TypeDeclaration node) {
		if(!node.isInterface()) {
			types.put(node.getName().getFullyQualifiedName(), node);
			hasVisited = true;
		}

		return super.visit(node);
	}
	
	public Map<String, TypeDeclaration> getTypes() {
		return types;
	}

	public TypeDeclaration getclassByName(String name) {
		return types.get(name);
	}
	public List<TypeDeclaration> getClasses(){
		List<TypeDeclaration> classes = new ArrayList<>();
		for(Map.Entry<String, TypeDeclaration> entry : types.entrySet()) {
			classes.add(entry.getValue());
		}
		return classes;
	}

	public Map<String, TypeDeclaration> getTypesMap() {
		return types;
	}

}


