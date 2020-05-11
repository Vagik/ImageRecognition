import os
import glob
import cv2

if __name__ == "__main__":

    raw_dir = 'C:/Git/ImageRecognition/Server/Images/to_resize'
    save_dir = 'C:/Git/ImageRecognition/Server/Images/resized'
    ext = 'jpg'
    target_width = 800
    start_name_number = 236

    fnames = glob.glob(os.path.join(raw_dir, "*.{}".format(ext)))
    os.makedirs(save_dir, exist_ok=True)

    for index, fname in enumerate(fnames):
        print(fname + '\n', end="", flush=True)
        img = cv2.imread(fname)
        height, width, channels = img.shape
        ratio = float(width) / float(target_width)
        target_size = (target_width, int(float(height) / ratio))
        img_small = cv2.resize(img, target_size)
        new_fname = "{}.{}".format(str(index + start_name_number), ext)
        small_fname = os.path.join(save_dir, new_fname)
        cv2.imwrite(small_fname, img_small)
