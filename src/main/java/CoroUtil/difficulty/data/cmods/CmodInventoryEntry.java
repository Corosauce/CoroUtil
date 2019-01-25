package CoroUtil.difficulty.data.cmods;

import CoroUtil.difficulty.data.DifficultyDataReader;

public class CmodInventoryEntry {

    public double min;
    public double max;
    public CmodInventory inventory;

    @Override
    public String toString() {

        String str = "";

        if (DifficultyDataReader.getDebugDifficulty() != -1) {
            str = " min: " + min + ", max: " + max + " ";
            if (DifficultyDataReader.getDebugDifficulty() >= min && DifficultyDataReader.getDebugDifficulty() <= max) {
                str += inventory.toString();
            }
        } else {
            str = " min: " + min + ", max: " + max + " ";
            str += inventory.toString();
        }

        return str;
    }
}
