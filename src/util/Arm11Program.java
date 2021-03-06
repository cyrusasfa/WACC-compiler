package util;

import instructions.*;

import java.util.*;

//.text
//   ???
//.global Main
//main:

public class Arm11Program {

    public static final String PRINT_STRING_NAME = "p_print_string";
    public static final String PRINT_BOOL_NAME = "p_print_bool";
    public static final String PRINT_INT_NAME = "p_print_int";
    public static final String PRINT_REF_NAME = "p_print_reference";
    public static final String PRINTLN_NAME = "p_print_ln";
    public static final String PRINT_CHAR_NAME = "putchar";
    public static final String READ_INT_NAME = "p_read_int";
    public static final String READ_CHAR_NAME = "p_read_char";
    public static final String ARRAY_BOUND_NAME = "p_check_array_bounds";
    public static final String OVERFLOW_NAME = "p_throw_overflow_error";
    public static final String DIVIDE_BY_ZERO_NAME = "p_check_divide_by_zero";
    public static final String RUNTIME_ERR_NAME = "p_throw_runtime_error";
    public static final String FREE_PAIR_NAME = "p_free_pair" ;
    public static final String NULL_PTR_NAME = "p_check_null_pointer";
    public static String decode(String input) {
        return input.replace("\\0", "\0").replace("\\b", "\b").replace("\\n", "\n").replace("\\f", "\f").replace("\\r", "\r").replace("\\\"", "\"").replace("\\'", "'").replace("\\\\", "\\");
    }

    Map<String, List<Instruction>> functions = new LinkedHashMap<>();

    Stack<List<Instruction>> scope = new Stack<>();

    List<Instruction> currentFunction;

    List<Instruction> globalCode = new LinkedList<>();

    int numMsgLabels = 0;

    public Arm11Program() {
        functions.put("global", globalCode);
        scope.push(globalCode);
        currentFunction = globalCode;
    }

    public Map<String, List<Instruction>> getCode() {
        return functions;
    }

    public void add(Instruction ins) {
        if(ins == null) return;
        if(currentFunction == null) globalCode.add(ins);
        else currentFunction.add(ins);
    }

    public Instruction getLastInstruction() {
        if (currentFunction == null) return globalCode.get(globalCode.size() - 1);
        else return currentFunction.get(currentFunction.size() - 1);
    }

    public String addMsgLabel(String msg) {
        if(numMsgLabels == 0) globalCode.add(new DataLabel());
        MsgLabel instruction = new MsgLabel(msg, numMsgLabels);
        globalCode.add(instruction);
        numMsgLabels++;
        return instruction.getIdent();
    }

    public String getMsgLabel(String msg) {
        for(Instruction ins : globalCode) {
            if(ins instanceof MsgLabel && ((MsgLabel) ins).getMsg().equals(msg)) {
                return ((MsgLabel) ins).getIdent();
            }
        }
        return addMsgLabel(msg);
    }


    public void addPrintString() {
        String printStringFunc = getMsgLabel("%.*s\\0");
        startFunction(PRINT_STRING_NAME);
        add(new LoadInstruction(Registers.r1, new Operand2(Registers.r0, true)));
        add(new AddInstruction(Registers.r2, Registers.r0, new Operand2('#', 4)));
        add(new LoadInstruction(Registers.r0, new Operand2(printStringFunc)));
        endPrintFunction("printf");
    }

    public void addPrintBool() {
        String printTrueFunc = getMsgLabel("true\\0");
        String printFalseFunc = getMsgLabel("false\\0");
        startFunction(PRINT_BOOL_NAME);
        add(new CompareInstruction(Registers.r0, new Operand2('#', 0)));
        add(new LoadNotEqualInstruction(Registers.r0, new Operand2(printTrueFunc)));
        add(new LoadEqualInstruction(Registers.r0, new Operand2(printFalseFunc)));
        endPrintFunction("printf");
    }

    public void addPrintInt() {
        String printIntFunc = getMsgLabel("%d\\0");
        startFunction(PRINT_INT_NAME);
        add(new MoveInstruction(Registers.r1, Registers.r0));
        add(new LoadInstruction(Registers.r0, new Operand2(printIntFunc)));
        endPrintFunction("printf");
    }


    public void addPrintRef() {
        String printRefFunc = getMsgLabel("%p\\0");
        startFunction(PRINT_REF_NAME);
        add(new MoveInstruction(Registers.r1, Registers.r0));
        add(new LoadInstruction(Registers.r0, new Operand2(printRefFunc)));
        endPrintFunction("printf");
    }

    public void addPrintlnFunc() {
        String printlnFunc = addMsgLabel("\\0");
        startFunction(PRINTLN_NAME);
        add(new LoadInstruction(Registers.r0, new Operand2(printlnFunc)));
        endPrintFunction("puts");
    }

    public void addReadInt() {
        String readIntFunc = getMsgLabel("%d\\0");
        startFunction(READ_INT_NAME);
        add(new MoveInstruction(Registers.r1, Registers.r0));
        add(new LoadInstruction(Registers.r0, new Operand2(readIntFunc)));
        endReadFunction();
    }

    public void addReadChar() {
        String readCharFunc = getMsgLabel(" %c\\0");
        startFunction(READ_CHAR_NAME);
        add(new MoveInstruction(Registers.r1, Registers.r0));
        add(new LoadInstruction(Registers.r0, new Operand2(readCharFunc)));
        endReadFunction();
    }

    public void addRuntimeErrFunction() {
        startErrorFunction(RUNTIME_ERR_NAME);
        add(new BranchLinkInstruction(PRINT_STRING_NAME));
        if(!functionDeclared(PRINT_STRING_NAME)) addPrintString();
        add(new MoveInstruction(Registers.r0, -1));
        add(new BranchLinkInstruction("exit"));
        endThrowFunction();
    }

