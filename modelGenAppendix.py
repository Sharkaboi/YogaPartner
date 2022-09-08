import os
from pathlib import Path
import re
import tqdm
import shutil
import time
import math

import matplotlib.pyplot as plt
from matplotlib.collections import LineCollection

from PIL import Image
import cv2

from difPy import dif

import numpy as np
import pandas as pd
import tensorflow as tf
import tensorflow.keras as keras
from keras.utils import np_utils
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense
from tensorflow.keras.layers import Dropout
from tensorflow.nn import relu
from tensorflow.keras.losses import CategoricalCrossentropy
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.optimizers import SGD
from tensorflow.keras.callbacks import EarlyStopping
from tensorflow.keras.callbacks import ModelCheckpoint
from sklearn.model_selection import train_test_split
from sklearn.metrics import *

import mediapipe as mp
mp_pose = mp.solutions.pose
mp_drawing = mp.solutions.drawing_utils 
mp_drawing_styles = mp.solutions.drawing_styles

# Global functions

version = 3
def getPath():
    return r".\data\v" + str(version)

def getClasses():
    path = getPath()
    return os.listdir(path)

def getPathOfClass(className):
    path = getPath()
    return os.path.join(path, className)

intermediates_dir = r".\intermediates\v" + str(version)
classFolders = getClasses()

# Dataset stats

def visualizeDataSet():
    counts = []
    imageTypes = {}
    for classFolder in classFolders:
        images = os.listdir(getPathOfClass(classFolder))
        count = len(images)
        counts.append(count)
        for image in images:
            image_path = os.path.join(getPathOfClass(classFolder), image)
            p = Path(image_path)
            ext = p.suffix
            imageTypes[ext] = imageTypes.get(ext, 0) + 1

    print("Dataset frequency distribution")
    for i in range(len(classFolders)):
        className = classFolders[i]
        count = counts[i]
        print(className + " : " + str(count))

    print()

    print("Image type frequency distribution")
    for key in imageTypes.keys():
        count = imageTypes[key]
        print(key + " : " + str(count))

    print()
    
visualizeDataSet()

# Cleaning Data

# Convert to Jpeg

for className in classFolders:
    class_path = getPathOfClass(className)
    files = os.listdir(class_path)
    for f in files:
        old_path = os.path.join(class_path, f)
        p = Path(old_path)
        ext = p.suffix
        if ext == ".webp":
            print("Converting " + f)
            new_file_name = f"{p.stem}.jepg"
            im = Image.open(old_path).convert("RGB")
            new_file_path = os.path.join(class_path, new_file_name)
            print(new_file_path)
            im.save(new_file_path, "jepg")
            print("Removing " + f)
            os.remove(old_path)
        if ext == ".png":
            print("Converting " + f)
            new_file_name = f"{p.stem}.jpg"
            im = Image.open(old_path).convert("RGB")
            new_file_path = os.path.join(class_path, new_file_name)
            print(new_file_path)
            im.save(new_file_path)
            print("Removing " + f)
            os.remove(old_path)
        if ext == ".JPG":
            print("Converting " + f)
            new_file_name = f"{p.stem}.jpg"
            im = Image.open(old_path).convert("RGB")
            new_file_path = os.path.join(class_path, new_file_name)
            print(new_file_path)
            im.save(new_file_path)
            print("Removing " + f)
            os.remove(old_path)
print("Converted Webp and PNG")

# Finding duplicate images

for className in tqdm.tqdm(classFolders, position=0):
    class_path = getPathOfClass(className)
    search = dif(class_path, delete=True)
print("Completed duplicate image search")

# Cleaning up folder names

for className in classFolders:
    old_path = getPathOfClass(className)
    new_name = re.sub(r"\s+", "_", className)
    if new_name != className:
        print("Renaming " + className)
    new_path = os.path.join(getPath(), new_name)
    os.rename(old_path, new_path)
print("Renamed class names")

# Removing non supported images

for className in classFolders:
    class_path = getPathOfClass(className)
    files = os.listdir(class_path)
    count = 1
    for f in files:
        f_path = os.path.join(class_path, f)
        if not (f.endswith(".jpg") or f.endswith(".jpeg")):
            os.remove(f_path)
            print("Removed " + f)
print("Finished removing non suporrted images")

# Cleaning up file names

for className in classFolders:
    class_path = getPathOfClass(className)
    files = os.listdir(class_path)
    count = 1
    for f in files:
        old_path = os.path.join(class_path, f)
        p = Path(old_path)
        new_file_name = f"{className}_{count}{p.suffix}"
        if new_file_name != f:
            print("Renaming " + f)
        new_file_path = os.path.join(class_path, new_file_name)
        if(os.path.exists(new_file_path)):
            print("Clash in names, change to unique pattern")
            break
        os.rename(old_path, new_file_path)
        count += 1
        
