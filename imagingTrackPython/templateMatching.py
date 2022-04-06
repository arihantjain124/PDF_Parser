# -*- coding: utf-8 -*-
"""
Created on Thu Mar 24 18:21:35 2022

@author: Sneha Sree
"""


    
import cv2
import numpy as np
from matplotlib import pyplot as plt

img_rgb = cv2.imread('image.png')
img_gray = cv2.cvtColor(img_rgb, cv2.COLOR_BGR2GRAY)
template1 = cv2.imread('template1.png',0)
template2=cv2.imread('template2.png',0)
template3=cv2.imread('template3.png',0)
h1, w1 = template1.shape[::]
h2, w2 = template2.shape[::]
h3, w3 = template3.shape[::]

res1 = cv2.matchTemplate(img_gray, template1, cv2.TM_CCOEFF_NORMED)
res2= cv2.matchTemplate(img_gray, template2, cv2.TM_CCOEFF_NORMED)
res3= cv2.matchTemplate(img_gray, template3, cv2.TM_CCOEFF_NORMED)

threshold1 = 0.95 #Pick only values above 0.8 since for TM_CCOEFF_NORMED, larger values = good fit.

loc1 = np.where( res1 >= threshold1)  
#Outputs 2 arrays. Combine these arrays to get x,y coordinates - take x from one array and y from the other.

#ZIP function is an iterator of tuples where first item in each iterator is paired together,
#then the second item and then third, etc. 

for pt in zip(*loc1[::-1]):   #-1 to swap the values as we assign x and y coordinate to draw the rectangle. 
    #Draw rectangle around each object. We know the top left (pt), draw rectangle to match the size of the template image.
    cv2.rectangle(img_rgb, pt, (pt[0]+w1 , pt[1]+h1), (0, 0,255), 1)  #red rectangles with thickness 1. 
 
threshold2 = 0.95
loc2 = np.where( res2 >= threshold2)  

for pt in zip(*loc2[::-1]):   
    cv2.rectangle(img_rgb, pt, (pt[0] + w2, pt[1] + h2), (0, 0, 255), 1)  
    
threshold3 = 1.0
loc3 = np.where( res3 >= threshold3)  

for pt in zip(*loc3[::-1]):   
    cv2.rectangle(img_rgb, pt, (pt[0] + w3, pt[1] + h3), (0, 0, 255), 1)  

#cv2.imwrite('images/template_matched.jpg', img_rgb)
cv2.imshow("Matched image", img_rgb)
cv2.waitKey()