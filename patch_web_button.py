import re

with open('public/index.html', 'r') as f:
    content = f.read()

# We will move the download button outside of subjectsContainer
old_html = """                    <div style="margin-top: 1rem;">
                        <button id="downloadPdfBtn" class="btn btn-outline" style="width: 100%; border-color: var(--primary); color: var(--primary);" onclick="window.downloadPdf()">
                            Télécharger le bulletin (PDF)
                        </button>
                    </div>
                </div>"""

new_html = """                </div>
                <div style="margin-top: 1rem;">
                    <button id="downloadPdfBtn" class="btn btn-outline" style="width: 100%; border-color: var(--primary); color: var(--primary);" onclick="window.downloadPdf()">
                        Télécharger le bulletin (PDF)
                    </button>
                </div>"""

content = content.replace(old_html, new_html)

with open('public/index.html', 'w') as f:
    f.write(content)

print("Button moved!")
