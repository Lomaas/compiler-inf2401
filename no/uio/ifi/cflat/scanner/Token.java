package no.uio.ifi.cflat.scanner;

/*
 * class Token
 */

/*
 * The different kinds of tokens read by Scanner.
 */
public enum Token {
    addToken,
    assignToken,
    commaToken,
    divideToken,
    doubleToken,
    elseToken,
    eofToken,
    equalToken,
    forToken,
    greaterEqualToken,
    greaterToken,
    ifToken,
    intToken,
    leftBracketToken,
    leftCurlToken,
    leftParToken,
    lessEqualToken,
    lessToken,
    multiplyToken,
    nameToken,
    notEqualToken,
    numberToken,
    rightBracketToken,
    rightCurlToken,
    rightParToken,
    returnToken,
    semicolonToken,
    subtractToken,
    whileToken;

    public static boolean isFactorOperator(Token t) {
        if(t == Token.multiplyToken || t == Token.divideToken)
            return true;

        return false;
    }

    public static boolean isTermOperator(Token t) {
        // TODO -- Must be changed in part 0:
        return false;
    }

    public static boolean isRelOperator(Token t) {
        // TODO -- Must be changed in part 0:
        return false;
    }

    public static boolean isOperand(Token t) {
       if(t == Token.subtractToken || t == Token.addToken)
           return true;

        return false;
    }

    public static boolean isTypeName(Token t) {

        return false;
    }
}
