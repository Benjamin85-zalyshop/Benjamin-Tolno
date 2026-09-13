import re

filepath = 'app/build.gradle.kts'
with open(filepath, 'r') as f:
    content = f.read()

# Increment versionCode to ensure they see it's a new version
# This might not be strictly necessary, but good practice
content = re.sub(r'versionCode = \d+', 'versionCode = 4', content)
content = re.sub(r'versionName = ".*?"', 'versionName = "1.0.3"', content)

with open(filepath, 'w') as f:
    f.write(content)

