# -*- coding: utf-8 -*-
"""
Created on Mon Mar 21 12:02:25 2022

@author: Sneha Sree
"""

import cv2 as cv
import numpy as np
import math

src_img = cv.imread('image.png',cv.IMREAD_GRAYSCALE)
#cv.imshow('Original Image',src_img)

dst_img = cv.Laplacian(src_img, cv.CV_8U , 3)

linesP = cv.HoughLinesP(dst_img, 1, np.pi /4, 50, None, 40,2 )
out_img = np.dstack([dst_img]*3)
for i in range(0, len(linesP)):
            lin = linesP[i][0]
            cv.line(out_img, (lin[0], lin[1]), (lin[2], lin[3]), (0,255,0), 1, cv.LINE_AA)

cv.imshow("Image with lines", out_img)
cv.waitKey(0)