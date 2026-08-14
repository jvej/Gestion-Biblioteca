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
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
public class GestionBiblioteca extends JFrame {

// GESTIÓN DE BIBLIOTECA - Dashboard con Java Swing (VERSIÓN 2 - REDISEÑO)


    // COLORES
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
                return new Color[] {new Color(127, 119, 221), new Color(238, 237, 254), new Color(38, 33,92)};
        }
    }


    //Listas de valores fijos
    private final String[] CATEGORIAS = {
            "Ficción", "No ficción", "Ciencia", "Tecnología",
            "Historia", "Infantil", "Académico", "Otro"
    };

    private final String[] ESTADOS = {
            "Disponible", "Prestado", "Reservado", "Vencido"
    };

    private static class Libro {
        String codigo, titulo, autor, editorial, categoria, estado, observaciones;
        int anio;
        boolean referencia;

        Libro(String codigo, String titulo, String autor, String editorial, String categoria, int anio, String estado, boolean referencia, String observaciones) {
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
    }

    private final List<Libro> libros = new ArrayList<>(); //Aqui se almacenan los libros en una lista generica
    private List<Libro> librosFiltrados = new ArrayList<>(); //Aqui se almacenan los libros que coinciden con la busqueda

    private Libro libroSeleccionado = null; //Se guarda el libro que el usuacio selecciono actualmente, tiene null porque al inicio del programa no tiene seleccionado ninguno
    private PanelRedondeado tarjetaSeleccionadaUI = null;  //Esta guarda la tarjeta visual que representa al libro seleccionado

    private int siguienteCodigo = 1; //Esto es para generar automáticamente el código del siguiente libro
    private int paginaActual = 0;
    private final int LIBROS_POR_PAGINA = 6;


    private JTextField txtCodigo;
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JTextField txtEditorial;
    private JComboBox<String> cmbCategoria;
    private JComboBox<String> cmbEstado;
    private JSpinner spnAnio;
    private JCheckBox chkReferencia;
    private JTextArea txtObservaciones;
    private JTextField txtBuscar;

    private JPanel panelGrillaLibros;
    private JLabel lblContadorLibros;
    private JPanel panelPaginacion;
    private JLabel lblFechaHora;

    // Tarjetas del dashboard
    private JLabel lblTotalLibros;
    private JLabel lblDisponibles;
    private JLabel lblPrestados;
    private JLabel lblVencidos;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public GestionBiblioteca() {

        setTitle("Gestión de Biblioteca - Dashboard Java Swing");
        setSize(1500, 930);
        setMinimumSize(new Dimension(1250, 750));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout());

        configurarLookAndFeel();

        // ===== CAMBIO 1: antes iba add(crearMenuLateral(), BorderLayout.WEST)
        //       ahora la navegación se ubica arriba, no al costado =====
        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearContenidoPrincipal(), BorderLayout.CENTER);

        cargarDatosIniciales();
        actualizarTarjetasDashboard();
        actualizarVistaCatalogo();
        iniciarReloj();
    }

    // LOOK AND FEEL

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
    private JPanel crearBarraSuperior() {

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(COLOR_SUPERFICIE);
        barra.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDE), new EmptyBorder(10, 22, 10, 22)));

        //-----------LOGOTIPO A LA IZQUIERDA---------
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logo.setOpaque(false);
        JLabel lblIcono  = new JLabel("\uD83D\uDCD6");
        lblIcono.setFont(new Font("SansSerif", Font.PLAIN, 22));
        JLabel lblLogo = new JLabel("Bibliotec+");
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 16));
        logo.add(lblIcono);
        logo.add(lblLogo);
        barra.add(logo, BorderLayout.WEST);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nav.setOpaque(false);
        nav.add(crearPildoraMenu("Inicio", false));
        nav.add(crearPildoraMenu("Catálogo", true));
        nav.add(crearPildoraMenu("Préstamos", false));
        nav.add(crearPildoraMenu("Reportes", false));
        nav.add(crearPildoraMenu("Configuración", false));
        barra.add(nav, BorderLayout.CENTER);

        // --- reloj + avatar a la derecha ---
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


    private PanelRedondeado crearPildoraMenu(String texto, boolean activo) {
        PanelRedondeado pildora = new PanelRedondeado(999);
        pildora.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pildora.setBorder(new EmptyBorder(7, 14, 7, 14));
        pildora.setBackground(activo? COLOR_VERDE : COLOR_FONDO);
        pildora.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", activo ? Font.BOLD : Font.PLAIN, 12));
        lbl.setForeground(activo ? COLOR_VERDE_TEXTO : new Color(90, 98, 110));
        pildora.add(lbl);
        pildora.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!activo) pildora.setBackground(new Color(232, 235, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!activo) pildora.setBackground(COLOR_FONDO);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!activo) {
                    JOptionPane.showMessageDialog(GestionBiblioteca.this, "Esta sección todavía no está implementada en esta actividad.\n"  + "El módulo funcional es \"Catálogo\".", "Aviso", JOptionPane.INFORMATION_MESSAGE);}
            }
        });

        return pildora;
    }

    private JPanel crearContenidoPrincipal() {
        JPanel principal = new PanelRedondeado(new BorderLayout());
        principal.setBackground(COLOR_FONDO);
        principal.setBorder(new EmptyBorder(14, 22, 18, 22));

        principal.add(crearPanelTarjetas(), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(16, 0));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(new EmptyBorder(14, 0, 0, 0));

        cuerpo.add(crearFormularioLibro(), BorderLayout.WEST);   // panel angosto y fijo
        cuerpo.add(crearPanelCatalogo(), BorderLayout.CENTER);   // grilla de tarjetas

        principal.add(cuerpo, BorderLayout.CENTER);

        return principal;
    }

    private JPanel crearPanelTarjetas() {

        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(1000, 90));

        lblTotalLibros = new JLabel("0");
        lblDisponibles = new JLabel("0");
        lblPrestados = new JLabel("0");
        lblVencidos = new JLabel("0");

        panel.add(crearTarjeta("📚", lblTotalLibros, "Libros en catálogo", COLOR_VERDE));
        panel.add(crearTarjeta("✔", lblDisponibles, "Disponibles ahora", new Color(99, 153, 34)));
        panel.add(crearTarjeta("⇄", lblPrestados, "Prestados", COLOR_AMBAR));
        panel.add(crearTarjeta("⏱", lblVencidos, "Vencidos", COLOR_CORAL));

        return panel;
    }


    private JPanel crearTarjeta(String icono, JLabel lblNumero, String descripcion, Color colorIcono) {

        PanelRedondeado tarjeta = new PanelRedondeado(12);
        tarjeta.setBackground(COLOR_SUPERFICIE);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));   // ===== CAMBIO 2: vertical, antes era horizontal =====
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

    private JPanel crearFormularioLibro() {

        PanelRedondeado panel = new PanelRedondeado(12);
        panel.setBackground(COLOR_SUPERFICIE);
        panel.setPreferredSize(new Dimension(250, 0));   // ancho fijo, a diferencia de la v1
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
        cmbEstado = new JComboBox<>(ESTADOS);
        estilizarCombo(cmbCategoria);
        estilizarCombo(cmbEstado);

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

        JLabel lblObs = new JLabel("Observaciones");
        lblObs.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblObs.setForeground(Color.GRAY);
        lblObs.setAlignmentX(Component.LEFT_ALIGNMENT);

        campos.add(txtCodigo);
        campos.add(Box.createVerticalStrut(6));
        campos.add(txtTitulo);
        campos.add(Box.createVerticalStrut(6));
        campos.add(txtAutor);
        campos.add(Box.createVerticalStrut(6));
        campos.add(txtEditorial);
        campos.add(Box.createVerticalStrut(6));
        campos.add(cmbCategoria);
        campos.add(Box.createVerticalStrut(6));
        campos.add(spnAnio);
        campos.add(Box.createVerticalStrut(6));
        campos.add(cmbEstado);
        campos.add(Box.createVerticalStrut(6));
        campos.add(chkReferencia);
        campos.add(Box.createVerticalStrut(6));
        campos.add(lblObs);
        campos.add(Box.createVerticalStrut(3));
        campos.add(scrollObs);

        panel.add(campos, BorderLayout.CENTER);

        // -------------------------
        // Botones de acción CRUD (ahora apilados, no en fila)
        // -------------------------
        JPanel botones = new JPanel();
        botones.setOpaque(false);
        botones.setLayout(new BoxLayout(botones, BoxLayout.Y_AXIS));

        JButton btnGuardar = crearBoton("Guardar libro", COLOR_VERDE, COLOR_VERDE_TEXTO, true);

        JPanel filaEditarEliminar = new JPanel(new GridLayout(1, 2, 6, 0));
        filaEditarEliminar.setOpaque(false);
        filaEditarEliminar.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaEditarEliminar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JButton btnEditar = crearBoton("Editar", new Color(238, 240, 244), Color.BLACK, false);
        JButton btnEliminar = crearBoton("Eliminar", new Color(252, 235, 235), COLOR_CORAL, false);
        filaEditarEliminar.add(btnEditar);
        filaEditarEliminar.add(btnEliminar);

        JButton btnNuevo = crearBoton("＋ Nuevo registro", new Color(238, 240, 244), Color.BLACK, false);
        JButton btnLimpiar = crearBoton("Limpiar formulario", COLOR_SUPERFICIE, Color.GRAY, false);

        botones.add(btnGuardar);
        botones.add(Box.createVerticalStrut(6));
        botones.add(filaEditarEliminar);
        botones.add(Box.createVerticalStrut(6));
        botones.add(btnNuevo);
        botones.add(Box.createVerticalStrut(4));
        botones.add(btnLimpiar);

        panel.add(botones, BorderLayout.SOUTH);

        // Eventos (misma lógica que la v1, solo cambian los botones que la disparan)
        btnNuevo.addActionListener(e -> nuevoLibro());
        btnGuardar.addActionListener(e -> guardarLibro());
        btnEditar.addActionListener(e -> editarLibro());
        btnEliminar.addActionListener(e -> eliminarLibro());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        return panel;
    }

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

    private void alinearIzquierdaYAngosto(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    private JPanel crearPanelCatalogo() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // --- buscador tipo píldora ---
        JPanel filaBuscador = new JPanel(new BorderLayout(8, 0));
        filaBuscador.setOpaque(false);

        PanelRedondeado buscador = new PanelRedondeado(999);
        buscador.setBackground(COLOR_SUPERFICIE);
        buscador.setLayout(new BorderLayout(6, 0));
        buscador.setBorder(new EmptyBorder(7, 14, 7, 14));

        JLabel lupa = new JLabel("🔍");
        lupa.setFont(new Font("SansSerif", Font.PLAIN, 12));
        buscador.add(lupa, BorderLayout.WEST);

        txtBuscar = new JTextField();
        txtBuscar.setBorder(null);
        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtBuscar.setToolTipText("Buscar por título, autor, código o categoría");
        buscador.add(txtBuscar, BorderLayout.CENTER);

        filaBuscador.add(buscador, BorderLayout.CENTER);

        lblContadorLibros = new JLabel("0 de 0 libros");
        lblContadorLibros.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblContadorLibros.setForeground(Color.GRAY);
        filaBuscador.add(lblContadorLibros, BorderLayout.EAST);

        panel.add(filaBuscador, BorderLayout.NORTH);

        // --- grilla de tarjetas de libros (reemplaza a la JTable) ---
        panelGrillaLibros = new JPanel(new GridLayout(0, 2, 10, 10));
        panelGrillaLibros.setOpaque(false);

        JPanel contenedorGrilla = new JPanel(new BorderLayout());
        contenedorGrilla.setOpaque(false);
        contenedorGrilla.add(panelGrillaLibros, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(contenedorGrilla);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        // --- paginación numerada ---
        panelPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        panelPaginacion.setOpaque(false);
        panel.add(panelPaginacion, BorderLayout.SOUTH);

        // Evento: filtrado dinámico (misma idea que la v1, pero sobre la lista, no sobre RowFilter)
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrarLibros(); }

            @Override
            public void removeUpdate(DocumentEvent e) { filtrarLibros(); }

            @Override
            public void changedUpdate(DocumentEvent e) { filtrarLibros(); }
        });

        return panel;
    }

    private PanelRedondeado crearTarjetaLibro(Libro libro) {

        Color[] colores = coloresEstado(libro.estado);

        PanelRedondeado tarjeta = new PanelRedondeado(12);
        tarjeta.setBackground(COLOR_SUPERFICIE);
        tarjeta.setLayout(new BorderLayout(10, 0));
        tarjeta.setBorder(new EmptyBorder(10, 10, 10, 12));
        tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel franja = new JPanel();
        franja.setBackground(colores[0]);
        franja.setPreferredSize(new Dimension(6, 10));
        tarjeta.add(franja, BorderLayout.WEST);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(libro.titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAutorAnio = new JLabel(libro.autor + " · " + libro.anio);
        lblAutorAnio.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblAutorAnio.setForeground(Color.GRAY);
        lblAutorAnio.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel filaInferior = new JPanel(new BorderLayout());
        filaInferior.setOpaque(false);
        filaInferior.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaInferior.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        PanelRedondeado badge = new PanelRedondeado(999);
        badge.setBackground(colores[1]);
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));
        JLabel lblEstado = new JLabel(libro.estado);
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 9));
        lblEstado.setForeground(colores[2]);
        badge.add(lblEstado);

        JLabel lblCategoria = new JLabel(libro.categoria);
        lblCategoria.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblCategoria.setForeground(Color.GRAY);

        filaInferior.add(badge, BorderLayout.WEST);
        filaInferior.add(lblCategoria, BorderLayout.EAST);

        contenido.add(lblTitulo);
        contenido.add(Box.createVerticalStrut(2));
        contenido.add(lblAutorAnio);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(filaInferior);

        tarjeta.add(contenido, BorderLayout.CENTER);

        MouseAdapter clicTarjeta = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarLibro(libro, tarjeta);
            }
        };
        tarjeta.addMouseListener(clicTarjeta);
        for (Component c : contenido.getComponents()) {
            c.addMouseListener(clicTarjeta);
        }

        return tarjeta;
    }

    private void seleccionarLibro(Libro libro, PanelRedondeado tarjetaUI) {

        if (tarjetaSeleccionadaUI != null) {
            tarjetaSeleccionadaUI.setSeleccionada(false);
        }

        libroSeleccionado = libro;
        tarjetaSeleccionadaUI = tarjetaUI;
        tarjetaUI.setSeleccionada(true);

        txtCodigo.setText(libro.codigo);
        txtTitulo.setText(libro.titulo);
        txtAutor.setText(libro.autor);
        txtEditorial.setText(libro.editorial);
        cmbCategoria.setSelectedItem(libro.categoria);
        spnAnio.setValue(libro.anio);
        cmbEstado.setSelectedItem(libro.estado);
        chkReferencia.setSelected(libro.referencia);
        txtObservaciones.setText(libro.observaciones);
    }

    private void cargarDatosIniciales() {

        libros.add(new Libro("L001", "Cien Años de Soledad", "Gabriel García Márquez",
                "Sudamericana", "Ficción", 1967, "Disponible", false, ""));

        libros.add(new Libro("L002", "Introducción a los Algoritmos", "Cormen, Leiserson, Rivest",
                "MIT Press", "Tecnología", 2009, "Prestado", false, ""));

        libros.add(new Libro("L003", "Breve Historia del Tiempo", "Stephen Hawking",
                "Crítica", "Ciencia", 1988, "Disponible", false, ""));

        libros.add(new Libro("L004", "Diccionario de la Real Academia", "RAE",
                "Espasa", "Académico", 2014, "Disponible", true, "Uso exclusivo en sala."));

        libros.add(new Libro("L005", "El Principito", "Antoine de Saint-Exupéry",
                "Reynal & Hitchcock", "Infantil", 1943, "Prestado", false, ""));

        libros.add(new Libro("L006", "Sapiens: De Animales a Dioses", "Yuval Noah Harari",
                "Debate", "Historia", 2011, "Vencido", false, ""));

        libros.add(new Libro("L007", "Clean Code", "Robert C. Martin",
                "Prentice Hall", "Tecnología", 2008, "Disponible", false, ""));

        libros.add(new Libro("L008", "Cien Enigmas de Costa Rica", "Varios Autores",
                "EUNED", "Historia", 2015, "Reservado", false, ""));

        siguienteCodigo = 9;
    }


    private void nuevoLibro() {
        limpiarFormulario();
        txtCodigo.setText(generarSiguienteCodigo());
        txtTitulo.requestFocus();
    }

    private String generarSiguienteCodigo() {
        return String.format("L%03d", siguienteCodigo);
    }

    private void guardarLibro() {

        if (!validarCampos()) return;

        String codigo = txtCodigo.getText().trim();

        if (existeCodigo(codigo, null)) {
            JOptionPane.showMessageDialog(this, "Ya existe un libro registrado con el código \"" + codigo + "\".\n"
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
                (String) cmbEstado.getSelectedItem(),
                chkReferencia.isSelected(),
                txtObservaciones.getText().trim()
        );

        libros.add(nuevo);
        siguienteCodigo++;

        actualizarTarjetasDashboard();
        filtrarLibros();

        JOptionPane.showMessageDialog(this, "El libro \"" + nuevo.titulo + "\" se guardó correctamente.", "Registro guardado", JOptionPane.INFORMATION_MESSAGE);

        limpiarFormulario();
    }

    private void eliminarLibro() {

        if (libroSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un libro del catálogo antes de eliminarlo.", "Ningún registro seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el libro \"" + libroSeleccionado.titulo + "\"?\nEsta acción no se puede deshacer.", "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            libros.remove(libroSeleccionado);

            actualizarTarjetasDashboard();
            limpiarFormulario();
            filtrarLibros();

            JOptionPane.showMessageDialog(this, "El libro fue eliminado del catálogo.", "Registro eliminado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtCodigo.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtEditorial.setText("");
        cmbCategoria.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);
        spnAnio.setValue(Calendar.getInstance().get(Calendar.YEAR));
        chkReferencia.setSelected(false);
        txtObservaciones.setText("");

        if (tarjetaSeleccionadaUI != null) {
            tarjetaSeleccionadaUI.setSeleccionada(false);
        }
        libroSeleccionado = null;
        tarjetaSeleccionadaUI = null;
    }

    //Hasta aqui voy

}