import java.sql.*;
import java.util.Scanner;

public class GymTracker {
    private static final String URL = "jdbc:mysql://localhost:3306/gym_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "********"; 

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver nahi mila bhai!");
            return;
        }

        Scanner sc = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n=== 🏋️ GYM WORKOUT TRACKER ===");
            System.out.println("1. View Weekly Routine");
            System.out.println("2. Change Weekly Routine (Poora Hafta Ek Saath)");
            System.out.println("3. Log Today's Exercise");
            System.out.println("4. View Today's Workout Logs (Naya Option 🔥)");
            System.out.println("5. Exit");
            System.out.print("Choice daalo bhai: ");
            int choice = sc.nextInt();
            sc.nextLine(); // Buffer clear

            if (choice == 5) {
                System.out.println("Gym jao, body banao! Bye bye.");
                break;
            }

            switch (choice) {
                case 1:
                    viewRoutine();
                    break;
                case 2:
                    updateWholeWeekRoutine(sc);
                    break;
                case 3:
                    logDailyWorkout(sc);
                    break;
                case 4:
                    viewTodaysLogs(); // Naya function call
                    break;
                default:
                    System.out.println("Galat choice hai bhai!");
            }
        }
        sc.close();
    }

    // 1. Routine dekhne ke liye
    private static void viewRoutine() {
        String query = "SELECT * FROM weekly_routine " +
                       "ORDER BY FIELD(day_name, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')";
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.println("\n--- YOUR WEEKLY ROUTINE ---");
            while (rs.next()) {
                System.out.println(rs.getString("day_name") + " -> " + rs.getString("workout_type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2. Poore hafte ka routine ek saath badalna
    private static void updateWholeWeekRoutine(Scanner sc) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String query = "{ call UpdateRoutine(?, ?) }";
        
        System.out.println("\n🔄 --- POORE HAFTE KA ROUTINE SET KAREIN ---");
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement cstmt = con.prepareCall(query)) {
            
            for (String day : days) {
                System.out.print(day + " ke liye kya workout hai?: ");
                String type = sc.nextLine();
                cstmt.setString(1, day);
                cstmt.setString(2, type);
                cstmt.execute();
            }
            System.out.println("\n🔥 Routine ek baar mein update ho gaya!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Dynamic Workout Logger (Jaisa tumne copy mein banaya tha)
    private static void logDailyWorkout(Scanner sc) {
        System.out.println("\n🏋️ --- TODAY'S WORKOUT LOGGER ---");
        System.out.print("Aaj ka target body part? (e.g., Chest and Heavy Arms): ");
        String bodyPart = sc.nextLine();
        
        System.out.print("Kitni exercises ki aaj?: ");
        int totalExercises = sc.nextInt();
        sc.nextLine(); 

        String logQuery = "{ call LogExercise(?, ?, ?, ?, ?) }";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement cstmt = con.prepareCall(logQuery)) {
            
            long millis = System.currentTimeMillis();
            Date today = new Date(millis);

            for (int i = 1; i <= totalExercises; i++) {
                System.out.println("\n--- Exercise #" + i + " ---");
                System.out.print("Exercise ka naam: ");
                String exName = sc.nextLine();
                System.out.print("Weight (KG): ");
                int weight = sc.nextInt();
                System.out.print("Sets: ");
                int sets = sc.nextInt();
                System.out.print("Reps: ");
                int reps = sc.nextInt();
                sc.nextLine(); 

                cstmt.setDate(1, today);
                cstmt.setString(2, exName + " (" + bodyPart + ")");
                cstmt.setInt(3, weight);
                cstmt.setInt(4, sets);
                cstmt.setInt(5, reps);
                cstmt.execute();
                System.out.println("✅ Saved!");
            }
            System.out.println("\n💪 Poora workout track ho gaya!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 4. 🔥 NAYA OPTION: Aaj ka data dekhne ke liye (Normal Statement + ResultSet)
    private static void viewTodaysLogs() {
        // CURDATE() se database automatic sirf aaj ki date ka data nikaalega
        String query = "SELECT exercise_name, weight, sets, reps FROM daily_logs WHERE log_date = CURDATE()";
        
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.println("\n📊 --- TODAY'S WORKOUT LOGS ---");
            System.out.println("--------------------------------------------------");
            System.out.printf("%-30s | %-7s | %-5s | %-5s\n", "Exercise Name", "Weight", "Sets", "Reps");
            System.out.println("--------------------------------------------------");
            
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.printf("%-30s | %-7d | %-5d | %-5d\n", 
                    rs.getString("exercise_name"), 
                    rs.getInt("weight"), 
                    rs.getInt("sets"), 
                    rs.getInt("reps"));
            }
            
            if (!hasData) {
                System.out.println("Bhai aaj koi exercise log nahi ki tumne!");
            }
            System.out.println("--------------------------------------------------");
            
        } catch (SQLException e) {
            System.out.println("Data dekhne mein koi error aaya!");
            e.printStackTrace();
        }
    }
}