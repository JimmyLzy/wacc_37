package WACC;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yh6714 on 30/11/15.
 */
public class Registers {

    private List<Register> registers = new ArrayList<>();
    private int messageCount = 0;

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

    public int getMessageCount() {
        return messageCount;
    }

    public void incMessageCount() {
        this.messageCount++;
    }

    private class Register<T> {
        private T value = null;

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
    }
}