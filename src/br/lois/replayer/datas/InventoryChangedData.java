package br.lois.replayer.datas;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import br.lois.replayer.robot.RobotInventory.Slot;

public class InventoryChangedData extends PlayerData {
	
	private Map<Slot, ItemStack> slots;
	
	{
		this.slots = new HashMap<>();
	}
	
	public InventoryChangedData(Player player, Map<Slot, ItemStack> mainSlots) {
		super(player);
		new HashMap<>(mainSlots).entrySet().forEach(entry -> slots.put(entry.getKey(), entry.getValue() == null ? new ItemStack(Material.AIR) : entry.getValue().clone()));
	}
	
	public Map<Slot, ItemStack> getSlots() {
		return slots;
	}
	
	@Override
	public String toString() {
		return "INVENTORY-CHANGED: " + getPlayer().getName() + "={main-hand=" + slots.get(Slot.MAIN_HAND) + "}";
	}
	
}