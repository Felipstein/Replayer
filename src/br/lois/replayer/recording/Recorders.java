package br.lois.replayer.recording;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import br.lois.replayer.Main;

public class Recorders implements Listener {
	
	private List<Recorder> recorders;
	
	private ItemStack itemRecorder;
	
	public Recorders() {
		this.recorders = new ArrayList<>();
		this.loadItemRecorder();
		Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
	}
	
	private void loadItemRecorder() {
		this.itemRecorder = new ItemStack(Material.CLOCK);
		ItemMeta meta = itemRecorder.getItemMeta();
		meta.setDisplayName("§eReplayer");
		meta.setLore(new ArrayList<>(Arrays.asList(" ", "§7Use §fbotão direito §7do mouse para §finiciar/finalizar §7a gravação", "", "§7Use §fbotão direito §7do mouse segurando §cSHIFT §7para deletar uma gravação já existente")));
		this.itemRecorder.setItemMeta(meta);
	}
	
	public Recorder getRecorder(Player recorder) {
		for(Recorder replayer : recorders) {
			if(replayer.getRecorder() == recorder) {
				return replayer;
			}
		}
		return null;
	}
	
	@EventHandler
	public void onUseItemRecorder(PlayerInteractEvent e) {
		if(e.getItem() != null) {
			if(e.getItem().equals(itemRecorder)) {
				if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					Recorder r = getRecorder(e.getPlayer());
					if(e.getPlayer().isSneaking()) {
						if(r == null) {
							e.getPlayer().sendMessage("§cNenhuma gravação encontrada.");
						} else {
							r.stop();
							Recorders.getInstance().getRecorders().remove(r);
							e.getPlayer().sendMessage("§aGravação removida com êxito.");
						}
					} else {
						if(r == null) {
							(r = new Recorder(e.getPlayer())).start();
							this.recorders.add(r);
							e.getPlayer().sendMessage("§aGravação iniciada.");
						} else if(r.isFinish()) {
							e.getPlayer().sendMessage("§cJá existe uma gravação! Utilize \"/r delete\" para deletar.");
						} else {
							r.stop();
							e.getPlayer().sendMessage("§aGravação finalizada.");
							e.getPlayer().sendMessage("§aTotal ticks: " + r.getCurrentTick() + "t");
						}
					}
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Recorder r = getRecorder(e.getPlayer());
		if(r != null) {
			if(!r.isFinish()) {
				r.stop();
				this.recorders.remove(r);
			}
		}
	}
	
	public ItemStack getItemRecorder() {
		return itemRecorder;
	}
	
	public List<Recorder> getRecorders() {
		return recorders;
	}
	
	public static Recorders getInstance() {
		return Main.getPlugin().getRecorders();
	}
	
}