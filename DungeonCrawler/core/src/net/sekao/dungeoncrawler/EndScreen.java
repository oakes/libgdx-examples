package net.sekao.dungeoncrawler;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.*;
import java.util.*;

public class EndScreen extends ScreenAdapter {
	Stage stage;
	Label label;
	
	public void show() {
		stage = new Stage();
		Label.LabelStyle style = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
		label = new Label("You have won!", style);
		label.setWrap(true);
		label.setAlignment(Align.center);
		label.setFontScale(3);
		stage.addActor(label);
	}

	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
	}

	public void resize(int width, int height) {
		label.setSize(width, height);
	}
}
