package com.example.logicsimulator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.lang.reflect.Array;
import java.util.ArrayList;

//This class represents any element that is positioned as a grid point
class Node {
    Paint paint = new Paint();
    Point position;

    Node(){}
    Node(Point in){
        this.position = in;
    }

    //This method prints the grid lines of a cell
    void printGrid(Canvas myCanvas, int gridBlockSize){
        // Change the paint color
        paint.setColor(Color.argb(255, 5*position.x, 10*position.y, 255-(position.x*position.y/2)));
        // Draw Vertical Line
        myCanvas.drawLine(
                gridBlockSize * position.x,
                gridBlockSize * position.y,
                gridBlockSize * position.x,
                (gridBlockSize * position.y ) + gridBlockSize,
                paint);
        // Draw Horizontal Line
        myCanvas.drawLine(
                gridBlockSize * position.x,
                gridBlockSize * position.y,
                (gridBlockSize * position.x) + gridBlockSize,
                gridBlockSize * position.y,
                paint);
    }
}

/*
    This class represents the larger grid elements on the screen.
    Either a CircuitElement or a Button
*/

abstract class ElementOrButton extends Node{
    int textSize = 25;
    int blockSize;
    String label = "";
    ElementOrButton(){}
}

//-------------------------------------------------------------------------------------------------
//This class represents the circuit elements on the screen
//The classes that extend this class handle both the visual and functional
//methods of each gate.
class CircuitElement extends ElementOrButton{
    CircuitElement a;
    Node outputNode;
    ArrayList<Node>inputNodes;

    CircuitElement(){}
    CircuitElement(Point in, int blockSize){
        updatePosition(in);
        this.blockSize = blockSize;
        this.label = "Element";
    }
    //Front end Methods:--------------------------------------------------

    //This method is for printing each elements color and label.
    void idle(Canvas myCanvas, int colorMod){
        colorMod++;
        paint.setColor(Color.argb(150, 25*colorMod, 30, 255/colorMod));
        color(myCanvas);
        paint.setColor(Color.argb(255, 255, 255, 255));
        draw(myCanvas);
    }

    void draw(Canvas myCanvas){
        paint.setColor(Color.argb(255, 0, 0, 0));
        paint.setTextSize (textSize);
        myCanvas.drawText(label, position.x*blockSize+blockSize/8, position.y*blockSize+blockSize/2, paint);
    }
    void color(Canvas myCanvas){
        myCanvas.drawRect(position.x * blockSize,
                position.y * blockSize,
                (position.x * blockSize) + blockSize,
                (position.y * blockSize)+ blockSize,
                paint );
    }

    //This method is for coloring a label white when an element is selected
    void select(Canvas myCanvas){
        paint.setColor(Color.argb(150, 0, 0, 255));
        color(myCanvas);
    }

    void updatePosition(Point in){position= new Point (in.x,in.y); update();}
    void update(){}

    //Back end Methods:----------------------------------
    void setA(CircuitElement a){
        this.a = a;
    }
    boolean eval(){return a.eval();}
    boolean checkPosition(Point in){
        return (in.x == position.x && in.y == position.y);
    }
}

class SWITCH extends  CircuitElement{
    private boolean state =false;

    SWITCH(Point in, int blockSize){
        this.blockSize = blockSize;
        this.label = "0";
        position = in;
        update();
    }

    void update(){
        //reset our output node
        this.outputNode = new Node (new Point (position.x*3+3, position.y*3+1));
    }

    void toggle() {
        if (label.equals("0")) {
            label = "1";
        } else
            label = "0";
        this.state = !this.state;
    }

    boolean eval(){return state;}
}

class LED extends  CircuitElement{
    LED(Point in, int blockSize){
        this.blockSize = blockSize;
        this.label = "LED";
        position = in;
        update();
    }
    void update(){
        //reset our input nodes
        this.inputNodes = new ArrayList<>();
        //Add the new input nodes at our new position
        this.inputNodes.add(new Node (new Point (position.x*3, position.y*3+1)));
    }
    void lightUp(Canvas canvas){
        paint.setColor(Color.argb(255, 255, 255, 150));
        color(canvas);
    }
}

