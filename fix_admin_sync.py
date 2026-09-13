import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Make sure we don't attach the schools listener for Admin, as Admin uses forceSyncSchools which does a one-time get.
# Actually, the listener is fine for Admin because they have full read access. 
# BUT if they get Permission Denied, maybe we should just rely on forceSyncSchools.

# Let's fix forceSyncSchools to wait for auth state
# We already did this with `.await()`.

content = content.replace(
    'firestore.collection("schools").get().addOnSuccessListener { snapshot ->',
    'val task = firestore.collection("schools").get()\n            task.addOnSuccessListener { snapshot ->'
)

with open(filepath, 'w') as f:
    f.write(content)

