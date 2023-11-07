import java.util.Objects;
import java.util.zip.CRC32;

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

    public PDU(String message, String destinationNickname, String originNickname) {
        this.originNickname = originNickname;
        this.destinationNickname = destinationNickname;
        this.message = message;
        this.errorLog = "maquinanaoexiste";
        this.crc = String.valueOf(generateCrc());
        originalData = null;
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

    private long generateCrc() {
        CRC32 temp = new CRC32();
        temp.update(getMessage().getBytes());
        return temp.getValue();
    }
    public String getOriginalData(){

        if (originalData.isEmpty()) {
            originalData =
                    "2000;"
                    + getOriginNickname()
                    + ":" + getDestinationNickname()
                    + ":" + getErrorLog()
                    + ":" + generateCrc()
                    + ":" + getMessage();
        }
        return originalData;
    }

    public void setErrorLog(String log) {
        this.errorLog = log;
        originalData =
                "2000;"
                + getOriginNickname()
                + ":" + getDestinationNickname()
                + ":" + getErrorLog()
                + ":" + getCrc()
                + ":" + getMessage();
    }

    public boolean checkCrc() {
        CRC32 temp = new CRC32();
        temp.update(getMessage().getBytes());
        return Objects.equals(String.valueOf(temp.getValue()), getCrc());
    }
}
