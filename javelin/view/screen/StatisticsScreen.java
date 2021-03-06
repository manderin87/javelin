package javelin.view.screen;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.quality.Quality;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.town.PurchaseScreen;

/**
 * Shows ally or enemy info.
 * 
 * @author alex
 */
public class StatisticsScreen extends InfoScreen {
	public StatisticsScreen(Combatant c) {
		super("");
		Monster m = c.source;
		ArrayList<String> lines = new ArrayList<String>();
		String monstername = m.name;
		if (!m.group.isEmpty()) {
			monstername += " (" + m.group + ")";
		}
		lines.add(monstername);
		lines.add(capitalize(Monster.SIZES[m.size]) + " " + m.type);
		lines.add("");
		if (c.mercenary) {
			lines.add("Mercenary ($"
					+ PurchaseScreen.formatcost(MercenariesGuild.getfee(c))
					+ "/day)");
		}
		lines.add("Challenge rating "
				+ Math.round(ChallengeRatingCalculator.calculateCr(m)));
		for (ClassAdvancement classlevels : ClassAdvancement.CLASSES) {
			int level = classlevels.getlevel(m);
			if (level > 0) {
				lines.add(classlevels.descriptivename + " level " + level);
			}
		}
		lines.add(describealignment(m));
		lines.add("");
		final String maxhp =
				Squad.active.members.contains(c) ? " (" + c.maxhp + "hp)" : "";
		lines.add("Hit dice     " + m.hd + maxhp);
		lines.add("Initiative   " + (m.initiative >= 0 ? "+" : "")
				+ m.initiative);
		lines.add("Speed        " + showspeed(m));
		lines.add("Armor class  " + alignnumber(m.ac + c.acmodifier));
		lines.add("");
		lines.add("Mêlée attacks");
		listattacks(lines, m.melee);
		lines.add("");
		lines.add("Ranged attacks");
		listattacks(lines, m.ranged);
		lines.add("");
		String qualities = describequalities(m, c);
		if (!qualities.isEmpty()) {
			lines.add(qualities);
		}
		lines.add("Saving throwns");
		lines.add(" Fortitude   " + save(m.fort));
		lines.add(" Reflex      " + save(m.ref));
		lines.add(" Will        " + save(m.will));
		lines.add("");
		lines.add(printability(m.strength, "Strength"));
		lines.add(printability(m.dexterity, "Dexterity"));
		lines.add(printability(m.constitution, "Constitution"));
		lines.add(printability(m.intelligence, "Intelligence"));
		lines.add(printability(m.wisdom, "Wisdom"));
		lines.add(printability(m.charisma, "Charisma"));
		lines.add("");
		if (!m.feats.isEmpty()) {
			String feats = "Feats: ";
			for (javelin.model.feat.Feat f : m.feats) {
				feats += f.name + ", ";
			}
			lines.add(feats.substring(0, feats.length() - 2));
			lines.add("");
		}
		final String skills = showskills(m);
		if (skills != null) {
			lines.add(skills);
		}
		lines.add(
				"Press v to see the monster description, any other key to exit");
		for (String line : lines) {
			text += line + "\n";
		}
		if (updatescreens().equals('v')) {
			text = "(The text below is taken from the d20 SRD and doesn't necessarily reflect the in-game enemy)\n\n"
					+ Javelin.DESCRIPTIONS.get(m.name);
			updatescreens();
		}
		Javelin.app.switchScreen(BattleScreen.active);
	}

	String showspeed(Monster m) {
		long speed = m.fly;
		boolean fly = true;
		if (speed == 0) {
			fly = false;
			speed = m.walk;
		}
		String speedtext = alignnumber(speed) + " feet";
		if (fly) {
			speedtext += " flying";
		}
		speedtext += " (" + speed / 5 + " squares)";
		if (m.swim > 0) {
			speedtext += ", swim " + m.swim + " feet";
		}
		if (m.burrow > 0) {
			speedtext += ", burrow " + m.burrow + " feet";
		}
		return speedtext;
	}

