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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

public class J2dVisitor extends ASTVisitor {
	
	/**
	 * Stack of writers for output
	 * 
	 * Using a stack allows for storing output for future use
	 */
	final private Stack<Writer> output = new Stack<>();
	
	/**
	 * Writer to output native method stubs to
	 */
	final private Writer nativeOutput;
	
	/**
	 * Generated interfaces for generic types
	 */
	final private Writer generatedInterfaces = new StringWriter();
	
	/**
	 * Levels of indentation
	 */
	private int indent = 0;
	
	/**
	 * Should the code be indented?
	 * TODO This needs to be a stack like output
	 */
	private boolean shouldIndent = true;
	
	/**
	 * Template constraints, if any
	 */
	private String constraints = "";
	
	/**
	 * Hold additional template parameters generated by wildcard
	 * parameters
	 */
	final private Queue<String> additionalTemplateParams = new LinkedList<>();
	
	/**
	 * List of comments in the compilation unit
	 */
	final private Queue<Comment> comments = new LinkedList<>();;
	
	/**
	 * Source code for the file. Used only for comments
	 */
	final private char[] sourceCode;
	
	/**
	 * Package name for output
	 */
	private String packageName = "";
	
	/**
	 * Module name for output
	 */
	final private String moduleName;
	
	/**
	 * Instance variables to rewrite
	 * Used when:
	 * 	void foo();
	 *  int foo;
	 * Occurs.
	 */
	private List<String> varRewrites = new ArrayList<String>();
	
	/**
	 * Methods to rewrite
	 * Used when:
	 *  void foo();
	 *  <T> T foo();
	 * Occurs
	 */
	private List<String> methodRewrites = new ArrayList<String>();
	
	/**
	 * Should a rewrite be performed?
	 */
	private boolean doRewrite = true;
	
	/**
	 * In a field access
	 */
	private boolean inFieldAccess = false;
	
	/**
	 * Locally declared variables
	 */
	private List<String> locals = new ArrayList<String>();
	
	/**
	 * Are we currently in a method?
	 */
	private boolean inMethod = false;
	
	/**
	 * List of types we are currently inside
	 */
	private Stack<String> inTypes = new Stack<>();
	
	/**
	 * Should paramaterised typed be forced to be 
	 * printed as JavaObject instead of T?
	 */
	private boolean forceJavaObject = false;

	/**
	 * Additional generated method to print (used for generic inheritance)
	 */
	private String additionalMethod = "";
	
	/**
	 * Should generic method names be rewritten
	 */
	private boolean rewriteMethods = false;
	
	/**
	 * Initializers for static fields
	 */
	private final Queue<String> staticFieldInitializers = new LinkedList<>();
	
	/**
	 * Initializers for fields
	 */
	private final Queue<String> fieldInitializers = new LinkedList<>();
	
	public J2dVisitor(Path file, char[] source) {
		nativeOutput = new StringWriter();
		sourceCode = source;
		
		String fileName = file.toFile().getName();
		String module = fileName.substring(0, fileName.length() - 5);
		moduleName = fixKeywords(module);
		
		pushWriter(new StringWriter());
	}
	
	public void save(String outputDir) throws IOException {
		String pkgDir = packageName.replace(".", FileSystems.getDefault().getSeparator());
		Path dir = Paths.get(outputDir, pkgDir);
		dir.toFile().mkdirs();
		Path outFile = dir.resolve(moduleName + ".d");
		
		Writer w = output.pop();

		w.flush();
		Files.write(outFile, w.toString().getBytes());
		
		String[] pkgs = packageName.split("\\.");
		String pkg = pkgs.length > 0 ? pkgs[0] : packageName;
		
		outFile = Paths.get(outputDir, pkg).resolve("native_methods.d");
		// TODO What if we're overwriting from a previous translation?
		if (!outFile.toFile().exists()) {
			Files.write(outFile,
					("module " + pkg + "native_methods;").getBytes(),
					StandardOpenOption.CREATE);
		}
		nativeOutput.flush();
		Files.write(outFile,
					nativeOutput.toString().getBytes(),
					StandardOpenOption.APPEND,
					StandardOpenOption.CREATE);

	}
	
	private String strRepeat(String str, int times) {
		String result = "";
		for (int i = 0; i < times; i++) {
			result += str;
		}
		return result;
	}
	
	private String getIndent() {
		return strRepeat("    ", indent);
	}
	
	private void pushWriter(Writer w) {
		output.add(w);
	}
	
	private void popWriter() {
		output.pop();
	}
	
