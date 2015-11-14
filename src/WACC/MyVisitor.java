package WACC;

import java.util.*;

import antlr.*;
import org.antlr.v4.runtime.misc.NotNull;

public class MyVisitor extends BasicParserBaseVisitor<AST.ASTNode> {

    AST ast = new AST();

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitProgram(@NotNull BasicParser.ProgramContext ctx) {
        AST.ProgramNode programNode = null;
        List<AST.FuncNode> functionNodes = new ArrayList<>();
        for (BasicParser.FuncContext functionContext : ctx.func()) {
            AST.FuncNode funcNode = (AST.FuncNode)visit(functionContext);
            funcNode.setParent((AST.ASTNode)programNode);
            functionNodes.add(funcNode);
        }

        AST.StatNode statNode = (AST.StatNode) visit(ctx.stat());
        statNode.setParent(programNode);

        programNode = ast.new ProgramNode(functionNodes, statNode);
        return programNode;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.FuncNode visitFunc(@NotNull BasicParser.FuncContext ctx) {

        AST.FuncNode funcNode = null;

        AST.TypeNode typeNode = (AST.TypeNode) visit(ctx.type());
        typeNode.setParent(funcNode);

        AST.IdentNode identNode = (AST.IdentNode) visit(ctx.ident());
        identNode.setParent(funcNode);

        List<AST.ParamNode> paramNodeList = null;
        if (ctx.param_list() != null ) {
            for (int i = 0; i < ctx.param_list().getChildCount(); i++) {
                AST.ParamNode paramNode = (AST.ParamNode)visit(ctx.param_list().getChild(i));
                paramNode.setParent(funcNode);
                paramNodeList.add(paramNode);
            }
        }

        AST.StatNode statNode = (AST.StatNode) visit(ctx.stat());
        statNode.setParent(funcNode);

        funcNode = ast.new FuncNode(typeNode, identNode, paramNodeList, statNode);

        for (int i = 0; i < ctx.param_list().getChildCount(); i++) {
            BasicParser.ParamContext paramContext = (BasicParser.ParamContext) ctx.param_list().getChild(i);
            funcNode.getSymbolTable().put(paramContext.ident().getText(), paramNodeList.get(i).getTypeNode());
        }
        if (funcNode.getParent().getSymbolTable().containsKey(ctx.ident().getText())) {
            System.out.println("error");
        }else {
            funcNode.getParent().getSymbolTable().put(ctx.ident().getText(), funcNode);
        }

        return funcNode;
    }
    /**
     * {@inheritDoc}
     *
     * @author Davies
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitIdent(@NotNull BasicParser.IdentContext ctx) {

        AST.IdentNode identNode = ast.new IdentNode(ctx.getText());

        return identNode;
    }
    /**
     * {@inheritDoc}
     *
     * @author Davies
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitPair_liter(@NotNull BasicParser.Pair_literContext ctx) {
        System.out.println("I found a Pair_liter");
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author Davies
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitParam(@NotNull BasicParser.ParamContext ctx) {
        System.out.println("found param");
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author Davies
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitExpr(@NotNull BasicParser.ExprContext ctx) {
        System.out.println("I found an Expr");
        System.out.println(ctx.depth());
        //System.out.println(ctx.getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getText());
//        if (ctx.getChildCount() == 3) {
//            if (!ctx.getChild(0).getText().equals('(')) {
//                if (ctx.getChild(0).equals(ctx.getChild(2))) {
//                    System.out.println("same type");
//                    System.out.println(ctx.getText());
//                }else{
//                    System.out.println("different type");
//                    System.out.println(ctx.getText());
//                }
//            }
//        }
//        System.out.println(ctx.getChildCount());
//        System.out.println(ctx.getChild(0).getText());
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author Davies
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitType(@NotNull BasicParser.TypeContext ctx) {
        System.out.println("found type");
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author Davies
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitUnary_oper(@NotNull BasicParser.Unary_operContext ctx) {
        return visitChildren(ctx);
    }



    /**
     * {@inheritDoc}
     *
     * @author Davies
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitPair_elem(@NotNull BasicParser.Pair_elemContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author Davies
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitArray_type(@NotNull BasicParser.Array_typeContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author YinJun
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitBase_type(@NotNull BasicParser.Base_typeContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author YinJun
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitPair_type(@NotNull BasicParser.Pair_typeContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author YinJun
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitStr_liter(@NotNull BasicParser.Str_literContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author YinJun
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitInt_sign(@NotNull BasicParser.Int_signContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author YinJun
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitAssign_lhs(@NotNull BasicParser.Assign_lhsContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author YinJun
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.StatNode visitStat(@NotNull BasicParser.StatContext ctx) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @author YinJun
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitBool_liter(@NotNull BasicParser.Bool_literContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author YinJun
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitParam_list(@NotNull BasicParser.Param_listContext ctx) {
        System.out.println("Found param list");
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author WangJiaYing & Jimmy
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitDigit(@NotNull BasicParser.DigitContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author WangJiaYing & Jimmy
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitArg_list(@NotNull BasicParser.Arg_listContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author WangJiaYing & Jimmy
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitArray_elem(@NotNull BasicParser.Array_elemContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author WangJiaYing & Jimmy
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitBinary_oper(@NotNull BasicParser.Binary_operContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author WangJiaYing & Jimmy
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitPair_elem_type(@NotNull BasicParser.Pair_elem_typeContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author WangJiaYing & Jimmy
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitChar_liter(@NotNull BasicParser.Char_literContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author WangJiaYing & Jimmy
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitArray_liter(@NotNull BasicParser.Array_literContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author WangJiaYing & Jimmy
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitAssign_rhs(@NotNull BasicParser.Assign_rhsContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @author WangJiaYing & Jimmy
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitInt_liter(@NotNull BasicParser.Int_literContext ctx) {
        return visitChildren(ctx);
    }
}
