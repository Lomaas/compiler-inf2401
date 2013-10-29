package no.uio.ifi.cflat.scanner;

/*
 * module Scanner
 */

import no.uio.ifi.cflat.chargenerator.CharGenerator;
import no.uio.ifi.cflat.error.Error;
import no.uio.ifi.cflat.log.Log;
import static no.uio.ifi.cflat.scanner.Token.*;



/*
 * Module for forming characters into tokens.
 */
public class Scanner {
    public static Token curToken, nextToken, nextNextToken;
    public static String curName, nextName, nextNextName;
    public static int curNum, nextNum, nextNextNum;
    public static int curLine, nextLine, nextNextLine;
    public static String word = "";

    public static void init() {

    }

    public static void finish() {

    }

    public static void readNext() {
        curToken = nextToken;
        curName = nextName;
        nextToken = nextNextToken;
        nextName = nextNextName;
        curNum = nextNum;
        nextNum = nextNextNum;
        curLine = nextLine;
        nextLine = nextNextLine;

        nextNextToken = null;
        while (nextNextToken == null) {
            nextNextLine = CharGenerator.curLineNum();
            nextNextName = "";
            nextNextNum = 0;

            if (! CharGenerator.isMoreToRead()) {
                nextNextToken = eofToken;
            } else {
                CharGenerator.readNext();

                switch(CharGenerator.curC){
                    case ' ' :
                        break;
                    case '(':
                        nextNextToken = Token.leftParToken;
                        break;
                    case ')':
                        nextNextToken = Token.rightParToken;
                        break;
                    case '{':
                        nextNextToken = Token.leftCurlToken;
                        break;
                    case '}':
                        nextNextToken = Token.rightCurlToken;
                        break;
                    case ';':
                        nextNextToken = Token.semicolonToken;
                        break;
                    case '=':
                        if(CharGenerator.nextC == '='){
                            nextNextToken = Token.equalToken;
                            CharGenerator.readNext();
                        }
                        else {
                            nextNextToken = Token.assignToken;
                        }
                        break;
                    case '>':
                        if(CharGenerator.nextC == '='){
                            nextNextToken = Token.greaterEqualToken;
                            CharGenerator.readNext();
                        }
                        else
                            nextNextToken = Token.greaterToken;

                        break;
                    case '<':
                        if(CharGenerator.nextC == '='){
                            nextNextToken = Token.lessEqualToken;
                            CharGenerator.readNext();
                        }
                        else
                            nextNextToken = Token.lessToken;
                        break;
                    case '+':
                        nextNextToken = Token.addToken;
                        break;
                    case '*':
                        nextNextToken = Token.multiplyToken;
                        break;
                    case ',':
                        nextNextToken = Token.commaToken;
                        break;
                    case '-':
                        if(checkIsDigit(CharGenerator.nextC)){
                            createNumberToken();
                        }
                        else
                            nextNextToken = Token.subtractToken;
                        break;
                    case '/':
                        if(CharGenerator.nextC == '*')
                            skipUntilFinishCommentTag();
                        else
                            nextNextToken = Token.divideToken;
                        break;
                    case '[':
                        nextNextToken = Token.leftBracketToken;
                        break;
                    case '’':
                        CharGenerator.readNext();
                        nextNextNum = (int)CharGenerator.curC;
                        CharGenerator.readNext();
                        nextNextToken = Token.numberToken;
                        break;
                    case '\'':
                        System.out.println("is ' char");
                        CharGenerator.readNext();
                        nextNextNum = (int)CharGenerator.curC;
                        nextNextToken = Token.numberToken;
                        CharGenerator.readNext();
                        break;
                    case ']':
                        nextNextToken = Token.rightBracketToken;
                        break;
                    case '!':
                        if(CharGenerator.nextC == '='){
                            nextNextToken = Token.notEqualToken;
                            CharGenerator.readNext();
                        }
                        else
                            Error.error(nextNextLine, "Illegal symbol" + CharGenerator.nextC + "!");
                        break;
                    default : {
                        // NameToken or NumberToken
                        if(checkIsDigit(CharGenerator.curC))
                            createNumberToken();
                        else {
                            readNextCharacters();
                            Scanner.evaluateWord();
                        }
                        word = "";
                        break;
                    }
                }
            }
        }
        Log.noteToken();
    }

    public static void check(Token t) {
        if (curToken != t)
            Error.expected("A " + t);
    }

    public static void check(Token t1, Token t2) {
        if (curToken != t1 && curToken != t2)
            Error.expected("A " + t1 + " or a " + t2);
    }

    public static void skip(Token t) {
        check(t);  readNext();
    }

    public static void skip(Token t1, Token t2) {
        check(t1,t2);  readNext();
    }

    public static boolean checkIsDigit(char k){
        int c = (int) k;
        //System.out.print(c);

        if(c >= 48 && c <= 57)
            return true;

        return false;
    }

    public static void createNumberToken(){
        word+= CharGenerator.curC;

        while(CharGenerator.nextC != ' ' && checkIsDigit(CharGenerator.nextC)){
            CharGenerator.readNext();
            word+= CharGenerator.curC;
        }
        nextNextNum = Integer.parseInt(word);
        nextNextToken = Token.numberToken;
    }

    public static void skipUntilFinishCommentTag(){
        CharGenerator.readNext();
        CharGenerator.readNext();

        while(CharGenerator.curC != '*' && CharGenerator.nextC != '/')
            CharGenerator.readNext();

        CharGenerator.readNext();   // curC will be '/'
    }

    public static void evaluateWord(){
        System.out.println(word);

        if(word.equals("int")){
            nextNextToken = Token.intToken;
        }
        else if(word.equals("while")){
            nextNextToken = Token.whileToken;
        }
        else if(word.equals("main")){
            nextNextToken = Token.nameToken;
            nextNextName = "main";
        }
        else if(word.equals("for")){
            nextNextToken = Token.forToken;
        }
        else if(word.equals("if")){
            nextNextToken = Token.ifToken;
        }
        else if(word.equals("double")){
            nextNextToken = Token.doubleToken;
        }
        else if(word.equals("return")){
            nextNextToken = Token.returnToken;
        }
        else if(word.equals("else")){
            nextNextToken = Token.elseToken;
        }
        else {
            // Nametoken
            nextNextToken = Token.nameToken;
            nextNextName = word;
        }
    }

    public static String readNextCharacters(){
        word += CharGenerator.curC;

        while(!isEndOfWord(CharGenerator.nextC)){
            word += CharGenerator.nextC;
            CharGenerator.readNext();
        }
        System.out.println(word);

        return word;
    }

    // Checks ASCII values
    public static boolean isEndOfWord(char c){
        int cast = (int) c;
        //System.out.println(cast);

        // A - Z
        if(cast >= 65 && cast <= 90)
            return false;
        // a-z
        if(cast >= 97 && cast <= 122)
            return false;

        // Numbers
        if(cast >= 48 && cast <= 57)
            return false;

        // underscore
        if(cast == 95)
            return false;

        return true;
    }
}
