import socket
from Include.Detect import Detector


BUFFER_SIZE = 4096
received_image_path = "D:\Git\ImageRecognition\Server\Images\Received.jpg"

sock = socket.socket()
sock.bind(('', 8000))
sock.listen(1)

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

    detector = Detector(received_image_path)
    detected = detector.Recognize()
    print('Detected: ', detected)
    conn.send(detected.encode("utf-8"))
    break
sock.close()

