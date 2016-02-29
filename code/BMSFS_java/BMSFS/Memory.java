package mipsparser_new.BMSFS;
import java.util.*;

public class Memory{

	public static int startAddressOfStack;
	public static int currentStackAddress; // stack is full uptill this pint 
	
	public static ArrayList<Integer> dynamicData; // dynamic data grows upWard
	public static int startAddressOfDynamicData;
	public static int currentDynamicDataAddress; // dynamic data is filled to the memory address before this address .
	                               // is we have to allocate memory then we can allocate memory starting
	                               // from this address .
	
	public static HashMap<String,Integer > localSymbolTable; // contains level(variable) names and their start address
	
	public Memory(){
		startAddressOfStack=Register.getRegValue("$sp");  // it should be 0x7FFFFFFC
		currentStackAddress=startAddressOfStack;
		
		startAddressOfDynamicData=0x10000000 ;
		currentDynamicDataAddress=startAddressOfDynamicData;
		
		int dynamicDataSize = 4*(startAddressOfStack-startAddressOfDynamicData+1) ;
		dynamicData = new ArrayList<Integer>(dynamicDataSize);
		
		for(int i=0;i<dynamicDataSize ; i++){
			dynamicData.add(0); 
		}
		
		localSymbolTable=new HashMap<String, Integer>();
	}
	
	public static void setBeginingAddressOfWord(String VariableName){
		if(currentDynamicDataAddress %4 !=0){
			currentDynamicDataAddress= (currentDynamicDataAddress/4 +1 ) *4 ; 
		}
		localSymbolTable.put(VariableName, currentDynamicDataAddress);
	}
	public static void setBeginingAddressOfHalfWord(String VariableName){
		if(currentDynamicDataAddress %2 !=0){
			currentDynamicDataAddress= (currentDynamicDataAddress/2 +1 ) *2 ; 
		}
		localSymbolTable.put(VariableName, currentDynamicDataAddress);
	}
	public static void setBeginingAddressOfByte(String VariableName){
		localSymbolTable.put(VariableName, currentDynamicDataAddress);
	}
	

	/*********************  NEW IMPLEMENTATION **********************************
	 * In this implementation , every element of array dynamicData contains a byte ,
	 * and these bytes are stored in Little-Indian fashion . 
	 * */
	
	public static void addWordInDynamicMemory(int value){
		while(currentDynamicDataAddress %4 !=0  && currentDynamicDataAddress < currentStackAddress){ // automatic alignment on word boundry 
			dynamicData.set(currentDynamicDataAddress++, 0) ; 
		}
		if(currentDynamicDataAddress +4 < currentStackAddress){
			storeWord(currentDynamicDataAddress, value);
			currentDynamicDataAddress+=4;
		}
	}
	public static void addHalfWordInDynamicMemory(int value){
		while(currentDynamicDataAddress %2 !=0  && currentDynamicDataAddress < currentStackAddress){ // automatic alignment on word boundry 
			dynamicData.set(currentDynamicDataAddress++, 0) ; 
		}
		if(currentDynamicDataAddress +4 < currentStackAddress){
			storeHalfWord(currentDynamicDataAddress, value);
			currentDynamicDataAddress+=2;
		}
	}
	public static void addByteInDynamicMemory(int value){
		if(currentDynamicDataAddress  < currentStackAddress){
			storeByte(currentDynamicDataAddress, value);
			currentDynamicDataAddress+=1;
		}
	}
	
