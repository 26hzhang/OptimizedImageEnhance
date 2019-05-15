function [frames,descriptors,gss,dogss]=Sift(I,varargin)
% SIFT Extract SIFT features
%   [FRAMES,DESCR]=SIFT(I) extracts the SIFT frames FRAMES and their
%   descriptors DESCR from the image I.
%
%   The image I must be gray-scale, of storage class DOUBLE and
%   ranging in [0,1].
%
%   FRAMES is a 4xK matrix storing one SIFT frame per column. Its
%   format is:
%     FRAMES(1:2,k)  center (X,Y) of the frame k,
%     FRAMES(3,k)    scale SIGMA of the frame k,
%     FRAMES(4,k)    orientation THETA of the frame k.
%   Note that the X,Y center coordinates are (0,0) based, contrary to
%   the standard MATLAB convention that uses (1,1) as the top-left
%   image coordiante. The plotting function PLOTSIFTFRAME() and
%   PLOTSIFTDESCRIPTOR() automatically shift the keypoints to the
%   default (1,1) reference.
%
%   DESCR is a DxK matrix stores one descriptor per columm (usually
%   D=128).
%
%   [FRAMES,DESCR,GSS,DOGSS]=SIFT(...) returns the Gaussian and
%   Difference of Gaussians scale spaces computed by the algorithm.
%
%   The function accepts the following option-value pairs:
%
%   Verbosity - Verbosity level [{0},1]
%     0 = quiet, 1 = print detailed progress report
%
%   BoundaryPoint - Remove frames on the image boundaries [0,{1}]
%     Remove points whose descriptor intersects the boundary.
%
%   NumOctaves - Number of octaves [1,2,...]
%     Number of octaves of the Gaussian scale space. By default it is
%     computed to cover all possible feature sizes.
%
%   FirstOctave - Index of the first octave [...,-1,{0},+1,...]
%     Setting the parameter to -1 has the effect of doubling the image
%     before computing the scale space.
%
%   NumLevels - [1,2,...]
%     Number of scale levels within each octave.
%
%   Sigma0 - Base smoothing [pixels]
%     Smoothing of the level 0 of octave 0 of the scale space. By
%     default it is set to be equivalent to the value 1.6 of [1].
%     Since however 1.6 is the smoothing of the level -1 and Simga0
%     of the level 0, the actual value is NOT 1.6.
%
%   SigmaN - Nominal smoothing [pixels, {0.5}]
%     Nominal smoothing of the input image.
%
%   Threshold - Strenght threshold [>= 0, {0.01}]
%     Maxima of the DOG scale space [1] below this threshold are
%     ignored. Smaller values accept more features.
%
%   EdgeThreshold - Localization threshold [>= 0, {10}]
%     Feature which have flattness score [1] above this threshold are
%     ignored. Bigger values accept more features.
%
%   Magnif - Descriptor window magnification
%     See SIFTDESCRIPTOR().
%
%   NumSpatialBins - Number of spatial bins [2,{4},6,...]
%     See SIFTDESCRIPTOR().
%
%   NumOrientbins - Number of orientation bins [1,2,...,{8},...]
%     See SIFTDESCRIPTOR().
%
%   See also GAUSSIANSS(), DIFFSS(), PLOTSIFTFRAME(), PLOTSIFTDESCRIPTOR(),
%            SIFTDESCRIPTOR(), SIFTMATCH().

[M,N,C] = size(I);

% Lowe's equivalents choices(default values)
S=3;
omin= 0;%-1;
O = 4;%floor(log2(min(M,N)))-omin-3;
sigma0=1.6*2^(1/S);
sigman=0.5;
thresh = 0.2 / S / 2;%0.04/S/2;
r = 18;%10;
NBP = 4;
NBO = 8;
magnif = 3.0;
discard_boundary_points = 1;
verb = 0;

