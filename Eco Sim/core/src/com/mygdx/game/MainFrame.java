package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Random;

public class MainFrame extends ApplicationAdapter implements Screen {

	static PerspectiveCamera cam; //PerspectiveCamera is a camera that can be manipulated by code to move it
	static ModelBatch modelBatch; //same as sprite batch but for 3d models
	static SpriteBatch spriteBatch; //this is for 2d texts and images
	static Environment environment; //environment is the lighting that will be assigned to each model
	static ArrayList<Animals> animals = new ArrayList<>(); //stores many instances of animal so many animals can exist at once
	static ArrayList<ModelInstance> terrainBlocks; //stores block instances (model instances) for terrain blocks
	static ArrayList<NodeClass> nodeClasses; //stores node classes instances which is used for berry bushes and stuff
	static ArrayList<TerrainCoordinates> terrainCoordinates; //stores terrain cord instances which store x y cords and vector cords of each tile by index
	static AssetManager assetManager; //used to generate models from assets folder
	static float textDirection = 180; //used to show text on in game a.i based on camera direction

	private static ModelInstance ocean; //used for ocean block that is separate from array list
	private float day, hour, minute, second; //for time
	private static int  preyAmount, predatorAmount, treeFreq, waterFreq, foodFreq;
	static int worldSize;
	static float secondChange = 1;
	private float time, cameraRotationTimer, cameraRotationCount, translationONE = 0.5f, translationTWO = 0f, camTurnCooldownTimer;
	private int cameraRotation = 0;
	private boolean loading, assignModelLoading, landLoading, cameraRotationLeft, cameraRotationRight, cameraIsRotating, camTurnCooldown;

	private Texture loadingImage;
	MainFrame(MainMenu.SIZE worldSize, MainMenu.POPULATION preyAmount, MainMenu.POPULATION predatorAmount, MainMenu.RESOURCES treeFreq, MainMenu.RESOURCES waterFreq, MainMenu.RESOURCES foodFreq){
		//uses switch on the enums passed to assign values set by the user's configuration settings
		switch (worldSize){
			case SMALL: MainFrame.worldSize = 15; break; 	case MEDIUM: MainFrame.worldSize = 25; break;	case LARGE: MainFrame.worldSize = 35; break;
		}
		switch (preyAmount){
			case LOW: MainFrame.preyAmount = 5; break;	case MODERATE: MainFrame.preyAmount = 10; break;  	case ALOT: MainFrame.preyAmount = 15; break;
		}
		switch(predatorAmount){
			case LOW: MainFrame.predatorAmount = 2; break;	case MODERATE: MainFrame.predatorAmount = 4; break;  	case ALOT: MainFrame.predatorAmount = 6; break;
		}
		switch (treeFreq){
			case LOW: MainFrame.treeFreq = 80; break; case DEFAULT: MainFrame.treeFreq = 40; break; case HIGH: MainFrame.treeFreq = 25; break;
		}
		switch (waterFreq){
			case LOW: MainFrame.waterFreq = 50; break; case DEFAULT: MainFrame.waterFreq = 25; break; case HIGH:  MainFrame.waterFreq = 15; break;
		}
		switch (foodFreq){
			case LOW: MainFrame.foodFreq = 50; break; case DEFAULT: MainFrame.foodFreq = 25; break; case HIGH: MainFrame.foodFreq = 15; break;
		}
		System.out.println(MainFrame.preyAmount + " and "  + MainFrame.predatorAmount);
	}
	@Override
	public void create () {
		//initialize array lists
		terrainBlocks = new ArrayList<>();
		nodeClasses = new ArrayList<>();
		terrainCoordinates = new ArrayList<>();
		modelBatch = new ModelBatch();

		//environment set up which uses rgb numbers for colors and ambient light method from color attribute class
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 0.8f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		//sets up light and it's intensity using direction light

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); //initializes camera with viewport and fov
		cam.position.set(30f, 15f, -13f);
		cam.lookAt(30,-8,100);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();
		//sets up basic camera settings and updates it

		//initialize sprite batch and assigns loading.png to texture
		spriteBatch = new SpriteBatch();
		loadingImage = new Texture("loading.png");

