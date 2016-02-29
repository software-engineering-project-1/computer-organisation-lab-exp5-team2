class Memory{
	constructor(){
		const startAddressOfStack=Register.getRegValue("$sp");  // it should be 0x7FFFFFFC
		const currentStackAddress=startAddressOfStack;
		
		const startAddressOfDynamicData=0x10000000 ;
		const currentDynamicDataAddress=startAddressOfDynamicData;
		
		var dynamicDataSize = 4*(startAddressOfStack-startAddressOfDynamicData+1) ;
		const dynamicData=[];
		for(var i=0;i<dynamicDataSize ; i++){
			dynamicData.push(0); 
		}
		
		const localSymbolTable=new Array();
	}
	static setBeginingAddressOfWord(VariableName){
		if(currentDynamicDataAddress %4 !==0){
			currentDynamicDataAddress= (currentDynamicDataAddress/4 +1 ) *4 ; 
		}
		localSymbolTable[VariableName]=currentDynamicDataAddress;
	}
	static setBeginingAddressOfHalfWord(VariableName){
		if(currentDynamicDataAddress %2 !==0){
			currentDynamicDataAddress= (currentDynamicDataAddress/2 +1 ) *2 ; 
		}
		localSymbolTable[VariableName]=currentDynamicDataAddress;
	}
	static setBeginingAddressOfByte(VariableName){
		localSymbolTable[VariableName]=currentDynamicDataAddress;
	}
	

	/*********************  NEW IMPLEMENTATION **********************************
	 * In this implementation , every element of array dynamicData contains a byte ,
	 * and these bytes are stored in Little-Indian fashion . 
	 * */
	
	static addWordInDynamicMemory( value){
		while(currentDynamicDataAddress %4 !==0  && currentDynamicDataAddress < currentStackAddress){ // automatic alignment on word boundry 
			dynamicData[currentDynamicDataAddress++]= 0 ; 
		}
		if(currentDynamicDataAddress +4 < currentStackAddress){
			storeWord(currentDynamicDataAddress, value);
			currentDynamicDataAddress+=4;
		}
	}
	static addHalfWordInDynamicMemory( value){
		while(currentDynamicDataAddress %2 !==0  && currentDynamicDataAddress < currentStackAddress){ // automatic alignment on word boundry 
			dynamicData[currentDynamicDataAddress++]= 0 ; 
		}
		if(currentDynamicDataAddress +4 < currentStackAddress){
			storeHalfWord(currentDynamicDataAddress, value);
			currentDynamicDataAddress+=2;
		}
	}
	static addByteInDynamicMemory( value){
		if(currentDynamicDataAddress  < currentStackAddress){
			storeByte(currentDynamicDataAddress, value);
			currentDynamicDataAddress+=1;
		}
	}
	
	static readWord( VariableName , disp /*should be multiple of 4*/){
		if(typeof VariableName==='string')
		{
		    if (disp==="undefined")
		        return readWord(VariableName,0);
		    else 
	    		var startAddress=localSymbolTable[VariableName];
	    		var addr=startAddress+disp ;
	    		return readWord(addr);
	    	}
	    else
	    {	
	    	addr=VariableName;
			var ret=0;
			if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
				if(addr%4===0){
	                var ind=addr-startAddressOfDynamicData ;
					for(var i=0;i<4;i++){
						var singleByte = dynamicData[ind+i] & 0xFF ;
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
	}
	
	static readHalfWord( VariableName , disp /*should be multiple of 2*/){
	    if( typeof VariableName==='string')
	    {
		    if (disp==="undefined")
		        return readHalfWord(VariableName,0);
		    else
	    		var startAddress=localSymbolTable[VariableName];
	    		var addr=startAddress+disp ;
	    		return readHalfWord(addr);}
	    else
	    {
	    	addr=VariableName;
			var ret=0;
			if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
				if(addr%2===0){
					var ind=addr-startAddressOfDynamicData ;
					for(var i=0;i<2;i++){
						var singleByte = dynamicData[ind+1] & 0xFF ; 
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
	}
	
	static readByte( VariableName ,disp ){
		if(typeof VariableName=='string')
		{
	    if (disp==="undefined")
	        return readByte(VariableName,0);
	    else
    		var startAddress=localSymbolTable[VariableName];
    		var addr=startAddress+disp ;
    		return readByte(addr);
    	}
    	else{
	    	addr=VariableName;
			var ret=0;
			if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
				
					var ind=addr-startAddressOfDynamicData ;
					ret=dynamicData[ind] & 0xFF ;
			}else{
				// ERROR ( addr out of range -> seg fault )
			}
			return (ret << 24 >> 24) ; // returning signed Byte as integer 
		}
	}
	static storeWord(VariableName ,  disp /*should be multiple of 4*/ ,  value){
	    if (typeof VariableName === 'string'){
		    if (disp==="undefined")
		        storeWord(VariableName,0,value);
		    else     
	    		var startAddress=localSymbolTable[VariableName];
	    		var addr=startAddress+disp ;
	    		storeWord(addr, value);
	    	}
	    else{
	    	addr=VariableName;
	    	value=disp;
	    	if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
				if(addr % 4===0){
					var ind=addr-startAddressOfDynamicData ;
					for(var i=0;i<4;i++){
						dynamicData[ind+i]=(value>>(8*i)) & 0xFF ;
	                                        
					}
				}else{
					// addr is not aligned on word boundry
				}
			}else{
				// ERROR ( addr out of range -> seg fault )
			}
	    }	
	}


	static storeHalfWord(VariableName , disp /*should be multiple of 2*/ ,  value){
		if (typeof VariableName === 'string'){
		    if (disp==="undefined")
			    storeHalfWord(VariableName, 0 , value) ;
			else
	    		var startAddress=localSymbolTable[VariableName];
	    		var addr=startAddress+disp ;
	    		storeHalfWord(addr, value);
		}
		else{
			addr=VariableName;
	    	value=disp;
			if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
				if(addr % 2===0){
					var ind=addr-startAddressOfDynamicData ;
					for(var i=0;i<2;i++){
						dynamicData[ind+i]= (value>>(8*i)) & 0xFF ;
					}
				}else{
					// addr is not aligned on Half word boundry
				}
			}else{
				// ERROR ( addr out of range -> seg fault )
			}

		}

	}
	
	static storeByte( VariableName , disp , value){
		if (typeof VariableName === 'string'){
		    if (disp==="undefined")
		        storeByte(VariableName,0,value);
		    else     
	    		var startAddress=localSymbolTable[VariableName];
	    		var addr=startAddress+disp ;
	    		storeByte(addr,value);
    	}
    	else{
    		addr=VariableName;
    		value=disp;
    		if(addr>=startAddressOfDynamicData && addr<startAddressOfStack){
				var ind=addr-startAddressOfDynamicData ;
				dynamicData[ind]=value & 0xFF;
			}else{
				// ERROR ( addr out of range -> seg fault )
			}
    	}
    }


}
