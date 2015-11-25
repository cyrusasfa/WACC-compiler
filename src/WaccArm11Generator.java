import antlr.WaccParserBaseVisitor;
import instructions.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import util.Arm11Program;
import util.Register;
import util.Registers;
import util.SymbolTable;

import java.util.Comparator;
import java.util.PriorityQueue;

import static antlr.WaccParser.*;

public class WaccArm11Generator extends WaccParserBaseVisitor<Register> {

    private Arm11Program state = new Arm11Program();
    private Registers registers = new Registers();
    private SymbolTable st;

    public String generate() {
        return state.toCode();
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.st = symbolTable;
    }

    /*
     * Adds each child of [tree] to a priority queue, with priority given by the childs weight
     * Then visits each of these children starting at the one which uses the most registers for optimal register usage
     */
    @Override
    public Register visitChildren(RuleNode tree) {
        PriorityQueue<ParseTree> children = new PriorityQueue<ParseTree>(1, new Comparator<ParseTree>() {
            @Override
            public int compare(ParseTree p1, ParseTree p2) {
                return compareWeights(p1, p2);
            }
        });

        for(int i = 0; i < tree.getChildCount(); i++) {
            children.add(tree.getChild(i));
        }

        Register result = null;
        for(ParseTree child : children) {
            result = visit(child);
        }

        return result;
    }

    /*
     * Compares the amount of registers that two parsetrees will use in code generation
     * Returns negative if p1 uses more than p2, else positive
     */
    public int compareWeights(ParseTree p1, ParseTree p2) {
        int w1 = weight(p1);
        int w2 = weight(p2);
        return w1 - w2;
    }

    /*
     * TODO
     * Calculates how many registers [tree] will use in code generation
     */
    public int weight(ParseTree tree) {
        return 0;
    }

    ////////////// VISITOR METHODS /////////////

    @Override
    public Register visitProg(ProgContext ctx) {
        state.add(new TextDirective());
        state.add(new GlobalDirective("main"));
        state.startFunction("main");
        visitChildren(ctx);
        state.endFunction();
        return null;
    }

    @Override
    public Register visitFunc(FuncContext ctx) {
        String ident = ctx.ident().getText();
        state.startFunction(ident);
        //TODO: param list
        visit(ctx.stat());
        state.endFunction();
        return null;
    }

    @Override
    public Register visitExitStat(ExitStatContext ctx) {
        Register result = visit(ctx.expr());

        state.add(new MoveInstruction(registers.getReturnRegister(), result));
        state.add(new BranchLinkInstruction("exit"));

        registers.freeReturnRegisters();

        state.add(new LoadInstruction(registers.getReturnRegister(), 0));
        return null;
    }

    @Override
    public Register visitExpr(ExprContext ctx) {
        if(ctx.INT_LIT() != null) {
            int i = Integer.parseInt(ctx.INT_LIT().getSymbol().getText());
            Register nextRegister = registers.getRegister();
            state.add(new LoadInstruction(nextRegister, i));
            return nextRegister;
        }
        return null;
    }

    @Override
    public Register visitIntSign(IntSignContext ctx) {
        return super.visitIntSign(ctx);
    }

    @Override
    public Register visitAssignRhs(AssignRhsContext ctx) {
        return super.visitAssignRhs(ctx);
    }

    @Override
    public Register visitArgList(ArgListContext ctx) {
        return super.visitArgList(ctx);
    }

    @Override
    public Register visitParam(ParamContext ctx) {
        return super.visitParam(ctx);
    }

    @Override
    public Register visitVarAssignment(VarAssignmentContext ctx) {
        return super.visitVarAssignment(ctx);
    }

    @Override
    public Register visitParamList(ParamListContext ctx) {
        return super.visitParamList(ctx);
    }

    @Override
    public Register visitType(TypeContext ctx) {
        return super.visitType(ctx);
    }

    @Override
    public Register visitOtherBinaryOper(OtherBinaryOperContext ctx) {
        return super.visitOtherBinaryOper(ctx);
    }

    @Override
    public Register visitCharacter(CharacterContext ctx) {
        return super.visitCharacter(ctx);
    }

    @Override
    public Register visitNewPair(NewPairContext ctx) {
        return super.visitNewPair(ctx);
    }

    @Override
    public Register visitBoolBinaryOper(BoolBinaryOperContext ctx) {
        return super.visitBoolBinaryOper(ctx);
    }

    @Override
    public Register visitIdent(IdentContext ctx) {
        return super.visitIdent(ctx);
    }

    @Override
    public Register visitBaseType(BaseTypeContext ctx) {
        return super.visitBaseType(ctx);
    }

    @Override
    public Register visitScopeStat(ScopeStatContext ctx) {
        return super.visitScopeStat(ctx);
    }

    @Override
    public Register visitPairLiter(PairLiterContext ctx) {
        return super.visitPairLiter(ctx);
    }

    @Override
    public Register visitReadStat(ReadStatContext ctx) {
        return super.visitReadStat(ctx);
    }

    @Override
    public Register visitPairElemType(PairElemTypeContext ctx) {
        return super.visitPairElemType(ctx);
    }

    @Override
    public Register visitVarDeclaration(VarDeclarationContext ctx) {
        return super.visitVarDeclaration(ctx);
    }

    @Override
    public Register visitReturnStat(ReturnStatContext ctx) {
        return super.visitReturnStat(ctx);
    }

    @Override
    public Register visitPrintStat(PrintStatContext ctx) {
        return super.visitPrintStat(ctx);
    }

    @Override
    public Register visitPairElem(PairElemContext ctx) {
        return super.visitPairElem(ctx);
    }

    @Override
    public Register visitArrayElem(ArrayElemContext ctx) {
        return super.visitArrayElem(ctx);
    }

    @Override
    public Register visitEscapedChar(EscapedCharContext ctx) {
        return super.visitEscapedChar(ctx);
    }

    @Override
    public Register visitStat(StatContext ctx) {
        return super.visitStat(ctx);
    }

    @Override
    public Register visitFreeStat(FreeStatContext ctx) {
        return super.visitFreeStat(ctx);
    }

    @Override
    public Register visitWhileStat(WhileStatContext ctx) {
        return super.visitWhileStat(ctx);
    }

    @Override
    public Register visitUnaryOper(UnaryOperContext ctx) {
        return super.visitUnaryOper(ctx);
    }

    @Override
    public Register visitIfStat(IfStatContext ctx) {
        return super.visitIfStat(ctx);
    }

    @Override
    public Register visitPairType(PairTypeContext ctx) {
        return super.visitPairType(ctx);
    }

    @Override
    public Register visitArrayLiter(ArrayLiterContext ctx) {
        return super.visitArrayLiter(ctx);
    }

    @Override
    public Register visitAssignLhs(AssignLhsContext ctx) {
        return super.visitAssignLhs(ctx);
    }

    @Override
    public Register visitComment(CommentContext ctx) {
        return super.visitComment(ctx);
    }

    @Override
    public Register visitFuncCall(FuncCallContext ctx) {
        return super.visitFuncCall(ctx);
    }

    @Override
    public Register visitPrintlnStat(PrintlnStatContext ctx) {
        return super.visitPrintlnStat(ctx);
    }
}
