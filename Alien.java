import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Alien {
    public String species;
    public int attackpower;

    public Alien(String s, int a) {
        species = s;
        attackpower = a;
    }

    public void attack(Player player) {
        List<CelestialBody> potentialTargets = new ArrayList<>();

        // Find unconquered planets first
        for (int i = 0; i < player.bodycount; i++) {
            CelestialBody body = player.celestialBodies[i];
            if (!body.conquered &&
                    (body.type.equals("Terrestrial") ||
                            body.type.equals("Desert") ||
                            body.type.equals("Ice Giant") ||
                            body.type.equals("Toxic"))) {
                potentialTargets.add(body);
            }
        }

        // If no unconquered planets, target any planet
        if (potentialTargets.isEmpty()) {
            for (int i = 0; i < player.bodycount; i++) {
                CelestialBody body = player.celestialBodies[i];
                if (body.type.equals("Terrestrial") ||
                        body.type.equals("Desert") ||
                        body.type.equals("Ice Giant") ||
                        body.type.equals("Toxic")) {
                    potentialTargets.add(body);
                }
            }
        }

        if (potentialTargets.isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "No planets available to attack!",
                    "Alien Attack",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Select random target
        CelestialBody target = potentialTargets.get(new Random().nextInt(potentialTargets.size()));
        Planet planet = (Planet) target;

        // Calculate damage
        int populationBefore = planet.population;
        int resourcesBefore = planet.resources;

        boolean success;
        int damagePopulation = 0;
        int damageResources = 0;

        // Build attack result message
        StringBuilder result = new StringBuilder();
        result.append("🛸 ALIEN ATTACK! 🛸\n\n");
        result.append("Species: ").append(species).append("\n");
        result.append("Target: ").append(planet.name).append("\n");
        result.append("Attack Power: ").append(attackpower).append("\n\n");

        if (planet.defense >= attackpower) {
            success = false;
            result.append("✅ DEFENSE SUCCESSFUL!\n\n");
            result.append("Your planet's defense (").append(planet.defense)
                    .append(") successfully repelled the attack (").append(attackpower).append(")!");
        } else {
            success = true;
            result.append("⚠️ PLANET DAMAGED!\n\n");

            // Apply damage
            damagePopulation = attackpower / 2;
            damageResources = attackpower / 4;

            planet.population = Math.max(0, planet.population - damagePopulation);
            planet.resources = Math.max(0, planet.resources - damageResources);

            result.append("Population:\n");
            result.append("  Before: ").append(populationBefore).append("\n");
            result.append("  After: ").append(planet.population).append("\n");
            result.append("  Lost: -").append(populationBefore - planet.population).append("\n\n");

            result.append("Resources:\n");
            result.append("  Before: ").append(resourcesBefore).append("\n");
            result.append("  After: ").append(planet.resources).append("\n");
            result.append("  Lost: -").append(resourcesBefore - planet.resources).append("\n\n");

            result.append("💡 TIP: Increase defense to protect against future attacks!");
        }

        // Get BodyID for database logging
        int targetBodyID = getBodyIDFromDatabase(planet.name);

        if (targetBodyID != -1) {
            // Record attack in database using stored procedure
            DatabaseManager.recordAlienAttack(
                    species,
                    attackpower,
                    targetBodyID,
                    success,
                    damagePopulation,
                    damageResources
            );

            // Save updated game state if damage occurred
            if (success) {
                DatabaseManager.saveGameState(player);
            }
        }

        // Show attack result in GUI
        JOptionPane.showMessageDialog(
                null,
                result.toString(),
                "Alien Attack Report",
                success ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Helper method to get BodyID from database
    private int getBodyIDFromDatabase(String planetName) {
        try {
            java.sql.Connection conn = DatabaseManager.getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT BodyID FROM Celestial_Bodies WHERE Name = ?"
            );
            stmt.setString(1, planetName);
            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int bodyID = rs.getInt("BodyID");
                rs.close();
                stmt.close();
                conn.close();
                return bodyID;
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Static method to trigger random alien attack
    public static void triggerRandomAttack(Player player) {
        String[] species = {
                "Zorglaxian Hive",
                "Kree Empire",
                "Skrull Armada",
                "Chitauri Swarm",
                "Thanos Legion"
        };

        Random rand = new Random();
        String alienSpecies = species[rand.nextInt(species.length)];
        int attackPower = rand.nextInt(300) + 200; // 200-500

        Alien alien = new Alien(alienSpecies, attackPower);
        alien.attack(player);
    }
}
