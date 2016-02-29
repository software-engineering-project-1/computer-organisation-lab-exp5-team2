package mipsparser_new;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import mipsparser_new.BMSFS.*;
import mipsparser_new.BMSFS.Error;

import java.io.FileReader;
import java.util.*;


public class MIPSProgram {
	private String filename;
	private ArrayList sourceList;
	private ArrayList commentRemovedSource;
	private ArrayList sourceOfDataSection ; 
	public ArrayList sourceOfTextSection ;
	private ArrayList<Integer> DataSectionLineNo;      // For Generating Error msg
	private ArrayList<Integer> TextSectionLineNo;      // For Generating Error msg
	private Register Reg;
	private Memory Mem;
	private ArrayList<Instruction> AllInstructions ; 
	private ArrayList CodeSection ;
	private Error assembleTimeError ;

	public MIPSProgram(String source) throws IOException{
		//System.out.println("*********  MIPS Simulator 1.1  *********\n");
		CodeSection=new ArrayList<String>();
		assembleTimeError = new Error();
		AllInstructions=new ArrayList<Instruction>();
		//String fileName="/home/rishi/study/RAShip/MyTestCases/recursion.asm";
		Reg=new Register();
		Mem=new Memory();
       
		readSource(source);
		removeComment();

//		PrintCommentRemovedSource();
		
		splitDataAndTextSection();
		//System.out.println("hi1");

		Error err = processDataSection();
		if(!err.isOk()){
			err.printErrorMsg();
			assembleTimeError = err ;
                        return ;
		}
                //System.out.println("hi2");

//		Testing();
		err = processTextSection();
                //System.out.println("hihihi");

		if(!err.isOk()){
			err.printErrorMsg();
                        assembleTimeError = err ;
			return ; 
		}
                //System.out.println("hi3");
                //printDataAndTextSections();
                //System.out.println("hi4");

		
//		printAllInstructions();
		
		//RunAllInstructions();
		
	}

        public Error getAssembleTimeError(){
            return assembleTimeError ; 
        }

	public ArrayList<String> getsourceOfTextSection(){
            ArrayList<String> sourceLinesToDisplay = new ArrayList<String>();

            for(int i=0 ; i<AllInstructions.size();i++){
                sourceLinesToDisplay.add(AllInstructions.get(i).getSourceString());
            }

            return sourceLinesToDisplay ;
        }

	public Error runCurrentInstruction(){
		Error err =new Error();
                int currentPC=Reg.getPC();
		int startPC=Instruction.startAddressOfInst;
		int index=(currentPC-startPC)/4;
		if(index>=AllInstructions.size()){
			// 
		}else{
			Instruction instr=AllInstructions.get(index);
			err  = instr.runSingleInstruction();
                    
                }
                return err;
	}
        /*public int RunSingleInstructions(){
		int PClimit=Register.getPC()+AllInstructions.size()*4;
		int startPC=Instruction.startAddressOfInst;
		int currentPC;
                int index = 0;
		while(true){
			currentPC=Reg.getPC();
			if(currentPC >= PClimit){
				break;
			}
			index=(currentPC-startPC)/4;
			Instruction instr=AllInstructions.get(index);
			instr.runSingleInstruction();
                        break;
		}
                return index;
	}*/

	
	
	private void RunAllInstructions() throws IOException { 
		
		int PClimit=Register.getPC()+AllInstructions.size()*4;
		
		int startPC=Instruction.startAddressOfInst;
		
		int currentPC;
		
		while(true){
			currentPC=Reg.getPC();

			if(currentPC >= PClimit){
				// all instructions have been executed 
				break;
			}
			int index=(currentPC-startPC)/4;
			Instruction instr=AllInstructions.get(index);
			Error err = instr.runSingleInstruction();
			if(!err.isOk()){
				err.printErrorMsg();
				break ; 
			}
			
		//	Register.printAllReg();
		}
		//Register.printAllReg();
	}

	private void printAllInstructions() {
		for(int i=0;i<AllInstructions.size();i++){
			System.out.println("\nPrinting "+i+"th instruction");
			System.out.println("mnemonic "+AllInstructions.get(i).getMnemonic());
			Instruction instr=AllInstructions.get(i);
			int NumOp=instr.getNumberOfOperands();
			int []operands=instr.getOperands();
			boolean []isImmediate=instr.getIsImmediate();
			int []sign=instr.getSign();
			for(int j=0;j<NumOp;j++){
				System.out.printf("%d %s %d\n",operands[j],(isImmediate[j]?"Immediate":"Register"),sign[j]);
			}
		}
	}

