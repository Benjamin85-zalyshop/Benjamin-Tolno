package com.example.ui;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlinx.coroutines.CoroutineScope;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$saveGrade$1", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0, 0}, l = {1345}, m = "invokeSuspend", n = {"db", "allGrades", "existingGrade", "remoteId", "gradeData", "newGradeObj"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5"})
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$saveGrade$1\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,2219:1\n1#2:2220\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$saveGrade$1.dex */
public final class SchoolViewModel$saveGrade$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $comment;
    final /* synthetic */ String $email;
    final /* synthetic */ Float $evaluationScore;
    final /* synthetic */ Float $examScore;
    final /* synthetic */ int $schoolId;
    final /* synthetic */ int $studentId;
    final /* synthetic */ String $studentRemoteId;
    final /* synthetic */ int $subjectId;
    final /* synthetic */ String $subjectRemoteId;
    final /* synthetic */ String $term;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$saveGrade$1(SchoolViewModel schoolViewModel, String str, int i, String str2, int i2, String str3, String str4, Float f, Float f2, String str5, int i3, Continuation<? super SchoolViewModel$saveGrade$1> continuation) {
        super(2, continuation);
        this.this$0 = schoolViewModel;
        this.$email = str;
        this.$studentId = i;
        this.$studentRemoteId = str2;
        this.$subjectId = i2;
        this.$subjectRemoteId = str3;
        this.$term = str4;
        this.$evaluationScore = f;
        this.$examScore = f2;
        this.$comment = str5;
        this.$schoolId = i3;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$saveGrade$1(this.this$0, this.$email, this.$studentId, this.$studentRemoteId, this.$subjectId, this.$subjectRemoteId, this.$term, this.$evaluationScore, this.$examScore, this.$comment, this.$schoolId, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Code restructure failed: missing block: B:33:0x00a7, code lost:
        if (r9 == null) goto L58;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r27) {
        /*
            Method dump skipped, instructions count: 500
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$saveGrade$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
