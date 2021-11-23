import java.util.ArrayList;

public class Compiler {
    private final Parser parser = new Parser();
    private int blocks = 0;
    private SymbolList currentList;
    private WhileBlock currentWhile = null;

    public static void main(String[] args) {
        Compiler compiler = new Compiler();
        compiler.compile();
        System.exit(0);
    }

    private void err(SyntaxTree tree) {
        System.out.println(tree.type);
        System.exit(tree.type);
    }

    private void declSysFunc() {
        ArrayList<Symbol> zero = new ArrayList<>(), one = new ArrayList<>(), oneArray = new ArrayList<>(), two = new ArrayList<>();
        ArrayList<Integer> dim = new ArrayList<>();
        dim.add(0);
        SymbolList list = new SymbolList(-1);
        one.add(list.declareNewVar("a"));
        oneArray.add(list.declareNewVarArray("b",dim,true));
        two.add(list.declareNewVar("c"));
        two.add(list.declareNewVarArray("d",dim,true));
        FuncSymbol temp;
        temp = currentList.declareNewFunction("getint",zero,true);
        System.out.println("declare i32 " + temp.toString() + "()");
        temp = currentList.declareNewFunction("getch",zero,true);
        System.out.println("declare i32 " + temp.toString() + "()");
        temp = currentList.declareNewFunction("getarray",oneArray,true);
        System.out.println("declare i32 " + temp.toString() + "(i32*)");
        temp = currentList.declareNewFunction("putint",one,false);
        System.out.println("declare void " + temp.toString() + "(i32)");
        temp = currentList.declareNewFunction("putch",one,false);
        System.out.println("declare void " + temp.toString() + "(i32)");
        temp = currentList.declareNewFunction("putarray",two,false);
        System.out.println("declare void " + temp.toString() + "(i32, i32*)");
    }

    public void compile() {
        SyntaxTree syntaxTree = parser.getSyntaxTree();
        StringBuilder body = new StringBuilder();
        currentList = new SymbolList(blocks++);
        declSysFunc();
        for(int i = 0; i < syntaxTree.getWidth(); i++) {
            SyntaxTree unit = syntaxTree.get(i);
            if(unit.type == SyntaxTree.FuncDef) body.append(funcDef(unit));
            else {
                SyntaxTree decl = unit.get(0);
                if (decl.type == SyntaxTree.ConstDecl) {
                    for (int j = 2; j < decl.getWidth(); j += 2) {
                        SyntaxTree def = decl.get(j);
                        if (def.get(1).type == Token.ASSIGN) {
                            constDef(def);
                        } else {
                            ArraySymbol newVar = declArray(def,body,true);
                            body.append(newVar).append(" = dso_local constant ").append(newVar.getType(0));
                            initGlobalArray(def.get(def.getWidth()-1),body,newVar,0);
                            body.append('\n');
                        }
                    }
                } else {
                    for (int j = 1; j < decl.getWidth(); j += 2) {
                        SyntaxTree def = decl.get(j);
                        if(def.getWidth()<=1 || def.get(1).type == Token.ASSIGN) {
                            Symbol newVar = currentList.declareNewVar(def.get(0).content);
                            if (newVar != null) {
                                if (def.getWidth() < 2) {
                                    body.append(newVar).append(" = global i32 0").append('\n');
                                } else {
                                    ExpReturnMsg initVal = expToMultiIns(def.get(2).get(0), body, false);
                                    if (initVal != null && initVal.isNumber())
                                        body.append(newVar).append(" = global i32 ").append(initVal.iVal).append('\n');
                                    else err(def);
                                }
                            } else err(def);
                        } else {
                            ArraySymbol newVar = declArray(def,body,false);
                            body.append(newVar).append(" = dso_local global ").append(newVar.getType(0)).append(' ');
                            if(def.get(def.getWidth()-2).type == Token.ASSIGN) {
                                initGlobalArray(def.get(def.getWidth() - 1), body, newVar, 0);
                            } else {
                                body.append("zeroinitializer");
                            }
                            body.append('\n');
                        }
                    }
                }
            }
        }
        if(currentList.symbolMap.containsKey("main")) {
            if(currentList.getSymbol("main").type==Symbol.Func) {
                if(((FuncSymbol)currentList.getSymbol("main")).param.size()==0) {
                    System.out.println(body);
                } else err(syntaxTree);
            } else err(syntaxTree);
        } else err(syntaxTree);
    }

