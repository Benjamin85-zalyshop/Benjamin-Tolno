import re
filepath = 'app/build.gradle.kts'
with open(filepath, 'r') as f:
    content = f.read()

content = re.sub(r'versionCode = \d+', 'versionCode = 17', content)
content = re.sub(r'versionName = ".*?"', 'versionName = "1.0.16"', content)

with open(filepath, 'w') as f:
    f.write(content)
