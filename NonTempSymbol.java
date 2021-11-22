public class NonTempSymbol extends Symbol{
    String originForm;
    public NonTempSymbol(int blockName, int innerNumber,int type,String originForm) {
        super(blockName,innerNumber,type);
        this.originForm = originForm;
    }
    @Override
    public String toString() {
        return (blockName==0)?"@"+originForm:"%"+"b" + blockName + "x" + innerNumber;
    }
}
