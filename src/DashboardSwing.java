
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
public class DashboardSwing extends JFrame {
//============================================================================
// GESTIÓN DE BIBLIOTECA - Dashboard con Java Swing (VERSIÓN 2 - REDISEÑO)
// ============================================================================
// =========================================================
// COLORES
// =========================================================
private final Color COLOR_FONDO = new Color(244, 246, 249);
    private final Color COLOR_SUPERFICIE = Color.WHITE;
    private final Color COLOR_BORDE = new Color(228, 231, 237);
    private final Color COLOR_VERDE = new Color(29, 158, 117);
    private final Color COLOR_VERDE_TEXTO = new Color(4, 52, 44);
    private final Color COLOR_AMBAR = new Color(186, 117, 23);
    private final Color COLOR_CORAL = new Color(163, 45, 45);
    private final Color COLOR_MORADO = new Color(127, 119, 221);

    //Colores por estado del libro: {franja lateral, fondo etiquetado, texto etiqueta}

    private Color[] coloresEstado(String estado) {
        switch (estado) {
            case "Disponible":
                return new Color[] {new Color(93, 202, 165), new Color (234, 243, 222), new Color(23, 52, 4)};
            case "Prestado":
                return new Color [] {new Color(239, 159, 39), new Color(250, 238, 218), new Color(65, 36, 2)};
            case "Vencido":
                return new Color[] {new Color(226, 75, 74), new Color(252, 235, 235), new Color(80, 19,19)};
            case "Reservado":
            default:
                return new Color[] {new Color(127, 119, 221), new Color(238, 237, 254)};
        }
    }


    //Listas de valores fijos
    private final String[] categorias = {
            "Ficción", "No ficción", "Ciencia", "Tecnología",
            "Historia", "Infantil", "Académico", "Otro"
    };

    private final String[] Estados = {
            "Disponible", "Prestado", "Reservado", "Vencido"
    };

    // COMPONENTES DEL FORMULARIO
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtCorreo;
    private JTextField txtTelefono;
    private JComboBox<String> cmbRol;
    private JComboBox<String> cmbEstado;
    private JTextField txtBuscar;

    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextArea txtNotas;

    private JLabel lblFechaHora;
    private JLabel lblEstadoBD;

    private int siguienteId = 6;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public DashboardSwing() {

        setTitle("Aplicación Swing - Interfaz de Usuario");
        setSize(1500, 930);
        setMinimumSize(new Dimension(1200, 750));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // Look & Feel
        configurarLookAndFeel();

        // Menú izquierdo
        add(crearMenuLateral(), BorderLayout.WEST);

        // Contenido central
        add(crearContenidoPrincipal(), BorderLayout.CENTER);

        // Barra inferior
        add(crearBarraEstado(), BorderLayout.SOUTH);

        // Actualizar fecha cada segundo
        iniciarReloj();
    }

