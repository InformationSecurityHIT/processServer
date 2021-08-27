import numpy as np
import PIL.Image
import cv2
from skimage import feature as ft
import time

from PreProcessing_FeatureExtraction.connect_center import connect_centres
from PreProcessing_FeatureExtraction.detect_vein_center_assign_score import compute_vein_score
from PreProcessing_FeatureExtraction.label import binaries
from PreProcessing_FeatureExtraction.normalize import normalize_data
from PreProcessing_FeatureExtraction.preprocessing import remove_hair
from PreProcessing_FeatureExtraction.profile_curvature import compute_curvature

"""
	In this method, the local maximum curvature is calculated in the cross-sectional 
	profile of all four directions, then selecting the profile that has the maximum 
	depth in the cross-sectional profile. And then to get the full pattern of nerves 
	we add, The result of four directions.

	Miura et al. Proposed a three-step algorithm to solve the above problem.

	Step in Algorithms:
	
	Extraction of the center positions of veins.
	Connection of the center positions.
	Labeling of the image.
"""


# @wrl
# 尺寸归一化
def size_normal(img):
    """
    :param img:输入的一张RGB图
    :return: 尺寸归一化后的RGB图
    """
    # img = PIL.Image.fromarray(img).convert('L')
    # img = np.asarray(img)
    return img.resize((300, 300))


# 计算质心
def cal_center(img1):
    img_origin = img1.view()
    # img_origin = img1
    img1 = np.where(img1 < 100, 0, img1)

    YCrCb = cv2.cvtColor(img1, cv2.COLOR_BGR2YCR_CB)  # 转换至YCrCb空间
    (y, cr, cb) = cv2.split(YCrCb)  # 拆分出Y,Cr,Cb值
    cr1 = cv2.GaussianBlur(cr, (5, 5), 0)
    _, skin = cv2.threshold(cr1, 0, 255, cv2.THRESH_BINARY)  # Ostu处理
    img1 = cv2.bitwise_and(img1, img1, mask=skin)
    img1 = cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY)
    dst = cv2.Laplacian(img1, cv2.CV_16S, ksize=3)
    img1 = cv2.convertScaleAbs(dst)
    h = cv2.findContours(img1, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)  # 寻找轮廓
    contour = h[0]
    contour = sorted(contour, key=cv2.contourArea, reverse=True)  # 已轮廓区域面积进行排序

    temp = np.asarray(img_origin)
    for i in range(contour[0].shape[0]):
        index_i = contour[0][i][0][0]
        index_j = contour[0][i][0][1]
        if (index_i < temp.shape[0] and index_j < temp.shape[1]):
            # print(index_i,index_j)
            temp[index_j][index_i][0] = 255
            temp[index_j][index_i][1] = 255
            temp[index_j][index_i][2] = 255
    # im = PIL.Image.fromarray(temp)
    # im.show()

    # contour = sorted(contour, key=cv2.contourArea, reverse=True)  # 已轮廓区域面积进行排序

    x_center = 0
    y_center = 0
    n = len(contour[0])
    min = contour[0][0][0][1]
    for i in range(n):
        x_center += contour[0][i][0][0]
        x = contour[0][i][0][1]
        y_center += x
        if min > x:
            min = x
    S = cv2.contourArea(contour[0])
    n0 = np.sqrt(S) / 2
    x_center = x_center / n
    y_center = min + n0
    return x_center, y_center, n0, temp


# 截取手背区域
def cut_area(img, x, y, n0):
    # 图像尺寸
    n = len(img)
    m = len(img[0])
    # 边长/2
    # n0 = np.sqrt(S) / 2
    left = x - n0  # 这个地方的赋值方式可以优化
    up = y - n0
    right = x + n0
    down = y + n0
    if left < 0:
        left = 0
    if up < 0:
        up = 0
    if right > m:
        right = m
    if down > n:
        down = n
    if (right - left) > (down - up):
        n0 = (down - up) / 2
        x = (right + left) / 2
        left = x - n0
        right = x + n0
    else:
        n0 = (right - left) / 2
        y = (up + down) / 2
        up = y - n0
        down = y + n0
    img = PIL.Image.fromarray(img).crop((left, up, right, down))
    return img


# @wrl


