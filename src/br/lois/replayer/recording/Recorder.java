package br.lois.replayer.recording;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.packetwrapper.WrapperPlayClientEntityAction;
import com.comphenix.packetwrapper.WrapperPlayClientLook;
import com.comphenix.packetwrapper.WrapperPlayClientPosition;
import com.comphenix.packetwrapper.WrapperPlayClientPositionLook;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;

import br.lois.replayer.Main;
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
import br.lois.replayer.datas.PlayerActionData;
import br.lois.replayer.datas.SneakingData;
import br.lois.replayer.datas.SpritingData;
import br.lois.replayer.datas.SwimmingData;
import br.lois.replayer.robot.RobotInventory;
import br.lois.replayer.robot.RobotInventory.Slot;

public class Recorder implements Listener {
	
	public static final long MAX_TICKS_PER_REPLAY = 2000l;
	
	private Player recorder;
	
	private BukkitTask task;
	private boolean recording, finish;
	
	private Map<Long, Set<Data>> datas;
	private int currentTick;
	
	private PacketAdapter packetsRecorder;
	
	private boolean ignoreRepeatedDatas;
	
	private boolean recentBurnDataRegistered, recentSwimmingDataRegistered;
	
	private Set<Item> itemsDroped;
	
	public Recorder(Player toRecorder) {
		this.recorder = toRecorder;
		this.datas = new HashMap<>();
		this.itemsDroped = new HashSet<>();
		this.packetsRecorder();
		Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
	}
	
