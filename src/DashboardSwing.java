import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * GESTIÓN DE BIBLIOTECA - Dashboard con Java Swing (VERSIÓN 3)
 * ---------------------------------------------------------------
 * Tres módulos administrados mediante CardLayout: Catálogo de libros,
 * Usuarios/Lectores y Préstamos/Devoluciones. Cada módulo expone un
 * formulario lateral de captura y una JTable con estilo oscuro,
 * ordenamiento y filtrado dinámico (TableRowSorter + RowFilter).
 */
public class DashboardSwing extends JFrame {

    // =========================================================
    // PALETA DE COLORES
    // =========================================================
    private final Color COLOR_FONDO = new Color(18, 18, 20);
    private final Color COLOR_SUPERFICIE = new Color(32, 32, 35);
    private final Color COLOR_SUPERFICIE_ALT = new Color(38, 38, 42);
    private final Color COLOR_BORDE = new Color(70, 70, 75);
    private final Color COLOR_TEXTO = new Color(235, 235, 238);
    private final Color COLOR_TEXTO_SECUNDARIO = new Color(165, 165, 172);
    private final Color COLOR_VERDE = new Color(29, 158, 117);
    private final Color COLOR_VERDE_TEXTO = new Color(4, 52, 44);
    private final Color COLOR_AMBAR = new Color(186, 117, 23);
    private final Color COLOR_CORAL = new Color(163, 45, 45);
    private final Color COLOR_MORADO = new Color(127, 119, 221);

    // Colores por estado (se reutilizan en libros, usuarios y préstamos):
    // {franja/fondo insignia, texto insignia}
    private Color[] coloresEstado(String estado) {
        switch (estado) {
            case "Disponible":
            case "Activo":
            case "Devuelto":
                return new Color[]{new Color(234, 243, 222), new Color(23, 52, 4)};
            case "Prestado":
                return new Color[]{new Color(250, 238, 218), new Color(65, 36, 2)};
            case "Vencido":
            case "Suspendido":
                return new Color[]{new Color(252, 235, 235), new Color(80, 19, 19)};
            case "Inactivo":
                return new Color[]{new Color(232, 232, 235), new Color(55, 55, 58)};
            case "Reservado":
            default:
                return new Color[]{new Color(238, 237, 254), new Color(38, 33, 92)};
        }
    }

    // =========================================================
    // LISTAS FIJAS PARA COMBOS
    // =========================================================
    private final String[] CATEGORIAS = {
            "Ficción", "No ficción", "Ciencia", "Tecnología",
            "Historia", "Infantil", "Académico", "Otro"
    };
    private final String[] ESTADOS_LIBRO = {"Disponible", "Prestado", "Reservado", "Vencido"};
    private final String[] TIPOS_USUARIO = {"Estudiante", "Docente", "General"};
    private final String[] ESTADOS_USUARIO = {"Activo", "Inactivo", "Suspendido"};

    // =========================================================
    // MODELOS DE DATOS
    // =========================================================
    private static class Libro {
        String codigo, titulo, autor, editorial, categoria, estado, observaciones;
        int anio;
        boolean referencia;

        Libro(String codigo, String titulo, String autor, String editorial, String categoria,
              int anio, String estado, boolean referencia, String observaciones) {
            this.codigo = codigo;
            this.titulo = titulo;
            this.autor = autor;
            this.editorial = editorial;
            this.categoria = categoria;
            this.anio = anio;
            this.estado = estado;
            this.referencia = referencia;
            this.observaciones = observaciones;
        }

        @Override
        public String toString() {
            return codigo + " — " + titulo;
        }
    }

    private static class Usuario {
        String id, nombre, correo, telefono, tipo, estado;

        Usuario(String id, String nombre, String correo, String telefono, String tipo, String estado) {
            this.id = id;
            this.nombre = nombre;
            this.correo = correo;
            this.telefono = telefono;
            this.tipo = tipo;
            this.estado = estado;
        }

        @Override
        public String toString() {
            return id + " — " + nombre;
        }
    }

    private static class Prestamo {
        String id;
        String codigoLibro, tituloLibro;
        String idUsuario, nombreUsuario;
        LocalDate fechaPrestamo, fechaLimite, fechaDevolucion;
        String estado;

        Prestamo(String id, String codigoLibro, String tituloLibro, String idUsuario, String nombreUsuario,
                 LocalDate fechaPrestamo, LocalDate fechaLimite, LocalDate fechaDevolucion, String estado) {
            this.id = id;
            this.codigoLibro = codigoLibro;
            this.tituloLibro = tituloLibro;
            this.idUsuario = idUsuario;
            this.nombreUsuario = nombreUsuario;
            this.fechaPrestamo = fechaPrestamo;
            this.fechaLimite = fechaLimite;
            this.fechaDevolucion = fechaDevolucion;
            this.estado = estado;
        }
    }


    private static class CampoInvalidoException extends Exception {
        CampoInvalidoException(String mensaje) {
            super(mensaje);
        }
    }
    // =========================================================
    // ALMACENAMIENTO EN MEMORIA
    // =========================================================
    private final List<Libro> libros = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Prestamo> prestamos = new ArrayList<>();

    private Libro libroSeleccionado = null;
    private Usuario usuarioSeleccionado = null;
    private Prestamo prestamoSeleccionado = null;

    private int siguienteCodigoLibro = 1;
    private int siguienteIdPrestamo = 1;

    // =========================================================
    // NAVEGACIÓN (CardLayout)
    // =========================================================
    private CardLayout cardLayout;
    private JPanel panelCentral;
    private String seccionActual = "catalogo";
    private final Map<String, PanelRedondeado> pildorasNav = new HashMap<>();
    private final Map<String, JLabel> etiquetasNav = new HashMap<>();

    // =========================================================
    // COMPONENTES: FORMULARIO LIBROS
    // =========================================================
    private JTextField txtCodigo, txtTitulo, txtAutor, txtEditorial;
    private JComboBox<String> cmbCategoria, cmbEstadoLibro;
    private JSpinner spnAnio;
    private JCheckBox chkReferencia;
    private JTextArea txtObservaciones;

    // =========================================================
    // COMPONENTES: FORMULARIO USUARIOS
    // =========================================================
    private JTextField txtIdUsuario, txtNombreUsuario, txtCorreo, txtTelefono;
    private JComboBox<String> cmbTipoUsuario, cmbEstadoUsuario;

    // =========================================================
    // COMPONENTES: FORMULARIO PRÉSTAMOS
    // =========================================================
    private JComboBox<Libro> cmbLibroPrestamo;
    private JComboBox<Usuario> cmbUsuarioPrestamo;
    private JSpinner spnFechaPrestamo, spnFechaLimite;
    private JLabel lblEstadoPrestamoSeleccionado;

    // =========================================================
    // COMPONENTES: TABLAS
    // =========================================================
    private DefaultTableModel modeloLibros, modeloUsuarios, modeloPrestamos;
    private JTable tablaLibros, tablaUsuarios, tablaPrestamos;
    private TableRowSorter<DefaultTableModel> sorterLibros, sorterUsuarios, sorterPrestamos;

    private JTextField txtBuscarLibros, txtBuscarUsuarios, txtBuscarPrestamos;
    private JLabel lblContadorLibros, lblContadorUsuarios, lblContadorPrestamos;

    private JLabel lblFechaHora;

