/*
	Copyright (c) 2013 Robert Clipsham <robert@octarineparrot.com>
	All rights reserved.
	
	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met:
	    * Redistributions of source code must retain the above copyright
	      notice, this list of conditions and the following disclaimer.
	    * Redistributions in binary form must reproduce the above copyright
	      notice, this list of conditions and the following disclaimer in the
	      documentation and/or other materials provided with the distribution.
	    * Neither the name of j2d nor the
	      names of its contributors may be used to endorse or promote products
	      derived from this software without specific prior written permission.
	
	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
	DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.octarineparrot.j2d;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public final class InputDirectoryVisitor extends
		SimpleFileVisitor<Path> {
	private final String[] args;
	private final Stack<List<String>> files;

	public InputDirectoryVisitor(String[] args) {
		this.args = args;
		files = new Stack<>();
	}
	
	private boolean onlyWarnings(IProblem[] problems) {
		for (IProblem problem : problems) {
			if (problem.isError()) {
				return false;
			}
		}
		return true;
	}
	
	private String getModName(Path file) {
		Path fn = file.getName(file.getNameCount() - 1);
		String mn = fn.toString();
		return J2dVisitor.fixKeywords(mn.substring(0, mn.length() - 5));
	}

	@Override
	public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) throws IOException {
		// TODO support package-info.java
		if (!file.toString().endsWith(".java") || file.toString().endsWith("package-info.java")) {
			System.out.println("Skipping " + file);
			return FileVisitResult.CONTINUE;
		}
		System.out.println("Translating " + file);
		files.peek().add(getModName(file));
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		Hashtable options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		/*Object[] oarr = options.keySet().toArray();
		for (int i = 0; i < options.size(); i++) {
			System.out.println(oarr[i] + " = " + options.get(oarr[i]));
		}
		System.exit(1);*/
		//System.out.println(options);
		parser.setCompilerOptions(options);
		parser.setResolveBindings(true);
		parser.setUnitName(file.toString());
		parser.setEnvironment(null, new String[] { args[0] }, null, true);
		byte[] bytes = Files.readAllBytes(file);
		char[] source = new String(bytes).toCharArray();
		bytes = null;
		parser.setSource(source);
		CompilationUnit cu = (CompilationUnit)parser.createAST(null);
		IProblem[] problems = cu.getProblems();
		boolean warnings = onlyWarnings(problems);
		if (problems.length == 0 || warnings) {
			if (problems.length > 0 && warnings) {
				System.err.println("Warning: File '" + file + "' contains warnings, which may lead to an invalid conversion.");
				for (IProblem problem : problems) {
					System.err.println(file + ":" +
									   problem.getSourceLineNumber() + ": " +
									   problem.getMessage());
				}
			}
			J2dVisitor visitor = new J2dVisitor(file, source);
			cu.accept(visitor);
			visitor.save(args[1]);
			return FileVisitResult.CONTINUE;
		} else {
			System.err.println("File '" + file + "' contains problems. Translation aborted.");
			for (IProblem problem : problems) {
				System.err.println(file + ":" +
								   problem.getSourceLineNumber() + ": " +
								   problem.getMessage());
			}
			return FileVisitResult.TERMINATE;
		}
		
	}
	
	@Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        throws IOException {
		files.push(new ArrayList<String>());
		return FileVisitResult.CONTINUE;
	}
	
	private String join(List<String> strs, String sep) {
		String res = "";
		for (String s : strs) {
			res += s + sep;
		}
		return res;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
		if (ex != null) {
			throw ex;
		}
		int rootDirParts = Paths.get(args[0]).getNameCount();
		// Ignore root directory, skip empty directories
		if (dir.getNameCount() == rootDirParts || files.peek().size() == 0) {
			files.pop();
			return FileVisitResult.CONTINUE;
		}
		Path relativeDir = dir.subpath(rootDirParts, dir.getNameCount());
		// Fix keywords in directory names
		ArrayList<String> parts = new ArrayList<>();
		for (Path p : relativeDir) {
			 parts.add(J2dVisitor.fixKeywords(p.toString()));
		}
		relativeDir = Paths.get(join(parts, FileSystems.getDefault().getSeparator()));
		String packageName = relativeDir.toString().replace(FileSystems.getDefault().getSeparator(), ".");
		Path p = Paths.get(args[1], relativeDir.toString());
		Writer w = new FileWriter(p.resolve("all.d").toFile());
		w.write("module " + packageName + ".all;\n\n");
		
		for (String s : files.pop()) {
			w.write("public import " + packageName + "." + s + ";\n");
		}
		w.flush();
		w.close();
		
		return FileVisitResult.CONTINUE;
	}
}