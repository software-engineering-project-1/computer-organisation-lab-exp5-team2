package mipsparser_new.BMSFS;

public class Register{
	public static int []Reg=new int[35]; // 32 general purpose registers from $0 to $31
	                               // $32 hi , $33 lo , $34 pc
	public Register() {
		for(int i=0;i<Reg.length;i++){
			Reg[i]=0;
		}
		initializePC();
		initializeSP();
	}
	
	private void initializeSP() {
//		updateRegister("$sp", 2147483644); // 7ffffffc 		
		updateRegister("$sp", 0x10100000);
	}

	public static void updateRegister(int regNo , int value){
		if(regNo==0){ // we can't change $0 , it's hardwired to produce 0
			return ;
		}
		if(regNo>0 && regNo < Reg.length){
			Reg[regNo]=value;
		}
	}
	
	public static void updateRegister(String regStr , int value){
		int regNo=getRegisterNumberFromString(regStr);
		if(regNo>0 && regNo<Reg.length){
			
			Reg[regNo]=value;
		}
	}
	
	public static int getRegValue(int regNo){
		if(regNo>=0 && regNo<Reg.length){
			return Reg[regNo];
		}
		return -1; // error (-1 can be valid return value also)
	}
        public static void setRegValue(int regNo, int value){
            if(regNo>=0 && regNo<Reg.length){
                Reg[regNo] = value;
            }
        }
	public static int getRegValue(String reg){
		int regNo=getRegisterNumberFromString(reg);
		if(regNo>=0 && regNo < Reg.length){
			return Reg[regNo];
		}
		return -1; // error (-1 can be valid return value also)
	}
	
	public static int getPC(){
		return Reg[34];
	}
	
	public static void updatePcRelative(int relativeShift){
		Reg[34]+=relativeShift;
	}
	
	public static void updatePcAbsolute(int newPCAddress){
		Reg[34]=newPCAddress;
	}
	
	public static void initializePC(){
		updatePcAbsolute(0x4000000);
	}
	public static void initializePC(int startAddr){
		updatePcAbsolute(startAddr);
	}
	
	public static void incrementPCToNextInstruction(){
		updatePcRelative(4);
	}
	
	public static int getHI(){
		return Reg[32];
	}
	
	public static void printAllReg(){
		System.out.println("\nPRINTING ALL REGISTERS");
		for(int i=0;i<Reg.length;i++){
			System.out.println(i+" "+Reg[i]);
		}
	}
	
	public static void updateHI(int newValue){
		Reg[32]=newValue;
	}
	
	public static int getLO(){
		return Reg[33];
	}
	
	public static void updateLO(int newValue){
		Reg[33]=newValue;
	}
	
	public static int getRegisterNumberFromString(String str){
		if(str.equalsIgnoreCase("$0") || str.equalsIgnoreCase("$zero")){
			return 0;
		}else if(str.equalsIgnoreCase("$1") || str.equalsIgnoreCase("$at")){
			return 1;
		}else if(str.equalsIgnoreCase("$2") || str.equalsIgnoreCase("$v0")){
			return 2;
		}else if(str.equalsIgnoreCase("$3") || str.equalsIgnoreCase("$v1")){
			return 3;
		}else if(str.equalsIgnoreCase("$4") || str.equalsIgnoreCase("$a0")){
			return 4;
		}else if(str.equalsIgnoreCase("$5") || str.equalsIgnoreCase("$a1")){
			return 5;
		}else if(str.equalsIgnoreCase("$6") || str.equalsIgnoreCase("$a2")){
			return 6;
		}else if(str.equalsIgnoreCase("$7") || str.equalsIgnoreCase("$a3")){
			return 7;
		}else if(str.equalsIgnoreCase("$8") || str.equalsIgnoreCase("$t0")){
			return 8;
		}else if(str.equalsIgnoreCase("$9") || str.equalsIgnoreCase("$t1")){
			return 9;
		}else if(str.equalsIgnoreCase("$10") || str.equalsIgnoreCase("$t2")){
			return 10;
		}else if(str.equalsIgnoreCase("$11") || str.equalsIgnoreCase("$t3")){
			return 11;
		}else if(str.equalsIgnoreCase("$12") || str.equalsIgnoreCase("$t4")){
			return 12;
		}else if(str.equalsIgnoreCase("$13") || str.equalsIgnoreCase("$t5")){
			return 13;
		}else if(str.equalsIgnoreCase("$14") || str.equalsIgnoreCase("$t6")){
			return 14;
		}else if(str.equalsIgnoreCase("$15") || str.equalsIgnoreCase("$t7")){
			return 15;
		}else if(str.equalsIgnoreCase("$16") || str.equalsIgnoreCase("$s0")){
			return 16;
		}else if(str.equalsIgnoreCase("$17") || str.equalsIgnoreCase("$s1")){
			return 17;
		}else if(str.equalsIgnoreCase("$18") || str.equalsIgnoreCase("$s2")){
			return 18;
		}else if(str.equalsIgnoreCase("$19") || str.equalsIgnoreCase("$s3")){
			return 19;
		}else if(str.equalsIgnoreCase("$20") || str.equalsIgnoreCase("$s4")){
			return 20;
		}else if(str.equalsIgnoreCase("$21") || str.equalsIgnoreCase("$s5")){
			return 21;
		}else if(str.equalsIgnoreCase("$22") || str.equalsIgnoreCase("$s6")){
			return 22;
		}else if(str.equalsIgnoreCase("$23") || str.equalsIgnoreCase("$s7")){
			return 23;
		}else if(str.equalsIgnoreCase("$24") || str.equalsIgnoreCase("$t8")){
			return 24;
		}else if(str.equalsIgnoreCase("$25") || str.equalsIgnoreCase("$t9")){
			return 25;
		}else if(str.equalsIgnoreCase("$26") || str.equalsIgnoreCase("$k0")){
			return 26;
		}else if(str.equalsIgnoreCase("$27") || str.equalsIgnoreCase("$k1")){
			return 27;
		}else if(str.equalsIgnoreCase("$28") || str.equalsIgnoreCase("$gp")){
			return 28;
		}else if(str.equalsIgnoreCase("$29") || str.equalsIgnoreCase("$sp")){
			return 29;
		}else if(str.equalsIgnoreCase("$30") || str.equalsIgnoreCase("$fp")){
			return 30;
		}else if(str.equalsIgnoreCase("$31") || str.equalsIgnoreCase("$ra")){
			return 31;
		}else if(str.equalsIgnoreCase("$hi")){
                    return 32;
                }else if(str.equalsIgnoreCase("$lo")){
                    return 33 ;
                }
		return -1; // error
	}
}