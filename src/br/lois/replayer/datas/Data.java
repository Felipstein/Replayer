package br.lois.replayer.datas;

import java.util.Random;

public class Data {
	
	private int id;
	
	{
		this.id = new Random().nextInt(10000);
	}
	
	public int getDataId() {
		return id;
	}
	
	@Override
	public String toString() {
		return "Dado/ação de §e" + getClass().getSuperclass() + "§f.";
	}
	
}