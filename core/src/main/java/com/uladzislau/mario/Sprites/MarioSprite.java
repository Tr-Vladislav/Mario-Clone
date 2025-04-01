package com.uladzislau.mario.Sprites;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.uladzislau.mario.Mario;
import com.uladzislau.mario.Screens.PlayScreen;
import com.uladzislau.mario.Sprites.Enemies.Enemy;
import com.uladzislau.mario.Sprites.Enemies.Turtle;

public class MarioSprite extends Sprite {
    public enum State {FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD}
    public State currentState;
    public State previousState;

    public World world;
    public Body b2body;
    private TextureRegion marioStand;
    private Animation marioRun;
    private TextureRegion marioJump;
    private TextureRegion marioDead;
    private TextureRegion bigMarioStand;
    private Animation<TextureRegion>  bigMarioRun;
    private TextureRegion bigMarioJump;
    private Animation<TextureRegion>  marioGrow;

    private float stateTimer;
    private boolean runningRight;
    private boolean marioIsBig;
    private boolean runGrowAnimation;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;
    private boolean marioIsDead;

    public MarioSprite(PlayScreen screen){
        this.world = screen.getWorld();

        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for(int i = 1; i < 4; i++){
            frames.add(new TextureRegion(screen.getAtlas().findRegion("Mario GFX/little_mario"), i*16, 0, 16, 16));
        }
        marioRun = new Animation(0.1f, frames);
        frames.clear();
        for(int i = 1; i < 4; i++){
            frames.add(new TextureRegion(screen.getAtlas().findRegion("Mario GFX/big_mario"), i*16, 0, 16, 32));
        }
        bigMarioRun = new Animation(0.1f, frames);
        frames.clear();

        frames.add(new TextureRegion(screen.getAtlas().findRegion("Mario GFX/big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("Mario GFX/big_mario"), 0, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("Mario GFX/big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("Mario GFX/big_mario"), 0, 0, 16, 32));
        marioGrow = new Animation(0.2f, frames);


        marioJump = new TextureRegion(screen.getAtlas().findRegion("Mario GFX/little_mario"), 80, 0, 16, 16);
        bigMarioJump = new TextureRegion(screen.getAtlas().findRegion("Mario GFX/big_mario"), 80, 0, 16, 32);
        frames.clear();

        defineMario();
        marioStand = new TextureRegion(screen.getAtlas().findRegion("Mario GFX/little_mario"), 0, 0, 16, 16);
        bigMarioStand = new TextureRegion(screen.getAtlas().findRegion("Mario GFX/big_mario"), 0, 0, 16, 32);

        marioDead = new TextureRegion(screen.getAtlas().findRegion("Mario GFX/little_mario"), 96, 0, 16, 16);
        setBounds(0, 0, 16/ Mario.PPM, 16/ Mario.PPM);
        setRegion(marioStand);
    }

    public void update(float dt){
        if(marioIsBig)
            setPosition(b2body.getPosition().x - getWidth()/2, b2body.getPosition().y - getHeight()/2 -6/Mario.PPM);
        else
            setPosition(b2body.getPosition().x - getWidth()/2, b2body.getPosition().y - getHeight()/2);
        setRegion(getFrame(dt));

        if(timeToDefineBigMario){
            defineBigMario();
        }
        if(timeToRedefineMario){
            redefineMario();
        }
    }
    public void hit(Enemy enemy){
        if (enemy instanceof Turtle && ((Turtle)enemy).getCurrentState() == Turtle.State.STANDING_SHELL) {
            ((Turtle)enemy).kick(this.getX() <= enemy.getX() ? Turtle.KICK_RIGHT_SPEED : Turtle.KICK_LEFT_SPEED);

        }else {
            if (marioIsBig) {
                marioIsBig = false;
                timeToRedefineMario = true;
                setBounds(getX(), getY(), getWidth(), getHeight() / 2);
                Mario.assetManager.get("audio/sounds/powerdown.wav", Sound.class).play();
            } else {
                Mario.assetManager.get("audio/music/mario_music.ogg", Music.class).stop();
                Mario.assetManager.get("audio/sounds/mariodie.wav", Sound.class).play();
                marioIsDead = true;
                Filter filter = new Filter();
                filter.maskBits = Mario.NOTHING_BIT;
                for (Fixture fixture : b2body.getFixtureList()) {
                    fixture.setFilterData(filter);
                }
                b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);

            }
        }
    }
    public void redefineMario(){
        Vector2 position = b2body.getPosition();
        world.destroyBody(b2body);


        BodyDef bdef = new BodyDef();
        bdef.position.set(position);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6/ Mario.PPM);

