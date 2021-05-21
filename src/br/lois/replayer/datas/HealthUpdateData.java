package br.lois.replayer.datas;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class HealthUpdateData extends PlayerData {
	
	private double health;
	private DamageCause cause;
	
	public HealthUpdateData(Player player, double health, DamageCause cause) {
		super(player);
		this.health = health;
		this.cause = cause;
	}
	
	public double getHealth() {
		return health;
	}
	
	public boolean hasCause() {
		return cause != null;
	}
	
	public DamageCause getCause() {
		return cause;
	}
	
	@Override
	public String toString() {
		return "HEALTH-UPDATE: " + getPlayer().getName() + "={health=" + health + "d}";
	}
	
}