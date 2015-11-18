package WACC;

import java.util.HashMap;
import java.util.List;

/**
 * Created by yh6714 on 13/11/15.
 */
public class AST {

    private ProgramNode root;

    public ProgramNode getRoot() {
        return root;
    }

    public void setRoot(ProgramNode root) {
        this.root = root;
    }

    public abstract class ASTNode {

        protected HashMap<String, ASTNode> symbolTable = new HashMap<>();

        protected ASTNode parent;

        public boolean getScope() {
            return scope;
        }

        public void setScope(boolean scope) {
            this.scope = scope;
        }

        protected boolean scope = false;

        public ASTNode getParent() {
            return parent;
        }

        public void setParent(ASTNode parent) {
            this.parent = parent;
        }

        public HashMap<String, ASTNode> getSymbolTable() {
            return symbolTable;
        }

        public void checkIfVaribleExist(IdentNode identNode) {
            ASTNode parent = getParent();
            boolean found = false;
            while (parent != null) {
                if (parent.getSymbolTable().containsKey(identNode.getIdent())) {
                    found = true;
                }
                parent = parent.getParent();
            }
            if (found == false) {
                throwSemanticError(this.getClass().toString());
            }
        }

        public void throwSemanticError(String errorMessage) {
            System.out.println(errorMessage);
            System.out.println("#semantic_error#");
            System.exit(200);
        }

        public abstract String getType();

        public abstract void check();
    }

    public class ProgramNode extends ASTNode {

        private List<FuncNode> functionNodes;
        private StatNode statNode;
        private HashMap<String, ASTNode> functionSymbolTable = new HashMap<>();

        public HashMap<String, ASTNode> getFunctionSymbolTable() {
            return functionSymbolTable;
        }


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
        public String getType() {
            return "Program";
        }

        @Override
        public void check() {
            setScope(true);

            for (FuncNode funcNode : functionNodes) {
                if (getFunctionSymbolTable().containsKey(funcNode.getIdentNode().getIdent())) {
                    throwSemanticError(this.getClass().toString());
                } else {
                    getFunctionSymbolTable().put(funcNode.getIdentNode().getIdent(), funcNode);
                }
            }

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

            type = "Function";
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
        }

        @Override
        public String getType() {
            return typeNode.getType();
        }

        public IdentNode getIdentNode() {
            return identNode;
        }

