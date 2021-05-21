package br.lois.replayer.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.packetwrapper.WrapperPlayServerAnimation;

import br.lois.replayer.Main;
import br.lois.replayer.Wrapper;
import br.lois.replayer.datas.AnimationData;
import br.lois.replayer.datas.Data;
import br.lois.replayer.datas.MotionData;
import br.lois.replayer.datas.PlayerActionData;
import br.lois.replayer.datas.SneakingData;
import br.lois.replayer.datas.SpritingData;
import br.lois.replayer.recording.Recorder;
import br.lois.replayer.recording.Recorders;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class RecorderCommand implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if(!(s instanceof Player)) {
			return false;
		}
		Player p = (Player) s;
		if(args.length == 0) {
			p.sendMessage("§6» §a/r item §fAdquire o item Replayer.");
			p.sendMessage("§6» §a/r total §fExibe informações de todas as gravação.");
			p.sendMessage("§6» §a/r info §fExibe informações da gravação.");
			p.sendMessage("§6» §a/r start [true|false] §fComeça a gravação.");
			p.sendMessage("§6» §a/r pause §fPausa a gravação.");
			p.sendMessage("§6» §a/r resume §fContinua a gravação.");
			p.sendMessage("§6» §a/r stop §fPara a gravação.");
			p.sendMessage("§6» §a/r delete §fRemove a gravação.");
			p.sendMessage("§6» §6/r outputdatas §fExibe todos os dados registrados por ticks.");
			p.sendMessage("§6» §c/r repeat §fRepete seus dados da gravação.");
			return true;
		}
		Recorder r = Recorders.getInstance().getRecorder(p);
		if(args[0].equals("item")) {
			p.getInventory().addItem(Recorders.getInstance().getItemRecorder());
		} else if(args[0].equals("total")) {
			if(Recorders.getInstance().getRecorders().isEmpty()) {
				p.sendMessage("§cNenhuma gravação encontrada.");
				return false;
			}
			for(Recorder replayer : Recorders.getInstance().getRecorders()) {
				p.sendMessage("§6» §a" + replayer.getRecorder().getName() + " §7- §bTicks: " + replayer.getCurrentTick() + "t");
			}
		} else if(args[0].equals("info")) {
			if(r == null) {
				p.sendMessage("§cNenhuma gravação registrada.");
				return false;
			}
			if(r.isRecording()) {
				p.sendMessage("§aGravação em progresso:");
				p.sendMessage("§7Tick atual: §f" + r.getCurrentTick() + "t");
			} else {
				if(r.isFinish()) {
					p.sendMessage("§cGravação finalizada.");
				} else {
					p.sendMessage("§cGravação pausada.");
				}
				p.sendMessage("§cTicks no total: " + r.getCurrentTick() + "t");
			}
		} else if(args[0].equals("start")) {
			if(r != null) {
				p.sendMessage("§cJá existe uma gravação! Remova com \"/r delete\".");
				return false;
			}
			(r = new Recorder(p)).start();
			Recorders.getInstance().getRecorders().add(r);
			p.sendMessage("§aGravação iniciada.");
		} else if(args[0].equals("pause")) {
			if(r == null) {
				p.sendMessage("§cNenhuma gravação ocorrendo no momento.");
				return false;
			}
			if(r.isPaused()) {
				p.sendMessage("§cJá está pausada.");
				return false;
			}
			if(r.isFinish()) {
				p.sendMessage("§cA gravação já foi finalizada.");
				return false;
			}
			r.pause();
			p.sendMessage("§aGravação pausada.");
			p.sendMessage("§aTicks no total: " + r.getCurrentTick() + "t");
		} else if(args[0].equals("resume")) {
			if(r == null) {
				p.sendMessage("§cNenhuma gravação ocorrendo no momento.");
				return false;
			}
			if(r.isFinish()) {
				p.sendMessage("§cA gravação já foi finalizada.");
				return false;
			}
			if(!r.isPaused()) {
				p.sendMessage("§cA gravação não está pausada.");
				return false;
			}
			r.resume();
			p.sendMessage("§aGravação despausada.");
		} else if(args[0].equals("stop")) {
			if(r == null) {
				p.sendMessage("§cNenhuma gravação ocorrendo no momento.");
				return false;
			}
			if(r.isFinish()) {
				p.sendMessage("§cA gravação já foi finalizada.");
				return false;
			}
			r.stop();
			p.sendMessage("§aGravação finalizada.");
			p.sendMessage("§aTicks no total: " + r.getCurrentTick() + "t");
		} else if(args[0].equals("delete")) {
			if(r == null) {
				p.sendMessage("§cNenhuma gravação encontrada.");
				return false;
			}
			r.stop();
			Recorders.getInstance().getRecorders().remove(r);
			p.sendMessage("§aGravação deletada.");
		} else if(args[0].equals("outputdatas") || args[0].equals("out")) {
			if(r == null) {
				p.sendMessage("§cNenhuma gravação encontrada.");
				return false;
			}
			if(r.getDatas().isEmpty()) {
				p.sendMessage("§c[ERRO] Nenhum dado foi registrado na gravação (?)");
				return false;
			}
			for(Map.Entry<Long, Set<Data>> entry : r.getDatas().entrySet()) {
				long tick = entry.getKey();
				Set<Data> datas = entry.getValue();
				StringBuilder sb = new StringBuilder();
				for(Data data : datas) {
					sb.append("§c» §f" + data.toString() + "\n");
				}
				TextComponent text1 = new TextComponent("§6." + tick + " §7 - ");
				TextComponent text2 = new TextComponent("§f" + datas.size() + " §eAção(ões)/Dado(s) registrados");
				TextComponent text = new TextComponent();
				text2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(sb.toString())));
				text.addExtra(text1);
				text.addExtra(text2);
				p.spigot().sendMessage(text);
			}
			p.sendMessage("§7§oOBS.: ticks não exibidos significa que não foram registrados nenhuma ação/dado.");
		} else if(args[0].equals("repeat")) {
			if(r == null) {
				p.sendMessage("§cNenhuma gravação encontrada.");
				return false;
			}
			if(!r.isFinish()) {
				p.sendMessage("§cFinalize primeiro.");
				return false;
			}
			Recorder rr = r;
			new BukkitRunnable() {
				long currentTick;
				@Override
				public void run() {
					if(currentTick > rr.getCurrentTick()) {
						this.cancel();
						return;
					}
					Set<Data> datas = rr.getDatas(currentTick);
					if(datas == null) {
						++currentTick;
						return;
					}
					if(!datas.isEmpty()) {
						for(Data data : datas) {
							if(data instanceof MotionData) {
								p.teleport(((MotionData) data).getLocation());
							} else if(data instanceof AnimationData) {
								WrapperPlayServerAnimation packet = new WrapperPlayServerAnimation();
								packet.setEntityID(((AnimationData) data).getPlayer().getEntityId());
								packet.setAnimation(((AnimationData) data).getAnimationId());
								packet.sendPacket(p);
							} else if(data instanceof SneakingData) {
								p.setSneaking(((SneakingData) data).isSneaking());
							} else if(data instanceof SpritingData) {
								p.setSprinting(((SpritingData) data).isSpriting());
							} else if(data instanceof PlayerActionData) {
								
							}
						}
					}
					Wrapper.sendActionBar(p, "§2Tick atual: §6" + currentTick + "t" + "/" + rr.getCurrentTick() + "t");
					++currentTick;
				}
			}.runTaskTimer(Main.getPlugin(), 1l, 1l);
		} else {
			p.sendMessage("§cArgumento desconhecido.");
			return false;
		}
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
		if(!(s instanceof Player)) {
			return null;
		}
		if(args.length == 1) {
			return Wrapper.getElementsStartingWith(args[0], false, "item", "total", "info", "start", "pause", "resume", "stop", "delete", "outputdatas", "repeat");
		}
		if(args[0].equals("start")) {
			return new ArrayList<>(Arrays.asList("true", "false"));
		}
		return new ArrayList<>();
	}
	
}