	private void print(String str) {
		try {
			Writer w = output.peek();
			if (shouldIndent) {
				w.write(getIndent());
				shouldIndent = false;
			}
			w.write(str);
			//w.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	private void println(String str) {
		print(str + "\n");
		shouldIndent = true;
	}
	
	public static String fixKeywords(String str) {
		// All D keywords which aren't also java keywords
		final String[] keywords = {
				"alias", "align", "asm", "auto", "body", "bool",
				"cast", "cdouble", "cent", "cfloat", "creal",
				"dchar", "debug", "delegate", "delete", "deprecated",
				"export", "extern", "foreach", "foreach_reverse", "function",
				"idouble", "ifloat", "immutable", "in", "inout", "invariant",
				"ireal", "is", "lazy", "macro", "mixin", "module", "nothrow",
				"out", "override", "pragma", "pure", "real", "ref", "scope",
				"shared", "struct", "template", "typedef", "typeid", "typeof",
				"ubyte", "ucent", "uint", "ulong", "union", "unittest",
				"ushort", "version", "wchar", "with"
		};
		final String[] jClasses = {
				"Error", "Exception", "Object", "Throwable", "TypeInfo"
		};
		if (Arrays.binarySearch(keywords, str) >= 0) {
			return str + "_";
		} else if (Arrays.binarySearch(jClasses, str) >= 0) {
			return "Java" + str;
		} else if (str.equals("toString")) {
			return "toJString";
		}
		return str;
	}
	
	int id = 0;

	private String genId() {
		return "_j2d_" + id++;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		System.out.println("Found: " + node.getClass());
		// TODO
		return false;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		System.out.println("Found: " + node.getClass());
		// TODO
		return super.visit(node);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		//System.out.println("Found: " + node.getClass());
		println(" {");
		indent++;
		for(Object o : node.bodyDeclarations()) {
			((BodyDeclaration)o).accept(this);
			println("");
		}
		indent--;
		print("}");
		return false;
	}

	@Override
	public boolean visit(ArrayAccess node) {
		//System.out.println("Found: " + node.getClass() + " " + node);
		node.getArray().accept(this);
		print("[");
		node.getIndex().accept(this);
		print("]");
		return false;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		//System.out.println("Found: " + node.getClass());
		if (node.getInitializer() != null) {
			node.getInitializer().accept(this);
			return false;
		}
		print("new ");
		Type base = node.getType().getComponentType();
		while (base instanceof ArrayType) {
			base = ((ArrayType)base).getComponentType();
		}
		base.accept(this);
		print("[");
		int printed = 0;
		for (Object o : node.dimensions()) {
			Expression e = (Expression)o;
			if (printed > 0) {
				print("][");
			}
			e.accept(this);
			printed++;
		}
		print("]");
		return false;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		//System.out.println("Found: " + node.getClass() + " " + node);
		print("[");
		int printed = 0;
		for (Object o : node.expressions()) {
			if (printed > 0) {
				print(", ");
			}
			((Expression)o).accept(this);
			printed++;
		}
		print("]");
		return false;
	}

	@Override
	public boolean visit(ArrayType node) {
		//System.out.println("Found: " + node.getClass());
		//visit(node.getComponentType());
		node.getComponentType().accept(this);
		print("[]");
		return false;
	}

	@Override
	public boolean visit(AssertStatement node) {
		//System.out.println("Found: " + node.getClass());
		print("assert(");
		node.getExpression().accept(this);
		if (node.getMessage() != null) {
			print(", ");
			node.getMessage().accept(this);
		}
		println(");");
		return false;
	}

	@Override
	public boolean visit(Assignment node) {
		//System.out.println("Found: " + node.getClass() + " " + node);
		node.getLeftHandSide().accept(this);
		print(" " + node.getOperator() + " ");
		node.getRightHandSide().accept(this);
		return false;
	}

	@Override
	public boolean visit(Block node) {
		//System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(BlockComment node) {
		//System.out.println("Found: " + node.getClass());
		//println("");
		//println(node.getStartPosition() + " " + node.getLength());
		String comment = new String(Arrays.copyOfRange(sourceCode,
											node.getStartPosition(),
											node.getStartPosition() + node.getLength()));
		print(comment);
		return false;
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		//System.out.println("Found: " + node.getClass());
		print(node.booleanValue() ? "true" : "false");
		return false;
	}

	@Override
	public boolean visit(BreakStatement node) {
		//System.out.println("Found: " + node.getClass());
		if (node.getLabel() == null) {
			println("break;");
			//if (node.getParent() instanceof SwitchCase) {
			//	indent--;
			//}
		} else {
			print("break ");
			node.getLabel().accept(this);
			println(";");
		}
		return false;
	}

	@Override
	public boolean visit(CastExpression node) {
		//System.out.println("Found: " + node.getClass());
		print("cast(");
		node.getType().accept(this);
		print(")(");
		node.getExpression().accept(this);
		print(")");
		return false;
	}

	@Override
	public boolean visit(CatchClause node) {
		//System.out.println("Found: " + node.getClass());
		ArrayList<Type> types = new ArrayList<>();
		// Handle union types in catch clauses
		if (node.getException().getType() instanceof UnionType) {
			//println("UNIONUNIONUNION");
			UnionType ut = (UnionType)node.getException().getType();
			for (Object o : ut.types()) {
				types.add((Type)o);
			}
		} else {
			types.add(node.getException().getType());
		}
		for (Type t : types) {
			print("catch (");
			t.accept(this);
			print(" ");
			node.getException().getName().accept(this);
			//node.getException().accept(this);
			println(") {");
			indent++;
			node.getBody().accept(this);
			indent--;
			print("} ");
		}
		return false;
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		String chr = node.getEscapedValue();
		// It would seem a lot of the text manipulation stuff
		// in java uses invalid unicode characters, so we use
		// this hack to get around it.
		if (chr.startsWith("'\\u")) {
			chr = "0x" + chr.substring(3, chr.length() - 1);
		}
		print(chr);
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		//System.out.println("Found: " + node.getClass() + " " + node)

		// Wrap the expression in parenthesis so it parses in D
		boolean needsParens = node.getParent() instanceof MethodInvocation ||
							  node.getParent() instanceof FieldAccess;
		if (needsParens) {
			print("(");
		}
		print("new ");
		if (node.getAnonymousClassDeclaration() != null) {
			print("class ");
		}
		node.getType().accept(this);
		// TODO What about arguments? (not valid D)
		if (node.getAnonymousClassDeclaration() == null) {
			print("(");
			int printed = 0;
			for (Object o : node.arguments()) {
				if (printed > 0) {
					print(", ");
				}
				((Expression)o).accept(this);
				printed++;
			}
			print(")");
		} else {
			node.getAnonymousClassDeclaration().accept(this);
		}
		if (needsParens) {
			print(")");
		}
		return false;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		//System.out.println("Found: " + node.getClass());
		// TODO Non-javadoc comments.
		// It looks like the way to do this is add a doComments() method
		// and call it before and after every AST node. Not ideal, but
		// it would work. Can probably use pre/postVisit methods for this
		// purpose
		
		for (Object o : node.getCommentList()) {
			if (!(o instanceof Javadoc)) {
				comments.add((Comment)o);
			}
		}
		/*List l = node.getCommentList();
		for (Object o : l) {
			Comment c = (Comment)o;
			println("COMMENT");
			println(c.toString());
			println("----");
			println(c.getAlternateRoot().getClass().toString());
			println("/COMMENT");
		}*/
		return super.visit(node);
	}
	
	@Override
	public void endVisit(CompilationUnit node) {
		println(generatedInterfaces.toString());
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		//System.out.println("Found: " + node.getClass());
		node.getExpression().accept(this);
		print(" ? ");
		node.getThenExpression().accept(this);
		print(" : ");
		node.getElseExpression().accept(this);
		return false;
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		//System.out.println("Found: " + node.getClass());
		// TODO Type arguments
		print("this(");
		int printed = 0;
		for (Object o : node.arguments()) {
			Expression t = (Expression)o;
			if (printed > 0) {
				print(", ");
			}
			t.accept(this);
			printed++;
		}
		println(");");
		return false;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		//System.out.println("Found: " + node.getClass());
		println("continue;");
		return false;
	}

	@Override
	public boolean visit(DoStatement node) {
		//System.out.println("Found: " + node.getClass());
		println("do {");
		indent++;
		node.getBody().accept(this);
		indent--;
		print("} while (");
		node.getExpression().accept(this);
		println(");");
		return false;
	}

	@Override
	public boolean visit(EmptyStatement node) {
		//System.out.println("Found: " + node.getClass());
		indent++;
		println(";");
		indent--;
		return false;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		//System.out.println("Found: " + node.getClass());
		print("foreach (");
		node.getParameter().accept(this);
		print("; ");
		node.getExpression().accept(this);
		println(") {");
		indent++;
		node.getBody().accept(this);
		indent--;
		println("}");
		return false;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		//System.out.println("Found: " + node.getClass());
		print("public static ");
		((EnumDeclaration)node.getParent()).getName().accept(this);
		print(" ");
		node.getName().accept(this);
		print(" = new ");
		((EnumDeclaration)node.getParent()).getName().accept(this);
		print("(");
		int printed = 0;
		for (Object o : node.arguments()) {
			if (printed > 0) {
				print(", ");
			}
			((Expression)o).accept(this);
			printed++;
		}
		println(");");
		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		//System.out.println("Found: " + node.getClass());
/*
 *  EnumDeclaration:
     [ Javadoc ] { ExtendedModifier } enum Identifier
         [ implements Type { , Type } ]
         {
         [ EnumConstantDeclaration { , EnumConstantDeclaration } ] [ , ]
         [ ; { ClassBodyDeclaration | ; } ]
         }
 */
		// TODO finish enum stuff

		printModifiers(node);
		print("class ");
		node.getName().accept(this);
		// TODO implements
		print(" : Enum!(");
		node.getName().accept(this);
		println(") {");
		indent++;
		for (Object o : node.enumConstants()) {
			((EnumConstantDeclaration)o).accept(this);
		}
		for (Object o : node.bodyDeclarations()) {
			((BodyDeclaration)o).accept(this);
		}
		indent--;
		println("}");
		return false;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		//System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}
	
	@Override
	public void endVisit(ExpressionStatement node) {
		println(";");
	}

	@Override
	public boolean visit(FieldAccess node) {
		//System.out.println("Found: " + node.getClass());
		node.getExpression().accept(this);
		print(".");
		inFieldAccess = true;
		node.getName().accept(this);
		inFieldAccess = false;
		return false;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		//System.out.println("Found: " + node.getClass());
		printModifiers(node);
		doRewrite = false;
		node.getType().accept(this);
		doRewrite = true;
		int printed = 0;
		if (node.fragments().size() > 0) {
			for (Object o : node.fragments()) {
				if (printed > 0) {
					print(",");
				}
				visit((VariableDeclarationFragment)o);
				printed++;
			}
		}
		println(";");
		if (staticFieldInitializers.size() > 0) {
			println("shared static this() {");
			indent++;
			for (String s : staticFieldInitializers) {
				println(s);
			}
			indent--;
			println("}");
			staticFieldInitializers.clear();
		}
		return false;
	}

	@Override
	public boolean visit(ForStatement node) {
		//System.out.println("Found: " + node.getClass());
		print("for (");
		int printed = 0;
		for (Object o : node.initializers()) {
			if (printed > 0) {
				print(", ");
			}
			((Expression)o).accept(this);
			printed++;
		}
		print("; ");
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		}
		print("; ");
		printed = 0;
		for (Object o : node.updaters()) {
			if (printed > 0) {
				print(", ");
			}
			((Expression)o).accept(this);
			printed++;
		}
		println(") {");
		indent++;
		node.getBody().accept(this);
		indent--;
		println("}");
		return false;
	}

	@Override
	public boolean visit(IfStatement node) {
		//System.out.println("Found: " + node.getClass());
		print("if (");
		node.getExpression().accept(this);
		println(") {");
		indent++;
		node.getThenStatement().accept(this);
		indent--;
		
		if (node.getElseStatement() == null) {
			println("}");
		} else {
			if (node.getElseStatement() instanceof IfStatement) {
				print("} else ");
				node.getElseStatement().accept(this);
			} else {
				println("} else {");
				indent++;
				node.getElseStatement().accept(this);
				indent--;
				println("}");
			}
		}
		return false;
	}
	
	private String cleanComponents(String name) {
		String[] strs = name.split("\\.");
		for (int i = 0; i < strs.length; i++) {
			strs[i] = fixKeywords(strs[i]);
		}
		String ret = "";
		for (String s : strs) {
			ret += s + ".";
		}
		return ret.substring(0, name.length());
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		//print(node.toString());
		print("import ");
		
		IBinding ib = node.resolveBinding();
		
		// import static foo...
		if (node.isStatic()) {
			// import static foo.*;
			if (node.isOnDemand()) {
				//node.getName().accept(this);
				//println(";");
				ITypeBinding itb = (ITypeBinding)ib;
				// TODO This is a bit of a hack.
				String s = itb.getBinaryName();
				int idx = s.indexOf('$');
				if (idx != -1) {
					print(cleanComponents(s.substring(0, idx)));
				} else {
					node.getName().accept(this);
				}
				println(";");
				// Aliases for fields
				for (IVariableBinding ivb : itb.getDeclaredFields()) {
					if ((ivb.getModifiers() & Modifier.STATIC) != 0 &&
						(ivb.getModifiers() & Modifier.PUBLIC) != 0) {
						// TODO Do we need to do something with these? Why are they here?
						// Skip this, comes from enum types.
						if (ivb.getName().equals("$VALUES") || ivb.getName().equals("$assertionsDisabled")) {
							continue;
						}
						print("alias ");
						node.getName().accept(this);
						print("." + fixKeywords(ivb.getName()) + " " + fixKeywords(ivb.getName()));
						println(";");
					}
				}
				// Aliased for methods - remove overloads
				Set<String> methods = new HashSet<>();
				for (IMethodBinding imb : itb.getDeclaredMethods()) {
					if ((imb.getModifiers() & Modifier.STATIC) != 0) {
						if (methods.contains(imb.getName())) {
							continue;
						}
						print("alias ");
						node.getName().accept(this);
						print("." + fixKeywords(imb.getName()) + " " + fixKeywords(imb.getName()));
						println(";");
						methods.add(imb.getName());
					}
				}
			} else {
				// import static foo.bar;
				if (ib instanceof ITypeBinding) {
					ITypeBinding itb = (ITypeBinding)ib;
					// TODO This is a bit of a hack.
					String s = itb.getBinaryName();
					int idx = s.indexOf('$');
					if (idx != -1) {
						print(cleanComponents(s.substring(0, idx)));
					} else {
						node.getName().accept(this);
					}

				} else if (ib instanceof IMethodBinding) {
					// apparently import static package.Outer.Inner.foobar; is a thing
					IMethodBinding imb = (IMethodBinding)ib;
					// TODO This is a bit of a hack.
					String s = imb.getDeclaringClass().getBinaryName();
					int idx = s.indexOf('$');
					if (idx != -1) {
						print(cleanComponents(s.substring(0, idx)));
					} else {
						((QualifiedName)node.getName()).getQualifier().accept(this);
					}
				} else {
					((QualifiedName)node.getName()).getQualifier().accept(this);
				}
				
				println(";");
				print("alias ");
				node.getName().accept(this);
				print(" ");
				((QualifiedName)node.getName()).getName().accept(this);
				println(";");
			}
		} else {
			// import foo.*;
			if (node.isOnDemand()) {
				// TODO Change after DIP 37
				node.getName().accept(this);
				if (ib instanceof ITypeBinding) {
					ITypeBinding itb = (ITypeBinding)ib;
					// TODO This is a bit of a hack.
					String s = itb.getBinaryName();
					int idx = s.indexOf('$');
					if (idx != -1) {
						print(cleanComponents(s.substring(0, idx)));
						print(" : ");
						print(cleanComponents(s.substring(idx + 1, s.length()).replace("$", ".")));
						println(";");
					} else {
						println(";");
					}
				} else {
					println(".all;");
				}
			} else {
				// import foo.inner;
				if (ib instanceof ITypeBinding) {
					ITypeBinding itb = (ITypeBinding)ib;
					// TODO This is a bit of a hack.
					String s = itb.getBinaryName();
					int idx = s.indexOf('$');
					if (idx != -1) {
						print(cleanComponents(s.substring(0, idx)));
						// TODO Is this the closest approximation we can get with D?
						print(" : ");
						print(fixKeywords(s.substring(s.lastIndexOf('.') + 1, idx)));
						println(";");
					} else {
						// import foo;
						node.getName().accept(this);
						println(";");
					}
				} 
			}
		}

		return false;
	}

	@Override
	public boolean visit(InfixExpression node) {
		//System.out.println("Found: " + node.getClass() + " " + node);

		boolean needParens = false;
		if (node.getOperator().equals(Operator.AND) ||
			node.getOperator().equals(Operator.XOR) ||
			node.getOperator().equals(Operator.OR)) {
			needParens = true;
		}

		if (needParens) {
			print("(");
		}
		node.getLeftOperand().accept(this);
		if (needParens) {
			print(")");
		}
		
		String op = " " + node.getOperator() + " ";

		if (node.getOperator().equals(Operator.EQUALS)) {
			op = " is ";
		} else if (node.getOperator().equals(Operator.NOT_EQUALS)) {
			op = " !is ";
		}

		if (node.getLeftOperand()
				.resolveTypeBinding()
				.getQualifiedName()
				.equals("java.lang.String") &&
				node.getOperator().equals(Operator.PLUS)) {
			// TODO Handle String + int etc
			op = " ~ ";
		}
		
		print(op);

		if (needParens) {
			print("(");
		}
		node.getRightOperand().accept(this);
		if (needParens) {
			print(")");
		}
		
		if (node.hasExtendedOperands()) {
			for (Object o : node.extendedOperands()) {
				print(op);
				if (needParens) {
					print("(");
				}
				((Expression)o).accept(this);
				if (needParens) {
					print(")");
				}
			}
		}
		return false;
	}

	@Override
	public boolean visit(Initializer node) {
		//System.out.println("Found: " + node.getClass());
		println("shared static this() {");
		indent++;
		node.getBody().accept(this);
		indent--;
		println("}");
		return false;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		//System.out.println("Found: " + node.getClass());
		print("(cast(");
		node.getRightOperand().accept(this);
		print(")(");
		node.getLeftOperand().accept(this);
		print(") != null)");
		return false;
	}

	@Override
	public boolean visit(Javadoc node) {
		//System.out.println("Found: " + node.getClass());
		// TODO JavaDoc -> DDoc
		/*for (Object o : node.tags()) {
			println("~~~~~~~~~~~~~~~~~~~~~~~~");
			TagElement te = (TagElement)o;
			println("Tag name: " + te.getTagName());
			for (Object p : te.fragments()) {
				//IDocElement ide = (IDocElement)p;
				println("	elem: " + p);
			}
			println("~~~~~~~~~~~~~~~~~~~~~~~~");
		}*/
		print(node.toString().replaceAll("\n", "\n" + getIndent()));
		return super.visit(node);
	}

	@Override
	public boolean visit(LabeledStatement node) {
		//System.out.println("Found: " + node.getClass());
		int oldIndent = indent;
		indent = 0;
		node.getLabel().accept(this);
		println(":");
		indent = oldIndent;
		node.getBody().accept(this);
		return false;
	}

	@Override
	public boolean visit(LineComment node) {
		//System.out.println("Found: " + node.getClass());
		/* TODO comments in the form:
			something(); // foo
			are put in the incorrect place.
		 */ 
		String comment = new String(Arrays.copyOfRange(sourceCode,
				node.getStartPosition(),
				node.getStartPosition() + node.getLength()));
		println(comment);
		return false;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		//System.out.println("Found: " + node.getClass());
		String name = node.getTypeName().getFullyQualifiedName();
		if (name.equals("Override")) {
			print("override ");
		} else if (name.equals("Deprecated")){
			print("deprecated ");
		} else {
			print("@_j2d_" + name + " ");
			//System.out.println("MarkerAnnotation: " + node);
			//throw new RuntimeException();
		}
		return false;
	}

	@Override
	public boolean visit(MemberRef node) {
		System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberValuePair node) {
		System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}
	
	/**
	 * Return all super interfaces and classes of a type
	 * 
	 * @param itb
	 * @return
	 */
	private Set<ITypeBinding> getParents(ITypeBinding itb, Set<ITypeBinding> supers) {
		if (itb.getSuperclass() != null) {
			supers.add(itb.getSuperclass());
			getParents(itb.getSuperclass(), supers);
		}
		/*for (ITypeBinding i : itb.getInterfaces()) {
			supers.add(i);
			getParents(i, supers);
		}*/
		return supers;
	}
	
	private boolean typesMatch(IMethodBinding m1, IMethodBinding m2) {
		if (m1.getReturnType().getErasure().equals(m2.getReturnType().getErasure())) {
			ITypeBinding[] m1Params = m1.getParameterTypes();
			ITypeBinding[] m2Params = m2.getParameterTypes();
			if (m1Params.length != m2Params.length) {
				return false;
			}
			for (int i = 0; i < m1Params.length; i++) {
				if (!m1Params[i].getErasure().equals(m2Params[i].getErasure())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean isOverride(MethodDeclaration node) {
		Set<ITypeBinding> supers = getParents(node.resolveBinding().getDeclaringClass(), new HashSet<ITypeBinding>());
		for (ITypeBinding itb : supers) {
			for (IMethodBinding imb : itb.getDeclaredMethods()) {
				if (imb.getName().equals(node.getName().toString()) &&
					typesMatch(imb, node.resolveBinding())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean hasNonStaticFields(ASTNode node) {
		if (node instanceof TypeDeclaration) {
			TypeDeclaration td = (TypeDeclaration)node;
			for (FieldDeclaration fd : td.getFields()) {
				if (!isStatic(fd)) {
					return true;
				}
			}
			return false;
		}
		// TODO is this correct for enums?
		return false;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		//System.out.println("Found: " + node.getClass());
		
		boolean overridden = isOverride(node);
		//println(node.getName() + ": isOverride = " + overridden);
		if (overridden && node.typeParameters().size() > 0) {
			forceJavaObject = true;
		}
		
		printModifiers(node);
		if (node.isConstructor()) {
			print("this(");
		} else {
			node.getReturnType2().accept(this);
			print(" ");
			doRewrite = false;
			//rewriteMethods = node.typeParameters().size() == 0;
			node.getName().accept(this);
			if (forceJavaObject) {
				print("Impl");
			}
			//rewriteMethods = false;
			doRewrite = true;
			print("(");
		}

		StringWriter sw = new StringWriter();
		pushWriter(sw);
		int printed = 0;
		if (node.parameters().size() > 0) {
			for (Object o : node.parameters()) {
				if (printed > 0) {
					print(", ");
				}
				visit((SingleVariableDeclaration)o);
				printed++;
			}
		}
		popWriter();
		
		if (!overridden && (node.typeParameters().size() > 0 || !additionalTemplateParams.isEmpty() || methodRewrites.contains(node.getName().toString()))) {
			printed = 0;
			for (Object o : node.typeParameters()) {
				if (printed > 0) {
					print(", ");
				}
				((TypeParameter)o).accept(this);
				printed++;
			}
			if (!additionalTemplateParams.isEmpty()) {
				for (String s : additionalTemplateParams) {
					if (printed > 0) {
						print(", ");
					}
					print(s);
					printed++;
				}
				additionalTemplateParams.clear();
			}
			print(")(");
		}
		print(sw.toString());
		if (node.getBody() == null && !isNative(node)) {
			if (node.typeParameters().size() > 0 || methodRewrites.contains(node.getName().toString())) {
				Writer w = new StringWriter();
				pushWriter(w);
				printModifiers(node);
				forceJavaObject = true;
				node.getReturnType2().accept(this);
				print(" ");
				doRewrite = false;
				node.getName().accept(this);
				print("Impl");
				doRewrite = true;
				print("(");
				printed = 0;
				for (Object o : node.parameters()) {
					if (printed > 0) {
						print(", ");
					}
					visit((SingleVariableDeclaration)o);
					printed++;
				}
				println(");");
				forceJavaObject = false;
				popWriter();
				additionalMethod = w.toString();
				
				println(") {");
				indent++;
				print("return cast(");
				node.getReturnType2().accept(this);
				print(")(");
				doRewrite = false;
				node.getName().accept(this);
				print("Impl");
				doRewrite = true;
				print("(");
				printed = 0;
				for (Object o : node.parameters()) {
					if (printed > 0) {
						print(", ");
					}
					SingleVariableDeclaration svd = (SingleVariableDeclaration)o;
					print("cast(");
					forceJavaObject = true;
					svd.getType().accept(this);
					forceJavaObject = false;
					print(")");
					print(fixKeywords(svd.getName().toString()));
					printed++;
				}
				print(")");
				println(");");
				indent--;
				println("}");
			} else {
				println(");");
			}
		} else {
			if (constraints.length() > 0) {
				println(")");
				indent += 2;
				println("if (" + constraints + ") {");
				indent -= 2;
				constraints = "";
			} else {
				println(") {");
			}
			indent++;
			if (node.isConstructor() && hasNonStaticFields(node.getParent())) {
				println("_j2d_intializeFields();");
			}
			if (isSynchronized(node)) {
				println("synchronized (this) {");
				indent++;
			}
			// If the method is native, we delegate the functionality
			// to a separate module
			if (isNative(node)) {
				IMethodBinding bindings = node.resolveBinding();
				String cn = bindings.getDeclaringClass().getQualifiedName();
				String[] pkgs = cn.split("\\.");
				String pkg = pkgs.length > 0 ? pkgs[0] : cn;
				String mn = node.getName().toString();
				String fqn = cn + "." + mn;
				String fnFqn = fqn.replace(".", "_");
				println("import " + pkg + ".native_methods;");
				print("return " + fnFqn + "(");
				if (isStatic(node)) {
					print("this");
					printed = 1;
				} else {
					printed = 0;
				}
				for (Object o : node.parameters()) {
					if (printed > 0) {
						print(", ");
					}
					print(fixKeywords(((SingleVariableDeclaration)o).getName().toString()));
					printed++;
				}
				println(");");
				// TODO This doesn't work with type parameters
				pushWriter(nativeOutput);
				int oldIndent = indent;
				indent = 0;
				println("");
				if (!isStatic(node)) {
					println("import " + cn + ";");
				}
				node.getReturnType2().accept(this);
				print(" " + fnFqn + "(");
				printed = 0;
				if (!isStatic(node)) {
					print(cn + " _this");
					printed++;
				}
				for (Object o : node.parameters()) {
					if (printed > 0) {
						print(", ");
					}
					visit((SingleVariableDeclaration)o);
					printed++;
				}
				println(") {");
				indent++;
				println("assert(false, \"Unimplemented native method: " + fqn + "\");");
				indent--;
				println("}");
				indent = oldIndent;
				popWriter();
			} else {
				inMethod = true;
				node.getBody().accept(this);
			}
		}
		return false;
	}
	
	@Override
	public void endVisit(MethodDeclaration node) {
		inMethod = false;
		locals.clear();
		if (node.getBody() != null || isNative(node)) {
			if (isSynchronized(node)) {
				indent--;
				println("}");
			}
			indent--;
			println("}");
		}
		println(additionalMethod);
		additionalMethod = "";
	}

	@Override
	public boolean visit(MethodInvocation node) {
		//System.out.println("Found: " + node.getClass());
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			print(".");
		}
		node.getName().accept(this);
		print("(");
		int printed = 0;
		if (node.arguments().size() > 0) {
			for (Object o : node.arguments()) {
				if (printed > 0) {
					print(", ");
				}
				//visit((Expression)o);
				((Expression)o).accept(this);
				//println("[[["+o.toString()+ " -- " + o.getClass() +"]]]");
				printed++;
			}
		}
		print(")");
		return false;
	}

	@Override
	public boolean visit(MethodRef node) {
		System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(Modifier node) {
		//System.out.println("Found: " + node.getClass());
		// Deal with synchronized separately
		if (!node.toString().equals("synchronized") && !node.toString().equals("native")) {
			// TODO Probably needs doing for VarDecls
			// TODO package visibility
			// NOTE If you change anything related to @... you should update the
			// sorting thing to move UDAs to the start of modifier lists
			if (node.getParent() instanceof FieldDeclaration && node.toString().equals("final")) {
				print("/*final*/ ");
			} else if (node.toString().equals("transient")) {
				// TODO This will probably change. Mostly there so it's greppable/doesn't compile
				print("@_j2d_transient ");
			} else if (node.toString().equals("strictfp")) {
				print("@_j2d_strictfp ");
			} else if (node.toString().equals("volatile")) {
				// TODO Should this be shared?
				print("@_j2d_volatile ");
			} else if (node.toString().equals("static")) {
				// Static is TLS in D
				print("__gshared static ");
			} else {
				print(node.toString() + " ");
			}
		}
		return false;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		//System.out.println("Found: " + node.getClass());
		print("null");
		return false;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		//System.out.println("Found: " + node.getClass());
		String number = node.toString();
		// Floating point literals are doubles by default in D
		if (number.endsWith("D") || number.endsWith("d")) {
			number = number.substring(0, number.length() - 1);
		}
		// 10l -> 10L
		if (number.endsWith("l")) {
			number = number.substring(0, number.length() - 1) + "L";
		}
		print(number);
		return false;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		//System.out.println("Found: " + node.getClass());

		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		print("module ");
		Writer w = new StringWriter();
		pushWriter(w);
		node.getName().accept(this);
		popWriter();
		
		packageName = w.toString();
		
		println(packageName + "." + moduleName + ";");
		return false;
	}
	
	@Override
	public boolean visit(ParameterizedType node) {
		//System.out.println("Found: " + node.getClass());
		boolean wildcard = false;
		for (Object o : node.typeArguments()) {
			if (o instanceof WildcardType) {
				wildcard = true;
			}
		}
		if (wildcard) {
			print("_j2d_I_");
			node.getType().accept(this);
			return false;
		}
		node.getType().accept(this);
		print("!(");
		int printed = 0;
		
		//println("@@@@@@@@@@@@@@@@@");
		//println(node.getParent().getClass().toString());
		//println("@@@@@@@@@@@@@@@@@");
		if (node.getParent() instanceof ClassInstanceCreation) {
			// Use this instead of node.typeArguments() to support the diamond operator
			for (ITypeBinding tb : node.resolveBinding().getTypeArguments()) {
				if (printed > 0) {
					print(", ");
				}
				// TODO This is a complete hack. We need to build an AST really.
				String s = tb.getName().replaceAll("\\?[^>]*?>", "JavaObject)")
									   .replace("<", "!(")
									   .replace(">", ")");
				print(fixKeywords(s));
				//ASTNode n = ((CompilationUnit)node.getRoot()).findDeclaringNode(tb);
				//getType(node.getParent().getAST(), tb).accept(this);
				printed++;
			}
		} else {
			for (Object o : node.typeArguments()) {
				if (printed > 0) {
					print(", ");
				}
				((Type)o).accept(this);
				printed++;
			}
		}
		print(")");
		return false;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		//System.out.println("Found: " + node.getClass());
		print("(");
		node.getExpression().accept(this);
		print(")");
		return false;
	}

	@Override
	public boolean visit(PostfixExpression node) {
		//System.out.println("Found: " + node.getClass());
		node.getOperand().accept(this);
		print(node.getOperator().toString());
		return false;
	}

	@Override
	public boolean visit(PrefixExpression node) {
		//System.out.println("Found: " + node.getClass());
		print(node.getOperator().toString());
		node.getOperand().accept(this);
		return false;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		//System.out.println("Found: " + node.getClass());
		if (node.getPrimitiveTypeCode() == PrimitiveType.BOOLEAN) {
			print("bool");
		} else {
			print(node.toString());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		//System.out.println("Found: " + node.getClass() + " " + node);
		node.getQualifier().accept(this);
		print("." + fixKeywords(node.getName().toString()));
		return false;
	}

	@Override
	public boolean visit(QualifiedType node) {
		System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		//System.out.println("Found: " + node.getClass());
		print("return ");
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		}
		println(";");
		return false;
	}
	
	private String doRewrites(String s) {
		if (inFieldAccess || (doRewrite && varRewrites.contains(s) && !locals.contains(s))) {
			return "_" + s;
		} else if (rewriteMethods && methodRewrites.contains(s)) {
			return s + "Impl";
		}
		return s;
	}

	@Override
	public boolean visit(SimpleName node) {
		//System.out.println("Found: " + node.getClass() + " " + node);
		print(fixKeywords(doRewrites(node.toString())));
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleType node) {
		//System.out.println("Found: " + node.getClass() + " " + node);
		if (forceJavaObject && node.resolveBinding().isTypeVariable()) {
			print("JavaObject");
			return false;
		}
		
		if (node.resolveBinding().isRawType()) {
			print("_j2d_I_");
			if (node.getName() instanceof QualifiedName) {
				((QualifiedName)node.getName()).getName().accept(this);
			} else {
				node.getName().accept(this);
			}
			return false;
		}
		
		if (isStatic(node.resolveBinding()) &&
			node.resolveBinding().getDeclaringClass() != null) {
			ITypeBinding itb = node.resolveBinding().getDeclaringClass();
			if (itb.isRawType() && node.getName() instanceof QualifiedName) {
				QualifiedName qn = (QualifiedName)node.getName();
				qn.getQualifier().accept(this);
				print("!(");
				int printed = 0;
				for (int i = 0; i < itb.getTypeDeclaration().getTypeParameters().length; i++) {
					if (printed > 0) {
						print(", ");
					}
					// The type doesn't matter really, as the static class
					// is independent of the template
					print("JavaObject");
					printed++;
				}
				print(").");
				qn.getName().accept(this);
				return false;
			}
		}
		
		Writer w = new StringWriter();
		pushWriter(w);
		node.getName().accept(this);
		popWriter();

		String type = node.resolveBinding().getBinaryName();

		if (node.getName() instanceof QualifiedName) {
			QualifiedName qn = (QualifiedName) node.getName();

			if (type.indexOf('$') != -1) {
				String cls = type.substring(0, type.indexOf("$"));
				if (!inTypes.contains(cls)) {
					int idx = cls.lastIndexOf('.');
					if (idx == -1) {
						print(w.toString());
						print(".");
						print(fixKeywords(cls));
					} else {
						print(fixKeywords(w.toString()));
						//String className = type.substring(idx + 1, type.indexOf('$'));
						//String fixedClassName = fixKeywords(className);
						//print(w.toString().replace(className + ".", fixedClassName + "." + fixedClassName + "."));
					}
				} else {
					print(w.toString());
				}
			} else {
				print(w.toString());
				if (!inTypes.contains(type)) {
					print(".");
					qn.getName().accept(this);
				}
			}
		} else {
			print(w.toString());
		}
		//if (node.resolveBinding().isRawType()) {
		//	print("!(JavaObject)");
		//}
		return false;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		//System.out.println("Found: " + node.getClass());
		print("@");
		node.getTypeName().accept(this);
		print("(");
		node.getValue().accept(this);
		println(")");
		return false;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		//System.out.println("Found: " + node.getClass() + " " + node);
		node.getType().accept(this);
		if (node.isVarargs()) {
			// TODO This is hacky, we should make a new node and visit it
			print("[]");
		}
		print(" ");
		node.getName().accept(this);
		
		//ASTRewrite rewriter = ASTRewrite.create(node.getAST());
		
		//rewriter.
		if (node.isVarargs()) {
			print("...");
		}
		return false;
	}

	@Override
	public boolean visit(StringLiteral node) {
		//System.out.println("Found: " + node.getClass());
		//print(node.getLiteralValue());
		print(node.getEscapedValue());
		print("w");
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		//System.out.println("Found: " + node.getClass());
		// TODO Type arguments
		print("super(");
		int printed = 0;
		for (Object o : node.arguments()) {
			Expression t = (Expression)o;
			if (printed > 0) {
				print(", ");
			}
			t.accept(this);
			printed++;
		}
		println(");");
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		//System.out.println("Found: " + node.getClass());
		// TODO check this is right
		if (node.getQualifier() != null) {
			print("super.outer");
		} else {
			print("super.");
			node.getName().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		//System.out.println("Found: " + node.getClass());
		// TODO Type arguments
		if (node.getQualifier() != null) {
			//node.getQualifier().accept(this);
			//print(".");
			// TODO This probably isn't always correct.
			print("super.outer");
			return false;
		}
		print("super.");
		node.getName().accept(this);
		print("(");
		int printed = 0;
		for (Object o : node.arguments()) {
			Expression t = (Expression)o;
			if (printed > 0) {
				print(", ");
			}
			t.accept(this);
			printed++;
		}
		print(")");
		return false;
	}

	@Override
	public boolean visit(SwitchCase node) {
		//System.out.println("Found: " + node.getClass());
		if (node.isDefault()) {
			println("default:");
		} else {
			print("case ");
			node.getExpression().accept(this);
			println(":");
		}
		// Case fallthrough
		SwitchStatement ss = (SwitchStatement)node.getParent();
		List statements = ss.statements();
		if (statements.indexOf(node) < statements.size() - 1) {
			Object nextSibling = statements.get(statements.indexOf(node) + 1);
			if (nextSibling instanceof SwitchCase) {
				//indent++;
				println("goto " + (((SwitchCase)nextSibling).isDefault() ? "default;" : "case;" ));
				//indent--;
				return false;
			}
		}
		// TODO Get indentation right
		//indent++;
		return false;
	}
	
	@Override
	public void endVisit(SwitchCase node) {
		//indent--;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		//System.out.println("Found: " + node.getClass());
		print("switch (");
		node.getExpression().accept(this);
		println(") {");
		indent++;
		for (Object o : node.statements()) {
			((Statement)o).accept(this);
		}
		indent--;
		println("}");
		return false;
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		//System.out.println("Found: " + node.getClass());
		print("synchronized (");
		node.getExpression().accept(this);
		println(") {");
		indent++;
		node.getBody().accept(this);
		indent--;
		println("}");
		return false;
	}

	@Override
	public boolean visit(TagElement node) {
		System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(TextElement node) {
		System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		//System.out.println("Found: " + node.getClass());
		// TODO This doesn't necessarily always work.
		//      eg. A : B, B : C, then in A, C.this
		if (node.getQualifier() != null) {
			print("this.outer");
		} else {
			print("this");
		}
		return false;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		//System.out.println("Found: " + node.getClass());
		print("throw ");
		node.getExpression().accept(this);
		println(";");
		return false;
	}

	@Override
	public boolean visit(TryStatement node) {
		//System.out.println("Found: " + node.getClass());
		if (node.resources().size() > 0) {
			Stack<String> finalize = new Stack<>();
			for (Object o : node.resources()) {
				VariableDeclarationExpression vde = (VariableDeclarationExpression)o;
				vde.accept(this);
				println(";");
				//finalize.add(vde.)
				for (Object vdf : vde.fragments()) {
					finalize.add(((VariableDeclarationFragment)vdf).getName().toString());
				}
			}
			for (String s : finalize) {
				println("scope (exit) {");
				indent++;
				println("if (" + fixKeywords(s) + " != null) {");
				indent++;
				println(fixKeywords(s) + ".close();");
				indent--;
				println("}");
				indent--;
				println("}");
			}
		}
		boolean hasOther = node.catchClauses().size() > 0 || node.getFinally() != null;
		if (hasOther) {
			println("try {");
		}
		indent++;
		node.getBody().accept(this);
		indent--;
		// TODO This does't work for try{}catch{}
		if (hasOther) {
			print("} ");
		}
		for (Object o : node.catchClauses()) {
			((CatchClause)o).accept(this);
		}
		if (node.getFinally() != null) {
			println("finally {");
			indent++;
			node.getFinally().accept(this);
			indent--;
			println("}");
		} else {
			println("");
		}
		
		return false;
	}
	
	private void printModifiers(BodyDeclaration node) {
		printModifiers(node.modifiers());
	}
	
	@SuppressWarnings("unchecked")
	private void printModifiers(List l) {
		// Move modifiers that turn into UDAs to the start
		ArrayList<Object> al = new ArrayList<>(l);
		Collections.sort(al, new Comparator<Object>(){
			@Override
			public int compare(Object o1, Object o2) {
				for (Object o : new Object[] { o1, o2 }) {
					if (o instanceof Modifier) {
						if (o.toString().equals("transient") ||
							o.toString().equals("strictfp")  ||
							o.toString().equals("volatile")) {
							return o == o1 ? -1 : 1;
						}
					} else if (o instanceof MarkerAnnotation) {
						MarkerAnnotation ma = (MarkerAnnotation)o;
						String name = ma.getTypeName().getFullyQualifiedName();
						if (name.equals("Override") ||
							name.equals("Deprecated")) {
							return o == o1 ? 1 : -1;
						}
					}
				}
				return 0;
			}
		});

		for (Object o : al) {
			//println(o.getClass().toString());
			if (o instanceof Modifier) {
				visit((Modifier)o);
			} else if (o instanceof SingleMemberAnnotation) {
				((SingleMemberAnnotation)o).accept(this);
			} else if (o instanceof NormalAnnotation) {
				((NormalAnnotation)o).accept(this);
			} else if (o instanceof MarkerAnnotation) {
				((MarkerAnnotation)o).accept(this);
			} else {
				throw new RuntimeException();
			}
		}
	}
	
	private boolean isSynchronized(BodyDeclaration node) {
		return (node.getModifiers() & Modifier.SYNCHRONIZED) != 0;
	}
	
	private boolean isNative(BodyDeclaration node) {
		return (node.getModifiers() & Modifier.NATIVE) != 0;
	}
	
	private boolean isStatic(BodyDeclaration node) {
		return (node.getModifiers() & Modifier.STATIC) != 0;
	}

	private boolean isStatic(ITypeBinding node) {
		return (node.getModifiers() & Modifier.STATIC) != 0;
	}
	
	/**
	 * In Java, it's valid to have a field and method with the same
	 * name.
	 * 
	 * If a method and field have the same name, rewrite the field
	 * to _fieldName.
	 * @param node
	 */
	private void fixNames(TypeDeclaration node) {
		List<FieldDeclaration> fds = Arrays.asList(node.getFields());
		List<MethodDeclaration> mds = Arrays.asList(node.getMethods());
		// TODO Make this O(m log n) rather than O(m x n)
		for (FieldDeclaration fd : fds) {
			for (VariableDeclarationFragment vdf : (List<VariableDeclarationFragment>)fd.fragments()) {
				for (MethodDeclaration md : mds) {
					if (md.getName().toString().equals(vdf.getName().toString())) {
						varRewrites.add(vdf.getName().toString());
					}
				}
			}
		}
		for (MethodDeclaration md : mds) {
			if (md.typeParameters().size() > 0) {
				for (MethodDeclaration md2 : mds) {
					if (md2 != md && md2.getName().toString().equals(md.getName().toString())) {
						methodRewrites.add(md.getName().toString());
					}
				}
			}
		}
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		//System.out.println("Found: " + node.getClass());
		
		if (node.typeParameters().size() > 0) {
			// TODO Remove once indentation with writers is fixed.
			int oldIndent = indent;
			pushWriter(generatedInterfaces);
			indent = 0;
			println("// Generated interface for wildcard types");
			print("interface _j2d_I_");
			// TODO This won't work if there are two classes called X, needs fqn.
			node.getName().accept(this);
			println(" {}");
			indent = oldIndent;
			popWriter();
		}
		
		inTypes.push(node.resolveBinding().getBinaryName());
		printModifiers(node);
		
		if (node.isInterface()) {
			print("interface ");
			node.getName().accept(this);
		} else {
			print("class ");
			node.getName().accept(this);
		}

		if (node.typeParameters().size() > 0) {
			print("(");
			int printed = 0;
			for (Object o : node.typeParameters()) {
				if (printed > 0) {
					print(", ");
				}
				((TypeParameter)o).accept(this);
				printed++;
			}
			if (!additionalTemplateParams.isEmpty()) {
				for (String s : additionalTemplateParams) {
					print(", " + s);
				}
				additionalTemplateParams.clear();
			}
			print(")");
		}
		
		if (node.typeParameters().size() > 0 ||
			((!node.isInterface() || node.superInterfaceTypes().size() != 0) &&
				!node.getName().toString().equals("Object"))) {
			print(" : ");
		}
		
		int printed = 0;
		if (node.getSuperclassType() != null) {
			if (node.getSuperclassType().toString().equals("Object")) {
				print("JavaObject");
			} else {
				node.getSuperclassType().accept(this);
				//print(node.getSuperclassType().toString());
			}
			printed++;
		} else {
			if (node.resolveBinding().getQualifiedName().equals("java.lang.Object")) {
				// TODO This is a horrible hack, but unfortunately there's
				//      no way around it due to D's lack of multiple inheritance
				// How this looks:
				//  class JavaObject : Exception {}
				// What it should look like:
				//  class JavaObject {}
				//  class JavaThrowable : JavaObject, Throwable {}
				print(": Exception");
				printed++;
			} else if (!node.isInterface()) {
				print("JavaObject");
				printed++;
			}
		}
		
		if (node.superInterfaceTypes().size() > 0) {
			for (Object o : node.superInterfaceTypes()) {
				if (printed > 0 || !node.isInterface()) {
					print(", ");
				}
				//print(o.toString());
				((Type)o).accept(this);
				printed++;
			}
		}
		if (node.typeParameters().size() > 0) {
			if (printed > 0) {
				print(", ");
			}
			print("_j2d_I_");
			node.getName().accept(this);
		}
		if (constraints.length() > 0) {
			println("");
			indent += 2;
			print("if (" + constraints + ")");
			indent -= 2;
			constraints = "";
		}
		println(" {");
		indent++;
		
		fixNames(node);
		
		for(Object o : node.bodyDeclarations()) {
			((BodyDeclaration)o).accept(this);
			println("");
		}
		return false;
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		if (fieldInitializers.size() > 0) {
			println("private int _j2d_noCalls = 0;");
			println("private void _j2d_intializeFields() {");
			indent++;
			println("// Make sure we don't overwrite fields when ctors call other ctors");
			println("if (!_j2d_noCalls) {");
			indent++;
			for (String s : fieldInitializers) {
				println(s);
			}
			indent--;
			println("}");
			println("_j2d_noCalls++;");
			indent--;
			println("}");
			fieldInitializers.clear();
		}
		inTypes.pop();
		indent--;
		println("}");
	}
	@Override
	public boolean visit(TypeDeclarationStatement node) {
		// TODO
		System.out.println("Found: " + node.getClass());
		return false;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		//System.out.println("Found: " + node.getClass());
		// TODO I don't know if this is the right way to do this
		print("typeid(");
		node.getType().accept(this);
		print(")");
		return false;
	}

	@Override
	public boolean visit(TypeParameter node) {
		//System.out.println("Found: " + node.getClass());
		node.getName().accept(this);
		if (node.typeBounds().size() == 1) {
			// TODO re-add this.
			//print(" : ");
			//((Type)node.typeBounds().get(0)).accept(this);
		} else if (node.typeBounds().size() > 1) {
			StringWriter sw = new StringWriter();
			pushWriter(sw);
			int printed = 0;
			for (Object o : node.typeBounds()) {
				Type t = (Type)o;
				if (printed > 0) {
					print(" && ");
				}
				print("is(");
				node.getName().accept(this);
				print(" : ");
				t.accept(this);
				print(")");
				printed++;
			}
			popWriter();
			if (constraints.length() > 0) {
				constraints += " && " + sw.toString();
			} else {
				constraints = sw.toString();
			}
		}
		return false;
	}

	@Override
	public boolean visit(UnionType node) {
		System.out.println("Found: " + node.getClass());
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		//System.out.println("Found: " + node.getClass());
		node.getType().accept(this);
		int printed = 0;
		if (node.fragments().size() > 0) {
			for (Object o : node.fragments()) {
				if (printed > 0) {
					print(",");
				}
				visit((VariableDeclarationFragment)o);
				printed++;
			}
		}
		return false;
	}
	
	private static final class NodeHolder {
		private ASTNode node = null;

		public ASTNode getNode() {
			return node;
		}

		public void setNode(ASTNode node) {
			this.node = node;
		}
	}
	
	/**
	 * Returns the start position of the node's next sibling, or
	 * the end position of the parent node if said sibling does not
	 * exist. If the parent node is the CompilationUnit, return -1.
	 * 
	 * @param node
	 * @return
	 */
	private int siblingOrParentPosition(final ASTNode node) {
		final ASTNode parent = node.getParent();
		final NodeHolder siblingHolder = new NodeHolder();
		// Bleugh. Is proper tree navigation too much to ask for? :<
		parent.accept(new ASTVisitor() {
			private boolean nextNode = false;
			@Override
			public boolean preVisit2(ASTNode n) {
				if (nextNode) {
					siblingHolder.setNode(n);
					nextNode = false; // TODO Shortcut this.
				}
				if (n == node) {
					nextNode = true;
				}
				if (n == parent) {
					return true;
				}
				return false;
			}
		});
		if (siblingHolder.getNode() == null) {
			if (parent instanceof CompilationUnit) {
				return -1;
			}
			//return siblingOrParentPosition(parent);
			//return -1;
			return parent.getStartPosition() + parent.getLength();
		} else {
			return siblingHolder.getNode().getStartPosition();
		}
	}
	
	/**
	 * Position of the end of the last node
	 */
	private int endOfLastNode = -1;
	
	private void doComments(ASTNode node, boolean post) {
		Comment c = comments.peek();

		while (c != null && c.getStartPosition() > endOfLastNode) {
			int startOfNextNode = post ? siblingOrParentPosition(node) 
					 				   : node.getStartPosition();

			if (c.getStartPosition() + c.getLength() < startOfNextNode) {
				comments.remove().accept(this);
				c = comments.peek();
			} else {
				break;
			}
		}
		endOfLastNode = node.getStartPosition() + node.getLength();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		// Skip CU
		if (node instanceof CompilationUnit) {
			return true;
		}
		
		if ((node instanceof EnumDeclaration || node instanceof TypeDeclaration) &&
			 node.getParent() instanceof CompilationUnit) {
			println("");
			println("// Implicit imports");

			// java.lang.* is implicitly imported in java
			println("import java.lang.all;");

			// Current package is implicitly imported in java
			if (packageName.length() != 0 && !packageName.equals("java.lang")) {
				println("import " + packageName + ".all;");
			} else {
				// TODO implicit imports in default package
			}

			// Certain things generated need some code support
			println("import j2d.core;");
			println("");
		}
		
		// Do JavaDoc
		if (node instanceof BodyDeclaration) {
			BodyDeclaration bd = (BodyDeclaration)node;
			if (bd.getJavadoc() != null) {
				bd.getJavadoc().accept(this);
			}
		}
		// Insert doc comments
		doComments(node, false);

		return true;
	}
	
	@Override
	public void postVisit(ASTNode node) {
		//super.postVisit(node);
		// Skip CU
		if (node instanceof CompilationUnit || node instanceof Comment) {
			return;
		}
		doComments(node, true);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		//System.out.println("Found: " + node.getClass());
		print(" ");
		Writer w = new StringWriter();
		pushWriter(w);
		if (inMethod) {
			doRewrite = false;
		}
		node.getName().accept(this);
		if (inMethod) {
			doRewrite = true;
		}
		popWriter();
		locals.add(w.toString());
		print(w.toString());
		Expression e = node.getInitializer();
		if (e != null) {
			if (node.getParent() instanceof FieldDeclaration) {
				FieldDeclaration parent = (FieldDeclaration)node.getParent();
				if (e instanceof NumberLiteral ||
					e instanceof BooleanLiteral ||
					e instanceof StringLiteral) {
					;
				} else {
					String initializer = w.toString() + " = ";
					w = new StringWriter();
					pushWriter(w);
					e.accept(this);
					popWriter();
					initializer += w.toString() + ";";
					if (isStatic(parent)) {
						staticFieldInitializers.add(initializer);
					} else {
						fieldInitializers.add(initializer);
					}
					return false;
				}
			}
			print(" = ");
			// TODO Things that can't be computed at compile time need
			//      moving into a static ctor
			e.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		//System.out.println("Found: " + node.getClass() + " " + node.toString());
		printModifiers(node.modifiers());
		node.getType().accept(this);
		int printed = 0;
		for (Object o : node.fragments()) {
			if (printed > 0) {
				print(",");
			}
			((VariableDeclarationFragment)o).accept(this);
			printed++;
		}
		return false;
	}
	
	@Override
	public void endVisit(VariableDeclarationStatement node) {
		println(";");
	}

	@Override
	public boolean visit(WhileStatement node) {
		//System.out.println("Found: " + node.getClass());
		print("while (");
		node.getExpression().accept(this);
		println(") {");
		indent++;
		node.getBody().accept(this);
		indent--;
		println("}");
		return false;
	}

	@Override
	public boolean visit(WildcardType node) {
		//System.out.println("Found: " + node.getClass() + " " + node);
		//println();
		ASTNode parent = node.getParent();
		while (parent instanceof ParameterizedType) {
			parent = parent.getParent();
		}
		//if (parent instanceof FieldDeclaration) {
			print("JavaObject");
			return false;
		//}
		/*String ident = genId();
		additionalTemplateParams.add(ident);
		
		print(ident);
		if (node.getBound() != null) {
			StringWriter sw = new StringWriter();
			pushWriter(sw);
			node.getBound().accept(this);
			popWriter();
			String append;
			if (node.isUpperBound()) {
				append =  "is(" + ident + " : " + sw + ")";
			} else {
				append = "is(" + sw + " : " + ident + ")";
			}
			if (constraints.length() > 0) {
				constraints += " && " + append;
			} else {
				constraints = append;
			}
		}
		return false;*/
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
