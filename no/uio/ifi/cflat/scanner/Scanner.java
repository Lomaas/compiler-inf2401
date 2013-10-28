package no.uio.ifi.cflat.scanner;

/*
 * module Scanner
 */

import no.uio.ifi.cflat.chargenerator.CharGenerator;
import no.uio.ifi.cflat.error.Error;
import no.uio.ifi.cflat.log.Log;
import static no.uio.ifi.cflat.scanner.Token.*;

/**
 *
 - naming convention! Example for del1: submit a file named “sjo049-del1.zip”. In there, have a directory called “sjo049-del1”, in which you have all your stuff.

 - build.xml file missing

 - I can’t build your compiler on an ifilab machine because there is some problem with the charset you are using in your source files. I tried a bit, but could not figure out how to convert your files to something the java compiler would take (using the iconv command line tool). It is a requirement that your submissions work on an ifilab machine.

 - can you handle negative numbers? (couldn’t check because of crash)


 */

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
                System.out.println(CharGenerator.curC);

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
                        CharGenerator.readNext();
                        System.out.println(CharGenerator.curC);
                        nextNextNum = (int)CharGenerator.curC;
                        nextNextToken = Token.numberToken;
                        CharGenerator.readNext();
                        System.out.println(CharGenerator.curC);
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
        System.out.print(c);
        if(c == 1 ||c == 2 || c == 3 || c == 4 || c == 5 || c == 6 || c == 7 || c == 8 || c == 9
            || c == -1 || c == -4 || c == -7|| c == -2 || c == -5 || c == -8 || c == -3 || c == -6 || c == -9)
        {
            System.out.println(c);
            return true;
        }
//        if(Character.isDigit(CharGenerator.curC))
//            return true;

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


    public static Boolean isMainToken(String word){
        if(word.equals("main")){
            return true;
        }
        return false;
    }

    public static String readNextCharacters(){
        word += CharGenerator.curC;

        while(CharGenerator.nextC != ' ' && !isEndOfWord(CharGenerator.nextC)){
            word += CharGenerator.nextC;
            CharGenerator.readNext();
        }
        return word;
    }

    public static boolean isEndOfWord(char c){
        switch(c)
        {
            case '(':
                return true;
            default:
                return false;

        }
    }
}
