package vanet.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;

import globals.Resources;
import globals.map.Waypoint;


public class WaypointActor extends Actor {
    private ShapeRenderer renderer = new ShapeRenderer();
    private final int WIDTH = 5;
    private final int HEIGHT = 5;
    Waypoint w;
    BitmapFont font;

    public WaypointActor(Waypoint w, BitmapFont font) {
        setWidth(WIDTH);
        setHeight(HEIGHT);

        this.w = w;
        updatePosition((float)w.getPosition().x, (float)w.getPosition().y);

        this.font = font;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
		font.draw(batch, w.getName(), getX()-10, getY());

        batch.end();

        renderer.setProjectionMatrix(batch.getProjectionMatrix());
        renderer.setTransformMatrix(batch.getTransformMatrix());
        renderer.translate(getX(), getY(), 0);

        renderer.begin(ShapeType.Filled);
        renderer.setColor(Color.BLACK);
        renderer.rect(0, 0, getWidth(), getHeight());
        renderer.end();

        batch.begin();
    }

    public void updatePosition(float x, float y) {
        setX(x);
        setY(Resources.DEFAULT_MAP_HEIGHT - y - getHeight()); // Makes top/left the 0/0
    }
}