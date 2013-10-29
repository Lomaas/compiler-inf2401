package no.uio.ifi.cflat.syntax;

/*
 * module Syntax
 */

import no.uio.ifi.cflat.cflat.Cflat;
import no.uio.ifi.cflat.code.Code;
import no.uio.ifi.cflat.error.Error;
import no.uio.ifi.cflat.log.Log;
import no.uio.ifi.cflat.scanner.Scanner;
import no.uio.ifi.cflat.scanner.Token;
import static no.uio.ifi.cflat.scanner.Token.*;
import no.uio.ifi.cflat.types.*;

/*
 * Creates a syntax tree by parsing; 
 * prints the parse tree (if requested);
 * checks it;
 * generates executable code. 
 */
public class Syntax {
    static DeclList library;
    static Program program;

    public static void init() {
        //-- Must be changed in part 1:
    }

    public static void finish() {
        //-- Must be changed in part 1:
    }

    public static void checkProgram() {
        program.check(library);
    }

    public static void genCode() {
        program.genCode(null);
    }

    public static void parseProgram() {
        program = Program.parse();
    }

    public static void printProgram() {
        program.printTree();
    }

    static void error(SyntaxUnit use, String message) {
        Error.error(use.lineNum, message);
    }
}


/*
 * Master class for all syntactic units.
 * (This class is not mentioned in the syntax diagrams.)
 */
abstract class SyntaxUnit {
    int lineNum;

    SyntaxUnit() {
        lineNum = Scanner.curLine;
    }

    /* The virtual methods: */
    abstract void check(DeclList curDecls);
    abstract void genCode(FuncDecl curFunc);
    abstract void printTree();
}


/*
 * A <program>
 */
class Program extends SyntaxUnit {
    DeclList progDecls;

    @Override void check(DeclList curDecls) {
        progDecls.check(curDecls);

        if (! Cflat.noLink) {
            // Check that 'main' has been declared properly:
            //-- Must be changed in part 2:
        }
    }

    @Override void genCode(FuncDecl curFunc) {
        progDecls.genCode(null);
    }

    static Program parse() {
        Log.enterParser("<program>");

        Program p = new Program();
        p.progDecls = GlobalDeclList.parse();
        if (Scanner.curToken != eofToken)
            Error.expected("A declaration");

        Log.leaveParser("</program>");
        return p;
    }

    @Override void printTree() {
        progDecls.printTree();
    }
}


/*
 * A declaration list.
 * (This class is not mentioned in the syntax diagrams.)
 */

abstract class DeclList extends SyntaxUnit {
    Declaration firstDecl = null;
    DeclList outerScope;

    DeclList () {
        //-- Must be changed in part 1:
    }

    @Override void check(DeclList curDecls) {
        outerScope = curDecls;

        Declaration dx = firstDecl;
        while (dx != null) {
            dx.check(this);  dx = dx.nextDecl;
        }
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }

    void addDecl(Declaration d) {
        //-- Must be changed in part 1:
//        if(firstDecl == null){
//            firstDecl = d;
//        }
//        outerScope.addDecl(d);  // added
    }

    int dataSize() {
        int res = 0;
        //-- Must be changed in part 2:
        return res;
    }

    Declaration findDecl(String name, SyntaxUnit usedIn) {
        //-- Must be changed in part 2:
        return null;
    }
}


/*
 * A list of global declarations. 
 * (This class is not mentioned in the syntax diagrams.)
 */
class GlobalDeclList extends DeclList {
    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static GlobalDeclList parse() {
        GlobalDeclList gdl = new GlobalDeclList();
        System.out.println("globaldeclist");
        Scanner.readNext();
        Scanner.readNext();
        Scanner.readNext();

        while (Token.isTypeName(Scanner.curToken)) {
            System.out.println("Out in main loop");
            if (Scanner.nextToken == nameToken) {
                System.out.println("NameToken " + Scanner.nextName);

                if (Scanner.nextNextToken == leftParToken) {
                    System.out.println("<Start> Function declaration");
                    gdl.addDecl(FuncDecl.parse());
                    System.out.println("<End> Function declaration");
                } else if (Scanner.nextNextToken == leftBracketToken) {
                    System.out.println("Global array Decl");

                    gdl.addDecl(GlobalArrayDecl.parse());
                } else {
                    System.out.println("<Start> Global var decl");
                    gdl.addDecl(GlobalSimpleVarDecl.parse());
                    System.out.println("<End> Global var decl");
                }
            } else {
                Error.expected("A declaration");
            }
        }
        System.out.println("end of GlobalDecList parse");
        System.out.println(Scanner.curToken);
        return gdl;
    }
}


