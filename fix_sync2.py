import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Let's locate the saveGrade function
match = re.search(r'fun saveGrade\(.*?\{.*?addOnFailureListener.*?\}', content, re.DOTALL)
if match:
    start_idx = match.start()
    end_idx = match.end()
    
    # We want to inject `syncStudentAcademicsToRTDB(schoolId, studentId, term)` right after the addOnFailureListener line.
    
    # Find the exact addOnFailureListener line for saveGrade
    sub_content = content[start_idx:end_idx + 150] # a bit more to get the closing braces
    
    # Let's just do a string replacement in this chunk
    chunk = content[start_idx:start_idx+2000]
    
    if "syncStudentAcademicsToRTDB" not in chunk:
        new_chunk = chunk.replace(
            ').addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }',
            ').addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }\n            syncStudentAcademicsToRTDB(schoolId, studentId, term)'
        )
        content = content[:start_idx] + new_chunk + content[start_idx+2000:]
        with open(filepath, 'w') as f:
            f.write(content)
        print("Fixed!")
    else:
        print("Already fixed?")
else:
    print("Not found")

