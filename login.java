package comm;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.*;
import java.sql.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import comm.Session;

/**
 * LOGIN — Styled Version (Design from Document 3 applied to Document 1 logic)
 * All original logic preserved. Only UI/UX design updated.
 */
public class login {

    private JFrame frmloginwindow;

    // LOGIN
    private JTextField txtUSERNAME;
    private JPasswordField txtPASSWORD;
    private JCheckBox showPassword;
    private JComboBox<String> cmbRole;

    // REGISTER
    private JTextField regUsername;
    private JPasswordField regPassword;
    private JTextField regMother;
    private JTextField regColor;
    private JTextField regBirthday;
    private JComboBox<String> regRole;

    // FORGOT
    private JTextField forgotUsername;
    private JTextField ansMother;
    private JTextField ansColor;
    private JTextField ansBirthday;

    private JTabbedPane jTabbedPane;
    private int attempts = 0;

    // ERROR LABELS
    private JLabel loginError, regError, forgotError;

    // ─── THEME CONSTANTS ──────────────────────────────────────────────────────
    private static final Color COL_STEEL_DARK  = new Color(10, 12, 18);
    private static final Color COL_STEEL_MID   = new Color(18, 22, 30);
    private static final Color COL_STEEL_LIGHT = new Color(38, 46, 58);
    private static final Color COL_STEEL_HOVER = new Color(52, 64, 80);
    private static final Color COL_YELLOW      = new Color(212, 160, 60);
    private static final Color COL_YELLOW_BRIGHT = new Color(255, 185, 0);
    private static final Color COL_YELLOW_DIM  = new Color(140, 104, 30);
    private static final Color COL_TEXT_MAIN   = new Color(245, 242, 230);
    private static final Color COL_TEXT_DIM    = new Color(160, 155, 140);
    private static final Color COL_FIELD_BG    = new Color(22, 28, 38);
    private static final Color COL_BORDER      = new Color(42, 50, 64);
    private static final Color COL_ERROR       = new Color(220, 70, 60);
    private static final Color COL_SUCCESS     = new Color(60, 180, 100);

    // ─── FONTS ────────────────────────────────────────────────────────────────
    private static final Font FONT_HEADER = new Font("Tahoma", Font.BOLD, 17);
    private static final Font FONT_TITLE  = new Font("Tahoma", Font.BOLD, 13);
    private static final Font FONT_LABEL  = new Font("Tahoma", Font.BOLD, 12);
    private static final Font FONT_FIELD  = new Font("Tahoma", Font.PLAIN, 13);
    private static final Font FONT_BUTTON = new Font("Tahoma", Font.BOLD, 12);
    private static final Font FONT_SIDE   = new Font("Tahoma", Font.BOLD, 11);
    private static final Font FONT_SMALL  = new Font("Tahoma", Font.PLAIN, 10);