	private void Testing() {
		System.out.println("\n\n*************** Testing *********************\n\n");
		
	}

	private void PrintCommentRemovedSource() {
		System.out.println("\n**********  PRINTING COMMENT REMOVED SOURCE FILE  ******************");
		for(int i=0;i<commentRemovedSource.size();i++)
			System.out.println(commentRemovedSource.get(i));
	}

	private void PrintSource() {
		System.out.println("**********  PRINTING SOURCE FILE  ******************");
		for(int i=0;i<sourceList.size();i++)
			System.out.println(sourceList.get(i));
	}

	/**
	 * Produces specified line of MIPS source program .
	 * @param lineNo
	 *            Line number of MIPS source program to get. Line 1 is first
	 *            line.
	 * @return Returns specified line of MIPS source. If outside the line range,
	 *         it returns null. Line 1 is first line.
	 **/
	public String getSourceLine(int lineNo) {
		if ((lineNo >= 1) && (lineNo <= sourceList.size()))
			return (String) sourceList.get(lineNo - 1);
		else
			return null;
	}

	/**
	 * Reads MIPS source code from file into structure.
	 * @param file  String containing name of MIPS source code file.
	 **/

	public void readSource(String source) {
		this.sourceList = new ArrayList<String>();
                String[] tempSource = source.split("\n");
		try {
                    for(int i=0;i<tempSource.length;i++){
                        sourceList.add(tempSource[i]);
                    }
		} catch (Exception e) {

		}
		return;
	}
	
	public void removeComment(){
		commentRemovedSource=new ArrayList<String>();
		for(int i=0;i<sourceList.size();i++){
			String s=removeCommentFromAString((String)sourceList.get(i));
			s=MyLibrary.removeBeginningAndEndingSpace(s);
			if(!s.equals("")){
				commentRemovedSource.add(s);
			}
		}
	}


	private String removeCommentFromAString(String str) {
		int ind=str.indexOf("//");
		if(ind==-1)return str;
		return str.substring(0, ind);
	}
	
	public void splitDataAndTextSection(){
		sourceOfDataSection= new ArrayList<String>()  ;
		sourceOfTextSection= new ArrayList<String>()  ;
		DataSectionLineNo  = new ArrayList<Integer>() ;
		TextSectionLineNo  = new ArrayList<Integer>() ; 
		
		int DATA=0 , TEXT=1;
		int flag=DATA;
		for(int i=0;i<commentRemovedSource.size();i++){
			String str=(String) commentRemovedSource.get(i);
			str=str.toLowerCase();
			if(str.startsWith(".text")){
				flag=TEXT;
			}else if(str.startsWith(".data")){
				flag=DATA;
			}else{
				if(flag==DATA){
					sourceOfDataSection.add(str);
					DataSectionLineNo.add(i+1);
				}else{
					sourceOfTextSection.add(str);
					TextSectionLineNo.add(i+1);
				}
			}
		}
	}
	
	public void printDataAndTextSections(){
		/*System.out.println("\n\nPRINTING DATA SECTION\n\n");
		for(int i=0;i<sourceOfDataSection.size();i++){
			System.out.println(sourceOfDataSection.get(i));
		}*/
		
		System.out.println("\n\nPRINTING TEXT SECTION\n\n");
		for(int i=0;i<sourceOfTextSection.size();i++){
			System.out.println(sourceOfTextSection.get(i));
		}
	}

	private boolean doesStartWithLevel(String str){
		for(int i=0;i<str.length();i++){
			if(MyLibrary.isSpaceChar(str.charAt(i))){
				return false;
			}else if(str.charAt(i)==':'){
				return true;
			}
		}
		return false;
	}
	
	private String getLevelName(String str){
		int ind=-1;
		for(int i=0;i<str.length();i++){
			if(MyLibrary.isSpaceChar(str.charAt(i))){
				break;
			}else if(str.charAt(i)==':'){
				ind=i ; break;
			}
		}
		if(ind==-1)return "";
		return str.substring(0, ind);
	}
	
