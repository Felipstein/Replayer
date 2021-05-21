package br.lois.replayer.datas;

import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;

public class SneakingData extends PlayerActionData {
	
	public SneakingData(Player player, boolean sneaking) {
		super(player, sneaking ? PlayerAction.START_SNEAKING : PlayerAction.STOP_SNEAKING);
	}
	
	public boolean isSneaking() {
		return getAction() == PlayerAction.START_SNEAKING;
	}
	
	@Override
	public String toString() {
		return "SNEAKING : " + getPlayer().getName() + "={sneaking=" + isSneaking() + ",sneaking-enum:" + getAction().toString() + "}";
	}
	
}