/**
 * Alex Henry on 13/10/2010
 */
package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseConsitution;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.model.unit.Monster;

/**
 * TODO leaving charisma and intelligence for now because they are useless with
 * the current game content. Probably both will be needed once skills are
 * implemented.
 */
public class AbilitiesFactor extends CrFactor {
	@Override
	public float calculate(final Monster monster) {
		final int[] abilites = new int[] { monster.strength, monster.dexterity,
				monster.constitution, monster.wisdom, monster.intelligence };
		float sum = 0;
		for (final int a : abilites) {
			if (a != 0) {
				sum += a - 10.5;
			}
		}
		sum = sum / 10;
		if (monster.intelligence == 0) {
			/**
			 * Immune to mind-affecting effects. Should be .5 but right now is
			 * only making automatic will saves.
			 */
			sum += .5;
		}
		return sum;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.defensive.add(new RaiseConsitution());
		handler.misc.add(new RaiseStrength());
		handler.misc.add(new RaiseDexterity());
		handler.misc.add(new RaiseWisdom());
	}
}