package WACC;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yh6714 on 13/11/15.
 */
public class AST {

    private AST ast;
    private ASTNode root;

    public ASTNode getRoot() {
        return root;
    }

    public void setRoot(ASTNode root) {

        this.root = root;
    }

    public abstract class ASTNode {

        protected HashMap<String, ASTNode> symbolTable = new HashMap<>();

        protected ASTNode parent;

        public ASTNode getParent() {
            return parent;
        }

        public void setParent(ASTNode parent) {
            this.parent = parent;
        }

        public HashMap<String, ASTNode> getSymbolTable() {
            return symbolTable;
        }

        public void throwSemanticError() {
            System.out.println("#semantic_error#");
            System.exit(200);
        }


        public String getType() {
            return null;
        }

        public void lookUpSymbolTable(IdentNode identNode) {
            if (getParent().getSymbolTable().containsKey(identNode.getIdent())) {
                throwSemanticError();
            }
        }

        public void check() {
        }

    }

    public class ProgramNode extends ASTNode {

        private List<FuncNode> functionNodes;
        private StatNode statNode;

        public ProgramNode(List<FuncNode> functionNodes, StatNode statNode) {

            this.functionNodes = functionNodes;
            for (FuncNode funcNode : functionNodes) {
                funcNode.setParent(this);
            }
            this.statNode = statNode;
            statNode.setParent(this);
            setRoot(this);
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
        private String type;

        public FuncNode(TypeNode typeNode, IdentNode identNode, List<ParamNode> paramNodes, StatNode statNode) {
            type = "function";
            this.typeNode = typeNode;
            typeNode.setParent(this);
            this.statNode = statNode;
            statNode.setParent(this);
            this.paramNodes = paramNodes;
                for (ParamNode paramNode : paramNodes) {
                    paramNode.setParent(this);
                }

            this.identNode = identNode;
            identNode.setParent(this);
            lookUpSymbolTable(identNode);
            getParent().getSymbolTable().put(identNode.getIdent(), this);
        }

        public TypeNode getTypeNode() {
            return typeNode;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public void check() {
            /*Checking paramNodeList*/
            LinkedList<ParamNode> checklist = new LinkedList<>();
            for (ParamNode paramNode : paramNodes) {
                if(checklist.contains(paramNode)) {
                    throwSemanticError();
                }
                checklist.add(paramNode);
            }
            statNode.check();
        }

    }


    public abstract class StatNode extends ASTNode {

        protected String command;

        public StatNode() {
            command = "";
        }

        public String getCommand() {
            return command;
        }

    }

    public class SkipNode extends StatNode {

        public SkipNode() {
            command = "skip";
        }

    }

    public class DeclarationNode extends StatNode {

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

            lookUpSymbolTable(identNode);
            getParent().getSymbolTable().put(identNode.getIdent(), typeNode);
        }

        public TypeNode getTypeNode() {
            return typeNode;
        }

        @Override
        public void check() {

            if(!typeNode.getType().equals(assign_rhsNode.getType())) {
                throwSemanticError();
            }

            assign_rhsNode.check();
        }

    }

    public class AssignmentNode extends StatNode {

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

            if(!assign_lhsNode.getType().equals(assign_rhsNode.getType())) {
                throwSemanticError();
            }
            assign_lhsNode.check();
            assign_rhsNode.check();

        }

    }

    public class ReadNode extends StatNode {

        private ASTNode assign_lhsNode;

        public ReadNode(ASTNode assign_lhsNode) {

            command = "read";
            this.assign_lhsNode = assign_lhsNode;
            assign_lhsNode.setParent(this);

        }

        @Override
        public void check() {
            String type = assign_lhsNode.getType();
            switch (type) {
                case "int":
                case "char":
                case "bool":
                case "String":
                case "null": break;
                default: throwSemanticError();
            }
            assign_lhsNode.check();
        }

    }

    public class FreeNode extends StatNode {

        private ExprNode exprNode;

        public FreeNode(ExprNode exprNode) {

            command = "free";
            this.exprNode = exprNode;
            exprNode.setParent(this);

        }

        @Override
        public void check() {
            String type = exprNode.getType();
            if(!type.contains("pair(") || type.contains("array")) {
                throwSemanticError();
            }
            exprNode.check();
        }

    }

    public class ReturnNode extends StatNode {

        private ExprNode exprNode;

        public ReturnNode(ExprNode exprNode) {

            command = "return";
            this.exprNode = exprNode;
            exprNode.setParent(this);

        }

