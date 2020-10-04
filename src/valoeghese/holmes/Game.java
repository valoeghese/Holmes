package valoeghese.holmes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class Game {
	public Game(int capacity, int id) {
		this.capacity = capacity;
		this.terminationTime = System.currentTimeMillis() + 1000 * 60 * 10; 
		Main.GAMES.put(id, this);
		this.id = id;
	}

	public final int capacity;
	public final long id;
	public final List<User> users = new ArrayList<>();
	public final Map<User, List<Card>> hands = new HashMap<>();
	public final Queue<Card> deck = createDeck();

	public long terminationTime;
	public boolean started = false;

	private void updateTime() {
		this.terminationTime = System.currentTimeMillis() + 1000 * 60 * 5; // 5 minute due time
	}

	public void addPlayer(User user) {
		if (!this.started) {
			if (this.users.size() < this.capacity) {
				this.updateTime();
				this.users.add(user);
				Main.USER_2_GAME.put(user, this);

				if (this.users.size() == this.capacity) {
					this.broadcast("The Game is AFOOT! Initialising Game!");
				}
			}
		}
	}

	public void removePlayer(User user) {
		if (this.users.contains(user)) {
			this.users.remove(user);
			Main.USER_2_GAME.remove(user, this);

			if (this.users.isEmpty()) {
				this.terminate();
			}
		}
	}

	public boolean terminateIfOverdue() {
		if (System.currentTimeMillis() > this.terminationTime) {
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

	private MessageAction message(User user, String message) {
		return user.openPrivateChannel().complete().sendMessage(message);
	}

	private void broadcast(String message) {
		for (User user : this.users) {
			try {
				message(user, message).queue();
			} catch (Exception e) {
				System.err.println("Error sending broadcast PM");
				e.printStackTrace();
			}
		}
	}

	private static Queue<Card> createDeck() {
		LinkedList<Card> result = new LinkedList<>();
		
		for (int i = 0; i < 6; ++i) {
			
		}
	}
}
