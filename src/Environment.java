import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Environment {
    public String nextIp;
    public String machineName;
    public int tokenTime;
    public Boolean token;
    public int port;

    public Environment() {
        try {
            File myObj = new File("src/config.txt");
            Scanner myReader = new Scanner(myObj);
            int line = 0;
            while (myReader.hasNext()) {
                String data = myReader.nextLine();
                if (data.contains(":")) {
                    String[] temp = data.split(":");
                    nextIp = temp[0];
                    port = Integer.parseInt(temp[1]);
                } else if (isNumeric(data)) {
                    tokenTime = Integer.parseInt(data);
                } else if (data.equalsIgnoreCase("true")||data.equalsIgnoreCase("false")) {
                    token = Boolean.parseBoolean(data);
                } else {
                    machineName = data;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File was not found in the directory.");
            e.printStackTrace();
        }
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
