import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class Socket extends Thread{
    private DatagramSocket socket;
    public void run(){
        while (true) {
            byte[] charArray = new byte[120];

            try {
                DatagramPacket packet
                        = new DatagramPacket(charArray, charArray.length);
                socket.receive(packet);

                processData(packet.getData());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processData(byte[] packegeContent){
        String data = new String(packegeContent, StandardCharsets.UTF_8);
        boolean isDataPackage = data.substring(0,3).equals("2000");

        if(isDataPackage){
            content = data.substring(3).split(":");
        }else {
            tokenRoutine();
        }
    }
    private void sendPackage(String header){
        packageToSend = header.toCharArray();
        DatagramPacket packet
                = new DatagramPacket(packageToSend, packageToSend.length, Enviroment.nextIp, Enviroment.port);
        socket.send(packet);
    }
}
