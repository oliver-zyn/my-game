package io.github.some_example_name;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Bloon implements Pool.Poolable {
    private static Sound popSound;

    public static void setPopSound(Sound sound) {
        popSound = sound;
    }

    public final Vector2 position;
    public boolean alive;
    public float speed;
    public int currentWaypoint;
    public int hp;
    public Vector2[] waypoints;

    private float spawnTimer;
    private boolean invulnerable;

    public Bloon() {
        this.position = new Vector2();
        this.alive = false;
        this.speed = 100f;
        this.currentWaypoint = 0;
        this.hp = 1;
        this.spawnTimer = 0f;
        this.invulnerable = false;
    }

    public void init(Vector2[] waypoints, float speed) {
        this.waypoints = waypoints;
        this.speed = speed;
        this.currentWaypoint = 0;
        this.hp = 1;
        this.alive = true;
        this.spawnTimer = 0f;
        this.invulnerable = true;
        if (waypoints.length > 0) {
            this.position.set(waypoints[0]);
        }
    }

    public void update(float delta) {
        if (!alive || waypoints == null) return;

        if (invulnerable) {
            spawnTimer += delta;
            if (spawnTimer >= 0.5f) {
                invulnerable = false;
            }
        }

        if (currentWaypoint >= waypoints.length) {
            alive = false;
            return;
        }

        Vector2 target = waypoints[currentWaypoint];
        float dx = target.x - position.x;
        float dy = target.y - position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 2f) {
            currentWaypoint++;
        } else {
            float move = speed * delta;
            position.x += (dx / dist) * move;
            position.y += (dy / dist) * move;
        }
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public float getSpawnTimer() {
        return spawnTimer;
    }

    public void hit() {
        if (invulnerable) return;
        hp--;
        if (hp <= 0) {
            alive = false;
            if (popSound != null) {
                popSound.play(0.6f);
            }
        }
    }

    @Override
    public void reset() {
        position.set(0, 0);
        alive = false;
        speed = 100f;
        currentWaypoint = 0;
        hp = 1;
        waypoints = null;
        spawnTimer = 0f;
        invulnerable = false;
    }
}
