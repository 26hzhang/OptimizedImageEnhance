## Removing Backscatter to Enhance the Visibility of Underwater Object
[Underwater vision enhancement](https://en.wikipedia.org/wiki/Underwater_computer_vision) via backscatter removing is widely used in ocean engineering. With increasing ocean exploration, underwater image processing has drawn more and more attention due to the important roles of video and image for obtain information. However, due to the existence of dust-like particles and light attenuation, underwater images and videos always suffer from the problems of low contrast and color distortion. In this thesis, we analyze the underwater light propagation process and propose an effective method to overcome the backscatter problem.

This method is based on the underwater optical model and image fusion. It mainly contains three steps, first, we decompose input image into reflectance and illuminance components; second, we utilize color correction technology and dehazing technology to handle these two components separately; finally, in order to rebuild result well, we applied the Gaussian and Laplacian pyramids based multi-scale fusion to reconstruct the target image, while Exposedness, Saliency and Laplacian contrast maps are utilized as weights to assist the fusion task.

### Description

##### Light Attenuation and the Physical Model of Light Propagation Underwater
<img src="images/Light_Attenuation.png" width = "425" height = "350" align=center />  <img src="images/Physical_Model.png" width = "425" height = "350" align=center />

##### The Procedures of Objects Visibility Enhancement Process
<img src="images/General_Procedure.png" width = "850" height = "350" align=center />

##### Image Decomposition and Background-Light Selection
<img src="images/Image_Decomp.png" width = "425" height = "250" align=center />  <img src="images/choose_background.png" width = "425" height = "250" align=center />

##### Coarse, Refined and Enhanced Transmission Map
<img src="images/Three_Map.png" width = "850" height = "230" align=center />

##### Transmissiom Map of Three Color Channel
left to right: red channel, green channel and blue channel

<img src="images/Three_Channel_Trans.png" width = "850" height = "230" align=center />

##### Restored Results of Illuminance and Reflectance Components
left: reflectance component, right: illuminance component

<img src="images/Refined_Reflect.png" width = "425" height = "360" align=center />  <img src="images/Refined_Illumin.png" width = "425" height = "360" align=center />

##### Normalized Weight Maps of Two Components
left: reflectance component, right: illuminance component

<img src="images/Weight_Map.png" width = "850" height = "300" align=center />

##### Multi-scale Fusion Result
<img src="images/Fusion_Output.png" width = "850" height = "300" align=center />

### Some Results

**NOTE**: For more details of results and analysis, see [**analysis.pdf**](analysis.pdf).

##### Original Images
<img src="images/org.png" width = "850" height = "250" align=center />

##### Restored Results with different methods
<img src="images/diver.png" width = "850" height = "500" align=center />

**Note**: (a) ACE. (b) Histogram equalization (HE). (c) Fu et al. (d) Ancuti et al. (e) Galdran et al. (f) our method.

<img src="images/fish.png" width = "850" height = "500" align=center />

<img src="images/scene.png" width = "850" height = "500" align=center />

##### RGB Color Space Mapping Results
left: color space of diver image, right: color space of fish image

<img src="images/diver_space.png" width = "425" height = "425" align=center />     <img src="images/fish_space.png" width = "425" height = "425" align=center />
