## Optimized Contrast Enhancement for Real-time Image and Video Dehazing
It is a fast and optimized dehazing algorithm for hazy images and videos is proposed in this work. Based on the observation that a hazy image exhibits low contrast in general, they restore the hazy image by enhancing its contrast. However, the overcompensation of the degraded contrast may truncate pixel values and cause information loss. Therefore, they formulate a cost function that consists of the contrast term and the information loss term. By minimizing the cost function, the proposed algorithm enhances the contrast and preserves the information optimally. Moreover, they extend the static image dehazing algorithm to real-time video dehazing. They reduce flickering artifacts in a dehazed video sequence by making trans- mission values temporally coherent.

### Results
##### Airlight Selection
<img src="images/airlight.png" width = "425" height = "425" align=center />

##### Haze Removal Results
<img src="images/org-canon.png" width = "425" height = "320" align=center />  <img src="images/coarse-canon.png" width = "425" height = "320" align=center />
<img src="images/dehaze-canon.png" width = "425" height = "320" align=center />  <img src="images/refine-canon.png" width = "425" height = "320" align=center />

<img src="images/org-flags.png" width = "425" height = "500" align=center />  <img src="images/coarse-flags.png" width = "425" height = "500" align=center />
<img src="images/dehaze-flags.png" width = "425" height = "500" align=center />  <img src="images/refine-flags.png" width = "425" height = "500" align=center />

<img src="images/org-train.png" width = "425" height = "320" align=center />  <img src="images/coarse-train.png" width = "425" height = "320" align=center />
<img src="images/dehaze-train.png" width = "425" height = "320" align=center />  <img src="images/refine-train.png" width = "425" height = "320" align=center />

<img src="images/org-ny.png" width = "425" height = "320" align=center />  <img src="images/coarse-ny.png" width = "425" height = "320" align=center />
<img src="images/dehaze-ny.png" width = "425" height = "320" align=center />  <img src="images/refine-ny.png" width = "425" height = "320" align=center />
