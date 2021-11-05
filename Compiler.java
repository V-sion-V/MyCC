import java.util.ArrayList;

public class Compiler {
    private final Parser parser = new Parser();
    private final ArrayList<SymbolList> symbolLists = new ArrayList<>();
    private SymbolList currentList = new SymbolList(1);
    boolean[] usedFunction = {false,false,false,false};

    public static void main(String[] args) {
        Compiler compiler = new Compiler();
        compiler.compile();
        System.exit(0);
    }

    private void err(SyntaxTree tree) {
        System.out.println(tree.type);
        System.exit(1);
    }

    private void declSysFunc() {
        String[] func={"declare i32 @getint()","declare i32 @getch()","declare void @putint(i32)","declare void @putch(i32)"};
        for(int i = 0; i < usedFunction.length; i++) {
            if(usedFunction[i]) {
                System.out.println(func[i]);
            }
        }
    }

    public void compile() {
        SyntaxTree syntaxTree = parser.getSyntaxTree();
        SyntaxTree ele = syntaxTree.get(0);
        String body = funcDef(ele);
        declSysFunc();
        System.out.println(body);
    }

    private String funcDef(SyntaxTree tree) {
        return "define dso_local " + (tree.get(0).get(0).type == Token.INT ? "i32" : "void") + " @main()" +
                block(tree.get(tree.getWidth() - 1));
    }

    private String block(SyntaxTree tree) {
        StringBuilder out = new StringBuilder();
        out.append("{\n");
        for(int i = 1; i < tree.getWidth()-1; i++) {
            blockItem(tree.get(i),out);
        }
        out.append("}\n");
        return out.toString();
    }

    private void blockItem(SyntaxTree tree, StringBuilder out) {
        tree = tree.get(0);
        if(tree.type == SyntaxTree.Decl) {
            tree = tree.get(0);
            if(tree.type == SyntaxTree.VarDecl) {
                for(int i = 1; i<tree.getWidth(); i+=2) {
                    varDef(tree.get(i),out);
                }
            } else {
                for(int i = 2; i<tree.getWidth(); i+=2) {
                    constDef(tree.get(i),out);
                }
            }
        } else {
            if(tree.get(0).type == SyntaxTree.LVal) {
                assign(tree,out);
            } else if(tree.get(0).type == SyntaxTree.RETURN) {
                ret(tree,out);
            } else if(tree.get(0).type != SyntaxTree.SEMI){
                expToMultiIns(tree.get(0).get(0),out);
            }
        }
    }

    private void varDef(SyntaxTree tree, StringBuilder out) {
        String lVal = tree.get(0).content;
        if(currentList.getSymbol(lVal)==null) {
            Symbol newVar = currentList.declareNewVar(lVal);
            out.append(newVar).append(" = alloca i32").append('\n');
            if (tree.getWidth() >= 3 && tree.get(tree.getWidth() - 2).type == Token.ASSIGN) {
                ExpReturnMsg ret = expToMultiIns(tree.get(tree.getWidth() - 1).get(0), out);
                out.append("store i32 ").append(ret).append(", i32* ").append(newVar).append('\n');
            }
        } else {
            err(tree);
        }
    }

    private void constDef(SyntaxTree tree, StringBuilder out) {
        String lVal = tree.get(0).content;
        if(currentList.getSymbol(lVal)==null) {
            ExpReturnMsg ret = expToMultiIns(tree.get(tree.getWidth()-1).get(0),out);
            if(ret != null && ret.isNumber()) {
                currentList.declareNewConst(lVal,ret.value);
            } else {
                err(tree);
            }
        } else {
            err(tree);
        }
    }

    private void assign(SyntaxTree tree, StringBuilder out) {
        String lVal = tree.get(0).get(0).content;
        if(currentList.getSymbol(lVal)!=null) {
            ExpReturnMsg ret = expToMultiIns(tree.get(2),out);
            out.append("store i32 ").append(ret).append(", i32* ").append(currentList.getSymbol(lVal)).append('\n');
        } else {
            err(tree);
        }
    }

    private void ret(SyntaxTree tree, StringBuilder out) {
        SyntaxTree secondChild = tree.get(1);
        if(secondChild.type == Token.SEMI) {
            out.append("ret void").append('\n');
        } else {
            ExpReturnMsg ret = expToMultiIns(secondChild,out);
            out.append("ret i32 ").append(ret).append('\n');
        }
    }

