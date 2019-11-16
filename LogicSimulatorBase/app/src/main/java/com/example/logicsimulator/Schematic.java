dpackage com.example.logicsimulator;

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
}
