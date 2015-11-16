package WACC;

import java.util.*;

import antlr.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

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
            AST.FuncNode funcNode = (AST.FuncNode) visit(functionContext);
            functionNodes.add(funcNode);
        }

        AST.StatNode sub_statNode = (AST.StatNode) visit(ctx.sub_stat());

        programNode = ast.new ProgramNode(functionNodes, sub_statNode);
        return programNode;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitFunc(@NotNull BasicParser.FuncContext ctx) {

        List<AST.ParamNode> paramNodeList = new ArrayList<>();
        List<ParseTree> paramContextList = new ArrayList<>();
        AST.StatNode stateNode = null;
        if (ctx.param_list() != null) {
            for (int i = 0; i < ctx.param_list().getChildCount(); i = i + 2) {
                paramContextList.add(ctx.param_list().getChild(i));
            }

        }

        for (ParseTree param : paramContextList) {
            paramNodeList.add((AST.ParamNode) visit(param));
        }
        if (ctx.if_sub_stat() != null) {
            stateNode = (AST.StatNode)visit(ctx.if_sub_stat());
        }else if (ctx.stat() != null) {
            stateNode = (AST.StatNode) visit(ctx.stat());
        }

        AST.FuncNode funcNode = ast.new FuncNode((AST.TypeNode) visit(ctx.type()), (AST.IdentNode) visit(ctx.ident()), paramNodeList, stateNode);

        for (int i = 0; i < paramContextList.size(); i++) {
            BasicParser.ParamContext paramContext = (BasicParser.ParamContext) paramContextList.get(i);
            funcNode.getSymbolTable().put(paramContext.ident().getText(), paramNodeList.get(i).getTypeNode());
        }



//        if (funcNode.getParent().getSymbolTable().containsKey(ctx.ident().getText())) {
//            System.out.println("error");
//        } else {
//            funcNode.getParent().getSymbolTable().put(ctx.ident().getText(), funcNode);
//        }

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

        return ast.new IdentNode(ctx.getText());
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

        return ast.new Pair_literNode();
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
        AST.ParamNode paramNode = null;

        AST.TypeNode typeNode = (AST.TypeNode) visit(ctx.type());

        AST.IdentNode identNode = (AST.IdentNode) visit(ctx.ident());

        paramNode = ast.new ParamNode(typeNode, identNode);

        return paramNode;
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
        if (ctx.OPEN_PARENTHESES() != null) {
            return visit(ctx.expr(0));

        } else if (ctx.binary_oper() != null) {
            switch (ctx.binary_oper().getText()) {
                case "*":
                    return ast.new MultNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case "/":
                    return ast.new DivNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case "%":
                    return ast.new ModNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case "+":
                    return ast.new PlusNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case "-":
                    return ast.new MinusNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case ">":
                    return ast.new GreaterNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case ">=":
                    return ast.new GreaterOrEqualNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case "<":
                    return ast.new SmallerNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case "<=":
                    return ast.new SmallerOrEqualNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case "==":
                    return ast.new EqualNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case "!=":
                    return ast.new NotEqualNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case "&&":
                    return ast.new LogicalAndNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
                case "||":
                    return ast.new LogicalOrNode(visit(ctx.expr(0)), visit(ctx.expr(1)));
            }

        } else if (ctx.unary_oper() != null) {
            switch (ctx.unary_oper().getText()) {
                case "!":
                    return ast.new NotOperNode((AST.ExprNode) visit(ctx.expr(0)));
                case "-":
                    return ast.new NegateOperNode((AST.ExprNode) visit(ctx.expr(0)));
                case "len":
                    return ast.new LenOperNode((AST.ExprNode) visit(ctx.expr(0)));
                case "ord":
                    return ast.new OrdOperNode((AST.ExprNode) visit(ctx.expr(0)));
                case "chr":
                    return ast.new CharOperNode((AST.ExprNode) visit(ctx.expr(0)));
            }

        } else if (ctx.array_elem() != null) {
            return visit(ctx.array_elem());

        } else if (ctx.ident() != null) {
            return visit(ctx.ident());

        } else if (ctx.int_liter() != null) {
            return visit(ctx.int_liter());

        } else if (ctx.bool_liter() != null) {
            return visit(ctx.bool_liter());

        } else if (ctx.char_liter() != null) {
            return visit(ctx.char_liter());

        } else if (ctx.str_liter() != null) {
            return visit(ctx.str_liter());

        } else if (ctx.pair_liter() != null) {
            return visit(ctx.pair_liter());

        }
        System.out.println("Error");
        return null;
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
        if (ctx.base_type() != null) {
            visit(ctx.base_type());
        } else if (ctx.OPEN_SQUARE_BRACKET() != null) {
            return ast.new Array_typeNode((AST.TypeNode) visit(ctx.type()));
        } else if (ctx.pair_type() != null) {
            return visit(ctx.pair_type());
        }
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
        //TODO
        System.out.println("Not Implemented");
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
        if (ctx.FST() != null) {
            return ast.new FSTNode((AST.ExprNode) visit(ctx.expr()));
        } else {
            return ast.new SNDNode((AST.ExprNode) visit(ctx.expr()));
        }
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

        return ast.new Array_typeNode((AST.TypeNode) visit(ctx.type()));
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
        if (ctx.BOOL() != null) {
            return ast.new BoolTypeNode();
        } else if (ctx.CHAR() != null) {
            return ast.new CharTypeNode();
        } else if (ctx.INT() != null) {
            return ast.new IntTypeNode();
        } else {
            return ast.new StringTypeNode();
        }
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

        return ast.new Pair_typeNode((AST.TypeNode) visit(ctx.pair_elem_type(0)),
                (AST.TypeNode) visit(ctx.pair_elem_type(1)));
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

        return ast.new Str_literNode(ctx.STR_LITER().getText());
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
        //TODO
        System.out.println("Not Implemented");
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
        if (ctx.array_elem() != null) {
            return visit(ctx.array_elem());
        } else if (ctx.ident() != null) {
            return visit(ctx.ident());
        } else if (ctx.pair_elem() != null) {
            return visit(ctx.pair_elem());
        }
        System.out.println("Error");
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
        if (ctx.CALL() != null) {
            List<AST.ExprNode> argNodeList = new ArrayList<>();
            if (ctx.arg_list() != null) {
                for (int i = 0; i < (ctx.arg_list().getChildCount() + 1) / 2; i++) {
                    argNodeList.add((AST.ExprNode) visit(ctx.arg_list().expr(i)));
                }
            }
            return ast.new CallNode((AST.IdentNode) visit(ctx.ident()), argNodeList);

        } else if (ctx.NEWPAIR() != null) {
            return ast.new NewPairNode((AST.ExprNode) visit(ctx.expr(0)), (AST.ExprNode) visit(ctx.expr(1)));

        } else if (ctx.pair_elem() != null) {
            return visit(ctx.pair_elem());

        } else if (ctx.array_liter() != null) {
            List<AST.ASTNode> exprNodeList = new ArrayList<>();
            for (int i = 0; i < ctx.array_liter().expr().size(); i++) {
                exprNodeList.add(visit(ctx.array_liter().expr(i)));
            }
            return ast.new Array_literNode(exprNodeList);
        } else if (ctx.expr(0) != null) {
            return visit(ctx.expr(0));
        }
        System.out.println("Error");
        return visitChildren(ctx);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public AST.ASTNode visitStat(@NotNull BasicParser.StatContext ctx) {

        AST.ASTNode returnNode = null;
        AST.StatNode sub_statNode = null;

        if (ctx.sub_stat() != null) {
            sub_statNode = (AST.StatNode)visit(ctx.sub_stat());
        }
        if (ctx.RETURN() != null) {
            returnNode = visit(ctx.RETURN());
        }else if (ctx.EXIT() != null) {
            returnNode = visit(ctx.EXIT());
        }

        return ast.new StatNode(sub_statNode, returnNode);
    }
    /**
     * {@inheritDoc}
     *
     * @author YinJun
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override
    public AST.ASTNode visitSub_stat(@NotNull BasicParser.Sub_statContext ctx) {
        //TODO symbol table config
        if (ctx.SEMICOLON() != null) {
            return ast.new MultipleStatNode((AST.StatNode) visit(ctx.sub_stat(0)), (AST.StatNode) visit(ctx.sub_stat(1)));
        } else if (ctx.BEGIN() != null) {
            return visit(ctx.sub_stat(0));
        } else if (ctx.WHILE() != null) {
            return ast.new WhileNode((AST.ExprNode) visit(ctx.expr()), (AST.StatNode) visit(ctx.sub_stat(0)));
        } else if (ctx.IF() != null) {
            return ast.new IfNode((AST.ExprNode) visit(ctx.expr()), (AST.StatNode) visit(ctx.if_sub_stat(0)), (AST.StatNode) visit(ctx.if_sub_stat(1)));
        } else if (ctx.PRINTLN() != null) {
            return ast.new PrintlnNode((AST.ExprNode) visit(ctx.expr()));
        } else if (ctx.PRINT() != null) {
            return ast.new PrintNode((AST.ExprNode) visit(ctx.expr()));
        } else if (ctx.EXIT() != null) {
            return ast.new ExitNode((AST.ExprNode)visit(ctx.expr()));
        } else if (ctx.FREE() != null) {
            return ast.new FreeNode((AST.ExprNode) visit(ctx.expr()));
        } else if (ctx.READ() != null) {
            return ast.new ReadNode(visit(ctx.assign_lhs()));
        } else if (ctx.SKIP() != null) {
            return ast.new SkipNode();
        } else if (ctx.assign_lhs() != null) {
            return ast.new AssignmentNode(visit(ctx.assign_lhs()), visit(ctx.assign_rhs()));
        } else if (ctx.type() != null) {
            return ast.new DeclarationNode((AST.TypeNode) visit(ctx.type()), (AST.IdentNode) visit(ctx.ident()), visit(ctx.assign_rhs()));
        }
        System.out.println("Error");
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public AST.ASTNode visitIf_sub_stat (@NotNull BasicParser.If_sub_statContext ctx) {

        if (ctx.SEMICOLON() != null) {
            return ast.new MultipleStatNode((AST.StatNode) visit(ctx.if_sub_stat(0)), (AST.StatNode) visit(ctx.if_sub_stat(1)));
        } else if (ctx.BEGIN() != null) {
            return visit(ctx.if_sub_stat(0));
        } else if (ctx.WHILE() != null) {
            return ast.new WhileNode((AST.ExprNode) visit(ctx.expr()), (AST.StatNode) visit(ctx.if_sub_stat(0)));
        } else if (ctx.IF() != null) {
            return ast.new IfNode((AST.ExprNode) visit(ctx.expr()), (AST.StatNode) visit(ctx.if_sub_stat(0)), (AST.StatNode) visit(ctx.if_sub_stat(1)));
        } else if (ctx.PRINTLN() != null) {
            return ast.new PrintlnNode((AST.ExprNode) visit(ctx.expr()));
        } else if (ctx.PRINT() != null) {
            return ast.new PrintNode((AST.ExprNode) visit(ctx.expr()));
        } else if (ctx.EXIT() != null) {
            return ast.new ExitNode((AST.ExprNode) visit(ctx.expr()));
        } else if (ctx.RETURN() != null) {
            return ast.new ReturnNode((AST.ExprNode) visit(ctx.expr()));
        } else if (ctx.FREE() != null) {
            return ast.new FreeNode((AST.ExprNode) visit(ctx.expr()));
        } else if (ctx.READ() != null) {
            return ast.new ReadNode(visit(ctx.assign_lhs()));
        } else if (ctx.SKIP() != null) {
            return ast.new SkipNode();
        } else if (ctx.assign_lhs() != null) {
            return ast.new AssignmentNode(visit(ctx.assign_lhs()), visit(ctx.assign_rhs()));
        } else if (ctx.type() != null) {
            return ast.new DeclarationNode((AST.TypeNode) visit(ctx.type()), (AST.IdentNode) visit(ctx.ident()), visit(ctx.assign_rhs()));
        }
        System.out.println("Error");
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
    public AST.ASTNode visitBool_liter(@NotNull BasicParser.Bool_literContext ctx) {

        return ast.new Bool_literNode(ctx.getText());
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
        //TODO
        System.out.println("Not Implemented");
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
        //TODO
        System.out.println("Error");
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

        //TODO
        System.out.println("Not Implemented");
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

        List<AST.ExprNode> exprNodeList = new ArrayList<>();
        for (BasicParser.ExprContext exprContext : ctx.expr()) {
            exprNodeList.add((AST.ExprNode) visit(exprContext));
        }
        return ast.new Array_elemNode((AST.IdentNode) visit(ctx.ident()), exprNodeList);
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

        //TODO
        System.out.println("Not Implemented");
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
        if (ctx.base_type() != null) {
            return visit(ctx.base_type());
        } else if (ctx.array_type() != null) {
            return visit(ctx.array_type());
        } else if (ctx.PAIR() != null) {
            return ast.new PairNode();
        }
        System.out.println("Error");
        return null;
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

        return ast.new Char_literNode(ctx.CHAR_LITER().getText());
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

        //TODO
        System.out.println("Not Implemented");
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

        String sign = "+";
        String number = "";
        if (ctx.int_sign() != null) {
            sign = ctx.int_sign().getText();
        }
        if (ctx.INTEGER() != null) {
            number = ctx.INTEGER().getText();
        }
        return ast.new Int_literNode(sign, number);
    }
}
