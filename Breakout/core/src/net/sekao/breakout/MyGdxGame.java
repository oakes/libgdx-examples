package net.sekao.breakout;

import com.badlogic.gdx.Game;
import net.sekao.breakout.MainScreen;

public class MyGdxGame extends Game {
	public void create() {
		this.setScreen(new MainScreen());
	}
}
