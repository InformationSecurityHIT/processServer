import sys
import os
import cv2
import numpy as np
from PreProcessing_FeatureExtraction import extract_feature

#利用orb获取特征点
def orb(img,orb_init,keyPoint):
    img = cv2.imread(img, 0)
    _, des = orb_init.detectAndCompute(img, None)
    # des1 = np.loadtxt("dest1.txt",dtype='uint8',delimiter=',')
    np.savetxt(keyPoint,des,fmt='%d',delimiter=',')

if __name__ == "__main__":

    imagePath = sys.argv[1]  # 获得注册人图片所在文件夹的绝对路径的绝对路径
    destRoot = sys.argv[2]  # 处理后图片所在的文件夹
    keyPoint = sys.argv[3]
    # imagePath ="D:\\work\\Java\\code\\wkr\\data_before\\wrll"
    # destRoot =  "D:\\work\\Java\\code\\wkr\\data_after\\wrll"
    # keyPoint = "D:\\work\\Java\\code\\wkr\\KeyPoint\\wrll"

    if not os.path.exists(destRoot):
        os.mkdir(destRoot)
    if not os.path.exists(keyPoint):
        os.mkdir(keyPoint)

    # images = os.listdir(destRoot)
    images = os.listdir(imagePath)
    orb_init = cv2.ORB_create(nfeatures=2000,scaleFactor=1.05,nlevels=4,edgeThreshold=31,
                              firstLevel=0,WTA_K=2,patchSize=31,fastThreshold=20)
    for image in images:
        name = image.split('.')[0]
        extract_feature.pretreatment(imagePath + "\\" + image, name, destRoot)
        orb(destRoot+"\\"+image,orb_init=orb_init,keyPoint=keyPoint+"\\"+name+".txt")





# i = 0
    # while(i<=11):
    #     print(str(i))
    # imagePath = "D:\work\Java\code\wkr\\data_before\\200"
    # destRoot = "D:\work\Java\code\wkr\\data_after\\200"
    # print("kkk")
    # imagePath ="D:\InformationSecurityCompetition\wkr\\data_before\\match"
    # destRoot =  "D:\InformationSecurityCompetition\wkr\\data_before\\match_after"
    # images = os.listdir(imagePath)
    # for image in images:
    #     name = image.split('\\')[-1].split('.')[0]
    #     processed_image = extract_feature.pretreatment(imagePath + "\\" + image, name, destRoot)
    #     # i+=1




    # imagePath = "D:\work\Python\code\SVM\DataBase\\2"
    # destRoot = "D:\work\Python\code\SVM\\train\\2"
    # images = os.listdir(imagePath)
    # i = 0
    # df = pd.DataFrame()
    # for image in images:
    #     print(image)
    #     name = image.split('\\')[-1].split('.')[0]
    #     processed_image = extract_feature.pretreatment(imagePath + "\\" + image, name, destRoot)
    #     # features = extract_feature.HOG_feature_extraction_from_numpy(processed_image)
    #     # df[i] = features
    #     # i = i + 1
    # # feature_data_path = "D:\\Lab\\xinan_new\\DZH\\features_data\\" + name.split("_")[0] + ".csv"
    # # df.to_csv(feature_data_path, header=False, index=False)
    # print("image process over.")
