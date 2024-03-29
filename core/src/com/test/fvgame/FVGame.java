package com.test.fvgame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.codeandweb.physicseditor.PhysicsShapeCache;

import java.util.HashMap;
import java.util.Random;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.*;

public class FVGame extends ApplicationAdapter {

	static final float STEP_TIME = 1f / 60f;
	static final int VELOCITY_ITERATIONS = 6;
	static final int POSITION_ITERATIONS = 2;
	static final float SCALE = 0.05f;
	static final int COUNT = 10;

	float accumulator = 0;

	final HashMap<String, Sprite> sprites = new HashMap<>();
	SpriteBatch batch;
	TextureAtlas textureAtlas;

	Body banana;
	Body ground;
	Body[] fruitBodies = new Body[COUNT];
	String[] names = new String[COUNT];

	OrthographicCamera camera;
	ExtendViewport viewport;

	World world;

	PhysicsShapeCache physicsBodies;

	Box2DDebugRenderer debugRenderer;
	
	@Override
	public void create () {
		Box2D.init();
		debugRenderer = new Box2DDebugRenderer();

		world = new World(new Vector2(0, -60	), true);
		physicsBodies = new PhysicsShapeCache("physics.xml");
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(50, 50, camera);
		batch = new SpriteBatch();
		textureAtlas = new TextureAtlas("sprites.txt");
		addSprites();

		generateFruit();

	}

	@Override
	public void render () {


		Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1);
//		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stepWorld();

		batch.begin();

		for(int i = 0; i < fruitBodies.length; i++){
			Body body = fruitBodies[i];
			String name = names[i];

			Vector2 position = body.getPosition();
			float degrees = (float) Math.toDegrees(body.getAngle());
			drawSprite(name, position.x, position.y, degrees);
		}

		batch.end();

		debugRenderer.render(world, camera.combined);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		batch.setProjectionMatrix(camera.combined);
		createGround();
	}

	@Override
	public void dispose () {
		batch.dispose();

		world.dispose();

		textureAtlas.dispose();

		debugRenderer.dispose();
	}

	private void addSprites(){
		Array<AtlasRegion> regions = textureAtlas.getRegions();

		for(AtlasRegion region : regions){
			Sprite sprite = textureAtlas.createSprite(region.name);

			float wigth = sprite.getWidth() * SCALE;
			float height = sprite.getHeight() * SCALE;

			sprite.setSize(wigth, height);
			sprite.setOrigin(0, 0 );

			sprites.put(region.name, sprite);
		}
	}

	private void drawSprite(String name, float x, float y, float degrees){
		Sprite sprite = sprites.get(name);

		sprite.setPosition(x, y);
		sprite.setRotation(degrees);
		sprite.setOrigin(0f, 0f);
		sprite.draw(batch);
	}

	private void stepWorld(){
		float delta = Gdx.graphics.getDeltaTime();

		accumulator += Math.min(delta, 0.25f);
		if (accumulator > STEP_TIME){
			accumulator -= STEP_TIME;

			world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
		}
	}

	private Body createBody(String name, float x, float y, float rotation){
		Body body = physicsBodies.createBody(name, world, SCALE, SCALE);
		body.setTransform(x, y, rotation);
		return body;
	}

	private void createGround(){
		if (ground != null){ world.destroyBody(ground);}

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 1;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(camera.viewportWidth, 1);

		fixtureDef.shape = shape;

		ground = world.createBody(bodyDef);
		ground.createFixture(fixtureDef);
		ground.setTransform(0,0,0);
		shape.dispose();
	}

	private void generateFruit(){
		String[] fruitNames = new String[]{"banana", "cherries", "orange"};

		Random random = new Random();

		for (int i = 0; i < fruitBodies.length; i++){
			String name = fruitNames[random.nextInt(fruitNames.length)];

			float x = random.nextFloat() * 50;
			float y = random.nextFloat() * 50 + 50;
			names[i] = name;
			fruitBodies[i] = createBody(name, x, y, 0);
		}
	}

}
