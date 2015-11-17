package WACC;

/**
 * Created by yh6714 on 17/11/15.
 */
public class Main2 {

        public static void main(String[] args) {


        }



    public class root {
        root parent;

        public void setParent(Function parent) {
            this.parent = parent;
        }
    }

    public class Function extends root{

        public Function(Ident ident) {
            ident.setParent(this);
        }
    }

    public class Ident extends root{
        Ident() {

        }


    }
}
