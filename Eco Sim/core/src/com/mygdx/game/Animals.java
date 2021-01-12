package com.mygdx.game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.*;

class Animals {
    private String type, meshType; //type used to determine if instance is a prey or predator, while mesh type is the string model name
    private int position, previousPosition, resourceLocation, indexNum; //used for method update-move() to assign positional acknowledgement on nodes/tiles
    private float time, time2, time3, time4, pregnancyTime, isChildTimer, food = 100, reproductiveUrge = 0, thirst = 100, consumingResourceTimer, matingTimerDelta;
    private ModelInstance rabbitInstance; //these times are different usages of delta time for timing purposes. //food, reproductive urge and thirst here are starting values

    //booleans used globally to determine if their current interest is looking for mate and if a resource is found
    private boolean foundResource;
    private boolean lookingForMate;
    private int visionRange;
    static float rabbitSpeed = 0.3f, wolfSpeed = 0.5f, animalStatTimer = 1; //editable speed values of animals and stat updater by MainFrameUI() class
    static int matingTimer = 6, consumptionTimer = 2; //editable mating and consumption timers for the MainFrameUI() class
    static boolean toggleStats = true; //editable stat toggling which enables stat texts on animals if true

    private boolean isChild; //if added animal is a child, then create child based on different code
    private int parentIndex; //parent index is position of the parent where the child should spawn
    private Vector3 parentVector; //parent vector is the xyz position of the parent where the child should spawn

    //following booleans and enums are self explanatory and used for executing specific code based on animal state
    private int targetMateOccupantIndex;
    private int targetPreyIndex;


    private boolean isPregnant;
    private boolean mating;
    private boolean move = false;
    private boolean movePrompt = false;
    private boolean placement;

    private enum gender {male, female}
    private gender Gender;

    private boolean alive = true;
    private boolean consumingResource;
    private boolean isItAPrey;
    private boolean aliveNodesReset;

    //global vector positions and pathing index used for completing movement in a path
    private Vector3 vectorPosition;
    private ArrayList<Vector3> pathing = new ArrayList<Vector3>(); private int pathingIndex = -1, usageIndex;
    private ArrayList<NodeClass> knownArea = new ArrayList<>();

    //font used from game central class, sprite batch used for text stats and strings are action and pregnancy status
    private SpriteBatch spriteBatch;
    private BitmapFont font = GameCentral.font;
    private String actionState = "null()", pregnant = "";

