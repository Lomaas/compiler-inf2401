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
        library = new GlobalDeclList();
        FuncDecl funcDecl = new FuncDecl("putchar");
        funcDecl.paramDeclList = new ParamDeclList();
        funcDecl.paramDeclList.numParams = 1;
        funcDecl.type = Types.intType;
        funcDecl.visible = true;
        funcDecl.lineNum = -1;
        funcDecl.assemblerName = (Cflat.underscoredGlobals() ? "_" : "") + "putchar";

        ParamDecl paramDecl = new ParamDecl("character");
        paramDecl.type = Types.intType;
        funcDecl.paramDeclList.addDecl(paramDecl);
        library.addDecl(funcDecl);

        funcDecl = new FuncDecl("getchar");
        funcDecl.paramDeclList = new ParamDeclList();
        funcDecl.paramDeclList.numParams = 0;
        funcDecl.type = Types.intType;
        funcDecl.visible = true;
        funcDecl.lineNum = -1;
        funcDecl.assemblerName = (Cflat.underscoredGlobals() ? "_" : "") + "getchar";
        library.addDecl(funcDecl);

        funcDecl = new FuncDecl("exit");
        funcDecl.paramDeclList = new ParamDeclList();
        funcDecl.paramDeclList.numParams = 1;
        funcDecl.type = Types.intType;
        funcDecl.visible = true;
        funcDecl.lineNum = -1;
        funcDecl.assemblerName = (Cflat.underscoredGlobals() ? "_" : "") + "exit";

        paramDecl = new ParamDecl("status");
        paramDecl.type = Types.intType;
        funcDecl.paramDeclList.addDecl(paramDecl);
        library.addDecl(funcDecl);

        funcDecl = new FuncDecl("getdouble");
        funcDecl.paramDeclList = new ParamDeclList();
        funcDecl.paramDeclList.numParams = 0;
        funcDecl.type = Types.doubleType;
        funcDecl.visible = true;
        funcDecl.lineNum = -1;
        funcDecl.assemblerName = (Cflat.underscoredGlobals() ? "_" : "") + "getdouble";
        library.addDecl(funcDecl);

        funcDecl = new FuncDecl("getint");
        funcDecl.paramDeclList = new ParamDeclList();
        funcDecl.paramDeclList.numParams = 0;
        funcDecl.type = Types.intType;
        funcDecl.visible = true;
        funcDecl.lineNum = -1;
        funcDecl.assemblerName = (Cflat.underscoredGlobals() ? "_" : "") + "getint";

        library.addDecl(funcDecl);

        funcDecl = new FuncDecl("putint");
        funcDecl.paramDeclList = new ParamDeclList();
        funcDecl.paramDeclList.numParams = 1;
        funcDecl.type = Types.intType;
        funcDecl.visible = true;
        funcDecl.lineNum = -1;
        funcDecl.assemblerName = (Cflat.underscoredGlobals() ? "_" : "") + "putint";

        paramDecl = new ParamDecl("c");
        paramDecl.type = Types.intType;
        funcDecl.paramDeclList.addDecl(paramDecl);
        library.addDecl(funcDecl);

        funcDecl = new FuncDecl("putdouble");
        funcDecl.paramDeclList = new ParamDeclList();
        funcDecl.paramDeclList.numParams = 1;
        funcDecl.type = Types.doubleType;
        funcDecl.visible = true;
        funcDecl.lineNum = -1;
        funcDecl.assemblerName = (Cflat.underscoredGlobals() ? "_" : "") + "putdouble";

        paramDecl = new ParamDecl("c");
        paramDecl.type = Types.doubleType;
        funcDecl.paramDeclList.addDecl(paramDecl);
        library.addDecl(funcDecl);
    }

    public static void finish() {

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
            Declaration d = progDecls.findDecl("main", this);
            if(d != null){
               d.checkWhetherFunction(0, this);
               d.type.checkType(lineNum, Types.intType, "int type");
            }
            else {
                Syntax.error(this, "Name main is unknown!");
            }
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
    Declaration lastDecl = null;
    DeclList outerScope;

    DeclList () {

    }

    @Override void check(DeclList curDecls) {
        outerScope = curDecls;

        Declaration dx = firstDecl;
        while (dx != null) {
            dx.check(this);  dx = dx.nextDecl;
        }
    }

    @Override void printTree() {
        Declaration iter = firstDecl;

        while(iter != null){
            iter.printTree();
            iter = iter.nextDecl;
        }
    }

    void addDecl(Declaration d) {
        if(firstDecl == null){
            firstDecl = d;
            lastDecl = d;
        }
        else {
            lastDecl.nextDecl = d;
            lastDecl = d;
            if(isDuplicate()){
                Error.error("Duplicate name declaration");
            }
        }
    }

    boolean isDuplicate(){
        Declaration iter = firstDecl;

        while(iter != null && iter != lastDecl){
            if(iter.name.equals(lastDecl.name)){
                return true;
            }
            iter = iter.nextDecl;
        }
        return false;
    }

    int dataSize() {
        int res = 0;
        Declaration iter = firstDecl;
        while (iter != null) {
            res += iter.declSize();
            iter = iter.nextDecl;
        }
        return res;
    }

    Declaration findDecl(String name, SyntaxUnit usedIn) {
        DeclList scope = this;
        Declaration iter = null;

        while(scope != null){
            iter = scope.firstDecl;

            while(iter != null){
                if(iter.name.equals(name) && iter.visible == true){
                    Log.noteBinding(name, iter.lineNum, lineNum);
                    return iter;
                }
                iter = iter.nextDecl;
            }
            scope = scope.outerScope;
        }
        Syntax.error(this, "Couldn't find declaration: " + name);
        return null;
    }
}


