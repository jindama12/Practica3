package TCP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPCliente {
    private JFrame ventana;
    private JTextArea areaChat;
    private JTextField campoMensaje;

    private String nombreUsuario;
    private int puerto = 12345;

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TCPCliente::new);
    }

    public TCPCliente() {
        try {
            socket = new Socket("localhost", puerto);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            boolean existe = true;
            while (existe) {
                nombreUsuario = JOptionPane.showInputDialog(null, "Ingrese el nombre del usuario", "Nombre de usuario", JOptionPane.PLAIN_MESSAGE);
                if (nombreUsuario.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "El nombre de usuario no puede estar vacio", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    dos.writeUTF(nombreUsuario);
                    if (dis.readUTF().equals(nombreUsuario)) {
                        JOptionPane.showMessageDialog(null, "El nombre de usuario ya está en uso. Intenta otro nombre", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "¡Bienvenido al chat, " + nombreUsuario + "!");
                        existe = false;
                    }
                }
            }

            crearInterfaz();

            Thread escucharMensajes = new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = dis.readUTF()) != null) {
                        areaChat.append(mensaje + "\n");
                    }
                } catch (IOException e) {
                    System.exit(0);
                }
            });
            escucharMensajes.start();
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
                    dos.writeUTF(mensaje);
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
}
