import javax.swing.*;
import java.util.Random;
import java.awt.GridLayout;

public class Planet extends CelestialBody {
    public int population;
    public int resources;
    public int defense;
    public boolean habitable;
    public boolean terraformed;
    public boolean scanned;

    public Planet(String n, String t, int p, int r, int d, boolean h) {
        super(n, t);
        population = p;
        resources = r;
        defense = d;
        habitable = h;
        terraformed = false;
        scanned = false;
    }

    @Override
    public void showStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Planet: ").append(name).append("\n");
        sb.append("Type: ").append(type).append("\n");
        sb.append("Population: ").append(population).append("\n");
        sb.append("Resources: ").append(resources).append("\n");
        sb.append("Defense: ").append(defense).append("\n");
        sb.append("Habitable: ").append(habitable ? "Yes" : "No").append("\n");
        sb.append("Terraformed: ").append(terraformed ? "Yes" : "No").append("\n");
        sb.append("Scanned: ").append(scanned ? "Yes" : "No").append("\n");
        sb.append("Conquered: ").append(conquered ? "Yes" : "No").append("\n");

        JOptionPane.showMessageDialog(null, sb.toString(), "Planet Status", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public boolean checkVictoryCondition() {
        switch (type) {
            case "Terrestrial":
                return population >= 1000 && resources >= 800 && defense >= 500;
            case "Desert":
                return population >= 800 && resources >= 600 && terraformed;
            case "Ice Giant":
                return resources >= 1200 && scanned;
            case "Toxic":
                return terraformed && defense >= 400;
            default:
                return false;
        }
    }

    public void showVictoryRequirements() {
        StringBuilder sb = new StringBuilder();
        sb.append("------Victory Requirement for ").append(name).append("------\n");
        switch (type) {
            case "Terrestrial":
                sb.append("1. Population >=1000 (Current: ").append(population).append(")\n");
                sb.append("2. Resources>=800 (Current: ").append(resources).append(")\n");
                sb.append("3. Defense>=500 (Current: ").append(defense).append(")\n");
                break;
            case "Desert":
                sb.append("1. population>=800 (current: ").append(population).append(")\n");
                sb.append("2. Resources>=600 (current: ").append(resources).append(")\n");
                sb.append("3. Terraformed (current: ").append(terraformed ? "Yes" : "No").append(")\n");
                break;
            case "Ice Giant":
                sb.append("1. Resources>=1200 (current: ").append(resources).append(")\n");
                sb.append("2. Scanned (current: ").append(scanned ? "Yes" : "No").append(")\n");
                break;
            case "Toxic":
                sb.append("1. Terraformed (current: ").append(terraformed ? "Yes" : "No").append(")\n");
                sb.append("2. Defense>=400 (Current: ").append(defense).append(")\n");
                break;
        }
        JOptionPane.showMessageDialog(null, sb.toString(), "Victory Requirements", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean performedSimpleTask() {
        int taskType = new Random().nextInt(2);

        if (taskType == 0) {
            return showNumberGuessingGame();
        } else {
            return showSpaceQuiz();
        }
    }

    private boolean showNumberGuessingGame() {
        int secretNumber = new Random().nextInt(10) + 1;
        int attempts = 3;

        while (attempts > 0) {
            String input = JOptionPane.showInputDialog(
                    null,
                    "Guess a number between 1 and 10\nAttempts left: " + attempts,
                    "Number Guessing Game",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) return false; // User canceled

            try {
                int guess = Integer.parseInt(input);

                if (guess == secretNumber) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Correct! You guessed the number.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    return true;
                } else {
                    attempts--;
                    String message = "Your guess is too " + (guess < secretNumber ? "low" : "high");
                    if (attempts > 0) {
                        message += ". Try again.";
                    } else {
                        message += ". Out of attempts! The correct number was " + secretNumber;
                    }
                    JOptionPane.showMessageDialog(
                            null,
                            message,
                            "Incorrect",
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
        return false;
    }

    private boolean showSpaceQuiz() {
        int questionNum = new Random().nextInt(5);
        String question = "";
        String[] options = new String[4];
        int correctAnswer = 0;

        switch (questionNum) {
            case 0:
                question = "Which planet is known as red Planet?";
                options = new String[]{"Venus", "Mars", "Jupiter", "Saturn"};
                correctAnswer = 2;
                break;
            case 1:
                question = "What is the largest planet in our solar system?";
                options = new String[]{"Earth", "Neptune", "Jupiter", "Uranus"};
                correctAnswer = 3;
                break;
            case 2:
                question = "Which of these is not the moon of Jupiter?";
                options = new String[]{"Europa", "Titan", "Ganymede", "Callisto"};
                correctAnswer = 2;
                break;
            case 3:
                question = "Which is the hottest planet in our solar system?";
                options = new String[]{"Mercury", "Venus", "Mars", "Earth"};
                correctAnswer = 2;
                break;
            case 4:
                question = "Which planet has most prominent ring system?";
                options = new String[]{"Uranus", "Neptune", "Saturn", "Jupiter"};
                correctAnswer = 3;
                break;
        }

        Object[] optionButtons = new Object[options.length];
        for (int i = 0; i < options.length; i++) {
            optionButtons[i] = (i + 1) + ". " + options[i];
        }

        int answer = JOptionPane.showOptionDialog(
                null,
                question,
                "Space Quiz",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                optionButtons,
                optionButtons[0]
        );

        return (answer + 1) == correctAnswer;
    }

    public void terraformPlanet() {
        if (terraformed) {
            JOptionPane.showMessageDialog(
                    null,
                    "This planet is already terraformed.",
                    "Terraforming",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                null,
                "Do you want to terraform this planet?",
                "Terraforming",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(
                    null,
                    "Terraforming skipped.",
                    "Terraforming",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
                null,
                "Terraforming requires solving this puzzle:\n" +
                        "You need to balance the atmospheric elements (Oxygen, Nitrogen, Carbon Dioxide)\n" +
                        "Each element must be between 18-22% for successful terraforming",
                "Terraforming Puzzle",
                JOptionPane.INFORMATION_MESSAGE
        );

        Random rand = new Random();
        int oxygen = rand.nextInt(15) + 10;
        int nitrogen = rand.nextInt(15) + 10;
        int co2 = rand.nextInt(15) + 10;

        for (int i = 0; i < 3; i++) {
            JPanel panel = new JPanel(new GridLayout(4, 2));
            panel.add(new JLabel("Current Atmosphere Composition:"));
            panel.add(new JLabel(""));
            panel.add(new JLabel("1. Oxygen: " + oxygen + "%"));
            panel.add(new JLabel(""));
            panel.add(new JLabel("2. Nitrogen: " + nitrogen + "%"));
            panel.add(new JLabel(""));
            panel.add(new JLabel("3. Carbon Dioxide: " + co2 + "%"));
            panel.add(new JLabel(""));

            JComboBox<String> elementCombo = new JComboBox<>(new String[]{"Oxygen", "Nitrogen", "Carbon Dioxide"});
            JSpinner changeSpinner = new JSpinner(new SpinnerNumberModel(0, -10, 10, 1));

            panel.add(new JLabel("Select element to adjust:"));
            panel.add(elementCombo);
            panel.add(new JLabel("Enter percentage change (-10 to +10):"));
            panel.add(changeSpinner);

            int result = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    "Terraforming Adjustment " + (i + 1) + "/3",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result != JOptionPane.OK_OPTION) {
                JOptionPane.showMessageDialog(
                        null,
                        "Terraforming failed! You canceled the process.",
                        "Terraforming Failed",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int change = (Integer) changeSpinner.getValue();
            switch (elementCombo.getSelectedIndex()) {
                case 0: oxygen += change; break;
                case 1: nitrogen += change; break;
                case 2: co2 += change; break;
            }
        }

        if (oxygen >= 18 && oxygen <= 22 && nitrogen >= 18 && nitrogen <= 22 && co2 >= 18 && co2 <= 22) {
            terraformed = true;
            habitable = true;
            JOptionPane.showMessageDialog(
                    null,
                    "Terraforming successful! " + name + " is now habitable.",
                    "Terraforming Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    null,
                    "Terraforming failed! Atmosphere not balanced properly.",
                    "Terraforming Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    @Override
    public boolean canModify() {
        return scanned && (habitable || terraformed);
    }

    @Override
    public void performTask() {
        if (!scanned) {
            if (performedSimpleTask()) {
                scanned = true;
                JOptionPane.showMessageDialog(
                        null,
                        "Planet scanned successfully!",
                        "Scan Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Scan failed. Try again.",
                        "Scan Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        } else {
            JOptionPane.showMessageDialog(
                    null,
                    "Planet already scanned. You can now modify it.",
                    "Scan Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}