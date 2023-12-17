package net.imagej.nn.layers;

import net.imagej.array.Array;
import net.imagej.array.Operation;
import net.imagej.nn.Function;
import net.imagej.nn.Layer;
import net.imagej.nn.enums.Activation;

public class Dense implements Layer {

    private Array weights;

    private Array bias;

    private Activation activation = Activation.NONE;

    public Dense(Array weights, Array bias, Activation activation) {
        this.weights = weights;
        this.bias = bias;
        this.activation = activation;
    }

    public Dense(Array weights, Array bias) {
        this.weights = weights;
        this.bias = bias;
    }

    private Array activate(Array input) {
        switch (activation) {
            case RELU: return Function.relu(input);
            case SIGMOID: return Function.sigmoid(input);
            case TANH: return Function.tanh(input);
            default: return input;
        }
    }

    @Override
    public void loadWeights(Array[] arrays) {
        weights = arrays[0];
        bias = arrays[1];
    }

    @Override
    public Array execute(Array input) {
        input = Operation.dot(input, weights);
        for (int i = 0; i < input.getLength(); i++) {
            input.set(Operation.plus((Array) input.get(i), bias), i);
        }
        return activate(input);
    }
}
