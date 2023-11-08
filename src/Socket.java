import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Scanner;

import static java.time.LocalDateTime.now;

public class Socket extends Thread {
    private final Environment env;
    private DatagramSocket socket;

    private LinkedList<PDU> PduLine;

    private Instant lastToken;

    public Socket(Environment env) throws SocketException {
        this.env = env; // Inicializa o ambiente com as configurações passadas
        this.socket = new DatagramSocket(5000); // Inicializa um socket Datagram na porta 5000
        this.PduLine = new LinkedList<>(); // Inicializa uma lista encadeada para armazenar PDUs
    }

    public void run() {
        if (env.token) {
            lastToken = Instant.now();
            System.out.println("Press enter to send package");
            Scanner myObj = new Scanner(System.in);  // Cria um Scanner para entrada do usuário
            myObj.nextLine(); // Aguarda a entrada do usuário
            myObj.close(); // Fecha o Scanner
            try {
                this.tokenRoutine(); // Executa a rotina do token
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        // Loop principal para processar pacotes continuamente
        while (true) {

            if (Duration.between(this.lastToken, Instant.now()).getSeconds() > 2L * env.tokenTime && env.token) {
                this.log("Humm, no token still? Seems like this ring is bigger than we though, or someone broke it :(");
            }

            byte[] charArray = new byte[120];

            try {
                DatagramPacket packet = new DatagramPacket(charArray, charArray.length);
                socket.receive(packet); // Recebe um pacote pelo socket

                processData(packet.getData(), packet.getLength()); // Processa os dados do pacote recebido

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                Thread.sleep(env.tokenTime * 1000L); // Aguarda o tempo especificado no ambiente
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void processData(byte[] packegeContent, int length) throws UnknownHostException {
        // Converte os dados do pacote em uma string usando UTF-8 e o comprimento especificado
        String data = new String(packegeContent, StandardCharsets.UTF_8).substring(0, length);

        if (data.substring(0, 4).contains("2000")) {
            // Se os primeiros caracteres indicam um pacote de dados
            PDU pdu = new PDU(data.substring(5)); // Cria um novo PDU com os dados restantes
            this.log("Package received: \n" + pdu); // Registra a recepção do pacote
            destinationRoutine(pdu); // Chama a rotina de destino com o PDU
        } else {
            tokenRoutine(); // Caso contrário, executa a rotina de token
        }
    }

    private void destinationRoutine(PDU pdu) throws UnknownHostException {
        if (pdu.getDestinationNickname().equals(env.machineName)) {
            if (pdu.checkCrc()) {
                pdu.setErrorLog("ACK");
                System.out.println("Packet for this computer received, resending to origin");
            } else {
                pdu.setErrorLog("NAK");
                System.out.println("Packet for this computer received with errors, resending to origin");
            }
        }
        if (pdu.getDestinationNickname().equals("TODOS")) {
            if (pdu.getErrorLog().equals("NAK")) {
                System.out.println("Packet broadcast received, already flagged as with errors, passing it forward");
            } else {
                if (pdu.checkCrc()) {
                    System.out.println("Packet broadcast received, passing it forward");
                } else {
                    pdu.setErrorLog("NAK");
                    System.out.println("Packet broadcast received with errors, resending to origin");
                }
            }
        }
        if (pdu.getOriginNickname().equals(env.machineName)) {
            if (pdu.getErrorLog().equals("maquinanaoexiste")) {
                this.log("Packet from this computer has unreachable destination, discarding");
                PduLine.removeFirst();
                sendToken();
                return;
            } else if (pdu.getErrorLog().equals("NAK")) {
                this.log("Packet from this computer has error, resending");
                sendPackage(PduLine.getFirst());
                return;
            } else {
                this.log("Packet from this computer successfully sent, removing from line");
                PduLine.removeFirst();
                sendToken();
                return;
            }
        } else {
            this.log("Packet not for us, passing forward");
        }
        try {
            sendPackage(pdu);
        } catch (NumberFormatException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void tokenRoutine() throws UnknownHostException {
        this.log("I have the token");
        if (Duration.between(this.lastToken, Instant.now()).getSeconds() < 2L * env.tokenTime && env.token) {
            this.log("Humm, this was faster than expected, more than one token in this ring apparently");
        }
        if (!PduLine.isEmpty()) {
            sendPackage(PduLine.getFirst());
        } else {
            this.log("There goes my token");
            sendToken();
        }
    }

    private void sendToken() throws UnknownHostException {
        // Cria um novo pacote de dados com o conteúdo "1000" como token
        DatagramPacket packet = new DatagramPacket(
                "1000".getBytes(), // Converte a string "1000" para um array de bytes
                "1000".getBytes().length, // Obtém o comprimento do array de bytes
                InetAddress.getByName(env.nextIp), // Obtém o endereço IP de destino do ambiente
                env.port // Obtém a porta de destino do ambiente
        );

        try {
            socket.send(packet); // Envia o pacote pelo socket
        } catch (IOException e) {
            e.printStackTrace(); // Em caso de erro no envio, imprime o rastreamento da pilha
        }
    }


    private void sendPackage(PDU pdu) throws NumberFormatException, UnknownHostException {
        this.log("package being sent: \n" + pdu); // Registra o envio do pacote no log
        byte[] packageToSend = pdu.getOriginalData().getBytes(); // Obtém os dados originais do PDU em forma de array de bytes

        // Cria um DatagramPacket com os dados do pacote, o endereço IP e a porta de destino
        DatagramPacket packet = new DatagramPacket(
                packageToSend, // Dados do pacote convertidos para bytes
                packageToSend.length, // Comprimento do array de bytes
                InetAddress.getByName(env.nextIp), // Endereço IP de destino do ambiente
                env.port // Porta de destino do ambiente
        );

        try {
            socket.send(packet); // Envia o pacote pelo socket
        } catch (IOException e) {
            e.printStackTrace(); // Em caso de erro no envio, imprime o rastreamento da pilha
        }
    }


    public boolean addPdu(PDU add) {
        if (PduLine.size() <= 10) {
            PduLine.add(add);
            return true;
        }
        return false;
    }

    private void log(String log) {
        System.out.println(now().getMinute() + ":" + now().getSecond() + ":" + String.valueOf(now().getNano()).substring(0, 2) + " - " + log + "\n");
    }
}