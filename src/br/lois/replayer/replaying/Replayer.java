package br.lois.replayer.replaying;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.packetwrapper.AbstractPacket;

import br.lois.replayer.Main;
import br.lois.replayer.Metadata;
import br.lois.replayer.Metadata.MetadataPose;
import br.lois.replayer.Wrapper;
import br.lois.replayer.datas.AnimationData;
import br.lois.replayer.datas.BedEnterData;
import br.lois.replayer.datas.BedLeaveData;
import br.lois.replayer.datas.BurnData;
import br.lois.replayer.datas.Data;
import br.lois.replayer.datas.HealthUpdateData;
import br.lois.replayer.datas.InitData;
import br.lois.replayer.datas.InventoryChangedData;
import br.lois.replayer.datas.InventoryUpdateData;
import br.lois.replayer.datas.ItemDropedData;
import br.lois.replayer.datas.MotionData;
import br.lois.replayer.datas.SneakingData;
import br.lois.replayer.datas.SpritingData;
import br.lois.replayer.datas.SwimmingData;
import br.lois.replayer.recording.Recorder;
import br.lois.replayer.robot.Robot;
import net.minecraft.server.v1_16_R3.Packet;

public class Replayer implements Listener {
	
	private List<Player> watchings;
	private Recorder recorded;
	
	private Robot replayer;
	
	private boolean running, finished;
	
	private BukkitTask task;
	
	private long speed;
	private int currentTick;
	
	public Replayer(Player watching, Recorder recorded) {
		Validate.isTrue(recorded.isFinish(), "Você não pode dar replay em uma gravação que ainda não foi finalizada.");
		this.watchings = new ArrayList<>(Arrays.asList(watching));
		this.recorded = recorded;
		this.speed = 1l;
		Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
	}
	
