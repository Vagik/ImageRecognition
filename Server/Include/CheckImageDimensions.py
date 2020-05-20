import os
import glob
import cv2

if __name__ == "__main__":
    dir = 'C:/Git/ImageRecognition/Server/Images/resized'
    ext = 'jpg'

    fnames = glob.glob(os.path.join(dir, "*.{}".format(ext)))

    for index, fname in enumerate(fnames):
        img = cv2.imread(fname)
        height, width, channels = img.shape
        if width == 800 and height == 600:
            print(fname + ' ++++++++\n', end="", flush=True)
        else:
            print(fname + ' --------\n', end="", flush=True)
