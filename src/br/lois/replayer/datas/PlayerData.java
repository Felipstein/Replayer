package br.lois.replayer.datas;

import org.bukkit.entity.Player;

public class PlayerData extends Data {
	
	private Player player;
	
	public PlayerData(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
}