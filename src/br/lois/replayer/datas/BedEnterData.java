package br.lois.replayer.datas;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;

import br.lois.replayer.Wrapper;

public class BedEnterData extends PlayerData {
	
	private Location bedLocation;
	private BedEnterResult result;
	
	public BedEnterData(Player player, Location bedLocation, BedEnterResult result) {
		super(player);
		this.bedLocation = bedLocation;
		this.result = result;
	}
	
	public Location getBedLocation() {
		return bedLocation;
	}
	
	public BedEnterResult getResult() {
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof BedEnterData) {
			return ((BedEnterData) obj).bedLocation.equals(bedLocation) && ((BedEnterData) obj).result == result;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "BED-ENTER: " + getPlayer().getName() + "={bed-location=" + Wrapper.locationToString(bedLocation, true) + ",result=" + result.toString() + "}";
	}
	
}