	private Error processDataSection(){
		Error err = new Error();
		for(int i=0;i<sourceOfDataSection.size();i++){
			String str=(String) sourceOfDataSection.get(i);
			if(doesStartWithLevel(str)){
			
				String levelName=getLevelName(str);
			
				Mem.setBeginingAddressOfWord(levelName);
				err = processDirectives(str.substring(levelName.length()+1) ,  DataSectionLineNo.get(i));
			}else {
				err = processDirectives(str , DataSectionLineNo.get(i) );
			}
			if(!err.isOk()){
				break;
			}
		}
		return err;
	}
	
	
	private Error processSpaceDirective(String str  , int sourceLineNo){
		Error err = new Error();
		str=MyLibrary.removeBeginningAndEndingSpace(str);
		if(!MyLibrary.isValidNumber(str)){
			err = new Error("Invalid Numaric Constant in Line Number "+sourceLineNo , Error.ASSEMBLE_TIME_ERROR);
			return err;
		}
		
		int numBytes=MyLibrary.fromStringToInt(str);
		if(numBytes > (Memory.currentStackAddress - Memory.currentDynamicDataAddress)){
			err = new Error("Segmentation Fault ! Unable To Allocate "+numBytes+" Bytes !" , Error.ASSEMBLE_TIME_ERROR);
			return err ; 
		}
		for(int i=0;i<numBytes;i++){
			Mem.addByteInDynamicMemory(0);
		}
		return err;
	}
	private Error processWordDirective(String str , int sourceLineNo){
		Error err = new Error();
		str=MyLibrary.removeBeginningAndEndingSpace(str);
		if(str.indexOf(':')!=-1){ // value : saperated 
			String A[]=str.split("[ ]*:[ ]*");
			if(A.length!= 2) { // error
				err = new Error("Syntax Error @ line "+sourceLineNo , Error.ASSEMBLE_TIME_ERROR);
				return err ; 
			}
			if(!MyLibrary.isValidNumber(A[0]) || !MyLibrary.isValidNumber(A[0])){
				err = new Error("Invalid Numaric Constant in Line Number "+sourceLineNo , Error.ASSEMBLE_TIME_ERROR);
				return err;
			}
			
			int value=MyLibrary.fromStringToInt(A[0]);
			int noOfWords=MyLibrary.fromStringToInt(A[1]);
			
			if(noOfWords*4 > (Memory.currentStackAddress - Memory.currentDynamicDataAddress)){
				err = new Error("Segmentation Fault ! Unable To Allocate "+noOfWords+" Words !" , Error.ASSEMBLE_TIME_ERROR);
				return err ; 
			}
			
			for(int i=0;i<noOfWords;i++){
				Mem.addWordInDynamicMemory(value);
			}
			
		}else if(str.indexOf(',')!=-1){ // values are comma saperated 
			String A[]=str.split("[ ]*,[ ]*");
			int noOfWords = A.length;
			if(noOfWords*4 > (Memory.currentStackAddress - Memory.currentDynamicDataAddress)){
				err = new Error("Segmentation Fault ! Unable To Allocate "+noOfWords+" Words !" , Error.ASSEMBLE_TIME_ERROR);
				return err ; 
			}
			
			for(int i=0;i<A.length;i++){
				if(!MyLibrary.isValidNumber(A[i]) ){
					err = new Error("Invalid Numaric Constant in Line Number "+sourceLineNo , Error.ASSEMBLE_TIME_ERROR);
					return err;
				}
				Mem.addWordInDynamicMemory(MyLibrary.fromStringToInt(A[i]));
			}
		}else { // only one value
			if(!MyLibrary.isValidNumber(str) ){
				err = new Error("Invalid Numaric Constant in Line Number "+sourceLineNo , Error.ASSEMBLE_TIME_ERROR);
				return err;
			}
			Mem.addWordInDynamicMemory(MyLibrary.fromStringToInt(str));
		}
		return err ; 
	}
	
	private boolean isValidString(String str){
		int len=str.length();
		if(len < 2){
			return false ; 
		}
		if(str.charAt(0)!='"' || str.charAt(len-1)!='"'){
			return false ; 
		}
		for(int i=1;i<len;i++){
			if(str.charAt(i) == '\\'){
				if(i==len-2){
					return false;
				}
				i++;
			}
		}
		return true;
	}
	
