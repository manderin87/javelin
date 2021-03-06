package javelin.controller.quality;

import javelin.controller.upgrade.Spell;
import javelin.model.spell.necromancy.Doom;
import javelin.model.unit.Monster;

/**
 * Currently gives a monster at will casts of {@link Doom} since worst fear
 * stages (like Frightened) would require very complicated game behavior.
 * 
 * TODO would probably be better to find an area Doom spell and use it instead
 * 
 * @author alex
 */
public class FrightfulPresence extends Quality {

	/** Constructor. */
	public FrightfulPresence() {
		super("Frightful presence");
	}

	@Override
	public void add(String declaration, Monster m) {
		Spell doom = new Doom();
		Spell previous = m.spells.has(doom);
		if (previous == null) {
			m.spells.add(doom);
		} else {
			doom = previous;
		}
		doom.perday = 5;
	}

	@Override
	public boolean has(Monster m) {
		return false;
	}

	@Override
	public float rate(Monster m) {
		// rated as spell
		return 0;
	}

}
