package com.isaac.examples;

import com.isaac.models.RemoveBackScatter;
import com.isaac.utils.ImShow;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class RemoveBackScatterExample {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final int blkSize = 10 * 10;
    private static final int patchSize = 8;
    private static final double lambda = 10;
    private static final double gamma = 1.7;
    private static final int r = 10;
    private static final double eps = 1e-6;
    private static final int level = 5;

    public static void main (String[] args) {
        String imgPath = "src/main/resources/underwater_images/underwater_scene.jpg";
        Mat image = Imgcodecs.imread(imgPath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        new ImShow("original").showImage(image); // show image
        Mat fusion = RemoveBackScatter.enhance(image, blkSize, patchSize, lambda, gamma, r, eps, level);
        fusion.convertTo(fusion, CvType.CV_8UC1);
        new ImShow("fusion").showImage(fusion); // show fusion result
    }
}
