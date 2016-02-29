
class Instruction {


    constructor(_mnemonic, _numOperands, _operands, _machineCode) {
		var mnemonic;    // the instruction name like add, addi
    	var numOperands;    // number of operands in this instruction
    	operands=[];     // all operands 1st operand @ index 0 and so on .
    	isImmediate=[]; // true if last operand is immediate not a register .
    	sign=[]; // only used in lw , sw instruction .
    	var machineCode; // Machine code of this instruction
    	var levelName; // if the Instruction contains a level then it's name is stored here
    	// otherwise levelName="";
    	var sourceString; // the string which was used to get this instruction
    	var startAddressOfInst = 0x4000000;
    	const addressOfLevels = {};
		if(num_Operands!=="undefined"){
			this.mnemonic = _mnemonic;
			this.numOperands = _numOperands;
			this.operands = _operands;
			this.machineCode = _machineCode;
			operands = [];
			isImmediate = [];
			sign = [];
			levelName = "";
		}
		else if(_mnemonic!=="undefined")
		{
			this.mnemonic = _mnemonic;
			operands = [];
			isImmediate = [];
			sign = [];
			levelName = "";
		}
		else{
			operands = [];
			isImmediate = [];
			sign = [];
			levelName = "";
		}
    }

    /**
     * Get operation mnemonic
     * @return operation mnemonic (e.g. addi, sw)
     */
    getName() {
        return mnemonic;
    }

    getNumberOfOperands() {
        return numOperands;
    }

    getOperands() {
        return operands;
    }
	getMachineCode() {
        return machineCode;
    }

    getSourceString() {
        return sourceString;
    }

    static isRegister(str) {
        return str.startsWith("$");
    }

