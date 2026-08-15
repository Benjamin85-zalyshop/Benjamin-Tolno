package com.example.ui;

import kotlin.Metadata;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SchoolViewModel.kt */
@Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
@DebugMetadata(c = "com.example.ui.SchoolViewModel$checkPendingPaymentStatus$1", f = "SchoolViewModel.kt", i = {1}, l = {2022, 2029}, m = "invokeSuspend", n = {"status"}, s = {"L$0"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$checkPendingPaymentStatus$1.dex */
public final class SchoolViewModel$checkPendingPaymentStatus$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Function1<String, Unit> $onResult;
    final /* synthetic */ String $orderId;
    Object L$0;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    /* JADX WARN: Multi-variable type inference failed */
    public SchoolViewModel$checkPendingPaymentStatus$1(String str, SchoolViewModel schoolViewModel, Function1<? super String, Unit> function1, Continuation<? super SchoolViewModel$checkPendingPaymentStatus$1> continuation) {
        super(2, continuation);
        this.$orderId = str;
        this.this$0 = schoolViewModel;
        this.$onResult = function1;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$checkPendingPaymentStatus$1(this.$orderId, this.this$0, this.$onResult, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x003c  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0047  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0076 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:20:0x0077  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r7) {
        /*
            r6 = this;
            java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r1 = r6.label
            switch(r1) {
                case 0: goto L1e;
                case 1: goto L19;
                case 2: goto L11;
                default: goto L9;
            }
        L9:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "call to 'resume' before 'invoke' with coroutine"
            r0.<init>(r1)
            throw r0
        L11:
            java.lang.Object r0 = r6.L$0
            java.lang.String r0 = (java.lang.String) r0
            kotlin.ResultKt.throwOnFailure(r7)
            goto L78
        L19:
            kotlin.ResultKt.throwOnFailure(r7)
            r1 = r7
            goto L32
        L1e:
            kotlin.ResultKt.throwOnFailure(r7)
            com.example.utils.ChapChapPayApi r1 = com.example.utils.ChapChapPayApi.INSTANCE
            java.lang.String r2 = r6.$orderId
            r3 = r6
            kotlin.coroutines.Continuation r3 = (kotlin.coroutines.Continuation) r3
            r4 = 1
            r6.label = r4
            java.lang.Object r1 = r1.checkOrderStatus(r2, r3)
            if (r1 != r0) goto L32
            return r0
        L32:
            java.lang.String r1 = (java.lang.String) r1
            java.lang.String r2 = "SUCCESS"
            boolean r2 = kotlin.jvm.internal.Intrinsics.areEqual(r1, r2)
            if (r2 == 0) goto L47
            com.example.ui.SchoolViewModel r2 = r6.this$0
            r2.activateSubscription()
            com.example.ui.SchoolViewModel r2 = r6.this$0
            r2.clearPendingOrderId()
            goto L54
        L47:
            java.lang.String r2 = "FAILED"
            boolean r2 = kotlin.jvm.internal.Intrinsics.areEqual(r1, r2)
            if (r2 == 0) goto L54
            com.example.ui.SchoolViewModel r2 = r6.this$0
            r2.clearPendingOrderId()
        L54:
            kotlinx.coroutines.MainCoroutineDispatcher r2 = kotlinx.coroutines.Dispatchers.getMain()
            kotlin.coroutines.CoroutineContext r2 = (kotlin.coroutines.CoroutineContext) r2
            com.example.ui.SchoolViewModel$checkPendingPaymentStatus$1$1 r3 = new com.example.ui.SchoolViewModel$checkPendingPaymentStatus$1$1
            kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> r4 = r6.$onResult
            r5 = 0
            r3.<init>(r4, r1, r5)
            kotlin.jvm.functions.Function2 r3 = (kotlin.jvm.functions.Function2) r3
            r4 = r6
            kotlin.coroutines.Continuation r4 = (kotlin.coroutines.Continuation) r4
            java.lang.Object r5 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r1)
            r6.L$0 = r5
            r5 = 2
            r6.label = r5
            java.lang.Object r2 = kotlinx.coroutines.BuildersKt.withContext(r2, r3, r4)
            if (r2 != r0) goto L77
            return r0
        L77:
            r0 = r1
        L78:
            kotlin.Unit r1 = kotlin.Unit.INSTANCE
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$checkPendingPaymentStatus$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* compiled from: SchoolViewModel.kt */
    @Metadata(d1 = {"\u0000\n\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\n"}, d2 = {"<anonymous>", "", "Lkotlinx/coroutines/CoroutineScope;"}, k = 3, mv = {2, 2, 0}, xi = 48)
    @DebugMetadata(c = "com.example.ui.SchoolViewModel$checkPendingPaymentStatus$1$1", f = "SchoolViewModel.kt", i = {}, l = {}, m = "invokeSuspend", n = {}, s = {})
    /* renamed from: com.example.ui.SchoolViewModel$checkPendingPaymentStatus$1$1  reason: invalid class name */
    /* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$checkPendingPaymentStatus$1$1.dex */
    public static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ Function1<String, Unit> $onResult;
        final /* synthetic */ String $status;
        int label;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        /* JADX WARN: Multi-variable type inference failed */
        AnonymousClass1(Function1<? super String, Unit> function1, String str, Continuation<? super AnonymousClass1> continuation) {
            super(2, continuation);
            this.$onResult = function1;
            this.$status = str;
        }

        public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
            return new AnonymousClass1(this.$onResult, this.$status, continuation);
        }

        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
        }

        public final Object invokeSuspend(Object $result) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
                case 0:
                    ResultKt.throwOnFailure($result);
                    Function1<String, Unit> function1 = this.$onResult;
                    if (function1 != null) {
                        function1.invoke(this.$status);
                    }
                    return Unit.INSTANCE;
                default:
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
        }
    }
}
