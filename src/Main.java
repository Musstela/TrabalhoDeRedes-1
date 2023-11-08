import java.net.SocketException;
import java.util.Objects;
import java.util.Scanner;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        Environment env = new Environment(); // Cria uma instância da classe Environment para configurar o ambiente do programa

        Socket main = null; // Declara uma variável Socket chamada 'main' e inicializa como nula
        try {
            main = new Socket(env); // Inicializa um novo objeto Socket usando o ambiente configurado
        } catch (SocketException e) {
            throw new RuntimeException(e); // Em caso de erro na criação do Socket, lança uma exceção RuntimeException
        }
        String userInput = "Don't mind me";
        Scanner myObj = new Scanner(System.in);  // Cria um Scanner para entrada do usuário
        while (!Objects.equals(userInput.toUpperCase(), "S")) {
            System.out.println("Would you like to create a PDUs or just start the program?");

            System.out.println("C - Create PDU");
            System.out.println("S - Start the program");
            userInput = myObj.nextLine(); // Aguarda a entrada do usuário

            if (Objects.equals(userInput.toUpperCase(), "C")) {
                if (!main.addPdu(createPdu(env))) {
                    System.out.println("Line is full, enough already");
                }
            } else {
                System.out.println("Ok then");
            }

        }

        main.start(); // Inicia a execução do socket
    }

    private static PDU createPdu(Environment env) {
        Scanner scn = new Scanner(System.in);  // Cria um Scanner para entrada do usuário
        System.out.println("What is the message?");
        String message = scn.nextLine();
        System.out.println("is it broadcast? (y/n)");
        String nickTo = null;
        if (scn.nextLine().equalsIgnoreCase("y")) {
            nickTo = "TODOS";
        } else {
            System.out.println("What is the nickname for the recipient?");
            nickTo = scn.nextLine();
        }
        System.out.println("Should we force an error with this message? (y/n)");
        boolean error = scn.nextLine().equalsIgnoreCase("y");
        return new PDU(message, nickTo, env.machineName, error);
    }

}