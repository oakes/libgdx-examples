package net.sekao.dungeoncrawler;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;

public class MyGdxGame extends Game {
	public void create() {
		this.setScreen(new MainScreen(this));
	}
}
