import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-app.js";
import { getDatabase, ref, onValue } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-database.js";

const firebaseConfig = {
  apiKey: "AIzaSyCFzxiVtMxfbFmnl9nXdg9JOLBjqAedqK0",
  authDomain: "scolapay-b6289.firebaseapp.com",
  databaseURL: "https://scolapay-b6289-default-rtdb.europe-west1.firebasedatabase.app",
  projectId: "scolapay-b6289",
  storageBucket: "scolapay-b6289.firebasestorage.app",
  messagingSenderId: "906222981497",
  appId: "1:906222981497:web:cdfc4b9bfe87e99ef0dfa8"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const database = getDatabase(app);

// Make functions available globally for HTML onclick attributes
window.toggleSection = function(id) {
    const section = document.getElementById(id);
    if (section.classList.contains('hidden')) {
        section.classList.remove('hidden');
        setTimeout(() => {
            section.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }, 100);
    } else {
        section.classList.add('hidden');
    }
};

window.showQrBadge = function() {
    const urlParams = new URLSearchParams(window.location.search);
    const rawUrl = window.location.href; 
    
    const qrData = encodeURIComponent(rawUrl);
    
    document.getElementById('qrCodeContainer').innerHTML = `<img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${qrData}" alt="QR Code">`;
    document.getElementById('qrStudentName').textContent = urlParams.get('name') || '';
    document.getElementById('qrStudentMat').textContent = 'Matricule: ' + (urlParams.get('mat') || '');
    
    document.getElementById('qrModal').classList.remove('hidden');
};

window.closeQrModal = function() {
    document.getElementById('qrModal').classList.add('hidden');
};

document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    
    if (!urlParams.has('name') && !urlParams.has('mat') && !urlParams.has('id')) {
        document.getElementById('loading').classList.add('hidden');
        document.getElementById('errorState').classList.remove('hidden');
        return;
    }

    const studentId = urlParams.get('id');
    const studentName = urlParams.get('name') || 'Élève';
    const studentMat = urlParams.get('mat') || 'N/A';
    const studentGrade = urlParams.get('grade') || '';
    const studentSection = urlParams.get('section') || '';
    
    // Finances (from URL initially)
    let totalFee = urlParams.get('totalFee') || '';
    let paidFee = urlParams.get('paidFee') || '';
    let dueFee = urlParams.get('dueFee') || '';
    let percent = urlParams.get('percent') || '0';
    
    // Académique (Bulletin)
    const term = urlParams.get('term') || '';
    const avg = urlParams.get('avg') || '';
    const rank = urlParams.get('rank') || '';
    const size = urlParams.get('size') || '';
    const mention = urlParams.get('mention') || '';
    
    // Ecole
    const school = urlParams.get('school') || 'ScolaPay';
    const year = urlParams.get('year') || 'Portail Parent';

    // Remplir les informations de l'école
    document.getElementById('schoolName').textContent = school;
    document.getElementById('schoolYear').textContent = year;
    document.getElementById('welcomeText').textContent = `Bonjour, Parent de ${studentName} (${studentGrade})`;

    function updateFinancialUI(tFee, pFee, dFee, pCent) {
        if (!tFee) {
            document.getElementById('financialSection').classList.add('hidden');
            return;
        }
        
        document.getElementById('financialSection').classList.remove('hidden');
        
        document.getElementById('mainDueFee').textContent = dFee || '0 GNF';
        document.getElementById('totalFee').textContent = tFee;
        document.getElementById('paidFee').textContent = pFee;
        document.getElementById('dueFee').textContent = dFee;
        
        let percentValue = parseFloat(String(pCent).replace(',', '.').replace('%', ''));
        if (isNaN(percentValue)) percentValue = 0;
        
        const barWidth = percentValue > 100 ? 100 : percentValue;
        
        setTimeout(() => {
            document.getElementById('paymentProgress').style.width = barWidth + '%';
        }, 300);
        
        document.getElementById('paymentPercent').textContent = percentValue.toFixed(0) + '% Payé';
        
        if (percentValue >= 100) {
            document.getElementById('paymentPercent').style.color = 'var(--success)';
            document.getElementById('paymentPercent').textContent = '✅ Scolarité Soldée';
        } else {
            document.getElementById('paymentPercent').style.color = 'var(--text-muted)';
        }
    }

    // Initialize with URL params first
    updateFinancialUI(totalFee, paidFee, dueFee, percent);

    // Setup academic UI
    if (avg) {
        document.getElementById('academicTerm').textContent = term;
        
        let maxScore = 20;
        if (studentSection.toLowerCase().includes('primaire') || studentSection.toLowerCase().includes('maternelle')) {
            maxScore = 10;
        }
        
        document.getElementById('academicAvg').textContent = avg + ' / ' + maxScore;
        document.getElementById('academicRank').textContent = rank + (rank == '1' ? 'er' : 'ème');
        document.getElementById('academicSize').textContent = size;
        document.getElementById('academicMention').textContent = mention;
    } else {
        document.getElementById('academicSection').classList.add('hidden');
    }

    // Connect to Firebase Realtime Database
    // We use the 'mat' (matricule) as the key to find the student data
    const studentKey = studentMat !== 'N/A' ? studentMat : studentId;
    
    if (studentKey) {
        const studentRef = ref(database, 'students/' + studentKey);
        onValue(studentRef, (snapshot) => {
            const data = snapshot.val();
            if (data) {
                const formatGNF = (num) => {
                    if (num === null || num === undefined) return "0 GNF";
                    // Using French locale and replacing spaces for large numbers
                    return Number(num).toLocaleString('fr-FR').replace(/,/g, ' ') + " GNF";
                };

                // Override URL data with fresh DB data
                const dbTotal = data.totalFee || 0;
                const dbPaid = data.paidFee || 0;
                const dbDue = dbTotal - dbPaid;
                
                let dbPercent = 0;
                if (dbTotal > 0) {
                    dbPercent = (dbPaid / dbTotal) * 100;
                }

                updateFinancialUI(formatGNF(dbTotal), formatGNF(dbPaid), formatGNF(dbDue), dbPercent);

                if (data.academics) {
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
                        document.getElementById('academicAvg').textContent = termData.avg + ' / ' + (document.getElementById('academicAvg').textContent.split('/')[1] || '20').trim();
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
                }

            }
        });
    }

    // Afficher le contenu
    setTimeout(() => {
        document.getElementById('loading').classList.add('hidden');
        document.getElementById('mainContent').classList.remove('hidden');
    }, 500);
});
