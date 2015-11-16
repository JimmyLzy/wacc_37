package WACC;
// import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.*;

// import antlr package (your code)
import antlr.*;
import org.antlr.v4.runtime.tree.gui.SystemFontMetrics;

import java.util.BitSet;

public class Main {

    public static void main(String[] args) throws Exception {

        // create a CharStream that reads from standard input
        ANTLRInputStream input = new ANTLRInputStream(System.in);

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

        System.out.println(tree.toStringTree(parser));

        // build and run my custom visitor
        MyVisitor visitor = new MyVisitor();
        AST.ASTNode astNode = visitor.visit(tree);
        astNode.check();
        System.out.println("Semantic check finish!");

    }
}
