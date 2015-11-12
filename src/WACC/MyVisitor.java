package WACC;
import java.util.*;
import antlr.*;

public class MyVisitor extends BasicParserBaseVisitor<Void>{

    public Void visitProgram(BasicParser.ProgramContext ctx){
        System.out.println("Good morning, who's awake!?");
        System.out.println(ctx.func().size());
        
        return visitChildren(ctx);
    }

    public Void visitFunc(BasicParser.FuncContext ctx){
        System.out.println("I found a funciton definition!");
        System.out.println(ctx.ident());
        System.out.print("Type info: ");
        //need to visit function args in a loop
        for (int i = 0; i < ctx.param_list().depth(); i++){
            visit(ctx.param_list().param(i));
        }
        System.out.print(" => ");
        //vist funtion return type (note this is out of normal tree order)
        visitChildren(ctx.type());
        System.out.println("");
        return visitChildren(ctx);
    }


    public Void visitBaseType(BasicParser.Base_typeContext ctx){
        System.out.print(1);
        return null;
    }
}