    // Tarjetas del dashboard superior
    private JLabel lblTotalLibros;
    private JLabel lblDisponibles;
    private JLabel lblPrestamosActivos;
    private JLabel lblPrestamosVencidos;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public DashboardSwing() {

        setTitle("Gestión de Biblioteca - Dashboard Java Swing");
        setSize(1500, 930);
        setMinimumSize(new Dimension(1250, 750));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());

        configurarLookAndFeel();

        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearContenidoPrincipal(), BorderLayout.CENTER);

        cargarDatosIniciales();
        mostrarSeccion("catalogo");
        iniciarReloj();
    }

    // =========================================================
    // LOOK AND FEEL
    // =========================================================
    private void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            UIManager.put("Panel.background", COLOR_SUPERFICIE);
            UIManager.put("Label.foreground", COLOR_TEXTO);
            UIManager.put("OptionPane.background", COLOR_SUPERFICIE);
            UIManager.put("OptionPane.messageForeground", COLOR_TEXTO);

            UIManager.put("TextField.background", COLOR_SUPERFICIE);
            UIManager.put("TextField.foreground", COLOR_TEXTO);
            UIManager.put("TextField.caretForeground", COLOR_TEXTO);

            UIManager.put("TextArea.background", COLOR_SUPERFICIE);
            UIManager.put("TextArea.foreground", COLOR_TEXTO);
            UIManager.put("TextArea.caretForeground", COLOR_TEXTO);

            UIManager.put("ComboBox.background", COLOR_SUPERFICIE);
            UIManager.put("ComboBox.foreground", COLOR_TEXTO);
            UIManager.put("ComboBox.selectionBackground", COLOR_VERDE);
            UIManager.put("ComboBox.selectionForeground", COLOR_VERDE_TEXTO);

            UIManager.put("FormattedTextField.background", COLOR_SUPERFICIE);
            UIManager.put("FormattedTextField.foreground", COLOR_TEXTO);
            UIManager.put("Spinner.background", COLOR_SUPERFICIE);

            UIManager.put("CheckBox.foreground", COLOR_TEXTO);

            UIManager.put("ScrollPane.background", COLOR_FONDO);
            UIManager.put("Viewport.background", COLOR_FONDO);

            UIManager.put("Table.background", COLOR_SUPERFICIE);
            UIManager.put("Table.foreground", COLOR_TEXTO);
            UIManager.put("Table.selectionBackground", COLOR_VERDE);
            UIManager.put("Table.selectionForeground", COLOR_VERDE_TEXTO);
            UIManager.put("TableHeader.background", COLOR_FONDO);
            UIManager.put("TableHeader.foreground", COLOR_TEXTO_SECUNDARIO);

        } catch (Exception e) {
            System.out.println("No se pudo cargar Look & Feel.");
        }
    }

    // =========================================================
    // BARRA SUPERIOR / NAVEGACIÓN
    // =========================================================
    private JPanel crearBarraSuperior() {

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(COLOR_SUPERFICIE);
        barra.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDE), new EmptyBorder(10, 22, 10, 22)));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logo.setOpaque(false);
        JLabel lblIcono = new JLabel("\uD83D\uDCD6");
        lblIcono.setFont(new Font("SansSerif", Font.PLAIN, 22));
        JLabel lblLogo = new JLabel("Biblioteca");
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 16));
        logo.add(lblIcono);
        logo.add(lblLogo);
        barra.add(logo, BorderLayout.WEST);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nav.setOpaque(false);
        nav.add(crearPildoraNav("Catálogo", "catalogo"));
        nav.add(crearPildoraNav("Usuarios", "usuarios"));
        nav.add(crearPildoraNav("Préstamos", "prestamos"));
        barra.add(nav, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        derecha.setOpaque(false);

        lblFechaHora = new JLabel();
        lblFechaHora.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblFechaHora.setForeground(Color.GRAY);
        derecha.add(lblFechaHora);

        PanelRedondeado avatar = new PanelRedondeado(999);
        avatar.setBackground(new Color(216, 90, 48));
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setLayout(new GridBagLayout());
        JLabel lblIniciales = new JLabel("BC");
        lblIniciales.setForeground(new Color(74, 27, 12));
        lblIniciales.setFont(new Font("SansSerif", Font.BOLD, 11));
        avatar.add(lblIniciales);
        derecha.add(avatar);

        barra.add(derecha, BorderLayout.EAST);

        return barra;
    }

    private PanelRedondeado crearPildoraNav(String texto, String idSeccion) {
        PanelRedondeado pildora = new PanelRedondeado(999);
        pildora.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pildora.setBorder(new EmptyBorder(7, 14, 7, 14));
        pildora.setBackground(COLOR_FONDO);
        pildora.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(COLOR_TEXTO_SECUNDARIO);
        pildora.add(lbl);

        pildorasNav.put(idSeccion, pildora);
        etiquetasNav.put(idSeccion, lbl);

        pildora.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarSeccion(idSeccion);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!idSeccion.equals(seccionActual)) pildora.setBackground(new Color(45, 45, 49));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!idSeccion.equals(seccionActual)) pildora.setBackground(COLOR_FONDO);
            }
        });

        return pildora;
    }

    private void mostrarSeccion(String idSeccion) {
        seccionActual = idSeccion;
        cardLayout.show(panelCentral, idSeccion);

        for (Map.Entry<String, PanelRedondeado> entrada : pildorasNav.entrySet()) {
            boolean activo = entrada.getKey().equals(idSeccion);
            entrada.getValue().setBackground(activo ? COLOR_VERDE : COLOR_FONDO);
            JLabel etiqueta = etiquetasNav.get(entrada.getKey());
            etiqueta.setForeground(activo ? COLOR_VERDE_TEXTO : COLOR_TEXTO_SECUNDARIO);
            etiqueta.setFont(new Font("SansSerif", activo ? Font.BOLD : Font.PLAIN, 12));
        }

        if ("prestamos".equals(idSeccion)) {
            actualizarCombosPrestamo();
        }
    }

    // =========================================================
    // CONTENIDO PRINCIPAL
    // =========================================================
    private JPanel crearContenidoPrincipal() {
        PanelRedondeado principal = new PanelRedondeado(12);
        principal.setLayout(new BorderLayout());
        principal.setBackground(COLOR_FONDO);
        principal.setBorder(new EmptyBorder(14, 22, 18, 22));

        principal.add(crearPanelTarjetas(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        panelCentral = new JPanel(cardLayout);
        panelCentral.setOpaque(false);
        panelCentral.setBorder(new EmptyBorder(14, 0, 0, 0));

        panelCentral.add(crearPanelCatalogo(), "catalogo");
        panelCentral.add(crearPanelUsuarios(), "usuarios");
        panelCentral.add(crearPanelPrestamos(), "prestamos");

        principal.add(panelCentral, BorderLayout.CENTER);

        return principal;
    }

    private JPanel crearPanelTarjetas() {

        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(1000, 90));

        lblTotalLibros = new JLabel("0");
        lblDisponibles = new JLabel("0");
        lblPrestamosActivos = new JLabel("0");
        lblPrestamosVencidos = new JLabel("0");

        panel.add(crearTarjeta("📚", lblTotalLibros, "Libros en catálogo", COLOR_VERDE));
        panel.add(crearTarjeta("✔", lblDisponibles, "Disponibles ahora", new Color(99, 153, 34)));
        panel.add(crearTarjeta("⇄", lblPrestamosActivos, "Préstamos activos", COLOR_AMBAR));
        panel.add(crearTarjeta("⏱", lblPrestamosVencidos, "Préstamos vencidos", COLOR_CORAL));

        return panel;
    }

    private JPanel crearTarjeta(String icono, JLabel lblNumero, String descripcion, Color colorIcono) {

        PanelRedondeado tarjeta = new PanelRedondeado(12);
        tarjeta.setBackground(COLOR_SUPERFICIE);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblIcono = new JLabel(icono);
        lblIcono.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblIcono.setForeground(colorIcono);
        lblIcono.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblNumero.setFont(new Font("SansSerif", Font.BOLD, 21));
        lblNumero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblDescripcion.setForeground(Color.GRAY);
        lblDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);

        tarjeta.add(lblIcono);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(lblNumero);
        tarjeta.add(Box.createVerticalStrut(2));
        tarjeta.add(lblDescripcion);

        return tarjeta;
    }

    // =========================================================
    // MÓDULO 1: CATÁLOGO DE LIBROS
    // =========================================================
    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(16, 12));
        panel.setOpaque(false);
        panel.add(crearBarraAccionesLibro(), BorderLayout.NORTH);
        panel.add(crearFormularioLibro(), BorderLayout.WEST);

        modeloLibros = crearModeloNoEditable(new String[]{"Código", "Título", "Autor", "Editorial", "Categoría", "Año", "Estado"});
        tablaLibros = crearTablaOscura(modeloLibros);
        sorterLibros = new TableRowSorter<>(modeloLibros);
        tablaLibros.setRowSorter(sorterLibros);
        aplicarRendererFilas(tablaLibros);
        aplicarRendererBadge(tablaLibros, 6);

        tablaLibros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaLibros.getSelectedRow() != -1) {
                int fila = tablaLibros.convertRowIndexToModel(tablaLibros.getSelectedRow());
                String codigo = (String) modeloLibros.getValueAt(fila, 0);
                Libro l = buscarLibroPorCodigo(codigo);
                if (l != null) cargarLibroEnFormulario(l);
            }
        });

        txtBuscarLibros = new JTextField();
        lblContadorLibros = new JLabel("0 de 0 registros");
        lblContadorLibros.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblContadorLibros.setForeground(Color.GRAY);

        txtBuscarLibros.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
            private void aplicarFiltro() {
                filtrarTabla(txtBuscarLibros, sorterLibros);
                actualizarContador(lblContadorLibros, tablaLibros, libros.size());
            }
        });

        panel.add(crearPanelTabla(tablaLibros, txtBuscarLibros, lblContadorLibros,
                "Buscar por código, título, autor o categoría"), BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearFormularioLibro() {

        PanelRedondeado panel = new PanelRedondeado(12);
        panel.setBackground(COLOR_SUPERFICIE);
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel titulo = new JLabel("＋ Agregar / editar libro");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel();
        campos.setOpaque(false);
        campos.setLayout(new BoxLayout(campos, BoxLayout.Y_AXIS));

        txtCodigo = crearCampoTexto("Código");
        txtTitulo = crearCampoTexto("Título");
        txtAutor = crearCampoTexto("Autor");
        txtEditorial = crearCampoTexto("Editorial");

        cmbCategoria = new JComboBox<>(CATEGORIAS);
        cmbEstadoLibro = new JComboBox<>(ESTADOS_LIBRO);
        estilizarCombo(cmbCategoria);
        estilizarCombo(cmbEstadoLibro);

        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        spnAnio = new JSpinner(new SpinnerNumberModel(anioActual, 1450, anioActual, 1));
        alinearIzquierdaYAngosto(spnAnio);

        chkReferencia = new JCheckBox("Solo referencia (no se presta)");
        chkReferencia.setOpaque(false);
        chkReferencia.setFont(new Font("SansSerif", Font.PLAIN, 11));
        chkReferencia.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtObservaciones = new JTextArea(3, 10);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setBorder(new LineBorder(COLOR_BORDE, 1, true));
        scrollObs.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollObs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        campos.add(etiquetaCampo("Código"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(txtCodigo);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Título"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(txtTitulo);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Autor"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(txtAutor);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Editorial"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(txtEditorial);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Categoría"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(cmbCategoria);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Año"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(spnAnio);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Estado"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(cmbEstadoLibro);
        campos.add(Box.createVerticalStrut(6));
        campos.add(chkReferencia);
        campos.add(Box.createVerticalStrut(6));
        campos.add(etiquetaCampo("Observaciones"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(scrollObs);

        panel.add(campos, BorderLayout.CENTER);

        return panel;
    }

    /** Barra horizontal de acciones del módulo de Libros: se ubica entre las tarjetas del
     *  dashboard y la barra de búsqueda, en lugar de ir apilada dentro del formulario lateral. */
    private JPanel crearBarraAccionesLibro() {
        PanelRedondeado barra = new PanelRedondeado(12);
        barra.setBackground(COLOR_SUPERFICIE);
        barra.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setBorder(new EmptyBorder(10, 14, 10, 14));

        JButton btnGuardar = crearBoton("Guardar libro", COLOR_VERDE, COLOR_VERDE_TEXTO, true);
        JButton btnEditar = crearBoton("Editar", new Color(238, 240, 244), Color.BLACK, false);
        JButton btnEliminar = crearBoton("Eliminar", new Color(252, 235, 235), COLOR_CORAL, false);
        JButton btnNuevo = crearBoton("＋ Nuevo registro", new Color(238, 240, 244), Color.BLACK, false);
        JButton btnLimpiar = crearBoton("Limpiar formulario", COLOR_SUPERFICIE, Color.GRAY, false);

        barra.add(btnGuardar);
        barra.add(btnEditar);
        barra.add(btnEliminar);
        barra.add(btnNuevo);
        barra.add(btnLimpiar);

        btnNuevo.addActionListener(e -> nuevoLibro());
        btnGuardar.addActionListener(e -> guardarLibro());
        btnEditar.addActionListener(e -> editarLibro());
        btnEliminar.addActionListener(e -> eliminarLibro());
        btnLimpiar.addActionListener(e -> limpiarFormularioLibro());

        return barra;
    }

    private void nuevoLibro() {
        limpiarFormularioLibro();
        txtCodigo.setText(generarSiguienteCodigoLibro());
        txtTitulo.requestFocus();
    }

    private String generarSiguienteCodigoLibro() {
        return String.format("L%03d", siguienteCodigoLibro);
    }

    private void guardarLibro() {
        if (!validarCamposLibro()) return;

        String codigo = txtCodigo.getText().trim();
        if (existeCodigoLibro(codigo, null)) {
            JOptionPane.showMessageDialog(this,
                    "Ya existe un libro registrado con el código \"" + codigo + "\".\n"
                            + "Utilice el botón \"Nuevo registro\" para generar un código distinto.",
                    "Código duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Libro nuevo = new Libro(
                codigo,
                txtTitulo.getText().trim(),
                txtAutor.getText().trim(),
                txtEditorial.getText().trim(),
                (String) cmbCategoria.getSelectedItem(),
                (Integer) spnAnio.getValue(),
                (String) cmbEstadoLibro.getSelectedItem(),
                chkReferencia.isSelected(),
                txtObservaciones.getText().trim()
        );

        libros.add(nuevo);
        siguienteCodigoLibro++;

        refrescarTodo();
        JOptionPane.showMessageDialog(this, "El libro \"" + nuevo.titulo + "\" se guardó correctamente.",
                "Registro guardado", JOptionPane.INFORMATION_MESSAGE);
        limpiarFormularioLibro();
    }

    private void editarLibro() {
        if (libroSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un libro de la tabla antes de editarlo.",
                    "Ningún registro seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarCamposLibro()) return;

        String codigo = txtCodigo.getText().trim();
        if (existeCodigoLibro(codigo, libroSeleccionado)) {
            JOptionPane.showMessageDialog(this, "Ya existe otro libro registrado con el código \"" + codigo + "\".",
                    "Código duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        libroSeleccionado.codigo = codigo;
        libroSeleccionado.titulo = txtTitulo.getText().trim();
        libroSeleccionado.autor = txtAutor.getText().trim();
        libroSeleccionado.editorial = txtEditorial.getText().trim();
        libroSeleccionado.categoria = (String) cmbCategoria.getSelectedItem();
        libroSeleccionado.anio = (Integer) spnAnio.getValue();
        libroSeleccionado.estado = (String) cmbEstadoLibro.getSelectedItem();
        libroSeleccionado.referencia = chkReferencia.isSelected();
        libroSeleccionado.observaciones = txtObservaciones.getText().trim();

        refrescarTodo();
        JOptionPane.showMessageDialog(this, "El libro se actualizó correctamente.",
                "Registro actualizado", JOptionPane.INFORMATION_MESSAGE);
        limpiarFormularioLibro();
    }

    private void eliminarLibro() {
        if (libroSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un libro de la tabla antes de eliminarlo.",
                    "Ningún registro seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (Prestamo p : prestamos) {
            if (p.codigoLibro.equalsIgnoreCase(libroSeleccionado.codigo)
                    && ("Activo".equals(p.estado) || "Vencido".equals(p.estado))) {
                JOptionPane.showMessageDialog(this,
                        "No se puede eliminar: el libro tiene un préstamo activo o vencido asociado.",
                        "Operación no permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar el libro \"" + libroSeleccionado.titulo + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            libros.remove(libroSeleccionado);
            refrescarTodo();
            limpiarFormularioLibro();
            JOptionPane.showMessageDialog(this, "El libro fue eliminado del catálogo.",
                    "Registro eliminado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void limpiarFormularioLibro() {
        txtCodigo.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtEditorial.setText("");
        cmbCategoria.setSelectedIndex(0);
        cmbEstadoLibro.setSelectedIndex(0);
        spnAnio.setValue(Calendar.getInstance().get(Calendar.YEAR));
        chkReferencia.setSelected(false);
        txtObservaciones.setText("");
        libroSeleccionado = null;
        tablaLibros.clearSelection();
    }

    private void cargarLibroEnFormulario(Libro l) {
        libroSeleccionado = l;
        txtCodigo.setText(l.codigo);
        txtTitulo.setText(l.titulo);
        txtAutor.setText(l.autor);
        txtEditorial.setText(l.editorial);
        cmbCategoria.setSelectedItem(l.categoria);
        spnAnio.setValue(l.anio);
        cmbEstadoLibro.setSelectedItem(l.estado);
        chkReferencia.setSelected(l.referencia);
        txtObservaciones.setText(l.observaciones);
    }

    private boolean validarCamposLibro() {
        StringBuilder errores = new StringBuilder();
        if (txtCodigo.getText().trim().isEmpty()) errores.append("- El código es obligatorio.\n");
        if (txtTitulo.getText().trim().isEmpty()) errores.append("- El título es obligatorio.\n");
        if (txtAutor.getText().trim().isEmpty()) errores.append("- El autor es obligatorio.\n");
        if (txtEditorial.getText().trim().isEmpty()) errores.append("- La editorial es obligatoria.\n");

        if (errores.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Por favor corrija los siguientes errores antes de continuar:\n\n" + errores,
                    "Datos incompletos", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean existeCodigoLibro(String codigo, Libro ignorar) {
        for (Libro l : libros) {
            if (l == ignorar) continue;
            if (l.codigo.equalsIgnoreCase(codigo)) return true;
        }
        return false;
    }

    private Libro buscarLibroPorCodigo(String codigo) {
        for (Libro l : libros) {
            if (l.codigo.equalsIgnoreCase(codigo)) return l;
        }
        return null;
    }

    // =========================================================
    // MÓDULO 2: USUARIOS / LECTORES
    // =========================================================
    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(16, 12));
        panel.setOpaque(false);
        panel.add(crearBarraAccionesUsuario(), BorderLayout.NORTH);
        panel.add(crearFormularioUsuario(), BorderLayout.WEST);

        modeloUsuarios = crearModeloNoEditable(new String[]{"ID / Cédula", "Nombre", "Correo", "Teléfono", "Tipo", "Estado"});
        tablaUsuarios = crearTablaOscura(modeloUsuarios);
        sorterUsuarios = new TableRowSorter<>(modeloUsuarios);
        tablaUsuarios.setRowSorter(sorterUsuarios);
        aplicarRendererFilas(tablaUsuarios);
        aplicarRendererBadge(tablaUsuarios, 5);

        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() != -1) {
                int fila = tablaUsuarios.convertRowIndexToModel(tablaUsuarios.getSelectedRow());
                String id = (String) modeloUsuarios.getValueAt(fila, 0);
                Usuario u = buscarUsuarioPorId(id);
                if (u != null) cargarUsuarioEnFormulario(u);
            }
        });

        txtBuscarUsuarios = new JTextField();
        lblContadorUsuarios = new JLabel("0 de 0 registros");
        lblContadorUsuarios.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblContadorUsuarios.setForeground(Color.GRAY);

        txtBuscarUsuarios.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
            private void aplicarFiltro() {
                filtrarTabla(txtBuscarUsuarios, sorterUsuarios);
                actualizarContador(lblContadorUsuarios, tablaUsuarios, usuarios.size());
            }
        });

        panel.add(crearPanelTabla(tablaUsuarios, txtBuscarUsuarios, lblContadorUsuarios,
                "Buscar por ID, nombre, correo o tipo"), BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearFormularioUsuario() {

        PanelRedondeado panel = new PanelRedondeado(12);
        panel.setBackground(COLOR_SUPERFICIE);
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel titulo = new JLabel("＋ Agregar / editar usuario");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel();
        campos.setOpaque(false);
        campos.setLayout(new BoxLayout(campos, BoxLayout.Y_AXIS));

        txtIdUsuario = crearCampoTexto("ID / Cédula");
        txtNombreUsuario = crearCampoTexto("Nombre completo");
        txtCorreo = crearCampoTexto("Correo electrónico");
        txtTelefono = crearCampoTexto("Teléfono");

        cmbTipoUsuario = new JComboBox<>(TIPOS_USUARIO);
        cmbEstadoUsuario = new JComboBox<>(ESTADOS_USUARIO);
        estilizarCombo(cmbTipoUsuario);
        estilizarCombo(cmbEstadoUsuario);

        campos.add(etiquetaCampo("ID / Cédula"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(txtIdUsuario);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Nombre completo"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(txtNombreUsuario);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Correo electrónico"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(txtCorreo);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Teléfono"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(txtTelefono);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Tipo de usuario"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(cmbTipoUsuario);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Estado"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(cmbEstadoUsuario);

        panel.add(campos, BorderLayout.CENTER);

        return panel;
    }

    /** Barra horizontal de acciones del módulo de Usuarios: misma idea que en Libros,
     *  arriba del buscador y debajo de las tarjetas del dashboard. */
    private JPanel crearBarraAccionesUsuario() {
        PanelRedondeado barra = new PanelRedondeado(12);
        barra.setBackground(COLOR_SUPERFICIE);
        barra.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setBorder(new EmptyBorder(10, 14, 10, 14));

        JButton btnGuardar = crearBoton("Guardar usuario", COLOR_VERDE, COLOR_VERDE_TEXTO, true);
        JButton btnEditar = crearBoton("Editar", new Color(238, 240, 244), Color.BLACK, false);
        JButton btnEliminar = crearBoton("Eliminar", new Color(252, 235, 235), COLOR_CORAL, false);
        JButton btnNuevo = crearBoton("＋ Nuevo registro", new Color(238, 240, 244), Color.BLACK, false);
        JButton btnLimpiar = crearBoton("Limpiar formulario", COLOR_SUPERFICIE, Color.GRAY, false);

        barra.add(btnGuardar);
        barra.add(btnEditar);
        barra.add(btnEliminar);
        barra.add(btnNuevo);
        barra.add(btnLimpiar);

        btnNuevo.addActionListener(e -> nuevoUsuario());
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnEditar.addActionListener(e -> editarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarFormularioUsuario());

        return barra;
    }

    private void nuevoUsuario() {
        limpiarFormularioUsuario();
        txtIdUsuario.requestFocus();
    }

    private void guardarUsuario() {
        if (!validarCamposUsuario()) return;

        String id = txtIdUsuario.getText().trim();
        if (existeIdUsuario(id, null)) {
            JOptionPane.showMessageDialog(this, "Ya existe un usuario registrado con el ID \"" + id + "\".",
                    "ID duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario nuevo = new Usuario(
                id,
                txtNombreUsuario.getText().trim(),
                txtCorreo.getText().trim(),
                txtTelefono.getText().trim(),
                (String) cmbTipoUsuario.getSelectedItem(),
                (String) cmbEstadoUsuario.getSelectedItem()
        );

        usuarios.add(nuevo);
        refrescarTodo();
        JOptionPane.showMessageDialog(this, "El usuario \"" + nuevo.nombre + "\" se guardó correctamente.",
                "Registro guardado", JOptionPane.INFORMATION_MESSAGE);
        limpiarFormularioUsuario();
    }

    private void editarUsuario() {
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario de la tabla antes de editarlo.",
                    "Ningún registro seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarCamposUsuario()) return;

        String id = txtIdUsuario.getText().trim();
        if (existeIdUsuario(id, usuarioSeleccionado)) {
            JOptionPane.showMessageDialog(this, "Ya existe otro usuario registrado con el ID \"" + id + "\".",
                    "ID duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        usuarioSeleccionado.id = id;
        usuarioSeleccionado.nombre = txtNombreUsuario.getText().trim();
        usuarioSeleccionado.correo = txtCorreo.getText().trim();
        usuarioSeleccionado.telefono = txtTelefono.getText().trim();
        usuarioSeleccionado.tipo = (String) cmbTipoUsuario.getSelectedItem();
        usuarioSeleccionado.estado = (String) cmbEstadoUsuario.getSelectedItem();

        refrescarTodo();
        JOptionPane.showMessageDialog(this, "El usuario se actualizó correctamente.",
                "Registro actualizado", JOptionPane.INFORMATION_MESSAGE);
        limpiarFormularioUsuario();
    }

    private void eliminarUsuario() {
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario de la tabla antes de eliminarlo.",
                    "Ningún registro seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (Prestamo p : prestamos) {
            if (p.idUsuario.equalsIgnoreCase(usuarioSeleccionado.id)
                    && ("Activo".equals(p.estado) || "Vencido".equals(p.estado))) {
                JOptionPane.showMessageDialog(this,
                        "No se puede eliminar: el usuario tiene un préstamo activo o vencido asociado.",
                        "Operación no permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar al usuario \"" + usuarioSeleccionado.nombre + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            usuarios.remove(usuarioSeleccionado);
            refrescarTodo();
            limpiarFormularioUsuario();
            JOptionPane.showMessageDialog(this, "El usuario fue eliminado del padrón.",
                    "Registro eliminado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void limpiarFormularioUsuario() {
        txtIdUsuario.setText("");
        txtNombreUsuario.setText("");
        txtCorreo.setText("");
        txtTelefono.setText("");
        cmbTipoUsuario.setSelectedIndex(0);
        cmbEstadoUsuario.setSelectedIndex(0);
        usuarioSeleccionado = null;
        tablaUsuarios.clearSelection();
    }

    private void cargarUsuarioEnFormulario(Usuario u) {
        usuarioSeleccionado = u;
        txtIdUsuario.setText(u.id);
        txtNombreUsuario.setText(u.nombre);
        txtCorreo.setText(u.correo);
        txtTelefono.setText(u.telefono);
        cmbTipoUsuario.setSelectedItem(u.tipo);
        cmbEstadoUsuario.setSelectedItem(u.estado);
    }

    private boolean validarCamposUsuario() {
        try {
            validarSoloNumeros(txtIdUsuario.getText().trim(), "ID / Cédula");
            validarSoloTexto(txtNombreUsuario.getText().trim(), "Nombre completo");
            validarCorreo(txtCorreo.getText().trim());
            validarSoloNumeros(txtTelefono.getText().trim(), "Teléfono");
            return true;
        } catch (CampoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /** Lanza una excepción si el campo está vacío o contiene números/símbolos (uso: nombres). */
    private void validarSoloTexto(String valor, String nombreCampo) throws CampoInvalidoException {
        if (valor.isEmpty()) {
            throw new CampoInvalidoException("El campo \"" + nombreCampo + "\" es obligatorio.");
        }
        if (!valor.matches("[\\p{L} .'-]+")) {
            throw new CampoInvalidoException("El campo \"" + nombreCampo + "\" no puede contener números ni símbolos.");
        }
    }

    /** Lanza una excepción si el campo está vacío o contiene letras (uso: teléfono, cédula). */
    private void validarSoloNumeros(String valor, String nombreCampo) throws CampoInvalidoException {
        if (valor.isEmpty()) {
            throw new CampoInvalidoException("El campo \"" + nombreCampo + "\" es obligatorio.");
        }
        if (!valor.matches("[0-9-]+")) {
            throw new CampoInvalidoException("El campo \"" + nombreCampo + "\" solo puede contener números.");
        }
    }

    /** Lanza una excepción si el correo está vacío o no cumple un formato básico válido. */
    private void validarCorreo(String valor) throws CampoInvalidoException {
        if (valor.isEmpty()) {
            throw new CampoInvalidoException("El correo electrónico es obligatorio.");
        }
        if (!valor.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new CampoInvalidoException("El correo electrónico no tiene un formato válido.");
        }
    }

    private boolean existeIdUsuario(String id, Usuario ignorar) {
        for (Usuario u : usuarios) {
            if (u == ignorar) continue;
            if (u.id.equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    private Usuario buscarUsuarioPorId(String id) {
        for (Usuario u : usuarios) {
            if (u.id.equalsIgnoreCase(id)) return u;
        }
        return null;
    }

    // =========================================================
    // MÓDULO 3: PRÉSTAMOS Y DEVOLUCIONES
    // =========================================================
    private JPanel crearPanelPrestamos() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);
        panel.add(crearFormularioPrestamo(), BorderLayout.WEST);

        modeloPrestamos = crearModeloNoEditable(new String[]{
                "ID", "Código libro", "Título", "ID usuario", "Nombre usuario",
                "Fecha préstamo", "Fecha límite", "Estado"
        });
        tablaPrestamos = crearTablaOscura(modeloPrestamos);
        sorterPrestamos = new TableRowSorter<>(modeloPrestamos);
        tablaPrestamos.setRowSorter(sorterPrestamos);
        aplicarRendererFilas(tablaPrestamos);
        aplicarRendererBadge(tablaPrestamos, 7);

        tablaPrestamos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaPrestamos.getSelectedRow() != -1) {
                int fila = tablaPrestamos.convertRowIndexToModel(tablaPrestamos.getSelectedRow());
                String id = (String) modeloPrestamos.getValueAt(fila, 0);
                Prestamo p = buscarPrestamoPorId(id);
                if (p != null) cargarPrestamoEnFormulario(p);
            }
        });

        txtBuscarPrestamos = new JTextField();
        lblContadorPrestamos = new JLabel("0 de 0 registros");
        lblContadorPrestamos.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblContadorPrestamos.setForeground(Color.GRAY);

        txtBuscarPrestamos.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
            private void aplicarFiltro() {
                filtrarTabla(txtBuscarPrestamos, sorterPrestamos);
                actualizarContador(lblContadorPrestamos, tablaPrestamos, prestamos.size());
            }
        });

        panel.add(crearPanelTabla(tablaPrestamos, txtBuscarPrestamos, lblContadorPrestamos,
                "Buscar por ID, libro o usuario"), BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearFormularioPrestamo() {

        PanelRedondeado panel = new PanelRedondeado(12);
        panel.setBackground(COLOR_SUPERFICIE);
        panel.setPreferredSize(new Dimension(270, 0));
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel titulo = new JLabel("↔ Registrar préstamo");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel();
        campos.setOpaque(false);
        campos.setLayout(new BoxLayout(campos, BoxLayout.Y_AXIS));

        cmbLibroPrestamo = new JComboBox<>();
        estilizarComboGenerico(cmbLibroPrestamo);

        cmbUsuarioPrestamo = new JComboBox<>();
        estilizarComboGenerico(cmbUsuarioPrestamo);

        spnFechaPrestamo = new JSpinner(new SpinnerDateModel());
        spnFechaPrestamo.setEditor(new JSpinner.DateEditor(spnFechaPrestamo, "dd/MM/yyyy"));
        spnFechaPrestamo.setValue(new Date());
        alinearIzquierdaYAngosto(spnFechaPrestamo);

        Calendar calInicial = Calendar.getInstance();
        calInicial.add(Calendar.DAY_OF_MONTH, 7);
        spnFechaLimite = new JSpinner(new SpinnerDateModel());
        spnFechaLimite.setEditor(new JSpinner.DateEditor(spnFechaLimite, "dd/MM/yyyy"));
        spnFechaLimite.setValue(calInicial.getTime());
        alinearIzquierdaYAngosto(spnFechaLimite);

        lblEstadoPrestamoSeleccionado = new JLabel("Sin préstamo seleccionado");
        lblEstadoPrestamoSeleccionado.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblEstadoPrestamoSeleccionado.setForeground(Color.GRAY);
        lblEstadoPrestamoSeleccionado.setAlignmentX(Component.LEFT_ALIGNMENT);

        campos.add(etiquetaCampo("Libro disponible"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(cmbLibroPrestamo);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Usuario activo"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(cmbUsuarioPrestamo);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Fecha de préstamo"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(spnFechaPrestamo);
        campos.add(Box.createVerticalStrut(8));
        campos.add(etiquetaCampo("Fecha de devolución estimada"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(spnFechaLimite);
        campos.add(Box.createVerticalStrut(10));
        campos.add(lblEstadoPrestamoSeleccionado);

        panel.add(campos, BorderLayout.CENTER);

        JPanel botones = new JPanel();
        botones.setOpaque(false);
        botones.setLayout(new BoxLayout(botones, BoxLayout.Y_AXIS));

        JButton btnRegistrar = crearBoton("Registrar préstamo", COLOR_VERDE, COLOR_VERDE_TEXTO, true);
        JButton btnDevolucion = crearBoton("Registrar devolución", new Color(238, 240, 244), Color.BLACK, false);
        JButton btnEliminar = crearBoton("Eliminar registro", new Color(252, 235, 235), COLOR_CORAL, false);
        JButton btnLimpiar = crearBoton("Limpiar formulario", COLOR_SUPERFICIE, Color.GRAY, false);

        botones.add(btnRegistrar);
        botones.add(Box.createVerticalStrut(6));
        botones.add(btnDevolucion);
        botones.add(Box.createVerticalStrut(6));
        botones.add(btnEliminar);
        botones.add(Box.createVerticalStrut(4));
        botones.add(btnLimpiar);

        panel.add(botones, BorderLayout.SOUTH);

        btnRegistrar.addActionListener(e -> registrarPrestamo());
        btnDevolucion.addActionListener(e -> registrarDevolucion());
        btnEliminar.addActionListener(e -> eliminarPrestamo());
        btnLimpiar.addActionListener(e -> limpiarFormularioPrestamo());

        return panel;
    }

    private void actualizarCombosPrestamo() {
        Libro libroPrevio = (Libro) cmbLibroPrestamo.getSelectedItem();
        Usuario usuarioPrevio = (Usuario) cmbUsuarioPrestamo.getSelectedItem();

        cmbLibroPrestamo.removeAllItems();
        for (Libro l : libros) {
            if (!l.referencia && "Disponible".equals(l.estado)) {
                cmbLibroPrestamo.addItem(l);
            }
        }

        cmbUsuarioPrestamo.removeAllItems();
        for (Usuario u : usuarios) {
            if ("Activo".equals(u.estado)) {
                cmbUsuarioPrestamo.addItem(u);
            }
        }

        if (libroPrevio != null) cmbLibroPrestamo.setSelectedItem(libroPrevio);
        if (usuarioPrevio != null) cmbUsuarioPrestamo.setSelectedItem(usuarioPrevio);
    }

    private void registrarPrestamo() {
        Libro libro = (Libro) cmbLibroPrestamo.getSelectedItem();
        Usuario usuario = (Usuario) cmbUsuarioPrestamo.getSelectedItem();

        if (libro == null || usuario == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un libro disponible y un usuario activo para registrar el préstamo.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate fPrestamo = dateToLocalDate((Date) spnFechaPrestamo.getValue());
        LocalDate fLimite = dateToLocalDate((Date) spnFechaLimite.getValue());

        if (fLimite.isBefore(fPrestamo)) {
            JOptionPane.showMessageDialog(this,
                    "La fecha de devolución estimada no puede ser anterior a la fecha de préstamo.",
                    "Fechas inválidas", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Prestamo prestamo = new Prestamo(generarSiguienteIdPrestamo(), libro.codigo, libro.titulo,
                usuario.id, usuario.nombre, fPrestamo, fLimite, null, "Activo");
        prestamos.add(prestamo);
        siguienteIdPrestamo++;

        libro.estado = "Prestado";

        refrescarTodo();
        JOptionPane.showMessageDialog(this, "Préstamo registrado para \"" + libro.titulo + "\".",
                "Préstamo registrado", JOptionPane.INFORMATION_MESSAGE);
        limpiarFormularioPrestamo();
    }

    private void registrarDevolucion() {
        if (prestamoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione en la tabla un préstamo activo o vencido para registrar la devolución.",
                    "Ningún préstamo seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("Devuelto".equals(prestamoSeleccionado.estado)) {
            JOptionPane.showMessageDialog(this, "Este préstamo ya fue devuelto anteriormente.",
                    "Operación no válida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        prestamoSeleccionado.estado = "Devuelto";
        prestamoSeleccionado.fechaDevolucion = LocalDate.now();

        Libro libro = buscarLibroPorCodigo(prestamoSeleccionado.codigoLibro);
        if (libro != null) libro.estado = "Disponible";

        refrescarTodo();
        JOptionPane.showMessageDialog(this, "Devolución registrada correctamente.",
                "Devolución registrada", JOptionPane.INFORMATION_MESSAGE);
        limpiarFormularioPrestamo();
    }

    private void eliminarPrestamo() {
        if (prestamoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un préstamo de la tabla antes de eliminarlo.",
                    "Ningún registro seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar este registro de préstamo?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            prestamos.remove(prestamoSeleccionado);
            refrescarTodo();
            limpiarFormularioPrestamo();
            JOptionPane.showMessageDialog(this, "El registro de préstamo fue eliminado.",
                    "Registro eliminado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void limpiarFormularioPrestamo() {
        spnFechaPrestamo.setValue(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        spnFechaLimite.setValue(cal.getTime());
        cmbLibroPrestamo.setSelectedIndex(cmbLibroPrestamo.getItemCount() > 0 ? 0 : -1);
        cmbUsuarioPrestamo.setSelectedIndex(cmbUsuarioPrestamo.getItemCount() > 0 ? 0 : -1);
        lblEstadoPrestamoSeleccionado.setText("Sin préstamo seleccionado");
        prestamoSeleccionado = null;
        tablaPrestamos.clearSelection();
    }

    private void cargarPrestamoEnFormulario(Prestamo p) {
        prestamoSeleccionado = p;
        lblEstadoPrestamoSeleccionado.setText("Seleccionado: " + p.id + " · Estado: " + p.estado);
    }

    private Prestamo buscarPrestamoPorId(String id) {
        for (Prestamo p : prestamos) {
            if (p.id.equalsIgnoreCase(id)) return p;
        }
        return null;
    }

    private String generarSiguienteIdPrestamo() {
        return String.format("P%03d", siguienteIdPrestamo);
    }

    private LocalDate dateToLocalDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // =========================================================
    // TABLAS: HELPERS DE ESTILO OSCURO
    // =========================================================
    private DefaultTableModel crearModeloNoEditable(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable crearTablaOscura(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setBackground(COLOR_SUPERFICIE);
        tabla.setForeground(COLOR_TEXTO);
        tabla.setSelectionBackground(COLOR_VERDE);
        tabla.setSelectionForeground(COLOR_VERDE_TEXTO);
        tabla.setGridColor(new Color(50, 50, 54));
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setRowHeight(34);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.setFillsViewportHeight(true);
        tabla.setRowSelectionAllowed(true);
        tabla.setColumnSelectionAllowed(false);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = tabla.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new EncabezadoRenderer());

        return tabla;
    }

    private void aplicarRendererFilas(JTable tabla) {
        FilaRenderer renderer = new FilaRenderer();
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void aplicarRendererBadge(JTable tabla, int columnaEstado) {
        tabla.getColumnModel().getColumn(columnaEstado).setCellRenderer(new BadgeRenderer());
    }

    private JPanel crearPanelTabla(JTable tabla, JTextField campoBusqueda, JLabel lblContador, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel filaBuscador = new JPanel(new BorderLayout(8, 0));
        filaBuscador.setOpaque(false);

        // Buscador con esquinas suavemente cuadradas (ya no ovalado/píldora)
        PanelRedondeado buscador = new PanelRedondeado(10);
        buscador.setBackground(COLOR_SUPERFICIE);
        buscador.setLayout(new BorderLayout(6, 0));
        buscador.setBorder(new EmptyBorder(7, 14, 7, 14));

        JLabel lupa = new JLabel("🔍");
        lupa.setFont(new Font("SansSerif", Font.PLAIN, 12));
        buscador.add(lupa, BorderLayout.WEST);

        campoBusqueda.setBorder(null);
        campoBusqueda.setFont(new Font("SansSerif", Font.PLAIN, 12));
        campoBusqueda.setToolTipText(placeholder);
        buscador.add(campoBusqueda, BorderLayout.CENTER);

        filaBuscador.add(buscador, BorderLayout.CENTER);
        filaBuscador.add(lblContador, BorderLayout.EAST);
        panel.add(filaBuscador, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(new LineBorder(COLOR_BORDE, 1, true));
        scroll.getViewport().setBackground(COLOR_SUPERFICIE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void filtrarTabla(JTextField campoBusqueda, TableRowSorter<DefaultTableModel> sorter) {
        String texto = campoBusqueda.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
        }
    }

    private void actualizarContador(JLabel lbl, JTable tabla, int total) {
        lbl.setText(tabla.getRowCount() + " de " + total + " registros");
    }

    // =========================================================
    // REFRESCO GLOBAL Y MÉTRICAS
    // =========================================================
    private void refrescarTodo() {
        verificarVencimientos();
        refrescarTablaLibros();
        refrescarTablaUsuarios();
        refrescarTablaPrestamos();
        actualizarCombosPrestamo();
        actualizarTarjetasDashboard();
    }

    private void verificarVencimientos() {
        LocalDate hoy = LocalDate.now();
        for (Prestamo p : prestamos) {
            if ("Activo".equals(p.estado) && p.fechaLimite.isBefore(hoy)) {
                p.estado = "Vencido";
                Libro libro = buscarLibroPorCodigo(p.codigoLibro);
                if (libro != null && "Prestado".equals(libro.estado)) {
                    libro.estado = "Vencido";
                }
            }
        }
    }

    private void refrescarTablaLibros() {
        modeloLibros.setRowCount(0);
        for (Libro l : libros) {
            modeloLibros.addRow(new Object[]{l.codigo, l.titulo, l.autor, l.editorial, l.categoria, l.anio, l.estado});
        }
        actualizarContador(lblContadorLibros, tablaLibros, libros.size());
    }

    private void refrescarTablaUsuarios() {
        modeloUsuarios.setRowCount(0);
        for (Usuario u : usuarios) {
            modeloUsuarios.addRow(new Object[]{u.id, u.nombre, u.correo, u.telefono, u.tipo, u.estado});
        }
        actualizarContador(lblContadorUsuarios, tablaUsuarios, usuarios.size());
    }

    private void refrescarTablaPrestamos() {
        modeloPrestamos.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Prestamo p : prestamos) {
            modeloPrestamos.addRow(new Object[]{
                    p.id, p.codigoLibro, p.tituloLibro, p.idUsuario, p.nombreUsuario,
                    p.fechaPrestamo.format(fmt), p.fechaLimite.format(fmt), p.estado
            });
        }
        actualizarContador(lblContadorPrestamos, tablaPrestamos, prestamos.size());
    }

    private void actualizarTarjetasDashboard() {
        int total = libros.size();
        int disponibles = 0;
        for (Libro l : libros) {
            if ("Disponible".equals(l.estado)) disponibles++;
        }

        int activos = 0, vencidos = 0;
        for (Prestamo p : prestamos) {
            if ("Activo".equals(p.estado)) activos++;
            if ("Vencido".equals(p.estado)) vencidos++;
        }

        lblTotalLibros.setText(String.valueOf(total));
        lblDisponibles.setText(String.valueOf(disponibles));
        lblPrestamosActivos.setText(String.valueOf(activos));
        lblPrestamosVencidos.setText(String.valueOf(vencidos));
    }

    // =========================================================
    // DATOS INICIALES (MOCK DATA)
    // =========================================================
    private void cargarDatosIniciales() {

        usuarios.add(new Usuario("302140556", "María Fernández", "maria.fernandez@mail.com", "8888-1234", "Estudiante", "Activo"));
        usuarios.add(new Usuario("104780321", "Carlos Jiménez", "carlos.jimenez@mail.com", "8777-5566", "Docente", "Activo"));
        usuarios.add(new Usuario("205990112", "Ana Rodríguez", "ana.rodriguez@mail.com", "8666-7788", "General", "Activo"));
        usuarios.add(new Usuario("118820456", "Luis Vargas", "luis.vargas@mail.com", "8555-9900", "Estudiante", "Suspendido"));
        usuarios.add(new Usuario("209650778", "Sofía Castro", "sofia.castro@mail.com", "8444-2211", "Docente", "Inactivo"));

        libros.add(new Libro("L001", "Cien Años de Soledad", "Gabriel García Márquez",
                "Sudamericana", "Ficción", 1967, "Disponible", false, ""));
        libros.add(new Libro("L002", "Introducción a los Algoritmos", "Cormen, Leiserson, Rivest",
                "MIT Press", "Tecnología", 2009, "Disponible", false, ""));
        libros.add(new Libro("L003", "Breve Historia del Tiempo", "Stephen Hawking",
                "Crítica", "Ciencia", 1988, "Disponible", false, ""));
        libros.add(new Libro("L004", "Diccionario de la Real Academia", "RAE",
                "Espasa", "Académico", 2014, "Disponible", true, "Uso exclusivo en sala."));
        libros.add(new Libro("L005", "El Principito", "Antoine de Saint-Exupéry",
                "Reynal & Hitchcock", "Infantil", 1943, "Disponible", false, ""));
        libros.add(new Libro("L006", "Sapiens: De Animales a Dioses", "Yuval Noah Harari",
                "Debate", "Historia", 2011, "Disponible", false, ""));
        libros.add(new Libro("L007", "Clean Code", "Robert C. Martin",
                "Prentice Hall", "Tecnología", 2008, "Disponible", false, ""));
        libros.add(new Libro("L008", "Cien Enigmas de Costa Rica", "Varios Autores",
                "EUNED", "Historia", 2015, "Reservado", false, ""));

        siguienteCodigoLibro = 9;

        LocalDate hoy = LocalDate.now();

        // Préstamo activo, todavía dentro del plazo
        crearPrestamoInicial("L002", "104780321", hoy.minusDays(5), hoy.plusDays(9), "Activo", null);

        // Préstamo cuya fecha límite ya pasó: verificarVencimientos() lo marcará como Vencido
        crearPrestamoInicial("L005", "302140556", hoy.minusDays(20), hoy.minusDays(6), "Activo", null);

        // Préstamo ya devuelto
        crearPrestamoInicial("L006", "205990112", hoy.minusDays(30), hoy.minusDays(25), "Devuelto", hoy.minusDays(24));

        refrescarTodo();
    }

    private void crearPrestamoInicial(String codigoLibro, String idUsuario, LocalDate fPrestamo,
                                      LocalDate fLimite, String estado, LocalDate fDevolucion) {
        Libro libro = buscarLibroPorCodigo(codigoLibro);
        Usuario usuario = buscarUsuarioPorId(idUsuario);
        if (libro == null || usuario == null) return;

        Prestamo p = new Prestamo(generarSiguienteIdPrestamo(), libro.codigo, libro.titulo,
                usuario.id, usuario.nombre, fPrestamo, fLimite, fDevolucion, estado);
        prestamos.add(p);
        siguienteIdPrestamo++;

        libro.estado = "Devuelto".equals(estado) ? "Disponible" : "Prestado";
    }

    // =========================================================
    // HELPERS DE FORMULARIO (COMUNES A LOS 3 MÓDULOS)
    // =========================================================
    private JTextField crearCampoTexto(String placeholder) {
        JTextField campo = new JTextField();
        campo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        campo.setToolTipText(placeholder);
        campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDE, 1, true), new EmptyBorder(5, 8, 5, 8)));
        alinearIzquierdaYAngosto(campo);
        return campo;
    }

    private void estilizarCombo(JComboBox<String> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        alinearIzquierdaYAngosto(combo);
    }

    private void estilizarComboGenerico(JComboBox<?> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        alinearIzquierdaYAngosto(combo);
    }

    private void alinearIzquierdaYAngosto(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    private JLabel etiquetaCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(Color.GRAY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton crearBoton(String texto, Color fondo, Color letra, boolean grande) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", grande ? Font.BOLD : Font.PLAIN, grande ? 12 : 11));
        boton.setBackground(fondo);
        boton.setForeground(letra);
        boton.setFocusPainted(false);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, grande ? 32 : 26));
        boton.setBorder(new CompoundBorder(
                new LineBorder(fondo.darker(), 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    // =========================================================
    // CLASES INTERNAS DE APOYO VISUAL
    // =========================================================
    private class PanelRedondeado extends JPanel {
        private final int radio;

        PanelRedondeado(int radio) {
            this.radio = radio;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radio, radio);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Colorea filas alternadas manteniendo el tema oscuro y resalta la selección en verde. */
    private class FilaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
            if (isSelected) {
                lbl.setBackground(COLOR_VERDE);
                lbl.setForeground(COLOR_VERDE_TEXTO);
            } else {
                lbl.setBackground(row % 2 == 0 ? COLOR_SUPERFICIE : COLOR_SUPERFICIE_ALT);
                lbl.setForeground(COLOR_TEXTO);
            }
            return lbl;
        }
    }

    /** Dibuja el valor de la columna "Estado" como una insignia de color, igual que en las tarjetas. */
    private class BadgeRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JPanel contenedor = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            contenedor.setOpaque(true);
            contenedor.setBackground(isSelected ? COLOR_VERDE : (row % 2 == 0 ? COLOR_SUPERFICIE : COLOR_SUPERFICIE_ALT));
            contenedor.setBorder(new EmptyBorder(0, 10, 0, 10));

            String estado = value == null ? "" : value.toString();
            Color[] colores = coloresEstado(estado);

            JLabel etiqueta = new JLabel(estado);
            etiqueta.setOpaque(true);
            etiqueta.setBackground(colores[0]);
            etiqueta.setForeground(colores[1]);
            etiqueta.setFont(new Font("SansSerif", Font.BOLD, 10));
            etiqueta.setBorder(new EmptyBorder(3, 9, 3, 9));

            contenedor.add(etiqueta);
            return contenedor;
        }
    }

    /** Encabezado de tabla con fondo oscuro y borde inferior, coherente con la barra superior. */
    private class EncabezadoRenderer extends DefaultTableCellRenderer {
        EncabezadoRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBackground(COLOR_FONDO);
            setForeground(COLOR_TEXTO_SECUNDARIO);
            setFont(new Font("SansSerif", Font.BOLD, 11));
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDE), new EmptyBorder(0, 10, 0, 10)));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a", locale);
        lblFechaHora.setText(LocalDateTime.now().format(formatter));
    }

}