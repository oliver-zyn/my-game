package io.github.some_example_name;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class GameWorld {
    public final Vector2[] waypoints;

    public final Array<Bloon> activeBloons = new Array<Bloon>();
    public final Array<Dart> activeDarts = new Array<Dart>();
    public final Array<Tower> towers = new Array<Tower>();
    public final Array<FloatingScore> floatingScores = new Array<FloatingScore>();

    public final Pool<Bloon> bloonPool = new Pool<Bloon>() {
        @Override
        protected Bloon newObject() {
            return new Bloon();
        }
    };

    public final Pool<Dart> dartPool = new Pool<Dart>() {
        @Override
        protected Dart newObject() {
            return new Dart();
        }
    };

    private float spawnTimer = 0f;
    private float spawnInterval = 1.5f;
    private int bloonsSpawned = 0;
    private int maxBloonsPerWave = 20;

    public int lives = 20;
    public int score = 0;

    public GameWorld(float screenWidth, float screenHeight) {
        float w = screenWidth;
        float h = screenHeight;
        waypoints = new Vector2[] {
            new Vector2(0, h * 0.83f),
            new Vector2(w * 0.21f, h * 0.83f),
            new Vector2(w * 0.21f, h * 0.37f),
            new Vector2(w * 0.47f, h * 0.37f),
            new Vector2(w * 0.47f, h * 0.69f),
            new Vector2(w * 0.73f, h * 0.69f),
            new Vector2(w * 0.73f, h * 0.23f),
            new Vector2(w, h * 0.23f)
        };
    }

    public void update(float delta) {
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval && bloonsSpawned < maxBloonsPerWave) {
            spawnTimer = 0f;
            Bloon bloon = bloonPool.obtain();
            bloon.init(waypoints, 80f);
            activeBloons.add(bloon);
            bloonsSpawned++;
        }

        for (int i = activeBloons.size - 1; i >= 0; i--) {
            Bloon bloon = activeBloons.get(i);
            bloon.update(delta);
            if (!bloon.alive) {
                if (bloon.hp > 0) lives--;
                activeBloons.removeIndex(i);
                bloonPool.free(bloon);
            }
        }

        for (int i = 0; i < towers.size; i++) {
            Tower tower = towers.get(i);
            if (tower.updateAndCanShoot(delta)) {
                Bloon target = tower.findTarget(activeBloons);
                if (target != null) {
                    Dart dart = dartPool.obtain();
                    dart.init(tower.position.x, tower.position.y,
                              target.position.x, target.position.y);
                    activeDarts.add(dart);
                }
            }
        }

        for (int i = activeDarts.size - 1; i >= 0; i--) {
            Dart dart = activeDarts.get(i);
            dart.update(delta);

            if (dart.alive) {
                for (int j = activeBloons.size - 1; j >= 0; j--) {
                    Bloon bloon = activeBloons.get(j);
                    if (!bloon.alive || bloon.isInvulnerable()) continue;
                    if (dart.position.dst(bloon.position) < 20f) {
                        bloon.hit();
                        dart.alive = false;
                        if (!bloon.alive) {
                            score += 10;
                            floatingScores.add(new FloatingScore(
                                bloon.position.x, bloon.position.y, "+10", 1f));
                            activeBloons.removeIndex(j);
                            bloonPool.free(bloon);
                        }
                        break;
                    }
                }
            }

            if (!dart.alive) {
                activeDarts.removeIndex(i);
                dartPool.free(dart);
            }
        }

        for (int i = floatingScores.size - 1; i >= 0; i--) {
            FloatingScore fs = floatingScores.get(i);
            fs.update(delta);
            if (!fs.alive) {
                floatingScores.removeIndex(i);
            }
        }
    }

    public void addTower(float x, float y) {
        towers.add(new Tower(x, y));
    }
}
