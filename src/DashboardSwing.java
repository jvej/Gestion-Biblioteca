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
    private JComboBox<String> cmCategoria;
    private JComboBox<String> cmbEstado;
    private JSpinner spnAnio;
    private JCheckBox chkRefencia;
    private JTextArea txtObservaciones;
    private JTextField txtBuscar;

    private JPanel panelGrillaLibros;
    private JLanel lblContadorLibros;
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
                        new LineBorder(new Color(195, 200, 210), 1, true),
                        new EmptyBorder(5, 10, 5, 10))
        );
        return campo;
    }

    private void configurarCombo(JComboBox<String> combo) {

        combo.setFont(new Font("SansSerif", Font.PLAIN, 14));

        combo.setPreferredSize(new Dimension(250, 35));
    }

    private void agregarFilaFormulario(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, Component componente) {

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

        JButton btnBuscar = crearBoton("🔍", new Color(245, 247, 250), Color.DARK_GRAY);

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
        String[] columnas = {"ID", "Nombre", "Correo", "Rol", "Estado"};
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