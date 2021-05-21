package br.lois.replayer.robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.mojang.datafixers.util.Pair;

import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;

public class RobotInventory {
	
	private Robot owner;
	
	private Map<Slot, ItemStack> slots;
	
	public RobotInventory(Robot owner) {
		this.owner = owner;
		this.slots = new HashMap<>();
		this.loadKeySlots();
	}
	
	private void loadKeySlots() {
		this.setSlot(Slot.HELMET, new ItemStack(Material.AIR));
		this.setSlot(Slot.CHESTPLATE, new ItemStack(Material.AIR));
		this.setSlot(Slot.LEGGINGS, new ItemStack(Material.AIR));
		this.setSlot(Slot.BOOTS, new ItemStack(Material.AIR));
		this.setSlot(Slot.MAIN_HAND, new ItemStack(Material.AIR));
		this.setSlot(Slot.OFF_HAND, new ItemStack(Material.AIR));
	}
	
	public boolean hasItem(Slot slot) {
		return getItem(slot) != null && getItem(slot).getType() != Material.AIR;
	}
	
	public ItemStack getItem(Slot slot) {
		return slots.get(slot);
	}
	
	public void setSlot(Slot slot, ItemStack item) {
		this.slots.put(slot, item == null ? new ItemStack(Material.AIR) : item);
		this.updateEquipment();
	}
	
	private void updateEquipment() {
		List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> items = new ArrayList<>();
		this.slots.entrySet().forEach(entry -> items.add(new Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>(slotToEnumSlot(entry.getKey()), CraftItemStack.asNMSCopy(entry.getValue()))));
		this.owner.handlePackets(new PacketPlayOutEntityEquipment(owner.getId(), items));
		// TODO: Pacote WrapperPlayServerEntityEquipment não está atualizado, por isso utilizei o pacote padrão do Spigot.
	}
	
	public void clear() {
		this.slots.clear();
		this.loadKeySlots();
	}
	
	public void removeItemOfMainHand() {
		this.setSlot(Slot.MAIN_HAND, null);
	}
	
	public void removeItemOfOffHand() {
		this.setSlot(Slot.OFF_HAND, null);
	}
	
	public void removeItemsOfHands() {
		this.setSlot(Slot.MAIN_HAND, null);
		this.setSlot(Slot.OFF_HAND, null);
	}
	
	public void clearItemsOfArmor() {
		this.setSlot(Slot.HELMET, null);
		this.setSlot(Slot.CHESTPLATE, null);
		this.setSlot(Slot.LEGGINGS, null);
		this.setSlot(Slot.BOOTS, null);
	}
	
	public List<ItemStack> getItems() {
		List<ItemStack> items = new ArrayList<>();
		this.slots.values().forEach(item -> items.add(item));
		return Collections.unmodifiableList(items);
	}
	
	public Map<Slot, ItemStack> getSlots() {
		return Collections.unmodifiableMap(slots);
	}
	
	public void setAllSlots(Map<Slot, ItemStack> slots) {
		if(slots == null) {
			this.slots = new HashMap<>();
			this.loadKeySlots();
		} else {
			this.slots = slots;
		}
		this.updateEquipment();
	}
	
	public void copyFromInventory(PlayerInventory inventory) {
		this.setAllSlots(convertFrom(inventory));
	}
	
	public Robot getOwner() {
		return owner;
	}
	
	public static Map<Slot, ItemStack> convertFrom(PlayerInventory inventory) {
		Map<Slot, ItemStack> slots = new HashMap<>();
		slots.put(Slot.HELMET, inventory.getHelmet() != null ? inventory.getHelmet() : new ItemStack(Material.AIR));
		slots.put(Slot.CHESTPLATE, inventory.getChestplate() != null ? inventory.getChestplate() : new ItemStack(Material.AIR));
		slots.put(Slot.LEGGINGS, inventory.getLeggings() != null ? inventory.getLeggings() : new ItemStack(Material.AIR));
		slots.put(Slot.BOOTS, inventory.getBoots() != null ? inventory.getBoots() : new ItemStack(Material.AIR));
		slots.put(Slot.MAIN_HAND, inventory.getItemInMainHand() != null ? inventory.getItemInMainHand() : new ItemStack(Material.AIR));
		slots.put(Slot.OFF_HAND, inventory.getItemInOffHand() != null ? inventory.getItemInOffHand() : new ItemStack(Material.AIR));
		return slots;
	}
	
	public enum Slot {
		
		HELMET, CHESTPLATE, LEGGINGS, BOOTS, MAIN_HAND, OFF_HAND;
		
		public static Slot getById(int idSlot) {
			switch(idSlot) {
			case 40: return Slot.OFF_HAND;
			case 39: return Slot.HELMET;
			case 38: return Slot.CHESTPLATE;
			case 37: return Slot.LEGGINGS;
			case 36: return Slot.BOOTS;
			default: return Slot.MAIN_HAND;
			}
		}
		
	}
	
	private EnumItemSlot slotToEnumSlot(Slot slot) {
		switch(slot) {
		case HELMET: return EnumItemSlot.HEAD;
		case CHESTPLATE: return EnumItemSlot.CHEST;
		case LEGGINGS: return EnumItemSlot.LEGS;
		case BOOTS: return EnumItemSlot.FEET;
		case MAIN_HAND: return EnumItemSlot.MAINHAND;
		case OFF_HAND: return EnumItemSlot.OFFHAND;
		}
		return null;
	}
	
}