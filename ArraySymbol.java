import java.util.ArrayList;

public class ArraySymbol extends NonTempSymbol{
    ArrayList<Integer> dimensions;
    boolean fromParam;
    public ArraySymbol(int blockName, int innerNumber, String originForm, int type, ArrayList<Integer> dimensions, boolean fromParam) {
        super(blockName,innerNumber,type,originForm);
        this.dimensions = dimensions;
        this.fromParam = fromParam;
    }

    public String getType(int dim) {
        StringBuilder out = new StringBuilder("i32");
        if(fromParam) dim += 1;
        for(int i = dimensions.size()-1; i >= dim; i--) {
            out.insert(0, "[" + dimensions.get(i) + " x ");
            out.append(']');
        }
        return out.toString();
    }
}
