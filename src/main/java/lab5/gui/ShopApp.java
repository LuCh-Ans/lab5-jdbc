package lab5.gui;

import lab5.db.DatabaseManager;
import lab5.model.Product;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ShopApp extends JFrame {

    private static final Color BG = new Color(187, 251, 136);
    private static final Color PANEL_BG = Color.black;
    private static final Color ACCENT = new Color(0, 255, 8, 255);
    private static final Color DANGER = new Color(220, 50, 50);
    private static final Color SUCCESS  = new Color(34, 160, 80);
    private static final Color WARN = new Color(210, 150, 0);
    private static final Color MUTED = new Color(120, 120, 130);

    private final DatabaseManager db = new DatabaseManager();

    private JLabel lblStatus;
    private JLabel lblUser;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfSearch;
    private JButton btnCreateDB, btnDropDB, btnClearTable, btnAdd, btnUpdate, btnDelete, btnCreateUser;

    public ShopApp() {
        super("Золотое Яблоко — Управление товарами");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 680);
        setMinimumSize(new Dimension(900, 550));
        setLocationRelativeTo(null);
        buildUI();
        showLoginDialog();
    }

    // UI
    private void buildUI() {
        setLayout(new BorderLayout());

        // Шапка
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ACCENT);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("Золотое Яблоко — Онлайн магазин");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        lblUser = new JLabel("Не подключено");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(new Color(255, 210, 220));

        JButton btnLogout = makeBtn("Выйти", DANGER);
        btnLogout.addActionListener(e -> logout());

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRight.setOpaque(false);
        topRight.add(lblUser);
        topRight.add(btnLogout);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Боковая панель
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(PANEL_BG);
        sidebar.setBorder(new EmptyBorder(16, 10, 16, 10));
        sidebar.setPreferredSize(new Dimension(210, 0));

        sideLabel(sidebar, "УПРАВЛЕНИЕ БД");
        btnCreateDB = sideBtn(sidebar, "Создать БД", SUCCESS);
        btnDropDB = sideBtn(sidebar, "Удалить БД", DANGER);

        sidebar.add(Box.createVerticalStrut(10));
        sideLabel(sidebar, "ТАБЛИЦА ТОВАРОВ");
        btnClearTable = sideBtn(sidebar, "Очистить таблицу", WARN);
        btnAdd = sideBtn(sidebar, "Добавить товар",   ACCENT);
        btnUpdate = sideBtn(sidebar, "Изменить товар",   new Color(60, 100, 200));
        btnDelete = sideBtn(sidebar, "Удалить по бренду", DANGER);

        sidebar.add(Box.createVerticalStrut(10));
        sideLabel(sidebar, "ПОЛЬЗОВАТЕЛИ");
        btnCreateUser = sideBtn(sidebar, "Создать пользователя", new Color(100, 50, 180));

        sidebar.add(Box.createVerticalGlue());

        // Центр
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(12, 12, 8, 12));

        // Поиск
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchPanel.setOpaque(false);
        tfSearch = new JTextField(22);
        tfSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 93, 194)),
                new EmptyBorder(4, 8, 4, 8)));

        JButton btnSearch = makeBtn("Поиск по бренду", ACCENT);
        JButton btnRefresh = makeBtn("Обновить список", new Color(80, 90, 100));
        btnSearch.addActionListener(e -> doSearch());
        btnRefresh.addActionListener(e -> doRefresh());
        tfSearch.addActionListener(e -> doSearch());

        searchPanel.add(new JLabel("Бренд:"));
        searchPanel.add(tfSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        // Таблица
        String[] cols = {"ID", "Название", "Бренд", "Категория", "Цена (руб.)", "В наличии"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 5 ? Boolean.class : super.getColumnClass(c);
            }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(230, 230, 235));

        int[] widths = {45, 270, 160, 130, 90, 80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 220)));

        center.add(searchPanel, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        //Статус-бар
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        statusBar.setBackground(new Color(0, 0, 0));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0, 0, 0)));
        lblStatus = new JLabel("Готово");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(MUTED);
        statusBar.add(lblStatus);

        add(sidebar, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        //Слушатели
        btnCreateDB.addActionListener(e -> doCreateDB());
        btnDropDB.addActionListener(e -> doDropDB());
        btnClearTable.addActionListener(e -> doClearTable());
        btnAdd.addActionListener(e -> doAddProduct());
        btnUpdate.addActionListener(e -> doUpdateProduct());
        btnDelete.addActionListener(e -> doDeleteByBrand());
        btnCreateUser.addActionListener(e -> doCreateUser());
    }

    private void showLoginDialog() {
        JDialog dlg = new JDialog(this, "Вход в систему", true);
        dlg.setSize(370, 240);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BG);
        form.setBorder(new EmptyBorder(20, 26, 16, 26));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 5, 6, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfUser = new JTextField("shop_admin", 18);
        JPasswordField tfPwd = new JPasswordField("admin123", 18);

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        form.add(new JLabel("Пользователь:"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(tfUser, c);
        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        form.add(new JLabel("Пароль:"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(tfPwd, c);

        JButton btnOk = makeBtn("Войти", ACCENT);
        JButton btnExit = makeBtn("Выход", DANGER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(PANEL_BG);
        btnRow.add(btnOk);
        btnRow.add(btnExit);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        form.add(btnRow, c);

        dlg.add(form, BorderLayout.CENTER);
        dlg.getRootPane().setDefaultButton(btnOk);
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        btnExit.addActionListener(e -> System.exit(0));
        btnOk.addActionListener(e -> {
            String user = tfUser.getText().trim();
            String pwd = new String(tfPwd.getPassword());
            try {
                db.connect(user, pwd);
                dlg.dispose();
                onLoginSuccess(user);
            } catch (Exception ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                if (cause instanceof SQLException && "3D000".equals(((SQLException) cause).getSQLState())) {
                    int r = JOptionPane.showConfirmDialog(dlg, "База данных shop_db не существует.\nСоздать её сейчас?", "БД не найдена", JOptionPane.YES_NO_OPTION);
                    if (r == JOptionPane.YES_OPTION) {
                        doCreateDB();
                        try {
                            db.connect(user, pwd);
                            dlg.dispose();
                            onLoginSuccess(user);
                        } catch (Exception ex2) {
                            JOptionPane.showMessageDialog(dlg, "Не удалось подключиться после создания БД:\n" + ex2.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(dlg, "Ошибка подключения:\n" + cause.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dlg.setVisible(true);
    }

    private void onLoginSuccess(String user) {
        boolean isAdmin = user.equals("shop_admin") || user.endsWith("_admin");
        lblUser.setText(user + (isAdmin ? "  [Администратор]" : "  [Гость]"));
        status("Подключено как " + user);
        doRefresh();
    }


    private void logout() {
        db.disconnect();
        tableModel.setRowCount(0);
        lblUser.setText("Не подключено");
        showLoginDialog();
    }

    // Действия
    private void doCreateDB() {
        JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
        JTextField tfSU = new JTextField("postgres");
        JPasswordField tfSP = new JPasswordField();
        p.add(new JLabel("Суперпользователь:")); p.add(tfSU);
        p.add(new JLabel("Пароль:")); p.add(tfSP);
        int r = JOptionPane.showConfirmDialog(this, p, "Создать БД shop_db", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;
        run(() -> {
            db.createDatabase(tfSU.getText().trim(), new String(tfSP.getPassword()));
            status("БД shop_db успешно создана!");
        });
    }

    private void doDropDB() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить базу данных shop_db?\nВСЕ данные будут удалены безвозвратно!",
                "Подтверждение", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
        JTextField tfSU = new JTextField("postgres");
        JPasswordField tfSP = new JPasswordField();
        p.add(new JLabel("Суперпользователь:")); p.add(tfSU);
        p.add(new JLabel("Пароль:")); p.add(tfSP);
        int r = JOptionPane.showConfirmDialog(this, p, "Учётные данные суперпользователя", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        run(() -> {
            db.dropDatabase(tfSU.getText().trim(), new String(tfSP.getPassword()));
            tableModel.setRowCount(0);
            status("БД shop_db удалена");
        });
    }

    private void doClearTable() {
        int c = JOptionPane.showConfirmDialog(this,
                "Очистить таблицу товаров? Все записи будут удалены.",
                "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        run(() -> { db.clearTable(); tableModel.setRowCount(0); status("Таблица очищена"); });
    }

    private void doAddProduct() {
        Product p = ProductDialog.show(this, null);
        if (p == null) return;
        run(() -> { db.addProduct(p); doRefresh(); status("Товар добавлен"); });
    }

    private void doUpdateProduct() {
        int row = table.getSelectedRow();
        if (row < 0) { info("Выберите строку в таблице"); return; }
        Product current = rowToProduct(row);
        Product updated = ProductDialog.show(this, current);
        if (updated == null) return;
        run(() -> { db.updateProduct(updated); doRefresh(); status("Товар обновлён"); });
    }

    private void doDeleteByBrand() {
        String brand = JOptionPane.showInputDialog(this, "Введите название бренда для удаления\n(удалятся все товары этого бренда):", "Удалить по бренду", JOptionPane.QUESTION_MESSAGE);
        if (brand == null || brand.isBlank()) return;
        run(() -> { db.deleteByBrand(brand.trim()); doRefresh(); status("Удалены товары бренда: " + brand); });
    }

    private void doSearch() {
        String brand = tfSearch.getText().trim();
        if (brand.isEmpty()) { doRefresh(); return; }
        run(() -> {
            List<Product> list = db.searchByBrand(brand);
            fillTable(list);
            status("Найдено товаров: " + list.size());
        });
    }

    private void doRefresh() {
        if (!db.isConnected()) return;
        run(() -> {
            List<Product> list = db.getAllProducts();
            fillTable(list);
            status("Товаров в таблице: " + list.size());
        });
    }

    private void doCreateUser() {
        JPanel p = new JPanel(new GridLayout(3, 2, 6, 6));
        JTextField tfNewUser = new JTextField();
        JPasswordField tfNewPwd = new JPasswordField();
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"guest", "admin"});
        p.add(new JLabel("Имя пользователя:")); p.add(tfNewUser);
        p.add(new JLabel("Пароль:")); p.add(tfNewPwd);
        p.add(new JLabel("Роль:")); p.add(cbRole);

        int r = JOptionPane.showConfirmDialog(this, p, "Создать пользователя БД", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String newUser = tfNewUser.getText().trim();
        String newPwd  = new String(tfNewPwd.getPassword());
        String role    = (String) cbRole.getSelectedItem();
        if (newUser.isEmpty() || newPwd.isEmpty()) { info("Заполните все поля"); return; }

        run(() -> {
            db.createUser(newUser, newPwd, role);
            status("Пользователь " + newUser + " создан с ролью " + role);
        });
    }

    private void run(SQLTask task) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

                    String msg = cause.getMessage();
                    if (cause instanceof SQLException) {
                        String sqlState = ((SQLException) cause).getSQLState();
                        if ("42501".equals(sqlState)) { msg = "Недостаточно прав доступа\nОперация доступна только администратору";
                        }
                    }

                    JOptionPane.showMessageDialog(ShopApp.this, msg, "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void fillTable(List<Product> products) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Product p : products) {
                tableModel.addRow(new Object[]{
                        p.getId(), p.getName(), p.getBrand(), p.getCategory(), p.getPrice(), p.isInStock()
                });
            }
        });
    }

    private Product rowToProduct(int row) {
        return new Product(
                (int) tableModel.getValueAt(row, 0),
                (String) tableModel.getValueAt(row, 1),
                (String) tableModel.getValueAt(row, 2),
                (String) tableModel.getValueAt(row, 3),
                (double) tableModel.getValueAt(row, 4),
                (boolean) tableModel.getValueAt(row, 5)
        );
    }

    private void status(String msg) { SwingUtilities.invokeLater(() -> lblStatus.setText(msg)); }
    private void info(String msg) { JOptionPane.showMessageDialog(this, msg); }

    // методы UI
    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        return b;
    }

    private JButton sideBtn(JPanel parent, String text, Color bg) {
        JButton b = makeBtn(text, bg);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(b);
        parent.add(Box.createVerticalStrut(6));
        return b;
    }

    private void sideLabel(JPanel parent, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(l);
        parent.add(Box.createVerticalStrut(4));
    }

    @FunctionalInterface
    interface SQLTask { void run() throws Exception; }

    // Main
    public static void main(String[] args) {
        try { Class.forName("org.postgresql.Driver"); }
        catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "PostgreSQL JDBC-драйвер не найден\nПоложите postgresql-*.jar в classpath",
                    "Ошибка запуска", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new ShopApp().setVisible(true));
    }
}
