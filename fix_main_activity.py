filepath = 'app/src/main/java/com/example/MainActivity.kt'
with open(filepath, 'r') as f:
    content = f.read()

find_text = '''    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()'''

replace_text = '''    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseInit", "Error initializing Firebase", e)
        }'''

if find_text in content:
    content = content.replace(find_text, replace_text)
    with open(filepath, 'w') as f:
        f.write(content)
    print("MainActivity patched successfully")
else:
    print("Could not find text in MainActivity")

