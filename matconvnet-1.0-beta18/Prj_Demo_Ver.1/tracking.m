% Email: elaheh.rashedi@wayne.edu
% Revised by Elaheh Rashedi, 10/21/2015

function [flag, initstate,imgSr, tracked_Images] = tracking(myvideo , initstate , myvideocurrenttime, fgr,imgVerified)
 disp('tracking started...');
    width = 224 ; lenght = 224;
    rand('state',0);
    %%
    x = initstate(1);% x axis at the Top left corner
    y = initstate(2);
    w = initstate(3);% width of the rectangle
    h = initstate(4);% height of the rectangle

    %%
   
    myvideo.currenttime = myvideocurrenttime;
    img = readFrame(myvideo);
    if length(size(img))==3
        img = rgb2gray(img);
    end
    img = double(img);

    %% 
    trparams.init_negnumtrain = 50;%number of trained negative samples
    trparams.init_postrainrad = 4;%radical scope of positive samples
    trparams.initstate = initstate;% object position [x y width height]
    trparams.srchwinsz = 25;% size of search window

    %% Classifier parameters
    clfparams.width = trparams.initstate(3);
    clfparams.height= trparams.initstate(4);
    % feature parameters
    % number of rectangle from 2 to 4.
    ftrparams.minNumRect =2;
    ftrparams.maxNumRect =4;  
    M = 100;% number of all weaker classifiers, i.e,feature pool
    %-------------------------
    posx.mu = zeros(M,1);% mean of positive features
    negx.mu = zeros(M,1);
    posx.sig= ones(M,1);% variance of positive features
    negx.sig= ones(M,1);

    lRate = 0.85;% Learning rate parameter
    %% Compute feature template
    [ftr.px,ftr.py,ftr.pw,ftr.ph,ftr.pwt] = HaarFtr(clfparams,ftrparams,M);

    %% Compute sample templates
    posx.sampleImage = sampleImgDet(img,initstate,trparams.init_postrainrad,1);
    negx.sampleImage = sampleImg(img,initstate,1.5*trparams.srchwinsz,4+trparams.init_postrainrad,trparams.init_negnumtrain);


    %% Feature extraction
    iH = integral(img);%Compute integral image
    posx.feature = getFtrVal(iH,posx.sampleImage,ftr);
    negx.feature = getFtrVal(iH,negx.sampleImage,ftr);
    [posx.mu,posx.sig,negx.mu,negx.sig] = classiferUpdate(posx,negx,posx.mu,posx.sig,negx.mu,negx.sig,lRate);% update distribution parameters

    %% Begin tracking
    xx = 1;
    while  hasFrame(myvideo)  && myvideo.currenttime < (myvideocurrenttime + 0.5)

        frameNumber = myvideo.currenttime * myvideo.FrameRate;
        %img = imread(img_dir(i).name);
        img = readFrame(myvideo);
        imgSr = img;% imgSr is used for showing tracking results.

        if length(size(img))==3
        img = rgb2gray(img);
        end    
        img = double(img); 
        iH = integral(img);%Compute integral image

        %% Coarse detection
        step = 4; % coarse search step
        detectx.sampleImage = sampleImgDet(img,initstate,trparams.srchwinsz,step);    
        detectx.feature = getFtrVal(iH,detectx.sampleImage,ftr);
        r = ratioClassifier(posx,negx,detectx.feature);% compute the classifier for all samples
        clf = sum(r);% linearly combine the ratio classifiers in r to the final classifier
        [c,index] = max(clf);
        x = detectx.sampleImage.sx(index);
        y = detectx.sampleImage.sy(index);
        w = detectx.sampleImage.sw(index);
        h = detectx.sampleImage.sh(index);
        initstate = [x y w h];

        %% Fine detection
        step = 1;
        detectx.sampleImage = sampleImgDet(img,initstate,10,step);    
        detectx.feature = getFtrVal(iH,detectx.sampleImage,ftr);
        r = ratioClassifier(posx,negx,detectx.feature);% compute the classifier for all samples
        clf = sum(r);% linearly combine the ratio classifiers in r to the final classifier
        [c,index] = max(clf);
        x = detectx.sampleImage.sx(index);
        y = detectx.sampleImage.sy(index);
        w = detectx.sampleImage.sw(index);
        h = detectx.sampleImage.sh(index);
        initstate = [x y w h];
        %% Show the tracking results
        %imshow(uint8(imgSr(y:y+h,x:x+w,:)),'parent',axee);
        imshow(uint8(imgSr));
        rectangle('Position',initstate,'LineWidth',2,'EdgeColor','g');
        face=imcrop(imgSr,initstate);
        imgVeri  = [imgVerified num2str(frameNumber) '.jpg'];
        imwrite(face,imgVeri);
        
        if (xx <=5)
            tracked_Images(:,:,:,xx) = imresize(face, [width,lenght]);
        end
        xx = xx+1;   
        
        %% Extract samples 
        posx.sampleImage = sampleImgDet(img,initstate,trparams.init_postrainrad,1);
        negx.sampleImage = sampleImg(img,initstate,1.5*trparams.srchwinsz,4+trparams.init_postrainrad,trparams.init_negnumtrain);
        %% Update all the features
        posx.feature = getFtrVal(iH,posx.sampleImage,ftr);
        negx.feature = getFtrVal(iH,negx.sampleImage,ftr); 
        [posx.mu,posx.sig,negx.mu,negx.sig] = classiferUpdate(posx,negx,posx.mu,posx.sig,negx.mu,negx.sig,lRate);% update distribution parameters  
    end
 flag = 0;
 disp('tracking completed..');
 
end