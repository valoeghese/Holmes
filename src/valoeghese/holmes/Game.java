package valoeghese.holmes;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.User;

public class Game {
	public Game(int capacity, int id) {
		this.capacity = capacity;
		this.timeTarget = System.currentTimeMillis() + 1000 * 60 * 10; 
		Main.GAMES.put(id, this);
		this.id = id;
	}

	public final int capacity;
	public long timeTarget;
	public final long id;
	public final List<User> users = new ArrayList<>();	

	private void updateTime() {
		this.timeTarget = System.currentTimeMillis() + 1000 * 60 * 5; // 5 minute due time
	}

	public void addPlayer(User user) {
		if (this.users.size() < this.capacity) {
			this.updateTime();
			this.users.add(user);
			Main.USER_2_GAME.put(user, this);
		}
	}

	public void removePlayer(User user) {
		this.users.remove(user);
		Main.USER_2_GAME.remove(user, this);

		if (this.users.isEmpty()) {
			this.terminate();
		}
	}

	public boolean terminateIfOverdue() {
		if (System.currentTimeMillis() > this.timeTarget) {
			this.terminate();
			return true;
		}

		return false;
	}

	public void terminate() {
		Main.GAMES.remove(this.id);

		for (User user : this.users) {
			Main.USER_2_GAME.remove(user);
		}
	}
}
