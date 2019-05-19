from keras.applications.vgg16 import VGG16

model = VGG16(weights='imagenet')

print("Saving model...")
model_json = model.to_json()
json_file = open("imagenet_model.json", "w")
json_file.write(model_json)
json_file.close()
model.save_weights("imagenet_model.h5")
print("Saving completed")