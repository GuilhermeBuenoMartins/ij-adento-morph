package net.imagej;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import net.imagej.array.Array;
import net.imagej.config.Config;
import net.imagej.nn.Model;

/**
 * Hello world!
 *
 */
public class App {

    private static Array readInput(String fileName) {
        JsonObject jsonObject = null;
        try (FileReader reader = new FileReader(fileName)) {
            jsonObject = new Gson().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            e.getStackTrace();
        }
        Double[][][][] jsonKernel = new Gson().fromJson(jsonObject.get("input"), Double[][][][].class);
        int[] shape = new int[] { jsonKernel.length, jsonKernel[0].length, jsonKernel[0][0].length, jsonKernel[0][0][0].length };
        Double[] doubles = new Double[] {};
        for (int i = 0; i < shape[0]; i++) {
            for (int j = 0; j < shape[1]; j++) {
                for (int k = 0; k < shape[2]; k++) {
                    for (int l = 0; l < shape[3]; l++) {
                        doubles = ArrayUtils.add(doubles, jsonKernel[i][j][k][l]);
                    }
                }
            }
        }
        return Array.create(shape, doubles);
    }

    public static void main(String[] args) {
        System.out.println("Initiating application...");
        Model model = Config.getModel();
        System.out.println("Model loaded!");
    }

}
