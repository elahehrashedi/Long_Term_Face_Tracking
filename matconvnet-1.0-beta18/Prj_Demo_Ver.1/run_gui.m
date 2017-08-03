function [ fig , start_time , tracking_box ] = run_gui (fig , videoName,imgVerified)

    %@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    video_available = 1 ; % when there is  more video available to show
    stop = 0 ; % the user request to stop the job
    %global vgg_net;
    %vgg_net=load('vgg_face_cut.mat');
    %load('trainedSVMonLFW.mat');
    %global SVM_model;
    %SVM_model = load('trainedSVMonLFW.mat');
    tracking_box = [0 , 0, 0,0]; %output
    start_time = 0 ;%output
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
    
    %@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    %create the fig to show video frames on it

    fig.Units = 'pixels';
    fig.Name = 'Video';
    %movegui(fig,'center');
    fig.Visible = 'on';
    %set(gcf, 'closerequestfcn', ''); % close bottom will not work
    set(gca,'visible','on');
    axee = gca ; % handle to the axes of current fig

    pos=get(axee,'position');
    slider_pose=[pos(1) pos(2)-0.05 pos(3) 0.02];
    
    figpos = get(fig,'Position') ;
    x= figpos(3)/2-85;
    pause_pose = [x,0.1,70,30];%[275,0.1,70,30];
    save_pose = [x+100,0.1,70,30];%[375,0.1,70,30];
    
    %% assign a function to the button pause
    pause_button    = uicontrol(fig, 'Style','pushbutton',...
             'String','Pause','Position', pause_pose,...
             'Callback',@pausebutton_Callback);

    %% assign a function to the button pause
    save_button    = uicontrol(fig, 'Style','pushbutton',...
             'String','Capture','Position', save_pose,...
             'Callback',@savebutton_Callback, 'enable','off');
         
    %% Creating Uicontrol
     slider_bar = uicontrol(fig,'style','slider',...
             'units','normalized','position',slider_pose,...
             'Callback',@slider_Callback,'min',0,'max',100);

    %% resize the button
    pause_button.Units = 'normalized';
    save_button.Units = 'normalized';
    align([pause_button,save_button],'distribute','bottom');
    %align([pause_button],'Center','None');
    
    video_duration = myvideo.duration ;
    frame_rate = myvideo.framerate ;
    numofframes = video_duration * frame_rate ;
    %start showing frame
    frame_num = 0 ;
    %capture_flag = 0 ; %1 capture or 0 not capture
    initstate = [0,0,10,10];
    pausestate = 1 ; % if we are in pause or play mode
    stepsize = 100/numofframes ;
    
  
    finish_flag = 0 ;% a flag to finish the program
    while(hasFrame(myvideo) && finish_flag==0 )
        
        myframe = readFrame(myvideo);
%         fs = size(myframe);
%         if min(fs(1), fs(2))> 300
%                 myframe = imresize(myframe, [floor(fs(1)*0.5),floor(fs(2)*0.5)]);
%         end
        
        imshow (myframe,'Parent',axee); %Handle of an axes that specifies the parent of the image object created by imshow.
        
        hold on;
            pause(0.0001);
        hold off;
        %while (pausestate == 0 || ~capture_flag ) % if we select the area we wait to play again or to capture
        while (pausestate == 0)
            hold on;
            pause(0.0001);
            hold off;
        end
        
        frame_num = frame_num + 1 ;
        set (slider_bar , 'Val' , get (slider_bar, 'Val')+stepsize) ;
    end 

    while (finish_flag == 0 )
        hold on;
        pause(0.0001);
        hold off;        
    end
   
    clf (fig);
    
    
%% %@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    function pausebutton_Callback(source,eventdata) % play and pause button 
        if (pausestate==1) % go to play state with capture enable
            set(save_button,'enable','off');%disable capture button
            set(pause_button,'enable','off');%disable play pause button
            %set(pause_button,'SelectionHighlight','off');
            rectangle = imrect(gca);
            initstate = round(getPosition(rectangle));
            %rectangle.addNewPositionCallback(@(pos)rectangle_Callback(pos));
            %rectangle.addNewPositionCallback(@(initstate));
            %rectangle_Callback(pos);
            set(pause_button,'string','Play');%change the text to play                     
            set(save_button,'enable','on');%enable capture button
            pausestate = 0 ;
            set(pause_button,'enable','on');%enable play pause button
        else % go back to pause state with capture disable       
            set(pause_button,'string','Pause');%change the text to play
            set(pause_button,'enable','on');%enable capture button
            set(save_button,'enable','off');%disable capture button
            pausestate = 1 ;
        end
       
    end    

%% %@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
%savebutton_Callback(source,eventdata,SVM_model)

    function savebutton_Callback(source,eventdata) % capture or not
        
        set(save_button,'enable','off');%disable capture button
        fgr = figure ;
        %fgr.Visible = 'on';
   
        %set(gca,'visible','on');
        %newaxe = gca ; % handle to
        
        %  get the position of the rectangle
        y1 = initstate(1); x1 = initstate(2); y2 = y1+initstate(3); x2 = x1+initstate(4);
 
        %cut
        tracking_box = [x1,y1,x2,y2] ; %output
        start_time = myvideo.currenttime ;
        finish_flag = 1 ;
        
        
        set(pause_button,'string','Pause');%change the text to play
        pausestate = 1 ;
        set(pause_button,'enable','on');%enable pause button

    end 

%% %@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    function slider_Callback (source,eventdata)
        hold on;
        pause(0.0001);
        hold off;
        newtime = get (slider_bar, 'Val');
        myvideo.currenttime = newtime * video_duration / 100 ;
        frame_num = round ( myvideo.currenttime * myvideo.framerate) ;
        hold on;
        pause(0.0001);
        hold off;
    end

end