public class Syntax {
    Tokenizer in = new Tokenizer();
    String token;

    public static void main(String[] args) {
        Syntax sy = new Syntax();

        //while(true) System.out.println(sy.in.getToken());

        sy.token = sy.in.getToken();
        sy.compUnit();
        System.exit(0);
    }

    void compUnit() {
        System.out.print("define ");
        if(token.equals("Int")) {
            funcDef();
        } else {
            System.exit(1);
        }
    }

    void funcDef() {
        System.out.print("dso_local ");
        if(token.equals("Int")) {
            funcType();
            if(token.startsWith("Ident(")) {
                ident();
                if (token.equals("LPar")) {
                    System.out.print("(");
                    token = in.getToken();
                    if (token.equals("RPar")) {
                        System.out.print(") ");
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
            System.out.print(" i32 ");
            token = in.getToken();
        } else {
            System.exit(1);
        }
    }

    void ident() {
        if(token.matches("Ident\\(main\\)")) {
            System.out.print(" @main ");
            token = in.getToken();
        } else {
            System.exit(1);
        }
    }

    void block() {
        if(token.equals("LBrace")) {
            System.out.println("{");
            token = in.getToken();
            if(token.equals("Return")) {
                stmt();
                if (token.equals("RBrace")) {
                    System.out.println("}");
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
            System.out.print("ret ");
            token = in.getToken();
            if(token.matches("Number\\([0-9]*\\)")) {
                System.out.print("i32 "+ token.substring(7,token.length()-1));
                token = in.getToken();
                if(token.equals("Semicolon")) {
                    System.out.println();
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
