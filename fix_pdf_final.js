const fs = require('fs');

function updateFile(file) {
    let content = fs.readFileSync(file, 'utf-8');
    
    // We will use a regex to replace the entire window.downloadPdf = function() { ... };
    // But since it's hard to match the end, let's replace everything between window.downloadPdf = function() { and window.showQrBadge = function() {
    
    const startIndex = content.indexOf('window.downloadPdf = function() {');
    const endIndex = content.indexOf('window.showQrBadge = function() {');
    
    if (startIndex !== -1 && endIndex !== -1) {
        const newFunc = `window.downloadPdf = function() {
    const btn = document.getElementById('downloadPdfBtn');
    if (btn) btn.style.display = 'none';
    const urlParams = new URLSearchParams(window.location.search);
    const term = urlParams.get('term') || '1er Trimestre';
    const schoolName = urlParams.get('school') || 'ScolaPay';
    document.getElementById('pdfSchoolName').textContent = schoolName;
    document.getElementById('pdfSchoolLogoInitial').textContent = schoolName.charAt(0).toUpperCase();
    document.getElementById('pdfTermInfo').textContent = \`\${term} • Année : 2025 - 2026\`;
    document.getElementById('pdfStudentName').textContent = urlParams.get('name') || 'Élève';
    document.getElementById('pdfStudentMat').textContent = urlParams.get('mat') || 'N/A';
    document.getElementById('pdfStudentSection').textContent = urlParams.get('section') || 'LE PRIMAIRE';
    document.getElementById('pdfStudentGrade').textContent = urlParams.get('grade') || '2ème Année';
    
    const qrData = encodeURIComponent(window.location.href);
    document.getElementById('pdfQrCode').innerHTML = \`<img src="https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=\${qrData}" alt="QR" style="width:100%;height:100%;" crossorigin="anonymous">\`;
    
    const avg = document.getElementById('academicAvg').textContent;
    document.getElementById('pdfAvg').textContent = avg + ' / 10';
    const rank = document.getElementById('academicRank').textContent;
    document.getElementById('pdfRank').textContent = rank + 'er sur 1 élèves';
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
                tr.innerHTML = \`
                    <td style="padding: 10px 16px; font-weight: 500; font-size: 13px;">\${matName}</td>
                    <td style="padding: 10px 16px; text-align: center; color: #0047FF; font-weight: 600; font-size: 13px;">\${coeff}</td>
                    <td style="padding: 10px 16px; text-align: center; color: #0047FF; font-weight: 600; font-size: 13px;">\${!isNaN(note) ? note.toFixed(2) : '-'}</td>
                    <td style="padding: 10px 16px; color: #4B5563; font-size: 13px;">\${mention}</td>
                \`;
                pdfTbody.appendChild(tr);
            }
        });
    }
    document.getElementById('pdfTotalCoeff').textContent = totalCoeff;
    document.getElementById('pdfTotalPoints').textContent = totalPoints.toFixed(2);
    document.getElementById('pdfClassAvg').textContent = avg + ' / 10';
    
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
    template.style.zIndex = '999998'; // Just below overlay
    
    setTimeout(() => {
        const elementToCapture = document.getElementById('pdfContent');
        const opt = {
            margin: [10, 0, 10, 0],
            filename: 'bulletin_de_notes.pdf',
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: { scale: 2, useCORS: true, scrollY: 0, windowWidth: 1000 },
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
`;

        content = content.substring(0, startIndex) + newFunc + content.substring(endIndex);
        fs.writeFileSync(file, content);
        console.log("Updated " + file);
    } else {
        console.log("Could not find boundaries in " + file);
    }
}

updateFile('public/app.js');
updateFile('scolapay-web/public/app.js');
