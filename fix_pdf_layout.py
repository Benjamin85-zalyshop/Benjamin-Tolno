import re

with open('public/index.html', 'r', encoding='utf-8') as f:
    content = f.read()

start_idx = content.find('<div id="pdfTemplate" style="display: none;">')
if start_idx == -1:
    print("Could not find start_idx")
    exit(1)

end_idx = content.find('<script type="module"', start_idx)
if end_idx == -1:
    print("Could not find end_idx")
    exit(1)

new_html = """<div id="pdfTemplate" style="display: none;">
        <div id="pdfContent" style="width: 800px; padding: 30px; font-family: 'Helvetica', 'Arial', sans-serif; background: #fff; box-sizing: border-box; color: #111827;">
            
            <!-- Header (Table Layout) -->
            <table style="width: 100%; background-color: #0047FF; color: white; padding: 20px; border-radius: 8px 8px 0 0; border-collapse: collapse;">
                <tr>
                    <td style="padding: 20px; vertical-align: middle; width: 60px;">
                        <div id="pdfSchoolLogoInitial" style="width: 60px; height: 60px; background: white; border-radius: 50%; display: inline-block; text-align: center; line-height: 60px; font-weight: bold; color: #0047FF; font-size: 24px;">Z</div>
                    </td>
                    <td style="padding: 20px; vertical-align: middle; text-align: left;">
                        <h1 id="pdfSchoolName" style="margin: 0; font-size: 24px; font-weight: bold;">ScolaPay</h1>
                        <p id="pdfSchoolContact" style="margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;">Conakry</p>
                    </td>
                    <td style="padding: 20px; vertical-align: middle; text-align: right;">
                        <h2 style="margin: 0; font-size: 20px; font-weight: bold;">BULLETIN DE NOTES</h2>
                        <p style="margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;" id="pdfTermInfo"></p>
                    </td>
                </tr>
            </table>

            <!-- Student Info (Table Layout) -->
            <table style="width: 100%; border: 1px solid #E5E7EB; border-top: none; border-radius: 0 0 8px 8px; padding: 20px; margin-bottom: 20px; background: #F9FAFB; border-collapse: collapse;">
                <tr>
                    <td style="padding: 20px; vertical-align: middle; width: 80px;">
                        <div style="width: 80px; height: 80px; background: #E5E7EB; border-radius: 8px; display: inline-block; text-align: center; line-height: 80px; color: #6B7280; font-size: 12px;">
                            <span id="pdfStudentPhotoPlaceholder">Photo</span>
                        </div>
                    </td>
                    <td style="padding: 20px; vertical-align: middle; text-align: left;">
                        <h2 id="pdfStudentName" style="margin: 0 0 8px 0; font-size: 20px; font-weight: bold; color: #111827; text-transform: uppercase;"></h2>
                        <p style="margin: 0 0 4px 0; font-size: 14px; color: #0047FF; font-weight: 600;">Matricule : #<span id="pdfStudentMat"></span></p>
                        <p style="margin: 0; font-size: 14px; color: #4B5563;">Section : <span id="pdfStudentSection"></span> &nbsp;|&nbsp; Classe : <span id="pdfStudentGrade"></span></p>
                    </td>
                    <td style="padding: 20px; vertical-align: middle; text-align: center; width: 100px;">
                        <div id="pdfQrCode" style="width: 70px; height: 70px; background: white; padding: 5px; border: 1px solid #E5E7EB; border-radius: 4px; display: inline-block;"></div>
                        <div style="font-size: 9px; color: #6B7280; margin-top: 4px;">VERIF. SCANNABLE</div>
                    </td>
                </tr>
            </table>

            <!-- Table -->
            <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px; border: 1px solid #E5E7EB; border-radius: 8px;">
                <thead>
                    <tr style="background: #0047FF; color: white;">
                        <th style="padding: 10px 16px; text-align: left; font-weight: 600; font-size: 13px; text-transform: uppercase; border: 1px solid #0047FF;">MATIÈRE</th>
                        <th style="padding: 10px 16px; text-align: center; font-weight: 600; font-size: 13px; text-transform: uppercase; border: 1px solid #0047FF;">COEFF</th>
                        <th style="padding: 10px 16px; text-align: center; font-weight: 600; font-size: 13px; text-transform: uppercase; border: 1px solid #0047FF;">NOTE (/10)</th>
                        <th style="padding: 10px 16px; text-align: left; font-weight: 600; font-size: 13px; text-transform: uppercase; border: 1px solid #0047FF;">APPRÉCIATION / REMARQUE</th>
                    </tr>
                </thead>
                <tbody id="pdfSubjectsTableBody">
                </tbody>
            </table>

            <!-- Summary (Table Layout) -->
            <table style="width: 100%; border: 1px solid #E5E7EB; border-radius: 8px; padding: 20px; background: #F9FAFB; margin-bottom: 40px; border-collapse: collapse;">
                <tr>
                    <td style="padding: 20px; vertical-align: middle; text-align: left; width: 33%;">
                        <h3 style="margin: 0 0 12px 0; font-size: 14px; font-weight: 600; color: #111827; text-transform: uppercase;">RÉCAPITULATIF</h3>
                        <p style="margin: 0 0 6px 0; font-size: 13px; color: #4B5563;">Total Coeff : <span id="pdfTotalCoeff">-</span></p>
                        <p style="margin: 0 0 6px 0; font-size: 13px; color: #4B5563;">Total Points : <span id="pdfTotalPoints">-</span></p>
                        <p style="margin: 0; font-size: 13px; color: #4B5563;">Moy. Classe : <span id="pdfClassAvg">-</span></p>
                    </td>
                    <td style="padding: 20px; vertical-align: middle; text-align: center; width: 33%; border-left: 1px solid #E5E7EB; border-right: 1px solid #E5E7EB;">
                        <h3 style="margin: 0 0 12px 0; font-size: 14px; font-weight: 600; color: #0047FF; text-transform: uppercase;">MOYENNE GÉNÉRALE</h3>
                        <div style="font-size: 32px; font-weight: bold; color: #0047FF; margin-bottom: 8px;" id="pdfAvg"></div>
                        <div style="display: inline-block; background: #0047FF; color: white; padding: 4px 12px; border-radius: 999px; font-size: 12px; font-weight: 600;" id="pdfAppreciation"></div>
                    </td>
                    <td style="padding: 20px; vertical-align: middle; text-align: center; width: 33%;">
                        <h3 style="margin: 0 0 12px 0; font-size: 14px; font-weight: 600; color: #111827; text-transform: uppercase;">RANG</h3>
                        <div style="font-size: 28px; font-weight: bold; color: #111827;" id="pdfRank"></div>
                    </td>
                </tr>
            </table>

            <!-- Signatures (Table Layout) -->
            <table style="width: 100%; border-collapse: collapse; margin-top: 40px;">
                <tr>
                    <td style="text-align: left; width: 50%; vertical-align: top;">
                        <p style="margin: 0; font-weight: 600; font-size: 14px; color: #111827;">Le Parent d'Élève</p>
                        <p style="margin: 4px 0 0 0; font-size: 12px; color: #6B7280;">(Signature)</p>
                    </td>
                    <td style="text-align: right; width: 50%; vertical-align: top;">
                        <p style="margin: 0; font-weight: 600; font-size: 14px; color: #111827;">Le Chef d'Établissement & Cachet</p>
                        <p style="margin: 4px 0 0 0; font-size: 12px; color: #6B7280;">(Signature et Sceau Officiel)</p>
                    </td>
                </tr>
            </table>

            <div style="text-align: center; margin-top: 50px; font-size: 11px; color: #9CA3AF; border-top: 1px solid #E5E7EB; padding-top: 20px;">
                Ce bulletin est un document officiel. Toute rature ou surcharge le rend nul. Généré par ScolaPay.
            </div>
        </div>
    </div>
    """

new_content = content[:start_idx] + new_html + content[end_idx:]

with open('public/index.html', 'w', encoding='utf-8') as f:
    f.write(new_content)

with open('scolapay-web/public/index.html', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("HTML Replaced successfully")
