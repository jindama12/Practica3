import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class TCPCliente {
    private JPanel panel1;
    private JTextField textField1;
    private JButton button1;
    private static int puerto = 2025;

    public static void main(String[] args) {
        JFrame frame = new JFrame("TCPCliente");
        frame.setContentPane(new TCPCliente().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        //frame.setVisible(true);

        boolean usuarioExiste = false;
        String nombreUsuario;
        do {
            nombreUsuario = pedirNombreUsuario();
            usuarioExiste = existeUsuario(nombreUsuario);
        } while(usuarioExiste == true);

        System.exit(0);
    }

    private static String pedirNombreUsuario() {
        return JOptionPane.showInputDialog(null,"Introduce el nombre de usuario", "Nombre de usuario", JOptionPane.PLAIN_MESSAGE);
    }

    private static boolean existeUsuario(String nombreUsuario) {
        boolean existe = false;
        try {
            Socket cliente = new Socket("localhost", puerto);

            OutputStream os = cliente.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            dos.writeUTF(nombreUsuario);

            InputStream is = cliente.getInputStream();
            DataInputStream dis = new DataInputStream(is);

            existe = dis.readBoolean();

            if (existe) {
                System.out.println("Usuario existe");
            } else {
                System.out.println("Usuario no existe");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return existe;
    }
}
