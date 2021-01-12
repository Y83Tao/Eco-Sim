package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;

class NodeClass {
    //develops values for each block on terrain. Used to determine if a resource is on a block and if there is an animal there and other data
    enum TYPE {TREE, WATER, BERRYBUSH, NOBERRIESBUSH, OTHER}
    private TYPE type; private Vector3 vectorPos;
    private ModelInstance modelInstance;
    private boolean occupation;

    private boolean occupationIsPrey;
    private int occupantIndex;
    private boolean isWater;
    private boolean regenBerryBush;
    private boolean noNodeExists;

    private int locationIndex;
    private float regenBerryBushTimer;
    NodeClass(TYPE type, Vector3 vectorPos, int locationIndex){
        //runs and sets up values and assets based on their resource type.
        occupantIndex = -1;
        this.type = type; this.vectorPos = vectorPos; this.locationIndex = locationIndex;
        Model model;
        switch(type){
            case TREE:
                model = MainFrame.assetManager.get("treeLowPoly.g3db", Model.class);
                modelInstance = new ModelInstance(model);
                modelInstance.transform.setToScaling(0.03f, 0.03f, 0.03f);
                vectorPos.y += 2f;
                modelInstance.transform.setTranslation(vectorPos);
                break;
            case WATER:
                MainFrame.terrainBlocks.get(locationIndex).transform.translate(0, -1f, 0);
                MainFrame.terrainBlocks.get(locationIndex).materials.get(0).set(ColorAttribute.createDiffuse(Color.BLUE));
                isWater = true;
                break;
            case BERRYBUSH:
                modelInstance = new ModelInstance(MainFrame.assetManager.get("BushWithBerrys01.g3db", Model.class));
                modelInstance.transform.setToScaling(0.04f, 0.04f, 0.04f);
                vectorPos.y += 2f;
                vectorPos.z += -6.5f;
                modelInstance.transform.setTranslation(vectorPos);
                break;
            case OTHER: noNodeExists = true; break;
        }
    }

    void changeToBushWithoutBerries(){ //changes to the bush without berries model and scales it to fit on the terrain
        modelInstance = new ModelInstance(MainFrame.assetManager.get("Bush01.g3db", Model.class));
        modelInstance.transform.setToScaling(0.04f, 0.04f, 0.04f);
        setType(TYPE.NOBERRIESBUSH);
        vectorPos.x += -6.5f;
        modelInstance.transform.setTranslation(vectorPos);
        regenBerryBush = true;
    }
    private void changeToBushWithBerries(){ //changes back once the regeneration process is complete
        modelInstance = new ModelInstance(MainFrame.assetManager.get("BushWithBerrys01.g3db", Model.class));
        modelInstance.transform.setToScaling(0.04f, 0.04f, 0.04f);
        setType(TYPE.BERRYBUSH);
        vectorPos.x += 6.5f;
        modelInstance.transform.setTranslation(vectorPos);
    }
    //getters and setters for various variables that is used by animal class to execute code based on these

    boolean getOccupation(){
        return occupation;
    }

    int getLocationIndex() {
        return locationIndex;
    }

    void setOccupation(boolean occupation){
        this.occupation = occupation;
    }

    boolean isOccupationIsPrey() {
        return occupationIsPrey;
    }

    void setOccupationIsPrey(boolean occupationIsPrey) {
        this.occupationIsPrey = occupationIsPrey;
    }

    int getOccupantIndex() {
        return occupantIndex;
    }

    void setOccupantIndex(int occupantIndex) {
        this.occupantIndex = occupantIndex;
    }

    TYPE getType(){
        return type;
    }

    private void setType(TYPE type){
        this.type = type;
    }

    void NodeClassObjectRender(){ //renders the model whether it is a tree berry bush, etc and has a timer for regeneration of berry bushes
        if (!noNodeExists && !isWater ) {
            MainFrame.modelBatch.begin(MainFrame.cam);
            MainFrame.modelBatch.render(modelInstance, MainFrame.environment);
            MainFrame.modelBatch.end();
        }
        if (regenBerryBush){
            regenBerryBushTimer += Gdx.graphics.getDeltaTime();
            if (regenBerryBushTimer >= 45){
                regenBerryBush = false;
                regenBerryBushTimer = 0;
                changeToBushWithBerries();
            }
        }
    }
}