    private void initGlobalArray(SyntaxTree initVal, StringBuilder out, ArraySymbol symbol, int dim) {
        if(initVal.get(0).type == Token.LB && initVal.get(1).type != Token.RB) {
            out.append(" [ ");
            for(int i = 0; i < symbol.dimensions.get(dim); i++) {
                out.append(symbol.getType(dim+1)).append(' ');
                if(i*2+1 < initVal.getWidth()) {
                    initGlobalArray(initVal.get(i*2+1),out,symbol,dim+1);
                } else {
                    if(dim+1 == symbol.dimensions.size()) out.append('0');
                    else out.append("zeroinitializer");
                }
                if(i+1<symbol.dimensions.get(dim)) {
                    out.append(", ");
                }
            }
            out.append(" ] ");
        } else if(initVal.get(0).type == SyntaxTree.Exp || initVal.get(0).type == SyntaxTree.ConstExp){
            if(dim == symbol.dimensions.size()) {
                ExpReturnMsg ret = expToMultiIns(initVal.get(0), out, false);
                if (ret != null && ret.isNumber()) {
                    out.append(ret.iVal);
                } else err(initVal);
            } else err(initVal);
        } else {
            out.append("zeroinitializer");
        }
    }

    private String funcDef(SyntaxTree tree) {
        StringBuilder out = new StringBuilder();
        StringBuilder inner = new StringBuilder();
        currentList = new SymbolList(blocks++, currentList);
        boolean isInt = tree.get(0).get(0).type == Token.INT;
        String funcName = tree.get(1).content;
        out.append("define dso_local ").append(isInt?"i32":"void").append(" @").append(funcName).append('(');
        ArrayList<Symbol> params = new ArrayList<>();
        if(tree.get(3).type == SyntaxTree.FuncFParams) {
            SyntaxTree fParams = tree.get(3);
            boolean first = true;
            for(int j = 0; j<fParams.getWidth();j+=2) {
                SyntaxTree fParam = fParams.get(j);
                if(fParam.getWidth() == 2) {

                    VarSymbol intParam = currentList.declareNewVar(fParam.get(1).content);
                    inner.append(intParam).append(" = alloca i32\n\t");
                    Symbol temp = currentList.declareNewTemp();
                    inner.append("store i32 ").append(temp).append(", i32 * ").append(intParam).append('\n');
                    out.append(first?"":", ").append("i32 ").append(temp);
                    params.add(intParam);
                } else {
                    ArrayList<Integer> dimensions = new ArrayList<>();
                    dimensions.add(0);
                    for(int i = 5;i<fParam.getWidth();i+=3) {
                        ExpReturnMsg dimWidth = expToMultiIns(fParam.get(i).get(0),out,false);
                        if(dimWidth!=null && dimWidth.isNumber()) {
                            dimensions.add(dimWidth.iVal);
                        } else err(fParam.get(i).get(0));
                    }
                    ArraySymbol arrayParam = currentList.declareNewVarArray(fParam.get(1).content,dimensions,true);
                    out.append(first?"":", ").append(arrayParam.getType(0)).append(" * ").append(arrayParam);
                    params.add(arrayParam);
                }
                first = false;
            }
        }
        FuncSymbol symbol = currentList.parent.declareNewFunction(funcName,params,isInt);
        out.append(") {\n\t").append(inner).append(block(tree.get(tree.getWidth() - 1), symbol)).append("}\n\n");
        return out.toString();
    }