	private Error storeAllChars(String str){
		Error err = new Error();
		int len=str.length();
		for(int i=1;i<len-1;i++){
			if(str.charAt(i) == '\\'){
				i++;
				if(str.charAt(i)=='b'){
					Memory.addByteInDynamicMemory('\b');
				}else if(str.charAt(i) == 't'){
					Memory.addByteInDynamicMemory('\t');
				}else if(str.charAt(i) == 'n'){
					Memory.addByteInDynamicMemory('\n');
				}else if(str.charAt(i) == 'f'){
					Memory.addByteInDynamicMemory('\f');
				}else if(str.charAt(i) == 'r'){
					Memory.addByteInDynamicMemory('\r');
				}else if(str.charAt(i) == '"'){
					Memory.addByteInDynamicMemory('"');
				}else if(str.charAt(i) == '\''){
					Memory.addByteInDynamicMemory('\'');
				}else if(str.charAt(i) == '\\'){
					Memory.addByteInDynamicMemory('\\');
				}
			}else{
				Memory.addByteInDynamicMemory(str.charAt(i));
			}
		}
		return err ; 

	}
	
	private Error processAsciiDirective(String str , int sourceLineNo){
		Error err = new Error();
		str=MyLibrary.removeBeginningAndEndingSpace(str);
		if(!isValidString(str)){
			err = new Error("Invalid String Constant in line no "+sourceLineNo+" !" , Error.ASSEMBLE_TIME_ERROR);
			return err;
		}
		err = storeAllChars(str);
		return err ; 
	}
	
	private Error processAsciizDirective(String str , int sourceLineNo){
		Error err = new Error();
		str=MyLibrary.removeBeginningAndEndingSpace(str);
		if(!isValidString(str)){
			err = new Error("Invalid String Constant in line no "+sourceLineNo+" !" , Error.ASSEMBLE_TIME_ERROR);
			return err;
		}
		err=storeAllChars(str);
		Memory.addByteInDynamicMemory('\n');
		return err ; 
	}
	
	
	private Error processDirectives(String str , int sourceLineNo) {
		str=MyLibrary.removeBeginningAndEndingSpace(str);
//		System.out.println("PROCESSING DIRECTIVE --> "+str);
		Error err = new Error();
		if(str.equals("")){
			return err;
		}
		if(str.startsWith(".space")){
			str=str.substring(6);
			err = processSpaceDirective(str , sourceLineNo);
		}else if(str.startsWith(".word")){
			str=str.substring(5);
			err = processWordDirective(str , sourceLineNo);
		}else if(str.startsWith(".asciiz")){
			str=str.substring(7);
			err = processAsciizDirective(str , sourceLineNo);
		}else if(str.startsWith(".ascii")){
			str=str.substring(6);
			err = processAsciiDirective(str , sourceLineNo);
		}else{
			err = new Error("Directive in line no "+sourceLineNo+" is not supported !" , Error.ASSEMBLE_TIME_ERROR);
		}
		return err ; 
	}
	
	private Error processTextSection(){
                //System.out.println("ptsec");
		Error err = new Error() ; 
		int startAddress=Instruction.startAddressOfInst;
		int currentAddr=startAddress;
		for(int i=0;i<sourceOfTextSection.size();i++){
		//	System.out.println(i+" "+sourceOfTextSection.get(i));
                        String str=(String) sourceOfTextSection.get(i);
                        str=MyLibrary.removeBeginningAndEndingSpace(str);
                        if(str.equals("")){
                  //          System.out.println("-------->"+str.charAt(0));
                            continue;
                        }
                        //System.out.println("-------->"+Integer.parseInt(str)+"<-------");
                   //     System.out.println("$"+str+"$"+" "+str.length());
			if(doesStartWithLevel(str)){
				String levelName=getLevelName(str);
				Instruction.addressOfLevels.put(levelName, currentAddr);
				str=str.substring(levelName.length()+1);
				str=MyLibrary.removeBeginningAndEndingSpace(str);
			}
			if(!str.equals("")){
				Instruction instr = new Instruction();
				CodeSection.add(str); 
				err = instr.parsInstruction(str , TextSectionLineNo.get(i) );
				if(!err.isOk()){
					return err ; 
				}
				AllInstructions.add(instr);
				currentAddr+=4;
			}
		}
		
		err = FillAllLevelNamesWithTheirAddress();
                //System.out.println("Done");
		return err ; 
	}

	private Error FillAllLevelNamesWithTheirAddress() {
		Error err = new Error() ; 
		for(int i=0;i<AllInstructions.size();i++){
			Instruction inst=AllInstructions.get(i);
			err = inst.fillLevelNameWithAddress();
			if(!err.isOk()){
				return err ; 
			}
			AllInstructions.set(i, inst);
		}
		return err ; 
	}

	//public static void main(String[] args) throws IOException {
	//	new MIPSProgram();
	//}
}