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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ray3k.caboodlejump.Core;
import com.ray3k.caboodlejump.EntityManager;
import com.ray3k.caboodlejump.InputManager;
import com.ray3k.caboodlejump.State;
import com.ray3k.caboodlejump.entities.BackgroundEntity;
import com.ray3k.caboodlejump.entities.CameraControllerEntity;
import com.ray3k.caboodlejump.entities.FallingPlatformEntity;
import com.ray3k.caboodlejump.entities.MovingPlatformEntity;
import com.ray3k.caboodlejump.entities.PlatformEntity;
import com.ray3k.caboodlejump.entities.PlayerEntity;

public class GameState extends State {
    private String selectedCharacter;
    private int score;
    private static int highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private Label scoreLabel;
    private EntityManager entityManager;
    private float platformSpacing;
    private float gravity;
    private float jumpPower;
    private CameraControllerEntity cameraControllerEntity;
    private float platformGoalY;
    
    public GameState(Core core) {
        super(core);
        platformSpacing = 400.0f;
        gravity = 1000.0f;
        jumpPower = 1000.0f;
    }
    
    @Override
    public void start() {
        score = 0;
        
        inputManager = new InputManager(); 
        
        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);
        uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiViewport.apply();
        
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        
        gameCamera = new OrthographicCamera();
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameViewport.apply();
        
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/ui/caboodle-jump-ui.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        entityManager = new EntityManager();
        
        createStageElements();
        
        BackgroundEntity backgroundEntity = new BackgroundEntity(this);
        backgroundEntity.setWidth(Gdx.graphics.getWidth());
        backgroundEntity.setHeight(Gdx.graphics.getHeight());
        
        cameraControllerEntity = new CameraControllerEntity(this);
        cameraControllerEntity.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f);
        
        PlayerEntity playerEntity = new PlayerEntity(this);
        playerEntity.setPosition(Gdx.graphics.getWidth() / 2.0f, 0.0f);
        
        for (float i = platformSpacing; i < Gdx.graphics.getHeight(); i += platformSpacing) {
            spawnPlatform(i);
            platformGoalY = i + platformSpacing;
        }
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        scoreLabel = new Label("0", skin);
        root.add(scoreLabel).expandY().padTop(25.0f).top();
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
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
        gameViewport.update(width, height);
        gameCamera.position.set(width / 2, height / 2.0f, 0.0f);
        
        uiViewport.update(width, height);
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        stage.getViewport().update(width, height, true);
    }

    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(String selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreLabel.setText(Integer.toString(score));
        if (score > highscore) {
            highscore = score;
        }
    }
    
    public void addScore(int score) {
        this.score += score;
        scoreLabel.setText(Integer.toString(this.score));
        if (this.score > highscore) {
            highscore = this.score;
        }
    }
    
    public void playBreakSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/break.wav", Sound.class).play(.5f);
    }
    
    public void playFallSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/fall.wav", Sound.class).play(.5f);
    }
    
    public void playJumpSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/jump.wav", Sound.class).play(.5f);
    }
    
    public void playPointSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/point.wav", Sound.class).play(.5f);
    }

    public float getPlatformSpacing() {
        return platformSpacing;
    }

    public void setPlatformSpacing(float platformSpacing) {
        this.platformSpacing = platformSpacing;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getJumpPower() {
        return jumpPower;
    }

    public void setJumpPower(float jumpPower) {
        this.jumpPower = jumpPower;
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }

    public CameraControllerEntity getCameraControllerEntity() {
        return cameraControllerEntity;
    }

    public void setCameraControllerEntity(
            CameraControllerEntity cameraControllerEntity) {
        this.cameraControllerEntity = cameraControllerEntity;
    }
    
    public void spawnPlatform(float y) {
        float x = MathUtils.random(70.0f, Gdx.graphics.getWidth() - 140.0f);
        PlatformEntity platform;
        int selection = MathUtils.random(0, 2);
        switch (selection) {
            case 0:
                platform = new PlatformEntity(this);
                break;
            case 1:
                platform = new MovingPlatformEntity(this);
                break;
            default:
                platform = new FallingPlatformEntity(this);
                break;
        }
        platform.setPosition(x, y);
    }

    public float getPlatformGoalY() {
        return platformGoalY;
    }

    public void setPlatformGoalY(float platformGoalY) {
        this.platformGoalY = platformGoalY;
    }
}