        @Override
        public void check() {
            ASTNode parent = getParent();
            while (!(parent instanceof FuncNode)) {
                parent.getParent();
            }
            String type = ((FuncNode) parent).getTypeNode().getType();
            if(!type.equals(exprNode.getType())) {
                throwSemanticError();
            }
            exprNode.check();
        }

    }

    public class ExitNode extends StatNode {

        private ExprNode exprNode;

        public ExitNode(ExprNode exprNode) {

            command = "exit";
            this.exprNode = exprNode;
            exprNode.setParent(this);

        }

        @Override
        public void check() {
            if(!exprNode.getType().equals("int")) {
                throwSemanticError();
            }
            exprNode.check();
        }

    }

    public class PrintNode extends StatNode {

        private ExprNode exprNode;

        public PrintNode(ExprNode exprNode) {

            command = "print";
            this.exprNode = exprNode;
            exprNode.setParent(this);

        }

        @Override
        public void check() {
            exprNode.check();
        }

    }

    public class PrintlnNode extends StatNode {

        private ExprNode exprNode;

        public PrintlnNode(ExprNode exprNode) {

            command = "println";
            this.exprNode = exprNode;
            exprNode.setParent(this);

        }

        @Override
        public void check() {
            exprNode.check();
        }

    }

    public class IfNode extends StatNode {

        private ExprNode exprNode;
        private StatNode statNodeTrue;
        private StatNode statNodeFalse;

        public IfNode(ExprNode exprNode, StatNode statNodeTrue, StatNode statNodeFalse) {

            command = "if";
            this.exprNode = exprNode;
            exprNode.setParent(this);
            this.statNodeTrue = statNodeTrue;
            statNodeTrue.setParent(this);
            if(statNodeFalse != null) {
                this.statNodeFalse = statNodeFalse;
                statNodeFalse.setParent(this);
            }

        }

        @Override
        public void check() {
            if(!exprNode.getType().equals("bool")) {
                throwSemanticError();
            }
            exprNode.check();
            statNodeTrue.check();
            if(statNodeFalse != null) {
                statNodeFalse.check();
            }
        }

    }

    public class WhileNode extends StatNode {

        private ExprNode exprNode;
        private StatNode statNode;

        public WhileNode(ExprNode exprNode, StatNode statNode) {

            command = "while";
            this.exprNode = exprNode;
            exprNode.setParent(this);
            this.statNode = statNode;
            statNode.setParent(this);

        }

        @Override
        public void check() {
            if(!exprNode.getType().equals("bool")) {
                throwSemanticError();
            }
            statNode.check();
        }

    }

    public class BeginNode extends StatNode {

        private StatNode statNode;

        public BeginNode(StatNode statNode) {

            command = "begin";
            this.statNode = statNode;
            statNode.setParent(this);

        }

        @Override
        public void check() {
            statNode.check();
        }

    }

    public class MultipleStatNode extends StatNode {

        private StatNode statNodeFirst;
        private StatNode statNodeSecond;

        public MultipleStatNode(StatNode statNodeFirst, StatNode statNodeSecond) {

            command = "multiple";
            this.statNodeFirst = statNodeFirst;
            statNodeFirst.setParent(this);
            this.statNodeSecond = statNodeSecond;
            statNodeSecond.setParent(this);

        }

        @Override
        public void check() {
            statNodeFirst.check();
            statNodeSecond.check();
        }

    }

    public abstract class Pair_elemNode extends ASTNode {

        protected String command;

        public Pair_elemNode() {
            command = "";
        }

        public String getCommand() {
            return command;
        }

    }


    public class FSTNode extends Pair_elemNode {

        private ExprNode exprNode;

        public FSTNode(ExprNode exprNode) {
            this.exprNode = exprNode;
            exprNode.setParent(this);
            command = "fst";
        }

        public String getType() {
            return exprNode.getType();
        }

        @Override
        public void check() {
            if(!exprNode.getType().contains("pair(")) {
                throwSemanticError();
            }
            exprNode.check();
        }

    }

    public class SNDNode extends Pair_elemNode {

        private ExprNode exprNode;

        public SNDNode(ExprNode exprNode) {
            this.exprNode = exprNode;
            exprNode.setParent(this);
            command = "snd";
        }

        public String getType() {
            return exprNode.getType();
        }


        @Override
        public void check() {
            if(!exprNode.getType().contains("pair(")) {
                throwSemanticError();
            }
            exprNode.check();
        }

    }

    public abstract class TypeNode extends ASTNode {

        protected String type;

        public TypeNode() {
            type = "";
        }

        @Override
        public boolean equals(Object that) {
            return getType().equals(((TypeNode) that).getType());
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
            type = typeNode.getType() + "array";

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
            lookUpSymbolTable(identNode);
            getParent().getSymbolTable().put(identNode.getIdent(), typeNode);

        }

