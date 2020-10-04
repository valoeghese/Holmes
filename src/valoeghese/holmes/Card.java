package valoeghese.holmes;

import java.util.HashMap;
import java.util.Map;

public class Card {
	public Card(String name, String... allowsNext) {
		BY_NAME.put(name, this);
		this.name = name;
		this.allowsNext = allowsNext;
	}

	public final String name;
	public final String[] allowsNext;

	public static final Map<String, Card> BY_NAME = new HashMap<>();
}