	String formatskill(String name, int ranks, int ability) {
		if (ranks == 0) {
			return "";
		}
		ranks += Monster.getbonus(ability);
		String bonus;
		if (ranks > 0) {
			bonus = "+" + ranks;
		} else {
			bonus = "-" + -ranks;
		}
		return name + " " + bonus + ", ";
	}

	String showskills(Monster m) {
		Skills s = m.skills;
		String output = "";
		output += formatskill("acrobatics", s.acrobatics, m.dexterity);
		output += formatskill("concentration", s.concentration, m.constitution);
		output += formatskill("diplomacy", s.diplomacy, m.charisma);
		output +=
				formatskill("disable device", s.disabledevice, m.intelligence);
		output += formatskill("gather information", s.gatherinformation,
				m.charisma);
		output += formatskill("heal", s.heal, m.wisdom);
		output += formatskill("knowledge", s.knowledge, m.intelligence);
		output += formatskill("perception", s.perception, m.wisdom);
		output += formatskill("search", s.search, m.intelligence);
		output += formatskill("spellcraft", s.spellcraft, m.intelligence);
		output += formatskill("stealth", s.stealth, m.dexterity);
		output += formatskill("survival", s.survival, m.wisdom);
		output += formatskill("use magic device", s.usemagicdevice, m.charisma);
		return output.isEmpty() ? null
				: "Skills: " + output.substring(0, output.length() - 2) + "\n";
	}

	String describealignment(Monster m) {
		String alignment;
		if (m.lawful == null) {
			if (m.good == null) {
				return "True neutral";
			}
			alignment = "Neutral";
		} else if (m.lawful) {
			alignment = "Lawful";
		} else {
			alignment = "Chaotic";
		}
		if (m.good == null) {
			return alignment += " neutral";
		} else if (m.good) {
			return alignment + " good";
		} else {
			return alignment + " evil";
		}
	}

	private String save(int x) {
		String sign = "";
		if (x >= 0) {
			sign = "+";
		}
		return sign + x;
	}

	private String printability(int score, String abilityname) {
		abilityname += " ";
		while (abilityname.length() < 13) {
			abilityname += " ";
		}
		return abilityname + alignnumber(score) + " ("
				+ Monster.getsignedbonus(score) + ")";
	}

	private String describequalities(Monster m, Combatant c) {
		ArrayList spells = new ArrayList(c.spells.size());
		for (javelin.controller.upgrade.Spell s : c.spells) {
			spells.add(s.toString());
		}
		String string = printqualities("Spells", spells);
		ArrayList<String> attacks = new ArrayList<String>();
		for (BreathWeapon breath : m.breaths) {
			attacks.add(breath.toString());
		}
		if (m.touch != null) {
			attacks.add(m.touch.toString());
		}
		string += printqualities("Special attacks", attacks);
		ArrayList<String> qualities = new ArrayList<String>();
		for (Quality q : Quality.qualities) {
			if (q.has(m)) {
				String description = q.describe(m);
				if (description != null) {
					qualities.add(description);
				}
			}
		}
		return string + printqualities("Special qualities", qualities);
	}

	String printqualities(String header, ArrayList<?> qualities) {
		if (qualities.isEmpty()) {
			return "";
		}
		header += ": ";
		qualities.sort(null);
		for (Object quality : qualities) {
			header += quality.toString().toLowerCase() + ", ";
		}
		return header.substring(0, header.length() - 2) + "\n";
	}

	public void listattacks(ArrayList<String> lines,
			List<AttackSequence> melee) {
		if (melee.isEmpty()) {
			lines.add(" None");
			return;
		}
		for (AttackSequence sequence : melee) {
			lines.add(" " + sequence.toString());
		}
	}

	public static String capitalize(String size) {
		return Character.toUpperCase(size.charAt(0))
				+ size.substring(1).toLowerCase();
	}

	private String alignnumber(long score) {
		return score < 10 ? " " + score : Long.toString(score);
	}

	public Character updatescreens() {
		Javelin.app.switchScreen(this);
		return InfoScreen.feedback();
	}
}
