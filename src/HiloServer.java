import java.io.*;
import java.net.Socket;

public class HiloServer implements Runnable {
    private Socket socket;

    public HiloServer() {
    }

    public HiloServer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String usuario = crearUsuario();
        System.out.println(usuario);
    }

    public String crearUsuario() {
        String usuario = null;

        try {
            InputStream is = socket.getInputStream();
            DataInputStream dis = new DataInputStream(is);

            OutputStream os = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            usuario = dis.readUTF();

            boolean existe = false;
            do {
                if (TCPServer.nombres.contains(usuario)) {
                    existe = true;
                }

                if (!existe) {
                    TCPServer.nombres.add(usuario);
                }

                dos.writeBoolean(existe);
            } while (existe);

            boolean salida = false;

            while(!salida) {

            }

        } catch (IOException e) {
            //System.err.println(e.getMessage());
        }

        return usuario;
    }
}
