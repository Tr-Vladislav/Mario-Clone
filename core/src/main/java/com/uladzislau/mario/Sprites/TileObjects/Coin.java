package com.uladzislau.mario.Sprites.TileObjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.uladzislau.mario.Mario;
import com.uladzislau.mario.Scenes.Hud;
import com.uladzislau.mario.Screens.PlayScreen;
import com.uladzislau.mario.Sprites.Items.ItemDef;
import com.uladzislau.mario.Sprites.Items.Mushroom;
import com.uladzislau.mario.Sprites.MarioSprite;

public class Coin extends InteractiveTileObject {
    private static TiledMapTileSet tileSet;
    private final int BLANK_COIN = 28;
    public Coin(PlayScreen screen, MapObject object) {
        super(screen, object);
        tileSet = map.getTileSets().getTileSet("tileset_gutter");
        fixture.setUserData(this);
        setCategoryFilter(Mario.COIN_BIT);
    }

    @Override
    public void onHeadHit(MarioSprite mario) {
        Gdx.app.log("Coin", "Collision");
        if(getCell().getTile().getId() == BLANK_COIN)
            Mario.assetManager.get("audio/sounds/bump.wav", Sound.class).play();
        else {
            if(object.getProperties().containsKey("mushroom")) {
                screen.spawnItem(new ItemDef(new Vector2(body.getPosition().x, body.getPosition().y + 16 / Mario.PPM), Mushroom.class));
                Mario.assetManager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
            }
            else
                Mario.assetManager.get("audio/sounds/coin.wav", Sound.class).play();
        }
        getCell().setTile(tileSet.getTile(BLANK_COIN));

        Hud.addScore(100);

    }
}
