package net.sekao.minicraft;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import java.util.*;

public class Entities {
	public static Entity create(String startLayer, TextureRegion down, TextureRegion up, TextureRegion standRight, TextureRegion walkRight) {
		Entity player = new Entity();
		player.width = 2;
		player.height = 2;
		player.xVelocity = 0;
		player.yVelocity = 0;
		player.minDistance = 5;
		player.startLayer = startLayer;

		float duration = 0.2f;
		player.down = new Animation(duration, down, Utils.flipX(down));
		player.up = new Animation(duration, up, Utils.flipX(up));
		player.right = new Animation(duration, standRight, walkRight);
		player.left = new Animation(duration, Utils.flipX(standRight), Utils.flipX(walkRight));

		player.image = down;

		return player;
	}
	
	public static Entity create(String startLayer, TextureRegion down, TextureRegion up) {
		Entity player = new Entity();
		player.width = 2;
		player.height = 2;
		player.xVelocity = 0;
		player.yVelocity = 0;
		player.minDistance = 5;
		player.startLayer = startLayer;

		float duration = 0.2f;
		Animation anim = new Animation(duration, down, up);
		player.down = anim;
		player.up = anim;
		player.right = anim;
		player.left = anim;

		player.image = down;

		return player;
	}

	public static Entity create(String startLayer, TextureRegion img) {
		Entity player = new Entity();
		player.width = 2;
		player.height = 2;
		player.xVelocity = 0;
		player.yVelocity = 0;
		player.startLayer = startLayer;
		player.image = img;

		return player;
	}

	public static boolean move(Entity e, Entity player, float delta) {
		float maxVelocity = 5;
		float maxNpcVelocity = 3;
		
		if (e.isMe == true) {
			e.attackTime = Math.max(0, e.attackTime - delta);
			
			boolean downTouched = Gdx.input.isTouched() && Gdx.input.getY() > Gdx.graphics.getHeight() * 2 / 3;
			if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || downTouched) {
				e.yVelocity = -1 * maxVelocity;
			}
			
			boolean upTouched = Gdx.input.isTouched() && Gdx.input.getY() < Gdx.graphics.getHeight() / 3;
			if (Gdx.input.isKeyPressed(Input.Keys.UP) || upTouched) {
				e.yVelocity = maxVelocity;
			}
			
			boolean leftTouched = Gdx.input.isTouched() && Gdx.input.getX() < Gdx.graphics.getWidth() / 3;
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || leftTouched) {
				e.xVelocity = -1 * maxVelocity;
			}
			
