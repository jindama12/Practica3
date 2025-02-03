package TCP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPCliente {
    private JFrame frame;
    private JTextArea chat;
    private JTextField mensaje;
    private JList<String> listaUsuarios;
    private DefaultListModel<String> usuariosConectados;

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
                        if (mensaje.startsWith("USUARIOS_CONECTADOS:")) {
                            String[] usuarios = mensaje.substring("USUARIOS_CONECTADOS:".length()).split(",");
                            usuariosConectados.clear();
                            for (String usuario : usuarios) {
                                if (!usuario.isEmpty()) {
                                    usuariosConectados.addElement(usuario);
                                }
                            }
                        } else {
                            chat.append(mensaje + "\n");
                        }
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
        frame = new JFrame("Chat - Usuario: " + nombreUsuario);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 500);
        frame.setLayout(new BorderLayout());

        // Panel de usuarios conectados
        usuariosConectados = new DefaultListModel<>();
        listaUsuarios = new JList<>(usuariosConectados);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        scrollUsuarios.setPreferredSize(new Dimension(150, 400));
        frame.add(scrollUsuarios, BorderLayout.WEST);

        chat = new JTextArea();
        chat.setEditable(false);
        JScrollPane scroll = new JScrollPane(chat);
        frame.add(scroll, BorderLayout.CENTER);

        mensaje = new JTextField();
        JButton botonEnviar = new JButton("Enviar");

        ActionListener enviarAccion = e -> {
            try {
                String mensaje = this.mensaje.getText().trim();
                if (!mensaje.isEmpty()) {
                    dos.writeUTF(mensaje);
                    this.mensaje.setText("");
                }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        };

        mensaje.addActionListener(enviarAccion);
        botonEnviar.addActionListener(enviarAccion);

        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.add(mensaje, BorderLayout.CENTER);
        panelEntrada.add(botonEnviar, BorderLayout.EAST);
        frame.add(panelEntrada, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}