/**
 * Discord bot based off of an existing Sherlock Holmes card game. Bot code for private hosting and use.
 * I recommend buying the actual game it's quite fun.
 */

package valoeghese.holmes;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.util.*;

public class Main extends ListenerAdapter {
	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (event.isFromGuild()) {
			this.onGuildMessageReceived(event);
		} else {
			this.onPrivateMessageReceived(event);
		}
	}

	private void onGuildMessageReceived(MessageReceivedEvent event) {
		Message message = event.getMessage();
		User author = message.getAuthor();

		if (!author.isBot()) {
			if (message.getContentRaw().equals("s.help")) {
				event.getChannel().sendMessage("Welcome to \"Holmes\", a card game about deception and trickery. This game is an unoffcial online port of a physical Sherlock Holmes card game, and I recommend getting the real game if you enjoy this! (pls don't sue me)\n"
						+ "It is recommended that all players get into a voice call with each other when playing.\n"
						+ "__**Rules**__\n"
						+ "- Each player starts with 6 cards.\n"
						+ "- One person will start with the first villain card, making them the starting villain. Three more villain cards exist in the deck, and thus multiple competing villains can arise.\n"
						+ "- One person will start with the \"Game if Afoot\" card, which they play as the starting player.\n"
						+ "- Each card has a list of cards in the \"next cards\" section about what can be played next.\n"
						+ "- Some cards have special conditions for playing, and will not be listed in the \"next cards\" section (detailed when starting a new game with `s.start [player count]`.)\n\n"
						+ "__**Villain**__\n"
						+ "- As the villain, you are trying to either __escape__ (by only having villain cards in your hand, allowing you to play one to escape with all of them), __arrest another villain__, or __use Thick Fog__ to reshuffle the player's hands or __Mycroft__ to exchange hands, providing a chance to switch sides.\n"
						+ "- You must do this while trying to escape being arrested yourself.").queue();
				event.getChannel().sendMessage("__**Non Villain**__\n"
						+ "- As a non villain, you are trying to find clues as to who may be the villain. This can be done through interactions with them (such as in your group vc), by looking at their actions, or through the help of the cards \"clue\" and \"disguise\"\n"
						+ "- Once you are confident you know who the villain is, you can make an arrest. This can be done either by having no cards in your hand, or playing an Arrest, Watson, or Holmes card. Additionally, **Inspector** counts as an Arrest if you are at Scotland Yard. Unless you play Watson or Holmes, there is a penalty of adding the target player's hand to yours and the target player drawing a new hand for a false arrest. All of these actions but the Holmes card can be countered by an Alibi.\n\n"
						+ "__**Scoring**__\n"
						+ "- At the end of the game, all players add up the point values of cards in their hand. A player may already have some points from the use of the **Telegram** card earlier in the game. Unarrested villains escape and are not counted. If a player successfully made an arrest, the points value of the arrested villain(s) is deducted from the arrestee's points. The player with the **least points** wins.").queue();
			} else if (message.getContentRaw().startsWith("s.start")) {
				try {
					String[] params = message.getContentRaw().split(" ");

					if (params.length > 1) {
						int playerCount = Math.min(24, Math.max(2, Integer.parseInt(params[1])));
						Game game = new Game(playerCount, nextGameId++);
						game.addPlayer(author);
						event.getChannel().sendMessage(author.getAsMention() + " started a game of " + playerCount + " players! React with :sunglasses: to join. [id " + game.id + "]").queue();
					} else {
						event.getChannel().sendMessage("Correct Format: 's.start [playercount]'").queue();
					}
				} catch (Exception e) {
					event.getChannel().sendMessage("Error: " + e.getLocalizedMessage()).queue();
				}
			} else {
				long sctm = System.currentTimeMillis();

				if (sctm > nextCleanupTime) {
					nextCleanupTime = sctm + 1000 * 40; // 40 second delays

					for (Game game : GAMES.values()) {
						game.terminateIfOverdue();
					}
				}
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (!event.isFromGuild()) return;

		try {
			if (event.getEmoji().toString().equals("RE:U+1f60e")) {
				Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();

				if (!message.getMentions().getMembers().isEmpty() && message.getAuthor().equals(event.getJDA().getSelfUser())) {
					User sender = event.getUser();
					String[] arr = message.getContentRaw().split(" ");
					String arr0 = arr[arr.length - 1];

					Game game = GAMES.get(Long.parseLong(arr0.substring(0, arr0.length() - 1)));

					if (game == null || game.terminateIfOverdue()) {
						return;
					}

					if (USER_2_GAME.containsKey(sender)) {
						Game other = USER_2_GAME.get(sender);

						if (!other.terminateIfOverdue()) {
							if (game == other) {
								return;
							}

							other.removePlayer(sender);
						}
					}

					game.addPlayer(sender);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		try {
			if (event.getEmoji().toString().equals("RE:U+1f60e")) {
				Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();

				if (!message.getMentions().getMembers().isEmpty() && message.getAuthor().equals(event.getJDA().getSelfUser())) {
					User sender = event.getUser();
					String[] arr = message.getContentRaw().split(" ");
					String arr0 = arr[arr.length - 1];

					Game game = GAMES.get(Long.parseLong(arr0.substring(0, arr0.length() - 1)));

					if (game == null || game.terminateIfOverdue()) {
						return;
					}

					game.removePlayer(sender);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onPrivateMessageReceived(MessageReceivedEvent event) {
		User author = event.getAuthor();

		if (!author.isBot()) {
			if (USER_2_GAME.containsKey(author)) {
				USER_2_GAME.get(author).process(author, event.getMessage().getContentRaw());
			} else {
				event.getChannel().sendMessage("You are not in a game, or your game session expired!").queue();
			}
		}
	}

	public static void main(String[] args) {
		try (FileInputStream fis = new FileInputStream("./properties.txt")) {
			Properties p = new Properties();
			p.load(fis);
			JDABuilder.createDefault(p.getProperty("key")).addEventListeners(new Main()).enableIntents(List.of(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES)).build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception running bot!", e);
		}
	}

	public static StringBuilder appendArray(StringBuilder stringBuilder, String[] allowsNext) {
		for (int i = 0; i < allowsNext.length; ++i) {
			if (i > 0) {
				stringBuilder.append(", ");
			}

			stringBuilder.append(allowsNext[i]);
		}

		return stringBuilder;
	}

	public static final Map<User, Game> USER_2_GAME = new HashMap<>();
	public static final Long2ObjectMap<Game> GAMES = new Long2ObjectArrayMap<>();

	private static int nextGameId = new Random().nextInt(10000) - 5000;
	private static long nextCleanupTime = System.currentTimeMillis() + 1000 * 40;
}
