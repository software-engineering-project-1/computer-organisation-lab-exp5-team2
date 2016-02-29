package mipsparser_new.BMSFS ; 

public class Error{
	public static final int NO_ERROR=0 , ASSEMBLE_TIME_ERROR=1 , RUN_TIME_ERROR=2 , RUN_TIME_WARNING=3; 
	private String errorMsg ; 
	private int errorType ;
	public Error(){
		errorMsg="";
		errorType = NO_ERROR ; 
	}
	public Error(String msg , int type){
		errorMsg  = msg ; 
		errorType = type ;
	}
	
	public boolean isOk(){ 
		return (errorType == NO_ERROR || errorType == RUN_TIME_WARNING) ;
	}
	
	public boolean isWarning(){
		return ( errorType==RUN_TIME_WARNING ); 
	}
	
	public void printErrorMsg(){
		System.out.println(errorMsg);
	}
	
	public String getErrorMsg(){
		return errorMsg ; 
	}
	
	public int getErrorType(){
		return errorType ; 
	}

        public String getErrorTypeString(){
            if(errorType ==NO_ERROR ){
                return "NO_ERROR";
            }else if(errorType == ASSEMBLE_TIME_ERROR){
                return "ASSEMBLE_TIME_ERROR";
            }else if(errorType ==RUN_TIME_ERROR ){
                return "RUN_TIME_ERROR";
            }else if(errorType ==RUN_TIME_WARNING ){
                return "RUN_TIME_WARNING";
            }
            return "";
        }
}
