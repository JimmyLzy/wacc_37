package WACC;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zl4214 on 01/12/15.
 */
public class Stack {

    private int size;
    private boolean ifDeclarationCodeGenerated = false;
    public static final int MAX_STACK_SIZE = 1024;

    private List<StackElem> stack = new ArrayList<>();

    public void add(String ident, int sizeOfType) {
        StackElem stackElem = new StackElem(ident, sizeOfType); 
        stack.add(stackElem);
        size += stackElem.getSize();
        stackElem.setOffset(size);
    }

    public int getStackElemOffset(String ident) {
        int offset = 0;
        for(StackElem stackElem : stack) {
            if (ident.equals(stackElem.getIdent())) {
                offset = size - stackElem.getOffset();
                break;
            }
        }
        return offset;
    }

    public boolean IfDeclarationCodeGenerated() {
        return ifDeclarationCodeGenerated;
    }

    public void setIfDeclarationCodeGenerated(boolean ifDeclarationCodeGenerated) {
        this.ifDeclarationCodeGenerated = ifDeclarationCodeGenerated;
    }

    public int getSize() {
        return size;
    }

    public void incSize(int sizeOfType) {
        size += sizeOfType;
    }

    public void decSize(int sizeOfType) {
        size -= sizeOfType;
    }


    private class StackElem {

        private String ident;
        private int sizeOfType;
        private int offset;


        private StackElem(String ident, int sizeOfType) {
            this.ident = ident;
            this.sizeOfType = sizeOfType;
        }

        public int getSize() {
            return sizeOfType;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }

        public String getIdent() {
            return ident;
        }
    }
}
