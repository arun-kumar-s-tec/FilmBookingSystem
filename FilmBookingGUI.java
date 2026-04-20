import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FilmBookingGUI {

    static final String DB_URL = "jdbc:mysql://localhost:3306/filmdb";
    static final String USER = "root";
    static final String PASS = "dbms";

    JFrame frame;

    public FilmBookingGUI() {
        frame = new JFrame("🎬 Film Booking System");
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.add(moviePanel(), BorderLayout.CENTER);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // ===== BUTTON STYLE =====
    JButton button(String txt) {
        JButton b = new JButton(txt);
        b.setBackground(new Color(70, 130, 180));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setPreferredSize(new Dimension(120, 40));
        return b;
    }

    // ===== TEXT FIELD =====
    JTextField field() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(200, 35));
        return tf;
    }

    // ================= MOVIE PANEL =================
    JPanel moviePanel() {

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(20, 40, 20, 40));

        // ===== FORM =====
        JPanel form = new JPanel(new GridLayout(4, 4, 20, 20));

        JTextField id = field();
        JTextField title = field();
        JTextField genre = field();
        JTextField duration = field();
        JTextField language = field();
        JTextField rating = field();
        JTextField price = field();
        JTextField date = field(); // YYYY-MM-DD

        form.add(new JLabel("Movie ID")); form.add(id);
        form.add(new JLabel("Title")); form.add(title);

        form.add(new JLabel("Genre")); form.add(genre);
        form.add(new JLabel("Duration")); form.add(duration);

        form.add(new JLabel("Language")); form.add(language);
        form.add(new JLabel("Rating")); form.add(rating);

        form.add(new JLabel("Price")); form.add(price);
        form.add(new JLabel("Release Date (YYYY-MM-DD)")); form.add(date);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.add(form);

        // ===== TABLE =====
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID","Title","Genre","Duration","Language","Rating","Price","Release Date"}, 0);

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        // ===== BUTTONS =====
        JButton insert = button("Insert");
        JButton update = button("Update");
        JButton delete = button("Delete");
        JButton view = button("View");
        JButton clear = button("Clear");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.add(insert);
        btnPanel.add(update);
        btnPanel.add(delete);
        btnPanel.add(view);
        btnPanel.add(clear);

        Runnable refresh = () -> loadTable(model, "SELECT * FROM movie");

        // ===== INSERT =====
        insert.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {

                PreparedStatement ps =
                        c.prepareStatement("INSERT INTO movie VALUES(?,?,?,?,?,?,?,?)");

                ps.setString(1, id.getText());
                ps.setString(2, title.getText());
                ps.setString(3, genre.getText());
                ps.setInt(4, Integer.parseInt(duration.getText()));
                ps.setString(5, language.getText());
                ps.setString(6, rating.getText());
                ps.setInt(7, Integer.parseInt(price.getText()));

                // DATE FIX
                ps.setDate(8, Date.valueOf(date.getText()));

                ps.executeUpdate();

                JOptionPane.showMessageDialog(null, "Movie Inserted Successfully!");
                refresh.run();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        });

        // ===== VIEW =====
        view.addActionListener(e -> {
            refresh.run();
            JOptionPane.showMessageDialog(null, "Data Loaded!");
        });

        // ===== UPDATE =====
        update.addActionListener(e -> {
            try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {

                PreparedStatement ps =
                        c.prepareStatement(
                                "UPDATE movie SET title=?,genre=?,duration=?,language=?,rating=?,price=?,release_date=? WHERE movie_id=?");

                ps.setString(1, title.getText());
                ps.setString(2, genre.getText());
                ps.setInt(3, Integer.parseInt(duration.getText()));
                ps.setString(4, language.getText());
                ps.setString(5, rating.getText());
                ps.setInt(6, Integer.parseInt(price.getText()));
                ps.setDate(7, Date.valueOf(date.getText()));
                ps.setString(8, id.getText());

                ps.executeUpdate();

                JOptionPane.showMessageDialog(null, "Movie Updated Successfully!");
                refresh.run();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        });

        // ===== DELETE =====
        delete.addActionListener(e ->
                deleteRow("movie", "movie_id", table, model, refresh));

        // ===== CLEAR =====
        clear.addActionListener(e -> {
            id.setText(""); title.setText(""); genre.setText("");
            duration.setText(""); language.setText("");
            rating.setText(""); price.setText(""); date.setText("");
        });

        // ===== TABLE CLICK =====
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (r != -1) {
                id.setText(model.getValueAt(r, 0).toString());
                title.setText(model.getValueAt(r, 1).toString());
                genre.setText(model.getValueAt(r, 2).toString());
                duration.setText(model.getValueAt(r, 3).toString());
                language.setText(model.getValueAt(r, 4).toString());
                rating.setText(model.getValueAt(r, 5).toString());
                price.setText(model.getValueAt(r, 6).toString());
                date.setText(model.getValueAt(r, 7).toString());
            }
        });

        // ===== LAYOUT =====
        JPanel top = new JPanel(new BorderLayout());
        top.add(centerWrap, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.SOUTH);

        main.add(top, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);

        refresh.run();
        return main;
    }

    // ================= COMMON =================
    void loadTable(DefaultTableModel model, String query) {
        model.setRowCount(0);

        try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement s = c.createStatement()) {

            ResultSet rs = s.executeQuery(query);

            while (rs.next()) {
                int cols = rs.getMetaData().getColumnCount();
                Object[] row = new Object[cols];

                for (int i = 0; i < cols; i++)
                    row[i] = rs.getObject(i + 1);

                model.addRow(row);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    void deleteRow(String tableName, String pk,
                   JTable table,
                   DefaultTableModel model,
                   Runnable refresh) {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(null, "Select a row first!");
            return;
        }

        String id = model.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                null, "Delete this record?", "Confirm",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {

            PreparedStatement ps =
                    c.prepareStatement("DELETE FROM " + tableName + " WHERE " + pk + "=?");

            ps.setString(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Record Deleted Successfully!");
            refresh.run();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            SwingUtilities.invokeLater(FilmBookingGUI::new);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}