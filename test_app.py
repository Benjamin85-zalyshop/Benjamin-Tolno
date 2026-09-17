import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

find_text = '''class SchoolViewModel(private val repository: SchoolRepository) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()'''

if find_text in content:
    print("Found Firestore init in ViewModel")
else:
    print("Not found")
