package lab5.gui;

import lab5.model.Product;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProductDialog extends JDialog {

    private Product result = null;
    private final JTextField tfName = new JTextField(24);
    private final JTextField tfBrand = new JTextField(24);
    private final JTextField tfCategory = new JTextField(24);
    private final JSpinner spPrice = new JSpinner(
            new SpinnerNumberModel(999.0, 0.0, 100000.0, 10.0));
    private final JCheckBox cbInStock = new JCheckBox("В наличии", true);

    private ProductDialog(Frame owner, Product existing) {
        super(owner, existing == null ? "Добавить товар" : "Редактировать товар", true);
        setSize(420, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 20, 8, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, c, 0, "Название товара:", tfName);
        addRow(form, c, 1, "Бренд:", tfBrand);
        addRow(form, c, 2, "Категория:", tfCategory);
        addRow(form, c, 3, "Цена (руб.):", spPrice);
        c.gridx = 1; c.gridy = 4;
        form.add(cbInStock, c);

        if (existing != null) {
            tfName.setText(existing.getName());
            tfBrand.setText(existing.getBrand());
            tfCategory.setText(existing.getCategory());
            spPrice.setValue(existing.getPrice());
            cbInStock.setSelected(existing.isInStock());
        }

        JButton btnOk     = makeBtn("Сохранить", new Color(34, 160, 80));
        JButton btnCancel = makeBtn("Отмена",    new Color(220, 50, 50));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setBorder(new EmptyBorder(0, 20, 14, 20));
        btnRow.add(btnCancel);
        btnRow.add(btnOk);

        add(form,   BorderLayout.CENTER);
        add(btnRow, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnOk);

        btnOk.addActionListener(e -> {
            String name  = tfName.getText().trim();
            String brand = tfBrand.getText().trim();
            if (name.isEmpty() || brand.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Название и бренд обязательны.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }
            result = new Product(existing != null ? existing.getId() : 0, name, brand, tfCategory.getText().trim(), ((Number) spPrice.getValue()).doubleValue(), cbInStock.isSelected());
            dispose();
        });
        btnCancel.addActionListener(e -> dispose());
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        p.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        p.add(field, c);
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(6, 16, 6, 16));
        return b;
    }

    public static Product show(Frame owner, Product existing) {
        ProductDialog dlg = new ProductDialog(owner, existing);
        dlg.setVisible(true);
        return dlg.result;
    }
}