/*
 * A list of global declarations. 
 * (This class is not mentioned in the syntax diagrams.)
 */
class GlobalDeclList extends DeclList {
    @Override void genCode(FuncDecl curFunc) {
        Declaration iter = firstDecl;

        while(iter != null){
            iter.genCode(curFunc);
            iter = iter.nextDecl;
        }
    }

    static GlobalDeclList parse() {
        GlobalDeclList gdl = new GlobalDeclList();
        Scanner.readNext();
        Scanner.readNext();
        Scanner.readNext();

        while (Token.isTypeName(Scanner.curToken)) {
            if (Scanner.nextToken == nameToken) {
                if (Scanner.nextNextToken == leftParToken) {
                    gdl.addDecl(FuncDecl.parse());
                } else if (Scanner.nextNextToken == leftBracketToken) {
                    gdl.addDecl(GlobalArrayDecl.parse());
                } else {
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
        Declaration iter = firstDecl;
        int decSize = 0;

        while(iter != null){
            decSize = decSize - iter.declSize();    // calculate position stack
            iter.assemblerName = decSize + "(%ebp)";
            iter.genCode(curFunc);
            iter = iter.nextDecl;
        }
    }

    static LocalDeclList parse() {
        LocalDeclList ldl = new LocalDeclList();

        while (Token.isTypeName(Scanner.curToken)) {
            if (Scanner.nextToken == nameToken) {

                if (Scanner.nextNextToken == leftBracketToken) {
                    ldl.addDecl(LocalArrayDecl.parse());
                } else {
                    ldl.addDecl(LocalSimpleVarDecl.parse());
                }
            } else {
                Error.expected("A declaration");
            }
        }
        return ldl;
    }
}


/*
 * A list of parameter declarations. 
 * (This class is not mentioned in the syntax diagrams.)
 */
class ParamDeclList extends DeclList {
    int numParams = 0;

    @Override void genCode(FuncDecl curFunc) {
        Declaration iter = firstDecl;
        int decSize = 8;

        while(iter != null){
            iter.assemblerName = decSize + "(%ebp)";
            decSize += iter.declSize();
            iter.genCode(curFunc);
            iter = iter.nextDecl;
        }
    }

    static ParamDeclList parse() {
        ParamDeclList paramDeclList = new ParamDeclList();

        while(Scanner.curToken != rightParToken){
            // Type, name
            ParamDecl paramDecl = ParamDecl.parse();
            paramDeclList.addDecl(paramDecl);
            paramDeclList.numParams++;

            if(Scanner.curToken == commaToken){
                Scanner.skip(commaToken);
            }
        }

        return paramDeclList;
    }

    @Override void printTree(){
        Declaration iter = firstDecl;
        boolean printComma = false;

        while(iter != null){
            if(printComma == true){
                Log.wTree(", ");
            }
            Log.wTree(iter.type.typeName() + " " + iter.name);
            iter = iter.nextDecl;
            printComma = true;
        }
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
 * A <var decl> OK
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

    void genStore(Type valType){
        if(type == Types.doubleType && valType == Types.doubleType){
            Code.genInstr("", "fstpl", assemblerName, name + " =");
        }
        else if(type == Types.doubleType && valType == Types.intType){
            Code.genInstr("", "movl", "%eax, " + Code.tmpLabel, "");
            Code.genInstr("", "fildl", Code.tmpLabel, "     (double)");
            Code.genInstr("", "fstpl", assemblerName, name + " =");
        }
        else if(type == Types.intType && valType == Types.doubleType){
            Code.genInstr("", "fistpl", assemblerName, name + " = (int)");
        }
        else if(type==Types.intType && valType == Types.intType){
            Code.genInstr("", "movl", "%eax," + assemblerName, name + " =");
        }
        else {
            Error.panic("Declaration.genStore");
        }
    }
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
        Code.genVar(assemblerName, true, declSize(),
                type.typeName() + " " + name + ";");
    }

    static GlobalArrayDecl parse() {
        Log.enterParser("<var decl>");

        GlobalArrayDecl globVarDec = new GlobalArrayDecl(Scanner.nextName);
        Type type = Types.getType(Scanner.curToken);
        Scanner.readNext(); // Skip type
        Scanner.skip(nameToken);
        Scanner.skip(leftBracketToken);
        globVarDec.type = new ArrayType(Scanner.curNum, type);
        Scanner.readNext(); // Skip number
        Scanner.skip(rightBracketToken);
        Scanner.skip(semicolonToken);
        Log.leaveParser("</var decl>");

        return globVarDec;
    }

    @Override void printTree() {
        ArrayType arrayType = (ArrayType) type;
        Log.wTree(arrayType.typeName() + " " + name);
        Log.wTree("[");
        Log.wTree(Integer.toString(arrayType.nElems));
        Log.wTree("]");
        Log.wTreeLn(";");
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
        visible = true;
    }

    @Override void checkWhetherArray(SyntaxUnit use) {
        Syntax.error(use, name + " is an simple variable and no array!");
    }

    @Override void checkWhetherSimpleVar(SyntaxUnit use) {
	    /* OK */
    }

    @Override void genCode(FuncDecl curFunc) {
        Code.genVar(assemblerName, true, declSize(),
                type.typeName() + " " +name + ";");
    }

    static GlobalSimpleVarDecl parse() {
        Log.enterParser("<var decl>");

        GlobalSimpleVarDecl globVarDec = new GlobalSimpleVarDecl(Scanner.nextName);
        globVarDec.type = Types.getType(Scanner.curToken);
        Scanner.readNext(); // skip type
        Scanner.skip(Token.nameToken);  // next should be name token
        Scanner.skip(semicolonToken);

        Log.leaveParser("</var decl>");
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
        visible = true;

        if (((ArrayType) type).nElems < 0)
            Syntax.error(this, "Arrays cannot have negative size!");
    }

    @Override void checkWhetherArray(SyntaxUnit use) {
        /* OK */
    }

    @Override void checkWhetherSimpleVar(SyntaxUnit use) {
        Syntax.error(use, name + " is an array and no simple variable!");
    }

    @Override void genCode(FuncDecl curFunc) {
//        if(type == Types.intType){
//            Code.genInstr("", "leal", assemblerName + ", %edx", "");
//            Code.genInstr("", "movl", "(%edx, %eax, 4), %eax", "");
//        }
//        else if(type == Types.doubleType){
//            Code.genInstr("", "leal", assemblerName+ ", %edx", "");
//            Code.genInstr("", "movl", "(%edx, %eax, 8), %eax", "");
//        }
    }

    static LocalArrayDecl parse() {
        Log.enterParser("<var decl>");

        LocalArrayDecl localArrayDecl = new LocalArrayDecl(Scanner.nextName);
        Type type = Types.getType(Scanner.curToken);
        Scanner.readNext(); // Skip type
        Scanner.skip(nameToken);
        Scanner.skip(leftBracketToken);
        localArrayDecl.type = new ArrayType(Scanner.curNum, type);
        Scanner.readNext(); // Skip number
        Scanner.skip(rightBracketToken);
        Scanner.skip(semicolonToken);

        Log.leaveParser("</var decl>");
        return localArrayDecl;
    }

    @Override void printTree() {
        ArrayType arrayType = (ArrayType) type;
        Log.wTree(arrayType.typeName() + " " + name);
        Log.wTree("[");
        Log.wTree(Integer.toString(arrayType.nElems));
        Log.wTree("]");
        Log.wTreeLn(";");
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
        visible = true;
    }

    @Override void checkWhetherArray(SyntaxUnit use) {
        Syntax.error(use, name + " is an simple variable and no array!");
    }

    @Override void checkWhetherSimpleVar(SyntaxUnit use) {
        /* OK */
    }

    @Override void genCode(FuncDecl curFunc) {
//
//        if(type == Types.intType)
//            Code.genInstr("", "movl", assemblerName + ", %eax", "     (int)");
//        else if(type == Types.doubleType)
//            Code.genInstr("", "fldl", assemblerName, "  (double)");
    }

    static LocalSimpleVarDecl parse() {
        Log.enterParser("<var decl>");

        LocalSimpleVarDecl lvd = new LocalSimpleVarDecl(Scanner.nextName);
        lvd.type = Types.getType(Scanner.curToken);
        Scanner.readNext();                 // skip type
        Scanner.skip(Token.nameToken);      // next should be name token
        Scanner.skip(semicolonToken);

        Log.leaveParser("</var decl>");
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
        visible = true;
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
        Log.leaveParser("</param decl>");
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
        visible = true;
        paramDeclList.check(curDecls);
        body.check(paramDeclList);
    }

    @Override void checkWhetherArray(SyntaxUnit use) {
        //-- Must be changed in part 2:
        Syntax.error(this, "Not an array");
    }

    @Override void checkWhetherFunction(int nParamsUsed, SyntaxUnit use) {
        /* OK */
        if(name.equals("main") && paramDeclList.numParams > 0){
            Syntax.error(this, "Function 'main' should have no parameters!");
        }
        else if(paramDeclList.numParams != nParamsUsed)
            Syntax.error(this, "Calls to " + name + " should have " +
                    Integer.toString(paramDeclList.numParams) + " parameters, not "+
                    Integer.toString(nParamsUsed)+ "!");

    }

    @Override void checkWhetherSimpleVar(SyntaxUnit use) {
        Syntax.error(this, "Not an simple var");
    }

    @Override void genCode(FuncDecl curFunc) {
        Code.genInstr("", ".globl", assemblerName,"");
        Code.genInstr(assemblerName, "enter", "$" + body.localDeclList.dataSize() + ", $0",
                "Start funksjon " + name);
        paramDeclList.genCode(this);
        body.genCode(this);

        if(type == Types.doubleType)
            Code.genInstr("", "fldz", "", "");

        Code.genInstr(".exit$" +name, "","", "");
        Code.genInstr("", "leave", "", "");
        Code.genInstr("", "ret", "", "End function " + name);
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

        Log.leaveParser("</func decl>");

        return funcDecl;
    }

    @Override void printTree() {
        Log.wTree(type.typeName() + " ");
        Log.wTree(name + " (");
        paramDeclList.printTree();
        Log.wTree(")");
        body.printTree();

    }
}

/*
 */

class FuncBody extends Statement {
    LocalDeclList localDeclList = null;
    StatmList body;

    @Override
    void check(DeclList curDecls) {
        if(localDeclList != null){
            localDeclList.check(curDecls);
            body.check(localDeclList);
        }
        else {
            body.check(curDecls);
        }
    }

    @Override
    void genCode(FuncDecl curFunc) {
        localDeclList.genCode(curFunc);
        body.genCode(curFunc);
    }

    @Override
    void printTree() {
        Log.wTreeLn("{");
        Log.indentTree();

        if(localDeclList != null)
           localDeclList.printTree();

        body.printTree();
        Log.outdentTree();
        Log.wTreeLn("}");
    }

    static FuncBody parse(){
        Log.enterParser("<func body>");

        FuncBody fb = new FuncBody();
        Scanner.skip(leftCurlToken);

        fb.localDeclList = LocalDeclList.parse();

        fb.body = StatmList.parse();
        Scanner.skip(rightCurlToken);
        Log.leaveParser("</func body>");

        return fb;
    }
}

/*
 * A <statm list>.
 */
class StatmList extends SyntaxUnit {
    Statement firstStatement = null;

    @Override void check(DeclList curDecls) {
        Statement iter = firstStatement;
        while(iter != null){
            iter.check(curDecls);
            iter = iter.nextStatm;
        }
    }

    @Override void genCode(FuncDecl curFunc) {
        Statement iter = firstStatement;
        while(iter != null){
            iter.genCode(curFunc);
            iter = iter.nextStatm;
        }
    }

    static StatmList parse() {
        Log.enterParser("<statm list>");

        StatmList sl = new StatmList();
        Statement lastStatm = null;

        while (Scanner.curToken != rightCurlToken){
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
        Statement iter = firstStatement;
        while(iter != null){
            iter.printTree();
            iter = iter.nextStatm;
        }
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

    }

    @Override void genCode(FuncDecl curFunc) {
        //-- Must be changed in part 2:
    }

    static EmptyStatm parse() {
        Log.enterParser("<emtpy-stmt");
        EmptyStatm emptyStatm = new EmptyStatm();
        Scanner.skip(semicolonToken);
        Log.leaveParser("</empty-stmt");
        return emptyStatm;
    }

    @Override void printTree() {
        Log.wTreeLn(";");
    }
}
	

/*
 * A <for-statm>.
 */
class ForStatm extends Statement {
    ForControl forControl;
    StatmList statmList;

    @Override
    void check(DeclList curDecls) {
        forControl.check(curDecls);
        statmList.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        if (forControl.firstAssignment != null){
            forControl.firstAssignment.genCode(curFunc);
        }
        String testLabel = Code.getLocalLabel(),
                endLabel = Code.getLocalLabel();

        Code.genInstr(testLabel, "", "", "Start for-statement");
        forControl.expression.genCode(curFunc);
        forControl.expression.valType.genJumpIfZero(endLabel);

        statmList.genCode(curFunc);

        if (forControl.secondAssignment != null){
            forControl.secondAssignment.genCode(curFunc);
        }
        Code.genInstr("", "jmp", testLabel, "");
        Code.genInstr(endLabel, "", "", "End for-statement");
    }

    @Override
    void printTree() {
        Log.wTreeLn();
        Log.wTree("for(");
        forControl.printTree();
        Log.wTreeLn("){");
        Log.indentTree();
        statmList.printTree();
        Log.outdentTree();
        Log.wTreeLn("}");
    }

    static ForStatm parse(){
        Log.enterParser("<for-statm>");

        ForStatm forStatm = new ForStatm();
        Scanner.skip(forToken);
        Scanner.skip(leftParToken);
        forStatm.forControl = ForControl.parse();
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        forStatm.statmList = StatmList.parse();
        Scanner.skip(rightCurlToken);
        Log.leaveParser("</for-statm>");

        return forStatm;
    }
}


class ForControl extends SyntaxUnit{
    Assignment firstAssignment = null;
    Assignment secondAssignment = null;
    Expression expression;

    void check(DeclList curDecls) {
        if(firstAssignment != null)
            firstAssignment.check(curDecls);

        expression.check(curDecls);

        if(secondAssignment != null)
            secondAssignment.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {

    }

    static ForControl parse(){
        ForControl forControl = new ForControl();

        if(Scanner.nextToken == assignToken)
            forControl.firstAssignment = Assignment.parse();

        Scanner.skip(semicolonToken);
        forControl.expression = Expression.parse();
        Scanner.skip(semicolonToken);

        if(Scanner.nextToken == assignToken)
            forControl.secondAssignment = Assignment.parse();

        return forControl;
    }

    void printTree(){
        if(firstAssignment != null)
            firstAssignment.printTree();
        Log.wTree(";");
        expression.printTree();
        Log.wTree(";");

        if(secondAssignment != null)
            secondAssignment.printTree();
    }
}

/*
 * An <if-statm>.
 */
class IfStatm extends Statement {
    Expression test;
    StatmList body;
    ElsePart elsePart = null;

    @Override void check(DeclList curDecls) {
        test.check(curDecls);
        body.check(curDecls);

        if(elsePart != null)
            elsePart.check(curDecls);
    }

    @Override void genCode(FuncDecl curFunc) {
        Code.genInstr("", "", "", "Start if-statement");
        test.genCode(curFunc);
        String label = Code.getLocalLabel();
        test.valType.genJumpIfZero(label);

        body.genCode(curFunc);

        if(elsePart != null){
            String label2 = Code.getLocalLabel();
            Code.genInstr("", "jmp", label2, "");
            Code.genInstr(label, "", "", "Else-part");
            elsePart.genCode(curFunc);
            Code.genInstr(label2, "", "", "End if-statement");
        }
        else{
            Code.genInstr(label, "", "", "End if-statement");
        }
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
        Log.wTree("if (");  test.printTree();  Log.wTreeLn(") {");
        Log.indentTree();
        body.printTree();
        Log.outdentTree();
        Log.wTreeLn("}");

        if(elsePart != null){
            elsePart.printTree();
        }
    }
}

class ElsePart extends Statement {
    StatmList body;

    static ElsePart parse(){
        Log.enterParser("<else-part>");

        ElsePart elsePart = new ElsePart();
        Scanner.skip(elseToken);
        Scanner.skip(leftCurlToken);
        elsePart.body = StatmList.parse();
        Scanner.skip(rightCurlToken);

        Log.leaveParser("</else-part>");
        return elsePart;
    }

    @Override
    void check(DeclList curDecls) {
        body.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        body.genCode(curFunc);
    }

    @Override
    void printTree() {
        Log.wTreeLn("else {");
        Log.indentTree();
        body.printTree();
        Log.outdentTree();
        Log.wTreeLn("}");
    }
}

/*
 * A <return-statm>.
 */
//-- Must be changed in part 2:
class ReturnStatm extends Statement {
    Expression expression;

    @Override
    void check(DeclList curDecls) {
        expression.check(curDecls);
        //expression.valType.checkType(lineNum, expression.valType, "Array index");
    }

    @Override
    void genCode(FuncDecl curFunc) {
        expression.valType.checkType(lineNum, curFunc.type, "Return value");    // checks return type is correct
        expression.genCode(curFunc);
        Code.genInstr("", "jmp", ".exit$" + curFunc.name, "");
    }

    @Override
    void printTree() {
        Log.wTree("return ");
        expression.printTree();
        Log.wTreeLn(";");
    }

    static ReturnStatm parse(){
        Log.enterParser("<return-stmt");
        ReturnStatm returnStatement = new ReturnStatm();
        Scanner.skip(returnToken);
        returnStatement.expression = Expression.parse();
        Scanner.skip(semicolonToken);
        Log.leaveParser("</return-statm>");

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



/*
 * An <expression list>.
 */

class ExprList extends SyntaxUnit {
    Expression firstExpr = null;
    Expression lastExpr = null;
    int expressions = 0;

    @Override void check(DeclList curDecls) {
        Expression iter = firstExpr;

        while(iter != null){
            iter.check(curDecls);
            iter = iter.nextExpr;
        }
    }

    @Override void genCode(FuncDecl curFunc) {
        Expression iter = lastExpr;
        int numParam = expressions;
        while(iter != null){
            iter.genCode(curFunc);

            if (iter.valType == Types.doubleType){
                Code.genInstr("", "subl", "$8, %esp", "");
                Code.genInstr("", "fstpl", "(%esp)", "Push parameter #" + numParam);
            }
            else{
                Code.genInstr("", "pushl", "%eax", "Push parameter #" + numParam);
            }
            numParam --;
            iter = iter.prevExpr;
        }
    }

    static ExprList parse() {
        Log.enterParser("<expr list>");

        ExprList exprList = new ExprList();

        if(Scanner.curToken != rightParToken){
            exprList.firstExpr = Expression.parse();
            exprList.lastExpr = exprList.firstExpr;

            exprList.expressions++;

            while(Scanner.curToken == commaToken){
                Scanner.skip(commaToken);
                Expression expression = Expression.parse();
                exprList.lastExpr.nextExpr = expression;     // assign pointer
                expression.prevExpr = exprList.lastExpr;
                exprList.lastExpr = expression;              // set new last expression
                exprList.expressions++;
            }
        }
        Log.leaveParser("</expr list>");
        return exprList;
    }

    @Override void printTree() {
        Expression iter = firstExpr;
        boolean addComma = false;

        while(iter != null){
            if(addComma)
                Log.wTree(",");

            iter.printTree();
            iter = iter.nextExpr;
            addComma = true;
        }
    }

    int dataSize(){
        int size = 0;

        Expression iter = firstExpr;
        while(iter!= null){
            size += iter.valType.size();
            iter = iter.nextExpr;
        }
        return size;
    }
}


/*
 * An <expression>
 */
class Expression extends Operand {
    Expression nextExpr = null;
    Expression prevExpr = null;
    Term firstTerm, secondTerm = null;
    Operator relOp = null;
    boolean innerExpr = false;

    @Override void check(DeclList curDecls) {
        firstTerm.check(curDecls);
        valType = firstTerm.type;

        if(relOp != null && secondTerm != null){
            relOp.check(curDecls);
            secondTerm.check(curDecls);

            valType.checkType(secondTerm.lineNum, secondTerm.type, "terms");
            relOp.opType = valType;
            valType = Types.intType;
        }
    }

    @Override void genCode(FuncDecl curFunc) {
        firstTerm.genCode(curFunc);

        if(relOp != null && secondTerm != null){

            if(firstTerm.type == Types.intType){
                Code.genInstr("", "pushl", "%eax", "");
            }
            else {
                Code.genInstr("", "subl", "$8, %esp", "");
                Code.genInstr("", "fstpl", "(%esp)", "");
            }
            secondTerm.genCode(curFunc);
            relOp.genCode(curFunc);
        }
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
        if(innerExpr)
            Log.wTree("(");

        firstTerm.printTree();

        if(relOp != null && secondTerm != null){
            relOp.printTree();
            secondTerm.printTree();
        }
        if(innerExpr)
            Log.wTree(")");

    }
}

class Factor extends Operator {
    Operand startOperand = null;
    FactorOperator firstFactorOper = null;
    Factor nextFactor = null;
    Type valType = null;

    @Override
    void genCode(FuncDecl curFunc) {
        startOperand.genCode(curFunc);
        Operand iter = startOperand.nextOperand;
        Operator iterOp = firstFactorOper;

        while(iter != null){
            if(valType == Types.intType){
                Code.genInstr("", "pushl", "%eax", "");
                iter.genCode(curFunc);
                Code.genInstr("", "movl", "%eax, %ecx", "");
                Code.genInstr("", "popl", "%eax", "");

                if(iterOp != null && iterOp.opToken == Token.divideToken){
                    Code.genInstr("", "cdq", "", "");
                    Code.genInstr("", "idivl", "%ecx", "Compute /");
                }
                else if(iterOp != null && iterOp.opToken == Token.multiplyToken){
                    Code.genInstr("", "imull", "%ecx, %eax", "Compute *");
                }
            }
            else if(valType == Types.doubleType){
                Code.genInstr("", "subl", "$8, %esp", "");
                Code.genInstr("", "fstpl", "(%esp)", "");
                iter.genCode(curFunc);
                Code.genInstr("", "fldl", "(%esp)", "");
                Code.genInstr("", "addl", "$8, %esp", "");

                if(iterOp != null && iterOp.opToken == Token.divideToken){
                    Code.genInstr("", "fdivp", "", "Compute /");
                }
                else if(iterOp != null && iterOp.opToken == Token.multiplyToken){
                    Code.genInstr("", "fmulp", "", "Compute *");
                }
            }
            iterOp.genCode(curFunc);
            iter = iter.nextOperand;
            iterOp = iterOp.nextOp;
        }
    }

    @Override
    void check(DeclList curDecls){
        startOperand.check(curDecls);

        Operand operandIter = startOperand;
        Operand previousOperand = null;
        FactorOperator factorOperIter = firstFactorOper;

        while(factorOperIter != null){
            previousOperand = operandIter;
            factorOperIter.check(curDecls);
            operandIter = operandIter.nextOperand;
            operandIter.check(curDecls);

            operandIter.valType.checkSameType(lineNum, previousOperand.valType,
                    "Operands");

            factorOperIter = (FactorOperator) factorOperIter.nextOp;
        }
        valType = startOperand.valType;
    }

    static Factor parse(){
        Log.enterParser("<factor>");

        Factor factor = new Factor();
        factor.startOperand = Operand.parse();    // goes to next

        Operand lastOperand = factor.startOperand;
        FactorOperator lastFactorOperator = null;

        while(Token.isFactorOperator(Scanner.curToken)){
            FactorOperator tmpFactor = FactorOperator.parse();

            if(factor.firstFactorOper == null){
                factor.firstFactorOper = tmpFactor;
            }

            Operand tmpOperand = Operand.parse();

            lastOperand.nextOperand = tmpOperand;     // sett pointer to next
            lastOperand = tmpOperand;

            if(lastFactorOperator != null){
                lastFactorOperator.nextOp = tmpFactor;
            }

            lastFactorOperator = tmpFactor;
        }
        Log.leaveParser("</factor>");
        return factor;
    }

    @Override
    void printTree() {
        startOperand.printTree();

        Operand operandIter = startOperand;
        FactorOperator factorOperIter = firstFactorOper;

        while(factorOperIter != null){
            factorOperIter.printTree();
            operandIter = operandIter.nextOperand;
            operandIter.printTree();
            factorOperIter = (FactorOperator) factorOperIter.nextOp;
        }
    }
};

class FactorOperator extends  Operator {
    @Override
    void genCode(FuncDecl curFunc) {

    }

    @Override
    void printTree() {
        if(opToken == multiplyToken)
            Log.wTree(" * ");
        else
            Log.wTree(" / ");
    }

    static FactorOperator parse(){
        Log.enterParser("<factor-operator>");

        FactorOperator factorOperator = new FactorOperator();
        factorOperator.opToken = Scanner.curToken;
        Scanner.readNext();     // skip it

        Log.leaveParser("</factor-operator");
        return factorOperator;
    }
}


/*
 * A <term>
 */
class Term extends SyntaxUnit {
    //-- Must be changed in part 2:
    Factor firstFactor = null;
    Operator firstOperator = null;
    Type type;


    @Override void check(DeclList curDecls) {
        Factor iterFactor = firstFactor;
        Operator iterOperator = firstOperator;
        iterFactor.check(curDecls);
        type = iterFactor.valType;

        Factor prevFactor = iterFactor;

        while(iterOperator != null){
            iterFactor = iterFactor.nextFactor;
            iterOperator.check(curDecls);
            iterFactor.check(curDecls);

            iterFactor.valType.checkSameType(lineNum, prevFactor.valType,
                    "Operands");
            iterOperator = iterOperator.nextOp;
            prevFactor = iterFactor;
        }
    }

    @Override void genCode(FuncDecl curFunc) {
        firstFactor.genCode(curFunc);

        Factor iter = firstFactor.nextFactor;
        Operator iterOp = firstOperator;

        while(iter != null){
            if(type == Types.intType){
                Code.genInstr("", "pushl", "%eax", "");
                iter.genCode(curFunc);
                Code.genInstr("", "movl", "%eax, %ecx", "");
                Code.genInstr("", "popl", "%eax", "");

                if(iterOp != null && iterOp.opToken == Token.addToken){
                    Code.genInstr("", "addl", "%ecx, %eax", "Compute +");
                }
                else if(iterOp != null && iterOp.opToken == Token.subtractToken){
                    Code.genInstr("", "subl", "%ecx, %eax", "Compute -");
                }
            }
            else if(type == Types.doubleType){
                if(iterOp != null){
                    Code.genInstr("", "subl", "$8, %esp", "");
                    Code.genInstr("", "fstpl", "(%esp)", "");

                    iter.genCode(curFunc);
                    Code.genInstr("", "fldl", "(%esp)", "");
                    Code.genInstr("", "addl", "$8, %esp", "");

                    if(iterOp.opToken == Token.subtractToken)
                        Code.genInstr("", "fsubp", "", "Compute -");
                    else if(iterOp.opToken == Token.addToken){
                        Code.genInstr("", "faddp", "", "Compute +");
                    }
                }
            }
            iterOp.genCode(curFunc);
            iter = iter.nextFactor;
            iterOp = iterOp.nextOp;
        }
    }

    static Term parse() {
        Log.enterParser("<term>");
        Term term = new Term();
        term.firstFactor = Factor.parse();

        Factor lastFactor = term.firstFactor;
        Operator lastOperator = null;

        while(Token.isTermOperator(Scanner.curToken)){
            TermOperator termOper = TermOperator.parse();

            Factor factor = Factor.parse();
            lastFactor.nextFactor = factor;
            lastFactor = factor;

            if(term.firstOperator == null)
                term.firstOperator = termOper;

            if(lastOperator != null){
                lastOperator.nextOp = termOper;
            }
            lastOperator = termOper;
        }
        Log.leaveParser("</term>");
        return term;
    }

    @Override void printTree() {
        Factor iterFactor = firstFactor;
        Operator iterOperator = firstOperator;

        iterFactor.printTree();

        while(iterOperator != null){
            iterFactor = iterFactor.nextFactor;
            iterOperator.printTree();
            iterFactor.printTree();
            iterOperator = iterOperator.nextOp;
        }
    }
}


/*
 * An <operator>
 */
abstract class Operator extends SyntaxUnit {
    Operator nextOp = null;
    Type opType;
    Token opToken;

    @Override void check(DeclList curDecls) {
        // What to do here?
    }
}

class TermOperator extends Operator {
    @Override
    void genCode(FuncDecl curFunc) {

    }

    @Override
    void printTree() {
        if(opToken == addToken)
            Log.wTree(" + ");
        else
            Log.wTree(" - ");
    }

    static TermOperator parse(){
        Log.enterParser("<term operator>");

        TermOperator termOperator = new TermOperator();
        if(Token.isTermOperator(Scanner.curToken))
            termOperator.opToken = Scanner.curToken;
        Scanner.readNext();

        Log.leaveParser("</term operator>");
        return termOperator;
    }
}


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
                Code.genInstr("", "setle", "%al", "Test <=");
//                Code.genInstr("", "cmpl", "$0, %eax", "");  // had to add this for it to work
                break;
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
    String name;
    boolean shouldRemoveReturnValue = false;
    ExprList exprList;
    FuncDecl funcDecl = null;

    @Override void check(DeclList curDecls) {
        funcDecl = (FuncDecl) curDecls.findDecl(name, this);
        funcDecl.checkWhetherFunction(exprList.expressions, this);
        valType = funcDecl.type;
        exprList.check(curDecls);
        Expression iter = exprList.firstExpr;
        Declaration d = funcDecl.paramDeclList.firstDecl;
        int numParam = 0;

        while(iter != null){
            iter.valType.checkType(exprList.lineNum, d.type,
                    "Parameter num: " + Integer.toString(numParam));

            iter = iter.nextExpr;
            d = d.nextDecl;
            numParam++;
        }
    }

    @Override void genCode(FuncDecl curFunc) {
        exprList.genCode(curFunc);
        Code.genInstr("", "call", funcDecl.assemblerName, "Call " + funcDecl.assemblerName);

        int size = exprList.dataSize();
        if (size > 0){
            Code.genInstr("", "addl", "$" + size + ",%esp", "Remove parameters");
        }
        if (funcDecl.type == Types.doubleType && shouldRemoveReturnValue){
            Code.genInstr("", "fstps", Code.tmpLabel, "Remove return value");
        }
    }

    static FunctionCall parse() {
        Log.enterParser("<function call>");

        FunctionCall funcCall = new FunctionCall();
        funcCall.name = Scanner.curName;
        Scanner.skip(nameToken);
        Scanner.skip(leftParToken);
        funcCall.exprList = ExprList.parse();
        Scanner.skip(rightParToken);

        Log.leaveParser("</function call>");
        return funcCall;
    }

    @Override void printTree() {
        Log.wTree(name + "(");
        exprList.printTree();
        Log.wTree(")");
    }
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
        Log.enterParser("<number>");

        Number num = new Number();
        num.numVal = Scanner.curNum;
        num.valType = Types.getType(intToken);
        Scanner.skip(numberToken);

        Log.leaveParser("</number>");
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
        if(index == null){
            if(valType == Types.doubleType){
                Code.genInstr("", "fldl", declRef.assemblerName, varName);
            }
            else if(valType == Types.intType){
                Code.genInstr("", "movl", declRef.assemblerName + ", %eax", varName);
            }
        }
        else{
            index.genCode(curFunc);
            Code.genInstr("", "leal", declRef.assemblerName + ", %edx", declRef.name +"[...]");

            if(valType == Types.doubleType){
                Code.genInstr("", "fldl", "(%edx, %eax, 8)", varName);
            }
            else if(valType == Types.intType){
                Code.genInstr("", "movl", "(%edx, %eax, 4), %eax", varName);
            }

        }
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

        Log.leaveParser("</variable>");

        return var;
    }

    @Override void printTree() {
        Log.wTree(varName);

        if(index != null){
            Log.wTree("[");
            index.printTree();
            Log.wTree("]");
        }
    }
}

class AssignStatm extends Statement {
    Assignment assignment = null;

    @Override
    void check(DeclList curDecls) {
        assignment.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        assignment.genCode(curFunc);
    }

    @Override
    void printTree() {
        assignment.printTree();
        Log.wTreeLn(";");
    }

    static AssignStatm parse(){
        Log.enterParser("<assign-statm>");

        AssignStatm assignStatm = new AssignStatm();
        assignStatm.assignment = Assignment.parse();
        Scanner.skip(semicolonToken);

        Log.leaveParser("</assign-statm>");

        return assignStatm;
    }
}

class Assignment extends Statement {
    Variable variable;
    Expression expression;

    static Assignment parse(){
        Log.enterParser("<assignment>");

        Assignment assignment = new Assignment();
        assignment.variable = Variable.parse();
        Scanner.skip(assignToken);
        assignment.expression = Expression.parse();

        Log.leaveParser("</assignment>");
        return assignment;
    }

    @Override
    void check(DeclList curDecls) {
        variable.check(curDecls);
        expression.check(curDecls);

//        if(expression.valType != null)
//            variable.valType.checkType(lineNum, expression.valType, "type");
    }

    @Override
    void genCode(FuncDecl curFunc) {
        if(variable.index != null){
            // array.
            // Ved alle int-operander er innom %EAX

            variable.index.genCode(curFunc);
            Code.genInstr("", "pushl", "%eax", ""); // push index on stack
            expression.genCode(curFunc);

            Type type = ((ArrayType)variable.declRef.type).elemType;
            String varName = variable.declRef.name;
            String assemblerName = variable.declRef.assemblerName;

            if (type == Types.intType && expression.valType == Types.intType){
                Code.genInstr("", "leal", assemblerName + ", %edx", "");
                Code.genInstr("", "popl", "%ecx", "");
                Code.genInstr("", "movl", "%eax,(%edx,%ecx,4)", varName + "[..] = ");
            }
            else if (type == Types.doubleType && expression.valType == Types.doubleType){
                Code.genInstr("", "leal", assemblerName + ", %edx", "");
                Code.genInstr("", "popl", "%eax", "");
                Code.genInstr("", "fstpl", "(%edx, %eax, 8)", varName + "[..] = ");
            }
            else if(type == Types.doubleType && expression.valType == Types.intType){
                Code.genInstr("", "leal", assemblerName + ", %edx", "");
                Code.genInstr("", "popl", "%ecx", "");
                Code.genInstr("", "movl", "%eax, " + Code.tmpLabel, "");
                Code.genInstr("", "fildl", Code.tmpLabel, "     (double)");
                Code.genInstr("", "fstpl", "(%edx, %ecx, 8)", varName + "[..] = ");

            }
            else if(type == Types.intType && expression.valType == Types.doubleType){
                Code.genInstr("", "leal", assemblerName + ", %edx", "");
                Code.genInstr("", "popl", "%ecx", "");
                Code.genInstr("", "fistpl", "(%edx, %ecx, 4)", varName + "[..] = ");
            }
            else {
                Error.panic("Declaration.genStore");
            }
        }
        else {
            expression.genCode(curFunc);
            variable.declRef.genStore(expression.valType);
        }
    }

    @Override
    void printTree() {
        variable.printTree();
        Log.wTree(" = ");
        expression.printTree();
    }
}

class CallStatm extends Statement {
    FunctionCall functCall = null;

    @Override
    void check(DeclList curDecls) {
       functCall.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        functCall.shouldRemoveReturnValue = true;
        functCall.genCode(curFunc);
    }

    @Override
    void printTree() {
        functCall.printTree();
        Log.wTreeLn(";");
    }

    static CallStatm parse(){
        Log.enterParser("<call-statm>");

        CallStatm callStatm = new CallStatm();
        callStatm.functCall = FunctionCall.parse();
        Scanner.skip(semicolonToken);

        Log.leaveParser("</call-statm>");

        return callStatm;
    }
}