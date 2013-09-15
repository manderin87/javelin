package javelin.model.condition;

import javelin.model.unit.Combatant;

public class Paralyzed extends Condition {

	private int dex;
	private int delta;

	public Paralyzed(float expireatp, Combatant c) {
		super(expireatp, c);
	}

	@Override
	void start(Combatant c) {
		c.source = c.source.clone();
		dex = c.source.dexterity;
		c.source.dexterity -= dex;
		delta = (int) Math.round(Math.floor(dex / 2f));
		c.source.raisedexterity(-delta);
		c.ap = expireat;
	}

	@Override
	void end(Combatant c) {
		c.source = c.source.clone();
		c.source.dexterity += dex;
		c.source.raisedexterity(+delta);
	}

	@Override
	public String describe() {
		return "paralyzed";
	}

}
