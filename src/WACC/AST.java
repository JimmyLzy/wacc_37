package WACC;

import antlr.BasicParser;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.HashMap;
import java.util.List;

/**
 * Created by yh6714 on 13/11/15.
 */
public class AST {

    private AST ast;
    private ASTNode root;

    public void setRoot(ASTNode root) {

        this.root = root;
    }

    public abstract class ASTNode {

        protected HashMap<String, ASTNode> symbolTable = new HashMap<>();

        protected ASTNode parent;

        public ASTNode getParent() {
            return parent;
        }

        public HashMap<String, ASTNode> getSymbolTable() {
            return symbolTable;
        }

        public void setParent(ASTNode parent) {
            this.parent = parent;
        }

        public void check() {
        }

    }

    public class ProgramNode extends ASTNode {

        private List<FuncNode> functionNodes;
        private StatNode sub_statNode;

        public ProgramNode(List<FuncNode> functionNodes, StatNode sub_statNode) {

            this.functionNodes = functionNodes;
            for (FuncNode funcNode : functionNodes) {
                funcNode.setParent(this);
            }
            this.sub_statNode = sub_statNode;
            sub_statNode.setParent(this);
        }

        @Override
        public void check() {

            for (FuncNode funcNode : functionNodes) {
                funcNode.check();
            }
            sub_statNode.check();
        }

    }

    public class FuncNode extends ASTNode {

        private TypeNode typeNode;
        private IdentNode identNode;
        private List<ParamNode> paramNodes;
        private StatNode statNode;

        public FuncNode(TypeNode typeNode, IdentNode identNode, List<ParamNode> paramNodes, StatNode statNode) {
            this.typeNode = typeNode;
            typeNode.setParent(this);
            this.statNode = statNode;
            statNode.setParent(this);
            this.paramNodes = paramNodes;
            if (!paramNodes.isEmpty()) {
                for (ParamNode paramNode : paramNodes) {
                    paramNode.setParent(this);
                }
            }
            this.identNode = identNode;
            identNode.setParent(this);
        }

        @Override
        public void check()  {
            typeNode.check();
            identNode.check();
            for (ParamNode paramNode : paramNodes) {
                paramNode.check();
            }
            statNode.check();
        }
    }


    public class StatNode extends ASTNode {

        protected String command;
        protected StatNode sub_statNode;
        protected ASTNode returnNode;

        public StatNode() {
            command = "";
        }

        public StatNode(StatNode sub_statNode, ASTNode returnNode) {
            this.sub_statNode = sub_statNode;
            this.returnNode = returnNode;
        }

        public String getCommand() {
            return command;
        }

    }

    public class Sub_StatNode extends StatNode {

        protected String command;

        public Sub_StatNode() {
            command = "";
        }

        public String getCommand() {
            return command;
        }

    }

    public class SkipNode extends Sub_StatNode {

        public SkipNode() {
            command = "skip";
        }

    }

    public class DeclarationNode extends Sub_StatNode {

        private TypeNode typeNode;
        private IdentNode identNode;
        private ASTNode assign_rhsNode;

        public DeclarationNode(TypeNode typeNode, IdentNode identNode, ASTNode assign_rhsNode) {

            command = "declaration";
            this.typeNode = typeNode;
            typeNode.setParent(this);
            this.identNode = identNode;
            identNode.setParent(this);
            this.assign_rhsNode = assign_rhsNode;
            assign_rhsNode.setParent(this);

        }

        @Override
        public void check() {
        }
    }

    public class AssignmentNode extends Sub_StatNode {

        private ASTNode assign_lhsNode;
        private ASTNode assign_rhsNode;

