package br.lois.replayer.datas;

import org.bukkit.entity.Player;

public class AnimationData extends PlayerData {
	
	public static final int SWING_MAIN_HAND = 0;
	public static final int DAMAGE = 1;
	public static final int LEAVE_BED = 2;
	public static final int SWING_OFF_HAND = 3;
	public static final int EATING_FOOD = 3;
	public static final int CRITICALS_EFFECT = 4;
	public static final int MAGIC_CRITIC_EFFECT = 5;
	
	private int animationId;
	
	public AnimationData(Player player, int animationId) {
		super(player);
		this.animationId = animationId;
	}
	
	public int getAnimationId() {
		return animationId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AnimationData) {
			return ((AnimationData) obj).animationId == animationId;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "ANIMATION: " + getPlayer().getName() + "={animation-id:" + animationId + "}";
	}
	
}