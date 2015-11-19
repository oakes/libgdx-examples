package net.sekao.minicraft;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.*;
import java.util.*;

public class MainScreen implements Screen {
	TiledMap map;
	Stage stage;
	OrthogonalTiledMapRenderer renderer;
	OrthographicCamera camera;
	ArrayList<Entity> entities;
	Entity player, swipe, hit;
	float time;
	ArrayList<Image> hearts, bolts;
	
	public void show() {
		time = 0;
		
		map = new TmxMapLoader().load("level1.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, 1 / Utils.pixelsPerTile);
		
		camera = new OrthographicCamera();

		Utils.start = Gdx.audio.newSound(Gdx.files.internal("test.wav"));
		Utils.playerHurt = Gdx.audio.newSound(Gdx.files.internal("playerhurt.wav"));
		Utils.monsterHurt = Gdx.audio.newSound(Gdx.files.internal("monsterhurt.wav"));
		Utils.death = Gdx.audio.newSound(Gdx.files.internal("death.wav"));
		
		Texture tiles = new Texture("tiles.png");
		TextureRegion[][] grid = TextureRegion.split(tiles, 16, 16);
		TextureRegion treeTexture = new TextureRegion(tiles, 0, 8, 16, 16);
		TextureRegion cactusTexture = new TextureRegion(tiles, 16, 8, 16, 16);
		TextureRegion attackDownTexture = new TextureRegion(tiles, 48, 0, 16, 8);
		TextureRegion attackRightTexture = new TextureRegion(tiles, 32, 8, 8, 16);
		TextureRegion hitTexture = new TextureRegion(tiles, 40, 8, 16, 16);
		TextureRegion heartOffTexture = new TextureRegion(tiles, 64, 0, 8, 8);
		TextureRegion boltOffTexture = new TextureRegion(tiles, 72, 0, 8, 8);
		TextureRegion heartOnTexture = new TextureRegion(tiles, 80, 0, 8, 8);
		TextureRegion boltOnTexture = new TextureRegion(tiles, 88, 0, 8, 8);
		TextureRegion blackTexture = new TextureRegion(tiles, 0, 18, 1, 1);
		
		player = Entities.create("grass", grid[6][0], grid[6][1], grid[6][2], grid[6][3]);
		player.isMe = true;
		player.health = 10;
		player.stamina = 5;
		player.damage = 4;
		
		entities = new ArrayList<Entity>();
		entities.add(player);

		swipe = Entities.create("grass", attackDownTexture, Utils.flipY(attackDownTexture), attackRightTexture, attackRightTexture);
		swipe.drawTime = 0;
		entities.add(swipe);
		
		hit = Entities.create("grass", hitTexture);
		hit.drawTime = 0;
		entities.add(hit);
		
		int zombieCount = 5;
		while (zombieCount > 0) {
			Entity zombie = Entities.create("grass", grid[6][4], grid[6][5], grid[6][6], grid[6][7]);
			zombie.isNpc = true;
			zombie.health = 10;
			zombie.damage = 3;
			entities.add(zombie);
			zombieCount = zombieCount - 1;
		}
		
		int slimeCount = 5;
		while (slimeCount > 0) {
			Entity slime = Entities.create("grass", grid[7][4], grid[7][5]);
			slime.isNpc = true;
			slime.health = 10;
			slime.damage = 2;
			entities.add(slime);
			slimeCount = slimeCount - 1;
		}

		int treeCount = 100;
		while (treeCount > 0) {
			Entity tree = Entities.create("grass", treeTexture);
			tree.health = 12;
			entities.add(tree);
			treeCount = treeCount - 1;
		}

		int cactusCount = 10;
		while (cactusCount > 0) {
			Entity cactus = Entities.create("desert", cactusTexture);
			cactus.damage = 3;
			cactus.health = 12;
			entities.add(cactus);
			cactusCount = cactusCount - 1;
		}
		
		for (Entity e : entities) {
			ArrayList<Entity> options = Utils.getLocationOptions(e.width, e.height, Utils.mapWidth, Utils.mapHeight);
			Utils.shuffle(options);
			for (Entity option : options) {
				if (Utils.isOnlyOnLayer(option, map, e.startLayer) && !Utils.isNearEntity(option, entities, e.minDistance)) {
					e.x = option.x;
					e.y = option.y;
					break;
				}
			}
		}

		Gdx.input.setInputProcessor(new InputAdapter() {
			public boolean keyDown(int keycode) {
				if (keycode == Input.Keys.SPACE) {
					Entities.attack(player, entities, swipe, hit, map);
					return true;
				}
				return false;
			}
			
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				int width = Gdx.graphics.getWidth();
				int height = Gdx.graphics.getHeight();
				int minX = width / 3, maxX = width * 2 / 3;
				int minY = height / 3, maxY = height * 2 / 3;
				if (screenX > minX && screenX < maxX && screenY > minY && screenY < maxY) {
					Entities.attack(player, entities, swipe, hit, map);
					return true;
				}
				return false;
			}
		});
		
		stage = new Stage();
		
		Image bar = Utils.createImage(stage, blackTexture, 0, 0);
		bar.setSize(Utils.stageWidth, Utils.pixelsPerTile * 2);
		
		hearts = new ArrayList<Image>();
		bolts = new ArrayList<Image>();
		
		int count = 0;
		while (count < player.health) {
			float x = count * Utils.pixelsPerTile;
			
			Utils.createImage(stage, heartOffTexture, x, Utils.pixelsPerTile);
			hearts.add(Utils.createImage(stage, heartOnTexture, x, Utils.pixelsPerTile));
			Utils.createImage(stage, boltOffTexture, x, 0);
			bolts.add(Utils.createImage(stage, boltOnTexture, x, 0));
			
			count++;
		}

		Utils.start.play();
	}
	
	public void render(float delta) {
		time += delta;
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.position.x = player.x;
		camera.position.y = player.y;
		camera.update();
		
		renderer.setView(camera);
		renderer.render();

		Collections.sort(entities);

		Entities.animateSwipe(swipe);
		Entities.animateHit(hit);

		Utils.updateImages(hearts, ((float) player.health) / 10);
		Utils.updateImages(bolts, (player.stamina - player.attackTime) / player.stamina);
		
		SpriteBatch batch = (SpriteBatch) renderer.getBatch();
		batch.begin();
		for (Entity e : entities) {
			if (e.drawTime > 0) {
				e.drawTime = Math.max(0, e.drawTime - delta);
			} else if (e.drawTime == 0 || e.health == 0) {
				continue;
			}
			if (Entities.move(e, player, delta)) {
				Entities.animate(e, time, map);
				Entities.preventMove(e, entities);
			}
			if (e.isNpc && Utils.canAttack(e, player)) {
				Entities.attack(e, entities, swipe, hit, map);
			}
			batch.draw(e.image, e.x, e.y, e.width, e.height);
		}
		batch.end();

		//stage.draw();
	}
	
	public void dispose() {
	}
	
	public void hide() {
	}
	
	public void pause() {
	}

	public void resize(int width, int height) {
		camera.setToOrtho(false, Utils.cameraHeight * width / height, Utils.cameraHeight);
		//stage.setViewport(Utils.stageWidth, Utils.stageWidth * height / width);
	}

	public void resume() {
	}
}
