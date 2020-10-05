package valoeghese.holmes;

import java.util.HashMap;
import java.util.Map;

public class Card {
	public Card(String name, int points, String... allowsNext) {
		BY_NAME.put(name, this);
		this.name = name;
		this.points = points;
		this.allowsNext = allowsNext;
	}

	public final String name;
	public final int points;
	public final String[] allowsNext;

	public static final Map<String, Card> BY_NAME = new HashMap<>();

	// No varargs implies that the 'next cards' for these are hard-coded
	// Villain cards', Detective cards', and Alibi's usages are hard-coded
	public static final Card FOOT = new Card("The Game Is Afoot", 0, "Train", "Hansom");
	public static final Card HOLMES = new Card("Holmes", 50);
	public static final Card MYCROFT = new Card("Mycroft", 40);
	public static final Card WATSON = new Card("Watson", 30);
	public static final Card VILLAIN_20 = new Card("Charles Augustus Milverton", 20);
	public static final Card VILLAIN_30 = new Card("John Clay", 30);
	public static final Card VILLAIN_40 = new Card("Colonel Moran", 40);
	public static final Card VILLAIN_50 = new Card("Professor Moriarty", 50);
	public static final Card TRAIN = new Card("Train", 1);
	public static final Card HANSOM = new Card("Hansom", 1);
	public static final Card THICK_FOG = new Card("Thick Fog", 5, "London", "Baker Street", "Scotland Yard", "The Country");
	public static final Card LONDON = new Card("London", 1, "Arrest", "Clue", "Disguise", "Train", "Hansom");
	public static final Card BAKER_STREET = new Card("Baker Street", 2, "Disguise", "Train", "Hansom", "I Suspect", "Telegram");
	public static final Card SCOTLAND_YARD = new Card("Scotland Yard", 5, "Arrest", "Inspector", "Train", "Hansom", "Disguise");
	public static final Card THE_COUNTRY = new Card("The Country", 1, "Arrest", "Clue", "Train", "Hansom", "Telegram");
	public static final Card TELEGRAM = new Card("Telegram", 10, "Train", "Hansom");
	public static final Card CLUE = new Card("Clue", 5, "Telegram", "Inspector", "Disguise", "I Suspect");
	public static final Card I_SUSPECT = new Card("I Suspect", 5, "Arrest", "Inspector", "Train", "Hansom");
	public static final Card DISGUISE = new Card("Disguise", 10, "Clue", "I Suspect");
	public static final Card ALIBI = new Card("Alibi", 15, "Train", "Hansom");
	public static final Card INSPECTOR = new Card("Inspector", 15, "Arrest", "Train", "Hansom");
	public static final Card ARREST = new Card("Arrest", 15, "Train", "Hansom");
}
