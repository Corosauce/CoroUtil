package CoroUtil.difficulty;

import CoroUtil.util.BlockCoord;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class DifficultyInfoPlayer {

    public BlockPos difficultyPos = BlockPos.ORIGIN;

    public int skipCount = 0;
    public int skipCountMax = 0;
    public int itemsNeeded = 0;

    public float difficultyEquipment;
    public float difficultyPlayerServerTime;
    public float difficultyAverageChunkTime;
    public float difficultyDPS;
    public float difficultyHealth;
    public float difficultyDistFromSpawn;
    public float difficultyBuffedLocation;
    public float difficultyDebuffedLocation;
    public float difficultyBuffInvasionSkip;

    public DifficultyInfoPlayer() {

    }

    public DifficultyInfoPlayer(BlockPos difficultyPos) {
        this.difficultyPos = difficultyPos;
    }

    public void toBytes(ByteBuf buf) {

        buf.writeInt(skipCount);
        buf.writeInt(skipCountMax);
        buf.writeInt(itemsNeeded);

        buf.writeFloat(difficultyDPS);

    }

    public void fromBytes(ByteBuf buf) {

        skipCount = buf.readInt();
        skipCountMax = buf.readInt();
        itemsNeeded = buf.readInt();

        difficultyDPS = buf.readFloat();
    }

    public void updateData(EntityPlayer player, int countNeededBase, double multiplier) {

        int skipCount = player.getEntityData().getInteger(DynamicDifficulty.dataPlayerInvasionSkipCountForMultiplier);

        if (skipCount == 0) {
            itemsNeeded = countNeededBase;
        } else {
            itemsNeeded = (int) ((double) countNeededBase * multiplier * (double)skipCount);
        }


        difficultyDPS = DynamicDifficulty.getDifficultyScaleForPosDPS(player.world, new BlockCoord(player.getPosition()));

    }

}
