import javax.swing.*;
import java.util.Random;
import java.awt.GridLayout;

public class Asteroid extends CelestialBody {
    public double mass;
    public double diameter;
    public double orbital_radius;
    public double velocity;
    public boolean hazardous;
    public boolean mined;
    public boolean orbitstabilized;

    public Asteroid(String n, double m, double d, double o, double v, boolean h) {
        super(n, "Asteroid");
        mass = m;
        diameter = d;
        orbital_radius = o;
        velocity = v;
        hazardous = h;
        mined = false;
        orbitstabilized = false;
    }

    @Override
    public void showStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Asteroid: ").append(name).append("\n");
        sb.append("Mass: ").append(mass).append(" kg\n");
        sb.append("Diameter: ").append(diameter).append(" km\n");
        sb.append("Orbit Radius: ").append(orbital_radius).append(" AU\n");
        sb.append("Velocity: ").append(velocity).append(" km/s\n");
        sb.append("Hazardous: ").append(hazardous ? "Yes" : "No").append("\n");
        sb.append("Mined: ").append(mined ? "Yes" : "No").append("\n");
        sb.append("Orbit Stabilized: ").append(orbitstabilized ? "Yes" : "No").append("\n");
        sb.append("Conquered: ").append(conquered ? "Yes" : "No").append("\n");

        JOptionPane.showMessageDialog(null, sb.toString(), "Asteroid Status", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public boolean checkVictoryCondition() {
        if (hazardous) {
            return orbitstabilized && mined && velocity < 20;
        } else {
            return mined && orbital_radius < 3 && orbitstabilized;
        }
    }

    public void showVictoryRequirements() {
        StringBuilder sb = new StringBuilder();
        sb.append("------Victory Requirement for ").append(name).append("------\n");
        if (hazardous) {
            sb.append("1. Orbit Stabilized (Current: ").append(orbitstabilized ? "Yes" : "No").append(")\n");
            sb.append("2. Mined (Current: ").append(mined ? "Yes" : "No").append(")\n");
            sb.append("3. Velocity <20 km/s (Current: ").append(velocity).append(")\n");
        } else {
            sb.append("1. Mined (Current: ").append(mined ? "Yes" : "No").append(")\n");
            sb.append("2. Orbital Radius < 3 AU (Current: ").append(orbital_radius).append(")\n");
            sb.append("3. Orbit Stabilized (Current: ").append(orbitstabilized ? "Yes" : "No").append(")\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString(), "Victory Requirements", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mineAsteroid() {
        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.add(new JLabel("Mining asteroid " + name + "....."));
        panel.add(new JLabel("Asteroids have different mineral patterns. Guess the correct pattern:"));
        panel.add(new JLabel("1. Linear deposits\n2. Spiral deposits\n3. Random clusters"));

        String input = JOptionPane.showInputDialog(
                null,
                panel,
                "Mining Asteroid",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null) return;

        try {
            int pattern = Integer.parseInt(input);
            int correctPattern = new Random().nextInt(3) + 1;

            if (pattern == correctPattern) {
                int resources = new Random().nextInt(200) + 50;
                if (diameter > 1.0) {
                    resources *= 2;
                }
                JOptionPane.showMessageDialog(
                        null,
                        "Success! You mined " + resources + " units of rare minerals.",
                        "Mining Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                mined = true;
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Wrong pattern! The correct pattern was " + correctPattern + ". Mining failed.",
                        "Mining Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Please enter a valid number!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void stabilizeOrbit() {
        if (!mined) {
            JOptionPane.showMessageDialog(
                    null,
                    "You must mine this asteroid first before stabilizing its orbit!",
                    "Stabilization Failed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (orbitstabilized) {
            JOptionPane.showMessageDialog(
                    null,
                    "This asteroid's orbit is already stabilized.",
                    "Orbit Stabilized",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                null,
                "Do you want to stabilize this asteroid's orbit?",
                "Orbit Stabilization",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String input = JOptionPane.showInputDialog(
                null,
                "To stabilize the orbit, answer this simple question:\n" +
                        "What is 5 + 7 * 8 - 9 * 10 /5?",
                "Orbit Stabilization Puzzle",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null) return;

        try {
            int answer = Integer.parseInt(input);
            if (answer == 43) {
                orbitstabilized = true;
                velocity = 15;
                JOptionPane.showMessageDialog(
                        null,
                        "Orbit stabilized successfully! Velocity set to 15 km/s.",
                        "Stabilization Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Incorrect answer. Orbit stabilization failed.",
                        "Stabilization Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Please enter a valid number!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public double getDensity() {
        if (diameter == 0) return 0;
        double radius = diameter * 500;
        double volume = (4.0 / 3.0) * 3.14159 * (radius * radius * radius);
        return mass / volume;
    }

    @Override
    public boolean canModify() {
        if (hazardous) {
            return orbitstabilized && mined;
        }
        return mined && orbitstabilized;
    }

    public void adjustVelocity() {
        if (!orbitstabilized) {
            JOptionPane.showMessageDialog(
                    null,
                    "You must stabilize the orbit first before adjusting velocity!",
                    "Adjustment Failed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Current Velocity: " + velocity + " km/s"));

        int choice = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Adjust Velocity",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String input = JOptionPane.showInputDialog(
                null,
                "To adjust velocity, solve this math problem:\n" +
                        "What is " + (int) velocity + " + " + (int) (velocity / 2) + "?",
                "Velocity Adjustment Puzzle",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null) return;

        try {
            int answer = Integer.parseInt(input);
            int correct = (int) (velocity + velocity / 2);

            if (answer == correct) {
                Object[] options = {"Increase", "Decrease", "Cancel"};
                int adjustChoice = JOptionPane.showOptionDialog(
                        null,
                        "Correct! You can now adjust velocity.",
                        "Adjust Velocity",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[2]
                );

                if (adjustChoice == 0) { // Increase
                    String amountInput = JOptionPane.showInputDialog(
                            null,
                            "Enter amount to increase:",
                            "Increase Velocity",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (amountInput != null) {
                        try {
                            double amount = Double.parseDouble(amountInput);
                            velocity += amount;
                            JOptionPane.showMessageDialog(
                                    null,
                                    "New velocity: " + velocity + " km/s",
                                    "Velocity Adjusted",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Invalid number entered!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                } else if (adjustChoice == 1) { // Decrease
                    String amountInput = JOptionPane.showInputDialog(
                            null,
                            "Enter amount to decrease:",
                            "Decrease Velocity",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (amountInput != null) {
                        try {
                            double amount = Double.parseDouble(amountInput);
                            velocity -= amount;
                            JOptionPane.showMessageDialog(
                                    null,
                                    "New velocity: " + velocity + " km/s",
                                    "Velocity Adjusted",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Invalid number entered!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Incorrect! The answer was " + correct + ". Try again.",
                        "Puzzle Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Please enter a valid number!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @Override
    public void performTask() {
        if (!mined) {
            JOptionPane.showMessageDialog(
                    null,
                    "You must mine this asteroid first before stabilizing its orbit!",
                    "Task Required",
                    JOptionPane.WARNING_MESSAGE
            );
            mineAsteroid();
            return;
        }
        JOptionPane.showMessageDialog(
                null,
                "All tasks completed for this asteroid.",
                "Tasks Complete",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}