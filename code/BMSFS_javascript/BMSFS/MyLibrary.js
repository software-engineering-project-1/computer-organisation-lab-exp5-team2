class MyLibrary {
    static removeBeginningAndEndingSpace(s) {
        var ind1=0;
        var ind2=s.length-1;
        var n = s.length;
        while(ind1<n && isSpaceChar(s.charAt(ind1)))
            ind1++;
        while(ind2>=0 && isSpaceChar(s.charAt(ind2)))
            ind2--;
        if(ind2<ind1)
            return "";
        return s.substring(ind1, ind2+1);
    }
    static isSpaceChar(ch) {
        return ch==' ' || ch=='\t' || ch=='\n' || ch=='\r' || Character.testWhite(ch);
    }
    testWhite (x) {
        var white = new RegExp(/^\s$/);
        return white.test(x);
    }
    static HexStringToDecInt(str){ // string starts with 0x
        var sign=1;
        if(str.charAt(0)=='-'){
            sign=-1;
            str=str.substring(3);
        }else {
            str=str.substring(2);
        }
        return sign*parseInt(str,16);
    }
    static fromStringToInt(str){ // str can be in decimal or hexadecimal form 
        str=removeBeginningAndEndingSpace(str);
        if(str.startsWith("#")){
            str=str.substring(1);
        }
        if(str.startsWith("0x") || str.startsWith("-0x")){ // hexadecimal
            return HexStringToDecInt(str);
        }else{ // decimal
            return parseInt(str);
        }
    }
    static isValidNumber(str){
        str=removeBeginningAndEndingSpace(str);
        var len=str.length;
        if(len===0){
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
        var i = 0;
        for(i=0;i<len;i++){
            if(str.charAt(i)<='0'||str.charAt(i)>='9'){
                return false;
            }
        }
        return true;
    }   
}
