import javax.swing.SwingUtilities;
public class Principal {
    public static void main(String[] args) {

        // SwingUtilities.invokeLater asegura que la interfaz se construya
        // dentro del hilo de eventos de Swing (Event Dispatch Thread),
        // que es la forma correcta y segura de iniciar cualquier GUI.
        SwingUtilities.invokeLater(() -> {
            GestionBiblioteca ventana = new GestionBiblioteca();
            ventana.setVisible(true);
        });
    }
}
