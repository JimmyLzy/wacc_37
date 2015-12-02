package WACC;


import java.util.HashMap;
import java.util.List;

/**
 * Created by yh6714 on 13/11/15.
 */

/*
 * All unused methods and fields are the prepartion of the code generation part
 */
public class AST {

    private Registers registers = new Registers();

    private ProgramNode root;

    public ProgramNode getRoot() {
        return root;
    }

    public void setRoot(ProgramNode root) {
        this.root = root;
    }

    public int getWordLength(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i)== '\\') {
                count++;
                i++;
            } else {
                count++;
            }
        }
        return count - 2;
    }


    /*The base of all other nodes and other nodes need to implement the methods
    * Containing:
    * Symboltable as hashmap
    * Parent of the current node
    * Check method to do the semantic check
    * */
    public abstract class ASTNode {

        protected HashMap<String, ASTNode> symbolTable = new HashMap<>();

        protected ASTNode parent;
        protected boolean scope = false;

        public boolean getScope() {
            return scope;
        }

        public void setScope(boolean scope) {
            this.scope = scope;
        }

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
                throwSemanticError(identNode.getIdent() + " has not been decleared before");
            }
        }

        public void throwSemanticError(String errorMessage) {
            System.out.println(errorMessage);
            System.out.println("#semantic_error#");
            System.exit(200);
        }

        public abstract String getType();

        public abstract void check();

        public abstract void generate(StringBuilder headerStringBuilder,
                                      StringBuilder mainStringBuilder, StringBuilder functionStringBuilder);
    }

    /*
     * The basic program node, containing two symbol tables for functions and program statements.
     */
    public class ProgramNode extends ASTNode {

        private List<FuncNode> functionNodes;
        private StatNode statNode;
        private HashMap<String, ASTNode> functionSymbolTable = new HashMap<>();

        public ProgramNode(List<FuncNode> functionNodes, StatNode statNode) {

            this.functionNodes = functionNodes;
            for (FuncNode funcNode : functionNodes) {
                funcNode.setParent(this);
            }
            this.statNode = statNode;
            statNode.setParent(this);
            setRoot(this);
        }

        public HashMap<String, ASTNode> getFunctionSymbolTable() {
            return functionSymbolTable;
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
                    throwSemanticError("The function " + funcNode.getIdentNode().getIdent()
                            + " cannot be overloaded");
                } else {
                    getFunctionSymbolTable().put(funcNode.getIdentNode().getIdent(), funcNode);
                }
            }

            for (FuncNode funcNode : functionNodes) {
                funcNode.check();
            }

            statNode.check();
        }

        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {
            headerStringBuilder.append(".data\n");
            mainStringBuilder.append(".text\n");
            mainStringBuilder.append(".global main\n");
            mainStringBuilder.append("main: \n");
            mainStringBuilder.append("PUSH {lr}  \n");
            for (FuncNode funcNode : functionNodes) {
                funcNode.generate(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            }
            statNode.generate(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            mainStringBuilder.append("MOV r0, #0\n");
            mainStringBuilder.append("POP {pc}\n");
        }
    }

    /*
     * Representing the functions declare in the program
     * Has program node as parent
     * Has it own scope which is the symboltable
     */
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

        public List<ParamNode> getParamNodes() {
            return paramNodes;
        }

        @Override
        public void check() {
            setScope(true);

            for (ParamNode paramNode : paramNodes) {
                if (this.getSymbolTable().containsKey(paramNode.getIdentNode().getIdent())) {
                    throwSemanticError("The function parameters cannot have same variable name");
                } else {
                    this.getSymbolTable().put(paramNode.getIdentNode().getIdent(), paramNode.getTypeNode());
                }
            }

            statNode.check();
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

    }

    /*
     * Spliting all statement to different nodes
     * The command field is for the use of code generation
     */
    public abstract class StatNode extends ASTNode {

        protected String command;

        public StatNode() {
            command = "";
        }

        public String getCommand() {
            return command;
        }

        public Pair_typeNode getPair_typeNode(ASTNode node) {
            Pair_typeNode result;
            if (node.getClass().toString().contains("IdentNode")) {
                result = (Pair_typeNode) ((IdentNode) node).getTypeNode();
            } else if (node.getClass().toString().contains("SNDNode")) {
                result = (Pair_typeNode) ((SNDNode) node).getTypeNode();
            } else if (node.getClass().toString().contains("FSTNode")) {
                result = (Pair_typeNode) ((FSTNode) node).getTypeNode();
            } else if (node.getClass().toString().contains("CallNode")) {
                result = (Pair_typeNode) ((CallNode) node).getTypeNode();
            } else {
                result = (Pair_typeNode) node;
            }
            return result;
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

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

    }

    /*
     * The DeclarationNode adds all varibles to the symboltable in correct scope
     * Checks semantically if the varible can be declare or not
     * Handles all edge cases
     */
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
                Pair_typeNode rhs = getPair_typeNode(assign_rhsNode);
                if (!(rhs.getFirstElem().equals("Null") || lhs.getFirstElem().equals(rhs.getFirstElem()))) {
                    throwSemanticError("Need same type when declaring the variable");
                }
                if (!(rhs.getSecondElem().equals("Null") || lhs.getSecondElem().equals(rhs.getSecondElem()))) {
                    throwSemanticError("Need same type when declaring the variable");
                }
            } else if (!typeNode.getType().equals(assign_rhsNode.getType())) {
                throwSemanticError("Need same type when declaring the variable");
            }
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {
            if (assign_rhsNode instanceof Str_literNode) {
                this.typeNode.setValue(((Str_literNode) assign_rhsNode).getValue());
            }
        }


        private void putIntoSymbolTable(ASTNode currentScope, String string, TypeNode node) {
            while (!currentScope.getScope()) {
                currentScope = currentScope.getParent();
            }
            if (currentScope.getSymbolTable().containsKey(string)) {
                throwSemanticError(currentScope.getClass().toString());
            } else {
                currentScope.getSymbolTable().put(string, node);
            }
        }

    }

    /*
     * Similar to DeclarationNode
     * Has handle left-hand-side node correctly
     */
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
                Pair_typeNode lhs = getPair_typeNode(assign_lhsNode);
                Pair_typeNode rhs = getPair_typeNode(assign_rhsNode);
                if (!(rhs.getFirstElem().equals("Null") || lhs.getFirstElem().equals(rhs.getFirstElem()))) {
                    throwSemanticError("Need same type when assigning the variable");
                }
                if (!(rhs.getSecondElem().equals("Null") || lhs.getSecondElem().equals(rhs.getSecondElem()))) {
                    throwSemanticError("Need same type when assigning the variable");
                }
            } else if (!assign_lhsNode.getType().equals(assign_rhsNode.getType())) {
                throwSemanticError("Need same type when assigning the variable");
            }

        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {
            if (assign_lhsNode instanceof IdentNode && assign_rhsNode instanceof Str_literNode) {
                getTypeNode((IdentNode) assign_lhsNode).setValue(((Str_literNode) assign_rhsNode).getValue());
            }
        }


        private TypeNode lookupSymbolTable(ASTNode currentScope, String string) {
            while (!currentScope.getScope()) {
                currentScope = currentScope.getParent();
            }
            return (TypeNode) currentScope.getSymbolTable().get(string);
        }

        public TypeNode getTypeNode(IdentNode identNode) {
            ASTNode parent = getParent();
            boolean found = false;
            while (parent != null) {
                if (parent.getSymbolTable().containsKey(identNode.getIdent())) {
                    return ((TypeNode) parent.getSymbolTable().get(identNode.getIdent()));
                }
                parent = parent.getParent();
            }
            if (found == false) {
                return null;
            }
            return null;
        }
    }

    /*
     * All other StatNodes are similar
     * Implemented their own check cases
     */
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
                    break;
                default:
                    throwSemanticError("The read statment can only read int or char type experssion");
            }
            if (!(assign_lhsNode instanceof IdentNode || assign_lhsNode instanceof Array_elemNode
                    || assign_lhsNode instanceof FSTNode || assign_lhsNode instanceof SNDNode)) {
                throwSemanticError("The read statment can only read a program varible, " +
                        "array element or a pair element");
            }
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
                throwSemanticError("The free staement takes invalid arguments");
            }

        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
                if (parent.equals(getRoot())) {
                    throwSemanticError("Can not return from program");
                }
                parent = parent.getParent();

            }
            if (this.getType() != parent.getType()) {
                throwSemanticError("Cannot return in program statement");
            }
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
                throwSemanticError("The exit statement must take int argument");
            }
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {
            int exitNum = 0;
            if (exprNode instanceof NegateOperNode) {
                NegateOperNode negateOperNode = (NegateOperNode) exprNode;
                exitNum = ((Int_literNode) negateOperNode.getExprNdoe()).getvalue() / -1;
            } else {
                Int_literNode int_literNode = (Int_literNode) exprNode;
                exitNum = int_literNode.getvalue();
            }
            mainStringBuilder.append("LDR r0, =" + exitNum + "\n");
            mainStringBuilder.append("BL exit\n");
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

//        @Override
//        public void generate(StringBuilder headerStringBuilder,
//                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {
//
//            mainStringBuilder.append("LDR r0, =msg_" + registers.getMessageCount() + "\n");
//            mainStringBuilder.append("BL p_print_string\n");
//
//            headerStringBuilder.append("msg_" + registers.getMessageCount() + ": \n");
//            registers.incMessageCount();
//            headerStringBuilder.append(".word " + getWordLength(exprNode.getValue()) + "\n");
//            headerStringBuilder.append(".ascii\t" + exprNode.getValue() + "\n");
//            if (!functionStringBuilder.toString().contains("p_print_string:")) {
//                functionStringBuilder.append("p_print_string:\n");
//                functionStringBuilder.append("PUSH {lr}\n");
//                functionStringBuilder.append("LDR r1, [r0]\n");
//                functionStringBuilder.append("ADD r2, r0, #4\n");
//                functionStringBuilder.append("LDR r0, =msg_" + registers.getMessageCount() + "\n");
//                functionStringBuilder.append("ADD r0, r0, #4\n");
//                functionStringBuilder.append("BL printf\n");
//                functionStringBuilder.append("MOV r0, #0\n");
//                functionStringBuilder.append("BL fflush\n");
//                functionStringBuilder.append("POP {pc}\n");
//                headerStringBuilder.append("msg_" + registers.getMessageCount() + ":\n");
//                registers.incMessageCount();
//                headerStringBuilder.append(".word 5\n");
//                headerStringBuilder.append(".ascii\t\"%.*s\\0\"\n");
//            }
//
//        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

            if (exprNode instanceof IdentNode) {
                switch (exprNode.getType()) {
                    case ("String"):
                        generatePrintStringLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
                        break;
                    case ("Bool"):
                        generatePrintBoolLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
                        break;
                    case ("Int"):
                        generatePrintIntLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
                        break;
                    case ("Char"):
                        generatePrintCharLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
                        break;
                }
            } else if (exprNode instanceof Str_literNode) {
                generatePrintStringLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            } else if (exprNode instanceof Bool_literNode) {
                generatePrintBoolLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            } else if (exprNode instanceof Int_literNode) {
                generatePrintIntLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            } else if (exprNode instanceof Char_literNode) {
                generatePrintCharLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            }
        }

        private void generatePrintCharLiter(StringBuilder headerStringBuilder,
                                            StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {
            mainStringBuilder.append("MOV r0, #'" + exprNode.getValue() + "'\n");
            mainStringBuilder.append("BL putchar\n");
        }

        private void generatePrintIntLiter(StringBuilder headerStringBuilder,
                                           StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

            mainStringBuilder.append("LDR r0, =" + exprNode.getValue() + "\n");
            mainStringBuilder.append("BL p_print_int\n");

            if (!functionStringBuilder.toString().contains("p_print_int:")) {
                functionStringBuilder.append("p_print_int:\n");
                functionStringBuilder.append("PUSH {lr}\n");
                functionStringBuilder.append("MOV r1, r0\n");
                functionStringBuilder.append("LDR r0, =msg_" + registers.getMessageCount() + "\n");
                functionStringBuilder.append("ADD r0, r0, #4\n");
                functionStringBuilder.append("BL printf\n");
                functionStringBuilder.append("MOV r0, #0\n");
                functionStringBuilder.append("BL fflush\n");
                functionStringBuilder.append("POP {pc}\n");
                headerStringBuilder.append("msg_" + registers.getMessageCount() + ":\n");
                registers.incMessageCount();
                headerStringBuilder.append(".word 3\n");
                headerStringBuilder.append(".ascii\t\"%d\\0\"\n");
            }
        }

        private void generatePrintBoolLiter(StringBuilder headerStringBuilder,
                                            StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

            mainStringBuilder.append("MOV r0, #" + (Boolean.valueOf(exprNode.getValue())?1:0) + "\n");
            mainStringBuilder.append("BL p_print_bool\n");

            if (!functionStringBuilder.toString().contains("p_print_bool:")) {
                functionStringBuilder.append("p_print_bool:\n");
                functionStringBuilder.append("PUSH {lr}\n");
                functionStringBuilder.append("CMP r0, #0\n");
                functionStringBuilder.append("LDRNE r0, =msg_" + registers.getMessageCount() + "\n");
                headerStringBuilder.append("msg_" + registers.getMessageCount() + ":\n");
                registers.incMessageCount();
                headerStringBuilder.append(".word 5\n");
                headerStringBuilder.append(".ascii\t\"true\\0\"\n");

                functionStringBuilder.append("LDREQ r0, =msg_" + registers.getMessageCount() + "\n");
                headerStringBuilder.append("msg_" + registers.getMessageCount() + ":\n");
                registers.incMessageCount();
                headerStringBuilder.append(".word 6\n");
                headerStringBuilder.append(".ascii\t\"false\\0\"\n");
                functionStringBuilder.append("ADD r0, r0, #4\n");
                functionStringBuilder.append("BL printf\n");
                functionStringBuilder.append("MOV r0, #0\n");
                functionStringBuilder.append("BL fflush\n");
                functionStringBuilder.append("POP {pc}\n");
            }
        }

        private void generatePrintStringLiter(StringBuilder headerStringBuilder,
                                              StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

            mainStringBuilder.append("LDR r0, =msg_" + registers.getMessageCount() + "\n");
            mainStringBuilder.append("BL p_print_string\n");

            headerStringBuilder.append("msg_" + registers.getMessageCount() + ": \n");
            headerStringBuilder.append(".word " + getWordLength(exprNode.getValue()) + "\n");
            headerStringBuilder.append(".ascii\t" + exprNode.getValue() + "\n");
            registers.incMessageCount();

            if (!functionStringBuilder.toString().contains("p_print_string:")) {
                functionStringBuilder.append("p_print_string:\n");
                functionStringBuilder.append("PUSH {lr}\n");
                functionStringBuilder.append("LDR r1, [r0]\n");
                functionStringBuilder.append("ADD r2, r0, #4\n");
                functionStringBuilder.append("LDR r0, =msg_" + registers.getMessageCount() + "\n");
                functionStringBuilder.append("ADD r0, r0, #4\n");
                functionStringBuilder.append("BL printf\n");
                functionStringBuilder.append("MOV r0, #0\n");
                functionStringBuilder.append("BL fflush\n");
                functionStringBuilder.append("POP {pc}\n");
                headerStringBuilder.append("msg_" + registers.getMessageCount() + ":\n");
                registers.incMessageCount();
                headerStringBuilder.append(".word 5\n");
                headerStringBuilder.append(".ascii\t\"%.*s\\0\"\n");
            }

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

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

            if (exprNode instanceof IdentNode) {
                switch (exprNode.getType()) {
                    case ("String"):
                        generatePrintStringLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
                        break;
                    case ("Bool"):
                        generatePrintBoolLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
                        break;
                    case ("Int"):
                        generatePrintIntLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
                        break;
                    case ("Char"):
                        generatePrintCharLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
                        break;
                }
            } else if (exprNode instanceof Str_literNode) {
                generatePrintStringLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            } else if (exprNode instanceof Bool_literNode) {
                generatePrintBoolLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            } else if (exprNode instanceof Int_literNode) {
                generatePrintIntLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            } else if (exprNode instanceof Char_literNode) {
                generatePrintCharLiter(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            }
            if (!functionStringBuilder.toString().contains("p_print_ln:")) {
                functionStringBuilder.append("p_print_ln:\n");
                functionStringBuilder.append("PUSH {lr}\n");
                functionStringBuilder.append("LDR r0, =msg_" + registers.getMessageCount() + "\n");
                functionStringBuilder.append("ADD r0, r0, #4\n");
                functionStringBuilder.append("BL puts\n");
                functionStringBuilder.append("MOV r0, #0\n");
                functionStringBuilder.append("BL fflush\n");
                functionStringBuilder.append("POP {pc}\n");

                headerStringBuilder.append("msg_" + registers.getMessageCount() + ":\n");
                registers.incMessageCount();
                headerStringBuilder.append(".word 1\n");
                headerStringBuilder.append(".ascii\t\"\\0\"\n");
            }
        }

        private void generatePrintCharLiter(StringBuilder headerStringBuilder,
                                            StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {
            mainStringBuilder.append("MOV r0, #'" + exprNode.getValue() + "'\n");
            mainStringBuilder.append("BL putchar\n");
            mainStringBuilder.append("BL p_print_ln\n");
        }

        private void generatePrintIntLiter(StringBuilder headerStringBuilder,
                                           StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

            mainStringBuilder.append("LDR r0, =" + exprNode.getValue() + "\n");
            mainStringBuilder.append("BL p_print_int\n");
            mainStringBuilder.append("BL p_print_ln\n");

            if (!functionStringBuilder.toString().contains("p_print_int:")) {
                functionStringBuilder.append("p_print_int:\n");
                functionStringBuilder.append("PUSH {lr}\n");
                functionStringBuilder.append("MOV r1, r0\n");
                functionStringBuilder.append("LDR r0, =msg_" + registers.getMessageCount() + "\n");
                functionStringBuilder.append("ADD r0, r0, #4\n");
                functionStringBuilder.append("BL printf\n");
                functionStringBuilder.append("MOV r0, #0\n");
                functionStringBuilder.append("BL fflush\n");
                functionStringBuilder.append("POP {pc}\n");
                headerStringBuilder.append("msg_" + registers.getMessageCount() + ":\n");
                registers.incMessageCount();
                headerStringBuilder.append(".word 3\n");
                headerStringBuilder.append(".ascii\t\"%d\\0\"\n");
            }
        }

        private void generatePrintBoolLiter(StringBuilder headerStringBuilder,
                                            StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

            mainStringBuilder.append("MOV r0, #" + (Boolean.valueOf(exprNode.getValue())?1:0) + "\n");
            mainStringBuilder.append("BL p_print_bool\n");
            mainStringBuilder.append("BL p_print_ln\n");

            if (!functionStringBuilder.toString().contains("p_print_bool:")) {
                functionStringBuilder.append("p_print_bool:\n");
                functionStringBuilder.append("PUSH {lr}\n");
                functionStringBuilder.append("CMP r0, #0\n");
                functionStringBuilder.append("LDRNE r0, =msg_" + registers.getMessageCount() + "\n");
                headerStringBuilder.append("msg_" + registers.getMessageCount() + ":\n");
                registers.incMessageCount();
                headerStringBuilder.append(".word 5\n");
                headerStringBuilder.append(".ascii\t\"true\\0\"\n");

                functionStringBuilder.append("LDREQ r0, =msg_" + registers.getMessageCount() + "\n");
                headerStringBuilder.append("msg_" + registers.getMessageCount() + ":\n");
                registers.incMessageCount();
                headerStringBuilder.append(".word 6\n");
                headerStringBuilder.append(".ascii\t\"false\\0\"\n");
                functionStringBuilder.append("ADD r0, r0, #4\n");
                functionStringBuilder.append("BL printf\n");
                functionStringBuilder.append("MOV r0, #0\n");
                functionStringBuilder.append("BL fflush\n");
                functionStringBuilder.append("POP {pc}\n");
            }
        }

        private void generatePrintStringLiter(StringBuilder headerStringBuilder,
                                              StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

            mainStringBuilder.append("LDR r0, =msg_" + registers.getMessageCount() + "\n");
            mainStringBuilder.append("BL p_print_string\n");
            mainStringBuilder.append("BL p_print_ln\n");

            headerStringBuilder.append("msg_" + registers.getMessageCount() + ": \n");
            headerStringBuilder.append(".word " + getWordLength(exprNode.getValue()) + "\n");
            headerStringBuilder.append(".ascii\t" + exprNode.getValue() + "\n");
            registers.incMessageCount();

            if (!functionStringBuilder.toString().contains("p_print_string:")) {
                functionStringBuilder.append("p_print_string:\n");
                functionStringBuilder.append("PUSH {lr}\n");
                functionStringBuilder.append("LDR r1, [r0]\n");
                functionStringBuilder.append("ADD r2, r0, #4\n");
                functionStringBuilder.append("LDR r0, =msg_" + registers.getMessageCount() + "\n");
                functionStringBuilder.append("ADD r0, r0, #4\n");
                functionStringBuilder.append("BL printf\n");
                functionStringBuilder.append("MOV r0, #0\n");
                functionStringBuilder.append("BL fflush\n");
                functionStringBuilder.append("POP {pc}\n");
                headerStringBuilder.append("msg_" + registers.getMessageCount() + ":\n");
                registers.incMessageCount();
                headerStringBuilder.append(".word 5\n");
                headerStringBuilder.append(".ascii\t\"%.*s\\0\"\n");
            }

        }

        public String getValue(IdentNode identNode) {
            ASTNode parent = getParent();
            boolean found = false;
            while (parent != null) {
                if (parent.getSymbolTable().containsKey(identNode.getIdent())) {
                    return ((TypeNode) parent.getSymbolTable().get(identNode.getIdent())).getValue();
                }
                parent = parent.getParent();
            }
            if (found == false) {
                return null;
            }
            return null;
        }

    }


    /*
     * IfNode has its own scope
     * Correctly checked the condition and ensure the statements are vaild
     */
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
                throwSemanticError("If statement can only take boolean argument");
            }
            statNodeTrue.check();
            if (statNodeFalse != null) {
                statNodeFalse.check();
            }
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

    }

    /*
     * WhileNode has its own scope
     * Correctly checked the condition and ensure the statements are vaild
     */
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
                throwSemanticError("While statement condition can only take boolean argument");
            }
            statNode.check();
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
        public String getType() {
            return "Begin";
        }

        @Override
        public void check() {

            setScope(true);
            statNode.check();

        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

    }

    /*
     * To introduce more statemtns
     */
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

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {
            statNodeFirst.generate(headerStringBuilder, mainStringBuilder, functionStringBuilder);
            statNodeSecond.generate(headerStringBuilder, mainStringBuilder, functionStringBuilder);
        }

    }

    /*
     * FSTNode has the type Pair
     * Need to return to correct type
     */
    public class FSTNode extends ASTNode {

        private IdentNode exprNode;

        public FSTNode(IdentNode exprNode) {
            this.exprNode = exprNode;
            exprNode.setParent(this);
        }

        public String getType() {

            return ((Pair_typeNode) (exprNode.getTypeNode())).getFirstElem();
        }

        public TypeNode getTypeNode() {
            return exprNode.getTypeNode();
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().contains("Pair")) {
                throwSemanticError("The FST statement can only take argument of type pair");
            }

        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

    }

    /*
     * SNDNode has the type Pair
     * Need to return to correct type
     */
    public class SNDNode extends ASTNode {

        private IdentNode exprNode;

        public SNDNode(IdentNode exprNode) {
            this.exprNode = exprNode;
            exprNode.setParent(this);
        }

        public String getType() {

            return ((Pair_typeNode) (exprNode.getTypeNode())).getSecondElem();
        }

        public TypeNode getTypeNode() {
            return exprNode.getTypeNode();
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().contains("Pair")) {
                throwSemanticError("The SND statement can only take argument of type pair");
            }

        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

    }

    /*
     * Including base-type, array-type, pair-type
     * The getType() method will return the type as String
     */
    public abstract class TypeNode extends ASTNode {

        protected String type;
        private String value;

        public TypeNode() {
            type = "";
        }

        @Override
        public boolean equals(Object that) {
            return getType().equals(((TypeNode) that).getType());
        }

        @Override
        public void check() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
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
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

        public String getElemType() {
            return typeNode.getType();
        }


    }

    /*
     * Representing the nodes of the parameters of the function
     * Save all parameters in the relating function scope
     */
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

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
            return "Pair";
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }
    }

    /*
     * Each different unary-operation needs to implement their own check() method in order to get meaningful
     * error message and condition.
     */
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

        public ExprNode getExprNdoe() {
            return exprNode;
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
                throwSemanticError("Not operator only take boolean argument");
            }

        }

        @Override
        public String getValue() {
            return String.valueOf(!(Boolean.parseBoolean(exprNode.getValue())));
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
                throwSemanticError("Negate operator only take int argument");
            }

        }

        @Override
        public String getValue() {
            return String.valueOf(-Integer.valueOf(exprNode.getValue()));
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
                throwSemanticError("Len operator only take int argument");
            }

        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
                throwSemanticError("Ord operator only take int argument");
            }

        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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
                throwSemanticError("Char operator only take int argument");
            }

        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

    }

    /*
     * Different binary-operation has different semantic requirement
     * All sub-classes overwrite the check() method
     */
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

            if (!exp1.getType().equals(exp2.getType()) && !exp2.getType().equals("Null")) {
                throwSemanticError("Binary operation need both sides have the same type");
            } else if (!exp1.getType().contains("Pair") && exp2.getType().contains("Pair")) {
                throwSemanticError("Binary operation for pair need to have type pair on both sides");
            }
        }
    }

    public class MultNode extends Binary_operNode {

        public MultNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "*";
        }

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on mutiply binary operator");
            } else if (!exp1.getType().equals("Int")) {
                throwSemanticError("Multiplication can only take int arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

    }

    public class DivNode extends Binary_operNode {

        public DivNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "/";
        }

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on divide binary operator");
            } else if (!exp1.getType().equals("Int")) {
                throwSemanticError("Division can only take int arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }
    }

    public class ModNode extends Binary_operNode {

        public ModNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "%";
        }

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on mod binary operator");
            } else if (!exp1.getType().equals("Int")) {
                throwSemanticError("Modules can only take int arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

    }

    public class PlusNode extends Binary_operNode {

        public PlusNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "+";
        }

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on add binary operator");
            } else if (!exp1.getType().equals("Int")) {
                throwSemanticError("Addition can only take int arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }
    }

    public class MinusNode extends Binary_operNode {

        public MinusNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "-";
        }

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on minus binary operator");
            } else if (!exp1.getType().equals("Int")) {
                throwSemanticError("Minus binary operator can only take int arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on greater binary operator");
            } else if (!(exp1.getType().equals("Int") || exp1.getType().equals("Char"))) {
                throwSemanticError("Greater binary operator can only take int or char arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on greater or equal binary operator");
            } else if (!(exp1.getType().equals("Int") || exp1.getType().equals("Char"))) {
                throwSemanticError("Greater or equal binary operator can only take int or char arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on smaller binary operator");
            } else if (!(exp1.getType().equals("Int") || exp1.getType().equals("Char"))) {
                throwSemanticError("Smaller binary operator can only take int or char arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on smaller or equal binary operator");
            } else if (!(exp1.getType().equals("Int") || exp1.getType().equals("Char"))) {
                throwSemanticError("Smaller or equal binary operator can only take int or char arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on equal binary operator");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

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

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on not equal binary operator");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }
    }

    public class LogicalAndNode extends Binary_operNode {

        public LogicalAndNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "&&";
        }

        @Override
        public void check() {
            exp1.check();
            exp2.check();

            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on not logical and operator");
            } else if (!exp1.getType().equals("Bool")) {
                throwSemanticError("Logical and operator can only take bool arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }
    }

    public class LogicalOrNode extends Binary_operNode {

        public LogicalOrNode(ASTNode exp1, ASTNode exp2) {
            super(exp1, exp2);
            binOp = "||";
        }

        @Override
        public void check() {
            exp1.check();
            exp2.check();
            if (!exp1.getType().equals(exp2.getType())) {
                throwSemanticError("Both expressions must have the same type on not logical or operator");
            } else if (!exp1.getType().equals("Bool")) {
                throwSemanticError("Logical or operator can only take bool arguments");
            }
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }
    }

    public abstract class ExprNode extends StatNode {
        @Override
        public void check() {
        }

        public abstract String getValue();
    }

    /*
     * IdentNode checks through the related scopes for the detail of a varible
     * getType() method returns the correct type of the ident
     */
    public class IdentNode extends ExprNode {

        private String ident;
        private String value;

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

            if (typeNode == null) {
                return "";
            }
            return typeNode.getType();

        }

        @Override
        public void check() {
            checkIfVaribleExist(this);
        }

        @Override
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

    }

    /*
     * The single element of an array
     */
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
        public String getValue() {
            return null;
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

        @Override
        public String getType() {
            String result = lookupSymbolTable(this, identNode.getIdent()).getType();
            if (result.equals("String")) {
                result = "Char";
            } else {
                result = result.replace("[]", "");
            }
            return result;
        }

        private TypeNode lookupSymbolTable(ASTNode currentScope, String string) {
            while (!currentScope.getScope()) {
                currentScope = currentScope.getParent();
            }
            return (TypeNode) currentScope.getSymbolTable().get(string);
        }
    }

    /*
     * All liters use the getType() method to return their type
     */
    public class Int_literNode extends ExprNode {

        private String value;
        private String sign;

        public Int_literNode(String sign, String value) {
            this.value = value;
            this.sign = sign;
        }

        @Override
        public String getType() {
            return "Int";
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

        public int getvalue() {
            if (sign.equals("-")) {
                return Integer.parseInt(value) / (-1);
            }
            return Integer.parseInt(value);
        }

        @Override
        public String getValue() {
            return String.valueOf(getvalue()) ;
        }
    }


    public class Bool_literNode extends ExprNode {

        private boolean value;

        public Bool_literNode(String value) {
            this.value = Boolean.parseBoolean(value);
        }

        public String getValue() {
            return String.valueOf(value);
        }

        @Override
        public String getType() {

            return "Bool";
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }
    }

    public class Char_literNode extends ExprNode {

        private char value;

        public Char_literNode(String value) {
            if (value.charAt(1) == '\\' && value.charAt(2) != '0') {
                this.value = value.charAt(2);
            } else {
                this.value = value.charAt(1);
            }
        }

        @Override
        public String getType() {

            return "Char";
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

        @Override
        public String getValue() {
            return Character.toString(value);
        }
    }

    public class Str_literNode extends ExprNode {

        private String value;

        public Str_literNode(String value) {
            this.value = value;
        }

        @Override
        public String getType() {

            return "String";
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

        @Override
        public String getValue() {
            return value;
        }
    }


    public class Array_literNode extends ASTNode {

        List<ASTNode> exprNodeList;

        public Array_literNode(List<ASTNode> exprNodeList) {

            this.exprNodeList = exprNodeList;
            for (ASTNode node : exprNodeList) {
                node.setParent(this);
            }

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

            for (ASTNode astNode : exprNodeList) {
                astNode.check();
            }
            if (!getType().equals("")) {
                for (ASTNode astNode : exprNodeList) {
                    if (!astNode.getType().equals(getElemType())) {
                        throwSemanticError("Array need to contain same type arguments");
                    }
                }
            }
        }

        public int getLength() {
            return exprNodeList.size();
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }
    }

    public class Pair_literNode extends ExprNode {

        @Override
        public String getType() {

            return "Null";
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }

        @Override
        public String getValue() {
            return null;
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
            return "Pair";
        }

        @Override
        public void check() {
            exprNode1.check();
            exprNode2.check();
        }

    }

    /*
     * Used when calling a method
     * Check the if the arguments are valid or not
     * Goes through the function symboltable in program scope
     */
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
            FuncNode funcNode = (FuncNode) getRoot().getFunctionSymbolTable().get(identNode.getIdent());
            return funcNode.getType();
        }

        public TypeNode getTypeNode() {
            return identNode.getTypeNode();
        }

        @Override
        public void check() {

            for (ExprNode exprNode : exprNodeList) {
                exprNode.check();
            }
            if (!getRoot().getFunctionSymbolTable().containsKey(identNode.getIdent())) {
                throwSemanticError("The function " + identNode.getIdent() + " has not been declared");
            }
            funcNode = (FuncNode) getRoot().getFunctionSymbolTable().get(identNode.getIdent());
            if (funcNode.getParamNodes().size() != exprNodeList.size()) {
                throwSemanticError("Argument size not matched in function: " + identNode.getIdent());
            }
            for (int i = 0; i < exprNodeList.size(); i++) {
                if (!funcNode.getParamNodes().get(i).getType().equals(exprNodeList.get(i).getType())) {
                    throwSemanticError("The " + (i + 1) + "th argument in function "
                            + identNode.getIdent() + " not match");
                }
            }
        }

        @Override
        public void generate(StringBuilder headerStringBuilder,
                             StringBuilder mainStringBuilder, StringBuilder functionStringBuilder) {

        }
    }

}