        @Override
        public void check() {
            setScope(true);

            for (ParamNode paramNode : paramNodes) {
                if (this.getSymbolTable().containsKey(paramNode.getIdentNode().getIdent())) {
                    throwSemanticError(this.getClass().toString());
                } else {
                    this.getSymbolTable().put(paramNode.getIdentNode().getIdent(), paramNode.getTypeNode());
                }
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

        @Override
        public String getType() {
            return "Skip";
        }

        @Override
        public void check() {

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

        }

        public TypeNode getTypeNode() {
            return typeNode;
        }

        @Override
        public String getType() {
            return "Declaration";
        }

        @Override
        public void check() {

            putIntoSymbolTable(this, identNode.getIdent(), typeNode);
            assign_rhsNode.check();

            if (assign_rhsNode.getType().equals("Null")) {
                return;
            }
            if (typeNode.getType().contains("Pair") && assign_rhsNode.getType().contains("Pair")) {
                Pair_typeNode lhs = (Pair_typeNode) typeNode;
                Pair_typeNode rhs;
                if (assign_rhsNode instanceof IdentNode) {
                    rhs = (Pair_typeNode) ((IdentNode) assign_rhsNode).getTypeNode();
                } else if (assign_rhsNode instanceof SNDNode) {
                    rhs = (Pair_typeNode) ((SNDNode) assign_rhsNode).getTypeNode();
                } else if (assign_rhsNode instanceof FSTNode) {
                    rhs = (Pair_typeNode) ((FSTNode) assign_rhsNode).getTypeNode();
                } else {
                    rhs = (Pair_typeNode) assign_rhsNode;
                }
                if (!(rhs.getFirstElem().equals("Null") || lhs.getFirstElem().equals(rhs.getFirstElem()))) {
                    throwSemanticError(this.getClass().toString());
                }
                if (!(rhs.getSecondElem().equals("Null") || lhs.getSecondElem().equals(rhs.getSecondElem()))) {
                    System.out.println(lhs.getSecondElem());
                    System.out.println(rhs.getSecondElem());
                    throwSemanticError(this.getClass().toString());
                }
            } else if (!typeNode.getType().equals(assign_rhsNode.getType())) {
                throwSemanticError(this.getClass().toString());
            }
        }

        private void putIntoSymbolTable(ASTNode currentScope, String string, TypeNode node) {
            while (!currentScope.getScope()) {
                currentScope = currentScope.getParent();
            }
            if (currentScope.getSymbolTable().containsKey(string)) {
                throwSemanticError(currentScope.getClass().toString());
            }else {
                currentScope.getSymbolTable().put(string, node);
            }
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
        public String getType() {
            return "Assignment";
        }

        @Override
        public void check() {

            assign_lhsNode.check();
            assign_rhsNode.check();

            if (assign_rhsNode.getType().equals("Null")) {
                return;
            }
            if (assign_lhsNode.getType().contains("Pair") && assign_rhsNode.getType().contains("Pair")) {
                Pair_typeNode lhs;
                if (assign_lhsNode instanceof IdentNode) {
                    lhs = (Pair_typeNode) ((IdentNode) assign_rhsNode).getTypeNode();
                } else if (assign_rhsNode instanceof SNDNode) {
                    lhs = (Pair_typeNode) ((SNDNode) assign_rhsNode).getTypeNode();
                } else if (assign_rhsNode instanceof FSTNode) {
                    lhs = (Pair_typeNode) ((FSTNode) assign_rhsNode).getTypeNode();
                } else {
                    lhs = (Pair_typeNode) assign_rhsNode;
                }
                Pair_typeNode rhs;
                if (assign_rhsNode instanceof IdentNode) {
                    rhs = (Pair_typeNode) ((IdentNode) assign_rhsNode).getTypeNode();
                } else if (assign_rhsNode instanceof SNDNode) {
                    rhs = (Pair_typeNode) ((SNDNode) assign_rhsNode).getTypeNode();
                } else if (assign_rhsNode instanceof FSTNode) {
                    rhs = (Pair_typeNode) ((FSTNode) assign_rhsNode).getTypeNode();
                } else {
                    rhs = (Pair_typeNode) assign_rhsNode;
                }
                if (!(rhs.getFirstElem().equals("Null") || lhs.getFirstElem().equals(rhs.getFirstElem()))) {
                    throwSemanticError(this.getClass().toString());
                }
                if (!(rhs.getSecondElem().equals("Null") || lhs.getSecondElem().equals(rhs.getSecondElem()))) {
                    System.out.println(lhs.getSecondElem());
                    System.out.println(rhs.getSecondElem());
                    throwSemanticError(this.getClass().toString());
                }
            } else if (!assign_lhsNode.getType().equals(assign_rhsNode.getType())) {
                throwSemanticError(this.getClass().toString());
            }

        }

        private TypeNode lookupSymbolTable(ASTNode currentScope, String string) {
            while (!currentScope.getScope()) {
                currentScope = currentScope.getParent();
            }
            return (TypeNode) currentScope.getSymbolTable().get(string);
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
        public String getType() {
            return "Read";
        }

        @Override
        public void check() {
            assign_lhsNode.check();
            String type = assign_lhsNode.getType();
            switch (type) {

                case "Int":
                case "Char":
                case "Bool":
                case "String":
                case "Null":
                    break;
                default:
                    throwSemanticError(this.getClass().toString());
            }

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
        public String getType() {

            return "Free";
        }

        @Override
        public void check() {

            exprNode.check();
            String type = exprNode.getType();

            if (!type.contains("Pair(") || type.contains("[]")) {
                throwSemanticError(this.getClass().toString());
            }

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
        public String getType() {

            return exprNode.getType();
        }

        @Override
        public void check() {
            exprNode.check();
            ASTNode parent = getParent();
            while (!(parent instanceof FuncNode)) {
                parent = parent.getParent();
            }
            if (this.getType() != parent.getType()) {
                throwSemanticError(this.getClass().toString());
            }
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
        public String getType() {
            return exprNode.getType();
        }

        @Override
        public void check() {
            exprNode.check();

            if (!exprNode.getType().equals("Int")) {
                throwSemanticError(this.getClass().toString());
            }
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
        public String getType() {
            return exprNode.getType();
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
        public String getType() {
            return exprNode.getType();
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
            if (statNodeFalse != null) {
                this.statNodeFalse = statNodeFalse;
                statNodeFalse.setParent(this);
            }

        }

        @Override
        public String getType() {
            return "If";
        }

        @Override
        public void check() {
            statNodeTrue.setScope(true);
            statNodeFalse.setScope(true);
            exprNode.check();

            if (!exprNode.getType().equals("Bool")) {
                throwSemanticError(this.getClass().toString());
            }
            statNodeTrue.check();
            if (statNodeFalse != null) {
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
        public String getType() {
            return "While";
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().equals("Bool")) {
                throwSemanticError(this.getClass().toString());
            }
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
        public String getType() {
            return "MultipleStat";
        }

        @Override
        public void check() {
            statNodeFirst.check();
            statNodeSecond.check();
        }

    }

    public class FSTNode extends ASTNode {


        private IdentNode exprNode;

        public FSTNode(IdentNode exprNode) {
            this.exprNode = exprNode;
            exprNode.setParent(this);
        }

        public String getType() {

            return ((Pair_typeNode)(exprNode.getTypeNode())).getFirstElem();
        }

        public TypeNode getTypeNode() {
            return exprNode.getTypeNode();
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().contains("Pair")) {
                throwSemanticError(this.getClass().toString());
            }

        }

    }

    public class SNDNode extends ASTNode {


        private IdentNode exprNode;

        public SNDNode(IdentNode exprNode) {
            this.exprNode = exprNode;
            exprNode.setParent(this);
        }

        public String getType() {

            return ((Pair_typeNode)(exprNode.getTypeNode())).getSecondElem();
        }

        public TypeNode getTypeNode() {
            return exprNode.getTypeNode();
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().contains("Pair")) {
                throwSemanticError(this.getClass().toString());
            }

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
    }


    public abstract class Base_typeNode extends TypeNode {

        public Base_typeNode() {
            super();
        }
    }

    public class IntTypeNode extends Base_typeNode {

        public IntTypeNode() {
            type = "Int";
        }

        @Override
        public String getType() {
            return "Int";
        }

        @Override
        public void check() {

        }
    }

    public class BoolTypeNode extends Base_typeNode {

        public BoolTypeNode() {
            type = "bool";
        }

        @Override
        public String getType() {
            return "Bool";
        }

        @Override
        public void check() {

        }
    }

    public class CharTypeNode extends Base_typeNode {

        public CharTypeNode() {
            type = "char";
        }

        @Override
        public String getType() {
            return "Char";
        }

        @Override
        public void check() {

        }
    }

    public class StringTypeNode extends Base_typeNode {

        public StringTypeNode() {
            type = "string";
        }

        @Override
        public String getType() {
            return "String";
        }

        @Override
        public void check() {

        }
    }


    public class Array_typeNode extends TypeNode {

        private TypeNode typeNode;

        public Array_typeNode(TypeNode typeNode) {

            this.typeNode = typeNode;
            typeNode.setParent(this);

        }

        @Override
        public String getType() {

            return typeNode.getType() + "[]";
        }

        @Override
        public void check() {

        }

        public String getElemType() {
            return typeNode.getType();
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

        public IdentNode getIdentNode() {
            return identNode;
        }

        public TypeNode getTypeNode() {
            return typeNode;
        }

        @Override
        public String getType() {
            return typeNode.getType();
        }

        @Override
        public void check() {

        }
    }

    public class Pair_typeNode extends TypeNode {


        private ASTNode pair_elemNode1;
        private ASTNode pair_elemNode2;

        public Pair_typeNode(ASTNode pair_elemNode1, ASTNode pair_elemNode2) {
            this.pair_elemNode1 = pair_elemNode1;
            pair_elemNode1.setParent(this);
            this.pair_elemNode2 = pair_elemNode2;
            pair_elemNode2.setParent(this);
        }


        public String getFirstElem() {
            return pair_elemNode1.getType();
        }

        public String getSecondElem() {
            return pair_elemNode2.getType();
        }

        @Override
        public String getType() {

 //           return "Pair(" + pair_elemNode1.getType() + ", " + pair_elemNode2.getType() + ")";
            return "Pair";
        }

        @Override
        public void check() {

        }
    }

    public class PairNode extends TypeNode {

        @Override
        public String getType() {
            return "Pair";
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

    }

    public class NotOperNode extends Unary_operNode {

        public NotOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "!";

        }

        @Override
        public String getType() {

            return "Bool";
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().equals("Bool")) {
                throwSemanticError(this.getClass().toString());
            }

        }

    }

    public class NegateOperNode extends Unary_operNode {

        public NegateOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "-";

        }

        @Override
        public String getType() {

            return "Int";
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().equals("Int")) {
                throwSemanticError(this.getClass().toString());
            }

        }

    }

    public class LenOperNode extends Unary_operNode {

        public LenOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "len";

        }

        @Override
        public String getType() {

            return "Int";
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().contains("[]")) {
                throwSemanticError(this.getClass().toString());
            }

        }
    }

    public class OrdOperNode extends Unary_operNode {

        public OrdOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "ord";

        }

        @Override
        public String getType() {

            return "Int";
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().equals("Char")) {
                throwSemanticError(this.getClass().toString());
            }

        }
    }

    public class CharOperNode extends Unary_operNode {

        public CharOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "chr";

        }

        @Override
        public String getType() {

            return "Int";
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().equals("Int")) {
                throwSemanticError(this.getClass().toString());
            }

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
            return (exp1).getType();
        }

        public String getBinOp() {
            return binOp;
        }

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (exp1.getType() != exp2.getType() && !exp2.getType().equals("Null")) {
                throwSemanticError(this.getClass().toString());
            }
            else if (!exp1.getType().contains("Pair") && exp2.getType().contains("Pair")) {
                throwSemanticError(this.getClass().toString());
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

        @Override
        public String getType() {

            return "Bool";
        }

    }

    public class GreaterOrEqualNode extends Binary_operNode {

        public GreaterOrEqualNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = ">=";
        }

        @Override
        public String getType() {

            return "Bool";
        }

    }

    public class SmallerNode extends Binary_operNode {

        public SmallerNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "<";
        }

        @Override
        public String getType() {

            return "Bool";
        }

    }

