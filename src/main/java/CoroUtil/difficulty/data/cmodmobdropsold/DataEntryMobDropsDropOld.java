package CoroUtil.difficulty.data.cmodmobdropsold;

import CoroUtil.difficulty.data.DataEntryBase;

/**
 * Created by Corosus on 2/1/2017.
 */
public class DataEntryMobDropsDropOld extends DataEntryBase {

    public String item;
    public int count;
    public int metadata;
    public String nbt;

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getMetadata() {
        return metadata;
    }

    public void setMetadata(int metadata) {
        this.metadata = metadata;
    }

    public String getNbt() {
        if (nbt == null) return "";
        return nbt;
    }

    public void setNbt(String nbt) {
        this.nbt = nbt;
    }
}
