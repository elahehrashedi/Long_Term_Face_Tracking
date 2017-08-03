function result = VTWVerification( videoName, start_time, px1, py1, px2, py2,...
    tracking_frames, similarity_threshold ,tp_tracking,...
    tp_not_verified_face, tp_not_face, path)

%// 50 , 0.7 , 3 , 70 , 120
saved_tracking_frames = tracking_frames ;
%tracking_frames = 15;
%similarity_threshold = 0.7;
%tp_tracking = 5;
%tp_not_verified_face = 5;
%tp_not_face = 10;
%imgVerified = '.\Data\imgVerified\162\';

%addpath(genpath(path));
% convnetpath = 'D:\Wayne Courses\Winter 2016\Project\FaceRetrieval\matconvnet-1.0-beta18\matconvnet-1.0-beta18';
% addpath(genpath('D:\Wayne Courses\Winter 2016\Project\FaceRetrieval\libsvm-master'));
% addpath(genpath('D:\Wayne Courses\Winter 2016\Project\FaceRetrieval\matconvnet-1.0-beta18\matconvnet-1.0-beta18\Prj_Demo_Ver.1\FCT'));
% addpath(genpath(convnetpath));

[pathstr,name,ext] = fileparts(videoName);
baseDir  = '.\Data';
mkdir( [baseDir '\imgVerified\'],name);

imgVerified = strcat(baseDir, '\imgVerified\',name ,'\');

%% load mat files
%vl_setupnn;

%% load Cascade CNN detector '
global net12;
global net12_c;
global net24;
global net24_c;
global net48;
global net48_c;
net12 = load(strcat(path,'\matconvnet-1.0-beta18\Prj_Demo_Ver.1\12net-newborn\f12net.mat')) ;
net12_c = load(strcat(path,'\matconvnet-1.0-beta18\Prj_Demo_Ver.1\12net-cc-v1\f12net_c.mat')) ;
net24 = load(strcat(path,'\matconvnet-1.0-beta18\Prj_Demo_Ver.1\24net-newborn\f24net-cpu.mat')) ;
net24_c = load(strcat(path,'\matconvnet-1.0-beta18\Prj_Demo_Ver.1\24net-cc-v1-no256\f24netc.mat')) ;
net48 = load(strcat(path,'\matconvnet-1.0-beta18\Prj_Demo_Ver.1\48net-6hard\f48net-cpu.mat')) ;
net48_c = load(strcat(path,'\matconvnet-1.0-beta18\Prj_Demo_Ver.1\48net-cc-cifar-v2-submean\f48netc.mat')) ;


%%
tracking_box = [px1, py1, px2, py2];
[pathstr,vname,ext] = fileparts(videoName);
%posFile = fopen(['.\VideoPos\' vname '.txt'],'w');
global vgg_net;
vgg_net=load(strcat(path,'\matconvnet-1.0-beta18\Prj_Demo_Ver.1\vgg_face_cut_fc7.mat'));


% size of face for feature extraction
width = 224 ; lenght = 224;

try
    myvideo = VideoReader( videoName );
catch exception3
    video_available=0;
    display (sprintf('\n\n\n\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n'));
    display (sprintf('========> this file is not available: %s\n',videoName));
    display (exception3.message) ;
    display (sprintf('\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n'));
    return;
end
myvideo.currenttime = start_time ;
myframe = readFrame(myvideo) ;


frame_size = size(myframe);
frame_number = round ( myvideo.currenttime * myvideo.framerate) ;
%frame_size = size(readFrame(myvideo));

video_duration = myvideo.duration ;
frame_rate = myvideo.framerate ;
numofframes = video_duration * frame_rate ;


x1 = tracking_box (1);y1 = tracking_box (2);x2 = tracking_box (3);y2 = tracking_box (4);
% get the selected image do detection on it
Image = myframe(x1:x2,y1:y2,:);
dbox =detection(Image);
result = 1;
if isempty(dbox)
       result = 0;
else
positionMat = [0,0,0,0,0];
pos_first_half = [0,0,0,0,0];
pos_second_half = [0,0,0,0,0];
posCount = 1; 
pos_first_half_count = 1;
pos_second_half_count = 1;
% save the detected face
dx1 = dbox(:,1); dy1 = dbox(:,2); dx2 = dbox(:,3); dy2 = dbox(:,4);
Image = myframe(x1+dx1:x1+dx1+dy2-dy1 ,y1+dy1:y1+dy1+dx2-dx1 ,:);
frameNumber = round(myvideo.currenttime * myvideo.FrameRate);
%imgVeri  = [imgVerified num2str(frameNumber) '.jpg'];
%imwrite(Image,imgVeri);
%fprintf(posFile, '%d %d %d %d %d\n' , frameNumber, dx1, dy1, dx2-dx1, dy2-dy1);

pos_second_half(pos_second_half_count,:)= [ round(frameNumber), dx1, dy1, dx2-dx1, dy2-dy1];
pos_second_half_count = pos_second_half_count +1;
do_tracking = 1;
pad = 10;
tracking_position = [y1+dy1, x1+dx1,dy2-dy1, dx2-dx1];


while_flag = 1 ;
first_run = 1;
last_frame = 1 ;
is_first_half = 0;

% frame_box = frame_number, position
frame_box_counter = 1 ;
while hasFrame(myvideo) &&  while_flag 
    
    if (do_tracking == 1 )
        %% do tracking
        second_tracking = 1;
        % New Tracking
        if (first_run)
            tracking_frames = 5 ;
        else
            tracking_frames = saved_tracking_frames ;
        end
        
        
        for i=last_frame:1:(tracking_frames+last_frame-1)
            % add the images which we skip ( delete last_frame from
            % images)
            if (hasFrame(myvideo))
                %img_files{i} = readFrame(myvideo);
                myframe = readFrame(myvideo);
                img_files{i} = myframe;
                
                frame_box {frame_box_counter} = {frame_number,[0,0,0,0]};
                frame_box_counter = frame_box_counter + 1 ;
                frame_number = frame_number + 1 ;
                %readFrame(myvideo);
            end
        end
        if (~first_run) %delete the previous tracked frames
            for i=1:1:(last_frame)
                img_files(i) = [] ;
                % img_files = fun_removecellrowcols(img_files,i,'rows')
            end
            %img_files(~cellfun('isempty',img_files));
            %img_files = [];
        end
        
        disp('tracking started...');
        t_boxes  = my_run_tracker (  tracking_position , first_run ) ; % rpt folder
        %just for show
        for k=1:1: size(t_boxes,1)%tracking_frames
            x=t_boxes(k,2); y = t_boxes(k,1); h = t_boxes(k,3) ; w = t_boxes(k,4) ;
            %imshow(img_files{k}(x:x+w,y:y+h,:));
            %imgVeri  = [imgVerified num2str(frameNumber) '_' num2str(k) '.jpg'];
            
            if is_first_half
                pos_first_half(pos_first_half_count,:)= [round(frameNumber+k), x, y, h, w];
                pos_first_half_count = pos_first_half_count +1;
            else
                pos_second_half(pos_second_half_count,:)= [round(frameNumber+k), x, y, h, w];
                pos_second_half_count = pos_second_half_count +1;
            end

            tracked_x = x; tracked_x2 = x+w ; tracked_y = y; tracked_y2 = y+h ;
            if tracked_x < 0
                tracked_x = 2 ;
            end
            if tracked_y < 0
                tracked_y = 2;
            end
            if tracked_x2 > frame_size(1)
                tracked_x2 = frame_size(1)-2;
            end
            if tracked_y2 > frame_size(2)
                tracked_y2 = frame_size(2)-2;
            end
            
        end
        disp('tracking completed...');
        
        for j=size(t_boxes,1):-1:1   % check and see what was the intention
            frame_box{1,j}{1,2} = t_boxes(j,:);
        end
        tracking_position = t_boxes(end,:) ; % change tracking position
        if (first_run ==1) %feature extraction
            for k=1:1:5
                x=t_boxes(k,2); y = t_boxes(k,1); h = t_boxes(k,3) ; w = t_boxes(k,4) ;
                tracked_Images (:,:,:,k) = imresize(img_files{k}(x:x+w,y:y+h,:), [width,lenght]);
            end
            
            Image_query = tracked_Images; % 5 images
            FeaCNN_query = feature_extraction(Image_query);
            FeaCNN_query = mean(FeaCNN_query,2);
            first_run = 0;
            second_tracking = 0;
        end
        
        last_frame = size(t_boxes,1) ;
        
        %myframe = readFrame(myvideo);
        %frame_number = frame_number + 1 ;
        %myframe = img;
        %myframe = img_files{end} ;%deleted because we are in the
        %middle of tracking and verification might be rejected
        % so myframe will change
        if (~second_tracking )
            myframe = img_files{(last_frame)};
        elseif (last_frame == saved_tracking_frames-1)
            myframe = img_files{(last_frame)};
        else
            myframe = img_files{(last_frame)+1};
        end
        %frameNumber = myvideo.currenttime * myvideo.FrameRate;
        frameNumber = round(frameNumber + size(t_boxes,1));
        % get the position of tracked face in the next frame
        yy1 = tracking_position(1); xx1 =tracking_position(2);
        yy2 = tracking_position(1)+tracking_position(3);
        xx2 = tracking_position(2)+tracking_position(4);
        
        if xx1 < 0
            xx1 = 2 ;
        end
        if yy1 < 0
            yy1 = 2;
        end
        if xx2 > frame_size(1)
            xx2 = frame_size(1)-2;
        end
        if yy2 > frame_size(2)
            yy2 = frame_size(2)-2;
        end
        img_part = myframe(xx1:xx2,yy1:yy2,:);
        tracked_image = imresize(img_part, [width,lenght]);
        Tracked_feature = feature_extraction (tracked_image);
        Score_cos = (FeaCNN_query'*Tracked_feature)/(norm(FeaCNN_query)*norm(Tracked_feature));
        if Score_cos > similarity_threshold
            do_tracking = 1;
        else
            
            if ((myvideo.currenttime) < myvideo.duration)
                myvideo.currenttime = myvideo.currenttime ;% +tp_tracking;
                
            else
                myvideo.currenttime = 1/myvideo.framerate ;
                %stop_flag =1 ; %video is finished
                is_first_half = 1;
                disp('go to the first part');
            end
            
            do_tracking = 0;
        end
        
        
    else % if (do_tracking == 0 )
        %% read frame and do detection
        
        myframe = readFrame(myvideo);     
        frameNumber = round(myvideo.currenttime * myvideo.FrameRate);
        boxes = detection(myframe);
        %% find a face
        if ~isempty(boxes)
            x1 = boxes(:,1);
            y1 = boxes(:,2);
            x2 = boxes(:,3);
            y2 = boxes(:,4);
            boxes_size = size(boxes);
            for xx = 1:boxes_size(1)
                tempx1=x1(xx)-pad;
                tempy1= y1(xx)-pad;
                tempx2 = x2(xx);
                tempy2= y2(xx);
                if (tempx1 < 0)
                    tempx1 = 1;
                end
                if (tempy1 <0)
                    tempy1 =1 ;
                end
                if tempx2 > frame_size(1)
                    tempx2 = frame_size(1)-2;
                end
                if tempy2 > frame_size(2)
                    tempy2 = frame_size(2)-2;
                end
                if (tempx2<=0 || tempy2<=0)
                    disp(tempx2);
                    disp(tempy2);
                end
                temp = myframe(round(tempx1):round(tempx2),round(tempy1):round(tempy2),:);
                Images(:,:,:,xx) = imresize(temp, [width,lenght]);
            end
            
            %% feature extraction for each detected face
            FeaCNN = feature_extraction(Images);
            Score_cos = zeros(boxes_size(1),1);
            Final_id = [];
            for i = 1:boxes_size(1)
                %disp('cosin computation..');
                y = FeaCNN(:,i);
                Score_cos(i) = (FeaCNN_query'*y)/(norm(FeaCNN_query)*norm(y));
                if Score_cos(i) > similarity_threshold
                    Final_id(i) = 1;
                else
                    Final_id(i) = -1;
                    disp('not verified..');
                end
            end
            
            labels = ones(boxes_size(1),1);
            [v,I] = find(Final_id==1);
            verified_flag = 0 ;
            for count = 1:size(v)
                if (I == 1)
                    disp('verified...');
                    face=imcrop(myframe,[y1(count)-pad,x1(count)-pad,(y2(count)-y1(count)),x2(count)-x1(count)]);
                    tracking_position = [y1(count)-pad,x1(count)-pad,(y2(count)-y1(count)),x2(count)-x1(count)];
                    
                    
                    %imgVeri  = [imgVerified num2str(frameNumber) '.jpg'];
                    if is_first_half
                        pos_first_half(pos_first_half_count,:)= [round(frameNumber), y1(count)-pad, x1(count)-pad, (y2(count)-y1(count)), x2(count)-x1(count)];
                        pos_first_half_count = pos_first_half_count +1;
                    else
                        pos_second_half(pos_second_half_count,:)= [round(frameNumber), y1(count)-pad, x1(count)-pad, (y2(count)-y1(count)), x2(count)-x1(count)];;
                        pos_second_half_count = pos_second_half_count +1;
                    end
                    
                    
                    do_tracking = 1;
                    verified_flag = 1;
                    %myframe = readFrame(myvideo);
                end
            end
            
            if verified_flag == 0
                do_tracking = 0 ;
                if (myvideo.currenttime+ tp_not_verified_face/myvideo.framerate < myvideo.duration)
                    myvideo.currenttime = myvideo.currenttime+ tp_not_verified_face/myvideo.framerate;
                else
                    myvideo.currenttime = 1/myvideo.framerate;
                    is_first_half = 1;
                    disp('go to the first part');
                end
                
                
            end
        else
            do_tracking = 0 ;
            if (myvideo.currenttime+tp_not_face/myvideo.framerate < myvideo.duration)
                myvideo.currenttime = myvideo.currenttime+tp_not_face/myvideo.framerate;
            else
                myvideo.currenttime = 1/myvideo.framerate ;
                is_first_half = 1;
                disp('go to the first part');
            end
           
            
        end
    end
    if is_first_half 
        if myvideo.currenttime >= start_time
            while_flag = 0;
        end
    end
   
end
end


%% %@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    function FeaCNN=feature_extraction(Image)
        % pre-processing
        %disp('extracting features of the selected face...');
        Image = single(Image);
        Image = bsxfun(@minus,Image,vgg_net.meta.normalization.averageImage);
        
        
        % run the CNN
        res = vl_simplenn(vgg_net, Image);
        FeaCNN = squeeze(gather(res(33).x)) ; %36 is ReLu of last FC layer
        %disp('feature extraction completed ');
        
    end
%% %@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    function [boxes ] = my_run_tracker(  start_box , first_time_track  )
        
        show_visualization = false ; % no need
        show_plots = false ; % no need
        %last_frame = 0 ;
        %base_path = 'C:/Users/Elaheh/Downloads/rpt-master/rpt-master/data';% You might need to change the path
        %video = 'Soccer';
        
        %addpath('mykcf');
        %default settings
        %if nargin < 1, video = 'choose'; end
        %if nargin < 2, kernel_type = 'linear'; end
        %if nargin < 3, feature_type = 'hog'; end
        %if nargin < 4, show_visualization = ~strcmp(video, 'all'); end
        %if nargin < 5, show_plots = ~strcmp(video, 'all'); end
        kernel_type = 'linear';
        feature_type = 'hog';
        
        
        %parameters according to the paper. at this point we can override
        %parameters based on the chosen kernel or feature type
        kernel.type = kernel_type;
        %show_visualization=false;
        features.gray = false;
        features.hog = false;
        
        
        padding = 3;  %extra area surrounding the target
        lambda = 1e-4;  %regularization
        output_sigma_factor = 0.1;  %spatial bandwidth (proportional to target)
        
        switch feature_type
            
            case 'hog',
                interp_factor = 0.02;
                
                kernel.sigma = 0.5;
                
                kernel.poly_a = 1;
                kernel.poly_b = 9;
                
                features.hog = true;
                features.hog_orientations = 9;
                cell_size = 4;
                
            case 'gray',
                interp_factor = 0.075;  %linear interpolation factor for adaptation
                
                kernel.sigma = 0.2;  %gaussian kernel bandwidth
                
                kernel.poly_a = 1;  %polynomial kernel additive term
                kernel.poly_b = 7;  %polynomial kernel exponent
                
                features.gray = true;
                cell_size = 1;
                
                
            otherwise
                error('Unknown feature.')
        end
        
        
        assert(any(strcmp(kernel_type, {'linear', 'polynomial', 'gaussian'})), 'Unknown kernel.')
        
        
        
        %we were given the name of a single video to process.
        
        %get image file names, initial state, and ground truth for evaluation
        %[img_files, pos, target_sz, ground_truth, video_path] = load_video_info(base_path, video);
        target_sz = [start_box(1,4), start_box(1,3)];
        pos = [start_box(1,2), start_box(1,1)] + floor(target_sz/2);
        %we have ground truth for the first frame only (initial position)
        ground_truth = []; 		%store positions instead of boxes
        
        
        
        %call tracker function with all the relevant parameters
        [boxes, time ] = my_tracker( pos, target_sz, ...
            padding, kernel, lambda, output_sigma_factor, interp_factor, ...
            cell_size, features, show_visualization, first_time_track);
        
        
        %calculate and show precision plot, as well as frames-per-second
        %giving error
        %precisions = precision_plot(positions, ground_truth, video, show_plots);
        %fps = numel(img_files) / time;
        
        %fprintf('%12s - FPS:% 4.2f\n', video, fps)
        
        %if nargout > 0,
        %return precisions at a 20 pixels threshold
        %precision = precisions(20);
        %end
    end
%% @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    function [boxes , time ] = my_tracker( pos, target_sz, ...
            padding, kernel, lambda, output_sigma_factor, interp_factor, cell_size, ...
            features, show_visualization , first_time_track)
        randn('seed',0);rand('seed',0);
        % the static parameters for all parts
        
        %last_frame= 0;
        param={};
        param.padding=padding;
        param.kernel=kernel;
        param.lambda = lambda;
        param.output_sigma_factor=output_sigma_factor;
        param.interp_factor=interp_factor;
        param.cell_size=cell_size;
        param.features=features;
        param.PSRange = 0.6/(1+padding);
        param.deleteThresholdP =5;
        param.deleteThresholdN =15;
        %param.numParticles=50;
        param.numParticles=15;%Ella code
        param.yellowArea = 1.8;
        param.blueArea = 9;
        %     temp = load('w2crs');
        %     param.w2c = temp.w2crs;
        init=1;
        continueScale =0;
        
        %if the target is large, lower the resolution, we don't need that much
        %detail
        resize_image = (sqrt(prod(target_sz)) >= 100);  %diagonal size >= threshold
        if resize_image,
            pos = floor(pos / 2);
            target_sz = floor(target_sz / 2);
        end
        
        
%         if show_visualization,  %create video interface
%             update_visualization = show_video(img_files, resize_image);
%         end
        
        
        %note: variables ending with 'f' are in the Fourier domain.
        
        time = 0;  %to calculate FPS
        positions = zeros(numel(img_files), 2);  %to calculate precision
        targetSize = zeros(numel(img_files), 2);  %to calculate precision
        for frame = 1:numel(img_files),
            %load image
            %im = imread([video_path img_files{frame}]);
            %im = imread([img_files{frame}]); %Ella code
            im = [img_files{frame}]; %Ella code
            if size(im,3) > 1,
                im = rgb2gray(im);
            end
            if resize_image,
                im = imresize(im, 0.5);
            end
            %tic()
            
            %initialize parts
            if frame==init
                if frame==1
                    pars = repmat([pos target_sz*0.6]',[1 param.numParticles]);
                    contexts=addParts(im,pars,pos,target_sz,param);
                end
                %             ddd =figure;
                points=[];
                n=numel(contexts);
                for i=1:n
                    if contexts{i}.target
                        p = contexts{i}.pos;
                        points = [points; p];
                        contexts{i}.displace = pos -p;
                    end
                end
                original = target_sz;
                param.his=[];
                % size of original image to be tracked : pos + target_sz
                %xorg = pos(2) - floor (target_sz(1)/2) ;
                %yorg = pos(1) - floor(target_sz(2)/2 );
                %worg = target_sz(2);
                %horg = target_sz(1);
                
                %disp('$$$$$$$$\n');
                %figure; imshow (im(yorg:(yorg+worg),xorg:(xorg+horg),:));
                area = prod(std(points));
                scale = prod(target_sz)/area;
                pnum = size(points,1);
            end % if frame == init
            
            
            %track the parts
            for i=1:numel(contexts)
                contexts{i} = kcftracker(im,contexts{i},param);
            end
            
            iiid=1:100;
            ixx=1:100;
            
            
            
            if frame~=1
                %savepos = pos ;
                save_xorg = pos(2) - floor (target_sz(1)/2) ;
                save_yorg = pos(1) - floor(target_sz(2)/2 );
                [pos,target_sz] = voting(pos,target_sz,contexts,1);
                [contexts,changed]=resetParts(im,pos,target_sz,contexts,param);
                [pos,~] = voting(pos,target_sz,contexts,changed);
                xorg = round (pos(2) - floor (target_sz(1)/2)) ;
                yorg = round(pos(1) - floor(target_sz(2)/2 ));
                        
                if (~first_time_track)
                    % only in first time tracking we can not do verification
                    %distance = sqrt ((pos(1)-savepos(1))^2+ (pos(2)-savepos(2))^2 ) ;
                    
                     distance = sqrt ((xorg-save_xorg)^2+ (yorg-save_yorg)^2 ) ;
                    
                    if (distance>6)
                        xorg = round(pos(2) - floor (target_sz(1)/2)) ;
                        yorg = round(pos(1) - floor(target_sz(2)/2 ));
                        worg = round(target_sz(2));
                        horg = round(target_sz(1));
                        img=im(yorg:(yorg+worg),xorg:(xorg+horg),:);
                        img= imresize(img, [width,lenght]);
                        FeaCNN_verify_img = feature_extraction(img);
                        Score_cos_verify = (FeaCNN_query'*FeaCNN_verify_img)/(norm(FeaCNN_query)*norm(FeaCNN_verify_img));
                        if Score_cos_verify < similarity_threshold
                            % not verified
                            last_frame = frame ;
                            %disp (last_frame);
                            return ; %stop tracking
                        end
                    end
                end
                
                
                
                %
                % size of original image to be tracked : pos + target_sz
                
                %             xorg = pos(2) - floor (target_sz(1)/2) ;
                %             yorg = pos(1) - floor(target_sz(2)/2 );
                %             worg = target_sz(2);
                %             horg = target_sz(1);
                %display('$$$$$$$$\n');
                %figure; imshow (im(yorg:(yorg+worg),xorg:(xorg+horg),:));
                
                %%%voting%%%%%%%%%%%%%%%%%
                %              [pos,target_sz] = voting(pos,target_sz,contexts,scale);
                n=numel(contexts);
                %             pos=[0,0];
                nn=0;
                points=[];
                
                target_sz = original*changed;
                %              param.his = [param.his;t];
                %             if size(param.his,1) >4
                %                 target_sz = [0.05 0.05 0.1 0.2 0.6 ] *  param.his(end-4:end,:);
                %             else
                %                 target_sz=t;
                %             end
                if changed >1.2 || changed<0.8
                    continueScale = continueScale +1;
                else
                    continueScale =0;
                end
                
                if continueScale > 5 % it should be change to 3 in future maybe
                    init=frame+1; % here the scale of image is getting bigger
                    continueScale =0;
                    %disp('############');
                end
                
                
                %%for draw
                
                n=numel(contexts);
                
                psr=[];
                
                
                for i=1:n
                    if isfield(contexts{i},'psr') && contexts{i}.psr >0
                        prob = contexts{i}.psr * size(contexts{i}.traj,1);
                        if isfield(contexts{i},'motionP')
                            prob = prob* contexts{i}.motionP;
                        end
                        psr=[psr prob];
                        iiid=[iiid i];
                    end
                end
                
                [~,ixx] = sort(psr,'descend');
                csize = 20;
                if csize > size(psr,2)
                    csize = size(psr,2);
                end
                ixx = ixx(1:csize);
                
                
                
                %%%voting%%%%%%%%%%%%%%%%%
            end
            
            positions(frame,:) = pos;
            targetSize(frame,:) = target_sz;
            %time = time + toc();
            a = target_sz./2 ;
            %boxes (frame,:) = floor ( [pos(2)+a(2) , pos(1)-a(1),  target_sz(2) , target_sz(1)] );
            boxes (frame,:) = floor ([pos(2)-a(1) , pos(1)-a(2),  target_sz(2) , target_sz(1)] .* 2 );
            
            %boxes (frame,:) = [floor (pos(2)+a(2)) , floor (pos(1)-a(1)),  floor (target_sz(2)) , floor (target_sz(1))];
            
%             
%             %visualization
%             if (show_visualization)
%                 csize = 20;
%                 if csize > size(ixx,2)
%                     csize = size(ixx,2);
%                 end
%                 if csize > param.numParticles
%                     csize = param.numParticles;
%                 end
%                 
%                 box = zeros(csize,5);
%                 for i=1:csize
%                     box(i,:) = [contexts{iiid(ixx(i))}.pos([2,1]) - contexts{iiid(ixx(i))}.target_sz([2,1])/2, ...
%                         contexts{iiid(ixx(i))}.target_sz([2,1]) contexts{iiid(ixx(i))}.target];
%                 end
%                 t=[pos([2 1]) - target_sz([2,1])/2, target_sz([2 1]), 3];
%                 box = [t;box];
%                 blue=param.blueArea;
%                 area= prod(target_sz);
%                 areaS = [sqrt(area), sqrt(area)];
%                 t=[pos([2 1]) - blue*areaS([2,1])/2, blue*areaS([2 1]), 4];
%                 box = [t;box];
%                 yellew=param.yellowArea;
%                 t=[pos([2 1]) - yellew*target_sz([2,1])/2, yellew*target_sz([2 1]), 5];
%                 box = [t;box];
%                 stop = update_visualization(frame, box);
%                 if stop, break, end  %user pressed Esc, stop early
%                 
%                 drawnow
%                 % 			pause(0.05)  %uncomment to run slower
%             end
            
            
        end
        if resize_image,
            positions = positions * 2;
            targetSize =targetSize * 2;
        end
        %         time= time / frame;
    end

    function blank= fillblank(blank,center,p, pos, sz)
        lu = floor(pos - p - floor(sz/2) + center);
        lu(lu<1)=1;
        
        blank(lu(1):lu(1)+sz(1),lu(2):lu(2)+sz(2) ) =1;
        
    end

%% @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
%     function ID = biaryClassification(labels,Score_cos)
%         [ID,id,a]= svmpredict(double(labels'),Score_cos, SVM_model);
%     end
%fclose(posFile);
positionFile = ['VideoPos/' vname ext '.mat'];
positionMat = [pos_first_half; pos_second_half];
save(positionFile,'positionMat');
disp('done!!!');

end
