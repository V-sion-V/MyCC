public class Parser {
    Tokenizer in = new Tokenizer();
    Token token;
    String out = "";

    public static void main(String[] args) {
        Parser parser = new Parser();
        parser.token = parser.in.nextToken();
        parser.compUnit();
        System.out.println(parser.out);
        System.exit(0);
    }

    void compUnit() {
        out += "define ";
        if (token.type == Token.INT) {
            funcDef();
        } else {
            System.exit(1);
        }
    }

    void funcDef() {
        out += "dso_local ";
        if (token.type == Token.INT) {
            funcType();
            if (token.type == Token.IDT) {
                ident();
                if (token.type == Token.LP) {
                    out += "(";
                    token = in.nextToken();
                    if (token.type == Token.RP) {
                        out += ") ";
                        token = in.nextToken();
                        block();
                    } else {
                        System.exit(1);
                    }
                } else {
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }
        } else {
            System.exit(1);
        }
    }

    void funcType() {
        if (token.type == Token.INT) {
            out += "i32 ";
            token = in.nextToken();
        } else {
            System.exit(1);
        }
    }

    void ident() {
        if (token.type == Token.IDT && token.content.equals("main")) {
            out += "@main ";
            token = in.nextToken();
        } else {
            System.exit(1);
        }
    }

    void block() {
        if (token.type == Token.LB) {
            out += "{\n";
            token = in.nextToken();
            if (token.type == Token.RETURN) {
                stmt();
                if (token.type == Token.RB) {
                    out += "}\n";
                    token = in.nextToken();
                } else {
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }
        } else {
            System.exit(1);
        }
    }

    void stmt() {
        if (token.type == Token.RETURN) {
            out += "ret ";
            token = in.nextToken();
            if (token.type == Token.NUM) {
                out += "i32 " + token.content;
                token = in.nextToken();
                if (token.type == Token.SEMI) {
                    out += "\n";
                    token = in.nextToken();
                } else {
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }
        } else {
            System.exit(1);
        }
    }
}