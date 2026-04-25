package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Dart implements Pool.Poolable {
    public final Vector2 position;
    public final Vector2 direction;
    public boolean alive;
    public float speed;
    public float angle;

    public Dart() {
        this.position = new Vector2();
        this.direction = new Vector2();
        this.alive = false;
        this.speed = 300f;
    }

    public void init(float startX, float startY, float targetX, float targetY) {
        position.set(startX, startY);
        float dx = targetX - startX;
        float dy = targetY - startY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            direction.set(dx / dist, dy / dist);
        } else {
            direction.set(1, 0);
        }
        alive = true;
        speed = 300f;
        angle = (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    public void update(float delta) {
        if (!alive) return;
        position.x += direction.x * speed * delta;
        position.y += direction.y * speed * delta;

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        if (position.x < -20 || position.x > w + 20 || position.y < -20 || position.y > h + 20) {
            alive = false;
        }
    }

    @Override
    public void reset() {
        position.set(0, 0);
        direction.set(0, 0);
        alive = false;
        speed = 300f;
        angle = 0f;
    }
}
