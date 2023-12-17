package net.imagej.nn;

import net.imagej.array.Array;

public class Model {

    Layer[] layers;

    public Model(Layer... layers) {
        this.layers = layers;
    }

    public final void loadWeights(int layerId, Array[] arrays) {
        layers[layerId].loadWeights(arrays);
    }

    public final Array execute(Array input) {
        for (int i = 0; i < layers.length; i++) {
            input = layers[i].execute(input);
        }
        return input;
    }
}
