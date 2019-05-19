from keras.engine.saving import model_from_json
from keras.preprocessing import image
from keras.applications.vgg16 import preprocess_input, decode_predictions
import numpy as np


class Detector:
    def __init__(self, image_path):
        self.image_path = image_path
        json_file = open("imagenet_model.json", "r")
        loaded_model_json = json_file.read()
        json_file.close()
        model = model_from_json(loaded_model_json)
        model.load_weights("imagenet_model.h5")
        self.model = model
        pass

    def Recognize(self):
        img = image.load_img(self.image_path, target_size=(224, 224))
        x = image.img_to_array(img)
        x = np.expand_dims(x, axis=0)
        x = preprocess_input(x)
        preds = self.model.predict(x)
        return decode_predictions(preds, top=3)[0][0][1]