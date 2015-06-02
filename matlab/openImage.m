function [resh] = openImage(name, image, mij_instance)
%% openImage (c) Eric Barnhill 2015. All rights reserved. MIT License, see README.
% This script will automatically open a Matlab object of any dimension >= 2
% in the present instance of MIJ
a = pwd;
sz = size(image);
thirdDim = 1;
for n = 1: (numel(sz)-2);edit roi_to_vector

    thirdDim = thirdDim * sz(n+2);
end
resh = reshape(image, sz(1), sz(2), thirdDim);
mij_instance.createImage(name, resh, 1);
