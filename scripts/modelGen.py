import os
import csv
import cv2
import numpy as np
import sys
import tqdm

from mediapipe.python.solutions import drawing_utils as mp_drawing
from mediapipe.python.solutions import pose as mp_pose

# Required structure of the images_in_folder:
#   data/
#     asana_1/
#       image_001.jpg
#       image_002.jpg
#       ...
#     asana_2/
#       image_001.jpg
#       image_002.jpg
#       ...
#     ...
version = 1
images_in_folder = 'A:\Projects\CollegeProjects\FinalYearProject\YogaPartner\data\\v' + str(version)

# Output folders for bootstrapped images and CSVs. Image will have a predicted
# Pose rendering and can be used to remove unwanted samples.
images_out_folder = 'A:\Projects\CollegeProjects\FinalYearProject\YogaPartner\intermediates\\v' + str(version) +'\data_out'

# Output CSV path to put bootstrapped poses to. This CSV will be used by the app.
#
# Output CSV format:
#   sample_00001,pose_class_1,x1,y1,z1,x2,y2,z2,...,x33,y33,z33
#   sample_00002,pose_class_2,x1,y1,z1,x2,y2,z2,...,x33,y33,z33
#   ...
csv_out_path = 'A:\Projects\CollegeProjects\FinalYearProject\YogaPartner\intermediates\\v' + str(version) +'\data_out.csv'

def getClasses() :
    # Folder names are used as pose class names.
    pose_class_names = []
    for n in os.listdir(images_in_folder):
        if n.startswith('.'):
            continue
        pose_class_names.append(n)

    return sorted(pose_class_names)

def getAbsolutePathOfInImage(pose_class_name, image_name):
    return os.path.join(images_in_folder, pose_class_name, image_name)

def getAbsolutePathOfOutImage(image_name):
    return os.path.join(images_out_folder, image_name)

def getAbsolutePathOfInClass(pose_class_name):
    return os.path.join(images_in_folder, pose_class_name)

def getAbsolutePathOfOutClass(pose_class_name):
    return os.path.join(images_out_folder, pose_class_name)

def makePlaceholderOutFolder(pose_class_name):
    if not os.path.exists(getAbsolutePathOfOutClass(pose_class_name)):
        os.makedirs(getAbsolutePathOfOutClass(pose_class_name))

def getImagePathsOfClass(pose_class_name):
    images = []
    for n in os.listdir(getAbsolutePathOfInClass(pose_class_name)):
        if n.startswith('.'):
            continue
        if not (n.endswith(".jpg") or n.endswith(".jpeg") or n.endswith(".png")):
            continue
        images.append(n)
    return sorted(images)
    
with open(csv_out_path, 'w') as csv_out_file:
    csv_out_writer = csv.writer(csv_out_file, delimiter=',', quoting=csv.QUOTE_MINIMAL)

    pose_class_names = getClasses()

    for pose_class_name in pose_class_names:
        print('Bootstrapping ', pose_class_name, file=sys.stderr)

        makePlaceholderOutFolder(pose_class_name)
        image_names = getImagePathsOfClass(pose_class_name)

        for image_name in tqdm.tqdm(image_names, position=0):
            image_path = getAbsolutePathOfInImage(pose_class_name, image_name)
            # Load image.
            input_frame = cv2.imread(image_path)
            input_frame = cv2.cvtColor(input_frame, cv2.COLOR_BGR2RGB)

            # Initialize fresh pose tracker and run it.
            with mp_pose.Pose() as pose_tracker:
                result = pose_tracker.process(image=input_frame)
                pose_landmarks = result.pose_landmarks

            # Save image with pose prediction (if pose was detected).
            output_frame = input_frame.copy()
            if pose_landmarks is not None:
                mp_drawing.draw_landmarks(
                    image=output_frame,
                    landmark_list=pose_landmarks,
                    connections=mp_pose.POSE_CONNECTIONS)
            output_frame = cv2.cvtColor(output_frame, cv2.COLOR_RGB2BGR)
            out_path = getAbsolutePathOfOutImage(image_name)
            cv2.imwrite(out_path, output_frame)

            # Save landmarks.
            if pose_landmarks is not None:
                # Check the number of landmarks and take pose landmarks.
                assert len(pose_landmarks.landmark) == 33, 'Unexpected number of predicted pose landmarks: {}'.format(
                    len(pose_landmarks.landmark))
                pose_landmarks = [[lmk.x, lmk.y, lmk.z]
                                  for lmk in pose_landmarks.landmark]

                # Map pose landmarks from [0, 1] range to absolute coordinates to get
                # correct aspect ratio.
                frame_height, frame_width = output_frame.shape[:2]
                pose_landmarks *= np.array([frame_width,
                                           frame_height, frame_width])

                # Write pose sample to CSV.
                pose_landmarks = np.around(
                    pose_landmarks, 5).flatten().astype(np.str).tolist()
                csv_out_writer.writerow(
                    [image_name, pose_class_name] + pose_landmarks)
