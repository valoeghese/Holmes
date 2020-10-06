package valoeghese.holmes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Card {
	public Card(String name, int points, String... allowsNext) {
		BY_NAME.put(name, this);
		this.name = name;
		this.points = points;
		this.allowsNext = allowsNext;
	}

	private List<Category> categories;
	public final String name;
	public final int points;
	public final String[] allowsNext;

	private Card categories(Category... categories) {
		this.categories = Arrays.asList(categories);
		return this;
	}

	public boolean hasCategory(Category category) {
		return this.categories != null && this.categories.contains(category);
	}

	public boolean canPlayNormally(Card location, String nextCard) {
		Card nextCardCard = BY_NAME.get(nextCard);

		if (nextCardCard.hasCategory(Category.DETECTIVE)) {
			return !this.hasCategory(Category.MOVEMENT) && (!nextCard.equals("Mycroft") || location != THE_COUNTRY);
		}

		if (this.hasCategory(Category.MOVEMENT)) {
			if (this == Card.HANSOM) {
				// I know that nxor is probably better but this is more readable
				return (nextCardCard == Card.THE_COUNTRY) == (location == Card.THE_COUNTRY);
			} else {
				return (nextCardCard == Card.THE_COUNTRY) != (location == Card.THE_COUNTRY);
			}
		}

		if (this.allowsNext == null) {
			return false;
		}

		for (String next : this.allowsNext) {
			if (next.equals(nextCard)) {
				return true;
			}
		}

		return false;
	}

	public static final Map<String, Card> BY_NAME = new HashMap<>();

	// Some usages are hardcoded, as Card is primarily merely a data type
	public static final Card FOOT = new Card("The Game Is Afoot", 0, "Train", "Hansom");
	public static final Card HOLMES = new Card("Holmes", 50).categories(Category.REQUIRES_TARGET, Category.DETECTIVE);
	public static final Card MYCROFT = new Card("Mycroft", 40).categories(Category.REQUIRES_TARGET, Category.DETECTIVE);
	public static final Card WATSON = new Card("Watson", 30).categories(Category.REQUIRES_TARGET, Category.DETECTIVE, Category.CAN_ALIBI);
	public static final Card VILLAIN_20 = new Card("(Villain) Charles Augustus Milverton", 20).categories(Category.VILLAIN);
	public static final Card VILLAIN_30 = new Card("(Villain) John Clay", 30).categories(Category.VILLAIN);
	public static final Card VILLAIN_40 = new Card("(Villain) Colonel Moran", 40).categories(Category.VILLAIN);
	public static final Card VILLAIN_50 = new Card("(Villain) Professor Moriarty", 50).categories(Category.VILLAIN);
	public static final Card TRAIN = new Card("Train", 1).categories(Category.MOVEMENT);
	public static final Card HANSOM = new Card("Hansom", 1).categories(Category.MOVEMENT);
	public static final Card THICK_FOG = new Card("Thick Fog", 5, "London", "Baker Street", "Scotland Yard", "The Country").categories(Category.MOVEMENT);
	public static final Card LONDON = new Card("London", 1, "Arrest", "Clue", "Disguise", "Train", "Hansom").categories(Category.LOCATION);
	public static final Card BAKER_STREET = new Card("Baker Street", 2, "Disguise", "Train", "Hansom", "I Suspect", "Telegram").categories(Category.LOCATION);
	public static final Card SCOTLAND_YARD = new Card("Scotland Yard", 5, "Arrest", "Inspector", "Train", "Hansom", "Disguise").categories(Category.LOCATION);
	public static final Card THE_COUNTRY = new Card("The Country", 1, "Arrest", "Clue", "Train", "Hansom", "Telegram").categories(Category.LOCATION);
	public static final Card TELEGRAM = new Card("Telegram", 10, "Train", "Hansom");
	public static final Card CLUE = new Card("Clue", 5, "Telegram", "Inspector", "Disguise", "I Suspect");
	public static final Card I_SUSPECT = new Card("I Suspect", 5, "Arrest", "Inspector", "Train", "Hansom").categories(Category.REQUIRES_TARGET, Category.CAN_ALIBI);
	public static final Card DISGUISE = new Card("Disguise", 10, "Clue", "I Suspect").categories(Category.REQUIRES_TARGET);
	public static final Card ALIBI = new Card("Alibi", 15, "Train", "Hansom");
	public static final Card INSPECTOR = new Card("Inspector", 15, "Arrest", "Train", "Hansom").categories(Category.REQUIRES_TARGET, Category.CAN_ALIBI);
	public static final Card ARREST = new Card("Arrest", 15, "Train", "Hansom").categories(Category.REQUIRES_TARGET, Category.CAN_ALIBI);
	public static final Card NONE = new Card("None", 0, "The Game Is Afoot");
}
