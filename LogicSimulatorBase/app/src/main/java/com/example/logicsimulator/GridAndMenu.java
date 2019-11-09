package com.example.logicsimulator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


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

    private boolean playing, saving = false;
    private Context context;
    private Canvas myCanvas;
    private Paint paint = new Paint();
    private final int numberOfCircuitElements = 10;
    private final int numberOfButtons = 14;
    private final int numberOfHorizontalCells = 30;
    private final int numberOfVerticalCells = 15;
    private int numberOfActiveElements = 0;
    private int numberOfSavableSchematic = 3;

    private CircuitElement[][] savedSchematics = new CircuitElement[numberOfSavableSchematic][];

    private Button[] menu = {new PLAY(0), new ADD(1), new SUB(2), new WIRE(3)
            , new AND(4), new OR(5), new NOT(6), new SWITCHBUTTON(7)
            , new LEDBUTTON(8), new TOGGLE(9), new Save(10), new A(11), new B(12), new C(13)};

    private Node[][] cells =
            new Node[numberOfHorizontalCells][numberOfVerticalCells];

    private CircuitElement[] elements =
            new CircuitElement[numberOfCircuitElements];

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
            for (CircuitElement element : elements)
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
        Toast toast= Toast.makeText(context,
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
        if(elements!= null) {
            for (int i = 0; i < numberOfCircuitElements; i++) {
                if (elements[i] != null) {
                    elements[i].idle(myCanvas, i);
                }
            }
        }
    }

    //Prints the wire connections between circuit elements.
    private void printWires() {
        //For all of the possible element
        if (elements != null) {
            for (CircuitElement element : elements) {
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
            elements[getElement(selectedElement)].select(myCanvas);
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
                onScreenToast("Circuit is Running");
                break;
            //-------------------------------------------------------------------------------
            case 1: //ADD BUTTON
                if(!playing){
                    add();
                    onScreenToast("Element Added");
                }
                break;
            //---------------------------------------------------------------------------------
            case 2: //SUB BUTTON
                if(!playing)
                sub();
                break;
            //-----------------------------------------------------------------------------
            case 3: //Wire BUTTON
                if(!playing) {
                    wire();
                    //Displays prompt with certain condition
                    if(numberOfActiveElements >= 2)
                        onScreenToast("Choose an Element to Wire To");
                }
                break;
            //------------------------------------------------------------------------
            case 4: //AND BUTTON
                and();
                break;
            //------------------------------------------------------------------------
            case 5://OR BUTTON
                or();
                break;
            //----------------------------------------------------------------------
            case 6: //NOT BUTTON
                not();
                break;
            //----------------------------------------------------------------------
            case 7: //SWITCH BUTTON
                inputSwitch();
                break;
            //-----------------------------------------------------------------
            case 8: //LED BUTTON
                led();
                break;
            //--------------------------------------------------------------------
            case 9: // 1/0 BUTTON
                toggle();
                break;
            //-----------------------------------------------------------------
            case 10: // Save Button
                if (!playing) {
                    save();
                    onScreenToast("Choose A, B, C to Save Current Layout");
                }
                break;
            //-----------------------------------------------------------------
            case 11: // A Button
                if(!playing) {
                    saveOrLoad(0);
                    onScreenToast("Layout saved to A");
                }
                break;
            //-----------------------------------------------------------------
            case 12: //B Button
                if(!playing) {
                    saveOrLoad(1);
                    onScreenToast("Layout saved to B");
                }
                break;
            //-----------------------------------------------------------------
            case 13: // C Button
                if(!playing) {
                    saveOrLoad(2);
                    onScreenToast("Layout saved to C");
                }
                break;
        }
    }

    //------------------------------------------------------------------------------------------
    //Button Function Methods:

    //This method toggles the playing boolean value;
    private  void play(){
        if(!nullConnections()) {
            Log.d("Debugging", "Now Playing");
            playing = !playing;
            ((PLAY) menu[0]).toggle();
        }
    }

    //This method adds an element to the elements Array
    private void add() {
        Point location = new Point(0,0);
        if((getElement(location)==-1)
                &&numberOfActiveElements<numberOfCircuitElements) {
            for (int i = 0; i < numberOfCircuitElements; i++) {
                if (elements[i] == null) {
                    elements[i] = new CircuitElement(location, largeCellSize);
                    break;
                }
            }
            numberOfActiveElements++;
            Log.d("Debugging", "Current Elements:" + numberOfActiveElements);
        }else
            Log.d("Debugging","No Element Added, Space occupied OR Too many element");
    }

    //This method removes an element from the elements Array and removes wire connections
    private void sub() {
        if (selectedElement != null) {
            Log.d("Debugging", "Element Subtracted");
            removeConnections();
            int index = getElement(selectedElement);
            elements[index] = null;
            selectedElement = null;
            numberOfActiveElements--;
            onScreenToast("Element Subtracted");
        } else
            Log.d("Debugging", "No Element Subtracted");
    }

    //This method removes an elements wire connections
    private void removeConnections(){
        for(CircuitElement element : elements){
            if (element != null) {
                if (element.a != null) {
                    if (element.a.checkPosition(selectedElement)) {
                        element.a = null;
                    }
                    if (element instanceof TwoInOneOut) {
                        if (((TwoInOneOut) element).b != null) {
                            if (((TwoInOneOut) element).b.checkPosition(selectedElement)) {
                                ((TwoInOneOut) element).b = null;
                            }
                        }
                    }
                }
            }
        }
    }

    //This method selects the output node of the selected element.
    //Note: Now that a node has been selected, out state has changed.
    // the next element that is selected will be wired.
    private void wire()  {
        if (selectedElement != null && elements[getElement(selectedElement)].outputNode!=null) {
            selectedNode = elements[getElement(selectedElement)].outputNode.position;
            Log.d("Debugging", "Output Node selected at:" + selectedNode);
        }


    }

    //The following methods (from and to led) changes an unclassified circuit element into
    //each methods respective circuit elements
    private void and() {
        if (selectedElement != null
                && elements[getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements[getElement(selectedElement)] = new ANDGATE(selectedElement, context, largeCellSize);
            selectedElement = null;
            onScreenToast("And Gate created");

        }
    }

    private void or() {
        if (selectedElement != null
                && elements[getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements[getElement(selectedElement)] = new ORGATE(selectedElement, context, largeCellSize);
            selectedElement = null;
            onScreenToast("Or Gate Created");

        }
    }

    private void not(){
        if(selectedElement!=null
                && elements[getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements[getElement(selectedElement)] = new NOTGATE(selectedElement, context, largeCellSize);
            selectedElement = null;
            onScreenToast("Not Gate Created");

        }
    }

    private void inputSwitch(){
        if (selectedElement != null
                && elements[getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements[getElement(selectedElement)] = new SWITCH(selectedElement, largeCellSize);
            selectedElement = null;
            onScreenToast("Switch Created");

        }
    }

    private void led() {
        Log.d("Debugging", "LED");
        if (selectedElement != null
                && elements[getElement(selectedElement)].getClass()== new CircuitElement().getClass()) {
            elements[getElement(selectedElement)] = new LED(selectedElement, largeCellSize);
            selectedElement = null;
            onScreenToast("LED Created");

        }
    }

    //This method changes the label of a switch from 0 to 1 or from 1 to 0
    private void toggle() {
        Log.d("Debugging", "1/0");
        if (selectedElement != null) {
            if (elements[getElement(selectedElement)] instanceof SWITCH) {
                String label = elements[getElement(selectedElement)].label;
                ((SWITCH) elements[getElement(selectedElement)]).toggle();
                selectedElement = null;
                onScreenToast("Switch Toggled");

            }
        }
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
        if(saving){
            saveSchematic(input);
            save();
        }
        else{
            loadSchematic(input);
            selectedElement = null;
            selectedNode = null;
        }

    }

    private void saveSchematic(int input){

            savedSchematics[input]=elements;
        Log.d("Debugging", "Saving Diagram");

    }

    private void loadSchematic(int input){
        if(savedSchematics[input]!=null) {
            elements = savedSchematics[input];
        }
        else{
            elements = new CircuitElement[numberOfCircuitElements];
        }
        Log.d("Debugging", "Loading Diagram");

    }
    //------------------------------------------------------------------------------------------
    //These are methods called by the touch processor

    //This method selects an element based on the users touch.
    void elementSelect(Point touchPoint){
        int elementIndex = getElement(touchPoint);
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
            move(touchPoint);
            selectedElement = null;
        }else{
            Log.d("Debugging", "No action taken.");
        }
        selectedButton = null;
    }

    //This method associates one element with another for wiring
    //It also stores a value(nodeNumber) to tell which input node has been selected
    void wireTwoElements(Point touchPoint, Point nodeTouch){
        if(selectedElement != touchPoint) {
            CircuitElement elementOutputting = elements[getElement((selectedElement))];
            CircuitElement elementGettingInput= elements[getElement(touchPoint)];
            if (elementGettingInput.inputNodes !=null) {
                int nodeNumber = getClosestNode(touchPoint, nodeTouch);
                setConnection(nodeNumber, elementGettingInput, elementOutputting);
            }
        }
        selectedNode = null;
        selectedElement = null;
        selectedButton = null;

    }

    //-------------------------------------------------------------------------------------------

    //This method returns the index of an element
    //If an element is not found, it returns -1.
    int getElement(Point input){
        if(elements!=null) {
            for (int i = 0; i < numberOfCircuitElements; i++) {
                if (elements[i] != null) {
                    if ((input.x == elements[i].position.x) && (input.y == elements[i].position.y)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private void setConnection(int nodeNumber, CircuitElement elementGettingInput, CircuitElement elementOutputting) {
        Log.d("Debugging", "Attempting to wire to Node: " + nodeNumber);

        //----------------------------------------------------------------------------
            if (nodeNumber == 0) {
                elementGettingInput.setA(elementOutputting);
            }
            else if (nodeNumber == 1) {
                ((TwoInOneOut) elementGettingInput).setB(elementOutputting);
            }
        }

    //This method returns the closest input node  to the users touch.
    private int getClosestNode(Point touchPoint, Point nodeTouch){
        //Boolean to tell if this is the first node or not.
        Boolean first = true;
        //This value will always be overwritten in the first loop.
        int newDistance = 0;
        //get the element we have touched
        CircuitElement element = elements[getElement(touchPoint)];
        int numberOfNodes = element.inputNodes.size();
            int shortest=0;
            //For every input node an element has (Should only be 2)
            for (int i = 0 ; i < numberOfNodes; i++) {
                //Create an array of distances
                int[] distances = new int[numberOfNodes];
                //Record our distance in our array
                distances[i] = getDistance(nodeTouch ,element.inputNodes.get(i).position);
                    if (first || newDistance > distances[i]) {
                        newDistance = distances[i];
                        shortest =i;
                        first = false;
                    }
            }
            return shortest;
    }

    private int getDistance(Point in, Point nodeTouch){
        int horizontalGap = nodeTouch.x -
                in.x;
        int verticalGap = nodeTouch.y -
                in.y;
        return (int) Math.sqrt(
                ((horizontalGap * horizontalGap) +
                        (verticalGap * verticalGap)));
    }

    //This method changes the position of a circuit element
    private void move(Point touchPoint){
        Log.d("Debugging", "Element Moved to:" +touchPoint.x+", "+touchPoint.y);
        elements[getElement(selectedElement)].updatePosition(touchPoint);
    }

    //This method checks that all the gates are connected before running.
    private boolean nullConnections(){
        for(CircuitElement element : elements) {
            if (element != null) {
                if (!(element instanceof SWITCH)) {
                    if (element.a == null) {
                        Log.d("Debugging", "Null Connections Found in Circuit Element connection");
                        return true;
                    }
                    if (element instanceof TwoInOneOut) {
                        TwoInOneOut check = (TwoInOneOut) element;
                        if (check.b == null) {
                            Log.d("Debugging", "Null Connections Found in Two in One Out");
                            return true;
                        }
                    }
                }
            }
        }
            Log.d("Debugging", "No Null Connections Found");
        return false;
    }
}