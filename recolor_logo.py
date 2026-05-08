import sys
from PIL import Image
import colorsys

def recolor_image(input_path, output_path, target_hue1, target_hue2):
    try:
        img = Image.open(input_path).convert('RGBA')
        pixels = img.load()
        width, height = img.size
        
        for y in range(height):
            for x in range(width):
                r, g, b, a = pixels[x, y]
                if a == 0:
                    continue
                
                # Convert to HSV
                h, s, v = colorsys.rgb_to_hsv(r/255.0, g/255.0, b/255.0)
                
                # We know the original has purples (h ~0.7-0.8) and cyans (h ~0.45-0.55)
                # We will shift purples to deep blue (h ~0.6) and cyans to sky blue (h ~0.55)
                
                # Simple hue shift:
                # If it's in the purple/pink range, make it deep blue
                if h > 0.65 or h < 0.1:
                    h = target_hue1  # Deep Blue
                # If it's in the cyan/green range, make it sky blue
                elif h >= 0.1 and h <= 0.65:
                    h = target_hue2  # Sky Blue
                    
                # Convert back
                nr, ng, nb = colorsys.hsv_to_rgb(h, s, v)
                pixels[x, y] = (int(nr*255), int(ng*255), int(nb*255), a)
                
        img.save(output_path)
        print("Successfully recolored and saved to", output_path)
    except Exception as e:
        print("Error:", e)

# Target Hues:
# Deep Blue: ~220 deg -> 220/360 = 0.61
# Sky Blue: ~200 deg -> 200/360 = 0.55
target1 = 0.61
target2 = 0.55

input_file = r"c:\projects\borrow_buddy_app\web_frontend\public\app_logo.png"
output_file = r"c:\projects\borrow_buddy_app\web_frontend\public\app_logo.png"
android_output = r"c:\projects\borrow_buddy_app\frontend\app\src\main\res\drawable\app_logo.png"

recolor_image(input_file, output_file, target1, target2)
# Copy to android
import shutil
shutil.copy2(output_file, android_output)
print("Copied to Android res folder.")
