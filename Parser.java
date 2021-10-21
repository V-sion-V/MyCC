public class Parser {
    Tokenizer in = new Tokenizer();
    Token token;
    String out = "";

    public static void main(String[] args) {
        Parser parser = new Parser();
        parser.token = parser.in.nextToken();
        SyntaxTree tree = parser.compUnit();
        System.out.println(parser.out);
        System.exit(0);
    }

    public SyntaxTree compUnit() { //
        out += "define ";
        SyntaxTree ret = new SyntaxTree(SyntaxTree.CompUnit);
        if (token.type == Token.INT) {
            ret.push(funcDef());
        } else {
            System.exit(1);
        }
        return ret;
    }

    public SyntaxTree funcDef() { //
        out += "dso_local ";
        SyntaxTree ret = new SyntaxTree(SyntaxTree.FuncDef);
        ret.push(funcType());
        if (token.type == Token.IDT && token.content.equals("main")) {
            out += "@main ";
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            if (token.type == Token.LP) {
                out += "(";
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
                if (token.type == Token.RP) {
                    out += ") ";
                    ret.push(new SyntaxTree(token));
                    token = in.nextToken();
                    ret.push(block());
                } else {
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }
        } else {
            System.exit(1);
        }
        return ret;
    }

    public SyntaxTree funcType() { //
        SyntaxTree ret = new SyntaxTree(SyntaxTree.FuncType);
        if (token.type == Token.INT) {
            out += "i32 ";
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
        } else {
            System.exit(1);
        }
        return ret;
    }

    public SyntaxTree block() { //
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Block);
        if (token.type == Token.LB) {
            out += "{\n";
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            ret.push(stmt());

            if (token.type == Token.RB) {
                out += "}\n";
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
            } else {
                System.exit(1);
            }
        } else {
            System.exit(1);
        }
        return ret;
    }

    public SyntaxTree stmt() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Stmt);
        SyntaxTree temp;
        if (token.type == Token.RETURN) {
            out += "ret ";
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            temp = exp();
            ret.push(temp);
            //calc
            out += "i32 "+temp.value +" ";
            //
            if (token.type == Token.SEMI) {
                out += "\n";
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
            } else {
                System.exit(1);
            }
        } else {
            System.exit(1);
        }
        return ret;
    }

    SyntaxTree exp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Exp);
        SyntaxTree temp;
        temp = addExp();

        //calc
        ret.value = temp.value;
        //
        ret.push(temp);
        return ret;
    }

    SyntaxTree addExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.AddExp);
        SyntaxTree temp;
        temp = mulExp();
        //calc
        ret.value = temp.value;
        //
        ret.push(temp);
        while(token.type == Token.PLUS || token.type == Token.MINUS) {
            ret.push(new SyntaxTree(token));
            //calc
            if(token.type == Token.PLUS) {
                //
                token = in.nextToken();
                temp = mulExp();
                //calc
                ret.value += temp.value;
            } else {
                //
                token = in.nextToken();
                temp = mulExp();
                //calc
                ret.value -= temp.value;
            }
            //
            ret.push(temp);
        }
        return ret;
    }

    SyntaxTree mulExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.MulExp);
        SyntaxTree temp;
        temp = unaryExp();
        //calc
        ret.value = temp.value;
        //
        ret.push(temp);
        while(token.type == Token.MULT || token.type == Token.DIV || token.type == Token.MODE) {
            ret.push(new SyntaxTree(token));
            //calc
            if(token.type == Token.MULT) {
                //
                token = in.nextToken();
                temp = unaryExp();
                //calc
                ret.value *= temp.value;
            } else if(token.type == Token.DIV) {
                //
                token = in.nextToken();
                temp = unaryExp();
                //calc
                ret.value /= temp.value;
            } else {
                //
                token = in.nextToken();
                temp = unaryExp();
                //calc
                ret.value %= temp.value;
            }
            //
            ret.push(temp);
        }
        return ret;
    }

    SyntaxTree unaryExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.UnaryExp);
        while(token.type == Token.PLUS || token.type == Token.MINUS) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
        }
        ret.push(primaryExp());
        //calc
        for(SyntaxTree i : ret.getChild()) ret.value *= i.value;
        //
        return ret;
    }

    SyntaxTree primaryExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.PrimaryExp);
        if(token.type == Token.NUM) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            //cal
            ret.value = ret.getChild().get(0).value;
            //
        } else if (token.type == Token.LP) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            ret.push(exp());
            //cal
            ret.value = ret.getChild().get(1).value;
            //
            if(token.type == Token.RP) {
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
            } else {
                System.exit(1);
            }
        } else {
            System.exit(1);
        }

        return ret;
    }
}