    //first code that is run, animal variables and models set up based on data passed into parameter
    Animals(String type, int indexNum, boolean isChild, int parentIndex, Vector3 parentVector){
        this.parentVector = new Vector3();
        spriteBatch = new SpriteBatch();
        this.indexNum = indexNum;
        this.type = type;
        this.isChild = isChild;
        this.parentVector = parentVector;
        this.parentIndex = parentIndex;
        if (type.equals("rabbit")){
            if (MainMenu.animalprey == MainMenu.ANIMALPREY.BUNNY) {meshType = "bun-bun.g3db"; isItAPrey = true; visionRange = 4;}
            else if(MainMenu.animalprey == MainMenu.ANIMALPREY.FLOATINGPANDAS){meshType = "Heavy_Models/escandalosos.obj"; isItAPrey = true; visionRange = 5;}}
        else if (type.equals("wolf")){
            if (MainMenu.animalpredator == MainMenu.ANIMALPREDATOR.WOLF) {meshType = "Wolfy.g3db"; isItAPrey = false; visionRange = 5;}
            else if (MainMenu.animalpredator == MainMenu.ANIMALPREDATOR.KNIGHT){meshType = "Heavy_Models/031713434b9c44a39a73c06c6e4cc3b2.g3db"; isItAPrey = false; visionRange = 6;}


        }
        //insures that male to female ratio is 1:1
        if (MainFrame.animals.size() % 2 == 0){Gender = gender.male;}
        else {Gender = gender.female;}

        animalMeshLoad(); //goes to next method
    }
    private void animalMeshLoad(){
        //picks a model to assign to and their positioning. Scales based on the positioning of the models.
            Model rabbit = MainFrame.assetManager.get(meshType, Model.class);
            rabbitInstance = new ModelInstance(rabbit);
            if (type.equals("rabbit")) {
                if (MainMenu.animalprey == MainMenu.ANIMALPREY.BUNNY) { rabbitInstance.transform.setToScaling(0.005f, 0.005f, 0.005f);}
        }
            else if (type.equals("wolf")){
                if (MainMenu.animalpredator == MainMenu.ANIMALPREDATOR.WOLF) {rabbitInstance.transform.setToScaling(0.008f, 0.008f, 0.008f);}
                else if (MainMenu.animalpredator == MainMenu.ANIMALPREDATOR.KNIGHT){rabbitInstance.transform.setToScaling(0.025f, 0.025f, 0.025f);}
        }
            int size = MainFrame.nodeClasses.size() / 2 + 5;
            if (!isChild) { //sets transformation, scaling and placement for if it is an adult animal
                while (!placement) {
                    Vector3 vectorPos = new Vector3();
                    MainFrame.terrainBlocks.get(size).transform.getTranslation(vectorPos);
                        vectorPos.y += 4f;
                    if (!MainFrame.nodeClasses.get(size).getOccupation()) { //checks to see if the spot if occupied, if not, check again but 3 blocks over
                        rabbitInstance.transform.setTranslation(vectorPos);
                        placement = true;
                        vectorPosition = vectorPos;
                        MainFrame.nodeClasses.get(size).setOccupation(true);
                        position = size;
                    } else {
                        size += 3;
                    }
                }
            } else { //sets transformations, scaling and placement for if it is a child
                rabbitInstance.transform.setToScaling(0.004f, 0.004f, 0.004f);
                if (isItAPrey && MainMenu.animalprey == MainMenu.ANIMALPREY.FLOATINGPANDAS){rabbitInstance.transform.setToScaling(0.5f, 0.5f, 0.5f);}
                if (!isItAPrey && MainMenu.animalpredator == MainMenu.ANIMALPREDATOR.KNIGHT){rabbitInstance.transform.setToScaling(0.015f, 0.015f, 0.015f);}
                rabbitInstance.transform.setTranslation(parentVector);


                placement = true;
                vectorPosition = parentVector;
                MainFrame.nodeClasses.get(parentIndex).setOccupation(true);
                position = parentIndex;

            }
    }

    void render(float deltaTime) { //this class runs most of animal instance using delta timer and repeated running
        if (alive){ //if the animal is alive, their stats will change every second (half a second if sped up by 2x)
            time += deltaTime;
            navigation();
            if (isItAPrey){ //prey and predator have different stat change values
                if (time > animalStatTimer) {
                    food -= 0.5;
                    thirst -= 1;
                    if (Gender == gender.male && !isChild) {reproductiveUrge += 2;}
                    if (reproductiveUrge > 100) {reproductiveUrge = 100;}
                    time = 0;
                }
            } else{
                if (time > animalStatTimer) {
                    food -= 2;
                    thirst -= 1;
                    if (Gender == gender.male && !isChild){ reproductiveUrge += 2; }
                    if (reproductiveUrge > 100) {reproductiveUrge = 100;}
                    time = 0;
                }
            }
            //renders the animal on the screen
            MainFrame.modelBatch.begin(MainFrame.cam);
            MainFrame.modelBatch.render(rabbitInstance, MainFrame.environment);
            MainFrame.modelBatch.end();

            //uses matrix onto sprite batch to adjust text positioning in an imaginary 3d environment for each animal to display stats on top of them
            spriteBatch.setProjectionMatrix((MainFrame.cam.combined));
            spriteBatch.setTransformMatrix(new Matrix4().translate(vectorPosition.x, vectorPosition.y + 4, vectorPosition.z).scl(0.05f).rotate(0,1,0,MainFrame.textDirection));
            if (toggleStats) {
                spriteBatch.begin();
                if (Gender == gender.male && !isChild) { //displaying of texts if it is toggled true
                    font.draw(spriteBatch, "Food = " + food + "\nThirst = " + thirst +
                            "\nR-urge = " + reproductiveUrge +
                            "%\n" + actionState, 10, 20);
                } else {
                    font.draw(spriteBatch, "Food = " + food + "\nThirst = " + thirst +
                            "\n" + pregnant + "\n" + actionState, 10, 20);
                }
                spriteBatch.end();
            }
            //runs these methods which have their own timers if the animal is true of these booleans
            if (isPregnant){pregnancy();}
            if (isChild) {growingUp();}

            if (time4 > 0) { //time4 used in update move method to switch up action if it doesn't detect anything when searching
                time4 += deltaTime;
            }
            if (food <= 0 || thirst <= 0) { //kills off animal if their food or thirst reaches below zero
                alive = false;
            }
        } else if (!aliveNodesReset){ //resets the nodes of the animal so it doesn't conflict for others when they die
            MainFrame.nodeClasses.get(previousPosition).setOccupationIsPrey(false);
            MainFrame.nodeClasses.get(previousPosition).setOccupation(false);
            MainFrame.nodeClasses.get(position).setOccupationIsPrey(false);
            MainFrame.nodeClasses.get(position).setOccupation(false);
            MainFrame.nodeClasses.get(previousPosition).setOccupantIndex(-1);
            aliveNodesReset = true;
        }
    }