        fdef.filter.categoryBits = Mario.MARIO_BIT;
        fdef.filter.maskBits =  Mario.GROUND_BIT |
            Mario.BRICK_BIT |
            Mario.COIN_BIT |
            Mario.ENEMY_BIT |
            Mario.OBJECT_BIT |
            Mario.ENEMY_HEAD_BIT |
            Mario.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / Mario.PPM, 6 / Mario.PPM), new Vector2(2 / Mario.PPM, 6 / Mario.PPM));
        fdef.filter.categoryBits = Mario.MARIO_HEAD_BIT;

        fdef.shape = head;
        fdef.isSensor = true;


        b2body.createFixture(fdef).setUserData(this);

        timeToRedefineMario = false;
    }

    public boolean isDead(){
        return marioIsDead;
    }

    public float getStateTimer(){
        return stateTimer;
    }

    public State getState(){
        if(marioIsDead){
            return State.DEAD;
        }
        else if(runGrowAnimation){
            return State.GROWING;
        }
        else if(b2body.getLinearVelocity().y > 0 || (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING)){
            return State.JUMPING;
        }
        if(b2body.getLinearVelocity().y < 0){
            return State.FALLING;
        }
        if(b2body.getLinearVelocity().x != 0){
            return State.RUNNING;
        }

        return State.STANDING;
    }
    public TextureRegion getFrame(float dt){
        currentState = getState();

        TextureRegion region;
        switch (currentState){
            case DEAD:
                region = marioDead;
                break;
            case GROWING:
                region = (TextureRegion) marioGrow.getKeyFrame(stateTimer);
                if(marioGrow.isAnimationFinished(stateTimer)){
                    runGrowAnimation = false;
                }
                break;
            case JUMPING:
                region = marioIsBig ? bigMarioJump: marioJump;
                break;
            case RUNNING:
                region = (TextureRegion) (marioIsBig ? bigMarioRun.getKeyFrame(stateTimer, true) :marioRun.getKeyFrame(stateTimer, true));
                break;
            case FALLING:
            case STANDING:
            default:
                region = marioIsBig? bigMarioStand : marioStand;
                break;
        }
        if((b2body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX() ){
            region.flip(true, false);
            runningRight = false;
        }
        else if((b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()){
            region.flip(true, false);
            runningRight = true;
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    public void grow(){
        runGrowAnimation = true;
        marioIsBig = true;
        timeToDefineBigMario = true;
        setBounds(getX(), getY(), getWidth(), getHeight()*2);
        Mario.assetManager.get("audio/sounds/powerup.wav", Sound.class).play();
    }

    public void defineBigMario(){

        Vector2 currentposition = b2body.getPosition();
        world.destroyBody(b2body);

        BodyDef bdef = new BodyDef();
        bdef.position.set(currentposition.add(0,10/Mario.PPM));
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6/ Mario.PPM);

        fdef.filter.categoryBits = Mario.MARIO_BIT;
        fdef.filter.maskBits =  Mario.GROUND_BIT |
            Mario.BRICK_BIT |
            Mario.COIN_BIT |
            Mario.ENEMY_BIT |
            Mario.OBJECT_BIT |
            Mario.ENEMY_HEAD_BIT |
            Mario.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);
        shape.setPosition(new Vector2(0, -14 / Mario.PPM));
        b2body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / Mario.PPM, 6 / Mario.PPM), new Vector2(2 / Mario.PPM, 6 / Mario.PPM));
        fdef.filter.categoryBits = Mario.MARIO_HEAD_BIT;

        fdef.shape = head;
        fdef.isSensor = true;


        b2body.createFixture(fdef).setUserData(this);
        timeToDefineBigMario = false;
    }

    public void defineMario(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(32/ Mario.PPM, 32/ Mario.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6/ Mario.PPM);

        fdef.filter.categoryBits = Mario.MARIO_BIT;
        fdef.filter.maskBits =  Mario.GROUND_BIT |
            Mario.BRICK_BIT |
            Mario.COIN_BIT |
            Mario.ENEMY_BIT |
            Mario.OBJECT_BIT |
            Mario.ENEMY_HEAD_BIT |
            Mario.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / Mario.PPM, 6 / Mario.PPM), new Vector2(2 / Mario.PPM, 6 / Mario.PPM));
        fdef.filter.categoryBits = Mario.MARIO_HEAD_BIT;

        fdef.shape = head;
        fdef.isSensor = true;


        b2body.createFixture(fdef).setUserData(this);

    }
    public boolean isBig() {
        return marioIsBig;
    }

}
