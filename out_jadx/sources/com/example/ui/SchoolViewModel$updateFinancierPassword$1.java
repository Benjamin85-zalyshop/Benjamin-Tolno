package com.example.ui;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$updateFinancierPassword$1", f = "SchoolViewModel.kt", i = {0, 1, 2, 2, 2, 3, 3, 3}, l = {2072, 2076, 2079, 2083}, m = "invokeSuspend", n = {"db", "db", "db", "currentAcc", "updated", "db", "currentAcc", "updated"}, s = {"L$0", "L$0", "L$0", "L$1", "L$2", "L$0", "L$1", "L$2"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$updateFinancierPassword$1.dex */
final class SchoolViewModel$updateFinancierPassword$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ String $email;
    final /* synthetic */ String $newPassword;
    Object L$0;
    Object L$1;
    Object L$2;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$updateFinancierPassword$1(String str, String str2, SchoolViewModel schoolViewModel, Continuation<? super SchoolViewModel$updateFinancierPassword$1> continuation) {
        super(2, continuation);
        this.$email = str;
        this.$newPassword = str2;
        this.this$0 = schoolViewModel;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$updateFinancierPassword$1(this.$email, this.$newPassword, this.this$0, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x00a6 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:24:0x00ab  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x011a A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:31:0x011b  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r25) {
        /*
            Method dump skipped, instructions count: 304
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$updateFinancierPassword$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
