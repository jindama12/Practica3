package UDP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;

public class UDPCliente {
    private JFrame ventana;
    private JTextArea areaChat;
    private JTextField campoMensaje;

    private String nombreUsuario;
    private int puerto = 12345;

    private DatagramSocket socket;
    private InetAddress direccionServidor;
    private boolean conectado = true;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UDPCliente::new);
    }

    public UDPCliente() {
        try {
            socket = new DatagramSocket();
            direccionServidor = InetAddress.getByName("localhost");

            boolean existe = true;
            while (existe) {
                nombreUsuario = JOptionPane.showInputDialog(null, "Ingrese el nombre del usuario", "Nombre de usuario", JOptionPane.PLAIN_MESSAGE);
                if (nombreUsuario.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "El nombre de usuario no puede estar vacio", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    enviarMensaje(nombreUsuario);
                    byte[] buffer = new byte[1024];
                    DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                    socket.receive(paquete);
                    String respuesta = new String(paquete.getData(), 0, paquete.getLength());
                    if (respuesta.equals("valido")) {
                        JOptionPane.showMessageDialog(null, "¡Bienvenido al chat, " + nombreUsuario + "!");
                        existe = false;
                    } else {
                        JOptionPane.showMessageDialog(null, "El nombre de usuario ya está en uso. Intenta otro nombre", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            crearInterfaz();

            // Hilo para escuchar mensajes del servidor
            Thread escucharMensajes = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                    while (conectado) {
                        socket.receive(paquete);
                        String mensaje = new String(paquete.getData(), 0, paquete.getLength());
                        areaChat.append(mensaje + "\n");
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            });
            escucharMensajes.start();

            // Hilo para actualizar el TTL en el servidor
            Thread ttlThread = new Thread(() -> {
                try {
                    while (conectado) {
                        enviarMensaje("TTL:" + nombreUsuario);
                        Thread.sleep(5000); // Actualizar el TTL cada 5 segundos
                    }
                } catch (InterruptedException | IOException e) {
                    System.err.println(e.getMessage());
                }
            });
            ttlThread.start();

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void crearInterfaz() {
        ventana = new JFrame("Chat - Usuario: " + nombreUsuario);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(500, 400);
        ventana.setLayout(new BorderLayout());

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaChat);
        ventana.add(scroll, BorderLayout.CENTER);

        campoMensaje = new JTextField();
        JButton botonEnviar = new JButton("Enviar");

        ActionListener enviarAccion = e -> {
            try {
                String mensaje = campoMensaje.getText().trim();
                if (!mensaje.isEmpty()) {
                    enviarMensaje(nombreUsuario + ": " + mensaje);
                    campoMensaje.setText("");
                }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        };

        campoMensaje.addActionListener(enviarAccion);
        botonEnviar.addActionListener(enviarAccion);

        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.add(campoMensaje, BorderLayout.CENTER);
        panelEntrada.add(botonEnviar, BorderLayout.EAST);
        ventana.add(panelEntrada, BorderLayout.SOUTH);

        ventana.setVisible(true);
    }

    private void enviarMensaje(String mensaje) throws IOException {
        byte[] buffer = mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, direccionServidor, puerto);
        socket.send(paquete);
    }
}