        public TypeNode getTypeNode() {
            return typeNode;
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
            type = "pair(" + pair_elem_typeNode1.getType() + ", " + pair_elem_typeNode2.getType() + ")";
        }

    }


    public class PairNode extends TypeNode {

        private String command;

        public PairNode() {
            command = "pair";
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

    }

    public class NotOperNode extends Unary_operNode {

        public NotOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "!";

        }

        @Override
        public String getType() {
            return "bool";
        }

        @Override
        public void check() {
            if(!exprNode.getType().equals("bool")) {
                throwSemanticError();
            }
            exprNode.check();
        }

    }

    public class NegateOperNode extends Unary_operNode {

        public NegateOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "-";

        }

        @Override
        public String getType() {
            return "int";
        }

        @Override
        public void check() {
            if(!exprNode.getType().equals("int")) {
                throwSemanticError();
            }
            exprNode.check();
        }

    }

    public class LenOperNode extends Unary_operNode {

        public LenOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "len";

        }

        @Override
        public String getType() {
            return "int";
        }

        @Override
        public void check() {
            if(!exprNode.getType().contains("array")) {
                throwSemanticError();
            }
            exprNode.check();
        }
    }

    public class OrdOperNode extends Unary_operNode {

        public OrdOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "ord";

        }

        @Override
        public String getType() {
            return "int";
        }

        @Override
        public void check() {
            if(!exprNode.getType().equals("char")) {
                throwSemanticError();
            }
            exprNode.check();
        }
    }

    public class CharOperNode extends Unary_operNode {

        public CharOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "chr";

        }

        @Override
        public String getType() {
            return "int";
        }

        @Override
        public void check() {
            if(!exprNode.getType().equals("int")) {
                throwSemanticError();
            }
            exprNode.check();
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

        @Override
        public String getType() {
            return ((TypeNode) exp1).getType();
        }

        public String getBinOp() {
            return binOp;
        }

        @Override
        public void check() {
            if (!((TypeNode) exp1).equals(exp2)) {
                throwSemanticError();
            }
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

        protected String type;

    }

    public class IdentNode extends ExprNode {

        private String ident;

        public IdentNode(String ident) {
            this.ident = ident;
            type = getType();
        }

        public String getIdent() {
            return ident;
        }

        @Override
        public void check() {

        }

        @Override
        public String getType() {
            ASTNode parent = null;
            ASTNode typeNode = null;
            System.out.println(getParent() == null);
            System.out.println(ident);
            System.out.println("==============");
            while ((parent = getParent()) != null && typeNode == null) {
                if(typeNode instanceof FuncNode) {
                    return ((FuncNode) typeNode).getType();
                }
                typeNode = parent.getSymbolTable().get(ident);
            }
            return typeNode.getType();
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
            type = identNode.getType();

        }

        @Override
        public void check() {

        }

        @Override
        public String getType() {
            return null;
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

        @Override
        public String getType() {
            return "int";
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

        @Override
        public String getType() {
            return "bool";
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

        @Override
        public String getType() {
            return "char";
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

        @Override
        public String getType() {
            return "string";
        }
    }


    public class Array_literNode extends ASTNode {

        List<ASTNode> exprNodeList;
        private String type = "";

        public Array_literNode(List<ASTNode> exprNodeList) {
            this.exprNodeList = exprNodeList;
            if (!exprNodeList.isEmpty()) {
                type = ((ExprNode) exprNodeList.get(0)).getType();
            }

        }

        public String getType() {
            return type;
        }

        @Override
        public void check() {

        }
    }

    public class Pair_literNode extends ExprNode {

        @Override
        public void check() {
        }

        @Override
        public String getType() {
            return "null";
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

        public String getType() {
            return "pair(" + exprNode1.getType() + ", " + exprNode2.getType() + ")";
        }

        @Override
        public void check() {
        }

    }

    public class CallNode extends ASTNode {

        private IdentNode identNode;
        private List<ExprNode> exprNodeList;
        private FuncNode funcNode;

        public CallNode(IdentNode identNode, List<ExprNode> exprNodeList) {

            this.identNode = identNode;
            identNode.setParent(this);
            this.exprNodeList = exprNodeList;
            for (ExprNode exprNode : exprNodeList) {
                exprNode.setParent(this);
            }
            funcNode = (FuncNode) getRoot().getSymbolTable().get(identNode);

        }

        public String getType() {
            return funcNode.getTypeNode().getType();
        }

        @Override
        public void check() {
        }

    }

}