import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class GymApp {
    private static final String URL = "jdbc:mysql://localhost:3306/gym_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "********"; 

    private static String currentLoggedInUser = "";

    // 🌟 FRONTEND COLOR PALETTE
    private static final Color BG_DARK = new Color(24, 28, 36);      
    private static final Color PANEL_DARK = new Color(32, 38, 50);   
    private static final Color ACCENT_BLUE = new Color(52, 152, 219); 
    private static final Color ACCENT_GREEN = new Color(46, 204, 113); 
    private static final Color ACCENT_RED = new Color(231, 76, 60);    
    private static final Color TEXT_WHITE = new Color(240, 244, 248);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }
        
        SwingUtilities.invokeLater(() -> new LoginRegisterWindow().setVisible(true));
    }

    static class LoginRegisterWindow extends JFrame {
        private JTextField userField;
        private JPasswordField passField;
        private JButton loginButton, registerButton;

        public LoginRegisterWindow() {
            setTitle("🔐 Secure Login - Gym Tracker");
            setSize(420, 360);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(false);
            
            JPanel mainPanel = new JPanel();
            mainPanel.setBackground(BG_DARK);
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

            JLabel welcomeLabel = new JLabel("FITNESS PORTAL", SwingConstants.CENTER);
            welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
            welcomeLabel.setForeground(ACCENT_BLUE);
            mainPanel.add(welcomeLabel, BorderLayout.NORTH);

            JPanel formPanel = new JPanel(new GridLayout(4, 1, 5, 5));
            formPanel.setBackground(BG_DARK);
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

            JLabel userLabel = new JLabel("Username / Identity");
            userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            userLabel.setForeground(TEXT_MUTED);
            userField = new JTextField();
            styleTextField(userField);

            JLabel passLabel = new JLabel("Secret Password");
            passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            passLabel.setForeground(TEXT_MUTED);
            passField = new JPasswordField();
            styleTextField(passField);

            formPanel.add(userLabel);
            formPanel.add(userField);
            formPanel.add(passLabel);
            formPanel.add(passField);
            mainPanel.add(formPanel, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
            btnPanel.setBackground(BG_DARK);

            loginButton = new JButton("Sign In");
            styleButton(loginButton, ACCENT_GREEN);

            registerButton = new JButton("Sign Up");
            styleButton(registerButton, PANEL_DARK);
            registerButton.setForeground(TEXT_WHITE);

            btnPanel.add(loginButton);
            btnPanel.add(registerButton);
            mainPanel.add(btnPanel, BorderLayout.SOUTH);

            add(mainPanel);

            loginButton.addActionListener(e -> {
                String username = userField.getText().trim();
                String password = new String(passField.getPassword()).trim();
                if (verifyLogin(username, password)) {
                    currentLoggedInUser = username;
                    dispose();
                    new StartJourneyWindow().setVisible(true);
                } else {
                    showModernError("Oops! Invalid username or password.");
                }
            });

            registerButton.addActionListener(e -> {
                String username = userField.getText().trim();
                String password = new String(passField.getPassword()).trim();
                if(!username.isEmpty() && registerNewUser(username, password)) {
                    JOptionPane.showMessageDialog(this, "Account setup done! Press Sign In now.");
                } else {
                    showModernError("Username taken or invalid fields.");
                }
            });
        }

        private boolean verifyLogin(String user, String pass) {
            String query = "SELECT * FROM app_users WHERE username = ? AND password = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, user);
                pstmt.setString(2, pass);
                return pstmt.executeQuery().next();
            } catch (SQLException e) { return false; }
        }

        private boolean registerNewUser(String user, String pass) {
            String query = "INSERT INTO app_users (username, password) VALUES (?, ?)";
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, user);
                pstmt.setString(2, pass);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) { return false; }
        }
    }

    static class StartJourneyWindow extends JFrame {
        private DefaultListModel<String> daysListModel;
        private JList<String> daysList;
        private JLabel statsLabel;

        public StartJourneyWindow() {
            setTitle("Dashboard - " + currentLoggedInUser);
            setSize(520, 560);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            
            JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
            mainPanel.setBackground(BG_DARK);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Top Info Header Card
            JPanel headerCard = new JPanel(new GridLayout(2, 1, 5, 5));
            headerCard.setBackground(PANEL_DARK);
            headerCard.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
            
            JLabel welcomeUser = new JLabel("Welcome back, " + currentLoggedInUser + " 👋", SwingConstants.LEFT);
            welcomeUser.setFont(new Font("Segoe UI", Font.BOLD, 18));
            welcomeUser.setForeground(TEXT_WHITE);

            statsLabel = new JLabel("📊 Loading Stats...", SwingConstants.LEFT);
            statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            statsLabel.setForeground(ACCENT_BLUE);

            headerCard.add(welcomeUser);
            headerCard.add(statsLabel);
            mainPanel.add(headerCard, BorderLayout.NORTH);

            // Days Selection List
            daysListModel = new DefaultListModel<>();
            daysList = new JList<>(daysListModel);
            daysList.setBackground(PANEL_DARK);
            daysList.setForeground(TEXT_WHITE);
            daysList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            daysList.setFixedCellHeight(40);
            daysList.setSelectionBackground(ACCENT_BLUE);
            daysList.setSelectionForeground(TEXT_WHITE);
            
            JScrollPane scrollPane = new JScrollPane(daysList);
            scrollPane.setBorder(BorderFactory.createLineBorder(PANEL_DARK));
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            // Bottom Buttons Panel (Grid layout for Add & Delete side-by-side)
            JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
            bottomPanel.setBackground(BG_DARK);

            JPanel btnGrid = new JPanel(new GridLayout(1, 2, 10, 0));
            btnGrid.setBackground(BG_DARK);

            JButton addDayBtn = new JButton("➕ Add New Day");
            styleButton(addDayBtn, ACCENT_BLUE);

            JButton deleteDayBtn = new JButton("❌ Delete Selected");
            styleButton(deleteDayBtn, ACCENT_RED);

            btnGrid.add(addDayBtn);
            btnGrid.add(deleteDayBtn);
            bottomPanel.add(btnGrid, BorderLayout.CENTER);
            
            JLabel hint = new JLabel("Double click on any scheduled day row to modify logs", SwingConstants.CENTER);
            hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            hint.setForeground(TEXT_MUTED);
            bottomPanel.add(hint, BorderLayout.SOUTH);
            
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);
            add(mainPanel);

            // Add Day Action
            addDayBtn.addActionListener(e -> {
                String day = JOptionPane.showInputDialog(this, "Enter Day Name (e.g. Monday, Day 1):");
                if (day != null && !day.trim().isEmpty()) {
                    String target = JOptionPane.showInputDialog(this, "Target Exercise/Body Part:");
                    saveNewDay(day.trim(), target != null ? target.trim() : "Rest Day");
                    loadUserDaysAndStats();
                }
            });

            // ❌ DELETE DAY ACTION LOGIC
            deleteDayBtn.addActionListener(e -> {
                String selected = daysList.getSelectedValue();
                if (selected == null) {
                    JOptionPane.showMessageDialog(this, "Pehle list se wo day select karo jise delete karna hai bhai!", "Selection Missing", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Selected row se Day Name nikalna
                String dayName = selected.split("  ➔  ")[0];

                int confirm = JOptionPane.showConfirmDialog(this, 
                        "Kya tum sach me '" + dayName + "' ka routine delete karna chahte ho?", 
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    deleteDayFromDB(dayName);
                    loadUserDaysAndStats(); // UI refresh
                    JOptionPane.showMessageDialog(this, dayName + " successfully delete ho gaya!");
                }
            });

            // Double click flow
            daysList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        String selected = daysList.getSelectedValue();
                        if (selected != null) {
                            String dayName = selected.split("  ➔  ")[0];
                            String workoutType = selected.split("  ➔  ")[1];
                            new MainOptionsMenuWindow(dayName, workoutType).setVisible(true);
                        }
                    }
                }
            });

            loadUserDaysAndStats();
        }

        private void loadUserDaysAndStats() {
            daysListModel.clear();
            int totalDays = 0, totalSets = 0;
            String queryDays = "SELECT day_name, workout_type FROM weekly_routine WHERE username = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = con.prepareStatement(queryDays)) {
                pstmt.setString(1, currentLoggedInUser);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    daysListModel.addElement(rs.getString("day_name") + "  ➔  " + rs.getString("workout_type"));
                    totalDays++;
                }
            } catch (SQLException e) { e.printStackTrace(); }

            String queryStats = "SELECT COUNT(*) FROM daily_logs WHERE username = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = con.prepareStatement(queryStats)) {
                pstmt.setString(1, currentLoggedInUser);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) totalSets = rs.getInt(1);
            } catch (SQLException e) { e.printStackTrace(); }

            statsLabel.setText("⚡ Tracked Days: " + totalDays + "  |  💪 Cumulative Sets Hit: " + totalSets);
        }

        private void saveNewDay(String day, String target) {
            String query = "INSERT INTO weekly_routine (day_name, workout_type, username) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE workout_type = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, day);
                pstmt.setString(2, target);
                pstmt.setString(3, currentLoggedInUser);
                pstmt.setString(4, target);
                pstmt.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }

        // Database se row udaane ka function
        private void deleteDayFromDB(String dayName) {
            String query = "DELETE FROM weekly_routine WHERE day_name = ? AND username = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, dayName);
                pstmt.setString(2, currentLoggedInUser);
                pstmt.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    static class MainOptionsMenuWindow extends JFrame {
        private String day, workoutType;

        public MainOptionsMenuWindow(String day, String workoutType) {
            this.day = day;
            this.workoutType = workoutType;

            setTitle("Manage Options - " + day);
            setSize(460, 440);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel(new GridLayout(6, 1, 12, 12));
            mainPanel.setBackground(BG_DARK);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

            JLabel title = new JLabel(day.toUpperCase() + " : " + workoutType, SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));
            title.setForeground(ACCENT_BLUE);
            mainPanel.add(title);

            JButton btn1 = createMenuButton("1. View Full Routine Board");
            btn1.addActionListener(e -> showRoutineDialog());
            mainPanel.add(btn1);

            JButton btn2 = createMenuButton("2. Edit Target Muscle Group");
            btn2.addActionListener(e -> changeRoutineDialog());
            mainPanel.add(btn2);

            JButton btn3 = createMenuButton("3. 🔥 Log Today's Working Sets");
            btn3.setBackground(ACCENT_BLUE);
            btn3.setForeground(TEXT_WHITE);
            btn3.addActionListener(e -> openSetsTrackerFlow());
            mainPanel.add(btn3);

            JButton btn4 = createMenuButton("4. View Active Workout Log");
            btn4.addActionListener(e -> viewTodaysLogsDialog());
            mainPanel.add(btn4);

            JButton btn5 = createMenuButton("5. Back to Workspace");
            btn5.setBackground(ACCENT_RED);
            btn5.setForeground(TEXT_WHITE);
            btn5.addActionListener(e -> dispose());
            mainPanel.add(btn5);

            add(mainPanel);
        }

        private JButton createMenuButton(String text) {
            JButton btn = new JButton(text);
            styleButton(btn, PANEL_DARK);
            btn.setForeground(TEXT_WHITE);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            return btn;
        }

        private void showRoutineDialog() {
            JDialog dialog = createStyledDialog("Workout Routine View", 420, 300);
            DefaultTableModel model = new DefaultTableModel(new String[]{"Day Target", "Exercise Matrix"}, 0);
            JTable table = createStyledTable(model);
            
            String query = "SELECT day_name, workout_type FROM weekly_routine WHERE username = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, currentLoggedInUser);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) model.addRow(new Object[]{rs.getString("day_name"), rs.getString("workout_type")});
            } catch (SQLException ex) { ex.printStackTrace(); }
            
            dialog.add(new JScrollPane(table));
            dialog.setVisible(true);
        }

        private void changeRoutineDialog() {
            String newWorkout = JOptionPane.showInputDialog(this, "Update Target Muscle:", workoutType);
            if (newWorkout != null && !newWorkout.trim().isEmpty()) {
                String query = "UPDATE weekly_routine SET workout_type = ? WHERE day_name = ? AND username = ?";
                try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                     PreparedStatement pstmt = con.prepareStatement(query)) {
                    pstmt.setString(1, newWorkout.trim());
                    pstmt.setString(2, day);
                    pstmt.setString(3, currentLoggedInUser);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Success! Muscle Group Shifted.");
                    dispose();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }

        private void openSetsTrackerFlow() {
            String exName = JOptionPane.showInputDialog(this, "Enter Exercise Name:");
            if (exName == null || exName.trim().isEmpty()) return;
            String setsStr = JOptionPane.showInputDialog(this, "Total Target Sets:");
            if (setsStr == null || setsStr.trim().isEmpty()) return;

            try {
                int totalSets = Integer.parseInt(setsStr.trim());
                for (int currentSet = 1; currentSet <= totalSets; currentSet++) {
                    String weightStr = JOptionPane.showInputDialog(this, "SET #" + currentSet + " / " + totalSets + "\nWeight load (KG):");
                    String repsStr = JOptionPane.showInputDialog(this, "SET #" + currentSet + " / " + totalSets + "\nReps count:");
                    if (weightStr != null && repsStr != null) {
                        saveSetToDB(exName.trim(), currentSet, totalSets, Integer.parseInt(weightStr.trim()), Integer.parseInt(repsStr.trim()));
                    }
                }
                JOptionPane.showMessageDialog(this, "All sets securely logged into database.");
            } catch (Exception ex) { showModernError("Invalid layout numbers."); }
        }

        private void saveSetToDB(String exName, int setNo, int totalSets, int weight, int reps) {
            String query = "INSERT INTO daily_logs (log_date, exercise_name, set_no, weight, sets, reps, username) VALUES (CURDATE(), ?, ?, ?, ?, ?, ?)";
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, exName + " (" + workoutType + ")");
                pstmt.setInt(2, setNo);
                pstmt.setInt(3, weight);
                pstmt.setInt(4, totalSets);
                pstmt.setInt(5, reps);
                pstmt.setString(6, currentLoggedInUser);
                pstmt.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }

        private void viewTodaysLogsDialog() {
            JDialog dialog = createStyledDialog("Active Performance Matrix", 520, 320);
            DefaultTableModel model = new DefaultTableModel(new String[]{"Exercise Target", "Set #", "Load (KG)", "Reps Done"}, 0);
            JTable table = createStyledTable(model);
            
            String query = "SELECT exercise_name, set_no, weight, reps FROM daily_logs WHERE log_date = CURDATE() AND username = ?";
            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, currentLoggedInUser);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString("exercise_name"), rs.getInt("set_no"), rs.getInt("weight"), rs.getInt("reps")});
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
            dialog.add(new JScrollPane(table));
            dialog.setVisible(true);
        }

        private JDialog createStyledDialog(String title, int w, int h) {
            JDialog dialog = new JDialog(this, title, true);
            dialog.setSize(w, h);
            dialog.setLocationRelativeTo(this);
            dialog.getContentPane().setBackground(BG_DARK);
            return dialog;
        }
    }

    private static void styleTextField(JTextField field) {
        field.setBackground(PANEL_DARK);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(TEXT_WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PANEL_DARK, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private static void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(PANEL_DARK);
        table.setForeground(TEXT_WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(BG_DARK);
        table.setSelectionBackground(ACCENT_BLUE);
        table.setSelectionForeground(TEXT_WHITE);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_DARK);
        header.setForeground(ACCENT_BLUE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return table;
    }

    private static void showModernError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Alert System", JOptionPane.ERROR_MESSAGE);
    }
}