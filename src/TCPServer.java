import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServer {
    public static ArrayList<String> nombres = new ArrayList<>();

    public static void main(String[] args) {
        //Carga de datos de prueba
        nombres.add("Jose");
        nombres.add("Hugo");

        while (true) {
            int puerto = 2025;
            ServerSocket servidor = null;

            try {
                servidor = new ServerSocket(puerto);
                Socket cliente = servidor.accept();

                //Inicio del hilo
                Thread hilo = new Thread(new HiloServer(cliente));
                hilo.start();

                servidor.close();
            } catch (IOException e) {
                //System.err.println(e.getMessage());
            }
        }
    }
}
