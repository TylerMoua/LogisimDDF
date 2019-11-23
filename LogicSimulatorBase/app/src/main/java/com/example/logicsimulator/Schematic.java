package com.example.logicsimulator;

import android.graphics.Point;
import android.util.Log;

import androidx.annotation.NonNull;

//Working on a class to replace the elements array. -Tyler

public class Schematic {
    int numberOfCircuitElements;
    public CircuitElement[] circuit;

    public Schematic(int numberOfCircuitElements){
        this.numberOfCircuitElements = numberOfCircuitElements;
        circuit = new CircuitElement[numberOfCircuitElements];
    }
    @NonNull
    @Override
    public String toString() {
        String string ="";
        for(int i = 0; i < circuit.length; i++){
            if(circuit[i]!= null) {
                string += circuit[i].label;
                string += ", ";
            }
        }
        return string;
    }

    //This method returns the index of an element
    //If an element is not found, it returns -1.
    int getElement(Point input){
        if(circuit!=null) {
            for (int i = 0; i < numberOfCircuitElements; i++) {
                if (circuit[i] != null) {
                    if ((input.x == circuit[i].position.x) && (input.y == circuit[i].position.y)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    void wireTwoElements(Point touchPoint, Point nodeTouch, Point selectedElement) {
        if (selectedElement != touchPoint) {
            CircuitElement elementOutputting = circuit[getElement((selectedElement))];
            CircuitElement elementGettingInput = circuit[getElement(touchPoint)];
            if (elementGettingInput.inputNodes != null) {
                int nodeNumber = getClosestNode(touchPoint, nodeTouch);
                setConnection(nodeNumber, elementGettingInput, elementOutputting);
            }
        }
    }
    void setConnection(int nodeNumber, CircuitElement elementGettingInput, CircuitElement elementOutputting) {
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
    int getClosestNode(Point touchPoint, Point nodeTouch){
        //Boolean to tell if this is the first node or not.
        Boolean first = true;
        //This value will always be overwritten in the first loop.
        int newDistance = 0;
        //get the element we have touched
        CircuitElement element = circuit[getElement(touchPoint)];
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

    int getDistance(Point in, Point nodeTouch){
        int horizontalGap = nodeTouch.x -
                in.x;
        int verticalGap = nodeTouch.y -
                in.y;
        return (int) Math.sqrt(
                ((horizontalGap * horizontalGap) +
                        (verticalGap * verticalGap)));
    }

    //This method changes the position of a circuit element
    void move(Point touchPoint, Point selectedElement){
        Log.d("Debugging", "Element Moved to:" +touchPoint.x+", "+touchPoint.y);
        circuit[getElement(selectedElement)].updatePosition(touchPoint);
    }


    boolean nullConnections() {
        for (CircuitElement element : circuit) {
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
    Schematic copy(){
        Schematic result = new Schematic(numberOfCircuitElements);
        for(int i = 0; i < numberOfCircuitElements; i++){
            result.circuit[i] = circuit[i];
        }
        return result;
    }
}
