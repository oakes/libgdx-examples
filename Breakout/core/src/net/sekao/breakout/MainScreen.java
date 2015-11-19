package net.sekao.breakout;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import java.util.*;

public class MainScreen implements Screen {
    Stage stage;
    World world;
    Image ball, paddle;
    Body ballBody, wallBody, floorBody, paddleBody;
    HashMap blocks;
    final int scale = 32;

    public void show() {
        stage = new Stage();
        blocks = new HashMap();

        Texture ballTexture = new Texture("ball.png");
        Texture blockTexture = new Texture("block.png");

        ball = new Image(ballTexture);
        stage.addActor(ball);
        paddle = new Image(blockTexture);
        stage.addActor(paddle);

        final float screenWidth = Gdx.graphics.getWidth();
        final float screenHeight = Gdx.graphics.getHeight();
        final float blockWidth = blockTexture.getWidth();
        final float blockHeight = blockTexture.getHeight();

        world = new World(new Vector2(0, 0), true);

        float col = screenWidth / blockWidth;
        while (col > 0) {
            col--;

            float row = screenHeight / blockHeight / 2;
            while (row > 0) {
                row--;

                float x = col * blockWidth;
                float y = row * blockHeight + screenHeight / 2;

                Image block = new Image(blockTexture);
                block.setPosition(x, y);
                stage.addActor(block);
                Body blockBody = createRectBody(x, y, blockWidth, blockHeight);
                blocks.put(blockBody, block);
            }
        }

        ballBody = createBallBody(100, 100, ballTexture.getWidth()/2);
        ballBody.setLinearVelocity(10, 10);

        wallBody = createRectBody(0, 0, screenWidth, screenHeight);
        floorBody = createRectBody(0, 0, screenWidth, 1);
        paddleBody = createRectBody(0, 0, blockWidth, blockHeight);

        Gdx.input.setInputProcessor(stage);
        stage.addListener(new InputListener() {
            public boolean handle(Event e) {
                float x = Gdx.input.getX() - blockWidth / 2;
                moveBody(paddleBody, x, 0);
                return true;
            }
        });

        world.setContactListener(new ContactListener() {
            public void beginContact(Contact c) {
                Body b = c.getFixtureA().getBody();
                Image i = (Image) blocks.get(b);
                if (i != null) {
                    i.remove();
                } else if (b == floorBody) {
                    show();
                }
            }
            public void endContact(Contact c) {}
            public void postSolve(Contact c, ContactImpulse ci) {}
            public void preSolve(Contact c, Manifold m) {}
        });
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.step(delta, 10, 10);
        paddle.setPosition(paddleBody.getPosition().x*scale, paddleBody.getPosition().y*scale);
        ball.setPosition(ballBody.getPosition().x*scale, ballBody.getPosition().y*scale);

        Iterator iter = blocks.keySet().iterator();
        while (iter.hasNext()) {
            Body b = (Body) iter.next();
            Image i = (Image) blocks.get(b);
            if (i.hasParent() == false) {
                world.destroyBody(b);
                iter.remove();
            }
        }

        stage.act(delta);
        stage.draw();
    }

    public void dispose() {
    }

    public void hide() {
    }

    public void pause() {
    }

    public void resize(int width, int height) {
    }

    public void resume() {
    }

    public Body createBallBody(float x, float y, float radius) {
        x = x/scale;
        y = y/scale;
        radius = radius/scale;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);
        Body body = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        shape.setPosition(new Vector2(radius, radius));
        Fixture fixture = body.createFixture(shape, 1);
        fixture.setFriction(0);
        fixture.setRestitution(1);
        shape.dispose();

        return body;
    }

    public Body createRectBody(float x, float y, float width, float height) {
        x = x/scale;
        y = y/scale;
        width = width/scale;
        height = height/scale;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(x, y);
        Body body = world.createBody(def);

        ChainShape shape = new ChainShape();
        float[] vertices = {
                0, 0,
                0, height,
                width, height,
                width, 0,
                0, 0
        };
        shape.createChain(vertices);
        body.createFixture(shape, 1);
        shape.dispose();

        return body;
    }

    public void moveBody(Body body, float x, float y) {
        x = x/scale;
        y = y/scale;

        body.setTransform(x, y, 0);
    }
}
