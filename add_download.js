window.downloadPdf = function() {
    const element = document.getElementById('academicSection');
    const btn = document.getElementById('downloadPdfBtn');
    if (btn) btn.style.display = 'none';
    const opt = {
        margin: [0.5, 0.5, 0.5, 0.5],
        filename: 'bulletin_de_notes.pdf',
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2 },
        jsPDF: { unit: 'in', format: 'a4', orientation: 'portrait' }
    };
    html2pdf().set(opt).from(element).save().then(() => {
        if (btn) btn.style.display = 'block';
    });
};
