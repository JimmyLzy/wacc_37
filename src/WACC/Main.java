package WACC;
// import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// import antlr package (your code)
import antlr.*;

import java.io.PrintWriter;


public class Main {

    public static void main(String[] args) throws Exception {

        // create a CharStream that reads from standard input
        ANTLRInputStream input = new ANTLRFileStream(args[0]);

        // create a lexer that feeds off of input CharStream
        BasicLexer lexer = new BasicLexer(input);

        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create a parser that feeds off the tokens buffer
        BasicParser parser = new BasicParser(tokens);

        ParseTree tree = parser.program(); // begin parsing at program rule

        if(parser.getNumberOfSyntaxErrors() > 0) {
            System.out.println("#syntax_error#");
            System.exit(100);
        }

        // build and run my custom visitor
        MyVisitor visitor = new MyVisitor();

        AST.ASTNode astNode = visitor.visit(tree);

        astNode.check();
        System.out.println("Semantic check finished");

        String fileName = args[0].substring(args[0].lastIndexOf("/") + 1, args[0].length() - "WACC".length());

        StringBuilder mainStringBuilder = new StringBuilder();
        StringBuilder headerStringBuilder = new StringBuilder();
        StringBuilder labelStringBuilder = new StringBuilder();
        StringBuilder functionStringBuilder = new StringBuilder();

        astNode.generate(headerStringBuilder, mainStringBuilder, labelStringBuilder, functionStringBuilder);
        PrintWriter fileWriter = new PrintWriter(fileName + "s");
        fileWriter.println(headerStringBuilder);
        fileWriter.println(mainStringBuilder);
        fileWriter.println(labelStringBuilder);
        fileWriter.println(functionStringBuilder);

        System.out.println(headerStringBuilder);
        System.out.println();
        System.out.println(mainStringBuilder);
        System.out.println();
        System.out.println(labelStringBuilder);
        System.out.println();
        System.out.println(functionStringBuilder);

        fileWriter.close();
    }
}
