package br.lois.replayer.datas;

import org.bukkit.entity.Player;

public class BurnData extends PlayerData {
	
	private boolean burning;
	
	public BurnData(Player player, boolean burning) {
		super(player);
		this.burning = burning;
	}
	
	public boolean isBurning() {
		return burning;
	}
	
	@Override
	public String toString() {
		return "BURNING: " + getPlayer().getName() + "={burning=" + burning + "}";
	}
	
}