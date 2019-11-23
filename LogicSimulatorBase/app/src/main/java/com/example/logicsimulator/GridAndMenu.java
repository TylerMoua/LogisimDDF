package com.example.logicsimulator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Stack;


/*
    This class handles all the operations that take place on the grid and menu
    The class stores information about the grid as well as the objects that
    exist and interact on the grid.
 */
class GridAndMenu {
    //These points tell the system if we are in a given state(See Design Doc)
    // based on if they == null or have a value.
    Point selectedElement, selectedNode;
    private Point selectedButton;

    private boolean playing, saving, introducing = false;
    private Context context;
    private Canvas myCanvas;
    private Paint paint = new Paint();
    private final int numberOfCircuitElements = 10;
    private final int numberOfButtons = 18;
    private final int numberOfHorizontalCells = 30;
    private final int numberOfVerticalCells = 15;
    private int numberOfActiveElements = 0;
    private int numberOfSavableSchematic = 3;

    Stack<Schematic> undoStack = new Stack<>();
    Stack<Schematic> redoStack = new Stack<>();


    private Schematic[] savedSchematics = new Schematic[numberOfSavableSchematic];


    private Button[] menu = {new PLAY(0), new ADD(1), new SUB(2), new WIRE(3)
            , new AND(4), new OR(5), new NOT(6), new SWITCHBUTTON(7)
            , new LEDBUTTON(8), new TOGGLE(9), new Save(10), new A(11), new B(12), new C(13), new UNDO(14), new REDO(15), new NAND(16), new INTRO(17)};

    private Node[][] cells =
            new Node[numberOfHorizontalCells][numberOfVerticalCells];

    Schematic elements;

    //largeCellSize: Circuit elements
    // menuCellSize: Buttons
    //smallCellSize: Grid nodes.
    int gridHeight, largeCellSize, smallCellSize, menuCellSize;
    private int gridLength;


    GridAndMenu(Context context,int numberOfHorizontalPixels, Bitmap blankBitMap) {

        this.context = context;
        this.myCanvas = new Canvas(blankBitMap);

        //Set the size of the grid cells and nodes:
        smallCellSize = numberOfHorizontalPixels / numberOfHorizontalCells;

        menuCellSize = numberOfHorizontalPixels/ numberOfButtons;

        //set the grid block sizes
        int cellRatio = numberOfHorizontalCells / numberOfCircuitElements;

        largeCellSize = smallCellSize * cellRatio;
        gridHeight = smallCellSize * numberOfVerticalCells;
        gridLength = smallCellSize * numberOfHorizontalCells;

        elements = new Schematic(numberOfCircuitElements, largeCellSize);
        populate();
    }

    //Fills the array of grid elements.
    private void populate() {
        for (int i = 0; i < numberOfHorizontalCells; i++) {
            for (int j = 0; j < numberOfVerticalCells; j++) {
                cells[i][j] = new Node(new Point (i, j));
            }
        }
    }

    //-------------------------------------------------------------------------------------------
    //Printing Methods

    //This is the main printing method.
    // It calls each individual printing/drawing method in a specific order
    void updateScreen() {
        debugUpdate();
        whiteOut();
        drawButtons();
        colorElements();
        drawGrid();
        printWires();
        updatePlay();
        updateSelection();
    }
    //This method may light up the LED based on the logic circuit.
    private void updatePlay(){
        if(playing){
            for (CircuitElement element : elements.circuit)
            {
                if(element instanceof LED){
                    if(element.eval()) {
                        ((LED)element).lightUp(myCanvas);
                    }else{
                        (element).idle(myCanvas, 10);
                    }
                }
            }
        }
    }

