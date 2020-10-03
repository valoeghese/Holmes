package valoeghese.holmes;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Main extends ListenerAdapter {
	private final Object2IntMap<User> USER_2_GAME = new Object2IntArrayMap<>();
	private final Int2ObjectMap<List<User>> GAME_2_USER = new Int2ObjectArrayMap<>();

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		User author = message.getAuthor();

		if (!author.isBot()) {
			
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
