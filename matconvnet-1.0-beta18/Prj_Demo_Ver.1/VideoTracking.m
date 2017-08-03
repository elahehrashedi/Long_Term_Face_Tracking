function VideoTracking( videoName, start_time, px1, py1, px2, py2,...
                        tracking_frames, similarity_threshold ,tp_tracking,...
                        tp_not_verified_face, tp_not_face,convnetpath,...
                        faceretrievalpath,svmpath,prjpath)
         
   
    display (sprintf('matlab code: px1 = %d , py1 = %d px2 = %d py2 = %d\n', px1 , py1 , px2 , py2));
                 
    %tracking_frames = 15;
    %similarity_threshold = 0.7;
    %tp_tracking = 5;
    %tp_not_verified_face = 5;
    %tp_not_face = 10;
    %imgVerified = '.\Data\imgVerified\162\';
    % convnetpath = 'C:\Elaheh\05_Winter2016\FaceRetrieval\matconvnet-1.0-beta18\matconvnet-1.0-beta18';
    % faceretrievalpath = 'C:\Elaheh\05_Winter2016\FaceRetrieval';
    % svmpath = 'C:\Elaheh\05_Winter2016\FaceRetrieval\libsvm-master';
    baseDir  = '.\Data';
    
    if ~isdeployed
        addpath(genpath(faceretrievalpath));
    end
    if ~isdeployed
        addpath(genpath(svmpath));
    end
    if ~isdeployed
        addpath(genpath(convnetpath));
    end

    [pathstr,name,ext] = fileparts(videoName); 
    mkdir( [baseDir '\imgVerified\'],name);
    
    imgVerified = strcat(baseDir, '\imgVerified\',name ,'\');
    display (sprintf('\n\n\n\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\nImage tracking started'));
    
%% load mat files 
    vl_setupnn;

%% load Cascade CNN detector '
    global net12;
    global net12_c;
    global net24;
    global net24_c;
    global net48;
    global net48_c;
    nettwelve = [prjpath '\12net-newborn\f12net.mat'];
    nettwelve_c = [prjpath '\12net-cc-v1\f12net_c.mat'];
    nettwentyfour = [ prjpath '\24net-newborn\f24net-cpu.mat'];
    nettwentyfour_c = [ prjpath '\24net-cc-v1-no256\f24netc.mat'];
    netfortyeight = [prjpath '\48net-6hard\f48net-cpu.mat'];
    netfortyeight_c = [prjpath '\48net-cc-cifar-v2-submean\f48netc.mat'];

    net12 = load(nettwelve) ;
    net12_c = load(nettwelve_c) ;
    net24 = load(nettwentyfour) ;
    net24_c = load(nettwentyfour_c) ;
    net48 = load(netfortyeight) ;
    net48_c = load(netfortyeight_c') ;

%%  
    tracking_box = [px1, py1, px2, py2];
    [pathstr,vname,ext] = fileparts(videoName); 
    posFile = fopen(['.\VideoPos\' vname '.txt'],'w');
    
    vggpath = [prjpath '\vgg_face_cut_fc7.mat'];
    global vgg_net;
    vgg_net=load(vggpath);
    %load('trainedSVMonLFW.mat');
    global SVM_model;
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
    frame_number = round ( myvideo.currenttime * myvideo.framerate) ;
    frame_size = size(readFrame(myvideo));
    video_duration = myvideo.duration ;
    frame_rate = myvideo.framerate ;
    numofframes = video_duration * frame_rate ;
    
    
    x1 = tracking_box (1);y1 = tracking_box (2);x2 = tracking_box (3);y2 = tracking_box (4);
    display (sprintf('matlab code: x1 = %d , y1 = %d x2 = %d y2 = %d\n', x1 , y1 , x2 , y2));
    % get the selected image do detection on it 
    Image = myframe(x1:x2,y1:y2,:);
    dbox =detection(Image);
    
    % postion of the detected faces and frame number
    positionMat = [0,0,0,0,0];
    % save the detected face 
    dx1 = dbox(:,1); dy1 = dbox(:,2); dx2 = dbox(:,3); dy2 = dbox(:,4);
    
    display (sprintf('matlab code: dx1 = %d , dy1 = %d dx2 = %d dy2 = %d\n', x1+dx1 , x1+dx1+dy2-dy1 , y1+dy1 , y1+dy1+dx2-dx1));
    Image = myframe(x1+dx1:x1+dx1+dy2-dy1 ,y1+dy1:y1+dy1+dx2-dx1 ,:);
    frameNumber = myvideo.currenttime * myvideo.FrameRate;
    imgVeri  = [imgVerified num2str(frameNumber) '.jpg'];
    imwrite(Image,imgVeri);
    display (sprintf('matlab code: position write in files x1 = %d , y1 = %d x2 = %d dy2 = %d\n', dx1 , dy1 , dx2-dx1 , dy2-dy1));
    fprintf(posFile, '%d %d %d %d %d\n' , frameNumber, dy1, dx1, dx2-dx1, dy2-dy1);
    posCount = 1;
    positionMat(posCount,:)= [ frameNumber, dy1, dx1, dy2, dx2];
    posCount = posCount +1;


    % set the image as the image query
    %Image_query = imresize(Image, [width,lenght]);

    % extracting feature of the image query
    %FeaCNN_query = feature_extraction (Image_query);


    do_tracking = 1;
    pad = 10;
    tracking_position = [y1+dy1, x1+dx1,dy2-dy1, dx2-dx1]; 
    
    stop_flag = 0 ; 
    first_run = 1;

    % frame_box = frame_number, position
    frame_box_counter = 1 ;
    while hasFrame(myvideo) && stop_flag == 0

        if (do_tracking == 1 )
            %% do tracking

            % New Tracking
            %[do_tracking, tracking_position,img, tracked_Images] = tracking ( myvideo,tracking_position, myvideo.currenttime,fgr,imgVerified);
            
            for i=1:1:tracking_frames
                if (hasFrame(myvideo)) 
                    img_files{i} = readFrame(myvideo);
                    frame_box {frame_box_counter} = {frame_number,[0,0,0,0]}; 
                    frame_box_counter = frame_box_counter + 1 ;
                    frame_number = frame_number + 1 ;
                    %readFrame(myvideo);
                end
            end
            disp('tracking started...');
            t_boxes  = my_run_tracker (  img_files , tracking_position ) ; % rpt folder             
            %just for show 
            if (first_run ==1) %feature extraction
                for k=1:1:5
                    x=t_boxes(k,2); y = t_boxes(k,1); h = t_boxes(k,3) ; w = t_boxes(k,4) ;
                    tracked_Images (:,:,:,k) = imresize(img_files{k}(x:x+w,y:y+h,:), [width,lenght]);
                end
                
                Image_query = tracked_Images; % 5 images
                FeaCNN_query = feature_extraction(Image_query); 
                FeaCNN_query = mean(FeaCNN_query,2);
                first_run = 0;
             end 
            
            xverify =t_boxes(tracking_frames,2); yverify = t_boxes(tracking_frames,1);
            hverify = t_boxes(tracking_frames,3) ; wverify = t_boxes(tracking_frames,4) ;
            verifyimg (:,:,:,1) = imresize(img_files{tracking_frames}(xverify:xverify+wverify,yverify:yverify+hverify,:), [width,lenght]);
            FeaCNN_verify = feature_extraction(verifyimg); 
            Score_cos_verify = (FeaCNN_query'*FeaCNN_verify)/(norm(FeaCNN_query)*norm(FeaCNN_verify));
            if Score_cos_verify > similarity_threshold
                isverified = 1 ;
            else
                isverified = 0;
            end
            k=tracking_frames;    
            %for k=tracking_frames:-1:1
            kk = 1;
            while (kk)
                        x=t_boxes(k,2); y = t_boxes(k,1); h = t_boxes(k,3) ; w = t_boxes(k,4) ;
                        
                        if (k > 4  && ~isverified )
                            k = k-5 ;
                            xverify =t_boxes(k,2); yverify = t_boxes(k,1);
                            hverify = t_boxes(k,3) ; wverify = t_boxes(k,4) ;
                            verifyimg (:,:,:,1) = imresize(img_files{k}(xverify:xverify+wverify,yverify:yverify+hverify,:), [width,lenght]);
                            FeaCNN_verify = feature_extraction(verifyimg); 
                            Score_cos_verify = (FeaCNN_query'*FeaCNN_verify)/(norm(FeaCNN_query)*norm(FeaCNN_verify));
                            if Score_cos_verify > similarity_threshold
                                    isverified = 1 ;
                            else
                                    isverified = 0;
                            end
                            %isverified = 
                        else
                        
                            %imshow(img_files{k}(x:x+w,y:y+h,:));
                          for i =1:k
                                imgVeri  = [imgVerified num2str(frameNumber) '_' num2str(i) '.jpg'];

                                fprintf(posFile, '%d %d %d %d %d\n' , frameNumber+i, y, x, h, w);

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
                                imwrite(img_files{i}(tracked_x:tracked_x2,tracked_y:tracked_y2,:),imgVeri);
                                positionMat(posCount,:)= [frameNumber+i, tracked_y, tracked_x, tracked_y2, tracked_x2];
                                posCount = posCount +1;
                            end 
                            kk = 0;
                        end
            end
            disp('tracking completed...');
            for i=size(t_boxes,1):-1:1
                frame_box{1,i}{1,2} = t_boxes (i,:);    
            end
            tracking_position = t_boxes (end,:) ; % change tracking position
            
             

            %myframe = readFrame(myvideo);
            %frame_number = frame_number + 1 ;
            %myframe = img;
            myframe = img_files{end} ;
            frameNumber = myvideo.currenttime * myvideo.FrameRate;

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
                
                if (myvideo.currenttime+tp_tracking < myvideo.duration)
                myvideo.currenttime = myvideo.currenttime+tp_tracking;  
                else 
                stop_flag =1 ;
                end
                
                do_tracking = 0;
            end
            
            disp(myvideo.currenttime);
            
            

        else % if (do_tracking == 0 )
            %% read frame and do detection
            
            myframe = readFrame(myvideo);
            frameNumber = myvideo.currenttime * myvideo.FrameRate;
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
                temp = myframe(tempx1:tempx2,tempy1:tempy2,:);
                Images(:,:,:,xx) = imresize(temp, [width,lenght]);
            end
            
           %% feature extraction for each detected face 
            FeaCNN = feature_extraction(Images);
            Score_cos = zeros(boxes_size(1),1);
            Final_id = [];
            for i = 1:boxes_size(1)
                disp('cosin computation..');
                    y = FeaCNN(:,i);
                    Score_cos(i) = (FeaCNN_query'*y)/(norm(FeaCNN_query)*norm(y));
                    if Score_cos(i) > similarity_threshold
                        Final_id(i) = 1;
                    else 
                        Final_id(i) = -1;
                    end
            end

            labels = ones(boxes_size(1),1);

            % svm model 
            %Final_id = biaryClassification(labels,Score_cos);

            
            [v,I] = find(Final_id==1);
            verified_flag = 0 ;
            for count = 1:size(v)
                if (I == 1)
                    disp('verified...');
                        face=imcrop(myframe,[y1(count)-pad,x1(count)-pad,(y2(count)-y1(count)),x2(count)-x1(count)]);
                        tracking_position = [y1(count)-pad,x1(count)-pad,(y2(count)-y1(count)),x2(count)-x1(count)];
                    

                    imgVeri  = [imgVerified num2str(frameNumber) '.jpg'];
                    imwrite(face,imgVeri);
                    fprintf(posFile,'%d %d %d %d %d\n' , frameNumber, y1(count)-pad, x1(count)-pad, (y2(count)-y1(count)), x2(count)-x1(count));
                    positionMat(posCount,:)= [frameNumber, x1(count)-pad, y1(count)-pad, x2(count),y2(count)];
                    posCount = posCount + 1;
                    do_tracking = 1;
                    verified_flag = 1;
                    %myframe = readFrame(myvideo);
                end
            end 
            
            if verified_flag == 0 
                do_tracking = 0 ;
                if (myvideo.currenttime+ tp_not_verified_face < myvideo.duration)
                    myvideo.currenttime = myvideo.currenttime+ tp_not_verified_face;
                else 
                    stop_flag = 1;
                end 

                disp(myvideo.currenttime);
            end 
        else 
            do_tracking = 0 ;
            if (myvideo.currenttime+tp_not_face < myvideo.duration)
                myvideo.currenttime = myvideo.currenttime+tp_not_face;  
            else 
                stop_flag =1 ;
            end
            disp(myvideo.currenttime);

        end 
        end 
    imshow(uint8(myframe));    
    end
        

%% %@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    function FeaCNN=feature_extraction(Image)
    % pre-processing 
    disp('extracting features of the selected face...');
    Image = single(Image);
    Image = bsxfun(@minus,Image,vgg_net.meta.normalization.averageImage);


    % run the CNN  
    res = vl_simplenn(vgg_net, Image);
    FeaCNN = squeeze(gather(res(33).x)) ; %36 is ReLu of last FC layer
    disp('feature extraction completed ');
    
    end

%     function ID = biaryClassification(labels,Score_cos)
%         [ID,id,a]= svmpredict(double(labels'),Score_cos, SVM_model);
%     end
fclose(posFile);
save('frame_pos.mat','positionMat');
disp('done!!!');

end
