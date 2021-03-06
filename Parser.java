public class Parser {
    private final Lexer in = new Lexer();
    private Token token;

    private void err() {
        System.out.println("err:"+token.type);
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
        while(token.type != Token.EOF) {
            if (in.getFurtherToken(1).type == Token.LP) ret.push(funcDef());
            else ret.push(decl());
        }
        return ret;
    }

    private SyntaxTree funcDef() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.FuncDef);
        ret.push(funcType());
        if (token.type == Token.IDT) {
            eatAndMove(ret);
            if (token.type == Token.LP) {
                eatAndMove(ret);
                if(token.type != Token.RP) {
                    ret.push(funcFParams());
                }
                eatAndMove(ret);
                ret.push(block());
            } else err();
        } else err();
        return ret;
    }

    private SyntaxTree funcFParams() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.FuncFParams);
        ret.push(funcFParam());
        while(token.type==Token.COMMA) {
            eatAndMove(ret);
            ret.push(funcFParam());
        }
        return ret;
    }

    private SyntaxTree funcFParam() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.FuncFParam);
        ret.push(bType());
        if(token.type == Token.IDT) {
            eatAndMove(ret);
            if(token.type == Token.LC) {
                eatAndMove(ret);
                if(token.type == Token.RC) {
                    eatAndMove(ret);
                    while(token.type == Token.LC) {
                        eatAndMove(ret);
                        ret.push(exp());
                        if(token.type == Token.RC) {
                            eatAndMove(ret);
                        } else err();
                    }
                } else err();
            }
        } else err();
        return ret;
    }

    private SyntaxTree funcType() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.FuncType);
        if (token.type == Token.INT||token.type == Token.VOID) {
            eatAndMove(ret);
        } else err();
        return ret;
    }

    private SyntaxTree block() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Block);
        if (token.type == Token.LB) {
            eatAndMove(ret);
            while(token.type != Token.RB) {
                ret.push(blockItem());
            }
            eatAndMove(ret);
        } else err();
        return ret;
    }

    private SyntaxTree blockItem () {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.BlockItem);
        if(token.type==Token.INT || token.type==Token.CONST) ret.push(decl());
        else ret.push(stmt());
        return ret;
    }

    private SyntaxTree decl() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Decl);
        if(token.type==Token.INT) {
            ret.push(varDecl());
        } else if(token.type==Token.CONST) {
            ret.push(constDecl());
        } else err();
        return ret;
    }

    private SyntaxTree constDecl() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.ConstDecl);
        if(token.type==Token.CONST) {
            eatAndMove(ret);
            ret.push(bType());
            ret.push(constDef());
            while(token.type==Token.COMMA) {
                eatAndMove(ret);
                ret.push(constDef());
            }
            if(token.type==Token.SEMI) {
                eatAndMove(ret);
            } else err();
        } else err();
        return ret;
    }

    private SyntaxTree constDef() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.ConstDef);
        if(token.type==Token.IDT) {
            eatAndMove(ret);
            while(token.type == Token.LC) {
                eatAndMove(ret);
                ret.push(constExp());
                if(token.type == Token.RC) {
                    eatAndMove(ret);
                } else err();
            }
            if(token.type==Token.ASSIGN) {
                eatAndMove(ret);
                ret.push(constInitVal());
            } else err();
        } else err();
        return ret;
    }

    private SyntaxTree constInitVal() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.ConstInitVal);
        if(token.type == Token.LB) {
            eatAndMove(ret);
            if(token.type == Token.RB) {
                eatAndMove(ret);
            } else {
                ret.push(constInitVal());
                while(token.type == Token.COMMA) {
                    eatAndMove(ret);
                    ret.push(constInitVal());
                }
                if(token.type == Token.RB) {
                    eatAndMove(ret);
                } else err();
            }
        } else {
            ret.push(constExp());
        }
        return ret;
    }

    private SyntaxTree varDecl() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.VarDecl);
        ret.push(bType());
        ret.push(varDef());
        while(token.type==Token.COMMA) {
            eatAndMove(ret);
            ret.push(varDef());
        }
        if(token.type==Token.SEMI) {
            eatAndMove(ret);
        } else err();
        return ret;
    }

    private SyntaxTree varDef() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.VarDef);
        if(token.type==Token.IDT) {
            eatAndMove(ret);
            while(token.type == Token.LC) {
                eatAndMove(ret);
                ret.push(constExp());
                if(token.type == Token.RC) {
                    eatAndMove(ret);
                } else err();
            }
            if(token.type==Token.ASSIGN) {
                eatAndMove(ret);
                ret.push(initVal());
            }
        } else err();
        return ret;
    }

    private SyntaxTree initVal() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.InitVal);
        if(token.type == Token.LB) {
            eatAndMove(ret);
            if(token.type == Token.RB) {
                eatAndMove(ret);
            } else {
                ret.push(initVal());
                while(token.type == Token.COMMA) {
                    eatAndMove(ret);
                    ret.push(initVal());
                }
                if(token.type == Token.RB) {
                    eatAndMove(ret);
                } else err();
            }
        } else {
            ret.push(exp());
        }
        return ret;
    }

    private SyntaxTree bType() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.BType);
        if(token.type==Token.INT) {
            eatAndMove(ret);
        } else err();
        return ret;
    }

    private SyntaxTree stmt() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Stmt);
        if (token.type == Token.RETURN) {
            eatAndMove(ret);
            if(token.type != Token.SEMI) {
                ret.push(exp());
            }
            if(token.type == Token.SEMI) {
                eatAndMove(ret);
            } else err();
        } else if(token.type == Token.LB) {
            ret.push(block());
        } else if(token.type == Token.IF) {
            eatAndMove(ret);
            if(token.type == Token.LP) {
                eatAndMove(ret);
                ret.push(cond());
                if(token.type == Token.RP) {
                    eatAndMove(ret);
                    ret.push(stmt());
                    if(token.type == Token.ELSE) {
                        eatAndMove(ret);
                        ret.push(stmt());
                    }
                } else err();
            } else  err();
        } else if(token.type==Token.WHILE) {
            eatAndMove(ret);
            if(token.type == Token.LP) {
                eatAndMove(ret);
                ret.push(cond());
                if(token.type == Token.RP) {
                    eatAndMove(ret);
                    ret.push(stmt());
                } else err();
            } else err();
        } else if(token.type == Token.CONTINUE || token.type == Token.BREAK) {
            eatAndMove(ret);
            if(token.type == Token.SEMI) {
                eatAndMove(ret);
            } else err();
        } else if(token.type != Token.SEMI){
            SyntaxTree temp = exp();
            if(token.type == Token.SEMI) {
                ret.push(temp);
                eatAndMove(ret);
            } else if(token.type == Token.ASSIGN) {
                if(temp.getWidth() == 1 &&
                        temp.get(0).getWidth() == 1 &&
                        temp.get(0).get(0).getWidth() == 1 &&
                        temp.get(0).get(0).get(0).getWidth() == 1 &&
                        temp.get(0).get(0).get(0).get(0).getWidth() == 1 &&
                        temp.get(0).get(0).get(0).get(0).type == SyntaxTree.PrimaryExp) {
                    SyntaxTree lVal = temp.get(0).get(0).get(0).get(0).get(0);
                    if (lVal.type == SyntaxTree.LVal) {
                        ret.push(lVal);
                        if (token.type == Token.ASSIGN) {
                            eatAndMove(ret);
                            ret.push(exp());
                            if (token.type == Token.SEMI) {
                                eatAndMove(ret);
                            } else err();
                        } else err();
                    } else err();
                } else err();
            } else err();
        } else {
            eatAndMove(ret);
        }
        return ret;
    }

    private SyntaxTree lVal() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.LVal);
        if(token.type==Token.IDT) {
            eatAndMove(ret);
            while(token.type == Token.LC) {
                eatAndMove(ret);
                ret.push(exp());
                if(token.type == Token.RC) {
                    eatAndMove(ret);
                } else err();
            }
        } else err();
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
            eatAndMove(ret);
            ret.push(mulExp());
        }
        return ret;
    }

    private SyntaxTree mulExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.MulExp);
        ret.push(unaryExp());
        while(token.type == Token.MULT || token.type == Token.DIV || token.type == Token.MODE) {
            eatAndMove(ret);
            ret.push(unaryExp());
        }
        return ret;
    }

    private SyntaxTree unaryExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.UnaryExp);
        while(token.type == Token.PLUS || token.type == Token.MINUS || token.type == Token.NOT) {
            eatAndMove(ret);
        }
        if(token.type==Token.IDT && in.getFurtherToken(0).type == Token.LP) {
            eatAndMove(ret);
            eatAndMove(ret);
            if(token.type == Token.RP) {
                eatAndMove(ret);
            } else {
                ret.push(funcRParams());
                if(token.type == Token.RP) {
                    eatAndMove(ret);
                } else err();
            }
        } else ret.push(primaryExp());
        return ret;
    }

    private SyntaxTree funcRParams() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.FuncRParams);
        ret.push(exp());
        while(token.type == Token.COMMA) {
            eatAndMove(ret);
            ret.push(exp());
        }
        return ret;
    }

    private SyntaxTree primaryExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.PrimaryExp);
        if(token.type == Token.NUM) {
            eatAndMove(ret);
        } else if (token.type == Token.LP) {
            eatAndMove(ret);
            ret.push(exp());
            if(token.type == Token.RP) {
                eatAndMove(ret);
            } else err();
        } else if(token.type == Token.IDT) {
            ret.push(lVal());
        } else err();
        return ret;
    }

    private SyntaxTree cond() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.Cond);
        ret.push(lOrExp());
        return ret;
    }

    private SyntaxTree lOrExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.LOrExp);
        ret.push(lAndExp());
        while(token.type == Token.OR) {
            eatAndMove(ret);
            ret.push(lAndExp());
        }
        return ret;
    }

    private SyntaxTree lAndExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.LAndExp);
        ret.push(eqExp());
        while(token.type == Token.AND) {
            eatAndMove(ret);
            ret.push(eqExp());
        }
        return ret;
    }

    private SyntaxTree eqExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.EqExp);
        ret.push(relExp());
        while(token.type == Token.EQ || token.type == Token.NE) {
            eatAndMove(ret);
            ret.push(relExp());
        }
        return ret;
    }

    private SyntaxTree relExp() {
        SyntaxTree ret = new SyntaxTree(SyntaxTree.RelExp);
        ret.push(addExp());
        while(token.type == Token.GT || token.type == Token.LT || token.type == Token.GE || token.type == Token.LE) {
            eatAndMove(ret);
            ret.push(addExp());
        }
        return ret;
    }
    
    private void eatAndMove(SyntaxTree tree) {
        tree.push(new SyntaxTree(token));
        token = in.nextToken();
    }
}