    //Toast Messages for On Screen Feedback - Ali
    private void onScreenToast(String prompt) {
        Toast toast = Toast.makeText(context,
                prompt, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    //Prints a debug message to indicate the state we are in.
    private void debugUpdate(){
        if(selectedButton == null)
            Log.d("Debugging", "Menu Selected: None"  + "\nElement Selected: " + selectedElement + "\nNode Selected:" + selectedNode);
        else
            Log.d("Debugging", "Menu Selected: " +  menu[selectedButton.x].label +"\nElement Selected: " + selectedElement + "\nNode Selected:" + selectedNode);
        Log.d("Debugging", "Undo Stack:");
        for (int i = 0; i < undoStack.size(); i++){
            Log.d("Debugging", "\n" + undoStack.elementAt(i).toString());
        }
        Log.d("Debugging", "Redo Stack:");
        for (int i = 0; i < redoStack.size(); i++){
            Log.d("Debugging", "\n" + redoStack.elementAt(i).toString());
        }
    }

    //This method whiteouts the screen
    private void whiteOut() {
        myCanvas.drawColor(Color.argb(255, 255, 255, 255));
    }

    //Draws the menu and labels for each button
    private void drawButtons() {
        for (int i = 0; i < numberOfButtons; i++)
            menu[i].printButtons(myCanvas, menuCellSize, gridHeight, gridLength);
    }

    //Draws the grid lines
    private void drawGrid() {
        for (int i = 0; i < numberOfHorizontalCells; i++) {
            for (int j = 0; j < numberOfVerticalCells; j++) {
                cells[i][j].printGrid(myCanvas, smallCellSize);
            }
        }
    }

    //Colors in the circuit elements on the grid for visibility
    private void colorElements() {
        if(elements.circuit!= null) {
            for (int i = 0; i < elements.circuit.length; i++) {
                if (elements.circuit[i] != null) {
                    elements.circuit[i].idle(myCanvas, i);
                }
            }
        }
    }

    //Prints the wire connections between circuit elements.
    private void printWires() {
        //For all of the possible element
        if (elements.circuit != null) {
            for (CircuitElement element : elements.circuit) {
                if (element != null) {
                    //if an element exists
                    if (element.a != null) {
                        drawWire(element.a.outputNode.position, element.inputNodes.get(0).position, 0, element.getClass().toString());
                    }
                    if (element instanceof TwoInOneOut) {
                        if (((TwoInOneOut) element).b != null) {
                            drawWire(((TwoInOneOut) element).b.outputNode.position, element.inputNodes.get(1).position, 1, element.getClass().toString());
                        }
                    }
                }
            }
        }
    }

    //Draws a line from an output node to an input node based on
    //The type of gate and which input node is being wired.
    private void drawWire(Point start, Point end, int node, String type){
        if (start != null && end != null) {
            if (node == 0) {
                if (type.equals("class com.example.logicsimulator.NOTGATE")||
                        type.equals("class com.example.logicsimulator.LED")) {
                    //wire to the center of the element
                    myCanvas.drawLine(
                            smallCellSize * start.x,
                            (smallCellSize * start.y) + smallCellSize / 2,
                            smallCellSize * end.x,
                            (smallCellSize * end.y) + smallCellSize / 2,
                            paint);


                } else {
                    //Wire to node 0, in the top of the element
                    myCanvas.drawLine(
                            smallCellSize * start.x,
                            (smallCellSize * start.y) + smallCellSize / 2,
                            smallCellSize * end.x,
                            (smallCellSize * end.y) + smallCellSize,
                            paint);

                }
            } else if (node == 1) {
                myCanvas.drawLine(
                        smallCellSize * start.x,
                        (smallCellSize * start.y) + smallCellSize / 2,
                        smallCellSize * end.x,
                        smallCellSize * end.y,
                        paint);
            }
        }
    }

    //Colors an element to indicate that it has been selected
    private void updateSelection() {
        if (selectedElement != null) {
            elements.circuit[elements.getElement(selectedElement)].select(myCanvas);
        }
        if (selectedButton != null) {
            menu[selectedButton.x].select(menuCellSize, myCanvas, gridHeight);
        }
    }

    //-------------------------------------------------------------------------------------------
    //This method hands the users menu button selection based on their touch
    void menuSelect(Point touchPoint) {
        selectedButton = touchPoint;
        menu[selectedButton.x].select(menuCellSize, myCanvas, gridHeight);
        processMenu(selectedButton);
    }

    //This element processes a menu selection and calls a "Button Menu Processing" method to handle
    //the selection
    private void processMenu(Point input)  {
        int buttonNumber = input.x;
        switch (buttonNumber) {
            //----------------------------------------------------------------------------
            case 0: //PLAY BUTTON
                play();
                if(playing)
                    onScreenToast("Circuit is Running");
                else
                    onScreenToast("Circuit Stopped");
                break;
            //-------------------------------------------------------------------------------
            case 1: //ADD BUTTON
                if(!playing){
                    pushToUndo();
                    add();
                    onScreenToast("Element Added");
                }
                break;
            //---------------------------------------------------------------------------------
            case 2: //SUB BUTTON
                if(!playing) {
                    pushToUndo();
                    sub();
                }
                break;
            //-----------------------------------------------------------------------------
            case 3: //Wire BUTTON
                if(!playing) {
                    pushToUndo();
                    wire();
                    if(numberOfActiveElements >= 2)
                        onScreenToast("Choose an Element to Wire To");
                }
                break;
            //------------------------------------------------------------------------
            case 4: //AND BUTTON
                pushToUndo();
                and();
                break;
            //------------------------------------------------------------------------
            case 5://OR BUTTON
                pushToUndo();
                or();
                break;
            //----------------------------------------------------------------------
            case 6: //NOT BUTTON
                pushToUndo();
                not();
                break;
            //----------------------------------------------------------------------
            case 7: //SWITCH BUTTON
                pushToUndo();
                inputSwitch();
                break;
            //-----------------------------------------------------------------
            case 8: //LED BUTTON
                pushToUndo();
                led();
                break;
            //--------------------------------------------------------------------
            case 9: // 1/0 BUTTON
                pushToUndo();
                toggle();
                break;
            //-----------------------------------------------------------------
            case 10: // Save Button
                if (!playing) {
                    if(numberOfActiveElements==0){
                        onScreenToast("Nothing to Save");
                    } else {
                        save();
                        onScreenToast("Choose A, B, C to Save Current Layout");
                    }

                }
                break;
            //-----------------------------------------------------------------
            case 11: // A Button

            //-----------------------------------------------------------------
            case 12: //B Button

            //-----------------------------------------------------------------
            case 13: // C Button
                if(!playing) {
                    saveOrLoad(buttonNumber);
                }
                break;
            //-----------------------------------------------------------------
            case 14: //Undo Button
                if(!playing){
                    if(undoStack.isEmpty()){
                        onScreenToast("Nothing to Undo");
                    } else {
                        undo();
                        onScreenToast("Undo");
                    }
                }
                break;
            //-----------------------------------------------------------------
            case 15: //Redo Button
                if(!playing){
                    if(redoStack.isEmpty()) {
                        onScreenToast("Nothing to Redo");
                    } else {
                        redo();
                        onScreenToast("Redo");
                    }
                }
                break;
            //-----------------------------------------------------------------
            case 16: //NAND Button
                nand();
                break;
            //-----------------------------------------------------------------

            case 17: //Intro Button
                intro();
                break;
        }
    }

    //------------------------------------------------------------------------------------------
    //Button Function Methods:

    //This method toggles the playing boolean value;
    private void play(){
        if(!elements.nullConnections()) {
            Log.d("Debugging", "Now Playing");
            playing = !playing;
            ((PLAY) menu[0]).toggle();
        }
        else
            onScreenToast("Not all elements connected");
    }

    //This method adds an element to the elements Array
    private void add() {
        if((elements.getElement(new Point(0,0))==-1) && numberOfActiveElements<numberOfCircuitElements) {
            elements.add();
            numberOfActiveElements++;
            Log.d("Debugging", "Current Elements:" + numberOfActiveElements);
        } else
            Log.d("Debugging","No Element Added, Space occupied OR Too many element");
    }

    //This method removes an element from the elements Array and removes wire connections
    private void sub() {
        if (selectedElement != null) {
            Log.d("Debugging", "Element Subtracted");
            elements.sub(selectedElement);
            selectedElement = null;
            numberOfActiveElements--;
            onScreenToast("Element Subtracted");
        } else
            Log.d("Debugging", "No Element Subtracted");
    }



    //This method selects the output node of the selected element.
    //Note: Now that a node has been selected, out state has changed.
    // the next element that is selected will be wired.
    private void wire()  {
        if (selectedElement != null && elements.circuit[elements.getElement(selectedElement)].outputNode!=null) {
            selectedNode = elements.circuit[elements.getElement(selectedElement)].outputNode.position;
            Log.d("Debugging", "Output Node selected at:" + selectedNode);
        }
    }

    //The following methods (from and to led) changes an unclassified circuit element into
    //each methods respective circuit elements
    private void and() {
        if (selectedElement != null
                && elements.circuit[elements.getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements.circuit[elements.getElement(selectedElement)] = new ANDGATE(selectedElement, context, largeCellSize);
            selectedElement = null;
            onScreenToast("AND Gate created");
        }
    }

    private void nand() {
        if (selectedElement != null
                && elements.circuit[elements.getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements.circuit[elements.getElement(selectedElement)] = new NANDGATE(selectedElement, context, largeCellSize);
            selectedElement = null;
            onScreenToast("NAND Gate created");
        }
    }

    private void or() {
        if (selectedElement != null
                && elements.circuit[elements.getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements.circuit[elements.getElement(selectedElement)] = new ORGATE(selectedElement, context, largeCellSize);
            selectedElement = null;
            onScreenToast("OR Gate Created");

        }
    }

    private void not(){
        if(selectedElement!=null
                && elements.circuit[elements.getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements.circuit[elements.getElement(selectedElement)] = new NOTGATE(selectedElement, context, largeCellSize);
            selectedElement = null;
            onScreenToast("NOT Gate Created");

        }
    }

    private void inputSwitch(){
        if (selectedElement != null
                && elements.circuit[elements.getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements.circuit[elements.getElement(selectedElement)] = new SWITCH(selectedElement, largeCellSize);
            selectedElement = null;
            onScreenToast("Switch Created");

        }
    }

    private void led() {
        Log.d("Debugging", "LED");
        if (selectedElement != null
                && elements.circuit[elements.getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements.circuit[elements.getElement(selectedElement)] = new LED(selectedElement, largeCellSize);
            selectedElement = null;
            onScreenToast("LED Created");

        }
    }

    //This method changes the label of a switch from 0 to 1 or from 1 to 0
    private void toggle() {
        Log.d("Debugging", "1/0");
        if (selectedElement != null) {
            if (elements.circuit[elements.getElement(selectedElement)] instanceof SWITCH) {
                ((SWITCH) elements.circuit[elements.getElement(selectedElement)]).toggle();
                selectedElement = null;
                onScreenToast("Switch Toggled");

            }
        }
    }

    //this method toggles our intro state
    private void intro() {
        introducing = !introducing;
    }

    //This method toggles our save state.
    private void save(){
        saving = !saving;
        ((Save) menu[10]).toggle();
    }


    // This method is called by the A, B, and C buttons.
    //Based on the saving boolean value, it will save or load a state from
    //The savedSchematics array.
    private void saveOrLoad(int input){
        input -= 11;
        if(saving){
            saveSchematic(input);
            save();
            onScreenToast("Layout Saved");
        }
        else{
            loadSchematic(input);
            selectedElement = null;
            selectedNode = null;
            onScreenToast("Layout Loaded");
        }

    }


    private void saveSchematic(int input){
            savedSchematics[input]=elements.copy();
        Log.d("Debugging", "Saving Diagram");
    }

    private void loadSchematic(int input){
        if(savedSchematics[input]!=null) {
            elements = savedSchematics[input];
        }
        else{
            elements.circuit = new CircuitElement[numberOfCircuitElements];
        }
        Log.d("Debugging", "Loading Diagram");

    }

    //------------------------------------------------------------------------------------------
    //These are methods called by the touch processor

    //This method selects an element based on the users touch.
    void elementSelect(Point touchPoint){
        int elementIndex = elements.getElement(touchPoint);
        if(elementIndex!=-1) {
            selectedElement = new Point(touchPoint.x, touchPoint.y);
            Log.d("Debugging", "Element Selected at:" +
                    touchPoint.x + ", " +touchPoint.y);
        }
        selectedButton = null;
    }

    //This method handles moving an element and deselecting a button.
    //Only called when the grid is touched by the user.
    void gridSelect(Point touchPoint){
        if(selectedElement!=null && selectedButton==null) {
            elements.move(touchPoint, selectedElement);
            selectedElement = null;
        }else{
            Log.d("Debugging", "No action taken.");
        }
        selectedButton = null;
    }

    //This method associates one element with another for wiring
    //It also stores a value(nodeNumber) to tell which input node has been selected
    void wireTwoElements(Point touchPoint, Point nodeTouch){
        elements.wireTwoElements(touchPoint, nodeTouch, selectedElement);
        selectedNode = null;
        selectedElement = null;
        selectedButton = null;
    }

    //-------------------------------------------------------------------------------------------

    //Methods for UNDO and REDO

    private void undo() {
        //The redo stack is topped off with the top element of the
        pushToRedo();

        //Our elements are replaced by the top of the undo Stack
        if(!undoStack.isEmpty())
            elements = undoStack.pop();

    }

    private void redo() {
        //The undo stack is topped off with the our current elements
        pushToUndo();

        //Our elements are replaced by the top of the redo Stack
        if(!redoStack.isEmpty())
            elements = redoStack.pop();
    }

    private void pushToRedo(){
        Schematic temp;
        temp = elements.copy();
        redoStack.push(temp);
    }
    private void pushToUndo(){
        Schematic temp;
        temp = elements.copy();
        undoStack.push(temp);
    }

}