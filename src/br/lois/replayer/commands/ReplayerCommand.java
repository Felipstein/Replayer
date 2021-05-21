package br.lois.replayer.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import br.lois.replayer.Wrapper;
import br.lois.replayer.recording.Recorder;
import br.lois.replayer.recording.Recorders;
import br.lois.replayer.replaying.Replayer;
import br.lois.replayer.replaying.Replayers;

public class ReplayerCommand implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if(!(s instanceof Player)) {
			return false;
		}
		Player p = (Player) s;
		if(args.length == 0) {
			p.sendMessage("§6» §a/rr play §fDá inicio ao replay.");
			p.sendMessage("§6» §a/rr pause §fPausa o replay.");
			p.sendMessage("§6» §a/rr resume §fDespausa o replay.");
			p.sendMessage("§6» §a/rr stop §fFinaliza o replay.");
			p.sendMessage("§6» §a/rr speed <velocidade> §fAltera a velocidade (por ticks) do replay.");
			p.sendMessage("§6» §a/rr settick <tick> [-noact] §fPule para determinado tick do replay.");
			p.sendMessage("§6» §a/rr invite <jogador> §fConvida algum jogador para assistir juntinho com você >.< :3.");
			return true;
		}
		Recorder r = Recorders.getInstance().getRecorder(p);
		if(r == null) {
			p.sendMessage("§cNenhuma gravação sua encontrada.");
			return false;
		}
		if(!r.isFinish()) {
			p.sendMessage("§cVocê deve finalizar primeiro a sua gravação.");
			return false;
		}
		Replayer rr = Replayers.getInstance().getReplaying();
		if(args[0].equals("play")) {
			if(rr != null) {
				p.sendMessage("§cJá foi iniciado um replay.");
				return false;
			}
			Replayers.getInstance().startReplay(new Replayer(p, r));
			p.sendMessage("§aReplay iniciado.");
		} else if(args[0].equals("pause")) {
			if(rr == null) {
				p.sendMessage("§cNenhum replay rodando.");
				return false;
			}
			if(rr.isFinished()) {
				p.sendMessage("§cO replay já terminou.");
				return false;
			}
			if(!rr.isRunning()) {
				p.sendMessage("§cO replay já está pausado.");
				return false;
			}
			rr.pause();
			p.sendMessage("§aReplay pausado.");
		} else if(args[0].equals("resume")) {
			if(rr == null) {
				p.sendMessage("§cNenhum replay rodando.");
				return false;
			}
			if(rr.isFinished()) {
				p.sendMessage("§cO replay já terminou.");
				return false;
			}
			if(rr.isRunning()) {
				p.sendMessage("§cO replay já está despausado.");
				return false;
			}
			rr.resume();
			p.sendMessage("§aReplay despausado.");
		} else if(args[0].equals("stop")) {
			if(rr == null) {
				p.sendMessage("§cNenhum replay rodando.");
				return false;
			}
			if(rr.isFinished()) {
				p.sendMessage("§cO replay já terminou.");
				return false;
			}
			rr.stop();
			p.sendMessage("§aReplay finalizado.");
		} else if(args[0].equals("speed")) {
			if(args.length == 1) {
				p.sendMessage("§cUtilize: \"/rr speed <velocidade>\".");
				return false;
			}
			long speed;
			try {
				speed = Long.parseLong(args[1]);
			} catch(NumberFormatException e) {
				p.sendMessage("§cValor não corresponde em números.");
				return false;
			}
			if(rr == null) {
				p.sendMessage("§cNenhum replay rodando.");
				return false;
			}
			if(rr.isFinished()) {
				p.sendMessage("§cO replay já terminou.");
				return false;
			}
			rr.setSpeed(speed);
			p.sendMessage("§aVelocidade alterado para §e" + speed + "t§a.");
		} else if(args[0].equals("settick")) {
			if(args.length == 1) {
				p.sendMessage("§cUtilize: \"/rr settick <tick>\".");
				return false;
			}
			int tick;
			try {
				tick = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				p.sendMessage("§cValor não corresponde em números.");
				return false;
			}
			boolean actions = true;
			if(args.length > 2) {
				actions = !args[2].equals("-noact");
			}
			if(rr == null) {
				p.sendMessage("§cNenhum replay rodando.");
				return false;
			}
			if(rr.isFinished()) {
				p.sendMessage("§cO replay já terminou.");
				return false;
			}
			rr.setCurrentTick(tick, actions);
			p.sendMessage("§aAlterado para o tick §e" + tick + "t§a.");
		} else if(args[0].equals("invite")) {
			if(args.length == 1) {
				p.sendMessage("§cUtilize: \"/rr invite <jogador>\".");
				return false;
			}
			Player player = Bukkit.getPlayer(args[1]);
			if(player == null) {
				p.sendMessage("§cJogador off-line.");
				return false;
			}
			if(rr == null) {
				p.sendMessage("§cNenhum replay rodando.");
				return false;
			}
			if(rr.isFinished()) {
				p.sendMessage("§cO replay já terminou.");
				return false;
			}
			rr.addWatcher(player);
			player.sendMessage("§aVocê foi convidado para assistir um replayzinho juntinho com §e" + p.getName() + " §a>.< :3 <3 sz s2");
			p.sendMessage("§aAgora §e" + player.getName() + " §aestá assistindo o replay.");
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
			return Wrapper.getElementsStartingWith(args[0], false, "play", "pause", "resume", "stop", "speed", "settick", "invite");
		}
		if(args[0].equals("speed")) {
			if(args.length == 2) {
				if(args[1].isEmpty()) {
					return new ArrayList<>(Arrays.asList("<velocidade>"));
				}
			}
		} else if(args[0].equals("settick")) {
			if(args.length == 2) {
				if(args[1].isEmpty()) {
					return new ArrayList<>(Arrays.asList("<tick>"));
				}
			}
			if(args.length == 3) {
				return new ArrayList<>(Arrays.asList("-noact"));
			}
		} else if(args[0].equals("invite")) {
			if(args.length == 2) {
				ArrayList<String> playersName = new ArrayList<>();
				Bukkit.getOnlinePlayers().stream().filter(player -> !player.getName().equals(s.getName())).forEach(player -> playersName.add(player.getName()));
				return Wrapper.getElementsStartingWith(args[1], true, playersName);
			}
		}
		return new ArrayList<>();
	}
	
}