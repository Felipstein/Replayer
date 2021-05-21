package br.lois.replayer.datas;

import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;

public class PlayerActionData extends PlayerData {
	
	private PlayerAction action;
	
	public PlayerActionData(Player player, PlayerAction action) {
		super(player);
		this.action = action;
	}
	
	public PlayerAction getAction() {
		return action;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PlayerActionData) {
			return ((PlayerActionData) obj).action == action;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "PLAYER-ACTION: " + getPlayer().getName() + "={action=" + action.toString() + "}";
	}
	
}