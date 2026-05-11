package io.github.some_example_name;

import com.badlogic.gdx.math.Vector2;

public class FloatingScore {
    public final Vector2 position;
    public final String text;
    public float timer;
    public final float duration;
    public boolean alive;

    public FloatingScore(float x, float y, String text, float duration) {
        this.position = new Vector2(x, y);
        this.text = text;
        this.timer = 0f;
        this.duration = duration;
        this.alive = true;
    }

    public void update(float delta) {
        if (!alive) return;
        timer += delta;
        position.y += 40f * delta;
        if (timer >= duration) {
            alive = false;
        }
    }

    public float getAlpha() {
        return 1f - (timer / duration);
    }
}
