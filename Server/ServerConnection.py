import json
import socket

from ServerDetector import Detector

BUFFER_SIZE = 4096
received_image_path = "C:\Git\ImageRecognition\Server\Images\Received.jpg"

sock = socket.socket()
sock.bind(('', 8000))
sock.listen(1)

detector = Detector()
while True:
    print('Connecting...')
    conn, addr = sock.accept()
    print('Connected:', addr)

    with open(received_image_path, "wb") as img:
        print("Receiving...")
        while True:
            try:
                data = conn.recv(BUFFER_SIZE)
                if not data or ("finished" in str(data)):
                    break
                img.write(data)
            except:
                print("Failed")
    img.close()
    print("Received")

    detection_result = detector.recognize_image(image_path=received_image_path)
    result_json = json.dumps([ob.__dict__ for ob in detection_result])
    print('Detected: ', detection_result)
    conn.send(result_json.encode("utf-8"))
    break
sock.close()

