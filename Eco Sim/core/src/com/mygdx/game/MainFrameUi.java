package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

class MainFrameUi {

    private static TextButton  showUs, speed, toggleStats, showMenu, addPrey, addPredator;
    private static Stage primaryStage, toggleMenuStage;
    private static boolean speedChange, toggleStatsChange, openMenu, showUsBoolean;
    private static Texture panel;
    private static SpriteBatch spriteBatch;

    static void MainFrameUiLoad(){
        //sets up the UI of the main screen

        //uses input multiplexer for multiple input processors (a.k.a using primary and toggle menu stage at the same time)
        InputMultiplexer inputDevices = new InputMultiplexer();
        primaryStage = new Stage();
        toggleMenuStage = new Stage();

        panel = new Texture("grey_panel.png");
        showMenu = new TextButton("", GameCentral.checkButtonStyle);
        showMenu.setPosition( 30, 800);
        toggleMenuStage.addActor(showMenu);

        speed = new TextButton("", GameCentral.checkButtonStyle);
        toggleStats = new TextButton("", GameCentral.checkButtonStyle);
        addPrey = new TextButton("", GameCentral.checkButtonStyle);
        addPredator = new TextButton("", GameCentral.checkButtonStyle);
        showUs = new TextButton("", GameCentral.checkButtonStyle);

        speed.setPosition(30, 510); speed.setHeight(40); speed.setWidth(40);
        toggleStats.setPosition(30, 460); toggleStats.setHeight(40); toggleStats.setWidth(40);
        addPrey.setPosition(30, 410); addPrey.setHeight(40); addPrey.setWidth(40);
        addPredator.setPosition(30, 360); addPredator.setHeight(40); addPredator.setWidth(40);
        showUs.setPosition(30, 310); showUs.setHeight(40); showUs.setWidth(40);

        primaryStage.addActor(speed);
        primaryStage.addActor(toggleStats);
        primaryStage.addActor(addPrey);
        primaryStage.addActor(addPredator);
        primaryStage.addActor(showUs);

        inputDevices.addProcessor(toggleMenuStage);
        inputDevices.addProcessor(primaryStage);

        Gdx.input.setInputProcessor(inputDevices);

        spriteBatch = new SpriteBatch();
    }

    private static void buttonFunctions() {
        //runs block of code if the button is pressed, which alters values and adds values in animal and main frame
        if (showMenu.isChecked() && !openMenu){
            openMenu = true;
        } else if (showMenu.isChecked() && openMenu){
            openMenu = false;
        }
        if (speed.isChecked() && !speedChange){
            speedChange = true;
            Animals.rabbitSpeed = 0.05f;
            Animals.wolfSpeed = 0.08f;
            MainFrame.secondChange = 0.5f;
            Animals.matingTimer = 3;
            Animals.consumptionTimer = 1;
            Animals.animalStatTimer = 0.5f;
        } else if (speed.isChecked() && speedChange) {
            speedChange = false;
            Animals.rabbitSpeed = 0.3f;
            Animals.wolfSpeed = 0.5f;
            MainFrame.secondChange = 1;
            Animals.matingTimer = 6;
            Animals.consumptionTimer = 2;
            Animals.animalStatTimer = 1f;
        }
        if (toggleStats.isChecked() && !toggleStatsChange){
            Animals.toggleStats = false;
            toggleStatsChange = true;
        } else if (toggleStats.isChecked() && toggleStatsChange){
            Animals.toggleStats = true;
            toggleStatsChange = false;
        }
        if (showUs.isChecked() && !showUsBoolean){
            showUsBoolean = true;
        } else if (showUs.isChecked() && showUsBoolean){
            showUsBoolean = false;
        }

        if (addPrey.isChecked()){
            MainFrame.animals.add(new Animals("rabbit", MainFrame.animals.size(), false, -1, null));
        }
        if (addPredator.isChecked()){
            MainFrame.animals.add(new Animals("wolf", MainFrame.animals.size(), false, -1, null));
        }



        showMenu.setChecked(false);
        speed.setChecked(false);
        toggleStats.setChecked(false);
        addPrey.setChecked(false);
        addPredator.setChecked(false);
        showUs.setChecked(false);
    }


    static void run(float delta){
        //runs from the main frame which draws the UI and its buttons accordingly
       toggleMenuStage.act(delta);
       toggleMenuStage.draw();
        MainFrame.spriteBatch.begin();
        GameCentral.font.draw(MainFrame.spriteBatch, "Show Tools", 100, 835);
        MainFrame.spriteBatch.end();

       buttonFunctions();
        if(openMenu) {
            MainFrame.spriteBatch.begin();
            MainFrame.spriteBatch.draw(panel, 0, 280, 300, 300);
            GameCentral.font.draw(MainFrame.spriteBatch, "Double Speed {" + speedChange + "}", 100, 540);
            GameCentral.font.draw(MainFrame.spriteBatch, "Toggle Stats {" + !toggleStatsChange + "}", 100, 490);
            GameCentral.font.draw(MainFrame.spriteBatch, "Add Prey", 100, 440);
            GameCentral.font.draw(MainFrame.spriteBatch, "Add Predator", 100, 390);
            GameCentral.font.draw(MainFrame.spriteBatch, "Show Us!", 100, 340);
            MainFrame.spriteBatch.end();
            primaryStage.act(delta);
            primaryStage.draw();
        }
        if (showUsBoolean){
            spriteBatch.setProjectionMatrix((MainFrame.cam.combined));
            spriteBatch.begin();
            GameCentral.font.draw(spriteBatch, "              > EcoSim <\n By Yun, Muhammed and Lokansh", -50, 50);
            spriteBatch.end();
        }
    }

}
