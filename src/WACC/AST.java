package WACC;

import java.util.HashMap;
import java.util.List;

/**
 * Created by yh6714 on 13/11/15.
 */
public class AST {

    public abstract class ASTNode {

        private HashMap<String, ASTNode> symbolTable = new HashMap<>();

        private ASTNode parent;

        public ASTNode getParent() {
            return parent;
        }

        public HashMap<String, ASTNode> getSymbolTable() {
            return symbolTable;
        }

        public void setParent(ASTNode parent) { this.parent = parent; }

        abstract public void check();
    }

    private AST ast;

    public class ProgramNode extends ASTNode {
        private List<FuncNode> functionNodes;
        private StatNode statNode;

        public ProgramNode(List<FuncNode> functionNodes, StatNode statNode) {
            this.functionNodes = functionNodes;
            this.statNode = statNode;
        }

        @Override
        public void check() {
            for (FuncNode funcNode : functionNodes) {
                funcNode.check();
            }
            statNode.check();
        }
    }

    public class FuncNode extends ASTNode {

        private TypeNode typeNode;
        private IdentNode identNode;
        private List<ParamNode> paramNodes;
        private StatNode statNode;

        public FuncNode(TypeNode typeNode, IdentNode identNode, List<ParamNode> paramNodes, StatNode statNode) {
            this.typeNode = typeNode;
            this.statNode = statNode;
            this.paramNodes = paramNodes;
            this.identNode = identNode;
        }

        @Override
        public void check() {
            typeNode.check();
            identNode.check();
            for (ParamNode paramNode : paramNodes) {
                paramNode.check();
            }
            statNode.check();
        }
    }

    public class Param_listNode extends ASTNode {

    }

    public class StatNode extends ASTNode {

    }
    public class Assign_lhsNode extends ASTNode {

    }
    public class Assign_rhsNode extends ASTNode {

    }
    public class Arg_listNode extends ASTNode {

    }
    public class Pair_elemNode extends ASTNode {

    }
    public class TypeNode extends ASTNode {

    }

    public class Base_typeNode extends ASTNode {

        @Override
        void check() {

        }
    }
    public class Array_typeNode extends ASTNode {

    }
    public class ParamNode extends ASTNode {

        public ASTNode getTypeNode() {
        }
    }
    public class Pair_typeNode extends ASTNode {

    }
    public class Pair_elem_typeNode extends ASTNode {

    }
    public class Unary_operNode extends ASTNode {

        private

        @Override
        void check() {

        }

    }
    public class Binary_operNode extends ASTNode {

    }
    public class IdentNode extends ASTNode {

        private String ident;

        public IdentNode(String ident) {
            this.ident = ident;
        }

        @Override
        public void check() {
        }
    }
    public class Array_elemNode extends ASTNode {

    }
    public class Int_literNode extends ASTNode {

    }
    public class DigitNode extends ASTNode {

    }
    public class Int_signNode extends ASTNode {

    }
    public class Bool_literNode extends ASTNode {

    }
    public class Char_literNode extends ASTNode {

    }
    public class Str_literNode extends ASTNode {

    }
    public class CharacterNode extends ASTNode {

    }
    public class Escaped_charNode extends ASTNode {

    }
    public class Array_literNode extends ASTNode {

    }
    public class Pair_literNode extends ASTNode {

    }
    public class CommentNode extends ASTNode {

    }

}