    // ══════════════════════════════════════════════════════════════════════════
    //  ORIGINAL SECURITY & DB LOGIC — UNCHANGED
    // ══════════════════════════════════════════════════════════════════════════

    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Connection connect() {
        try {
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/login_system?useSSL=false&serverTimezone=UTC",
                    "root", "");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database Connection Failed!");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                login window = new login();
                window.frmloginwindow.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public login() { initialize(); }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI INITIALISATION
    // ══════════════════════════════════════════════════════════════════════════

    private void initialize() {
    	
    	
    	
        // ── Frame ──────────────────────────────────────────────────────────────
        frmloginwindow = new JFrame();
        frmloginwindow.setTitle("Samson's BuildWorks & Infra Solutions — Portal");
        frmloginwindow.setBounds(100, 100, 900, 560);
        frmloginwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmloginwindow.getContentPane().setBackground(COL_STEEL_DARK);
        frmloginwindow.getContentPane().setLayout(null);
        frmloginwindow.setLocationRelativeTo(null); // center on screen

        // ── Sidebar ────────────────────────────────────────────────────────────
        JPanel sidebar = createSidebar();
        sidebar.setBounds(0, 0, 195, 540);
        frmloginwindow.getContentPane().add(sidebar);

        // ── Content area ────────────────────────────────────────────────────────
        jTabbedPane = buildStyledTabbedPane();
        jTabbedPane.setBounds(195, 0, 705, 540);
        frmloginwindow.getContentPane().add(jTabbedPane);

        // ── Tabs ───────────────────────────────────────────────────────────────
        jTabbedPane.addTab("Sign In",         buildLoginPanel());
        jTabbedPane.addTab("Create Account",  buildRegisterPanel());
        jTabbedPane.addTab("Forgot Password", buildForgotPanel());

        // ── Side nav buttons ───────────────────────────────────────────────────
        sidebar.add(createSideButton("▶  SIGN IN",          130, e -> {
            jTabbedPane.setSelectedIndex(0);
            SwingUtilities.invokeLater(() -> txtUSERNAME.requestFocusInWindow());
        }));
        sidebar.add(createSideButton("▶  CREATE ACCOUNT",   178, e -> {
            jTabbedPane.setSelectedIndex(1);
            SwingUtilities.invokeLater(() -> regUsername.requestFocusInWindow());
        }));
        sidebar.add(createSideButton("▶  FORGOT PASSWORD",  226, e -> {
            jTabbedPane.setSelectedIndex(2);
            SwingUtilities.invokeLater(() -> forgotUsername.requestFocusInWindow());
        }));

        SwingUtilities.invokeLater(() -> txtUSERNAME.requestFocusInWindow());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel createSidebar() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // dark background gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(14, 18, 26), 0, getHeight(), new Color(8, 10, 16));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // gold accent bar on right edge
                g2.setColor(COL_YELLOW);
                g2.fillRect(getWidth() - 3, 0, 3, getHeight());

                // subtle grid overlay
                g2.setColor(new Color(212, 160, 60, 12));
                for (int y = 0; y < getHeight(); y += 24) g2.drawLine(0, y, getWidth(), y);
                for (int x = 0; x < getWidth(); x += 24) g2.drawLine(x, 0, x, getHeight());

                // hazard stripe band at bottom
                int bandH = 48;
                int bY = getHeight() - bandH;
                int sw = 18;
                for (int x = -bandH; x < getWidth() + bandH; x += sw * 2) {
                    int[] xs = {x, x + sw, x + sw + bandH, x + bandH};
                    int[] ys = {getHeight(), getHeight(), bY, bY};
                    g2.setColor(COL_YELLOW);
                    g2.fillPolygon(xs, ys, 4);
                    int[] xs2 = {x + sw, x + sw * 2, x + sw * 2 + bandH, x + sw + bandH};
                    g2.setColor(COL_STEEL_DARK);
                    g2.fillPolygon(xs2, ys, 4);
                }

                g2.dispose();
            }
        };
        panel.setLayout(null);
        panel.setOpaque(false);

