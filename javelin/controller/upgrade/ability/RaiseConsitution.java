package javelin.controller.upgrade.ability;

import java.beans.PropertyVetoException;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseConsitution extends RaiseAbility {

	public RaiseConsitution() {
		super("constitution");
	}

	@Override
			int getabilityvalue(Monster m) {
		return m.constitution;
	}

	@Override
			boolean setattribute(Combatant c, int l)
					throws PropertyVetoException {
		c.source.raiseconstitution(c, 1);
		return true;
	}

}