/*
 * A list of local declarations. 
 * (This class is not mentioned in the syntax diagrams.)
 */
class LocalDeclList extends DeclList {
    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static LocalDeclList parse() {
        System.out.println("Inside parse local dec list");
        System.out.println(Scanner.curToken);
        LocalDeclList ldl = new LocalDeclList();

        while (Token.isTypeName(Scanner.curToken)) {
            if (Scanner.nextToken == nameToken) {
                System.out.println("NameToken " + Scanner.nextName);

                if (Scanner.nextNextToken == leftBracketToken) {
                    System.out.println("Local array Decl");
                    ldl.addDecl(LocalArrayDecl.parse());
                } else {
                    System.out.println("<Start> Local var decl");
                    ldl.addDecl(LocalSimpleVarDecl.parse());
                    System.out.println("<End> Local var decl");
                }
            } else {
                Error.expected("A declaration");
            }
        }
        System.out.println("end of localDecList parse");

        return ldl;
    }
}


/*
 * A list of parameter declarations. 
 * (This class is not mentioned in the syntax diagrams.)
 */
class ParamDeclList extends DeclList {
    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static ParamDeclList parse() {

        //-- Must be changed in part 1:
        ParamDeclList paramDeclList = new ParamDeclList();

        while(Scanner.curToken != rightParToken){
            // Type, name
            ParamDecl paramDecl = ParamDecl.parse();
            paramDeclList.addDecl(paramDecl);

            if(Scanner.curToken == commaToken){
                Scanner.skip(commaToken);
            }
        }

        return paramDeclList;
    }
}


/*
 * Any kind of declaration.
 * (This class is not mentioned in the syntax diagrams.)
 */
abstract class Declaration extends SyntaxUnit {
    String name, assemblerName;
    Type type;
    boolean visible = false;
    Declaration nextDecl = null;

    Declaration(String n) {
        name = n;
    }

    abstract int declSize();

    /**
     * checkWhetherArray: Utility method to check whether this Declaration is
     * really an array. The compiler must check that a name is used properly;
     * for instance, using an array name a in "a()" or in "x=a;" is illegal.
     * This is handled in the following way:
     * <ul>
     * <li> When a name a is found in a setting which implies that should be an
     *      array (i.e., in a construct like "a["), the parser will first 
     *      search for a's declaration d.
     * <li> The parser will call d.checkWhetherArray(this).
     * <li> Every sub-class of Declaration will implement a checkWhetherArray.
     *      If the declaration is indeed an array, checkWhetherArray will do
     *      nothing, but if it is not, the method will give an error message.
     * </ul>
     * Examples
     * <dl>
     *  <dt>GlobalArrayDecl.checkWhetherArray(...)</dt>
     *  <dd>will do nothing, as everything is all right.</dd>
     *  <dt>FuncDecl.checkWhetherArray(...)</dt>
     *  <dd>will give an error message.</dd>
     * </dl>
     */
    abstract void checkWhetherArray(SyntaxUnit use);

    /**
     * checkWhetherFunction: Utility method to check whether this Declaration
     * is really a function.
     *
     * @param nParamsUsed Number of parameters used in the actual call.
     *                    (The method will give an error message if the
     *                    function was used with too many or too few parameters.)
     * @param use From where is the check performed?
     * @see   checkWhetherArray
     */
    abstract void checkWhetherFunction(int nParamsUsed, SyntaxUnit use);

    /**
     * checkWhetherSimpleVar: Utility method to check whether this
     * Declaration is really a simple variable.
     *
     * @see   checkWhetherArray
     */
    abstract void checkWhetherSimpleVar(SyntaxUnit use);
}


/*
 * A <var decl>
 */
