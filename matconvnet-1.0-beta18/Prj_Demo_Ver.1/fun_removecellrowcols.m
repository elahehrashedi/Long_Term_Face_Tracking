function celda = fun_removecellrowcols(celda,rowcols,varargin)
% fun_removecellrowcols(cell,rowcols,str_rowcols)
% AUTHOR: José Crespo Barrios
% ---------------
% EXPLANATION: removes the non desired rows/columns from a cell.
% For example, if we have a [3x3] cell, and introduce:
%
% cellA = fun_removecellrowcols(cellA,2,'rows')
% the output will be a [2x3] cell, in which the middle row has been removed
% If introduced instead:
%
% cellA = fun_removecellrowcols(cellA,[1,2],'cols')
% then the dimensions of cellA will be [3x1] in which 1st and 2nd columns
% have been removed.
% ---------------
% BREAKDOWN:
% º celda:      the cell we desire to process ("celda" is cell in spanish)
% º rowcols:    vector of rows/cols to be removed from the cell
% º str_rowcols(optional): 
%               'rows' or 'cols'. by default is set to 'rows'

clear
clc
ind_ejemplo = 0; % illustrates an usage example when true (1)

%% módulo ejemplo de inputs
if ind_ejemplo
    clear
    clc
    ind_ejemplo = 1;
    
    % inputs de ejemplo
    celda       = {'mario',182; 'luigi',172; 'toad',175};
    rowcols     = [2, 3];
    varargin{1} = 'rows'; nargin = 3;
end
%% módulo función

% por defecto, se entenderá que trata con filas
if nargin < 3
    str_filcol = 'rows';
else
    str_filcol = varargin{1};
end

% recoloca en vector columna rowcols
if size(rowcols,2) > 1
    rowcols = rowcols';
end

% en caso de eliminar filas...
if strcmp(str_filcol,'rows')
    ncols = size(celda,2);
    for auxfil = 1: length(rowcols)
        fil          = rowcols(auxfil,1);
        celda(fil,:) = cell(1, ncols);
    end
    celda = reshape( celda(~cellfun('isempty',celda)), [], ncols);

% en caso de eliminar columnas...
else
    nfils = size(celda,1);
    for auxfil = 1: length(rowcols)
        col          = rowcols(auxfil,1);
        celda(:,col) = cell(nfils, 1);
    end
    celda = reshape( celda(~cellfun('isempty',celda)), nfils, []);
end
