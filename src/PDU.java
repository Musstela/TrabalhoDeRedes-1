public class PDU {
    private String originNickname;
    private String destinationNickname;
    private String errorLog;
    private String crc;
    private String message;
    private String originalData;

    public PDU(String content){
        String[] splittedContent = content.split(":");
        originNickname = splittedContent[0];
        destinationNickname = splittedContent[1];
        errorLog = splittedContent[2];
        crc = splittedContent[3];
        message = splittedContent[4];
        originalData = content;
    }
    
    public String getOriginNickname() {
        return originNickname;
    }

    public String getDestinationNickname() {
        return destinationNickname;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public String getCrc() {
        return crc;
    }

    public String getMessage() {
        return message;
    }

    public String getOriginalData(){
        return originalData;
    }
}
