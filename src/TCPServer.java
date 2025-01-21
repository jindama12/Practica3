import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServer {
    public static void main(String[] args) {
        ArrayList<String> nombres = new ArrayList<String>();
        nombres.add("Jose");
        nombres.add("Hugo");

        int puerto = 2025;

        ServerSocket servidor = null;
        try {
            servidor = new ServerSocket(puerto);

            while (true) {
                Socket cliente = servidor.accept();
                InputStream is = cliente.getInputStream();
                DataInputStream dis = new DataInputStream(is);

                OutputStream os = cliente.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);

                String usuarioBuscar = dis.readUTF();
                System.out.println(usuarioBuscar);

                boolean existe = false;
                for (String usuario : nombres) {
                    if (usuario.toLowerCase().equals(usuarioBuscar.toLowerCase())) {
                        existe = true;
                    }
                }

                dos.writeBoolean(existe);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
