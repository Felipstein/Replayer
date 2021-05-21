package br.lois.replayer.replaying;

import br.lois.replayer.Main;

public class Replayers {
	
	private Replayer replaying;
	
	public void startReplay(Replayer replayer) {
		this.replaying = replayer;
		this.replaying.start();
	}
	
	public boolean isReplaying() {
		return replaying != null;
	}
	
	public void setReplaying(Replayer replaying) {
		this.replaying = replaying;
	}
	
	public Replayer getReplaying() {
		return replaying;
	}
	
	public static Replayers getInstance() {
		return Main.getPlugin().getReplayers();
	}
	
}