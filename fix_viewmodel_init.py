import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

find_text = '''class SchoolViewModel(private val repository: SchoolRepository) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()'''

replace_text = '''class SchoolViewModel(private val repository: SchoolRepository) : ViewModel() {
    private val firestore: com.google.firebase.firestore.FirebaseFirestore by lazy { com.google.firebase.firestore.FirebaseFirestore.getInstance() }'''

content = content.replace(find_text, replace_text)

with open(filepath, 'w') as f:
    f.write(content)

