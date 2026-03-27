import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.SQLException;

public class MainMenuGUI {
    private JFrame frame;
    private JPanel mainPanel;
    private HashMap<String, Player> playerDatabase;

    public MainMenuGUI() {
        playerDatabase = new HashMap<>();
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Galactic Dominion");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.BLACK);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));

        JLabel titleLabel = new JLabel("Galactic Dominion", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(0, 200, 255));

        JLabel subtitleLabel = new JLabel("Where you explore universal objects", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(Color.WHITE);

        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 200, 100, 200));
        buttonPanel.setBackground(Color.BLACK);

        JButton startButton = new JButton("Start Game");
        styleButton(startButton);
        startButton.addActionListener(e -> showLoginDialog());

        JButton exitButton = new JButton("Exit");
        styleButton(exitButton);
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(startButton);
        buttonPanel.add(exitButton);

        // Add components to main panel
        mainPanel.add(titlePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);

        // Add key listener for Enter key
        frame.getRootPane().registerKeyboardAction(
                e -> showLoginDialog(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(30, 30, 70));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 200), 2));
        button.setPreferredSize(new Dimension(200, 60));
    }

    private void showLoginDialog() {
        String[] options = {"New Player", "Existing Player"};
        int choice = JOptionPane.showOptionDialog(
                frame,
                "Welcome to Galactic Dominion",
                "Player Login",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) { // New Player
            handleNewPlayerRegistration();
        } else if (choice == 1) { // Existing Player
            handleExistingPlayerLogin();
        }
    }

    private void handleNewPlayerRegistration() {
        JPanel newPlayerPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField nameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        newPlayerPanel.add(new JLabel("Commander Name:"));
        newPlayerPanel.add(nameField);
        newPlayerPanel.add(new JLabel("Password:"));
        newPlayerPanel.add(passwordField);
        newPlayerPanel.add(new JLabel(""));
        newPlayerPanel.add(new JLabel(""));

        int result = JOptionPane.showConfirmDialog(
                frame,
                newPlayerPanel,
                "New Player Registration",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (name.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Name and password cannot be empty!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Register player in database
            String playerID = DatabaseManager.registerPlayer(name, password);

            if (playerID != null) {
                // Create player object
                Player newPlayer = new Player(name, password);

                // The registerPlayer stored procedure generates the ID, so we need to use that
                // We'll create a new constructor or setter for this

                // Initialize new game
                GameInitializer.initializeNewGame(newPlayer);

                // Save initial game state to database
                DatabaseManager.saveGameState(newPlayer);

                // Store in local cache
                playerDatabase.put(playerID, newPlayer);

                String message = "Registration Successful!\n\n" +
                        "Your Player ID: " + playerID + "\n\n" +
                        "IMPORTANT: Please save this ID!\n" +
                        "You will need it to log in to your game.\n\n" +
                        "Your game has been saved to the database.";

                JOptionPane.showMessageDialog(frame,
                        message,
                        "Registration Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                openGameGUI(newPlayer);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Registration failed. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExistingPlayerLogin() {
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField idField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        loginPanel.add(new JLabel("Player ID:"));
        loginPanel.add(idField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel(""));
        loginPanel.add(new JLabel(""));

        int result = JOptionPane.showConfirmDialog(
                frame,
                loginPanel,
                "Player Login",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String playerID = idField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (playerID.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Player ID and password cannot be empty!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Attempt login through database
            boolean loginSuccess = DatabaseManager.loginPlayer(playerID, password);

            if (loginSuccess) {
                // Load player data from database
                Player existingPlayer = DatabaseManager.loadPlayerData(playerID);

                if (existingPlayer != null) {
                    // If player has no celestial bodies, initialize new game
                    if (existingPlayer.bodycount == 0) {
                        GameInitializer.initializeNewGame(existingPlayer);
                        DatabaseManager.saveGameState(existingPlayer);
                    }

                    // Store in local cache
                    playerDatabase.put(playerID, existingPlayer);

                    JOptionPane.showMessageDialog(
                            frame,
                            "Welcome back, Commander " + existingPlayer.getName() + "!\n" +
                                    "Your game has been loaded from the database.",
                            "Login Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    openGameGUI(existingPlayer);
                } else {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Error loading player data. Please try again.",
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Invalid credentials. Please check your Player ID and password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void openGameGUI(Player player) {
        frame.dispose();
        new GameGUI(player, playerDatabase).setVisible(true);
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Test database connection first
        System.out.println("Testing database connection...");
        try {
            Connection conn = DatabaseManager.getConnection();
            System.out.println("✅ SUCCESS! Connected to database!");
            System.out.println("Database: " + conn.getCatalog());
            conn.close();
        } catch (SQLException e) {
            System.out.println("❌ FAILED! Could not connect to database");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // If connection works, start the game
        SwingUtilities.invokeLater(() -> {
            new MainMenuGUI().show();
        });
    }
}