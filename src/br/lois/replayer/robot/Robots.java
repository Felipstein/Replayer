package br.lois.replayer.robot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.lois.replayer.Main;

public class Robots {
	
	private Set<Robot> robots;
	
	public Robots() {
		this.robots = new HashSet<>();
	}
	
	public Robot getRobot(String name) {
		for(Robot robot : robots) {
			if(robot.getName().equalsIgnoreCase(name)) {
				return robot;
			}
		}
		return null;
	}
	
	public Robot getRobot(UUID uuid) {
		for(Robot robot : robots) {
			if(robot.getUniqueId().equals(uuid)) {
				return robot;
			}
		}
		return null;
	}
	
	public void addRobot(Robot robot) {
		this.robots.add(robot);
	}
	
	public void removeRobot(Robot robot) {
		this.robots.remove(robot);
	}
	
	public Set<Robot> getRobots() {
		return robots;
	}
	
	public static Robots getInstance() {
		return Main.getPlugin().getRobots();
	}
	
}