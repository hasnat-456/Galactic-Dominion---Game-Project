import javax.swing.*;
import java.util.Random;
import java.awt.GridLayout;

public class Nebula extends CelestialBody {
    public int density;
    public int radiationlevel;
    public boolean visibilityreduction;
    public boolean mapped;
    public boolean resourcesextracted;

    public Nebula(String n, int d, int r, boolean vr) {
        super(n, "Nebula");
        density = d;
        radiationlevel = r;
        visibilityreduction = vr;
        mapped = false;
        resourcesextracted = false;
    }

    @Override
    public void showStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nebula: ").append(name).append("\n");
        sb.append("Density: ").append(density).append("\n");
        sb.append("Radiation Level: ").append(radiationlevel).append("\n");
        sb.append("Visibility Reduction: ").append(visibilityreduction ? "Yes" : "No").append("\n");
        sb.append("Mapped: ").append(mapped ? "Yes" : "No").append("\n");
        sb.append("Resources Extracted: ").append(resourcesextracted ? "Yes" : "No").append("\n");
        sb.append("Conquered: ").append(conquered ? "Yes" : "No").append("\n");

        JOptionPane.showMessageDialog(null, sb.toString(), "Nebula Status", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public boolean checkVictoryCondition() {
        return mapped && resourcesextracted && radiationlevel < 50;
    }

    public void showVictoryRequirements() {
        StringBuilder sb = new StringBuilder();
        sb.append("------Victory Requirement for ").append(name).append("------\n");
        sb.append("1. Mapped (current: ").append(mapped ? "Yes" : "No").append(")\n");
        sb.append("2. Resources Extracted (Current: ").append(resourcesextracted ? "Yes" : "No").append(")\n");
        sb.append("3. Radiation Level <50 (Current: ").append(radiationlevel).append(")\n");

        JOptionPane.showMessageDialog(null, sb.toString(), "Victory Requirements", JOptionPane.INFORMATION_MESSAGE);
    }

    public void adjustRadiation() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel("Current Radiation Level: " + radiationlevel));

        int choice = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Adjust Radiation",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String input = JOptionPane.showInputDialog(
                null,
                "Calculate the square root of " + (radiationlevel * 4) + " (rounded to nearest integer):",
                "Radiation Adjustment Puzzle",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null) return;

        try {
            int answer = Integer.parseInt(input);
            int correct = (int) Math.round(Math.sqrt(radiationlevel * 4));

            if (answer == correct) {
                JOptionPane.showMessageDialog(
                        null,
                        "Correct! You can now adjust radiation.",
                        "Puzzle Solved",
                        JOptionPane.INFORMATION_MESSAGE
                );

                Object[] options = {"Increase", "Decrease", "Cancel"};
                int adjustChoice = JOptionPane.showOptionDialog(
                        null,
                        "Do you want to increase or decrease radiation?",
                        "Adjust Radiation",
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
                            "Increase Radiation",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (amountInput != null) {
                        try {
                            int amount = Integer.parseInt(amountInput);
                            radiationlevel += amount;
                            JOptionPane.showMessageDialog(
                                    null,
                                    "New radiation level: " + radiationlevel,
                                    "Radiation Adjusted",
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
                            "Decrease Radiation",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (amountInput != null) {
                        try {
                            int amount = Integer.parseInt(amountInput);
                            radiationlevel -= amount;
                            JOptionPane.showMessageDialog(
                                    null,
                                    "New radiation level: " + radiationlevel,
                                    "Radiation Adjusted",
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

    public int exploreNebula() {
        if (!mapped) {
            JOptionPane.showMessageDialog(
                    null,
                    "You must map this nebula first!",
                    "Exploration Failed",
                    JOptionPane.WARNING_MESSAGE
            );
            performTask();
            return 0;
        }

        JOptionPane.showMessageDialog(
                null,
                "Exploring the nebula....",
                "Nebula Exploration",
                JOptionPane.INFORMATION_MESSAGE
        );

        if (new Random().nextInt(100) < 50) {
            int foundCredits = new Random().nextInt(200) + 50;
            JOptionPane.showMessageDialog(
                    null,
                    "You found an ancient alien tech and earned " + foundCredits + " credits!",
                    "Exploration Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
            resourcesextracted = true;
            return foundCredits;
        } else {
            JOptionPane.showMessageDialog(
                    null,
                    "High radiation! Your ship's shield weakened.",
                    "Exploration Failed",
                    JOptionPane.WARNING_MESSAGE
            );
            return 0;
        }
    }

    @Override
    public void performTask() {
        if (!mapped) {
            String input = JOptionPane.showInputDialog(
                    null,
                    "You must map this nebula before exploring it!\n" +
                            "Mapping requires solving a puzzle:\n" +
                            "What is 3 * 7 + 5?",
                    "Nebula Mapping",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (input != null) {
                try {
                    int answer = Integer.parseInt(input);
                    if (answer == 26) {
                        mapped = true;
                        JOptionPane.showMessageDialog(
                                null,
                                "Correct! Nebula mapped successfully.",
                                "Mapping Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                null,
                                "Incorrect answer. Mapping failed",
                                "Mapping Failed",
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
            return;
        }

        if (!resourcesextracted) {
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Do you want to extract resources from this nebula?",
                    "Resource Extraction",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                String input = JOptionPane.showInputDialog(
                        null,
                        "To extract resources, solve this mini-game:\n" +
                                "Find the hidden element in the nebula (enter a number between 1-5):",
                        "Resource Extraction Game",
                        JOptionPane.QUESTION_MESSAGE
                );

                if (input != null) {
                    try {
                        int guess = Integer.parseInt(input);
                        int correct = new Random().nextInt(5) + 1;

                        if (guess == correct) {
                            resourcesextracted = true;
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Success! You extracted valuable resources from the nebula.",
                                    "Extraction Success",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Failed! The element was at position " + correct + ". Try again.",
                                    "Extraction Failed",
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
            }
            return;
        }

        JOptionPane.showMessageDialog(
                null,
                "All tasks are completed for this nebula",
                "Tasks Complete",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public boolean canModify() {
        return mapped && resourcesextracted;
    }
}
