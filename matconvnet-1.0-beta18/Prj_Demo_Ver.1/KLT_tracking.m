function flag = KLT_tracking(myvideo , initstate , myvideocurrenttime, fgr,imgVerified)
 disp('tracking started...');
bbox = initstate;
myvideo.currenttime = myvideocurrenttime;
videoFrame = readFrame(myvideo);

bboxPoints = bbox2points(bbox(1, :));
points = detectMinEigenFeatures(rgb2gray(videoFrame), 'ROI', bbox);
pointTracker = vision.PointTracker('MaxBidirectionalError', 2);
points = points.Location;
initialize(pointTracker, points, videoFrame);

oldPoints = points;
videoPlayer  = vision.VideoPlayer('Position',...
    [100 100 [size(videoFrame, 2), size(videoFrame, 1)]+30]);
while hasFrame(myvideo && myvideo.currenttime < (myvideocurrenttime + 1))
    
    % get the next frame
    videoFrame = readFrame(myvideo);
    frameNumber = myvideo.currenttime * myvideo.FrameRate;
    % Track the points. Note that some points may be lost.
    [points, isFound] = step(pointTracker, videoFrame);
    visiblePoints = points(isFound, :);
    oldInliers = oldPoints(isFound, :);
    
    if size(visiblePoints, 1) >= 2 % need at least 2 points
        
        % Estimate the geometric transformation between the old points
        % and the new points and eliminate outliers
        [xform, oldInliers, visiblePoints] = estimateGeometricTransform(...
            oldInliers, visiblePoints, 'similarity', 'MaxDistance', 4);
        
        % Apply the transformation to the bounding box points
        bboxPoints = transformPointsForward(xform, bboxPoints);
                
        % Insert a bounding box around the object being tracked
        bboxPolygon = reshape(bboxPoints', 1, []);
        %face=imcrop(videoFrame,bboxPolygon);
        %imgVeri  = [imgVerified num2str(frameNumber) '.jpg'];
        %imwrite(face,imgVeri);
        videoFrame = insertShape(videoFrame, 'Polygon', bboxPolygon, ...
            'LineWidth', 2);
                
        % Display tracked points
        videoFrame = insertMarker(videoFrame, visiblePoints, '+', ...
            'Color', 'white');       
        
        % Reset the points
        oldPoints = visiblePoints;
        setPoints(pointTracker, oldPoints);        
    end
    
    % Display the annotated video frame using the video player object
    step(videoPlayer, videoFrame);
end
flag = 0; 
 disp('tracking completed..');
end 