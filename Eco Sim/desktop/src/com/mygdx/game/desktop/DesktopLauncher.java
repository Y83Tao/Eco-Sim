package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.GameCentral;

public class DesktopLauncher { //This is the first class that runs, which sets up basic configurations and boots up the GameCentral class
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "EcoSim";
		config.width = 1500;
		config.height = 900;
		config.backgroundFPS = 60;
		config.resizable = false;
		new LwjglApplication(new GameCentral(), config); //this begins application at GameCentral
	}
}
