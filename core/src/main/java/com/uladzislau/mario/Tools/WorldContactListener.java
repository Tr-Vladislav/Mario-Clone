package com.uladzislau.mario.Tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.*;
import com.uladzislau.mario.Mario;
import com.uladzislau.mario.Sprites.Enemies.Enemy;
import com.uladzislau.mario.Sprites.Items.Item;
import com.uladzislau.mario.Sprites.MarioSprite;
import com.uladzislau.mario.Sprites.TileObjects.InteractiveTileObject;

public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        int cDef = fixtureA.getFilterData().categoryBits | fixtureB.getFilterData().categoryBits;

        switch (cDef){
            case Mario.MARIO_HEAD_BIT | Mario.BRICK_BIT:
                if(fixtureA.getFilterData().categoryBits == Mario.MARIO_HEAD_BIT)
                    ((InteractiveTileObject) fixtureB.getUserData()).onHeadHit((MarioSprite) fixtureA.getUserData());
                else
                    ((InteractiveTileObject) fixtureA.getUserData()).onHeadHit((MarioSprite) fixtureB.getUserData());
                break;
            case Mario.MARIO_HEAD_BIT | Mario.COIN_BIT:
                if(fixtureA.getFilterData().categoryBits == Mario.MARIO_HEAD_BIT)
                    ((InteractiveTileObject) fixtureB.getUserData()).onHeadHit((MarioSprite) fixtureA.getUserData());
                else
                    ((InteractiveTileObject) fixtureA.getUserData()).onHeadHit((MarioSprite) fixtureB.getUserData());
                break;
            case Mario.ENEMY_HEAD_BIT | Mario.MARIO_BIT:
                if(fixtureA.getFilterData().categoryBits == Mario.ENEMY_HEAD_BIT)
                    ((Enemy)fixtureA.getUserData()).hitOnHead((MarioSprite) fixtureB.getUserData());
                else
                    ((Enemy)fixtureB.getUserData()).hitOnHead((MarioSprite) fixtureA.getUserData());
                break;
            case Mario.ENEMY_BIT | Mario.OBJECT_BIT:
                if(fixtureA.getFilterData().categoryBits == Mario.ENEMY_BIT)
                    ((Enemy)fixtureA.getUserData()).reverseVelocity(true, false);
               else
                    ((Enemy)fixtureB.getUserData()).reverseVelocity(true, false);
                break;
            case Mario.MARIO_BIT | Mario.ENEMY_BIT:
                if(fixtureA.getFilterData().categoryBits == Mario.MARIO_HEAD_BIT)
                    ((MarioSprite) fixtureB.getUserData()).hit((Enemy)fixtureA.getUserData());
                else
                    ((MarioSprite) fixtureA.getUserData()).hit((Enemy)fixtureB.getUserData());
                break;
            case Mario.ENEMY_BIT | Mario.ENEMY_BIT:
                ((Enemy)fixtureA.getUserData()).onEnemyHit((Enemy)fixtureB.getUserData());
                ((Enemy)fixtureB.getUserData()).onEnemyHit((Enemy)fixtureA.getUserData());
                break;
            case Mario.ITEM_BIT | Mario.OBJECT_BIT:
                if(fixtureA.getFilterData().categoryBits == Mario.ENEMY_BIT)
                    ((Item)fixtureA.getUserData()).reverseVelocity(true, false);
                else
                    ((Item)fixtureB.getUserData()).reverseVelocity(true, false);
                break;
            case Mario.ITEM_BIT | Mario.MARIO_BIT:
                if(fixtureA.getFilterData().categoryBits == Mario.ENEMY_BIT)
                    ((Item)fixtureA.getUserData()).use((MarioSprite) fixtureB.getUserData());
                else
                    ((Item)fixtureB.getUserData()).use((MarioSprite) fixtureA.getUserData());
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }
}