    public class SmallerOrEqualNode extends Binary_operNode {

        public SmallerOrEqualNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "<=";
        }

        @Override
        public String getType() {

            return "Bool";
        }

    }

    public class EqualNode extends Binary_operNode {

        public EqualNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "==";
        }

        @Override
        public String getType() {

            return "Bool";
        }

    }

    public class NotEqualNode extends Binary_operNode {

        public NotEqualNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "!=";
        }

        @Override
        public String getType() {

            return "Bool";
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

    public abstract class ExprNode extends StatNode {

        protected String type;
    }

    public class IdentNode extends ExprNode {

        private String ident;

        public IdentNode(String ident) {
            this.ident = ident;
        }

        public String getIdent() {
            return ident;
        }

        public TypeNode getTypeNode() {

            ASTNode parent = getParent();
            ASTNode typeNode = null;
            while (parent != null && typeNode == null) {
                if (typeNode instanceof FuncNode) {
                    return (TypeNode) typeNode;
                }
                typeNode = parent.getSymbolTable().get(ident);
                parent = parent.getParent();
            }
            return (TypeNode) typeNode;

        }

        @Override
        public String getType() {

            ASTNode typeNode = getTypeNode();

            if(typeNode == null) {
                return "";
            }
            return typeNode.getType();

        }

        @Override
        public void check() {
            checkIfVaribleExist(this);
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

            identNode.check();
            for (ExprNode exprNode : exprNodes) {
                exprNode.check();
            }
            checkIfVaribleExist(identNode);
        }

        @Override
        public String getType() {
            return lookupSymbolTable(this, identNode.getIdent()).getType();
        }

        private TypeNode lookupSymbolTable(ASTNode currentScope, String string) {
            while (!currentScope.getScope()) {
                currentScope = currentScope.getParent();
            }
            return (TypeNode) currentScope.getSymbolTable().get(string);
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

            return "Int";
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

            return "Bool";
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

            return "Char";
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

            return "String";
        }
    }


    public class Array_literNode extends ASTNode {

        List<ASTNode> exprNodeList;

        public Array_literNode(List<ASTNode> exprNodeList) {

            this.exprNodeList = exprNodeList;

        }

        @Override
        public String getType() {

            return exprNodeList.get(0).getType() + "[]";
        }

        private String getElemType() {
            return exprNodeList.get(0).getType();
        }

        @Override
        public void check() {

            for(ASTNode astNode : exprNodeList) {
                astNode.check();
            }
            if(!getType().equals("")) {
                for(ASTNode astNode : exprNodeList) {
                    if(!astNode.getType().equals(getElemType())) {
                        throwSemanticError(this.getClass().toString());
                    }
                }
            }
        }
    }

    public class Pair_literNode extends ExprNode {

        @Override
        public void check() {
        }

        @Override
        public String getType() {

            return "Null";
        }
    }

    public class NewPairNode extends Pair_typeNode {

        private ExprNode exprNode1;
        private ExprNode exprNode2;

        public NewPairNode(ExprNode exprNode1, ExprNode exprNode2) {


            super(exprNode1, exprNode2);
            this.exprNode1 = exprNode1;
            exprNode1.setParent(this);
            this.exprNode2 = exprNode2;
            exprNode2.setParent(this);

        }

        public String getType() {

  /*          String exprNode1Type = exprNode1.getType();
            if (exprNode1Type.contains("Pair")) {
                exprNode1Type = "Pair";
            }
            String exprNode2Type = exprNode2.getType();
            if (exprNode2Type.contains("Pair")) {
                exprNode2Type = "Pair";
            }
            return "Pair(" + exprNode1Type + ", " + exprNode2Type + ")";
          */
            return "Pair";
        }

        @Override
        public void check() {
            exprNode1.check();
            exprNode2.check();
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
        }

        public String getType() {
            FuncNode funcNode = (FuncNode)getRoot().getFunctionSymbolTable().get(identNode.getIdent());
            return funcNode.getType();
        }

        @Override
        public void check() {

            for(ExprNode exprNode : exprNodeList) {
                exprNode.check();
            }
            if (!getRoot().getFunctionSymbolTable().containsKey(identNode.getIdent())) {
                throwSemanticError(this.getClass().toString());
            }
        }

    }

}