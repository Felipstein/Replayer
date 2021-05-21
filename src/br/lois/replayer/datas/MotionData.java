package br.lois.replayer.datas;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import br.lois.replayer.Wrapper;

public class MotionData extends PlayerData {
	
	private Location location;
	
	public MotionData(Player player, double x, double y, double z) {
		this(player, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
	}
	
	public MotionData(Player player, float yaw, float pitch) {
		this(player, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), yaw, pitch);
	}
	
	public MotionData(Player player, double x, double y, double z, float yaw, float pitch) {
		this(player, new Location(player.getWorld(), x, y, z, yaw, pitch));
	}
	
	public MotionData(Player player, Location location) {
		super(player);
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MotionData) {
			MotionData data = (MotionData) obj;
			return location.equals(data.location);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "MOTION: " + getPlayer().getName() + "={location={" + Wrapper.locationToString(location, false) + "}}";
	}
	
}