package com.tesis.aether.examples.tree.viewer.common;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class TreeLoader {
	private DefaultMutableTreeNode raiz = null;
	private DefaultMutableTreeNode actual = null;

	public TreeLoader(String name) {
		raiz = new  DefaultMutableTreeNode(name);
		actual = raiz;
	}
	
	public TreeLoader() {
		raiz = new DefaultMutableTreeNode("/");
		actual = raiz;
	}

	public void addDirectory(String name){
    	try{
        	if (actual.getChildCount() > 0 && actual.getChildAt(0).toString() == null)
        		actual.remove(0);
            DefaultMutableTreeNode treeNode = new  DefaultMutableTreeNode(name);
            treeNode.setAllowsChildren(true);
            treeNode.add(new DefaultMutableTreeNode());
            actual.add(treeNode);
    	} catch (java.lang.IllegalStateException ise) {
    		System.out.println ("El elemento '" + actual.toString() + "' no permite hijos.");
    	}
    }

	public void addArchive(String name){
    	try {
        	if (actual.getChildCount() > 0 && actual.getChildAt(0).toString() == null)
        		actual.remove(0);
            DefaultMutableTreeNode treeNode = new  DefaultMutableTreeNode(name);
            treeNode.setAllowsChildren(false);
            actual.add(treeNode);
    	} catch (java.lang.IllegalStateException ise) {
    		System.out.println ("El elemento '" + actual.toString() + "' no permite hijos.");
    	}
    }

	public void enterDirectory(String name){
    	DefaultMutableTreeNode node = getNode(name, actual);
    	if (node != null) 
    		actual = node;
    }

	public void leaveDirectory(){
        TreeNode tn = actual.getParent(); 
    	if (tn != null)
    		actual = (DefaultMutableTreeNode) tn;
    	else
    		actual = raiz;
    }
	
	public DefaultMutableTreeNode gerRoot() {
		return raiz;
	}

    private DefaultMutableTreeNode getNode(String name, DefaultMutableTreeNode root){
    	DefaultMutableTreeNode ret = null;
    	@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> e = root.children();
    	while (e.hasMoreElements()){
    		ret = e.nextElement();
    		if (ret != null && ret.toString().equals(name))
    			return ret;
    	}
    	return null;
    }

}
