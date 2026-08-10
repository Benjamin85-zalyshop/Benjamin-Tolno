window.downloadPdf = function() {
    const btn = document.getElementById('downloadPdfBtn');
    if (btn) btn.style.display = 'none';
    
    const urlParams = new URLSearchParams(window.location.search);
    
    // Fill the template
    const term = urlParams.get('term') || '1er Trimestre';
    document.getElementById('pdfTermInfo').textContent = `${term} • Année : 2025 - 2026`;
    
    document.getElementById('pdfStudentName').textContent = urlParams.get('name') || 'Élève';
    document.getElementById('pdfStudentMat').textContent = urlParams.get('mat') || 'N/A';
    document.getElementById('pdfStudentSection').textContent = urlParams.get('section') || 'LE PRIMAIRE';
    document.getElementById('pdfStudentGrade').textContent = urlParams.get('grade') || '2ème Année';
    
    // QR Code
    const qrData = encodeURIComponent(window.location.href);
    document.getElementById('pdfQrCode').innerHTML = `<img src="https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=${qrData}" alt="QR" style="width:100%;height:100%;">`;
    
    // Summary
    const avg = document.getElementById('academicAvg').textContent;
    document.getElementById('pdfAvg').textContent = avg + ' / 10';
    
    const rank = document.getElementById('academicRank').textContent;
    document.getElementById('pdfRank').textContent = rank + 'er sur 1 élèves';
    
    const apprec = document.getElementById('academicAppreciation');
    document.getElementById('pdfAppreciation').textContent = apprec ? apprec.textContent.replace('Appréciation : ', '') : 'Encouragements';
    
    // Subjects Table
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
                const coeff = 1; // Assuming 1 for now
                
                totalCoeff += coeff;
                if (!isNaN(note)) {
                    totalPoints += (note * coeff);
                }
                
                let mention = 'Assez bien';
                if (note >= 9) mention = 'Très bien';
                else if (note >= 7) mention = 'Bien';
                else if (note < 5) mention = 'Passable';
                
                const tr = document.createElement('tr');
                tr.style.borderBottom = '1px solid #E5E7EB';
                // Alternate row color
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
    document.getElementById('pdfClassAvg').textContent = avg + ' / 10';
    
    // Show template off-screen
    const template = document.getElementById('pdfTemplate');
    template.style.display = 'block';
    template.style.position = 'absolute';
    template.style.top = '-9999px';
    template.style.left = '-9999px';
    
    // Wait for QR image to load before capturing
    setTimeout(() => {
        const elementToCapture = document.getElementById('pdfContent');
        const opt = {
            margin:       [10, 0, 10, 0],
            filename:     'bulletin_de_notes.pdf',
            image:        { type: 'jpeg', quality: 0.98 },
            html2canvas:  { scale: 2, useCORS: true },
            jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
        };
        
        html2pdf().set(opt).from(elementToCapture).save().then(() => {
            if (btn) btn.style.display = 'block';
            template.style.display = 'none';
        }).catch(err => {
            console.error(err);
            if (btn) btn.style.display = 'block';
            template.style.display = 'none';
        });
    }, 1000); // 1s wait for images
};
