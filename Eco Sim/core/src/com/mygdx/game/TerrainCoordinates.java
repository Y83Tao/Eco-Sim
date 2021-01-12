package com.mygdx.game;

public class TerrainCoordinates {
    private int x;
    private int y;
    private float xPos;
    private float zPos;
    private int locationIndex;
    TerrainCoordinates(int x, int y, float xPos, float zpos, int locationIndex){ //runs on each block which gives it proper x y cords and their vector pos
        this.x = x;
        this.y = y;
        this.xPos =xPos;
        this.zPos = zpos;
        this.locationIndex = locationIndex;
    }
    //getters and setters for x y pos and their vector positions

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    int getLocationIndex(){
        return locationIndex;
    }

    float getxPos() {
        return xPos;
    }

    float getzPos() {
        return zPos;
    }
}
