package com.example.logicsimulator;

import androidx.annotation.NonNull;

//Working on a class to replace the elements array.

public class Schematic {
    int numberOfCircuitElements;
    public CircuitElement[] circuit = new CircuitElement[numberOfCircuitElements];

    public Schematic(int numberOfCircuitElements){
        this.numberOfCircuitElements = numberOfCircuitElements;
    }

    @NonNull
    @Override
    public String toString() {
        String string ="";
        for(int i = 0; i < circuit.length; i++){
            string+=circuit[i].label;
            string+="\n";
        }
        return string;
    }
}