		MainFrameUi.MainFrameUiLoad(); //runs main frame user interface constructor to set up it's variables/etc
		assetLoading(); //runs asset loading method to load up assets
	}

	private void assetLoading(){
		//initializes asset manager which is used to .load asset from assets folder and uses model class for basic model construction
		assetManager = new AssetManager();
		assetManager.load("Wolfy.g3db", Model.class);
		assetManager.load("Rock.g3db", Model.class);
		assetManager.load("bun-bun.g3db", Model.class);
		assetManager.load("treeLowPoly.g3db", Model.class);
		assetManager.load("Bush01.g3db", Model.class);
		assetManager.load("BushWithBerrys01.g3db", Model.class);
		assetManager.load("Heavy_Models/031713434b9c44a39a73c06c6e4cc3b2.g3db",Model.class);
		assetManager.load("Heavy_Models/escandalosos.obj",Model.class);
		loading = true; //set to true so rendering knows when asset loading code is complete
	}
	private void assetAssignment(){
		//spawns preys and predators based on user configuration by using a for loop and adding it to animal array list
		for (int a = 0; a < preyAmount; a++) {
			animals.add(new Animals("rabbit", a, false, -1, null));
			System.out.println("rabbit" + a); //debug
		}
	for (int a = preyAmount; a < (preyAmount +  predatorAmount); a++) {
			animals.add(new Animals("wolf", a, false, -1, null));
			System.out.println("wolf"+ a); //debug
		}
		//tells the rendering class that the spawn is complete
		assignModelLoading = true;
	}


	private void run() {
		time += Gdx.graphics.getDeltaTime(); //time variable increases using time diff from system to use as a loading screen to let assets load
		if (loading && assetManager.update() && !landLoading){generateLand(worldSize, worldSize);}
		else if (loading && assetManager.update() && !assignModelLoading){assetAssignment();}
		else if (time > 2){

			Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			Gdx.gl.glClearColor(135 / 255f, 206 / 255f, 235 / 255f, 1);
			cam.update(true); //allows for the camera to update repeated for user controlling

			modelBatch.begin(cam); //assigns camera to model batch rendering system to place models in compliance to camera
			modelBatch.render(terrainBlocks, environment); //model batch renders the terrain blocks array list using the enviornment made
			modelBatch.render(ocean, environment);

			modelBatch.end();

			for (NodeClass a : nodeClasses) { //runs all the bushes from the node classes array lists which individually runs their own instance of node class
				a.NodeClassObjectRender();
			}

			for (int a = 0; a < animals.size(); a++){ //runs all the animals in animal array list allowing for each animal to exist independently
				animals.get(a).render(Gdx.graphics.getDeltaTime());
			}
		}
		else { //draws loading screen before timer is complete
			spriteBatch.begin();
			spriteBatch.draw(loadingImage, 0, 0, 1500, 900);
			spriteBatch.end();
		}

		//all user input detections using input keys class for camera movement
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			cam.translate(translationONE, 0, -translationTWO);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			cam.translate(-translationONE, 0, translationTWO);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			cam.translate(translationTWO, 0, translationONE);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			cam.translate(-translationTWO, 0, -translationONE);
		}
		if (!cameraIsRotating) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
				cameraRotation++;
				if (cameraRotation >= 4) {
					cameraRotation = 0;
				}
				cameraRotationLeft = true;
				cameraIsRotating = true;
			}
			if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
				cameraRotation--;
				if (cameraRotation <= -1) {
					cameraRotation = 3;
				}
				cameraRotationRight = true;
				cameraIsRotating = true;
			}
		}

		//TIME
		second += Gdx.graphics.getDeltaTime();
		if (second >= secondChange){
			minute += 1; second = 0;
		} if (minute >= 60){
			hour ++; minute = 0;
		} if (hour >= 12){
			day ++;
			hour = 0;
		}
		spriteBatch.begin(); //prints out the time duration on top
		GameCentral.font.draw(spriteBatch, "  Day " + (int) day + "  |  " + (int) hour + ":" + (int) minute + ":"+ (int) (second * 100), 700, 850);
		spriteBatch.end();

		rotate(); //forCameraRotation

	}
	private void rotate(){ //for rotating camera and adjusting key function outputs and text of animals stats based on camera rotation
		if (cameraRotationLeft || cameraRotationRight){
			float rotation;
			if (cameraRotationLeft){rotation = 4.5f;} else {rotation = -4.5f;}
			cameraRotationTimer += Gdx.graphics.getDeltaTime();
			if (cameraRotationTimer >= 0.001){
				cam.rotate(rotation, 0, 1, 0);
				cameraRotationTimer = 0;
				cameraRotationCount ++;
				if (cameraRotationCount >= 20){
					cameraRotationLeft = false; cameraRotationRight = false;
					cameraRotationCount = 0;
					camTurnCooldown = true;
				}
			}
		}
		//creates cool down after rotating camera to prevent displaced translation values
		if (camTurnCooldown){
			camTurnCooldownTimer += Gdx.graphics.getDeltaTime();
			if (camTurnCooldownTimer >= 0.5){
				cameraIsRotating = false;
				camTurnCooldown = false; camTurnCooldownTimer = 0;
			}
		}
		//sets up player movement translation values based on camera rotation
		if (cameraRotation == 0){
			translationONE = 0.5f;
			translationTWO = 0f;
			textDirection = 180;
		}
		else if (cameraRotation == 1){
			translationONE = 0f;
			translationTWO = 0.5f;
			textDirection = 270;
		}
		else if (cameraRotation == 2){
			translationONE = -0.5f;
			translationTWO = 0f;
			textDirection = 0;
		}
		else if (cameraRotation == 3){
			translationONE = 0f;
			translationTWO = -0.5f;
			textDirection = 90;
		}
	}

	private void generateLand(int xGen, int yGen){ //generates land based on configs, which adds model instances to the terrain blocks array list
		int xAmount = xGen, location = 0, zChange = 0;
		Random random = new Random();
		ModelBuilder modelBuilder = new ModelBuilder();
		Model grass = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal );
		ocean = new ModelInstance(modelBuilder.createBox(1000f, 5f, 1000f, new Material(ColorAttribute.createDiffuse(Color.BLUE)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal ));
		ocean.transform.translate(0, -2, 0);
		int posx = 0, posy = 0;
		while (true) {
			if (xGen != 0) { //develops the row of X valued blocks until there is none left, which then resets but on a different z cord and adding a y cord.
				xGen --;
				terrainBlocks.add(new ModelInstance(grass));
				terrainBlocks.get(location).transform.translate(xGen * 5, random.nextFloat(), zChange);
				Vector3 positionForNode = new Vector3();
				terrainBlocks.get(location).transform.getTranslation(positionForNode);

				if (random.nextInt(waterFreq) == 1 && (location % worldSize) != 0){
					nodeClasses.add(new NodeClass(NodeClass.TYPE.WATER, positionForNode, location));
					MainFrame.terrainBlocks.get(location).materials.get(0).set(ColorAttribute.createDiffuse(Color.BLUE));
					}
				else if (random.nextInt(foodFreq) == 1 && (location % worldSize) != 0) {
					nodeClasses.add(new NodeClass(NodeClass.TYPE.BERRYBUSH, positionForNode, location));}
				else if (random.nextInt(treeFreq) == 1){
					nodeClasses.add(new NodeClass(NodeClass.TYPE.TREE, positionForNode, location));
				}
				else {
					nodeClasses.add(new NodeClass(NodeClass.TYPE.OTHER, positionForNode, location));}

				terrainCoordinates.add(new TerrainCoordinates(posx, posy, positionForNode.x, positionForNode.z, location));
				System.out.println("x = " + terrainCoordinates.get(location).getX() + " y = " + terrainCoordinates.get(location).getY());
				location += 1; posx += 1;

			} else if (yGen != 0){
				xGen = xAmount;
				zChange += 5;
				yGen --;
				posy += 1; posx = 0;
			} else {
				break;
			}
			//System.out.println(xGen + "  " + yGen); {DEBUG}
		}

		landLoading = true;
	}
	@Override
	public void show() {
		create(); //first code that runs is the create() method, creates all the values
	}
	@Override
	public void render(float v) {//runs the main game and UI
		run();
		MainFrameUi.run(v); //class that runs buttons UI
	}
	@Override
	public void hide() {
	}

	@Override
	public void dispose () { //dispose model batch when done to prevent memory leaks
		modelBatch.dispose();
	}
}