        // Logo box
        JPanel logoBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COL_YELLOW);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        logoBox.setLayout(null);
        logoBox.setOpaque(false);
        logoBox.setBounds(16, 18, 162, 86);
        panel.add(logoBox);

        JLabel lbl1 = new JLabel("SAMSON'S");
        lbl1.setFont(new Font("Tahoma", Font.BOLD, 15));
        lbl1.setForeground(COL_STEEL_DARK);
        lbl1.setBounds(10, 10, 145, 22);
        logoBox.add(lbl1);

        JLabel lbl2 = new JLabel("BUILDWORKS");
        lbl2.setFont(new Font("Tahoma", Font.BOLD, 18));
        lbl2.setForeground(COL_STEEL_DARK);
        lbl2.setBounds(10, 30, 145, 24);
        logoBox.add(lbl2);

        JLabel lbl3 = new JLabel("& INFRA SOLUTIONS");
        lbl3.setFont(new Font("Tahoma", Font.PLAIN, 9));
        lbl3.setForeground(new Color(30, 30, 30));
        lbl3.setBounds(10, 56, 145, 16);
        logoBox.add(lbl3);

        // Section title
        JLabel lblNav = new JLabel("NAVIGATION");
        lblNav.setFont(new Font("Tahoma", Font.BOLD, 9));
        lblNav.setForeground(new Color(100, 96, 86));
        lblNav.setBounds(16, 112, 160, 16);
        panel.add(lblNav);

        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TABBED PANE (hidden tabs — nav controls selection)
    // ══════════════════════════════════════════════════════════════════════════

    private JTabbedPane buildStyledTabbedPane() {
        JTabbedPane tp = new JTabbedPane() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(16, 20, 28), getWidth(), getHeight(), new Color(10, 13, 20));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // blueprint grid
                g2.setColor(new Color(212, 160, 60, 8));
                for (int y = 0; y < getHeight(); y += 28) g2.drawLine(0, y, getWidth(), y);
                for (int x = 0; x < getWidth(); x += 28) g2.drawLine(x, 0, x, getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tp.setOpaque(true);
        tp.setBackground(COL_STEEL_MID);
        tp.setForeground(COL_TEXT_DIM);
        tp.setFont(FONT_SIDE);

        // Hide tab headers — navigation is via sidebar buttons
        UIManager.put("TabbedPane.tabAreaBackground",   COL_STEEL_DARK);
        UIManager.put("TabbedPane.selected",            COL_STEEL_MID);
        UIManager.put("TabbedPane.background",          COL_STEEL_DARK);
        UIManager.put("TabbedPane.foreground",          COL_TEXT_DIM);
        UIManager.put("TabbedPane.contentAreaColor",    COL_STEEL_MID);

        return tp;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOGIN PANEL
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildLoginPanel() {
        JPanel loginPanel = createTabPanel();

        // Header
        loginPanel.add(makeSectionHeader("[ EMPLOYEE SIGN IN ]", 30, 24));
        loginPanel.add(makeYellowDivider(30, 58, 590));

        JLabel sub = new JLabel("Authorized Personnel Only  ·  Site Access Required");
        sub.setFont(FONT_SMALL);
        sub.setForeground(COL_TEXT_DIM);
        sub.setBounds(30, 64, 400, 16);
        loginPanel.add(sub);

        // Welcome
        JLabel welcome = new JLabel("Welcome back! Please sign in to continue.");
        welcome.setFont(new Font("Tahoma", Font.ITALIC, 11));
        welcome.setForeground(new Color(160, 155, 140));
        welcome.setBounds(30, 84, 400, 16);
        loginPanel.add(welcome);

        // USERNAME
        loginPanel.add(makeLabel("USERNAME:", 30, 118));
        txtUSERNAME = makeTextField("Enter your username");
        txtUSERNAME.setBounds(190, 114, 290, 32);
        loginPanel.add(txtUSERNAME);

        // PASSWORD
        loginPanel.add(makeLabel("PASSWORD:", 30, 162));
        txtPASSWORD = makePasswordField("Enter your password");
        txtPASSWORD.setBounds(190, 158, 290, 32);
        loginPanel.add(txtPASSWORD);

        // ROLE
        loginPanel.add(makeLabel("ROLE:", 30, 206));
        cmbRole = makeStyledCombo(new String[]{"customer", "admin"});
        cmbRole.setBounds(190, 202, 290, 32);
        cmbRole.setToolTipText("Select your account role");
        loginPanel.add(cmbRole);

        // Show Password
        showPassword = new JCheckBox("Show Password");
        showPassword.setFont(FONT_SMALL);
        showPassword.setForeground(COL_TEXT_DIM);
        showPassword.setBackground(new Color(0, 0, 0, 0));
        showPassword.setOpaque(false);
        showPassword.setBounds(190, 240, 160, 22);
        showPassword.setToolTipText("Toggle password visibility");
        loginPanel.add(showPassword);

        showPassword.addActionListener(e -> {
            txtPASSWORD.setEchoChar(showPassword.isSelected() ? (char) 0 : '•');
        });

        // Buttons
        JButton btnLogin = makePrimaryButton("⬛  LOGIN");
        btnLogin.setBounds(190, 274, 140, 36);
        btnLogin.setToolTipText("Click to sign in");
        loginPanel.add(btnLogin);

        JButton btnClear = makeSecondaryButton("CLEAR");
        btnClear.setBounds(340, 274, 110, 36);
        btnClear.setToolTipText("Clear all fields");
        loginPanel.add(btnClear);

        // Error label
        loginError = makeErrorLabel(190, 320);
        loginPanel.add(loginError);

        // Warning stripe decoration
        loginPanel.add(makeWarnStripe(30, 388, 590, 18));

        // Keyboard: Enter on password fires login
        txtPASSWORD.addActionListener(e -> btnLogin.doClick());
        txtUSERNAME.addActionListener(e -> txtPASSWORD.requestFocusInWindow());

        // ── CLEAR LOGIC (UNCHANGED) ────────────────────────────────────────────
        btnClear.addActionListener(e -> {
            txtUSERNAME.setText("");
            txtPASSWORD.setText("");
            loginError.setText("");
            txtUSERNAME.requestFocusInWindow();
            cmbRole.setSelectedIndex(0);
            resetFieldBorder(txtUSERNAME);
            resetFieldBorder(txtPASSWORD);
        });

        // ── LOGIN LOGIC (UNCHANGED) ────────────────────────────────────────────
        btnLogin.addActionListener(e -> {

            loginError.setText("");
            resetFieldBorder(txtUSERNAME);
            resetFieldBorder(txtPASSWORD);

            btnLogin.setEnabled(false);

            if (txtUSERNAME.getText().isEmpty() || txtPASSWORD.getPassword().length == 0) {
                loginError.setText("⚠  All fields are required!");
                if (txtUSERNAME.getText().isEmpty()) highlightField(txtUSERNAME);
                if (txtPASSWORD.getPassword().length == 0) highlightField(txtPASSWORD);
                btnLogin.setEnabled(true);
                return;
            }

            if (!txtUSERNAME.getText().matches("^[a-zA-Z0-9@!._-]+$")) {
                loginError.setText("⚠  Invalid username format!");
                highlightField(txtUSERNAME);
                btnLogin.setEnabled(true);
                return;
            }

            Connection con = connect();
            if (con == null) {
                btnLogin.setEnabled(true);
                return;
            }

            try {
                String sql = "SELECT * FROM users WHERE username=?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, txtUSERNAME.getText());
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    String enteredPassword = hashPassword(String.valueOf(txtPASSWORD.getPassword()));
                    String storedPassword  = rs.getString("password");
                    String selectedRole    = cmbRole.getSelectedItem().toString();
                    String dbRole          = rs.getString("role");

                    if (enteredPassword.equals(storedPassword) && selectedRole.equalsIgnoreCase(dbRole)) {
                        Session.username = txtUSERNAME.getText();
                        Session.role     = rs.getString("role");
                        JOptionPane.showMessageDialog(null,
                                "Login Successful!\nWelcome " + dbRole.toUpperCase() + "!");
                        txtUSERNAME.setText("");
                        txtPASSWORD.setText("");
                        frmloginwindow.dispose();
                        OrderingSystemMenu menu = new OrderingSystemMenu();
                        menu.showWindow();
                    } else {
                        attempts++;
                        JOptionPane.showMessageDialog(null,
                                "Invalid credentials! Attempt: " + attempts);
                        txtUSERNAME.setText("");
                        txtPASSWORD.setText("");
                        txtUSERNAME.requestFocusInWindow();
                        highlightField(txtUSERNAME);
                        highlightField(txtPASSWORD);
                        if (attempts >= 3) {
                            txtUSERNAME.setEnabled(false);
                            txtPASSWORD.setEnabled(false);
                            btnLogin.setEnabled(false);
                            loginError.setText("⚠  Too many failed attempts. Account locked.");
                            JOptionPane.showMessageDialog(null, "Too many failed attempts! Account locked.");
                        }
                    }
                } else {
                    loginError.setText("⚠  Username not found.");
                    highlightField(txtUSERNAME);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                loginError.setText("⚠  Database error. Try again.");
            }

            btnLogin.setEnabled(true);
        });

        return loginPanel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REGISTER PANEL
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildRegisterPanel() {
        JPanel registerPanel = createTabPanel();

        registerPanel.add(makeSectionHeader("[ CREATE ACCOUNT ]", 30, 24));
        registerPanel.add(makeYellowDivider(30, 58, 590));

        JLabel sub = new JLabel("Fill all fields to create your account.");
        sub.setFont(FONT_SMALL);
        sub.setForeground(COL_TEXT_DIM);
        sub.setBounds(30, 64, 400, 16);
        registerPanel.add(sub);

        regUsername = makeTextField("Choose a username");
        regPassword = makePasswordField("Min 8 chars, upper, lower, number, symbol");
        regMother   = makeTextField("Mother's maiden name");
        regColor    = makeTextField("Your favorite color");
        regBirthday = makeTextField("YYYY-MM-DD");
        regRole     = makeStyledCombo(new String[]{"customer", "admin"});

        regUsername.setToolTipText("Alphanumeric, @, !, ., _, - allowed");
        regPassword.setToolTipText("Min 8 chars with upper, lowercase, digit, special character");
        regBirthday.setToolTipText("Format: YYYY-MM-DD");
        regRole.setToolTipText("Select account role");

        String[]     labels = {"USERNAME:", "PASSWORD:", "MOTHER'S NAME:", "FAVORITE COLOR:", "BIRTHDAY:", "ROLE:"};
        JComponent[] fields = {regUsername, regPassword, regMother, regColor, regBirthday, regRole};

        for (int i = 0; i < labels.length; i++) {
            registerPanel.add(makeLabel(labels[i], 30, 90 + i * 46));
            fields[i].setBounds(210, 86 + i * 46, 290, 32);
            registerPanel.add(fields[i]);
        }

        JLabel hint = new JLabel("Format: YYYY-MM-DD");
        hint.setFont(new Font("Tahoma", Font.PLAIN, 9));
        hint.setForeground(COL_TEXT_DIM);
        hint.setBounds(506, 316, 110, 16);
        registerPanel.add(hint);

        JButton btnRegister = makePrimaryButton("⬛  REGISTER");
        btnRegister.setBounds(210, 366, 150, 36);
        btnRegister.setToolTipText("Create new account");
        registerPanel.add(btnRegister);

        JButton btnRegClear = makeSecondaryButton("CLEAR");
        btnRegClear.setBounds(372, 366, 110, 36);
        btnRegClear.setToolTipText("Clear all fields");
        registerPanel.add(btnRegClear);

        regError = makeErrorLabel(210, 412);
        registerPanel.add(regError);

        registerPanel.add(makeWarnStripe(30, 458, 590, 18));

        // Keyboard navigation
        regUsername.addActionListener(e -> regPassword.requestFocusInWindow());
        regPassword.addActionListener(e -> regMother.requestFocusInWindow());
        regMother.addActionListener(e -> regColor.requestFocusInWindow());
        regColor.addActionListener(e -> regBirthday.requestFocusInWindow());
        regBirthday.addActionListener(e -> btnRegister.doClick());

        // ── CLEAR ─────────────────────────────────────────────────────────────
        btnRegClear.addActionListener(e -> {
            regUsername.setText(""); regPassword.setText("");
            regMother.setText("");   regColor.setText("");
            regBirthday.setText(""); regError.setText("");
            regRole.setSelectedIndex(0);
            resetFieldBorder(regUsername); resetFieldBorder(regPassword);
            resetFieldBorder(regMother);   resetFieldBorder(regColor);
            resetFieldBorder(regBirthday);
            regUsername.requestFocusInWindow();
        });

        // ── REGISTER LOGIC (UNCHANGED) ────────────────────────────────────────
        btnRegister.addActionListener(e -> {
            regError.setText("");
            resetFieldBorder(regUsername); resetFieldBorder(regPassword);
            resetFieldBorder(regMother);   resetFieldBorder(regColor);
            resetFieldBorder(regBirthday);

            boolean hasError = false;
            if (regUsername.getText().isEmpty()) { highlightField(regUsername); hasError = true; }
            if (regPassword.getPassword().length == 0) { highlightField(regPassword); hasError = true; }
            if (regMother.getText().isEmpty()) { highlightField(regMother); hasError = true; }
            if (regColor.getText().isEmpty()) { highlightField(regColor); hasError = true; }
            if (regBirthday.getText().isEmpty()) { highlightField(regBirthday); hasError = true; }
            if (hasError) { regError.setText("⚠  Please fill all fields!"); return; }

            String username = regUsername.getText();
            String password = String.valueOf(regPassword.getPassword());

            if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-]).{8,}$")) {
                regError.setText("⚠  Weak password! Use upper, lower, digit & symbol.");
                highlightField(regPassword);
                return;
            }

            if (!regBirthday.getText().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                regError.setText("⚠  Birthday must be YYYY-MM-DD!");
                highlightField(regBirthday);
                return;
            }

            if (!username.matches("^[a-zA-Z0-9@!._-]+$")) {
                regError.setText("⚠  Invalid username format!");
                highlightField(regUsername);
                return;
            }

            Connection con = connect();
            if (con == null) return;

            try {
                String checkSql = "SELECT * FROM users WHERE username=?";
                PreparedStatement checkPst = con.prepareStatement(checkSql);
                checkPst.setString(1, username);
                ResultSet checkRs = checkPst.executeQuery();

                if (checkRs.next()) {
                    regError.setText("⚠  Username already exists!");
                    highlightField(regUsername);
                    return;
                }

                String sql = "INSERT INTO users (username, password, mother_name, favorite_color, birthday, role) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, username);
                pst.setString(2, hashPassword(password));
                pst.setString(3, regMother.getText());
                pst.setString(4, regColor.getText());
                pst.setString(5, regBirthday.getText());
                pst.setString(6, regRole.getSelectedItem().toString());
                pst.executeUpdate();

                JOptionPane.showMessageDialog(null, "✔  Account Created Successfully!");
                regUsername.setText(""); regPassword.setText("");
                regMother.setText("");   regColor.setText("");
                regBirthday.setText(""); regRole.setSelectedIndex(0);

            } catch (Exception ex) {
                ex.printStackTrace();
                regError.setText("⚠  Database error. Try again.");
            }
        });

        return registerPanel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FORGOT PASSWORD PANEL
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildForgotPanel() {
        JPanel forgotPanel = createTabPanel();

        forgotPanel.add(makeSectionHeader("[ IDENTITY VERIFICATION ]", 30, 24));
        forgotPanel.add(makeYellowDivider(30, 58, 590));

        JLabel info = new JLabel("Answer your security questions to recover access.");
        info.setFont(FONT_SMALL);
        info.setForeground(COL_TEXT_DIM);
        info.setBounds(30, 64, 500, 16);
        forgotPanel.add(info);

        forgotUsername = makeTextField("Enter your username");
        ansMother      = makeTextField("Your mother's name");
        ansColor       = makeTextField("Your favorite color");
        ansBirthday    = makeTextField("YYYY-MM-DD");

        forgotUsername.setToolTipText("Enter your registered username");
        ansMother.setToolTipText("Enter your mother's maiden name");
        ansColor.setToolTipText("Enter your favorite color");
        ansBirthday.setToolTipText("Format: YYYY-MM-DD");

        String[]     labels = {"USERNAME:", "MOTHER'S NAME:", "FAVORITE COLOR:", "BIRTHDAY:"};
        JTextField[] fields = {forgotUsername, ansMother, ansColor, ansBirthday};

        for (int i = 0; i < labels.length; i++) {
            forgotPanel.add(makeLabel(labels[i], 30, 100 + i * 52));
            fields[i].setBounds(210, 96 + i * 52, 290, 32);
            forgotPanel.add(fields[i]);
        }

        JLabel hint = new JLabel("Format: YYYY-MM-DD");
        hint.setFont(new Font("Tahoma", Font.PLAIN, 9));
        hint.setForeground(COL_TEXT_DIM);
        hint.setBounds(506, 304, 110, 16);
        forgotPanel.add(hint);

        JButton btnVerify = makePrimaryButton("⬛  VERIFY");
        btnVerify.setBounds(210, 316, 150, 36);
        btnVerify.setToolTipText("Verify your identity to reset password");
        forgotPanel.add(btnVerify);

        forgotError = makeErrorLabel(210, 362);
        forgotPanel.add(forgotError);

        forgotPanel.add(makeWarnStripe(30, 440, 590, 18));

        // Keyboard navigation
        forgotUsername.addActionListener(e -> ansMother.requestFocusInWindow());
        ansMother.addActionListener(e -> ansColor.requestFocusInWindow());
        ansColor.addActionListener(e -> ansBirthday.requestFocusInWindow());
        ansBirthday.addActionListener(e -> btnVerify.doClick());

        // ── VERIFY LOGIC (UNCHANGED) ──────────────────────────────────────────
        btnVerify.addActionListener(e -> {
            forgotError.setText("");
            resetFieldBorder(forgotUsername); resetFieldBorder(ansMother);
            resetFieldBorder(ansColor);       resetFieldBorder(ansBirthday);

            boolean hasError = false;
            if (forgotUsername.getText().isEmpty()) { highlightField(forgotUsername); hasError = true; }
            if (ansMother.getText().isEmpty())      { highlightField(ansMother);      hasError = true; }
            if (ansColor.getText().isEmpty())        { highlightField(ansColor);        hasError = true; }
            if (ansBirthday.getText().isEmpty())     { highlightField(ansBirthday);     hasError = true; }
            if (hasError) { forgotError.setText("⚠  All fields are required!"); return; }

            if (!ansBirthday.getText().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                forgotError.setText("⚠  Birthday must be YYYY-MM-DD!");
                highlightField(ansBirthday);
                return;
            }

            Connection con = connect();
            if (con == null) return;

            try {
                String sql = "SELECT * FROM users WHERE username=?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, forgotUsername.getText());
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    if (rs.getString("mother_name").equalsIgnoreCase(ansMother.getText()) &&
                        rs.getString("favorite_color").equalsIgnoreCase(ansColor.getText()) &&
                        rs.getString("birthday").equals(ansBirthday.getText())) {

                        String newPassword = JOptionPane.showInputDialog(frmloginwindow, "Enter new password:");
                        if (newPassword != null && !newPassword.isEmpty()) {
                            PreparedStatement updatePst = con.prepareStatement(
                                    "UPDATE users SET password=? WHERE username=?");
                            updatePst.setString(1, hashPassword(newPassword));
                            updatePst.setString(2, forgotUsername.getText());
                            updatePst.executeUpdate();
                            JOptionPane.showMessageDialog(null, "✔  Password reset successful!");
                            forgotUsername.setText(""); ansMother.setText("");
                            ansColor.setText("");       ansBirthday.setText("");
                        }

                    } else {
                        forgotError.setText("⚠  Incorrect answers. Please try again.");
                        JOptionPane.showMessageDialog(null, "Incorrect Answers!");
                    }
                } else {
                    forgotError.setText("⚠  User not found.");
                    highlightField(forgotUsername);
                    JOptionPane.showMessageDialog(null, "User not found!");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                forgotError.setText("⚠  Database error. Try again.");
            }
        });

        return forgotPanel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPER — Tab Panel base
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel createTabPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(16, 20, 28), getWidth(), getHeight(), new Color(10, 13, 20));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // blueprint grid
                g2.setColor(new Color(212, 160, 60, 8));
                for (int y = 0; y < getHeight(); y += 28) g2.drawLine(0, y, getWidth(), y);
                for (int x = 0; x < getWidth(); x += 28) g2.drawLine(x, 0, x, getHeight());

                // corner brackets
                g2.setColor(COL_YELLOW);
                int cs = 18;
                g2.drawLine(10, 10, 10 + cs, 10); g2.drawLine(10, 10, 10, 10 + cs);
                g2.drawLine(getWidth() - 10 - cs, 10, getWidth() - 10, 10); g2.drawLine(getWidth() - 10, 10, getWidth() - 10, 10 + cs);
                g2.drawLine(10, getHeight() - 10, 10 + cs, getHeight() - 10); g2.drawLine(10, getHeight() - 10, 10, getHeight() - 10 - cs);
                g2.drawLine(getWidth() - 10 - cs, getHeight() - 10, getWidth() - 10, getHeight() - 10); g2.drawLine(getWidth() - 10, getHeight() - 10, getWidth() - 10, getHeight() - 10 - cs);

                g2.dispose();
            }
        };
        p.setLayout(null);
        p.setOpaque(true);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FIELD HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private void highlightField(JComponent field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_ERROR, 2),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    private void resetFieldBorder(JComponent field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  WIDGET FACTORIES
    // ══════════════════════════════════════════════════════════════════════════

    private JLabel makeSectionHeader(String text, int x, int y) {
        JLabel lbl = new JLabel("[SIGN IN ]");
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(COL_YELLOW);
        lbl.setBounds(x, y, 500, 28);
        return lbl;
    }

    private JPanel makeYellowDivider(int x, int y, int width) {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(COL_YELLOW);
                g.fillRect(0, 0, getWidth(), 2);
                g.setColor(COL_YELLOW_DIM);
                g.fillRect(0, 2, getWidth(), 1);
            }
        };
        d.setOpaque(false);
        d.setBounds(x, y, width, 3);
        return d;
    }

    private JPanel makeWarnStripe(int x, int y, int width, int height) {
        JPanel stripe = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int sw = 22;
                for (int i = -getHeight(); i < getWidth() + getHeight(); i += sw * 2) {
                    int[] xs  = {i, i + sw, i + sw + getHeight(), i + getHeight()};
                    int[] ys  = {getHeight(), getHeight(), 0, 0};
                    g2.setColor(COL_YELLOW);
                    g2.fillPolygon(xs, ys, 4);
                    int[] xs2 = {i + sw, i + sw * 2, i + sw * 2 + getHeight(), i + sw + getHeight()};
                    g2.setColor(COL_STEEL_DARK);
                    g2.fillPolygon(xs2, ys, 4);
                }
                g2.setColor(COL_YELLOW_DIM);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
        stripe.setOpaque(false);
        stripe.setBounds(x, y, width, height);
        return stripe;
    }

    private JLabel makeLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COL_TEXT_DIM);
        lbl.setBounds(x, y, 170, 30);
        return lbl;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField();
        styleInputComponent(tf);
        // Placeholder simulation
        tf.setForeground(COL_TEXT_DIM);
        tf.setText(placeholder);
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(COL_TEXT_MAIN);
                }
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(COL_YELLOW, 1),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(COL_TEXT_DIM);
                }
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(COL_BORDER, 1),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            }
        });
        return tf;
    }

    // Overload for cases where we need to get text safely (ignoring placeholder)
    private String getFieldText(JTextField field, String placeholder) {
        String t = field.getText();
        return t.equals(placeholder) ? "" : t;
    }

    private JPasswordField makePasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField();
        styleInputComponent(pf);
        pf.setEchoChar('•');
        pf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(COL_YELLOW, 1),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(COL_BORDER, 1),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            }
        });
        return pf;
    }

    private void styleInputComponent(JTextField tf) {
        tf.setBackground(COL_FIELD_BG);
        tf.setForeground(COL_TEXT_MAIN);
        tf.setCaretColor(COL_YELLOW);
        tf.setFont(FONT_FIELD);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COL_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    private JComboBox<String> makeStyledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setBackground(COL_FIELD_BG);
        c.setForeground(COL_TEXT_MAIN);
        c.setFont(FONT_FIELD);
        c.setBorder(new LineBorder(COL_BORDER, 1));
        c.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? COL_YELLOW : COL_FIELD_BG);
                setForeground(isSelected ? COL_STEEL_DARK : COL_TEXT_MAIN);
                setFont(FONT_FIELD);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        return c;
    }

    private JButton makePrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()   ? COL_YELLOW_DIM
                         : getModel().isRollover()  ? COL_YELLOW_BRIGHT
                         : isEnabled()              ? COL_YELLOW
                         :                            new Color(80, 72, 40);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setFont(FONT_BUTTON);
                g2.setColor(isEnabled() ? COL_STEEL_DARK : new Color(100, 96, 80));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(FONT_BUTTON);
        return btn;
    }

    private JButton makeSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() || getModel().isPressed() ? COL_STEEL_HOVER : COL_STEEL_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(COL_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.setFont(FONT_BUTTON);
                g2.setColor(COL_TEXT_DIM);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(FONT_BUTTON);
        return btn;
    }

    private JLabel makeErrorLabel(int x, int y) {
        JLabel lbl = new JLabel("");
        lbl.setFont(new Font("Tahoma", Font.BOLD, 11));
        lbl.setForeground(COL_ERROR);
        lbl.setBounds(x, y, 440, 22);
        return lbl;
    }

    private JButton createSideButton(String text, int y, ActionListener action) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (hovered || getModel().isPressed()) {
                    g2.setColor(COL_YELLOW);
                    g2.fillRect(0, 0, 4, getHeight());
                    g2.setColor(new Color(212, 160, 60, 28));
                    g2.fillRect(4, 0, getWidth() - 4, getHeight());
                    g2.setColor(COL_YELLOW);
                } else {
                    g2.setColor(COL_STEEL_LIGHT);
                    g2.fillRect(0, 0, 2, getHeight());
                    g2.setColor(COL_TEXT_DIM);
                }
                g2.setFont(FONT_SIDE);
                FontMetrics fm = g2.getFontMetrics();
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), 14, ty);
                g2.dispose();
            }
        };
        btn.setBounds(0, y, 192, 36);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(FONT_SIDE);
        btn.addActionListener(action);
        return btn;
    }

    public void showWindow() {
        frmloginwindow.setVisible(true);
    }
}