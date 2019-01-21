package CoroUtil.forge;

import java.io.FileOutputStream;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class AsyncSaveTask extends Thread {
	private final NBTTagCompound data;
	private final FileOutputStream fos;
	
	public AsyncSaveTask(NBTTagCompound data, FileOutputStream fos)
	{
		this.data = data;
		this.fos = fos;
	}
	
	@Override
	public void run()
	{
		try {
			CompressedStreamTools.writeCompressed(data, fos);
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
