import java.util.Random;

class Technology {
    public int researchpoints;
    public int techlevel;
    public boolean newplanetsdiscoverd;
    public boolean advancedresourcesfound;

    public Technology() {
        researchpoints = 0;
        techlevel = 1;
        newplanetsdiscoverd = false;
        advancedresourcesfound = false;
    }

    public String conductResearch() {
        int gain = new Random().nextInt(100) + 50;
        StringBuilder result = new StringBuilder();
        result.append("Your current research points: ").append(researchpoints).append("\n")
                .append("You gained: ").append(gain).append(" research points!\n");

        researchpoints += gain;

        if (researchpoints >= 300) {
            techlevel++;
            researchpoints -= 300;
            result.append("Technology Level upgraded to: ").append(techlevel).append("\n")
                    .append("Remaining research points: ").append(researchpoints).append("\n");

            if (techlevel >= 2 && !newplanetsdiscoverd) {
                newplanetsdiscoverd = true;
                result.append("You discovered a new planet using deepspace scanners!\n");
            }

            if (techlevel >= 3 && !advancedresourcesfound) {
                advancedresourcesfound = true;
                result.append("You unlocked advanced resources location!\n");
            }
        }

        return result.toString();
    }

    public void showTechStatus() {
        System.out.println("\n------Technology Status--------");
        System.out.println("Tech Level: " + techlevel);
        System.out.println("Research points: " + researchpoints);
        System.out.println("New Discovered Planet: " + (newplanetsdiscoverd ? "Yes" : "No"));
        System.out.println("Advanced resources found: " + (advancedresourcesfound ? "Yes" : "No"));
    }

    public int getTechLevel() {
        return techlevel;
    }

    public int getResearchPoints() {
        return researchpoints;
    }
}
