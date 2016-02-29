package mipsparser_new.BMSFS;

import mipsparser_new.BMSFS.*;
import java.util.*;

public class Instruction {

    private String mnemonic;    // the instruction name like add, addi
    private int numOperands;    // number of operands in this instruction
    private int[] operands;     // all operands 1st operand @ index 0 and so on .
    boolean[] isImmediate; // true if last operand is immediate not a register .
    private int[] sign; // only used in lw , sw instruction .
    private String machineCode; // Machine code of this instruction
    private String levelName; // if the Instruction contains a level then it's name is stored hr
    // otherwise levelName="";
    private String sourceString; // the string which was used to get this instruction
    public static int startAddressOfInst = 0x4000000;
    public static HashMap<String, Integer> addressOfLevels = new HashMap<String, Integer>();

    public Instruction() {
        operands = new int[4];
        isImmediate = new boolean[4];
        sign = new int[4];
        levelName = "";
    }

    public Instruction(String _mnemonic) {
        this.mnemonic = _mnemonic;
        operands = new int[4];
        isImmediate = new boolean[4];
        sign = new int[4];
        levelName = "";
    }

    public Instruction(String _mnemonic, int _numOperands, int[] _operands, String _machineCode) {
        this.mnemonic = _mnemonic;
        this.numOperands = _numOperands;
        this.operands = _operands;
        this.machineCode = _machineCode;
        operands = new int[4];
        isImmediate = new boolean[4];
        sign = new int[4];
        levelName = "";
    }

    /**
     * Get operation mnemonic
     * @return operation mnemonic (e.g. addi, sw)
     */
    public String getName() {
        return mnemonic;
    }

    public int getNumberOfOperands() {
        return numOperands;
    }

    public int[] getOperands() {
        return operands;
    }

    public String getMachineCode() {
        return machineCode;
    }

    public String getSourceString() {
        return sourceString;
    }

    public static boolean isRegister(String str) {
        return str.startsWith("$");
    }