print("Finished cleaning file names")

# Removing truncated images

for className in classFolders:
    class_path = getPathOfClass(className)
    files = os.listdir(class_path)
    for file in files:
        fpath = os.path.join(class_path, file)
        img = Image.open(fpath)
        try:
            img.getpixel((0,0))
            plt.imread(fpath)
        except OSError as e:
            print("Please delete "+ fpath)
            
print("Done")

# Cleaning to (h, w, 3) format

classFolders = getClasses()
for className in classFolders:
    class_path = getPathOfClass(className)
    files = os.listdir(class_path)
    for file in files:
        fpath = os.path.join(class_path, file)
        img = plt.imread(fpath)
        if not (len(img.shape) == 3 and img.shape[2] == 3):
            print(fpath)
            
print("Done")

visualizeDataSet()

# Pre-process

filepaths = []
labels = []

for className in classFolders:
    class_path = getPathOfClass(className)
    files = os.listdir(class_path)
    for f in files:
        fpath = os.path.join(class_path, f)
        filepaths.append(fpath)
        labels.append(className)

Fseries = pd.Series(filepaths, name='filepaths')
Lseries = pd.Series(labels, name='labels')
df = pd.concat([Fseries, Lseries], axis=1)

trim_classes_to_count = 200

df = df.copy()
sample_list=[] 
groups = df.groupby('labels')
for label in df['labels'].unique():        
    group = groups.get_group(label)
    sample_count = len(group)         
    if sample_count > trim_classes_to_count :
        group = group.sample(
            trim_classes_to_count, 
            replace=False, 
            weights=None, 
            random_state=123, 
            axis=0
        ).reset_index(drop=True)
    sample_list.append(group)
df = pd.concat(sample_list, axis=0).reset_index(drop=True)
balance = list(df['labels'].value_counts())
print(balance)

trsplit = 0.8
# 20% - 10% test, 10% validation
tsplit = 0.2
strat=df['labels']    
train_df, test_df = train_test_split(df, train_size=trsplit, shuffle=True, random_state=123, stratify=strat)
print('train_df length: ', len(train_df), '  test_df length: ',len(test_df), ' total length: ', len(filepaths))
print(train_df['labels'].value_counts())

print(train_df)

# Running pre-trained detection model

# Save detected poses of training set for debugging

landmark_count = 33

def saveImagesWithKeypoints(image_path, results):
    image_path = os.path.abspath(image_path)
    try:
        image = cv2.imread(image_path)
        image = image.copy()
        pose_landmarks = results.pose_landmarks
        if pose_landmarks is not None and len(pose_landmarks.landmark) == landmark_count:
            mp_drawing.draw_landmarks(
                image,
                results.pose_landmarks,
                mp_pose.POSE_CONNECTIONS,
                landmark_drawing_spec=mp_drawing_styles.get_default_pose_landmarks_style()
            )
            
        p = Path(image_path)
        fileName = p.name
        fp = os.path.join(intermediates_dir, fileName)
        fp = r""+os.path.abspath(fp)
        if not cv2.imwrite(fp, image):
            raise Exception("image not saved to {}".format(fp))
    except (Exception) as e:
        print("Failed to write image for {}".format(image_path))
        raise e

def process_result(result, image_shape, image_path, class_name):
    pose_landmarks = result.pose_landmarks
    if pose_landmarks is not None and len(pose_landmarks.landmark) == landmark_count:
        pose_landmarks = [[lmk.x, lmk.y, lmk.z] for lmk in pose_landmarks.landmark]
        # Map pose landmarks from [0, 1] range to absolute coordinates to get correct aspect ratio.
        frame_height, frame_width = image_shape
        pose_landmarks *= np.array([frame_width, frame_height, frame_width])
        pose_landmarks = np.around(pose_landmarks, 5).flatten().astype(float).tolist()
    else:
        print("Pose was none or len < {} for {}".format(landmark_count, image_path))
    return (class_name, pose_landmarks)

