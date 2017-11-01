package com.isaac.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import com.isaac.utils.Filters;
import com.isaac.utils.ImShow;

public class GuidedFilterEnhanceExample {
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {
		String imgPath = "src/main/resources/dcp_images/enhancement/tulips.bmp";
		Mat image = Imgcodecs.imread(imgPath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		new ImShow("image").showImage(image);
		image.convertTo(image, CvType.CV_32F);
		List<Mat> img = new ArrayList<>();
		Core.split(image, img);
		int r = 16;
		double eps = 0.01;
		
		Mat q_r = Filters.GuidedImageFilter(img.get(0), img.get(0), r, eps);
		Mat q_g = Filters.GuidedImageFilter(img.get(1), img.get(1), r, eps);
		Mat q_b = Filters.GuidedImageFilter(img.get(2), img.get(2), r, eps);
		
		Mat q = new Mat();
		Core.merge(new ArrayList<>(Arrays.asList(q_r, q_g, q_b)), q);
		q.convertTo(q, CvType.CV_8UC1);
		new ImShow("q").showImage(q);
	}

}