    //getters and setters for various values for technical uses such as executing code based on if their target animal is following...

    private boolean isPregnant() {
        return isPregnant;
    }

    private boolean isChild() {
        return isChild;
    }

    private void setMating(boolean mating) {
        this.mating = mating;
    }

    private boolean isMating() {
        return mating;
    }

    private void setAlive(boolean alive) {
        this.alive = alive;
    }

    private boolean isAlive() {return alive;}

    private void pregnancy(){
        pregnancyTime += Gdx.graphics.getDeltaTime();
        System.out.println(time3);
        if (pregnancyTime >= 20){
            MainFrame.animals.add(new Animals(this.type, MainFrame.animals.size(), true, position, vectorPosition));
            pregnancyTime = 0;
            isPregnant = false;
            pregnant = "";
        }
    }
    private void growingUp(){
        //if it was a child, this method will run. This takes time delta of system and once it surpasses a time, the animal will mature and be free of the is child boolean
        //also scales models based on their model
        isChildTimer += Gdx.graphics.getDeltaTime();
        if (isChildTimer > 20){
            rabbitInstance.transform.setToScaling(0.008f, 0.008f, 0.008f);
            if (isItAPrey && MainMenu.animalprey == MainMenu.ANIMALPREY.FLOATINGPANDAS){rabbitInstance.transform.setToScaling(1f, 1f, 1f);}
            if (!isItAPrey && MainMenu.animalpredator == MainMenu.ANIMALPREDATOR.KNIGHT){rabbitInstance.transform.setToScaling(0.025f, 0.025f, 0.025f);}

            isChild = false;
            isChildTimer = 0;
            Vector3 vectorPos = new Vector3();
            MainFrame.terrainBlocks.get(position).transform.getTranslation(vectorPos);
            vectorPos.y += 4f;
            rabbitInstance.transform.setTranslation(vectorPos);
        }
    }

    private void navigation(){
        //navigation tells animal what to do at the moment, if it was moving, keep moving. If it was consuming something, keep doing that. else,
        //figure out what to do
        //{DEBUG}
         //System.out.println("consuming resource = " + consumingResource);
         //System.out.println("mating = " + mating);
        //System.out.println("lookingformate = " + lookingForMate);
        if (!consumingResource && !mating) {
            if (movePrompt) {
                move = true;
            } else {
                whatToDo();
            }
            if (move) {
                updateMove(pathing.get(usageIndex));
                time3 = 0;
            }
        } else {consumingResourceMethod();}
    }
    private void consumingResourceMethod(){
        //uses a timer for each time animal consumes a resource or is mating. executes block of code if these times are complete.
        //consuming resource used when eating berries or drinking water, so it takes a bit of time to consume.
        //mating timer for when animals mate so it takes time to mate rather than instant.
        consumingResourceTimer += Gdx.graphics.getDeltaTime();
        if (!mating){
            actionState = "consumingResource()";
            if (consumingResourceTimer >= consumptionTimer){
                consumingResource = false;
                consumingResourceTimer = 0;
                if (MainFrame.nodeClasses.get(resourceLocation).getType() == NodeClass.TYPE.BERRYBUSH) {
                    MainFrame.nodeClasses.get(resourceLocation).changeToBushWithoutBerries();
                    food += 20;
                    if (food > 100) {food = 100;}
                } else  if (MainFrame.nodeClasses.get(resourceLocation).getType() == NodeClass.TYPE.WATER) {
                    thirst += 35;
                    if (thirst > 100) {thirst = 100;}
                }
                MainFrame.nodeClasses.get(resourceLocation).setOccupation(false);
            }
        } else {
            actionState = "mating()";
            matingTimerDelta += Gdx.graphics.getDeltaTime();
            if (consumingResource = true) { consumingResource = false;}
            if (matingTimerDelta >= matingTimer){
                consumingResourceTimer = 0;
                reproductiveUrge = 0;
                matingTimerDelta = 0;
                if (Gender == gender.female){
                    pregnant = "pregnant()";
                    isPregnant = true;
                }
                mating = false;
                //System.out.println("MATING COMPLETE");
            }
            if ( !MainFrame.animals.get(targetMateOccupantIndex).isAlive()){
                mating = false;
                consumingResourceTimer = 0;
                matingTimerDelta = 0;

            }

        }

    }

