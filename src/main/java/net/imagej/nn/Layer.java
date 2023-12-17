package net.imagej.nn;

import net.imagej.array.Array;

public interface Layer {
    
    void loadWeights(Array[] arrays);

    Array execute(Array input);
    
}
