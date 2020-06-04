import json
import socket

from ServerDetector import Detector

class ServerConnection:
    def __init__(self):
        self.BUFFER_SIZE = 4096
        self.received_image_path = "C:\Git\ImageRecognition\Server\Images\Received.jpg"
        self.sock = socket.socket()
        self.sock.bind(('', 8000))
        self.sock.listen(1)

    def connect_for_one_detection(self):
        while True:
            detector = Detector()
            print('Connecting...')
            conn, addr = self.sock.accept()
            print('Connected:', addr)

            with open(self.received_image_path, "wb") as img:
                print("Receiving...")
                while True:
                    try:
                        data = conn.recv(self.BUFFER_SIZE)
                        if not data or ("finished" in str(data)):
                            break
                        img.write(data)
                    except:
                        print("Failed")
            img.close()
            print("Received")

            detection_result = detector.recognize_image(image_path=self.received_image_path)
            result_json = json.dumps([ob.__dict__ for ob in detection_result])
            conn.send(result_json.encode("utf-8"))
            break
        self.sock.close()


if __name__ == "__main__":
    while True:
        connection = ServerConnection()
        connection.connect_for_one_detection()