package br.lois.replayer.datas;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import br.lois.replayer.robot.RobotInventory;
import br.lois.replayer.robot.RobotInventory.Slot;

public class InitData extends PlayerData {
	
	private Location location;
	private double health;
	
	private int fireTicks;
	
	private Map<Slot, ItemStack> mainSlots;
	
	public InitData(Player player) {
		this(player, player.getLocation(), player.getHealth(), player.getFireTicks(), RobotInventory.convertFrom(player.getInventory()));
	}
	
	public InitData(Player player, Location location, double health, int fireTicks, Map<Slot, ItemStack> mainSlots) {
		super(player);
		this.location = location;
		this.health = health;
		this.fireTicks = fireTicks;
		this.mainSlots = mainSlots;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public double getHealth() {
		return health;
	}
	
	public int getFireTicks() {
		return fireTicks;
	}
	
	public Map<Slot, ItemStack> getMainSlots() {
		return mainSlots;
	}
	
	@Override
	public String toString() {
		return "INIT: " + getPlayer().getName() + "={<default-values-loaded>}";
	}
	
}