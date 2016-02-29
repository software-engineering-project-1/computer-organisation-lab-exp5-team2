class Register{
	constructor() {
	    const Reg = [];
		var i = 0;
		for(i=0;i<reg.length;i++) {
			Reg[i]=0;
		}
		initializePC();
		initializeSP();
	}
	initializeSP(){
		updateRegister("$sp", 0x10100000);
	}
	static updateRegister(value,regStr){
		if(typeof(regStr)==='number'){
			if(regStr===0){
				return;
			}
			if(regStr>0 && regStr<Reg.length) {
				Reg[regStr]=value;
			}
		}
		else if(typeof(regStr)==='string') {
			var regNum=getRegisterNumberFromString(regStr);
			if(regNum>0 && regNum<Reg.length){
			
				Reg[regNum]=value;
			}
		}
	}
	
	static getRegValue(reg){
		if(typeof(reg)=='number'){
			if(reg>=0 && reg<Reg.length){
				return Reg[reg];
			}
			return -1; // error (-1 can be valid return value also)
		}
		else if(typeof(reg)=='string'){
			var regNum=getRegisterNumberFromString(reg);
			if(regNum>=0 && regNum < Reg.length){
				return Reg[regNum];
			}
			return -1; 
		}
	}
	static setRegValue(regNo,value){
            if(regNo>=0 && regNo<Reg.length){
                Reg[regNo] = value;
            }
        }
	static getPC(){
		return Reg[34];
	}
	static updatePcRelative(relativeShift){
		Reg[34]+=relativeShift;
	}
	static updatePcAbsolute(newPCAddress){
		Reg[34]=newPCAddress;
	}
	static initializePC(startAddr){
	    if(startAddr==="undefined"){
	        updatePcAbsolute(0x4000000);
	    }
	    else
		    updatePcAbsolute(startAddr);
	}
	static incrementPCToNextInstruction(){
		updatePcRelative(4);
	}
	static getHI(){
		return Reg[32];
	}
	static printAllReg(){
		console.log("\nPRINTING ALL REGISTERS");
		var i = 0;
		for(i=0;i<Reg.length;i++){
			console.log(i+" "+Reg[i]);
		}
	}
	static updateHI(newValue){
		Reg[32]=newValue;
	}
	static getLO(){
		return Reg[33];
	}
	static updateLO(newValue){
		Reg[33]=newValue;
	}
	static getRegisterNumberFromString(str){
		if(str.ignoreCase=="$0" || str.ignoreCase=="$zero"){
			return 0;
		}else if(str.ignoreCase=="$1" || str.ignoreCase=="$at"){
			return 1;
		}else if(str.ignoreCase=="$2" || str.ignoreCase=="$v0"){
			return 2;
		}else if(str.ignoreCase=="$3" || str.ignoreCase=="$v1"){
			return 3;
		}else if(str.ignoreCase=="$4" || str.ignoreCase=="$a0"){
			return 4;
		}else if(str.ignoreCase=="$5" || str.ignoreCase=="$a1"){
			return 5;
		}else if(str.ignoreCase=="$6" || str.ignoreCase=="$a2"){
			return 6;
		}else if(str.ignoreCase=="$7" || str.ignoreCase=="$a3"){
			return 7;
		}else if(str.ignoreCase=="$8" || str.ignoreCase=="$t0"){
			return 8;
		}else if(str.ignoreCase=="$9" || str.ignoreCase=="$t1"){
			return 9;
		}else if(str.ignoreCase=="$10" || str.ignoreCase=="$t2"){
			return 10;
		}else if(str.ignoreCase=="$11" || str.ignoreCase=="$t3"){
			return 11;
		}else if(str.ignoreCase=="$12" || str.ignoreCase=="$t4"){
			return 12;
		}else if(str.ignoreCase=="$13" || str.ignoreCase=="$t5"){
			return 13;
		}else if(str.ignoreCase=="$14" || str.ignoreCase=="$t6"){
			return 14;
		}else if(str.ignoreCase=="$15" || str.ignoreCase=="$t7"){
			return 15;
		}else if(str.ignoreCase=="$16" || str.ignoreCase=="$s0"){
			return 16;
		}else if(str.ignoreCase=="$17" || str.ignoreCase=="$s1"){
			return 17;
		}else if(str.ignoreCase=="$18" || str.ignoreCase=="$s2"){
			return 18;
		}else if(str.ignoreCase=="$19" || str.ignoreCase=="$s3"){
			return 19;
		}else if(str.ignoreCase=="$20" || str.ignoreCase=="$s4"){
			return 20;
		}else if(str.ignoreCase=="$21" || str.ignoreCase=="$s5"){
			return 21;
		}else if(str.ignoreCase=="$22" || str.ignoreCase=="$s6"){
			return 22;
		}else if(str.ignoreCase=="$23" || str.ignoreCase=="$s7"){
			return 23;
		}else if(str.ignoreCase=="$24" || str.ignoreCase=="$t8"){
			return 24;
		}else if(str.ignoreCase=="$25" || str.ignoreCase=="$t9"){
			return 25;
		}else if(str.ignoreCase=="$26" || str.ignoreCase=="$k0"){
			return 26;
		}else if(str.ignoreCase=="$27" || str.ignoreCase=="$k1"){
			return 27;
		}else if(str.ignoreCase=="$28" || str.ignoreCase=="$gp"){
			return 28;
		}else if(str.ignoreCase=="$29" || str.ignoreCase=="$sp"){
			return 29;
		}else if(str.ignoreCase=="$30" || str.ignoreCase=="$fp"){
			return 30;
		}else if(str.ignoreCase=="$31" || str.ignoreCase=="$ra"){
			return 31;
		}else if(str.ignoreCase=="$hi"){
                    return 32;
                }else if(str.ignoreCase=="$lo"){
                    return 33 ;
                }
		return -1; // error
	}
}
	