    private void whatToDo(){
        //tells the animal what to do based on their current stats
        //create pathing location will pass an enum so the method can know what to look for
        if (food <= thirst && ((food > reproductiveUrge) || (food < 50))){
            if (isItAPrey) {createPathingLocation(NodeClass.TYPE.BERRYBUSH); }
            else {createPathingLocation(NodeClass.TYPE.OTHER);}
           // System.out.println("ran food"); {debug}
            }
        else if (thirst < food && ((thirst > reproductiveUrge) || (thirst < 50))){
            createPathingLocation(NodeClass.TYPE.WATER);
            //System.out.println("ran water"); {debug]
                }
        else {
            //System.out.println("ran reproduciton"); {debug}
            lookingForMate = true;
            createPathingLocation(NodeClass.TYPE.OTHER);
            }
        //System.out.println(this.type + "  ran  " + lookingForMate); {debug}
    }

    private void createPathingLocation(NodeClass.TYPE type) {
        //first this method creates the known area in the animal's vicinity. this is what the animal sees
        //known area array list will be filled based on their vision
        int movementCalculator;
        int height = visionRange, width = visionRange;

        for (int a = -width ; a <= width; a++) {

            movementCalculator = position + ((MainFrame.worldSize ) * a);
            int nearestEdgeBottom = movementCalculator, nearestEdgeTop = movementCalculator - 1;
            while (true) {
                if (nearestEdgeBottom % MainFrame.worldSize != 0) {
                    nearestEdgeBottom++;
                } else if (nearestEdgeTop % (MainFrame.worldSize) != 0) {
                    nearestEdgeTop--;
                } else {
                    break;
                }

            }
            for (int i = -height ; i <= height ; i++) {
                if (movementCalculator + i < MainFrame.nodeClasses.size() && movementCalculator + i >= 0) {
                    if (((movementCalculator + i != 0) && ((movementCalculator + i) % (MainFrame.worldSize + 1) != 0))) {
                        if (isItAPrey && !lookingForMate){
                            if ((movementCalculator + i) < (nearestEdgeBottom) && (movementCalculator + i) >= (nearestEdgeTop) &&  !MainFrame.nodeClasses.get(movementCalculator + i).getOccupation()) {
                                knownArea.add(MainFrame.nodeClasses.get(movementCalculator + i));
                            }
                        } else {
                            if ((movementCalculator + i) < (nearestEdgeBottom) && (movementCalculator + i) >= (nearestEdgeTop)) {
                                knownArea.add(MainFrame.nodeClasses.get(movementCalculator + i));
                            }
                        }
                        //{DEBUG PURPOSES}
                        //System.out.println("MOvement = " + (movementCalculator + i));
                        //System.out.println("bot = " + nearestEdgeBottom + " " + ((movementCalculator + i) < nearestEdgeBottom) + "  top  = " + nearestEdgeTop + " " + ((movementCalculator + i) > (nearestEdgeTop)));
                        // + ((movementCalculator + i) > nearestEdgeTop));
                    }

                }
            }
        }
        foundResource = false;
        //the code will proceed to the resource detector method which will see whether the resource is detected in known area
        switch (type) {
            case BERRYBUSH:
                resourceDetector(NodeClass.TYPE.BERRYBUSH);
                break;
            case WATER:
                resourceDetector(NodeClass.TYPE.WATER);
                break;

            case OTHER:
                resourceDetector(NodeClass.TYPE.OTHER);
                break;

        }
        Random rand  = new Random();
        if (!this.foundResource && knownArea.size() > 1) {
            createPathing(rand.nextInt(MainFrame.worldSize * MainFrame.worldSize));
            if (time4 == 0){time4 = 1;}
        }
    }
        private void resourceDetector(NodeClass.TYPE type){
        //based on what the target resource is, the animal will see if the resource is within range. if it is, they will take the position and check others for closer ones
        if (type != NodeClass.TYPE.OTHER) {
            foundResource = false;
            int closestResource = 0;
            boolean foundResource = false;
            int closestXDiff = 99999, closestYDiff = 99999, xDiff, yDiff;
             //{debug}
             // for (int a = 0; a <= MainFrame.worldSize * MainFrame.worldSize; a++) {
             //   MainFrame.terrainBlocks.get(a).materials.get(0).set(ColorAttribute.createDiffuse(Color.GREEN));
             //}
            for (NodeClass a : knownArea) {
                if (a.getType() == type) {
                    xDiff = (MainFrame.terrainCoordinates.get(a.getLocationIndex()).getX() - MainFrame.terrainCoordinates.get(position).getX());
                    yDiff = ((MainFrame.terrainCoordinates.get(a.getLocationIndex()).getY() - MainFrame.terrainCoordinates.get(position).getY()));
                    if (xDiff < closestXDiff && yDiff < closestYDiff) {
                        closestXDiff = xDiff;
                        closestYDiff = yDiff;
                        closestResource = a.getLocationIndex();
                        foundResource = true;
                    }
                } else {
                    if (type == NodeClass.TYPE.BERRYBUSH) actionState = "searchingForFood()";
                    else if (type == NodeClass.TYPE.WATER) actionState = "searchingForWater()";
                }
             // {debug }MainFrame.terrainBlocks.get(a.getLocationIndex()).materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
            }
            if (foundResource) {
                if (type == NodeClass.TYPE.BERRYBUSH) actionState = "goingToFood()";
                else if (type == NodeClass.TYPE.WATER) actionState = "goingToWater()";
                this.foundResource = true;
                createPathing(closestResource);
            }
        }
        else {
            foundResource = false;
            int closestResource = 0;
            boolean foundResource = false;
            int closestXDiff = 99999, closestYDiff = 99999, xDiff, yDiff;
            //for (int a = 0; a <= MainFrame.worldSize * MainFrame.worldSize; a++) {
            //    MainFrame.terrainBlocks.get(a).materials.get(0).set(ColorAttribute.createDiffuse(Color.GREEN));
            //}
            for (NodeClass a : knownArea) {
               // System.out.println("Looking for mate" + lookingForMate);
                if (!lookingForMate) {
                    if (a.isOccupationIsPrey() && a.getOccupantIndex() != -1) {
                        //System.out.println("FOUND PREY AREA");
                        xDiff = (MainFrame.terrainCoordinates.get(a.getLocationIndex()).getX() - MainFrame.terrainCoordinates.get(position).getX());
                        yDiff = ((MainFrame.terrainCoordinates.get(a.getLocationIndex()).getY() - MainFrame.terrainCoordinates.get(position).getY()));
                        if (xDiff < closestXDiff && yDiff < closestYDiff) {
                            targetPreyIndex = a.getOccupantIndex();
                            closestXDiff = xDiff;
                            closestYDiff = yDiff;
                            closestResource = a.getLocationIndex();
                            foundResource = true;
                        }
                    } else {
                        actionState = "searchingForPrey()";
                    }
                    //MainFrame.terrainBlocks.get(a.getLocationIndex()).materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
                } else {
                //    System.out.println("ran this");
                    if (MainFrame.nodeClasses.get(a.getLocationIndex()).getOccupantIndex() != -1 &&MainFrame.nodeClasses.get(a.getLocationIndex()).getOccupantIndex() < MainFrame.animals.size() ) {
                       // System.out.println("founded");
                        if (MainFrame.animals.get(a.getOccupantIndex()).Gender == gender.female
                                && !MainFrame.animals.get(a.getOccupantIndex()).isPregnant()
                                && !MainFrame.animals.get(a.getOccupantIndex()).isMating()
                                && !MainFrame.animals.get(a.getOccupantIndex()).isChild()) {
                            if ((this.type.equals("rabbit") && MainFrame.animals.get(a.getOccupantIndex()).type.equals("rabbit")) ||
                                    (this.type.equals("wolf") && MainFrame.animals.get(a.getOccupantIndex()).type.equals("wolf")) ) {
                                xDiff = (MainFrame.terrainCoordinates.get(a.getLocationIndex()).getX() - MainFrame.terrainCoordinates.get(position).getX());
                                yDiff = ((MainFrame.terrainCoordinates.get(a.getLocationIndex()).getY() - MainFrame.terrainCoordinates.get(position).getY()));
                                if (xDiff < closestXDiff && yDiff < closestYDiff) {
                                    System.out.println("found mate");
                                    closestXDiff = xDiff;
                                    closestYDiff = yDiff;
                                    closestResource = a.getLocationIndex();
                                    foundResource = true;
                                    targetMateOccupantIndex = MainFrame.nodeClasses.get(closestResource).getOccupantIndex();
                                }
                            }
                        }
                        } else {
                            actionState = "searchingForMate()";
                        }

                }
            }

                if (foundResource) {
                    if(!lookingForMate) {actionState = "chasingPrey()";}
                    else {actionState = "goingToMate()";}
                    this.foundResource = true;
                    createPathing(closestResource);
                    //System.out.println("FOUND PREY"); {debug}
                    //then creates a path towards the resource

                }

        }

        }