//Each gate has its own Bitmap Image
abstract class GATE extends CircuitElement{
    //Temp is used to resize a bitmap to a given gate size.
    Bitmap temp, icon;

    @Override
    void draw(Canvas myCanvas){
        myCanvas.drawBitmap(icon, position.x*blockSize, position.y*blockSize, paint);
    }
}

class NOTGATE extends  GATE {
    NOTGATE(Point in, Context context, int blockSize){
        this.blockSize = blockSize;
        position = in;
        setBitmap(context);
        update();
    }

    private void setBitmap(Context context) {
        temp = BitmapFactory.decodeResource(context.getResources(), R.drawable.not);
        icon = Bitmap.createScaledBitmap(temp, blockSize, blockSize, false);
    }

    void update(){
        //reset our output node
        this.outputNode = new Node (new Point (position.x*3+3, position.y*3+1));
        //reset our input nodes
        this.inputNodes = new ArrayList<>();
        //Add the new input nodes at our new position
        this.inputNodes.add(new Node (new Point (position.x*3, position.y*3+1)));
    }

    public boolean eval(){return !this.a.eval();}
}

//This class is used for the AND and OR gates.
//In its constructor, it creates two input node Points.
//It also has a CircuitElement b for the second input.
abstract class TwoInOneOut extends  GATE{
    CircuitElement b;

    void setB (CircuitElement b){this.b = b;}

    void update(){
        //reset our output node
        this.outputNode = new Node (new Point (position.x*3+3, position.y*3+1));
        //reset our input nodes
        this.inputNodes = new ArrayList<>();
        //Add the new input nodes at our new position
        this.inputNodes.add(new Node (new Point (position.x*3, position.y*3)));
        this.inputNodes.add(new Node (new Point (position.x*3, position.y*3+2)));
    }

}

class ANDGATE extends TwoInOneOut{

    //bitmap to be resized into the icon bitmap

    ANDGATE(Point in, Context context, int blockSize){
        this.blockSize = blockSize;
        position = in;
        setBitmap(context);
        update();
    }

    private void setBitmap(Context context) {
        temp = BitmapFactory.decodeResource(context.getResources(), R.drawable.and);
        icon = Bitmap.createScaledBitmap(temp, blockSize, blockSize, false);
    }
    public boolean eval() {
        return a.eval() && b.eval();
    }

}

class ORGATE extends  TwoInOneOut{
    ORGATE(Point in, Context context, int blockSize){
        this.blockSize = blockSize;
        position = in;
        setBitmap(context);
        update();
    }

    private void setBitmap(Context context) {
        temp = BitmapFactory.decodeResource(context.getResources(), R.drawable.or);
        icon = Bitmap.createScaledBitmap(temp, blockSize, blockSize, false);
    }
    public boolean eval() {
        return a.eval() || b.eval();
    }

}

//-------------------------------------------------------------------------------------------------
//Buttons
abstract class Button extends ElementOrButton{
    Button(){}

    void printLabel(int blockSize, Canvas myCanvas, int gridHeight) {

        paint.setColor(Color.argb(255, 0, 0, 0));

        paint.setTextSize (textSize);

        myCanvas.drawText(label, position.x*blockSize+blockSize/8, gridHeight+blockSize/2, paint);
    }

    void printButtons(Canvas myCanvas, int buttonBlockSize, int gridHeight, int gridLength){
        // Change the paint color
        paint.setColor(Color.argb(255, 20*position.x, 10*position.y, 255-(position.x*position.y)));
        // Draw Vertical Line
        myCanvas.drawLine(
                buttonBlockSize * position.x,
                gridHeight,
                buttonBlockSize * position.x,
                gridHeight+buttonBlockSize, paint);
        // Draw Horizontal Line
        myCanvas.drawLine(
                0,
                gridHeight,
                gridLength,
                gridHeight,
                paint);

        printLabel(buttonBlockSize, myCanvas,gridHeight);
    }
    void select(int blockSize, Canvas myCanvas, int gridHeight){
        paint.setColor(Color.argb(255, 0, 0, 255));
        color(blockSize, myCanvas, gridHeight);
    }

