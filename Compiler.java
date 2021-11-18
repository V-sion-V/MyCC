import java.util.ArrayList;

public class Compiler {
    private final Parser parser = new Parser();
    private int blocks = 0;
    private SymbolList currentList;
    private WhileBlock currentWhile = null;
    boolean[] usedFunction = {false, false, false, false};

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
        String[] func = {"declare i32 @getint()", "declare i32 @getch()", "declare void @putint(i32)", "declare void @putch(i32)"};
        for (int i = 0; i < usedFunction.length; i++) {
            if (usedFunction[i]) {
                System.out.println(func[i]);
            }
        }
    }

    public void compile() {
        SyntaxTree syntaxTree = parser.getSyntaxTree();
        StringBuilder body = new StringBuilder();
        currentList = new SymbolList(blocks++);
        for(int i = 0; i < syntaxTree.getWidth(); i++) {
            SyntaxTree unit = syntaxTree.get(i);
            if(unit.type == SyntaxTree.FuncDef) body.append(funcDef(unit));
            else {
                SyntaxTree decl = unit.get(0);
                if (decl.type == SyntaxTree.ConstDecl)
                    for (int j = 2; j < decl.getWidth(); j += 2) constDef(decl.get(j));
                else {
                    for (int j = 1; j < decl.getWidth(); j += 2) {
                        SyntaxTree def = decl.get(j);
                        Symbol newVar = currentList.declareNewVar(def.get(0).content);
                        if (newVar != null) {
                            if(def.getWidth()<2) {
                                body.append(newVar).append(" = global i32 0").append('\n');
                            } else {
                                ExpReturnMsg initVal = expToMultiIns(def.get(2).get(0),body,false);
                                if(initVal!=null && initVal.isNumber()) body.append(newVar).append(" = global i32 ").append(initVal.iVal).append('\n');
                                else err(def);
                            }
                        } else err(def);
                    }
                }
            }
        }
        declSysFunc();//
        System.out.println(body);
    }

    private String funcDef(SyntaxTree tree) {
        return "define dso_local " + (tree.get(0).get(0).type == Token.INT ? "i32" : "void") + " @main() {\n" +
                block(tree.get(tree.getWidth() - 1)) + "}\n";
    }

    private String block(SyntaxTree tree) {
        StringBuilder out = new StringBuilder();
        SymbolList old = currentList;
        currentList = new SymbolList(blocks++, old);
        for (int i = 1; i < tree.getWidth() - 1; i++) {
            out.append(blockItem(tree.get(i)));
            if (tree.get(i).get(0).get(0).type == Token.RETURN) break;
        }
        currentList = old;
        return out.toString();
    }

    private String blockItem(SyntaxTree tree) {
        StringBuilder out = new StringBuilder();
        tree = tree.get(0);
        if (tree.type == SyntaxTree.Decl) {
            tree = tree.get(0);
            if (tree.type == SyntaxTree.VarDecl)
                for (int i = 1; i < tree.getWidth(); i += 2) out.append(varDef(tree.get(i)));
            else
                for (int i = 2; i < tree.getWidth(); i += 2) constDef(tree.get(i));
        } else out.append(stmt(tree));
        return out.toString();
    }

    /*
    Elements smaller than 'stmt' will not have its own string builder.
    Because only the elements like 'stmt' or 'block' might be ordered reversely.
     */
    private String stmt(SyntaxTree tree) {
        StringBuilder out = new StringBuilder();
        if (tree.get(0).type == SyntaxTree.LVal) {
            assign(tree, out);
        } else if (tree.get(0).type == SyntaxTree.RETURN) {
            ret(tree, out);
        } else if (tree.get(0).type == SyntaxTree.Exp) {
            expToMultiIns(tree.get(0).get(0), out, false);
        } else if (tree.get(0).type == SyntaxTree.IF) {
            ifBranch(tree, out);
        } else if (tree.get(0).type == SyntaxTree.WHILE) {
            whileLoop(tree, out);
        } else if (tree.get(0).type == SyntaxTree.CONTINUE) {
            if(currentWhile != null){
                out.append("br label ").append(currentWhile.labelCond).append('\n');
            } else err(tree);
        } else if (tree.get(0).type == SyntaxTree.BREAK) {
            if(currentWhile != null){
                out.append("br label ").append(currentWhile.labelExit).append('\n');
            } else err(tree);
        } else if (tree.get(0).type == SyntaxTree.Block) {
            out.append(block(tree.get(0)));
        }
        return out.toString();
    }


    private void whileLoop(SyntaxTree tree, StringBuilder out) {
        StringBuilder condIns = new StringBuilder();
        Symbol labelCond = currentList.declareNewTemp(),labelLoop = currentList.declareNewTemp(),labelExit = currentList.declareNewTemp();
        currentWhile = new WhileBlock(labelCond,labelExit,currentWhile);
        ExpReturnMsg cond = expToMultiIns(tree.get(2).get(0), condIns, true);
        String loopStmt = stmt(tree.get(4));
        out.append("br label ").append(labelCond).append('\n');
        out.append('\n').append(labelCond.toString().substring(1)).append(":\n");
        out.append(condIns);
        out.append("br i1 ").append(cond).append(", label ").append(labelLoop).append(", label ").append(labelExit).append('\n');
        out.append('\n').append(labelLoop.toString().substring(1)).append(":\n");
        out.append(loopStmt).append("br label ").append(labelCond).append('\n');
        out.append('\n').append(labelExit.toString().substring(1)).append(":\n");
        currentWhile = currentWhile.parent;
    }

    private void ifBranch(SyntaxTree tree, StringBuilder out) {
        ExpReturnMsg cond = expToMultiIns(tree.get(2).get(0), out, true);
        Symbol labelIf = currentList.declareNewTemp(), labelElse = null;
        String ifStmt = stmt(tree.get(4)), elseStmt = null;
        if (tree.getWidth() > 5) {
            labelElse = currentList.declareNewTemp();
            elseStmt = stmt(tree.get(6));
        }
        Symbol labelExit = currentList.declareNewTemp();
        out.append("br i1 ").append(cond).append(", label ").append(labelIf).append(", label ");
        if (labelElse == null) out.append(labelExit).append('\n');
        else out.append(labelElse).append('\n');
        out.append('\n').append(labelIf.toString().substring(1)).append(":\n");
        out.append(ifStmt).append("br label ").append(labelExit).append('\n');
        if (elseStmt != null && labelElse != null) {
            out.append('\n').append(labelElse.toString().substring(1)).append(":\n");
            out.append(elseStmt).append("br label ").append(labelExit).append('\n');
        }
        out.append('\n').append(labelExit.toString().substring(1)).append(":\n");
    }

    private String varDef(SyntaxTree tree) {
        StringBuilder out = new StringBuilder();
        String lVal = tree.get(0).content;
        Symbol newVar = currentList.declareNewVar(lVal);
        if (newVar != null) {
            out.append(newVar).append(" = alloca i32").append('\n');
            if (tree.getWidth() >= 3 && tree.get(tree.getWidth() - 2).type == Token.ASSIGN) {
                ExpReturnMsg ret = expToMultiIns(tree.get(tree.getWidth() - 1).get(0), out, false);
                out.append("store i32 ").append(ret).append(", i32* ").append(newVar).append('\n');
            }
        } else err(tree);
        return out.toString();
    }

    private void constDef(SyntaxTree tree) {
        String lVal = tree.get(0).content;
        ExpReturnMsg ret = expToMultiIns(tree.get(tree.getWidth() - 1).get(0), new StringBuilder(), false);
        if (ret != null && ret.isNumber()) {
            Symbol x = currentList.declareNewConst(lVal, ret.iVal);
            if (x == null) err(tree);
        } else err(tree);
    }

    private void assign(SyntaxTree tree, StringBuilder out) {
        String lVal = tree.get(0).get(0).content;
        if (currentList.getSymbol(lVal) != null) {
            ExpReturnMsg ret = expToMultiIns(tree.get(2), out, false);
            out.append("store i32 ").append(ret).append(", i32* ").append(currentList.getSymbol(lVal)).append('\n');
        } else err(tree);
    }

    private void ret(SyntaxTree tree, StringBuilder out) {
        SyntaxTree secondChild = tree.get(1);
        if (secondChild.type == Token.SEMI) {
            out.append("ret void").append('\n');
        } else {
            ExpReturnMsg ret = expToMultiIns(secondChild, out, false);
            out.append("ret i32 ").append(ret).append('\n');
        }
    }

    private ExpReturnMsg expToMultiIns(SyntaxTree tree, StringBuilder out, boolean fromCond) {
        ArrayList<SyntaxTree> child = tree.getChild();
        if (tree.type == SyntaxTree.Exp || tree.type == SyntaxTree.ConstExp) {
            return expToMultiIns(child.get(0), out, fromCond);
        } else if (fromCond && (tree.type == SyntaxTree.LAndExp || tree.type == SyntaxTree.LOrExp)) {
            ArrayList<ExpReturnMsg> numbers = new ArrayList<>();
            ArrayList<Integer> calculators = new ArrayList<>();
            for (int i = 0; i + 1 < child.size(); i += 2) {
                numbers.add(expToMultiIns(child.get(i), out, true));
                calculators.add(child.get(i + 1).type);
            }
            numbers.add(expToMultiIns(child.get(child.size() - 1), out, true));
            ExpReturnMsg last = toBoolean(numbers.get(0), out), now;
            Symbol temp;
            for (int i = 1; i < numbers.size(); i++) {
                temp = currentList.declareNewTemp();
                now = toBoolean(numbers.get(i), out);
                int sep = calculators.get(i - 1);
                out.append(temp).append(" = ");
                switch (sep) {
                    case Token.AND -> out.append("and");
                    case Token.OR -> out.append("or");
                }
                out.append(" i1 ").append(last).append(", ").append(now).append('\n');
                last = new ExpReturnMsg(temp, true);
            }
            return toBoolean(last, out);
        } else if (fromCond && (tree.type == SyntaxTree.EqExp || tree.type == SyntaxTree.RelExp)) {
            if (tree.getWidth() == 1) {
                return expToMultiIns(child.get(0), out, true);
            }
            ArrayList<ExpReturnMsg> numbers = new ArrayList<>();
            ArrayList<Integer> calculators = new ArrayList<>();
            for (int i = 0; i + 1 < child.size(); i += 2) {
                numbers.add(expToMultiIns(child.get(i), out, true));
                calculators.add(child.get(i + 1).type);
            }
            numbers.add(expToMultiIns(child.get(child.size() - 1), out, true));
            ExpReturnMsg last = toInt(numbers.get(0), out), now;
            Symbol temp;
            for (int i = 1; i < numbers.size(); i++) {
                temp = currentList.declareNewTemp();
                last = toInt(last, out);
                now = toInt(numbers.get(i), out);
                int sep = calculators.get(i - 1);
                out.append(temp).append(" = ").append("icmp ");
                switch (sep) {
                    case Token.EQ -> out.append("eq");
                    case Token.NE -> out.append("ne");
                    case Token.GE -> out.append("sge");
                    case Token.LE -> out.append("sle");
                    case Token.GT -> out.append("sgt");
                    case Token.LT -> out.append("slt");
                }
                out.append(" i32 ").append(last).append(", ").append(now).append('\n');
                last = new ExpReturnMsg(temp, true);
            }
            return toBoolean(last, out);
        } else if (tree.type == SyntaxTree.MulExp || tree.type == SyntaxTree.AddExp) {
            ArrayList<ExpReturnMsg> numbers = new ArrayList<>();
            ArrayList<Integer> calculators = new ArrayList<>();
            for (int i = 0; i + 1 < child.size(); i += 2) {
                numbers.add(expToMultiIns(child.get(i), out, fromCond));
                calculators.add(child.get(i + 1).type);
            }
            numbers.add(expToMultiIns(child.get(child.size() - 1), out, fromCond));
            ExpReturnMsg last, now;
            Symbol temp;
            int startAt = 1;
            if (numbers.get(0).isNumber()) {
                int startNumber = numbers.get(0).iVal;
                for (int i = 1; i < numbers.size() && numbers.get(i).isNumber(); i++) {
                    int sep = calculators.get(i - 1);
                    switch (sep) {
                        case Token.MULT -> startNumber *= numbers.get(i).iVal;
                        case Token.DIV -> {
                            if (numbers.get(i).iVal == 0) err(tree);
                            startNumber /= numbers.get(i).iVal;
                        }
                        case Token.MODE -> {
                            if (numbers.get(i).iVal == 0) err(tree);
                            startNumber %= numbers.get(i).iVal;
                        }
                        case Token.PLUS -> startNumber += numbers.get(i).iVal;
                        case Token.MINUS -> startNumber -= numbers.get(i).iVal;
                    }
                    startAt = i + 1;
                }
                last = new ExpReturnMsg(startNumber);
            } else {
                last = numbers.get(0);
            }
            last = toInt(last, out);
            for (int i = startAt; i < numbers.size(); i++) {
                temp = currentList.declareNewTemp();
                now = toInt(numbers.get(i), out);
                int sep = calculators.get(i - 1);
                out.append(temp).append(" = ");
                switch (sep) {
                    case Token.MULT -> out.append("mul");
                    case Token.DIV -> {
                        if (now.toString().equals("0")) err(tree);
                        out.append("sdiv");
                    }
                    case Token.MODE -> {
                        if (now.toString().equals("0")) err(tree);
                        out.append("srem");
                    }
                    case Token.PLUS -> out.append("add");
                    case Token.MINUS -> out.append("sub");
                }
                out.append(" i32 ").append(last).append(", ").append(now).append('\n');
                last = new ExpReturnMsg(temp, false);
            }
            return last;
        } else if (tree.type == SyntaxTree.UnaryExp) {
            int sgn = 1, not = 0;
            for (int i = 0; i + 1 < child.size(); i++) {
                if (child.get(i).type != Token.PLUS) {
                    if (child.get(i).type == Token.MINUS) sgn *= -1;
                    else if (child.get(i).type == Token.NOT && fromCond) not++;
                    else if (child.get(i).type == Token.NOT) err(tree);
                    else break;
                }
            }
            ExpReturnMsg primary;
            if (tree.get(tree.getWidth() - 1).type == Token.RP) {
                /**/
                primary = new ExpReturnMsg(currentList.declareNewTemp());
                switch (tree.get(0).content) {
                    case "getint" -> {
                        out.append(primary).append(" = call i32 @getint()").append('\n');
                        usedFunction[0] = true;
                    }
                    case "getch" -> {
                        out.append(primary).append(" = call i32 @getch()").append('\n');
                        usedFunction[1] = true;
                    }
                    case "putint" -> {
                        ExpReturnMsg param = expToMultiIns(tree.get(2).get(0), out, fromCond);
                        out.append("call void @putint(i32 ").append(param).append(")").append('\n');
                        usedFunction[2] = true;
                        return new ExpReturnMsg(0);
                    }
                    case "putch" -> {
                        ExpReturnMsg param = expToMultiIns(tree.get(2).get(0), out, fromCond);
                        out.append("call void @putch(i32 ").append(param).append(")").append('\n');
                        usedFunction[3] = true;
                        return new ExpReturnMsg(0);
                    }
                }
                /**/
            } else primary = expToMultiIns(child.get(child.size() - 1), out, fromCond);
            Symbol temp = currentList.declareNewTemp();
            if (not == 0) {
                if (sgn == 1) return primary;
                else if (primary != null && primary.isNumber()) return new ExpReturnMsg(-primary.iVal);
                else {
                    out.append(temp).append(" = mul i32 -1, ").append(primary).append('\n');
                    return new ExpReturnMsg(temp);
                }
            } else {
                if (not % 2 == 0) out.append(temp).append(" = icmp ne i32 0, ").append(primary).append('\n');
                else out.append(temp).append(" = icmp eq i32 0, ").append(primary).append('\n');
                return new ExpReturnMsg(temp, true);
            }
        } else if (tree.type == SyntaxTree.PrimaryExp) {
            if (child.get(0).type == SyntaxTree.NUM) {
                return new ExpReturnMsg(Integer.parseInt(child.get(0).content));
            } else if (child.get(0).type == Token.LP) {
                return expToMultiIns(child.get(1), out, fromCond);
            } else {
                String ident = child.get(0).get(0).content;
                Symbol symbol = currentList.getSymbol(ident);
                if (symbol != null && symbol.type == Symbol.CONST) {
                    return new ExpReturnMsg(symbol.constValue);
                } else if (symbol != null) {
                    Symbol temp = currentList.declareNewTemp();
                    out.append(temp).append(" = load i32, i32* ").append(symbol).append('\n');
                    return new ExpReturnMsg(temp);
                } else err(tree);
            }
        } else err(tree);
        return null;
    }

    private ExpReturnMsg toBoolean(ExpReturnMsg x, StringBuilder out) {
        if (x.isSymbol()) {
            Symbol temp = currentList.declareNewTemp();
            out.append(temp).append(" = icmp ne i32 0, ").append(x).append('\n');
            return new ExpReturnMsg(temp, true);
        }
        return x;
    }

    private ExpReturnMsg toInt(ExpReturnMsg x, StringBuilder out) {
        if (x.isBooleanSymbol()) {
            Symbol temp = currentList.declareNewTemp();
            out.append(temp).append(" = zext i1 ").append(x).append(" to i32").append('\n');
            return new ExpReturnMsg(temp, false);
        }
        return x;
    }
}
