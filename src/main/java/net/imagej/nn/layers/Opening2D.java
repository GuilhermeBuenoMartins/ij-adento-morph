package net.imagej.nn.layers;

import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;

import net.imagej.array.Array;
import net.imagej.nn.Layer;
import net.imagej.nn.enums.Activation;
import net.imagej.nn.enums.Padding;

public class Opening2D implements Layer {

    private Array kernel;

    private Activation activation = Activation.NONE;

    private Padding padding = Padding.SAME;

    public Opening2D(Array kernel, Activation activation, Padding padding) {
        this.kernel = kernel;
        this.activation = activation;
        this.padding = padding;
    }

    public Opening2D(Array kernel, Activation activation) {
        this.kernel = kernel;
        this.activation = activation;
    }

    public Opening2D(Array kernel) {
        this.kernel = kernel;
    }

    @Override
    public void loadWeights(Array[] arrays) {
        kernel = arrays[0];
    }

    @Override
    public Array execute(Array input) {
        int[] kernelShape = kernel.getShape();
        int[] inputShape = input.getShape();
        int[] inputFrom = Arrays.stream(inputShape).map(i -> 0).toArray();
        int[] inputTo = Arrays.copyOf(inputShape, inputShape.length);
        int[] kernelFrom = Arrays.stream(kernelShape).map(i -> 0).toArray();
        int[] kernelTo = Arrays.copyOf(kernelShape, kernelShape.length);
        Array[] arrays = new Array[] {};
        for (int i = 0; i < kernelShape[kernelShape.length - 1]; i++) {
            kernelFrom[kernelFrom.length - 1] = i;
            kernelTo[kernelTo.length - 1] = i + 1;
            for (int j = 0; j < inputShape[inputShape.length - 1]; j++) {
                kernelFrom[kernelFrom.length - 2] = j;
                kernelTo[kernelTo.length - 2] = j + 1;
                inputFrom[inputFrom.length - 1] = j;
                inputTo[inputTo.length - 1] = j + 1;
                Array kernelSlice = kernel.slice(kernelFrom, kernelTo);
                Array erosion = new Erosion2D(kernelSlice, Activation.NONE, padding).execute(input.slice(inputFrom, inputTo));
                Array dilation = new Dilation2D(kernelSlice, Activation.NONE, padding).execute(erosion);
                arrays = ArrayUtils.add(arrays, dilation);
            }
        }
        Array output = arrays[0];
        for (int i = 1; i < arrays.length; i++) {
            output = output.concat(arrays[i], 1);
        }
        return output;
    }

}
