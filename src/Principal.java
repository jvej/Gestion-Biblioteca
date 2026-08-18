import javax.swing.SwingUtilities;
public class Principal {
    //Estudiantes:
    //Jeferson David Sanchez Sanchez
    //Axel Josue Montoya Ovares
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DashboardSwing ventana = new DashboardSwing();
            ventana.setVisible(true);
        });
    }
}
