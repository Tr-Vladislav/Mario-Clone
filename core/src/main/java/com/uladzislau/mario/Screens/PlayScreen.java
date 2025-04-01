package com.uladzislau.mario.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.uladzislau.mario.Mario;
import com.uladzislau.mario.Scenes.Hud;
import com.uladzislau.mario.Sprites.Enemies.Enemy;
import com.uladzislau.mario.Sprites.Items.Item;
import com.uladzislau.mario.Sprites.Items.ItemDef;
import com.uladzislau.mario.Sprites.Items.Mushroom;
import com.uladzislau.mario.Sprites.MarioSprite;
import com.uladzislau.mario.Tools.B2WorldCreator;
import com.uladzislau.mario.Tools.WorldContactListener;

import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class PlayScreen implements Screen {

    private Mario game;
    private TextureAtlas atlas;

    private OrthographicCamera gameCamera;
    private Viewport gamePort;
    private Hud hud;

    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    private World world;
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;

    private MarioSprite player;
    private Music music;

    public Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;

    public PlayScreen(Mario game) {
        this.game = game;

        atlas = new TextureAtlas("Mario_and_Enemies.pack");


        gameCamera = new OrthographicCamera();
        gamePort = new FitViewport(Mario.V_WIDTH /  Mario.PPM, Mario.V_HEIGHT / Mario.PPM, gameCamera);

        hud = new Hud(game.batch);

        mapLoader = new TmxMapLoader();
        map = mapLoader.load("level1.tmx");

        renderer = new OrthogonalTiledMapRenderer(map, 1 / Mario.PPM);
        gameCamera.position.set(Mario.V_WIDTH/2/Mario.PPM, Mario.V_HEIGHT/2/Mario.PPM, 0);

        world = new World(new Vector2(0,-10), true);
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        player = new MarioSprite(this);

        world.setContactListener(new WorldContactListener());
        music = Mario.assetManager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        music.play();

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();

    }
    public void spawnItem(ItemDef itemDef){
        itemsToSpawn.add(itemDef);
    }

    public void handleSpawningItems(){
        if(!itemsToSpawn.isEmpty()){
            ItemDef idef = itemsToSpawn.poll();
            if(idef.type == Mushroom.class){
                items.add(new Mushroom(this, idef.position.x, idef.position.y));
            }
        }
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }
    @Override
    public void show() {

    }

    public boolean gameOver(){
        if(player.currentState == MarioSprite.State.DEAD && player.getStateTimer() > 3){
            return true;
        }
        return false;
    }

    public void update(float dt){
        handleInput(dt);
        handleSpawningItems();

        world.step(1/60F, 6, 2);

        player.update(dt);
        for(Enemy enemy: creator.getEnemies()) {
            enemy.update(dt);
            if (enemy.getX() < player.getX() +224/Mario.PPM)
                enemy.b2body.setActive(true);
        }

        for(Item item: items){
            item.update(dt);

        }
        hud.update(dt);

        if(player.currentState != MarioSprite.State.DEAD)
            gameCamera.position.x = player.b2body.getPosition().x;

        gameCamera.update();
        renderer.setView(gameCamera);
    }

    public void handleInput(float dt){
        if(player.currentState != MarioSprite.State.DEAD) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
                player.b2body.applyLinearImpulse(new Vector2(0, 4f), player.b2body.getWorldCenter(), true);
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2body.getLinearVelocity().x < 2)
                player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2body.getLinearVelocity().x > -2)
                player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);
        }

    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        b2dr.render(world, gameCamera.combined);

        game.batch.setProjectionMatrix(gameCamera.combined);
        game.batch.begin();
        player.draw(game.batch);
        for(Enemy enemy: creator.getEnemies())
            enemy.draw(game.batch);
        for(Item item: items){
            item.draw(game.batch);
        }

        game.batch.end();

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if(gameOver()){
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    public TiledMap getMap() {
        return map;
    }
    public World getWorld() {
        return world;
    }



    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        b2dr.dispose();
        world.dispose();
        hud.dispose();

    }
}
