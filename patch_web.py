import re

with open('public/app.js', 'r') as f:
    content = f.read()

# Replace imports
old_imports = """import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-app.js";
import { getFirestore, doc, onSnapshot } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-firestore.js";"""

new_imports = """import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-app.js";
import { getDatabase, ref, onValue } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-database.js";"""

content = content.replace(old_imports, new_imports)

# Replace firestore init
old_init = "const firestore = getFirestore(app);"
new_init = "const database = getDatabase(app);"
content = content.replace(old_init, new_init)

# Replace fetch logic
old_fetch = """    if (rid) {
        const studentRef = doc(firestore, 'students', rid);
        onSnapshot(studentRef, (snapshot) => {
            if (snapshot.exists()) {
                const data = snapshot.data();"""

new_fetch = """    if (rid) {
        const studentRef = ref(database, 'students/' + rid);
        onValue(studentRef, (snapshot) => {
            if (snapshot.exists()) {
                const data = snapshot.val();"""

content = content.replace(old_fetch, new_fetch)

with open('public/app.js', 'w') as f:
    f.write(content)

print("Web patched!")
