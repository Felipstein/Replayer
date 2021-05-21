package br.lois.replayer.datas;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import br.lois.replayer.Wrapper;

public class ItemDropedData extends PlayerData {
	
	private ItemStack item;
	private Vector velocity;
	private Location location;
	
	public ItemDropedData(Player player, Item item) {
		super(player);
		this.item = item.getItemStack().clone();
		this.velocity = item.getVelocity();
		this.location = item.getLocation();
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public Vector getVelocity() {
		return velocity;
	}
	
	public Location getLocation() {
		return location;
	}
	
	@Override
	public String toString() {
		return "ITEM-DROPPED: " + getPlayer().getName() + "={type=" + item.getType() + ",location={" + Wrapper.locationToString(location, false) + "}}";
	}
	
}