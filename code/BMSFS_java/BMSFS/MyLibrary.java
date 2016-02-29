package mipsparser_new.BMSFS ; 

public class MyLibrary{
	
	public static String removeBeginningAndEndingSpace(String s) {
		int ind1=0 , ind2=s.length()-1 , n=s.length();
		while(ind1<n && isSpaceChar(s.charAt(ind1)))ind1++;
		while(ind2>=0 && isSpaceChar(s.charAt(ind2)))ind2--;
		if(ind2<ind1)return "";
		return s.substring(ind1, ind2+1);
	}

	public static boolean isSpaceChar(char ch) {
		return ch==' ' || ch=='\t' || ch=='\n' || ch=='\r' || Character.isWhitespace(ch);
	}
	
	public static int HexStringToDecInt(String str){ // string starts with 0x
		int sign=1;
		if(str.charAt(0)=='-'){
			sign=-1;
			str=str.substring(3);
		}else {
			str=str.substring(2);
		}
		return sign*Integer.parseInt(str,16);
	}
	
	public static int fromStringToInt(String str){ // str can be in decimal or hexadecimal form 
		str=removeBeginningAndEndingSpace(str);
		if(str.startsWith("#")){
			str=str.substring(1);
		}
		if(str.startsWith("0x") || str.startsWith("-0x")){ // hexadecimal
			return HexStringToDecInt(str);
		}else{ // decimal
			return Integer.parseInt(str);
		}
	}
	public static boolean isValidNumber(String str){
		str=removeBeginningAndEndingSpace(str);
		int len=str.length();
		if(len==0){
			return false;
		}
		if(str.startsWith("#")){
			str=str.substring(1);
		}
		if(str.charAt(0)=='+' || str.charAt(0)=='-'){
			str=str.substring(1);
		}
		if(str.startsWith("0x")){
			str=str.substring(2);
		}
		len=str.length();
		for(int i=0;i<len;i++){
			if(!Character.isDigit(str.charAt(i))){
				return false;
			}
		}
		return true;
	}
};