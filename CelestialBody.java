abstract class CelestialBody {
    public String name;
    public String type;
    public boolean conquered;

    public CelestialBody(String n, String t) {
        name = n;
        type = t;
        conquered = false;
    }

    public void showStatus() {
        System.out.println("Celestial Body: " + name + "(" + type + ")");
        System.out.println("Conquered: " + (conquered ? "Yes" : "No"));
    }

    public abstract boolean checkVictoryCondition();
    public abstract void performTask();
    public abstract boolean canModify();
}