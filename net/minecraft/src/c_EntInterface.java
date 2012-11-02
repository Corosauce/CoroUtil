package net.minecraft.src;
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode

import java.util.List;
import java.util.LinkedList;
import java.util.Random;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import CoroAI.*;
import CoroAI.entity.*;

public class c_EntInterface extends EntityMob
{
    public InventoryPlayer inventory;
    public c_EntityPlayerMPExt fakePlayer;

    public c_EntInterface(World world)
    {
        super(world);

        try
        {
            if (!world.isRemote)
            {
                fakePlayer = newFakePlayer(world);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return;
        }
    }

    public c_EntityPlayerMPExt newFakePlayer(World world)
    {
        MinecraftServer mc = MinecraftServer.getServer();

        //this.setDead();
        
        if (mc == null)
        {
            return null;
        }

        //int dim = world.worldInfo.getDimension();
        c_EntityPlayerMPExt player = null;//

        player = new c_EntityPlayerMPExt(mc, world, "fakePlayer", new ItemInWorldManager(world));
        
        if (world.playerEntities.size() > 0)
        {
        	
        	
            if (world.playerEntities.get(0) instanceof EntityPlayerMP)
            {
                player.playerNetServerHandler = ((EntityPlayerMP)world.playerEntities.get(0)).playerNetServerHandler;
                player.dimension = ((EntityPlayerMP)world.playerEntities.get(0)).dimension;
            }
        }
        else
        {
            //System.out.println("fakeplayer has no netserverhandler, might behave oddly");
        }

        //mc.configManager.netManager
        //player.movementInput = new MovementInputFromOptions(mod_ZombieCraft.mc.gameSettings);
        return player;
    }

    public int getMaxHealth()
    {
        return 20;
    }

    public int getHealth()
    {
        return this.health;
    }

    public int getPlHealth()
    {
        return this.fakePlayer.health;
    }

    public void setPlHealth(int h)
    {
    	try {
    		if (fakePlayer != null) fakePlayer.health = h;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }

    public boolean getSleeping()
    {
        return this.fakePlayer.sleeping;
    }

    public void setSleeping(boolean h)
    {
        fakePlayer.sleeping = h;
    }

    public void updateItemUse(ItemStack is, int val)
    {
        fakePlayer.updateItemUse(is, val);
    }

    public void onItemUseFinish()
    {
        if (fakePlayer instanceof EntityPlayerMP && ((EntityPlayerMP)fakePlayer).playerNetServerHandler != null)
        {
            fakePlayer.onItemUseFinish();
        }
    }

    public void setThrower(EntityThrowable prj, EntityLiving ent)
    {
        prj.thrower = ent;
    }
}
