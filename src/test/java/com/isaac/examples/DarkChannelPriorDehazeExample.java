package com.isaac.examples;

import com.isaac.models.DarkChannelPriorDehaze;
import com.isaac.utils.ImShow;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class DarkChannelPriorDehazeExample {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    // Paper url: https://www.robots.ox.ac.uk/~vgg/rg/papers/hazeremoval.pdf
    private static final double krnlRatio = 0.01; // set kernel ratio
    private static final double minAtmosLight = 240.0; // set minimum atmospheric light
    private static final double eps = 0.000001;


    public static void main (String[] args) {
        String imgPath = "src/main/resources/haze_images/train.bmp";
        Mat image = Imgcodecs.imread(imgPath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        new ImShow("Original").showImage(image);
        Mat result = DarkChannelPriorDehaze.enhance(image, krnlRatio, minAtmosLight, eps);
        new ImShow("Dehazing").showImage(result);
    }
}
