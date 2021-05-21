package br.lois.replayer.datas;

import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;

public class SpritingData extends PlayerActionData {
	
	public SpritingData(Player player, boolean spriting) {
		super(player, spriting ? PlayerAction.START_SPRINTING : PlayerAction.STOP_SPRINTING);
	}
	
	public boolean isSpriting() {
		return getAction() == PlayerAction.START_SPRINTING;
	}
	
	@Override
	public String toString() {
		return "SPRITING : " + getPlayer().getName() + "={spriting=" + isSpriting() + ",spriting-enum:" + getAction().toString() + "}";
	}
	
}