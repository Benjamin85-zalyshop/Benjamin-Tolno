package com.example.ui;

import com.example.data.models.Student;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlinx.coroutines.CoroutineScope;
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$updateStudentPhoto$1", f = "SchoolViewModel.kt", i = {0}, l = {357}, m = "invokeSuspend", n = {"updated"}, s = {"L$0"})
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$updateStudentPhoto$1\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,2219:1\n1#2:2220\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$updateStudentPhoto$1.dex */
final class SchoolViewModel$updateStudentPhoto$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ String $photoBase64;
    final /* synthetic */ Student $student;
    Object L$0;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$updateStudentPhoto$1(Student student, String str, SchoolViewModel schoolViewModel, String str2, Continuation<? super SchoolViewModel$updateStudentPhoto$1> continuation) {
        super(2, continuation);
        this.$student = student;
        this.$photoBase64 = str;
        this.this$0 = schoolViewModel;
        this.$email = str2;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$updateStudentPhoto$1(this.$student, this.$photoBase64, this.this$0, this.$email, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:25:0x00c2  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00e3 A[Catch: Exception -> 0x0146, TryCatch #1 {Exception -> 0x0146, blocks: (B:11:0x005e, B:13:0x0068, B:19:0x0074, B:21:0x0081, B:23:0x00a1, B:26:0x00c3, B:28:0x00d7, B:34:0x00e3, B:35:0x00e8, B:37:0x00f6, B:42:0x010e, B:44:0x0119, B:45:0x013c, B:22:0x0097), top: B:71:0x005e }] */
    /* JADX WARN: Removed duplicated region for block: B:37:0x00f6 A[Catch: Exception -> 0x0146, TryCatch #1 {Exception -> 0x0146, blocks: (B:11:0x005e, B:13:0x0068, B:19:0x0074, B:21:0x0081, B:23:0x00a1, B:26:0x00c3, B:28:0x00d7, B:34:0x00e3, B:35:0x00e8, B:37:0x00f6, B:42:0x010e, B:44:0x0119, B:45:0x013c, B:22:0x0097), top: B:71:0x005e }] */
    /* JADX WARN: Removed duplicated region for block: B:51:0x0150  */
    /* JADX WARN: Removed duplicated region for block: B:57:0x015c  */
    /* JADX WARN: Removed duplicated region for block: B:61:0x0192  */
    /* JADX WARN: Removed duplicated region for block: B:62:0x0193  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r26) {
        /*
            Method dump skipped, instructions count: 432
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$updateStudentPhoto$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
