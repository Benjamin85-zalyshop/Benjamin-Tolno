import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

find_text = '''class SchoolViewModel(private val repository: SchoolRepository) : ViewModel() {
    private val _dbStatus = MutableStateFlow<String>("Checking...")'''

replace_text = '''class SchoolViewModel(private val repository: SchoolRepository) : ViewModel() {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    
    private val _dbStatus = MutableStateFlow<String>("Checking...")'''

# Need to find the exact declaration
find_exact = '''class SchoolViewModel(private val repository: SchoolRepository) : ViewModel() {
    
    private val firestore = FirebaseFirestore.getInstance()'''

replace_exact = '''class SchoolViewModel(private val repository: SchoolRepository) : ViewModel() {
    
    private val firestore by lazy { FirebaseFirestore.getInstance() }'''

content = content.replace(find_exact, replace_exact)

with open(filepath, 'w') as f:
    f.write(content)

