package br.lois.replayer.datas;

import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;

public class BedLeaveData extends PlayerActionData {
	
	public BedLeaveData(Player player) {
		super(player, PlayerAction.STOP_SLEEPING);
	}
	
}