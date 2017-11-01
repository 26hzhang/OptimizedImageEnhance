## Adaptive Local Tone Mapping Based on Retinex for HDR Image
This project is the Java implementation of the algorithm proposed. It is a new tone mapping technique for high dynamic range images based on the retinex theory. This algorithm consists of two steps, global adaptation and local adaptation of the human visual system. In the local adaptation process, the Gaussian filter of the retinex algorithms is substituted with a guided filter to reduce halo artifacts. To guarantee good rendition and dynamic range compression, a contrast enhancement factor based on the luminance values of the scene is used. In addition, an adaptive nonlinearity offset is introduced to deal with the strength of the logarithm function’s nonlinearity.

To make this algorithm performs better, some modifications are made based on the original methods. For the details of the algorithm propsed by the author, you'd better read the paper. Here only show some of the modifications.

**Maximum Filter**

In order to make the algorithm to deal with the low illumination image well, I apply a Maximum filter before the Gaussian blur.

**Contrast Enhancement Factor & Adaptive nonlinearly offset**

The contrast enhancement factor is given by:
<p align="center">
  <img src="images/original-alpha.png" width = "150" height = "60"/>
</p>

where η denotes the contrast control parameter, and <img src="images/Lgmax.png" width = "40" height = "20" align=center /> is the maximum luminance value of the global adaptation output. The other, the adaptive nonlinearity offset which varies in accordance with the scene contents can be written as:
<p align="center">
<img src="images/original-beta.png" width = "80" height = "35" align=center />
</p>

where λ is the nonlinearity control parameter, and <img src="images/Lg_.png" width = "25" height = "25" align=center /> is the log-average luminance of the global adaptation output.
To make the result better, these nonlinear formula are modified.
<p align="center">
<img src="images/modified-alpha.png" width = "320" height = "90" align=center />
<b>, and then, </b>
<img src="images/modified-alpha2.png" width = "320" height = "50" align= center />
</p>
where η denotes the contrast control parameter, a controls the increasing rate of global contrast, <img src="images/WRgmax.png" width = "45" height = "20" align=center /> is the maximum luminance value of the global adaptation output, b is the shrinkage parameters to control the shape of Sigmoid function, <img src="images/alpha_max.png" width = "30" height = "15" align=center /> is the maximum value of contrast enhancement factor.

**Results and Statistics**

<img src="images/original-iris.png" width = "425" height = "320" align=center />  <img src="images/altm-iris.png" width = "425" height = "320" align=center />

<img src="images/original-whitehouse.png" width = "425" height = "320" align=center />  <img src="images/altm-whitehouse.png" width = "425" height = "320" align=center />

<img src="images/original-horses.png" width = "425" height = "320" align=center />  <img src="images/altm-horses.png" width = "425" height = "320" align=center />

<img src="images/original-liahthouse.png" width = "425" height = "320" align=center />  <img src="images/altm-liahthouse.png" width = "425" height = "320" align=center />

<img src="images/original-water.png" width = "425" height = "320" align=center />  <img src="images/altm-water.png" width = "425" height = "320" align=center />
