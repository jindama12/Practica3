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

            usuarioConectado(nombreUsuario);

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

    public void enviarListaUsuarios() {
        try {
            synchronized (hilosClientes) {
                StringBuilder listaUsuarios = new StringBuilder("USUARIOS_CONECTADOS:");
                for (String usuario : nombresUsuarios) {
                    listaUsuarios.append(usuario).append(",");
                }
                String lista = listaUsuarios.toString();
                for (HiloServidor cliente : hilosClientes) {
                    cliente.dos.writeUTF(lista);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void usuarioConectado(String nombreUsuario) {
        enviarMensaje("Usuario conectado: " + nombreUsuario);
        enviarListaUsuarios();
    }

    public void usuarioDesconectado(String nombreUsuario) {
        enviarMensaje("Usuario desconectado: " + nombreUsuario);
        enviarListaUsuarios();
    }

    public void desconectarCliente() {
        try {
            if (nombreUsuario != null) {
                synchronized (nombresUsuarios) {
                    nombresUsuarios.remove(nombreUsuario);
                }
                usuarioDesconectado(nombreUsuario);
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