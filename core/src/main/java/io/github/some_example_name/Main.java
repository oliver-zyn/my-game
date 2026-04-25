package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;
    private AssetManager manager;
    private GameWorld world;
    private boolean loaded;
    private boolean assetsReady;
    private float displayProgress;
    private Texture bloonTexture, dartTexture, towerTexture, pathTexture, loadingTexture;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        loaded = false;
        assetsReady = false;
        displayProgress = 0f;
        loadingTexture = new Texture(Gdx.files.internal("path.png"));

        manager = new AssetManager();
        manager.load("bloon_red.png", Texture.class);
        manager.load("dart.png", Texture.class);
        manager.load("tower.png", Texture.class);
        manager.load("path.png", Texture.class);
    }

    @Override
    public void render() {
        if (!loaded) {
            renderLoading();
        } else {
            renderGame();
        }
    }

    private void renderLoading() {
        if (manager.update()) {
            assetsReady = true;
        }

        float targetProgress = assetsReady ? 1f : manager.getProgress();
        displayProgress += (targetProgress - displayProgress) * 0.02f;
        if (displayProgress > 0.99f) displayProgress = 1f;

        if (assetsReady && displayProgress >= 1f) {
            bloonTexture = manager.get("bloon_red.png", Texture.class);
            dartTexture = manager.get("dart.png", Texture.class);
            towerTexture = manager.get("tower.png", Texture.class);
            pathTexture = manager.get("path.png", Texture.class);
            world = new GameWorld(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            Gdx.input.setInputProcessor(new InputAdapter() {
                @Override
                public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                    world.addTower(screenX, Gdx.graphics.getHeight() - screenY);
                    return true;
                }
            });

            loaded = true;
            return;
        }

        float progress = displayProgress;
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float barWidth = w * 0.5f;
        float barHeight = 20f;
        float barX = (w - barWidth) / 2f;
        float barY = h / 2f - barHeight;

        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        batch.begin();

        font.draw(batch, "Carregando... " + (int) (progress * 100) + "%", w / 2f - 60, h / 2f + 30);

        batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        batch.draw(loadingTexture, barX, barY, barWidth, barHeight);

        batch.setColor(0.2f, 0.8f, 0.2f, 1f);
        batch.draw(loadingTexture, barX, barY, barWidth * progress, barHeight);

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private void renderGame() {
        world.update(Gdx.graphics.getDeltaTime());
        ScreenUtils.clear(0.3f, 0.6f, 0.2f, 1f);
        batch.begin();

        Vector2[] wp = world.waypoints;
        for (int i = 0; i < wp.length - 1; i++) {
            drawPathSegment(wp[i].x, wp[i].y, wp[i + 1].x, wp[i + 1].y, 60f);
        }

        float tw = towerTexture.getWidth();
        float th = towerTexture.getHeight();
        for (int i = 0; i < world.towers.size; i++) {
            Tower tower = world.towers.get(i);
            batch.draw(towerTexture,
                tower.position.x - tw / 2f, tower.position.y - th / 2f,
                tw / 2f, th / 2f, tw, th, 1, 1, tower.angle + 90,
                0, 0, (int) tw, (int) th, false, false);
        }

        for (int i = 0; i < world.activeBloons.size; i++) {
            Bloon bloon = world.activeBloons.get(i);
            if (bloon.alive) {
                batch.draw(bloonTexture,
                    bloon.position.x - bloonTexture.getWidth() / 2f,
                    bloon.position.y - bloonTexture.getHeight() / 2f);
            }
        }

        float dw = dartTexture.getWidth();
        float dh = dartTexture.getHeight();
        for (int i = 0; i < world.activeDarts.size; i++) {
            Dart dart = world.activeDarts.get(i);
            if (dart.alive) {
                batch.draw(dartTexture,
                    dart.position.x - dw / 2f, dart.position.y - dh / 2f,
                    dw / 2f, dh / 2f, dw, dh, 1, 1, dart.angle,
                    0, 0, (int) dw, (int) dh, false, false);
            }
        }

        float h = Gdx.graphics.getHeight();
        font.draw(batch, "Vidas: " + world.lives + "  Pontos: " + world.score, 10, h - 10);
        font.draw(batch, "Pool bloons livres: " + world.bloonPool.getFree()
            + " | Pool dardos livres: " + world.dartPool.getFree(), 10, h - 30);
        font.draw(batch, "Bloons ativos: " + world.activeBloons.size
            + " | Dardos ativos: " + world.activeDarts.size, 10, h - 50);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        loadingTexture.dispose();
        manager.dispose();
    }

    private void drawPathSegment(float x1, float y1, float x2, float y2, float width) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        int pw = pathTexture.getWidth();
        int ph = pathTexture.getHeight();
        int tiles = Math.max(1, (int) (length / ph));

        if (Math.abs(dx) > Math.abs(dy)) {
            float stepX = dx / tiles;
            for (int t = 0; t < tiles; t++) {
                batch.draw(pathTexture, x1 + stepX * t, y1 - width / 2f, Math.abs(stepX) + 1, width);
            }
        } else {
            float stepY = dy / tiles;
            float tileLen = Math.abs(stepY);
            for (int t = 0; t < tiles; t++) {
                float drawY = Math.min(y1 + stepY * t, y1 + stepY * (t + 1));
                batch.draw(pathTexture,
                    x1 - width / 2f, drawY - 2,
                    width / 2f, (tileLen + 4) / 2f,
                    width, tileLen + 4, 1, 1, 90,
                    0, 0, pw, ph, false, false);
            }
        }
    }
}
