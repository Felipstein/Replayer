package br.lois.replayer.datas;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import br.lois.replayer.robot.RobotInventory;

public class InventoryUpdateData extends PlayerData {
	
	private RobotInventory.Slot slot;
	private ItemStack item;
	
	public InventoryUpdateData(Player player, RobotInventory.Slot slot, ItemStack item) {
		super(player);
		this.slot = slot;
		this.item = item == null ? null : item.clone();
	}
	
	public RobotInventory.Slot getSlot() {
		return slot;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	@Override
	public String toString() {
		return "INVENTORY-UPDATE: " + getPlayer().getName() + "={slot=" + slot.toString() + ",item=" + (item == null ? "AIR" : item.toString()) + "}";
	}
	
}