    private ExpReturnMsg expToMultiIns(SyntaxTree tree, StringBuilder out) {
        ArrayList<SyntaxTree> child = tree.getChild();
        if(tree.type == SyntaxTree.Exp || tree.type == SyntaxTree.ConstExp) {
            return expToMultiIns(child.get(0),out);
        } else if (tree.type == SyntaxTree.MulExp || tree.type == SyntaxTree.AddExp) {
            ArrayList<ExpReturnMsg> numbers = new ArrayList<>();
            ArrayList<Integer> calculators = new ArrayList<>();
            for(int i = 0; i+1 < child.size(); i+=2) {
                numbers.add(expToMultiIns(child.get(i),out));
                calculators.add(child.get(i+1).type);
            }
            numbers.add(expToMultiIns(child.get(child.size()-1),out));
            ExpReturnMsg last;
            Symbol temp;
            int startAt = 1;
            if(numbers.get(0).isNumber()) {
                int startNumber = numbers.get(0).value;
                for(int i = 1; i < numbers.size() && numbers.get(i).isNumber(); i++){
                    if (calculators.get(i-1) == Token.MULT) {
                        startNumber *= numbers.get(i).value;
                    } else if (calculators.get(i-1) == Token.DIV) {
                        if (numbers.get(i).value == 0) err(tree);
                        startNumber /= numbers.get(i).value;
                    } else if (calculators.get(i-1) == Token.MODE) {
                        if (numbers.get(i).value == 0) err(tree);
                        startNumber %= numbers.get(i).value;
                    } else if (calculators.get(i-1) == Token.PLUS) {
                        startNumber += numbers.get(i).value;
                    } else {
                        startNumber -= numbers.get(i).value;
                    }
                    startAt = i+1;
                }
                last = new ExpReturnMsg(startNumber);
            } else {
                last = numbers.get(0);
            }
            for(int i = startAt; i < numbers.size(); i++) {
                temp = currentList.declareNewTemp();
                out.append(temp).append(" = ");
                if(calculators.get(i-1)==Token.MULT) {
                    out.append("mul");
                } else if(calculators.get(i-1)==Token.DIV) {
                    if(numbers.get(i).toString().equals("0")) err(tree);
                    out.append("sdiv");
                } else if (calculators.get(i-1) == Token.MODE) {
                    if(numbers.get(i).toString().equals("0")) err(tree);
                    out.append("srem");
                } else if (calculators.get(i-1) == Token.PLUS) {
                    out.append("add");
                } else {
                    out.append("sub");
                }
                out.append(" i32 ").append(last).append(", ").append(numbers.get(i)).append('\n');
                last = new ExpReturnMsg(temp);
            }
            return last;
        } else if (tree.type == SyntaxTree.UnaryExp) {
            if(tree.get(tree.getWidth()-1).type == Token.RP) {
                /**/
                if(tree.get(0).content.equals("getint")) {
                    Symbol temp = currentList.declareNewTemp();
                    out.append(temp).append(" = call i32 @getint()").append('\n');
                    usedFunction[0]=true;
                    return new ExpReturnMsg(temp);
                } else if(tree.get(0).content.equals("getch")) {
                    Symbol temp = currentList.declareNewTemp();
                    out.append(temp).append(" = call i32 @getch()").append('\n');
                    usedFunction[1]=true;
                    return new ExpReturnMsg(temp);
                } else if(tree.get(0).content.equals("putint")) {
                    ExpReturnMsg param = expToMultiIns(tree.get(2).get(0),out);
                    out.append("call void @putint(i32 ").append(param).append(")").append('\n');
                    usedFunction[2]=true;
                    return new ExpReturnMsg(0);
                } else if(tree.get(0).content.equals("putch")){
                    ExpReturnMsg param = expToMultiIns(tree.get(2).get(0),out);
                    out.append("call void @putch(i32 ").append(param).append(")").append('\n');
                    usedFunction[3]=true;
                    return new ExpReturnMsg(0);
                }
                /**/
            } else {
                int sgn = 1;
                for(int i = 0; i+1< child.size();i++) {
                    if(child.get(i).type == Token.MINUS) {
                        sgn*=-1;
                    }
                }
                ExpReturnMsg primary = expToMultiIns(child.get(child.size()-1),out);
                if(sgn == 1) return primary;
                else if(primary!=null && primary.symbol == null) return new ExpReturnMsg(-primary.value);
                else {
                    Symbol temp = currentList.declareNewTemp();
                    out.append(temp).append(" = mul i32 -1, ").append(primary).append('\n');
                    return new ExpReturnMsg(temp);
                }
            }

        } else if(tree.type == SyntaxTree.PrimaryExp) {
            if(child.get(0).type == SyntaxTree.NUM) {
                return new ExpReturnMsg(Integer.parseInt(child.get(0).content));
            } else if(child.get(0).type == Token.LP) {
                return expToMultiIns(child.get(1),out);
            } else {
                String ident = child.get(0).get(0).content;
                Symbol symbol = currentList.getSymbol(ident);
                if(symbol != null && symbol.type == Symbol.CONST) {
                    return new ExpReturnMsg(symbol.constValue);
                } else if(symbol != null) {
                    Symbol temp = currentList.declareNewTemp();
                    out.append(temp).append(" = load i32, i32* ").append(symbol).append('\n');;
                    return new ExpReturnMsg(temp);
                } else {
                    err(tree);
                }
            }
        } else err(tree);
        return null;
    }
}
