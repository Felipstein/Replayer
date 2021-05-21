package br.lois.replayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;

public class Wrapper {
	
	public static void sendActionBar(List<Player> players, Object message) {
		players.forEach(player -> sendActionBar(player, message));
	}
	
	public static void sendActionBar(Player player, Object message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(String.valueOf(message)), ChatMessageType.GAME_INFO, player.getUniqueId());
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
	
	public static String locationToString(Location location, boolean castInt) {
		if(castInt) {
			return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getYaw() + "," + location.getPitch();
		} else {
			return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
		}
	}
	
	public static ArrayList<String> getElementsStartingWith(String startWith, boolean ignoreCase, ArrayList<String> elements) {
		return new ArrayList<String>(elements.stream().filter(x -> ignoreCase ? x.toLowerCase().startsWith(startWith.toLowerCase()) : x.startsWith(startWith)).collect(Collectors.toList()));
	}
	
	public static ArrayList<String> getElementsStartingWith(String startWith, boolean ignoreCase, String... elements) {
		return getElementsStartingWith(startWith, ignoreCase, toArrayList(elements));
	}
	
	public static ArrayList<String> getElementsThatContains(String contains, boolean ignoreCase, ArrayList<String> elements) {
		return new ArrayList<String>(elements.stream().filter(x -> ignoreCase ? x.toLowerCase().contains(contains.toLowerCase()) : x.contains(contains)).collect(Collectors.toList()));
	}
	
	public static ArrayList<String> getElementsThatContains(String contains, boolean ignoreCase, String... elements) {
		return getElementsThatContains(contains, ignoreCase, toArrayList(elements));
	}
	
	public static <T> ArrayList<T> toArrayList(T[] obj) {
		ArrayList<T> lista = new ArrayList<T>();
		for(int i = 0; i < obj.length; i++) {
			lista.add(obj[i]);
		}
		return lista;
	}
	
	public static boolean isArmor(ItemStack item) {
		return item.getType().toString().contains("_HELMET") || item.getType().toString().contains("_CHESTPLATE") || item.getType().toString().contains("_LEGGINGS") || item.getType().toString().contains("_BOOTS");
	}
	
}