import json

import tensorflow as tf
import cv2 as cv
from timeit import default_timer as timer


class DetectionItem:
    def __init__(self, class_id, score, top_x, top_y, bottom_x, bottom_y):
        super().__init__()
        self.class_id = class_id
        self.score = score
        self.top_left_x = top_x
        self.top_left_y = top_y
        self.bottom_right_x = bottom_x
        self.bottom_right_y = bottom_y

    def to_json(self):
        return json.dumps(self, default=lambda o: o.__dict__, sort_keys=True, indent=4)

    def __repr__(self):
        return self.to_json()


class Detector:
    def __init__(self, debug_mode=False):
        self.debug_mode = debug_mode
        model_file_path = "C:\Git\ImageRecognition\Server\Trained model\model.pb"
        with tf.compat.v1.gfile.FastGFile(model_file_path, 'rb') as f:
            self.graph_def = tf.compat.v1.GraphDef()
            self.graph_def.ParseFromString(f.read())

    def recognize_image(self, image_path):
        with tf.compat.v1.Session() as sess:
            sess.graph.as_default()
            tf.import_graph_def(self.graph_def, name='')

            start_time = timer()
            img = cv.imread(image_path)
            rows = img.shape[0]
            cols = img.shape[1]
            inp = cv.resize(img, (300, 300))
            inp = inp[:, :, [2, 1, 0]]  # BGR2RGB

            out = sess.run([sess.graph.get_tensor_by_name('num_detections:0'),
                            sess.graph.get_tensor_by_name('detection_scores:0'),
                            sess.graph.get_tensor_by_name('detection_boxes:0'),
                            sess.graph.get_tensor_by_name('detection_classes:0')],
                           feed_dict={'image_tensor:0': inp.reshape(1, inp.shape[0], inp.shape[1], 3)})

            detection_result = []
            num_detections = int(out[0][0])
            for i in range(num_detections):
                classId = int(out[3][0][i])
                score = float(out[1][0][i])
                bbox = [float(v) for v in out[2][0][i]]
                if score >= 0.5:
                    x = int(bbox[1] * cols)
                    y = int(bbox[0] * rows)
                    right = int(bbox[3] * cols)
                    bottom = int(bbox[2] * rows)

                    item = DetectionItem(classId, score, x, y, right, bottom)
                    print(classId)
                    detection_result.append(item)
                    cv.rectangle(img, (int(x), int(y)), (int(right), int(bottom)), (125, 255, 51), thickness=2)

            if self.debug_mode:
                cv.namedWindow("Products Detection", cv.WINDOW_NORMAL);
                cv.imshow('Products Detection', img)
                cv.waitKey()
            else:
                end_time = timer()
                print("Time: ", (end_time - start_time))
                return detection_result
