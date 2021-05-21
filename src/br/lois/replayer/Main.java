package br.lois.replayer;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import br.lois.replayer.commands.*;
import br.lois.replayer.recording.Recorders;
import br.lois.replayer.replaying.Replayers;
import br.lois.replayer.robot.Robots;

public class Main extends JavaPlugin {
	
	public static final String SERVER_VERSION = "v1_16_R3";
	
	private static Main plugin;
	
	private Recorders recorders;
	private Replayers replayers;
	private Robots robots;
	
	@Override
	public void onEnable() {
		Main.plugin = this;
		this.recorders = new Recorders();
		this.replayers = new Replayers();
		this.robots = new Robots();
		this.setupCommand("recorder", new RecorderCommand());
		this.setupCommand("replayer", new ReplayerCommand());
	}
	
	@Override
	public void onDisable() {
		if(Replayers.getInstance().isReplaying()) {
			Replayers.getInstance().getReplaying().stop();
		}
	}
	
	private void setupCommand(String syntax, Object commandClass) {
		this.getCommand(syntax).setExecutor((CommandExecutor) commandClass);
		this.getCommand(syntax).setTabCompleter((TabCompleter) commandClass);
	}
	
	public Replayers getReplayers() {
		return replayers;
	}
	
	public Recorders getRecorders() {
		return recorders;
	}
	
	public Robots getRobots() {
		return robots;
	}
	
	public static Main getPlugin() {
		return plugin;
	}
	
}