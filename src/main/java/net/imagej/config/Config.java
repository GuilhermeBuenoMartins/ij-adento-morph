package net.imagej.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import net.imagej.nn.enums.Activation;
import net.imagej.array.Array;
import net.imagej.nn.Model;
import net.imagej.nn.layers.Dense;
import net.imagej.nn.layers.Flatten;
import net.imagej.nn.layers.Opening2D;
import net.imagej.nn.layers.TopHatOpening2D;

public class Config {

    private static final String SEPARATOR = ",";

    private static final String SETTINGS_PROPERTIES = "src/main/resources/settings.properties";

    private static Properties properties = null;

    private static final Properties getProperties() {
        if (properties != null) {
            return properties;
        }
        try {
            properties = new Properties();
            properties.load(new FileInputStream(SETTINGS_PROPERTIES));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return properties;
    }

    private static final Model createDefaultModel() {
        return new Model(
                new TopHatOpening2D(null),
                new Opening2D(null),
                new Flatten(),
                new Dense(null, null, Activation.TANH),
                new Dense(null, null, Activation.TANH),
                new Dense(null, null, Activation.SIGMOID));
    }

    private static final int[] getShape(int layerId, String attributeName) {
        String propertyName = String.format("layers.layer_%d.%s.shape", layerId, attributeName);
        String propertyValue = getProperties().getProperty(propertyName);
        return propertyValue != null
                ? Arrays.stream(propertyValue.split(SEPARATOR)).mapToInt(p -> Integer.parseInt(p)).toArray()
                : null;
    }

    private static final Array readH5(int layerId, String attributeName, int[] shape) {
        if (shape != null) {
            String fileName = String.format("%s/layer_%d.h5", getProperties().getProperty("layers.directory"), layerId);
            IHDF5SimpleReader simpleReader = HDF5Factory.open(fileName);
            return Array.create(shape,
                    Arrays.stream(simpleReader.readDoubleArray(attributeName)).boxed().toArray(Double[]::new));
        }
        return null;
    }

    public static final Model getModel() {
        Model model = createDefaultModel();
        int layersLength = Integer.parseInt(getProperties().getProperty("layers.length"));
        String[] attributeNames = new String[] { "weights", "bias" };
        for (int layerId = 0; layerId < layersLength; layerId++) {
            Array[] arrays = new Array[] {};
            for (int i = 0; i < attributeNames.length; i++) {
                int[] shape = getShape(layerId, attributeNames[i]);
                if (shape != null) {
                    arrays = ArrayUtils.add(arrays, readH5(layerId, attributeNames[i], shape));
                }
            }
            model.loadWeights(layerId, arrays);
        }
        return model;
    }

}
