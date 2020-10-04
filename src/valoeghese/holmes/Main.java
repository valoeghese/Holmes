package valoeghese.holmes;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
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
						int playerCount = Integer.parseInt(params[1]);
						USER_2_GAME.put(author, value);
						event.getChannel().sendMessage(author.getAsMention() + " started a game! React with :sunglasses: to join. [" + );
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
	public void onGenericGuildMessageReaction(GenericGuildMessageReactionEvent event) {
		// TODO Auto-generated method stub
		super.onGenericGuildMessageReaction(event);
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		// TODO Auto-generated method stub
		super.onPrivateMessageReceived(event);
	}

	public static final Map<User, Game> USER_2_GAME = new HashMap<>();
	public static int n = 0;

	public static void main(String[] args) {
		try (FileInputStream fis = new FileInputStream(new File("properties.txt"))) {
			Properties p = new Properties();
			p.load(fis);
			new JDABuilder(p.getProperty("key")).addEventListeners(new Main()).build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception running bot!", e);
		}
	}
}
