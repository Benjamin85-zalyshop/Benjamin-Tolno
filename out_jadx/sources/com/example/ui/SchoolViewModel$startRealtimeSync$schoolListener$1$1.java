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
@DebugMetadata(c = "com.example.ui.SchoolViewModel$startRealtimeSync$schoolListener$1$1", f = "SchoolViewModel.kt", i = {1, 1}, l = {706, 719}, m = "invokeSuspend", n = {"currentLocalAcc", "updated"}, s = {"L$0", "L$1"})
@SourceDebugExtension({"SMAP\nSchoolViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 SchoolViewModel.kt\ncom/example/ui/SchoolViewModel$startRealtimeSync$schoolListener$1$1\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,2219:1\n1#2:2220\n*E\n"})
/* loaded from: /app/applet/app/build/intermediates/project_dex_archive/debug/dexBuilderDebug/out/com/example/ui/SchoolViewModel$startRealtimeSync$schoolListener$1$1.dex */
public final class SchoolViewModel$startRealtimeSync$schoolListener$1$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Long $createdAt;
    final /* synthetic */ String $displayName;
    final /* synthetic */ String $email;
    final /* synthetic */ boolean $hasActiveSubscription;
    final /* synthetic */ boolean $isPendingValidation;
    final /* synthetic */ String $paymentPhoneNumber;
    final /* synthetic */ String $rejectionReason;
    final /* synthetic */ String $schoolName;
    final /* synthetic */ long $subscriptionExpiryDate;
    final /* synthetic */ String $transactionId;
    Object L$0;
    Object L$1;
    int label;
    final /* synthetic */ SchoolViewModel this$0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public SchoolViewModel$startRealtimeSync$schoolListener$1$1(SchoolViewModel schoolViewModel, String str, Long l, String str2, boolean z, boolean z2, String str3, String str4, String str5, String str6, long j, Continuation<? super SchoolViewModel$startRealtimeSync$schoolListener$1$1> continuation) {
        super(2, continuation);
        this.this$0 = schoolViewModel;
        this.$email = str;
        this.$createdAt = l;
        this.$schoolName = str2;
        this.$hasActiveSubscription = z;
        this.$isPendingValidation = z2;
        this.$paymentPhoneNumber = str3;
        this.$transactionId = str4;
        this.$displayName = str5;
        this.$rejectionReason = str6;
        this.$subscriptionExpiryDate = j;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        return new SchoolViewModel$startRealtimeSync$schoolListener$1$1(this.this$0, this.$email, this.$createdAt, this.$schoolName, this.$hasActiveSubscription, this.$isPendingValidation, this.$paymentPhoneNumber, this.$transactionId, this.$displayName, this.$rejectionReason, this.$subscriptionExpiryDate, continuation);
    }

    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return create(coroutineScope, continuation).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:24:0x00ae  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x00b1  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r24) {
        /*
            r23 = this;
            r0 = r23
            java.lang.Object r1 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r2 = r0.label
            r3 = 1
            switch(r2) {
                case 0: goto L27;
                case 1: goto L21;
                case 2: goto L14;
                default: goto Lc;
            }
        Lc:
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException
            java.lang.String r2 = "call to 'resume' before 'invoke' with coroutine"
            r1.<init>(r2)
            throw r1
        L14:
            java.lang.Object r1 = r0.L$1
            com.example.data.models.SchoolAccount r1 = (com.example.data.models.SchoolAccount) r1
            java.lang.Object r2 = r0.L$0
            com.example.data.models.SchoolAccount r2 = (com.example.data.models.SchoolAccount) r2
            kotlin.ResultKt.throwOnFailure(r24)
            goto L92
        L21:
            kotlin.ResultKt.throwOnFailure(r24)
            r2 = r24
            goto L3e
        L27:
            kotlin.ResultKt.throwOnFailure(r24)
            com.example.ui.SchoolViewModel r2 = r0.this$0
            com.example.data.repository.SchoolRepository r2 = com.example.ui.SchoolViewModel.access$getRepository$p(r2)
            java.lang.String r4 = r0.$email
            r5 = r0
            kotlin.coroutines.Continuation r5 = (kotlin.coroutines.Continuation) r5
            r0.label = r3
            java.lang.Object r2 = r2.getSchoolAccountByName(r4, r5)
            if (r2 != r1) goto L3e
            return r1
        L3e:
            com.example.data.models.SchoolAccount r2 = (com.example.data.models.SchoolAccount) r2
            if (r2 == 0) goto Lb6
            java.lang.Long r4 = r0.$createdAt
            if (r4 == 0) goto L4b
            long r4 = r4.longValue()
            goto L4f
        L4b:
            long r4 = r2.getCreatedAt()
        L4f:
            r15 = r4
            java.lang.String r6 = r0.$schoolName
            boolean r9 = r0.$hasActiveSubscription
            boolean r10 = r0.$isPendingValidation
            java.lang.String r11 = r0.$paymentPhoneNumber
            java.lang.String r12 = r0.$transactionId
            java.lang.String r13 = r0.$displayName
            java.lang.String r14 = r0.$rejectionReason
            long r4 = r0.$subscriptionExpiryDate
            r19 = r4
            r21 = 6157(0x180d, float:8.628E-42)
            r22 = 0
            r5 = 0
            r7 = 0
            r8 = 0
            r17 = 0
            r18 = 0
            r4 = r2
            com.example.data.models.SchoolAccount r4 = com.example.data.models.SchoolAccount.copy$default(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r17, r18, r19, r21, r22)
            com.example.ui.SchoolViewModel r5 = r0.this$0
            com.example.data.repository.SchoolRepository r5 = com.example.ui.SchoolViewModel.access$getRepository$p(r5)
            r6 = r0
            kotlin.coroutines.Continuation r6 = (kotlin.coroutines.Continuation) r6
            java.lang.Object r7 = kotlin.coroutines.jvm.internal.SpillingKt.nullOutSpilledVariable(r2)
            r0.L$0 = r7
            r0.L$1 = r4
            r7 = 2
            r0.label = r7
            java.lang.Object r5 = r5.insertSchoolAccountDirect(r4, r6)
            if (r5 != r1) goto L91
            return r1
        L91:
            r1 = r4
        L92:
            com.example.ui.SchoolViewModel r4 = r0.this$0
            kotlinx.coroutines.flow.MutableStateFlow r4 = com.example.ui.SchoolViewModel.access$get_schoolAccount$p(r4)
            r4.setValue(r1)
            com.example.ui.SchoolViewModel r4 = r0.this$0
            kotlinx.coroutines.flow.MutableStateFlow r4 = com.example.ui.SchoolViewModel.access$get_schoolName$p(r4)
            java.lang.String r5 = r0.$displayName
            java.lang.CharSequence r5 = (java.lang.CharSequence) r5
            java.lang.String r6 = r0.$schoolName
            int r7 = r5.length()
            if (r7 != 0) goto Lae
            goto Laf
        Lae:
            r3 = 0
        Laf:
            if (r3 == 0) goto Lb3
            r3 = 0
            r5 = r6
        Lb3:
            r4.setValue(r5)
        Lb6:
            kotlin.Unit r1 = kotlin.Unit.INSTANCE
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.example.ui.SchoolViewModel$startRealtimeSync$schoolListener$1$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