durations = []
def detect(ndf):
    with mp_pose.Pose(
        static_image_mode=True,
        model_complexity=1
    ) as pose:
        rows = []
        for i, value in tqdm.tqdm(ndf.iterrows(), position=0):
            begin = time.time()
            label = value['labels']
            image_path = value['filepaths']
            image = cv2.imread(image_path)
            results = pose.process(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
            end = time.time()
            rows.append(process_result(results, image.shape[:2], image_path, label))
            saveImagesWithKeypoints(image_path, results)
            if results.pose_landmarks is not None:
                durations.append(end - begin)   
        return rows
    
train_in = detect(train_df)
test_in = detect(test_df)
print("Done detecting")
print("Total time taken - " + str(sum(durations)))
print("Max time taken - " + str(max(durations)))
print("Min time taken - " + str(min(durations)))
print("Average time taken - " + str(sum(durations) / len(durations)))

# Train classifier model

# Classes

labels_list = [
    "adho_mukha_shvanasana",
    "bhujangasana",
    "bidalasana",
    "phalakasana",
    "ustrasana",
    "utkatasana",
    "utkata_konasana",
    "virabhadrasana_i",
    "virabhadrasana_ii",
    "vrikshasana",
]

# Pre-processing

TORSO_MULTIPLIER = 2.5

def load_as_df(class_to_landmarks):
    y = [entry[0] for entry in class_to_landmarks]
    y = np_utils.to_categorical([labels_list.index(className) for className in y])
    x = [entry[1] for entry in class_to_landmarks]
    return x, y

def average(a, b):
    ax = a[0]
    ay = a[1]
    az = a[2]
    bx = b[0]
    by = b[1]
    bz = b[2]
    return ((ax + bx) * 0.5, (ay + by) * 0.5, (az + bz) * 0.5)

def subtract(a, b):
    ax = a[0]
    ay = a[1]
    az = a[2]
    bx = b[0]
    by = b[1]
    bz = b[2]
    return (ax - bx, ay - by, az - bz)

def multiply(a, multiple):
    ax = a[0]
    ay = a[1]
    az = a[2]
    return (ax * multiple, ay * multiple, az * multiple)

def l2Norm2D(a):
    ax = a[0]
    ay = a[1]
    return math.hypot(ax, ay)

def get_pose_size(landmarks):
    hips_center = average(
        landmarks[mp_pose.PoseLandmark.LEFT_HIP], 
        landmarks[mp_pose.PoseLandmark.RIGHT_HIP]
    )
    shoulders_center = average(
        landmarks[mp_pose.PoseLandmark.LEFT_SHOULDER],
        landmarks[mp_pose.PoseLandmark.RIGHT_SHOULDER]
    )
    torso_size = l2Norm2D(subtract(hips_center, shoulders_center))
    max_distance = torso_size * TORSO_MULTIPLIER
    # torsoSize * TORSO_MULTIPLIER is the floor we want but actual size
    # can be bigger for a given pose depending on extension of limbs etc so we calculate that.
    for landmark in landmarks:
        distance = l2Norm2D(subtract(hips_center, landmark))
        if (distance > max_distance):
            max_distance = distance
    return max_distance

def normalize(entry):
    normalized = list(zip(*[iter(entry)]*3))
    left_hip = normalized[mp_pose.PoseLandmark.LEFT_HIP]
    right_hip = normalized[mp_pose.PoseLandmark.RIGHT_HIP]
    hip_center = average(left_hip, right_hip)
    normalized = [subtract(point, hip_center) for point in normalized]
    pose_size = get_pose_size(normalized)
    normalized = [multiply(point, 1 / pose_size) for point in normalized]
    #Multiplication by 100 to make it easier to debug.
    normalized = [multiply(point, 100) for point in normalized]
    return normalized
    
def get_embedding(entry):
    embeddings = []

    # Group our distances by number of joints between the pairs.
    # One joint. - (9)
    embeddings.append(
        subtract(
            average(entry[mp_pose.PoseLandmark.LEFT_SHOULDER], entry[mp_pose.PoseLandmark.RIGHT_SHOULDER]),
            average(entry[mp_pose.PoseLandmark.LEFT_HIP], entry[mp_pose.PoseLandmark.RIGHT_HIP])
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_SHOULDER],
            entry[mp_pose.PoseLandmark.LEFT_SHOULDER]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_ELBOW],
            entry[mp_pose.PoseLandmark.RIGHT_SHOULDER]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.LEFT_WRIST],
            entry[mp_pose.PoseLandmark.LEFT_ELBOW]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_WRIST],
            entry[mp_pose.PoseLandmark.RIGHT_ELBOW]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.LEFT_KNEE],
            entry[mp_pose.PoseLandmark.LEFT_HIP]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_KNEE],
            entry[mp_pose.PoseLandmark.RIGHT_HIP]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.LEFT_ANKLE],
            entry[mp_pose.PoseLandmark.LEFT_KNEE]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_ANKLE],
            entry[mp_pose.PoseLandmark.RIGHT_KNEE]
        )
    )
    
    # Two joints. (4)
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.LEFT_WRIST],
            entry[mp_pose.PoseLandmark.LEFT_SHOULDER]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_WRIST],
            entry[mp_pose.PoseLandmark.RIGHT_SHOULDER]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.LEFT_ANKLE],
            entry[mp_pose.PoseLandmark.LEFT_HIP]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_ANKLE],
            entry[mp_pose.PoseLandmark.RIGHT_HIP]
        )
    )

    # Four joints. (2)
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.LEFT_WRIST],
            entry[mp_pose.PoseLandmark.LEFT_HIP]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_WRIST],
            entry[mp_pose.PoseLandmark.RIGHT_HIP]
        )
    )

    # Five joints. (4)
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.LEFT_ANKLE],
            entry[mp_pose.PoseLandmark.LEFT_SHOULDER]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_ANKLE],
            entry[mp_pose.PoseLandmark.RIGHT_SHOULDER]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.LEFT_WRIST],
            entry[mp_pose.PoseLandmark.LEFT_HIP]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_WRIST],
            entry[mp_pose.PoseLandmark.RIGHT_HIP]
        )
    )

    # Cross body. (4)
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_ELBOW],
            entry[mp_pose.PoseLandmark.LEFT_ELBOW]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_KNEE],
            entry[mp_pose.PoseLandmark.LEFT_KNEE]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_WRIST],
            entry[mp_pose.PoseLandmark.LEFT_WRIST]
        )
    )
    embeddings.append(
        subtract(
            entry[mp_pose.PoseLandmark.RIGHT_ANKLE],
            entry[mp_pose.PoseLandmark.LEFT_ANKLE]
        )
    )
    return embeddings
    
