import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-app.js";
import { getDatabase, ref, onValue, set, update, get } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-database.js";
import { getAuth, signInAnonymously } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-auth.js";
import { getFirestore, doc, setDoc, increment } from "https://www.gstatic.com/firebasejs/10.8.1/firebase-firestore.js";

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

    // ChapChapPay Test Credentials provided by user
    const CHAPCHAP_TEST_API_KEY = "bb752fc80b1c0a8548cb15b0b570c911c01320a7efc35691f8381f75bb36ac85";
    const CHAPCHAP_TEST_HMAC_KEY = "a28318af8a78f0460c88657ef5592e6b";

    function sanitizeFirebaseKey(val) {
        return (val || "default").replace(/[.#$\[\]\/]/g, "_").trim();
    }

    // Payment state for parent
    const studentPaymentState = {
        rid: rid,
        studentName: studentName,
        studentGrade: studentGrade,
        schoolName: school,
        totalFee: 0,
        paidFee: 0,
        dueFee: 0,
        currency: "GNF",
        selectedMethod: "orange_money",
        isOnlinePaymentAllowed: true,
        schoolLockReason: ""
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
                    listenToSchoolStatus(data.schoolName);
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

    function listenToSchoolStatus(sName) {
        if (!sName) return;
        const schoolKey = sanitizeFirebaseKey(sName);
        const schoolRef = ref(database, 'schools/' + schoolKey);
        try {
            onValue(schoolRef, (snap) => {
                if (snap.exists()) {
                    const sData = snap.val();
                    studentPaymentState.isOnlinePaymentAllowed = (sData.onlinePaymentEnabled !== false && sData.isAppLocked !== true);
                    studentPaymentState.schoolLockReason = sData.lockReason || "";
                    studentPaymentState.schoolApiKey = (sData.chapchapApiKey || "").trim();
                    studentPaymentState.schoolMerchantPhone = (sData.merchantPhone || "").trim();
                    if (sData.email) studentPaymentState.schoolEmail = sData.email;
                } else {
                    studentPaymentState.isOnlinePaymentAllowed = true;
                }
                updateBlockedUI();
            }, (err) => {
                console.warn("RTDB school read notice:", err);
                studentPaymentState.isOnlinePaymentAllowed = true;
                updateBlockedUI();
            });
        } catch (e) {
            console.warn("listenToSchoolStatus error:", e);
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

        submitBtn.classList.add('hidden');
        loadingDiv.classList.remove('hidden');

        const orderId = "PAY_" + Date.now() + "_" + Math.floor(100 + Math.random() * 900);
        const studentDesc = `Frais Scolarité - ${studentPaymentState.studentName} (${studentPaymentState.schoolName})`;

        let chapchapPaymentUrl = null;
        const activeApiKey = (studentPaymentState.schoolApiKey && studentPaymentState.schoolApiKey.length > 5) 
            ? studentPaymentState.schoolApiKey 
            : CHAPCHAP_TEST_API_KEY;

        try {
            // Appel API ChapChapPay (Clé propre de l'école si renseignée, sinon clé test)
            try {
                const response = await fetch("https://chapchappay.com/api/ecommerce/create", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "CCP-Api-Key": activeApiKey
                    },
                    body: JSON.stringify({
                        amount: amount,
                        description: studentDesc,
                        order_id: orderId,
                        client_phone: rawPhone
                    })
                });

                if (response.ok) {
                    const resJson = await response.json();
                    if (resJson.payment_url) {
                        chapchapPaymentUrl = resJson.payment_url;
                        console.log("ChapChapPay Payment URL:", resJson.payment_url);
                    }
                }
            } catch (apiError) {
                console.warn("ChapChapPay direct web api notice (test sandbox active):", apiError);
            }

            // Enregistrement du paiement dans Firebase RTDB
            const now = new Date();
            const dateStr = now.toLocaleDateString('fr-FR') + ' ' + now.toLocaleTimeString('fr-FR', {hour: '2-digit', minute: '2-digit'});
            const operatorLabel = studentPaymentState.selectedMethod === 'orange_money' ? 'Orange Money' : 'MTN MoMo';

            if (studentPaymentState.rid) {
                const updatedPaid = (studentPaymentState.paidFee || 0) + amount;

                // 1. Mise à jour du solde élève
                try {
                    await update(ref(database, 'students/' + studentPaymentState.rid), {
                        paidFee: updatedPaid
                    });
                } catch (stErr) {
                    console.warn("RTDB student update permission warning:", stErr);
                }

                // 2. Création de l'enregistrement de reçu
                try {
                    const receiptRef = ref(database, `students/${studentPaymentState.rid}/payments/${orderId}`);
                    await set(receiptRef, {
                        amount: amount,
                        date: dateStr,
                        timestamp: Date.now(),
                        paymentMethod: `Paiement en ligne ChapChapPay (${operatorLabel})`,
                        operator: studentPaymentState.selectedMethod,
                        phoneNumber: rawPhone,
                        transactionId: orderId,
                        feeType: "Frais de Scolarité",
                        schoolName: studentPaymentState.schoolName,
                        studentName: studentPaymentState.studentName
                    });
                } catch (rcErr) {
                    console.warn("RTDB receipt creation permission warning:", rcErr);
                }

                // 3. Option B : Mise à jour de la comptabilité école et commission ScolaPay
                try {
                    const schoolKey = sanitizeFirebaseKey(studentPaymentState.schoolName);
                    const schoolRef = ref(database, 'schools/' + schoolKey);
                    let sData = {};
                    try {
                        const schoolSnap = await get(schoolRef);
                        if (schoolSnap.exists()) sData = schoolSnap.val();
                    } catch (snapErr) {
                        console.warn("RTDB school snapshot warning:", snapErr);
                    }

                    const newCount = (sData.onlinePaymentsCount || 0) + 1;
                    const newTotal = (sData.onlinePaymentsTotal || 0) + amount;
                    const newComm = (sData.unpaidCommission || 0) + 3000;

                    await update(schoolRef, {
                        schoolName: studentPaymentState.schoolName,
                        onlinePaymentsCount: newCount,
                        onlinePaymentsTotal: newTotal,
                        unpaidCommission: newComm,
                        lastPaymentDate: dateStr,
                        lastPaymentTimestamp: Date.now()
                    });

                    // Si une clé email existe, mettre à jour également sous la clé email
                    const schoolEmail = sData.email || studentPaymentState.schoolEmail;
                    if (schoolEmail) {
                        const emailKey = sanitizeFirebaseKey(schoolEmail);
                        if (emailKey !== schoolKey) {
                            try {
                                await update(ref(database, 'schools/' + emailKey), {
                                    schoolName: studentPaymentState.schoolName,
                                    email: schoolEmail,
                                    onlinePaymentsCount: newCount,
                                    onlinePaymentsTotal: newTotal,
                                    unpaidCommission: newComm,
                                    lastPaymentDate: dateStr,
                                    lastPaymentTimestamp: Date.now()
                                });
                            } catch (e) {}
                        }
                    }

                    // 4. Synchronisation directe dans Cloud Firestore
                    try {
                        const targetDocs = [];
                        if (schoolEmail) targetDocs.push(schoolEmail);
                        if (studentPaymentState.schoolName && !targetDocs.includes(studentPaymentState.schoolName)) {
                            targetDocs.push(studentPaymentState.schoolName);
                        }
                        for (const docId of targetDocs) {
                            await setDoc(doc(firestoreDb, "schools", docId), {
                                unpaidCommission: increment(3000),
                                onlinePaymentsCount: increment(1),
                                onlinePaymentsTotal: increment(amount),
                                lastPaymentDate: dateStr,
                                lastPaymentTimestamp: Date.now()
                            }, { merge: true });
                        }
                    } catch (fsErr) {
                        console.warn("Firestore sync warning from web portal:", fsErr);
                    }
                } catch (scErr) {
                    console.warn("RTDB school stats update warning:", scErr);
                }

                // Mise à jour de l'état local
                studentPaymentState.paidFee = updatedPaid;
                studentPaymentState.dueFee = Math.max(0, studentPaymentState.totalFee - updatedPaid);
            }

            // Affichage de l'écran de confirmation avec détails
            loadingDiv.classList.add('hidden');
            document.getElementById('paymentModalForm').classList.add('hidden');
            document.getElementById('paymentSuccessView').classList.remove('hidden');

            document.getElementById('successAmountText').textContent = formatCurrency(amount);
            document.getElementById('successTransId').textContent = orderId;
            document.getElementById('successDate').textContent = dateStr;
            document.getElementById('successOperator').textContent = operatorLabel + ` (${rawPhone})`;

            const gatewayContainer = document.getElementById('chapchapGatewayLinkContainer');
            const gatewayLink = document.getElementById('chapchapGatewayLink');
            if (chapchapPaymentUrl && gatewayContainer && gatewayLink) {
                gatewayLink.href = chapchapPaymentUrl;
                gatewayContainer.classList.remove('hidden');
            } else if (gatewayContainer) {
                gatewayContainer.classList.add('hidden');
            }

            // Actualisation dynamique de la jauge sur la page
            const updatedPercent = studentPaymentState.totalFee > 0 ? (studentPaymentState.paidFee / studentPaymentState.totalFee) * 100 : 100;
            updateFinancialUI(
                formatCurrency(studentPaymentState.totalFee),
                formatCurrency(studentPaymentState.paidFee),
                formatCurrency(studentPaymentState.dueFee),
                updatedPercent
            );

        } catch (err) {
            console.error("Erreur globale lors du traitement du paiement:", err);
            loadingDiv.classList.add('hidden');
            submitBtn.classList.remove('hidden');
            alert("Erreur lors du traitement du paiement : " + (err && err.message ? err.message : "Vérifiez vos paramètres réseau et réessayez."));
        }
    };

    // Initial check for school status
    listenToSchoolStatus(school);

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
