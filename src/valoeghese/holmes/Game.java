package valoeghese.holmes;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.User;

public class Game {
	public Game(int capacity) {
		this.capacity = capacity;
		this.time = System.currentTimeMillis();
	}

	public final int capacity;
	public final long time;
	public final List<User> users = new ArrayList<>();	
}
