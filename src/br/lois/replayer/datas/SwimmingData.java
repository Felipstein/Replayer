package br.lois.replayer.datas;

import org.bukkit.entity.Player;

public class SwimmingData extends PlayerData {
	
	private boolean swimming;
	
	public SwimmingData(Player player, boolean swimming) {
		super(player);
		this.swimming = swimming;
	}
	
	public boolean isSwimming() {
		return swimming;
	}
	
	@Override
	public String toString() {
		return "SWIMMING: " + getPlayer().getName() + "={swimming=" + swimming + "}";
	}
	
}