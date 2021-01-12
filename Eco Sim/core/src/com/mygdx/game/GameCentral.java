package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class GameCentral extends Game { //This class was made for ease access of fonts and textbuttonstyles for all classes. Also accesses GAME library
    static BitmapFont font; //allows all classes to easily access the font generated
    static TextButton.TextButtonStyle textButtonStyle, checkButtonStyle; //
    private static Skin skin, checkSkin;
    @Override
    public void create() {
        //Generates font using a generator which generates a specific font based on the parameters specified
        FreeTypeFontGenerator generatedFont = new FreeTypeFontGenerator(Gdx.files.internal("afont.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameters.size = 20;
        parameters.color = Color.BLACK;
        font = generatedFont.generateFont(parameters); //standard procedure to generate font

        //Text-button-style are used for the unpressed and pressed buttons shown in menu, configuration and the game's UI
        //Text-button-style requires a skin that takes the buttonData created from TexturePacker program.
        textButtonStyle = new TextButton.TextButtonStyle();
        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("buttonData.data")));
        textButtonStyle.up = skin.getDrawable("buttonUnpressed");
        textButtonStyle.down = skin.getDrawable("buttonPressed");
        //The skin allows us to assign specific PNGs of each button towards the TextButtonStyle

        checkButtonStyle = new TextButton.TextButtonStyle();
        checkSkin = new Skin();
        checkSkin.addRegions(new TextureAtlas(Gdx.files.internal("check.data")));
        checkButtonStyle.up = checkSkin.getDrawable("grey_button10");
        checkButtonStyle.down = checkSkin.getDrawable("grey_button11");

        checkButtonStyle.font = font;
        textButtonStyle.font = font;
        //Assigns a font to the Text Button if we were to put text on the button

        this.setScreen(new MainMenu(this));
        //setups screen using GAME EXTENSION and assigns screen to the MAINMENU class

    }
    @Override
    public void render() {
        super.render();
    } //Renders all the void renders in each set screen
    //APples
}
