/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.caboodlejump.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.caboodlejump.Core;
import com.ray3k.caboodlejump.State;

public class MenuState extends State {
    private Stage stage;
    private Skin skin;
    private Table root;

    public MenuState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/ui/caboodle-jump-ui.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        Gdx.input.setInputProcessor(stage);
        
        Image bg = new Image(skin.getTiledDrawable("bg"));
        bg.setFillParent(true);
        stage.addActor(bg);
        
        createMenu();
    }
    
    private void createMenu() {
        FileHandle fileHandle = Gdx.files.local(Core.DATA_PATH + "/data.json");
        JsonReader reader = new JsonReader();
        JsonValue val = reader.parse(fileHandle);
        
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Label label = new Label(val.getString("title"), skin, "title");
        label.setAlignment(Align.center);
        root.add(label).padBottom(50.0f);
        
        root.defaults().space(30.0f).padLeft(25.0f);
        root.row();
        TextButton textButtton = new TextButton("play", skin);
        root.add(textButtton).expandY().bottom();
        
        textButtton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/point.wav", Sound.class).play(.25f);
                showCharacterSelect();
            }
        });
        
        root.row();
        textButtton = new TextButton("Quit", skin);
        root.add(textButtton).expandY().top();
        
        textButtton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/point.wav", Sound.class).play(.25f);
                Gdx.app.exit();
            }
        });
    }
    
    private void showCharacterSelect() {
        ((GameState)getCore().getStateManager().getState("game")).setSelectedCharacter("rex");
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                super.result(object);
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/point.wav", Sound.class).play(.25f);
                showStageOptions();
            }
            
        };
        
        dialog.text("Choose a character...");
        
        ButtonGroup buttonGroup = new ButtonGroup();
        dialog.getContentTable().row();
        
        ImageTextButton imageTextButton = new ImageTextButton("Jumpasaurus Rex", skin, "rex");
        buttonGroup.add(imageTextButton);
        dialog.getContentTable().add(imageTextButton).growX();
        
        imageTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                ((GameState)getCore().getStateManager().getState("game")).setSelectedCharacter("rex");
            }
        });
        
        dialog.getContentTable().row();
        imageTextButton = new ImageTextButton("Jump Bud", skin, "rose");
        buttonGroup.add(imageTextButton);
        dialog.getContentTable().add(imageTextButton).growX();
        
        imageTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                ((GameState)getCore().getStateManager().getState("game")).setSelectedCharacter("rose");
            }
        });
        
        dialog.button("OK");
        
        dialog.show(stage);
    }
    
    private void showStageOptions() {
        final GameState gameState = (GameState) getCore().getStateManager().getState("game");
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                super.result(object);
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/point.wav", Sound.class).play(.25f);
                getCore().getStateManager().loadState("game");
            }
            
        };
        dialog.setFillParent(true);
        
        Label label = new Label("Stage settings...", skin, "title");
        dialog.getContentTable().add(label).colspan(2);
        
        dialog.getContentTable().row();
        label = new Label("Platform Spacing", skin);
        dialog.getContentTable().add(label).colspan(2);
        
        dialog.getContentTable().row();
        final Slider spacingSlider = new Slider(50, 800, 1, false, skin);
        spacingSlider.setValue(gameState.getPlatformSpacing());
        dialog.getContentTable().add(spacingSlider).growX().padLeft(50.0f);
        
        final Label spacingLabel = new Label(Integer.toString((int) spacingSlider.getValue()), skin);
        dialog.getContentTable().add(spacingLabel).width(100.0f);
        
        spacingSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                spacingLabel.setText(Integer.toString((int) spacingSlider.getValue()));
                gameState.setPlatformSpacing(spacingSlider.getValue());
            }
        });
        
        dialog.getContentTable().row();
        label = new Label("Gravity", skin);
        dialog.getContentTable().add(label).colspan(2);
        
        dialog.getContentTable().row();
        final Slider gravitySlider = new Slider(100, 2000, 1, false, skin);
        gravitySlider.setValue(gameState.getGravity());
        dialog.getContentTable().add(gravitySlider).growX().padLeft(50.0f);
        
        final Label gravityLabel = new Label(Integer.toString((int) gravitySlider.getValue()), skin);
        dialog.getContentTable().add(gravityLabel).width(100.0f);
        
        gravitySlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                gravityLabel.setText(Integer.toString((int) gravitySlider.getValue()));
                gameState.setGravity(gravitySlider.getValue());
            }
        });
        
        dialog.getContentTable().row();
        label = new Label("Jump Power", skin);
        dialog.getContentTable().add(label).colspan(2);
        
        dialog.getContentTable().row();
        final Slider jumpPowerSlider = new Slider(50, 2000, 1, false, skin);
        jumpPowerSlider.setValue(gameState.getJumpPower());
        dialog.getContentTable().add(jumpPowerSlider).growX().padLeft(50.0f);
        
        final Label jumpPowerLabel = new Label(Integer.toString((int) jumpPowerSlider.getValue()), skin);
        dialog.getContentTable().add(jumpPowerLabel).width(100.0f);
        
        jumpPowerSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                jumpPowerLabel.setText(Integer.toString((int) jumpPowerSlider.getValue()));
                gameState.setJumpPower(jumpPowerSlider.getValue());
            }
        });
        
        dialog.button("OK");
        
        dialog.show(stage);
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void act(float delta) {
        stage.act(delta);
    }

    @Override
    public void dispose() {
        
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}