import json

from ServerDetector import Detector

if __name__ == "__main__":
    detector = Detector(True)
    for i in range (1, 31):
        image_path = "C:\Git\ImageRecognition\Server\Images\check\/" + str(i) + ".jpg"
        detection_result = detector.recognize_image(image_path)