	private BukkitRunnable getRunnable() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				if(!running) {
					Wrapper.sendActionBar(watchings, "§c* PAUSADO * §2Tick atual: §6" + currentTick + "t/" + getMaxTicks() + "t §c* PAUSADO *");
					return;
				}
				if(currentTick > getMaxTicks()) {
					sendMessageToWatchers("§aReplay finalizado.");
					Replayer.this.stop();
					return;
				}
				Set<Data> datas = recorded.getDatas(currentTick);
				if(datas != null && !datas.isEmpty()) {
					datas.forEach(data -> makeAction(data));
				}
				Wrapper.sendActionBar(watchings, "§2Tick atual: §6" + currentTick + "t/" + getMaxTicks() + "t");
				currentTick += speed;
			}
		};
	}
	
	private void makeAction(Data dated) {
		if(dated instanceof InitData) {
			InitData data = (InitData) dated;
			this.replayer = new Robot(data.getPlayer(), data.getHealth(), data.getLocation());
			this.replayer.spawn();
			this.replayer.getInventory().setAllSlots(data.getMainSlots());
			
		} else {
			return;
		}
		if(dated instanceof InitData) {
			InitData data = (InitData) dated;
			this.replayer = new Robot(data.getPlayer(), data.getHealth(), data.getLocation());
			this.replayer.spawn();
			this.replayer.getInventory().setAllSlots(data.getMainSlots());
		} else if(dated instanceof AnimationData) {
			this.replayer.makeAnimation(((AnimationData) dated).getAnimationId());
		} else if(dated instanceof BedEnterData) {
			if(((BedEnterData) dated).getResult() == BedEnterResult.OK) {
				this.replayer.teleport(((BedEnterData) dated).getBedLocation());
				Metadata metadata = Metadata.getMetadataBuilder(((BedEnterData) dated).getPlayer());
				metadata.setPose(MetadataPose.SLEEPING);
				this.replayer.setData(metadata.getDataWatcher());
				this.replayer.updateMetadata();
			}
		} else if(dated instanceof HealthUpdateData) {
			this.replayer.setHealth(((HealthUpdateData) dated).getHealth());
			if(((HealthUpdateData) dated).getCause() != null) {
				switch(((HealthUpdateData) dated).getCause()) {
				case FIRE:
				case FIRE_TICK:
				case LAVA:
					replayer.getLocation().getWorld().playSound(replayer.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0f, 1.0f);
					return;
				case DROWNING:
					replayer.getLocation().getWorld().playSound(replayer.getLocation(), Sound.ENTITY_PLAYER_HURT_DROWN, 1.0f, 1.0f);
					return;
				default:
					replayer.getLocation().getWorld().playSound(replayer.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
				}
			}
		} else if(dated instanceof MotionData) {
			this.replayer.teleport(((MotionData) dated).getLocation());
		} else if(dated instanceof SneakingData) {
			Metadata metadata = Metadata.getMetadataBuilder(((SneakingData) dated).getPlayer());
			metadata.setPose(((SneakingData) dated).isSneaking() ? MetadataPose.SNEAKING : MetadataPose.STANDING);
			this.replayer.setData(metadata.getDataWatcher());
			this.replayer.updateMetadata();
		} else if(dated instanceof SpritingData) {
			Metadata metadata = Metadata.getMetadataBuilder(((SpritingData) dated).getPlayer());
			metadata.setSpriting(((SpritingData) dated).isSpriting());
			metadata.loadSupplementedStatus();
			this.replayer.setData(metadata.getDataWatcher());
			this.replayer.updateMetadata();
		} else if(dated instanceof BedLeaveData) {
			Metadata metadata = Metadata.getMetadataBuilder(((SpritingData) dated).getPlayer());
			metadata.setPose(MetadataPose.STANDING);
			this.replayer.setData(metadata.getDataWatcher());
			this.replayer.updateMetadata();
			this.replayer.makeAnimation(AnimationData.LEAVE_BED);
		} else if(dated instanceof InventoryUpdateData) {
			this.replayer.getInventory().setSlot(((InventoryUpdateData) dated).getSlot(), ((InventoryUpdateData) dated).getItem());
		} else if(dated instanceof InventoryChangedData) {
			this.replayer.getInventory().setAllSlots(((InventoryChangedData) dated).getSlots());
		} else if(dated instanceof BurnData) {
			Metadata metadata = Metadata.getMetadataBuilder(((BurnData) dated).getPlayer());
			metadata.setBurning(((BurnData) dated).isBurning());
			metadata.loadSupplementedStatus();
			this.replayer.setData(metadata.getDataWatcher());
			this.replayer.updateMetadata();
		} else if(dated instanceof SwimmingData) {
			Metadata metadata = Metadata.getMetadataBuilder(((SwimmingData) dated).getPlayer());
			metadata.setSwimming(((SwimmingData) dated).isSwimming());
			metadata.loadSupplementedStatus();
			metadata.setPose(((SwimmingData) dated).isSwimming() ? MetadataPose.SWIMMING : MetadataPose.STANDING);
			this.replayer.setData(metadata.getDataWatcher());
			this.replayer.updateMetadata();
		} else if(dated instanceof ItemDropedData) {
			Item item = ((ItemDropedData) dated).getLocation().getWorld().dropItemNaturally(((ItemDropedData) dated).getLocation(), ((ItemDropedData) dated).getItem());
			item.setVelocity(((ItemDropedData) dated).getVelocity());
		}
	}
	
	public void start() {
		if(finished) {
			this.finished = false;
		}
		this.running = true;
		this.task = getRunnable().runTaskTimer(Main.getPlugin(), 1l, 1l);
	}
	
	public void stop() {
		if(finished) {
			return;
		}
		this.running = false;
		this.finished = true;
		if(task != null) {
			this.task.cancel();
			this.task = null;
		}
		if(replayer != null) {
			this.replayer.remove();
		}
		HandlerList.unregisterAll(this);
		Replayers.getInstance().setReplaying(null);
	}
	
	public void pause() {
		if(finished) {
			return;
		}
		this.running = false;
	}
	
	public void resume() {
		if(finished) {
			return;
		}
		this.running = true;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if(watchings.contains(e.getPlayer())) {
			this.watchings.remove(e.getPlayer());
			if(watchings.isEmpty()) {
				this.stop();
			}
		}
	}
	
	public void setSpeed(long speed) {
		this.speed = speed;
	}
	
	public int getCurrentTick() {
		return currentTick;
	}
	
	public void setCurrentTick(int currentTick, boolean makeActions) {
		Validate.isTrue(currentTick <= getMaxTicks() && currentTick >= 0, "Você não pode pular para um tick que não existe.");
		this.currentTick = currentTick;
		if(makeActions) {
			Set<Data> datas = recorded.getDatas(currentTick);
			if(datas != null && !datas.isEmpty()) {
				datas.forEach(data -> makeAction(data));
			}
		}
	}
	
	public int getMaxTicks() {
		return recorded.getCurrentTick();
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public BukkitTask getTask() {
		return task;
	}
	
	public List<Player> getWatchings() {
		return Collections.unmodifiableList(watchings);
	}
	
	public void addWatcher(Player player) {
		this.watchings.add(player);
	}
	
	public void removeWatcher(Player player) {
		this.watchings.remove(player);
	}
	
	public Player getWatching(String name) {
		for(Player player : watchings) {
			if(player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}
	
	public Recorder getRecorded() {
		return recorded;
	}
	
	public Robot getReplayingReplayerRobot() {
		return replayer;
	}
	
	public void sendMessageToWatchers(Object message) {
		this.watchings.forEach(player -> player.sendMessage(String.valueOf(message)));
	}
	
	@SuppressWarnings("unused")
	private void handlePackets(AbstractPacket... packets) {
		for(AbstractPacket packet : packets) {
			this.watchings.forEach(player -> packet.sendPacket(player));
		}
	}
	
	@SuppressWarnings("unused")
	private void handlePackets(Packet<?>... packets) {
		for(Packet<?> packet : packets) {
			this.watchings.forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet));
		}
	}
	
}