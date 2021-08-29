import numpy as np
import os
import cv2
import heapq
import threading
import time
import multiprocessing
from multiprocessing import Pool
from multiprocessing import Process
import sys
from PreProcessing_FeatureExtraction import extract_feature

def search(image,orb_init,data_dir):
    image = cv2.imread(image,0)
    _, des = orb_init.detectAndCompute(image, None)
    result=[]
    dirs = os.listdir(data_dir)
    for dir in dirs:
        result_tmp=[]
        path = data_dir+"\\"+dir
        features = os.listdir(path)
        for feature in features:
            good_matches_sum,key1,key2 = orb1(des,path+"\\"+feature)
            good_matches_sum = good_matches_sum if abs(key1-key2)<70 else 0
            result_tmp.append(good_matches_sum)
        result.append(result_tmp)
    return result
def orb1(des,feature_path):
    des2 = np.loadtxt(feature_path,dtype='uint8',delimiter=',')
    bf = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=True)
    matches = bf.match(des, des2)
    good_match = sorted(matches, key=lambda x: x.distance)
    return len(good_match),des.shape[0],des2.shape[0]

def task(img_before,destRoot,data_dir):
    orb_init = cv2.ORB_create(nfeatures=2000, scaleFactor=1.05, nlevels=4, edgeThreshold=31,
                              firstLevel=0, WTA_K=2, patchSize=31, fastThreshold=20)
    dirs = os.listdir(data_dir)
    name = img_before.split('\\')[-1].split('.')[0]  # 这个地方根据文件名命名后续文件可能需要改
    extract_feature.pretreatment(img_before, name, destRoot)
    res = search(destRoot + "\\" + name + ".bmp", orb_init=orb_init, data_dir=data_dir)
    res = dirs[calculate_max4(res)]
    return res
def calculate_max4(result):
    res=[]
    sum_res=[]
    for result_tmp in result:
        res.append(heapq.nlargest(5, result_tmp))
    for result_tmp in res:
        sum_res.append(sum(result_tmp))
    return sum_res.index(max(sum_res))

class MyThread(threading.Thread):
    def __init__(self,img_before,destRoot,data_dir,orb_init):
        super().__init__()
        self.img_before = img_before
        self.destRoot = destRoot
        self.data_dir = data_dir
        self.orb_init = orb_init
        self.res = ""
        self.flag = False
        self.dirs = os.listdir(self.data_dir)
    def run(self):
        name = self.img_before.split('\\')[-1].split('.')[0]  # 这个地方根据文件名命名后续文件可能需要改
        extract_feature.pretreatment(self.img_before, name, self.destRoot)
        res = search(self.destRoot + "\\" + name+".bmp", orb_init=self.orb_init, data_dir=self.data_dir)
        self.res = self.dirs[calculate_max4(res)]
        self.flag = True




if __name__ == "__main__":
    # imagePath = sys.argv[1]  # 获得待处理图片文件夹的绝对路径  #D:\InformationSecurityCompetition\wkr\\data_before\\match"
    # # destRoot = sys.argv[2]  # 处理后图片所在的文件夹          #D:\InformationSecurityCompetition\wkr\\data_before\\match_after"
    # data_dir = "D:\\work\\Java\\code\\wkr\\data_after"
    # imagePath ="D:\\work\\Java\\code\\wkr\\data_before\\new_match"
    # destRoot =  "D:\\work\\Java\\code\\wkr\\data_before\\match_after"
    # images = os.listdir(imagePath)
    # destRootimages = os.listdir(destRoot)
    # # for image in images:
    # #     name = image.split(".")[0]  # 这个地方根据文件名命名后续文件可能需要改
    # #     extract_feature.pretreatment(imagePath + "\\" + image, name, destRoot)
    # dirs = os.listdir(data_dir)
    # list_res = []
    # for image in destRootimages:
    #     res = search(destRoot+"\\"+image,data_dir=data_dir)
    #     list_res.append(dirs[calculate_max4(res)])
    # maxlabel = max(list_res, key=list_res.count)
    # print(maxlabel)
    # print(list_res)

    #多进程优化

    imagePath = sys.argv[1]  # 获得待处理图片文件夹的绝对路径
    destRoot = sys.argv[2]  # 处理后图片所在的文件夹

    # imagePath = "D:\\work\\Java\\code\\wkr\\data_before\\new_match"
    # destRoot = "D:\\work\\Java\\code\\wkr\\data_before\\match_after"
    data_dir = "D:\\InformationSecurityCompetition\\gameUse\\processServer\\device\\device0\\keyPoint"
    # if not os.path.exists(destRoot):
    #     os.mkdir(destRoot)
    images = os.listdir(imagePath)
    dirs = os.listdir(data_dir)
    results = []

    multiprocessing.freeze_support()
    pool = Pool(processes=len(images))
    for image in images:
        results.append(pool.apply_async(task,args=(imagePath+"\\"+image,destRoot,data_dir)))
    pool.close()
    pool.join()
    list_result = []
    for re in results:
        list_result.append(re.get())
    # print(list_result)


    # imagePath = "D:\\work\\Java\\code\\wkr\\data_before\\new_match"
    # destRoot = "D:\\work\\Java\\code\\wkr\\data_before\\match_after"
    # data_dir = "D:\work\Java\code\wkr\\keyPoint"
    # # if not os.path.exists(destRoot):
    # #     os.mkdir(destRoot)
    # images = os.listdir(imagePath)
    # dirs = os.listdir(data_dir)
    # orb = cv2.ORB_create(nfeatures=2000,scaleFactor=1.05,nlevels=4,edgeThreshold=31,
    #                      firstLevel=0,WTA_K=2,patchSize=31,fastThreshold=20)
    # list_res = []
    # thread = []
    #
    # s = time.time()
    # for image in images:
    #     t = MyThread(imagePath+"\\"+image,destRoot,data_dir,orb)
    #     thread.append(t)
    #     t.start()
    # for t in thread:
    #     t.join()
    # print(time.time()-s)
    # for t in thread:
    #     list_res.append(t.res)
    # print(list_res)