package UDP;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UDPServidor {
    public static final ArrayList<String> nombresUsuarios = new ArrayList<>();
    public static final ArrayList<InetAddress> direccionesClientes = new ArrayList<>();
    public static final ArrayList<Integer> puertosClientes = new ArrayList<>();
    public static final Map<String, Long> ttlClientes = new HashMap<>();

    public static void main(String[] args) {
        int puerto = 12345;

        System.out.println("Servidor iniciado en el puerto " + puerto);
        try {
            DatagramSocket socketServidor = new DatagramSocket(puerto);

            Thread verificadorTTL = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        synchronized (nombresUsuarios) {
                            long tiempoActual = System.currentTimeMillis();
                            for (int i = 0; i < nombresUsuarios.size(); i++) {
                                String usuario = nombresUsuarios.get(i);
                                if (tiempoActual - ttlClientes.getOrDefault(usuario, 0L) > 10000) {
                                    System.out.println("Usuario desconectado: " + usuario);
                                    enviarMensajeATodos("Usuario desconectado: " + usuario);
                                    nombresUsuarios.remove(i);
                                    direccionesClientes.remove(i);
                                    puertosClientes.remove(i);
                                    ttlClientes.remove(usuario);
                                    i--;
                                    enviarListaUsuarios();
                                }
                            }
                        }
                    } catch (InterruptedException | IOException e) {
                        System.err.println("Error en el verificador de TTL: " + e.getMessage());
                    }
                }
            });
            verificadorTTL.start();

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                socketServidor.receive(paquete);

                String mensaje = new String(paquete.getData(), 0, paquete.getLength());
                InetAddress direccionCliente = paquete.getAddress();
                int puertoCliente = paquete.getPort();

                if (mensaje.startsWith("TTL:")) {
                    String usuario = mensaje.substring(4);
                    ttlClientes.put(usuario, System.currentTimeMillis());
                } else if (!nombresUsuarios.contains(mensaje) && !mensaje.contains(":")) {
                    nombresUsuarios.add(mensaje);
                    direccionesClientes.add(direccionCliente);
                    puertosClientes.add(puertoCliente);
                    ttlClientes.put(mensaje, System.currentTimeMillis());
                    enviarMensaje("valido", direccionCliente, puertoCliente);
                    enviarMensajeATodos("Usuario conectado: " + mensaje);
                    enviarListaUsuarios();
                } else if (mensaje.contains(":")) {
                    enviarMensajeATodos(mensaje);
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    private static void enviarMensaje(String mensaje, InetAddress direccion, int puerto) throws IOException {
        byte[] buffer = mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, direccion, puerto);
        new DatagramSocket().send(paquete);
    }

    private static void enviarMensajeATodos(String mensaje) throws IOException {
        for (int i = 0; i < direccionesClientes.size(); i++) {
            enviarMensaje(mensaje, direccionesClientes.get(i), puertosClientes.get(i));
        }
    }

    private static void enviarListaUsuarios() throws IOException {
        StringBuilder listaUsuarios = new StringBuilder("USUARIOS_CONECTADOS:");
        for (String usuario : nombresUsuarios) {
            listaUsuarios.append(usuario).append(",");
        }
        String lista = listaUsuarios.toString();
        enviarMensajeATodos(lista);
    }
}