        public AssignmentNode(ASTNode assign_lhsNode, ASTNode assign_rhsNode) {

            command = "assignment";
            this.assign_lhsNode = assign_lhsNode;
            assign_lhsNode.setParent(this);
            this.assign_rhsNode = assign_rhsNode;
            assign_rhsNode.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class ReadNode extends Sub_StatNode {

        private ASTNode assign_lhsNode;

        public ReadNode(ASTNode assign_lhsNode) {

            command = "read";
            this.assign_lhsNode = assign_lhsNode;
            assign_lhsNode.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class FreeNode extends Sub_StatNode {

        private ExprNode exprNode;

        public FreeNode(ExprNode exprNode) {

            command = "free";
            this.exprNode = exprNode;
            exprNode.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class ReturnNode extends StatNode {

        private ExprNode exprNode;

        public ReturnNode(ExprNode exprNode) {

            this.exprNode = exprNode;
            exprNode.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class ExitNode extends StatNode {

        private ExprNode exprNode;

        public ExitNode(ExprNode exprNode) {

            this.exprNode = exprNode;
            exprNode.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class PrintNode extends Sub_StatNode {

        private ExprNode exprNode;

        public PrintNode(ExprNode exprNode) {

            command = "print";
            this.exprNode = exprNode;
            exprNode.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class PrintlnNode extends Sub_StatNode {

        private ExprNode exprNode;

        public PrintlnNode(ExprNode exprNode) {

            command = "println";
            this.exprNode = exprNode;
            exprNode.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class IfNode extends Sub_StatNode {

        private ExprNode exprNode;
        private StatNode if_sub_statNodeTrue;
        private StatNode if_sub_statNodeFalse;

        public IfNode(ExprNode exprNode, StatNode if_sub_statNodeTrue, StatNode if_sub_statNodeFalse) {

            command = "if";
            this.exprNode = exprNode;
            exprNode.setParent(this);
            this.if_sub_statNodeTrue = if_sub_statNodeTrue;
            if_sub_statNodeTrue.setParent(this);
            this.if_sub_statNodeFalse = if_sub_statNodeFalse;
            if_sub_statNodeFalse.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class If_Sub_StatNode extends Sub_StatNode {


    }


    public class WhileNode extends StatNode {

        private ExprNode exprNode;
        private StatNode sub_statNode;

        public WhileNode(ExprNode exprNode, StatNode sub_statNode) {

            command = "while";
            this.exprNode = exprNode;
            exprNode.setParent(this);
            this.sub_statNode = sub_statNode;
            sub_statNode.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class BeginNode extends Sub_StatNode {

        private Sub_StatNode sub_statNode;

        public BeginNode(Sub_StatNode sub_statNode) {

            command = "begin";
            this.sub_statNode = sub_statNode;
            sub_statNode.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class MultipleStatNode extends StatNode {

        private StatNode sub_statNodeFirst;
        private StatNode sub_statNodeSecond;

        public MultipleStatNode(StatNode sub_statNodeFirst, StatNode sub_statNodeSecond) {

            command = "multiple";
            this.sub_statNodeFirst = sub_statNodeFirst;
            sub_statNodeFirst.setParent(this);
            this.sub_statNodeSecond = sub_statNodeSecond;
            sub_statNodeSecond.setParent(this);

        }

        @Override
        public void check() {

        }

    }

    public class Arg_listNode extends ASTNode {

        @Override
        public void check() {

        }

    }

    public abstract class Pair_elemNode extends ASTNode {

        protected String order;

        public Pair_elemNode() {
            order = "";
        }

        public String getOrder() {
            return order;
        }

        @Override
        public void check() {

        }
    }



    public class FSTNode extends Pair_elemNode {

        private ExprNode exprNode;

        public FSTNode(ExprNode exprNode) {
            this.exprNode = exprNode;
            exprNode.setParent(this);
            order = "fst";
        }

        @Override
        public void check() {

        }

    }

    public class SNDNode extends Pair_elemNode {

        private ExprNode exprNode;

        public SNDNode(ExprNode exprNode) {
            this.exprNode = exprNode;
            exprNode.setParent(this);
            order = "snd";
        }

        @Override
        public void check() {

        }

    }

    public abstract class TypeNode extends ASTNode {

        protected String type;

        public TypeNode() {
            type = "";
        }

        @Override
        public void check() {

        }

        public String getType() {
            return type;
        }
    }


    public abstract class Base_typeNode extends TypeNode {

        public Base_typeNode() {
            super();
        }
    }

    public class IntTypeNode extends Base_typeNode {

        public IntTypeNode() {
            type = "int";
        }

    }

    public class BoolTypeNode extends Base_typeNode {

        public BoolTypeNode() {
            type = "bool";
        }

    }

    public class CharTypeNode extends Base_typeNode {

        public CharTypeNode() {
            type = "char";
        }

    }

    public class StringTypeNode extends Base_typeNode {

        public StringTypeNode() {
            type = "string";
        }

    }


    public class Array_typeNode extends TypeNode {

        private TypeNode typeNode;

        public Array_typeNode(TypeNode typeNode) {

            this.typeNode = typeNode;
            typeNode.setParent(this);
            type = "array";

        }

        @Override
        public void check() {

        }
    }

    public class ParamNode extends ASTNode {

        private TypeNode typeNode;
        private IdentNode identNode;

        public ParamNode(TypeNode typeNode, IdentNode identNode) {
            this.typeNode = typeNode;
            typeNode.setParent(this);
            this.identNode = identNode;
            identNode.setParent(this);
        }

        public ASTNode getTypeNode() {
            return typeNode;
        }

        @Override
        public void check() {

        }
    }

    public class Pair_typeNode extends TypeNode {


        private TypeNode pair_elem_typeNode1;
        private TypeNode pair_elem_typeNode2;

        public Pair_typeNode(TypeNode pair_elem_typeNode1, TypeNode pair_elem_typeNode2) {
            this.pair_elem_typeNode1 = pair_elem_typeNode1;
            pair_elem_typeNode1.setParent(this);
            this.pair_elem_typeNode2 = pair_elem_typeNode2;
            pair_elem_typeNode2.setParent(this);
            type = "pair";
        }

        @Override
        public void check() {

        }
    }

    public abstract class Pair_elem_typeNode extends ASTNode {

        @Override
        public void check() {

        }
    }

    public class PairNode extends TypeNode {

        private String command;

        public PairNode() {
            command = "pair";
        }


        @Override
        public void check() {

        }
    }

    public abstract class Unary_operNode extends ExprNode {

        protected String unOp;
        protected ExprNode exprNode;

        public Unary_operNode(ExprNode exprNode) {

            this.exprNode = exprNode;
            exprNode.setParent(this);
            unOp = "";

        }

        public String getUnOp() {
            return unOp;
        }

        @Override
        public void check() {

        }

    }

    public class NotOperNode extends Unary_operNode {

        public NotOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "!";

        }

    }

    public class NegateOperNode extends Unary_operNode {

        public NegateOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "-";

        }

    }

    public class LenOperNode extends Unary_operNode {

        public LenOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "len";

        }

    }

    public class OrdOperNode extends Unary_operNode {

        public OrdOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "ord";

        }

    }

    public class CharOperNode extends Unary_operNode {

        public CharOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "chr";

        }

    }

    public abstract class Binary_operNode extends ExprNode {


        protected ASTNode exp1;
        protected ASTNode exp2;
        protected String binOp;

        public Binary_operNode(ASTNode exp1, ASTNode exp2) {

            this.exp1 = exp1;
            exp1.setParent(this);
            this.exp2 = exp2;
            exp2.setParent(this);
            binOp = "";

        }

        public String getBinOp() {
            return binOp;
        }

        @Override
        public void check() {

        }
    }

    public class MultNode extends Binary_operNode {

        public MultNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "*";
        }


    }

    public class DivNode extends Binary_operNode {

        public DivNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "/";
        }

    }

    public class ModNode extends Binary_operNode {

        public ModNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "%";
        }

    }

    public class PlusNode extends Binary_operNode {

        public PlusNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "+";
        }

    }

    public class MinusNode extends Binary_operNode {

        public MinusNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "-";
        }

    }

    public class GreaterNode extends Binary_operNode {

        public GreaterNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = ">";
        }

    }

    public class GreaterOrEqualNode extends Binary_operNode {

        public GreaterOrEqualNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = ">=";
        }

    }

    public class SmallerNode extends Binary_operNode {

        public SmallerNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "<";
        }

    }

    public class SmallerOrEqualNode extends Binary_operNode {

        public SmallerOrEqualNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "<=";
        }

    }

    public class EqualNode extends Binary_operNode {

        public EqualNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "=";
        }

    }

    public class NotEqualNode extends Binary_operNode {

        public NotEqualNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "!=";
        }

    }

    public class LogicalAndNode extends Binary_operNode {

        public LogicalAndNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "&&";
        }

    }

    public class LogicalOrNode extends Binary_operNode {

        public LogicalOrNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "||";
        }

    }

    public abstract class ExprNode extends ASTNode {

    }

    public class IdentNode extends ExprNode {

        private String ident;

        public IdentNode(String ident) {
            this.ident = ident;
        }

        public String getIdent() {
            return ident;
        }

        @Override
        public void check() {

        }

    }

    public class Array_elemNode extends ExprNode {

        private IdentNode identNode;
        private List<ExprNode> exprNodes;

        public Array_elemNode(IdentNode identNode, List<ExprNode> exprNodes) {

            this.identNode = identNode;
            identNode.setParent(this);
            this.exprNodes = exprNodes;
            for (ExprNode exprNode : exprNodes) {
                exprNode.setParent(this);
            }

        }

        @Override
        public void check() {

        }
    }


    public class Int_literNode extends ExprNode {

        private String value;
        private String sign;

        public Int_literNode(String sign, String value) {
            this.value = value;
            this.sign = sign;
        }

        /*public Integer getValue() {
            return value;
        }*/

        @Override
        public void check() {

        }
    }



    public class Bool_literNode extends ExprNode {

        private boolean value;

        public Bool_literNode(String value) {
            this.value = Boolean.parseBoolean(value);
        }

        public boolean getValue() {
            return value;
        }

        @Override
        public void check() {

        }
    }

    public class Char_literNode extends ExprNode {

        private char value;

        public Char_literNode(String value) {
            this.value = value.charAt(0);
        }

        public char getValue() {
            return value;
        }

        @Override
        public void check() {

        }
    }

    public class Str_literNode extends ExprNode {

        private String value;

        public Str_literNode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public void check() {

        }
    }


    public class Array_literNode extends ASTNode {

        List<ASTNode> exprNodeList;

        public Array_literNode(List<ASTNode> exprNodeList) {
            this.exprNodeList = exprNodeList;
        }

        @Override
        public void check() {

        }
    }

    public class Pair_literNode extends ExprNode {

        private String value = "null";

        @Override
        public void check() {
        }
    }

    public class NewPairNode extends ASTNode {

        private ExprNode exprNode1;
        private ExprNode exprNode2;

        public NewPairNode(ExprNode exprNode1, ExprNode exprNode2) {

            this.exprNode1 = exprNode1;
            exprNode1.setParent(this);
            this.exprNode2 = exprNode2;
            exprNode2.setParent(this);

        }

        @Override
        public void check() {
        }

    }

    public class CallNode extends ASTNode {

        private IdentNode identNode;
        private List<ExprNode> exprNodeList;

        public CallNode(IdentNode identNode, List<ExprNode> exprNodeList) {

            this.identNode = identNode;
            identNode.setParent(this);
            this.exprNodeList = exprNodeList;
            for (ExprNode exprNode : exprNodeList) {
                exprNode.setParent(this);
            }

        }

        @Override
        public void check() {
        }

    }

}