package net.meteor.common;

import java.util.ArrayList;

import net.meteor.common.climate.CrashLocation;
import net.meteor.common.climate.HandlerMeteor;
import net.meteor.common.packets.PacketBlockedMeteor;
import net.meteor.common.packets.PacketButtonPress;
import net.meteor.common.packets.PacketGhostMeteor;
import net.meteor.common.packets.PacketLastCrash;
import net.meteor.common.packets.PacketSettings;
import net.meteor.common.packets.PacketSoonestMeteor;
import net.meteor.plugin.baubles.Baubles;
import net.meteor.plugin.baubles.PacketToggleMagnetism;
import net.meteor.plugin.baubles.PacketTogglePlayerMagnetism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.relauncher.Side;

public class ClientHandler
{
	public static CrashLocation lastCrashLocation = null;
	public static ChunkCoordinates nearestTimeLocation = null;
	public static ArrayList<ChunkCoordinates> ghostMetLocs = new ArrayList<ChunkCoordinates>(); // TODO Privatize
	
	public void registerPackets() {
		MeteorsMod.network.registerMessage(PacketBlockedMeteor.Handler.class, PacketBlockedMeteor.class, 0, Side.CLIENT);
		MeteorsMod.network.registerMessage(PacketButtonPress.Handler.class, PacketButtonPress.class, 1, Side.SERVER);
		MeteorsMod.network.registerMessage(PacketGhostMeteor.Handler.class, PacketGhostMeteor.class, 2, Side.CLIENT);
		MeteorsMod.network.registerMessage(PacketLastCrash.Handler.class, PacketLastCrash.class, 3, Side.CLIENT);
		MeteorsMod.network.registerMessage(PacketSettings.Handler.class, PacketSettings.class, 4, Side.CLIENT);
		MeteorsMod.network.registerMessage(PacketSoonestMeteor.Handler.class, PacketSoonestMeteor.class, 5, Side.CLIENT);
		if (Baubles.isBaublesLoaded()) {
			MeteorsMod.network.registerMessage(PacketToggleMagnetism.Handler.class, PacketToggleMagnetism.class, 6, Side.SERVER);
			MeteorsMod.network.registerMessage(PacketTogglePlayerMagnetism.Handler.class, PacketTogglePlayerMagnetism.class, 7, Side.CLIENT);
		}
	}

	public static ChunkCoordinates getClosestIncomingMeteor(double pX, double pZ) {
		ChunkCoordinates coords = null;
		double y = 50.0D;
		for (int i = 0; i < ghostMetLocs.size(); i++) {
			if (coords != null) {
				ChunkCoordinates loc = ghostMetLocs.get(i);
				double var1 = getDistance(pX, y, pZ, loc.posX, y, loc.posZ);
				double var2 = getDistance(pX, y, pZ, coords.posX, y, coords.posZ);
				if (var1 < var2)
					coords = loc;
			}
			else {
				coords = ghostMetLocs.get(i);
			}
		}
		return coords;
	}

	private static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
		double var7 = x1 - x2;
		double var9 = y1 - y2;
		double var11 = z1 - z2;
		return MathHelper.sqrt_double(var7 * var7 + var9 * var9 + var11 * var11);
	}
	
	public static IChatComponent createMessage(String s, EnumChatFormatting ecf) {
		return new ChatComponentText(s).setChatStyle(new ChatStyle().setColor(ecf));
	}

	@SubscribeEvent
	public void playerLoggedIn(PlayerLoggedInEvent event)
	{
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		MeteorsMod.network.sendTo(new PacketSettings(), player);
	}
	
	@SubscribeEvent
	public void entityJoinWorld(EntityJoinWorldEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			if (event.entity instanceof EntityPlayer) {
				EntityPlayerMP player = (EntityPlayerMP) event.entity;
				HandlerMeteor metHandler = MeteorsMod.proxy.metHandlers.get(event.world.provider.dimensionId);
				MeteorsMod.network.sendTo(new PacketGhostMeteor(), player);		// Clear Ghost Meteors
				metHandler.sendGhostMeteorPackets(player);
				if (metHandler.getForecast() == null) {
					MeteorsMod.log.info("FORECAST WAS NULL");
				}
				MeteorsMod.network.sendTo(new PacketLastCrash(metHandler.getForecast().getLastCrashLocation()), player);
				MeteorsMod.network.sendTo(new PacketSoonestMeteor(metHandler.getForecast().getNearestTimeMeteor()), player);
			}
		}
	}

}