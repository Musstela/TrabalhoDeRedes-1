import java.net.SocketException;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        Environment env = new Environment();
        System.out.println("Salve");

        Socket main = null;
        try {
            main = new Socket(env);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        main.start();

    }
}