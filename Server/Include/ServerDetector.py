import tensorflow as tf
import os
from PIL import Image
import numpy as np
import cv2


def convert_to_opencv(image):
    # RGB -> BGR conversion is performed as well.
    image = image.convert('RGB')
    r,g,b = np.array(image).T
    opencv_image = np.array([b,g,r]).transpose()
    return opencv_image


if __name__ == "__main__":
    imageFile = "D:\Git\ImageRecognition\Server\Images\Received.jpg"

    graph_def = tf.compat.v1.GraphDef()
    labels = []

    # These are set to the default names from exported models, update as needed.
    filename = 'C:\Git\ImageRecognition\Server\Trained model\frozen_inference_graph.pb'
    labels_filename = 'C:\Git\ImageRecognition\Server\Trained model\label_map.pbtxt'

    # Import the TF graph
    with tf.io.gfile.GFile(filename, 'rb') as f:
        graph_def.ParseFromString(f.read())
        tf.import_graph_def(graph_def, name='')

    # Create a list of labels.
    with open(labels_filename, 'rt') as lf:
        for l in lf:
            labels.append(l.strip())

    image = Image.open(imageFile)

    # Convert to OpenCV format
    image = convert_to_opencv(image)

    output_layer = 'loss:0'
    input_node = 'Placeholder:0'

    with tf.compat.v1.Session() as sess:
        try:
            prob_tensor = sess.graph.get_tensor_by_name(output_layer)
            predictions, = sess.run(prob_tensor, {input_node: [image]})
        except KeyError:
            print("Couldn't find classification output layer: " + output_layer + ".")
            print("Verify this a model exported from an Object Detection project.")
            exit(-1)