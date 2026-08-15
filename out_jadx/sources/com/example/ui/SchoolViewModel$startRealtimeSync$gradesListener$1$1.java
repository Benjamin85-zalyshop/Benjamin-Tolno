package com.example.ui;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.QuerySnapshot;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$startRealtimeSync$gradesListener$1$1", f = "SchoolViewModel.kt", i = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4}, l = {1145, 1149, 1151, 1166, 1182}, m = "invokeSuspend", n = {"change", "doc", "remoteId", "studentRemoteId", "subjectRemoteId", "term", "evalScoreVal", "evalScore", "examScoreVal", "examScore", "teacherComment", "studentId", "subjectId", "localStudentId", "change", "doc", "remoteId", "studentRemoteId", "subjectRemoteId", "term", "evalScoreVal", "evalScore", "examScoreVal", "examScore", "teacherComment", "studentId", "subjectId", "localStudentId", "change", "doc", "remoteId", "studentRemoteId", "subjectRemoteId", "term", "evalScoreVal", "evalScore", "examScoreVal", "examScore", "teacherComment", "existing", "studentId", "subjectId", "localStudentId", "change", "doc", "remoteId", "studentRemoteId", "subjectRemoteId", "term", "evalScoreVal", "evalScore", "examScoreVal", "examScore", "teacherComment", "existing", "studentId", "subjectId", "localStudentId", "change", "doc", "remoteId"}, s = {"L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "I$0", "I$1", "I$2", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "I$0", "I$1", "I$2", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "I$1", "I$2", "L$1", "L$2", "L$3", "L$4", "L$5", "L$6", "L$7", "L$8", "L$9", "L$10", "L$11", "L$12", "I$0", "I$1", "I$2", "L$1", "L$2", "L$3"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$gradesListener$1$1.dex */
public final class SchoolViewModel$startRealtimeSync$gradesListener$1$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ int $schoolId;
    final /* synthetic */ QuerySnapshot $snapshot;
    int I$0;
    int I$1;
    int I$2;
    Object L$0;
    Object L$1;
    Object L$10;
    Object L$11;
    Object L$12;
    Object L$2;
    Object L$3;
    Object L$4;
    Object L$5;
    Object L$6;
    Object L$7;
    Object L$8;
    Object L$9;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* compiled from: SchoolViewModel.kt */
    @Metadata(k = 3, mv = {2, 2, 0}, xi = 48)
    /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$gradesListener$1$1$WhenMappings.dex */
    public static final /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[DocumentChange.Type.values().length];
            try {
                iArr[DocumentChange.Type.ADDED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DocumentChange.Type.MODIFIED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DocumentChange.Type.REMOVED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            $EnumSwitchMapping$0 = iArr;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$startRealtimeSync$gradesListener$1$1(QuerySnapshot querySnapshot, SchoolViewModel schoolViewModel, int i, Continuation<? super SchoolViewModel$startRealtimeSync$gradesListener$1$1> continuation) {
        super(2, continuation);
        this.$snapshot = querySnapshot;
        this.this$0 = schoolViewModel;
        this.$schoolId = i;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$startRealtimeSync$gradesListener$1$1(this.$snapshot, this.this$0, this.$schoolId, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x018c  */
    /* JADX WARN: Removed duplicated region for block: B:73:0x0307  */
    /* JADX WARN: Removed duplicated region for block: B:74:0x030c  */
    /* JADX WARN: Removed duplicated region for block: B:80:0x0331  */
    /* JADX WARN: Removed duplicated region for block: B:86:0x0394  */
    /* JADX WARN: Removed duplicated region for block: B:91:0x044e  */
    /* JADX WARN: Removed duplicated region for block: B:96:0x0502  */
    /* JADX WARN: Removed duplicated region for block: B:97:0x050d  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:21:0x01f5 -> B:22:0x01fb). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:90:0x0444 -> B:12:0x0186). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:95:0x04f8 -> B:12:0x0186). Please submit an issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:96:0x0502 -> B:12:0x0186). Please submit an issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r39) {
        /*
            Method dump skipped, instructions count: 1322
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$startRealtimeSync$gradesListener$1$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
