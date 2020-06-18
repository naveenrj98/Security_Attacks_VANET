package vanet.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;

import globals.map.DefaultMap;


import globals.map.Waypoint;


public class VanetGUI implements ApplicationListener {
	private Stage stage;
	private Texture bgImage;
	private BitmapFont font;

	public void addActor(Actor a) {
		stage.addActor(a);
	}

	public void removeActor(Actor a) {
		// @Check
		a.remove();
	}
	@Override
	public void create () {
	    Viewport viewport = new ScalingViewport(Scaling.fit, 1800, 1800, new OrthographicCamera());
		stage = new Stage(viewport);
    	Gdx.input.setInputProcessor(stage);

		font = new BitmapFont();
		font.setColor(Color.BLACK);
		font.scale(1);

		// waypoints
		for(Waypoint w: DefaultMap.getInstance().getWaypoints()) {
			addActor(new WaypointActor(w, font));
		}

		// bgr image
		bgImage = new Texture(Gdx.files.internal("bgImage.png"));
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

		stage.getBatch().begin();
		stage.getBatch().draw(bgImage, 0, 0);


		stage.getBatch().end();
		stage.draw();
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}
}
