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
        outerScope.addDecl(d);  // added
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
        while (Token.isTypeName(Scanner.curToken)) {
            if (Scanner.nextToken == nameToken) {
                if (Scanner.nextNextToken == leftParToken) {
                    gdl.addDecl(FuncDecl.parse());
                } else if (Scanner.nextNextToken == leftBracketToken) {
                    gdl.addDecl(GlobalArrayDecl.parse());
                } else {
                    //-- Must be changed in part 1:
                    // TODO Check global parameters
                    System.out.println("Check global parameters");
                    gdl.addDecl(GlobalSimpleVarDecl.parse());
                }
            } else {
                Error.expected("A declaration");
            }
        }
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
        //-- Must be changed in part 1:
        return null;
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
        return null;
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

        //-- Must be changed in part 1:
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

        //-- Must be changed in part 1:
        return null;
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

        //-- Must be changed in part 1:
        return null;
    }
}


/*
 * A <func decl>
 */
class FuncDecl extends Declaration {
    //-- Must be changed in part 1+2:
    ParamDeclList paramDeclList;
    StatmList body;


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
        Scanner.skip(leftParToken);
        funcDecl.paramDeclList = ParamDeclList.parse();
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        funcDecl.body = StatmList.parse();
        Scanner.skip(rightCurlToken);
        Log.enterParser("</func decl>");

        return funcDecl;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
}


/*
 * A <statm list>.
 */
class StatmList extends SyntaxUnit {
    //-- Must be changed in part 1:

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
            //-- Must be changed in part 1:
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
            //-- Must be changed in part 1:
        } else if (Scanner.curToken == nameToken) {

            //-- Must be changed in part 1:
        } else if (Scanner.curToken == forToken) {
            //-- Must be changed in part 1:
        } else if (Scanner.curToken == ifToken) {
            s = IfStatm.parse();
        } else if (Scanner.curToken == returnToken) {
            //-- Must be changed in part 1:
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
    //-- Must be changed in part 1+2:

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static EmptyStatm parse() {
        //-- Must be changed in part 1:
        return null;
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
 * An <if-statm>.
 */
class IfStatm extends Statement {
    Expression test;
    StatmList body;
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

        // TODO Else statement?

        Log.leaveParser("</if-statm>");
        return ifStm;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
}


/*
 * A <return-statm>.
 */
//-- Must be changed in part 1+2:


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
        Expression lastExpr = null;

        Log.enterParser("<expr list>");

        //-- Must be changed in part 1:
        return null;
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


/*
 * A <term>
 */
class Term extends SyntaxUnit {
    //-- Must be changed in part 1+2:

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static Term parse() {
        //-- Must be changed in part 1:
        return null;
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
    //-- Must be changed in part 1+2:

    @Override void check(DeclList curDecls) {
        //-- Must be changed in part 2:
    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static FunctionCall parse() {
        //-- Must be changed in part 1:
        return null;
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
        //-- Must be changed in part 1:
        return null;
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
        //-- Must be changed in part 1:


        Log.enterParser("</variable>");

        return null;
    }

    @Override void printTree() {
        //-- Must be changed in part 1:
    }
}
