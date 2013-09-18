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

    /**
     * CurToken, NextToken og NextNextToken disse variablene er av klassen Token. curToken er det aktuelle symbolet vi skal analysere,
     * mens nextToken og nextNextToken er de to etterfølgende symbolene. Variablene curLine, nextLine og nextNextLine vil inneholde
     * linjenummeret for de tilsvarende symbolene. Om curToken er et nameToken, vil curName inneholde det aktuelle navnet,
     * og om det er et numberToken, vil curNum inneholde tallverdien. Det tilsvarende gjelder for nextName og nextNum og
     * for nextNextName og nextNextNum.
     * Metoden readNext vil plassere de neste symbolene i curToken, nextToken og nextNextToken. Alle **-kommentarer vil bli oversett.
     * Når det ikke er flere symboler igjen på filen, vil curToken-variabelen få verdien eofToken.
     */

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
                        if(CharGenerator.nextC == '=') nextNextToken = Token.equalToken;
                        else nextNextToken = Token.assignToken;

                        break;
                    case '>':
                        if(CharGenerator.nextC == '=')  nextNextToken = Token.greaterEqualToken;
                        else    nextNextToken = Token.greaterToken;

                        break;
                    case '<':
                        if(CharGenerator.nextC == '=')  nextNextToken = Token.lessEqualToken;
                        else    nextNextToken = Token.lessToken;
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
                        if(checkIsDigit())
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

    private static boolean isLetterAZ(char c) {
        if(Character.isLetter(c) && c != 'æ' && c != 'ø' && c != 'å')
            return true;
        return false;
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

    public static boolean checkIsDigit(){

        if(Character.isDigit(CharGenerator.curC))
            return true;

        return false;
    }

    public static void createNumberToken(){
        word+= CharGenerator.curC;

        while(CharGenerator.nextC != ' ' && Character.isDigit(CharGenerator.nextC)){
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
        if(word.equals("int")){
            // TODO Check for array
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
            // TODO Check for array
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


    public static Boolean isMainToken(String word){
        if(word.equals("main")){
            return true;
        }
        return false;
    }

    public static String readNextCharacters(){
        word += CharGenerator.curC;

        while(CharGenerator.nextC != ' ' && (Character.isDigit(CharGenerator.nextC) || Character.isLetter(CharGenerator.nextC))){
            word += CharGenerator.nextC;
            CharGenerator.readNext();

        }
        return word;
    }
}
