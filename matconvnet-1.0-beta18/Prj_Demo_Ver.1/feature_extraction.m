function FeaCNN=feature_extraction(Image)
    % pre-processing 
    disp('extracting features of the selected face...');
    Image = bsxfun(@minus,Image,vgg_net.meta.normalization.averageImage);


    % run the CNN  
    res = vl_simplenn(vgg_net, Image);
    FeaCNN = squeeze(gather(res(33).x)) ; %36 is ReLu of last FC layer
    disp('feature extraction completed ');
    
end