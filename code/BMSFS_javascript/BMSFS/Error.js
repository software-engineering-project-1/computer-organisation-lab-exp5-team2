class Error {
    constructor(msg, type) {
        const NO_ERROR = 0;
        const ASSEMBLE_TIME_ERROR = 1;
        const RUN_TIME_ERROR = 2;
        const RUN_TIME_WARNING = 3;
        if (msg === undefined)
            errorMsg = "";
        else
            errorMsg = msg;
        if (errorType === undefined)
            errorType = NO_ERROR;
        else
            errorType = type;
    }
    isOk() {
        return (this.errorType == Error.NO_ERROR || this.errorType == Error.RUN_TIME_WARNING);
    }
    isWarning() {
        return (this.errorType == Error.RUN_TIME_WARNING);
    }
    printErrorMsg() {
        console.info(this.errorMsg);
    }
    getErrorMsg() {
        return this.errorMsg;
    }
    getErrorType() {
        return this.errorType;
    }
    getErrorTypeString() {
        if ((this.errorType == Error.NO_ERROR)) {
            return "NO_ERROR";
        } else if ((this.errorType == Error.ASSEMBLE_TIME_ERROR)) {
            return "ASSEMBLE_TIME_ERROR";
        } else if ((this.errorType == Error.RUN_TIME_ERROR)) {
            return "RUN_TIME_ERROR";
        } else if ((this.errorType == Error.RUN_TIME_WARNING)) {
            return "RUN_TIME_WARNING";
        }
        return "";
    }
}


