package br.lois.replayer.datas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import br.lois.replayer.Wrapper;

public class BlockChangedData extends PlayerData {
	
	private Block changed;
	private Material oldMaterial, newMaterial;
	
	public BlockChangedData(Player player, Block changed, Material oldMaterial, Material newMaterial) {
		super(player);
		this.changed = changed;
		this.oldMaterial = oldMaterial;
		this.newMaterial = newMaterial;
	}
	
	public Block getBlockChanged() {
		return changed;
	}
	
	public Location getLocation() {
		return changed.getLocation();
	}
	
	public Material getOldMaterial() {
		return oldMaterial;
	}
	
	public Material getNewMaterial() {
		return newMaterial;
	}
	
	@Override
	public String toString() {
		return "BLOCK-CHANGED: " + getPlayer().getName() + "={type=" + changed.getType() + ",location={" + Wrapper.locationToString(changed.getLocation(), true) + "},old-material=" + oldMaterial + ",new-material=" + newMaterial + "}";
	}
	
}