	private void packetsRecorder() {
		ProtocolLibrary.getProtocolManager().addPacketListener(packetsRecorder = new PacketAdapter(Main.getPlugin(),
				PacketType.Play.Client.POSITION,
				PacketType.Play.Client.LOOK,
				PacketType.Play.Client.POSITION_LOOK,
				PacketType.Play.Client.ARM_ANIMATION,
				PacketType.Play.Client.ENTITY_ACTION) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if(!Recorder.this.recording) {
					return;
				}
				PacketContainer packet = event.getPacket();
				PacketType type = packet.getType();
				if(type == PacketType.Play.Client.POSITION) {
					WrapperPlayClientPosition wrapped = new WrapperPlayClientPosition(packet);
					Recorder.this.saveData(new MotionData(event.getPlayer(), wrapped.getX(), wrapped.getY(), wrapped.getZ()));
				} else if(type == PacketType.Play.Client.LOOK) {
					WrapperPlayClientLook wrapped = new WrapperPlayClientLook(packet);
					Recorder.this.saveData(new MotionData(event.getPlayer(), wrapped.getYaw(), wrapped.getPitch()));
				} else if(type == PacketType.Play.Client.POSITION_LOOK) {
					WrapperPlayClientPositionLook wrapped = new WrapperPlayClientPositionLook(packet);
					Recorder.this.saveData(new MotionData(event.getPlayer(), wrapped.getX(), wrapped.getY(), wrapped.getZ(), wrapped.getYaw(), wrapped.getPitch()));
				} else if(type == PacketType.Play.Client.ARM_ANIMATION) {
					Recorder.this.saveData(new AnimationData(event.getPlayer(), 0));
				} else if(type == PacketType.Play.Client.ENTITY_ACTION) {
					WrapperPlayClientEntityAction wrapped = new WrapperPlayClientEntityAction(packet);
					PlayerAction action = wrapped.getAction();
					Data data;
					if(action == PlayerAction.START_SNEAKING || action == PlayerAction.STOP_SNEAKING) {
						data = new SneakingData(event.getPlayer(), action == PlayerAction.START_SNEAKING);
					} else if(action == PlayerAction.START_SPRINTING || action == PlayerAction.STOP_SPRINTING) {
						data = new SpritingData(event.getPlayer(), action == PlayerAction.START_SPRINTING);
					} else if(action == PlayerAction.STOP_SLEEPING) {
						data = new BedLeaveData(event.getPlayer());
					} else {
						data = new PlayerActionData(event.getPlayer(), action);
					}
					Recorder.this.saveData(data);
				}
			}
		});
	}
	
	@EventHandler
	public void onPlayerBurnOrSwim(PlayerMoveEvent e) {
		if(recorder == e.getPlayer()) {
			if(e.getPlayer().getFireTicks() > -20) {
				if(!recentBurnDataRegistered) {
					this.recentBurnDataRegistered = true;
					this.saveData(new BurnData(e.getPlayer(), true));
				}
			} else if(e.getPlayer().isSwimming()) {
				if(!recentSwimmingDataRegistered) {
					this.recentSwimmingDataRegistered = true;
					this.saveData(new SwimmingData(e.getPlayer(), true));
				}
			} else if(!e.getPlayer().isSwimming()) {
				if(recentSwimmingDataRegistered) {
					this.recentSwimmingDataRegistered = false;
					this.saveData(new SwimmingData(e.getPlayer(), false));
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			if(recorder == (Player) e.getEntity()) {
				Player player = (Player) e.getEntity();
				this.saveData(new AnimationData(player, AnimationData.DAMAGE));
				this.saveData(new HealthUpdateData(player, player.getHealth() - e.getDamage(), e.getCause()));
			}
		}
	}
	
	@EventHandler
	public void onPlayerEnterBed(PlayerBedEnterEvent e) {
		if(recorder == e.getPlayer()) {
			this.saveData(new BedEnterData(e.getPlayer(), e.getBed().getLocation(), e.getBedEnterResult()));
		}
	}
	
	@EventHandler
	public void onPlayerLeaveBed(PlayerBedLeaveEvent e) {
		if(recorder == e.getPlayer()) {
			this.saveData(new AnimationData(e.getPlayer(), AnimationData.LEAVE_BED));
		}
	}
	
	@EventHandler
	public void onPlayerCrit(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(recorder == p) {
				if(e.getDamager().getFallDistance() > 0.0f && !e.getDamager().isOnGround() && e.getDamager().getVehicle() == null) {
					this.saveData(new AnimationData(p, AnimationData.CRITICALS_EFFECT));
				}
				if(e.getDamager() instanceof Player) {
					Player d = (Player) e.getDamager();
					ItemStack item = d.getInventory().getItemInMainHand();
					if(item.containsEnchantment(Enchantment.DAMAGE_ALL)) {
						this.saveData(new AnimationData(p, AnimationData.MAGIC_CRITIC_EFFECT));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPutItemInHand(PlayerItemHeldEvent e) {
		if(recorder == e.getPlayer()) {
			ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
			this.saveData(new InventoryUpdateData(e.getPlayer(), Slot.MAIN_HAND, item));
		}
	}
	
	@EventHandler
	public void onSawpItemsInHands(PlayerSwapHandItemsEvent e) {
		if(recorder == e.getPlayer()) {
			if((e.getMainHandItem() == null || e.getMainHandItem().getType() == Material.AIR) && (e.getOffHandItem() == null || e.getOffHandItem().getType() == Material.AIR)) {
				return;
			}
			this.saveData(new InventoryUpdateData(e.getPlayer(), Slot.MAIN_HAND, e.getMainHandItem()));
			this.saveData(new InventoryUpdateData(e.getPlayer(), Slot.OFF_HAND, e.getOffHandItem()));
		}
	}
	
	@EventHandler
	public void onChangeMainSlots(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player) {
			if(recorder == (Player) e.getWhoClicked()) {
				this.saveData(new InventoryChangedData((Player) e.getWhoClicked(), RobotInventory.convertFrom(e.getWhoClicked().getInventory())));
			}
		}
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent e) {
		if(recorder == e.getPlayer()) {
			this.saveData(new InventoryChangedData(e.getPlayer(), RobotInventory.convertFrom(e.getPlayer().getInventory())));
			this.saveData(new ItemDropedData(e.getPlayer(), e.getItemDrop()));
			this.itemsDroped.add(e.getItemDrop());
		}
	}
	
	@EventHandler
	public void onPickupItem(EntityPickupItemEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(recorder == p) {
				if(p.getInventory().getItemInMainHand().getType() == Material.AIR) {
					int nextFreeSlot = 0;
					for(int i = 0; i < 9; ++i) {
						ItemStack item = p.getInventory().getItem(i);
						if(item == null || item.getType() == Material.AIR) {
							nextFreeSlot = i;
							break;
						}
					}
					if(nextFreeSlot == 0 && (p.getInventory().getItem(0) == null || p.getInventory().getItem(0).getType() != Material.AIR)) {
						return;
					}
					if(p.getInventory().getHeldItemSlot() == nextFreeSlot) {
						this.saveData(new InventoryUpdateData(p, Slot.MAIN_HAND, e.getItem().getItemStack()));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onOutOfBlocks(BlockPlaceEvent e) {
		if(recorder == e.getPlayer()) {
			if(e.getItemInHand().getAmount() == 1) {
				this.saveData(new InventoryChangedData(e.getPlayer(), RobotInventory.convertFrom(e.getPlayer().getInventory())));
			}
		}
	}
	
	@EventHandler
	public void onOutOfProjectiles(ProjectileLaunchEvent e) {
		if(e.getEntity().getShooter() instanceof Player) {
			Player p = (Player) e.getEntity().getShooter();
			if(recorder == p) {
				if(p.getInventory().getItemInMainHand().getType() != Material.BOW) {
					if(p.getInventory().getItemInMainHand().getAmount() == 1) {
						this.saveData(new InventoryChangedData(p, RobotInventory.convertFrom(p.getInventory())));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onWearTheArmor(PlayerInteractEvent e) {
		if(recorder == e.getPlayer()) {
			if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(e.getItem() == null) {
					return;
				}
				if(e.getItem().getType().toString().contains("_HELMET")) {
					if(e.getPlayer().getInventory().getArmorContents()[3] == null || e.getPlayer().getInventory().getArmorContents()[3].getType() == Material.AIR) {
						this.saveData(new InventoryUpdateData(e.getPlayer(), Slot.HELMET, e.getItem()));
					}
				} else if(e.getItem().getType().toString().contains("_CHESTPLATE")) {
					if(e.getPlayer().getInventory().getArmorContents()[2] == null || e.getPlayer().getInventory().getArmorContents()[2].getType() == Material.AIR) {
						this.saveData(new InventoryUpdateData(e.getPlayer(), Slot.CHESTPLATE, e.getItem()));
					}
				} else if(e.getItem().getType().toString().contains("_LEGGINGS")) {
					if(e.getPlayer().getInventory().getArmorContents()[1] == null || e.getPlayer().getInventory().getArmorContents()[1].getType() == Material.AIR) {
						this.saveData(new InventoryUpdateData(e.getPlayer(), Slot.LEGGINGS, e.getItem()));
					}
				} else if(e.getItem().getType().toString().contains("_BOOTS")) {
					if(e.getPlayer().getInventory().getArmorContents()[0] == null || e.getPlayer().getInventory().getArmorContents()[0].getType() == Material.AIR) {
						this.saveData(new InventoryUpdateData(e.getPlayer(), Slot.BOOTS, e.getItem()));
					}
				} else {
					return;
				}
				if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
					this.saveData(new InventoryUpdateData(e.getPlayer(), Slot.MAIN_HAND, null));
				}
			}
		}
	}
	
	@EventHandler
	public void onItemBreak(PlayerItemBreakEvent e) {
		if(recorder == e.getPlayer()) {
			this.saveData(new InventoryChangedData(e.getPlayer(), RobotInventory.convertFrom(e.getPlayer().getInventory())));
		}
	}
	
	private BukkitRunnable getRunnable() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				if(recording) {
					if(currentTick == 0l) {
						Recorder.this.saveData(new InitData(recorder));
					}
					if(currentTick > MAX_TICKS_PER_REPLAY) {
						recorder.sendMessage("§aGravação finalizada por excesso de tempo ultrapassado (" + MAX_TICKS_PER_REPLAY + " ticks).");
						Recorder.this.stop();
						this.cancel();
						return;
					}
					Wrapper.sendActionBar(recorder, "§aTick atual: §6" + currentTick + "t");
					if(recorder.getFireTicks() <= -20 && recentBurnDataRegistered) {
						recentBurnDataRegistered = false;
						saveData(new BurnData(recorder, false));
					}
					currentTick++;
					cleanDeathItemsDroped();
				}
			}
		};
	}
	
	private void cleanDeathItemsDroped() {
		new HashSet<>(itemsDroped).stream().filter(item -> item == null).forEach(item -> itemsDroped.remove(item));
	}
	
	public void saveData(Data data) {
		this.saveDatas(data);
	}
	
	public void saveDatas(Data... datas) {
		this.putDatas(currentTick, datas);
	}
	
	public void putDatas(long tick, Data... datas) {
		Set<Data> listDatas = this.datas.containsKey(tick) ? this.datas.get(tick) : new HashSet<>();
		if(ignoreRepeatedDatas) {
			listDatas.addAll(new HashSet<>(Arrays.asList(datas)));
		} else {
			Set<Data> datasField = new HashSet<>(listDatas);
			for(Data data : datas) {
				if(data instanceof MotionData) {
					if(hasM(datasField, (MotionData) data)) {
						continue;
					}
				}
				datasField.add(data);
			}
			listDatas = datasField;
		}
		this.datas.put(tick, listDatas);
	}
	
	private boolean hasM(Set<Data> datas, MotionData data) {
		for(Data data2 : datas) {
			if(data2 instanceof MotionData) {
				if(((MotionData) data2).equals(data)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void replaceDatas(long tick, Data... datas) {
		this.datas.put(tick, new HashSet<>(Arrays.asList(datas)));
	}
	
	public Set<Data> getDatas(long tick) {
		return datas.get(tick);
	}
	
	public boolean hasData(long tick) {
		return datas.containsKey(tick);
	}
	
	public void start() {
		this.recording = true;
		this.task = getRunnable().runTaskTimer(Main.getPlugin(), 1l, 1l);
	}
	
	public void pause() {
		this.recording = false;
	}
	
	public void resume() {
		this.recording = true;
	}
	
	public void stop() {
		this.recording = false;
		if(task != null) {
			this.task.cancel();
			this.task = null;
		}
		this.finish = true;
		ProtocolLibrary.getProtocolManager().removePacketListener(packetsRecorder);
		HandlerList.unregisterAll(this);
	}
	
	public Player getRecorder() {
		return recorder;
	}
	
	public boolean isPaused() {
		return !finish && !recording;
	}
	
	public boolean isFinish() {
		return finish;
	}
	
	public boolean isRecording() {
		return recording;
	}
	
	public Map<Long, Set<Data>> getDatas() {
		return datas;
	}
	
	public int getCurrentTick() {
		return currentTick;
	}
	
}