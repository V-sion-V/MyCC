public class WhileBlock {
    public final Symbol labelCond, labelExit;
    WhileBlock parent;

    public WhileBlock(Symbol labelCond, Symbol labelExit, WhileBlock parent) {
        this.labelCond = labelCond;
        this.labelExit = labelExit;
        this.parent = parent;
    }
}
