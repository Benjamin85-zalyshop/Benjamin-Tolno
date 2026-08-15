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
@DebugMetadata(c = "com.example.ui.SchoolViewModel$insertPayment$1", f = "SchoolViewModel.kt", i = {1, 1, 1, 1, 1, 1, 1}, l = {558, 583}, m = "invokeSuspend", n = {"student", "studentRemoteId", "db", "docRef", "remoteId", "paymentData", "date"}, s = {"L$0", "L$1", "L$2", "L$3", "L$4", "L$5", "J$0"})
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$insertPayment$1\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,2219:1\n1#2:2220\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$insertPayment$1.dex */
public final class SchoolViewModel$insertPayment$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ long $amount;
    final /* synthetic */ String $email;
    final /* synthetic */ String $paymentMethod;
    final /* synthetic */ String $reason;
    final /* synthetic */ int $schoolId;
    final /* synthetic */ int $studentId;
    long J$0;
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
    public SchoolViewModel$insertPayment$1(SchoolViewModel schoolViewModel, int i, int i2, String str, long j, String str2, String str3, Continuation<? super SchoolViewModel$insertPayment$1> continuation) {
        super(2, continuation);
        this.this$0 = schoolViewModel;
        this.$schoolId = i;
        this.$studentId = i2;
        this.$email = str;
        this.$amount = j;
        this.$reason = str2;
        this.$paymentMethod = str3;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$insertPayment$1(this.this$0, this.$schoolId, this.$studentId, this.$email, this.$amount, this.$reason, this.$paymentMethod, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x005f  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0079  */
    /* JADX WARN: Removed duplicated region for block: B:25:0x009c  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x0073 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r27) {
        /*
            Method dump skipped, instructions count: 448
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$insertPayment$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
