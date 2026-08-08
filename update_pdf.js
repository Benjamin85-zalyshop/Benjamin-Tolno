window.downloadPdf = function() {
    const element = document.getElementById('academicSection');
    const btn = document.getElementById('downloadPdfBtn');
    
    if (btn) btn.style.display = 'none';
    
    // Save current scroll position and scroll to top to avoid html2canvas blank page bug
    const originalScroll = window.scrollY;
    window.scrollTo(0, 0);
    
    // Fix overflow-x which can break html2canvas capture
    const tableContainer = element.querySelector('div[style*="overflow-x"]');
    const oldOverflow = tableContainer ? tableContainer.style.overflowX : '';
    if (tableContainer) tableContainer.style.overflowX = 'visible';
    
    // Force white background
    const oldBg = element.style.backgroundColor;
    element.style.backgroundColor = '#ffffff';
    
    const opt = {
        margin:       10,
        filename:     'bulletin_de_notes.pdf',
        image:        { type: 'jpeg', quality: 0.98 },
        html2canvas:  { 
            scale: 2, 
            useCORS: true,
            scrollY: 0
        },
        jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };
    
    setTimeout(() => {
        html2pdf().set(opt).from(element).save().then(() => {
            if (btn) btn.style.display = 'block';
            if (tableContainer) tableContainer.style.overflowX = oldOverflow;
            element.style.backgroundColor = oldBg;
            window.scrollTo(0, originalScroll);
        }).catch(err => {
            console.error(err);
            if (btn) btn.style.display = 'block';
            if (tableContainer) tableContainer.style.overflowX = oldOverflow;
            element.style.backgroundColor = oldBg;
            window.scrollTo(0, originalScroll);
        });
    }, 300);
};
