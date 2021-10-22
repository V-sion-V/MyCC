import java.util.ArrayList;
import java.util.regex.Pattern;

public class CalculateExpValue {
    private static CalculateExpValue instance = null;

    private CalculateExpValue() {}

    public static CalculateExpValue getInstance() {
        if(instance == null) instance = new CalculateExpValue();
        return instance;
    }

    public int getNumberValue(SyntaxTree tree) {
        ArrayList<SyntaxTree> child = tree.getChild();
        if(tree.type == SyntaxTree.Exp) {
            return getNumberValue(child.get(0));
        } else if (tree.type == SyntaxTree.AddExp) {
            int sum = getNumberValue(child.get(0));
            for(int i = 1; i+1<child.size();i+=2) {
                int temp = getNumberValue(child.get(i+1));
                if(child.get(i).type==Token.PLUS) {
                    sum += temp;
                } else {
                    sum -= temp;
                }
            }
            return sum;
        } else if (tree.type == SyntaxTree.MulExp) {
            int product = getNumberValue(child.get(0));
            for(int i = 1; i+1<child.size();i+=2) {
                int temp = getNumberValue(child.get(i+1));
                if(child.get(i).type==Token.MULT) {
                    product *= temp;
                } else if(child.get(i).type==Token.DIV) {
                    if(temp == 0) System.exit(2);
                    product /= temp;
                } else {
                    if(temp == 0) System.exit(2);
                    product %= temp;
                }
            }
            return product;
        } else if (tree.type == SyntaxTree.UnaryExp) {
            int sgn = 1;
            for(int i = 0; i+1< child.size();i++) {
                if(child.get(i).type == Token.MINUS) {
                    sgn*=-1;
                }
            }
            return getNumberValue(child.get(child.size()-1))*sgn;
        } else if(tree.type == SyntaxTree.PrimaryExp) {
            if(child.size()==1) {
                return Integer.parseInt(child.get(0).content);
            } else {
                return getNumberValue(child.get(1));
            }
        } else System.exit(2);
        return 0;
    }
}