			boolean rightTouched = Gdx.input.isTouched() && Gdx.input.getX() > Gdx.graphics.getWidth() * 2 / 3;
			if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || rightTouched) {
				e.xVelocity = maxVelocity;
			}
		} else if (e.isNpc == true) {
			if (e.attackTime == 0) {
				e.attackTime = 1;
			}
			
			e.attackTime = Math.max(0, e.attackTime - delta);
			
			if (Utils.isNearEntity(e, player, 6)) {
				float xDiff = e.x - player.x;
				float yDiff = e.y - player.y;
				float closeEnough = 1.5f;
				
				if (xDiff > closeEnough) {
					e.xVelocity = -1 * maxNpcVelocity;
				} else if (xDiff < -1 * closeEnough) {
					e.xVelocity = maxNpcVelocity;
				}

				if (yDiff > closeEnough) {
					e.yVelocity = -1 * maxNpcVelocity;
				} else if (yDiff < -1 * closeEnough) {
					e.yVelocity = maxNpcVelocity;
				}
			} else if (e.attackTime == 0) {
				e.xVelocity = maxNpcVelocity * Utils.random();
				e.yVelocity = maxNpcVelocity * Utils.random();
			}
		}

		e.xChange = e.xVelocity * delta;
		e.yChange = e.yVelocity * delta;

		e.x = e.x + e.xChange;
		e.y = e.y + e.yChange;

		e.xVelocity = Utils.decelerate(e.xVelocity);
		e.yVelocity = Utils.decelerate(e.yVelocity);

		return e.xChange != 0 || e.yChange != 0;
	}
	
	public static void animate(Entity e, float time, TiledMap map) {
		Animation anim = null;
		if (e.yVelocity != 0) {
			if (e.yVelocity > 0) {
				anim = e.up;
				e.lastDirection = Entity.Direction.UP;
			} else {
				anim = e.down;
				e.lastDirection = Entity.Direction.DOWN;
			}
		} else if (e.xVelocity != 0) {
			if (e.xVelocity > 0) {
				anim = e.right;
				e.lastDirection = Entity.Direction.RIGHT;
			} else {
				anim = e.left;
				e.lastDirection = Entity.Direction.LEFT;
			}
		}
		
		if (anim != null) {
			e.image = anim.getKeyFrame(time, true);
		}
		
		if (Utils.isOnLayer(e, map, "water")) {
			e.image = new TextureRegion(e.image);
			e.image.setRegionHeight((int) Utils.pixelsPerTile);
		}

		e.width = e.image.getRegionWidth() / Utils.pixelsPerTile;
		e.height = e.image.getRegionHeight() / Utils.pixelsPerTile;
	}

	public static void preventMove(Entity e, ArrayList<Entity> entities) {
		if (Utils.isNearEntity(e, entities, 1) || !Utils.isOnMap(e)) {
			e.x = e.x - e.xChange;
			e.y = e.y - e.yChange;
			e.xVelocity = 0;
			e.yVelocity = 0;
		}
	}

	public static void attack(Entity e, ArrayList<Entity> entities, Entity swipe, Entity hit, TiledMap map) {
		if (Utils.isOnLayer(e, map, "water")) {
			return;
		}
		
		Entity victim = null;
		for (Entity e2 : entities) {
			if (Utils.canAttack(e, e2)) {
				boolean xIsGood = false, yIsGood = false;
				switch (e.lastDirection) {
					case DOWN:
						xIsGood = true;
						yIsGood = e.y - e2.y > 0;
						break;
					case UP:
						xIsGood = true;
						yIsGood = e.y - e2.y < 0;
						break;
					case RIGHT:
						xIsGood = e.x - e2.x < 0;
						yIsGood = true;
						break;
					case LEFT:
						xIsGood = e.x - e2.x > 0;
						yIsGood = true;
						break;
				}
				if (xIsGood && yIsGood) {
					victim = e2;
					break;
				}
			}
		}
		
		if (e.isMe && e.health > 0) {
			swipe.related = e;
			swipe.drawTime = 0.2f;
		}

		hit.related = victim;
		if (victim != null) {
			hit.drawTime = 0.2f;
			victim.health = Math.max(0, victim.health - e.damage);
			
			e.attackTime = Math.min(e.stamina, e.attackTime + 1);
			
			if (victim.isMe == true) {
				Utils.playerHurt.play();
				if (victim.health == 0) {
					Utils.death.play();
				}
			} else {
				Utils.monsterHurt.play();
			}
		}
	}

	public static void animateSwipe(Entity swipe) {
		if (swipe.related == null) {
			return;
		}
		
		switch (swipe.related.lastDirection) {
			case DOWN:
				swipe.x = swipe.related.x;
				swipe.y = swipe.related.y - 1;
				swipe.image = swipe.down.getKeyFrame(0, true);
				break;
			case UP:
				swipe.x = swipe.related.x;
				swipe.y = swipe.related.y + 1;
				swipe.image = swipe.up.getKeyFrame(0, true);
				break;
			case RIGHT:
				swipe.x = swipe.related.x + 1;
				swipe.y = swipe.related.y;
				swipe.image = swipe.right.getKeyFrame(0, true);
				break;
			case LEFT:
				swipe.x = swipe.related.x - 1;
				swipe.y = swipe.related.y;
				swipe.image = swipe.left.getKeyFrame(0, true);
				break;
		}
	}

	public static void animateHit(Entity hit) {
		if (hit.related == null) {
			return;
		}
		
		hit.x = hit.related.x;
		hit.y = hit.related.y - 0.1f;
	}
}