    void color(int blockSize, Canvas myCanvas, int gridHeight){
        myCanvas.drawRect(position.x * blockSize,
                gridHeight,
                (position.x * blockSize) + blockSize,
                gridHeight+ blockSize,
                paint );

        printLabel(blockSize, myCanvas, gridHeight);
    }
}

//Each button class has a unique label value based on their name.
class ADD extends Button {
    ADD(int x) {
        position = new Point(x,0);
        this.label = "ADD";
    }
}


class SUB extends Button{
    SUB(int x) {
        position = new Point(x,0);
        this.label = "SUB";
    }
}

class AND extends Button{
    AND(int x) {
        position = new Point(x,0);
        this.label = "AND";
    }
}

class OR extends Button{
    OR(int x) {
        position = new Point(x,0);
        this.label = "OR";
    }
}

class NOT extends Button{
    NOT(int x) {
        position = new Point(x,0);
        this.label = "NOT";
    }
}

class WIRE extends Button{
    WIRE(int x) {
        position = new Point(x,0);
        this.label = "WIRE";
    }

}

class SWITCHBUTTON extends Button{
    SWITCHBUTTON(int x) {
        position = new Point(x,0);
        this.label = "SWITCH";
    }
}

class LEDBUTTON extends Button{
    LEDBUTTON(int x) {
        position = new Point(x,0);
        this.label = "LED";
    }
}

class TOGGLE extends Button{
    TOGGLE(int x) {
        position = new Point(x,0);
        this.label = "TOGGLE";
    }
}


class A extends  Button{
    A(int x) {
        position = new Point(x,0);
        this.label = "A";
    }
}

class B extends  Button{
    B(int x) {
        position = new Point(x,0);
        this.label = "B";
    }
}

class C extends  Button{
    C(int x) {
        position = new Point(x,0);
        this.label = "C";
    }
}
class UNDO extends Button{
    UNDO(int x) {
        position = new Point(x,0);
        this.label = "UNDO";
    }
}

class REDO extends Button{
    REDO(int x) {
        position = new Point(x,0);
        this.label = "REDO";
    }
}

//button to create an example of the AND schematic
class XAND extends Button {
    XAND(int x) {
        position = new Point(x, 0);
        this.label = "XAND";
    }
}

//These buttons can change their color based on its state.
abstract class SavedClick extends Button{
    boolean state = false;
    void toggle(){state=!state;}
    //Same as Button class printButtons but changes the color to red if state is true
    @Override
    void printButtons(Canvas myCanvas, int buttonBlockSize, int gridHeight, int gridLength){
        // Change the paint color
        if(state)
            paint.setColor(Color.argb(255, 255, 0, 0));
        else
            paint.setColor(Color.argb(255, 255, 255, 255));

        color(buttonBlockSize, myCanvas, gridHeight);

        // Draw Vertical Line
        myCanvas.drawLine(
                buttonBlockSize * position.x,
                gridHeight,
                buttonBlockSize * position.x,
                gridHeight+buttonBlockSize, paint);
        // Draw Horizontal Line
        myCanvas.drawLine(
                0,
                gridHeight,
                gridLength,
                gridHeight,
                paint);

        printLabel(buttonBlockSize, myCanvas,gridHeight);
    }
}

class Save extends SavedClick {
    Save(int x) {
        position = new Point(x,0);
        this.label = "Save";
    }

}

class PLAY extends SavedClick {
    PLAY(int x) {
        position = new Point(x,0);
        this.label = "PLAY";
    }

}
