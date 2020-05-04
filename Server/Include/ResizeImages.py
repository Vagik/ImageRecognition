import os
import glob
import cv2

if __name__ == "__main__":

    raw_dir = 'C:/Git/ImageRecognition/Server/Images/train'
    save_dir = 'C:/Git/ImageRecognition/Server/Images/resized'
    ext = 'jpg'
    target_size = (800, 600)

    fnames = glob.glob(os.path.join(raw_dir, "*.{}".format(ext)))
    os.makedirs(save_dir, exist_ok=True)

    for index, fname in enumerate(fnames):
        print(".", end="", flush=True)
        img = cv2.imread(fname)
        img_small = cv2.resize(img, target_size)
        new_fname = "{}.{}".format(str(index), ext)
        small_fname = os.path.join(save_dir, new_fname)
        cv2.imwrite(small_fname, img_small)
