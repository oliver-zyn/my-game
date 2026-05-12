package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;
    private AssetManager manager;
    private GameWorld world;
    private OrthographicCamera camera;

    private boolean loaded;
    private boolean assetsReady;
    private float displayProgress;

    private Texture bloonTexture, dartTexture, towerTexture, loadingTexture;
    private Texture pathHorizontal, pathVertical;
    private Texture cornerLL, cornerLR, cornerUL, cornerUR;
    private Texture grassTexture, treeLargeTexture, treeSmallTexture, barricadeTexture;
    private Texture barrelRedTexture, barrelGreenTexture, crateTexture, sandbagTexture;
    private Scenery scenery;
    private Music bgMusic;

    private static final float CAMERA_SPEED = 300f;
    private static final float ZOOM_SPEED = 0.1f;
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 2.0f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        loaded = false;
        assetsReady = false;
        displayProgress = 0f;
        loadingTexture = new Texture(Gdx.files.internal("path.png"));

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera(w, h);
        camera.position.set(w / 2f, h / 2f, 0);
        camera.update();

        manager = new AssetManager();
        manager.load("bloon_red.png", Texture.class);
        manager.load("dart.png", Texture.class);
        manager.load("tower.png", Texture.class);
        manager.load("path_h.png", Texture.class);
        manager.load("path_v.png", Texture.class);
        manager.load("path_corner_ll.png", Texture.class);
        manager.load("path_corner_lr.png", Texture.class);
        manager.load("path_corner_ul.png", Texture.class);
        manager.load("path_corner_ur.png", Texture.class);
        manager.load("grass.png", Texture.class);
        manager.load("tree_large.png", Texture.class);
        manager.load("tree_small.png", Texture.class);
        manager.load("barricade.png", Texture.class);
        manager.load("barrel_red.png", Texture.class);
        manager.load("barrel_green.png", Texture.class);
        manager.load("crate.png", Texture.class);
        manager.load("sandbag.png", Texture.class);
        manager.load("shoot.ogg", Sound.class);
        manager.load("pop.ogg", Sound.class);
        manager.load("bgmusic.mp3", Music.class);
    }

    @Override
    public void render() {
        if (!loaded) {
            renderLoading();
        } else {
            updateCamera(Gdx.graphics.getDeltaTime());
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
            initGame();
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

    private void initGame() {
        bloonTexture = manager.get("bloon_red.png", Texture.class);
        dartTexture = manager.get("dart.png", Texture.class);
        towerTexture = manager.get("tower.png", Texture.class);
        pathHorizontal = manager.get("path_h.png", Texture.class);
        pathVertical = manager.get("path_v.png", Texture.class);
        cornerLL = manager.get("path_corner_ll.png", Texture.class);
        cornerLR = manager.get("path_corner_lr.png", Texture.class);
        cornerUL = manager.get("path_corner_ul.png", Texture.class);
        cornerUR = manager.get("path_corner_ur.png", Texture.class);
        grassTexture = manager.get("grass.png", Texture.class);
        treeLargeTexture = manager.get("tree_large.png", Texture.class);
        treeSmallTexture = manager.get("tree_small.png", Texture.class);
        barricadeTexture = manager.get("barricade.png", Texture.class);
        barrelRedTexture = manager.get("barrel_red.png", Texture.class);
        barrelGreenTexture = manager.get("barrel_green.png", Texture.class);
        crateTexture = manager.get("crate.png", Texture.class);
        sandbagTexture = manager.get("sandbag.png", Texture.class);

        Dart.setShootSound(manager.get("shoot.ogg", Sound.class));
        Bloon.setPopSound(manager.get("pop.ogg", Sound.class));

        bgMusic = manager.get("bgmusic.mp3", Music.class);
        bgMusic.setLooping(true);
        bgMusic.setVolume(0.15f);
        bgMusic.play();

        float worldW = Gdx.graphics.getWidth();
        float worldH = Gdx.graphics.getHeight();
        world = new GameWorld(worldW, worldH);

        Texture[] decorations = new Texture[] {
            treeLargeTexture, treeLargeTexture, treeSmallTexture, treeSmallTexture,
            barricadeTexture, barrelRedTexture, barrelGreenTexture,
            crateTexture, sandbagTexture
        };
        scenery = new Scenery(grassTexture, decorations,
            world.waypoints, 60f, worldW, worldH, 35);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 worldCoords = new Vector3(screenX, screenY, 0);
                camera.unproject(worldCoords);
                world.addTower(worldCoords.x, worldCoords.y);
                return true;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                camera.zoom += amountY * ZOOM_SPEED;
                if (camera.zoom < MIN_ZOOM) camera.zoom = MIN_ZOOM;
                if (camera.zoom > MAX_ZOOM) camera.zoom = MAX_ZOOM;
                return true;
            }
        });
    }

    private void updateCamera(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.position.y += CAMERA_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.position.y -= CAMERA_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.position.x -= CAMERA_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.position.x += CAMERA_SPEED * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            float vol = bgMusic.getVolume() + 0.5f * delta;
            if (vol > 1f) vol = 1f;
            bgMusic.setVolume(vol);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            float vol = bgMusic.getVolume() - 0.5f * delta;
            if (vol < 0f) vol = 0f;
            bgMusic.setVolume(vol);
        }

        camera.update();
    }

    private void renderGame() {
        world.update(Gdx.graphics.getDeltaTime());
        ScreenUtils.clear(0.15f, 0.4f, 0.15f, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        scenery.draw(batch);

        drawPath();

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
                if (bloon.isInvulnerable()) {
                    float alpha = ((int) (bloon.getSpawnTimer() * 10)) % 2 == 0 ? 1f : 0.3f;
                    batch.setColor(1f, 1f, 1f, alpha);
                }
                batch.draw(bloonTexture,
                    bloon.position.x - bloonTexture.getWidth() / 2f,
                    bloon.position.y - bloonTexture.getHeight() / 2f);
                batch.setColor(1f, 1f, 1f, 1f);
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

        batch.end();

        batch.setProjectionMatrix(batch.getProjectionMatrix().idt());
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);
        batch.begin();

        font.draw(batch, "Vidas: " + world.lives + "  Pontos: " + world.score, 10, screenH - 10);
        font.draw(batch, "Pool bloons: " + world.bloonPool.getFree()
            + " | Pool darts: " + world.dartPool.getFree(), 10, screenH - 30);
        font.draw(batch, "Zoom: " + String.format("%.1f", camera.zoom)
            + " | Cam: (" + (int) camera.position.x + ", " + (int) camera.position.y + ")", 10, screenH - 50);
        font.draw(batch, "WASD: camera | Scroll: zoom | UP/DOWN: volume ("
            + (int)(bgMusic.getVolume() * 100) + "%)", 10, screenH - 70);

        for (int i = 0; i < world.floatingScores.size; i++) {
            FloatingScore fs = world.floatingScores.get(i);
            Vector3 screenPos = new Vector3(fs.position.x, fs.position.y, 0);
            camera.project(screenPos);
            font.setColor(1f, 1f, 0f, fs.getAlpha());
            font.draw(batch, fs.text, screenPos.x, screenPos.y);
        }
        font.setColor(1f, 1f, 1f, 1f);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        loadingTexture.dispose();
        manager.dispose();
    }

    private void drawPath() {
        Vector2[] wp = world.waypoints;
        float tileSize = 64f;
        float half = tileSize / 2f;

        for (int i = 0; i < wp.length - 1; i++) {
            float x1 = wp[i].x;
            float y1 = wp[i].y;
            float x2 = wp[i + 1].x;
            float y2 = wp[i + 1].y;

            float dx = x2 - x1;
            float dy = y2 - y1;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            float ux = dx / len;
            float uy = dy / len;

            if (i > 0) {
                x1 += ux * half;
                y1 += uy * half;
            }
            if (i < wp.length - 2) {
                x2 -= ux * half;
                y2 -= uy * half;
            }

            drawPathSegment(x1, y1, x2, y2, tileSize);
        }

        for (int i = 1; i < wp.length - 1; i++) {
            Texture corner = getCornerTexture(wp[i - 1], wp[i], wp[i + 1]);
            batch.draw(corner, wp[i].x - half, wp[i].y - half, tileSize, tileSize);
        }
    }

    private Texture getCornerTexture(Vector2 prev, Vector2 curr, Vector2 next) {
        float inDx = Math.signum(curr.x - prev.x);
        float inDy = Math.signum(curr.y - prev.y);
        float outDx = Math.signum(next.x - curr.x);
        float outDy = Math.signum(next.y - curr.y);

        if (inDx > 0 && outDy < 0) return cornerLL;
        if (inDy < 0 && outDx > 0) return cornerUR;
        if (inDx > 0 && outDy > 0) return cornerUL;
        if (inDy > 0 && outDx > 0) return cornerLR;
        if (inDx < 0 && outDy < 0) return cornerLR;
        if (inDx < 0 && outDy > 0) return cornerUR;
        if (inDy > 0 && outDx < 0) return cornerLL;
        if (inDy < 0 && outDx < 0) return cornerUL;
        return cornerUR;
    }

    private void drawPathSegment(float x1, float y1, float x2, float y2, float width) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        boolean horizontal = Math.abs(dx) > Math.abs(dy);
        Texture tex = horizontal ? pathHorizontal : pathVertical;
        int tileSize = horizontal ? tex.getWidth() : tex.getHeight();
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        int tiles = Math.max(1, (int) (length / tileSize));

        if (horizontal) {
            float stepX = dx / tiles;
            for (int t = 0; t < tiles; t++) {
                batch.draw(tex, x1 + stepX * t, y1 - width / 2f, Math.abs(stepX) + 1, width);
            }
        } else {
            float stepY = dy / tiles;
            for (int t = 0; t < tiles; t++) {
                float drawY = Math.min(y1 + stepY * t, y1 + stepY * (t + 1));
                batch.draw(tex, x1 - width / 2f, drawY, width, Math.abs(stepY) + 1);
            }
        }
    }
}
