import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Scanner;

public class Socket extends Thread{
    private final Environment env;
    private DatagramSocket socket;

    private LinkedList<PDU> PduLine;

    private PDU newPackage;

    public Socket(Environment env) throws SocketException {
        this.env = env;
        this.socket = new DatagramSocket(5000);
        this.PduLine = new LinkedList<>();
        newPackage = new PDU("Oi pessoal!", "Deon",env.machineName);
    }

    public void run(){
        if(env.token) {
            PduLine.add(newPackage);
            System.out.println("Press enter to send package");
            Scanner myObj = new Scanner(System.in);  // Create a Scanner object
            myObj.nextLine();
            myObj.close();
            try {
                this.tokenRoutine();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
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
            try {
                Thread.sleep(env.tokenTime * 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processData(byte[] packegeContent) throws UnknownHostException {
        String data = new String(packegeContent, StandardCharsets.UTF_8);

        if(data.substring(0,12).contains("2000")){
            PDU pdu = new PDU(data.substring(3));
            destinationRoutine(pdu);
        }else {
            tokenRoutine();
        }
    }
    private void destinationRoutine(PDU pdu) {
        if(pdu.getDestinationNickname().equals(env.machineName)){
            if(pdu.checkCrc()) {
                pdu.setErrorLog("ACK");
                System.out.println("Packet for this computer received, resending to origin");
            }
            else {
                pdu.setErrorLog("NAK");
                System.out.println("Packet for this computer received with errors, resending to origin");
            }
        }
        System.out.println(pdu.getOriginNickname());
        if(pdu.getOriginNickname().equals(env.machineName)){
            if(pdu.getErrorLog().equals("maquinanaoexiste")){
                System.out.println("Packet from this computer has unreachable destination, discarding");
                PduLine.removeFirst();
                try {
                    sendToken();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                return;
            } else if (pdu.getErrorLog().equals("NAK")) {
                System.out.println("Packet from this computer has error, resending");
                try {
                    sendPackage(PduLine.getFirst());
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        }
        try {
            System.out.println("Packet not for us, passing forward");
            sendPackage(pdu);
        } catch (NumberFormatException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void tokenRoutine() throws UnknownHostException {
        System.out.println("I have the token");
        if(!PduLine.isEmpty()){
            sendPackage(PduLine.getFirst());
        }
        else{
            System.out.println("There goes my token");
            sendToken();
        }
    }

    private void sendToken() throws UnknownHostException {
        DatagramPacket packet
                = new DatagramPacket(
                "1000".getBytes(),
                "1000".length(),
                InetAddress.getByName(env.nextIp),
                env.port
        );
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPackage(PDU pdu) throws NumberFormatException, UnknownHostException{
        byte[] packageToSend = pdu.getOriginalData().getBytes();

        DatagramPacket packet
                = new DatagramPacket(
                packageToSend,
                packageToSend.length,
                InetAddress.getByName(env.nextIp),
                env.port
        );
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean addPdu(PDU add) {
        if (PduLine.size() >= 10) {
            PduLine.add(add);
            return true;
        }
        return false;
    }
}