        private void createPathing(int location){
        //using the position of the target resource, this method will create the shortest path to reach said resource
            int xGoal = MainFrame.terrainCoordinates.get(location).getX();
            int yGoal = MainFrame.terrainCoordinates.get(location).getY();
            int xPos = MainFrame.terrainCoordinates.get(position).getX();
            int yPos = MainFrame.terrainCoordinates.get(position).getY();
            resourceLocation = location;
            int xNetMove = xGoal - xPos, yNetMove = yGoal - yPos;
            while (xPos != xGoal || yPos != yGoal){
                int xNetMove2 = xGoal - xPos, yNetMove2 = yGoal - yPos;
                if (xNetMove2 != 0) {xPos += (xNetMove / abs(xNetMove));}
                if (yNetMove2 != 0) {yPos += (yNetMove / abs(yNetMove));}
                //{DEBUG}
                //System.out.println("Goal x = " + (xPos == xGoal));
                //System.out.println("Goal y = " + (yPos == yGoal));
                    for (TerrainCoordinates a: MainFrame.terrainCoordinates){
                        Vector3 pathpos = new Vector3();
                        if (a.getX() == xPos && a.getY() == yPos){
                            MainFrame.terrainBlocks.get(a.getLocationIndex()).transform.getTranslation(pathpos);
                            pathing.add(pathpos); pathingIndex ++;
                        }
                    }
                    //{debug}
                //System.out.println(xPos + "  " + yPos);
            }
            //{debug}
            //System.out.println(pathing.size());
            System.out.println(type + "  " + pathing.size());
            if (pathing.size() > 1) {movePrompt = true;}
            if (pathing.size() < 2){ if (isItAPrey || MainFrame.nodeClasses.get(resourceLocation).getType() == NodeClass.TYPE.WATER && !mating) {consumingResource = true;}}
        }


