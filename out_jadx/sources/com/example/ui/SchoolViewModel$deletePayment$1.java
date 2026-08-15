package com.example.ui;

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
@DebugMetadata(c = "com.example.ui.SchoolViewModel$deletePayment$1", f = "SchoolViewModel.kt", i = {1, 1}, l = {601, 612}, m = "invokeSuspend", n = {"paymentsList", "payment"}, s = {"L$0", "L$1"})
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$deletePayment$1\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,2219:1\n1#2:2220\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$deletePayment$1.dex */
final class SchoolViewModel$deletePayment$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ int $paymentId;
    Object L$0;
    Object L$1;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$deletePayment$1(SchoolViewModel schoolViewModel, String str, int i, Continuation<? super SchoolViewModel$deletePayment$1> continuation) {
        super(2, continuation);
        this.this$0 = schoolViewModel;
        this.$email = str;
        this.$paymentId = i;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$deletePayment$1(this.this$0, this.$email, this.$paymentId, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x005f  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0079  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x00d7 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:40:0x00d8  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x00db  */
    /* JADX WARN: Removed duplicated region for block: B:48:0x0073 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r11) {
        /*
            Method dump skipped, instructions count: 246
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$deletePayment$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
