import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-app.js";
import { getFirestore, doc, onSnapshot } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-firestore.js";

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
const firestore = getFirestore(app);

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

window.downloadPdf = function() {
    const btn = document.getElementById('downloadPdfBtn');
    if (btn) btn.style.display = 'none';
    const urlParams = new URLSearchParams(window.location.search);
    const term = urlParams.get('term') || '1er Trimestre';
    const schoolName = urlParams.get('school') || 'ScolaPay';
    document.getElementById('pdfSchoolName').textContent = schoolName;
    if (document.getElementById('pdfSchoolLogoInitial')) { document.getElementById('pdfSchoolLogoInitial').textContent = schoolName.charAt(0).toUpperCase(); }
    document.getElementById('pdfTermInfo').textContent = `${term} • Année : 2025 - 2026`;
    document.getElementById('pdfStudentName').textContent = urlParams.get('name') || 'Élève';
    document.getElementById('pdfStudentMat').textContent = urlParams.get('mat') || 'N/A';
    document.getElementById('pdfStudentSection').textContent = urlParams.get('section') || 'LE PRIMAIRE';
    document.getElementById('pdfStudentGrade').textContent = urlParams.get('grade') || '2ème Année';
    
    const qrData = encodeURIComponent(window.location.href);
    document.getElementById('pdfQrCode').innerHTML = `<img src="https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=${qrData}" alt="QR" style="width:100%;height:100%;" crossorigin="anonymous">`;
    
    const avg = document.getElementById('academicAvg').textContent;
    document.getElementById('pdfAvg').textContent = avg;
    const rawRank = urlParams.get('rank') || '1';
    const rawSize = urlParams.get('size') || '1';
    const rankSuffix = rawRank === '1' ? 'er' : 'ème';
    const plural = parseInt(rawSize) > 1 ? 's' : '';
    document.getElementById('pdfRank').textContent = rawRank + rankSuffix + ' sur ' + rawSize + ' élève' + plural;
    const apprec = document.getElementById('academicAppreciation');
    document.getElementById('pdfAppreciation').textContent = apprec ? apprec.textContent.replace('Appréciation : ', '') : 'Encouragements';
    
    const origTbody = document.getElementById('subjectsTableBody');
    const pdfTbody = document.getElementById('pdfSubjectsTableBody');
    pdfTbody.innerHTML = '';
    let totalCoeff = 0;
    let totalPoints = 0;
    if (origTbody) {
        const rows = origTbody.querySelectorAll('tr');
        rows.forEach((row, index) => {
            const cells = row.querySelectorAll('td');
            if (cells.length >= 3) {
                const matName = cells[0].textContent;
                const note = parseFloat(cells[2].textContent);
                const coeff = 1;
                totalCoeff += coeff;
                if (!isNaN(note)) totalPoints += (note * coeff);
                let mention = 'Assez bien';
                if (note >= 9) mention = 'Très bien';
                else if (note >= 7) mention = 'Bien';
                else if (note < 5) mention = 'Passable';
                const tr = document.createElement('tr');
                tr.style.borderBottom = '1px solid #E5E7EB';
                if (index % 2 !== 0) tr.style.background = '#F9FAFB';
                tr.innerHTML = `
                    <td style="padding: 10px 16px; font-weight: 500; font-size: 13px;">${matName}</td>
                    <td style="padding: 10px 16px; text-align: center; color: #0047FF; font-weight: 600; font-size: 13px;">${coeff}</td>
                    <td style="padding: 10px 16px; text-align: center; color: #0047FF; font-weight: 600; font-size: 13px;">${!isNaN(note) ? note.toFixed(2) : '-'}</td>
                    <td style="padding: 10px 16px; color: #4B5563; font-size: 13px;">${mention}</td>
                `;
                pdfTbody.appendChild(tr);
            }
        });
    }
    document.getElementById('pdfTotalCoeff').textContent = totalCoeff;
    document.getElementById('pdfTotalPoints').textContent = totalPoints.toFixed(2);
    document.getElementById('pdfClassAvg').textContent = avg;
    
    const template = document.getElementById('pdfTemplate');
    const originalScroll = window.scrollY;
    
    // Create an overlay to hide the template from the user while rendering
    const overlay = document.createElement('div');
    overlay.style.position = 'fixed';
    overlay.style.top = '0';
    overlay.style.left = '0';
    overlay.style.width = '100vw';
    overlay.style.height = '100vh';
    overlay.style.backgroundColor = '#ffffff';
    overlay.style.zIndex = '999999';
    overlay.style.display = 'flex';
    overlay.style.justifyContent = 'center';
    overlay.style.alignItems = 'center';
    overlay.style.flexDirection = 'column';
    overlay.innerHTML = '<div style="width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #0047FF; border-radius: 50%; animation: spin 1s linear infinite;"></div><p style="margin-top: 20px; font-family: Inter, sans-serif; font-weight: 600; color: #111827;">Génération du PDF...</p><style>@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }</style>';
    document.body.appendChild(overlay);

    window.scrollTo(0, 0);
    
    template.style.display = 'block';
    template.style.position = 'absolute';
    template.style.top = '0px';
    template.style.left = '0px';
    template.style.width = '800px';
    template.style.transform = 'scale(1)';
    template.style.transformOrigin = 'top left';
    template.style.zIndex = '999998'; // Just below overlay
    
    setTimeout(() => {
        const elementToCapture = document.getElementById('pdfContent');
        const opt = {
            margin: [10, 0, 10, 0],
            filename: 'bulletin_de_notes.pdf',
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: { scale: 2, useCORS: true, scrollY: 0, scrollX: 0, windowWidth: 800 },
            jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
        };
        html2pdf().set(opt).from(elementToCapture).save().then(() => {
            if (btn) btn.style.display = 'block';
            template.style.display = 'none';
            document.body.removeChild(overlay);
            window.scrollTo(0, originalScroll);
        }).catch(err => {
            console.error(err);
            if (btn) btn.style.display = 'block';
            template.style.display = 'none';
            document.body.removeChild(overlay);
            window.scrollTo(0, originalScroll);
        });
    }, 1500); // Wait 1.5s for QR Code image to fully load
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
    const rid = urlParams.get('rid') || '';
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

    // Connect to Firestore
    if (rid) {
        const studentRef = doc(firestore, 'students', rid);
        onSnapshot(studentRef, (snapshot) => {
            if (snapshot.exists()) {
                const data = snapshot.data();
                                const formatCurrency = (num) => {
                    if (num === null || num === undefined) return "0 " + (window.schoolCurrency || "GNF");
                    return Number(num).toLocaleString('fr-FR').replace(/,/g, ' ') + " " + (window.schoolCurrency || "GNF");
                };

                // Override URL data with fresh DB data
                const dbTotal = data.totalFee || 0;
                const dbPaid = data.paidFee || 0;
                const dbDue = dbTotal - dbPaid;
                
                let dbPercent = 0;
                if (dbTotal > 0) {
                    dbPercent = (dbPaid / dbTotal) * 100;
                }

                updateFinancialUI(formatCurrency(dbTotal), formatCurrency(dbPaid), formatCurrency(dbDue), dbPercent);

                if (data.photoBase64) {
                    window.studentPhotoBase64 = data.photoBase64;
                    let photoContainer = document.getElementById('pdfStudentPhotoContainer');
                    if (!photoContainer) {
                        const placeholder = document.getElementById('pdfStudentPhotoPlaceholder');
                        if (placeholder) {
                            photoContainer = placeholder.parentElement;
                            photoContainer.id = 'pdfStudentPhotoContainer';
                        }
                    }
                    if (photoContainer) {
                        photoContainer.innerHTML = '<img src="data:image/jpeg;base64,' + data.photoBase64 + '" style="width:100%;height:100%;object-fit:cover;border-radius:8px;" crossorigin="anonymous">';
                    }
                }
                if (data.schoolLogo) {
                    window.schoolLogoBase64 = data.schoolLogo;
                    let logoContainer = document.getElementById('pdfSchoolLogoContainer');
                    if (!logoContainer) {
                        const placeholder = document.getElementById('pdfSchoolLogoInitial');
                        if (placeholder) {
                            logoContainer = placeholder.parentElement;
                            logoContainer.id = 'pdfSchoolLogoContainer';
                        }
                    }
                    if (logoContainer) {
                        logoContainer.innerHTML = '<img src="data:image/png;base64,' + data.schoolLogo + '" style="width:100%;height:100%;object-fit:contain;border-radius:50%;" crossorigin="anonymous">';
                    }
                }
                if (data.schoolName) {
                    document.getElementById('schoolName').textContent = data.schoolName;
                    document.getElementById('pdfSchoolName').textContent = data.schoolName;
                }
                if (data.schoolAddress) {
                    document.getElementById('pdfSchoolContact').textContent = data.schoolAddress;
                }
                if (data.currency) {
                    window.schoolCurrency = data.currency;
                }
                if (data.schoolYear) {
                    document.getElementById('schoolYear').textContent = data.schoolYear;
                }


                const currentTerm = document.getElementById('academicTerm').textContent;
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
