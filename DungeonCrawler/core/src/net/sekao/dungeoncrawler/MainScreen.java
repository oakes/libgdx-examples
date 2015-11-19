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
import com.badlogic.gdx.utils.Timer;
import java.util.*;

public class MainScreen implements Screen {
	Level level;
	int levelNum = 1;
	
	IsometricTiledMapRenderer renderer;
	OrthographicCamera camera;
	Entity player, cursorEntity;
	float time;
	Pixmap attackCursor;
	Timer resetTimer;
	
	ShapeRenderer healthPlayer, healthNpc;
	final float BAR_X = 5, BAR_Y = 5;
	final float BAR_WIDTH = 20, BAR_HEIGHT = 80, NPC_BAR_HEIGHT = 0.1f;

	Stage stage;
	Label dropLabel;

	public MainScreen(Game game) {
		Utils.game = game;
	}

	public void drawNPCInfo() {
		float percent = player.health / (player.health + player.wounds);
		healthPlayer.begin(ShapeRenderer.ShapeType.Filled);
		healthPlayer.setColor(Color.RED);
		healthPlayer.rect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
		healthPlayer.setColor(Color.GREEN);
		healthPlayer.rect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT * percent);
		healthPlayer.end();

		dropLabel.setVisible(false);
		if (cursorEntity != null && cursorEntity.isNPC == true) {
			if (cursorEntity.health > 0) {
				percent = cursorEntity.health / (cursorEntity.health + cursorEntity.wounds);
				healthNpc.setProjectionMatrix(camera.combined);
				healthNpc.begin(ShapeRenderer.ShapeType.Filled);
				healthNpc.setColor(Color.RED);
				healthNpc.rect(cursorEntity.x, cursorEntity.y + cursorEntity.height, cursorEntity.width, NPC_BAR_HEIGHT);
				healthNpc.setColor(Color.GREEN);
				healthNpc.rect(cursorEntity.x, cursorEntity.y + cursorEntity.height, cursorEntity.width * percent, NPC_BAR_HEIGHT);
				healthNpc.end();
			} else if (cursorEntity.drop != null) {
				dropLabel.setVisible(true);
				dropLabel.setText(cursorEntity.drop.name);

				Vector3 v = new Vector3(cursorEntity.x, cursorEntity.y, 0);
				camera.project(v);
				dropLabel.setPosition(v.x, v.y);
			}
		}

		stage.draw();
	}
	
	public void show() {
		time = 0;
		
		attackCursor = new Pixmap(Gdx.files.internal("dwarven_gauntlet.png"));
		healthPlayer = new ShapeRenderer();
		healthNpc = new ShapeRenderer();
		
		Utils.playerHurt = Gdx.audio.newSound(Gdx.files.internal("playerhurt.wav"));
		Utils.monsterHurt = Gdx.audio.newSound(Gdx.files.internal("monsterhurt.wav"));
		Utils.death = Gdx.audio.newSound(Gdx.files.internal("death.wav"));

		player = Entities.createPlayer();
		level = Levels.createLevel(player, levelNum);
		
		renderer = new IsometricTiledMapRenderer(level.map, 1 / Utils.PIXELS_PER_TILE);
		camera = new OrthographicCamera();
		
		stage = new Stage();
		Label.LabelStyle style = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
		dropLabel = new Label("", style);
		dropLabel.setVisible(false);
		stage.addActor(dropLabel);

		Gdx.input.setInputProcessor(new InputAdapter() {
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				Entity target = Utils.getEntityAtCursor(camera, level.entities, screenX, screenY);
				
				if (button == Input.Buttons.LEFT) {
					if (target != null) {
						if (target.health == 0 && target.drop != null) {
							Overlays.add(player, target.drop);
							target.drop = null;
						} else if (target.isStaircase == true) {
							levelNum += 1;
							level = Levels.createLevel(player, levelNum);
						}
					}
				} else {
					if (!Utils.canAttack(player, target)) {
						target = null;
					}
					Entities.attack(player, target);
				}
				
				return true;
			}

			public boolean mouseMoved(int screenX, int screenY) {
				cursorEntity = Utils.getEntityAtCursor(camera, level.entities, screenX, screenY);
                Cursor customCursor;
				if (cursorEntity != null) {
                    customCursor = Gdx.graphics.newCursor(attackCursor, 0, 0);
				} else {
                    customCursor = Gdx.graphics.newCursor(null, 0, 0);
				}
				Gdx.graphics.setCursor(customCursor);
				return true;
			}
		});
	}
	
	public void render(float delta) {
		time += delta;
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.position.x = player.x;
		camera.position.y = player.y;
		camera.update();

		renderer.setView(camera);

		Collections.sort(level.entities);

		SpriteBatch batch = (SpriteBatch) renderer.getBatch();
		batch.begin();
		for (Entity e : level.entities) {
			if (e.layer != null) {
				renderer.renderTileLayer(e.layer);
			} else {
				if (e.isMe == true || e.isNPC == true) {
					if (Entities.move(e, player, delta)) {
						Entities.animate(e, time);
						Entities.preventMove(e, level.entities, level.map);
					} else {
						Entities.recover(e);
						if (Utils.canAttack(e, player)) {
							Entities.attack(e, player);
						}
					}
				}
				for (Overlay o : e.overlays) {
					batch.draw(o.image, e.x, e.y, e.width, e.height);
				}
			}
		}
		batch.end();

		drawNPCInfo();

		if (resetTimer == null) {
			if (player.health == 0) {
				resetTimer = new Timer();
				resetTimer.scheduleTask(new Timer.Task() {
					public void run() {
						MainScreen screen = new MainScreen(Utils.game);
						screen.levelNum = levelNum;
						Utils.game.setScreen(screen);
					}
				}, 2);
			} else if (level != null) {
				for (Entity e : level.entities) {
					if (e.isBoss == true && e.health == 0) {
						resetTimer = new Timer();
						resetTimer.scheduleTask(new Timer.Task() {
							public void run() {
								EndScreen screen = new EndScreen();
								Utils.game.setScreen(screen);
							}
						}, 2);
					}
				}
			}
		}
	}
	
	public void dispose() {
	}
	
	public void hide() {
	}
	
	public void pause() {
	}

	public void resize(int width, int height) {
		camera.setToOrtho(false, Utils.CAMERA_HEIGHT * width / height, Utils.CAMERA_HEIGHT);
	}

	public void resume() {
	}
}
