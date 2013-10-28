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

    // multiply og divide
    public static boolean isFactorOperator(Token t) {
        if(t == Token.multiplyToken || t == Token.divideToken)
            return true;

        return false;
    }
    // + og -
    public static boolean isTermOperator(Token t) {
        if(t == Token.subtractToken || t == Token.addToken)
            return true;

        return false;
    }

    // greater, leseser notEqaul
    public static boolean isRelOperator(Token t) {
        boolean isRelOp = false;
        isRelOp |= t == greaterEqualToken;
        isRelOp |= t == greaterToken;
        isRelOp |= t == lessToken;
        isRelOp |= t == lessEqualToken;
        isRelOp |= t == notEqualToken;
        isRelOp |= t == equalToken;

        return isRelOp;
    }

    // Number, name
    public static boolean isOperand(Token t) {
        if(t == Token.numberToken || t == Token.nameToken)
            return true;

        return false;
    }


    public static boolean isTypeName(Token t) {
        boolean isRelOp = false;
        isRelOp |= t == doubleToken;
        isRelOp |= t == intToken;
        return isRelOp;
    }
}
