import os

if __name__ == "__main__":
    path = 'C:/Git/ImageRecognition/Server/Images/train'
    files = os.listdir(path)
    for index, file in enumerate(files):
        os.rename(os.path.join(path, file), os.path.join(path, ''.join([str(index), '.jpg'])))