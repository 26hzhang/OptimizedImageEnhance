## Single Image Haze Removal Using Dark Channel Prior
The dark channel prior dehazing is a simple but effective method to remove haze from a single input image. The dark channel prior is a kind of statistics of outdoor haze-free images. It is based on a key observation—most local patches in outdoor haze-free images contain some pixels whose intensity is very low in at least one color channel. Using this prior with the haze imaging model, it is able to directly estimate the thickness of the haze and recover a high-quality haze-free image.

### Results
##### Enhancement Example
<img src="images/tulips.png" width = "425" height = "400" align=center />  <img src="images/tulipsResult.png" width = "425" height = "400" align=center />

##### Feathering Example
<img src="images/toy.bmp" width = "283" height = "320" align=center />  <img src="images/toy-mask.bmp" width = "283" height = "320" align=center />  <img src="images/toyResult.png" width = "283" height = "320" align=center />

##### Flash Example
<img src="images/cave-flash.bmp" width = "283" height = "360" align=center />  <img src="images/cave-noflash.bmp" width = "283" height = "360" align=center />  <img src="images/flashResult.png" width = "283" height = "360" align=center />

##### Haze Removal Results
<img src="images/gugong.png" width = "425" height = "600" align=center />  <img src="images/gugongDehaze.png" width = "425" height = "600" align=center />

<img src="images/ny.png" width = "425" height = "320" align=center />  <img src="images/nyDehaze.png" width = "425" height = "320" align=center />

<img src="images/flags.png" width = "425" height = "500" align=center />  <img src="images/flagsDehaze.png" width = "425" height = "500" align=center />

<img src="images/train.png" width = "425" height = "320" align=center />  <img src="images/trainDehaze.png" width = "425" height = "320" align=center />
