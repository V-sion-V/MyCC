public class Token {
    static final int  ERR = -2, EOF = -1, IDT = 0, NUM = 1;
    static final int  CONST = 2, VOID = 3, INT = 4, IF = 5, ELSE = 6, WHILE = 7, BREAK = 8, CONTINUE = 9, RETURN = 10;
    static final int  LE = 32, GE = 33, EQ = 34, NE = 35, AND = 36, OR = 37;
    static final int  ASSIGN = 64, SEMI = 65, COMMA = 66, LP = 67, RP = 68, LC = 69, RC = 70, LB = 71, RB = 72,
            PLUS = 73, MINUS = 74, MULT = 75, DIV = 76, MODE = 77, NOT = 78, LT = 79, GT = 80;
    final int type;
    final String content;

    Token(int type) {
        this(type,"");
    }

    Token(int type,String content) {
        this.type = type;
        this.content = content;
    }
}
