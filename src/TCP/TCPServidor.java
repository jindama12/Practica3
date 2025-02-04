package TCP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServidor {
    public static final ArrayList<String> nombresUsuarios = new ArrayList<>();
    public static final ArrayList<HiloServidor> hilosClientes = new ArrayList<>();

    public static void main(String[] args) {
        int puerto = 12345;

        try {
            ServerSocket socketServidor = new ServerSocket(puerto);

            while (true) {
                Socket socketCliente = socketServidor.accept();

                Thread cliente = new Thread(new HiloServidor(socketCliente));
                cliente.start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}
