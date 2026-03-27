public class GameInitializer {
    public static void initializeNewGame(Player player) {
        // Add starter planets
        player.addPlanet(new Planet("Earth", "Terrestrial", 1000, 700, 500, true));
        player.addPlanet(new Planet("Mars", "Desert", 600, 400, 300, true));
        player.addPlanet(new Planet("Neptune", "Ice Giant", 800, 600, 350, true));
        player.addPlanet(new Planet("Venus", "Toxic", 400, 500, 250, false));

        // Add nebulas
        player.addNebula(new Nebula("Orion Nebula", 80, 60, true));
        player.addNebula(new Nebula("Horsehead Nebula", 50, 30, false));
        player.addNebula(new Nebula("Crab Nebula", 90, 85, true));
        player.addNebula(new Nebula("Helix Nebula", 70, 55, false));

        // Add asteroids
        player.addAsteroid(new Asteroid("Ceres", 9.393e20, 939.4, 2.77, 17, false));
        player.addAsteroid(new Asteroid("Vesta", 2.5908e20, 525.4, 2.36, 19.3, false));
        player.addAsteroid(new Asteroid("Apophis", 6.1e10, 0.37, 0.922, 30.7, true));
        player.addAsteroid(new Asteroid("Pallas", 2.11e20, 512, 2.77, 17.3, false));
    }
}