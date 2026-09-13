import re

filepath = 'app/build.gradle.kts'
with open(filepath, 'r') as f:
    content = f.read()

content = re.sub(r'versionCode = \d+', 'versionCode = 6', content)
content = re.sub(r'versionName = ".*?"', 'versionName = "1.0.5"', content)

with open(filepath, 'w') as f:
    f.write(content)

