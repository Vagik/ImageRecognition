from ServerDetector import Detector

if __name__ == "__main__":
    detector = Detector(True)
    image_path = "C:\Git\ImageRecognition\Server\Images\Received.jpg"
    detector.recognize_image(image_path)

