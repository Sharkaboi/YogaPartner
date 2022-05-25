import csv
import numpy as np

landmark_count = 33

def save_to_csv(results, class_name, image_path, image_shape, csv_out_writer):
    pose_landmarks = results.pose_landmarks
    if pose_landmarks is not None and len(pose_landmarks.landmark) == landmark_count:
        pose_landmarks = [[lmk.x, lmk.y, lmk.z] for lmk in pose_landmarks.landmark]
        # Map pose landmarks from [0, 1] range to absolute coordinates to get correct aspect ratio.
        frame_height, frame_width = image_shape
        pose_landmarks *= np.array([frame_width, frame_height, frame_width])
        pose_landmarks = np.around(pose_landmarks, 5).flatten().astype(str).tolist()
        p = Path(image_path)
        fileName = p.name
        csv_out_writer.writerow([fileName, class_name] + pose_landmarks)
    else:
        print('No pose detected - length  - ' + image_path)
    return pose_landmarks

train_in = []
csvPath = os.path.join(intermediates_dir, "train_out.csv")
with open(csvPath, 'w', newline='') as csv_file:
    csv_out_writer = csv.writer(csv_file, delimiter=',', quoting=csv.QUOTE_MINIMAL)
    for row in train_rows:
        results, class_name, image_path, image_shape = row
        media_pipe_landmarks = save_to_csv(results, class_name, image_path, image_shape, csv_out_writer)
        if media_pipe_landmarks is not None:
            train_in.append((class_name, media_pipe_landmarks))
print("Training csv saved")

test_in = []
csvPath = os.path.join(intermediates_dir, "test_out.csv")
with open(csvPath, 'w', newline='') as csv_file:
    csv_out_writer = csv.writer(csv_file, delimiter=',', quoting=csv.QUOTE_MINIMAL)
    for row in test_rows:
        results, class_name, image_path, image_shape = row
        media_pipe_landmarks = save_to_csv(results, class_name, image_path, image_shape, csv_out_writer)
        if media_pipe_landmarks is not None:
            test_in.append((class_name, media_pipe_landmarks))
    print("Testing csv saved")