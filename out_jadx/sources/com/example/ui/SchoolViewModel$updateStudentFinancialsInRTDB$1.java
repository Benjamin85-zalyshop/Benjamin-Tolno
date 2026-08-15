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
@DebugMetadata(c = "com.example.ui.SchoolViewModel$updateStudentFinancialsInRTDB$1", f = "SchoolViewModel.kt", i = {1}, l = {1843, 1844}, m = "invokeSuspend", n = {"student"}, s = {"L$0"})
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$updateStudentFinancialsInRTDB$1\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,2219:1\n1#2:2220\n774#3:2221\n865#3,2:2222\n*S KotlinDebug\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$updateStudentFinancialsInRTDB$1\n*L\n1844#1:2221\n1844#1:2222,2\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$updateStudentFinancialsInRTDB$1.dex */
public final class SchoolViewModel$updateStudentFinancialsInRTDB$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ int $schoolId;
    final /* synthetic */ int $studentId;
    Object L$0;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$updateStudentFinancialsInRTDB$1(SchoolViewModel schoolViewModel, int i, int i2, Continuation<? super SchoolViewModel$updateStudentFinancialsInRTDB$1> continuation) {
        super(2, continuation);
        this.this$0 = schoolViewModel;
        this.$schoolId = i;
        this.$studentId = i2;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$updateStudentFinancialsInRTDB$1(this.this$0, this.$schoolId, this.$studentId, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:111:0x00e6 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:114:0x0063 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:19:0x004f A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:28:0x0068 A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:30:0x006b A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:37:0x009b A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:46:0x00ce A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:52:0x00ed A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:53:0x00f2  */
    /* JADX WARN: Removed duplicated region for block: B:57:0x010a A[Catch: Exception -> 0x0026, LOOP:2: B:55:0x0104->B:57:0x010a, LOOP_END, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:66:0x012c A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:72:0x0195 A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:78:0x01a1 A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:81:0x01bb A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:87:0x01c7 A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:90:0x01da A[Catch: Exception -> 0x0026, TryCatch #0 {Exception -> 0x0026, blocks: (B:7:0x001a, B:34:0x0083, B:35:0x0095, B:37:0x009b, B:42:0x00ae, B:43:0x00b2, B:44:0x00c8, B:46:0x00ce, B:50:0x00e7, B:52:0x00ed, B:54:0x00f3, B:55:0x0104, B:57:0x010a, B:58:0x0118, B:60:0x0120, B:66:0x012c, B:68:0x0137, B:70:0x0153, B:72:0x0195, B:78:0x01a1, B:79:0x01aa, B:81:0x01bb, B:87:0x01c7, B:88:0x01cc, B:90:0x01da, B:95:0x01f5, B:97:0x0200, B:99:0x0226, B:69:0x014b, B:8:0x0020, B:16:0x0041, B:17:0x0049, B:19:0x004f, B:26:0x0064, B:28:0x0068, B:30:0x006b, B:13:0x002d), top: B:104:0x000b }] */
    /* JADX WARN: Removed duplicated region for block: B:98:0x0224  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r22) {
        /*
            Method dump skipped, instructions count: 576
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$updateStudentFinancialsInRTDB$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
