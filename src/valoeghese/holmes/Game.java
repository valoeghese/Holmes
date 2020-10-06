package valoeghese.holmes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class Game {
	public Game(int capacity, int id) {
		this.capacity = capacity;
		this.terminationTime = System.currentTimeMillis() + 1000 * 60 * 10; 
		Main.GAMES.put(id, this);
		this.id = id;
	}

	private final int capacity;
	public final long id;
	private final List<User> users = new ArrayList<>();
	private final Map<User, List<Card>> hands = new HashMap<>();
	private final Object2IntMap<User> points = new Object2IntArrayMap<>();
	private Queue<Card> deck;
	private int turn = 0;
	private Card location = Card.BAKER_STREET;
	private Card previous = Card.NONE;

	private long terminationTime;
	private boolean started = false;
	private boolean pause = false; // whether to pause the game for an out-of-turn player response

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

	public void removePlayer(User user) {
		if (this.users.contains(user)) {
			this.users.remove(user);
			Main.USER_2_GAME.remove(user, this);

			if (this.users.isEmpty()) {
				this.terminate();
			}
		}
	}

	public void process(User author, String message) {
		if (this.terminateIfOverdue()) {
			this.message(author, "Your session has expired!").queue();
			return;
		}

		if (this.pause) {
			// TODO
		} else if (this.users.get(this.turn) == author) {
			try {
				List<Card> hand = this.hands.get(author);

				if (hand.isEmpty()) {
					// TODO rules for arrest/draw of non-villains
					this.nextTurn();
					this.announcePlayerTurn();
				} else if (message.equals("draw")) {
					// action
					drawCards(author, this.deck, 1);
					this.broadcastExcept(author, "**" + author.getName() + "** has chosen to draw.");

					// response
					this.message(author, "You drew *" + hand.get(hand.size() - 1).name + "*").queue();

					// finalise
					this.nextTurn();
					this.announcePlayerTurn();
				} else {
					int cardIndex = Integer.parseInt(message);
					Card attempted = hand.get(cardIndex);
					boolean villainEscape = false;

					// if allowed to play
					if (this.previous.canPlayNormally(this.location, attempted.name) || (villainEscape = onlyVillains(hand))) {
						// remove card from hand and play it
						this.previous = hand.remove(cardIndex);

						// announce action to other players
						this.broadcastExcept(author, "**" + author.getName() + "** has chosen to play *" + this.previous.name + "*.");

						// RESOLVE CARD ACTIONS!

						// check if villains win
						if (villainEscape) {
							this.broadcast("**" + author.getName() + "** has *escaped* as the villain!");
							this.endGame();
						}

						int specialEffect = 0;

						// if it's a location card, set it as location
						if (this.previous.hasCategory(Category.LOCATION)) {
							this.location = this.previous;

							if (this.previous == Card.SCOTLAND_YARD) {
								specialEffect = 1;
							}
						}
						// Otherwise check if requires a target
						else if (this.previous.hasCategory(Category.REQUIRES_TARGET)) {

						}
						// Otherwise check the remaining cards
						else if (this.previous == Card.THICK_FOG) {
							Object2IntMap<User> cardsInHand = new Object2IntArrayMap<>();
							LinkedList<Card> allHands = new LinkedList<>();

							// collect counts and cards
							this.hands.forEach((user, cards) -> {
								cardsInHand.put(user, cards.size());
								allHands.addAll(cards);
							});

							// shuffle
							Collections.shuffle(allHands);

							// redeal
							cardsInHand.forEach((user, count) -> {
								List<Card> newHand = new ArrayList<>();
								StringBuilder handList = new StringBuilder("New cards in hand:");

								for (int i = 0; i < count; ++i) {
									Card card = allHands.remove();
									newHand.add(card);
									handList.append("\n- [" + i + "] " + card.name);
								}

								this.hands.put(user, newHand);

								this.message(user, handList.toString()).queue();
							});
						} else if (this.previous == Card.CLUE) {
							User previousPlayer = this.previousPlayer();
							List<Card> cards = new ArrayList<>(this.hands.get(previousPlayer));

							switch (cards.size()) {
							case 0:
								this.message(author, previousPlayer.getName() + " has no cards in their hand!").queue();
								break;
							case 1:
								this.message(author, previousPlayer.getName() + " has only " + cards.get(0).name + " in their hand.").queue();
								break;
							default:
								Collections.shuffle(cards);
								this.message(author, "You reveal the cards " + cards.get(0).name + " and " + cards.get(1).name + " in " + previousPlayer.getName() + "'s hand.").queue();
								break;
							}
						} else if (this.previous == Card.TELEGRAM) {
							specialEffect = 2;
						}

						//						// Check if an alibi check needs to be made
						//						else if (this.previous.hasCategory(Category.CAN_ALIBI)) {
						//							
						//						}
						//						// otherwise resolve specific card actions
						//						else switch (attempted.name) {
						//						case "Disguise":
						//							this.message(author, message).queue();
						//							break;
						//						}

						this.nextTurn();

						if (specialEffect == 1) {
							drawCards(this.users.get(this.turn), this.deck, 2);
						} else if (specialEffect == 2) {
							this.addPoints(this.users.get(this.turn), 10);
						}

						this.announcePlayerTurn();
					} else {
						this.message(author, "Invalid Card.").queue();
					}
				}
			} catch (NumberFormatException e) {
				System.out.println("ignored:");
				e.printStackTrace(System.out);
			} catch (Exception e) {
				this.message(author, "Error processing message: " + e.getLocalizedMessage()).complete();
				e.printStackTrace();
			}
		}
	}

	private void addPoints(User user, int points) {
		this.points.computeInt(user, (usr, current) -> current == null ? points : points + current);
	}

	private void endGame() {
		List<String> winner = null;
		int winnerPoints = Integer.MAX_VALUE;
		StringBuilder resultMsg = new StringBuilder("The game has ended! Points:");

		for (User user : this.users) {
			int points = this.points.getOrDefault(user, 0) + this.hands.get(user).stream().mapToInt(card -> card.points).sum();

			if (points < winnerPoints) {
				winner = new ArrayList<>();
				winner.add(user.getName());
			} else if (points == winnerPoints) {
				winner.add(user.getName()); // should never be null because what kind of idiot ends with integer.maxvalue points
			}

			resultMsg.append("\n**" + user.getName() + "**: " + points + " points");
		}

		resultMsg.append("\n\n");

		if (winner.size() == 1) {
			resultMsg.append("The winner is **" + winner.get(0) + "**");
		} else {
			resultMsg.append("The winners are: **");
			Main.appendArray(resultMsg, winner.toArray(new String[0]));
			resultMsg.append("**");
		}

		this.broadcast(resultMsg.toString());
		this.terminate();
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

	// PRIVATE METHODS

	private MessageAction message(User user, String message) {
		return user.openPrivateChannel().complete().sendMessage(message);
	}

	private void broadcast(String message) {
		this.broadcastExcept(null, message);
	}

	private void broadcastExcept(User exempt, String message) {
		for (User user : this.users) {
			if (user != exempt) {
				try {
					message(user, message).queue();
				} catch (Exception e) {
					System.err.println("Error sending broadcast PM");
					e.printStackTrace();
				}
			}
		}
	}

	private void startGame() {
		this.started = true;
		Collections.shuffle(this.users);
		this.broadcast("The game is afoot! Initialising Game!\nRemember, **Villain**, **Detective**, and **Alibi** cards have special play conditions and are not listed on the card's 'can play next' list:\n- **Villain** cards can be played only when you only have villain cards remaining.\n- **Detective** cards (Holmes, Mycroft, Watson) can be played after any non-location card. Mycroft has the added condition that you must not be in the country.\n- **Alibi** can only be played when you are prompted whether you wish to use an alibi to cancel a defendable card effect.");
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
			msg.append(this.users.get(this.turn).getName());

			if (i != this.capacity - 1) {
				msg.append(", ");
			}

			this.nextTurn();
		}

		this.broadcast(msg.toString());
		this.announcePlayerTurn();
	}

	private void announcePlayerTurn() {
		try {
			this.broadcast(Main.appendArray(new StringBuilder("We are in *")
					.append(this.location.name)
					.append("*.\n**Previous Card**: *")
					.append(this.previous.name)
					.append("*\n**Allowed Next Cards**:"), this.previous.allowsNext).toString());

			User user = this.users.get(this.turn);
			StringBuilder handList = new StringBuilder();
			List<Card> hand = this.hands.get(user);

			int index = 0;

			for (Card card : hand) {
				handList.append("\n- [" + (index++) + "] " + card.name);
			}

			this.message(user, "It is your turn! Cards in your hand: " + handList.toString() + "\nRespond with the [card index] to play a card, or \"draw\" to draw.").queue();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private User previousPlayer() {
		int i = this.turn - 1;
		return this.users.get(i < 0 ? this.capacity - 1 : 1);
	}

	private void nextTurn() {
		if (++this.turn >= this.capacity) {
			this.turn = 0;
		}
	}

	private void drawCards(User user, Queue<Card> deck, int count) {
		if (!deck.isEmpty()) {
			List<Card> cards = this.hands.computeIfAbsent(user, u -> new ArrayList<>());

			for (int i = 0; i < count; ++i) {
				cards.add(deck.remove());
			}
		}
	}

	private static boolean onlyVillains(List<Card> hand) {
		return hand.stream().allMatch(card -> card.hasCategory(Category.VILLAIN));
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
