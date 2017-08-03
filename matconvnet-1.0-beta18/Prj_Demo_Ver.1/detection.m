function boxes = detection(img)
    %disp('face detection');
    [~,~,c]=size(img);
    if c==1
        img=repmat(img,[1,1,3]);
    end
    boxes = [];
    pad = 10;
    [hh,ww,~]=size(img);
    I = uint8(zeros(hh+pad*2,ww+pad*2,3));
    I (pad:hh+pad-1,pad:ww+pad-1,:) = img;
    boxes_temp = scanpic_fast_only12_24_48_newmodel_submean_demo(I,1:1);   
    boxes = [boxes;boxes_temp];
end 