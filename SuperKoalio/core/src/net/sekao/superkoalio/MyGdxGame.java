package net.sekao.superkoalio;

import com.badlogic.gdx.Game;
import net.sekao.superkoalio.MainScreen;

public class MyGdxGame extends Game {
	public void create() {
		this.setScreen(new MainScreen());
	}
}
