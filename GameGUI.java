import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Random;

public class GameGUI extends JFrame {
    private Player player;
    private HashMap<String, Player> playerDatabase;
    private JPanel mainPanel;
    private JPanel contentPanel;

    public GameGUI(Player player, HashMap<String, Player> playerDatabase) {
        this.player = player;
        this.playerDatabase = playerDatabase;
        initialize();
    }

    private void initialize() {
        setTitle("Galactic Dominion - Commander " + player.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(30, 30, 70));
        menuBar.setForeground(Color.WHITE);

        // Game menu
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setForeground(Color.WHITE);

        JMenuItem saveItem = new JMenuItem("Save Game");
        saveItem.addActionListener(e -> saveGame());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitGame());

        gameMenu.add(saveItem);
        gameMenu.add(exitItem);
        menuBar.add(gameMenu);

        setJMenuBar(menuBar);

        // Create side panel with buttons
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(new Color(30, 30, 70));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] menuItems = {
                "View Celestial Bodies",
                "Upgrade Technology",
                "Earn Credits",
                "Research Technology",
                "View Technology Status",
                "Check Victory Conditions"
        };

        for (String item : menuItems) {
            JButton button = new JButton(item);
            styleMenuButton(button);
            button.addActionListener(new MenuButtonListener(item));
            sidePanel.add(button);
            sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Create content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.BLACK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add welcome message initially
        JLabel welcomeLabel = new JLabel(
                "<html><div style='text-align: center;'>" +
                        "<h1>Welcome, Commander " + player.getName() + "!</h1>" +
                        "<p>Your journey to conquer the galaxy begins now.</p>" +
                        "<p>Select an option from the menu to begin.</p>" +
                        "</div></html>",
                SwingConstants.CENTER
        );
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        contentPanel.add(welcomeLabel, BorderLayout.CENTER);

        mainPanel.add(sidePanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void styleMenuButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setBackground(new Color(50, 50, 100));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 200), 1));
    }

    private void saveGame() {
        // Show saving message
        JOptionPane optionPane = new JOptionPane(
                "Saving game to database...",
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{},
                null
        );

        JDialog dialog = optionPane.createDialog(this, "Saving");

        // Save in background thread to prevent UI freezing
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return DatabaseManager.saveGameState(player);
            }

            @Override
            protected void done() {
                dialog.dispose();
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(
                                GameGUI.this,
                                "Game saved successfully to database!\n\n" +
                                        "Player: " + player.getName() + "\n" +
                                        "Player ID: " + player.getPlayerID() + "\n" +
                                        "Credits: " + player.credits + "\n" +
                                        "Tech Level: " + player.techlevel + "\n" +
                                        "Celestial Bodies: " + player.bodycount,
                                "Save Successful",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                GameGUI.this,
                                "Failed to save game. Please check database connection.",
                                "Save Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            GameGUI.this,
                            "Error saving game: " + e.getMessage(),
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        dialog.setVisible(true);
        worker.execute();
    }

    private void exitGame() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Do you want to save before exiting?",
                "Exit Game",
                JOptionPane.YES_NO_CANCEL_OPTION
        );

        if (confirm == JOptionPane.CANCEL_OPTION) {
            return; // Don't exit
        }

        if (confirm == JOptionPane.YES_OPTION) {
            // Save game
            boolean saved = DatabaseManager.saveGameState(player);
            if (saved) {
                JOptionPane.showMessageDialog(
                        this,
                        "Game saved successfully!",
                        "Save Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        }

        // End game session in database
        DatabaseManager.endGameSession(player.getPlayerID());

        dispose();
        new MainMenuGUI().show();
    }

    private class MenuButtonListener implements ActionListener {
        private String menuItem;

        public MenuButtonListener(String menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            contentPanel.removeAll();

            switch (menuItem) {
                case "View Celestial Bodies":
                    showCelestialBodiesMenu();
                    break;
                case "Upgrade Technology":
                    showUpgradeTechnology();
                    break;
                case "Earn Credits":
                    showEarnCreditsMenu();
                    break;
                case "Research Technology":
                    String researchResult = player.researchTech();
                    JOptionPane.showMessageDialog(
                            GameGUI.this,
                            researchResult,
                            "Research Complete",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    break;

                case "View Technology Status":
                    showTechnologyStatus();
                    break;
                case "Check Victory Conditions":
                    showVictoryConditions();
                    break;
            }

            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    private void showCelestialBodiesMenu() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(30, 30, 70));
        tabbedPane.setForeground(Color.WHITE);

        // Planets tab
        JPanel planetsPanel = new JPanel();
        planetsPanel.setLayout(new BoxLayout(planetsPanel, BoxLayout.Y_AXIS));
        planetsPanel.setBackground(Color.BLACK);
        planetsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < player.bodycount; i++) {
            CelestialBody body = player.celestialBodies[i];
            if (body.type.equals("Terrestrial") || body.type.equals("Desert") ||
                    body.type.equals("Ice Giant") || body.type.equals("Toxic")) {
                JButton planetButton = new JButton(body.name + " - " + (body.conquered ? "Conquered" : "Not Conquered"));
                styleCelestialBodyButton(planetButton);
                planetButton.addActionListener(e -> showPlanetMenu((Planet) body));
                planetsPanel.add(planetButton);
                planetsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        JScrollPane planetsScroll = new JScrollPane(planetsPanel);
        planetsScroll.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.addTab("Planets", planetsScroll);

        // Nebulas tab
        JPanel nebulasPanel = new JPanel();
        nebulasPanel.setLayout(new BoxLayout(nebulasPanel, BoxLayout.Y_AXIS));
        nebulasPanel.setBackground(Color.BLACK);
        nebulasPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < player.bodycount; i++) {
            CelestialBody body = player.celestialBodies[i];
            if (body.type.equals("Nebula")) {
                JButton nebulaButton = new JButton(body.name + " - " + (body.conquered ? "Conquered" : "Not Conquered"));
                styleCelestialBodyButton(nebulaButton);
                nebulaButton.addActionListener(e -> showNebulaMenu((Nebula) body));
                nebulasPanel.add(nebulaButton);
                nebulasPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        JScrollPane nebulasScroll = new JScrollPane(nebulasPanel);
        nebulasScroll.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.addTab("Nebulas", nebulasScroll);

        // Asteroids tab
        JPanel asteroidsPanel = new JPanel();
        asteroidsPanel.setLayout(new BoxLayout(asteroidsPanel, BoxLayout.Y_AXIS));
        asteroidsPanel.setBackground(Color.BLACK);
        asteroidsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < player.bodycount; i++) {
            CelestialBody body = player.celestialBodies[i];
            if (body.type.equals("Asteroid")) {
                JButton asteroidButton = new JButton(body.name + " - " + (body.conquered ? "Conquered" : "Not Conquered"));
                styleCelestialBodyButton(asteroidButton);
                asteroidButton.addActionListener(e -> showAsteroidMenu((Asteroid) body));
                asteroidsPanel.add(asteroidButton);
                asteroidsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        JScrollPane asteroidsScroll = new JScrollPane(asteroidsPanel);
        asteroidsScroll.setBorder(BorderFactory.createEmptyBorder());
        tabbedPane.addTab("Asteroids", asteroidsScroll);

        // Victory condition button
        JButton victoryButton = new JButton("Check Galaxy Victory Condition");
        victoryButton.addActionListener(e -> showVictoryConditions());
        victoryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        styleMenuButton(victoryButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(victoryButton);

        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.add(panel, BorderLayout.CENTER);
    }

    private void styleCelestialBodyButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 30));
        button.setBackground(new Color(50, 50, 100));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private void showPlanetMenu(Planet planet) {
        JDialog dialog = new JDialog(this, "Planet: " + planet.name, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.BLACK);

        // Status panel
        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setBackground(Color.BLACK);
        statusArea.setForeground(Color.WHITE);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        StringBuilder status = new StringBuilder();
        status.append("Planet: ").append(planet.name).append("\n");
        status.append("Type: ").append(planet.type).append("\n");
        status.append("Population: ").append(planet.population).append("\n");
        status.append("Resources: ").append(planet.resources).append("\n");
        status.append("Defense: ").append(planet.defense).append("\n");
        status.append("Habitable: ").append(planet.habitable ? "Yes" : "No").append("\n");
        status.append("Terraformed: ").append(planet.terraformed ? "Yes" : "No").append("\n");
        status.append("Scanned: ").append(planet.scanned ? "Yes" : "No").append("\n");
        status.append("Conquered: ").append(planet.conquered ? "Yes" : "No").append("\n");

        statusArea.setText(status.toString());
        panel.add(new JScrollPane(statusArea), BorderLayout.NORTH);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonsPanel.setBackground(Color.BLACK);

        String[] options = {
                "View Status", "Increase Population", "Increase Resources",
                "Increase Defense", "Terraform Planet", "Check Victory Condition"
        };

        for (String option : options) {
            JButton button = new JButton(option);
            styleMenuButton(button);
            button.addActionListener(e -> handlePlanetAction(planet, option, dialog));
            buttonsPanel.add(button);
        }

        JButton returnButton = new JButton("Return");
        styleMenuButton(returnButton);
        returnButton.addActionListener(e -> dialog.dispose());
        buttonsPanel.add(returnButton);

        panel.add(buttonsPanel, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handlePlanetAction(Planet planet, String action, JDialog dialog) {
        switch (action) {
            case "View Status":
                planet.showStatus();
                break;

            case "Increase Population":
                if (!planet.canModify()) {
                    JOptionPane.showMessageDialog(dialog,
                            "You must complete the required task first!",
                            "Task Required",
                            JOptionPane.WARNING_MESSAGE);
                    planet.performTask();
                    break;
                }
                if (planet.performedSimpleTask()) {
                    String input = JOptionPane.showInputDialog(dialog,
                            "Task successful! Enter amount to increase:");
                    try {
                        int inc = Integer.parseInt(input);
                        planet.population += inc;

                        // Save to database after modification
                        DatabaseManager.saveGameState(player);

                        JOptionPane.showMessageDialog(dialog,
                                "New Population: " + planet.population + "\nGame saved!",
                                "Population Increased",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(dialog,
                                "Invalid number entered!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Task Failed! Cannot increase population this time.",
                            "Task Failed",
                            JOptionPane.WARNING_MESSAGE);
                }
                break;

            case "Increase Resources":
                if (!planet.canModify()) {
                    JOptionPane.showMessageDialog(dialog,
                            "You must complete the required task first!",
                            "Task Required",
                            JOptionPane.WARNING_MESSAGE);
                    planet.performTask();
                    break;
                }
                if (planet.performedSimpleTask()) {
                    String input = JOptionPane.showInputDialog(dialog,
                            "Task successful! Enter amount to increase:");
                    try {
                        int inc = Integer.parseInt(input);
                        planet.resources += inc;

                        // Save to database
                        DatabaseManager.saveGameState(player);

                        JOptionPane.showMessageDialog(dialog,
                                "New Resources: " + planet.resources + "\nGame saved!",
                                "Resources Increased",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(dialog,
                                "Invalid number entered!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Task failed! Cannot increase resources this time.",
                            "Task Failed",
                            JOptionPane.WARNING_MESSAGE);
                }
                break;

            case "Increase Defense":
                if (!planet.canModify()) {
                    JOptionPane.showMessageDialog(dialog,
                            "You must complete the required tasks first!",
                            "Task Required",
                            JOptionPane.WARNING_MESSAGE);
                    planet.performTask();
                    break;
                }
                if (planet.performedSimpleTask()) {
                    String input = JOptionPane.showInputDialog(dialog,
                            "Task successful! Enter amount to increase:");
                    try {
                        int inc = Integer.parseInt(input);
                        planet.defense += inc;

                        // Save to database
                        DatabaseManager.saveGameState(player);

                        JOptionPane.showMessageDialog(dialog,
                                "New Defense: " + planet.defense + "\nGame saved!",
                                "Defense Increased",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(dialog,
                                "Invalid number entered!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Task failed! Cannot increase defense this time",
                            "Task Failed",
                            JOptionPane.WARNING_MESSAGE);
                }
                break;

            case "Terraform Planet":
                planet.terraformPlanet();
                // Save after terraforming
                DatabaseManager.saveGameState(player);
                break;

            case "Check Victory Condition":
                if (planet.checkVictoryCondition()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Victory condition met for this planet!",
                            "Victory",
                            JOptionPane.INFORMATION_MESSAGE);
                    planet.conquered = true;

                    // Save conquered status
                    DatabaseManager.saveGameState(player);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Victory condition not yet met for this planet.",
                            "Not Conquered",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                planet.showVictoryRequirements();
                break;
        }
    }

    private void showNebulaMenu(Nebula nebula) {
        JDialog dialog = new JDialog(this, "Nebula: " + nebula.name, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.BLACK);

        // Status panel
        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setBackground(Color.BLACK);
        statusArea.setForeground(Color.WHITE);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        StringBuilder status = new StringBuilder();
        status.append("Nebula: ").append(nebula.name).append("\n");
        status.append("Density: ").append(nebula.density).append("\n");
        status.append("Radiation Level: ").append(nebula.radiationlevel).append("\n");
        status.append("Visibility Reduction: ").append(nebula.visibilityreduction ? "Yes" : "No").append("\n");
        status.append("Mapped: ").append(nebula.mapped ? "Yes" : "No").append("\n");
        status.append("Resources Extracted: ").append(nebula.resourcesextracted ? "Yes" : "No").append("\n");
        status.append("Conquered: ").append(nebula.conquered ? "Yes" : "No").append("\n");

        statusArea.setText(status.toString());
        panel.add(new JScrollPane(statusArea), BorderLayout.NORTH);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonsPanel.setBackground(Color.BLACK);

        String[] options = {
                "View Status", "Explore Nebula", "Adjust Radiation Level",
                "Extract Resources", "Check Victory Condition"
        };

        for (String option : options) {
            JButton button = new JButton(option);
            styleMenuButton(button);
            button.addActionListener(e -> handleNebulaAction(nebula, option, dialog));
            buttonsPanel.add(button);
        }

        JButton returnButton = new JButton("Return");
        styleMenuButton(returnButton);
        returnButton.addActionListener(e -> dialog.dispose());
        buttonsPanel.add(returnButton);

        panel.add(buttonsPanel, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleNebulaAction(Nebula nebula, String action, JDialog dialog) {
        switch (action) {
            case "View Status":
                nebula.showStatus();
                break;
            case "Explore Nebula":
                int creditsEarned = nebula.exploreNebula();
                player.credits += creditsEarned;
                player.totalearnedcredits += creditsEarned;
                JOptionPane.showMessageDialog(dialog,
                        "You found " + creditsEarned + " credits!\nCurrent credits: " + player.credits,
                        "Exploration Result",
                        JOptionPane.INFORMATION_MESSAGE
                );
                break;
            case "Adjust Radiation Level":
                nebula.adjustRadiation();
                break;
            case "Extract Resources":
                nebula.performTask();
                break;
            case "Check Victory Condition":
                if (nebula.checkVictoryCondition()) {
                    JOptionPane.showMessageDialog(dialog, "Victory condition met for this nebula!", "Victory", JOptionPane.INFORMATION_MESSAGE);
                    nebula.conquered = true;
                } else {
                    JOptionPane.showMessageDialog(dialog, "Victory condition not yet met for this nebula.", "Not Conquered", JOptionPane.INFORMATION_MESSAGE);
                }
                nebula.showVictoryRequirements();
                break;
        }
    }

    private void showAsteroidMenu(Asteroid asteroid) {
        JDialog dialog = new JDialog(this, "Asteroid: " + asteroid.name, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.BLACK);

        // Status panel
        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setBackground(Color.BLACK);
        statusArea.setForeground(Color.WHITE);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        StringBuilder status = new StringBuilder();
        status.append("Asteroid: ").append(asteroid.name).append("\n");
        status.append("Mass: ").append(asteroid.mass).append(" kg\n");
        status.append("Diameter: ").append(asteroid.diameter).append(" km\n");
        status.append("Orbit Radius: ").append(asteroid.orbital_radius).append(" AU\n");
        status.append("Velocity: ").append(asteroid.velocity).append(" km/s\n");
        status.append("Hazardous: ").append(asteroid.hazardous ? "Yes" : "No").append("\n");
        status.append("Mined: ").append(asteroid.mined ? "Yes" : "No").append("\n");
        status.append("Orbit Stabilized: ").append(asteroid.orbitstabilized ? "Yes" : "No").append("\n");
        status.append("Conquered: ").append(asteroid.conquered ? "Yes" : "No").append("\n");

        statusArea.setText(status.toString());
        panel.add(new JScrollPane(statusArea), BorderLayout.NORTH);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonsPanel.setBackground(Color.BLACK);

        String[] options = {
                "View Status", "Mine Asteroid", "Adjust Velocity",
                "Stabilize Orbit", "Check Victory Condition"
        };

        for (String option : options) {
            JButton button = new JButton(option);
            styleMenuButton(button);
            button.addActionListener(e -> handleAsteroidAction(asteroid, option, dialog));
            buttonsPanel.add(button);
        }

        JButton returnButton = new JButton("Return");
        styleMenuButton(returnButton);
        returnButton.addActionListener(e -> dialog.dispose());
        buttonsPanel.add(returnButton);

        panel.add(buttonsPanel, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleAsteroidAction(Asteroid asteroid, String action, JDialog dialog) {
        switch (action) {
            case "View Status":
                asteroid.showStatus();
                break;
            case "Mine Asteroid":
                asteroid.mineAsteroid();
                break;
            case "Adjust Velocity":
                asteroid.adjustVelocity();
                break;
            case "Stabilize Orbit":
                asteroid.stabilizeOrbit();
                break;
            case "Check Victory Condition":
                if (asteroid.checkVictoryCondition()) {
                    JOptionPane.showMessageDialog(dialog, "Victory condition met for this asteroid", "Victory", JOptionPane.INFORMATION_MESSAGE);
                    asteroid.conquered = true;
                } else {
                    JOptionPane.showMessageDialog(dialog, "Victory condition not yet met for this asteroid", "Not Conquered", JOptionPane.INFORMATION_MESSAGE);
                }
                asteroid.showVictoryRequirements();
                break;
        }
    }

    private void showUpgradeTechnology() {
        if (player.credits < 200) {
            JOptionPane.showMessageDialog(
                    this,
                    "Not enough credits to upgrade!\nYour current credits are " + player.credits,
                    "Insufficient Credits",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int secretNumber = new java.util.Random().nextInt(10) + 1;
        int attempts = 3;

        while (attempts > 0) {
            String input = JOptionPane.showInputDialog(
                    this,
                    "Guess a secret number between 1 and 10\nAttempts left: " + attempts,
                    "Upgrade Technology",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) break; // User canceled

            try {
                int guess = Integer.parseInt(input);
                if (guess == secretNumber) {
                    player.techlevel++;
                    player.credits -= 200;

                    // Save to database after upgrade
                    DatabaseManager.saveGameState(player);

                    JOptionPane.showMessageDialog(
                            this,
                            "Correct! Technology upgraded to level " + player.techlevel + "!\n" +
                                    "Your credits after upgrading: " + player.credits + "\n" +
                                    "Game saved!",
                            "Upgrade Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                } else {
                    attempts--;
                    String message = "Your guess is too " + (guess < secretNumber ? "low" : "high");
                    if (attempts > 0) {
                        message += ". Try again.";
                    } else {
                        message += ". Out of attempts. The correct number was " + secretNumber + ".";
                    }
                    JOptionPane.showMessageDialog(
                            this,
                            message,
                            "Incorrect Guess",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid number!",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    private void showEarnCreditsMenu() {
        JDialog dialog = new JDialog(this, "Earn Credits", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.BLACK);

        JPanel buttonsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonsPanel.setBackground(Color.BLACK);

        String[] options = {
                "Discover New Planets", "Sell Resources", "Rescue Civilians",
                "Defend from Alien Attacks", "Scan Anomalies", "Win a Quiz",
                "Check Earned Credits"
        };

        for (String option : options) {
            JButton button = new JButton(option);
            styleMenuButton(button);
            button.addActionListener(e -> handleEarnCreditsAction(option, dialog));
            buttonsPanel.add(button);
        }

        JButton returnButton = new JButton("Return");
        styleMenuButton(returnButton);
        returnButton.addActionListener(e -> dialog.dispose());
        buttonsPanel.add(returnButton);

        panel.add(buttonsPanel, BorderLayout.CENTER);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void handleEarnCreditsAction(String action, JDialog dialog) {
        String result = "";

        switch (action) {
            case "Discover New Planets":
                result = player.discoverNewPlanet();
                break;
            case "Scan Anomalies":
                result = player.scanAnomalies();
                break;
            case "Defend from Alien Attacks":
                result = player.defendFromAliens();
                break;
            case "Rescue Civilians":
                result = player.rescueCivilians();
                break;
            case "Win a Quiz":
                spaceQuiz();
                return;
            case "Check Earned Credits":
                checkEarnedCredits();
                return;
        }

        // Show result in a dialog
        JOptionPane.showMessageDialog(
                dialog,
                result,
                action + " Result",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void sellResources() {
        int total = 0;
        for (int i = 0; i < player.bodycount; i++) {
            CelestialBody body = player.celestialBodies[i];
            if (!body.type.equals("Nebula") && !body.type.equals("Asteroid")) {
                Planet p = (Planet) body;
                if (p != null) total += p.resources;
            }
        }

        String input = JOptionPane.showInputDialog(
                this,
                "You have " + total + " resources. Enter amount to sell:",
                "Sell Resources",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null) return; // User canceled

        try {
            int amount = Integer.parseInt(input);

            if (amount > total) {
                JOptionPane.showMessageDialog(
                        this,
                        "Not enough Resources",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            player.credits += amount / 2;
            player.totalearnedcredits += amount / 2;
            int left = amount;

            for (int i = 0; i < player.bodycount; i++) {
                CelestialBody body = player.celestialBodies[i];
                if (!body.type.equals("Nebula") && !body.type.equals("Asteroid")) {
                    Planet p = (Planet) body;
                    if (p != null) {
                        int take = Math.min(left, p.resources);
                        p.resources -= take;
                        left -= take;
                        if (left == 0) break;
                    }
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    "You sold " + amount + " resources for " + (amount / 2) + " credits.",
                    "Sale Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid number!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void spaceQuiz() {
        String[] questions = {
                "Which planet is the Red Planet? ",
                "Which planet has the most moons?",
                "Which planet is not a gas giant? ",
                "Which planet has the longest day?",
                "Which planet is most like Earth? "
        };

        String[][] choices = {
                {"Venus", "Mars", "Jupiter", "Saturn"},
                {"Earth", "Saturn", "Jupiter", "Neptune"},
                {"Saturn", "Neptune", "Mars", "Uranus"},
                {"Mercury", "Venus", "Earth", "Mars"},
                {"Mars", "Venus", "Mercury", "Neptune"}
        };

        int[] correctAnswers = {2, 3, 3, 2, 1};
        int questionIndex = new Random().nextInt(5);

        Object[] options = {
                "1. " + choices[questionIndex][0],
                "2. " + choices[questionIndex][1],
                "3. " + choices[questionIndex][2],
                "4. " + choices[questionIndex][3]
        };

        int answer = JOptionPane.showOptionDialog(
                this,
                questions[questionIndex],
                "Space Quiz",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (answer == correctAnswers[questionIndex] - 1) {
            int reward = new Random().nextInt(901) + 100;
            player.credits += reward;
            player.totalearnedcredits += reward;
            JOptionPane.showMessageDialog(
                    this,
                    "Correct! You earned " + reward + " credits\n" +
                            "Total credits: " + player.credits,
                    "Quiz Result",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Wrong! Correct answer is: " +
                            choices[questionIndex][correctAnswers[questionIndex] - 1],
                    "Quiz Result",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void showTechnologyStatus() {
        JDialog dialog = new JDialog(this, "Technology Status", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.BLACK);

        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setBackground(Color.BLACK);
        statusArea.setForeground(Color.WHITE);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        StringBuilder status = new StringBuilder();
        status.append("------Technology Status--------\n");
        status.append("Tech Level: ").append(player.tech.techlevel).append("\n");
        status.append("Research points: ").append(player.tech.researchpoints).append("\n");
        status.append("New Discovered Planet: ").append(player.tech.newplanetsdiscoverd ? "Yes" : "No").append("\n");
        status.append("Advanced resources found: ").append(player.tech.advancedresourcesfound ? "Yes" : "No").append("\n");

        statusArea.setText(status.toString());
        panel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        styleMenuButton(closeButton);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(closeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showVictoryConditions() {
        int conqueredCount = 0;
        int totalBodies = player.bodycount;

        for (int i = 0; i < player.bodycount; i++) {
            if (player.celestialBodies[i].conquered) {
                conqueredCount++;
            }
        }

        StringBuilder message = new StringBuilder();
        message.append(".....Galaxy Victory Conditions.....\n");
        message.append("1. Conquered at least 80% of the Celestial bodies (").append(conqueredCount).append("/").append(totalBodies).append(")\n");
        message.append("2. Reached Technology Level 10 (Current: ").append(player.techlevel).append(")\n");
        message.append("3. Have at least 5000 credits (Current: ").append(player.credits).append(")\n\n");

        boolean condition1 = (conqueredCount >= totalBodies * 0.8);
        boolean condition2 = (player.techlevel >= 10);
        boolean condition3 = (player.credits >= 5000);

        if (condition1 && condition2 && condition3) {
            message.append("Congratulations! You have conquered the galaxy!");
            JOptionPane.showMessageDialog(
                    this,
                    message.toString(),
                    "Victory!",
                    JOptionPane.INFORMATION_MESSAGE
            );
            System.exit(0);
        } else {
            message.append("Galaxy Victory conditions are not met yet.\n");
            if (!condition1) {
                message.append("- Need to conquer more celestial bodies\n");
            }
            if (!condition2) {
                message.append("- Need higher technology level\n");
            }
            if (!condition3) {
                message.append("- Need more credits\n");
            }

            JOptionPane.showMessageDialog(
                    this,
                    message.toString(),
                    "Victory Status",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    private void checkEarnedCredits() {
        JOptionPane.showMessageDialog(
                this,
                "Current Credits: " + player.credits + "\n" +
                        "Total Credits Earned: " + player.totalearnedcredits,
                "Credits Summary",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}