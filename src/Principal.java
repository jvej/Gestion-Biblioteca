import javax.swing.SwingUtilities;
public class Principal {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DashboardSwing ventana = new DashboardSwing();
            ventana.setVisible(true);
        });
    }
}