    private String block(SyntaxTree tree, FuncSymbol func) {
        StringBuilder out = new StringBuilder();
        if(func == null) currentList = new SymbolList(blocks++, currentList);
        for (int i = 1; i < tree.getWidth() - 1; i++) {
            out.append(blockItem(tree.get(i)));
            if (tree.get(i).get(0).get(0).type == Token.RETURN) break;
            else if(i == tree.getWidth() - 2 && func!=null) {
                if(!func.isInt) out.append("ret void\n");
                else out.append("ret i32 0\n");
            }
        }
        currentList = currentList.parent;
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
                for (int i = 2; i < tree.getWidth(); i += 2) out.append(constDef(tree.get(i)));
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
                out.append("br label ").append(currentWhile.labelCond).append("\n\t");
            } else err(tree);
        } else if (tree.get(0).type == SyntaxTree.BREAK) {
            if(currentWhile != null){
                out.append("br label ").append(currentWhile.labelExit).append("\n\t");
            } else err(tree);
        } else if (tree.get(0).type == SyntaxTree.Block) {
            out.append(block(tree.get(0),null));
        }
        return out.toString();
    }


    private void whileLoop(SyntaxTree tree, StringBuilder out) {
        StringBuilder condIns = new StringBuilder();
        Symbol labelCond = currentList.declareNewTemp(),labelLoop = currentList.declareNewTemp(),labelExit = currentList.declareNewTemp();
        currentWhile = new WhileBlock(labelCond,labelExit,currentWhile);
        ExpReturnMsg cond = expToMultiIns(tree.get(2).get(0), condIns, true);
        String loopStmt = stmt(tree.get(4));
        out.append("br label ").append(labelCond).append("\n");
        out.append('\n').append(labelCond.toString().substring(1)).append(":\n\t");
        out.append(condIns);
        out.append("br i1 ").append(cond).append(", label ").append(labelLoop).append(", label ").append(labelExit).append("\n");
        out.append('\n').append(labelLoop.toString().substring(1)).append(":\n\t");
        out.append(loopStmt).append("br label ").append(labelCond).append("\n");
        out.append('\n').append(labelExit.toString().substring(1)).append(":\n\t");
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
        out.append('\n').append(labelIf.toString().substring(1)).append(":\n\t");
        out.append(ifStmt).append("br label ").append(labelExit).append('\n');
        if (elseStmt != null && labelElse != null) {
            out.append('\n').append(labelElse.toString().substring(1)).append(":\n\t");
            out.append(elseStmt).append("br label ").append(labelExit).append("\n");
        }
        out.append('\n').append(labelExit.toString().substring(1)).append(":\n\t");
    }

    private String varDef(SyntaxTree tree) {
        StringBuilder out = new StringBuilder();
        if(tree.getWidth()<=1||tree.get(1).type != Token.LC) {
            String lVal = tree.get(0).content;
            Symbol newVar = currentList.declareNewVar(lVal);
            if (newVar != null) {
                out.append(newVar).append(" = alloca i32").append("\n\t");
                if (tree.getWidth() >= 3 && tree.get(tree.getWidth() - 2).type == Token.ASSIGN) {
                    ExpReturnMsg ret = expToMultiIns(tree.get(tree.getWidth() - 1).get(0), out, false);
                    out.append("store i32 ").append(ret).append(", i32* ").append(newVar).append("\n\t");
                }
            } else err(tree);
        } else {
            ArraySymbol symbol = declArray(tree,out,false);
            initToZero(out, symbol);
            if(tree.get(tree.getWidth()-1).type == SyntaxTree.InitVal) {
                initArray(tree.get(tree.getWidth()-1),out,symbol,symbol,0);
            }
        }
        return out.toString();
    }

    private void initToZero(StringBuilder out, ArraySymbol symbol) {
        out.append(symbol).append(" = alloca ").append(symbol.getType(0)).append("\n\t");
        out.append("store ").append(symbol.getType(0)).append(" zeroinitializer, ")
                .append(symbol.getType(0)).append("* ").append(symbol).append("\n\t");
    }

    private String constDef(SyntaxTree tree) {
        StringBuilder out = new StringBuilder();
        if(tree.get(1).type == Token.ASSIGN) {
            String lVal = tree.get(0).content;
            ExpReturnMsg ret = expToMultiIns(tree.get(2).get(0), out, false);
            if (ret != null && ret.isNumber()) {
                Symbol x = currentList.declareNewConst(lVal, ret.iVal);
                if (x == null) err(tree);
            } else err(tree.get(2).get(0));
        } else {
            ArraySymbol symbol = declArray(tree,out,true);
            initToZero(out, symbol);
            initArray(tree.get(tree.getWidth()-1),out,symbol,symbol,0);
        }
        return out.toString();
    }

    private ArraySymbol declArray(SyntaxTree tree, StringBuilder out, boolean isConst) {
        String lVal = tree.get(0).content;
        ArrayList<Integer> dimensions = new ArrayList<>();
        for(int i = 2; i<tree.getWidth()&&tree.get(i).type == SyntaxTree.ConstExp; i+=3){
            ExpReturnMsg dimWidth = expToMultiIns(tree.get(i).get(0),out,false);
            if(dimWidth!=null && dimWidth.isNumber()) {
                dimensions.add(dimWidth.iVal);
            } else err(tree.get(i).get(0));
        }
        ArraySymbol symbol;
        if(isConst) symbol = currentList.declareNewConstArray(lVal, dimensions);
        else symbol = currentList.declareNewVarArray(lVal, dimensions, false);
        return symbol;
    }

    private void initArray(SyntaxTree tree, StringBuilder out, ArraySymbol symbol, Symbol thisStep, int dim) {
        if(tree.get(0).type == Token.LB && tree.get(1).type != Token.RB) {
            TempSymbol nextStep = currentList.declareNewTemp();
            out.append(nextStep).append(" = getelementptr ").append(symbol.getType(dim)).append(", ");
            out.append(symbol.getType(dim)).append("* ").append(thisStep).append(", i32 0, i32 0\n\t");
            for(int i = 1; i < tree.getWidth(); i+=2) {
                initArray(tree.get(i),out,symbol,nextStep,dim+1);
                if(i+2<tree.getWidth()) {
                    thisStep = nextStep;
                    nextStep = currentList.declareNewTemp();
                    out.append(nextStep).append(" = getelementptr ").append(symbol.getType(dim+1)).append(", ");
                    out.append(symbol.getType(dim+1)).append("* ").append(thisStep).append(", i32 ").append(1).append("\n\t");
                }
            }
        } else if(tree.get(0).type == SyntaxTree.ConstExp) {
            if(dim == symbol.dimensions.size()) {
                ExpReturnMsg ret = expToMultiIns(tree.get(0), out, false);
                if (ret != null && ret.isNumber()) {
                    out.append("store i32 ").append(ret.iVal).append(", i32* ").append(thisStep).append("\n\t");
                } else err(tree);
            } else err(tree);
        } else if(tree.get(0).type == SyntaxTree.Exp) {
            ExpReturnMsg ret = expToMultiIns(tree.get(0), out, false);
            if (ret != null) {
                out.append("store i32 ").append(ret).append(", i32* ").append(thisStep).append("\n\t");
            } else err(tree);
        }
    }

    private void assign(SyntaxTree tree, StringBuilder out) {
        String idtName = tree.get(0).get(0).content;
        Symbol symbol = currentList.getSymbol(idtName);
        if (symbol != null) {
            ExpReturnMsg ret = expToMultiIns(tree.get(tree.getWidth()-2), out, false);
            if (symbol.type == Symbol.Var) {
                out.append("store i32 ").append(ret).append(", i32* ").append(symbol).append("\n\t");
            } else if(symbol.type == Symbol.VarArray) {
                SyntaxTree lVal = tree.get(0);
                if(((ArraySymbol)symbol).dimensions.size() == lVal.getWidth()/3) {
                    Symbol tempPtr = getArrayElementPtr(out, symbol, lVal,false);
                    out.append("store i32 ").append(ret).append(", i32* ").append(tempPtr).append("\n\t");
                } else err(lVal);
            } else err(tree);
        } else err(tree);
    }

    private void ret(SyntaxTree tree, StringBuilder out) {
        SyntaxTree secondChild = tree.get(1);
        if (secondChild.type == Token.SEMI) {
            out.append("ret void").append("\n");
        } else {
            ExpReturnMsg ret = expToMultiIns(secondChild, out, false);
            out.append("ret i32 ").append(ret).append("\n");
        }
    }

    private ExpReturnMsg expToMultiIns(SyntaxTree tree, StringBuilder out, boolean fromCond) {
        if (tree.type == SyntaxTree.Exp || tree.type == SyntaxTree.ConstExp) {
            return expToMultiIns(tree.get(0), out, fromCond);
        } else if (fromCond && (tree.type == SyntaxTree.LAndExp || tree.type == SyntaxTree.LOrExp)) {
            ArrayList<ExpReturnMsg> numbers = new ArrayList<>();
            ArrayList<Integer> calculators = new ArrayList<>();
            for (int i = 0; i + 1 < tree.getWidth(); i += 2) {
                numbers.add(expToMultiIns(tree.get(i), out, true));
                calculators.add(tree.get(i + 1).type);
            }
            numbers.add(expToMultiIns(tree.get(tree.getWidth() - 1), out, true));
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
                out.append(" i1 ").append(last).append(", ").append(now).append("\n\t");
                last = new ExpReturnMsg(temp, true);
            }
            return toBoolean(last, out);
        } else if (fromCond && (tree.type == SyntaxTree.EqExp || tree.type == SyntaxTree.RelExp)) {
            if (tree.getWidth() == 1) {
                return expToMultiIns(tree.get(0), out, true);
            }
            ArrayList<ExpReturnMsg> numbers = new ArrayList<>();
            ArrayList<Integer> calculators = new ArrayList<>();
            for (int i = 0; i + 1 < tree.getWidth(); i += 2) {
                numbers.add(expToMultiIns(tree.get(i), out, true));
                calculators.add(tree.get(i + 1).type);
            }
            numbers.add(expToMultiIns(tree.get(tree.getWidth() - 1), out, true));
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
                out.append(" i32 ").append(last).append(", ").append(now).append("\n\t");
                last = new ExpReturnMsg(temp, true);
            }
            return toBoolean(last, out);
        } else if (tree.type == SyntaxTree.MulExp || tree.type == SyntaxTree.AddExp) {
            ArrayList<ExpReturnMsg> numbers = new ArrayList<>();
            ArrayList<Integer> calculators = new ArrayList<>();
            for (int i = 0; i + 1 < tree.getWidth(); i += 2) {
                numbers.add(expToMultiIns(tree.get(i), out, fromCond));
                calculators.add(tree.get(i + 1).type);
            }
            numbers.add(expToMultiIns(tree.get(tree.getWidth() - 1), out, fromCond));
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
                out.append(" i32 ").append(last).append(", ").append(now).append("\n\t");
                last = new ExpReturnMsg(temp, false);
            }
            return last;
        } else if (tree.type == SyntaxTree.UnaryExp) {
            int sgn = 1, not = 0, i;
            for (i = 0; i + 1 < tree.getWidth(); i++) {
                if (tree.get(i).type != Token.PLUS) {
                    if (tree.get(i).type == Token.MINUS) sgn *= -1;
                    else if (tree.get(i).type == Token.NOT && fromCond) not++;
                    else if (tree.get(i).type == Token.NOT) err(tree);
                    else break;
                }
            }
            ExpReturnMsg primary;
            if (tree.get(tree.getWidth() - 1).type == Token.RP) {
                primary = new ExpReturnMsg(currentList.declareNewTemp());
                String funcName = tree.get(i).content;
                FuncSymbol f = (FuncSymbol) currentList.getSymbol(funcName);
                if(f!=null) {
                    StringBuilder call = new StringBuilder();
                    if(tree.get(i+2).type == SyntaxTree.FuncRParams) {
                        if(f.isInt) call.append(primary).append(" = call i32 ").append(f).append("(");
                        else call.append("call void ").append(f).append("(");
                        SyntaxTree params = tree.get(i+2);
                        for(int j = 0;j<params.getWidth();j+=2) {
                            if(f.param.size() >= j/2+1) {
                                if(f.param.get(j/2).type == Symbol.Var || f.param.get(j/2).type == Symbol.Const) {
                                    ExpReturnMsg param = expToMultiIns(params.get(j),out,false);
                                    call.append(j==0?"":", ").append("i32 ").append(param);
                                } else {
                                    if(params.get(j).getWidth() == 1 &&
                                            params.get(j).get(0).getWidth() == 1 &&
                                            params.get(j).get(0).get(0).getWidth() == 1 &&
                                            params.get(j).get(0).get(0).get(0).getWidth() == 1 &&
                                            params.get(j).get(0).get(0).get(0).get(0).getWidth() == 1 &&
                                            params.get(j).get(0).get(0).get(0).get(0).get(0).type == SyntaxTree.LVal){
                                        SyntaxTree primaryExp = params.get(j).get(0).get(0).get(0).get(0);
                                        Symbol arrayName = currentList.getSymbol(primaryExp.get(0).get(0).content);
                                        ArraySymbol fp = (ArraySymbol)f.param.get(j/2), rp = (ArraySymbol)arrayName;
                                        int fpSize = fp.dimensions.size(), rpSize= rp.dimensions.size();
                                        if(primaryExp.get(0).getWidth()/3 + fpSize == rpSize) {
                                            for(int k = 1; fpSize-k>0;k++)
                                                if(!fp.dimensions.get(fpSize - k).equals(rp.dimensions.get(rpSize - k)))
                                                    err(params.get(k));
                                            call.append(j==0?"":", ").append(fp.getType(0)).append("* ");
                                            call.append(getArrayElementPtr(out,arrayName,primaryExp.get(0),true));
                                        } else err(params.get(j));
                                    } else err(params.get(j));
                                }
                            } else err(params.get(j));
                        }
                        call.append(")\n\t");
                        out.append(call);
                    } else if(f.param.size()==0) {
                        if(f.isInt) out.append(primary).append(" = call i32 ").append(f).append("()\n\t");
                        else out.append("call void ").append(f).append("()\n\t");
                    } else err(tree.get(i+2));
                } else err(tree.get(i));
            } else primary = expToMultiIns(tree.get(tree.getWidth() - 1), out, fromCond);
            if(primary != null && primary.isPtr) err(tree);
            Symbol temp = currentList.declareNewTemp();
            if (not == 0) {
                if (sgn == 1) return primary;
                else if (primary != null && primary.isNumber()) return new ExpReturnMsg(-primary.iVal);
                else {
                    out.append(temp).append(" = mul i32 -1, ").append(primary).append("\n\t");
                    return new ExpReturnMsg(temp);
                }
            } else {
                if (not % 2 == 0) out.append(temp).append(" = icmp ne i32 0, ").append(primary).append("\n\t");
                else out.append(temp).append(" = icmp eq i32 0, ").append(primary).append("\n\t");
                return new ExpReturnMsg(temp, true);
            }
        } else if (tree.type == SyntaxTree.PrimaryExp) {
            if (tree.get(0).type == SyntaxTree.NUM) {
                return new ExpReturnMsg(Integer.parseInt(tree.get(0).content));
            } else if (tree.get(0).type == Token.LP) {
                return expToMultiIns(tree.get(1), out, fromCond);
            } else {
                String ident = tree.get(0).get(0).content;
                Symbol symbol = currentList.getSymbol(ident);
                if (symbol != null && symbol.type == Symbol.Const) {
                    return new ExpReturnMsg(((ConstSymbol)symbol).constValue);
                } else if (symbol != null && (symbol.type == Symbol.ConstArray||symbol.type == Symbol.VarArray)) {
                    SyntaxTree lVal = tree.get(0);
                    Symbol tempPtr =  getArrayElementPtr(out, symbol, lVal, false);
                    Symbol tempVal = currentList.declareNewTemp();
                    out.append(tempVal).append(" = load i32, i32* ").append(tempPtr).append("\n\t");
                    if(((ArraySymbol)symbol).dimensions.size() == lVal.getWidth()/3) {
                        return new ExpReturnMsg(tempVal);
                    } else if(((ArraySymbol)symbol).dimensions.size() > lVal.getWidth()/3) {
                        ExpReturnMsg ret =  new ExpReturnMsg(tempVal);
                        ret.isPtr = true;
                        return ret;
                    } else err(lVal);
                } else if (symbol != null) {
                    Symbol temp = currentList.declareNewTemp();
                    out.append(temp).append(" = load i32, i32* ").append(symbol).append("\n\t");
                    return new ExpReturnMsg(temp);
                } else err(tree);
            }
        } else err(tree);
        return null;
    }

    private Symbol getArrayElementPtr(StringBuilder out, Symbol symbol, SyntaxTree lVal, boolean fromParam) {
        StringBuilder getPtr = new StringBuilder();
        Symbol tempPtr = currentList.declareNewTemp();
        getPtr.append(tempPtr).append(" = getelementptr ").append(((ArraySymbol)symbol).getType(0)).append(", ");
        getPtr.append(((ArraySymbol)symbol).getType(0)).append("* ").append(symbol).append((((ArraySymbol)symbol).fromParam)?"":", i32 0");
        for(int i = 2; i < lVal.getWidth(); i+=3) {
            ExpReturnMsg ret = expToMultiIns(lVal.get(i),out,false);
            getPtr.append(", i32 ").append(ret);
        }
        out.append(getPtr).append(fromParam?", i32 0\n\t":"\n\t");
        return tempPtr;
    }

    private ExpReturnMsg toBoolean(ExpReturnMsg x, StringBuilder out) {
        if (x.isSymbol()) {
            Symbol temp = currentList.declareNewTemp();
            out.append(temp).append(" = icmp ne i32 0, ").append(x).append("\n\t");
            return new ExpReturnMsg(temp, true);
        }
        return x;
    }

    private ExpReturnMsg toInt(ExpReturnMsg x, StringBuilder out) {
        if (x.isBooleanSymbol()) {
            Symbol temp = currentList.declareNewTemp();
            out.append(temp).append(" = zext i1 ").append(x).append(" to i32").append("\n\t");
            return new ExpReturnMsg(temp, false);
        }
        return x;
    }
}