    public void addOverflowError() {
        String overflowFunc;
        overflowFunc = getMsgLabel("OverflowError: the result is too small/large to store in a 4-byte signed-integer.\\n");
        startErrorFunction(OVERFLOW_NAME);
        add(new LoadInstruction(Registers.r0, new Operand2(overflowFunc)));
        endErrorFunction();
    }

    public void addDivideByZeroError() {
        String divideByZeroFunc = getMsgLabel("DivideByZeroError: divide or modulo by zero\\n\\0");
        startFunction(DIVIDE_BY_ZERO_NAME);
        add(new CompareInstruction(Registers.r1, new Operand2('#', 0)));
        add(new LoadEqualInstruction(Registers.r0, new Operand2(divideByZeroFunc)));
        add(new BranchLinkEqualInstruction(RUNTIME_ERR_NAME));
        if(!functionDeclared(RUNTIME_ERR_NAME)) addRuntimeErrFunction();
        endFunction();
    }

    public void addArrayBoundError() {
        String arrayBoundNegFunc = getMsgLabel("ArrayIndexOutOfBoundsError: negative index\\n\\0");
        String arrayBoundTooLargeFunc = getMsgLabel("ArrayIndexOutOfBoundsError: index too large\\n\\0");
        startFunction(ARRAY_BOUND_NAME);
        add(new CompareInstruction(Registers.r0, new Operand2('#', 0)));
        add(new LoadLessThanInstruction(Registers.r0, new Operand2(arrayBoundNegFunc)));
        add(new BranchLinkLessThanInstruction(RUNTIME_ERR_NAME));
        add(new LoadInstruction(Registers.r1, new Operand2(Registers.r1, true)));
        add(new CompareInstruction(Registers.r0, new Operand2(Registers.r1)));
        add(new LoadCarrySetInstruction(Registers.r0, new Operand2(arrayBoundTooLargeFunc)));
        add(new BranchLinkCarrySetInstruction(RUNTIME_ERR_NAME));
        if(!functionDeclared(RUNTIME_ERR_NAME)) addRuntimeErrFunction();
        endFunction();
    }

    public void addNullPtrError(){
        String nullPtrFunc = getMsgLabel("NullReferenceError: dereference a null reference\\n\\0");
        startFunction(NULL_PTR_NAME);
        add(new CompareInstruction(Registers.r0, new Operand2('#', 0)));
        add(new LoadEqualInstruction(Registers.r0, new Operand2(nullPtrFunc)));
        add(new BranchLinkEqualInstruction(RUNTIME_ERR_NAME));
        if(!functionDeclared(RUNTIME_ERR_NAME)) addRuntimeErrFunction();
        endFunction();
    }

    public void addFreePair() {
        String freePairFunc = getMsgLabel("NullReferenceError: dereference a null reference\\n\\0");
        startFunction(FREE_PAIR_NAME);
        add(new CompareInstruction(Registers.r0, new Operand2('#', 0)));
        add(new LoadEqualInstruction(Registers.r0, new Operand2(freePairFunc)));
        add(new BranchEqualInstruction(RUNTIME_ERR_NAME));
        add(new PushInstruction(Registers.r0));
        add(new LoadInstruction(Registers.r0, new Operand2(Registers.r0, true)));
        add(new BranchLinkInstruction("free"));
        add(new LoadInstruction(Registers.r0, new Operand2(Registers.sp, true)));
        add(new LoadInstruction(Registers.r0, new Operand2(Registers.r0, 4)));
        add(new BranchLinkInstruction("free"));
        add(new PopInstruction(Registers.r0));
        add(new BranchLinkInstruction("free"));
        if(!functionDeclared(RUNTIME_ERR_NAME)) addRuntimeErrFunction();
        endFunction();
    }

    public void startFunction(String name) {
        startErrorFunction(name);
        currentFunction.add(new PushInstruction(Registers.lr));
    }


    private void startErrorFunction(String name) {
        currentFunction = new LinkedList<>();
        functions.put(name, currentFunction);
        currentFunction.add(new LabelInstruction(name));
        scope.push(currentFunction);
    }

    public void endFunction() {
        currentFunction.add(new PopInstruction(Registers.pc));
        scope.pop();
        currentFunction = scope.peek();
    }

    public void endUserFunction() {
        currentFunction.add(new PopInstruction(Registers.pc));
        currentFunction.add(new LtorgDirective());
        scope.pop();
        currentFunction = scope.peek();
    }

    private void endErrorFunction() {
        add(new BranchLinkInstruction(RUNTIME_ERR_NAME));
        if(!functionDeclared(RUNTIME_ERR_NAME)) addRuntimeErrFunction();
        endThrowFunction();
    }

    private void endThrowFunction() {
        scope.pop();
        currentFunction = scope.peek();
    }

    public void endPrintFunction(String branch) {
        add(new AddInstruction(Registers.r0, Registers.r0, new Operand2('#', 4)));
        add(new BranchLinkInstruction(branch));
        add(new MoveInstruction(Registers.r0, 0));
        add(new BranchLinkInstruction("fflush"));
        endFunction();
    }

    private void endReadFunction() {
        add(new AddInstruction(Registers.r0, Registers.r0, new Operand2('#', 4)));
        add(new BranchLinkInstruction("scanf"));
        endFunction();
    }

    public boolean functionDeclared(String name) {
        return functions.containsKey(name);
    }

    public String toCode() {
        StringBuilder program = new StringBuilder();

        for(List<Instruction> func : functions.values()) {
            for(Instruction ins : func) {
                for(int i = 0; i < ins.indentation; i++) program.append("\t");
                program.append(ins.toCode());
                program.append('\n');
            }
        }

        return program.toString();
    }
}
