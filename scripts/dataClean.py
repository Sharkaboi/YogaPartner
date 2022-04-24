import os
import re
from pathlib import Path

version = 1
path = "A:\Projects\CollegeProjects\FinalYearProject\YogaPartner\data\v" + version
folders = os.listdir(path)
for f in folders:
    old_path = os.path.join(path, f)
    new_name = re.sub(r"\s+", "_", f)
    new_path = os.path.join(path, re.sub(r"\s+", "_", new_name))
    os.rename(old_path, new_path)
    images = os.listdir(new_path)
    count = 1
    for image in images:
        if not (image.endswith(".jpg") or image.endswith(".jpeg") or image.endswith(".png")):
            os.remove(os.path.join(new_path, image))
            print("Removed " + image)
            continue
        old_file_path = os.path.join(new_path, image)
        p = Path(old_file_path)
        ext = p.suffix
        new_file_name = f"{new_name}_{count}{ext}"
        new_file_path = os.path.join(new_path, new_file_name)
        os.rename(old_file_path, new_file_path)
        count += 1
