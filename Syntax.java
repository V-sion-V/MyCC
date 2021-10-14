public class Syntax {
    Tokenizer in = new Tokenizer();
    String token;
    String out = "";

    public static void main(String[] args) {
        Syntax sy = new Syntax();

        //while(true) System.out.println(sy.in.getToken());

        sy.token = sy.in.getToken();
        sy.compUnit();
        System.out.println(sy.out);
        System.exit(0);
    }

    void compUnit() {
        out+="define ";
        if(token.equals("Int")) {
            funcDef();
        } else {
            System.exit(1);
        }
    }

    void funcDef() {
        out+="dso_local ";
        if(token.equals("Int")) {
            funcType();
            if(token.startsWith("Ident(")) {
                ident();
                if (token.equals("LPar")) {
                    out+="(";
                    token = in.getToken();
                    if (token.equals("RPar")) {
                        out+=") ";
                        token = in.getToken();
                        block();
                    } else {
                        System.exit(1);
                    }
                } else {
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }
        } else {
            System.exit(1);
        }
    }

    void funcType() {
        if(token.equals("Int")) {
            out+= "i32 ";
            token = in.getToken();
        } else {
            System.exit(1);
        }
    }

    void ident() {
        if(token.matches("Ident\\(main\\)")) {
            out+="@main ";
            token = in.getToken();
        } else {
            System.exit(1);
        }
    }

    void block() {
        if(token.equals("LBrace")) {
            out+="{\n";
            token = in.getToken();
            if(token.equals("Return")) {
                stmt();
                if (token.equals("RBrace")) {
                    out+="}\n";
                    token = in.getToken();
                } else {
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }
        } else {
            System.exit(1);
        }
    }

    void stmt() {
        if(token.equals("Return")) {
            out+= "ret ";
            token = in.getToken();
            if(token.matches("Number\\([0-9]*\\)")) {
                out += "i32 "+ token.substring(7,token.length()-1);
                token = in.getToken();
                if(token.equals("Semicolon")) {
                    out+="\n";
                    token = in.getToken();
                } else {
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }
        } else {
            System.exit(1);
        }
    }
}
