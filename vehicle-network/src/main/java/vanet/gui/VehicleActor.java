package vanet.gui;

import globals.AttackerEnum;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;

import globals.Resources;


public class VehicleActor extends Actor {
    private ShapeRenderer renderer = new ShapeRenderer();
    private final int WIDTH = 35;
    private final int HEIGHT = 35;
    private AttackerEnum aType;
    private String name;
	private BitmapFont font;

    public VehicleActor(float x, float y, String name, AttackerEnum aType) {
        setWidth(WIDTH);
        setHeight(HEIGHT);
        updatePosition(x, y);
        this.aType = aType;
        this.name = name;

        font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.scale(1);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        font.draw(batch, name, getX(), getY());

        batch.end();

        renderer.setProjectionMatrix(batch.getProjectionMatrix());
        renderer.setTransformMatrix(batch.getTransformMatrix());
        renderer.translate(getX(), getY(), 0);

        renderer.begin(ShapeType.Filled);
        if (aType == AttackerEnum.NO_ATTACKER)
            renderer.setColor(Color.GREEN);
        else
            renderer.setColor(Color.RED);
        renderer.rect(0, 0, getWidth(), getHeight());
        renderer.end();

        batch.begin();
    }

    public void updatePosition(float x, float y) {
        setX(x);
        setY(Resources.DEFAULT_MAP_HEIGHT - y - getHeight()); // Makes top/left the 0/0
    }
}