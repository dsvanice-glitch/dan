package comm;

import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;

public class OrderingSystemMenu {

    //  THEME
    private static final Color BG_DARK       = new Color(10, 12, 18);
    private static final Color BG_PANEL      = new Color(16, 20, 28);
    private static final Color BG_CARD       = new Color(22, 27, 38);
    private static final Color BG_INPUT      = new Color(28, 35, 48);
    private static final Color ACCENT_GOLD   = new Color(212, 160, 60);
    private static final Color ACCENT_AMBER  = new Color(255, 185, 0);
    private static final Color TEXT_PRIMARY  = new Color(245, 242, 230);
    private static final Color TEXT_MUTED    = new Color(170, 165, 150);
    private static final Color BORDER_COLOR  = new Color(40, 48, 64);
    private static final Color SUCCESS_GREEN = new Color(60, 180, 100);
    private static final Color DANGER_RED    = new Color(210, 70, 70);
    private static final Color WARN_ORANGE   = new Color(220, 130, 30);
    private static final Color TABLE_SEL     = new Color(212, 160, 60, 55);
    private static final Color LOW_STOCK_BG  = new Color(80, 30, 30);

    //  FONTS
    private static final Font FONT_BRAND    = new Font("Tahoma", Font.BOLD, 16);
    private static final Font FONT_TITLE    = new Font("Tahoma", Font.BOLD, 14);
    private static final Font FONT_NAV      = new Font("Tahoma", Font.BOLD, 12);
    private static final Font FONT_LABEL    = new Font("Tahoma", Font.BOLD, 13);
    private static final Font FONT_INPUT    = new Font("Tahoma", Font.PLAIN, 13);
    private static final Font FONT_TABLE    = new Font("Tahoma", Font.PLAIN, 12);
    private static final Font FONT_DATETIME = new Font("Courier New", Font.BOLD, 12);
    private static final Font FONT_SMALL    = new Font("Tahoma", Font.PLAIN, 10);

    // Photo storage folder (relative to working directory)
    private static final String PHOTO_DIR = "product_photos";

    //  FIELDS
    private JFrame frmparentMenu;
    private JTabbedPane tabbedPane;
    private JLabel lblDateTime;

    private JComboBox<String> cmbProducts;
    private JComboBox<String> cmbCustomers;
    private DefaultComboBoxModel<String> productModel;
    private DefaultComboBoxModel<String> custonerModel;

    // Product image store (productId -> ImageIcon)
    private static final Map<Integer, ImageIcon> productImages = new HashMap<>();

    // ══════════════════════════════════════════════════════════════════════
    //  PHOTO PERSISTENCE HELPERS  (NEW)
    // ══════════════════════════════════════════════════════════════════════

