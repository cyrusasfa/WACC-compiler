package instructions;

import util.Register;

public class PushInstruction extends Instruction {

    private Register r;

    public PushInstruction(Register r) {
        this.r = r;
    }

    @Override
    public String toCode() {
        return "PUSH {" + r.toString() + "}";
    }
}
