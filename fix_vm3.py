with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'r') as f:
    code = f.read()

# Remove the import com.example.data.models.* from the middle of the file if it's there
# Wait, I did:
# data class SchoolAdminItem(...)
# import com.example.data.repository.SchoolRepository
# import com.google.firebase.firestore.FirebaseFirestore
# These imports are in the middle of the file. They need to be at the top.

# Let's clean up imports
lines = code.split('\n')
imports = []
other = []
for line in lines:
    if line.startswith('import '):
        imports.append(line)
    else:
        other.append(line)

new_code = "package com.example.ui\n\n" + '\n'.join(imports) + '\n\n' + '\n'.join(other).replace("package com.example.ui", "").strip()

with open('app/src/main/java/com/example/ui/SchoolViewModel.kt', 'w') as f:
    f.write(new_code)
