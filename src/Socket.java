import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Socket extends Thread{
    private final Environment env;
    private DatagramSocket socket;

    public Socket(Environment env) {
        this.env = env;
    }

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
            PDU pdu = new PDU(data.substring(3));
            destinationRoutine(pdu);
        }else {
            tokenRoutine();
        }
    }
    private void destinationRoutine(PDU pdu) {
        if(pdu.getDestinationNickname().equals(env.machineName)){

        }else{
            try {
                sendPackage(pdu);
            } catch (NumberFormatException | UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    private void tokenRoutine() {
    }

    private void sendPackage(PDU pdu) throws NumberFormatException, UnknownHostException{
        byte[] packageToSend = pdu.getOriginalData().getBytes();
        
        DatagramPacket packet
                = new DatagramPacket(
                    packageToSend,
                    packageToSend.length,
                    InetAddress.getByAddress(env.nextIp.getBytes()),
                    Integer.valueOf(env.port)
                );
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
