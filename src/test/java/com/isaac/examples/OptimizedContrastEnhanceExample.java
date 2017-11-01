package com.isaac.examples;

import com.isaac.models.OptimizedContrastEnhance;
import com.isaac.utils.ImShow;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class OptimizedContrastEnhanceExample {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final int blkSize = 100; // block size
    private static final int patchSize = 4; // patch size
    private static final double lambda = 5.0; // control the relative importance of contrast loss and information loss
    private static final double eps = 1e-8;
    private static final int krnlSize = 10;

    public static void main (String[] args) {
        String imgPath = "src/main/resources/haze_images/canon_2.jpg";
        Mat image = Imgcodecs.imread(imgPath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        new ImShow("org-image").showImage(image);
        Mat result = OptimizedContrastEnhance.enhance(image, blkSize, patchSize, lambda, eps, krnlSize);
        result.convertTo(result, CvType.CV_8UC1);
        new ImShow("dehaze-image").showImage(result);
    }
}
