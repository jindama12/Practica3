package TCP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static TCP.TCPServidor.*;

public class HiloServidor implements Runnable {
    private Socket socket;
    private String nombreUsuario;
    private DataInputStream dis;
    private DataOutputStream dos;

    public HiloServidor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            boolean existe = true;
            while (existe) {
                nombreUsuario = dis.readUTF();
                synchronized (nombresUsuarios) {
                    if (!nombresUsuarios.contains(nombreUsuario)) {
                        dos.writeUTF("valido");
                        nombresUsuarios.add(nombreUsuario);
                        existe = false;
                    } else {
                        dos.writeUTF(nombreUsuario);
                    }
                }
            }

            synchronized (hilosClientes) {
                hilosClientes.add(this);
            }

            enviarMensaje("Usuario conectado: " + nombreUsuario);

            String mensaje;
            while ((mensaje = dis.readUTF()) != null) {
                enviarMensaje(nombreUsuario + ": " + mensaje);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            desconectarCliente();
        }
    }

    public void enviarMensaje(String mensaje) {
        try {
            synchronized (hilosClientes) {
                for (HiloServidor cliente : hilosClientes) {
                    cliente.dos.writeUTF(mensaje);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void desconectarCliente() {
        try {
            if (nombreUsuario != null) {
                synchronized (nombresUsuarios) {
                    nombresUsuarios.remove(nombreUsuario);
                }
                enviarMensaje("Usuario desconectado: " + nombreUsuario);
            }

            if (dis != null) {
                synchronized (hilosClientes) {
                    hilosClientes.remove(this);
                }
            }

            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
