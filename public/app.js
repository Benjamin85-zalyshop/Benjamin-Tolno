import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-app.js";
import { getDatabase, ref, onValue, set, update, get } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-database.js";
import { getAuth, signInAnonymously } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-auth.js";
import { getFirestore, doc, setDoc, increment, collection, query, where, getDocs, getDoc } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-firestore.js";

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
const auth = getAuth(app);
const firestoreDb = getFirestore(app);

// Authenticate anonymously so parents can read/write without credentials
signInAnonymously(auth).catch(err => {
    console.warn("Auth anonyme notice:", err.message);
});

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
                const noteStr = cells[2].textContent.replace(',', '.').trim();
                const note = noteStr === '-' ? NaN : parseFloat(noteStr);
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
    let urlParams = new URLSearchParams(window.location.search);
    
    // 1. Sauvegarde systématique de la session élève lorsqu'un QR code est scanné
    if (urlParams.has('name') || urlParams.has('mat') || urlParams.has('id')) {
        try {
            sessionStorage.setItem('scolapay_last_query', window.location.search);
            localStorage.setItem('scolapay_last_query', window.location.search);
        } catch (e) {}
    } else {
        // 2. Si on arrive sur /paiement ou racine sans paramètres, restaurer le dernier élève scanné
        const savedQuery = sessionStorage.getItem('scolapay_last_query') || localStorage.getItem('scolapay_last_query');
        if (savedQuery && savedQuery.length > 3) {
            const hasPaymentIntent = window.location.pathname.includes('paiement') || urlParams.has('open_payment');
            const targetParams = savedQuery.replace(/^\?/, '') + (hasPaymentIntent ? '&open_payment=true' : '');
            window.location.replace('/?' + targetParams);
            return;
        }

        // Si aucun élève n'a été scanné dans le navigateur
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
    const schoolEmailParam = urlParams.get('email') || urlParams.get('schoolEmail') || '';
    const year = urlParams.get('year') || 'Portail Parent';

    // Remplir les informations de l'école
    document.getElementById('schoolName').textContent = school;
    document.getElementById('schoolYear').textContent = year;
    document.getElementById('welcomeText').textContent = `Bonjour, Parent de ${studentName} (${studentGrade})`;

    // Format currency helper
    const formatCurrency = (num) => {
        if (num === null || num === undefined) return "0 " + (window.schoolCurrency || "GNF");
        return Number(num).toLocaleString('fr-FR').replace(/,/g, ' ') + " " + (window.schoolCurrency || "GNF");
    };

    function sanitizeFirebaseKey(val) {
        return (val || "default").replace(/[.#$\[\]\/]/g, "_").trim();
    }

    // Payment state for parent
    const studentPaymentState = {
        rid: rid,
        studentName: studentName,
        studentGrade: studentGrade,
        schoolName: school,
        schoolEmail: schoolEmailParam,
        totalFee: 0,
        paidFee: 0,
        dueFee: 0,
        currency: "GNF",
        selectedMethod: "orange_money",
        isOnlinePaymentAllowed: true,
        schoolLockReason: "",
        schoolMerchantPhone: "",
        schoolMerchantCode: ""
    };

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
        const studentRef = ref(database, 'students/' + rid);
        onValue(studentRef, (snapshot) => {
            if (snapshot.exists()) {
                const data = snapshot.val();
                                const formatCurrency = (num) => {
                    if (num === null || num === undefined) return "0 " + (window.schoolCurrency || "GNF");
                    return Number(num).toLocaleString('fr-FR').replace(/,/g, ' ') + " " + (window.schoolCurrency || "GNF");
                };

                // Override URL data with fresh DB data
                const dbTotal = data.totalFee || 0;
                const dbPaid = data.paidFee || 0;
                const dbDue = dbTotal - dbPaid;
                
                studentPaymentState.totalFee = dbTotal;
                studentPaymentState.paidFee = dbPaid;
                studentPaymentState.dueFee = Math.max(0, dbDue);
                if (data.studentName) studentPaymentState.studentName = data.studentName;
                if (data.grade) studentPaymentState.studentGrade = data.grade;
                if (data.schoolEmail) {
                    studentPaymentState.schoolEmail = data.schoolEmail;
                }
                if (data.schoolName) {
                    studentPaymentState.schoolName = data.schoolName;
                    listenToSchoolStatus(data.schoolName, data.schoolEmail);
                } else if (data.schoolEmail) {
                    listenToSchoolStatus("", data.schoolEmail);
                }
                
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
                if (data.logoBase64) {
                    window.schoolLogoBase64 = data.logoBase64;
                    let logoContainer = document.getElementById('pdfSchoolLogoContainer');
                    if (!logoContainer) {
                        const placeholder = document.getElementById('pdfSchoolLogoInitial');
                        if (placeholder) {
                            logoContainer = placeholder.parentElement;
                            logoContainer.id = 'pdfSchoolLogoContainer';
                        }
                    }
                    if (logoContainer) {
                        logoContainer.innerHTML = '<img src="data:image/png;base64,' + data.logoBase64 + '" style="width:100%;height:100%;object-fit:contain;border-radius:50%;" crossorigin="anonymous">';
                    }
                }
                if (data.schoolName) {
                    document.getElementById('schoolName').textContent = data.schoolName;
                    document.getElementById('pdfSchoolName').textContent = data.schoolName;
                }
                if (data.schoolAddress) {
                    document.getElementById('pdfSchoolContact').textContent = data.schoolAddress;
                } else if (data.schoolName) {
                    // Fallback to old contact string from Android if needed, but Android now sends schoolAddress
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
                            tdEval.textContent = subjData['Eval'] || '-';
                            
                            const tdAvg = document.createElement('td');
                            tdAvg.style.padding = "0.75rem";
                            tdAvg.style.fontWeight = "600";
                            tdAvg.textContent = subjData['Moy'] || '-';
                            
                            const avgScore = parseFloat((subjData['Moy'] || '').replace(',', '.'));
                            if (!isNaN(avgScore) && avgScore < maxScore / 2) {
                                tdAvg.style.color = "var(--danger)";
                            }
                            
                            const evalScore = parseFloat((subjData['Eval'] || '').replace(',', '.'));
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

    // ==========================================
    // GESTION DU PAIEMENT EN LIGNE (CHAPCHAPPAY)
    // & CONTRÔLE SUSPENSION ÉCOLE (KILL-SWITCH)
    // ==========================================

    function listenToSchoolStatus(sName, sEmail) {
        if (!sName && !sEmail) return;
        const targetEmail = sEmail || studentPaymentState.schoolEmail;
        if (sName) {
            const schoolKey = sanitizeFirebaseKey(sName);
            const schoolRef = ref(database, 'schools/' + schoolKey);
            try {
                onValue(schoolRef, (snap) => {
                    if (snap.exists()) {
                        const sData = snap.val();
                        studentPaymentState.isOnlinePaymentAllowed = (sData.onlinePaymentEnabled !== false && sData.isAppLocked !== true);
                        studentPaymentState.schoolLockReason = sData.lockReason || "";
                        if (sData.chapchapApiKey && sData.chapchapApiKey.trim().length > 5) {
                            studentPaymentState.schoolApiKey = sData.chapchapApiKey.trim();
                        }
                        if (sData.merchantPhone) {
                            studentPaymentState.schoolMerchantPhone = sData.merchantPhone.trim();
                        }
                        if (sData.merchantCode) {
                            studentPaymentState.schoolMerchantCode = sData.merchantCode.trim();
                        }
                        if (sData.email) studentPaymentState.schoolEmail = sData.email;
                    }
                    updateBlockedUI();
                }, (err) => {
                    console.warn("RTDB school read notice:", err);
                    updateBlockedUI();
                });
            } catch (e) {
                console.warn("listenToSchoolStatus error:", e);
            }
        }

        if (targetEmail) {
            const emailKey = sanitizeFirebaseKey(targetEmail);
            const emailRef = ref(database, 'schools/' + emailKey);
            try {
                onValue(emailRef, (snap) => {
                    if (snap.exists()) {
                        const sData = snap.val();
                        if (sData.onlinePaymentEnabled !== undefined || sData.isAppLocked !== undefined) {
                            studentPaymentState.isOnlinePaymentAllowed = (sData.onlinePaymentEnabled !== false && sData.isAppLocked !== true);
                            studentPaymentState.schoolLockReason = sData.lockReason || "";
                        }
                        if (sData.chapchapApiKey && sData.chapchapApiKey.trim().length > 5) {
                            studentPaymentState.schoolApiKey = sData.chapchapApiKey.trim();
                        }
                        if (sData.merchantPhone) {
                            studentPaymentState.schoolMerchantPhone = sData.merchantPhone.trim();
                        }
                        if (sData.merchantCode) {
                            studentPaymentState.schoolMerchantCode = sData.merchantCode.trim();
                        }
                    }
                    updateBlockedUI();
                }, (err) => {
                    console.warn("RTDB emailKey read notice:", err);
                });
            } catch (e) {
                console.warn("RTDB emailKey error:", e);
            }

            try {
                getDoc(doc(firestoreDb, "schools", targetEmail)).then(dSnap => {
                    if (dSnap.exists()) {
                        const fData = dSnap.data();
                        if (fData.onlinePaymentEnabled === false || fData.isAppLocked === true) {
                            studentPaymentState.isOnlinePaymentAllowed = false;
                            studentPaymentState.schoolLockReason = fData.lockReason || "";
                            updateBlockedUI();
                        }
                        if (fData.chapchapApiKey && fData.chapchapApiKey.trim().length > 5) {
                            studentPaymentState.schoolApiKey = fData.chapchapApiKey.trim();
                        }
                        if (fData.merchantPhone) {
                            studentPaymentState.schoolMerchantPhone = fData.merchantPhone.trim();
                        }
                        if (fData.merchantCode) {
                            studentPaymentState.schoolMerchantCode = fData.merchantCode.trim();
                        }
                    }
                }).catch(e => console.warn("Firestore school doc lookup notice:", e));
            } catch (e) {}
        }

        // Si schoolEmail est encore vide, recherche dans Firestore par displayName
        if (!targetEmail && sName && sName !== 'ScolaPay') {
            try {
                const q = query(collection(firestoreDb, "schools"), where("displayName", "==", sName));
                getDocs(q).then(qSnap => {
                    if (!qSnap.empty) {
                        studentPaymentState.schoolEmail = qSnap.docs[0].id;
                        const fData = qSnap.docs[0].data();
                        if (fData.onlinePaymentEnabled === false || fData.isAppLocked === true) {
                            studentPaymentState.isOnlinePaymentAllowed = false;
                            studentPaymentState.schoolLockReason = fData.lockReason || "";
                            updateBlockedUI();
                        }
                        if (fData.chapchapApiKey && fData.chapchapApiKey.trim().length > 5) {
                            studentPaymentState.schoolApiKey = fData.chapchapApiKey.trim();
                        }
                        if (fData.merchantPhone) {
                            studentPaymentState.schoolMerchantPhone = fData.merchantPhone.trim();
                        }
                        if (fData.merchantCode) {
                            studentPaymentState.schoolMerchantCode = fData.merchantCode.trim();
                        }
                    }
                }).catch(e => console.warn("Firestore school lookup notice:", e));
            } catch (e) {}
        }
    }

    function updateBlockedUI() {
        const alertBox = document.getElementById('schoolBlockedAlert');
        const fieldsBox = document.getElementById('paymentFieldsContainer');
        const reasonText = document.getElementById('schoolBlockedMessage');
        
        if (!alertBox || !fieldsBox) return;

        if (!studentPaymentState.isOnlinePaymentAllowed) {
            alertBox.classList.remove('hidden');
            fieldsBox.classList.add('hidden');
            if (studentPaymentState.schoolLockReason) {
                reasonText.textContent = studentPaymentState.schoolLockReason;
            } else {
                reasonText.textContent = "Le service de paiement en ligne pour cet établissement est actuellement indisponible. Merci de vous rapprocher de la direction de l'école pour effectuer votre règlement.";
            }
        } else {
            alertBox.classList.add('hidden');
            fieldsBox.classList.remove('hidden');
        }
    }

    window.openPaymentModal = function() {
        const modal = document.getElementById('paymentModal');
        if (!modal) return;

        // Reset views
        document.getElementById('paymentModalForm').classList.remove('hidden');
        document.getElementById('paymentSuccessView').classList.add('hidden');
        const unavailView = document.getElementById('paymentUnavailableView');
        if (unavailView) unavailView.classList.add('hidden');
        const merchantView = document.getElementById('paymentMerchantInstructionsView');
        if (merchantView) merchantView.classList.add('hidden');
        document.getElementById('paymentLoading').classList.add('hidden');
        document.getElementById('submitPayBtn').classList.remove('hidden');

        // Check if school is blocked
        updateBlockedUI();

        // Populate student info
        document.getElementById('modalStudentName').textContent = studentPaymentState.studentName || 'Élève';
        document.getElementById('modalStudentGrade').textContent = studentPaymentState.studentGrade || '';
        document.getElementById('modalSchoolName').textContent = studentPaymentState.schoolName || 'ScolaPay';
        document.getElementById('modalDueAmount').textContent = formatCurrency(studentPaymentState.dueFee);
        document.getElementById('modalCurrency').textContent = studentPaymentState.currency;

        const defaultPay = studentPaymentState.dueFee > 0 ? studentPaymentState.dueFee : 50000;
        document.getElementById('payAmountInput').value = defaultPay;
        updatePayButtonLabel(defaultPay);

        // Listen for input changes
        document.getElementById('payAmountInput').oninput = function() {
            const val = parseFloat(this.value) || 0;
            updatePayButtonLabel(val);
        };

        modal.classList.remove('hidden');
    };

    window.closePaymentModal = function() {
        const modal = document.getElementById('paymentModal');
        if (modal) modal.classList.add('hidden');
    };

    window.copyShortCode = function() {
        const el = document.getElementById('merchantShortCodeText');
        if (!el) return;
        const text = el.textContent.trim();
        const showSuccess = () => {
            const toast = document.getElementById('copyShortToast');
            if (toast) {
                toast.style.display = 'block';
                setTimeout(() => { toast.style.display = 'none'; }, 3000);
            }
        };
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(showSuccess).catch(() => fallbackCopy(text, showSuccess));
        } else {
            fallbackCopy(text, showSuccess);
        }
    };

    window.copyAmountNumber = function() {
        const el = document.getElementById('merchantTransferAmount');
        if (!el) return;
        const text = el.textContent.replace(/[^0-9]/g, '');
        const showSuccess = () => {
            const toast = document.getElementById('copyToast');
            if (toast) {
                toast.style.display = 'block';
                setTimeout(() => { toast.style.display = 'none'; }, 3000);
            }
        };
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(showSuccess).catch(() => fallbackCopy(text, showSuccess));
        } else {
            fallbackCopy(text, showSuccess);
        }
    };

    window.copyMerchantNumber = function() {
        const phoneEl = document.getElementById('merchantPhoneNumber');
        if (!phoneEl) return;
        const text = phoneEl.textContent.replace(/\s+/g, '');
        const showSuccess = () => {
            const toast = document.getElementById('copyToast');
            if (toast) {
                toast.textContent = "✓ Numéro marchand copié !";
                toast.style.display = 'block';
                setTimeout(() => { toast.style.display = 'none'; }, 3000);
            }
        };

        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(showSuccess).catch(() => {
                fallbackCopy(text, showSuccess);
            });
        } else {
            fallbackCopy(text, showSuccess);
        }
    };

    window.copyMerchantCode = function() {
        const codeEl = document.getElementById('merchantCodeDisplay');
        if (!codeEl) return;
        const text = codeEl.textContent.trim();
        const showSuccess = () => {
            const toast = document.getElementById('copyToast');
            if (toast) {
                toast.textContent = "✓ Code marchand copié !";
                toast.style.display = 'block';
                setTimeout(() => { toast.style.display = 'none'; }, 3000);
            }
        };

        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(showSuccess).catch(() => {
                fallbackCopy(text, showSuccess);
            });
        } else {
            fallbackCopy(text, showSuccess);
        }
    };

    function fallbackCopy(text, onSuccess) {
        const temp = document.createElement('input');
        temp.value = text;
        document.body.appendChild(temp);
        temp.select();
        try {
            document.execCommand('copy');
            if (onSuccess) onSuccess();
        } catch (e) {}
        document.body.removeChild(temp);
    }

    window.selectPaymentMethod = function(method) {
        studentPaymentState.selectedMethod = method;
        const optOrange = document.getElementById('optOrange');
        const optMtn = document.getElementById('optMtn');
        if (method === 'orange_money') {
            optOrange.classList.add('selected');
            optMtn.classList.remove('selected');
        } else {
            optOrange.classList.remove('selected');
            optMtn.classList.add('selected');
        }
    };

    window.setPaymentAmount = function(ratio) {
        const base = studentPaymentState.dueFee > 0 ? studentPaymentState.dueFee : 100000;
        const calculated = Math.round(base * ratio);
        const input = document.getElementById('payAmountInput');
        if (input) {
            input.value = calculated;
            updatePayButtonLabel(calculated);
        }
    };

    function updatePayButtonLabel(amount) {
        const btnText = document.getElementById('payBtnAmount');
        if (btnText) {
            btnText.textContent = formatCurrency(amount);
        }
    }

    window.executeOnlinePayment = async function() {
        const payAmountInput = document.getElementById('payAmountInput');
        const payPhoneInput = document.getElementById('payPhoneInput');
        const submitBtn = document.getElementById('submitPayBtn');
        const loadingDiv = document.getElementById('paymentLoading');

        const amount = parseFloat(payAmountInput.value);
        const rawPhone = (payPhoneInput.value || "").trim().replace(/\s+/g, '');

        if (isNaN(amount) || amount <= 0) {
            alert("Veuillez indiquer un montant valide à payer.");
            payAmountInput.focus();
            return;
        }

        if (rawPhone.length < 9) {
            alert("Veuillez renseigner un numéro de téléphone valide (ex: 622 12 34 56).");
            payPhoneInput.focus();
            return;
        }

        if (!studentPaymentState.isOnlinePaymentAllowed) {
            updateBlockedUI();
            return;
        }

        // Vérification de la configuration marchand Orange Money de l'école
        const hasMerchantConfig = Boolean(
            (studentPaymentState.schoolMerchantCode && studentPaymentState.schoolMerchantCode.trim().length > 0) ||
            (studentPaymentState.schoolMerchantPhone && studentPaymentState.schoolMerchantPhone.trim().length > 0)
        );

        if (!hasMerchantConfig) {
            // L'école n'a pas encore configuré son code marchand ou son numéro marchand
            document.getElementById('paymentModalForm').classList.add('hidden');
            const unavailView = document.getElementById('paymentUnavailableView');
            if (unavailView) {
                const sName = studentPaymentState.schoolName || 'cet établissement';
                const stName = studentPaymentState.studentName || 'votre enfant';
                const sNameEl = document.getElementById('unavailableSchoolName');
                const stNameEl = document.getElementById('unavailableStudentName');
                if (sNameEl) sNameEl.textContent = sName;
                if (stNameEl) stNameEl.textContent = stName;
                unavailView.classList.remove('hidden');
            } else {
                alert("Le paiement Orange Money n'est pas encore configuré pour l'établissement " + (studentPaymentState.schoolName || "") + ".\nL'école n'a pas encore renseigné son code marchand Orange Money.");
            }
            return;
        }

        try {
            // Afficher immédiatement l'écran dédié Orange Money Marchand avec la syntaxe exacte
            document.getElementById('paymentModalForm').classList.add('hidden');

            const merchantView = document.getElementById('paymentMerchantInstructionsView');
            if (merchantView) {
                const sName = studentPaymentState.schoolName || 'cet établissement';
                const stName = studentPaymentState.studentName || 'l\'élève';
                const mCode = (studentPaymentState.schoolMerchantCode && studentPaymentState.schoolMerchantCode.trim().length > 0)
                    ? studentPaymentState.schoolMerchantCode.trim()
                    : '346789';
                const mPhone = (studentPaymentState.schoolMerchantPhone && studentPaymentState.schoolMerchantPhone.trim().length > 0)
                    ? studentPaymentState.schoolMerchantPhone.trim()
                    : '660377887';
                const formattedAmt = formatCurrency(amount);

                const elStName = document.getElementById('merchantStudentName');
                const elScName = document.getElementById('merchantSchoolName');
                const elPhone = document.getElementById('merchantPhoneNumber');
                const elCode = document.getElementById('merchantCodeDisplay');
                const elAmt = document.getElementById('merchantTransferAmount');

                if (elStName) elStName.textContent = stName;
                if (elScName) elScName.textContent = sName;
                if (elPhone) elPhone.textContent = mPhone;
                if (elCode) elCode.textContent = mCode;
                if (elAmt) elAmt.textContent = formattedAmt;

                const badge = document.getElementById('merchantOperatorBadge');
                const ussdBtn = document.getElementById('merchantUssdCallBtn');
                const ussdBtnText = document.getElementById('merchantUssdBtnText');
                const shortCodeText = document.getElementById('merchantShortCodeText');
                const gradeLabel = document.getElementById('merchantStudentGradeLabel');
                if (gradeLabel) gradeLabel.textContent = studentPaymentState.studentGrade || '';

                const cleanMCode = mCode.replace(/[^0-9]/g, '');
                const cleanAmount = Math.round(amount);

                if (badge) {
                    badge.textContent = 'Orange Money';
                    badge.style.background = '#FF7900';
                    badge.style.color = 'white';
                }

                // Formule officielle Orange Money Guinée : *144*6*code_Marchand*montant*code_secret#
                const fullShortCode = `*144*6*${cleanMCode}*${cleanAmount}#`;
                if (shortCodeText) shortCodeText.textContent = fullShortCode;
                if (ussdBtn) ussdBtn.href = `tel:${encodeURIComponent(fullShortCode)}`;
                if (ussdBtnText) ussdBtnText.textContent = `Composer ${fullShortCode}`;

                // Bouton WhatsApp prérempli
                const waBtn = document.getElementById('merchantWhatsAppBtn');
                if (waBtn) {
                    const cleanPhone = mPhone.replace(/[^0-9]/g, '');
                    const waPhone = cleanPhone.startsWith('224') ? cleanPhone : '224' + cleanPhone;
                    const gradeInfo = studentPaymentState.studentGrade ? ` (${studentPaymentState.studentGrade})` : '';
                    const messageText = `Bonjour, je viens d'effectuer le paiement des frais de scolarité pour l'élève ${stName}${gradeInfo}.\nMontant : ${formattedAmt}\nCode Marchand Orange Money : ${mCode}\nNuméro Orange Money : ${mPhone}\nMerci de bien vouloir valider et me délivrer le reçu officiel.`;
                    waBtn.href = `https://wa.me/${waPhone}?text=${encodeURIComponent(messageText)}`;
                }

                merchantView.classList.remove('hidden');
            }
            return;

        } catch (err) {
            console.error("Erreur globale lors du traitement du paiement:", err);
            loadingDiv.classList.add('hidden');
            submitBtn.classList.remove('hidden');
            alert("Erreur lors du traitement du paiement : " + (err && err.message ? err.message : "Vérifiez vos paramètres réseau et réessayez."));
        }
    };

    // Initial check for school status
    listenToSchoolStatus(school, schoolEmailParam);

    // Afficher le contenu
    setTimeout(() => {
        document.getElementById('loading').classList.add('hidden');
        document.getElementById('mainContent').classList.remove('hidden');

        // Si l'utilisateur est arrivé pour payer (ex: redirection /paiement ou open_payment=true)
        if (window.location.pathname.includes('paiement') || urlParams.has('open_payment')) {
            setTimeout(() => {
                if (typeof window.openPaymentModal === 'function') {
                    window.openPaymentModal();
                }
            }, 300);
        }
    }, 500);
});