    /** Saves an ImageIcon to disk as product_photos/<id>.png */
    private void savePhotoToDisk(int prodId, ImageIcon icon) {
        try {
            File dir = new File(PHOTO_DIR);
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, prodId + ".png");
            BufferedImage bi;
            Image img = icon.getImage();
            if (img instanceof BufferedImage) {
                bi = (BufferedImage) img;
            } else {
                bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
            }
            ImageIO.write(bi, "png", out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Loads a single product photo from disk into productImages map if file exists */
    private void loadPhotoFromDisk(int prodId) {
        File f = new File(PHOTO_DIR, prodId + ".png");
        if (f.exists()) {
            try {
                BufferedImage bi = ImageIO.read(f);
                if (bi != null) productImages.put(prodId, new ImageIcon(bi));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /** Called once at startup — loads all saved product photos from disk */
    private void loadAllPhotosFromDisk() {
        File dir = new File(PHOTO_DIR);
        if (!dir.exists()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));
        if (files == null) return;
        for (File f : files) {
            try {
                String name = f.getName().replace(".png", "");
                int prodId = Integer.parseInt(name);
                BufferedImage bi = ImageIO.read(f);
                if (bi != null) productImages.put(prodId, new ImageIcon(bi));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /** Deletes the photo file for a product from disk */
    private void deletePhotoFromDisk(int prodId) {
        File f = new File(PHOTO_DIR, prodId + ".png");
        if (f.exists()) f.delete();
    }

    //  ORIGINAL LOGIC METHODS

    private Connection getConnection() throws Exception {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/login_system", "root", "");
    }

    private ResultSet runQuery(String sql, Object... params) throws Exception {
        Connection conn = getConnection();
        PreparedStatement pst = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) pst.setObject(i + 1, params[i]);
        return pst.executeQuery();
    }

    private void loadCustomers(DefaultTableModel model, JComboBox<String> combo) {
        model.setRowCount(0);
        combo.removeAllItems();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
            while (rs.next()) {
                String name = rs.getString("name");
                model.addRow(new Object[]{rs.getInt("id"), name, rs.getString("contact"), rs.getString("address")});
                combo.addItem(name);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private double getLatestPrice(String productName) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT price FROM products WHERE name=?")) {
            pst.setString(1, productName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getDouble("price");
        }
        return 0;
    }

    private void refreshProducts(JComboBox<String> cmbProducts, DefaultComboBoxModel<String> model) {
        model.removeAllElements();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, price FROM products")) {
            while (rs.next()) model.addElement(rs.getString("name") + " - " + rs.getDouble("price"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadCustomersToCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM customers")) {
            while (rs.next()) combo.addItem(rs.getInt("id") + " - " + rs.getString("name"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private String generateOrderID(Connection conn) throws Exception {
        PreparedStatement pst = conn.prepareStatement(
                "SELECT COUNT(*) FROM orders WHERE DATE(order_date) = CURDATE()");
        ResultSet rs = pst.executeQuery();
        int count = 1;
        if (rs.next()) count = rs.getInt(1) + 1;
        String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return "ORD-" + datePart + "-" + String.format("%03d", count);
    }

    private void loadProductsToCombo(JComboBox<String> cmbProducts) {
        cmbProducts.removeAllItems();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, price FROM products")) {
            while (rs.next()) cmbProducts.addItem(rs.getString("name") + " - " + rs.getDouble("price"));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void logActivity(String action) {
        try (Connection conn = getConnection()) {
            PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO activity_logs(username, action_done) VALUES (?, ?)");
            pst.setString(1, Session.username);
            pst.setString(2, action);
            pst.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadDashboard(JLabel sales, JLabel orders, JLabel products, JLabel lowStock) {
        try (Connection conn = getConnection()) {
            PreparedStatement pst1 = conn.prepareStatement("SELECT IFNULL(SUM(total_amount),0) FROM orders");
            ResultSet rs1 = pst1.executeQuery();
            if (rs1.next()) sales.setText("₱ " + String.format("%.2f", rs1.getDouble(1)));

            PreparedStatement pst2 = conn.prepareStatement("SELECT COUNT(*) FROM orders");
            ResultSet rs2 = pst2.executeQuery();
            if (rs2.next()) orders.setText(String.valueOf(rs2.getInt(1)));

            PreparedStatement pst3 = conn.prepareStatement("SELECT COUNT(*) FROM products");
            ResultSet rs3 = pst3.executeQuery();
            if (rs3.next()) products.setText(String.valueOf(rs3.getInt(1)));

            PreparedStatement pst4 = conn.prepareStatement("SELECT COUNT(*) FROM products WHERE stock <= 5");
            ResultSet rs4 = pst4.executeQuery();
            if (rs4.next()) lowStock.setText(String.valueOf(rs4.getInt(1)));
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    //  WIDGET FACTORIES

    private JTextField styledField(String tooltip) {
        JTextField f = new JTextField();
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_GOLD);
        f.setFont(FONT_INPUT);
        f.setToolTipText(tooltip);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1), new EmptyBorder(3, 8, 3, 8)));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACCENT_GOLD, 1), new EmptyBorder(3, 8, 3, 8)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_COLOR, 1), new EmptyBorder(3, 8, 3, 8)));
            }
        });
        return f;
    }

    private void styleCombo(JComboBox<String> c) {
        c.setBackground(BG_INPUT);
        c.setForeground(TEXT_PRIMARY);
        c.setFont(FONT_INPUT);
        c.setBorder(new LineBorder(BORDER_COLOR, 1));
        c.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT_GOLD : BG_INPUT);
                setForeground(isSelected ? BG_DARK : TEXT_PRIMARY);
                setFont(FONT_INPUT);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
    }

    private JComboBox<String> styledCombo(DefaultComboBoxModel<String> model) {
        JComboBox<String> c = new JComboBox<>(model);
        styleCombo(c);
        return c;
    }

    private JButton navButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(ACCENT_GOLD);
                    g2.fillRect(4, getHeight() - 3, getWidth() - 8, 2);
                }
                g2.dispose();
            }
        };
        b.setFont(FONT_NAV); b.setForeground(TEXT_PRIMARY);
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(4, 10, 4, 10));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(ACCENT_GOLD); b.repaint(); }
            public void mouseExited(MouseEvent e)  { b.setForeground(TEXT_PRIMARY); b.repaint(); }
        });
        return b;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = !isEnabled()             ? new Color(80, 72, 40)
                         : getModel().isPressed()   ? ACCENT_GOLD.darker()
                         : getModel().isRollover()  ? ACCENT_AMBER
                         :                            ACCENT_GOLD;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setFont(getFont());
                g2.setColor(isEnabled() ? BG_DARK : new Color(100, 96, 80));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(new Font("Tahoma", Font.BOLD, 12));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton dangerButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? DANGER_RED.darker()
                         : getModel().isRollover() ? DANGER_RED
                         :                           new Color(90, 30, 30);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setFont(getFont());
                g2.setColor(TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(new Font("Tahoma", Font.BOLD, 12));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton ghostButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color border = getModel().isRollover() ? ACCENT_GOLD : BORDER_COLOR;
                g2.setColor(getModel().isRollover() ? new Color(212, 160, 60, 20) : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.setFont(getFont());
                g2.setColor(getModel().isRollover() ? ACCENT_GOLD : TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(new Font("Tahoma", Font.PLAIN, 12));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_PRIMARY);
        l.setFont(FONT_LABEL);
        return l;
    }

    private void styleTable(JTable t) {
        t.setBackground(BG_CARD);
        t.setForeground(TEXT_PRIMARY);
        t.setFont(FONT_TABLE);
        t.setRowHeight(26);
        t.setGridColor(BORDER_COLOR);
        t.setSelectionBackground(TABLE_SEL);
        t.setSelectionForeground(TEXT_PRIMARY);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.getTableHeader().setBackground(new Color(28, 35, 48));
        t.getTableHeader().setForeground(ACCENT_GOLD);
        t.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        t.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));
        t.getTableHeader().setReorderingAllowed(false);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setAutoCreateRowSorter(true);
    }

    private JScrollPane styledScroll(JTable t) {
        styleTable(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(new LineBorder(BORDER_COLOR, 1));
        styleScrollBar(sp.getVerticalScrollBar());
        styleScrollBar(sp.getHorizontalScrollBar());
        return sp;
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setBackground(BG_PANEL);
        sb.setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                thumbColor = new Color(60, 72, 90); trackColor = BG_PANEL;
            }
            protected JButton createDecreaseButton(int o) { return invisBtn(); }
            protected JButton createIncreaseButton(int o) { return invisBtn(); }
            private JButton invisBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    private void alignRight(JTable table) {
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);
        right.setBackground(BG_CARD);
        right.setForeground(TEXT_PRIMARY);
        for (int i = 1; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(right);
    }

    //  ENTRY POINT

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                OrderingSystemMenu window = new OrderingSystemMenu();
                window.frmparentMenu.setVisible(true);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    public OrderingSystemMenu() { initialize(); }

    //  INITIALIZE

    private void initialize() {

        // Load all saved product photos from disk at startup
        loadAllPhotosFromDisk();

        // Frame
        frmparentMenu = new JFrame();
        frmparentMenu.setTitle("Samson's BuildWorks and Infra Solutions");
        frmparentMenu.setBounds(100, 100, 1020, 780);
        frmparentMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmparentMenu.getContentPane().setBackground(BG_DARK);
        frmparentMenu.getContentPane().setLayout(null);
        frmparentMenu.setBackground(BG_DARK);
        frmparentMenu.setLocationRelativeTo(null);

        // Header Panel
        JPanel headerPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 25, 36), getWidth(), 0, new Color(12, 16, 24));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ACCENT_GOLD);
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.setColor(new Color(212, 160, 60, 22));
                g2.setStroke(new java.awt.BasicStroke(40));
                g2.drawLine(getWidth() - 170, 0, getWidth() - 90, getHeight());
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBounds(0, 0, 1020, 92);
        frmparentMenu.getContentPane().add(headerPanel);

        JPanel accentBar = new JPanel() {
            protected void paintComponent(Graphics g) {
                g.setColor(ACCENT_GOLD);
                g.fillRect(0, 0, 4, getHeight());
            }
        };
        accentBar.setOpaque(false);
        accentBar.setBounds(20, 16, 4, 58);
        headerPanel.add(accentBar);

        JLabel lblBrand = new JLabel(
            "<html><span style='font-size:15px;'>SAMSON'S</span>" +
            "<br><span style='font-size:9px; color:#AAA;'>BUILDWORKS &amp; INFRA SOLUTIONS</span></html>");
        lblBrand.setForeground(TEXT_PRIMARY);
        lblBrand.setFont(FONT_BRAND);
        lblBrand.setBounds(34, 17, 280, 55);
        headerPanel.add(lblBrand);

        JLabel lblWelcome = new JLabel("Welcome, " + Session.username + "  ·  " +
                (Session.role != null ? Session.role.toUpperCase() : "USER"));
        lblWelcome.setForeground(new Color(140, 136, 120));
        lblWelcome.setFont(FONT_SMALL);
        lblWelcome.setBounds(34, 72, 320, 14);
        headerPanel.add(lblWelcome);

        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setForeground(BORDER_COLOR);
        sep.setBounds(318, 22, 1, 48);
        headerPanel.add(sep);

        // Nav Buttons
        JButton btnHome      = navButton("🏠  HOME");
        JButton btnOrders    = navButton("🛒  ORDERS");
        JButton btnProducts  = navButton("📦  PRODUCT");
        JButton btnCustomers = navButton("👤  CUSTOMERS");
        JButton btnReports   = navButton("📊  REPORTS");
        JButton btnCalendar  = navButton("📅  CALENDAR");
        JButton btnLogout    = navButton("⏻  LOGOUT");

        btnHome.setBounds(328, 30, 90, 30);
        btnOrders.setBounds(420, 30, 100, 30);
        btnProducts.setBounds(524, 30, 98, 30);
        btnCustomers.setBounds(626, 30, 112, 30);
        btnReports.setBounds(742, 30, 96, 30);
        btnCalendar.setBounds(842, 30, 100, 30);
        btnLogout.setBounds(946, 30, 58, 30);

        btnLogout.setForeground(new Color(180, 80, 80));
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogout.setForeground(DANGER_RED); btnLogout.repaint(); }
            public void mouseExited(MouseEvent e)  { btnLogout.setForeground(new Color(180, 80, 80)); btnLogout.repaint(); }
        });

        btnHome.setToolTipText("Go to Dashboard");
        btnOrders.setToolTipText("Place a new order");
        btnProducts.setToolTipText("Manage products and inventory");
        btnCustomers.setToolTipText("Manage customer records");
        btnReports.setToolTipText("View sales reports");
        btnCalendar.setToolTipText("Open calendar");
        btnLogout.setToolTipText("Logout of the system");

        headerPanel.add(btnHome); headerPanel.add(btnOrders); headerPanel.add(btnProducts);
        headerPanel.add(btnCustomers); headerPanel.add(btnReports); headerPanel.add(btnCalendar); headerPanel.add(btnLogout);

        lblDateTime = new JLabel();
        lblDateTime.setForeground(ACCENT_GOLD);
        lblDateTime.setFont(FONT_DATETIME);
        lblDateTime.setBounds(328, 64, 240, 18);
        headerPanel.add(lblDateTime);

        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            lblDateTime.setText("⬡ " + sdf.format(new Date()));
        });
        timer.start();

        btnCalendar.addActionListener(e -> {
            JDialog dialog = new JDialog(frmparentMenu, "Calendar", true);
            dialog.setSize(320, 320);
            dialog.setLocationRelativeTo(frmparentMenu);
            dialog.getContentPane().setBackground(BG_CARD);
            JCalendar calendar = new JCalendar();
            calendar.setBackground(BG_CARD);
            dialog.getContentPane().add(calendar);
            dialog.setVisible(true);
        });

        // Tabbed Pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP) {
            public void updateUI() {
                setUI(new BasicTabbedPaneUI() {
                    protected void installDefaults() {
                        super.installDefaults();
                        highlight = BG_DARK; lightHighlight = BG_DARK;
                        shadow = BG_DARK; darkShadow = BG_DARK; focus = BG_DARK;
                    }
                    protected void paintTabArea(Graphics g, int tp, int si) {}
                    protected void paintFocusIndicator(Graphics g, int tp, Rectangle[] r, int ti, Rectangle ir, Rectangle tr, boolean s) {}
                    protected void paintContentBorder(Graphics g, int tp, int si) {}
                    protected void paintTabBackground(Graphics g, int tp, int ti, int x, int y, int w, int h, boolean s) {}
                    protected void paintTabBorder(Graphics g, int tp, int ti, int x, int y, int w, int h, boolean s) {}
                    protected int calculateTabHeight(int tp, int ti, int fh) { return 0; }
                    protected int calculateTabWidth(int tp, int ti, FontMetrics fm) { return 0; }
                });
            }
        };
        tabbedPane.setBackground(BG_DARK);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setBounds(0, 92, 1020, 660);
        frmparentMenu.getContentPane().add(tabbedPane);

        java.util.function.Function<String, JPanel> makeTab = title -> {
            JPanel p = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(BG_DARK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            p.setOpaque(true);
            p.setBackground(BG_DARK);

            JPanel bar = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    GradientPaint gp = new GradientPaint(0, 0, BG_PANEL, getWidth(), 0, BG_DARK);
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(ACCENT_GOLD);
                    g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                    g2.dispose();
                }
            };
            bar.setOpaque(false);
            bar.setBounds(0, 0, 1020, 40);

            JLabel lbl = new JLabel("▸  " + title.toUpperCase());
            lbl.setForeground(ACCENT_GOLD);
            lbl.setFont(FONT_TITLE);
            lbl.setBounds(20, 9, 600, 22);
            bar.add(lbl);
            p.add(bar);
            return p;
        };

        // ══════════════════════════════════════════════════════════════════════
        //  HOME TAB
        // ══════════════════════════════════════════════════════════════════════
        JPanel home = makeTab.apply("Home — Dashboard");
        tabbedPane.addTab("Home", null, home, null);

        JPanel heroBanner = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                GradientPaint strip = new GradientPaint(0, 0, ACCENT_GOLD, 0, getHeight(), new Color(120, 80, 20));
                g2.setPaint(strip);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.setColor(new Color(212, 160, 60, 14));
                g2.setStroke(new java.awt.BasicStroke(50));
                g2.drawLine(getWidth() - 200, 0, getWidth() - 80, getHeight());
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new java.awt.BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        heroBanner.setOpaque(false);
        heroBanner.setBounds(20, 52, 980, 100);
        home.add(heroBanner);

        JLabel heroIcon = new JLabel("🏗");
        heroIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        heroIcon.setBounds(18, 14, 52, 52);
        heroBanner.add(heroIcon);

        String timeGreeting;
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12)      timeGreeting = "Good morning";
        else if (hour < 17) timeGreeting = "Good afternoon";
        else                timeGreeting = "Good evening";

        JLabel heroGreeting = new JLabel(timeGreeting + ", " + Session.username + "!");
        heroGreeting.setForeground(ACCENT_GOLD);
        heroGreeting.setFont(new Font("Tahoma", Font.BOLD, 18));
        heroGreeting.setBounds(80, 14, 700, 28);
        heroBanner.add(heroGreeting);

        JLabel heroSub = new JLabel(
            "Welcome back to Samson's BuildWorks & Infra Solutions — your complete ordering and inventory hub.");
        heroSub.setForeground(TEXT_MUTED);
        heroSub.setFont(new Font("Tahoma", Font.PLAIN, 12));
        heroSub.setBounds(80, 44, 860, 18);
        heroBanner.add(heroSub);

        JLabel heroTip = new JLabel(
            "Tip: Use the nav bar above to switch between Orders, Products, Customers, and Reports.");
        heroTip.setForeground(new Color(120, 115, 100));
        heroTip.setFont(new Font("Tahoma", Font.PLAIN, 10));
        heroTip.setBounds(80, 64, 860, 16);
        heroBanner.add(heroTip);

        JLabel roleBadge = new JLabel("  " + (Session.role != null ? Session.role.toUpperCase() : "USER") + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(212, 160, 60, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(ACCENT_GOLD);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        roleBadge.setForeground(ACCENT_GOLD);
        roleBadge.setFont(new Font("Tahoma", Font.BOLD, 10));
        roleBadge.setOpaque(false);
        roleBadge.setBounds(880, 36, 80, 22);
        heroBanner.add(roleBadge);

        JLabel homeWelcome = new JLabel(
            "<html><div style='text-align:center;'>" +
            "<span style='font-size:22px; color:#D4A03C;'>Samson's BuildWorks</span><br>" +
            "<span style='font-size:13px; color:#888;'>Ordering &amp; Inventory Management System</span>" +
            "</div></html>");
        homeWelcome.setBounds(210, 166, 600, 60);
        homeWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        home.add(homeWelcome);

        JPanel homeDivider = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp  = new GradientPaint(0, 0, BG_DARK, getWidth() / 2, 0, ACCENT_GOLD);
                GradientPaint gp2 = new GradientPaint(getWidth() / 2, 0, ACCENT_GOLD, getWidth(), 0, BG_DARK);
                g2.setPaint(gp);  g2.fillRect(0, 0, getWidth() / 2, 2);
                g2.setPaint(gp2); g2.fillRect(getWidth() / 2, 0, getWidth() / 2, 2);
                g2.dispose();
            }
        };
        homeDivider.setOpaque(false);
        homeDivider.setBounds(160, 238, 700, 2);
        home.add(homeDivider);

        String[] statTitles = {"TOTAL SALES", "TOTAL ORDERS", "TOTAL PRODUCTS", "LOW STOCK ITEMS"};
        JLabel[] statValues = new JLabel[4];
        String[] statIcons  = {"₱", "#", "☰", "⚠"};
        Color[]  statColors = {SUCCESS_GREEN, ACCENT_GOLD, new Color(80, 140, 210), DANGER_RED};

        for (int i = 0; i < 4; i++) {
            final int fi = i;
            int col = fi % 2, row = fi / 2;
            JPanel card = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(BG_CARD);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(statColors[fi]);
                    g2.fillRect(0, 0, 4, getHeight());
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setBounds(160 + col * 360, 258 + row * 100, 330, 80);

            JLabel iconLbl = new JLabel(statIcons[fi]);
            iconLbl.setFont(new Font("Tahoma", Font.BOLD, 22));
            iconLbl.setForeground(statColors[fi]);
            iconLbl.setBounds(14, 12, 36, 36);
            card.add(iconLbl);

            JLabel titleLbl = new JLabel(statTitles[fi]);
            titleLbl.setFont(FONT_SMALL);
            titleLbl.setForeground(TEXT_MUTED);
            titleLbl.setBounds(56, 10, 260, 18);
            card.add(titleLbl);

            statValues[fi] = new JLabel("—");
            statValues[fi].setFont(new Font("Tahoma", Font.BOLD, 22));
            statValues[fi].setForeground(TEXT_PRIMARY);
            statValues[fi].setBounds(56, 30, 260, 34);
            card.add(statValues[fi]);

            home.add(card);
        }

        loadDashboard(statValues[0], statValues[1], statValues[2], statValues[3]);

        JButton btnRefreshDash = ghostButton("↻  Refresh");
        btnRefreshDash.setBounds(448, 474, 124, 30);
        btnRefreshDash.setToolTipText("Refresh dashboard statistics");
        btnRefreshDash.addActionListener(e -> loadDashboard(statValues[0], statValues[1], statValues[2], statValues[3]));
        home.add(btnRefreshDash);

        // ══════════════════════════════════════════════════════════════════════
        //  ORDERS TAB
        // ══════════════════════════════════════════════════════════════════════
        JPanel orders = makeTab.apply("Orders — New Transaction");
        tabbedPane.addTab("Orders", null, orders, null);

        productModel = new DefaultComboBoxModel<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, price FROM products")) {
            while (rs.next())
                productModel.addElement(rs.getString("name") + " - " + rs.getDouble("price"));
        } catch (Exception ex) { ex.printStackTrace(); }

        JPanel orderInputRow = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_PANEL);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDER_COLOR);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        orderInputRow.setOpaque(false);
        orderInputRow.setBounds(0, 42, 1020, 62);
        orders.add(orderInputRow);

        JLabel lblCustomer = styledLabel("Customer:");
        lblCustomer.setBounds(16, 18, 90, 25);
        orderInputRow.add(lblCustomer);

        JComboBox<String> cmbCustomersOrders = new JComboBox<>();
        styleCombo(cmbCustomersOrders);
        cmbCustomersOrders.setBounds(108, 18, 260, 26);
        cmbCustomersOrders.setToolTipText("Select the customer placing this order");
        orderInputRow.add(cmbCustomersOrders);

        loadCustomersToCombo(cmbCustomersOrders);

        JLabel lblProduct = styledLabel("Product:");
        lblProduct.setBounds(380, 18, 80, 25);
        orderInputRow.add(lblProduct);

        cmbProducts = styledCombo(productModel);
        cmbProducts.setBounds(464, 18, 290, 26);
        cmbProducts.setToolTipText("Select or scan a product");
        orderInputRow.add(cmbProducts);

        JLabel lblQty = styledLabel("Qty:");
        lblQty.setBounds(766, 18, 40, 25);
        orderInputRow.add(lblQty);

        JTextField txtQty = styledField("Quantity");
        txtQty.setBounds(808, 18, 80, 26);
        orderInputRow.add(txtQty);

        JButton btnAdd = primaryButton("+ Add");
        btnAdd.setBounds(900, 14, 100, 34);
        btnAdd.setToolTipText("Add product to cart");
        orderInputRow.add(btnAdd);

        String[] cartCols = {"Product", "Price", "Available", "Qty", "Subtotal"};
        DefaultTableModel cartModel = new DefaultTableModel(cartCols, 0);
        JTable cartTable = new JTable(cartModel);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(280);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        JScrollPane cartScroll = styledScroll(cartTable);
        alignRight(cartTable);
        cartScroll.setBounds(20, 114, 980, 280);
        orders.add(cartScroll);

        // ── PRODUCT PHOTO PREVIEW PANEL (Orders Tab) ──────────────────────────
        JLabel lblOrderPhotoHeader = new JLabel("◈  PRODUCT PHOTO");
        lblOrderPhotoHeader.setForeground(ACCENT_GOLD);
        lblOrderPhotoHeader.setFont(new Font("Tahoma", Font.BOLD, 10));
        lblOrderPhotoHeader.setBounds(630, 458, 188, 14);
        orders.add(lblOrderPhotoHeader);

        JPanel orderPhotoPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        orderPhotoPanel.setOpaque(false);
        orderPhotoPanel.setBounds(630, 474, 188, 170);
        orders.add(orderPhotoPanel);

        JLabel orderPhotoImg = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Icon ic = getIcon();
                if (ic == null) {
                    g2.setFont(new Font("Tahoma", Font.PLAIN, 28));
                    g2.setColor(new Color(50, 60, 78));
                    FontMetrics fm = g2.getFontMetrics();
                    String ico = "📷";
                    g2.drawString(ico, (getWidth() - fm.stringWidth(ico)) / 2, getHeight() / 2 - 2);
                    g2.setFont(FONT_SMALL);
                    g2.setColor(TEXT_MUTED);
                    String hint = "No photo";
                    FontMetrics fm2 = g2.getFontMetrics();
                    g2.drawString(hint, (getWidth() - fm2.stringWidth(hint)) / 2, getHeight() / 2 + 14);
                }
                g2.dispose();
            }
        };
        orderPhotoImg.setBounds(10, 10, 168, 130);
        orderPhotoPanel.add(orderPhotoImg);

        JLabel orderPhotoName = new JLabel("Select a product", SwingConstants.CENTER);
        orderPhotoName.setForeground(TEXT_MUTED);
        orderPhotoName.setFont(FONT_SMALL);
        orderPhotoName.setBounds(0, 144, 188, 16);
        orderPhotoPanel.add(orderPhotoName);

        // Update photo when product combo selection changes
        cmbProducts.addActionListener(e -> {
            String selected = (String) cmbProducts.getSelectedItem();
            if (selected == null) {
                orderPhotoImg.setIcon(null);
                orderPhotoName.setText("Select a product");
                orderPhotoPanel.repaint();
                return;
            }
            String productName = selected.split(" - ")[0].trim();
            try (Connection conn = getConnection();
                 PreparedStatement pst = conn.prepareStatement("SELECT id FROM products WHERE name = ?")) {
                pst.setString(1, productName);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    int prodId = rs.getInt("id");
                    // Try map first, then disk
                    ImageIcon icon = productImages.get(prodId);
                    if (icon == null) {
                        loadPhotoFromDisk(prodId);
                        icon = productImages.get(prodId);
                    }
                    if (icon != null) {
                        Image scaled = icon.getImage().getScaledInstance(168, 130, Image.SCALE_SMOOTH);
                        orderPhotoImg.setIcon(new ImageIcon(scaled));
                    } else {
                        orderPhotoImg.setIcon(null);
                    }
                } else {
                    orderPhotoImg.setIcon(null);
                }
            } catch (Exception ex) {
                orderPhotoImg.setIcon(null);
                ex.printStackTrace();
            }
            orderPhotoName.setText(productName);
            orderPhotoPanel.repaint();
        });

        JPanel footerRow = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_PANEL);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDER_COLOR);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        footerRow.setOpaque(false);
        footerRow.setBounds(0, 404, 1020, 52);
        orders.add(footerRow);

        JLabel lblTotal = new JLabel("TOTAL:  ₱ 0.00");
        lblTotal.setBounds(20, 14, 300, 24);
        lblTotal.setForeground(ACCENT_GOLD);
        lblTotal.setFont(new Font("Tahoma", Font.BOLD, 15));
        footerRow.add(lblTotal);

        JButton btnPlaceOrder = primaryButton("✔  Place Order");
        btnPlaceOrder.setBounds(810, 11, 188, 30);
        btnPlaceOrder.setToolTipText("Confirm and submit this order");
        footerRow.add(btnPlaceOrder);

        JButton btnRemove = dangerButton("✖  Remove Item");
        btnRemove.setBounds(20, 470, 150, 30);
        btnRemove.setToolTipText("Remove selected item from cart");
        orders.add(btnRemove);

        JButton btnEdit = ghostButton("✎  Edit Qty");
        btnEdit.setBounds(182, 470, 128, 30);
        btnEdit.setToolTipText("Edit quantity of selected cart item");
        orders.add(btnEdit);

        JLabel lblScan = styledLabel("Barcode:");
        lblScan.setFont(FONT_SMALL);
        lblScan.setForeground(TEXT_MUTED);
        lblScan.setBounds(328, 474, 80, 22);
        orders.add(lblScan);

        JTextField txtBarcodeScan = styledField("Scan or type barcode");
        txtBarcodeScan.setBounds(412, 470, 200, 26);
        txtBarcodeScan.setToolTipText("Scan barcode to auto-select product");
        orders.add(txtBarcodeScan);

        final double[] total = {0};
        Runnable computeTotal = () -> {
            total[0] = 0;
            for (int i = 0; i < cartModel.getRowCount(); i++)
                total[0] += ((Number) cartModel.getValueAt(i, 4)).doubleValue();
            lblTotal.setText("TOTAL:  ₱ " + String.format("%.2f", total[0]));
        };

        btnAdd.addActionListener(e -> {
            try {
                String selected = (String) cmbProducts.getSelectedItem();
                if (selected == null) { JOptionPane.showMessageDialog(frmparentMenu, "Select product!"); return; }
                if (txtQty.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(frmparentMenu, "Enter quantity!"); return; }
                int qty = Integer.parseInt(txtQty.getText());
                if (qty <= 0) { JOptionPane.showMessageDialog(frmparentMenu, "Quantity must be greater than 0!"); return; }
                String productName = selected.split("-")[0].trim();
                Connection conn = getConnection();
                PreparedStatement pst = conn.prepareStatement("SELECT price, stock FROM products WHERE name = ?");
                pst.setString(1, productName);
                ResultSet rs = pst.executeQuery();
                if (!rs.next()) { JOptionPane.showMessageDialog(frmparentMenu, "Product not found!"); return; }
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                if (qty > stock) {
                    JOptionPane.showMessageDialog(frmparentMenu, "Not enough stock!\nAvailable stock: " + stock);
                    return;
                }
                boolean found = false;
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    if (cartModel.getValueAt(i, 0).equals(productName)) {
                        int existingQty = (int) cartModel.getValueAt(i, 3);
                        int newQty = existingQty + qty;
                        if (newQty > stock) {
                            JOptionPane.showMessageDialog(frmparentMenu, "Exceeds stock!\nAvailable stock: " + stock);
                            return;
                        }
                        cartModel.setValueAt(newQty, i, 3);
                        cartModel.setValueAt(price * newQty, i, 4);
                        found = true; break;
                    }
                }
                if (!found) cartModel.addRow(new Object[]{productName, price, stock, qty, price * qty});
                computeTotal.run();
                txtQty.setText("");
                txtQty.requestFocusInWindow();
            } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, "Invalid input!"); }
        });

        txtQty.addActionListener(e -> btnAdd.doClick());

        btnRemove.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(frmparentMenu, "Remove selected item?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    cartModel.removeRow(row);
                    computeTotal.run();
                }
            } else JOptionPane.showMessageDialog(frmparentMenu, "Select item to remove!");
        });

        btnEdit.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                String input = JOptionPane.showInputDialog(frmparentMenu, "Enter new quantity:");
                try {
                    int newQty = Integer.parseInt(input);
                    String productName = cartModel.getValueAt(row, 0).toString();
                    Connection conn = getConnection();
                    PreparedStatement pst = conn.prepareStatement("SELECT stock, price FROM products WHERE name = ?");
                    pst.setString(1, productName);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        int stock = rs.getInt("stock");
                        double price = rs.getDouble("price");
                        if (newQty > stock) {
                            JOptionPane.showMessageDialog(frmparentMenu, "Exceeds stock!\nAvailable stock: " + stock);
                            return;
                        }
                        cartModel.setValueAt(newQty, row, 3);
                        cartModel.setValueAt(price * newQty, row, 4);
                        computeTotal.run();
                    }
                } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, "Invalid input!"); }
            } else JOptionPane.showMessageDialog(frmparentMenu, "Select item!");
        });

        txtBarcodeScan.addActionListener(e -> {
            try (Connection conn = getConnection()) {
                PreparedStatement pst = conn.prepareStatement("SELECT name, price FROM products WHERE barcode=?");
                pst.setString(1, txtBarcodeScan.getText());
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    cmbProducts.setSelectedItem(rs.getString("name") + " - " + rs.getDouble("price"));
                    txtQty.requestFocus();
                } else {
                    JOptionPane.showMessageDialog(frmparentMenu, "Barcode not found!");
                }
            } catch (Exception ex) { ex.printStackTrace(); }
            txtBarcodeScan.setText("");
        });

        btnPlaceOrder.addActionListener(e -> {
            if (cartModel.getRowCount() == 0) { JOptionPane.showMessageDialog(frmparentMenu, "Cart is empty!"); return; }
            int confirm = JOptionPane.showConfirmDialog(frmparentMenu, "Confirm and place this order?", "Confirm Order", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            btnPlaceOrder.setEnabled(false);
            Connection conn = null; PreparedStatement pstOrder = null, pstDetails = null, pstStock = null; ResultSet rs = null;
            try {
                conn = getConnection(); conn.setAutoCommit(false);
                String orderCode = generateOrderID(conn);

                String orderSQL = "INSERT INTO orders (order_code, customer_id, total_amount, order_date) VALUES (?, ?, ?, NOW())";
                pstOrder = conn.prepareStatement(orderSQL, Statement.RETURN_GENERATED_KEYS);
                pstOrder.setString(1, orderCode);
                String selectedCustomer = cmbCustomersOrders.getSelectedItem().toString();
                int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);
                pstOrder.setInt(2, customerId);
                pstOrder.setDouble(3, total[0]);
                pstOrder.executeUpdate();
                rs = pstOrder.getGeneratedKeys();
                int orderId = 0;
                if (rs.next()) orderId = rs.getInt(1);

                pstDetails = conn.prepareStatement(
                        "INSERT INTO order_details (order_id, product, price, quantity, subtotal) VALUES (?, ?, ?, ?, ?)");
                pstStock = conn.prepareStatement("UPDATE products SET stock = stock - ? WHERE name = ?");

                StringBuilder receipt = new StringBuilder();
                receipt.append("SAMSON BUILDWORKS\nMarikina City\nContact: 09123456789\n");
                receipt.append("========================\n");
                receipt.append("RECEIPT\n========================\n");
                receipt.append("Order ID: ").append(orderCode).append("\n");
                receipt.append("Customer: ").append(selectedCustomer).append("\n\n");

                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    String product  = cartModel.getValueAt(i, 0).toString();
                    double price    = ((Number) cartModel.getValueAt(i, 1)).doubleValue();
                    int qty         = ((Number) cartModel.getValueAt(i, 3)).intValue();
                    double subtotal = ((Number) cartModel.getValueAt(i, 4)).doubleValue();
                    pstDetails.setInt(1, orderId); pstDetails.setString(2, product);
                    pstDetails.setDouble(3, price); pstDetails.setInt(4, qty);
                    pstDetails.setDouble(5, subtotal); pstDetails.addBatch();
                    pstStock.setInt(1, qty); pstStock.setString(2, product); pstStock.addBatch();
                    receipt.append(product).append(" x").append(qty).append(" — ₱").append(String.format("%.2f", subtotal)).append("\n");
                }
                pstDetails.executeBatch(); pstStock.executeBatch(); conn.commit();
                logActivity("Placed order: " + orderCode);
                receipt.append("\nTOTAL: ₱").append(String.format("%.2f", total[0]));

                JTextArea txtReceipt = new JTextArea(receipt.toString());
                txtReceipt.setEditable(false);
                txtReceipt.setBackground(BG_CARD);
                txtReceipt.setForeground(TEXT_PRIMARY);
                txtReceipt.setFont(new Font("Courier New", Font.PLAIN, 12));
                JOptionPane.showMessageDialog(frmparentMenu, new JScrollPane(txtReceipt), "Receipt", JOptionPane.INFORMATION_MESSAGE);
                try { txtReceipt.print(); } catch (Exception printEx) { JOptionPane.showMessageDialog(frmparentMenu, "Printing failed!"); }

                cartModel.setRowCount(0); total[0] = 0; lblTotal.setText("TOTAL:  ₱ 0.00");
                refreshProducts(cmbProducts, productModel);

            } catch (Exception ex) {
                ex.printStackTrace();
                try { if (conn != null) conn.rollback(); } catch (Exception e1) { e1.printStackTrace(); }
                JOptionPane.showMessageDialog(frmparentMenu, "Error: " + ex.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception ignored) {}
                try { if (pstOrder != null) pstOrder.close(); } catch (Exception ignored) {}
                try { if (pstDetails != null) pstDetails.close(); } catch (Exception ignored) {}
                try { if (pstStock != null) pstStock.close(); } catch (Exception ignored) {}
                try { if (conn != null) conn.close(); } catch (Exception ignored) {}
                btnPlaceOrder.setEnabled(true);
            }
        });

        // ══════════════════════════════════════════════════════════════════════
        //  PRODUCTS TAB
        // ══════════════════════════════════════════════════════════════════════
        JPanel products = makeTab.apply("Products — Inventory Management");
        tabbedPane.addTab("Products", null, products, null);

        JPanel prodFormPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_PANEL);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDER_COLOR);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        prodFormPanel.setOpaque(false);
        prodFormPanel.setBounds(0, 42, 1020, 170);
        products.add(prodFormPanel);

        // Photo upload row
        final ImageIcon[] pendingPhoto = { null };

        JPanel photoPreview = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (pendingPhoto[0] != null) {
                    Image img = pendingPhoto[0].getImage()
                            .getScaledInstance(getWidth() - 4, getHeight() - 4, Image.SCALE_SMOOTH);
                    g2.drawImage(img, 2, 2, null);
                } else {
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                    g2.setFont(new Font("Tahoma", Font.PLAIN, 22));
                    g2.setColor(new Color(60, 72, 90));
                    FontMetrics fm = g2.getFontMetrics();
                    String icon = "📷";
                    g2.drawString(icon, (getWidth() - fm.stringWidth(icon)) / 2, getHeight() / 2 - 4);
                    g2.setFont(FONT_SMALL);
                    g2.setColor(TEXT_MUTED);
                    String hint = "No photo";
                    FontMetrics fm2 = g2.getFontMetrics();
                    g2.drawString(hint, (getWidth() - fm2.stringWidth(hint)) / 2, getHeight() / 2 + 14);
                }
                g2.dispose();
            }
        };
        photoPreview.setOpaque(false);
        photoPreview.setBounds(20, 10, 80, 80);
        prodFormPanel.add(photoPreview);

        JButton btnChoosePhoto = ghostButton("📂 Choose Photo");
        btnChoosePhoto.setBounds(110, 10, 140, 30);
        btnChoosePhoto.setToolTipText("Select an image for this product (JPG / PNG / GIF)");
        prodFormPanel.add(btnChoosePhoto);

        JButton btnClearPhoto = dangerButton("✖ Remove Photo");
        btnClearPhoto.setBounds(110, 48, 140, 28);
        btnClearPhoto.setToolTipText("Remove the current product photo");
        prodFormPanel.add(btnClearPhoto);

        JLabel lblPhotoPath = new JLabel("No photo selected");
        lblPhotoPath.setForeground(TEXT_MUTED);
        lblPhotoPath.setFont(FONT_SMALL);
        lblPhotoPath.setBounds(260, 22, 500, 16);
        prodFormPanel.add(lblPhotoPath);

        JPanel photoSep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BORDER_COLOR);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        photoSep.setOpaque(false);
        photoSep.setBounds(20, 98, 980, 1);
        prodFormPanel.add(photoSep);

        // Fields
        JLabel lblPName = styledLabel("Name:");
        lblPName.setBounds(20, 108, 70, 25); prodFormPanel.add(lblPName);
        JTextField txtPName = styledField("Product name");
        txtPName.setBounds(95, 108, 220, 26);
        txtPName.setToolTipText("Enter product name");
        prodFormPanel.add(txtPName);

        JLabel lblPPrice = styledLabel("Price:");
        lblPPrice.setBounds(20, 138, 70, 25); prodFormPanel.add(lblPPrice);
        JTextField txtPPrice = styledField("0.00");
        txtPPrice.setBounds(95, 138, 220, 26);
        txtPPrice.setToolTipText("Enter unit price (e.g. 150.00)");
        prodFormPanel.add(txtPPrice);

        JLabel lblPStock = styledLabel("Stock:");
        lblPStock.setBounds(340, 108, 70, 25); prodFormPanel.add(lblPStock);
        JTextField txtPStock = styledField("0");
        txtPStock.setBounds(415, 108, 160, 26);
        txtPStock.setToolTipText("Enter stock quantity");
        prodFormPanel.add(txtPStock);

        JLabel lblBarcode = styledLabel("Barcode:");
        lblBarcode.setBounds(340, 138, 80, 25); prodFormPanel.add(lblBarcode);
        JTextField txtBarcode = styledField("Barcode (optional)");
        txtBarcode.setBounds(424, 138, 160, 26);
        txtBarcode.setToolTipText("Product barcode (optional)");
        prodFormPanel.add(txtBarcode);

        JLabel lblSearch = styledLabel("Search:");
        lblSearch.setFont(FONT_SMALL);
        lblSearch.setForeground(TEXT_MUTED);
        lblSearch.setBounds(600, 108, 78, 25); prodFormPanel.add(lblSearch);
        JTextField txtSearch = styledField("Filter by name...");
        txtSearch.setBounds(644, 108, 180, 26);
        txtSearch.setToolTipText("Type to filter products");
        prodFormPanel.add(txtSearch);

        JButton btnAddProd    = primaryButton("+ Add");
        JButton btnUpdateProd = ghostButton("↑ Update");
        JButton btnDeleteProd = dangerButton("✖ Delete");
        JButton btnClearProd  = ghostButton("⟳ Clear");

        btnAddProd.setBounds(838, 108, 90, 28);
        btnUpdateProd.setBounds(938, 108, 62, 28);
        btnDeleteProd.setBounds(838, 140, 90, 28);
        btnClearProd.setBounds(938, 140, 62, 28);

        btnAddProd.setToolTipText("Add new product");
        btnUpdateProd.setToolTipText("Update selected product");
        btnDeleteProd.setToolTipText("Delete selected product");
        btnClearProd.setToolTipText("Clear form fields");

        prodFormPanel.add(btnAddProd); prodFormPanel.add(btnUpdateProd);
        prodFormPanel.add(btnDeleteProd); prodFormPanel.add(btnClearProd);

        // Product table
        String[] prodCols = {"ID", "Barcode", "Name", "Price", "Stock"};
        DefaultTableModel prodModel = new DefaultTableModel(prodCols, 0);
        JTable prodTable = new JTable(prodModel);
        prodTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        prodTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        prodTable.getColumnModel().getColumn(2).setPreferredWidth(340);
        prodTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        prodTable.getColumnModel().getColumn(4).setPreferredWidth(110);

        prodTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                try {
                    Object stockObj = table.getValueAt(row, 4);
                    if (stockObj != null && Integer.parseInt(stockObj.toString()) <= 5) {
                        c.setBackground(isSelected ? TABLE_SEL : LOW_STOCK_BG);
                        c.setForeground(new Color(255, 160, 60));
                    } else {
                        c.setBackground(isSelected ? TABLE_SEL : BG_CARD);
                        c.setForeground(TEXT_PRIMARY);
                    }
                } catch (Exception ex) {
                    c.setBackground(isSelected ? TABLE_SEL : BG_CARD);
                    c.setForeground(TEXT_PRIMARY);
                }
                setFont(FONT_TABLE);
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return c;
            }
        });

        styleTable(prodTable);
        JScrollPane prodScroll = new JScrollPane(prodTable);
        prodScroll.setBackground(BG_CARD);
        prodScroll.getViewport().setBackground(BG_CARD);
        prodScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        styleScrollBar(prodScroll.getVerticalScrollBar());
        styleScrollBar(prodScroll.getHorizontalScrollBar());
        prodScroll.setBounds(20, 222, 780, 360);
        products.add(prodScroll);

        // Live photo preview panel (right of table)
        JPanel rowPhotoPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        rowPhotoPanel.setOpaque(false);
        rowPhotoPanel.setBounds(812, 222, 188, 200);
        products.add(rowPhotoPanel);

        JLabel rowPhotoImg = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Icon ic = getIcon();
                if (ic != null) {
                    ic.paintIcon(this, g2, 0, 0);
                } else {
                    g2.setFont(new Font("Tahoma", Font.PLAIN, 32));
                    g2.setColor(new Color(50, 60, 78));
                    g2.drawString("📷", 60, 88);
                    g2.setFont(FONT_SMALL);
                    g2.setColor(TEXT_MUTED);
                    g2.drawString("No photo", 58, 110);
                }
                g2.dispose();
            }
        };
        rowPhotoImg.setBounds(10, 10, 168, 140);
        rowPhotoPanel.add(rowPhotoImg);

        JLabel rowPhotoName = new JLabel("Product Photo", SwingConstants.CENTER);
        rowPhotoName.setForeground(TEXT_MUTED);
        rowPhotoName.setFont(FONT_SMALL);
        rowPhotoName.setBounds(0, 156, 188, 16);
        rowPhotoPanel.add(rowPhotoName);

        JLabel lblPhotoSide = new JLabel("◈  PRODUCT PHOTO");
        lblPhotoSide.setForeground(ACCENT_GOLD);
        lblPhotoSide.setFont(new Font("Tahoma", Font.BOLD, 10));
        lblPhotoSide.setBounds(812, 210, 188, 14);
        products.add(lblPhotoSide);

        // Legend
        JLabel legendLow = new JLabel("■ Low Stock (≤5)");
        legendLow.setFont(FONT_SMALL);
        legendLow.setForeground(new Color(255, 140, 60));
        legendLow.setBounds(20, 592, 160, 18);
        products.add(legendLow);

        // LOAD PRODUCTS
        Runnable loadProducts = () -> {
            prodModel.setRowCount(0);
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
                while (rs.next()) {
                    prodModel.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("barcode"),
                        rs.getString("name"), rs.getDouble("price"), rs.getInt("stock")
                    });
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        };
        loadProducts.run();

        prodTable.getSelectionModel().addListSelectionListener(e -> {
            int row = prodTable.getSelectedRow();
            if (row >= 0) {
                txtBarcode.setText(prodModel.getValueAt(row, 1) != null ? prodModel.getValueAt(row, 1).toString() : "");
                txtPName.setText(prodModel.getValueAt(row, 2).toString());
                txtPPrice.setText(prodModel.getValueAt(row, 3).toString());
                txtPStock.setText(prodModel.getValueAt(row, 4).toString());

                int prodId = (int) prodModel.getValueAt(row, 0);
                // Try memory map first, then load from disk if missing
                ImageIcon storedIcon = productImages.get(prodId);
                if (storedIcon == null) {
                    loadPhotoFromDisk(prodId);
                    storedIcon = productImages.get(prodId);
                }
                if (storedIcon != null) {
                    Image scaled = storedIcon.getImage().getScaledInstance(168, 140, Image.SCALE_SMOOTH);
                    rowPhotoImg.setIcon(new ImageIcon(scaled));
                    rowPhotoName.setText(txtPName.getText());
                    pendingPhoto[0] = storedIcon;
                    lblPhotoPath.setText("(existing photo)");
                    photoPreview.repaint();
                } else {
                    rowPhotoImg.setIcon(null);
                    rowPhotoName.setText("No photo");
                    pendingPhoto[0] = null;
                    lblPhotoPath.setText("No photo selected");
                    photoPreview.repaint();
                }
                rowPhotoPanel.repaint();
            }
        });

        txtPName.addActionListener(e -> txtPPrice.requestFocusInWindow());
        txtPPrice.addActionListener(e -> txtPStock.requestFocusInWindow());
        txtPStock.addActionListener(e -> txtBarcode.requestFocusInWindow());

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                String text = txtSearch.getText().toLowerCase();
                prodModel.setRowCount(0);
                try (Connection conn = getConnection();
                     PreparedStatement pst = conn.prepareStatement(
                             "SELECT * FROM products WHERE LOWER(name) LIKE ?")) {
                    pst.setString(1, "%" + text + "%");
                    ResultSet rs = pst.executeQuery();
                    while (rs.next()) {
                        prodModel.addRow(new Object[]{
                            rs.getInt("id"), rs.getString("barcode"),
                            rs.getString("name"), rs.getDouble("price"), rs.getInt("stock")
                        });
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        java.util.function.Supplier<Boolean> checkPassword = () -> {
            String input = JOptionPane.showInputDialog(frmparentMenu, "Enter admin password:");
            return input != null && input.equals("admin123");
        };

        // ── Choose photo: save to disk immediately ─────────────────────────────
        btnChoosePhoto.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Product Photo");
            fc.setFileFilter(new FileNameExtensionFilter("Image files (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
            fc.setAcceptAllFileFilterUsed(false);
            if (fc.showOpenDialog(frmparentMenu) == JFileChooser.APPROVE_OPTION) {
                File chosen = fc.getSelectedFile();
                try {
                    BufferedImage bi = ImageIO.read(chosen);
                    if (bi == null) { JOptionPane.showMessageDialog(frmparentMenu, "Cannot read image file!"); return; }
                    pendingPhoto[0] = new ImageIcon(bi);
                    lblPhotoPath.setText(chosen.getName());
                    photoPreview.repaint();

                    // If a row is selected, save to disk immediately so it persists
                    int selectedRow = prodTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        int prodId = (int) prodModel.getValueAt(selectedRow, 0);
                        productImages.put(prodId, pendingPhoto[0]);
                        savePhotoToDisk(prodId, pendingPhoto[0]);   // <-- PERSIST TO DISK
                        // Refresh side panel
                        Image scaled = pendingPhoto[0].getImage().getScaledInstance(168, 140, Image.SCALE_SMOOTH);
                        rowPhotoImg.setIcon(new ImageIcon(scaled));
                        rowPhotoName.setText(prodModel.getValueAt(selectedRow, 2).toString());
                        rowPhotoPanel.repaint();
                        // Also refresh the orders tab combo photo if that product is selected there
                        String comboSelected = (String) cmbProducts.getSelectedItem();
                        if (comboSelected != null) {
                            String comboName = comboSelected.split(" - ")[0].trim();
                            String rowName = prodModel.getValueAt(selectedRow, 2).toString();
                            if (comboName.equals(rowName)) {
                                Image ordScaled = pendingPhoto[0].getImage().getScaledInstance(168, 130, Image.SCALE_SMOOTH);
                                orderPhotoImg.setIcon(new ImageIcon(ordScaled));
                                orderPhotoName.setText(rowName);
                                orderPhotoPanel.repaint();
                            }
                        }
                    }
                } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, "Error loading image: " + ex.getMessage()); }
            }
        });

        btnClearPhoto.addActionListener(e -> {
            pendingPhoto[0] = null;
            lblPhotoPath.setText("No photo selected");
            photoPreview.repaint();
            // Remove from map and disk if a row is selected
            int selectedRow = prodTable.getSelectedRow();
            if (selectedRow >= 0) {
                int prodId = (int) prodModel.getValueAt(selectedRow, 0);
                productImages.remove(prodId);
                deletePhotoFromDisk(prodId);   // <-- DELETE FROM DISK
                rowPhotoImg.setIcon(null);
                rowPhotoName.setText("No photo");
                rowPhotoPanel.repaint();
                // Clear orders tab preview if same product is selected
                String comboSelected = (String) cmbProducts.getSelectedItem();
                if (comboSelected != null) {
                    String comboName = comboSelected.split(" - ")[0].trim();
                    String rowName = prodModel.getValueAt(selectedRow, 2).toString();
                    if (comboName.equals(rowName)) {
                        orderPhotoImg.setIcon(null);
                        orderPhotoName.setText(rowName);
                        orderPhotoPanel.repaint();
                    }
                }
            }
        });

        btnAddProd.addActionListener(e -> {
            if (!checkPassword.get()) return;
            if (txtPName.getText().trim().isEmpty() || txtPPrice.getText().trim().isEmpty() || txtPStock.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(frmparentMenu, "All fields are required!"); return;
            }
            try {
                double price = Double.parseDouble(txtPPrice.getText());
                int stock = Integer.parseInt(txtPStock.getText());
                if (price <= 0 || stock < 0) { JOptionPane.showMessageDialog(frmparentMenu, "Invalid price or stock!"); return; }
                try (Connection conn = getConnection()) {
                    PreparedStatement pst = conn.prepareStatement(
                            "INSERT INTO products(barcode, name, price, stock) VALUES (?, ?, ?, ?)");
                    pst.setString(1, txtBarcode.getText());
                    pst.setString(2, txtPName.getText());
                    pst.setDouble(3, price); pst.setInt(4, stock);
                    pst.executeUpdate();

                    if (pendingPhoto[0] != null) {
                        try (PreparedStatement idPst = conn.prepareStatement(
                                "SELECT id FROM products WHERE name=? ORDER BY id DESC LIMIT 1")) {
                            idPst.setString(1, txtPName.getText());
                            ResultSet idRs = idPst.executeQuery();
                            if (idRs.next()) {
                                int newId = idRs.getInt(1);
                                productImages.put(newId, pendingPhoto[0]);
                                savePhotoToDisk(newId, pendingPhoto[0]);   // <-- PERSIST TO DISK
                            }
                        }
                    }

                    JOptionPane.showMessageDialog(frmparentMenu, "✔  Product Added!");
                    logActivity("Added product: " + txtPName.getText());
                    loadProducts.run(); refreshProducts(cmbProducts, productModel);
                    pendingPhoto[0] = null; lblPhotoPath.setText("No photo selected"); photoPreview.repaint();
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, "Error: " + ex.getMessage()); }
        });

        btnUpdateProd.addActionListener(e -> {
            if (!checkPassword.get()) return;
            int row = prodTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(frmparentMenu, "Select product!"); return; }
            if (txtPName.getText().trim().isEmpty() || txtPPrice.getText().trim().isEmpty() || txtPStock.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(frmparentMenu, "All fields are required!"); return;
            }
            try {
                double price = Double.parseDouble(txtPPrice.getText());
                int stock = Integer.parseInt(txtPStock.getText());
                if (price <= 0 || stock < 0) { JOptionPane.showMessageDialog(frmparentMenu, "Invalid values!"); return; }
                try (Connection conn = getConnection()) {
                    PreparedStatement pst = conn.prepareStatement(
                            "UPDATE products SET name=?, price=?, stock = stock + ? WHERE id=?");
                    pst.setString(1, txtPName.getText()); pst.setDouble(2, price);
                    pst.setInt(3, stock); pst.setInt(4, (int) prodModel.getValueAt(row, 0));
                    pst.executeUpdate();

                    int prodId = (int) prodModel.getValueAt(row, 0);
                    if (pendingPhoto[0] != null) {
                        productImages.put(prodId, pendingPhoto[0]);
                        savePhotoToDisk(prodId, pendingPhoto[0]);   // <-- PERSIST TO DISK
                        Image scaled = pendingPhoto[0].getImage().getScaledInstance(168, 140, Image.SCALE_SMOOTH);
                        rowPhotoImg.setIcon(new ImageIcon(scaled));
                        rowPhotoName.setText(txtPName.getText());
                        rowPhotoPanel.repaint();
                    }

                    JOptionPane.showMessageDialog(frmparentMenu, "✔  Updated!");
                    loadProducts.run(); refreshProducts(cmbProducts, productModel);
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, "Error: " + ex.getMessage()); }
        });

        btnDeleteProd.addActionListener(e -> {
            if (!checkPassword.get()) return;
            int row = prodTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(frmparentMenu, "Select product!"); return; }
            int confirm = JOptionPane.showConfirmDialog(frmparentMenu, "Delete this product?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try (Connection conn = getConnection()) {
                int prodId = (int) prodModel.getValueAt(row, 0);
                PreparedStatement pst = conn.prepareStatement("DELETE FROM products WHERE id=?");
                pst.setInt(1, prodId); pst.executeUpdate();
                productImages.remove(prodId);
                deletePhotoFromDisk(prodId);   // <-- DELETE FROM DISK
                rowPhotoImg.setIcon(null); rowPhotoName.setText("No photo"); rowPhotoPanel.repaint();
                JOptionPane.showMessageDialog(frmparentMenu, "✔  Deleted!");
                loadProducts.run(); refreshProducts(cmbProducts, productModel);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, "Error: " + ex.getMessage()); }
        });

        btnClearProd.addActionListener(e -> {
            txtPName.setText(""); txtPPrice.setText(""); txtPStock.setText(""); txtBarcode.setText("");
            pendingPhoto[0] = null; lblPhotoPath.setText("No photo selected"); photoPreview.repaint();
            prodTable.clearSelection();
            txtPName.requestFocusInWindow();
        });

        // ══════════════════════════════════════════════════════════════════════
        //  CUSTOMERS TAB
        // ══════════════════════════════════════════════════════════════════════
        JPanel customers = makeTab.apply("Customers — Client Records");
        tabbedPane.addTab("Customers", null, customers, null);

        JPanel custFormPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_PANEL);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDER_COLOR);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        custFormPanel.setOpaque(false);
        custFormPanel.setBounds(0, 42, 1020, 130);
        customers.add(custFormPanel);

        JLabel lblCName = styledLabel("Name:");
        lblCName.setBounds(20, 18, 70, 25); custFormPanel.add(lblCName);
        JTextField txtCName = styledField("Customer full name");
        txtCName.setBounds(95, 18, 220, 26);
        txtCName.setToolTipText("Customer full name");
        custFormPanel.add(txtCName);

        JLabel lblCContact = styledLabel("Contact:");
        lblCContact.setBounds(20, 56, 80, 25); custFormPanel.add(lblCContact);
        JTextField txtCContact = styledField("e.g. 09xxxxxxxxx");
        txtCContact.setBounds(95, 56, 220, 26);
        txtCContact.setToolTipText("Customer contact number");
        custFormPanel.add(txtCContact);

        JLabel lblCAddress = styledLabel("Address:");
        lblCAddress.setBounds(20, 94, 80, 25); custFormPanel.add(lblCAddress);
        JTextField txtCAddress = styledField("Full address");
        txtCAddress.setBounds(95, 94, 350, 26);
        txtCAddress.setToolTipText("Customer address");
        custFormPanel.add(txtCAddress);

        JButton btnAddCustomer    = primaryButton("+ Add");
        JButton btnUpdateCustomer = ghostButton("↑ Update");
        JButton btnDeleteCustomer = dangerButton("✖ Delete");

        btnAddCustomer.setBounds(500, 18, 100, 30);
        btnUpdateCustomer.setBounds(612, 18, 108, 30);
        btnDeleteCustomer.setBounds(500, 58, 100, 30);

        btnAddCustomer.setToolTipText("Add new customer");
        btnUpdateCustomer.setToolTipText("Update selected customer");
        btnDeleteCustomer.setToolTipText("Delete selected customer");

        custFormPanel.add(btnAddCustomer); custFormPanel.add(btnUpdateCustomer); custFormPanel.add(btnDeleteCustomer);

        String[] custCols = {"ID", "Name", "Contact", "Address"};
        DefaultTableModel custModel = new DefaultTableModel(custCols, 0);
        JTable custTable = new JTable(custModel);
        custTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        custTable.getColumnModel().getColumn(1).setPreferredWidth(240);
        custTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        custTable.getColumnModel().getColumn(3).setPreferredWidth(460);
        JScrollPane custScroll = styledScroll(custTable);
        custScroll.setBounds(20, 182, 980, 400);
        customers.add(custScroll);

        cmbCustomers = new JComboBox<>();
        styleCombo(cmbCustomers);
        cmbCustomers.setBounds(20, 594, 270, 26);
        customers.add(cmbCustomers);

        custTable.getSelectionModel().addListSelectionListener(e -> {
            int row = custTable.getSelectedRow();
            if (row >= 0) {
                txtCName.setText(custModel.getValueAt(row, 1).toString());
                txtCContact.setText(custModel.getValueAt(row, 2).toString());
                txtCAddress.setText(custModel.getValueAt(row, 3).toString());
            }
        });

        txtCName.addActionListener(e -> txtCContact.requestFocusInWindow());
        txtCContact.addActionListener(e -> txtCAddress.requestFocusInWindow());
        txtCAddress.addActionListener(e -> btnAddCustomer.doClick());

        loadCustomers(custModel, cmbCustomers);

        btnAddCustomer.addActionListener(e -> {
            if (txtCName.getText().trim().isEmpty() || txtCContact.getText().trim().isEmpty() || txtCAddress.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(frmparentMenu, "Fill all fields!"); return;
            }
            try (Connection conn = getConnection()) {
                PreparedStatement pst = conn.prepareStatement(
                        "INSERT INTO customers(name, contact, address) VALUES (?, ?, ?)");
                pst.setString(1, txtCName.getText()); pst.setString(2, txtCContact.getText()); pst.setString(3, txtCAddress.getText());
                pst.executeUpdate();
                JOptionPane.showMessageDialog(frmparentMenu, "✔  Customer Added!");
                logActivity("Added customer: " + txtCName.getText());
                loadCustomers(custModel, cmbCustomers);
                loadCustomersToCombo(cmbCustomersOrders);
                txtCName.setText(""); txtCContact.setText(""); txtCAddress.setText("");
                txtCName.requestFocusInWindow();
            } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, ex.getMessage()); }
        });

        btnUpdateCustomer.addActionListener(e -> {
            int row = custTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(frmparentMenu, "Select customer!"); return; }
            try (Connection conn = getConnection()) {
                PreparedStatement pst = conn.prepareStatement(
                        "UPDATE customers SET name=?, contact=?, address=? WHERE id=?");
                pst.setString(1, txtCName.getText()); pst.setString(2, txtCContact.getText());
                pst.setString(3, txtCAddress.getText()); pst.setInt(4, (int) custModel.getValueAt(row, 0));
                pst.executeUpdate();
                JOptionPane.showMessageDialog(frmparentMenu, "✔  Updated!");
                loadCustomers(custModel, cmbCustomers);
                loadCustomersToCombo(cmbCustomersOrders);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, ex.getMessage()); }
        });

        btnDeleteCustomer.addActionListener(e -> {
            int row = custTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(frmparentMenu, "Select customer!"); return; }
            int confirm = JOptionPane.showConfirmDialog(frmparentMenu, "Delete this customer?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try (Connection conn = getConnection()) {
                PreparedStatement pst = conn.prepareStatement("DELETE FROM customers WHERE id=?");
                pst.setInt(1, (int) custModel.getValueAt(row, 0)); pst.executeUpdate();
                JOptionPane.showMessageDialog(frmparentMenu, "✔  Deleted!");
                loadCustomers(custModel, cmbCustomers);
                loadCustomersToCombo(cmbCustomersOrders);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, ex.getMessage()); }
        });

        // ══════════════════════════════════════════════════════════════════════
        //  REPORTS TAB
        // ══════════════════════════════════════════════════════════════════════
        JPanel reports = makeTab.apply("Reports — Sales Analytics");
        tabbedPane.addTab("Reports", null, reports, null);

        JPanel reportToolbar = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_PANEL);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDER_COLOR);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        reportToolbar.setOpaque(false);
        reportToolbar.setBounds(0, 42, 1020, 62);
        reports.add(reportToolbar);

        JLabel lblStart = styledLabel("From:");
        lblStart.setBounds(16, 18, 56, 25); reportToolbar.add(lblStart);
        JDateChooser startDate = new JDateChooser();
        startDate.setBounds(74, 18, 150, 26);
        startDate.setBackground(BG_INPUT); startDate.setForeground(TEXT_PRIMARY);
        startDate.setToolTipText("Start date for report");
        reportToolbar.add(startDate);

        JLabel lblEnd = styledLabel("To:");
        lblEnd.setBounds(234, 18, 36, 25); reportToolbar.add(lblEnd);
        JDateChooser endDate = new JDateChooser();
        endDate.setBounds(272, 18, 150, 26);
        endDate.setBackground(BG_INPUT); endDate.setForeground(TEXT_PRIMARY);
        endDate.setToolTipText("End date for report");
        reportToolbar.add(endDate);

        String[] types = {"Daily Sales", "Monthly Sales", "Product Sales"};
        JComboBox<String> cmbType = new JComboBox<>(types);
        styleCombo(cmbType);
        cmbType.setBounds(434, 18, 160, 26);
        cmbType.setToolTipText("Select report type");
        reportToolbar.add(cmbType);

        JButton btnGenerate = primaryButton("▶ Generate");
        btnGenerate.setBounds(608, 14, 112, 34);
        btnGenerate.setToolTipText("Generate report");
        reportToolbar.add(btnGenerate);

        JButton btnPrint = ghostButton("⎙ Print");
        btnPrint.setBounds(730, 14, 80, 34);
        btnPrint.setToolTipText("Print report");
        reportToolbar.add(btnPrint);

        JButton btnCSV = ghostButton("CSV");
        btnCSV.setBounds(820, 14, 64, 34);
        btnCSV.setToolTipText("Export to CSV file");
        reportToolbar.add(btnCSV);

        JButton btnDOC = ghostButton("DOC");
        btnDOC.setBounds(894, 14, 64, 34);
        btnDOC.setToolTipText("Export to DOC file");
        reportToolbar.add(btnDOC);

        JLabel lblReportTotal = new JLabel("GRAND TOTAL: ₱ 0.00");
        lblReportTotal.setBounds(20, 112, 500, 24);
        lblReportTotal.setForeground(ACCENT_GOLD);
        lblReportTotal.setFont(new Font("Tahoma", Font.BOLD, 14));
        reports.add(lblReportTotal);

        String[] reportCols = {"Label", "Total"};
        DefaultTableModel reportModel = new DefaultTableModel(reportCols, 0);
        JTable reportTable = new JTable(reportModel);
        reportTable.getColumnModel().getColumn(0).setPreferredWidth(650);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(330);
        JScrollPane reportScroll = styledScroll(reportTable);
        alignRight(reportTable);
        reportScroll.setBounds(20, 144, 980, 430);
        reports.add(reportScroll);

        java.util.function.Supplier<Boolean> checkPasswordReport = () -> {
            String input = JOptionPane.showInputDialog(frmparentMenu, "Enter admin password:");
            return input != null && input.equals("admin123");
        };

        btnGenerate.addActionListener(e -> {
            if (startDate.getDate() == null || endDate.getDate() == null) {
                JOptionPane.showMessageDialog(frmparentMenu, "Select date range!"); return;
            }
            btnGenerate.setEnabled(false);
            try (Connection conn = getConnection()) {
                String type = cmbType.getSelectedItem().toString();
                reportModel.setRowCount(0);
                java.sql.Date start = new java.sql.Date(startDate.getDate().getTime());
                java.sql.Date end   = new java.sql.Date(endDate.getDate().getTime());
                PreparedStatement pst;
                if (type.equals("Daily Sales")) {
                    pst = conn.prepareStatement(
                        "SELECT DATE(order_date) as label, SUM(total_amount) as total FROM orders " +
                        "WHERE DATE(order_date) BETWEEN ? AND ? GROUP BY DATE(order_date)");
                } else if (type.equals("Monthly Sales")) {
                    pst = conn.prepareStatement(
                        "SELECT DATE_FORMAT(order_date,'%Y-%m') as label, SUM(total_amount) as total FROM orders " +
                        "WHERE DATE(order_date) BETWEEN ? AND ? GROUP BY DATE_FORMAT(order_date,'%Y-%m')");
                } else {
                    pst = conn.prepareStatement(
                        "SELECT product as label, SUM(subtotal) as total FROM order_details od " +
                        "JOIN orders o ON od.order_id = o.id " +
                        "WHERE DATE(o.order_date) BETWEEN ? AND ? GROUP BY product");
                }
                pst.setDate(1, start); pst.setDate(2, end);
                ResultSet rs = pst.executeQuery();
                double grandTotal = 0;
                while (rs.next()) {
                    double val = rs.getDouble("total");
                    grandTotal += val;
                    reportModel.addRow(new Object[]{rs.getString("label"), String.format("₱ %.2f", val)});
                }
                lblReportTotal.setText("GRAND TOTAL:  ₱ " + String.format("%.2f", grandTotal));
            } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, ex.getMessage()); }
            finally { btnGenerate.setEnabled(true); }
        });

        btnPrint.addActionListener(e -> {
            try { reportTable.print(); } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, "Print failed!"); }
        });

        btnCSV.addActionListener(e -> {
            if (!checkPasswordReport.get()) return;
            try {
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(frmparentMenu) == JFileChooser.APPROVE_OPTION) {
                    java.io.FileWriter fw = new java.io.FileWriter(fc.getSelectedFile() + ".csv");
                    for (int i = 0; i < reportModel.getColumnCount(); i++) fw.write(reportModel.getColumnName(i) + ",");
                    fw.write("\n");
                    for (int i = 0; i < reportModel.getRowCount(); i++) {
                        for (int j = 0; j < reportModel.getColumnCount(); j++) fw.write(reportModel.getValueAt(i, j).toString() + ",");
                        fw.write("\n");
                    }
                    fw.close();
                    JOptionPane.showMessageDialog(frmparentMenu, "✔  CSV Exported!");
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, "Export failed!"); }
        });

        btnDOC.addActionListener(e -> {
            if (!checkPasswordReport.get()) return;
            try {
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(frmparentMenu) == JFileChooser.APPROVE_OPTION) {
                    java.io.FileWriter fw = new java.io.FileWriter(fc.getSelectedFile() + ".doc");
                    fw.write("REPORT\n\n");
                    for (int i = 0; i < reportModel.getRowCount(); i++)
                        fw.write(reportModel.getValueAt(i, 0) + " - " + reportModel.getValueAt(i, 1) + "\n");
                    fw.write("\nTOTAL: " + lblReportTotal.getText());
                    fw.close();
                    JOptionPane.showMessageDialog(frmparentMenu, "✔  DOC Exported!");
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frmparentMenu, "Export failed!"); }
        });

        // Logout tab
        JPanel logout = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        logout.setOpaque(true);
        tabbedPane.addTab("Logout", null, logout, null);

        // Nav wiring
        btnHome.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        btnOrders.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        btnProducts.addActionListener(e -> {
            if ("admin".equalsIgnoreCase(Session.role)) {
                tabbedPane.setSelectedIndex(2);
            } else {
                JOptionPane.showMessageDialog(frmparentMenu, "Access denied! Admin privileges required.");
            }
        });
        btnCustomers.addActionListener(e -> tabbedPane.setSelectedIndex(3));
        btnReports.addActionListener(e -> tabbedPane.setSelectedIndex(4));

        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        frmparentMenu,
                        "Are you sure you want to logout?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Session.clear();
                    JOptionPane.showMessageDialog(frmparentMenu, "You have been logged out successfully.");
                    frmparentMenu.dispose();
                    login loginWindow = new login();
                    loginWindow.showWindow();
                }
            }
        });

        // Role-based access control
        if ("customer".equalsIgnoreCase(Session.role)) {
            btnProducts.setEnabled(false);
            btnCustomers.setEnabled(false);
            btnReports.setEnabled(false);
            tabbedPane.setEnabledAt(2, false);
            tabbedPane.setEnabledAt(3, false);
            tabbedPane.setEnabledAt(4, false);
        }
    }

    public void showWindow() {
        frmparentMenu.setVisible(true);
    }
}