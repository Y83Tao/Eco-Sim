package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class MainMenu implements Screen{
    //Using stage as a field for text-buttons to be placed and allow input processors
    //Game here will be assigned to GameCentral game to account for screen transition
    private Game game;
    private boolean configuration;
    private Stage startMenu, configurations;
    private Texture background, frame, panel; //Texture used for background, frame and panel images
    private SpriteBatch batch; //sprite batch is the rendering system used to render 2d images and text
    //These are global text-buttons used outside of override methods of library
    private TextButton start;
    private TextButton exit;
    private TextButton beginSimulation;
    private TextButton worldSize;
    private TextButton predatorType;
    private TextButton preyType;
    //enums are used to easily differentiate configuration settings set by the user.
    enum RESOURCES {LOW, DEFAULT, HIGH}
    enum SIZE {SMALL, MEDIUM, LARGE}
    enum POPULATION  {LOW, MODERATE, ALOT} enum ANIMALPREDATOR {WOLF, KNIGHT} enum ANIMALPREY{BUNNY, FLOATINGPANDAS}
    private static RESOURCES tree, water, food; private SIZE map;
    private RESOURCES[] resources = new RESOURCES[3]; private TextButton[] buttons = new TextButton[3];
    private static POPULATION prey, predator; static ANIMALPREDATOR animalpredator; static ANIMALPREY animalprey;
    private POPULATION[] popArray = new POPULATION[2]; private TextButton[] popButtonArray = new TextButton[2];
    //Arrays of enums used to shorten code in for loop

    //These are range and speed values set as static so MainFrameUI class can have user manipulate these values
    private static int preyVisionRange = 4,  predatorVisionRange = 4;
    private static float preySpeed = 0.3f,predatorSpeed = 0.5f;


    MainMenu(Game game){
        this.game = game;
    } //This sets the game to this class


    @Override
    public void show() {
        batch = new SpriteBatch();
        startMenu = new Stage();
        configurations = new Stage();
        background = new Texture("background.jpg"); frame = new Texture("coolFrameLol.png"); panel = new Texture("grey_panel.png");
        start = new TextButton("Start", GameCentral.textButtonStyle); start.setPosition(150, 500);
        exit = new TextButton("Exit", GameCentral.textButtonStyle); exit.setPosition(150, 425);
        startMenu.addActor(start);  startMenu.addActor(exit); //assigning each button to the stage Start Menu
        Gdx.input.setInputProcessor(startMenu); //allows for the system to detect any clicks towards actors in the stage

        //Using Text-Button instances to create buttons for configuration settings.
        beginSimulation = new TextButton("Begin Simulation", GameCentral.textButtonStyle); beginSimulation.setPosition(1150, 500);
        TextButton treeFreq = new TextButton("Default", GameCentral.textButtonStyle);
        treeFreq.setPosition(300, 650); configurations.addActor(treeFreq);
        TextButton waterFreq = new TextButton("Default", GameCentral.textButtonStyle);
        waterFreq.setPosition(300, 600); configurations.addActor(waterFreq);
        TextButton foodFreq = new TextButton("Default", GameCentral.textButtonStyle);
        foodFreq.setPosition(300, 550); configurations.addActor(foodFreq);
        worldSize = new TextButton("Medium", GameCentral.textButtonStyle); worldSize.setPosition(300, 270); configurations.addActor(worldSize);

        predatorType = new TextButton("Wolf", GameCentral.textButtonStyle); predatorType.setPosition(870, 650); configurations.addActor(predatorType);
        preyType = new TextButton("Bunny", GameCentral.textButtonStyle); preyType.setPosition(870, 600); configurations.addActor(preyType);
        TextButton predatorAmount = new TextButton("Moderate", GameCentral.textButtonStyle);
        predatorAmount.setPosition(870, 550); configurations.addActor(predatorAmount);
        TextButton preyAmount = new TextButton("Moderate", GameCentral.textButtonStyle);
        preyAmount.setPosition(870, 500); configurations.addActor(preyAmount);

        configurations.addActor(beginSimulation);

        //setting default settings for configurations
        tree = RESOURCES.DEFAULT;
        water = RESOURCES.DEFAULT;
        food = RESOURCES.DEFAULT;
        map = SIZE.MEDIUM;

        animalprey = ANIMALPREY.BUNNY;
        animalpredator = ANIMALPREDATOR.WOLF;
        prey = POPULATION.MODERATE;
        predator = POPULATION.MODERATE;

        popArray[0] = prey; popArray[1] = predator; popButtonArray[0] = preyAmount; popButtonArray[1] = predatorAmount;

        resources[0] = tree; resources[1] = water; resources[2] = food;
        buttons[0] = treeFreq; buttons[1] = waterFreq; buttons[2] = foodFreq;
    }

    public void buttonFunctions() {
        //this function that runs from render() executes these blocks for code based on whether the button is pressed for a specific button.
        //!configuration section runs off two buttons, one that exits when clicked and one that sets a boolean to true which goes to another screen
        //Applies for rest of method
        if (!configuration) {
            if (start.isPressed()) {
                Gdx.input.setInputProcessor(configurations);
                configuration = true;
            }
            if (exit.isPressed()) {
                Gdx.app.exit();
            }
        } else {
            if (beginSimulation.isPressed()) {
                game.setScreen(new MainFrame(map, popArray[0], popArray[1], tree, water, food));
            } //sets up the main game and passing parameters based on configuration settings
            //Uses for loops to check if these configurations are checked and switches settings based on current configuration
            for (int a = 0; a < 3; a++) {
                if (buttons[a].isChecked()) {
                    switch (resources[a]) {
                        case LOW:
                            buttons[a].setText("Default");
                            resources[a] = RESOURCES.DEFAULT;
                            buttons[a].setChecked(false);
                            break;
                        case DEFAULT:
                            buttons[a].setText("High");
                            resources[a] = RESOURCES.HIGH;
                            buttons[a].setChecked(false);
                            break;
                        case HIGH:
                            buttons[a].setText("LOW");
                            resources[a] = RESOURCES.LOW;
                            buttons[a].setChecked(false);
                            break;
                    }
                }
            }
            if (predatorType.isChecked()) {
                switch (animalpredator) {
                    case KNIGHT:
                        predatorType.setText("Wolf");
                        animalpredator = ANIMALPREDATOR.WOLF;
                        predatorType.setChecked(false);
                        predatorVisionRange = 4;
                        break;
                    case WOLF:
                        predatorType.setText("Knight");
                        animalpredator = ANIMALPREDATOR.KNIGHT;
                        predatorType.setChecked(false);
                        predatorVisionRange = 6;
                        break;
                }
            }
            if (preyType.isChecked()) {
                switch (animalprey) {
                    case BUNNY:
                        preyType.setText("Floating Pandas");
                        animalprey = ANIMALPREY.FLOATINGPANDAS;
                        preyType.setChecked(false);
                        preyVisionRange = 5;
                        break;
                    case FLOATINGPANDAS:
                        preyType.setText("Bunny");
                        animalprey = ANIMALPREY.BUNNY;
                        preyType.setChecked(false);
                        preyVisionRange = 4;
                        break;
                }
            }
            for (int a = 0; a < 2; a++) {
                if (popButtonArray[a].isChecked()) {
                    switch (popArray[a]) {
                        case LOW:
                            popButtonArray[a].setText("Moderate");
                            popArray[a] = POPULATION.MODERATE;
                            popButtonArray[a].setChecked(false);
                            break;
                        case MODERATE:
                            popButtonArray[a].setText("Alot");
                            popArray[a] = POPULATION.ALOT;
                            popButtonArray[a].setChecked(false);
                            break;
                        case ALOT:
                            popButtonArray[a].setText("Low");
                            popArray[a] = POPULATION.LOW;
                            popButtonArray[a].setChecked(false);
                            break;
                    }
                }
            }
            if (worldSize.isChecked()) {
                {
                    switch (map) {
                        case SMALL:
                            worldSize.setText("Medium");
                            map = SIZE.MEDIUM;
                            worldSize.setChecked(false);
                            break;
                        case MEDIUM:
                            worldSize.setText("Large");
                            map = SIZE.LARGE;
                            worldSize.setChecked(false);
                            break;
                        case LARGE:
                            worldSize.setText("Small");
                            map = SIZE.SMALL;
                            worldSize.setChecked(false);
                            break;
                    }
                }


            }
        }
    }


    @Override
    public void render(float v) { //render runes constantly, which allows for usage of sprite batch, stage and buttonFunctions()
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (!configuration){

            //sprite batch draws each image repeated so it can be shown on screen.
            batch.begin();
            batch.draw(background, 0, 0, 1700, 900);
            batch.draw(panel, 100, 150, 400, 200);
            batch.draw(frame, -20, 550, 550, 250);
            //Uses Game Central set fonts to draw text on given coordinates and scales to give it a decent look in proportions
            GameCentral.font.getData().setScale( 0.75f);
            GameCentral.font.draw(batch, "A Simulation By Yun, Muhammed, Lokansh", 120, 670);
            GameCentral.font.getData().setScale( 0.8f);
            GameCentral.font.draw(batch, " In this program, environmental factors are at \nthe control of humans, and through it we are able \nto simulate an ecosystem, " +
                    "and observe the \ninteraction between nature and animals. \nUsers able to set factors, including tree and " +
                    "\nwater abundance, and will be able to manipulate \npopulations to observe their effects " +
                    "\nand the sustainability of animal \npopulations over time.", 120, 335);
            GameCentral.font.getData().setScale( 1f);
            GameCentral.font.draw(batch, "EcoSimYML", 198, 698);
            batch.end();

            //draws the stage which holds all the buttons
            startMenu.act(v);
            startMenu.draw();

        } else {
            //runs this code when boolean by clicking start button is set to true
            batch.begin();
            batch.draw(background, 0, 0, 30000, 900); //just to get the are of the background that is a solid color
            batch.draw(panel, 80, 450, 450, 350); batch.draw(panel, 80, 190, 450, 200);
            batch.draw(panel, 650, 450, 450, 350); batch.draw(panel, 650, 190, 450, 200);
            GameCentral.font.getData().setScale(1f);
            GameCentral.font.draw(batch, "Environmental Settings", 120, 750); GameCentral.font.draw(batch, "Tree Frequency\n\nWater Frequency\n\nFood Frequency", 105, 700);
            GameCentral.font.draw(batch, "World Settings", 120, 350); GameCentral.font.draw(batch, "World Size\n\nSmall 25x25    Medium 50x50    Large 75x75", 105, 300);
            GameCentral.font.draw(batch, "Base Settings", 680,  750);
            GameCentral.font.draw(batch, "Predator Type\n\nPrey Type\n\nPredator #\n\nPrey #", 680,  695);

            GameCentral.font.draw(batch, "Prey = " + animalprey + "\nVision Range = " + preyVisionRange + "  Speed = " + preySpeed, 680,  360);
            GameCentral.font.draw(batch, "Predator = " + animalpredator + "\nVision Range = " + predatorVisionRange + "  Speed = " + predatorSpeed, 680,  275);

            GameCentral.font.getData().setScale(1f);
            batch.end();

            configurations.act(v);
            configurations.draw();
        }
        buttonFunctions();
    }

    @Override
    public void resize(int i, int i1) {

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
            background.dispose();
    }
}
