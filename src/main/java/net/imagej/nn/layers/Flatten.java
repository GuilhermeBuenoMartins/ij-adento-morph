package net.imagej.nn.layers;

import java.util.Arrays;

import net.imagej.array.Array;
import net.imagej.nn.Layer;

public class Flatten implements Layer {

    @Override
    public void loadWeights(Array[] arrays) {
        // Implementation unnecessary.
    }

    @Override
    public Array execute(Array input) {
        int[] inputShape = input.getShape();
        int numberOfFeature = Arrays.stream(inputShape, 1, inputShape.length).reduce(1, (r, i) -> r * i);
        return input.reshape(new int[] {inputShape[0], numberOfFeature});
    }
    
    
}
