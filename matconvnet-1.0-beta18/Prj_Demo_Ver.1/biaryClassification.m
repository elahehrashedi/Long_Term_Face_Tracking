function ID = biaryClassification(labels,Score_cos)
      
        L= double(labels);
        load('trainedSVMonLFW_vggfc7_unaligned.mat');
       ID= svmpredict(L,Score_cos, SVM_model);
    end
