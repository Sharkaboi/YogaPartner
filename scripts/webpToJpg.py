from PIL import Image
import os
import re
from pathlib import Path

DIR = "A:\Projects\CollegeProjects\YogaPartner\data\\v2\\adho_mukha_shvanasana"
files = os.listdir(DIR)
for f in files:
    old_path = os.path.join(DIR, f)
    p = Path(old_path)
    ext = p.suffix
    print("Checking " + f)
    if ext == ".webp":
        print("Converting " + f)
        new_file_name = f"{p.stem}.jpeg"
        im = Image.open(old_path).convert("RGB")
        new_file_path = os.path.join(DIR, new_file_name)
        print(new_file_path)
        im.save(new_file_path, "jpeg")
        print("Removing " + f)
        os.remove(old_path)

files = os.listdir(DIR)
count = 0
for f in files:
    if ext == ".webp":
        count = count + 1
print("Webp count " + str(count))
