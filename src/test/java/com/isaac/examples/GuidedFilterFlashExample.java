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

public class GuidedFilterFlashExample {
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {
		String imgPath = "src/main/resources/dcp_images/flash/cave-flash.bmp";
		String guidedImgPath = "src/main/resources/dcp_images/flash/cave-noflash.bmp";
		Mat image = Imgcodecs.imread(imgPath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		new ImShow("image").showImage(image);
		image.convertTo(image, CvType.CV_32F);
		Mat guide = Imgcodecs.imread(guidedImgPath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		guide.convertTo(guide, CvType.CV_32F);
		List<Mat> img = new ArrayList<>();
		List<Mat> gid = new ArrayList<>();
		Core.split(image, img);
		Core.split(guide, gid);
		
		int r = 8;
		double eps = 0.02 * 0.02;
		Mat q_r = Filters.GuidedImageFilter(img.get(0), gid.get(0), r, eps);
		Mat q_g = Filters.GuidedImageFilter(img.get(1), gid.get(1), r, eps);
		Mat q_b = Filters.GuidedImageFilter(img.get(2), gid.get(2), r, eps);
		Mat q = new Mat();
		Core.merge(new ArrayList<>(Arrays.asList(q_r, q_g, q_b)), q);
		q.convertTo(q, CvType.CV_8UC1);
		new ImShow("q").showImage(q);
	}

}
