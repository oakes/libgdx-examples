package net.sekao.tetris;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.Input.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

public class MainScreen implements Screen {
    TextureRegion[] shapes = null;
    Image activePiece = null;
    Stage stage = null;
    Timer timer = null;
    int viewWidth = 0;
    int viewHeight = 0;
    int scale = 0;

    public void show() {
        viewWidth = Gdx.graphics.getWidth();
        viewHeight = Gdx.graphics.getHeight();
        scale = viewWidth / 15;
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        Texture sheet = new Texture("shapes.png");
        shapes = new TextureRegion[] {
                new TextureRegion(sheet, 0, 0, 1, 4),
                new TextureRegion(sheet, 1, 0, 2, 3),
                new TextureRegion(sheet, 3, 0, 2, 3),
                new TextureRegion(sheet, 5, 0, 2, 2),
                new TextureRegion(sheet, 7, 0, 2, 3),
                new TextureRegion(sheet, 9, 0, 3, 2),
                new TextureRegion(sheet, 12, 0, 2, 3)
        };

        timer = new Timer();
        timer.scheduleTask(new Timer.Task() {
            public void run() {
                if (activePiece == null || !movePiece(activePiece, 0, -scale)) {
                    checkForFullRows();
                    activePiece = createPiece(new Random().nextInt(shapes.length));
                }
            }
        }, 0, 1);
        timer.start();

        stage.addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode) {
                if (activePiece == null) {
                    return false;
                }

                if (keycode == Keys.DPAD_DOWN) {
                    movePiece(activePiece, 0, -scale);
                } else if (keycode == Keys.DPAD_LEFT) {
                    movePiece(activePiece, -scale, 0);
                } else if (keycode == Keys.DPAD_RIGHT) {
                    movePiece(activePiece, scale, 0);
                } else if (keycode == Keys.SPACE) {
                    rotatePiece(activePiece);
                }

                return true;
            }
        });
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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

    Image createPiece(int index) {
        Image piece = new Image(shapes[index]);
        piece.setScale(scale);
        piece.setPosition(0, viewHeight - (4 * scale));
        stage.addActor(piece);
        return piece;
    }

    boolean movePiece(Image piece, float xChange, float yChange) {
        int pixelCount = countPixels(0, 0, viewWidth, viewHeight);
        piece.setPosition(piece.getX() + xChange, piece.getY() + yChange);
        if (pixelCount != countPixels(0, 0, viewWidth, viewHeight)) {
            piece.setPosition(piece.getX() - xChange, piece.getY() - yChange);
            return false;
        }
        return true;
    }

    boolean rotatePiece(Image piece) {
        int pixelCount = countPixels(0, 0, viewWidth, viewHeight);
        piece.rotateBy(90);
        if (pixelCount != countPixels(0, 0, viewWidth, viewHeight)) {
            piece.rotateBy(-90);
            return false;
        }
        return true;
    }

    int countPixels(int x, int y, int w, int h) {
        render(0);
        byte[] bytes = ScreenUtils.getFrameBufferPixels(x, y, w, h, false);
        ByteBuffer pixels = ByteBuffer.wrap(bytes);
        int count = 0;
        while (pixels.hasRemaining()) {
            if (new Color(pixels.getInt()).a != 0) {
                count++;
            }
        }
        return count;
    }

    void checkForFullRows() {
        int y = viewHeight - scale;
        while (y >= 0) {
            if (countPixels(0, y, viewWidth, 1) == viewWidth) {
                moveColumnsDown(y + scale);
            }
            y = y - scale;
        }
    }

    void moveColumnsDown(int y) {
        ArrayList<Image> bottomColumns = new ArrayList<Image>();
        ArrayList<Image> topColumns = new ArrayList<Image>();

        int x = 0;
        while (x < viewWidth) {
            Image columnBottom = new Image(ScreenUtils.getFrameBufferTexture(x, 0, scale, y - scale));
            columnBottom.setPosition(x, 0);
            bottomColumns.add(columnBottom);

            if (countPixels(x, y, scale, viewHeight - y) > 0) {
                Image columnTop = new Image(ScreenUtils.getFrameBufferTexture(x, y, scale, viewHeight - y));
                columnTop.setPosition(x, y);
                topColumns.add(columnTop);
            }

            x = x + scale;
        }

        stage.getRoot().clearChildren();

        for (Image column : bottomColumns) {
            stage.addActor(column);
        }
        for (Image column : topColumns) {
            stage.addActor(column);
            while (movePiece(column, 0, -scale)) {}
        }
    }
}