    static isImmediate(str) {
        if (str==="") {
            return false;
        }
        if (str.charAt(0) == '-') {
            str = str.substring(1);
        }
        if (str.startsWith("0x")) { // hexadecimal immediate value
			           
			for (var i = 2; i < str.length(); i++) {
                var ch = str.charAt(i);
                if (!((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F'))) {
                    return false;
                }
            }
        } else { // decimal immediate value
			 
            for (var i = 0; i < str.length(); i++) {
                var ch = str.charAt(i);
                if (ch < '0' || ch > '9') {
                    return false;
                }
            }
        }
        return true;
    }

    parseLoadStoreInstr(str, lineNoInSource) {
        var err = new Error();

        // operand[0] <- Rdest
        // operand[1] <- Rx if present
        // operand[2] <- immediate value , if Rx not present this value will be in operand[1]
		var i =0;        
		for (i = 0; i < 4; i++) {
            isImmediate[i] = false;
            sign[i] = 1;
        }

        str = str.replace(/ /g, "");
        A = str.split(",");
        operands[0] = Register.getRegisterNumberFromString(A[0]);
        if (operands[0] == -1) {
            err = new Error("Syntax Error @ Line " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
            return err;
        }
        numOperands = 1;
        str = A[1];
        if (str.search("+")!=-1) { // in case of + , operand before + is a label only
            A = str.split("+");
            if (Memory.localSymbolTable.hasOwnProperty(A[0])) {
                operands[numOperands] = Memory.localSymbolTable[A[0]];
                isImmediate[numOperands++] = true;
            } else {
                err = new Error("Undefined label name : " + A[0] + " Or Syntax error @ line " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                return err;
            }
            str = A[1];
            if (str.search("(")!=-1) {
                var ind1 = str.indexOf("(");
                var ind2 = str.indexOf(")");
                if (ind2 == -1) { // mismatching opening and closing brackets
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                reg = str.substring(ind1 + 1, ind2);
                operands[numOperands++] = Register.getRegisterNumberFromString(reg);
                if (operands[numOperands - 1] == -1) { // invalid register
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                str = str.replace(/\\(.*\\)/g, "");
            }
            if (str==="") { // parsing done
                return err;
            } else if (isRegister(str)) {
                operands[numOperands] = Register.getRegisterNumberFromString(str);
                if (operands[numOperands] == -1) { // invalid register
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                isImmediate[numOperands++] = false;
            } else if (isImmediate(str)) {
                operands[numOperands] = MyLibrary.fromStringTovar(str);
                isImmediate[numOperands++] = true;
            } else {
                err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                return err;
            }
        } else if (str.search("-")!=-1) { // in case of - , operand before - is a label only
            A = str.split("-");
            if (Memory.localSymbolTable.hasOwnProperty(A[0])) {
                operands[numOperands] = Memory.localSymbolTable[A[0]];
                isImmediate[numOperands++] = true;
            } else {
                err = new Error("Undefined label name : " + A[0] + " Or Syntax error @ line " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                return err;
            }
            str = A[1];
            if (str.search("(")!=-1) {
                var ind1 = str.indexOf("(");
                var ind2 = str.indexOf(")");
                if (ind2 == -1) { // mismatching opening and closing brackets
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                reg = str.substring(ind1 + 1, ind2);
                sign[numOperands] = -1;
                operands[numOperands++] = Register.getRegisterNumberFromString(reg);
                if (operands[numOperands - 1] == -1) { // invalid register
                    err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                    return err;
                }
                str = str.replace(/\\(.*\\)/g, "");
            }
            if (str==="") { // parsing done
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
            if (Memory.localSymbolTable.hasOwnProperty(k)) { // str is a symbol  (e.g  la $Rdest , symbol)

                operands[numOperands] = Memory.localSymbolTable[str];
                isImmediate[numOperands++] = true;
            } else {
                if (str.search("(")!=-1) {
                    var ind1 = str.indexOf("(");
                    var ind2 = str.indexOf(")");
                    if (ind2 == -1) {
                        err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                        return err;
                    }
                    reg = str.substring(ind1 + 1, ind2);

                    operands[numOperands++] = Register.getRegisterNumberFromString(reg);
                    if (operands[numOperands - 1] == -1) {
                        err = new Error("Syntax error @ line : " + lineNoInSource, Error.ASSEMBLE_TIME_ERROR);
                        return err;
                    }

                    str = str.replace(/\\(.*\\)/g, "");

                }
                if (str==="") { // parsing done
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

    parseBasicInstrucction( str, lineNoInSource) {
        var err = new Error();
		var i =0;
        for (i = 0; i < 4; i++) {
            sign[i] = 1;
        }
        numOperands = 0;
        levelName = "";
        A= str.split(",");
        for (i = 0; i < A.length; i++) {
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

    parsInstruction( str, lineNoInSource) {
        var err = new Error();

        str = MyLibrary.removeBeginningAndEndingSpace(str);

        sourceString = str;

        mnemonic = getMnemonicFromString(str);
        str = str.substring(mnemonic.length());

        str = str.replace(/ /g, ""); // all space characters are removed from the instruction
        if (str==="") {
            System.out.println("Null Str Found");
        }
        if (mnemonic.ignoreCase=="add") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="addi") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="addiu") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="addu") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="and") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="andi") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="b") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="beq") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="beqz") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="bge") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="bgez") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="bgezal") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="bgt") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="bgtz") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="ble") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="blez") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="blt") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="bltz") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="bltzal") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="bne") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="bnez") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="clo") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="clz") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="div") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="divu") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="j") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="jal") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="jalr") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="jr") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="la") {  // CHECK LOAD STORE PARSING 
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="lb") {
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="lh") {
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="li") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="lw") {
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="mfhi") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="mflo") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="mthi") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="mtlo") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="move") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="movn") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="movz") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="mul") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="mulo") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="mult") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="neg") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="nor") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="not") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="or") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="ori") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="sll") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="sllv") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="slt") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="slti") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="sltiu") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="sltu") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="srl") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="srlv") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="sb") {
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="seb") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="seh") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="sh") {
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="sub") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="sw") {
            // have to handle
            err = parseLoadStoreInstr(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="xor") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else if (mnemonic.ignoreCase=="xori") {
            err = parseBasicInstrucction(str, lineNoInSource);
        } else {
            err = new Error("Unsupported or invalid instruction : " + mnemonic, Error.ASSEMBLE_TIME_ERROR);
        }
        return err;
    }
	//private
    getMnemonicFromString(str) {
        var ind = str.length();
        for (var i = 0; i < str.length(); i++) {
            if (MyLibrary.isSpaceChar(str.charAt(i))) {
                ind = i;
                break;
            }
        }
        return str.substring(0, ind);
    }

    fillLevelNameWithAddress() {
        var err = new Error();
        if (levelName==="") {
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

    getMnemonic() {
        return mnemonic;
    }

    getIsImmediate() {
        return isImmediate;
    }

    getSign() {
        return sign;
    }

    runSingleInstruction() {

        /* Here we go , the code to run each and every instruction is here ..
         * */
        var err = new Error();
        var oldPc = Register.getPC();

        if (mnemonic.ignoreCase=="add") {
            var add1 = Register.getRegValue(operands[1]); // rs
            var add2 = isImmediate[2] ? operands[2] : Register.getRegValue(operands[2]); //rt
            if (isImmediate[2]) {
                // is immediate value is greater than 16 bit , error
                if (add2 >= (1 << 16)) {
                    err = new Error("Runtime Error: Immediate constant greater than (1<<16) in add instruction ", Error.RUN_TIME_ERROR);
                    return err;
                }
                add2 = add2 << 16 >> 16;
            }

            var sum = add1 + add2;
            // overflow on A+B detected when A and B have same sign and A+B has other sign.
            if ((add1 >= 0 && add2 >= 0 && sum < 0)
                    || (add1 < 0 && add2 < 0 && sum >= 0)) {
                // overflow error , dest reg will not be updated . (LAKSHYA)
                err = new Error("Overflow detected in add instruction , destination register didn't change", Error.RUN_TIME_WARNING);
            } else {
                Register.updateRegister(operands[0], sum);
            }

            Register.incrementPCToNextInstruction();

        } else if (mnemonic.ignoreCase=="addi") {
            var add1 = Register.getRegValue(operands[1]);
            var add2 = operands[2]; //is immediate value is greater than 16 bit , error (LAKSHYA)
            if (add2 >= (1 << 16)) {
                err = new Error("Runtime Error: Immediate constant greater than (1<<16) in add instruction ", Error.RUN_TIME_ERROR);
                return err;
            }
            add2 = add2 << 16 >> 16;
            var sum = add1 + add2;
            // overflow on A+B detected when A and B have same sign and A+B has other sign.
            if ((add1 >= 0 && add2 >= 0 && sum < 0)
                    || (add1 < 0 && add2 < 0 && sum >= 0)) {
                // overflow error , dest reg will not be updated .
                err = new Error("Overflow detected in addi instruction , destination register didn't change", Error.RUN_TIME_WARNING);
            } else {
                Register.updateRegister(operands[0], sum);
            }

            Register.incrementPCToNextInstruction();

        } else if (mnemonic.ignoreCase=="addiu") {
            var add1 = Register.getRegValue(operands[1]);
            var add2 = operands[2]; //is immediate value is greater than 16 bit , error (LAKSHYA)
            if (add2 >= (1 << 16)) {
                err = new Error("Runtime Error: Immediate constant greater than (1<<16) in add instruction ", Error.RUN_TIME_ERROR);
                return err;
            }
            add2 = add2 << 16 >> 16;
            var sum = add1 + add2;

            Register.updateRegister(operands[0], sum);

            Register.incrementPCToNextInstruction();

        } else if (mnemonic.ignoreCase=="addu") {
            var add1 = Register.getRegValue(operands[1]); // rs
            var add2 = isImmediate[2] ? operands[2] : Register.getRegValue(operands[2]); //rt
            if (isImmediate[2]) {
                // is immediate value is greater than 16 bit , error 
                if (add2 >= (1 << 16)) {
                    err = new Error("Runtime Error: Immediate constant greater than (1<<16) in add instruction ", Error.RUN_TIME_ERROR);
                    return err;
                }
                add2 = add2 << 16 >> 16;
            }

            var sum = add1 + add2;

            Register.updateRegister(operands[0], sum);

            Register.incrementPCToNextInstruction();

        } else if (mnemonic.ignoreCase=="and") {
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    & Register.getRegValue(operands[2]));

            Register.incrementPCToNextInstruction();


        } else if (mnemonic.ignoreCase=="andi") {
            // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    & (operands[2] & 0x0000FFFF)); // zero extended 16 bit

            Register.incrementPCToNextInstruction();


        } else if (mnemonic.ignoreCase=="b") {

            Register.updatePcAbsolute(operands[0]);
            var newPc = Register.getPC();


        } else if (mnemonic.ignoreCase=="beq") {
            if (Register.getRegValue(operands[0]) == Register.getRegValue(operands[1])) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="beqz") {
            if (Register.getRegValue(operands[0]) === 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="bge") {
            if (Register.getRegValue(operands[0]) >= Register.getRegValue(operands[1])) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="bgez") {
            if (Register.getRegValue(operands[0]) >= 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="bgezal") {
            if (Register.getRegValue(operands[0]) >= 0) {
                Register.updateRegister("$ra", Register.getPC() + 4); // DELAY BRANCH , PC+8
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="bgt") {
            var op0 = isImmediate[0] ? operands[0] : Register.getRegValue(operands[0]);
            var op1 = isImmediate[1] ? operands[1] : Register.getRegValue(operands[1]);
            if (op0 > op1) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="bgtz") {
            if (Register.getRegValue(operands[0]) > 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="ble") {
            if (Register.getRegValue(operands[0]) <= Register.getRegValue(operands[1])) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="blez") {
            if (Register.getRegValue(operands[0]) <= 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="blt") {
            if (Register.getRegValue(operands[0]) < Register.getRegValue(operands[1])) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="bltz") {
            if (Register.getRegValue(operands[0]) < 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="bltzal") {
            if (Register.getRegValue(operands[0]) < 0) {
                Register.updateRegister("ra", Register.getPC() + 4);
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="bne") {
            if (Register.getRegValue(operands[0]) != Register.getRegValue(operands[1])) {
                Register.updatePcAbsolute(operands[2]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="bnez") {
            if (Register.getRegValue(operands[0]) !== 0) {
                Register.updatePcAbsolute(operands[1]);
            } else {
                Register.incrementPCToNextInstruction();
            }


        } else if (mnemonic.ignoreCase=="clo") {
            var sourceRegValue = Register.getRegValue(operands[1]);
            var temp = 32;
            for (var i = 31; i >= 0; i--) {
                if (((1 << i) & (sourceRegValue)) === 0) {
                    temp = 31 - i;
                    break;
                }
            }
            Register.updateRegister(operands[0], temp);
            Register.incrementPCToNextInstruction();

        } else if (mnemonic.ignoreCase=="clz") {
            var sourceRegValue = Register.getRegValue(operands[1]);
            var temp = 32;
            for (var i = 31; i >= 0; i--) {
                if (((1 << i) & (sourceRegValue)) !== 0) {
                    temp = 31 - i;
                    break;
                }
            }
            Register.updateRegister(operands[0], temp);
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="div") {
            // div can have 2 or 3 operands
            if (numOperands == 2) {
                if (Register.getRegValue(operands[1]) === 0) {
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
                if (Register.getRegValue(operands[2]) === 0) {
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


        } else if (mnemonic.ignoreCase=="divu") {
            var rs = Register.getRegValue(operands[0]);
            var rt = Register.getRegValue(operands[1]);

            if (rt === 0) {
                err = new Error("Runtime Error : Division by 0 in divu instruction", Error.RUN_TIME_ERROR);
                return err;
            }

            rs = (rs << 32) >>> 32;
            rt = (rt << 32) >>> 32;

            var loValue = rs / rt;
            var hiValue = rs % rt;

            // sign extend loValue and hiValue
            loValue = (loValue << 32) >> 32;
            hiValue = (hiValue << 32) >> 32;

            Register.updateLO(int(loValue));
            Register.updateHI(int(hiValue));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="j") {
            Register.updatePcAbsolute(operands[0]);


        } else if (mnemonic.ignoreCase=="jal") {
            Register.updateRegister(31, Register.getPC() + 4); // $ra is 31st reg

            Register.updatePcAbsolute(operands[0]);


        } else if (mnemonic.ignoreCase=="jalr") {
            Register.updateRegister("$ra", Register.getPC() + 4);

            Register.updatePcAbsolute(Register.getRegValue(operands[0]));


        } else if (mnemonic.ignoreCase=="jr") {

            Register.updatePcAbsolute(Register.getRegValue(operands[0]));

            return err;
        } else if (mnemonic.ignoreCase=="la") {
            // take care of address
            var address = 0;
            for (var i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }
            Register.updateRegister(operands[0], address);
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.ignoreCase=="lb") {
            var address = 0;
            for (var i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }

            Register.updateRegister(operands[0], Memory.readByte(address));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="lh") {
            var address = 0;
            for (var i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }
            Register.updateRegister(operands[0], Memory.readHalfWord(address));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="li") {
            Register.updateRegister(operands[0], operands[1]); // ERROR CHECKING  (negative also , fit into 32 bit )
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.ignoreCase=="lw") {
            var address = 0;
            for (var i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }

            Register.updateRegister(operands[0], Memory.readWord(address));

            Register.incrementPCToNextInstruction();


        } else if (mnemonic.ignoreCase=="mfhi") {
            Register.updateRegister(operands[0], Register.getHI());
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.ignoreCase=="mflo") {
            Register.updateRegister(operands[0], Register.getLO());
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.ignoreCase=="mthi") {
            Register.updateHI(Register.getRegValue(operands[0]));
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.ignoreCase=="mtlo") {
            Register.updateLO(Register.getRegValue(operands[0]));
            Register.incrementPCToNextInstruction();


        } else if (mnemonic.ignoreCase=="move") {
            Register.updateRegister(operands[0], Register.getRegValue(operands[1]));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="movn") {
            if (Register.getRegValue(operands[2]) !== 0) {
                Register.updateRegister(operands[0], Register.getRegValue(operands[1]));
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="movz") {
            if (Register.getRegValue(operands[2]) === 0) {
                Register.updateRegister(operands[0], Register.getRegValue(operands[1]));
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="mul") {
            if (numOperands == 2) {
                var op1 = long(Register.getRegValue(operands[0]));
                var op2 = isImmediate[1] ? long((operands[1])) : long(Register.getRegValue(operands[1]));
                if (isImmediate[1]) {
                    op2 = (op2 << 16) >> 16;
                }

                var product = op1 * op2;

                Register.updateRegister("$hi", (int) (product >> 32));
                Register.updateRegister("$lo", (int) ((product << 32) >> 32));
            } else if (numOperands == 3) {
                var op1 = long(Register.getRegValue(operands[1]));
                var op2 = isImmediate[2] ? long(operands[2]) : long(Register.getRegValue(operands[2]));
                if (isImmediate[2]) {
                    op2 = (op2 << 16) >> 16;
                }

                var product = op1 * op2;

                Register.updateRegister(operands[0], (int) ((product << 32) >> 32));

                Register.updateRegister("$hi", (int) (product >> 32));
                Register.updateRegister("$lo", (int) ((product << 32) >> 32));
            } else {
                // ERROR
            }
            Register.incrementPCToNextInstruction();


        }
        else if (mnemonic.ignoreCase=="mulo") {
            var op1 = long(Register.getRegValue(operands[1]));
            var op2 = isImmediate[2] ? long(operands[2]) : long(Register.getRegValue(operands[2]));
            if (isImmediate[2]) {
                op2 = (op2 << 16) >> 16;
            }

            var product = op1 * op2;
            // overflow exception needs to be generated in case of overflow (TO BE IMPLEMENTED)
            Register.updateRegister(operands[0], (int) ((product << 32) >> 32));

            Register.updateRegister("$hi", (int) (product >> 32));
            Register.updateRegister("$lo", (int) ((product << 32) >> 32));

            Register.incrementPCToNextInstruction();

        } else if (mnemonic.ignoreCase=="mult") {
            var op1 = long(Register.getRegValue(operands[0]));
            var op2 = long(Register.getRegValue(operands[1]));

            var product = op1 * op2;

            Register.updateRegister("$hi", int(product >> 32));
            Register.updateRegister("$lo", int((product << 32) >> 32));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="neg") {
            Register.updateRegister(operands[0], -Register.getRegValue(operands[1]));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="nor") {
            Register.updateRegister(operands[0],
                    ~(Register.getRegValue(operands[1])
                    | Register.getRegValue(operands[2])));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="not") {
            Register.updateRegister(operands[0],
                    ~(Register.getRegValue(operands[1])));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="or") {
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    | Register.getRegValue(operands[2]));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="ori") {
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    | (operands[2] & 0x0000FFFF));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="sb") {
            var address = 0;
            for (var i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }
            Memory.storeByte(address, Register.getRegValue(operands[0]));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="seb") {
            var rt = Register.getRegValue(1);

            Register.updateRegister(operands[0], (rt & 0xFF) << 24 >> 24);

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="seh") {
            var rt = Register.getRegValue(1);

            Register.updateRegister(operands[0], (rt & 0xFFFF) << 16 >> 16);
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="sh") {
            var address = 0;
            for (var i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }
            Memory.storeHalfWord(address, Register.getRegValue(operands[0]));
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="sll") {

            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1]) << operands[2]);
            // operand[2] should be less than 32 , otherwise ERROR 

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="sllv") {
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1]) << (Register.getRegValue(operands[2]) & 0x0000001F));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="slt") {
            var rs = Register.getRegValue(operands[1]);
            var rt = Register.getRegValue(operands[2]);
            if (rs < rt) {
                Register.updateRegister(operands[0], 1);
            } else {
                Register.updateRegister(operands[0], 0);
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="slti") {
            var rs = Register.getRegValue(operands[1]);
            var rt = operands[2];
            if (rs < rt) {
                Register.updateRegister(operands[0], 1);
            } else {
                Register.updateRegister(operands[0], 0);
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="sltiu") {
            var rs = Register.getRegValue(operands[1]);
            var immediate = operands[2];
            rs = (rs << 32) >>> 32;
            immediate = (immediate << 32) >>> 32;
            if (rs < immediate) {
                Register.updateRegister(operands[0], 1);
            } else {
                Register.updateRegister(operands[0], 0);
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="sltu") {
            var rs = Register.getRegValue(operands[1]);
            var rt = Register.getRegValue(operands[2]);
            rs = (rs << 32) >>> 32;
            rt = (rt << 32) >>> 32;
            if (rs < rt) {
                Register.updateRegister(operands[0], 1);
            } else {
                Register.updateRegister(operands[0], 0);
            }
            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="srl") {
            // must zero-fill, so use ">>>" instead of ">>".
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1]) >>> operands[2]);

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="srlv") {
            // Mask all but low 5 bits of register containing shamt.Use ">>>" to zero-fill.
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    >>> (Register.getRegValue(operands[2]) & 0x0000001F));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="sub") {
            var sub1 = Register.getRegValue(operands[1]);
            var sub2;
            if (isImmediate[2]) {
                sub2 = operands[2];
            } else {
                sub2 = Register.getRegValue(operands[2]);
            }
            var dif = sub1 - sub2;
            // overflow on A-B detected when A and B have opposite signs and A-B has B's sign
            if ((sub1 >= 0 && sub2 < 0 && dif < 0)
                    || (sub1 < 0 && sub2 >= 0 && dif >= 0)) {
                // overflow , Rdest should not be updated
            } else {
                Register.updateRegister(operands[0], dif);
            }

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="subu") {
            var sub1 = Register.getRegValue(operands[1]);
            var sub2;
            if (isImmediate[2]) {
                sub2 = operands[2];
            } else {
                sub2 = Register.getRegValue(operands[2]);
            }
            var dif = sub1 - sub2;

            Register.updateRegister(operands[0], dif);

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="sw") {
            var address = 0;
            for (var i = 1; i < numOperands; i++) {
                address += (isImmediate[i] ? (operands[i] * sign[i]) : (sign[i] * Register.getRegValue(operands[i])));
            }
            Memory.storeWord(address, Register.getRegValue(operands[0]));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="xor") {
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    ^ Register.getRegValue(operands[2]));

            Register.incrementPCToNextInstruction();
        } else if (mnemonic.ignoreCase=="xori") {
            // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
            Register.updateRegister(operands[0],
                    Register.getRegValue(operands[1])
                    ^ (operands[2] & 0x0000FFFF));

            Register.incrementPCToNextInstruction();
        } else {
            err = new Error("Instruction " + mnemonic + " Not Supported ! ", Error.RUN_TIME_ERROR);
        }

        var newPc = Register.getPC();
        if (oldPc == newPc) {
            err = new Error("Infinite loop detected !", Error.RUN_TIME_ERROR);
        }
        return err;
    }
}
