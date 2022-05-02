from PIL import Image
import os
import re
from pathlib import Path

DIR = "A:\Projects\CollegeProjects\YogaPartner\data\\v2\\adho_mukha_shvanasana"
files = os.listdir(DIR)
count = 0
for f in files:
    old_path = os.path.join(DIR, f)
    p = Path(old_path)
    new_file_name = f"adho_mukha_shvanasana_{count}{p.suffix}"
    new_file_path = os.path.join(DIR, new_file_name)
    os.rename(old_path, new_file_path)
    count = count + 1