	public static int readWord(int addr){
		int ret=0;
		if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
			if(addr%4==0){
				//System.out.println("Reading value at : "+addr);
                                int ind=addr-startAddressOfDynamicData ;
				for(int i=0;i<4;i++){
					int singleByte = dynamicData.get(ind+i) & 0xFF ;
					ret=ret|(singleByte<< (8*i)) ; 
				}
			}else{
				// addr is not aligned on word boundry 
			}
		}else{
			// ERROR ( addr out of range -> seg fault )
		}
		return ret;
	}
	
	public static int readHalfWord(int addr){
		int ret=0;
		if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
			if(addr%2==0){
				int ind=addr-startAddressOfDynamicData ;
				for(int i=0;i<2;i++){
					int singleByte = dynamicData.get(ind+i) & 0xFF ; 
					ret=ret|(singleByte<< (8*i)) ; 
				}
			}else{
				// addr is not aligned on half word boundry 
			}
		}else{
			// ERROR ( addr out of range -> seg fault )
		}
		return (ret << 16 >> 16 ) ; // returning HW as signed integer 
	}
	
	public static int readByte(int addr){
		int ret=0;
		if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
			
				int ind=addr-startAddressOfDynamicData ;
				ret=dynamicData.get(ind) & 0xFF ;
		}else{
			// ERROR ( addr out of range -> seg fault )
		}
		return (ret << 24 >> 24) ; // returning signed Byte as integer 
	}
	
	public static void storeWord(int addr , int value){
            //System.out.println("Calling sw for : "+addr);
            if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
			if(addr % 4==0){
				int ind=addr-startAddressOfDynamicData ;
				for(int i=0;i<4;i++){
					dynamicData.set(ind+i, (value>>(8*i)) & 0xFF );
                                        
				}
			}else{
				// addr is not aligned on word boundry
			}
		}else{
			// ERROR ( addr out of range -> seg fault )
		}
	}
	public static void storeHalfWord(int addr , int value){
		if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
			if(addr % 2==0){
				int ind=addr-startAddressOfDynamicData ;
				for(int i=0;i<2;i++){
					dynamicData.set(ind+i, (value>>(8*i)) & 0xFF );
				}
			}else{
				// addr is not aligned on Half word boundry
			}
		}else{
			// ERROR ( addr out of range -> seg fault )
		}
	}
	public static void storeByte(int addr , int value){
		if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
			int ind=addr-startAddressOfDynamicData ;
			dynamicData.set(ind, value & 0xFF);
		}else{
			// ERROR ( addr out of range -> seg fault )
		}
	}
	
	
	public static void storeWord(String VariableName , int disp /*should be multiple of 4*/ , int value){
		int startAddress=localSymbolTable.get(VariableName);
		int addr=startAddress+disp ;
		storeWord(addr, value);
	}
	public static void storeWord(String VariableName , int value){
		storeWord(VariableName,0,value);
	}
	
	
	public static void storeHalfWord(String VariableName , int disp /*should be multiple of 2*/ , int value){
		int startAddress=localSymbolTable.get(VariableName);
		int addr=startAddress+disp ;
		storeHalfWord(addr, value);
	}
	public static void storeHalfWord(String VariableName , int value){
		storeHalfWord(VariableName, 0 , value) ; 
	}
	
	
	public static void storeByte(String VariableName , int disp , int value){
		int startAddress=localSymbolTable.get(VariableName);
		int addr=startAddress+disp ;
		storeByte(addr,value);
	}
	public static void storeByte(String VariableName,int value){
		storeByte(VariableName,0,value);
	}
	
	
	public static int readWord(String VariableName , int disp /*should be multiple of 4*/){
		int startAddress=localSymbolTable.get(VariableName);
		int addr=startAddress+disp ;
		return readWord(addr);
	}
	public static int readWord(String VariableName){
		return readWord(VariableName,0);
	}
	
	
	public static int readHalfWord(String VariableName , int disp /*should be multiple of 2*/){
		int startAddress=localSymbolTable.get(VariableName);
		int addr=startAddress+disp ;
		return readHalfWord(addr);
	}
	public static int readHalfWord(String VariableName){
		return readHalfWord(VariableName,0);
	}
	
	
	public static int readByte(String VariableName , int disp ){
		int startAddress=localSymbolTable.get(VariableName);
		int addr=startAddress+disp ;
		return readByte(addr);
	}
	public static int readByte(String VariableName){
		return readByte(VariableName,0);
	}
	

        /*
	public static void printDynamicMemory() {
		for(int i=0;i<dynamicData.size();i++){
			System.out.println(Integer.toString(startAddressOfDynamicData+4*i)+" "+Integer.toString(dynamicData.get(i)));
		}
	}
        public static int readDynamicMemory(int address){
		if(address<startAddressOfDynamicData) return 0;
		int index=(address-startAddressOfDynamicData)/4;
		int len=dynamicData.size();
		if(index>=len) return 0;
		return dynamicData.get(index);
	}
        */
}