def pre_process(x):
    processed_x = []
    for entry in x:
        normalized_landmarks = normalize(entry)
        embeddings = get_embedding(normalized_landmarks)
        flattened_embeddings = [point_f for feature in embeddings for point_f in feature]
        processed_x.append(flattened_embeddings)
    return tf.convert_to_tensor(processed_x)

X_train, y_train = load_as_df(train_in)
X_test_in, y_test_in = load_as_df(test_in)

# split test to test and validation (50-50 = 10% each)
X_test, X_val, y_test, y_val = train_test_split(X_test_in, y_test_in, test_size=0.5, shuffle=True, random_state=123)

processed_X_train = pre_process(X_train)
processed_X_val =  pre_process(X_val)
processed_X_test = pre_process(X_test)

print("Count of classes:")
print(len(labels_list))
print("Size of train x, y :")
print(len(processed_X_train))
print(len(y_train))
print("Size of test x, y :")
print(len(processed_X_test))
print(len(y_test))
print("Size of val x, y :")
print(len(processed_X_val))
print(len(y_val))

print(processed_X_train[50:51])

print(y_train[50:51])

inputs = tf.keras.Input(shape=(69))
model = Sequential()
model.add(inputs)
model.add(Dense(128, activation=relu))
model.add(Dropout(0.5))
model.add(Dense(64, activation=relu))
model.add(Dropout(0.5))
model.add(Dense(len(labels_list), activation="softmax"))

optimizer = Adam()

model.compile(
    optimizer = optimizer,
    loss = CategoricalCrossentropy(),
    metrics = ['accuracy']
)

# Start training
model_history = model.fit(
    processed_X_train, 
    y_train,
    epochs = 200,
    batch_size = 16,
    validation_data = (processed_X_val, y_val)
)

def show_val_metric(history, metric):
    train_metrics = history.history[metric]
    val_metrics = history.history['val_' + metric]
    epochs = range(1, len(train_metrics) + 1)
    plt.plot(epochs, train_metrics)
    plt.plot(epochs, val_metrics)
    plt.title('Training and validation ' + metric)
    plt.xlabel("Epochs")
    plt.ylabel(metric)
    plt.legend(["train_" + metric, 'val_' + metric])
    plt.show()

show_val_metric(model_history, 'loss')
show_val_metric(model_history, 'accuracy')

# Convert and save tflite model

converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
ct = time.strftime("%Y%m%d-%H%M%S")
open("models/converted_model{}.tflite".format(ct), "wb").write(tflite_model)
print("Done")

# Testing model

loss, accuracy = model.evaluate(processed_X_test, y_test, verbose = 0)
print("=====================Loss=======================")
print('Loss : ', loss)
print("Accuracy : ", accuracy)

y_test_pred_model = model.predict(processed_X_test)
y_test_pred = np_utils.to_categorical(np.argmax(y_test_pred_model, axis=1))

print("=====================Accuracy=======================")
print(accuracy_score(y_test, y_test_pred))

print("=====================Classification Report=======================")
print(classification_report(y_test, y_test_pred, target_names = labels_list))

print("=====================Confusion Matrix=======================")
y_pred_classes = [labels_list[i] for i in np.argmax(y_test_pred_model, axis=1)]
y_test_classes = [labels_list[i] for i in np.argmax(y_test, axis=1)]

con_matrix = confusion_matrix(y_test_classes, y_pred_classes, labels=labels_list)
print(con_matrix)

print(model.summary())