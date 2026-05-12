package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class Scenery {
    private final Texture grass;
    private final Array<Decoration> placed = new Array<Decoration>();
    private final float worldWidth;
    private final float worldHeight;

    public Scenery(Texture grass, Texture[] decorations,
                   Vector2[] waypoints, float pathWidth,
                   float worldWidth, float worldHeight, int count) {
        this.grass = grass;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        Random rng = new Random(42);
        int tries = 0;
        float minSpacing = 60f;
        while (placed.size < count && tries < count * 30) {
            tries++;
            float x = rng.nextFloat() * worldWidth;
            float y = rng.nextFloat() * worldHeight;
            if (isNearPath(x, y, waypoints, pathWidth + 35f)) continue;
            if (overlapsExisting(x, y, minSpacing)) continue;
            Texture tex = decorations[rng.nextInt(decorations.length)];
            placed.add(new Decoration(x, y, tex));
        }
    }

    public void draw(SpriteBatch batch) {
        int gw = grass.getWidth();
        int gh = grass.getHeight();
        for (float y = 0; y < worldHeight; y += gh) {
            for (float x = 0; x < worldWidth; x += gw) {
                batch.draw(grass, x, y);
            }
        }
        for (int i = 0; i < placed.size; i++) {
            Decoration d = placed.get(i);
            batch.draw(d.texture,
                d.x - d.texture.getWidth() / 2f,
                d.y - d.texture.getHeight() / 2f);
        }
    }

    private boolean overlapsExisting(float x, float y, float minDist) {
        for (int i = 0; i < placed.size; i++) {
            Decoration d = placed.get(i);
            float dx = d.x - x;
            float dy = d.y - y;
            if (dx * dx + dy * dy < minDist * minDist) return true;
        }
        return false;
    }

    private boolean isNearPath(float px, float py, Vector2[] waypoints, float radius) {
        for (int i = 0; i < waypoints.length - 1; i++) {
            if (distanceToSegment(px, py, waypoints[i], waypoints[i + 1]) < radius) {
                return true;
            }
        }
        return false;
    }

    private float distanceToSegment(float px, float py, Vector2 a, Vector2 b) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float lenSq = dx * dx + dy * dy;
        if (lenSq == 0) {
            float ddx = px - a.x;
            float ddy = py - a.y;
            return (float) Math.sqrt(ddx * ddx + ddy * ddy);
        }
        float t = ((px - a.x) * dx + (py - a.y) * dy) / lenSq;
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        float closestX = a.x + t * dx;
        float closestY = a.y + t * dy;
        float ddx = px - closestX;
        float ddy = py - closestY;
        return (float) Math.sqrt(ddx * ddx + ddy * ddy);
    }

    private static class Decoration {
        final float x, y;
        final Texture texture;

        Decoration(float x, float y, Texture texture) {
            this.x = x;
            this.y = y;
            this.texture = texture;
        }
    }
}
