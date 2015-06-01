function [resh] =openImage(image, mij_instance)
a = pwd;
sz = size(image);
thirdDim = 1;
for n = 1: (numel(sz)-2);
    thirdDim = thirdDim * sz(n+2);
end
resh = reshape(image, sz(1), sz(2), thirdDim);
mij_instance.createImage(resh);