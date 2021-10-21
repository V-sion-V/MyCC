import java.util.ArrayList;

public class SyntaxTree extends Token {
    static final int CompUnit = 128, Decl = 129, ConstDecl = 130, BType = 131, ConstDef = 132,ConstInitVal = 133,
    VarDecl = 134, VarDef = 135, InitVal = 136, FuncDef = 137, FuncType = 138, FuncFParams = 139, FuncFParam = 140,
    Block = 141, BlockItem = 142, Stmt = 143, Exp = 144, Cond = 145, LVal = 146, PrimaryExp = 147,UnaryExp = 148,
    UnaryOp = 149, FuncRParams = 150, MulExp = 151, AddExp = 152, RelExp = 153, EqExp = 154, LAndExp = 155,
    LOrExp = 156, ConstExp = 157;
    private ArrayList<SyntaxTree> child = null;
    public int value = 1;

    SyntaxTree(int type) {
        super(type);
        setValue();
    }

    SyntaxTree(Token token) {
        super(token.type,token.content);
        setValue();
    }

    private void setValue(){
        if(type == PLUS) value = 1;
        else if(type == MINUS) value = -1;
        else if(type == NUM) value = Integer.parseInt(content);
    }

    public void push(SyntaxTree a) {
        if(child == null) {
            child = new ArrayList<>();
        }
        child.add(a);
    }

    public ArrayList<SyntaxTree> getChild() {
        if(child == null) {
            child = new ArrayList<>();
        }
        return child;
    }

    public ArrayList<Token> getVt() {
        ArrayList<Token> ret = new ArrayList<>();
        if(this.type < 128) {
            ret.add(this);
        } else {
            for(SyntaxTree i : getChild()) {
                ret.addAll(i.getVt());
            }
        }
        return ret;
    }
}
