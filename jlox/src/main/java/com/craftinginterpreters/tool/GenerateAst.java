package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Based on a mini-grammar, generate a set of classes/sublasses in a file called Expr
 */
public class GenerateAst {

    // size of single level of indentation
    final static String indent = "    ";

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println(indent + "static class " + className + " extends " + baseName +" {");

        // Constructor
        writer.println(indent + indent + className + "(" + fieldList + ") {");

        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println(indent + indent + indent + "this." + name + " = " + name + ";");
        }
        writer.println(indent + indent + "}");

        // Visitor pattern
        writer.println();
        writer.println(indent + indent + "@Override");
        writer.println(indent + indent + "<R> R accept(Visitor<R> visitor) {");
        writer.println(indent + indent + indent + "return visitor.visit" + className + baseName + "(this);");
        writer.println(indent + indent + "}");

        // Fields
        writer.println();
        for (String field : fields) {
            writer.println(indent + indent + "final " + field + ";");
        }
        writer.println(indent + "}");
        writer.println();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println(indent + "interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(indent + indent + "R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println(indent + "}");
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {

        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // The base accept() method.
        writer.println(indent + "abstract <R> R accept(Visitor<R> visitor);");
        writer.println("}");
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java GenerateAst <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary     : Expr left, Token operator, Expr right",
                "Grouping   : Expr expression",
                "Literal    : Object value",
                "Unary      : Token operator, Expr right"
        ));
    }
}