    private void updateMove(Vector3 vectorBlock) {
        //this code will run constantly until the path to the resource is complete or it will stop if something interrupts it
        knownArea.clear();;
        //{debug}
        //move = true;
       // System.out.println("ran");
        vectorPosition = new Vector3();
        rabbitInstance.transform.getTranslation(vectorPosition);
        if ((!foundResource && time4 > 2)){
            move = false;
            lookingForMate = false;
            usageIndex = 0;
            pathingIndex = 0;
            movePrompt = false;
            pathing.clear();
            time4 = 0;
            whatToDo();
        }

        float xMove = vectorBlock.x - vectorPosition.x;
        float zMove = vectorBlock.z - vectorPosition.z;

                if (xMove != 0.0) {vectorPosition.x += (xMove / abs(xMove));}
                if (zMove != 0.0) {vectorPosition.z += (zMove / abs(zMove));}
                //{debug}
       // System.out.println("X at x" + (vectorPosition.x == vectorBlock.x) + "  " + xMove);
       // System.out.println("Y at y" + (vectorPosition.z == vectorBlock.z) + "  " + zMove);
                if (usageIndex + 1 == pathingIndex ) {
                    if (MainFrame.nodeClasses.get(resourceLocation).getType() == NodeClass.TYPE.BERRYBUSH && !MainFrame.nodeClasses.get(resourceLocation).getOccupation() ||
                            MainFrame.nodeClasses.get(resourceLocation).getType() == NodeClass.TYPE.WATER && !MainFrame.nodeClasses.get(resourceLocation).getOccupation()) {
                       // System.out.println("runnigngesgegesg"); {debug}
                        consumingResource = true;
                        MainFrame.nodeClasses.get(resourceLocation).setOccupation(true);
                        if (isItAPrey) {MainFrame.nodeClasses.get(position).setOccupationIsPrey(true);}
                    } else if (!isItAPrey && !lookingForMate){
                        if (MainFrame.nodeClasses.get(resourceLocation).isOccupationIsPrey() && MainFrame.nodeClasses.get(resourceLocation).getOccupantIndex() != -1 &&  targetPreyIndex < MainFrame.animals.size()){
                            MainFrame.animals.get(targetPreyIndex).setAlive(false);
                            food += 50;
                            if (food >= 100) {food = 100;}
                        }
                        } else if (MainFrame.nodeClasses.get(resourceLocation).getOccupantIndex() != -1){
                        if (lookingForMate &&
                                !MainFrame.animals.get(MainFrame.nodeClasses.get(resourceLocation).getOccupantIndex()).isMating()
                                && !MainFrame.animals.get(MainFrame.nodeClasses.get(resourceLocation).getOccupantIndex()).isPregnant()) {

                            MainFrame.animals.get(targetMateOccupantIndex).setMating(true);
                            lookingForMate = false;
                            mating = true;
                        }
                    }
                    move = false;
                    usageIndex = 0;
                    pathingIndex = 0;
                    movePrompt = false;
                    pathing.clear();
                }

            rabbitInstance.transform.setTranslation(vectorPosition.x, vectorPosition.y, vectorPosition.z);
            for (TerrainCoordinates a : MainFrame.terrainCoordinates){
                  //  System.out.println((a.getzPos() == vectorPosition.z) + "  " + (a.getxPos() == vectorPosition.x) ); {debug}
                    if (a.getxPos() == vectorPosition.x && a.getzPos() == vectorPosition.z){
                        position = a.getLocationIndex();
                       // System.out.println("RANING"); {debug}
                        break;
                    }
            }
            int occupationCounter = 0;
            for (NodeClass a: MainFrame.nodeClasses){
                if (a.isOccupationIsPrey()){
                    occupationCounter ++;
                }
            }
       // System.out.println("OCCUPIED BY PREY = " + occupationCounter); {debug}

            if (vectorBlock.x == vectorPosition.x && vectorBlock.z == vectorPosition.z) {
                for (TerrainCoordinates a : MainFrame.terrainCoordinates) {
                    //  System.out.println((a.getzPos() == vectorPosition.z) + "  " + (a.getxPos() == vectorPosition.x) ); {debug}
                    if (a.getxPos() == vectorPosition.x && a.getzPos() == vectorPosition.z) {
                        position = a.getLocationIndex();
                        //  System.out.println("RANING"); {debug}
                       break;
                    }
                }
                time2 += 0.015; //this code will run based on the speed of each animal, which sets their placement accordingly once prompted
                if ((type.equals("rabbit") && time2 > rabbitSpeed) || (type.equals("wolf") && time2 > wolfSpeed) ) {
                    time2 = 0;
                  //  System.out.println("previous position" + previousPosition);
                    if (previousPosition != 0) {MainFrame.nodeClasses.get(previousPosition).setOccupation(false);}

                    MainFrame.nodeClasses.get(previousPosition).setOccupantIndex(-1);

                    if (isItAPrey) {
                        if (previousPosition != 0) {
                            MainFrame.nodeClasses.get(previousPosition).setOccupationIsPrey(false);
                        }
                            previousPosition = position;
                            MainFrame.nodeClasses.get(position).setOccupationIsPrey(true);
                    } else {
                        previousPosition = position;
                    }
                    MainFrame.nodeClasses.get(position).setOccupantIndex(indexNum);
                    MainFrame.nodeClasses.get(position).setOccupation(true);
                    usageIndex++;
                }

            }

    }
}
