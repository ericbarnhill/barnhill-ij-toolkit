function [v] = roi_to_vector(mij_instance)
%% roi_to_vector (c) Eric Barnhill 2015. All rights reserved. MIT License, see README.
% This script runs in conjunction with MIJ / Miji 
% It will convert an ImageJ ROI to a vector of selection indices
% Example usage: [v] = roi_to_vector(MIJ);
% nb: Before running script, make sure the desired image and ROI in ImageJ are
% selected+++

mij_instance.run('Selection_To_Indices');
v = str2num(clipboard('paste'));
