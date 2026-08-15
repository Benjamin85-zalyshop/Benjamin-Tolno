package com.example.ui;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u001a\u000e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0001¨\u0006\u0003"}, d2 = {"normalizeGradeName", "", "grade", "app_debug"}, k = 2, mv = {2, 2, 0}, xi = 48)
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModelKt.dex */
public final class SchoolViewModelKt {
    @NotNull
    public static final String normalizeGradeName(@NotNull String grade) {
        Intrinsics.checkNotNullParameter(grade, "grade");
        String trimmed = StringsKt.trim(grade).toString();
        if (StringsKt.equals(trimmed, "1ere Année", true) || StringsKt.equals(trimmed, "1ere annee", true) || StringsKt.equals(trimmed, "1ère annee", true)) {
            return "1ère Année";
        }
        if (StringsKt.equals(trimmed, "2eme Année", true) || StringsKt.equals(trimmed, "2eme annee", true) || StringsKt.equals(trimmed, "2ème annee", true)) {
            return "2ème Année";
        }
        if (StringsKt.equals(trimmed, "3eme Année", true) || StringsKt.equals(trimmed, "3eme annee", true) || StringsKt.equals(trimmed, "3ème annee", true)) {
            return "3ème Année";
        }
        if (StringsKt.equals(trimmed, "4eme Année", true) || StringsKt.equals(trimmed, "4eme annee", true) || StringsKt.equals(trimmed, "4ème annee", true)) {
            return "4ème Année";
        }
        if (StringsKt.equals(trimmed, "5eme Année", true) || StringsKt.equals(trimmed, "5eme annee", true) || StringsKt.equals(trimmed, "5ème annee", true)) {
            return "5ème Année";
        }
        if (StringsKt.equals(trimmed, "6eme Année", true) || StringsKt.equals(trimmed, "6eme annee", true) || StringsKt.equals(trimmed, "6ème annee", true)) {
            return "6ème Année";
        }
        if (StringsKt.equals(trimmed, "7eme Année", true) || StringsKt.equals(trimmed, "7eme annee", true) || StringsKt.equals(trimmed, "7ème annee", true)) {
            return "7ème Année";
        }
        if (StringsKt.equals(trimmed, "8eme Année", true) || StringsKt.equals(trimmed, "8eme annee", true) || StringsKt.equals(trimmed, "8ème annee", true)) {
            return "8ème Année";
        }
        if (StringsKt.equals(trimmed, "9eme Année", true) || StringsKt.equals(trimmed, "9eme annee", true) || StringsKt.equals(trimmed, "9ème annee", true)) {
            return "9ème Année";
        }
        if (StringsKt.equals(trimmed, "10eme Année", true) || StringsKt.equals(trimmed, "10eme annee", true) || StringsKt.equals(trimmed, "10ème annee", true)) {
            return "10ème Année";
        }
        if (StringsKt.equals(trimmed, "11eme Année", true) || StringsKt.equals(trimmed, "11eme annee", true) || StringsKt.equals(trimmed, "11ème annee", true)) {
            return "11ème Année";
        }
        if (StringsKt.equals(trimmed, "12eme Année", true) || StringsKt.equals(trimmed, "12eme annee", true) || StringsKt.equals(trimmed, "12ème annee", true)) {
            return "12ème Année";
        }
        return StringsKt.equals(trimmed, "petite section", true) ? "Petite Section" : StringsKt.equals(trimmed, "moyenne section", true) ? "Moyenne Section" : StringsKt.equals(trimmed, "grande section", true) ? "Grande Section" : trimmed;
    }
}
