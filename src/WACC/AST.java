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

    private boolean isLastState = true;

    private String resultReg = registers.get(0).toString();

    // label counters
    private int messageCount = 0;
    private int labelCount = 0;

    public ProgramNode getRoot() {
        return root;
    }

    public void setRoot(ProgramNode root) {
        this.root = root;
    }

    public int getWordLength(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\\') {
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
        protected Stack stack;

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

        public abstract String getValue();

        public abstract void check();

        public abstract void generate(AssemblyBuilder builder);

        public void setCurrentStack(Stack stack) {
            this.stack = stack;
        }
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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {
            stack = new Stack();
            statNode.setCurrentStack(stack);

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

        public void generate(AssemblyBuilder builder) {
            builder.setCurrent(builder.getMain());
            builder.getHeader().append(".data\n");
            builder.getMain().append(".text\n");
            builder.getMain().append(".global main\n");
            builder.getMain().append("main: \n");
            builder.getMain().append("PUSH {lr}  \n");
            for (FuncNode funcNode : functionNodes) {
                funcNode.generate(builder);
            }
            statNode.generate(builder);
            builder.getMain().append("MOV " + resultReg + ", #0\n");
            registers.get(0).setValue(0);
            builder.getMain().append("POP {pc}\n");
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

        @Override
        public String getValue() {
            return null;
        }

        public IdentNode getIdentNode() {
            return identNode;
        }

        public List<ParamNode> getParamNodes() {
            return paramNodes;
        }

        @Override
        public void check() {
            stack = new Stack();
            statNode.setCurrentStack(stack);

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
        public void generate(AssemblyBuilder builder) {

            builder.setCurrent(builder.getFunction());
            statNode.generate(builder);
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

        public void addBackToStack(AssemblyBuilder builder) {
            int stackSize = stack.getSize();
            int num = stackSize / Stack.MAX_STACK_SIZE;
            int remainder = stackSize % Stack.MAX_STACK_SIZE;
            if (isLastState && stack.IfDeclarationCodeGenerated()) {
                for (int i = 0; i < num; i++) {
                    builder.getCurrent().append("ADD sp, sp, #" + Stack.MAX_STACK_SIZE + "\n");
                }
                builder.getCurrent().append("ADD sp, sp, #" + remainder + "\n");
            }
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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            this.typeNode.setParent(this);
            this.typeNode.setIdent(identNode.getIdent());
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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {

            putIntoSymbolTable(this, identNode.getIdent(), typeNode);
            assign_rhsNode.check();
            stack.add(identNode.getIdent(), typeNode.getNumOfByte());
            assign_rhsNode.setCurrentStack(stack);


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
        public void generate(AssemblyBuilder builder) {
            if (assign_rhsNode instanceof IdentNode) {
                typeNode.setIdent(((IdentNode) assign_rhsNode).getIdent());
            }
            setTypeNodeValue(typeNode, assign_rhsNode);
            int stackSize = stack.getSize();
            int num = stackSize / Stack.MAX_STACK_SIZE;
            int remainder = stackSize % Stack.MAX_STACK_SIZE;
            if (!stack.IfDeclarationCodeGenerated()) {
                for (int i = 0; i < num; i++) {
                    builder.getCurrent().append("SUB sp, sp, #" + Stack.MAX_STACK_SIZE + "\n");
                }
                builder.getCurrent().append("SUB sp, sp, #" + remainder + "\n");
                stack.setIfDeclarationCodeGenerated(true);
            }
            assign_rhsNode.generate(builder);
            if (!(getParent() instanceof MultipleStatNode)) {
                addBackToStack(builder);
            }
        }

        private void setTypeNodeValue(TypeNode typeNode, ASTNode assign_rhsNode) {
            typeNode.setValue((assign_rhsNode).getValue());
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
        public String getValue() {
            return null;
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
        public void generate(AssemblyBuilder builder) {
            if (assign_lhsNode instanceof IdentNode) {
                if (assign_rhsNode instanceof IdentNode) {
                    ((IdentNode) assign_lhsNode).getTypeNode().setIdent(((IdentNode) assign_rhsNode).getIdent());
                }
                if (assign_rhsNode instanceof Str_literNode) {
                    getTypeNode((IdentNode) assign_lhsNode).setValue((assign_rhsNode).getValue());
                } else if (assign_rhsNode.getType().equals("Int")) {
                    getTypeNode((IdentNode) assign_lhsNode).setValue((assign_rhsNode.getValue()));
                }
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
        public String getValue() {
            return null;
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
        public void generate(AssemblyBuilder builder) {
            if (assign_lhsNode instanceof IdentNode) {
                switch (assign_lhsNode.getType()) {
                    case ("String"):
                        generateReadStringLiter(builder);
                        break;
                    case ("Bool"):
                        generateReadBoolLiter(builder);
                        break;
                    case ("Int"):
                        generateReadIntLiter(builder);
                        break;
                    case ("Char"):
                        generateReadCharLiter(builder);
                        break;
                }
            }
        }

        private void generateReadCharLiter(AssemblyBuilder builder) {
            builder.getCurrent().append("ADD r0, sp, #0\n");
            builder.getCurrent().append("BL p_read_char\n");
            if (!builder.getLabel().toString().contains("p_read_char:")) {
                builder.getLabel().append("p_read_char:\n");
                builder.getLabel().append("PUSH {lr}\n");
                builder.getLabel().append("MOV r1, r0\n");
                builder.getLabel().append("LDR r0, =msg_" + messageCount + "\n");
                builder.getLabel().append("ADD r0, r0, #4\n");
                builder.getLabel().append("BL scanf\n");
                builder.getLabel().append("POP {pc}\n");

                builder.getHeader().append("msg_" + messageCount + ":\n");
                builder.getHeader().append(".word 4\n");
                builder.getHeader().append(".ascii\t\" %c\\0\"\n");
                messageCount++;
            }

        }

        private void generateReadIntLiter(AssemblyBuilder builder) {

            builder.getCurrent().append("ADD r0, sp, #0\n");
            builder.getCurrent().append("BL p_read_int\n");
            ((IdentNode) assign_lhsNode).getTypeNode().setValue("[sp]");
            if (!builder.getLabel().toString().contains("p_read_int:")) {
                builder.getLabel().append("p_read_int:\n");
                builder.getLabel().append("PUSH {lr}\n");
                builder.getLabel().append("MOV r1, r0\n");
                builder.getLabel().append("LDR r0, =msg_" + messageCount + "\n");
                builder.getLabel().append("ADD r0, r0, #4\n");
                builder.getLabel().append("BL scanf\n");
                builder.getLabel().append("POP {pc}\n");

                builder.getHeader().append("msg_" + messageCount + ":\n");
                builder.getHeader().append(".word 3\n");
                builder.getHeader().append(".ascii\t\"%d\\0\"\n");
                messageCount++;
            }

        }

        private void generateReadBoolLiter(AssemblyBuilder builder) {

        }

        private void generateReadStringLiter(AssemblyBuilder builder) {
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
        public String getValue() {
            return null;
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
        public void generate(AssemblyBuilder builder) {

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
        public String getValue() {
            return null;
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
        public void generate(AssemblyBuilder builder) {

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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {
            exprNode.check();

            if (!exprNode.getType().equals("Int")) {
                throwSemanticError("The exit statement must take int argument");
            }
        }

        @Override
        public void generate(AssemblyBuilder builder) {
            int exitNum = 0;
            if (exprNode instanceof NegateOperNode) {
                NegateOperNode negateOperNode = (NegateOperNode) exprNode;
                exitNum = ((Int_literNode) negateOperNode.getExprNdoe()).getvalue() / -1;
            } else if (exprNode instanceof IdentNode) {
                builder.getCurrent().append("LDR " + resultReg + ", =" + exprNode.getValue() + "\n");
                builder.getCurrent().append("BL exit\n");
                return;
            } else {
                Int_literNode int_literNode = (Int_literNode) exprNode;
                exitNum = int_literNode.getvalue();
            }
            builder.getCurrent().append("LDR " + resultReg + ", =" + exitNum + "\n");
            builder.getCurrent().append("BL exit\n");
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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {
            exprNode.check();
        }

        @Override
        public void generate(AssemblyBuilder builder) {
            if (exprNode.getType().equals("String")) {
                generatePrintStringLiter(builder);
            }else if (exprNode.getType().equals("Bool")) {
                generatePrintBoolLiter(builder);
            }else if (exprNode.getType().equals("Int")) {
                generatePrintIntLiter(builder);
            }else if (exprNode.getType().equals("Char")) {
                generatePrintCharLiter(builder);
            }
        }

        private void generatePrintCharLiter(AssemblyBuilder builder) {
            if (exprNode instanceof IdentNode) {
                builder.getMain().append("LDRSB r0, [sp]\n");
            } else {
                builder.getMain().append("MOV r0, #" + exprNode.getValue() + "\n");
            }
            builder.getMain().append("BL putchar\n");
        }

        private void generatePrintIntLiter(AssemblyBuilder builder) {

//            if (exprNode instanceof IdentNode) {
//                builder.getMain().append("LDR r0, [sp]");
//            } else {
                builder.getMain().append("LDR r0, =" + exprNode.getValue() + "\n");
//            }


            builder.getMain().append("BL p_print_int\n");

            if (!builder.getLabel().toString().contains("p_print_int:")) {
                builder.getLabel().append("p_print_int:\n");
                builder.getLabel().append("PUSH {lr}\n");
                builder.getLabel().append("MOV r1, r0\n");
                builder.getLabel().append("LDR r0, =msg_" + messageCount + "\n");
                builder.getLabel().append("ADD r0, r0, #4\n");
                builder.getLabel().append("BL printf\n");
                builder.getLabel().append("MOV r0, #0\n");
                builder.getLabel().append("BL fflush\n");
                builder.getLabel().append("POP {pc}\n");
                builder.getHeader().append("msg_" + messageCount + ":\n");
                messageCount++;
                builder.getHeader().append(".word 3\n");
                builder.getHeader().append(".ascii\t\"%d\\0\"\n");
            }
        }

        private void generatePrintBoolLiter(AssemblyBuilder builder) {

            builder.getMain().append("MOV r0, #" + (Boolean.valueOf(exprNode.getValue()) ? 1 : 0) + "\n");
            builder.getMain().append("BL p_print_bool\n");

            if (!builder.getLabel().toString().contains("p_print_bool:")) {
                builder.getLabel().append("p_print_bool:\n");
                builder.getLabel().append("PUSH {lr}\n");
                builder.getLabel().append("CMP r0, #0\n");
                builder.getLabel().append("LDRNE r0, =msg_" + messageCount + "\n");
                builder.getHeader().append("msg_" + messageCount + ":\n");
                messageCount++;
                builder.getHeader().append(".word 5\n");
                builder.getHeader().append(".ascii\t\"true\\0\"\n");

                builder.getLabel().append("LDREQ r0, =msg_" + messageCount + "\n");
                builder.getHeader().append("msg_" + messageCount + ":\n");
                messageCount++;
                builder.getHeader().append(".word 6\n");
                builder.getHeader().append(".ascii\t\"false\\0\"\n");
                builder.getLabel().append("ADD r0, r0, #4\n");
                builder.getLabel().append("BL printf\n");
                builder.getLabel().append("MOV r0, #0\n");
                builder.getLabel().append("BL fflush\n");
                builder.getLabel().append("POP {pc}\n");
            }
        }

        private void generatePrintStringLiter(AssemblyBuilder builder) {

            builder.getCurrent().append("LDR r0, =msg_" + messageCount + "\n");
            builder.getCurrent().append("BL p_print_string\n");

            builder.getHeader().append("msg_" + messageCount + ": \n");
            builder.getHeader().append(".word " + getWordLength(exprNode.getValue()) + "\n");
            builder.getHeader().append(".ascii\t" + exprNode.getValue() + "\n");
            messageCount++;

            if (!builder.getLabel().toString().contains("p_print_string:")) {
                builder.getLabel().append("p_print_string:\n");
                builder.getLabel().append("PUSH {lr}\n");
                builder.getLabel().append("LDR r1, [r0]\n");
                builder.getLabel().append("ADD r2, r0, #4\n");
                builder.getLabel().append("LDR r0, =msg_" + messageCount + "\n");
                builder.getLabel().append("ADD r0, r0, #4\n");
                builder.getLabel().append("BL printf\n");
                builder.getLabel().append("MOV r0, #0\n");
                builder.getLabel().append("BL fflush\n");
                builder.getLabel().append("POP {pc}\n");
                builder.getHeader().append("msg_" + messageCount + ":\n");
                messageCount++;
                builder.getHeader().append(".word 5\n");
                builder.getHeader().append(".ascii\t\"%.*s\\0\"\n");
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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {
            exprNode.check();
        }

        @Override
        public void generate(AssemblyBuilder builder) {
            if (exprNode.getType().equals("String")) {
                generatePrintStringLiter(builder);
            }else if (exprNode.getType().equals("Bool")) {
                generatePrintBoolLiter(builder);
            }else if (exprNode.getType().equals("Int")) {
                generatePrintIntLiter(builder);
            }else if (exprNode.getType().equals("Char")) {
                generatePrintCharLiter(builder);
            }
            if (!builder.getLabel().toString().contains("p_print_ln:")) {
                builder.getLabel().append("p_print_ln:\n");
                builder.getLabel().append("PUSH {lr}\n");
                builder.getLabel().append("LDR r0, =msg_" + messageCount + "\n");
                builder.getLabel().append("ADD r0, r0, #4\n");
                builder.getLabel().append("BL puts\n");
                builder.getLabel().append("MOV r0, #0\n");
                builder.getLabel().append("BL fflush\n");
                builder.getLabel().append("POP {pc}\n");

                builder.getHeader().append("msg_" + messageCount + ":\n");
                messageCount++;
                builder.getHeader().append(".word 1\n");
                builder.getHeader().append(".ascii\t\"\\0\"\n");
            }
        }

        private void generatePrintCharLiter(AssemblyBuilder builder) {
            if (exprNode instanceof IdentNode) {
                builder.getCurrent().append("LDRSB r0, [sp]\n");
            } else {
                builder.getCurrent().append("MOV r0, #" + exprNode.getValue() + "\n");
            }
            builder.getCurrent().append("BL putchar\n");
            builder.getCurrent().append("BL p_print_ln\n");
        }

        private void generatePrintIntLiter(AssemblyBuilder builder) {
            String value =  exprNode.getValue().equals("[sp]") ? "[sp]" : "=" + exprNode.getValue();

            builder.getCurrent().append("LDR r0, " + value + "\n");
            builder.getCurrent().append("BL p_print_int\n");
            builder.getCurrent().append("BL p_print_ln\n");

            if (!builder.getLabel().toString().contains("p_print_int:")) {
                builder.getLabel().append("p_print_int:\n");
                builder.getLabel().append("PUSH {lr}\n");
                builder.getLabel().append("MOV r1, r0\n");
                builder.getLabel().append("LDR r0, =msg_" + messageCount + "\n");
                builder.getLabel().append("ADD r0, r0, #4\n");
                builder.getLabel().append("BL printf\n");
                builder.getLabel().append("MOV r0, #0\n");
                builder.getLabel().append("BL fflush\n");
                builder.getLabel().append("POP {pc}\n");
                builder.getHeader().append("msg_" + messageCount + ":\n");
                messageCount++;
                builder.getHeader().append(".word 3\n");
                builder.getHeader().append(".ascii\t\"%d\\0\"\n");
            }
        }

        private void generatePrintBoolLiter(AssemblyBuilder builder) {

            builder.getCurrent().append("MOV r0, #" + (Boolean.valueOf(exprNode.getValue()) ? 1 : 0) + "\n");
            builder.getCurrent().append("BL p_print_bool\n");
            builder.getCurrent().append("BL p_print_ln\n");

            if (!builder.getLabel().toString().contains("p_print_bool:")) {
                builder.getLabel().append("p_print_bool:\n");
                builder.getLabel().append("PUSH {lr}\n");
                builder.getLabel().append("CMP r0, #0\n");
                builder.getLabel().append("LDRNE r0, =msg_" + messageCount + "\n");
                builder.getHeader().append("msg_" + messageCount + ":\n");
                messageCount++;
                builder.getHeader().append(".word 5\n");
                builder.getHeader().append(".ascii\t\"true\\0\"\n");

                builder.getLabel().append("LDREQ r0, =msg_" + messageCount + "\n");
                builder.getHeader().append("msg_" + messageCount + ":\n");
                messageCount++;
                builder.getHeader().append(".word 6\n");
                builder.getHeader().append(".ascii\t\"false\\0\"\n");
                builder.getLabel().append("ADD r0, r0, #4\n");
                builder.getLabel().append("BL printf\n");
                builder.getLabel().append("MOV r0, #0\n");
                builder.getLabel().append("BL fflush\n");
                builder.getLabel().append("POP {pc}\n");
            }
        }

        private void generatePrintStringLiter(AssemblyBuilder builder) {

            builder.getCurrent().append("LDR r0, =msg_" + messageCount + "\n");
            builder.getCurrent().append("BL p_print_string\n");
            builder.getCurrent().append("BL p_print_ln\n");

            builder.getHeader().append("msg_" + messageCount + ": \n");
            builder.getHeader().append(".word " + getWordLength(exprNode.getValue()) + "\n");
            builder.getHeader().append(".ascii\t" + exprNode.getValue() + "\n");
            messageCount++;

            if (!builder.getLabel().toString().contains("p_print_string:")) {
                builder.getLabel().append("p_print_string:\n");
                builder.getLabel().append("PUSH {lr}\n");
                builder.getLabel().append("LDR r1, [r0]\n");
                builder.getLabel().append("ADD r2, r0, #4\n");
                builder.getLabel().append("LDR r0, =msg_" + messageCount + "\n");
                builder.getLabel().append("ADD r0, r0, #4\n");
                builder.getLabel().append("BL printf\n");
                builder.getLabel().append("MOV r0, #0\n");
                builder.getLabel().append("BL fflush\n");
                builder.getLabel().append("POP {pc}\n");
                builder.getHeader().append("msg_" + messageCount + ":\n");
                messageCount++;
                builder.getHeader().append(".word 5\n");
                builder.getHeader().append(".ascii\t\"%.*s\\0\"\n");
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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {
            Stack stackTrue = new Stack();
            Stack stackFalse = new Stack();
            statNodeTrue.setCurrentStack(stackTrue);
            statNodeFalse.setCurrentStack(stackFalse);

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
        public void generate(AssemblyBuilder builder) {

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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {
            Stack whileBodyStack = new Stack();
            statNode.setCurrentStack(whileBodyStack);

            exprNode.check();

            if (!exprNode.getType().equals("Bool")) {
                throwSemanticError("While statement condition can only take boolean argument");
            }
            statNode.check();
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {
            stack = new Stack();
            statNode.setCurrentStack(stack);
            setScope(true);
            statNode.check();

        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {
            statNodeFirst.setCurrentStack(stack);
            statNodeSecond.setCurrentStack(stack);

            statNodeFirst.check();
            statNodeSecond.check();
        }

        @Override
        public void generate(AssemblyBuilder builder) {
            isLastState = false;
            statNodeFirst.generate(builder);
            if (!(getParent() instanceof MultipleStatNode)) {
                isLastState = true;
            }
            statNodeSecond.generate(builder);
            statNodeSecond.addBackToStack(builder);

        }

    }

    public abstract class Pair_elemNode extends ASTNode {

        protected IdentNode exprNode;

        public Pair_elemNode(IdentNode exprNode) {
            this.exprNode = exprNode;
            exprNode.setParent(this);
        }

        public IdentNode getIdentNode() {
            return exprNode;
        }

        public TypeNode getTypeNode() {
            return exprNode.getTypeNode();
        }
    }

    /*
     * FSTNode has the type Pair
     * Need to return to correct type
     */
    public class FSTNode extends Pair_elemNode {

        private IdentNode exprNode;

        public FSTNode(IdentNode exprNode) {
            super(exprNode);
        }

        public String getType() {

            return ((Pair_typeNode) getTypeNode()).getFirstElem();
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().contains("Pair")) {
                throwSemanticError("The FST statement can only take argument of type pair");
            }

        }

        @Override
        public void generate(AssemblyBuilder builder) {

        }

    }

    /*
     * SNDNode has the type Pair
     * Need to return to correct type
     */
    public class SNDNode extends Pair_elemNode {

        private IdentNode exprNode;

        public SNDNode(IdentNode exprNode) {
            super(exprNode);
        }

        public String getType() {

            return ((Pair_typeNode) getTypeNode()).getSecondElem();
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void check() {

            exprNode.check();

            if (!exprNode.getType().contains("Pair")) {
                throwSemanticError("The SND statement can only take argument of type pair");
            }

        }

        @Override
        public void generate(AssemblyBuilder builder) {

        }

    }

    /*
     * Including base-type, array-type, pair-type
     * The getType() method will return the type as String
     */
    public abstract class TypeNode extends ASTNode {

        protected String type;
        private String value;
        private String ident;

        public TypeNode() {
            type = "";
        }

        public String getIdent() {
            return ident;
        }

        public void setIdent(String ident) {
            this.ident = ident;
        }

        @Override
        public boolean equals(Object that) {
            return getType().equals(((TypeNode) that).getType());
        }

        @Override
        public void check() {
        }

        public abstract int getNumOfByte();

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
        public int getNumOfByte() {
            return 4;
        }

        @Override
        public String getType() {
            return "Int";
        }

        @Override
        public void generate(AssemblyBuilder builder) {

        }

    }

    public class BoolTypeNode extends Base_typeNode {

        public BoolTypeNode() {
            type = "bool";
        }

        @Override
        public int getNumOfByte() {
            return 1;
        }

        @Override
        public String getType() {
            return "Bool";
        }

        @Override
        public void generate(AssemblyBuilder builder) {

        }

    }

    public class CharTypeNode extends Base_typeNode {

        public CharTypeNode() {
            type = "char";
        }

        @Override
        public int getNumOfByte() {
            return 1;
        }

        @Override
        public String getType() {
            return "Char";
        }

        @Override
        public void generate(AssemblyBuilder builder) {

        }

    }

    public class StringTypeNode extends Base_typeNode {

        public StringTypeNode() {

            type = "string";
        }

        @Override
        public int getNumOfByte() {
            return 4;
        }

        @Override
        public String getType() {
            return "String";
        }

        @Override
        public void generate(AssemblyBuilder builder) {

        }

    }


    public class Array_typeNode extends TypeNode {

        private TypeNode typeNode;

        public Array_typeNode(TypeNode typeNode) {

            this.typeNode = typeNode;
            typeNode.setParent(this);

        }

        @Override
        public int getNumOfByte() {
            return -1;
        }

        @Override
        public String getType() {
            return typeNode.getType() + "[]";
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
        public String getValue() {
            return null;
        }

        @Override
        public void check() {
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
        public int getNumOfByte() {
            return -1;
        }

        @Override
        public String getType() {
            return "Pair";
        }

        @Override
        public void generate(AssemblyBuilder builder) {

        }

    }

    public class PairNode extends TypeNode {

        @Override
        public int getNumOfByte() {
            return -1;
        }

        @Override
        public String getType() {
            return "Pair";
        }

        @Override
        public void check() {

        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
        public void generate(AssemblyBuilder builder) {

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
        public void generate(AssemblyBuilder builder) {

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
        public void generate(AssemblyBuilder builder) {

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
            return String.valueOf((int)exprNode.getValue().charAt(1));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

        }
    }

    public class CharOperNode extends Unary_operNode {

        public CharOperNode(ExprNode exprNode) {

            super(exprNode);
            unOp = "chr";

        }

        @Override
        public String getType() {

            return "Char";
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
            int value = Integer.valueOf(exprNode.getValue());
            return "\'" + (char) value + "\'";
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            return String.valueOf(Integer.valueOf(exp1.getValue()) * Integer.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            return String.valueOf(Integer.valueOf(exp1.getValue()) / Integer.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            return String.valueOf(Integer.valueOf(exp1.getValue()) % Integer.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            return String.valueOf(Integer.valueOf(exp1.getValue()) + Integer.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            return String.valueOf(Integer.valueOf(exp1.getValue()) - Integer.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            if (exp1.getType().equals("Char")) {
                return String.valueOf((int)((exp1).getValue().charAt(1)) > ((int)(exp2).getValue().charAt(1)));
            }
            return String.valueOf(Integer.valueOf(exp1.getValue()) > Integer.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            if (exp1.getType().equals("Char")) {
                return String.valueOf((int)((exp1).getValue().charAt(1)) >= ((int)(exp2).getValue().charAt(1)));
            }
            return String.valueOf(Integer.valueOf(exp1.getValue()) >= Integer.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            if (exp1.getType().equals("Char")) {
                return String.valueOf((int)((exp1).getValue().charAt(1)) < ((int)(exp2).getValue().charAt(1)));
            }
            return String.valueOf(Integer.valueOf(exp1.getValue()) < Integer.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            if (exp1.getType().equals("Char")) {
                return String.valueOf((int)((exp1).getValue().charAt(1)) <= ((int)(exp2).getValue().charAt(1)));
            }
            return String.valueOf(Integer.valueOf(exp1.getValue()) <= Integer.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            if (exp1 instanceof Str_literNode || exp2 instanceof Str_literNode) {
                return "false";
            } else if (exp1.getType().equals("String")) {
                return String.valueOf(((IdentNode) exp1).getTypeNode().getIdent().equals(((IdentNode) exp2).getTypeNode().getIdent()));
            }
            return String.valueOf(exp1.getValue().equals(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            if (exp1 instanceof Str_literNode || exp2 instanceof Str_literNode) {
                return "true";
            } else if (exp1.getType().equals("String")) {
                return String.valueOf(!((IdentNode) exp1).getTypeNode().getIdent().equals(((IdentNode) exp2).getTypeNode().getIdent()));
            }
            return String.valueOf(!exp1.getValue().equals(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            return String.valueOf(Boolean.valueOf(exp1.getValue()) && Boolean.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
            return String.valueOf(Boolean.valueOf(exp1.getValue()) || Boolean.valueOf(exp2.getValue()));
        }

        @Override
        public void generate(AssemblyBuilder builder) {

        }
    }

    public abstract class ExprNode extends StatNode {
        @Override
        public void check() {
        }

        protected int getStackOffset() {
            ASTNode parent = getParent();
            int stackOffset = 0;
            if (parent instanceof DeclarationNode) {
                stackOffset = stack.getStackElemOffset(((DeclarationNode) parent).identNode.getIdent());
            } else if (parent instanceof AssignmentNode) {
                ASTNode assignLHS = ((AssignmentNode) parent).assign_lhsNode;
                String ident;
                if (assignLHS instanceof IdentNode) {
                    ident = ((IdentNode) assignLHS).getIdent();
                } else if (assignLHS instanceof Array_elemNode) {
                    ident = ((Array_elemNode) assignLHS).identNode.getIdent();
                } else {
                    ident = ((Pair_elemNode) assignLHS).getIdentNode().getIdent();
                }
                stackOffset = stack.getStackElemOffset(ident);
            }
            return stackOffset;
        }

        public abstract String getValue();

        protected String getStackPointer() {
            int stackOffset = getStackOffset();
            String result;
            if (stackOffset == 0) {
                result = ", [sp";
            } else {
                result = ", [sp, #" + stackOffset;
            }
            return result + "]";
        }
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

        public void setIdent(String ident) {
            this.ident = ident;
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
            return getTypeNode().getValue();
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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
        public void generate(AssemblyBuilder builder) {

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
        public void generate(AssemblyBuilder builder) {

            int num = Integer.parseInt(value);
            if (sign.equals("-")) {
                num *= -1;
            }
            Registers.Register firstEmptyRegister = registers.getFirstEmptyRegister();
            builder.getCurrent().append("LDR " + firstEmptyRegister + ", =" + num + "\n");
            builder.getCurrent().append("STR " + firstEmptyRegister + getStackPointer() + "\n");
        }

        public int getvalue() {
            if (sign.equals("-")) {
                return Integer.parseInt(value) / (-1);
            }
            return Integer.parseInt(value);
        }

        @Override
        public String getValue() {
            return String.valueOf(getvalue());
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
        public void generate(AssemblyBuilder builder) {

            Registers.Register firstEmptyRegister = registers.getFirstEmptyRegister();
            if (value) {
                builder.getCurrent().append("MOV " + firstEmptyRegister + ", #1\n");
            } else {
                builder.getCurrent().append("MOV " + firstEmptyRegister + ", #0\n");
            }
            builder.getCurrent().append("STRB " + firstEmptyRegister + getStackPointer() + "\n");
        }
    }

    public class Char_literNode extends ExprNode {

        private String value;
        private char c;

        public Char_literNode(String v) {
            c = v.charAt(1);
            if (v.charAt(1) == '\\') {
                switch (v.charAt(2)) {
                    case '0':
                        value = "0";
                        break;
                    case 'b':
                        value = "8";
                        break;
                    case 't':
                        value = "9";
                        break;
                    case 'n':
                        value = "10";
                        break;
                    case 'f':
                        value = "12";
                        break;
                    case 'r':
                        value = "13";
                        break;
                    case '"':
                        value = "\'\"\'";
                        break;
                    case '\'':
                        value = "\'\\\'\'";
                        break;
                    case '\\':
                        value = "\'\\\'";
                        break;
                }

            } else {
                this.value = "\'" + v.charAt(1) + "\'";
            }
        }

        public char getChar() {
            return c;
        }

        @Override
        public String getType() {

            return "Char";
        }

        @Override
        public void generate(AssemblyBuilder builder) {
            Registers.Register firstEmptyRegister = registers.getFirstEmptyRegister();
            builder.getCurrent().append("MOV " + firstEmptyRegister + ", #" + value + "\n");
            builder.getCurrent().append("STRB " + firstEmptyRegister + getStackPointer() + "\n");
        }

        @Override
        public String getValue() {
            return value;
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
        public void generate(AssemblyBuilder builder) {

            Registers.Register firstEmptyRegister = registers.getFirstEmptyRegister();
            builder.getCurrent().append("LDR " + firstEmptyRegister + ", =msg_" + messageCount + "\n");
            builder.getCurrent().append("STR " + firstEmptyRegister + getStackPointer() + "\n");
            builder.getHeader().append("msg_" + messageCount + ": \n");
            builder.getHeader().append(".word " + getWordLength(value) + "\n");
            builder.getHeader().append(".ascii\t" + value + "\n");
            messageCount++;
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

        @Override
        public String getValue() {
            return null;
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
        public void generate(AssemblyBuilder builder) {

        }
    }

    public class Pair_literNode extends ExprNode {

        @Override
        public String getType() {

            return "Null";
        }

        @Override
        public void generate(AssemblyBuilder builder) {

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

        @Override
        public String getValue() {
            return null;
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
        public void generate(AssemblyBuilder builder) {

        }
    }

}
