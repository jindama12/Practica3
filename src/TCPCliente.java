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

        //Preguntar por nombre de usuario y comprobar si existe
        boolean usuarioExiste = false;
        String nombreUsuario;
        do {
            nombreUsuario = pedirNombreUsuario();
            usuarioExiste = existeUsuario(nombreUsuario);
        } while(usuarioExiste == true);

        System.exit(0);
    }

    //Función que muestra el cuadro de diálogo para pedir el nombre de usuario
    private static String pedirNombreUsuario() {
        return JOptionPane.showInputDialog(null,"Introduce el nombre de usuario", "Nombre de usuario", JOptionPane.PLAIN_MESSAGE);
    }

    //Función que comprueba si el nombre de usuario introducido en el cuadro de diálogo ya está en uso en el servidor
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
