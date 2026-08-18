import re

with open('public/app.js', 'r') as f:
    content = f.read()

# Replace RTDB import with Firestore import
content = content.replace(
    'import { getDatabase, ref, onValue } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-database.js";',
    'import { getFirestore, doc, onSnapshot } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-firestore.js";'
)

# Replace database init with firestore init
content = content.replace(
    'const database = getDatabase(app);',
    'const firestore = getFirestore(app);'
)

# Fix the rid usage
content = content.replace(
    "const studentMat = urlParams.get('mat') || 'N/A';",
    "const studentMat = urlParams.get('mat') || 'N/A';\n    const rid = urlParams.get('rid') || '';"
)

# Replace RTDB logic
rtdb_logic = """    // Connect to Firebase Realtime Database
    // We use the 'mat' (matricule) as the key to find the student data
    const studentKey = studentMat !== 'N/A' ? studentMat : studentId;
    
    if (studentKey) {
        const studentRef = ref(database, 'students/' + studentKey);
        onValue(studentRef, (snapshot) => {
            const data = snapshot.val();
            if (data) {"""

firestore_logic = """    // Connect to Firestore
    if (rid) {
        const studentRef = doc(firestore, 'students', rid);
        onSnapshot(studentRef, (snapshot) => {
            if (snapshot.exists()) {
                const data = snapshot.data();"""

content = content.replace(rtdb_logic, firestore_logic)

# Replace RTDB academics logic
rtdb_academics = """                if (data.academics) {
                    const currentTerm = document.getElementById('academicTerm').textContent;
                    let termData = data.academics[currentTerm];
                    
                    // If currentTerm doesn't exist, try the first available term
                    if (!termData) {
                        const availableTerms = Object.keys(data.academics);
                        if (availableTerms.length > 0) {
                            termData = data.academics[availableTerms[0]];
                            document.getElementById('academicTerm').textContent = availableTerms[0];
                        }
                    }
                    
                    if (termData) {
                        document.getElementById('academicSection').classList.remove('hidden');
                        let maxScore = 20;
                        if (studentSection.toLowerCase().includes('primaire') || studentSection.toLowerCase().includes('maternelle')) {
                            maxScore = 10;
                        }
                        document.getElementById('academicAvg').textContent = termData.avg + ' / ' + maxScore;
                        document.getElementById('academicRank').textContent = termData.rank + (termData.rank == '1' ? 'er' : 'ème');
                        document.getElementById('academicSize').textContent = termData.size;
                        document.getElementById('academicMention').textContent = termData.mention;
                        
                        if (termData.subjects) {
                            const tbody = document.getElementById('subjectsTableBody');
                            tbody.innerHTML = '';
                            
                            Object.entries(termData.subjects).forEach(([subjectName, subjData]) => {
                                const tr = document.createElement('tr');
                                tr.style.borderBottom = "1px solid #E5E7EB";
                                
                                const tdName = document.createElement('td');
                                tdName.style.padding = "0.75rem";
                                tdName.textContent = subjectName;
                                
                                const tdEval = document.createElement('td');
                                tdEval.style.padding = "0.75rem";
                                tdEval.textContent = subjData.eval;
                                
                                const tdAvg = document.createElement('td');
                                tdAvg.style.padding = "0.75rem";
                                tdAvg.style.fontWeight = "600";
                                tdAvg.textContent = subjData.avg;
                                
                                const maxScore = parseFloat(subjData.max) || 20;
                                const avgScore = parseFloat(subjData.avg);
                                if (!isNaN(avgScore) && avgScore < maxScore / 2) {
                                    tdAvg.style.color = "var(--danger)";
                                }
                                
                                const evalScore = parseFloat(subjData.eval);
                                if (!isNaN(evalScore) && evalScore < maxScore / 2) {
                                    tdEval.style.color = "var(--danger)";
                                }
                                
                                tr.appendChild(tdName);
                                tr.appendChild(tdEval);
                                tr.appendChild(tdAvg);
                                
                                tbody.appendChild(tr);
                            });
                            
                            document.getElementById('subjectsContainer').classList.remove('hidden');
                        }
                    }
                }"""

firestore_academics = """                const currentTerm = document.getElementById('academicTerm').textContent;
                let termKey = "academic_" + currentTerm;
                
                // fallback to finding the first academic_* if exact term not found
                if (!data[termKey]) {
                    const keys = Object.keys(data).filter(k => k.startsWith('academic_'));
                    if (keys.length > 0) {
                        termKey = keys[0];
                        document.getElementById('academicTerm').textContent = termKey.replace('academic_', '');
                    }
                }
                
                if (data[termKey]) {
                    const termData = data[termKey];
                    document.getElementById('academicSection').classList.remove('hidden');
                    let maxScore = 20;
                    if (studentSection.toLowerCase().includes('primaire') || studentSection.toLowerCase().includes('maternelle')) {
                        maxScore = 10;
                    }
                    if (termData.average) document.getElementById('academicAvg').textContent = termData.average + ' / ' + maxScore;
                    if (termData.rank) document.getElementById('academicRank').textContent = termData.rank + (termData.rank == '1' ? 'er' : 'ème');
                    if (termData.classSize) document.getElementById('academicSize').textContent = termData.classSize;
                    if (termData.mention) document.getElementById('academicMention').textContent = termData.mention;
                    
                    if (termData.details && Array.isArray(termData.details)) {
                        const tbody = document.getElementById('subjectsTableBody');
                        tbody.innerHTML = '';
                        
                        termData.details.forEach(subjData => {
                            const tr = document.createElement('tr');
                            tr.style.borderBottom = "1px solid #E5E7EB";
                            
                            const tdName = document.createElement('td');
                            tdName.style.padding = "0.75rem";
                            tdName.textContent = subjData['Matière'] || '';
                            
                            const tdEval = document.createElement('td');
                            tdEval.style.padding = "0.75rem";
                            tdEval.textContent = subjData['Éval.'] || '-';
                            
                            const tdAvg = document.createElement('td');
                            tdAvg.style.padding = "0.75rem";
                            tdAvg.style.fontWeight = "600";
                            tdAvg.textContent = subjData['Moy.'] || '-';
                            
                            const avgScore = parseFloat(subjData['Moy.']);
                            if (!isNaN(avgScore) && avgScore < maxScore / 2) {
                                tdAvg.style.color = "var(--danger)";
                            }
                            
                            const evalScore = parseFloat(subjData['Éval.']);
                            if (!isNaN(evalScore) && evalScore < maxScore / 2) {
                                tdEval.style.color = "var(--danger)";
                            }
                            
                            tr.appendChild(tdName);
                            tr.appendChild(tdEval);
                            tr.appendChild(tdAvg);
                            
                            tbody.appendChild(tr);
                        });
                        
                        document.getElementById('subjectsContainer').classList.remove('hidden');
                    }
                }"""

content = content.replace(rtdb_academics, firestore_academics)

with open('public/app.js', 'w') as f:
    f.write(content)

print("Patched app.js successfully")
