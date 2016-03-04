function vector_to_roi(selection, mij_instance)
%% vector_to_roi (c) Eric Barnhill 2015. All rights reserved. MIT License, see README
% This script runs in conjunction with MIJ / Miji
% it converts a vector of indices from matlab to an imagej selection.
% The call is void. Example usage: vector_to_roi(selection, MIJ);
% Required: vector of indices and MIJ instance 
% Because the indices are a point cloud, this will not result in an elegant
% polygon selection. Future work will incorporate a point-cloud-to-polygon
% routine. You may find that ImageJ's "enlarge" selection tool will convert
% the cloud into a polygorn (enlage by 1, then by -1) or if it is convex,
% use "Convex Hull"

selec = '';
for n = 1:numel(selection)
    selec = sprintf('%s %s',selec,num2str(selection(n)));
    if mod(n,10) == 0
		selec = sprintf('%s\n',selec);
	end
end
clipboard('copy', selec);
mij_instance.run('Indices_To_Selection');