package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.old.Game;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.town.PurchaseScreen;
import tyrant.mikera.engine.RPG;

/**
 * The **Mercenaries guild** allows a player to hire mercenaries, which are paid
 * a certain amount in gold per day.
 * 
 * @see Combatant#mercenary
 * @author alex
 */
public class MercenariesGuild extends UniqueLocation {
	private static final int STARTINGMERCENARIES = 9;
	static boolean DEBUG = false;
	public ArrayList<Combatant> mercenaries = new ArrayList<Combatant>();
	public ArrayList<Combatant> all = new ArrayList<Combatant>();

	/** Constructor. */
	public MercenariesGuild() {
		super("Mercenaries' Guild", "Mercenaries' Guild", 11, 15);
		gossip = true;
		while (mercenaries.size() < STARTINGMERCENARIES) {
			generatemercenary();
		}
		if (DEBUG) {
			garrison.clear();
		}
	}

	void generatemercenary() {
		int cr = RPG.r(11, 20);
		Combatant c = null;
		Realm r = Realm.random();
		while (c == null) {
			List<Monster> tier = Javelin.MONSTERSBYCR.get((float) RPG.r(1, cr));
			if (tier != null) {
				Monster m = RPG.pick(tier);
				if (!m.humanoid) {
					return;
				}
				c = new Combatant(null, m.clone(), true);
				c.mercenary = true;
				r.baptize(c);
				for (Combatant c2 : mercenaries) {
					if (c.toString().equals(c2.toString())) {
						return;
					}
				}
			}
		}
		int tries = 0;
		while (c.source.challengeRating < cr) {
			c.upgrade(r);
			tries += 1;
			if (tries >= 100) {
				return;
			}
		}
		mercenaries.add(c);
		all.add(c);
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		Collections.sort(mercenaries, new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				return new Float(
						ChallengeRatingCalculator.calculateCr(o2.source))
								.compareTo(ChallengeRatingCalculator
										.calculateCr(o1.source));
			}
		});
		ArrayList<String> prices = new ArrayList<String>(mercenaries.size());
		for (Combatant c : mercenaries) {
			prices.add(c + " ($" + PurchaseScreen.formatcost(getfee(c)) + ")");
		}
		int index = Javelin.choose(
				"\"Welcome to the guild! Do you want to hire one of our mercenaries for a modest daily fee?\"\n\nYou have $"
						+ PurchaseScreen.formatcost(Squad.active.gold),
				prices, true, false);
		if (index == -1) {
			return true;
		}
		if (!recruit(mercenaries.get(index), true)) {
			return false;
		}
		mercenaries.remove(index);
		return true;
	}

	/**
	 * Pays for the rest of the day and adds to active {@link Squad}. If cannot
	 * pay warn the user.
	 * 
	 * @param message
	 *            If <code>true</code> and doesn't have enough money will open
	 *            up a {@link InfoScreen} to let the player know. Use
	 *            <code>false</code> to warn in another manner.
	 * @return <code>false</code> if doesn't have enough money to pay in
	 *         advance.
	 */
	static public boolean recruit(Combatant combatant, boolean message) {
		long advance = Math.max(1, Javelin.getHour() * getfee(combatant) / 24);
		if (Squad.active.gold < advance) {
			if (message) {
				Javelin.app.switchScreen(new InfoScreen(
						"You don't have the money to pay today's advancement ($"
								+ PurchaseScreen.formatcost(advance) + ")!"));
				Game.getInput();
			}
			return false;
		}
		combatant.mercenary = true;
		Squad.active.gold -= advance;
		Squad.active.members.add(combatant);
		return true;
	}

	/**
	 * @return Daily fee for a mercenary, based on it's CR (single treasure
	 *         value).
	 */
	public static int getfee(Combatant c) {
		float value = RewardCalculator
				.getgold(ChallengeRatingCalculator.calculateCr(c.source));
		int roundto;
		if (value > 1000) {
			roundto = 1000;
		} else if (value > 100) {
			roundto = 100;
		} else if (value > 10) {
			roundto = 10;
		} else {
			roundto = 1;
		}
		int fee = Math.round(value / roundto);
		return fee * roundto;
	}

	public void receive(Combatant c) {
		if (all.contains(c)) {
			mercenaries.add(c);
		}
	}

	@Override
	public List<Combatant> getcombatants() {
		ArrayList<Combatant> combatants = new ArrayList<Combatant>(garrison);
		combatants.addAll(all);
		return combatants;
	}
}
