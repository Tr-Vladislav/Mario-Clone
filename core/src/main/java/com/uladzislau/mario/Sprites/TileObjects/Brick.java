package com.uladzislau.mario.Sprites.TileObjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;
import com.uladzislau.mario.Mario;
import com.uladzislau.mario.Scenes.Hud;
import com.uladzislau.mario.Screens.PlayScreen;
import com.uladzislau.mario.Sprites.MarioSprite;

public class Brick extends InteractiveTileObject {
    public Brick(PlayScreen screen, MapObject object) {
        super(screen, object);
        fixture.setUserData(this);
        setCategoryFilter(Mario.BRICK_BIT);
    }

    @Override
    public void onHeadHit(MarioSprite mario) {
        if (mario.isBig()) {
            setCategoryFilter(Mario.DESTROYED_BIT);
            getCell().setTile(null);
            Hud.addScore(200);
            Mario.assetManager.get("audio/sounds/breakblock.wav", Sound.class).play();
        }
        Mario.assetManager.get("audio/sounds/bump.wav", Sound.class).play();
    }
}