if nargin > 1
    for k=1:2:length(varargin)
        switch lower(varargin{k})
            case 'numoctaves'
                O = varargin{k+1};
            case 'firstoctave'
                omin = varargin{k+1};
            case 'numlevels'
                S = varargin{k+1};
            case 'sigma0'
                sigma0 = varargin{k+1};
            case 'sigman'
                sigman = varargin{k+1};
            case 'threshold'
                thresh = varargin{k+1};
            case 'edgethreshold'
                r = varargin{k+1};
            case 'boundarypoint'
                discard_boundary_points = varargin{k+1};
            case 'numspatialbins'
                NBP = varargin{k+1};
            case 'numorientbins'
                NBO = varargin{k+1};
            case 'magnif'
                magnif = varargin{k+1};
            case 'verbosity'
                verb = varargin{k+1} ;
            otherwise
                error(['Unknown parameter ''' varargin{k} '''.']);
        end
    end
end

% The image I must be gray-scale, of storage class DOUBLE and ranging in [0,1].
if C > 1
  error('I should be a grayscale image') ;
end

frames = [];
descriptors = [];

% --------------------------------------------------------------------
%                     SIFT Detector and Descriptor
% --------------------------------------------------------------------

% compute the Gaussian scale space of image I, that is, construct the
% Gaussian Pyramid
if verb>0 
    fprintf('SIFT: computing scale space...'); tic; 
end
gss = gaussianss(I,sigman,O,S,omin,-1,S+1,sigma0);
if verb>0
    fprintf('(%.3f s gss; ',toc); tic; 
end

% compute the Difference of scale space, that is, construct the DoG Pyramid
dogss = diffss(gss);
if verb > 0
    fprintf('%.3f s dogss) done\n',toc); 
end

if verb > 0
    fprintf('\nSIFT scale space parameters [PropertyName in brackets]\n');
    fprintf('  sigman [SigmaN]        : %f\n', sigman);
    fprintf('  sigma0 [Sigma0]        : %f\n', dogss.sigma0);
    fprintf('       O [NumOctaves]    : %d\n', dogss.O);
    fprintf('       S [NumLevels]     : %d\n', dogss.S);
    fprintf('    omin [FirstOctave]   : %d\n', dogss.omin);
    fprintf('    smin                 : %d\n', dogss.smin);
    fprintf('    smax                 : %d\n', dogss.smax);
    fprintf('\nSIFT detector parameters\n')
    fprintf('  thersh [Threshold]     : %e\n', thresh);
    fprintf('       r [EdgeThreshold] : %.3f\n', r);
    fprintf('\nSIFT descriptor parameters\n')
    fprintf('  magnif [Magnif]        : %.3f\n', magnif);
    fprintf('     NBP [NumSpatialBins]: %d\n', NBP);
    fprintf('     NBO [NumOrientBins] : %d\n', NBO);
end

for o=1:gss.O
    if verb > 0
        fprintf('\nSIFT: processing octave %d\n', o-1+omin); tic; 
    end
    
    % Local maxima of the DOG octave
    % The 80% tricks discards early very weak points before refinement.
    oframes1 = siftlocalmax(  dogss.octave{o}, 0.8*thresh, dogss.smin  );
	oframes = [oframes1 , siftlocalmax( - dogss.octave{o}, 0.8*thresh, dogss.smin)];
    
    if verb > 0
        fprintf('SIFT: %d initial points (%.3f s)\n',size(oframes,2),toc);tic;
    end
	
    if size(oframes, 2) == 0
        continue;
    end
    
    % Remove points too close to the boundary
    if discard_boundary_points
        rad = magnif * gss.sigma0 * 2.^(oframes(3,:)/gss.S) * NBP / 2 ;
        sel=find(oframes(1,:)-rad >= 1 & oframes(1,:)+rad <= size(gss.octave{o},2) & ...
                    oframes(2,:)-rad >= 1 & oframes(2,:)+rad <= size(gss.octave{o},1));
        oframes=oframes(:,sel);
        if verb > 0
            fprintf('SIFT: %d away from boundary\n', size(oframes,2)); tic;
        end
    end
		
    % Refine the location, threshold strength and remove points on edges
   	oframes = siftrefinemx(oframes, dogss.octave{o}, dogss.smin, thresh, r);
    if verb > 0
        fprintf('SIFT: %d refined (%.3f s)\n', size(oframes,2),toc); tic;
    end

    % Compute the orientations
	oframes = siftormx(oframes, gss.octave{o}, gss.S, gss.smin, gss.sigma0 );
		
    % Store frames
	x = 2^(o-1+gss.omin) * oframes(1,:);
	y = 2^(o-1+gss.omin) * oframes(2,:);
	sigma = 2^(o-1+gss.omin) * gss.sigma0 * 2.^(oframes(3,:)/gss.S);		
	frames = [frames, [x(:)'; y(:)'; sigma(:)'; oframes(4,:)]];

    % Descriptors
    if verb > 0
        fprintf('\nSIFT: computing descriptors...'); tic;
    end
	sh = siftdescriptor(gss.octave{o}, oframes, gss.sigma0, gss.S, gss.smin, ...
                    'Magnif', magnif, 'NumSpatialBins', NBP, 'NumOrientBins', NBO);
    descriptors = [descriptors, sh];
    if verb > 0 
        fprintf('done (%.3f s)\n',toc); 
    end
end



function SS = gaussianss(I,sigman,O,S,omin,smin,smax,sigma0)
% GAUSSIANSS
%   SS = GAUSSIANSS(I,SIGMAN,O,S,OMIN,SMIN,SMAX,SIGMA0) returns the
%   Gaussian scale space of image I. Image I is assumed to be
%   pre-smoothed at level SIGMAN. O,S,OMIN,SMIN,SMAX,SIGMA0 are the
%   parameters of the scale space

%Scale Space Multiplicative Step k
k = 2^(1/S);

if nargin<7 
   sigma0=1.6*k;
end

if omin<0
   for o=1:-omin
        I=doubleSize(I);
   end
elseif omin>0
   for o=1:-omin
        I=halveSize(I);
   end
end

[M,N] = size(I);                      %size of image
dsigma0 = sigma0*sqrt(1-1/k^2);       %scale step factor
so=-smin+1;                           %index offset

% Scale space structure
SS.O = O;
SS.S = S;
SS.sigma0 = sigma0;
SS.omin = omin;
SS.smin = smin;
SS.smax = smax;

%First octave

% The first level of the first octave has scale index (o,s) =
% (omin,smin) and scale coordinate
%    sigma(omin,smin) = sigma0 2^omin k^smin
% The input image I is at nominal scale sigman. Thus in order to get
% the first level of the pyramid we need to apply a smoothing of
%   sqrt( (sigma0 2^omin k^smin)^2 - sigman^2 ).
% As we have pre-scaled the image omin octaves (up or down,
% depending on the sign of omin), we need to correct this value
% by dividing by 2^omin, getting
%   sqrt( (sigma0 k^smin)^2 - (sigman/2^omin)^2 )
SS.octave{1} = zeros(M,N,smax-smin+1); 
SS.octave{1}(:,:,1) = smooth(I,sqrt((sigma0*k^smin)^2 -(sigman/2^omin)^2));

for s=smin+1:smax
    % Here we go from (omin,s-1) to (omin,s). The extra smoothing
    % standard deviation is
    %  (sigma0 2^omin 2^(s/S) )^2 - (simga0 2^omin 2^(s/S-1/S) )^2
    % Aftred dividing by 2^omin (to take into account the fact
    % that the image has been pre-scaled omin octaves), the
    % standard deviation of the smoothing kernel is
    %   dsigma = sigma0 k^s sqrt(1-1/k^2)
    dsigma = k^s * dsigma0;
    SS.octave{1}(:,:,s+so) = smooth( squeeze(SS.octave{1}(:,:,s-1+so)) ,dsigma);
end

%Other octaves
for o=2:O
    % We need to initialize the first level of octave (o,smin) from
    % the closest possible level of the previous octave. A level (o,s)
    % in this octave corrsponds to the level (o-1,s+S) in the previous
    % octave. In particular, the level (o,smin) correspnds to
    % (o-1,smin+S). However (o-1,smin+S) might not be among the levels
    % (o-1,smin), ..., (o-1,smax) that we have previously computed.
    % The closest pick is
    %                       /  smin+S    if smin+S <= smax
    % (o-1,sbest) , sbest = |
    %                       \  smax      if smin+S > smax
    % The amount of extra smoothing we need to apply is then given by
    %  ( sigma0 2^o 2^(smin/S) )^2 - ( sigma0 2^o 2^(sbest/S - 1) )^2
    % As usual, we divide by 2^o to cancel out the effect of the
    % downsampling and we get
    %  ( sigma 0 k^smin )^2 - ( sigma0 2^o k^(sbest - S) )^2
    sbest = min(smin+S,smax);
    TMP = halvesize( squeeze(SS.octave{o-1}(:,:,sbest+so)) );
    sigma_next = sigma0*k^smin;
    sigma_prev = sigma0*k^(sbest-S);
    
    if (sigma_next>sigma_prev)
       sig=sqrt(sigma_next^2-sigma_prev^2);
       TMP= smooth( TMP,sig);
    end
    
    [M,N] = size(TMP);
    SS.octave{o} = zeros(M,N,smax-smin+1); 
    SS.octave{o}(:,:,1) = TMP;
    
    for s=smin+1:smax
        % The other levels are determined as above for the first octave.
        dsigma = k^s * dsigma0;
        SS.octave{o}(:,:,s+so) = smooth( squeeze(SS.octave{o}(:,:,s-1+so)) ,dsigma);
    end
end

% -------------------------------------------------------------------------
%                                      Auxiliary functions
% -------------------------------------------------------------------------
function J = halvesize(I)
J=I(1:2:end,1:2:end);


function J = doubleSize(I)
[M,N]=size(I) ;
J = zeros(2*M,2*N) ;
J(1:2:end,1:2:end) = I ;
J(2:2:end-1,2:2:end-1) = 0.25*I(1:end-1,1:end-1) + 0.25*I(2:end,1:end-1) + ...
	0.25*I(1:end-1,2:end) + 0.25*I(2:end,2:end) ;
J(2:2:end-1,1:2:end) = 0.5*I(1:end-1,:) + 0.5*I(2:end,:) ;
J(1:2:end,2:2:end-1) = 0.5*I(:,1:end-1) + 0.5*I(:,2:end) ;


function J = smooth(I,s)
%filter 
h=fspecial('gaussian',ceil(4*s),s);
%convolution
J=imfilter(I,h);
return;



function dss = diffss(ss)
% DIFFSS  Difference of scale space
%   DSS=DIFFSS(SS) returns a scale space DSS obtained by subtracting
%   consecutive levels of the scale space SS.
%
%   In SIFT, this function is used to compute the difference of
%   Gaussian scale space from the Gaussian scale space of an image.
dss.smin = ss.smin;
dss.smax = ss.smax-1;
dss.omin =ss.omin;
dss.O = ss.O;
dss.S = ss.S;
dss.sigma0 = ss.sigma0;
for o=1:dss.O
    % Can be done at once, but it turns out to be faster in this way
    [M,N,S] = size(ss.octave{o});
    dss.octave{o} = zeros(M,N,S-1);
    for s=1:S-1
        dss.octave{o}(:,:,s) = ss.octave{o}(:,:,s+1) -  ss.octave{o}(:,:,s);   
    end
end



function J = siftlocalmax(octave, thresh,smin)
[N,M,S]=size(octave); 
nb=1;
k=0.0002;
%for each point of this scale space, we look for extrama bigger than thresh
J = [];
for s=2:S-1
    for j=20:M-20
        for i=20:N-20
            a=octave(i,j,s);
            if a>thresh+k ...
                    && a>octave(i-1,j-1,s-1)+k && a>octave(i-1,j,s-1)+k && a>octave(i-1,j+1,s-1)+k ...
                    && a>octave(i,j-1,s-1)+k && a>octave(i,j+1,s-1)+k && a>octave(i+1,j-1,s-1)+k ...
                    && a>octave(i+1,j,s-1)+k && a>octave(i+1,j+1,s-1)+k && a>octave(i-1,j-1,s)+k ...
                    && a>octave(i-1,j,s)+k && a>octave(i-1,j+1,s)+k && a>octave(i,j-1,s)+k ...
                    && a>octave(i,j+1,s)+k && a>octave(i+1,j-1,s)+k && a>octave(i+1,j,s)+k ...
                    && a>octave(i+1,j+1,s)+k && a>octave(i-1,j-1,s+1)+k && a>octave(i-1,j,s+1)+k ...
                    && a>octave(i-1,j+1,s+1)+k && a>octave(i,j-1,s+1)+k && a>octave(i,j+1,s+1)+k ...
                    && a>octave(i+1,j-1,s+1)+k && a>octave(i+1,j,s+1)+k && a>octave(i+1,j+1,s+1)+k
                J(1,nb)=j-1;
                J(2,nb)=i-1;
                J(3,nb)=s+smin-1;
                nb=nb+1;
            end
        end
    end
end



function J=siftrefinemx(oframes,octave,smin,thres,r)
[M,N,S]=size(octave);  
[L,K]=size(oframes);
comp=1;
for p = 1:K
    b=zeros(1,3) ;
    A=oframes(:,p);
    x=A(1)+1;
    y=A(2)+1;
    s=A(3)+1-smin;
    %Local maxima extracted from the DOG have coordinates 1<=x<=N-2, 1<=y<=M-2
    % and 1<=s-mins<=S-2. This is also the range of the points that we can refine.
    if(x < 2 || x > N-1 || y < 2 || y > M-1 || s < 2 || s > S-1) 
        continue ;
    end
    val=octave(y,x,s);
    Dx=0;Dy=0;Ds=0;Dxx=0;Dyy=0;Dss=0;Dxy=0;Dxs=0;Dys=0 ;
    dx = 0 ;
    dy = 0 ;
    for iter = 1:5 
        A = zeros(3,3) ;             
        x = x + dx ;
        y = y + dy ;
        if (x < 2 || x > N-1 || y < 2 || y > M-1 )  break ; end
        % Compute the gradient.
        Dx = 0.5 * (octave(y,x+1,s) - octave(y,x-1,s));
        Dy = 0.5 * (octave(y+1,x,s) - octave(y-1,x,s)) ;
        Ds = 0.5 * (octave(y,x,s+1) - octave(y,x,s-1)) ; 
        % Compute the Hessian.
        Dxx = (octave(y,x+1,s) + octave(y,x-1,s) - 2.0 * octave(y,x,s)) ;
        Dyy = (octave(y+1,x,s) + octave(y-1,x,s) - 2.0 * octave(y,x,s)) ;
        Dss = (octave(y,x,s+1) + octave(y,x,s-1) - 2.0 * octave(y,x,s)) ;  
        Dys = 0.25 * ( octave(y+1,x,s+1) + octave(y-1,x,s-1) - octave(y-1,x,s+1) - octave(y+1,x,s-1) ) ;
        Dxy = 0.25 * ( octave(y+1,x+1,s) + octave(y-1,x-1,s) - octave(y-1,x+1,s) - octave(y+1,x-1,s) ) ;
        Dxs = 0.25 * ( octave(y,x+1,s+1) + octave(y,x-1,s-1) - octave(y,x-1,s+1) - octave(y,x+1,s-1) ) ;
        % Solve linear system. 
        A(1,1) = Dxx ;
        A(2,2) = Dyy ;
        A(3,3) = Dss ;
        A(1,2) = Dxy ;
        A(1,3) = Dxs ; 
        A(2,3) = Dys ;
        A(2,1) = Dxy ;
        A(3,1) = Dxs ;
        A(3,2) = Dys ;
        b(1) = - Dx ;
        b(2) = - Dy ;
        b(3) = - Ds ;
        c=b*inv(A);
        % If the translation of the keypoint is big, move the keypoint and re-iterate the computation. Otherwise we are all set.
        if (c(1) >  0.6 && x < N-2 )
            if (c(1) < -0.6 && x > 1)
                dx=0;
            else
                dx=1;
            end
        else
            if (c(1) < -0.6 && x > 1)
                dx=-1;
            else
                dx=0;
            end
        end   
        if (c(2) >  0.6 && y < N-2 )
            if (c(2) < -0.6 && y > 1)
                dy=0;
            else
                dy=1;
            end
        else
            if (c(2) < -0.6 && y > 1)
                dy=-1;
            else
                dy=0;
            end
        end           
        if( dx == 0 && dy == 0 ) break ; end
    end
    %we keep the value only of it verify the conditions
    val = val + 0.5 * (Dx * c(1) + Dy * c(2) + Ds * c(3)) ;
    score = (Dxx+Dyy)*(Dxx+Dyy) / (Dxx*Dyy - Dxy*Dxy) ; 
    xn = x + c(1) ;
    yn = y + c(2) ;
    sn = s + c(3) ;
    if (abs(val) > thres) && ...
            (score < (r+1)*(r+1)/r) && ...
            (score >= 0) && ...
            (abs(c(1)) < 1.5) && ...
            (abs(c(2)) < 1.5) && ...
            (abs(c(3)) < 1.5) && ...
            (xn >= 0) && ...
            (xn <= M-1) && ...
            (yn >= 0) && ...
            (yn <= N-1) && ...
            (sn >= 0) && ...
            (sn <= S-1)              
        J(1,comp)=xn-1;
        J(2,comp)=yn-1;
        J(3,comp)=sn-1+smin;
        comp=comp+1;
    end 
end
return



function oframes = siftormx(oframes, octave, S, smin, sigma0 )
% this function computes the major orientation of the keypoint (oframes).
% Note that there can be multiple major orientations. In that case, the
% SIFT keys will be duplicated for each major orientation
% Author: Yantao Zheng. Nov 2006.  For Project of CS5240

frames = [];                  
win_factor = 1.5 ;  
NBINS = 36;
histo = zeros(1, NBINS);
[M, N, s_num] = size(octave); % M is the height of image, N is the width of image; num_level is the number of scale level of the octave

key_num = size(oframes, 2);
magnitudes = zeros(M, N, s_num);
angles = zeros(M, N, s_num);
% compute image gradients
for si = 1: s_num
    img = octave(:,:,si);
    dx_filter = [-0.5 0 0.5];
    dy_filter = dx_filter';
    gradient_x = imfilter(img, dx_filter);
    gradient_y = imfilter(img, dy_filter);
    magnitudes(:,:,si) =sqrt( gradient_x.^2 + gradient_y.^2);
    angles(:,:,si) = mod(atan(gradient_y ./ (eps + gradient_x)) + 2*pi, 2*pi);
end

% round off the cooridnates and 
x = oframes(1,:);
y = oframes(2,:) ;
s = oframes(3,:);

x_round = floor(oframes(1,:) + 0.5);
y_round = floor(oframes(2,:) + 0.5);
scales = floor(oframes(3,:) + 0.5) - smin;

for p=1:key_num
    s = scales(p);
    xp= x_round(p);
    yp= y_round(p);
    sigmaw = win_factor * sigma0 * 2^(double (s / S)) ;
    W = floor(3.0* sigmaw);
    
    for xs = xp - max(W, xp-1): min((N - 2), xp + W)
        for ys = yp - max(W, yp-1) : min((M-2), yp + W)
            dx = (xs - x(p));
            dy = (ys - y(p));
            if dx^2 + dy^2 <= W^2 % the points are within the circle
               wincoef = exp(-(dx^2 + dy^2)/(2*sigmaw^2));
               bin = round( NBINS *  angles(ys, xs, s+ 1)/(2*pi) + 0.5);

               histo(bin) = histo(bin) + wincoef * magnitudes(ys, xs, s+ 1);
            end
            
        end
    end
    
    theta_max = max(histo);
    theta_indx = find(histo> 0.8 * theta_max);
    
    for i = 1: size(theta_indx, 2)
        theta = 2*pi * theta_indx(i) / NBINS;
        frames = [frames, [x(p) y(p) s theta]'];        
    end   
end
oframes = frames;



function descriptors = siftdescriptor(octave, oframes, sigma0, S, smin, varargin)
% gaussian scale space of an octave
% frames containing keypoint coordinates and scale, and orientation
% base sigma value
% level of scales in the octave
for k=1:2:length(varargin)
	switch lower(varargin{k})
      case 'magnif'
        magnif = varargin{k+1} ;
        
      case 'numspatialbins'
        NBP = varargin{k+1} ;  
        
      case  'numorientbins'
        NBO = varargin{k+1} ;   
        
      otherwise
        error(['Unknown parameter ' varargin{k} '.']) ;
     end
end 
      
                               
num_spacialBins = NBP;
num_orientBins = NBO;
key_num = size(oframes, 2);
% compute the image gradients 
[M, N, s_num] = size(octave); % M is the height of image, N is the width of image; num_level is the number of scale level of the octave
descriptors = [];
magnitudes = zeros(M, N, s_num);
angles = zeros(M, N, s_num);
% compute image gradients
for si = 1: s_num
    img = octave(:,:,si);
    dx_filter = [-0.5 0 0.5];
    dy_filter = dx_filter';
    gradient_x = imfilter(img, dx_filter);
    gradient_y = imfilter(img, dy_filter);
    magnitudes(:,:,si) =sqrt( gradient_x.^2 + gradient_y.^2);
%     if sum( gradient_x == 0) > 0
%         fprintf('00');
%     end
    angles(:,:,si) = mod(atan(gradient_y ./ (eps + gradient_x)) + 2*pi, 2*pi);
end

x = oframes(1,:);
y = oframes(2,:);
s = oframes(3,:);
% round off
x_round = floor(oframes(1,:) + 0.5);
y_round = floor(oframes(2,:) + 0.5);
scales =  floor(oframes(3,:) + 0.5) - smin;

for p = 1: key_num

    s = scales(p);
    xp= x_round(p);
    yp= y_round(p);
    theta0 = oframes(4,p);
    sinth0 = sin(theta0) ;
    costh0 = cos(theta0) ;
    sigma = sigma0 * 2^(double (s / S)) ;
    SBP = magnif * sigma;
    %W =  floor( sqrt(2.0) * SBP * (NBP + 1) / 2.0 + 0.5);
    W =   floor( 0.8 * SBP * (NBP + 1) / 2.0 + 0.5);
    
    descriptor = zeros(NBP, NBP, NBO);
    
    % within the big square, select the pixels with the circle and put into
    % the histogram. no need to do rotation which is very expensive
    for dxi = max(-W, 1-xp): min(W, N -2 - xp)
        for dyi = max(-W, 1-yp) : min(+W, M-2-yp)
            mag = magnitudes(yp + dyi, xp + dxi, s); % the gradient magnitude at current point(yp + dyi, xp + dxi)
            angle = angles(yp + dyi, xp + dxi, s) ;  % the gradient angle at current point(yp + dyi, xp + dxi)
            angle = mod(-angle + theta0, 2*pi);      % adjust the angle with the major orientation of the keypoint and mod it with 2*pi
            dx = double(xp + dxi - x(p));            % x(p) is the exact keypoint location (floating number). dx is the relative location of the current pixel with respect to the keypoint
            dy = double(yp + dyi - y(p));            % dy is the relative location of the current pixel with respect to the keypoint
            
            nx = ( costh0 * dx + sinth0 * dy) / SBP ; % nx is the normalized location after rotation (dx, dy) with the major orientation angle. this tells which x-axis spatial bin the pixel falls in 
            ny = (-sinth0 * dx + costh0 * dy) / SBP ; 
            nt = NBO * angle / (2* pi) ;
            wsigma = NBP/2 ;
            wincoef =  exp(-(nx*nx + ny*ny)/(2.0 * wsigma * wsigma)) ;
            
            binx = floor( nx - 0.5 ) ;
            biny = floor( ny - 0.5 ) ;
            bint = floor( nt );
            rbinx = nx - (binx+0.5) ;
            rbiny = ny - (biny+0.5) ;
            rbint = nt - bint ;
             
            for(dbinx = 0:1) 
               for(dbiny = 0:1) 
                   for(dbint = 0:1) 
                        % if condition limits the samples within the square
                        % width W. binx+dbinx is the rotated x-coordinate.
                        % therefore the sampling square is effectively a
                        % rotated one
                        if( binx+dbinx >= -(NBP/2) && ...
                            binx+dbinx <   (NBP/2) && ...
                            biny+dbiny >= -(NBP/2) && ...
                            biny+dbiny <   (NBP/2) &&  isnan(bint) == 0) 
                              
                              weight = wincoef * mag * abs(1 - dbinx - rbinx) ...
                                  * abs(1 - dbiny - rbiny) ...
                                  * abs(1 - dbint - rbint) ;
   
                              descriptor(binx+dbinx + NBP/2 + 1, biny+dbiny + NBP/2+ 1, mod((bint+dbint),NBO)+1) = ...
                                  descriptor(binx+dbinx + NBP/2+ 1, biny+dbiny + NBP/2+ 1, mod((bint+dbint),NBO)+1 ) +  weight ;
                        end
                   end
               end
            end
        end  
    end
    descriptor = reshape(descriptor, 1, NBP * NBP * NBO);
    descriptor = descriptor ./ norm(descriptor);
            
            %Truncate at 0.2
    indx = find(descriptor > 0.2);
    descriptor(indx) = 0.2;
    descriptor = descriptor ./ norm(descriptor);
    
    descriptors = [descriptors, descriptor'];
end