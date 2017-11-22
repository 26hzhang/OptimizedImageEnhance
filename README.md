# Fast Optimized Image/Video Enhancement Methods
It is a set of image/video enhancement methods, implemented by Java, to tackle several common tasks, such as dehazing, denoising, underwater [backscatter](https://en.wikipedia.org/wiki/Backscatter) removal, low illuminance enhancement, featuring, smoothing and etc.

**NOTE that** this repository is the integration of several repositories of Image/Video processing on [my GitHub](https://github.com/IsaacChanghau), and those standalone repositories will be **deprecated** later.
- RemoveBackScatter -- **Deleted**, its zip file is available here: [[link]](/pre_standalones/RemoveBackScatter-master.zip).
- OptimizedContrastEnhance -- **Deleted**, its zip file is available here: [[link]](/pre_standalones/OptimizedContrastEnhance-master.zip).
- [ImageEnhanceViaFusion](https://github.com/IsaacChanghau/ImageEnhanceViaFusion) -- will be deprecated, its zip file is available here: [[link]](/pre_standalones/ImageEnhanceViaFusion-master.zip)
- HazeRemovalByDarkChannelPrior -- **Deleted**, its zip file is available here: [[link]](/pre_standalones/HazeRemovalByDarkChannelPrior-master.zip)
- ALTMRetinex -- **Deleted**, its zip file is available here: [[link]](/pre_standalones/ALTMRetinex-master.zip)
- [Image-Enhance-via-Retinex-and-DT-CWT](https://github.com/IsaacChanghau/Image-Enhance-via-Retinex-and-DT-CWT) -- will be deprecated, its zip file is available here: [[link]](/pre_standalones/Image-Enhance-via-Retinex-and-DT-CWT-master.zip)

## Description
This Java project contains five different models for image/video enhancement methods as we as the MATLAB codes (in [`matlab`](/matlab/) directory) for each of them.
* [ALTMRetinex](/src/main/java/com/isaac/models/ALTMRetinex.java) is inspired by [Adaptive Local Tone Mapping Based on Retinex for HDR Image](http://koasas.kaist.ac.kr/bitstream/10203/172985/1/73275.pdf), which is published by Ahn, Hyunchan. **Details and Results are shown here**: [[link]](/matlab/ALTMRetinex/README.md).
* [DarkChannelPriorDehaze](/src/main/java/com/isaac/models/DarkChannelPriorDehaze.java) is the algorithm proposed in [Single Image Haze Removal Using Dark Channel Prior](http://kaiminghe.com/publications/pami10dehaze.pdf), published by [Kaiming He](http://kaiminghe.com/). **Details and Results are shown here**: [[link]](/matlab/DarkChannelPriorDehaze/README.md).
* [FusionEnhance](/src/main/java/com/isaac/models/FusionEnhance.java) is implemented according to the method described in [Enhancing Underwater Images and Videos by Fusion](http://perso.telecom-paristech.fr/~Gousseau/ProjAnim/2015/ImageSousMarine.pdf), published by Cosmin Ancuti. **Details and Results are shown here**: [[link]](/matlab/FusionEnhance/README.md).
* [OptimizedContrastEnhance](/src/main/java/com/isaac/models/OptimizedContrastEnhance.java) is implemented according to the method described in [Optimized Contrast Enhancement for Real-time Image and Video Dehazing](http://www.sciencedirect.com/science/article/pii/S1047320313000242), published by Jin-Hwan Kim. **Details and Results are shown here**: [[link]](/matlab/OptimizedContrastEnhance/README.md).
* [RemoveBackScatter](/src/main/java/com/isaac/models/RemoveBackScatter.java), Removing Backscatter to Enhance the Visibility of Underwater Object, is a fast and effective backscatter removal and enhancement method to enhance the underwater image/video as well as light hazel images. **Details and Results are shown here**: [[link]](/matlab/RemoveBackScatter/README.md).

Despite the above methods implemented in both Java and MATLAB, below contains several image enhancement and underwater restoration algorithms implemented in MATLAB (*They will be implemented in Java in the future*).
* [AutomaticRedChannelRestoration](/matlab/AutomaticRedChannelRestoration.zip) is implemented on the basis of [Automatic Red-Channel Underwater Image Restoration](http://www.sciencedirect.com/science/article/pii/S1047320314001874).
* [RetinexBasedRestoration](/matlab/RetinexBasedRestoration.zip) is the method proposed in [A Retinex-based Enhancing Approach for Single Underwater Image](http://smartdsp.xmu.edu.cn/memberpdf/fuxueyang/1.pdf).
* [AutomaticRecoveryAtmosphericLight](/matlab/AutomaticRecoveryAtmosphericLight.zip) is the method proposed in [Automatic Recovery of the Atmospheric Light in Hazy Images](http://www.cs.huji.ac.il/~werman/Papers/iiii2013.pdf).
* [DehazingCorrectionAndDecomposition](/matlab/DehazingCorrectionAndDecomposition.zip) is implemented on the basis of [Single Image Dehazing with White Balance Correction and Image Decomposition](http://ieeexplore.ieee.org/abstract/document/6411690/?reload=true).
* [EnhanceViaRetinexDTCWT](/matlab/EnhanceViaRetinexDTCWT/), Image Enhancement Based on Retinex and Dual-Tree Complex Wavelet Transform, which is a efficient and effective method for enhancement of low-illuminance image/video. **Details and Results are shown here**: [[link]](/matlab/EnhanceViaRetinexDTCWT/README.md).

## Requirements
* [Java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
* [OpenCV](http://opencv.org/). To make the OpenCV can work with JAVA IDE, like IntelliJ or Eclipse, you may need to follow the guide of [[Install OpenCV for Java]](https://isaacchanghau.github.io/2017/04/10/Installing-OpenCV-for-Java/) to setup OpenCV.
* [ImShow-Java-OpenCV](https://github.com/master-atul/ImShow-Java-OpenCV), a plain image display codes implemented by OpenCV Java version (This codes is already included in the project).

## Results
### Adaptive Local Tone Mapping Based on Retinex for HDR Image
<img src="/matlab/ALTMRetinex/images/original-horses.png" width = "425" height = "320" align=center />  <img src="/matlab/ALTMRetinex/images/altm-horses.png" width = "425" height = "320" align=center />

### Image Enhancement Based on Retinex and Dual-Tree Complex Wavelet Transform
<img src="/matlab/EnhanceViaRetinexDTCWT/images/whitehouse.png" width = "425" height = "320" align = center />  <img src="/matlab/EnhanceViaRetinexDTCWT/images/result.png" width = "425" height = "320" align = center />

### Single Image Haze Removal Using Dark Channel Prior
<img src="/matlab/DarkChannelPriorDehaze/images/train.png" width = "425" height = "320" align=center />  <img src="/matlab/DarkChannelPriorDehaze/images/trainDehaze.png" width = "425" height = "320" align=center />

### Optimized Contrast Enhancement for Real-time Image and Video Dehazing
<img src="/matlab/OptimizedContrastEnhance/images/org-ny.png" width = "425" height = "320" align=center />  <img src="/matlab/OptimizedContrastEnhance/images/dehaze-ny.png" width = "425" height = "320" align=center />

### Enhancing Underwater Images and Videos by Fusion
<img src="/matlab/FusionEnhance/images/org-3.png" width = "425" height = "350" align=center />  <img src="/matlab/FusionEnhance/images/enh-3.png" width = "425" height = "350" align=center />

### Removing Backscatter to Enhance the Visibility of Underwater Object
<img src="/matlab/RemoveBackScatter/images/Fusion_Output.png" width = "850" height = "300" align=center />
