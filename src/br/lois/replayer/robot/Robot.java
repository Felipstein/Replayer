package br.lois.replayer.robot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.WrapperPlayServerAnimation;
import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityHeadRotation;
import com.comphenix.packetwrapper.WrapperPlayServerEntityLook;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerEntityTeleport;
import com.comphenix.packetwrapper.WrapperPlayServerNamedEntitySpawn;
import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_16_R3.Packet;

public class Robot {
	
	private int id;
	private GameProfile gameProfile;
	
	private Location location, spawnpoint;
	
	private WrappedDataWatcher data;
	
	private double health;
	private boolean alive;
	
	private Collection<? extends Player> visibleTo;
	
	private RobotInventory inventory;
	
	@Deprecated
	private boolean enabledTab;
	
	public Robot(Player copy, double health, Location spawnpoint) {
		this(copy.getUniqueId(), copy.getName(), health, spawnpoint);
	}
	
	public Robot(UUID uuid, String name, double health, Location spawnpoint) {
		this.id = 50000 + new Random().nextInt(50000);
		this.gameProfile = new GameProfile(uuid == null ? UUID.randomUUID() : uuid, name);
		this.spawnpoint = location = spawnpoint;
		this.health = health;
		this.visibleTo = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		this.inventory = new RobotInventory(this);
		Robots.getInstance().addRobot(this);
	}
	
	public void spawn() {
		this.location = spawnpoint;
		WrapperPlayServerNamedEntitySpawn packet = new WrapperPlayServerNamedEntitySpawn();
		packet.setEntityID(id);
		packet.setPlayerUUID(gameProfile.getId());
		packet.setPosition(location.toVector());
		packet.setYaw(location.getYaw());
		packet.setPitch(location.getPitch());
		if(data != null) {
			packet.setMetadata(data);
		}
		if(enabledTab) {
			this.handlePackets(packet, getPlayerInfoPacket(PlayerInfoAction.ADD_PLAYER));		
		} else {
			this.handlePackets(packet);
		}
		this.lookAt(location.getYaw(), location.getPitch());
		this.alive = true;
	}
	
	public void despawn() {
		WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy();
		packet.setEntityIds(new int[] {id});
		this.handlePackets(packet);
		this.alive = false;
	}
	
	public void setData(WrappedDataWatcher data) {
		this.data = data;
	}
	
	public void updateMetadata() {
		WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();
		packet.setEntityID(id);
		packet.setMetadata(data.getWatchableObjects());
		this.handlePackets(packet);
	}
	
	public void remove() {
		this.despawn();
		if(enabledTab) {
			this.handlePackets(getPlayerInfoPacket(PlayerInfoAction.REMOVE_PLAYER));
		}
		Robots.getInstance().removeRobot(this);
	}
	
	public void teleport(Location location) {
		WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport();
		packet.setEntityID(id);
		packet.setX(location.getX());
		packet.setY(location.getY());
		packet.setZ(location.getZ());
		packet.setYaw(location.getYaw());
		packet.setPitch(location.getPitch());
		this.lookAt(location.getYaw(), location.getPitch());
		Block downBlock = location.getWorld().getBlockAt(location.clone().subtract(0, 1, 0));
		packet.setOnGround(downBlock != null && downBlock.getType() != Material.AIR);
		this.handlePackets(packet);
		this.location = location;
	}
	
	public void teleportHere(Entity... entities) {
		for(Entity entity : entities) {
			entity.teleport(location);
		}
	}
	
	public void setYaw(float yaw) {
		this.lookAt(yaw, location.getPitch());
	}
	
	public void setPitch(float pitch) {
		this.lookAt(location.getYaw(), pitch);
	}
	
	private void lookAt(float yaw, float pitch) {
		WrapperPlayServerEntityLook packet1 = new WrapperPlayServerEntityLook();
		packet1.setEntityID(id);
		packet1.setYaw(yaw);
		packet1.setPitch(pitch);
		packet1.setOnGround(isOnGround());
		WrapperPlayServerEntityHeadRotation packet2 = new WrapperPlayServerEntityHeadRotation();
		packet2.setEntityID(id);
		packet2.setHeadYaw((byte) getHeadYaw(yaw));
		this.handlePackets(packet1, packet2);
	}
	
	public void makeAnimation(int animationId) {
		WrapperPlayServerAnimation packet = new WrapperPlayServerAnimation();
		packet.setEntityID(id);
		packet.setAnimation(animationId);
		this.handlePackets(packet);
	}
	
	public void sendChatMessage(Object message) {
		this.visibleTo.forEach(player -> player.sendMessage("<" + gameProfile.getName() + "> " + String.valueOf(message)));
	}
	
	public void handlePackets(AbstractPacket... packets) {
		for(AbstractPacket packet : packets) {
			this.visibleTo.forEach(player -> packet.sendPacket(player));
		}
	}
	
	public void handlePackets(Packet<?>... packets) {
		for(Packet<?> packet : packets) {
			this.visibleTo.forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet));
		}
	}
	
	private WrapperPlayServerPlayerInfo getPlayerInfoPacket(PlayerInfoAction action) {
		WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo();
		packet.setAction(action);
		PlayerInfoData playerInfoData = new PlayerInfoData(new WrappedGameProfile(gameProfile.getId(), gameProfile.getName()), 1, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(gameProfile.getName()));
		packet.setData(new ArrayList<>(Arrays.asList(playerInfoData)));
		return packet;
	}
	
	private float getHeadYaw(float yaw) {
		return yaw * 256 / 360;
	}
	
	public boolean isOnGround() {
		Block downBlock = location.getWorld().getBlockAt(location.clone().subtract(0, 1, 0));
		return downBlock != null && downBlock.getType() != Material.AIR;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return gameProfile.getName();
	}
	
	public UUID getUniqueId() {
		return gameProfile.getId();
	}
	
	public GameProfile getGameProfile() {
		return gameProfile;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setSpawnpoint(Location spawnpoint) {
		this.spawnpoint = spawnpoint;
	}
	
	public Location getSpawnpoint() {
		return spawnpoint;
	}
	
	public void setHealth(double health) {
		Validate.isTrue(health >= 0, "A vida da entidade marionete não pode ser negativa.");
		this.health = health;
	}
	
	public double getHealth() {
		return health;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void setVisibleTo(Collection<? extends Player> visibleTo) {
		this.visibleTo = visibleTo;
	}
	
	public Collection<? extends Player> getVisibleTo() {
		return Collections.unmodifiableCollection(visibleTo);
	}
	
	public RobotInventory getInventory() {
		return inventory;
	}
	
}