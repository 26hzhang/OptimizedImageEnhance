package com.isaac.examples;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import com.isaac.utils.Filters;
import com.isaac.utils.ImShow;

public class GuidedFilterSmoothingExample {
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {
		String imgPath = "src/main/resources/dcp_images/smoothing/cat.bmp";
		Mat image = Imgcodecs.imread(imgPath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		new ImShow("image").showImage(image);
		image.convertTo(image, CvType.CV_32F);
		Mat guide = image.clone();
		int r = 4; // try r=2, 4, or 8
		double eps = 0.16; // try eps=0.01, 0.04, 0.16
		Mat q = Filters.GuidedImageFilter(image, guide, r, eps);
		q.convertTo(q, CvType.CV_8UC1);
		new ImShow("q").showImage(q);;
	}

}
