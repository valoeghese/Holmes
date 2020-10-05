package valoeghese.holmes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	public Queue<Card> deck;
	private int turn = 0;

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
					this.startGame();
				}
			}
		}
	}

	private void startGame() {
		this.started = true;
		this.broadcast("The Game is AFOOT! Initialising Game!");
		int toAdd = this.capacity * 6 - 2;

		LinkedList<Card> deck = createDeck();
		LinkedList<Card> starting = new LinkedList<>();
		List<Card> villains = new ArrayList<>(Arrays.asList(Card.VILLAIN_20, Card.VILLAIN_30, Card.VILLAIN_40, Card.VILLAIN_50));
		Collections.shuffle(villains);

		starting.add(villains.remove(0));
		starting.add(Card.FOOT);

		for (int i = 0; i < toAdd; ++i) {
			starting.add(deck.remove());
		}

		Collections.shuffle(starting);
		deck.addAll(villains);
		Collections.shuffle(deck);

		this.deck = deck;

		User startingUser = null;

		for (User user : this.users) {
			this.drawCards(user, starting, 6);

			if (this.hands.get(user).contains(Card.FOOT)) {
				startingUser = user;
			}
		}

		this.turn = this.users.indexOf(startingUser);

		StringBuilder msg = new StringBuilder("**Turn Order**: ");

		for (int i = 0; i < this.capacity; ++i) {
			msg.append(this.users.get(i).getName());

			if (i != this.capacity - 1) {
				msg.append(", ");
			}

			this.nextTurn();
		}

		this.broadcast(msg.toString());
	}

	private void nextTurn() {
		if (++this.turn >= this.capacity) {
			this.turn = 0;
		}
	}

	private void drawCards(User user, Queue<Card> deck, int count) {
		List<Card> cards = this.hands.computeIfAbsent(user, u -> new ArrayList<>());

		for (int i = 0; i < count; ++i) {
			cards.add(deck.remove());
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

	private static LinkedList<Card> createDeck() {
		LinkedList<Card> result = new LinkedList<>();

		result.add(Card.HOLMES);
		result.add(Card.WATSON);
		result.add(Card.MYCROFT);

		for (int i = 0; i < 3; ++i) {
			result.add(Card.THICK_FOG);
			result.add(Card.SCOTLAND_YARD);
			result.add(Card.BAKER_STREET);
		}

		for (int i = 0; i < 4; ++i) {
			result.add(Card.TELEGRAM);
			result.add(Card.DISGUISE);
		}

		for (int i = 0; i < 5; ++i) {
			result.add(Card.INSPECTOR);
		}

		for (int i = 0; i < 6; ++i) {
			result.add(Card.LONDON);
			result.add(Card.ARREST);
			result.add(Card.ALIBI);
		}

		for (int i = 0; i < 12; ++i) {
			result.add(Card.TRAIN);
			result.add(Card.HANSOM);
			result.add(Card.THE_COUNTRY);
			result.add(Card.I_SUSPECT);
			result.add(Card.CLUE);
		}

		Collections.shuffle(result);
		return result;
	}
}
