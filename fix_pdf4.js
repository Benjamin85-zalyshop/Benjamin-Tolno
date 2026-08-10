const fs = require('fs');

function updateFile(file) {
    let content = fs.readFileSync(file, 'utf-8');
    
    const oldLogic = `    const template = document.getElementById('pdfTemplate');
    
    const originalScroll = window.scrollY;
    window.scrollTo(0, 0);
    
    template.style.display = 'block';
    template.style.position = 'absolute';
    template.style.top = '0px';
    template.style.left = '0px';
    template.style.zIndex = '-1000';
    
    setTimeout(() => {
        const elementToCapture = document.getElementById('pdfContent');
        const opt = {
            margin: [10, 0, 10, 0],
            filename: 'bulletin_de_notes.pdf',
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: { scale: 2, useCORS: true, scrollY: 0 },
            jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
        };
        html2pdf().set(opt).from(elementToCapture).save().then(() => {
            if (btn) btn.style.display = 'block';
            template.style.display = 'none';
            window.scrollTo(0, originalScroll);
        }).catch(err => {
            console.error(err);
            if (btn) btn.style.display = 'block';
            template.style.display = 'none';
            window.scrollTo(0, originalScroll);
        });
    }, 1000);`;

    const newLogic = `    const template = document.getElementById('pdfTemplate');
    
    const originalScroll = window.scrollY;
    window.scrollTo(0, 0);
    
    // We will place it visibly but offscreen to the right, so it has proper dimensions
    template.style.display = 'block';
    template.style.position = 'absolute';
    template.style.top = '0px';
    template.style.left = '2000px'; 
    template.style.width = '800px';
    
    setTimeout(() => {
        const elementToCapture = document.getElementById('pdfContent');
        const opt = {
            margin: [10, 0, 10, 0],
            filename: 'bulletin_de_notes.pdf',
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: { scale: 2, useCORS: true, scrollY: 0, windowWidth: 1200 },
            jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
        };
        html2pdf().set(opt).from(elementToCapture).save().then(() => {
            if (btn) btn.style.display = 'block';
            template.style.display = 'none';
            window.scrollTo(0, originalScroll);
        }).catch(err => {
            console.error(err);
            if (btn) btn.style.display = 'block';
            template.style.display = 'none';
            window.scrollTo(0, originalScroll);
        });
    }, 1000);`;

    if (content.includes(oldLogic)) {
        content = content.replace(oldLogic, newLogic);
        fs.writeFileSync(file, content);
        console.log("Updated " + file);
    } else {
        console.log("Could not find old logic in " + file);
    }
}

updateFile('public/app.js');
updateFile('scolapay-web/public/app.js');
