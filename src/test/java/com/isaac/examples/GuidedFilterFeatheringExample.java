package com.isaac.examples;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import com.isaac.utils.Filters;
import com.isaac.utils.ImShow;

public class GuidedFilterFeatheringExample {
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {
		String imgPath = "src/main/resources/dcp_images/feathering/toy.bmp";
		String guidedImgPath = "src/main/resources/dcp_images/feathering/toy-mask.bmp";
		Mat image = Imgcodecs.imread(imgPath, Imgcodecs.CV_LOAD_IMAGE_COLOR); // load image
		new ImShow("image").showImage(image);
		image.convertTo(image, CvType.CV_32F);
		Mat guide = Imgcodecs.imread(guidedImgPath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		guide.convertTo(guide, CvType.CV_32F);
		int r = 60;
		double eps = 0.000001;
		Mat q = Filters.GuidedImageFilter_Color(image, guide, r, eps, 1, -1);
		q.convertTo(q, CvType.CV_8UC1);
		new ImShow("q").showImage(q);
	}

}
