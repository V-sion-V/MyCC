public class Parser {
    private final Lexer in = new Lexer();
    private Token token;

    private void err() {
        System.out.println(token.type);
        System.exit(5);
    }

    public Parser(){
        token = in.nextToken();
    }

    public SyntaxTree getSyntaxTree () {
        SyntaxTree temp = compUnit();
        if(token.type!=Token.EOF) err();
        return temp;
    }

    private SyntaxTree compUnit() { //
        SyntaxTree ret = new SyntaxTree(SyntaxTree.CompUnit);
        if (token.type == Token.INT || token.type == Token.VOID) {
            ret.push(funcDef());
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree funcDef() { //
        SyntaxTree ret = new SyntaxTree(SyntaxTree.FuncDef);
        ret.push(funcType());

        if (token.type == Token.IDT) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            if (token.type == Token.LP) {
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
                if (token.type == Token.RP) {
                    ret.push(new SyntaxTree(token));
                    token = in.nextToken();
                    ret.push(block());
                } else {
                    err();
                }
            } else {
                err();
            }
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree funcType() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.FuncType);
        if (token.type == Token.INT||token.type == Token.VOID) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
        } else{
            err();
        }
        return ret;
    }

    private SyntaxTree block() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Block);
        if (token.type == Token.LB) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            while(token.type==Token.IDT || token.type==Token.INT || token.type==Token.CONST
                    || token.type==Token.NOT || token.type==Token.MINUS || token.type == Token.PLUS
                    || token.type==Token.LP || token.type==Token.NUM
                    || token.type==Token.SEMI || token.type == Token.RETURN) {

                ret.push(blockItem());
            }
            if (token.type == Token.RB) {
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
            } else {
                err();
            }
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree blockItem () {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.BlockItem);
        if(token.type==Token.IDT ||token.type==Token.RETURN || token.type==Token.MINUS
                || token.type == Token.PLUS || token.type==Token.LP || token.type==Token.NUM 
                || token.type==Token.SEMI || token.type == Token.NOT) {
            ret.push(stmt());
        } else if(token.type==Token.INT || token.type==Token.CONST) {
            ret.push(decl());
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree decl() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Decl);
        if(token.type==Token.INT) {
            ret.push(varDecl());
        } else if(token.type==Token.CONST) {
            ret.push(constDecl());
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree constDecl() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.ConstDecl);
        if(token.type==Token.CONST) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            ret.push(bType());
            ret.push(constDef());
            while(token.type==Token.COMMA) {
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
                ret.push(constDef());
            }
            if(token.type==Token.SEMI) {
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
            } else {
                err();
            }
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree constDef() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.ConstDef);
        if(token.type==Token.IDT) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            if(token.type==Token.ASSIGN) {
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
                ret.push(constInitVal());
            } else {
                err();
            }
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree constInitVal() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.ConstInitVal);
        ret.push(constExp());
        return ret;
    }

    private SyntaxTree varDecl() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.VarDecl);
        ret.push(bType());
        ret.push(varDef());
        while(token.type==Token.COMMA) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            ret.push(varDef());
        }
        if(token.type==Token.SEMI) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree varDef() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.VarDef);
        if(token.type==Token.IDT) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            if(token.type==Token.ASSIGN) {
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
                ret.push(initVal());
            }
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree initVal() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.InitVal);
        ret.push(exp());
        return ret;
    }

    private SyntaxTree bType() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.BType);
        if(token.type==Token.INT) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree stmt() { //
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Stmt);

        if (token.type == Token.RETURN) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            SyntaxTree temp = exp();

            //calc
            //out += "i32 "+CalculateExpValue.getInstance().getNumberValue(temp) +" ";
            //

            ret.push(temp);
        } else if(token.type==Token.IDT){
            ret.push(lVal());
            if(token.type==Token.ASSIGN) {
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
                ret.push(exp());
            } else {
                err();
            }
        } else if(token.type!=Token.SEMI){
            ret.push(exp());
        }

        if(token.type==Token.SEMI) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree lVal() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.LVal);
        if(token.type==Token.IDT) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
        } else {
            err();
        }
        return ret;
    }

    private SyntaxTree constExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.ConstExp);
        ret.push(addExp());
        return ret;
    }

    private SyntaxTree exp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Exp);
        ret.push(addExp());
        return ret;
    }

    private SyntaxTree addExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.AddExp);
        ret.push(mulExp());
        while(token.type == Token.PLUS || token.type == Token.MINUS) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            ret.push(mulExp());
        }
        return ret;
    }

    private SyntaxTree mulExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.MulExp);
        ret.push(unaryExp());
        while(token.type == Token.MULT || token.type == Token.DIV || token.type == Token.MODE) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            ret.push(unaryExp());
        }
        return ret;
    }

    private SyntaxTree unaryExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.UnaryExp);
        while(token.type == Token.PLUS || token.type == Token.MINUS) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
        }
        ret.push(primaryExp());
        return ret;
    }

    private SyntaxTree primaryExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.PrimaryExp);
        if(token.type == Token.NUM) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
        } else if (token.type == Token.LP) {
            ret.push(new SyntaxTree(token));
            token = in.nextToken();
            ret.push(exp());
            if(token.type == Token.RP) {
                ret.push(new SyntaxTree(token));
                token = in.nextToken();
            } else {
                err();
            }
        } else if(token.type == Token.IDT) {
            ret.push(lVal());
        } else {
            err();
        }
        return ret;
    }
}