abstract class VarDecl extends Declaration {
    VarDecl(String n) {
        super(n);
    }

    @Override int declSize() {
        return type.size();
    }

    @Override void checkWhetherFunction(int nParamsUsed, SyntaxUnit use) {
        Syntax.error(use, name + " is a variable and no function!");
    }

    @Override void printTree() {
        Log.wTree(type.typeName() + " " + name);
        Log.wTreeLn(";");
    }

    //-- Must be changed in part 1+2:
}


/*
 * A global array declaration
 */
class GlobalArrayDecl extends VarDecl {
    GlobalArrayDecl(String n) {
        super(n);
        assemblerName = (Cflat.underscoredGlobals() ? "_" : "") + n;
    }

    @Override void check(DeclList curDecls) {
        visible = true;
        if (((ArrayType)type).nElems < 0)
            Syntax.error(this, "Arrays cannot have negative size!");
    }

    @Override void checkWhetherArray(SyntaxUnit use) {
	/* OK */
    }

    @Override void checkWhetherSimpleVar(SyntaxUnit use) {
        Syntax.error(use, name + " is an array and no simple variable!");
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static GlobalArrayDecl parse() {
        Log.enterParser("<var decl>");

        GlobalArrayDecl globVarDec = new GlobalArrayDecl(Scanner.nextName);
        globVarDec.type = Types.getType(Scanner.curToken);
        Scanner.readNext(); // Skip type
        Scanner.skip(nameToken);
        Scanner.skip(leftBracketToken);
        Number.parse();
        Scanner.skip(rightBracketToken);
        Scanner.skip(semicolonToken);
        Log.enterParser("</var decl>");

        return globVarDec;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
}


/*
 * A global simple variable declaration
 */
class GlobalSimpleVarDecl extends VarDecl {
    GlobalSimpleVarDecl(String n) {
        super(n);
        assemblerName = (Cflat.underscoredGlobals() ? "_" : "") + n;
    }

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherSimpleVar(SyntaxUnit use) {
	/* OK */
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static GlobalSimpleVarDecl parse() {
        Log.enterParser("<var decl>");

        GlobalSimpleVarDecl globVarDec = new GlobalSimpleVarDecl(Scanner.nextName);
        globVarDec.type = Types.getType(Scanner.curToken);
        Scanner.readNext(); // skip type
        Scanner.skip(Token.nameToken);  // next should be name token
        Scanner.skip(semicolonToken);

        Log.enterParser("</var decl>");
        return globVarDec;
    }
}


/*
 * A local array declaration
 */
class LocalArrayDecl extends VarDecl {
    LocalArrayDecl(String n) {
        super(n);
    }

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherSimpleVar(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static LocalArrayDecl parse() {
        Log.enterParser("<var decl>");

        LocalArrayDecl localArrayDecl = new LocalArrayDecl(Scanner.nextName);
        localArrayDecl.type = Types.getType(Scanner.curToken);
        Scanner.readNext(); // Skip type
        Scanner.skip(nameToken);
        Scanner.skip(leftBracketToken);
        Number.parse();
        Scanner.skip(rightBracketToken);
        Scanner.skip(semicolonToken);

        Log.enterParser("</var decl>");
        return null;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }

}


/*
 * A local simple variable declaration
 */
class LocalSimpleVarDecl extends VarDecl {
    LocalSimpleVarDecl(String n) {
        super(n);
    }

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherSimpleVar(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static LocalSimpleVarDecl parse() {
        Log.enterParser("<var decl>");

        LocalSimpleVarDecl lvd = new LocalSimpleVarDecl(Scanner.nextName);
        lvd.type = Types.getType(Scanner.curToken);
        Scanner.readNext();                 // skip type
        Scanner.skip(Token.nameToken);      // next should be name token
        Scanner.skip(semicolonToken);

        Log.enterParser("</var decl>");
        return lvd;
    }
}


/*
 * A <param decl>
 */
class ParamDecl extends VarDecl {
    int paramNum = 0;

    ParamDecl(String n) {
        super(n);
    }

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherSimpleVar(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static ParamDecl parse() {
        Log.enterParser("<param decl>");
        ParamDecl parDec = new ParamDecl(Scanner.nextName);
        parDec.type = Types.getType(Scanner.curToken);
        Scanner.readNext(); // skip type
        Scanner.skip(nameToken);
        Log.enterParser("</param decl>");
        return parDec;
    }
}


/*
 * A <func decl>
 */
class FuncDecl extends Declaration {
    //-- Must be changed in part 1+2:
    ParamDeclList paramDeclList;
    FuncBody body;

    FuncDecl(String n) {
        // Used for user functions:

        super(n);
        assemblerName = (Cflat.underscoredGlobals() ? "_" : "") + n;
        //-- Must be changed in part 1:
    }

    @Override int declSize() {
        return 0;
    }

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherFunction(int nParamsUsed, SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override void checkWhetherSimpleVar(SyntaxUnit use) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static FuncDecl parse() {
        Log.enterParser("<func decl>");

        FuncDecl funcDecl = new FuncDecl(Scanner.nextName);
        funcDecl.type = Types.getType(Scanner.curToken);
        Scanner.readNext(); // skip type
        Scanner.skip(nameToken);
        Scanner.skip(leftParToken);
        funcDecl.paramDeclList = ParamDeclList.parse();
        Scanner.skip(rightParToken);
        funcDecl.body = FuncBody.parse();

        Log.enterParser("</func decl>");

        return funcDecl;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
}

/*
 */

class FuncBody extends Statement {
    StatmList body;
    LocalDeclList localDeclList;

    @Override
    void check(DeclList curDecls) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void printTree() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    static FuncBody parse(){
        Log.enterParser("<func body>");

        FuncBody fb = new FuncBody();
        Scanner.skip(leftCurlToken);
        fb.localDeclList = LocalDeclList.parse();
        fb.body = StatmList.parse();
        Scanner.skip(rightCurlToken);
        Log.enterParser("</func body>");

        return fb;
    }
}

/*
 * A <statm list>.
 */
class StatmList extends SyntaxUnit {
    //-- Must be changed in part 1:
    LocalSimpleVarDecl varDecl;
    Statement firstStatement = null;

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static StatmList parse() {
        Log.enterParser("<statm list>");

        StatmList sl = new StatmList();
        Statement lastStatm = null;

        while (Scanner.curToken != rightCurlToken) {
            Statement statement = Statement.parse();

            if(sl.firstStatement == null)
                sl.firstStatement = statement;

            if(lastStatm != null)
                lastStatm.nextStatm = statement;

            lastStatm = statement;
        }

        Log.leaveParser("</statm list>");
        return sl;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
}


/*
 * A <statement>.
 */
abstract class Statement extends SyntaxUnit {
    Statement nextStatm = null;

    static Statement parse() {
        Log.enterParser("<statement>");

        Statement s = null;
        if (Scanner.curToken==nameToken &&
                Scanner.nextToken==leftParToken) {
            s = CallStatm.parse();
        } else if (Scanner.curToken == nameToken) {
            s = AssignStatm.parse();
        } else if (Scanner.curToken == forToken) {
            s = ForStatm.parse();
        } else if (Scanner.curToken == ifToken) {
            s = IfStatm.parse();
        } else if (Scanner.curToken == returnToken) {
            s = ReturnStatm.parse();
        } else if (Scanner.curToken == whileToken) {
            s = WhileStatm.parse();
        } else if (Scanner.curToken == semicolonToken) {
            s = EmptyStatm.parse();
        } else {
            Error.expected("A statement");
        }
        Log.leaveParser("</statement>");
        return s;
    }
}




/*
 * An <empty statm>.
 */
class EmptyStatm extends Statement {
    //-- Must be changed in part 2:

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static EmptyStatm parse() {
        EmptyStatm emptyStatm = new EmptyStatm();
        Scanner.skip(semicolonToken);
        return emptyStatm;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
}
	

/*
 * A <for-statm>.
 */
//-- Must be changed in part 1+2:
class ForStatm extends Statement {
    ForControl forControl;
    StatmList statmList;

    @Override
    void check(DeclList curDecls) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void printTree() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    static ForStatm parse(){
        Log.enterParser("<for-statm>");

        ForStatm forStatm = new ForStatm();
        Scanner.skip(forToken);
        Scanner.skip(leftParToken);
        forStatm.forControl = ForControl.parse();
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        StatmList statmList = StatmList.parse();
        Scanner.skip(rightCurlToken);
        Log.enterParser("</for-statm>");

    }
}


class ForControl {
    static ForControl parse(){
        ForControl forControl = new ForControl();



        return forControl;
    }
}

/*
 * An <if-statm>.
 */
class IfStatm extends Statement {
    Expression test;
    StatmList body;
    ElsePart elsePart;

    //-- Must be changed in part 1+2:

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static IfStatm parse() {
        //-- Must be changed in part 1:
        Log.enterParser("<if-statm>");

        IfStatm ifStm = new IfStatm();
        Scanner.readNext(); // jumps over if
        Scanner.skip(leftParToken);
        ifStm.test = Expression.parse(); // checks expression
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        ifStm.body = StatmList.parse();
        Scanner.skip(rightCurlToken);

        if(Scanner.curToken == elseToken){
            ifStm.elsePart = ElsePart.parse();
        }

        Log.leaveParser("</if-statm>");
        return ifStm;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
}

class ElsePart extends Statement {
    StatmList statmList;

    static ElsePart parse(){
        ElsePart elsePart = new ElsePart();
        Scanner.skip(elseToken);
        Scanner.skip(leftCurlToken);
        StatmList statmList = StatmList.parse();
        Scanner.skip(rightCurlToken);
        return elsePart;
    }

    @Override
    void check(DeclList curDecls) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void printTree() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}


/*
 * A <return-statm>.
 */
//-- Must be changed in part 2:
class ReturnStatm extends  Statement {
    Expression expression;

    @Override
    void check(DeclList curDecls) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void printTree() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    static ReturnStatm parse(){
        ReturnStatm returnStatement = new ReturnStatm();
        Scanner.skip(returnToken);
        returnStatement.expression = Expression.parse();
        Scanner.skip(semicolonToken);
        return returnStatement;
    }
}

/*
 * A <while-statm>.
 */
class WhileStatm extends Statement {
    Expression test;
    StatmList body;

    @Override void check(DeclList curDecls) {
        test.check(curDecls);
        body.check(curDecls);
    }

    @Override void genCode(FuncDecl curFunc) {
        String testLabel = Code.getLocalLabel(),
                endLabel  = Code.getLocalLabel();

        Code.genInstr(testLabel, "", "", "Start while-statement");
        test.genCode(curFunc);
        test.valType.genJumpIfZero(endLabel);
        body.genCode(curFunc);
        Code.genInstr("", "jmp", testLabel, "");
        Code.genInstr(endLabel, "", "", "End while-statement");
    }

    static WhileStatm parse() {
        Log.enterParser("<while-statm>");

        WhileStatm ws = new WhileStatm();
        Scanner.readNext(); // jumps over while
        Scanner.skip(leftParToken);
        ws.test = Expression.parse();
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        ws.body = StatmList.parse();
        Scanner.skip(rightCurlToken);

        Log.leaveParser("</while-statm>");
        return ws;
    }

    @Override void printTree() {
        Log.wTree("while (");  test.printTree();  Log.wTreeLn(") {");
        Log.indentTree();
        body.printTree();
        Log.outdentTree();
        Log.wTreeLn("}");
    }
}


//-- Must be changed in part 1+2:


/*
 * An <expression list>.
 */

class ExprList extends SyntaxUnit {
    Expression firstExpr = null;

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static ExprList parse() {
        Log.enterParser("<expr list>");

        Expression lastExpr = null;
        ExprList exprList = new ExprList();
        exprList.firstExpr = Expression.parse();
        lastExpr = exprList.firstExpr;

        while(Scanner.curToken == commaToken){
            Expression expression = Expression.parse();
            lastExpr.nextExpr = expression;     // assign pointer
            lastExpr = expression;              // set new last expression
        }

        Log.enterParser("</expr list>");
        return exprList;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
    //-- Must be changed in part 1:
}


/*
 * An <expression>
 */
class Expression extends Operand {
    Expression nextExpr = null;
    Term firstTerm, secondTerm = null;
    Operator relOp = null;
    boolean innerExpr = false;

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static Expression parse() {
        Log.enterParser("<expression>");

        Expression e = new Expression();
        e.firstTerm = Term.parse();
        if (Token.isRelOperator(Scanner.curToken)) {
            e.relOp = RelOperator.parse();
            e.secondTerm = Term.parse();
        }
        Log.leaveParser("</expression>");
        return e;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
}

class Factor extends Operator {

    @Override
    void genCode(FuncDecl curFunc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void printTree() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    static Factor parse(){
        Log.leaveParser("<factor>");

        Factor factor = new Factor();

        Operand startOperator = Operand.parse();    // goes to next
        Operand iter = null;

        while(Token.isFactorOperator(Scanner.curToken)){
            FactorOperator

            startOperator.nextOperand = oper;
        }
        Log.leaveParser("</factor>");
        return factor;
    }
};

class FactorOperator extends  Operator {

    @Override
    void genCode(FuncDecl curFunc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void printTree() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    static FactorOperator parse(){
        Log.enterParser("<factor>");

        FactorOperator factorOperator = new FactorOperator();
        factorOperator.operand = Operand.parse();
        Log.enterParser("</factor>");

        return factorOperator;
    }
}


/*
 * A <term>
 */
class Term extends SyntaxUnit {
    //-- Must be changed in part 1+2:
    Factor factor;
    Factor firstFactor = null;
    Operator operator;


    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static Term parse() {
        //-- Must be changed in part 1:
        Term term = new Term();
        term.factor = Factor.parse();

        if(Token.isTermOperator(Scanner.curToken)){
            term.operator = new AddOperator();
            term.operator.opToken = Scanner.curToken;
        }
        else if(Token.isFactorOperator(Scanner.curToken)){

        }
        return term;
    }

    @Override void printTree() {
        //-- Must be changed in part 1+2:
    }
}

//-- Must be changed in part 1+2:

/*
 * An <operator>
 */
abstract class Operator extends SyntaxUnit {
    Operator nextOp = null;
    Type opType;
    Token opToken;

    @Override void check(DeclList curDecls) {}
}

class AddOperator extends Operator {

    @Override
    void genCode(FuncDecl curFunc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void printTree() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

//-- Must be changed in part 1+2:


/*
 * A relational operator (==, !=, <, <=, > or >=).
 */

class RelOperator extends Operator {
    @Override void genCode(FuncDecl curFunc) {
        if (opType == Types.doubleType) {
            Code.genInstr("", "fldl", "(%esp)", "");
            Code.genInstr("", "addl", "$8,%esp", "");
            Code.genInstr("", "fsubp", "", "");
            Code.genInstr("", "fstps", Code.tmpLabel, "");
            Code.genInstr("", "cmpl", "$0,"+Code.tmpLabel, "");
        } else {
            Code.genInstr("", "popl", "%ecx", "");
            Code.genInstr("", "cmpl", "%eax,%ecx", "");
        }
        Code.genInstr("", "movl", "$0,%eax", "");
        switch (opToken) {
            case equalToken:
                Code.genInstr("", "sete", "%al", "Test ==");  break;
            case notEqualToken:
                Code.genInstr("", "setne", "%al", "Test !=");  break;
            case lessToken:
                Code.genInstr("", "setl", "%al", "Test <");  break;
            case lessEqualToken:
                Code.genInstr("", "setle", "%al", "Test <=");  break;
            case greaterToken:
                Code.genInstr("", "setg", "%al", "Test >");  break;
            case greaterEqualToken:
                Code.genInstr("", "setge", "%al", "Test >=");  break;
        }
    }

    static RelOperator parse() {
        Log.enterParser("<rel operator>");

        RelOperator ro = new RelOperator();
        ro.opToken = Scanner.curToken;
        Scanner.readNext();

        Log.leaveParser("</rel operator>");
        return ro;
    }

    @Override void printTree() {
        String op = "?";
        switch (opToken) {
            case equalToken:        op = "==";  break;
            case notEqualToken:     op = "!=";  break;
            case lessToken:         op = "<";   break;
            case lessEqualToken:    op = "<=";  break;
            case greaterToken:      op = ">";   break;
            case greaterEqualToken: op = ">=";  break;
        }
        Log.wTree(" " + op + " ");
    }
}


/*
 * An <operand>
 */
abstract class Operand extends SyntaxUnit {
    Operand nextOperand = null;
    Type valType;

    static Operand parse() {
        Log.enterParser("<operand>");

        Operand o = null;
        if (Scanner.curToken == numberToken) {
            o = Number.parse();
        } else if (Scanner.curToken==nameToken && Scanner.nextToken==leftParToken) {
            o = FunctionCall.parse();
        } else if (Scanner.curToken == nameToken) {
            o = Variable.parse();
        } else if (Scanner.curToken == leftParToken) {
            Scanner.readNext();
            o = Expression.parse();
            ((Expression)o).innerExpr = true;
            Scanner.skip(rightParToken);
        } else {
            Error.expected("An operand");
        }

        Log.leaveParser("</operand>");
        return o;
    }
}


/*
 * A <function call>.
 */
class FunctionCall extends Operand {
    //-- Must be changed in part 2:
    String name;
    ExprList exprList;

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static FunctionCall parse() {
        Log.enterParser("<function call>");
        FunctionCall funcCall = new FunctionCall();
        funcCall.name = Scanner.curName;
        Scanner.skip(nameToken);
        Scanner.skip(leftParToken);
        funcCall.exprList = ExprList.parse();
        Scanner.skip(rightParToken);
        Log.enterParser("</function call>");
        return funcCall;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
    //-- Must be changed in part 1+2:
}


/*
 * A <number>.
 */
class Number extends Operand {
    int numVal;

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        Code.genInstr("", "movl", "$"+numVal+",%eax", ""+numVal);
    }

    static Number parse() {
        Number num = new Number();
        num.numVal = Scanner.curNum;
        num.valType = Types.getType(numberToken);
        Scanner.skip(numberToken);

        return num;
    }

    @Override void printTree() {
        Log.wTree("" + numVal);
    }
}


/*
 * A <variable>.
 */

class Variable extends Operand {
    String varName;
    VarDecl declRef = null;
    Expression index = null;

    @Override void check(DeclList curDecls) {
        Declaration d = curDecls.findDecl(varName,this);
        if (index == null) {
            d.checkWhetherSimpleVar(this);
            valType = d.type;
        } else {
            d.checkWhetherArray(this);
            index.check(curDecls);
            index.valType.checkType(lineNum, Types.intType, "Array index");
            valType = ((ArrayType)d.type).elemType;
        }
        declRef = (VarDecl)d;
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static Variable parse() {
        Log.enterParser("<variable>");

        Variable var = new Variable();
        var.varName = Scanner.curName;
        Scanner.skip(nameToken);

        if(Scanner.curToken == leftBracketToken){
            Scanner.skip(leftBracketToken);
            var.index = Expression.parse();
            Scanner.skip(rightBracketToken);
        }

        Log.enterParser("</variable>");

        return var;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
}

class AssignStatm extends Statement {
    Assignment assignment;
    @Override
    void check(DeclList curDecls) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void printTree() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    static AssignStatm parse(){
        Log.enterParser("<assign-statm>");

        AssignStatm assignStatm = new AssignStatm();
        assignStatm.assignment = Assignment.parse();
        Scanner.skip(semicolonToken);
        Log.enterParser("</assign-statm>");

        return assignStatm;
    }
}

class Assignment extends Variable {
    Variable variable;
    Expression expression;

    static Assignment parse(){
        Log.enterParser("<assignment>");

        Assignment assignment = new Assignment();
        assignment.variable = Variable.parse();
        Scanner.skip(equalToken);
        assignment.expression = Expression.parse();

        Log.enterParser("</assignment>");
        return assignment;
    }
}

class CallStatm extends Statement {
    FunctionCall functCall = new FunctionCall();

    @Override
    void check(DeclList curDecls) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void genCode(FuncDecl curFunc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void printTree() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    static CallStatm parse(){
        Log.enterParser("<call-statm>");

        CallStatm callStatm = new CallStatm();
        callStatm.functCall = FunctionCall.parse();
        Scanner.skip(semicolonToken);
        Log.enterParser("</call-statm>");

        return callStatm;
    }
}