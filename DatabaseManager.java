import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.JOptionPane;

public class DatabaseManager {
    // Database connection details
    private static final String SERVER = "DESKTOP-BE2441N\\SQLSERVER2022"; // Your actual server name
    private static final String PORT = "1433";
    private static final String DATABASE = "GalacticDominion";

    // SQL Server Authentication
    private static final String USERNAME = "galactic_user"; // The user we created
    private static final String PASSWORD = "GalacticPass123!"; // The password we set

    private static final String CONNECTION_STRING =
            "jdbc:sqlserver://" + SERVER + ":" + PORT + ";" +
                    "databaseName=" + DATABASE + ";" +
                    "user=" + USERNAME + ";" +
                    "password=" + PASSWORD + ";" +
                    "encrypt=true;" +
                    "trustServerCertificate=true;";

    // Get database connection
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(CONNECTION_STRING);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found", e);
        }
    }

    // Hash password using SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback
        }
    }

    // Register new player
    public static String registerPlayer(String name, String password) {
        String passwordHash = hashPassword(password);

        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_RegisterPlayer(?, ?)}")) {

            stmt.setString(1, name);
            stmt.setString(2, passwordHash);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("NewPlayerID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Database Error: " + e.getMessage(),
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    // Login player
    public static boolean loginPlayer(String playerID, String password) {
        String passwordHash = hashPassword(password);

        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_LoginPlayer(?, ?)}")) {

            stmt.setString(1, playerID);
            stmt.setString(2, passwordHash);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String message = rs.getString("Message");
                return message.contains("Successful");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Database Error: " + e.getMessage(),
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    // Load player data
    public static Player loadPlayerData(String playerID) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM Players WHERE PlayerID = ?")) {

            stmt.setString(1, playerID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("Name");
                String passwordHash = rs.getString("PasswordHash");
                int techLevel = rs.getInt("TechnologyLevel");
                int credits = rs.getInt("Credits");
                int totalEarnedCredits = rs.getInt("TotalEarnedCredits");

                Player player = new Player(name, passwordHash, playerID);
                player.techlevel = techLevel;
                player.credits = credits;
                player.totalearnedcredits = totalEarnedCredits;

                // Load celestial bodies
                loadCelestialBodies(player, conn);

                // Load technology progress
                loadTechnologyProgress(player, conn);

                return player;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error loading player data: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    // Load celestial bodies for player
    private static void loadCelestialBodies(Player player, Connection conn) throws SQLException {
        String query = "SELECT cb.*, p.*, n.*, a.* " +
                "FROM Player_CelestialBodies pcb " +
                "JOIN Celestial_Bodies cb ON pcb.BodyID = cb.BodyID " +
                "LEFT JOIN Planets p ON cb.BodyID = p.BodyID " +
                "LEFT JOIN Nebulas n ON cb.BodyID = n.BodyID " +
                "LEFT JOIN Asteroids a ON cb.BodyID = a.BodyID " +
                "WHERE pcb.PlayerID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, player.getPlayerID());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("Type");
                String name = rs.getString("Name");
                boolean conquered = rs.getBoolean("Conquered");

                if (type.equals("Planet")) {
                    int population = rs.getInt("Population");
                    int resources = rs.getInt("Resources");
                    int defense = rs.getInt("Defense");
                    boolean habitable = rs.getBoolean("Habitable");
                    boolean terraformed = rs.getBoolean("Terraformed");
                    boolean scanned = rs.getBoolean("Scanned");

                    Planet planet = new Planet(name, "Terrestrial", population, resources, defense, habitable);
                    planet.terraformed = terraformed;
                    planet.scanned = scanned;
                    planet.conquered = conquered;
                    player.addPlanet(planet);

                } else if (type.equals("Nebula")) {
                    int density = rs.getInt("Density");
                    int radiationLevel = rs.getInt("RadiationLevel");
                    boolean visibilityReduction = rs.getBoolean("VisibilityReduction");
                    boolean mapped = rs.getBoolean("Mapped");
                    boolean resourcesExtracted = rs.getBoolean("ResourcesExtracted");

                    Nebula nebula = new Nebula(name, density, radiationLevel, visibilityReduction);
                    nebula.mapped = mapped;
                    nebula.resourcesextracted = resourcesExtracted;
                    nebula.conquered = conquered;
                    player.addNebula(nebula);

                } else if (type.equals("Asteroid")) {
                    double mass = rs.getDouble("Mass");
                    double diameter = rs.getDouble("Diameter");
                    double orbitRadius = rs.getDouble("OrbitRadius");
                    double velocity = rs.getDouble("Velocity");
                    boolean hazardous = rs.getBoolean("Hazardous");
                    boolean mined = rs.getBoolean("Mined");
                    boolean orbitStabilized = rs.getBoolean("OrbitStabilized");

                    Asteroid asteroid = new Asteroid(name, mass, diameter, orbitRadius, velocity, hazardous);
                    asteroid.mined = mined;
                    asteroid.orbitstabilized = orbitStabilized;
                    asteroid.conquered = conquered;
                    player.addAsteroid(asteroid);
                }
            }
        }
    }

    // Load technology progress
    private static void loadTechnologyProgress(Player player, Connection conn) throws SQLException {
        String query = "SELECT t.* FROM Technology t " +
                "JOIN Player_Technology_Progress ptp ON t.TechID = ptp.TechID " +
                "WHERE ptp.PlayerID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, player.getPlayerID());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                player.tech.researchpoints = rs.getInt("ResearchPoints");
                player.tech.techlevel = rs.getInt("TechLevel");
                player.tech.newplanetsdiscoverd = rs.getBoolean("NewPlanetsDiscovered");
                player.tech.advancedresourcesfound = rs.getBoolean("AdvancedResourcesFound");
            }
        }
    }

    // Save complete game state
    public static boolean saveGameState(Player player) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Update player data
            savePlayerData(player, conn);

            // Save celestial bodies
            saveCelestialBodies(player, conn);

            // Save technology progress
            saveTechnologyProgress(player, conn);

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error saving game: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Save player data
    private static void savePlayerData(Player player, Connection conn) throws SQLException {
        String query = "UPDATE Players SET TechnologyLevel = ?, Credits = ?, TotalEarnedCredits = ? WHERE PlayerID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, player.techlevel);
            stmt.setInt(2, player.credits);
            stmt.setInt(3, player.totalearnedcredits);
            stmt.setString(4, player.getPlayerID());
            stmt.executeUpdate();
        }
    }

    // Save celestial bodies
    private static void saveCelestialBodies(Player player, Connection conn) throws SQLException {
        for (int i = 0; i < player.bodycount; i++) {
            CelestialBody body = player.celestialBodies[i];

            // Check if body exists
            int bodyID = getCelestialBodyID(body.name, conn);

            if (bodyID == -1) {
                // Insert new celestial body
                bodyID = insertCelestialBody(body, conn);

                // Link to player
                linkCelestialBodyToPlayer(player.getPlayerID(), bodyID, conn);
            }

            // Update conquered status
            updateCelestialBodyStatus(bodyID, body.conquered, conn);

            // Update specific type data
            if (body instanceof Planet) {
                updatePlanetData((Planet) body, bodyID, conn);
            } else if (body instanceof Nebula) {
                updateNebulaData((Nebula) body, bodyID, conn);
            } else if (body instanceof Asteroid) {
                updateAsteroidData((Asteroid) body, bodyID, conn);
            }
        }
    }

    // Get celestial body ID
    private static int getCelestialBodyID(String name, Connection conn) throws SQLException {
        String query = "SELECT BodyID FROM Celestial_Bodies WHERE Name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("BodyID");
            }
        }
        return -1;
    }

    // Insert celestial body
    private static int insertCelestialBody(CelestialBody body, Connection conn) throws SQLException {
        String query = "INSERT INTO Celestial_Bodies (Name, Type, Conquered) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, body.name);
            stmt.setString(2, body.type);
            stmt.setBoolean(3, body.conquered);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    // Link celestial body to player
    private static void linkCelestialBodyToPlayer(String playerID, int bodyID, Connection conn) throws SQLException {
        String query = "IF NOT EXISTS (SELECT 1 FROM Player_CelestialBodies WHERE PlayerID = ? AND BodyID = ?) " +
                "INSERT INTO Player_CelestialBodies (PlayerID, BodyID) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerID);
            stmt.setInt(2, bodyID);
            stmt.setString(3, playerID);
            stmt.setInt(4, bodyID);
            stmt.executeUpdate();
        }
    }

    // Update celestial body conquered status
    private static void updateCelestialBodyStatus(int bodyID, boolean conquered, Connection conn) throws SQLException {
        String query = "UPDATE Celestial_Bodies SET Conquered = ? WHERE BodyID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, conquered);
            stmt.setInt(2, bodyID);
            stmt.executeUpdate();
        }
    }

    // Update planet data
    private static void updatePlanetData(Planet planet, int bodyID, Connection conn) throws SQLException {
        // Check if planet record exists
        String checkQuery = "SELECT PlanetID FROM Planets WHERE BodyID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, bodyID);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Update existing
                String updateQuery = "UPDATE Planets SET Population = ?, Resources = ?, Defense = ?, " +
                        "Habitable = ?, Terraformed = ?, Scanned = ? WHERE BodyID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setInt(1, planet.population);
                    stmt.setInt(2, planet.resources);
                    stmt.setInt(3, planet.defense);
                    stmt.setBoolean(4, planet.habitable);
                    stmt.setBoolean(5, planet.terraformed);
                    stmt.setBoolean(6, planet.scanned);
                    stmt.setInt(7, bodyID);
                    stmt.executeUpdate();
                }
            } else {
                // Insert new
                String insertQuery = "INSERT INTO Planets (BodyID, Population, Resources, Defense, Habitable, Terraformed, Scanned) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setInt(1, bodyID);
                    stmt.setInt(2, planet.population);
                    stmt.setInt(3, planet.resources);
                    stmt.setInt(4, planet.defense);
                    stmt.setBoolean(5, planet.habitable);
                    stmt.setBoolean(6, planet.terraformed);
                    stmt.setBoolean(7, planet.scanned);
                    stmt.executeUpdate();
                }
            }
        }
    }

    // Update nebula data
    private static void updateNebulaData(Nebula nebula, int bodyID, Connection conn) throws SQLException {
        String checkQuery = "SELECT NebulaID FROM Nebulas WHERE BodyID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, bodyID);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String updateQuery = "UPDATE Nebulas SET Density = ?, RadiationLevel = ?, " +
                        "VisibilityReduction = ?, Mapped = ?, ResourcesExtracted = ? WHERE BodyID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setInt(1, nebula.density);
                    stmt.setInt(2, nebula.radiationlevel);
                    stmt.setBoolean(3, nebula.visibilityreduction);
                    stmt.setBoolean(4, nebula.mapped);
                    stmt.setBoolean(5, nebula.resourcesextracted);
                    stmt.setInt(6, bodyID);
                    stmt.executeUpdate();
                }
            } else {
                String insertQuery = "INSERT INTO Nebulas (BodyID, Density, RadiationLevel, VisibilityReduction, Mapped, ResourcesExtracted) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setInt(1, bodyID);
                    stmt.setInt(2, nebula.density);
                    stmt.setInt(3, nebula.radiationlevel);
                    stmt.setBoolean(4, nebula.visibilityreduction);
                    stmt.setBoolean(5, nebula.mapped);
                    stmt.setBoolean(6, nebula.resourcesextracted);
                    stmt.executeUpdate();
                }
            }
        }
    }

    // Update asteroid data
    private static void updateAsteroidData(Asteroid asteroid, int bodyID, Connection conn) throws SQLException {
        String checkQuery = "SELECT AsteroidID FROM Asteroids WHERE BodyID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, bodyID);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String updateQuery = "UPDATE Asteroids SET Mass = ?, Diameter = ?, OrbitRadius = ?, " +
                        "Velocity = ?, Hazardous = ?, Mined = ?, OrbitStabilized = ? WHERE BodyID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setDouble(1, asteroid.mass);
                    stmt.setDouble(2, asteroid.diameter);
                    stmt.setDouble(3, asteroid.orbital_radius);
                    stmt.setDouble(4, asteroid.velocity);
                    stmt.setBoolean(5, asteroid.hazardous);
                    stmt.setBoolean(6, asteroid.mined);
                    stmt.setBoolean(7, asteroid.orbitstabilized);
                    stmt.setInt(8, bodyID);
                    stmt.executeUpdate();
                }
            } else {
                String insertQuery = "INSERT INTO Asteroids (BodyID, Mass, Diameter, OrbitRadius, Velocity, Hazardous, Mined, OrbitStabilized) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setInt(1, bodyID);
                    stmt.setDouble(2, asteroid.mass);
                    stmt.setDouble(3, asteroid.diameter);
                    stmt.setDouble(4, asteroid.orbital_radius);
                    stmt.setDouble(5, asteroid.velocity);
                    stmt.setBoolean(6, asteroid.hazardous);
                    stmt.setBoolean(7, asteroid.mined);
                    stmt.setBoolean(8, asteroid.orbitstabilized);
                    stmt.executeUpdate();
                }
            }
        }
    }

    // Save technology progress
    private static void saveTechnologyProgress(Player player, Connection conn) throws SQLException {
        // Check if technology record exists for player
        String checkQuery = "SELECT t.TechID FROM Technology t " +
                "JOIN Player_Technology_Progress ptp ON t.TechID = ptp.TechID " +
                "WHERE ptp.PlayerID = ?";

        int techID = -1;
        try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
            stmt.setString(1, player.getPlayerID());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                techID = rs.getInt("TechID");
            }
        }

        if (techID != -1) {
            // Update existing technology
            String updateQuery = "UPDATE Technology SET ResearchPoints = ?, TechLevel = ?, " +
                    "NewPlanetsDiscovered = ?, AdvancedResourcesFound = ? WHERE TechID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setInt(1, player.tech.researchpoints);
                stmt.setInt(2, player.tech.techlevel);
                stmt.setBoolean(3, player.tech.newplanetsdiscoverd);
                stmt.setBoolean(4, player.tech.advancedresourcesfound);
                stmt.setInt(5, techID);
                stmt.executeUpdate();
            }
        } else {
            // Create new technology record
            String insertTech = "INSERT INTO Technology (ResearchPoints, TechLevel, NewPlanetsDiscovered, AdvancedResourcesFound) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertTech, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, player.tech.researchpoints);
                stmt.setInt(2, player.tech.techlevel);
                stmt.setBoolean(3, player.tech.newplanetsdiscoverd);
                stmt.setBoolean(4, player.tech.advancedresourcesfound);
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    techID = rs.getInt(1);

                    // Link to player
                    String linkQuery = "INSERT INTO Player_Technology_Progress (PlayerID, TechID) VALUES (?, ?)";
                    try (PreparedStatement linkStmt = conn.prepareStatement(linkQuery)) {
                        linkStmt.setString(1, player.getPlayerID());
                        linkStmt.setInt(2, techID);
                        linkStmt.executeUpdate();
                    }
                }
            }
        }
    }

    // Earn credits (calls stored procedure)
    public static void earnCredits(String playerID, int amount, String action) {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_EarnCredits(?, ?, ?)}")) {

            stmt.setString(1, playerID);
            stmt.setInt(2, amount);
            stmt.setString(3, action);
            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Conduct research (calls stored procedure)
    public static void conductResearch(String playerID, int pointsEarned) {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_ConductResearch(?, ?)}")) {

            stmt.setString(1, playerID);
            stmt.setInt(2, pointsEarned);
            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Record alien attack
    public static void recordAlienAttack(String species, int attackPower, int targetBodyID,
                                         boolean success, int damagePop, int damageRes) {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_RecordAlienAttack(?, ?, ?, ?, ?, ?)}")) {

            stmt.setString(1, species);
            stmt.setInt(2, attackPower);
            stmt.setInt(3, targetBodyID);
            stmt.setBoolean(4, success);
            stmt.setInt(5, damagePop);
            stmt.setInt(6, damageRes);
            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // End game session
    public static void endGameSession(String playerID) {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_EndGameSession(?)}")) {

            stmt.setString(1, playerID);
            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Check galaxy victory
    public static String checkGalaxyVictory(String playerID) {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_CheckGalaxyVictory(?)}")) {

            stmt.setString(1, playerID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Message");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Error checking victory conditions";
    }
}