    public static boolean isImmediate(String str) {
        if (str.equals("")) {
            return false;
        }
        if (str.charAt(0) == '-') {
            str = str.substring(1);
        }
        if (str.startsWith("0x")) { // hexadecimal immediate value
            for (int i = 2; i < str.length(); i++) {
                char ch = str.charAt(i);
                if (!((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F'))) {
                    return false;
                }
            }
        } else { // decimal immediate value
            for (int i = 0; i < str.length(); i++) {
                char ch = str.charAt(i);
                if (ch < '0' || ch > '9') {
                    return false;
                }
            }
        }
        return true;
    }

    public Error parseLoadStoreInstr(String str, int lineNoInSource) {
        Error err = new Error();

        // operand[0] <- Rdest
        // operand[1] <- Rx if present
        // operand[2] <- immediate value , if Rx not present this value will be in operand[1]
        for (int i = 0; i < 4; i++) {
            isImmediate[i] = false;
            sign[i] = 1;
        }
        //	System.out.println("********* INSIDE LOAD STORE PARSING ******** PARSING STRING -->"+str+"<<--");

        str = str.replaceAll(" ", "");
        String A[] = str.split(",");
        operands[0] = Register.getRegisterNumberFromString(A[0]);
        if (operands[0] == -1) {
            err = new Error("Syntax Error @ Line " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
            return err;
        }
        numOperands = 1;
        str = A[1];
        if (str.contains("+")) { // in case of + , operand before + is a label only
            A = str.split("+");
            if (Memory.localSymbolTable.containsKey(A[0])) {
                operands[numOperands] = Memory.localSymbolTable.get(A[0]);
                isImmediate[numOperands++] = true;
            } else {
                err = new Error("Undefined label name : " + A[0] + " Or Syntax error @ line " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                return err;
            }
            str = A[1];
            if (str.contains("(")) {
                int ind1 = str.indexOf("(");
                int ind2 = str.indexOf(")");
                if (ind2 == -1) { // mismatching opening and closing brackets
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                String reg = str.substring(ind1 + 1, ind2);
                operands[numOperands++] = Register.getRegisterNumberFromString(reg);
                if (operands[numOperands - 1] == -1) { // invalid register
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                str = str.replaceAll("\\(.*\\)", "");
            }
            if (str.equals("")) { // parsing done
                return err;
            } else if (isRegister(str)) {
                operands[numOperands] = Register.getRegisterNumberFromString(str);
                if (operands[numOperands] == -1) { // invalid register
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                isImmediate[numOperands++] = false;
            } else if (isImmediate(str)) {
                operands[numOperands] = MyLibrary.fromStringToInt(str);
                isImmediate[numOperands++] = true;
            } else {
                err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                return err;
            }
        } else if (str.contains("-")) { // in case of - , operand before - is a label only
            A = str.split("-");
            if (Memory.localSymbolTable.containsKey(A[0])) {
                operands[numOperands] = Memory.localSymbolTable.get(A[0]);
                isImmediate[numOperands++] = true;
            } else {
                err = new Error("Undefined label name : " + A[0] + " Or Syntax error @ line " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                return err;
            }
            str = A[1];
            if (str.contains("(")) {
                int ind1 = str.indexOf("(");
                int ind2 = str.indexOf(")");
                if (ind2 == -1) { // mismatching opening and closing brackets
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                String reg = str.substring(ind1 + 1, ind2);
                sign[numOperands] = -1;
                operands[numOperands++] = Register.getRegisterNumberFromString(reg);
                if (operands[numOperands - 1] == -1) { // invalid register
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                str = str.replaceAll("\\(.*\\)", "");
            }
            if (str.equals("")) { // parsing done
                return err;
            } else if (isRegister(str)) {
                operands[numOperands] = Register.getRegisterNumberFromString(str);
                if (operands[numOperands] == -1) { // invalid register
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                sign[numOperands] = -1;
                isImmediate[numOperands++] = false;
            } else if (isImmediate(str)) {
                operands[numOperands] = MyLibrary.fromStringToInt(str);
                sign[numOperands] = -1;
                isImmediate[numOperands++] = true;
            } else {
                err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                return err;
            }
        } else {
            // la $Rdest , symbol
            // lw $Rdest , imm
            // lw $Rdest , (Rx)
            // lw $Rdest , imm(Rx)
            // lw $Rdest , Ry(Rx)
            if (Memory.localSymbolTable.containsKey(str)) { // str is a symbol  (e.g  la $Rdest , symbol)

                operands[numOperands] = Memory.localSymbolTable.get(str);
                isImmediate[numOperands++] = true;
            } else {
                if (str.contains("(")) {
                    int ind1 = str.indexOf("(");
                    int ind2 = str.indexOf(")");
                    if (ind2 == -1) {
                        err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                        return err;
                    }
                    String reg = str.substring(ind1 + 1, ind2);

                    operands[numOperands++] = Register.getRegisterNumberFromString(reg);
                    if (operands[numOperands - 1] == -1) {
                        err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                        return err;
                    }

                    str = str.replaceAll("\\(.*\\)", "");

                }
                if (str.equals("")) { // parsing done
                    return err;
                } else if (isRegister(str)) {
                    operands[numOperands] = Register.getRegisterNumberFromString(str);
                    if (operands[numOperands] == -1) {
                        err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                        return err;
                    }
                    isImmediate[numOperands++] = false;
                } else if (isImmediate(str)) {
                    operands[numOperands] = MyLibrary.fromStringToInt(str);
                    isImmediate[numOperands++] = true;
                } else {
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
            }
        }
        return err;
    }

    public Error parseBasicInstrucction(String str, int lineNoInSource) {
        Error err = new Error();

        for (int i = 0; i < 4; i++) {
            sign[i] = 1;
        }
        numOperands = 0;
        levelName = "";
        String A[] = str.split(",");
        for (int i = 0; i < A.length; i++) {
            if (isRegister(A[i])) {
                operands[i] = Register.getRegisterNumberFromString(A[i]);
                if (operands[i] == -1) {
                    err = new Error("Syntax Error @ Line " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                isImmediate[i] = false;
                numOperands++;
            } else if (isImmediate(A[i])) {
                operands[i] = MyLibrary.fromStringToInt(A[i]);
                isImmediate[i] = true;
                numOperands++;
            } else { // it's level
                levelName = A[i];
            }
        }

        return err;
    }

    public Error parsInstruction(String str, int lineNoInSource) {
        Error err = new Error();

        str = MyLibrary.removeBeginningAndEndingSpace(str);

        sourceString = str;

        mnemonic = getMnemonicFromString(str);
        str = str.substring(mnemonic.length());

        str = str.replaceAll(" ", ""); // all space characters are removed from the instruction
        if (str.equals("")) {
            System.out.println("Null Str Found");
        }
        if (mnemonic.equalsIgnoreCase("add")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("addi")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("addiu")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("addu")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("and")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("andi")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("b")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("beq")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("beqz")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("bge")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("bgez")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("bgezal")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("bgt")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("bgtz")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("ble")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("blez")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("blt")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("bltz")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("bltzal")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("bne")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("bnez")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("clo")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("clz")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("div")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("divu")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("j")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("jal")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("jalr")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("jr")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("la")) {  // CHECK LOAD STORE PARSING 
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("lb")) {
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("lh")) {
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("li")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("lw")) {
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("mfhi")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("mflo")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("mthi")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("mtlo")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("move")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("movn")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("movz")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("mul")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("mulo")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("mult")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("neg")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("nor")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("not")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("or")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("ori")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("sll")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("sllv")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("slt")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("slti")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("sltiu")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("sltu")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("srl")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("srlv")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("sb")) {
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("seb")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("seh")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("sh")) {
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("sub")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("sw")) {
            // have to handle
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("xor")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.equalsIgnoreCase("xori")) {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else {
            err = new Error("Unsupported or invalid instruction : " + mnemonic, Error.ASSEMBLE_TIME_ERROR);
        }
        return err;
    }

    private String getMnemonicFromString(String str) {
        int ind = str.length();
        for (int i = 0; i < str.length(); i++) {
            if (MyLibrary.isSpaceChar(str.charAt(i))) {
                ind = i;
                break;
            }
        }
        return str.substring(0, ind);
    }

    public Error fillLevelNameWithAddress() {
        Error err = new Error();
        if (levelName.equals("")) {
            return err;
        }

        if (!addressOfLevels.containsKey(levelName)) {
            err = new Error("Undefined Label Name : " + levelName + " Or Syntax error ! ", Error.ASSEMBLE_TIME_ERROR);
            return err;
        }

        isImmediate[numOperands] = true;
        operands[numOperands++] = addressOfLevels.get(levelName);
        return err;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public boolean[] getIsImmediate() {
        return isImmediate;
    }

    public int[] getSign() {
        return sign;
    }

    public Error runSingleInstruction() {

        /* Here we go , the code to run each and every instruction is here ..
         * */
        Error err = new Error();
        int oldPc = Register.getPC();

        if (mnemonic.equalsIgnoreCase("add")) {
            int add1 = Register.getRegValue(operands[1]); // rs
            int add2 = isImmediate[2] ? operands[2] : Register.getRegValue(operands[2]); //rt
            if (isImmediate[2]) {
                // is immediate value is greater than 16 bit , error
                if (add2 >= (1 << 16)) {
                    err = new Error("Runtime Error: Immediate constant greater than (1<<16) in add instruction ", Error.RUN_TIME_ERROR);
                    return err;
                }
                add2 = add2 << 16 >> 16;
            }

            int sum = add1 + add2;
            // overflow on A+B detected when A and B have same sign and A+B has other sign.
            if ((add1 >= 0 && add2 >= 0 && sum < 0)
                    || (add1 < 0 && add2 < 0 && sum >= 0)) {
                // overflow error , dest reg will not be updated . (LAKSHYA)
                err = new Error("Overflow detected in add instruction , destination register didn't change", Error.RUN_TIME_WARNING);
            } else {
                Register.updateRegister(operands[0], sum);
            }

            Register.incrementPCToNextInstruction();

        } else if (mnemonic.equalsIgnoreCase("addi")) {
            int add1 = Register.getRegValue(operands[1]);
            int add2 = operands[2]; //is immediate value is greater than 16 bit , error (LAKSHYA)
            if (add2 >= (1 << 16)) {
                err = new Error("Runtime Error: Immediate constant greater than (1<<16) in add instruction ", Error.RUN_TIME_ERROR);
                return err;
            }
            add2 = add2 << 16 >> 16;
            int sum = add1 + add2;
            // overflow on A+B detected when A and B have same sign and A+B has other sign.
            if ((add1 >= 0 && add2 >= 0 && sum < 0)
                    || (add1 < 0 && add2 < 0 && sum >= 0)) {
                // overflow error , dest reg will not be updated .
                err = new Error("Overflow detected in addi instruction , destination register didn't change", Error.RUN_TIME_WARNING);
            } else {
                Register.updateRegister(operands[0], sum);
            }

            Register.incrementPCToNextInstruction();

        } else if (mnemonic.equalsIgnoreCase("addiu")) {
            int add1 = Register.getRegValue(operands[1]);
            int add2 = operands[2]; //is immediate value is greater than 16 bit , error (LAKSHYA)
            if (add2 >= (1 << 16)) {
                err = new Error("Runtime Error: Immediate constant greater than (1<<16) in add instruction ", Error.RUN_TIME_ERROR);
                return err;
            }
            add2 = add2 << 16 >> 16;
            int sum = add1 + add2;

            Register.updateRegister(operands[0], sum);

            Register.incrementPCToNextInstruction();

        } else if (mnemonic.equalsIgnoreCase("addu")) {
            int add1 = Register.getRegValue(operands[1]); // rs
            int add2 = isImmediate[2] ? operands[2] : Register.getRegValue(operands[2]); //rt
            if (isImmediate[2]) {
                // is immediate value is greater than 16 bit , error 
                if (add2 >= (1 << 16)) {
                    err = new Error("Runtime Error: Immediate constant greater than (1<<16) in add instruction ", Error.RUN_TIME_ERROR);
                    return err;
                }
                add2 = add2 << 16 >> 16;
            }

            int sum = add1 + add2;

            Register.updateRegister(operands[0], sum);

            Register.incrementPCToNextInstruction();

        } else if (mnemonic.equalsIgnoreCase("and")) {
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    & Register.getRegValue(operands[2]));

            Register.incrementPCToNextInstruction();


        } else if (mnemonic.equalsIgnoreCase("andi")) {
            // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    & (operands[2] & 0x0000FFFF)); // zero extended 16 bit

            Register.incrementPCToNextInstruction();


        } else if (mnemonic.equalsIgnoreCase("b")) {

            Register.updatePcAbsolute(operands[0]);
            int newPc = Register.getPC();


        } else if (mnemonic.equalsIgnoreCase("beq")) {
            if (Register.getRegValue(operands[0]) == Register.getRegValue(operands[1])) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("beqz")) {
            if (Register.getRegValue(operands[0]) == 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("bge")) {
            if (Register.getRegValue(operands[0]) >= Register.getRegValue(operands[1])) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("bgez")) {
            if (Register.getRegValue(operands[0]) >= 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("bgezal")) {
            if (Register.getRegValue(operands[0]) >= 0) {
                Register.updateRegister("$ra", Register.getPC() + 4); // DELAY BRANCH , PC+8
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("bgt")) {
            int op0 = isImmediate[0] ? operands[0] : Register.getRegValue(operands[0]);
            int op1 = isImmediate[1] ? operands[1] : Register.getRegValue(operands[1]);
            if (op0 > op1) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("bgtz")) {
            if (Register.getRegValue(operands[0]) > 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("ble")) {
            if (Register.getRegValue(operands[0]) <= Register.getRegValue(operands[1])) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("blez")) {
            if (Register.getRegValue(operands[0]) <= 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("blt")) {
            if (Register.getRegValue(operands[0]) < Register.getRegValue(operands[1])) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("bltz")) {
            if (Register.getRegValue(operands[0]) < 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("bltzal")) {
            if (Register.getRegValue(operands[0]) < 0) {
                Register.updateRegister("ra", Register.getPC() + 4);
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("bne")) {
            if (Register.getRegValue(operands[0]) != Register.getRegValue(operands[1])) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("bnez")) {
            if (Register.getRegValue(operands[0]) != 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.equalsIgnoreCase("clo")) {
            int sourceRegValue = Register.getRegValue(operands[1]);
            int temp = 32;
            for (int i = 31; i >= 0; i--) {
                if (((1 << i) & (sourceRegValue)) == 0) {
                    temp = 31 - i;
                    break;
                }
            }
            //System.out.println("temp = " + temp);
            Register.updateRegister(operands[0], temp);
            Register.incrementPCToNextInstruction();

        } else if (mnemonic.equalsIgnoreCase("clz")) {
            int sourceRegValue = Register.getRegValue(operands[1]);
            int temp = 32;
            for (int i = 31; i >= 0; i--) {
                if (((1 << i) & (sourceRegValue)) != 0) {
                    temp = 31 - i;
                    break;
                }
            }
            Register.updateRegister(operands[0], temp);
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("div")) {
            // div can have 2 or 3 operands
            if (numOperands == 2) {
                if (Register.getRegValue(operands[1]) == 0) {
                    // division by zero , ERROR
                    err = new Error("Runtime Error : Division by 0 in div instruction", Error.RUN_TIME_ERROR);
                    return err;
                } else {

                    Register.updateRegister("$hi", Register.getRegValue(operands[0])
                            % Register.getRegValue(operands[1]));
                    Register.updateRegister("$lo", Register.getRegValue(operands[0])
                            / Register.getRegValue(operands[1]));
                }
            } else if (numOperands == 3) {
                if (Register.getRegValue(operands[2]) == 0) {
                    // division by zero ,  Error
                    err = new Error("Runtime Error : Division by 0 in div instruction", Error.RUN_TIME_ERROR);
                    return err;
                } else {

                    Register.updateRegister("$hi", Register.getRegValue(operands[1])
                            % Register.getRegValue(operands[2]));
                    Register.updateRegister("$lo", Register.getRegValue(operands[1])
                            / Register.getRegValue(operands[2]));

                    Register.updateRegister(operands[0], Register.getRegValue("$lo"));

                }
            }

            Register.incrementPCToNextInstruction();


        } else if (mnemonic.equalsIgnoreCase("divu")) {
            long rs = Register.getRegValue(operands[0]);
            long rt = Register.getRegValue(operands[1]);

            if (rt == 0) {
                err = new Error("Runtime Error : Division by 0 in divu instruction", Error.RUN_TIME_ERROR);
                return err;
            }

            rs = (rs << 32) >>> 32;
            rt = (rt << 32) >>> 32;

            long loValue = rs / rt;
            long hiValue = rs % rt;

            // sign extend loValue and hiValue
            loValue = (loValue << 32) >> 32;
            hiValue = (hiValue << 32) >> 32;

            Register.updateLO((int) loValue);
            Register.updateHI((int) hiValue);

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("j")) {
            Register.updatePcAbsolute(operands[0]);


        } else if (mnemonic.equalsIgnoreCase("jal")) {
            Register.updateRegister(31, Register.getPC() + 4); // $ra is 31st reg

            Register.updatePcAbsolute(operands[0]);


        } else if (mnemonic.equalsIgnoreCase("jalr")) {
            Register.updateRegister("$ra", Register.getPC() + 4);

            Register.updatePcAbsolute(Register.getRegValue(operands[0]));


        } else if (mnemonic.equalsIgnoreCase("jr")) {

            Register.updatePcAbsolute(Register.getRegValue(operands[0]));

            return err;
        } else if (mnemonic.equalsIgnoreCase("la")) {
            // take care of address
            int address = 0;
            for (int i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }
            Register.updateRegister(operands[0], address);
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.equalsIgnoreCase("lb")) {
            int address = 0;
            for (int i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }

            Register.updateRegister(operands[0], Memory.readByte(address));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("lh")) {
            int address = 0;
            for (int i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }
            Register.updateRegister(operands[0], Memory.readHalfWord(address));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("li")) {
            Register.updateRegister(operands[0], operands[1]); // ERROR CHECKING  (negative also , fit into 32 bit )
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.equalsIgnoreCase("lw")) {
            int address = 0;
            for (int i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }

            Register.updateRegister(operands[0], Memory.readWord(address));

            Register.incrementPCToNextInstruction();


        } else if (mnemonic.equalsIgnoreCase("mfhi")) {
            Register.updateRegister(operands[0], Register.getHI());
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.equalsIgnoreCase("mflo")) {
            Register.updateRegister(operands[0], Register.getLO());
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.equalsIgnoreCase("mthi")) {
            Register.updateHI(Register.getRegValue(operands[0]));
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.equalsIgnoreCase("mtlo")) {
            Register.updateLO(Register.getRegValue(operands[0]));
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.equalsIgnoreCase("move")) {
            Register.updateRegister(operands[0], Register.getRegValue(operands[1]));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("movn")) {
            if (Register.getRegValue(operands[2]) != 0) {
                Register.updateRegister(operands[0], Register.getRegValue(operands[1]));
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("movz")) {
            if (Register.getRegValue(operands[2]) == 0) {
                Register.updateRegister(operands[0], Register.getRegValue(operands[1]));
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("mul")) {
            if (numOperands == 2) {
                long op1 = (long) Register.getRegValue(operands[0]);
                long op2 = isImmediate[1] ? (long) (operands[1]) : (long) Register.getRegValue(operands[1]);
                if (isImmediate[1]) {
                    op2 = (op2 << 16) >> 16;
                }

                long product = op1 * op2;

                Register.updateRegister("$hi", (int) (product >> 32));
                Register.updateRegister("$lo", (int) ((product << 32) >> 32));
            } else if (numOperands == 3) {
                long op1 = (long) Register.getRegValue(operands[1]);
                long op2 = isImmediate[2] ? (long) (operands[2]) : (long) Register.getRegValue(operands[2]);
                if (isImmediate[2]) {
                    op2 = (op2 << 16) >> 16;
                }

                long product = op1 * op2;

                Register.updateRegister(operands[0], (int) ((product << 32) >> 32));

                Register.updateRegister("$hi", (int) (product >> 32));
                Register.updateRegister("$lo", (int) ((product << 32) >> 32));
            } else {
                // ERROR
            }
            Register.incrementPCToNextInstruction();


        } // TO DO ------->
        // TO DO ------->
        // TO DO ------->
        // TO DO ------->
        else if (mnemonic.equalsIgnoreCase("mulo")) {
            long op1 = (long) Register.getRegValue(operands[1]);
            long op2 = isImmediate[2] ? (long) (operands[2]) : (long) Register.getRegValue(operands[2]);
            if (isImmediate[2]) {
                op2 = (op2 << 16) >> 16;
            }

            //   		System.out.println("In mulo op1 = "+op1+" op2= "+op2);

            long product = op1 * op2;
            // overflow exception needs to be generated in case of overflow (TO BE IMPLEMENTED)
            Register.updateRegister(operands[0], (int) ((product << 32) >> 32));

            Register.updateRegister("$hi", (int) (product >> 32));
            Register.updateRegister("$lo", (int) ((product << 32) >> 32));

            Register.incrementPCToNextInstruction();

        } else if (mnemonic.equalsIgnoreCase("mult")) {
            long op1 = (long) Register.getRegValue(operands[0]);
            long op2 = (long) Register.getRegValue(operands[1]);

            long product = op1 * op2;

            Register.updateRegister("$hi", (int) (product >> 32));
            Register.updateRegister("$lo", (int) ((product << 32) >> 32));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("neg")) {
            Register.updateRegister(operands[0], -Register.getRegValue(operands[1]));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("nor")) {
            Register.updateRegister(operands[0],
                    ~(Register.getRegValue(operands[1])
                    | Register.getRegValue(operands[2])));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("not")) {
            Register.updateRegister(operands[0],
                    ~(Register.getRegValue(operands[1])));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("or")) {
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    | Register.getRegValue(operands[2]));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("ori")) {
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    | (operands[2] & 0x0000FFFF));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("sb")) {
            int address = 0;
            for (int i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }
            Memory.storeByte(address, Register.getRegValue(operands[0]));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("seb")) {
            int rt = Register.getRegValue(1);

            Register.updateRegister(operands[0], (rt & 0xFF) << 24 >> 24);

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("seh")) {
            int rt = Register.getRegValue(1);

            Register.updateRegister(operands[0], (rt & 0xFFFF) << 16 >> 16);
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("sh")) {
            int address = 0;
            for (int i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }
            Memory.storeHalfWord(address, Register.getRegValue(operands[0]));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("sll")) {

            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1]) << operands[2]);
            // operand[2] should be less than 32 , otherwise ERROR 

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("sllv")) {
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1]) << (Register.getRegValue(operands[2]) & 0x0000001F));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("slt")) {
            int rs = Register.getRegValue(operands[1]);
            int rt = Register.getRegValue(operands[2]);
            if (rs < rt) {
                Register.updateRegister(operands[0], 1);
            } else {
                Register.updateRegister(operands[0], 0);
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("slti")) {
            int rs = Register.getRegValue(operands[1]);
            int rt = operands[2];
            if (rs < rt) {
                Register.updateRegister(operands[0], 1);
            } else {
                Register.updateRegister(operands[0], 0);
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("sltiu")) {
            long rs = Register.getRegValue(operands[1]);
            long immediate = operands[2];
            rs = (rs << 32) >>> 32;
            immediate = (immediate << 32) >>> 32;
            if (rs < immediate) {
                Register.updateRegister(operands[0], 1);
            } else {
                Register.updateRegister(operands[0], 0);
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("sltu")) {
            long rs = Register.getRegValue(operands[1]);
            long rt = Register.getRegValue(operands[2]);
            rs = (rs << 32) >>> 32;
            rt = (rt << 32) >>> 32;
            if (rs < rt) {
                Register.updateRegister(operands[0], 1);
            } else {
                Register.updateRegister(operands[0], 0);
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("srl")) {
            // must zero-fill, so use ">>>" instead of ">>".
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1]) >>> operands[2]);

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("srlv")) {
            // Mask all but low 5 bits of register containing shamt.Use ">>>" to zero-fill.
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    >>> (Register.getRegValue(operands[2]) & 0x0000001F));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("sub")) {
            int sub1 = Register.getRegValue(operands[1]);
            int sub2;
            if (isImmediate[2]) {
                sub2 = operands[2];
            } else {
                sub2 = Register.getRegValue(operands[2]);
            }
            int dif = sub1 - sub2;
            // overflow on A-B detected when A and B have opposite signs and A-B has B's sign
            if ((sub1 >= 0 && sub2 < 0 && dif < 0)
                    || (sub1 < 0 && sub2 >= 0 && dif >= 0)) {
                // overflow , Rdest should not be updated
            } else {
                Register.updateRegister(operands[0], dif);
            }

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("subu")) {
            int sub1 = Register.getRegValue(operands[1]);
            int sub2;
            if (isImmediate[2]) {
                sub2 = operands[2];
            } else {
                sub2 = Register.getRegValue(operands[2]);
            }
            int dif = sub1 - sub2;

            Register.updateRegister(operands[0], dif);

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("sw")) {
            int address = 0;
            for (int i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }
            //System.out.println("\nStoring "+ Register.getRegValue(operands[0])+"at address = "+address);
            Memory.storeWord(address, Register.getRegValue(operands[0]));
            //System.out.println("Value at "+address+" is "+Memory.readWord(address)+"\n");

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("xor")) {
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    ^ Register.getRegValue(operands[2]));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.equalsIgnoreCase("xori")) {
            // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    ^ (operands[2] & 0x0000FFFF));

            Register.incrementPCToNextInstruction();
        } else {
            //	System.out.println("*****Instruction not supported in this version:******* MNEMONIC: "+mnemonic);
            err = new Error("Instruction " + mnemonic + " Not Supported ! ", Error.RUN_TIME_ERROR);
        }

        int newPc = Register.getPC();
        if (oldPc == newPc) {
            err = new Error("Infinite loop detected !", Error.RUN_TIME_ERROR);
        }
        return err;
    }
}
