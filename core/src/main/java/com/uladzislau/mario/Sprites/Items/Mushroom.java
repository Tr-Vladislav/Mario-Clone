package com.uladzislau.mario.Sprites.Items;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.uladzislau.mario.Mario;
import com.uladzislau.mario.Screens.PlayScreen;
import com.uladzislau.mario.Sprites.MarioSprite;

public class Mushroom extends Item {
    public Mushroom(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        setRegion(screen.getAtlas().findRegion("Mario GFX/mushroom"), 0,0,16,16);
        velocity = new Vector2(1f, 0);
    }

    @Override
    public void defineItem() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);


        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6/ Mario.PPM);
        fdef.filter.categoryBits = Mario.ITEM_BIT;
        fdef.filter.maskBits = Mario.MARIO_BIT|
            Mario.OBJECT_BIT|
            Mario.GROUND_BIT|
            Mario.COIN_BIT |
            Mario.BRICK_BIT;

        fdef.shape = shape;
        body.createFixture(fdef).setUserData(this);

    }

    @Override
    public void use(MarioSprite mario) {
        destroy();
        mario.grow();
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        setPosition(body.getPosition().x - getWidth()/2, body.getPosition().y - getHeight()/2);
        velocity.y = body.getLinearVelocity().y;
        body.setLinearVelocity(velocity);
    }
}
