package br.lois.replayer;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import net.minecraft.server.v1_16_R3.EntityPose;

public class Metadata {
	
	private Player player;
	
	private WrappedDataWatcher dataWatcher;
	
	private boolean burning, sneaking, spriting, swimming, elytra;
	
	public Metadata(Player player) {
		this.dataWatcher = WrappedDataWatcher.getEntityWatcher(player).deepClone();
	}
	
	private void setValue(int index, Object value) {
		this.dataWatcher.setObject(index, value);
	}
	
	
	public MetadataStatus getStatus() {
		return MetadataStatus.getByMask((byte) dataWatcher.getObject(0));
	}
	
	
	public void setStatus(MetadataStatus status) {
		this.setValue(0, status.getMask());
	}
	
	public void setStatusToDefault() {
	//	this.setValue(0, (byte) 0);
		this.setStatus(MetadataStatus.DEFAULT);
	}
	
	public void loadSupplementedStatus() {
		byte finalValue = 0;
		if(burning) {
			finalValue = (byte) (finalValue | MetadataStatus.BURNING.mask);
		}
		if(sneaking) {
			finalValue = (byte) (finalValue | MetadataStatus.SNEAKING.mask);
		}
		if(spriting) {
			finalValue = (byte) (finalValue | MetadataStatus.SPRITING.mask);
		}
		if(swimming) {
			finalValue = (byte) (finalValue | MetadataStatus.SWIMMING.mask);
		}
		if(elytra) {
			finalValue = (byte) (finalValue | MetadataStatus.FLYING_WITH_ELYTRA.mask);
		}
		this.setValue(0, finalValue);
	}
	
	public void setSneaking(boolean sneaking) {
		this.sneaking = sneaking;
	}
	
	public boolean isSneaking() {
		return sneaking;
	}
	
	public void setSpriting(boolean spriting) {
		this.spriting = spriting;
	}
	
	public boolean isSpriting() {
		return spriting;
	}
	
	public void setSwimming(boolean swimming) {
		this.swimming = swimming;
	}
	
	public boolean isSwimming() {
		return swimming;
	}
	
	public void setBurning(boolean burning) {
		this.burning = burning;
	}
	
	public boolean isBurning() {
		return burning;
	}
	
	public void setElytra(boolean elytra) {
		this.elytra = elytra;
	}
	
	public boolean isElytra() {
		return elytra;
	}
	
	public boolean isCustomNameVisible() {
		return (boolean) dataWatcher.getObject(3);
	}
	
	public void setCustomNameVisible(boolean customNameVisible) {
		this.setValue(3, customNameVisible);
	}
	
	public boolean hasNoGravity() {
		return (boolean) dataWatcher.getObject(5);
	}
	
	public void setNoGravity(boolean noGravity) {
		this.setValue(5, noGravity);
	}
	
	public MetadataPose getPose() {
		EntityPose entityPose = (EntityPose) dataWatcher.getObject(6);
		if(entityPose == null) {
			return MetadataPose.STANDING;
		}
		return MetadataPose.getByString(entityPose.toString());
	}
	
	public void setPose(MetadataPose pose) {
	//	Object enumField = null;
	//	try {
	//		Class<?> entityPose = Class.forName("net.minecraft.server." + Main.SERVER_VERSION + ".EntityPose");
	//		Method valueOf = entityPose.getMethod("valueOf", String.class);
	//		enumField = valueOf.invoke(null, pose.toString());
	//		
	//	} catch(Exception e) {
	//		e.printStackTrace();
	//	}
		this.setValue(6, EntityPose.valueOf(pose.toString()));
	}
	
	public void setPoseToDefault() {
		this.setPose(MetadataPose.STANDING);
	}
	
	public void toDefault() {
		this.setStatusToDefault();
		this.setPoseToDefault();
	}
	
	public WrappedDataWatcher getDataWatcher() {
		return dataWatcher;
	}
	
	public WrapperPlayServerEntityMetadata applyMetadata() {
		return applyMetadata(Bukkit.getOnlinePlayers());
	}
	
	public WrapperPlayServerEntityMetadata applyMetadata(Collection<? extends Player> sendPacketTo) {
		WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();
		packet.setEntityID(player.getEntityId());
		packet.setMetadata(dataWatcher.getWatchableObjects());
		sendPacketTo.forEach(player -> packet.sendPacket(player));
		return packet;
	}
	
	public static Metadata getMetadataBuilder(Player player) {
		return new Metadata(player);
	}
	
	public static enum MetadataStatus {
		
		DEFAULT(0),
		BURNING(0x01),
		SNEAKING(0x02),
		SPRITING(0x08),
		SWIMMING(0x10),
		INVISIBLE(0x20),
		GLOWING_EFFECT(0x40),
		FLYING_WITH_ELYTRA(0x80);
		
		private byte mask;
		
		MetadataStatus(int mask) {
			this((byte) mask);
		}
		
		MetadataStatus(byte mask) {
			this.mask = mask;
		}
		
		public byte getMask() {
			return mask;
		}
		
		public static MetadataStatus getByMask(byte mask) {
			switch(mask) {
				case (byte) 0: return DEFAULT;
				case (byte) 0x01: return BURNING;
				case (byte) 0x02: return SNEAKING;
				case (byte) 0x08: return SPRITING;
				case (byte) 0x10: return SWIMMING;
				case (byte) 0x20: return INVISIBLE;
				case (byte) 0x40: return GLOWING_EFFECT;
				case (byte) 0x80: return FLYING_WITH_ELYTRA;
				default: return null;
			}
		}
		
	}
	
	public static enum MetadataPose {
		
		DEFAULT(0),
		STANDING(0),
		FALL_FLYING(1),
		SLEEPING(2),
		SWIMMING(3),
		SPIN_ATTACK(4),
		SNEAKING(5),
		DYING(6);
		
		private int varInt;
		
		MetadataPose(int varInt) {
			this.varInt = varInt;
		}
		
		public int getVarInt() {
			return varInt;
		}
		
		@Override
		public String toString() {
			return super.toString().equals("DEFAULT") ? "STANDING" : super.toString().equals("SNEAKING") ? "CROUCHING" : super.toString();
		}
		
		public static MetadataPose getByVarInt(int varInt) {
			switch(varInt) {
			case 0: return STANDING;
			case 1: return FALL_FLYING;
			case 2: return SLEEPING;
			case 3: return SWIMMING;
			case 4: return SPIN_ATTACK;
			case 5: return SNEAKING;
			case 6: return DYING;
			default: return STANDING;
			}
		}
		
		public static MetadataPose getByString(String varString) {
			if(varString.equalsIgnoreCase("STANDING")) {
				return STANDING;
			} else if(varString.equalsIgnoreCase("FALL_FLYING")) {
				return FALL_FLYING;
			} else if(varString.equalsIgnoreCase("SLEEPING")) {
				return SLEEPING;
			} else if(varString.equalsIgnoreCase("SWIMMING")) {
				return SWIMMING;
			} else if(varString.equalsIgnoreCase("SPIN_ATTACK")) {
				return SPIN_ATTACK;
			} else if(varString.equalsIgnoreCase("SNEAKING") || varString.equalsIgnoreCase("CROUCHING")) {
				return SNEAKING;
			} else if(varString.equalsIgnoreCase("DYING")) {
				return DYING;
			} else {
				return STANDING;
			}
		}
		
	}
	
}