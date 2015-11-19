package net.sekao.dungeoncrawler;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import java.util.*;

public class Overlays {
	public static Overlay create(TextureRegion[][] grid, float interval) {
		Overlay o = new Overlay();
		
		int row = grid.length;
		
		o.move = new Animation[row];
		o.attack = new TextureRegion[row];
		o.special = new TextureRegion[row];
		o.hit = new TextureRegion[row];
		o.dead = new TextureRegion[row];
		
		while (row > 0) {
			row--;
			o.move[row] = new Animation(interval, grid[row][0], grid[row][1], grid[row][2], grid[row][3]);
			o.attack[row] = grid[row][4];
			o.special[row] = grid[row][5];
			o.hit[row] = grid[row][6];
			o.dead[row] = grid[row][7];
		}

		int south = Entity.Direction.S.ordinal();
		o.image = grid[south][0];

		return o;
	}

	public static Overlay create(String path, int maskSize, float interval) {
		TextureRegion[][] grid = Utils.splitTexture(path, Utils.GRID_TILE_SIZE, maskSize);

		return create(grid, interval);
	}

	public static ArrayList<Overlay> createItems() {
		ArrayList<Overlay> items = new ArrayList<Overlay>();

		Overlay armor = create("characters/male_heavy.png", 128, 0.1f);
		armor.health = 80;
		armor.name = "Heavy Armor";
		armor.zIndex = 1;
		items.add(armor);

		Overlay sword = create("characters/male_longsword.png", 128, 0.1f);
		sword.damage = 4;
		sword.name = "Sword";
		sword.zIndex = 2;
		items.add(sword);
		
		Overlay shield = create("characters/male_shield.png", 128, 0.1f);
		shield.health = 20;
		shield.name = "Shield";
		shield.zIndex = 2;
		items.add(shield);

		return items;
	}

	public static void add(Entity e, Overlay o) {
		if (!e.overlays.contains(o)) {
			e.overlays.add(o);
			Collections.sort(e.overlays);
			e.damage += o.damage;
			e.health += o.health;
		}
	}
}
