package valoeghese.holmes;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Main extends ListenerAdapter {
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		User author = message.getAuthor();

		if (!author.isBot()) {
			if (message.getContentRaw().startsWith("s.start")) {
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
			}
		}
	}

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		try {
			if (event.getReactionEmote().toString().equals("RE:U+1f60e")) {
				Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();

				if (!message.getMentionedMembers().isEmpty() && message.getAuthor().equals(event.getJDA().getSelfUser())) {
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
			if (event.getReactionEmote().toString().equals("RE:U+1f60e")) {
				Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();

				if (!message.getMentionedMembers().isEmpty() && message.getAuthor().equals(event.getJDA().getSelfUser())) {
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

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		// TODO Auto-generated method stub
		super.onPrivateMessageReceived(event);
	}

	public static final Map<User, Game> USER_2_GAME = new HashMap<>();
	public static final Long2ObjectMap<Game> GAMES = new Long2ObjectArrayMap<>();

	public static int nextGameId = new Random().nextInt(10000) - 5000;

	public static void main(String[] args) {
		try (FileInputStream fis = new FileInputStream(new File("./properties.txt"))) {
			Properties p = new Properties();
			p.load(fis);
			new JDABuilder(p.getProperty("key")).addEventListeners(new Main()).build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception running bot!", e);
		}
	}
}