    // =========================================================
    // LOOK AND FEEL
    // =========================================================
    private void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("No se pudo cargar Look & Feel.");
        }
    }

    // =========================================================
    // MENÚ LATERAL
    // =========================================================
    private JPanel crearMenuLateral() {

        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(250, 0));
        menu.setBackground(COLOR_MENU);
        menu.setLayout(new BorderLayout());

        // -------------------------
        // Perfil
        // -------------------------
        JPanel pnlPerfil = new JPanel();
        pnlPerfil.setOpaque(false);
        pnlPerfil.setLayout(new BoxLayout(pnlPerfil, BoxLayout.Y_AXIS));
        pnlPerfil.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel lblAvatar = new JLabel("●");
        lblAvatar.setFont(new Font("Arial", Font.BOLD, 70));
        lblAvatar.setForeground(new Color(56, 130, 230));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNombre = new JLabel("<html><center>Dr. Juan De Dios<br>Murillo Morera</center></html>");
        lblNombre.setForeground(Color.WHITE);
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRol = new JLabel("Administrador");
        lblRol.setForeground(new Color(220, 225, 230));
        lblRol.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlPerfil.add(lblAvatar);
        pnlPerfil.add(Box.createVerticalStrut(8));
        pnlPerfil.add(lblNombre);
        pnlPerfil.add(Box.createVerticalStrut(8));
        pnlPerfil.add(lblRol);

        menu.add(pnlPerfil, BorderLayout.NORTH);

        // -------------------------
        // Opciones
        // -------------------------
        JPanel opciones = new JPanel();
        opciones.setOpaque(false);
        opciones.setLayout(new BoxLayout(opciones, BoxLayout.Y_AXIS));
        opciones.setBorder(new EmptyBorder(5, 12, 5, 12));

        opciones.add(crearBotonMenu("⌂  Inicio", true));
        opciones.add(Box.createVerticalStrut(7));

        opciones.add(crearBotonMenu("♟  Usuarios", false));
        opciones.add(Box.createVerticalStrut(7));

        opciones.add(crearBotonMenu("□  Productos", false));
        opciones.add(Box.createVerticalStrut(7));

        opciones.add(crearBotonMenu("🛒  Ventas", false));
        opciones.add(Box.createVerticalStrut(7));

        opciones.add(crearBotonMenu("▥  Reportes", false));
        opciones.add(Box.createVerticalStrut(7));

        opciones.add(crearBotonMenu("⚙  Configuración", false));
        opciones.add(Box.createVerticalStrut(7));

        opciones.add(crearBotonMenu("ⓘ  Acerca de", false));

        menu.add(opciones, BorderLayout.CENTER);

        // -------------------------
        // Cerrar sesión
        // -------------------------
        JPanel panelSalir = new JPanel(new BorderLayout());
        panelSalir.setOpaque(false);
        panelSalir.setBorder(new EmptyBorder(10, 15, 20, 15));

        JButton btnSalir = crearBoton(
                "⇥  Cerrar Sesión",
                new Color(45, 70, 90),
                new Color(255, 90, 90)
        );

        btnSalir.addActionListener(e -> cerrarSesion());

        panelSalir.add(btnSalir);

        menu.add(panelSalir, BorderLayout.SOUTH);

        return menu;
    }

    private JButton crearBotonMenu(String texto, boolean activo) {

        JButton boton = new JButton(texto);

        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        boton.setPreferredSize(new Dimension(220, 55));

        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 16));

        boton.setForeground(Color.WHITE);
        boton.setBackground(activo ? COLOR_MENU_ACTIVO : COLOR_MENU);

        boton.setBorder(new EmptyBorder(0, 18, 0, 10));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!activo) {
                    boton.setBackground(new Color(38, 79, 108));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!activo) {
                    boton.setBackground(COLOR_MENU);
                }
            }
        });

        boton.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        DashboardSwing.this,
                        "Seleccionó: " + texto.replaceAll("[^A-Za-zÁÉÍÓÚáéíóúñÑ ]", "").trim()
                )
        );

        return boton;
    }

    // =========================================================
    // CONTENIDO PRINCIPAL
    // =========================================================
    private JPanel crearContenidoPrincipal() {

        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(COLOR_FONDO);

        principal.add(crearCabecera(), BorderLayout.NORTH);

        JPanel contenido = new JPanel();
        contenido.setBackground(COLOR_FONDO);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Tarjetas
        contenido.add(crearPanelTarjetas());
        contenido.add(Box.createVerticalStrut(12));

        // Formulario + tabla
        contenido.add(crearPanelGestionUsuarios());
        contenido.add(Box.createVerticalStrut(12));

        // Información y notas
        contenido.add(crearPanelInferior());

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        principal.add(scroll, BorderLayout.CENTER);

        return principal;
    }

    // =========================================================
    // CABECERA
    // =========================================================
    private JPanel crearCabecera() {

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(
                new CompoundBorder(
                        new MatteBorder(0, 0, 1, 0, COLOR_BORDE),
                        new EmptyBorder(22, 35, 18, 35)
                )
        );

        JPanel izquierda = new JPanel();
        izquierda.setOpaque(false);
        izquierda.setLayout(new BoxLayout(izquierda, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Dashboard");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 27));

        JLabel subtitulo = new JLabel("Bienvenido al sistema de gestión");
        subtitulo.setForeground(Color.GRAY);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 15));

        izquierda.add(titulo);
        izquierda.add(Box.createVerticalStrut(5));
        izquierda.add(subtitulo);

        cabecera.add(izquierda, BorderLayout.WEST);

        lblFechaHora = new JLabel();
        lblFechaHora.setHorizontalAlignment(SwingConstants.RIGHT);
        lblFechaHora.setForeground(COLOR_AZUL);
        lblFechaHora.setFont(new Font("SansSerif", Font.PLAIN, 14));

        cabecera.add(lblFechaHora, BorderLayout.EAST);

        return cabecera;
    }

    // =========================================================
    // TARJETAS
    // =========================================================
    private JPanel crearPanelTarjetas() {

        JPanel panel = new JPanel(new GridLayout(1, 4, 18, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        panel.setPreferredSize(new Dimension(1000, 140));

        panel.add(crearTarjeta(
                "👥",
                "128",
                "Usuarios",
                COLOR_AZUL
        ));

        panel.add(crearTarjeta(
                "□",
                "256",
                "Productos",
                COLOR_VERDE
        ));

        panel.add(crearTarjeta(
                "🛒",
                "75",
                "Ventas Hoy",
                COLOR_NARANJA
        ));

        panel.add(crearTarjeta(
                "$",
                "₡1,250,000",
                "Ingresos Hoy",
                COLOR_MORADO
        ));

        return panel;
    }

    private JPanel crearTarjeta(String icono, String numero, String descripcion, Color color) {
        JPanel tarjeta = crearPanelRedondeado();
        tarjeta.setLayout(new BorderLayout());

        JPanel arriba = new JPanel(new BorderLayout(15, 0));
        arriba.setOpaque(false);
        arriba.setBorder(new EmptyBorder(15, 18, 10, 18));

        JLabel lblIcono = new JLabel(icono);
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcono.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblIcono.setForeground(color);

        lblIcono.setPreferredSize(new Dimension(65, 65));
        lblIcono.setBorder(
                new LineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100), 1, true));

        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));

        JLabel lblNumero = new JLabel(numero);
        lblNumero.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 14));

        datos.add(lblNumero);
        datos.add(Box.createVerticalStrut(5));
        datos.add(lblDescripcion);

        arriba.add(lblIcono, BorderLayout.WEST);
        arriba.add(datos, BorderLayout.CENTER);

        JButton detalles = new JButton("Ver detalles  →");
        detalles.setForeground(color);
        detalles.setBackground(Color.WHITE);
        detalles.setBorder(new CompoundBorder(
                        new MatteBorder(1, 0, 0, 0, COLOR_BORDE),
                        new EmptyBorder(8, 15, 8, 15)
                )
        );

        detalles.setHorizontalAlignment(SwingConstants.LEFT);
        detalles.setFocusPainted(false);
        detalles.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tarjeta.add(arriba, BorderLayout.CENTER);
        tarjeta.add(detalles, BorderLayout.SOUTH);

        return tarjeta;
    }

    // =========================================================
    // GESTIÓN DE USUARIOS
    // =========================================================
    private JPanel crearPanelGestionUsuarios() {

        JPanel panel = crearPanelRedondeado();
        panel.setLayout(new BorderLayout(25, 10));
        panel.setBorder(new CompoundBorder(
                        new LineBorder(COLOR_BORDE, 1, true),
                        new EmptyBorder(18, 18, 15, 18)
                )
        );

        // Título
        JLabel titulo = new JLabel("Registro de Usuario");
        titulo.setForeground(COLOR_AZUL);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));

        panel.add(titulo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 30, 0));
        centro.setOpaque(false);

        centro.add(crearFormularioUsuario());
        centro.add(crearPanelTabla());

        panel.add(centro, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // FORMULARIO
    // =========================================================
    private JPanel crearFormularioUsuario() {

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombre = crearCampoTexto();
        txtApellido = crearCampoTexto();
        txtCorreo = crearCampoTexto();
        txtTelefono = crearCampoTexto();

        cmbRol = new JComboBox<>(new String[]{"Administrador", "Usuario", "Invitado"});

        cmbEstado = new JComboBox<>(new String[]{ "Activo", "Inactivo "});

        configurarCombo(cmbRol);
        configurarCombo(cmbEstado);

        agregarFilaFormulario(formulario, gbc, 0, "Nombre:", txtNombre);

        agregarFilaFormulario(formulario, gbc, 1, "Apellido:", txtApellido);

        agregarFilaFormulario(formulario, gbc, 2, "Correo:", txtCorreo);

        agregarFilaFormulario(formulario, gbc, 3, "Teléfono:", txtTelefono
        );

        agregarFilaFormulario(formulario, gbc, 4, "Rol:", cmbRol);

        agregarFilaFormulario(formulario, gbc, 5, "Estado:", cmbEstado);

        // Valores iniciales
        txtNombre.setText("Juan De Dios");
        txtApellido.setText("Murillo Morera");
        txtCorreo.setText("juan.murillo@una.ac.cr");
        txtTelefono.setText("8888-8888");

        panel.add(formulario, BorderLayout.CENTER);

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);

        JButton btnNuevo =
                crearBoton("▣ Nuevo", COLOR_VERDE, Color.WHITE);

        JButton btnGuardar =
                crearBoton("▣ Guardar", COLOR_AZUL, Color.WHITE);

        JButton btnEditar =
                crearBoton("✎ Editar", COLOR_NARANJA, Color.WHITE);

        JButton btnEliminar =
                crearBoton("▣ Eliminar", COLOR_ROJO, Color.WHITE);

        JButton btnLimpiar =
                crearBoton(
                        "⌫ Limpiar",
                        new Color(225, 229, 235),
                        Color.BLACK
                );

        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        panel.add(botones, BorderLayout.SOUTH);

        // Eventos
        btnNuevo.addActionListener(e -> limpiarFormulario());

        btnGuardar.addActionListener(e -> guardarUsuario());

        btnEditar.addActionListener(e -> editarUsuario());

        btnEliminar.addActionListener(e -> eliminarUsuario());

        btnLimpiar.addActionListener(e -> limpiarFormulario());

        return panel;
    }

    private JTextField crearCampoTexto() {

        JTextField campo = new JTextField();

        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campo.setPreferredSize(new Dimension(250, 35));

        campo.setBorder(
                new CompoundBorder(
                        new LineBorder(
                                new Color(195, 200, 210),
                                1,
                                true
                        ),
                        new EmptyBorder(5, 10, 5, 10)
                )
        );

        return campo;
    }

    private void configurarCombo(JComboBox<String> combo) {

        combo.setFont(new Font("SansSerif", Font.PLAIN, 14));

        combo.setPreferredSize(
                new Dimension(250, 35)
        );
    }

    private void agregarFilaFormulario(
            JPanel panel,
            GridBagConstraints gbc,
            int fila,
            String etiqueta,
            Component componente
    ) {

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0;

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(componente, gbc);
    }

    // =========================================================
    // TABLA
    // =========================================================
    private JPanel crearPanelTabla() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // -------------------------
        // Buscador
        // -------------------------
        JPanel buscador = new JPanel(new BorderLayout(10, 0));
        buscador.setOpaque(false);

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("SansSerif", Font.PLAIN, 14));

        txtBuscar = crearCampoTexto();

        JButton btnBuscar = crearBoton(
                "🔍",
                new Color(245, 247, 250),
                Color.DARK_GRAY
        );

        JPanel buscadorDerecha = new JPanel(new BorderLayout(6, 0));
        buscadorDerecha.setOpaque(false);

        buscadorDerecha.add(txtBuscar, BorderLayout.CENTER);
        buscadorDerecha.add(btnBuscar, BorderLayout.EAST);

        buscador.add(lblBuscar, BorderLayout.WEST);
        buscador.add(buscadorDerecha, BorderLayout.CENTER);

        panel.add(buscador, BorderLayout.NORTH);

        // -------------------------
        // Modelo
        // -------------------------
        String[] columnas = {
                "ID",
                "Nombre",
                "Correo",
                "Rol",
                "Estado"
        };

        Object[][] datos = {

                {
                        1,
                        "Juan De Dios Murillo",
                        "juan.murillo@una.ac.cr",
                        "Administrador",
                        "Activo"
                },

                {
                        2,
                        "María García Pérez",
                        "maria.garcia@una.ac.cr",
                        "Usuario",
                        "Activo"
                },

                {
                        3,
                        "Carlos López Ruiz",
                        "carlos.lopez@una.ac.cr",
                        "Usuario",
                        "Inactivo"
                },

                {
                        4,
                        "Ana Lucía Soto",
                        "ana.soto@una.ac.cr",
                        "Usuario",
                        "Activo"
                },

                {
                        5,
                        "Luis Fernando Mora",
                        "luis.mora@una.ac.cr",
                        "Administrador",
                        "Activo"
                }
        };

        modeloTabla = new DefaultTableModel(
                datos,
                columnas
        ) {
            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);

        tablaUsuarios.setRowHeight(36);
        tablaUsuarios.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        tablaUsuarios.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        tablaUsuarios.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );

        tablaUsuarios.setSelectionBackground(
                new Color(190, 218, 250)
        );

        tablaUsuarios.setGridColor(
                new Color(225, 228, 235)
        );

        tablaUsuarios.setShowVerticalLines(true);
        tablaUsuarios.setShowHorizontalLines(true);

        tablaUsuarios.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        sorter =
                new TableRowSorter<>(modeloTabla);

        tablaUsuarios.setRowSorter(sorter);

        JScrollPane scroll =
                new JScrollPane(tablaUsuarios);

        scroll.setPreferredSize(
                new Dimension(600, 220)
        );

        panel.add(scroll, BorderLayout.CENTER);

        // Al seleccionar una fila, cargar formulario
        tablaUsuarios.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e
                    ) {
                        cargarUsuarioSeleccionado();
                    }
                }
        );

        // -------------------------
        // Filtro automático
        // -------------------------
        txtBuscar.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(
                            DocumentEvent e
                    ) {
                        filtrarTabla();
                    }

                    @Override
                    public void removeUpdate(
                            DocumentEvent e
                    ) {
                        filtrarTabla();
                    }

                    @Override
                    public void changedUpdate(
                            DocumentEvent e
                    ) {
                        filtrarTabla();
                    }
                }
        );

        btnBuscar.addActionListener(
                e -> filtrarTabla()
        );

        // -------------------------
        // Paginación visual
        // -------------------------
        JPanel paginacion = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        6,
                        0
                )
        );

        paginacion.setOpaque(false);

        paginacion.add(
                crearBotonPequeno("|<")
        );

        paginacion.add(
                crearBotonPequeno("<")
        );

        JButton pagina1 =
                crearBotonPequeno("1");

        pagina1.setBackground(COLOR_AZUL);
        pagina1.setForeground(Color.WHITE);

        paginacion.add(pagina1);
        paginacion.add(crearBotonPequeno("2"));
        paginacion.add(crearBotonPequeno("3"));
        paginacion.add(crearBotonPequeno(">"));
        paginacion.add(crearBotonPequeno(">|"));

        JLabel mostrando =
                new JLabel(
                        "   Mostrando 1 - 5 de 12"
                );

        mostrando.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        paginacion.add(mostrando);

        panel.add(
                paginacion,
                BorderLayout.SOUTH
        );

        return panel;
    }

    // =========================================================
    // PANEL INFERIOR
    // =========================================================
    private JPanel crearPanelInferior() {

        JPanel panel = new JPanel(
                new GridLayout(1, 2, 15, 0)
        );

        panel.setOpaque(false);

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        250
                )
        );

        panel.setPreferredSize(
                new Dimension(
                        1000,
                        220
                )
        );

        panel.add(crearInformacionSistema());
        panel.add(crearNotasRapidas());

        return panel;
    }

    private JPanel crearInformacionSistema() {

        JPanel panel = crearPanelRedondeado();

        panel.setLayout(
                new BorderLayout()
        );

        panel.setBorder(
                new CompoundBorder(
                        new LineBorder(
                                COLOR_BORDE,
                                1,
                                true
                        ),
                        new EmptyBorder(
                                18,
                                18,
                                18,
                                18
                        )
                )
        );

        JLabel titulo =
                new JLabel(
                        "Información del Sistema"
                );

        titulo.setForeground(COLOR_AZUL);

        titulo.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        16
                )
        );

        panel.add(
                titulo,
                BorderLayout.NORTH
        );

        JPanel contenido =
                new JPanel(
                        new GridBagLayout()
                );

        contenido.setOpaque(false);

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(
                        6,
                        8,
                        6,
                        8
                );

        gbc.anchor =
                GridBagConstraints.WEST;

        agregarInfo(
                contenido,
                gbc,
                0,
                "Versión:",
                "1.0.0"
        );

        agregarInfo(
                contenido,
                gbc,
                1,
                "Usuario Actual:",
                "Administrador"
        );

        agregarInfo(
                contenido,
                gbc,
                2,
                "Sistema Operativo:",
                System.getProperty("os.name")
        );

        agregarInfo(
                contenido,
                gbc,
                3,
                "Memoria JVM:",
                obtenerMemoria()
        );

        agregarInfo(
                contenido,
                gbc,
                4,
                "Java:",
                System.getProperty("java.version")
        );

        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy hh:mm a"
                );

        agregarInfo(
                contenido,
                gbc,
                5,
                "Fecha de Inicio:",
                LocalDateTime
                        .now()
                        .format(fmt)
        );

        panel.add(
                contenido,
                BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel crearNotasRapidas() {

        JPanel panel =
                crearPanelRedondeado();

        panel.setLayout(
                new BorderLayout(
                        0,
                        12
                )
        );

        panel.setBorder(
                new CompoundBorder(
                        new LineBorder(
                                COLOR_BORDE,
                                1,
                                true
                        ),
                        new EmptyBorder(
                                18,
                                18,
                                15,
                                18
                        )
                )
        );

        JLabel titulo =
                new JLabel(
                        "Notas Rápidas"
                );

        titulo.setForeground(
                COLOR_AZUL
        );

        titulo.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        16
                )
        );

        panel.add(
                titulo,
                BorderLayout.NORTH
        );

        txtNotas =
                new JTextArea();

        txtNotas.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        txtNotas.setLineWrap(true);
        txtNotas.setWrapStyleWord(true);

        txtNotas.setText(
                """
                Bienvenido al sistema.
                Aquí puede administrar usuarios, productos y ventas.
                Seleccione una opción del menú para comenzar.
                """
        );

        txtNotas.setBorder(
                new EmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        JScrollPane scroll =
                new JScrollPane(
                        txtNotas
                );

        panel.add(
                scroll,
                BorderLayout.CENTER
        );

        JButton btnGuardarNota =
                crearBoton(
                        "▣ Guardar Nota",
                        new Color(
                                245,
                                247,
                                250
                        ),
                        Color.DARK_GRAY
                );

        btnGuardarNota.addActionListener(
                e ->
                        JOptionPane.showMessageDialog(
                                this,
                                "Nota guardada correctamente.",
                                "Notas",
                                JOptionPane.INFORMATION_MESSAGE
                        )
        );

        JPanel pie =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                0,
                                0
                        )
                );

        pie.setOpaque(false);
        pie.add(btnGuardarNota);

        panel.add(
                pie,
                BorderLayout.SOUTH
        );

        return panel;
    }

    // =========================================================
    // BARRA DE ESTADO
    // =========================================================
    private JPanel crearBarraEstado() {

        JPanel barra =
                new JPanel(
                        new BorderLayout()
                );

        barra.setBackground(Color.WHITE);

        barra.setBorder(
                new CompoundBorder(
                        new MatteBorder(
                                1,
                                0,
                                0,
                                0,
                                COLOR_BORDE
                        ),
                        new EmptyBorder(
                                12,
                                25,
                                12,
                                25
                        )
                )
        );

        lblEstadoBD =
                new JLabel(
                        "●  Conectado a la base de datos"
                );

        lblEstadoBD.setForeground(
                new Color(
                        30,
                        140,
                        55
                )
        );

        lblEstadoBD.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        JLabel desarrollado =
                new JLabel(
                        "© 2026 - Desarrollado con Java Swing"
                );

        desarrollado.setForeground(
                Color.GRAY
        );

        desarrollado.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        barra.add(
                lblEstadoBD,
                BorderLayout.WEST
        );

        barra.add(
                desarrollado,
                BorderLayout.EAST
        );

        return barra;
    }

    // =========================================================
    // CRUD
    // =========================================================
    private void guardarUsuario() {

        if (!validarFormulario()) {
            return;
        }

        String nombreCompleto =
                txtNombre.getText().trim()
                        + " "
                        + txtApellido
                        .getText()
                        .trim();

        modeloTabla.addRow(
                new Object[]{
                        siguienteId++,
                        nombreCompleto,
                        txtCorreo
                                .getText()
                                .trim(),
                        cmbRol
                                .getSelectedItem(),
                        cmbEstado
                                .getSelectedItem()
                }
        );

        JOptionPane.showMessageDialog(
                this,
                "Usuario guardado correctamente.",
                "Guardar usuario",
                JOptionPane.INFORMATION_MESSAGE
        );

        limpiarFormulario();
    }

    private void editarUsuario() {

        int filaVista =
                tablaUsuarios
                        .getSelectedRow();

        if (filaVista == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un usuario de la tabla.",
                    "Editar",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!validarFormulario()) {
            return;
        }

        int filaModelo =
                tablaUsuarios
                        .convertRowIndexToModel(
                                filaVista
                        );

        String nombreCompleto =
                txtNombre.getText().trim()
                        + " "
                        + txtApellido
                        .getText()
                        .trim();

        modeloTabla.setValueAt(
                nombreCompleto,
                filaModelo,
                1
        );

        modeloTabla.setValueAt(
                txtCorreo.getText().trim(),
                filaModelo,
                2
        );

        modeloTabla.setValueAt(
                cmbRol.getSelectedItem(),
                filaModelo,
                3
        );

        modeloTabla.setValueAt(
                cmbEstado.getSelectedItem(),
                filaModelo,
                4
        );

        JOptionPane.showMessageDialog(
                this,
                "Usuario actualizado correctamente.",
                "Editar",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void eliminarUsuario() {

        int filaVista =
                tablaUsuarios
                        .getSelectedRow();

        if (filaVista == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un usuario de la tabla.",
                    "Eliminar",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int respuesta =
                JOptionPane.showConfirmDialog(
                        this,
                        "¿Desea eliminar el usuario seleccionado?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (
                respuesta
                        == JOptionPane.YES_OPTION
        ) {

            int filaModelo =
                    tablaUsuarios
                            .convertRowIndexToModel(
                                    filaVista
                            );

            modeloTabla.removeRow(
                    filaModelo
            );

            limpiarFormulario();

            JOptionPane.showMessageDialog(
                    this,
                    "Usuario eliminado correctamente."
            );
        }
    }

    private boolean validarFormulario() {

        if (
                txtNombre.getText().trim().isEmpty()
                        ||
                        txtApellido.getText().trim().isEmpty()
                        ||
                        txtCorreo.getText().trim().isEmpty()
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Nombre, apellido y correo son obligatorios.",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        if (
                !txtCorreo
                        .getText()
                        .contains("@")
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ingrese un correo electrónico válido.",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        return true;
    }

    private void cargarUsuarioSeleccionado() {

        int filaVista =
                tablaUsuarios
                        .getSelectedRow();

        if (filaVista == -1) {
            return;
        }

        int filaModelo =
                tablaUsuarios
                        .convertRowIndexToModel(
                                filaVista
                        );

        String nombreCompleto =
                modeloTabla
                        .getValueAt(
                                filaModelo,
                                1
                        )
                        .toString();

        String[] partes =
                nombreCompleto.split(
                        " ",
                        2
                );

        txtNombre.setText(
                partes[0]
        );

        txtApellido.setText(
                partes.length > 1
                        ? partes[1]
                        : ""
        );

        txtCorreo.setText(
                modeloTabla
                        .getValueAt(
                                filaModelo,
                                2
                        )
                        .toString()
        );

        cmbRol.setSelectedItem(
                modeloTabla
                        .getValueAt(
                                filaModelo,
                                3
                        )
        );

        cmbEstado.setSelectedItem(
                modeloTabla
                        .getValueAt(
                                filaModelo,
                                4
                        )
        );
    }

    private void limpiarFormulario() {

        txtNombre.setText("");
        txtApellido.setText("");
        txtCorreo.setText("");
        txtTelefono.setText("");

        cmbRol.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);

        tablaUsuarios.clearSelection();

        txtNombre.requestFocus();
    }

    // =========================================================
    // BUSCADOR
    // =========================================================
    private void filtrarTabla() {

        String texto =
                txtBuscar
                        .getText()
                        .trim();

        if (texto.isEmpty()) {

            sorter.setRowFilter(null);

        } else {

            sorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)"
                                    + java.util.regex.Pattern
                                    .quote(texto)
                    )
            );
        }
    }

    // =========================================================
    // CERRAR SESIÓN
    // =========================================================
    private void cerrarSesion() {

        int opcion =
                JOptionPane.showConfirmDialog(
                        this,
                        "¿Desea cerrar la sesión?",
                        "Cerrar sesión",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (
                opcion
                        == JOptionPane.YES_OPTION
        ) {
            dispose();
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================
    private JPanel crearPanelRedondeado() {

        JPanel panel =
                new JPanel();

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new LineBorder(
                        COLOR_BORDE,
                        1,
                        true
                )
        );

        return panel;
    }

    private JButton crearBoton(
            String texto,
            Color fondo,
            Color letra
    ) {

        JButton boton =
                new JButton(texto);

        boton.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );

        boton.setBackground(fondo);
        boton.setForeground(letra);

        boton.setFocusPainted(false);

        boton.setBorder(
                new CompoundBorder(
                        new LineBorder(
                                new Color(
                                        Math.max(
                                                fondo.getRed() - 20,
                                                0
                                        ),
                                        Math.max(
                                                fondo.getGreen() - 20,
                                                0
                                        ),
                                        Math.max(
                                                fondo.getBlue() - 20,
                                                0
                                        )
                                ),
                                1,
                                true
                        ),
                        new EmptyBorder(
                                9,
                                14,
                                9,
                                14
                        )
                )
        );

        boton.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        return boton;
    }

    private JButton crearBotonPequeno(
            String texto
    ) {

        JButton boton =
                new JButton(texto);

        boton.setPreferredSize(
                new Dimension(
                        42,
                        36
                )
        );

        boton.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );

        boton.setFocusPainted(false);

        return boton;
    }

    private void agregarInfo(
            JPanel panel,
            GridBagConstraints gbc,
            int fila,
            String titulo,
            String valor
    ) {

        gbc.gridx = 0;
        gbc.gridy = fila;

        JLabel lblTitulo =
                new JLabel(titulo);

        lblTitulo.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        panel.add(
                lblTitulo,
                gbc
        );

        gbc.gridx = 1;

        JLabel lblValor =
                new JLabel(valor);

        lblValor.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        panel.add(
                lblValor,
                gbc
        );
    }

    private String obtenerMemoria() {

        Runtime runtime =
                Runtime.getRuntime();

        long memoriaUsada =
                runtime.totalMemory()
                        - runtime.freeMemory();

        long memoriaMB =
                memoriaUsada
                        / 1024
                        / 1024;

        return memoriaMB + " MB";
    }

    // =========================================================
    // RELOJ
    // =========================================================
    private void iniciarReloj() {
        Timer timer = new Timer(1000, e -> actualizarFecha());
        timer.start();
        actualizarFecha();
    }

    private void actualizarFecha() {

        Locale locale = new Locale("es", "CR");

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, dd 'de' MMMM 'de' yyyy"
                                + "     hh:mm:ss a",
                        locale
                );

        String fecha =
                LocalDateTime
                        .now()
                        .format(formatter);

        lblFechaHora.setText(
                fecha
        );
    }

    // =========================================================
    // MAIN
    // =========================================================
    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(
                () -> {

                    DashboardSwing ventana =
                            new DashboardSwing();

                    ventana.setVisible(true);
                }
        );
    }
}