def vein_pattern(image, kernel_size, sigma):
    data = np.asarray(image, dtype=float)
    filter_data = remove_hair(data, kernel_size)
    preprocessed_data = normalize_data(filter_data, 0, 255)
    kappa = compute_curvature(preprocessed_data, sigma=sigma)
    score = compute_vein_score(kappa)
    conect_score = connect_centres(score)
    threshold = binaries(np.amax(conect_score, axis=2))
    vein_pattern = np.multiply(image, threshold, dtype=float)
    return vein_pattern


# @wrl
def GlobalThreshold(img, T=50):
    """
    :param img:img是个灰度图矩阵
    :return:全局阈值化后的矩阵
    """
    temp = np.where(img < T, 1, 0)
    # im = Image.fromarray(temp*255).convert('L') #这个地方改了
    # im.save(destRoot+"\\global_threshold_"+str(name)+".bmp")
    # im.show()
    return temp


def pretreatment(image_path, name, dest):
    image = cv2.imread(image_path)
    x, y, n0, image = cal_center(image)  # 求质心，求面积
    image = cut_area(image, x, y, n0)  # 裁剪，返回Image
    image = size_normal(image)  # 尺寸归一化 返回Image
    image = cv2.cvtColor(np.array(image), cv2.COLOR_BGR2GRAY)
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    image = clahe.apply(image)

    temp = GlobalThreshold(image, 70)

    processed_image = vein_pattern(image, 6, 8)

    for i in range(processed_image.shape[0]):
        for j in range(processed_image.shape[1]):
            if (np.abs(processed_image[i][j]) < 1e-5):
                processed_image[i][j] = 255
            else:
                processed_image[i][j] = 0

    img_final = (temp + processed_image).astype(bool)

    im = PIL.Image.fromarray(img_final).convert('L')
    im.save(dest + "\\" + name + ".bmp")

    # if(dest is None):
    #     im = PIL.Image.fromarray(img_final).convert('L')
    #     im.save(dest+"\\"+name+".bmp")
    return img_final


# 提取图片特征
def HOG_feature_extraction_from_image(image):
    img = cv2.imread(image)
    # img = PIL.Image.fromarray(img)
    features = ft.hog(img,  # input image
                      orientations=9,  # number of bins
                      pixels_per_cell=(16, 16),  # pixel per cell
                      cells_per_block=(5, 5),  # cells per blcok
                      block_norm='L1',  # block norm : str {‘L1’, ‘L1-sqrt’, ‘L2’, ‘L2-Hys’}
                      transform_sqrt=True,  # power law compression (also known as gamma correction)
                      feature_vector=True,  # flatten the final vectors
                      visualize=False
                      )  # return HOG map
    return features


#
def HOG_feature_extraction_from_numpy(image):
    features = ft.hog(image,  # input image
                      orientations=9,  # number of bins
                      pixels_per_cell=(16, 16),  # pixel per cell
                      cells_per_block=(5, 5),  # cells per blcok
                      block_norm='L1',  # block norm : str {‘L1’, ‘L1-sqrt’, ‘L2’, ‘L2-Hys’}
                      transform_sqrt=True,  # power law compression (also known as gamma correction)
                      feature_vector=True,  # flatten the final vectors
                      visualize=False
                      )  # return HOG map
    return features


# test

# pretreatment("..\\ImageRoot\\1_left_0.bmp","1_left_0","..\\temp")
"""
import cv2
import matplotlib.pyplot as plt


start = time.time()
image_path = '../ImageRoot/1L1.jpg'
#image = cv2.imread(image_path, 0)
image = cv2.imread(image_path)
x, y, n0 = cal_center(image)  #求质心，求面积
image = cut_area(image, x, y, n0)   #裁剪，返回Image
image = size_normal(image)  # 尺寸归一化 返回Image
image = cv2.cvtColor(np.array(image), cv2.COLOR_BGR2GRAY)
clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
image = clahe.apply(image)

processed_image = vein_pattern(image, 6, 8)

for i in range(processed_image.shape[0]):
    for j in range(processed_image.shape[1]):
        if(np.abs(processed_image[i][j])<1e-5):
            processed_image[i][j] = 255
        else:
            processed_image[i][j] = 0

#PIL.Image.fromarray(processed_image).convert('L').show()
end = time.time()
print(end-start)

plt.subplot(1,2,1)
plt.imshow(image, cmap='gray')
plt.title('Original Image')

plt.subplot(1,2,2)
plt.imshow(processed_image, cmap='gray')
plt.title('Processed Image')

plt.suptitle("Vein Pattern")
plt.tight_layout()
plt.savefig("../processed_img/7.jpg")
plt.show()
"""
