package net.imagej.nn.layers;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import net.imagej.array.Array;
import net.imagej.array.Operation;
import net.imagej.nn.Layer;
import net.imagej.nn.enums.Activation;
import net.imagej.nn.enums.Padding;

public class TopHatOpening2D implements Layer {

    private Array kernel;

    private Activation activation = Activation.NONE;

    private Padding padding = Padding.SAME;

    public TopHatOpening2D(Array kernel) {
        this.kernel = kernel;
    }

    @Override
    public void loadWeights(Array[] arrays) {
        kernel = arrays[0];
    }

    @Override
    public Array execute(Array input) {
        int[] inputShape = input.getShape();
        int[] kernelShape = kernel.getShape();
        Array topHatInput = input.slice(IntStream.range(0, inputShape.length).map(i -> 0).toArray(), inputShape);
        int numberOfBands = inputShape[inputShape.length - 1] * kernelShape[kernelShape.length - 1];
        Array opening = new Opening2D(kernel, activation, padding).execute(input);
        int[] openingShape = opening.getShape();
        int[] fromOpening = IntStream.range(0, openingShape.length).map(i -> 0).toArray();
        int[] toOpening = Arrays.copyOf(openingShape, openingShape.length);
        Array[] arrays = new Array[]{};
        for (int i = 0; i < numberOfBands; i += inputShape[inputShape.length - 1]) {
            fromOpening[fromOpening.length - 1] =  i;
            toOpening[toOpening.length - 1] = i + inputShape[inputShape.length - 1];
            arrays =  ArrayUtils.add(arrays, Operation.minus(topHatInput, opening.slice(fromOpening, toOpening)));
        }
        Array output = arrays[0];
        for (int i = 1; i < arrays.length; i++) {
            output = output.concat(arrays[i], 1);
        }
        return output;
    }
    
}
