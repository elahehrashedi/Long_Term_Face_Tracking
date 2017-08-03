 clear all;
 close all;

convnetpath = '.\.\matconvnet-1.0-beta18';
addpath(genpath(convnetpath));
videoName = '24.avi';
videoPath ='.\24.avi';
baseDir  = '.\Data'; %save result images
root = 'Data/24.avi/';
[pathstr,name,ext] = fileparts(videoName); 
 mkdir( [baseDir '\imgVerified\'],name);
% 
imgVerified = strcat(baseDir, '\imgVerified\',name ,'\');




%% load mat files 
vl_setupnn;

%% load Cascade CNN detector '
global net12;
global net12_c;
global net24;
global net24_c;
global net48;
global net48_c;
net12 = load('12net-newborn/f12net.mat') ;
net12_c = load('12net-cc-v1/f12net_c.mat') ;
net24 = load('24net-newborn/f24net-cpu.mat') ;
net24_c = load('24net-cc-v1-no256/f24netc.mat') ;
net48 = load('48net-6hard/f48net-cpu.mat') ;
net48_c = load('48net-cc-cifar-v2-submean/f48netc.mat') ;
% global SVM_model ;
 %SVM_model = load('trainedSVMonLFW.mat');

%%
%vl_setupnn;

%% 
fig = figure('Visible','off','Position',[300,300,700,500]);
movegui(fig,'center');
%set(gcf, 'closerequestfcn', ''); % close bottom will  work now

[  fig, start_time , tracking_box  ] = run_gui(fig , videoName,imgVerified);   
%fprintf (file_videopositions,'%d %d %d %d %d %d\n', filenum, initstate(1),initstate(2),initstate(3),initstate(4), frame_num);

%set(gcf, 'closerequestfcn', 'closereq'); % close bottom will  work now
close (fig);
%% Play video until it paused 
 tic
tracking_frames = 50; % it was 30 originally
similarity_threshold = 0.75;
tp_tracking = 3;
tp_not_verified_face = 20;%
tp_not_face = 20;%
% imgVerified = '.\Data\imgVerified\282\';
% convnetpath = 'D:\Wayne Courses\Winter 2016\Project\FaceRetrieval\matconvnet-1.0-beta18\matconvnet-1.0-beta18';
% faceretrievalpath = 'D:\Wayne Courses\Winter 2016\Project\FaceRetrieval';
% svmpath = 'D:\Wayne Courses\Winter 2016\Project\FaceRetrieval\libsvm-master';
% prjpath = 'D:\Wayne Courses\Winter 2016\Project\FaceRetrieval\matconvnet-1.0-beta18\matconvnet-1.0-beta18\Prj_Demo_Ver.1';
%[ output_args ] = VideoTrackingNew( videoName , start_time , tracking_box );
path = 'C:\ver1.9' ;
 result = VTWVerification( videoName, start_time, tracking_box(1), tracking_box(2), tracking_box(3), tracking_box(4),...
                         tracking_frames, similarity_threshold ,tp_tracking,...
                         tp_not_verified_face, tp_not_face , path);
result
%VideoTracking( videoName, start_time, tracking_box(1), tracking_box(2), tracking_box(3), tracking_box(4),...
%                        tracking_frames, similarity_threshold ,tp_tracking,...
%                        tp_not_verified_face, tp_not_face,convnetpath,...
%                        faceretrievalpath,svmpath,prjpath);
%[ output_args ] = VTWSavingPos( videoName , start_time , tracking_box );
toc


% we need to optimize the code when calculating backtime
% we need to make the code clean
