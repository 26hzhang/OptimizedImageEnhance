package com.isaac.examples;

import com.isaac.models.ALTMRetinex;
import com.isaac.utils.ImShow;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ALTMRetinexExample {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // Local Adaptation Parameters
    private static final int r = 10;
    private static final double eps = 0.01;
    private static final double eta = 36.0;
    private static final double lambda = 10.0;
    private static final double krnlRatio = 0.01;

    public static void main (String[] args) {
        String imgPath = "src/main/resources/dark_images/liahthouse.png";
        Mat image = Imgcodecs.imread(imgPath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        new ImShow("Original").showImage(image);
        Mat result = ALTMRetinex.enhance(image, r, eps, eta, lambda, krnlRatio);
        new ImShow("result").showImage(result);
    }
}
