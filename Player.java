import java.util.Scanner;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JOptionPane;

class Player {
    private String playerID;
    public String name;
    public CelestialBody[] celestialBodies;
    public int bodycount;
    public int techlevel;
    public int credits;
    public int totalearnedcredits;
    public Technology tech;
    public int planetcount;
    public Planet[] planets;
    private String password;

    // Constructor for new player (used during registration)
    public Player(String n, String pw) {
        name = n;
        password = pw;
        celestialBodies = new CelestialBody[12];
        planets = new Planet[4];
        bodycount = 0;
        planetcount = 0;
        techlevel = 1;
        credits = 500;
        totalearnedcredits = 0;
        tech = new Technology();
        playerID = generateID();
    }

    // Constructor for loading existing player from database
    public Player(String n, String passwordHash, String pid) {
        name = n;
        password = passwordHash;
        playerID = pid;
        celestialBodies = new CelestialBody[12];
        planets = new Planet[4];
        bodycount = 0;
        planetcount = 0;
        techlevel = 1;
        credits = 500;
        totalearnedcredits = 0;
        tech = new Technology();
    }

    private String generateID() {
        Random rand = new Random();
        int randomNum = 10000 + rand.nextInt(90000);
        return name + "_" + randomNum;
    }

    public void addPlanet(Planet p) {
        if (planetcount < 4 && bodycount < 12) {
            planets[planetcount++] = p;
            celestialBodies[bodycount++] = p;
        }
    }

    public void addNebula(Nebula n) {
        if (bodycount < 12) {
            celestialBodies[bodycount++] = n;
        }
    }

    public void addAsteroid(Asteroid a) {
        if (bodycount < 12) {
            celestialBodies[bodycount++] = a;
        }
    }

    public String discoverNewPlanet() {
        if (tech.getTechLevel() >= 2) {
            int creditsEarned = 100;
            credits += creditsEarned;
            totalearnedcredits += creditsEarned;

            // Record in database
            DatabaseManager.earnCredits(playerID, creditsEarned, "Discovered New Planet");

            String[] types = {"Terrestrial", "Desert", "Ice Giant", "Toxic"};
            String newname = "Planet X-" + new Random().nextInt(1000);
            String newtype = types[new Random().nextInt(4)];
            boolean habitable = (newtype.equals("Terrestrial") || newtype.equals("Desert"));

            addPlanet(new Planet(newname, newtype,
                    new Random().nextInt(500) + 100,
                    new Random().nextInt(600) + 200,
                    new Random().nextInt(400) + 100,
                    habitable
            ));

            // Save game state after discovering planet
            DatabaseManager.saveGameState(this);

            return "You discovered a habitable planet! +100 credits\n" +
                    "Current credits: " + credits + "\n" +
                    "New planet " + newname + "(" + newtype + ") added to your celestial bodies!";
        } else {
            return "Tech level too low. Reach Level 2 to discover planets";
        }
    }

    public String rescueCivilians() {
        int reward = new Random().nextInt(100) + 50;
        credits += reward;
        totalearnedcredits += reward;

        // Record in database
        DatabaseManager.earnCredits(playerID, reward, "Rescued Civilians");

        return "You rescued stranded civilians and earned " + reward + " credits!\n" +
                "Current credits: " + credits;
    }

    public String defendFromAliens() {
        boolean success = new Random().nextBoolean();
        if (success) {
            int creditsEarned = 75;
            credits += creditsEarned;
            totalearnedcredits += creditsEarned;

            // Record in database
            DatabaseManager.earnCredits(playerID, creditsEarned, "Defended from Aliens");

            return "You defended your planet from alien attack! +75 credits\n" +
                    "Current credits: " + credits;
        } else {
            return "Aliens retreated before you arrived. No reward.";
        }
    }

    public String scanAnomalies() {
        int find = new Random().nextInt(3);
        if (find == 0) {
            int creditsEarned = 150;
            credits += creditsEarned;
            totalearnedcredits += creditsEarned;

            // Record in database
            DatabaseManager.earnCredits(playerID, creditsEarned, "Scanned Anomalies");

            return "Anomaly detected! You earned 150 credits.\n" +
                    "Current credits: " + credits;
        } else {
            return "No significant anomalies found.";
        }
    }

    public void checkEarnedCredits() {
        JOptionPane.showMessageDialog(
                null,
                "Current Credits: " + this.credits + "\n" +
                        "Total Credits Earned: " + this.totalearnedcredits,
                "Credits Summary",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void checkGalaxyVictory() {
        // First check locally
        int conqueredcount = 0;
        int totalbodies = bodycount;

        for (int i = 0; i < bodycount; i++) {
            if (celestialBodies[i].conquered) {
                conqueredcount++;
            }
        }

        StringBuilder message = new StringBuilder();
        message.append(".....Galaxy Victory Conditions.....\n");
        message.append("1. Conquered at least 80% of the Celestial bodies (")
                .append(conqueredcount).append("/").append(totalbodies).append(")\n");
        message.append("2. Reached Technology Level 10 (Current: ")
                .append(techlevel).append(")\n");
        message.append("3. Have at least 5000 credits (Current: ")
                .append(credits).append(")\n\n");

        boolean condition1 = (conqueredcount >= totalbodies * 0.8);
        boolean condition2 = (techlevel >= 10);
        boolean condition3 = (credits >= 5000);

        if (condition1 && condition2 && condition3) {
            message.append("Congratulations! You have conquered the galaxy!");

            // Check victory in database (will trigger sp_CheckGalaxyVictory)
            String dbResult = DatabaseManager.checkGalaxyVictory(playerID);

            JOptionPane.showMessageDialog(
                    null,
                    message.toString() + "\n\n" + dbResult,
                    "Victory!",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // End game session
            DatabaseManager.endGameSession(playerID);
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
                    null,
                    message.toString(),
                    "Victory Status",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getName() {
        return name;
    }

    public boolean checkPassword(String pw) {
        return this.password.equals(DatabaseManager.hashPassword(pw));
    }

    public String researchTech() {
        String result = tech.conductResearch();

        // Get research points earned from result (parse the string)
        // This is a simple approach - you might want to modify conductResearch to return the points
        int pointsEarned = 0;
        if (result.contains("You gained: ")) {
            try {
                String[] parts = result.split("You gained: ")[1].split(" research points")[0].split(" ");
                pointsEarned = Integer.parseInt(parts[0]);
            } catch (Exception e) {
                pointsEarned = 50; // default
            }
        }

        // Record research in database
        DatabaseManager.conductResearch(playerID, pointsEarned);

        // Save game state after research
        DatabaseManager.saveGameState(this);

        return result;
    }

    // Save game state to database
    public void saveGame() {
        boolean success = DatabaseManager.saveGameState(this);
        if (success) {
            JOptionPane.showMessageDialog(
                    null,
                    "Game saved successfully!",
                    "Save Game",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to save game. Please try again.",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
