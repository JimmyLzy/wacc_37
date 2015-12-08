package WACC;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yh6714 on 30/11/15.
 */
public class Registers {

    private List<Register> registers = new ArrayList<>();

    public Registers() {
        for (int i = 0; i < 15; i++) {
            Register register =  new Register(null, "r" + String.valueOf(i), i);
            registers.add(register);
        }
    }

    public List<Register> getEmptyRegisters() {
        List<Register> registersNotInUse = new ArrayList<>();
        for (Register register : registers) {
            if (register.isEmpty()) {
                registersNotInUse.add(register);
            }
        }
        return registersNotInUse;
    }

    public Register getFirstEmptyRegister() {
        for (Register register : registers) {
            if (register.isEmpty()) {
                return register;
            }
        }
        return null;
    }

    public Register get(int index) {
        return registers.get(index);
    }

    class Register<T> {
        private T value;
        private int index;
        private String registerNum;

        public Register(T value, String registerNum, int index) {
            this.value = value;
            this.registerNum = registerNum;
            this.index = index;
        }

        public Register(T value) {
            this.value = value;
        }

        public boolean isEmpty() {
            return value == null;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return registerNum;
        }

        public Register getNext() {
            return registers.get(index + 1);
        }
    }
}