package CoroUtil.difficulty;

public class DifficultyQueryContext {

    private String context = "";
    private int invasionNumber = -1;
    private float difficulty = 0;

    public DifficultyQueryContext(String context, int invasionNumber, float difficulty) {
        this.context = context;
        this.invasionNumber = invasionNumber;
        this.difficulty = difficulty;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getInvasionNumber() {
        return invasionNumber;
    }

    public void setInvasionNumber(int invasionNumber) {
        this.invasionNumber = invasionNumber;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }
}
